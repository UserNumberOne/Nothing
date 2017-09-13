package net.minecraft.item;

public class ItemBook extends Item {
   public boolean isEnchantable(ItemStack stack) {
      return stack.stackSize == 1;
   }

   public int getItemEnchantability() {
      return 1;
   }
}
