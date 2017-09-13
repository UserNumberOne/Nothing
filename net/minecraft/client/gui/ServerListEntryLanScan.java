package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ServerListEntryLanScan implements GuiListExtended.IGuiListEntry {
   private final Minecraft mc = Minecraft.getMinecraft();

   public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      int i = y + slotHeight / 2 - this.mc.fontRendererObj.FONT_HEIGHT / 2;
      this.mc.fontRendererObj.drawString(I18n.format("lanServer.scanning"), this.mc.currentScreen.width / 2 - this.mc.fontRendererObj.getStringWidth(I18n.format("lanServer.scanning")) / 2, i, 16777215);
      String s;
      switch((int)(Minecraft.getSystemTime() / 300L % 4L)) {
      case 0:
      default:
         s = "O o o";
         break;
      case 1:
      case 3:
         s = "o O o";
         break;
      case 2:
         s = "o o O";
      }

      this.mc.fontRendererObj.drawString(s, this.mc.currentScreen.width / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2, i + this.mc.fontRendererObj.FONT_HEIGHT, 8421504);
   }

   public void setSelected(int var1, int var2, int var3) {
   }

   public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
      return false;
   }

   public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
   }
}
