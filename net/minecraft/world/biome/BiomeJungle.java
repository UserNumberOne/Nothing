package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMelon;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenVines;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

public class BiomeJungle extends Biome {
   private final boolean isEdge;
   private static final IBlockState JUNGLE_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
   private static final IBlockState JUNGLE_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
   private static final IBlockState OAK_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

   public BiomeJungle(boolean var1, Biome.BiomeProperties var2) {
      super(var2);
      this.isEdge = var1;
      if (var1) {
         this.theBiomeDecorator.treesPerChunk = 2;
      } else {
         this.theBiomeDecorator.treesPerChunk = 50;
      }

      this.theBiomeDecorator.grassPerChunk = 25;
      this.theBiomeDecorator.flowersPerChunk = 4;
      if (!var1) {
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityOcelot.class, 2, 1, 1));
      }

      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityChicken.class, 10, 4, 4));
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(var1.nextInt(10) == 0 ? BIG_TREE_FEATURE : (var1.nextInt(2) == 0 ? new WorldGenShrub(JUNGLE_LOG, OAK_LEAF) : (!this.isEdge && var1.nextInt(3) == 0 ? new WorldGenMegaJungle(false, 10, 20, JUNGLE_LOG, JUNGLE_LEAF) : new WorldGenTrees(false, 4 + var1.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, true))));
   }

   public WorldGenerator getRandomWorldGenForGrass(Random var1) {
      return var1.nextInt(4) == 0 ? new WorldGenTallGrass(BlockTallGrass.EnumType.FERN) : new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      super.decorate(var1, var2, var3);
      int var4 = var2.nextInt(16) + 8;
      int var5 = var2.nextInt(16) + 8;
      int var6 = var1.getHeight(var3.add(var4, 0, var5)).getY() * 2;
      if (var6 < 1) {
         var6 = 1;
      }

      int var7 = var2.nextInt(var6);
      if (TerrainGen.decorate(var1, var2, var3, EventType.PUMPKIN)) {
         (new WorldGenMelon()).generate(var1, var2, var3.add(var4, var7, var5));
      }

      WorldGenVines var8 = new WorldGenVines();
      if (TerrainGen.decorate(var1, var2, var3, EventType.GRASS)) {
         for(int var11 = 0; var11 < 50; ++var11) {
            var7 = var2.nextInt(16) + 8;
            boolean var9 = true;
            int var10 = var2.nextInt(16) + 8;
            var8.generate(var1, var2, var3.add(var7, 128, var10));
         }
      }

   }
}
