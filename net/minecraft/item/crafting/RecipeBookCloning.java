package net.minecraft.item.crafting;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.world.World;

public class RecipeBookCloning extends ShapelessRecipes implements IRecipe {
   public RecipeBookCloning() {
      super(new ItemStack(Items.WRITTEN_BOOK, 0, -1), Arrays.asList(new ItemStack(Items.WRITABLE_BOOK, 0, 0)));
   }

   public boolean matches(InventoryCrafting inventorycrafting, World world) {
      int i = 0;
      ItemStack itemstack = null;

      for(int j = 0; j < inventorycrafting.getSizeInventory(); ++j) {
         ItemStack itemstack1 = inventorycrafting.getStackInSlot(j);
         if (itemstack1 != null) {
            if (itemstack1.getItem() == Items.WRITTEN_BOOK) {
               if (itemstack != null) {
                  return false;
               }

               itemstack = itemstack1;
            } else {
               if (itemstack1.getItem() != Items.WRITABLE_BOOK) {
                  return false;
               }

               ++i;
            }
         }
      }

      if (itemstack != null && i > 0) {
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
      int i = 0;
      ItemStack itemstack = null;

      for(int j = 0; j < inventorycrafting.getSizeInventory(); ++j) {
         ItemStack itemstack1 = inventorycrafting.getStackInSlot(j);
         if (itemstack1 != null) {
            if (itemstack1.getItem() == Items.WRITTEN_BOOK) {
               if (itemstack != null) {
                  return null;
               }

               itemstack = itemstack1;
            } else {
               if (itemstack1.getItem() != Items.WRITABLE_BOOK) {
                  return null;
               }

               ++i;
            }
         }
      }

      if (itemstack != null && i >= 1 && ItemWrittenBook.getGeneration(itemstack) < 2) {
         ItemStack itemstack2 = new ItemStack(Items.WRITTEN_BOOK, i);
         itemstack2.setTagCompound(itemstack.getTagCompound().copy());
         itemstack2.getTagCompound().setInteger("generation", ItemWrittenBook.getGeneration(itemstack) + 1);
         if (itemstack.hasDisplayName()) {
            itemstack2.setStackDisplayName(itemstack.getDisplayName());
         }

         return itemstack2;
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
      ItemStack[] aitemstack = new ItemStack[inventorycrafting.getSizeInventory()];

      for(int i = 0; i < aitemstack.length; ++i) {
         ItemStack itemstack = inventorycrafting.getStackInSlot(i);
         if (itemstack != null && itemstack.getItem() instanceof ItemWrittenBook) {
            aitemstack[i] = itemstack.copy();
            aitemstack[i].stackSize = 1;
            break;
         }
      }

      return aitemstack;
   }
}
