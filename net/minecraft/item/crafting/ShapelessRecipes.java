package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
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
      this.recipeOutput = output;
      this.recipeItems = inputList;
   }

   @Nullable
   public ItemStack getRecipeOutput() {
      return this.recipeOutput;
   }

   public ItemStack[] getRemainingItems(InventoryCrafting var1) {
      ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];

      for(int i = 0; i < aitemstack.length; ++i) {
         ItemStack itemstack = inv.getStackInSlot(i);
         aitemstack[i] = ForgeHooks.getContainerItem(itemstack);
      }

      return aitemstack;
   }

   public boolean matches(InventoryCrafting var1, World var2) {
      List list = Lists.newArrayList(this.recipeItems);

      for(int i = 0; i < inv.getHeight(); ++i) {
         for(int j = 0; j < inv.getWidth(); ++j) {
            ItemStack itemstack = inv.getStackInRowAndColumn(j, i);
            if (itemstack != null) {
               boolean flag = false;

               for(ItemStack itemstack1 : list) {
                  if (itemstack.getItem() == itemstack1.getItem() && (itemstack1.getMetadata() == 32767 || itemstack.getMetadata() == itemstack1.getMetadata())) {
                     flag = true;
                     list.remove(itemstack1);
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }
         }
      }

      return list.isEmpty();
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      return this.recipeOutput.copy();
   }

   public int getRecipeSize() {
      return this.recipeItems.size();
   }
}
