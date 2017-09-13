package net.minecraft.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RecipesWeapons {
   private final String[][] recipePatterns = new String[][]{{"X", "X", "#"}};
   private final Object[][] recipeItems = new Object[][]{{Blocks.PLANKS, Blocks.COBBLESTONE, Items.IRON_INGOT, Items.DIAMOND, Items.GOLD_INGOT}, {Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.GOLDEN_SWORD}};

   public void addRecipes(CraftingManager var1) {
      for(int var2 = 0; var2 < this.recipeItems[0].length; ++var2) {
         Object var3 = this.recipeItems[0][var2];

         for(int var4 = 0; var4 < this.recipeItems.length - 1; ++var4) {
            Item var5 = (Item)this.recipeItems[var4 + 1][var2];
            var1.addRecipe(new ItemStack(var5), this.recipePatterns[var4], '#', Items.STICK, 'X', var3);
         }
      }

      var1.addRecipe(new ItemStack(Items.BOW, 1), " #X", "# X", " #X", 'X', Items.STRING, '#', Items.STICK);
      var1.addRecipe(new ItemStack(Items.ARROW, 4), "X", "#", "Y", 'Y', Items.FEATHER, 'X', Items.FLINT, '#', Items.STICK);
      var1.addRecipe(new ItemStack(Items.SPECTRAL_ARROW, 2), " # ", "#X#", " # ", 'X', Items.ARROW, '#', Items.GLOWSTONE_DUST);
   }
}
