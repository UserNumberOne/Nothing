package net.minecraft.item.crafting;

import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.world.World;

public class RecipeBookCloning implements IRecipe {
   public boolean matches(InventoryCrafting var1, World var2) {
      int var3 = 0;
      ItemStack var4 = null;

      for(int var5 = 0; var5 < var1.getSizeInventory(); ++var5) {
         ItemStack var6 = var1.getStackInSlot(var5);
         if (var6 != null) {
            if (var6.getItem() == Items.WRITTEN_BOOK) {
               if (var4 != null) {
                  return false;
               }

               var4 = var6;
            } else {
               if (var6.getItem() != Items.WRITABLE_BOOK) {
                  return false;
               }

               ++var3;
            }
         }
      }

      return var4 != null && var3 > 0;
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      int var2 = 0;
      ItemStack var3 = null;

      for(int var4 = 0; var4 < var1.getSizeInventory(); ++var4) {
         ItemStack var5 = var1.getStackInSlot(var4);
         if (var5 != null) {
            if (var5.getItem() == Items.WRITTEN_BOOK) {
               if (var3 != null) {
                  return null;
               }

               var3 = var5;
            } else {
               if (var5.getItem() != Items.WRITABLE_BOOK) {
                  return null;
               }

               ++var2;
            }
         }
      }

      if (var3 != null && var2 >= 1 && ItemWrittenBook.getGeneration(var3) < 2) {
         ItemStack var6 = new ItemStack(Items.WRITTEN_BOOK, var2);
         var6.setTagCompound(var3.getTagCompound().copy());
         var6.getTagCompound().setInteger("generation", ItemWrittenBook.getGeneration(var3) + 1);
         if (var3.hasDisplayName()) {
            var6.setStackDisplayName(var3.getDisplayName());
         }

         return var6;
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
      ItemStack[] var2 = new ItemStack[var1.getSizeInventory()];

      for(int var3 = 0; var3 < var2.length; ++var3) {
         ItemStack var4 = var1.getStackInSlot(var3);
         if (var4 != null && var4.getItem() instanceof ItemWrittenBook) {
            var2[var3] = var4.copy();
            var2[var3].stackSize = 1;
            break;
         }
      }

      return var2;
   }
}
