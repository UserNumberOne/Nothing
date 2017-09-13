package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.math.BigInteger;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.network.login.server.SPacketEnableCompression;
import net.minecraft.network.login.server.SPacketEncryptionRequest;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.util.CryptManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class NetHandlerLoginClient implements INetHandlerLoginClient {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft mc;
   private final GuiScreen previousGuiScreen;
   private final NetworkManager networkManager;
   private GameProfile gameProfile;

   public NetHandlerLoginClient(NetworkManager var1, Minecraft var2, GuiScreen var3) {
      this.networkManager = var1;
      this.mc = var2;
      this.previousGuiScreen = var3;
   }

   public void handleEncryptionRequest(SPacketEncryptionRequest var1) {
      final SecretKey var2 = CryptManager.createNewSharedKey();
      String var3 = var1.getServerId();
      PublicKey var4 = var1.getPublicKey();
      String var5 = (new BigInteger(CryptManager.getServerIdHash(var3, var4, var2))).toString(16);
      if (this.mc.getCurrentServerData() != null && this.mc.getCurrentServerData().isOnLAN()) {
         try {
            this.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), var5);
         } catch (AuthenticationException var10) {
            LOGGER.warn("Couldn't connect to auth servers but will continue to join LAN");
         }
      } else {
         try {
            this.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), var5);
         } catch (AuthenticationUnavailableException var7) {
            this.networkManager.closeChannel(new TextComponentTranslation("disconnect.loginFailedInfo", new Object[]{new TextComponentTranslation("disconnect.loginFailedInfo.serversUnavailable", new Object[0])}));
            return;
         } catch (InvalidCredentialsException var8) {
            this.networkManager.closeChannel(new TextComponentTranslation("disconnect.loginFailedInfo", new Object[]{new TextComponentTranslation("disconnect.loginFailedInfo.invalidSession", new Object[0])}));
            return;
         } catch (AuthenticationException var9) {
            this.networkManager.closeChannel(new TextComponentTranslation("disconnect.loginFailedInfo", new Object[]{var9.getMessage()}));
            return;
         }
      }

      this.networkManager.sendPacket(new CPacketEncryptionResponse(var2, var4, var1.getVerifyToken()), new GenericFutureListener() {
         public void operationComplete(Future var1) throws Exception {
            NetHandlerLoginClient.this.networkManager.enableEncryption(var2);
         }
      });
   }

   private MinecraftSessionService getSessionService() {
      return this.mc.getSessionService();
   }

   public void handleLoginSuccess(SPacketLoginSuccess var1) {
      this.gameProfile = var1.getProfile();
      this.networkManager.setConnectionState(EnumConnectionState.PLAY);
      FMLNetworkHandler.fmlClientHandshake(this.networkManager);
      NetHandlerPlayClient var2 = new NetHandlerPlayClient(this.mc, this.previousGuiScreen, this.networkManager, this.gameProfile);
      this.networkManager.setNetHandler(var2);
      FMLClientHandler.instance().setPlayClient(var2);
   }

   public void onDisconnect(ITextComponent var1) {
      if (this.previousGuiScreen != null && this.previousGuiScreen instanceof GuiScreenRealmsProxy) {
         this.mc.displayGuiScreen((new DisconnectedRealmsScreen(((GuiScreenRealmsProxy)this.previousGuiScreen).getProxy(), "connect.failed", var1)).getProxy());
      } else {
         this.mc.displayGuiScreen(new GuiDisconnected(this.previousGuiScreen, "connect.failed", var1));
      }

   }

   public void handleDisconnect(SPacketDisconnect var1) {
      this.networkManager.closeChannel(var1.getReason());
   }

   public void handleEnableCompression(SPacketEnableCompression var1) {
      if (!this.networkManager.isLocalChannel()) {
         this.networkManager.setCompressionThreshold(var1.getCompressionThreshold());
      }

   }
}
