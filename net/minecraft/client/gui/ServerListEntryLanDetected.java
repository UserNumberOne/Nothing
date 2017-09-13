package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ServerListEntryLanDetected implements GuiListExtended.IGuiListEntry {
   private final GuiMultiplayer screen;
   protected final Minecraft mc;
   protected final LanServerInfo serverData;
   private long lastClickTime;

   protected ServerListEntryLanDetected(GuiMultiplayer var1, LanServerInfo var2) {
      this.screen = var1;
      this.serverData = var2;
      this.mc = Minecraft.getMinecraft();
   }

   public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      this.mc.fontRendererObj.drawString(I18n.format("lanServer.title"), var2 + 32 + 3, var3 + 1, 16777215);
      this.mc.fontRendererObj.drawString(this.serverData.getServerMotd(), var2 + 32 + 3, var3 + 12, 8421504);
      if (this.mc.gameSettings.hideServerAddress) {
         this.mc.fontRendererObj.drawString(I18n.format("selectServer.hiddenAddress"), var2 + 32 + 3, var3 + 12 + 11, 3158064);
      } else {
         this.mc.fontRendererObj.drawString(this.serverData.getServerIpPort(), var2 + 32 + 3, var3 + 12 + 11, 3158064);
      }

   }

   public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.screen.selectServer(var1);
      if (Minecraft.getSystemTime() - this.lastClickTime < 250L) {
         this.screen.connectToSelected();
      }

      this.lastClickTime = Minecraft.getSystemTime();
      return false;
   }

   public void setSelected(int var1, int var2, int var3) {
   }

   public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   public LanServerInfo getServerData() {
      return this.serverData;
   }
}
