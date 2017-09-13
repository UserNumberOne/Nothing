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
      super(Minecraft.getMinecraft(), p_i45526_2_, p_i45526_3_, p_i45526_4_, p_i45526_5_, p_i45526_6_);
      this.proxy = selectionList;
   }

   protected int getSize() {
      return this.proxy.getItemCount();
   }

   protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      this.proxy.selectItem(slotIndex, isDoubleClick, mouseX, mouseY);
   }

   protected boolean isSelected(int var1) {
      return this.proxy.isSelectedItem(slotIndex);
   }

   protected void drawBackground() {
      this.proxy.renderBackground();
   }

   protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.proxy.renderItem(entryID, insideLeft, yPos, insideSlotHeight, mouseXIn, mouseYIn);
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
      this.proxy.renderSelected(p_178043_1_, p_178043_2_, p_178043_3_, p_178043_4_);
   }

   protected void drawSelectionBox(int var1, int var2, int var3, int var4) {
      int i = this.getSize();

      for(int j = 0; j < i; ++j) {
         int k = insideTop + j * this.slotHeight + this.headerPadding;
         int l = this.slotHeight - 4;
         if (k > this.bottom || k + l < this.top) {
            this.updateItemPos(j, insideLeft, k);
         }

         if (this.showSelectionBox && this.isSelected(j)) {
            this.renderSelected(this.width, k, l, Tezzelator.instance);
         }

         this.drawSlot(j, insideLeft, k, l, mouseXIn, mouseYIn);
      }

   }
}
