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

   public boolean matches(InventoryCrafting var1, World var2) {
      if (var1.getWidth() == 3 && var1.getHeight() == 3) {
         for(int var3 = 0; var3 < var1.getWidth(); ++var3) {
            for(int var4 = 0; var4 < var1.getHeight(); ++var4) {
               ItemStack var5 = var1.getStackInRowAndColumn(var3, var4);
               if (var5 == null) {
                  return false;
               }

               Item var6 = var5.getItem();
               if (var3 == 1 && var4 == 1) {
                  if (var6 != Items.LINGERING_POTION) {
                     return false;
                  }
               } else if (var6 != Items.ARROW) {
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
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      ItemStack var2 = var1.getStackInRowAndColumn(1, 1);
      if (var2 != null && var2.getItem() == Items.LINGERING_POTION) {
         ItemStack var3 = new ItemStack(Items.TIPPED_ARROW, 8);
         PotionUtils.addPotionToItemStack(var3, PotionUtils.getPotionFromItem(var2));
         PotionUtils.appendEffects(var3, PotionUtils.getFullEffectsFromItem(var2));
         return var3;
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

   public ItemStack[] getRemainingItems(InventoryCrafting var1) {
      return EMPTY_ITEMS;
   }
}
