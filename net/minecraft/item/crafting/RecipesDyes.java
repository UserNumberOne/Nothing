package net.minecraft.item.crafting;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RecipesDyes {
   public void addRecipes(CraftingManager var1) {
      for(int var2 = 0; var2 < 16; ++var2) {
         var1.addShapelessRecipe(new ItemStack(Blocks.WOOL, 1, var2), new ItemStack(Items.DYE, 1, 15 - var2), new ItemStack(Item.getItemFromBlock(Blocks.WOOL)));
         var1.addRecipe(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 8, 15 - var2), "###", "#X#", "###", '#', new ItemStack(Blocks.HARDENED_CLAY), 'X', new ItemStack(Items.DYE, 1, var2));
         var1.addRecipe(new ItemStack(Blocks.STAINED_GLASS, 8, 15 - var2), "###", "#X#", "###", '#', new ItemStack(Blocks.GLASS), 'X', new ItemStack(Items.DYE, 1, var2));
         var1.addRecipe(new ItemStack(Blocks.STAINED_GLASS_PANE, 16, var2), "###", "###", '#', new ItemStack(Blocks.STAINED_GLASS, 1, var2));
      }

      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.YELLOW.getDyeDamage()), new ItemStack(Blocks.YELLOW_FLOWER, 1, BlockFlower.EnumFlowerType.DANDELION.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.POPPY.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 3, EnumDyeColor.WHITE.getDyeDamage()), Items.BONE);
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 9, EnumDyeColor.WHITE.getDyeDamage()), Blocks.BONE_BLOCK);
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.PINK.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.ORANGE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.YELLOW.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.LIME.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.GRAY.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.SILVER.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.GRAY.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 3, EnumDyeColor.SILVER.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.LIGHT_BLUE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.CYAN.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.PURPLE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.MAGENTA.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.PURPLE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 3, EnumDyeColor.MAGENTA.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 4, EnumDyeColor.MAGENTA.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.LIGHT_BLUE.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.MAGENTA.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ALLIUM.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.SILVER.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.RED_TULIP.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.ORANGE.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.SILVER.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.SILVER.getDyeDamage()), new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.YELLOW.getDyeDamage()), new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.SUNFLOWER.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.MAGENTA.getDyeDamage()), new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.SYRINGA.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.ROSE.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 2, EnumDyeColor.PINK.getDyeDamage()), new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta()));
      var1.addShapelessRecipe(new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage()), new ItemStack(Items.BEETROOT, 1));

      for(int var3 = 0; var3 < 16; ++var3) {
         var1.addRecipe(new ItemStack(Blocks.CARPET, 3, var3), "##", '#', new ItemStack(Blocks.WOOL, 1, var3));
      }

   }
}
