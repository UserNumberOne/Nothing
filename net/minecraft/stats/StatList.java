package net.minecraft.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.GameData;

public class StatList {
   protected static final Map ID_TO_STAT_MAP = Maps.newHashMap();
   public static final List ALL_STATS = Lists.newArrayList();
   public static final List BASIC_STATS = Lists.newArrayList();
   public static final List USE_ITEM_STATS = Lists.newArrayList();
   public static final List MINE_BLOCK_STATS = Lists.newArrayList();
   public static final StatBase LEAVE_GAME = (new StatBasic("stat.leaveGame", new TextComponentTranslation("stat.leaveGame", new Object[0]))).initIndependentStat().registerStat();
   public static final StatBase PLAY_ONE_MINUTE = (new StatBasic("stat.playOneMinute", new TextComponentTranslation("stat.playOneMinute", new Object[0]), StatBase.timeStatType)).initIndependentStat().registerStat();
   public static final StatBase TIME_SINCE_DEATH = (new StatBasic("stat.timeSinceDeath", new TextComponentTranslation("stat.timeSinceDeath", new Object[0]), StatBase.timeStatType)).initIndependentStat().registerStat();
   public static final StatBase SNEAK_TIME = (new StatBasic("stat.sneakTime", new TextComponentTranslation("stat.sneakTime", new Object[0]), StatBase.timeStatType)).initIndependentStat().registerStat();
   public static final StatBase WALK_ONE_CM = (new StatBasic("stat.walkOneCm", new TextComponentTranslation("stat.walkOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase CROUCH_ONE_CM = (new StatBasic("stat.crouchOneCm", new TextComponentTranslation("stat.crouchOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase SPRINT_ONE_CM = (new StatBasic("stat.sprintOneCm", new TextComponentTranslation("stat.sprintOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase SWIM_ONE_CM = (new StatBasic("stat.swimOneCm", new TextComponentTranslation("stat.swimOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase FALL_ONE_CM = (new StatBasic("stat.fallOneCm", new TextComponentTranslation("stat.fallOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase CLIMB_ONE_CM = (new StatBasic("stat.climbOneCm", new TextComponentTranslation("stat.climbOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase FLY_ONE_CM = (new StatBasic("stat.flyOneCm", new TextComponentTranslation("stat.flyOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase DIVE_ONE_CM = (new StatBasic("stat.diveOneCm", new TextComponentTranslation("stat.diveOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase MINECART_ONE_CM = (new StatBasic("stat.minecartOneCm", new TextComponentTranslation("stat.minecartOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase BOAT_ONE_CM = (new StatBasic("stat.boatOneCm", new TextComponentTranslation("stat.boatOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase PIG_ONE_CM = (new StatBasic("stat.pigOneCm", new TextComponentTranslation("stat.pigOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase HORSE_ONE_CM = (new StatBasic("stat.horseOneCm", new TextComponentTranslation("stat.horseOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase AVIATE_ONE_CM = (new StatBasic("stat.aviateOneCm", new TextComponentTranslation("stat.aviateOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
   public static final StatBase JUMP = (new StatBasic("stat.jump", new TextComponentTranslation("stat.jump", new Object[0]))).initIndependentStat().registerStat();
   public static final StatBase DROP = (new StatBasic("stat.drop", new TextComponentTranslation("stat.drop", new Object[0]))).initIndependentStat().registerStat();
   public static final StatBase DAMAGE_DEALT = (new StatBasic("stat.damageDealt", new TextComponentTranslation("stat.damageDealt", new Object[0]), StatBase.divideByTen)).registerStat();
   public static final StatBase DAMAGE_TAKEN = (new StatBasic("stat.damageTaken", new TextComponentTranslation("stat.damageTaken", new Object[0]), StatBase.divideByTen)).registerStat();
   public static final StatBase DEATHS = (new StatBasic("stat.deaths", new TextComponentTranslation("stat.deaths", new Object[0]))).registerStat();
   public static final StatBase MOB_KILLS = (new StatBasic("stat.mobKills", new TextComponentTranslation("stat.mobKills", new Object[0]))).registerStat();
   public static final StatBase ANIMALS_BRED = (new StatBasic("stat.animalsBred", new TextComponentTranslation("stat.animalsBred", new Object[0]))).registerStat();
   public static final StatBase PLAYER_KILLS = (new StatBasic("stat.playerKills", new TextComponentTranslation("stat.playerKills", new Object[0]))).registerStat();
   public static final StatBase FISH_CAUGHT = (new StatBasic("stat.fishCaught", new TextComponentTranslation("stat.fishCaught", new Object[0]))).registerStat();
   public static final StatBase JUNK_FISHED = (new StatBasic("stat.junkFished", new TextComponentTranslation("stat.junkFished", new Object[0]))).registerStat();
   public static final StatBase TREASURE_FISHED = (new StatBasic("stat.treasureFished", new TextComponentTranslation("stat.treasureFished", new Object[0]))).registerStat();
   public static final StatBase TALKED_TO_VILLAGER = (new StatBasic("stat.talkedToVillager", new TextComponentTranslation("stat.talkedToVillager", new Object[0]))).registerStat();
   public static final StatBase TRADED_WITH_VILLAGER = (new StatBasic("stat.tradedWithVillager", new TextComponentTranslation("stat.tradedWithVillager", new Object[0]))).registerStat();
   public static final StatBase CAKE_SLICES_EATEN = (new StatBasic("stat.cakeSlicesEaten", new TextComponentTranslation("stat.cakeSlicesEaten", new Object[0]))).registerStat();
   public static final StatBase CAULDRON_FILLED = (new StatBasic("stat.cauldronFilled", new TextComponentTranslation("stat.cauldronFilled", new Object[0]))).registerStat();
   public static final StatBase CAULDRON_USED = (new StatBasic("stat.cauldronUsed", new TextComponentTranslation("stat.cauldronUsed", new Object[0]))).registerStat();
   public static final StatBase ARMOR_CLEANED = (new StatBasic("stat.armorCleaned", new TextComponentTranslation("stat.armorCleaned", new Object[0]))).registerStat();
   public static final StatBase BANNER_CLEANED = (new StatBasic("stat.bannerCleaned", new TextComponentTranslation("stat.bannerCleaned", new Object[0]))).registerStat();
   public static final StatBase BREWINGSTAND_INTERACTION = (new StatBasic("stat.brewingstandInteraction", new TextComponentTranslation("stat.brewingstandInteraction", new Object[0]))).registerStat();
   public static final StatBase BEACON_INTERACTION = (new StatBasic("stat.beaconInteraction", new TextComponentTranslation("stat.beaconInteraction", new Object[0]))).registerStat();
   public static final StatBase DROPPER_INSPECTED = (new StatBasic("stat.dropperInspected", new TextComponentTranslation("stat.dropperInspected", new Object[0]))).registerStat();
   public static final StatBase HOPPER_INSPECTED = (new StatBasic("stat.hopperInspected", new TextComponentTranslation("stat.hopperInspected", new Object[0]))).registerStat();
   public static final StatBase DISPENSER_INSPECTED = (new StatBasic("stat.dispenserInspected", new TextComponentTranslation("stat.dispenserInspected", new Object[0]))).registerStat();
   public static final StatBase NOTEBLOCK_PLAYED = (new StatBasic("stat.noteblockPlayed", new TextComponentTranslation("stat.noteblockPlayed", new Object[0]))).registerStat();
   public static final StatBase NOTEBLOCK_TUNED = (new StatBasic("stat.noteblockTuned", new TextComponentTranslation("stat.noteblockTuned", new Object[0]))).registerStat();
   public static final StatBase FLOWER_POTTED = (new StatBasic("stat.flowerPotted", new TextComponentTranslation("stat.flowerPotted", new Object[0]))).registerStat();
   public static final StatBase TRAPPED_CHEST_TRIGGERED = (new StatBasic("stat.trappedChestTriggered", new TextComponentTranslation("stat.trappedChestTriggered", new Object[0]))).registerStat();
   public static final StatBase ENDERCHEST_OPENED = (new StatBasic("stat.enderchestOpened", new TextComponentTranslation("stat.enderchestOpened", new Object[0]))).registerStat();
   public static final StatBase ITEM_ENCHANTED = (new StatBasic("stat.itemEnchanted", new TextComponentTranslation("stat.itemEnchanted", new Object[0]))).registerStat();
   public static final StatBase RECORD_PLAYED = (new StatBasic("stat.recordPlayed", new TextComponentTranslation("stat.recordPlayed", new Object[0]))).registerStat();
   public static final StatBase FURNACE_INTERACTION = (new StatBasic("stat.furnaceInteraction", new TextComponentTranslation("stat.furnaceInteraction", new Object[0]))).registerStat();
   public static final StatBase CRAFTING_TABLE_INTERACTION = (new StatBasic("stat.craftingTableInteraction", new TextComponentTranslation("stat.workbenchInteraction", new Object[0]))).registerStat();
   public static final StatBase CHEST_OPENED = (new StatBasic("stat.chestOpened", new TextComponentTranslation("stat.chestOpened", new Object[0]))).registerStat();
   public static final StatBase SLEEP_IN_BED = (new StatBasic("stat.sleepInBed", new TextComponentTranslation("stat.sleepInBed", new Object[0]))).registerStat();
   private static final StatBase[] BLOCKS_STATS = new StatBase[4096];
   private static final StatBase[] CRAFTS_STATS = new StatBase[32000];
   private static final StatBase[] OBJECT_USE_STATS = new StatBase[32000];
   private static final StatBase[] OBJECT_BREAK_STATS = new StatBase[32000];
   private static final StatBase[] OBJECTS_PICKED_UP_STATS = new StatBase[32000];
   private static final StatBase[] OBJECTS_DROPPED_STATS = new StatBase[32000];

   @Nullable
   public static StatBase getBlockStats(Block var0) {
      return BLOCKS_STATS[Block.getIdFromBlock(var0)];
   }

   @Nullable
   public static StatBase getCraftStats(Item var0) {
      return CRAFTS_STATS[Item.getIdFromItem(var0)];
   }

   @Nullable
   public static StatBase getObjectUseStats(Item var0) {
      return OBJECT_USE_STATS[Item.getIdFromItem(var0)];
   }

   @Nullable
   public static StatBase getObjectBreakStats(Item var0) {
      return OBJECT_BREAK_STATS[Item.getIdFromItem(var0)];
   }

   @Nullable
   public static StatBase getObjectsPickedUpStats(Item var0) {
      return OBJECTS_PICKED_UP_STATS[Item.getIdFromItem(var0)];
   }

   @Nullable
   public static StatBase getDroppedObjectStats(Item var0) {
      return OBJECTS_DROPPED_STATS[Item.getIdFromItem(var0)];
   }

   public static void init() {
      initMiningStats();
      initStats();
      initItemDepleteStats();
      initCraftableStats();
      initPickedUpAndDroppedStats();
      AchievementList.init();
      EntityList.init();
   }

   private static void initCraftableStats() {
      HashSet var0 = Sets.newHashSet();

      for(IRecipe var2 : CraftingManager.getInstance().getRecipeList()) {
         if (var2.getRecipeOutput() != null) {
            var0.add(var2.getRecipeOutput().getItem());
         }
      }

      for(ItemStack var7 : FurnaceRecipes.instance().getSmeltingList().values()) {
         var0.add(var7.getItem());
      }

      for(Item var8 : var0) {
         if (var8 != null) {
            int var3 = Item.getIdFromItem(var8);
            String var4 = getItemName(var8);
            if (var4 != null) {
               CRAFTS_STATS[var3] = (new StatCrafting("stat.craftItem.", var4, new TextComponentTranslation("stat.craftItem", new Object[]{(new ItemStack(var8)).getTextComponent()}), var8)).registerStat();
            }
         }
      }

      replaceAllSimilarBlocks(CRAFTS_STATS, true);
   }

   private static void initMiningStats() {
      for(Block var1 : GameData.getBlockRegistry().typeSafeIterable()) {
         Item var2 = Item.getItemFromBlock(var1);
         if (var2 != null) {
            int var3 = Block.getIdFromBlock(var1);
            String var4 = getItemName(var2);
            if (var4 != null && var1.getEnableStats()) {
               BLOCKS_STATS[var3] = (new StatCrafting("stat.mineBlock.", var4, new TextComponentTranslation("stat.mineBlock", new Object[]{(new ItemStack(var1)).getTextComponent()}), var2)).registerStat();
               MINE_BLOCK_STATS.add((StatCrafting)BLOCKS_STATS[var3]);
            }
         }
      }

      replaceAllSimilarBlocks(BLOCKS_STATS, false);
   }

   private static void initStats() {
      for(Item var1 : GameData.getItemRegistry().typeSafeIterable()) {
         if (var1 != null) {
            int var2 = Item.getIdFromItem(var1);
            String var3 = getItemName(var1);
            if (var3 != null) {
               OBJECT_USE_STATS[var2] = (new StatCrafting("stat.useItem.", var3, new TextComponentTranslation("stat.useItem", new Object[]{(new ItemStack(var1)).getTextComponent()}), var1)).registerStat();
               if (!(var1 instanceof ItemBlock)) {
                  USE_ITEM_STATS.add((StatCrafting)OBJECT_USE_STATS[var2]);
               }
            }
         }
      }

      replaceAllSimilarBlocks(OBJECT_USE_STATS, true);
   }

   private static void initItemDepleteStats() {
      for(Item var1 : GameData.getItemRegistry().typeSafeIterable()) {
         if (var1 != null) {
            int var2 = Item.getIdFromItem(var1);
            String var3 = getItemName(var1);
            if (var3 != null && var1.isDamageable()) {
               OBJECT_BREAK_STATS[var2] = (new StatCrafting("stat.breakItem.", var3, new TextComponentTranslation("stat.breakItem", new Object[]{(new ItemStack(var1)).getTextComponent()}), var1)).registerStat();
            }
         }
      }

      replaceAllSimilarBlocks(OBJECT_BREAK_STATS, true);
   }

   private static void initPickedUpAndDroppedStats() {
      for(Item var1 : GameData.getItemRegistry().typeSafeIterable()) {
         if (var1 != null) {
            int var2 = Item.getIdFromItem(var1);
            String var3 = getItemName(var1);
            if (var3 != null) {
               OBJECTS_PICKED_UP_STATS[var2] = (new StatCrafting("stat.pickup.", var3, new TextComponentTranslation("stat.pickup", new Object[]{(new ItemStack(var1)).getTextComponent()}), var1)).registerStat();
               OBJECTS_DROPPED_STATS[var2] = (new StatCrafting("stat.drop.", var3, new TextComponentTranslation("stat.drop", new Object[]{(new ItemStack(var1)).getTextComponent()}), var1)).registerStat();
            }
         }
      }

      replaceAllSimilarBlocks(OBJECT_BREAK_STATS, true);
   }

   private static String getItemName(Item var0) {
      ResourceLocation var1 = (ResourceLocation)Item.REGISTRY.getNameForObject(var0);
      return var1 != null ? var1.toString().replace(':', '.') : null;
   }

   private static void replaceAllSimilarBlocks(StatBase[] var0, boolean var1) {
      mergeStatBases(var0, Blocks.WATER, Blocks.FLOWING_WATER, var1);
      mergeStatBases(var0, Blocks.LAVA, Blocks.FLOWING_LAVA, var1);
      mergeStatBases(var0, Blocks.LIT_PUMPKIN, Blocks.PUMPKIN, var1);
      mergeStatBases(var0, Blocks.LIT_FURNACE, Blocks.FURNACE, var1);
      mergeStatBases(var0, Blocks.LIT_REDSTONE_ORE, Blocks.REDSTONE_ORE, var1);
      mergeStatBases(var0, Blocks.POWERED_REPEATER, Blocks.UNPOWERED_REPEATER, var1);
      mergeStatBases(var0, Blocks.POWERED_COMPARATOR, Blocks.UNPOWERED_COMPARATOR, var1);
      mergeStatBases(var0, Blocks.REDSTONE_TORCH, Blocks.UNLIT_REDSTONE_TORCH, var1);
      mergeStatBases(var0, Blocks.LIT_REDSTONE_LAMP, Blocks.REDSTONE_LAMP, var1);
      mergeStatBases(var0, Blocks.DOUBLE_STONE_SLAB, Blocks.STONE_SLAB, var1);
      mergeStatBases(var0, Blocks.DOUBLE_WOODEN_SLAB, Blocks.WOODEN_SLAB, var1);
      mergeStatBases(var0, Blocks.DOUBLE_STONE_SLAB2, Blocks.STONE_SLAB2, var1);
      mergeStatBases(var0, Blocks.GRASS, Blocks.DIRT, var1);
      mergeStatBases(var0, Blocks.FARMLAND, Blocks.DIRT, var1);
   }

   private static void mergeStatBases(StatBase[] var0, Block var1, Block var2, boolean var3) {
      int var4;
      int var5;
      if (var3) {
         var4 = Item.getIdFromItem(Item.getItemFromBlock(var1));
         var5 = Item.getIdFromItem(Item.getItemFromBlock(var2));
      } else {
         var4 = Block.getIdFromBlock(var1);
         var5 = Block.getIdFromBlock(var2);
      }

      if (var0[var4] != null && var0[var5] == null) {
         var0[var5] = var0[var4];
      } else {
         ALL_STATS.remove(var0[var4]);
         MINE_BLOCK_STATS.remove(var0[var4]);
         BASIC_STATS.remove(var0[var4]);
         var0[var4] = var0[var5];
      }

   }

   public static StatBase getStatKillEntity(EntityList.EntityEggInfo var0) {
      return var0.spawnedID == null ? null : (new StatBase("stat.killEntity." + var0.spawnedID, new TextComponentTranslation("stat.entityKill", new Object[]{new TextComponentTranslation("entity." + var0.spawnedID + ".name", new Object[0])}))).registerStat();
   }

   public static StatBase getStatEntityKilledBy(EntityList.EntityEggInfo var0) {
      return var0.spawnedID == null ? null : (new StatBase("stat.entityKilledBy." + var0.spawnedID, new TextComponentTranslation("stat.entityKilledBy", new Object[]{new TextComponentTranslation("entity." + var0.spawnedID + ".name", new Object[0])}))).registerStat();
   }

   public static StatBase getOneShotStat(String var0) {
      return (StatBase)ID_TO_STAT_MAP.get(var0);
   }

   /** @deprecated */
   @Deprecated
   public static void reinit() {
      ID_TO_STAT_MAP.clear();
      BASIC_STATS.clear();
      USE_ITEM_STATS.clear();
      MINE_BLOCK_STATS.clear();

      for(StatBase[] var3 : new StatBase[][]{BLOCKS_STATS, CRAFTS_STATS, OBJECT_USE_STATS, OBJECT_BREAK_STATS, OBJECTS_PICKED_UP_STATS, OBJECTS_DROPPED_STATS}) {
         for(int var4 = 0; var4 < var3.length; ++var4) {
            if (var3[var4] != null) {
               ALL_STATS.remove(var3[var4]);
               var3[var4] = null;
            }
         }
      }

      ArrayList var5 = Lists.newArrayList(ALL_STATS);
      ALL_STATS.clear();

      for(StatBase var7 : var5) {
         var7.registerStat();
      }

      initMiningStats();
      initStats();
      initItemDepleteStats();
      initCraftableStats();
      initPickedUpAndDroppedStats();
   }
}
