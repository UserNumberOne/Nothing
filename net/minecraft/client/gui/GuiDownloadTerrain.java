package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDownloadTerrain extends GuiScreen {
   private final NetHandlerPlayClient connection;
   private int progress;

   public GuiDownloadTerrain(NetHandlerPlayClient var1) {
      this.connection = netHandler;
   }

   protected void keyTyped(char var1, int var2) throws IOException {
   }

   public void initGui() {
      this.buttonList.clear();
   }

   public void updateScreen() {
      ++this.progress;
      if (this.progress % 20 == 0) {
         this.connection.sendPacket(new CPacketKeepAlive());
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawBackground(0);
      this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingTerrain"), this.width / 2, this.height / 2 - 50, 16777215);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }
}
