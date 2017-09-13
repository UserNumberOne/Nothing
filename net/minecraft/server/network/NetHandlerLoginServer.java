package net.minecraft.server.network;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
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
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.CryptManager;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;

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
   public String hostname = "";

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
            this.server.getPlayerList().initializeConnectionToPlayer(this.networkManager, this.player);
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

      EntityPlayerMP var1 = this.server.getPlayerList().attemptLogin(this, this.loginGameProfile, this.hostname);
      if (var1 != null) {
         this.currentLoginState = NetHandlerLoginServer.LoginState.ACCEPTED;
         if (this.server.aF() >= 0 && !this.networkManager.isLocalChannel()) {
            this.networkManager.sendPacket(new SPacketEnableCompression(this.server.aF()), new ChannelFutureListener() {
               public void operationComplete(ChannelFuture param1) throws Exception {
                  // $FF: Couldn't be decompiled
               }

               public void operationComplete(ChannelFuture param1) throws Exception {
                  // $FF: Couldn't be decompiled
               }
            });
         }

         this.networkManager.sendPacket(new SPacketLoginSuccess(this.loginGameProfile));
         EntityPlayerMP var2 = this.server.getPlayerList().getPlayerByUUID(this.loginGameProfile.getId());
         if (var2 != null) {
            this.currentLoginState = NetHandlerLoginServer.LoginState.DELAY_ACCEPT;
            this.player = this.server.getPlayerList().processLogin(this.loginGameProfile, var1);
         } else {
            this.server.getPlayerList().initializeConnectionToPlayer(this.networkManager, this.server.getPlayerList().processLogin(this.loginGameProfile, var1));
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
      if (this.server.getOnlineMode() && !this.networkManager.isLocalChannel()) {
         this.currentLoginState = NetHandlerLoginServer.LoginState.KEY;
         this.networkManager.sendPacket(new SPacketEncryptionRequest("", this.server.O().getPublic(), this.verifyToken));
      } else {
         this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
      }

   }

   public void processEncryptionResponse(CPacketEncryptionResponse var1) {
      Validate.validState(this.currentLoginState == NetHandlerLoginServer.LoginState.KEY, "Unexpected key packet", new Object[0]);
      PrivateKey var2 = this.server.O().getPrivate();
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
                  String var2 = (new BigInteger(CryptManager.getServerIdHash("", NetHandlerLoginServer.this.server.O().getPublic(), NetHandlerLoginServer.this.secretKey))).toString(16);
                  NetHandlerLoginServer.this.loginGameProfile = NetHandlerLoginServer.this.server.ay().hasJoinedServer(new GameProfile((UUID)null, var1.getName()), var2);
                  if (NetHandlerLoginServer.this.loginGameProfile != null) {
                     if (!NetHandlerLoginServer.this.networkManager.isChannelOpen()) {
                        return;
                     }

                     String var3 = NetHandlerLoginServer.this.loginGameProfile.getName();
                     InetAddress var4 = ((InetSocketAddress)NetHandlerLoginServer.this.networkManager.getRemoteAddress()).getAddress();
                     UUID var5 = NetHandlerLoginServer.this.loginGameProfile.getId();
                     final CraftServer var6 = NetHandlerLoginServer.this.server.server;
                     AsyncPlayerPreLoginEvent var7 = new AsyncPlayerPreLoginEvent(var3, var4, var5);
                     var6.getPluginManager().callEvent(var7);
                     if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
                        final PlayerPreLoginEvent var8 = new PlayerPreLoginEvent(var3, var4, var5);
                        if (var7.getResult() != Result.ALLOWED) {
                           var8.disallow(var7.getResult(), var7.getKickMessage());
                        }

                        Waitable var9 = new Waitable() {
                           protected Result evaluate() {
                              var6.getPluginManager().callEvent(var8);
                              return var8.getResult();
                           }
                        };
                        NetHandlerLoginServer.this.server.processQueue.add(var9);
                        if (var9.get() != Result.ALLOWED) {
                           NetHandlerLoginServer.this.closeConnection(var8.getKickMessage());
                           return;
                        }
                     } else if (var7.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                        NetHandlerLoginServer.this.closeConnection(var7.getKickMessage());
                        return;
                     }

                     NetHandlerLoginServer.LOGGER.info("UUID of player {} is {}", new Object[]{NetHandlerLoginServer.this.loginGameProfile.getName(), NetHandlerLoginServer.this.loginGameProfile.getId()});
                     NetHandlerLoginServer.this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                  } else if (NetHandlerLoginServer.this.server.R()) {
                     NetHandlerLoginServer.LOGGER.warn("Failed to verify username but will let them in anyway!");
                     NetHandlerLoginServer.this.loginGameProfile = NetHandlerLoginServer.this.getOfflineProfile(var1);
                     NetHandlerLoginServer.this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                  } else {
                     NetHandlerLoginServer.this.closeConnection("Failed to verify username!");
                     NetHandlerLoginServer.LOGGER.error("Username '{}' tried to join with an invalid session", new Object[]{var1.getName()});
                  }
               } catch (AuthenticationUnavailableException var10) {
                  if (NetHandlerLoginServer.this.server.R()) {
                     NetHandlerLoginServer.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                     NetHandlerLoginServer.this.loginGameProfile = NetHandlerLoginServer.this.getOfflineProfile(var1);
                     NetHandlerLoginServer.this.currentLoginState = NetHandlerLoginServer.LoginState.READY_TO_ACCEPT;
                  } else {
                     NetHandlerLoginServer.this.closeConnection("Authentication servers are down. Please try again later, sorry!");
                     NetHandlerLoginServer.LOGGER.error("Couldn't verify username because servers are unavailable");
                  }
               } catch (Exception var11) {
                  NetHandlerLoginServer.this.closeConnection("Failed to verify username!");
                  NetHandlerLoginServer.this.server.server.getLogger().log(Level.WARNING, "Exception verifying " + var1.getName(), var11);
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
