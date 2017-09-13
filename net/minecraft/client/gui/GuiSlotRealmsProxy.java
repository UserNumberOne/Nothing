package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsScrolledSelectionList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSlotRealmsProxy extends GuiSlot {
   private final RealmsScrolledSelectionList selectionList;

   public GuiSlotRealmsProxy(RealmsScrolledSelectionList var1, int var2, int var3, int var4, int var5, int var6) {
      super(Minecraft.getMinecraft(), var2, var3, var4, var5, var6);
      this.selectionList = var1;
   }

   protected int getSize() {
      return this.selectionList.getItemCount();
   }

   protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      this.selectionList.selectItem(var1, var2, var3, var4);
   }

   protected boolean isSelected(int var1) {
      return this.selectionList.isSelectedItem(var1);
   }

   protected void drawBackground() {
      this.selectionList.renderBackground();
   }

   protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.selectionList.renderItem(var1, var2, var3, var4, var5, var6);
   }

   public int getWidth() {
      return super.width;
   }

   public int getMouseY() {
      return super.mouseY;
   }

   public int getMouseX() {
      return super.mouseX;
   }

   protected int getContentHeight() {
      return this.selectionList.getMaxPosition();
   }

   protected int getScrollBarX() {
      return this.selectionList.getScrollbarPosition();
   }

   public void handleMouseInput() {
      super.handleMouseInput();
   }
}
