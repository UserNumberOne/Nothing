package net.minecraft.client.multiplayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiConnecting extends GuiScreen {
   private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
   private static final Logger LOGGER = LogManager.getLogger();
   private NetworkManager networkManager;
   private boolean cancel;
   private final GuiScreen previousGuiScreen;

   public GuiConnecting(GuiScreen var1, Minecraft var2, ServerData var3) {
      this.mc = var2;
      this.previousGuiScreen = var1;
      ServerAddress var4 = ServerAddress.fromString(var3.serverIP);
      var2.loadWorld((WorldClient)null);
      var2.setServerData(var3);
      this.connect(var4.getIP(), var4.getPort());
   }

   public GuiConnecting(GuiScreen var1, Minecraft var2, String var3, int var4) {
      this.mc = var2;
      this.previousGuiScreen = var1;
      var2.loadWorld((WorldClient)null);
      this.connect(var3, var4);
   }

   private void connect(final String var1, final int var2) {
      LOGGER.info("Connecting to {}, {}", new Object[]{var1, var2});
      (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
         public void run() {
            InetAddress var1x = null;

            try {
               if (GuiConnecting.this.cancel) {
                  return;
               }

               var1x = InetAddress.getByName(var1);
               GuiConnecting.this.networkManager = NetworkManager.createNetworkManagerAndConnect(var1x, var2, GuiConnecting.this.mc.gameSettings.isUsingNativeTransport());
               GuiConnecting.this.networkManager.setNetHandler(new NetHandlerLoginClient(GuiConnecting.this.networkManager, GuiConnecting.this.mc, GuiConnecting.this.previousGuiScreen));
               GuiConnecting.this.networkManager.sendPacket(new C00Handshake(210, var1, var2, EnumConnectionState.LOGIN, true));
               GuiConnecting.this.networkManager.sendPacket(new CPacketLoginStart(GuiConnecting.this.mc.getSession().getProfile()));
            } catch (UnknownHostException var5) {
               if (GuiConnecting.this.cancel) {
                  return;
               }

               GuiConnecting.LOGGER.error("Couldn't connect to server", var5);
               GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new TextComponentTranslation("disconnect.genericReason", new Object[]{"Unknown host"})));
            } catch (Exception var6) {
               if (GuiConnecting.this.cancel) {
                  return;
               }

               GuiConnecting.LOGGER.error("Couldn't connect to server", var6);
               String var3 = var6.toString();
               if (var1x != null) {
                  String var4 = var1x + ":" + var2;
                  var3 = var3.replaceAll(var4, "");
               }

               GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new TextComponentTranslation("disconnect.genericReason", new Object[]{var3})));
            }

         }
      }).start();
   }

   public void updateScreen() {
      if (this.networkManager != null) {
         if (this.networkManager.isChannelOpen()) {
            this.networkManager.processReceivedPackets();
         } else {
            this.networkManager.checkDisconnected();
         }
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
   }

   public void initGui() {
      this.buttonList.clear();
      this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel")));
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == 0) {
         this.cancel = true;
         if (this.networkManager != null) {
            this.networkManager.closeChannel(new TextComponentString("Aborted"));
         }

         this.mc.displayGuiScreen(this.previousGuiScreen);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      if (this.networkManager == null) {
         this.drawCenteredString(this.fontRendererObj, I18n.format("connect.connecting"), this.width / 2, this.height / 2 - 50, 16777215);
      } else {
         this.drawCenteredString(this.fontRendererObj, I18n.format("connect.authorizing"), this.width / 2, this.height / 2 - 50, 16777215);
      }

      super.drawScreen(var1, var2, var3);
   }
}
