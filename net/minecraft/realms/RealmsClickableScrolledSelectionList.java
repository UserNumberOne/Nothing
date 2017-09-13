package net.minecraft.realms;

import net.minecraft.client.gui.GuiClickableScrolledSelectionListProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsClickableScrolledSelectionList {
   private final GuiClickableScrolledSelectionListProxy proxy;

   public RealmsClickableScrolledSelectionList(int var1, int var2, int var3, int var4, int var5) {
      this.proxy = new GuiClickableScrolledSelectionListProxy(this, p_i46052_1_, p_i46052_2_, p_i46052_3_, p_i46052_4_, p_i46052_5_);
   }

   public void render(int var1, int var2, float var3) {
      this.proxy.drawScreen(p_render_1_, p_render_2_, p_render_3_);
   }

   public int width() {
      return this.proxy.width();
   }

   public int ym() {
      return this.proxy.mouseY();
   }

   public int xm() {
      return this.proxy.mouseX();
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
      return this.proxy.width() / 2 + 124;
   }

   public void mouseEvent() {
      this.proxy.handleMouseInput();
   }

   public void customMouseEvent(int var1, int var2, int var3, float var4, int var5) {
   }

   public void scroll(int var1) {
      this.proxy.scrollBy(p_scroll_1_);
   }

   public int getScroll() {
      return this.proxy.getAmountScrolled();
   }

   protected void renderList(int var1, int var2, int var3, int var4) {
   }

   public void itemClicked(int var1, int var2, int var3, int var4, int var5) {
   }

   public void renderSelected(int var1, int var2, int var3, Tezzelator var4) {
   }

   public void setLeftPos(int var1) {
      this.proxy.setSlotXBoundsFromLeft(p_setLeftPos_1_);
   }
}
