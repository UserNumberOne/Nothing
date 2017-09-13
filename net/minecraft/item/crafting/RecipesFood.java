package net.minecraft.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class RecipesFood {
   public void addRecipes(CraftingManager var1) {
      var1.addShapelessRecipe(new ItemStack(Items.MUSHROOM_STEW), Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Items.BOWL);
      var1.addRecipe(new ItemStack(Items.COOKIE, 8), "#X#", 'X', new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage()), '#', Items.WHEAT);
      var1.addRecipe(new ItemStack(Items.RABBIT_STEW), " R ", "CPM", " B ", 'R', new ItemStack(Items.COOKED_RABBIT), 'C', Items.CARROT, 'P', Items.BAKED_POTATO, 'M', Blocks.BROWN_MUSHROOM, 'B', Items.BOWL);
      var1.addRecipe(new ItemStack(Items.RABBIT_STEW), " R ", "CPD", " B ", 'R', new ItemStack(Items.COOKED_RABBIT), 'C', Items.CARROT, 'P', Items.BAKED_POTATO, 'D', Blocks.RED_MUSHROOM, 'B', Items.BOWL);
      var1.addRecipe(new ItemStack(Blocks.MELON_BLOCK), "MMM", "MMM", "MMM", 'M', Items.MELON);
      var1.addRecipe(new ItemStack(Items.BEETROOT_SOUP), "OOO", "OOO", " B ", 'O', Items.BEETROOT, 'B', Items.BOWL);
      var1.addRecipe(new ItemStack(Items.MELON_SEEDS), "M", 'M', Items.MELON);
      var1.addRecipe(new ItemStack(Items.PUMPKIN_SEEDS, 4), "M", 'M', Blocks.PUMPKIN);
      var1.addShapelessRecipe(new ItemStack(Items.PUMPKIN_PIE), Blocks.PUMPKIN, Items.SUGAR, Items.EGG);
      var1.addShapelessRecipe(new ItemStack(Items.FERMENTED_SPIDER_EYE), Items.SPIDER_EYE, Blocks.BROWN_MUSHROOM, Items.SUGAR);
      var1.addShapelessRecipe(new ItemStack(Items.BLAZE_POWDER, 2), Items.BLAZE_ROD);
      var1.addShapelessRecipe(new ItemStack(Items.MAGMA_CREAM), Items.BLAZE_POWDER, Items.SLIME_BALL);
   }
}
