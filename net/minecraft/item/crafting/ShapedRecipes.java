package net.minecraft.item.crafting;

import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftShapedRecipe;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.inventory.ShapedRecipe;

public class ShapedRecipes implements IRecipe {
   private final int recipeWidth;
   private final int recipeHeight;
   private final ItemStack[] recipeItems;
   private final ItemStack recipeOutput;
   private boolean copyIngredientNBT;

   public ShapedRecipes(int i, int j, ItemStack[] aitemstack, ItemStack itemstack) {
      this.recipeWidth = i;
      this.recipeHeight = j;
      this.recipeItems = aitemstack;
      this.recipeOutput = itemstack;
   }

   public ShapedRecipe toBukkitRecipe() {
      CraftShapedRecipe recipe;
      CraftItemStack result = CraftItemStack.asCraftMirror(this.recipeOutput);
      recipe = new CraftShapedRecipe(result, this);
      label40:
      switch(this.recipeHeight) {
      case 1:
         switch(this.recipeWidth) {
         case 1:
            recipe.shape(new String[]{"a"});
            break label40;
         case 2:
            recipe.shape(new String[]{"ab"});
            break label40;
         case 3:
            recipe.shape(new String[]{"abc"});
         default:
            break label40;
         }
      case 2:
         switch(this.recipeWidth) {
         case 1:
            recipe.shape(new String[]{"a", "b"});
            break label40;
         case 2:
            recipe.shape(new String[]{"ab", "cd"});
            break label40;
         case 3:
            recipe.shape(new String[]{"abc", "def"});
         default:
            break label40;
         }
      case 3:
         switch(this.recipeWidth) {
         case 1:
            recipe.shape(new String[]{"a", "b", "c"});
            break;
         case 2:
            recipe.shape(new String[]{"ab", "cd", "ef"});
            break;
         case 3:
            recipe.shape(new String[]{"abc", "def", "ghi"});
         }
      }

      char c = 'a';

      for(ItemStack stack : this.recipeItems) {
         if (stack != null) {
            recipe.setIngredient(c, CraftMagicNumbers.getMaterial(stack.getItem()), stack.getMetadata());
         }

         ++c;
      }

      return recipe;
   }

   @Nullable
   public ItemStack getRecipeOutput() {
      return this.recipeOutput;
   }

   public ItemStack[] getRemainingItems(InventoryCrafting inventorycrafting) {
      ItemStack[] aitemstack = new ItemStack[inventorycrafting.getSizeInventory()];

      for(int i = 0; i < aitemstack.length; ++i) {
         ItemStack itemstack = inventorycrafting.getStackInSlot(i);
         if (itemstack != null && itemstack.getItem().hasContainerItem()) {
            aitemstack[i] = new ItemStack(itemstack.getItem().getContainerItem());
         }
      }

      return aitemstack;
   }

   public boolean matches(InventoryCrafting inventorycrafting, World world) {
      for(int i = 0; i <= 3 - this.recipeWidth; ++i) {
         for(int j = 0; j <= 3 - this.recipeHeight; ++j) {
            if (this.checkMatch(inventorycrafting, i, j, true)) {
               return true;
            }

            if (this.checkMatch(inventorycrafting, i, j, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkMatch(InventoryCrafting inventorycrafting, int i, int j, boolean flag) {
      for(int k = 0; k < 3; ++k) {
         for(int l = 0; l < 3; ++l) {
            int i1 = k - i;
            int j1 = l - j;
            ItemStack itemstack = null;
            if (i1 >= 0 && j1 >= 0 && i1 < this.recipeWidth && j1 < this.recipeHeight) {
               if (flag) {
                  itemstack = this.recipeItems[this.recipeWidth - i1 - 1 + j1 * this.recipeWidth];
               } else {
                  itemstack = this.recipeItems[i1 + j1 * this.recipeWidth];
               }
            }

            ItemStack itemstack1 = inventorycrafting.getStackInRowAndColumn(k, l);
            if (itemstack1 != null || itemstack != null) {
               if (itemstack1 == null && itemstack != null || itemstack1 != null && itemstack == null) {
                  return false;
               }

               if (itemstack.getItem() != itemstack1.getItem()) {
                  return false;
               }

               if (itemstack.getMetadata() != 32767 && itemstack.getMetadata() != itemstack1.getMetadata()) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
      ItemStack itemstack = this.getRecipeOutput().copy();
      if (this.copyIngredientNBT) {
         for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
            ItemStack itemstack1 = inventorycrafting.getStackInSlot(i);
            if (itemstack1 != null && itemstack1.hasTagCompound()) {
               itemstack.setTagCompound(itemstack1.getTagCompound().copy());
            }
         }
      }

      return itemstack;
   }

   public int getRecipeSize() {
      return this.recipeWidth * this.recipeHeight;
   }
}
