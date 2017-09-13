package net.minecraft.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiResourcePackList extends GuiListExtended {
   protected final Minecraft mc;
   protected final List resourcePackEntries;

   public GuiResourcePackList(Minecraft var1, int var2, int var3, List var4) {
      super(var1, var2, var3, 32, var3 - 55 + 4, 36);
      this.mc = var1;
      this.resourcePackEntries = var4;
      this.centerListVertically = false;
      this.setHasListHeader(true, (int)((float)var1.fontRendererObj.FONT_HEIGHT * 1.5F));
   }

   protected void drawListHeader(int var1, int var2, Tessellator var3) {
      String var4 = TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + this.getListHeader();
      this.mc.fontRendererObj.drawString(var4, var1 + this.width / 2 - this.mc.fontRendererObj.getStringWidth(var4) / 2, Math.min(this.top + 3, var2), 16777215);
   }

   protected abstract String getListHeader();

   public List getList() {
      return this.resourcePackEntries;
   }

   protected int getSize() {
      return this.getList().size();
   }

   public ResourcePackListEntry getListEntry(int var1) {
      return (ResourcePackListEntry)this.getList().get(var1);
   }

   public int getListWidth() {
      return this.width;
   }

   protected int getScrollBarX() {
      return this.right - 6;
   }
}
