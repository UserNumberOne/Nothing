package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftShapelessRecipe;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.inventory.ShapelessRecipe;

public class ShapelessRecipes implements IRecipe {
   private final ItemStack recipeOutput;
   private final List recipeItems;

   public ShapelessRecipes(ItemStack itemstack, List list) {
      this.recipeOutput = itemstack;
      this.recipeItems = list;
   }

   public ShapelessRecipe toBukkitRecipe() {
      CraftItemStack result = CraftItemStack.asCraftMirror(this.recipeOutput);
      CraftShapelessRecipe recipe = new CraftShapelessRecipe(result, this);

      for(ItemStack stack : this.recipeItems) {
         if (stack != null) {
            recipe.addIngredient(CraftMagicNumbers.getMaterial(stack.getItem()), stack.getMetadata());
         }
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
      ArrayList arraylist = Lists.newArrayList(this.recipeItems);

      for(int i = 0; i < inventorycrafting.getHeight(); ++i) {
         for(int j = 0; j < inventorycrafting.getWidth(); ++j) {
            ItemStack itemstack = inventorycrafting.getStackInRowAndColumn(j, i);
            if (itemstack != null) {
               boolean flag = false;

               for(ItemStack itemstack1 : arraylist) {
                  if (itemstack.getItem() == itemstack1.getItem() && (itemstack1.getMetadata() == 32767 || itemstack.getMetadata() == itemstack1.getMetadata())) {
                     flag = true;
                     arraylist.remove(itemstack1);
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }
         }
      }

      return arraylist.isEmpty();
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
      return this.recipeOutput.copy();
   }

   public int getRecipeSize() {
      return this.recipeItems.size();
   }
}
