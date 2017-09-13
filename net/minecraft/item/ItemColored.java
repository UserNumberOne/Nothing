package net.minecraft.item;

import net.minecraft.block.Block;

public class ItemColored extends ItemBlock {
   private String[] subtypeNames;

   public ItemColored(Block var1, boolean var2) {
      super(block);
      if (hasSubtypes) {
         this.setMaxDamage(0);
         this.setHasSubtypes(true);
      }

   }

   public int getMetadata(int var1) {
      return damage;
   }

   public ItemColored setSubtypeNames(String[] var1) {
      this.subtypeNames = names;
      return this;
   }

   public String getUnlocalizedName(ItemStack var1) {
      if (this.subtypeNames == null) {
         return super.getUnlocalizedName(stack);
      } else {
         int i = stack.getMetadata();
         return i >= 0 && i < this.subtypeNames.length ? super.getUnlocalizedName(stack) + "." + this.subtypeNames[i] : super.getUnlocalizedName(stack);
      }
   }
}
