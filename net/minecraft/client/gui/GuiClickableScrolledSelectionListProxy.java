package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.Tezzelator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class GuiClickableScrolledSelectionListProxy extends GuiSlot {
   private final RealmsClickableScrolledSelectionList proxy;

   public GuiClickableScrolledSelectionListProxy(RealmsClickableScrolledSelectionList var1, int var2, int var3, int var4, int var5, int var6) {
      super(Minecraft.getMinecraft(), var2, var3, var4, var5, var6);
      this.proxy = var1;
   }

   protected int getSize() {
      return this.proxy.getItemCount();
   }

   protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      this.proxy.selectItem(var1, var2, var3, var4);
   }

   protected boolean isSelected(int var1) {
      return this.proxy.isSelectedItem(var1);
   }

   protected void drawBackground() {
      this.proxy.renderBackground();
   }

   protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.proxy.renderItem(var1, var2, var3, var4, var5, var6);
   }

   public int width() {
      return super.width;
   }

   public int mouseY() {
      return super.mouseY;
   }

   public int mouseX() {
      return super.mouseX;
   }

   protected int getContentHeight() {
      return this.proxy.getMaxPosition();
   }

   protected int getScrollBarX() {
      return this.proxy.getScrollbarPosition();
   }

   public void handleMouseInput() {
      super.handleMouseInput();
      if (this.scrollMultiplier > 0.0F && Mouse.getEventButtonState()) {
         this.proxy.customMouseEvent(this.top, this.bottom, this.headerPadding, this.amountScrolled, this.slotHeight);
      }

   }

   public void renderSelected(int var1, int var2, int var3, Tezzelator var4) {
      this.proxy.renderSelected(var1, var2, var3, var4);
   }

   protected void drawSelectionBox(int var1, int var2, int var3, int var4) {
      int var5 = this.getSize();

      for(int var6 = 0; var6 < var5; ++var6) {
         int var7 = var2 + var6 * this.slotHeight + this.headerPadding;
         int var8 = this.slotHeight - 4;
         if (var7 > this.bottom || var7 + var8 < this.top) {
            this.updateItemPos(var6, var1, var7);
         }

         if (this.showSelectionBox && this.isSelected(var6)) {
            this.renderSelected(this.width, var7, var8, Tezzelator.instance);
         }

         this.drawSlot(var6, var1, var7, var8, var3, var4);
      }

   }
}
