package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class ShapelessRecipes implements IRecipe {
   private final ItemStack recipeOutput;
   public final List recipeItems;

   public ShapelessRecipes(ItemStack var1, List var2) {
      this.recipeOutput = var1;
      this.recipeItems = var2;
   }

   @Nullable
   public ItemStack getRecipeOutput() {
      return this.recipeOutput;
   }

   public ItemStack[] getRemainingItems(InventoryCrafting var1) {
      ItemStack[] var2 = new ItemStack[var1.getSizeInventory()];

      for(int var3 = 0; var3 < var2.length; ++var3) {
         ItemStack var4 = var1.getStackInSlot(var3);
         var2[var3] = ForgeHooks.getContainerItem(var4);
      }

      return var2;
   }

   public boolean matches(InventoryCrafting var1, World var2) {
      ArrayList var3 = Lists.newArrayList(this.recipeItems);

      for(int var4 = 0; var4 < var1.getHeight(); ++var4) {
         for(int var5 = 0; var5 < var1.getWidth(); ++var5) {
            ItemStack var6 = var1.getStackInRowAndColumn(var5, var4);
            if (var6 != null) {
               boolean var7 = false;

               for(ItemStack var9 : var3) {
                  if (var6.getItem() == var9.getItem() && (var9.getMetadata() == 32767 || var6.getMetadata() == var9.getMetadata())) {
                     var7 = true;
                     var3.remove(var9);
                     break;
                  }
               }

               if (!var7) {
                  return false;
               }
            }
         }
      }

      return var3.isEmpty();
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      return this.recipeOutput.copy();
   }

   public int getRecipeSize() {
      return this.recipeItems.size();
   }
}
