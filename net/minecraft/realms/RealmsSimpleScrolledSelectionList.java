package net.minecraft.realms;

import net.minecraft.client.gui.GuiSimpleScrolledSelectionListProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsSimpleScrolledSelectionList {
   private final GuiSimpleScrolledSelectionListProxy proxy;

   public RealmsSimpleScrolledSelectionList(int var1, int var2, int var3, int var4, int var5) {
      this.proxy = new GuiSimpleScrolledSelectionListProxy(this, width, height, top, bottom, slotHeight);
   }

   public void render(int var1, int var2, float var3) {
      this.proxy.drawScreen(p_render_1_, p_render_2_, p_render_3_);
   }

   public int width() {
      return this.proxy.getWidth();
   }

   public int ym() {
      return this.proxy.getMouseY();
   }

   public int xm() {
      return this.proxy.getMouseX();
   }

   protected void renderItem(int var1, int var2, int var3, int var4, Tezzelator var5, int var6, int var7) {
   }

   public void renderItem(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.renderItem(p_renderItem_1_, p_renderItem_2_, p_renderItem_3_, p_renderItem_4_, Tezzelator.instance, p_renderItem_5_, p_renderItem_6_);
   }

   public int getItemCount() {
      return 0;
   }

   public void selectItem(int var1, boolean var2, int var3, int var4) {
   }

   public boolean isSelectedItem(int var1) {
      return false;
   }

   public void renderBackground() {
   }

   public int getMaxPosition() {
      return 0;
   }

   public int getScrollbarPosition() {
      return this.proxy.getWidth() / 2 + 124;
   }

   public void mouseEvent() {
      this.proxy.handleMouseInput();
   }

   public void scroll(int var1) {
      this.proxy.scrollBy(p_scroll_1_);
   }

   public int getScroll() {
      return this.proxy.getAmountScrolled();
   }

   protected void renderList(int var1, int var2, int var3, int var4) {
   }
}
