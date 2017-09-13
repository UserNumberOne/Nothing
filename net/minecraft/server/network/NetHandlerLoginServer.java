package net.minecraft.server.network;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.SecretKey;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginServer;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.network.login.server.SPacketEnableCompression;
import net.minecraft.network.login.server.SPacketEncryptionRequest;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.CryptManager;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerLoginServer implements INetHandlerLoginServer, ITickable {
   private static final AtomicInteger AUTHENTICATOR_THREAD_ID = new AtomicInteger(0);
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Random RANDOM = new Random();
   private final byte[] verifyToken = new byte[4];
   private final MinecraftServer server;
   public final NetworkManager networkManager;
   private NetHandlerLoginServer.LoginState currentLoginState = NetHandlerLoginServer.LoginState.HELLO;
   private int connectionTimer;
   private GameProfile loginGameProfile;
   private final String serverId = "";
   private SecretKey secretKey;
   private EntityPlayerMP player;

   public NetHandlerLoginServer(MinecraftServer var1, NetworkManager var2) {
      this.server = var1;
      this.networkManager = var2;
      RANDOM.nextBytes(this.verifyToken);
   }

   public void update() {
      if (this.currentLoginState == NetHandlerLoginServer.LoginState.READY_TO_ACCEPT) {
         this.tryAcceptPlayer();
      } else if (this.currentLoginState == NetHandlerLoginServer.LoginState.DELAY_ACCEPT) {
         EntityPlayerMP var1 = this.server.getPlayerList().getPlayerByUUID(this.loginGameProfile.getId());
         if (var1 == null) {
            this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
            FMLNetworkHandler.fmlServerHandshake(this.server.getPlayerList(), this.networkManager, this.player);
            this.player = null;
         }
      }

      if (this.connectionTimer++ == 600) {
         this.closeConnection("Took too long to log in");
      }

   }

   public void closeConnection(String var1) {
      try {
         LOGGER.info("Disconnecting {}: {}", new Object[]{this.getConnectionInfo(), var1});
         TextComponentString var2 = new TextComponentString(var1);
         this.networkManager.sendPacket(new SPacketDisconnect(var2));
         this.networkManager.closeChannel(var2);
      } catch (Exception var3) {
         LOGGER.error("Error whilst disconnecting player", var3);
      }

   }

   public void tryAcceptPlayer() {
      if (!this.loginGameProfile.isComplete()) {
         this.loginGameProfile = this.getOfflineProfile(this.loginGameProfile);
      }

      String var1 = this.server.getPlayerList().allowUserToConnect(this.networkManager.getRemoteAddress(), this.loginGameProfile);
      if (var1 != null) {
         this.closeConnection(var1);
      } else {
         this.currentLoginState = NetHandlerLoginServer.LoginState.ACCEPTED;
         if (this.server.getNetworkCompressionThreshold() >= 0 && !this.networkManager.isLocalChannel()) {
            this.networkManager.sendPacket(new SPacketEnableCompression(this.server.getNetworkCompressionThreshold()), new ChannelFutureListener() {
               public void operationComplete(ChannelFuture var1) throws Exception {
                  NetHandlerLoginServer.this.networkManager.setCompressionThreshold(NetHandlerLoginServer.this.server.getNetworkCompressionThreshold());
               }
            });
         }

         this.networkManager.sendPacket(new SPacketLoginSuccess(this.loginGameProfile));
         EntityPlayerMP var2 = this.server.getPlayerList().getPlayerByUUID(this.loginGameProfile.getId());
         if (var2 != null) {
            this.currentLoginState = NetHandlerLoginServer.LoginState.DELAY_ACCEPT;
            this.player = this.server.getPlayerList().createPlayerForUser(this.loginGameProfile);
         } else {
            FMLNetworkHandler.fmlServerHandshake(this.server.getPlayerList(), this.networkManager, this.server.getPlayerList().createPlayerForUser(this.loginGameProfile));
         }
      }

   }

   public void onDisconnect(ITextComponent var1) {
      LOGGER.info("{} lost connection: {}", new Object[]{this.getConnectionInfo(), var1.getUnformattedText()});
   }

   public String getConnectionInfo() {
      return this.loginGameProfile != null ? this.loginGameProfile + " (" + this.networkManager.getRemoteAddress() + ")" : String.valueOf(this.networkManager.getRemoteAddress());
   }

   public void processLoginStart(CPacketLoginStart var1) {
      Validate.validState(this.currentLoginState == NetHandlerLoginServer.LoginState.HELLO, "Unexpected hello packet", new Object[0]);
      this.loginGameProfile = var1.getProfile();
      if (this.server.isServerInOnlineMode() && !this.networkManager.isLocalChannel()) {
         this.currentLoginState = NetHandlerLoginServer.LoginState.KEY;
         this.networkManager.sendPacket(new SPacketEncryptionRequest("", this.server.getKeyPair().getPublic(), this.verifyToken));
      } else {
         this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
      }

   }

   public void processEncryptionResponse(CPacketEncryptionResponse var1) {
      Validate.validState(this.currentLoginState == NetHandlerLoginServer.LoginState.KEY, "Unexpected key packet", new Object[0]);
      PrivateKey var2 = this.server.getKeyPair().getPrivate();
      if (!Arrays.equals(this.verifyToken, var1.getVerifyToken(var2))) {
         throw new IllegalStateException("Invalid nonce!");
      } else {
         this.secretKey = var1.getSecretKey(var2);
         this.currentLoginState = NetHandlerLoginServer.LoginState.AUTHENTICATING;
         this.networkManager.enableEncryption(this.secretKey);
         (new Thread("User Authenticator #" + AUTHENTICATOR_THREAD_ID.incrementAndGet()) {
            public void run() {
               GameProfile var1 = NetHandlerLoginServer.this.loginGameProfile;

               try {
                  String var2 = (new BigInteger(CryptManager.getServerIdHash("", NetHandlerLoginServer.this.server.getKeyPair().getPublic(), NetHandlerLoginServer.this.secretKey))).toString(16);
                  NetHandlerLoginServer.this.loginGameProfile = NetHandlerLoginServer.this.server.getMinecraftSessionService().hasJoinedServer(new GameProfile((UUID)null, var1.getName()), var2);
                  if (NetHandlerLoginServer.this.loginGameProfile != null) {
                     NetHandlerLoginServer.LOGGER.info("UUID of player {} is {}", new Object[]{NetHandlerLoginServer.this.loginGameProfile.getName(), NetHandlerLoginServer.this.loginGameProfile.getId()});
                     NetHandlerLoginServer.this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                  } else if (NetHandlerLoginServer.this.server.isSinglePlayer()) {
                     NetHandlerLoginServer.LOGGER.warn("Failed to verify username but will let them in anyway!");
                     NetHandlerLoginServer.this.loginGameProfile = NetHandlerLoginServer.this.getOfflineProfile(var1);
                     NetHandlerLoginServer.this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                  } else {
                     NetHandlerLoginServer.this.closeConnection("Failed to verify username!");
                     NetHandlerLoginServer.LOGGER.error("Username '{}' tried to join with an invalid session", new Object[]{var1.getName()});
                  }
               } catch (AuthenticationUnavailableException var3) {
                  if (NetHandlerLoginServer.this.server.isSinglePlayer()) {
                     NetHandlerLoginServer.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                     NetHandlerLoginServer.this.loginGameProfile = NetHandlerLoginServer.this.getOfflineProfile(var1);
                     NetHandlerLoginServer.this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                  } else {
                     NetHandlerLoginServer.this.closeConnection("Authentication servers are down. Please try again later, sorry!");
                     NetHandlerLoginServer.LOGGER.error("Couldn't verify username because servers are unavailable");
                  }
               }

            }
         }).start();
      }
   }

   protected GameProfile getOfflineProfile(GameProfile var1) {
      UUID var2 = UUID.nameUUIDFromBytes(("OfflinePlayer:" + var1.getName()).getBytes(Charsets.UTF_8));
      return new GameProfile(var2, var1.getName());
   }

   static enum LoginState {
      HELLO,
      KEY,
      AUTHENTICATING,
      READY_TO_ACCEPT,
      DELAY_ACCEPT,
      ACCEPTED;
   }
}
