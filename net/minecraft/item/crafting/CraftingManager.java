package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.BlockWall;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CraftingManager {
   private static final CraftingManager INSTANCE = new CraftingManager();
   private final List recipes = Lists.newArrayList();

   public static CraftingManager getInstance() {
      return INSTANCE;
   }

   private CraftingManager() {
      (new RecipesTools()).addRecipes(this);
      (new RecipesWeapons()).addRecipes(this);
      (new RecipesIngots()).addRecipes(this);
      (new RecipesFood()).addRecipes(this);
      (new RecipesCrafting()).addRecipes(this);
      (new RecipesArmor()).addRecipes(this);
      (new RecipesDyes()).addRecipes(this);
      this.recipes.add(new RecipesArmorDyes());
      this.recipes.add(new RecipeBookCloning());
      this.recipes.add(new RecipesMapCloning());
      this.recipes.add(new RecipesMapExtending());
      this.recipes.add(new RecipeFireworks());
      this.recipes.add(new RecipeRepairItem());
      this.recipes.add(new RecipeTippedArrow());
      (new RecipesBanners()).addRecipes(this);
      (new ShieldRecipes()).addRecipes(this);
      this.addRecipe(new ItemStack(Items.PAPER, 3), "###", '#', Items.REEDS);
      this.addShapelessRecipe(new ItemStack(Items.BOOK, 1), Items.PAPER, Items.PAPER, Items.PAPER, Items.LEATHER);
      this.addShapelessRecipe(new ItemStack(Items.WRITABLE_BOOK, 1), Items.BOOK, new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()), Items.FEATHER);
      this.addRecipe(new ItemStack(Blocks.OAK_FENCE, 3), "W#W", "W#W", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.OAK.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.BIRCH_FENCE, 3), "W#W", "W#W", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.SPRUCE_FENCE, 3), "W#W", "W#W", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.JUNGLE_FENCE, 3), "W#W", "W#W", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.ACACIA_FENCE, 3), "W#W", "W#W", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.DARK_OAK_FENCE, 3), "W#W", "W#W", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.COBBLESTONE_WALL, 6, BlockWall.EnumType.NORMAL.getMetadata()), "###", "###", '#', Blocks.COBBLESTONE);
      this.addRecipe(new ItemStack(Blocks.COBBLESTONE_WALL, 6, BlockWall.EnumType.MOSSY.getMetadata()), "###", "###", '#', Blocks.MOSSY_COBBLESTONE);
      this.addRecipe(new ItemStack(Blocks.NETHER_BRICK_FENCE, 6), "###", "###", '#', Blocks.NETHER_BRICK);
      this.addRecipe(new ItemStack(Blocks.OAK_FENCE_GATE, 1), "#W#", "#W#", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.OAK.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.BIRCH_FENCE_GATE, 1), "#W#", "#W#", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.SPRUCE_FENCE_GATE, 1), "#W#", "#W#", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.JUNGLE_FENCE_GATE, 1), "#W#", "#W#", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.ACACIA_FENCE_GATE, 1), "#W#", "#W#", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.DARK_OAK_FENCE_GATE, 1), "#W#", "#W#", '#', Items.STICK, 'W', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.JUKEBOX, 1), "###", "#X#", "###", '#', Blocks.PLANKS, 'X', Items.DIAMOND);
      this.addRecipe(new ItemStack(Items.LEAD, 2), "~~ ", "~O ", "  ~", '~', Items.STRING, 'O', Items.SLIME_BALL);
      this.addRecipe(new ItemStack(Blocks.NOTEBLOCK, 1), "###", "#X#", "###", '#', Blocks.PLANKS, 'X', Items.REDSTONE);
      this.addRecipe(new ItemStack(Blocks.BOOKSHELF, 1), "###", "XXX", "###", '#', Blocks.PLANKS, 'X', Items.BOOK);
      this.addRecipe(new ItemStack(Blocks.SNOW, 1), "##", "##", '#', Items.SNOWBALL);
      this.addRecipe(new ItemStack(Blocks.SNOW_LAYER, 6), "###", '#', Blocks.SNOW);
      this.addRecipe(new ItemStack(Blocks.CLAY, 1), "##", "##", '#', Items.CLAY_BALL);
      this.addRecipe(new ItemStack(Blocks.BRICK_BLOCK, 1), "##", "##", '#', Items.BRICK);
      this.addRecipe(new ItemStack(Blocks.GLOWSTONE, 1), "##", "##", '#', Items.GLOWSTONE_DUST);
      this.addRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1), "##", "##", '#', Items.QUARTZ);
      this.addRecipe(new ItemStack(Blocks.WOOL, 1), "##", "##", '#', Items.STRING);
      this.addRecipe(new ItemStack(Blocks.TNT, 1), "X#X", "#X#", "X#X", 'X', Items.GUNPOWDER, '#', Blocks.SAND);
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB, 6, BlockStoneSlab.EnumType.COBBLESTONE.getMetadata()), "###", '#', Blocks.COBBLESTONE);
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB, 6, BlockStoneSlab.EnumType.STONE.getMetadata()), "###", '#', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB, 6, BlockStoneSlab.EnumType.SAND.getMetadata()), "###", '#', Blocks.SANDSTONE);
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB, 6, BlockStoneSlab.EnumType.BRICK.getMetadata()), "###", '#', Blocks.BRICK_BLOCK);
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB, 6, BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), "###", '#', Blocks.STONEBRICK);
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB, 6, BlockStoneSlab.EnumType.NETHERBRICK.getMetadata()), "###", '#', Blocks.NETHER_BRICK);
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB, 6, BlockStoneSlab.EnumType.QUARTZ.getMetadata()), "###", '#', Blocks.QUARTZ_BLOCK);
      this.addRecipe(new ItemStack(Blocks.STONE_SLAB2, 6, BlockStoneSlabNew.EnumType.RED_SANDSTONE.getMetadata()), "###", '#', Blocks.RED_SANDSTONE);
      this.addRecipe(new ItemStack(Blocks.PURPUR_SLAB, 6, 0), "###", '#', Blocks.PURPUR_BLOCK);
      this.addRecipe(new ItemStack(Blocks.WOODEN_SLAB, 6, 0), "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.OAK.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.WOODEN_SLAB, 6, BlockPlanks.EnumType.BIRCH.getMetadata()), "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.WOODEN_SLAB, 6, BlockPlanks.EnumType.SPRUCE.getMetadata()), "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.WOODEN_SLAB, 6, BlockPlanks.EnumType.JUNGLE.getMetadata()), "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.WOODEN_SLAB, 6, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4), "###", '#', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.WOODEN_SLAB, 6, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4), "###", '#', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.LADDER, 3), "# #", "###", "# #", '#', Items.STICK);
      this.addRecipe(new ItemStack(Items.OAK_DOOR, 3), "##", "##", "##", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.OAK.getMetadata()));
      this.addRecipe(new ItemStack(Items.SPRUCE_DOOR, 3), "##", "##", "##", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
      this.addRecipe(new ItemStack(Items.BIRCH_DOOR, 3), "##", "##", "##", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
      this.addRecipe(new ItemStack(Items.JUNGLE_DOOR, 3), "##", "##", "##", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
      this.addRecipe(new ItemStack(Items.ACACIA_DOOR, 3), "##", "##", "##", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.ACACIA.getMetadata()));
      this.addRecipe(new ItemStack(Items.DARK_OAK_DOOR, 3), "##", "##", "##", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.TRAPDOOR, 2), "###", "###", '#', Blocks.PLANKS);
      this.addRecipe(new ItemStack(Items.IRON_DOOR, 3), "##", "##", "##", '#', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Blocks.IRON_TRAPDOOR, 1), "##", "##", '#', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Items.SIGN, 3), "###", "###", " X ", '#', Blocks.PLANKS, 'X', Items.STICK);
      this.addRecipe(new ItemStack(Items.CAKE, 1), "AAA", "BEB", "CCC", 'A', Items.MILK_BUCKET, 'B', Items.SUGAR, 'C', Items.WHEAT, 'E', Items.EGG);
      this.addRecipe(new ItemStack(Items.SUGAR, 1), "#", '#', Items.REEDS);
      this.addRecipe(new ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.OAK.getMetadata()), "#", '#', new ItemStack(Blocks.LOG, 1, BlockPlanks.EnumType.OAK.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.SPRUCE.getMetadata()), "#", '#', new ItemStack(Blocks.LOG, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.BIRCH.getMetadata()), "#", '#', new ItemStack(Blocks.LOG, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.JUNGLE.getMetadata()), "#", '#', new ItemStack(Blocks.LOG, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.PLANKS, 4, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4), "#", '#', new ItemStack(Blocks.LOG2, 1, BlockPlanks.EnumType.ACACIA.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.PLANKS, 4, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4), "#", '#', new ItemStack(Blocks.LOG2, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4));
      this.addRecipe(new ItemStack(Items.STICK, 4), "#", "#", '#', Blocks.PLANKS);
      this.addRecipe(new ItemStack(Blocks.TORCH, 4), "X", "#", 'X', Items.COAL, '#', Items.STICK);
      this.addRecipe(new ItemStack(Blocks.TORCH, 4), "X", "#", 'X', new ItemStack(Items.COAL, 1, 1), '#', Items.STICK);
      this.addRecipe(new ItemStack(Items.BOWL, 4), "# #", " # ", '#', Blocks.PLANKS);
      this.addRecipe(new ItemStack(Items.GLASS_BOTTLE, 3), "# #", " # ", '#', Blocks.GLASS);
      this.addRecipe(new ItemStack(Blocks.RAIL, 16), "X X", "X#X", "X X", 'X', Items.IRON_INGOT, '#', Items.STICK);
      this.addRecipe(new ItemStack(Blocks.GOLDEN_RAIL, 6), "X X", "X#X", "XRX", 'X', Items.GOLD_INGOT, 'R', Items.REDSTONE, '#', Items.STICK);
      this.addRecipe(new ItemStack(Blocks.ACTIVATOR_RAIL, 6), "XSX", "X#X", "XSX", 'X', Items.IRON_INGOT, '#', Blocks.REDSTONE_TORCH, 'S', Items.STICK);
      this.addRecipe(new ItemStack(Blocks.DETECTOR_RAIL, 6), "X X", "X#X", "XRX", 'X', Items.IRON_INGOT, 'R', Items.REDSTONE, '#', Blocks.STONE_PRESSURE_PLATE);
      this.addRecipe(new ItemStack(Items.MINECART, 1), "# #", "###", '#', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Items.CAULDRON, 1), "# #", "# #", "###", '#', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Items.BREWING_STAND, 1), " B ", "###", '#', Blocks.COBBLESTONE, 'B', Items.BLAZE_ROD);
      this.addRecipe(new ItemStack(Blocks.LIT_PUMPKIN, 1), "A", "B", 'A', Blocks.PUMPKIN, 'B', Blocks.TORCH);
      this.addRecipe(new ItemStack(Items.CHEST_MINECART, 1), "A", "B", 'A', Blocks.CHEST, 'B', Items.MINECART);
      this.addRecipe(new ItemStack(Items.FURNACE_MINECART, 1), "A", "B", 'A', Blocks.FURNACE, 'B', Items.MINECART);
      this.addRecipe(new ItemStack(Items.TNT_MINECART, 1), "A", "B", 'A', Blocks.TNT, 'B', Items.MINECART);
      this.addRecipe(new ItemStack(Items.HOPPER_MINECART, 1), "A", "B", 'A', Blocks.HOPPER, 'B', Items.MINECART);
      this.addRecipe(new ItemStack(Items.BOAT, 1), "# #", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.OAK.getMetadata()));
      this.addRecipe(new ItemStack(Items.SPRUCE_BOAT, 1), "# #", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
      this.addRecipe(new ItemStack(Items.BIRCH_BOAT, 1), "# #", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
      this.addRecipe(new ItemStack(Items.JUNGLE_BOAT, 1), "# #", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
      this.addRecipe(new ItemStack(Items.ACACIA_BOAT, 1), "# #", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.ACACIA.getMetadata()));
      this.addRecipe(new ItemStack(Items.DARK_OAK_BOAT, 1), "# #", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata()));
      this.addRecipe(new ItemStack(Items.BUCKET, 1), "# #", " # ", '#', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Items.FLOWER_POT, 1), "# #", " # ", '#', Items.BRICK);
      this.addShapelessRecipe(new ItemStack(Items.FLINT_AND_STEEL, 1), new ItemStack(Items.IRON_INGOT, 1), new ItemStack(Items.FLINT, 1));
      this.addRecipe(new ItemStack(Items.BREAD, 1), "###", '#', Items.WHEAT);
      this.addRecipe(new ItemStack(Blocks.OAK_STAIRS, 4), "#  ", "## ", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.OAK.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.BIRCH_STAIRS, 4), "#  ", "## ", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.SPRUCE_STAIRS, 4), "#  ", "## ", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.JUNGLE_STAIRS, 4), "#  ", "## ", "###", '#', new ItemStack(Blocks.PLANKS, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.ACACIA_STAIRS, 4), "#  ", "## ", "###", '#', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4));
      this.addRecipe(new ItemStack(Blocks.DARK_OAK_STAIRS, 4), "#  ", "## ", "###", '#', new ItemStack(Blocks.PLANKS, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4));
      this.addRecipe(new ItemStack(Items.FISHING_ROD, 1), "  #", " #X", "# X", '#', Items.STICK, 'X', Items.STRING);
      this.addRecipe(new ItemStack(Items.CARROT_ON_A_STICK, 1), "# ", " X", '#', Items.FISHING_ROD, 'X', Items.CARROT);
      this.addRecipe(new ItemStack(Blocks.STONE_STAIRS, 4), "#  ", "## ", "###", '#', Blocks.COBBLESTONE);
      this.addRecipe(new ItemStack(Blocks.BRICK_STAIRS, 4), "#  ", "## ", "###", '#', Blocks.BRICK_BLOCK);
      this.addRecipe(new ItemStack(Blocks.STONE_BRICK_STAIRS, 4), "#  ", "## ", "###", '#', Blocks.STONEBRICK);
      this.addRecipe(new ItemStack(Blocks.NETHER_BRICK_STAIRS, 4), "#  ", "## ", "###", '#', Blocks.NETHER_BRICK);
      this.addRecipe(new ItemStack(Blocks.SANDSTONE_STAIRS, 4), "#  ", "## ", "###", '#', Blocks.SANDSTONE);
      this.addRecipe(new ItemStack(Blocks.RED_SANDSTONE_STAIRS, 4), "#  ", "## ", "###", '#', Blocks.RED_SANDSTONE);
      this.addRecipe(new ItemStack(Blocks.QUARTZ_STAIRS, 4), "#  ", "## ", "###", '#', Blocks.QUARTZ_BLOCK);
      this.addRecipe(new ItemStack(Items.PAINTING, 1), "###", "#X#", "###", '#', Items.STICK, 'X', Blocks.WOOL);
      this.addRecipe(new ItemStack(Items.ITEM_FRAME, 1), "###", "#X#", "###", '#', Items.STICK, 'X', Items.LEATHER);
      this.addRecipe(new ItemStack(Items.GOLDEN_APPLE), "###", "#X#", "###", '#', Items.GOLD_INGOT, 'X', Items.APPLE);
      this.addRecipe(new ItemStack(Items.GOLDEN_CARROT), "###", "#X#", "###", '#', Items.GOLD_NUGGET, 'X', Items.CARROT);
      this.addRecipe(new ItemStack(Items.SPECKLED_MELON, 1), "###", "#X#", "###", '#', Items.GOLD_NUGGET, 'X', Items.MELON);
      this.addRecipe(new ItemStack(Blocks.LEVER, 1), "X", "#", '#', Blocks.COBBLESTONE, 'X', Items.STICK);
      this.addRecipe(new ItemStack(Blocks.TRIPWIRE_HOOK, 2), "I", "S", "#", '#', Blocks.PLANKS, 'S', Items.STICK, 'I', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Blocks.REDSTONE_TORCH, 1), "X", "#", '#', Items.STICK, 'X', Items.REDSTONE);
      this.addRecipe(new ItemStack(Items.REPEATER, 1), "#X#", "III", '#', Blocks.REDSTONE_TORCH, 'X', Items.REDSTONE, 'I', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()));
      this.addRecipe(new ItemStack(Items.COMPARATOR, 1), " # ", "#X#", "III", '#', Blocks.REDSTONE_TORCH, 'X', Items.QUARTZ, 'I', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()));
      this.addRecipe(new ItemStack(Items.CLOCK, 1), " # ", "#X#", " # ", '#', Items.GOLD_INGOT, 'X', Items.REDSTONE);
      this.addRecipe(new ItemStack(Items.COMPASS, 1), " # ", "#X#", " # ", '#', Items.IRON_INGOT, 'X', Items.REDSTONE);
      this.addRecipe(new ItemStack(Items.MAP, 1), "###", "#X#", "###", '#', Items.PAPER, 'X', Items.COMPASS);
      this.addRecipe(new ItemStack(Blocks.STONE_BUTTON, 1), "#", '#', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.WOODEN_BUTTON, 1), "#", '#', Blocks.PLANKS);
      this.addRecipe(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 1), "##", '#', new ItemStack(Blocks.STONE, 1, BlockStone.EnumType.STONE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1), "##", '#', Blocks.PLANKS);
      this.addRecipe(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 1), "##", '#', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1), "##", '#', Items.GOLD_INGOT);
      this.addRecipe(new ItemStack(Blocks.DISPENSER, 1), "###", "#X#", "#R#", '#', Blocks.COBBLESTONE, 'X', Items.BOW, 'R', Items.REDSTONE);
      this.addRecipe(new ItemStack(Blocks.DROPPER, 1), "###", "# #", "#R#", '#', Blocks.COBBLESTONE, 'R', Items.REDSTONE);
      this.addRecipe(new ItemStack(Blocks.PISTON, 1), "TTT", "#X#", "#R#", '#', Blocks.COBBLESTONE, 'X', Items.IRON_INGOT, 'R', Items.REDSTONE, 'T', Blocks.PLANKS);
      this.addRecipe(new ItemStack(Blocks.STICKY_PISTON, 1), "S", "P", 'S', Items.SLIME_BALL, 'P', Blocks.PISTON);
      this.addRecipe(new ItemStack(Items.BED, 1), "###", "XXX", '#', Blocks.WOOL, 'X', Blocks.PLANKS);
      this.addRecipe(new ItemStack(Blocks.ENCHANTING_TABLE, 1), " B ", "D#D", "###", '#', Blocks.OBSIDIAN, 'B', Items.BOOK, 'D', Items.DIAMOND);
      this.addRecipe(new ItemStack(Blocks.ANVIL, 1), "III", " i ", "iii", 'I', Blocks.IRON_BLOCK, 'i', Items.IRON_INGOT);
      this.addRecipe(new ItemStack(Items.LEATHER), "##", "##", '#', Items.RABBIT_HIDE);
      this.addShapelessRecipe(new ItemStack(Items.ENDER_EYE, 1), Items.ENDER_PEARL, Items.BLAZE_POWDER);
      this.addShapelessRecipe(new ItemStack(Items.FIRE_CHARGE, 3), Items.GUNPOWDER, Items.BLAZE_POWDER, Items.COAL);
      this.addShapelessRecipe(new ItemStack(Items.FIRE_CHARGE, 3), Items.GUNPOWDER, Items.BLAZE_POWDER, new ItemStack(Items.COAL, 1, 1));
      this.addRecipe(new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "QQQ", "WWW", 'G', Blocks.GLASS, 'Q', Items.QUARTZ, 'W', Blocks.WOODEN_SLAB);
      this.addRecipe(new ItemStack(Items.END_CRYSTAL), "GGG", "GEG", "GTG", 'G', Blocks.GLASS, 'E', Items.ENDER_EYE, 'T', Items.GHAST_TEAR);
      this.addRecipe(new ItemStack(Blocks.HOPPER), "I I", "ICI", " I ", 'I', Items.IRON_INGOT, 'C', Blocks.CHEST);
      this.addRecipe(new ItemStack(Items.ARMOR_STAND, 1), "///", " / ", "/_/", '/', Items.STICK, '_', new ItemStack(Blocks.STONE_SLAB, 1, BlockStoneSlab.EnumType.STONE.getMetadata()));
      this.addRecipe(new ItemStack(Blocks.END_ROD, 4), "/", "#", '/', Items.BLAZE_ROD, '#', Items.CHORUS_FRUIT_POPPED);
      this.addRecipe(new ItemStack(Blocks.BONE_BLOCK, 1), "XXX", "XXX", "XXX", 'X', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()));
      Collections.sort(this.recipes, new Comparator() {
         public int compare(IRecipe var1, IRecipe var2) {
            return var1 instanceof ShapelessRecipes && var2 instanceof ShapedRecipes ? 1 : (var2 instanceof ShapelessRecipes && var1 instanceof ShapedRecipes ? -1 : (var2.getRecipeSize() < var1.getRecipeSize() ? -1 : (var2.getRecipeSize() > var1.getRecipeSize() ? 1 : 0)));
         }
      });
   }

   public ShapedRecipes addRecipe(ItemStack var1, Object... var2) {
      String var3 = "";
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      if (var2[var4] instanceof String[]) {
         String[] var12 = (String[])var2[var4++];

         for(String var11 : var12) {
            ++var6;
            var5 = var11.length();
            var3 = var3 + var11;
         }
      } else {
         while(var2[var4] instanceof String) {
            String var7 = (String)var2[var4++];
            ++var6;
            var5 = var7.length();
            var3 = var3 + var7;
         }
      }

      HashMap var13;
      for(var13 = Maps.newHashMap(); var4 < var2.length; var4 += 2) {
         Character var14 = (Character)var2[var4];
         ItemStack var16 = null;
         if (var2[var4 + 1] instanceof Item) {
            var16 = new ItemStack((Item)var2[var4 + 1]);
         } else if (var2[var4 + 1] instanceof Block) {
            var16 = new ItemStack((Block)var2[var4 + 1], 1, 32767);
         } else if (var2[var4 + 1] instanceof ItemStack) {
            var16 = (ItemStack)var2[var4 + 1];
         }

         var13.put(var14, var16);
      }

      ItemStack[] var15 = new ItemStack[var5 * var6];

      for(int var17 = 0; var17 < var5 * var6; ++var17) {
         char var19 = var3.charAt(var17);
         if (var13.containsKey(Character.valueOf(var19))) {
            var15[var17] = ((ItemStack)var13.get(Character.valueOf(var19))).copy();
         } else {
            var15[var17] = null;
         }
      }

      ShapedRecipes var18 = new ShapedRecipes(var5, var6, var15, var1);
      this.recipes.add(var18);
      return var18;
   }

   public void addShapelessRecipe(ItemStack var1, Object... var2) {
      ArrayList var3 = Lists.newArrayList();

      for(Object var7 : var2) {
         if (var7 instanceof ItemStack) {
            var3.add(((ItemStack)var7).copy());
         } else if (var7 instanceof Item) {
            var3.add(new ItemStack((Item)var7));
         } else {
            if (!(var7 instanceof Block)) {
               throw new IllegalArgumentException("Invalid shapeless recipe: unknown type " + var7.getClass().getName() + "!");
            }

            var3.add(new ItemStack((Block)var7));
         }
      }

      this.recipes.add(new ShapelessRecipes(var1, var3));
   }

   public void addRecipe(IRecipe var1) {
      this.recipes.add(var1);
   }

   @Nullable
   public ItemStack findMatchingRecipe(InventoryCrafting var1, World var2) {
      for(IRecipe var4 : this.recipes) {
         if (var4.matches(var1, var2)) {
            return var4.getCraftingResult(var1);
         }
      }

      return null;
   }

   public ItemStack[] getRemainingItems(InventoryCrafting var1, World var2) {
      for(IRecipe var4 : this.recipes) {
         if (var4.matches(var1, var2)) {
            return var4.getRemainingItems(var1);
         }
      }

      ItemStack[] var5 = new ItemStack[var1.getSizeInventory()];

      for(int var6 = 0; var6 < var5.length; ++var6) {
         var5[var6] = var1.getStackInSlot(var6);
      }

      return var5;
   }

   public List getRecipeList() {
      return this.recipes;
   }
}
