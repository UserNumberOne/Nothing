package net.minecraft.item.crafting;

import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.world.World;

class RecipeTippedArrow extends ShapedRecipes implements IRecipe {
   private static final ItemStack[] EMPTY_ITEMS = new ItemStack[9];

   RecipeTippedArrow() {
      super(3, 3, new ItemStack[]{new ItemStack(Items.ARROW, 0), new ItemStack(Items.ARROW, 0), new ItemStack(Items.ARROW, 0), new ItemStack(Items.ARROW, 0), new ItemStack(Items.LINGERING_POTION, 0), new ItemStack(Items.ARROW, 0), new ItemStack(Items.ARROW, 0), new ItemStack(Items.ARROW, 0), new ItemStack(Items.ARROW, 0)}, new ItemStack(Items.TIPPED_ARROW, 8));
   }

   public boolean matches(InventoryCrafting inventorycrafting, World world) {
      if (inventorycrafting.getWidth() == 3 && inventorycrafting.getHeight() == 3) {
         for(int i = 0; i < inventorycrafting.getWidth(); ++i) {
            for(int j = 0; j < inventorycrafting.getHeight(); ++j) {
               ItemStack itemstack = inventorycrafting.getStackInRowAndColumn(i, j);
               if (itemstack == null) {
                  return false;
               }

               Item item = itemstack.getItem();
               if (i == 1 && j == 1) {
                  if (item != Items.LINGERING_POTION) {
                     return false;
                  }
               } else if (item != Items.ARROW) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
      ItemStack itemstack = inventorycrafting.getStackInRowAndColumn(1, 1);
      if (itemstack != null && itemstack.getItem() == Items.LINGERING_POTION) {
         ItemStack itemstack1 = new ItemStack(Items.TIPPED_ARROW, 8);
         PotionUtils.addPotionToItemStack(itemstack1, PotionUtils.getPotionFromItem(itemstack));
         PotionUtils.appendEffects(itemstack1, PotionUtils.getFullEffectsFromItem(itemstack));
         return itemstack1;
      } else {
         return null;
      }
   }

   public int getRecipeSize() {
      return 9;
   }

   @Nullable
   public ItemStack getRecipeOutput() {
      return null;
   }

   public ItemStack[] getRemainingItems(InventoryCrafting inventorycrafting) {
      return EMPTY_ITEMS;
   }
}
