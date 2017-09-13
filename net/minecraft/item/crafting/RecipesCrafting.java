package net.minecraft.item.crafting;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class RecipesCrafting {
   public void addRecipes(CraftingManager var1) {
      var1.addRecipe(new ItemStack(Blocks.CHEST), "###", "# #", "###", '#', Blocks.PLANKS);
      var1.addShapelessRecipe(new ItemStack(Blocks.TRAPPED_CHEST), Blocks.CHEST, Blocks.TRIPWIRE_HOOK);
      var1.addRecipe(new ItemStack(Blocks.ENDER_CHEST), "###", "#E#", "###", '#', Blocks.OBSIDIAN, 'E', Items.ENDER_EYE);
      var1.addRecipe(new ItemStack(Blocks.FURNACE), "###", "# #", "###", '#', Blocks.COBBLESTONE);
      var1.addRecipe(new ItemStack(Blocks.CRAFTING_TABLE), "##", "##", '#', Blocks.PLANKS);
      var1.addRecipe(new ItemStack(Blocks.SANDSTONE), "##", "##", '#', new ItemStack(Blocks.SAND, 1, BlockSand.EnumType.SAND.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.RED_SANDSTONE), "##", "##", '#', new ItemStack(Blocks.SAND, 1, BlockSand.EnumType.RED_SAND.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.SANDSTONE, 4, BlockSandStone.EnumType.SMOOTH.getMetadata()), "##", "##", '#', new ItemStack(Blocks.SANDSTONE, 1, BlockSandStone.EnumType.DEFAULT.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.RED_SANDSTONE, 4, BlockRedSandstone.EnumType.SMOOTH.getMetadata()), "##", "##", '#', new ItemStack(Blocks.RED_SANDSTONE, 1, BlockRedSandstone.EnumType.DEFAULT.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.SANDSTONE, 1, BlockSandStone.EnumType.CHISELED.getMetadata()), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.SAND.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.RED_SANDSTONE, 1, BlockRedSandstone.EnumType.CHISELED.getMetadata()), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB2, 1, BlockStoneSlabNew.EnumType.RED_SANDSTONE.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, BlockQuartz.EnumType.CHISELED.getMetadata()), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.QUARTZ.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 2, BlockQuartz.EnumType.LINES_Y.getMetadata()), "#", "#", '#', new ItemStack(Blocks.QUARTZ_BLOCK, 1, BlockQuartz.EnumType.DEFAULT.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.STONEBRICK, 4), "##", "##", '#', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.CHISELED_META), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()));
      var1.addShapelessRecipe(new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.MOSSY_META), Blocks.STONEBRICK, Blocks.VINE);
      var1.addShapelessRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1), Blocks.COBBLESTONE, Blocks.VINE);
      var1.addRecipe(new ItemStack(Blocks.IRON_BARS, 16), "###", "###", '#', Items.IRON_INGOT);
      var1.addRecipe(new ItemStack(Blocks.GLASS_PANE, 16), "###", "###", '#', Blocks.GLASS);
      var1.addRecipe(new ItemStack(Blocks.REDSTONE_LAMP, 1), " R ", "RGR", " R ", 'R', Items.REDSTONE, 'G', Blocks.GLOWSTONE);
      var1.addRecipe(new ItemStack(Blocks.BEACON, 1), "GGG", "GSG", "OOO", 'G', Blocks.GLASS, 'S', Items.NETHER_STAR, 'O', Blocks.OBSIDIAN);
      var1.addRecipe(new ItemStack(Blocks.NETHER_BRICK, 1), "NN", "NN", 'N', Items.NETHERBRICK);
      var1.addRecipe(new ItemStack(Blocks.RED_NETHER_BRICK, 1), "NW", "WN", 'N', Items.NETHERBRICK, 'W', Items.NETHER_WART);
      var1.addRecipe(new ItemStack(Blocks.STONE, 2, BlockStone.EnumType.DIORITE.getMetadata()), "CQ", "QC", 'C', Blocks.COBBLESTONE, 'Q', Items.QUARTZ);
      var1.addShapelessRecipe(new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.GRANITE.getMetadata()), new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.DIORITE.getMetadata()), Items.QUARTZ);
      var1.addShapelessRecipe(new ItemStack(Blocks.STONE, 2, BlockStone.EnumType.ANDESITE.getMetadata()), new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.DIORITE.getMetadata()), Blocks.COBBLESTONE);
      var1.addRecipe(new ItemStack(Blocks.DIRT, 4, BlockDirt.DirtType.COARSE_DIRT.getMetadata()), "DG", "GD", 'D', new ItemStack(Blocks.DIRT, 1, BlockDirt.DirtType.DIRT.getMetadata()), 'G', Blocks.GRAVEL);
      var1.addRecipe(new ItemStack(Blocks.STONE, 4, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata()), "SS", "SS", 'S', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.DIORITE.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.STONE, 4, BlockStone.EnumType.GRANITE_SMOOTH.getMetadata()), "SS", "SS", 'S', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.GRANITE.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.STONE, 4, BlockStone.EnumType.ANDESITE_SMOOTH.getMetadata()), "SS", "SS", 'S', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.ANDESITE.getMetadata()));
      var1.addRecipe(new ItemStack(Blocks.PRISMARINE, 1, BlockPrismarine.ROUGH_META), "SS", "SS", 'S', Items.PRISMARINE_SHARD);
      var1.addRecipe(new ItemStack(Blocks.PRISMARINE, 1, BlockPrismarine.BRICKS_META), "SSS", "SSS", "SSS", 'S', Items.PRISMARINE_SHARD);
      var1.addRecipe(new ItemStack(Blocks.PRISMARINE, 1, BlockPrismarine.DARK_META), "SSS", "SIS", "SSS", 'S', Items.PRISMARINE_SHARD, 'I', new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()));
      var1.addRecipe(new ItemStack(Blocks.SEA_LANTERN, 1, 0), "SCS", "CCC", "SCS", 'S', Items.PRISMARINE_SHARD, 'C', Items.PRISMARINE_CRYSTALS);
      var1.addRecipe(new ItemStack(Blocks.PURPUR_BLOCK, 4, 0), "FF", "FF", 'F', Items.CHORUS_FRUIT_POPPED);
      var1.addRecipe(new ItemStack(Blocks.PURPUR_STAIRS, 4, 0), "#  ", "## ", "###", '#', Blocks.PURPUR_BLOCK);
      var1.addRecipe(new ItemStack(Blocks.PURPUR_PILLAR, 1, 0), "#", "#", '#', Blocks.PURPUR_SLAB);
      var1.addRecipe(new ItemStack(Blocks.END_BRICKS, 4, 0), "##", "##", '#', Blocks.END_STONE);
      var1.addRecipe(new ItemStack(Blocks.MAGMA, 1, 0), "##", "##", '#', Items.MAGMA_CREAM);
      var1.addRecipe(new ItemStack(Blocks.NETHER_WART_BLOCK, 1, 0), "###", "###", "###", '#', Items.NETHER_WART);
   }
}
