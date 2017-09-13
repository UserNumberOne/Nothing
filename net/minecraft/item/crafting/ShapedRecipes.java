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

   public ShapedRecipes(int var1, int var2, ItemStack[] var3, ItemStack var4) {
      this.recipeWidth = var1;
      this.recipeHeight = var2;
      this.recipeItems = var3;
      this.recipeOutput = var4;
   }

   public ShapedRecipe toBukkitRecipe() {
      CraftShapedRecipe var2;
      CraftItemStack var1 = CraftItemStack.asCraftMirror(this.recipeOutput);
      var2 = new CraftShapedRecipe(var1, this);
      label40:
      switch(this.recipeHeight) {
      case 1:
         switch(this.recipeWidth) {
         case 1:
            var2.shape(new String[]{"a"});
            break label40;
         case 2:
            var2.shape(new String[]{"ab"});
            break label40;
         case 3:
            var2.shape(new String[]{"abc"});
         default:
            break label40;
         }
      case 2:
         switch(this.recipeWidth) {
         case 1:
            var2.shape(new String[]{"a", "b"});
            break label40;
         case 2:
            var2.shape(new String[]{"ab", "cd"});
            break label40;
         case 3:
            var2.shape(new String[]{"abc", "def"});
         default:
            break label40;
         }
      case 3:
         switch(this.recipeWidth) {
         case 1:
            var2.shape(new String[]{"a", "b", "c"});
            break;
         case 2:
            var2.shape(new String[]{"ab", "cd", "ef"});
            break;
         case 3:
            var2.shape(new String[]{"abc", "def", "ghi"});
         }
      }

      char var3 = 'a';

      for(ItemStack var7 : this.recipeItems) {
         if (var7 != null) {
            var2.setIngredient(var3, CraftMagicNumbers.getMaterial(var7.getItem()), var7.getMetadata());
         }

         ++var3;
      }

      return var2;
   }

   @Nullable
   public ItemStack getRecipeOutput() {
      return this.recipeOutput;
   }

   public ItemStack[] getRemainingItems(InventoryCrafting var1) {
      ItemStack[] var2 = new ItemStack[var1.getSizeInventory()];

      for(int var3 = 0; var3 < var2.length; ++var3) {
         ItemStack var4 = var1.getStackInSlot(var3);
         if (var4 != null && var4.getItem().hasContainerItem()) {
            var2[var3] = new ItemStack(var4.getItem().getContainerItem());
         }
      }

      return var2;
   }

   public boolean matches(InventoryCrafting var1, World var2) {
      for(int var3 = 0; var3 <= 3 - this.recipeWidth; ++var3) {
         for(int var4 = 0; var4 <= 3 - this.recipeHeight; ++var4) {
            if (this.checkMatch(var1, var3, var4, true)) {
               return true;
            }

            if (this.checkMatch(var1, var3, var4, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkMatch(InventoryCrafting var1, int var2, int var3, boolean var4) {
      for(int var5 = 0; var5 < 3; ++var5) {
         for(int var6 = 0; var6 < 3; ++var6) {
            int var7 = var5 - var2;
            int var8 = var6 - var3;
            ItemStack var9 = null;
            if (var7 >= 0 && var8 >= 0 && var7 < this.recipeWidth && var8 < this.recipeHeight) {
               if (var4) {
                  var9 = this.recipeItems[this.recipeWidth - var7 - 1 + var8 * this.recipeWidth];
               } else {
                  var9 = this.recipeItems[var7 + var8 * this.recipeWidth];
               }
            }

            ItemStack var10 = var1.getStackInRowAndColumn(var5, var6);
            if (var10 != null || var9 != null) {
               if (var10 == null && var9 != null || var10 != null && var9 == null) {
                  return false;
               }

               if (var9.getItem() != var10.getItem()) {
                  return false;
               }

               if (var9.getMetadata() != 32767 && var9.getMetadata() != var10.getMetadata()) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      ItemStack var2 = this.getRecipeOutput().copy();
      if (this.copyIngredientNBT) {
         for(int var3 = 0; var3 < var1.getSizeInventory(); ++var3) {
            ItemStack var4 = var1.getStackInSlot(var3);
            if (var4 != null && var4.hasTagCompound()) {
               var2.setTagCompound(var4.getTagCompound().copy());
            }
         }
      }

      return var2;
   }

   public int getRecipeSize() {
      return this.recipeWidth * this.recipeHeight;
   }
}
