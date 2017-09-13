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
   public void addRecipes(CraftingManager manager) {
      manager.addRecipe(new ItemStack(Blocks.CHEST), "###", "# #", "###", '#', Blocks.PLANKS);
      manager.addShapelessRecipe(new ItemStack(Blocks.TRAPPED_CHEST), Blocks.CHEST, Blocks.TRIPWIRE_HOOK);
      manager.addRecipe(new ItemStack(Blocks.ENDER_CHEST), "###", "#E#", "###", '#', Blocks.OBSIDIAN, 'E', Items.ENDER_EYE);
      manager.addRecipe(new ItemStack(Blocks.FURNACE), "###", "# #", "###", '#', Blocks.COBBLESTONE);
      manager.addRecipe(new ItemStack(Blocks.CRAFTING_TABLE), "##", "##", '#', Blocks.PLANKS);
      manager.addRecipe(new ItemStack(Blocks.SANDSTONE), "##", "##", '#', new ItemStack(Blocks.SAND, 1, BlockSand.EnumType.SAND.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.RED_SANDSTONE), "##", "##", '#', new ItemStack(Blocks.SAND, 1, BlockSand.EnumType.RED_SAND.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.SANDSTONE, 4, BlockSandStone.EnumType.SMOOTH.getMetadata()), "##", "##", '#', new ItemStack(Blocks.SANDSTONE, 1, BlockSandStone.EnumType.DEFAULT.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.RED_SANDSTONE, 4, BlockRedSandstone.EnumType.SMOOTH.getMetadata()), "##", "##", '#', new ItemStack(Blocks.RED_SANDSTONE, 1, BlockRedSandstone.EnumType.DEFAULT.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.SANDSTONE, 1, BlockSandStone.EnumType.CHISELED.getMetadata()), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.SAND.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.RED_SANDSTONE, 1, BlockRedSandstone.EnumType.CHISELED.getMetadata()), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB2, 1, BlockStoneSlabNew.EnumType.RED_SANDSTONE.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, BlockQuartz.EnumType.CHISELED.getMetadata()), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.QUARTZ.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 2, BlockQuartz.EnumType.LINES_Y.getMetadata()), "#", "#", '#', new ItemStack(Blocks.QUARTZ_BLOCK, 1, BlockQuartz.EnumType.DEFAULT.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.STONEBRICK, 4), "##", "##", '#', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.CHISELED_META), "#", "#", '#', new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()));
      manager.addShapelessRecipe(new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.MOSSY_META), Blocks.STONEBRICK, Blocks.VINE);
      manager.addShapelessRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1), Blocks.COBBLESTONE, Blocks.VINE);
      manager.addRecipe(new ItemStack(Blocks.IRON_BARS, 16), "###", "###", '#', Items.IRON_INGOT);
      manager.addRecipe(new ItemStack(Blocks.GLASS_PANE, 16), "###", "###", '#', Blocks.GLASS);
      manager.addRecipe(new ItemStack(Blocks.REDSTONE_LAMP, 1), " R ", "RGR", " R ", 'R', Items.REDSTONE, 'G', Blocks.GLOWSTONE);
      manager.addRecipe(new ItemStack(Blocks.BEACON, 1), "GGG", "GSG", "OOO", 'G', Blocks.GLASS, 'S', Items.NETHER_STAR, 'O', Blocks.OBSIDIAN);
      manager.addRecipe(new ItemStack(Blocks.NETHER_BRICK, 1), "NN", "NN", 'N', Items.NETHERBRICK);
      manager.addRecipe(new ItemStack(Blocks.RED_NETHER_BRICK, 1), "NW", "WN", 'N', Items.NETHERBRICK, 'W', Items.NETHER_WART);
      manager.addRecipe(new ItemStack(Blocks.STONE, 2, BlockStone.EnumType.DIORITE.getMetadata()), "CQ", "QC", 'C', Blocks.COBBLESTONE, 'Q', Items.QUARTZ);
      manager.addShapelessRecipe(new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.GRANITE.getMetadata()), new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.DIORITE.getMetadata()), Items.QUARTZ);
      manager.addShapelessRecipe(new ItemStack(Blocks.STONE, 2, BlockStone.EnumType.ANDESITE.getMetadata()), new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.DIORITE.getMetadata()), Blocks.COBBLESTONE);
      manager.addRecipe(new ItemStack(Blocks.DIRT, 4, BlockDirt.DirtType.COARSE_DIRT.getMetadata()), "DG", "GD", 'D', new ItemStack(Blocks.DIRT, 1, BlockDirt.DirtType.DIRT.getMetadata()), 'G', Blocks.GRAVEL);
      manager.addRecipe(new ItemStack(Blocks.STONE, 4, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata()), "SS", "SS", 'S', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.DIORITE.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.STONE, 4, BlockStone.EnumType.GRANITE_SMOOTH.getMetadata()), "SS", "SS", 'S', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.GRANITE.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.STONE, 4, BlockStone.EnumType.ANDESITE_SMOOTH.getMetadata()), "SS", "SS", 'S', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.ANDESITE.getMetadata()));
      manager.addRecipe(new ItemStack(Blocks.PRISMARINE, 1, BlockPrismarine.ROUGH_META), "SS", "SS", 'S', Items.PRISMARINE_SHARD);
      manager.addRecipe(new ItemStack(Blocks.PRISMARINE, 1, BlockPrismarine.BRICKS_META), "SSS", "SSS", "SSS", 'S', Items.PRISMARINE_SHARD);
      manager.addRecipe(new ItemStack(Blocks.PRISMARINE, 1, BlockPrismarine.DARK_META), "SSS", "SIS", "SSS", 'S', Items.PRISMARINE_SHARD, 'I', new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()));
      manager.addRecipe(new ItemStack(Blocks.SEA_LANTERN, 1, 0), "SCS", "CCC", "SCS", 'S', Items.PRISMARINE_SHARD, 'C', Items.PRISMARINE_CRYSTALS);
      manager.addRecipe(new ItemStack(Blocks.PURPUR_BLOCK, 4, 0), "FF", "FF", 'F', Items.CHORUS_FRUIT_POPPED);
      manager.addRecipe(new ItemStack(Blocks.PURPUR_STAIRS, 4, 0), "#  ", "## ", "###", '#', Blocks.PURPUR_BLOCK);
      manager.addRecipe(new ItemStack(Blocks.PURPUR_PILLAR, 1, 0), "#", "#", '#', Blocks.PURPUR_SLAB);
      manager.addRecipe(new ItemStack(Blocks.END_BRICKS, 4, 0), "##", "##", '#', Blocks.END_STONE);
      manager.addRecipe(new ItemStack(Blocks.MAGMA, 1, 0), "##", "##", '#', Items.MAGMA_CREAM);
      manager.addRecipe(new ItemStack(Blocks.NETHER_WART_BLOCK, 1, 0), "###", "###", "###", '#', Items.NETHER_WART);
   }
}
