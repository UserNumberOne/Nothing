package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiListExtended extends GuiSlot {
   public GuiListExtended(Minecraft var1, int var2, int var3, int var4, int var5, int var6) {
      super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
   }

   protected void elementClicked(int var1, boolean var2, int var3, int var4) {
   }

   protected boolean isSelected(int var1) {
      return false;
   }

   protected void drawBackground() {
   }

   protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.getListEntry(entryID).drawEntry(entryID, insideLeft, yPos, this.getListWidth(), insideSlotHeight, mouseXIn, mouseYIn, this.isMouseYWithinSlotBounds(mouseYIn) && this.getSlotIndexFromScreenCoords(mouseXIn, mouseYIn) == entryID);
   }

   protected void updateItemPos(int var1, int var2, int var3) {
      this.getListEntry(entryID).setSelected(entryID, insideLeft, yPos);
   }

   public boolean mouseClicked(int var1, int var2, int var3) {
      if (this.isMouseYWithinSlotBounds(mouseY)) {
         int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
         if (i >= 0) {
            int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int k = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
            int l = mouseX - j;
            int i1 = mouseY - k;
            if (this.getListEntry(i).mousePressed(i, mouseX, mouseY, mouseEvent, l, i1)) {
               this.setEnabled(false);
               return true;
            }
         }
      }

      return false;
   }

   public boolean mouseReleased(int var1, int var2, int var3) {
      for(int i = 0; i < this.getSize(); ++i) {
         int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
         int k = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
         int l = p_148181_1_ - j;
         int i1 = p_148181_2_ - k;
         this.getListEntry(i).mouseReleased(i, p_148181_1_, p_148181_2_, p_148181_3_, l, i1);
      }

      this.setEnabled(true);
      return false;
   }

   public abstract GuiListExtended.IGuiListEntry getListEntry(int var1);

   @SideOnly(Side.CLIENT)
   public interface IGuiListEntry {
      void setSelected(int var1, int var2, int var3);

      void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8);

      boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6);

      void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6);
   }
}
