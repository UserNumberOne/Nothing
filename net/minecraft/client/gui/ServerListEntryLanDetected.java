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
      this.screen = p_i47141_1_;
      this.serverData = p_i47141_2_;
      this.mc = Minecraft.getMinecraft();
   }

   public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      this.mc.fontRendererObj.drawString(I18n.format("lanServer.title"), x + 32 + 3, y + 1, 16777215);
      this.mc.fontRendererObj.drawString(this.serverData.getServerMotd(), x + 32 + 3, y + 12, 8421504);
      if (this.mc.gameSettings.hideServerAddress) {
         this.mc.fontRendererObj.drawString(I18n.format("selectServer.hiddenAddress"), x + 32 + 3, y + 12 + 11, 3158064);
      } else {
         this.mc.fontRendererObj.drawString(this.serverData.getServerIpPort(), x + 32 + 3, y + 12 + 11, 3158064);
      }

   }

   public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.screen.selectServer(slotIndex);
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
