package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenFossils;

public class BiomeSwamp extends Biome {
   protected static final IBlockState WATER_LILY = Blocks.WATERLILY.getDefaultState();

   protected BiomeSwamp(Biome.BiomeProperties var1) {
      super(var1);
      this.theBiomeDecorator.treesPerChunk = 2;
      this.theBiomeDecorator.flowersPerChunk = 1;
      this.theBiomeDecorator.deadBushPerChunk = 1;
      this.theBiomeDecorator.mushroomsPerChunk = 8;
      this.theBiomeDecorator.reedsPerChunk = 10;
      this.theBiomeDecorator.clayPerChunk = 1;
      this.theBiomeDecorator.waterlilyPerChunk = 4;
      this.theBiomeDecorator.sandPerChunk2 = 0;
      this.theBiomeDecorator.sandPerChunk = 0;
      this.theBiomeDecorator.grassPerChunk = 5;
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySlime.class, 1, 1, 1));
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return SWAMP_FEATURE;
   }

   public BlockFlower.EnumFlowerType pickRandomFlower(Random var1, BlockPos var2) {
      return BlockFlower.EnumFlowerType.BLUE_ORCHID;
   }

   public void genTerrainBlocks(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      double var8 = GRASS_COLOR_NOISE.getValue((double)var4 * 0.25D, (double)var5 * 0.25D);
      if (var8 > 0.0D) {
         int var10 = var4 & 15;
         int var11 = var5 & 15;

         for(int var12 = 255; var12 >= 0; --var12) {
            if (var3.getBlockState(var11, var12, var10).getMaterial() != Material.AIR) {
               if (var12 == 62 && var3.getBlockState(var11, var12, var10).getBlock() != Blocks.WATER) {
                  var3.setBlockState(var11, var12, var10, WATER);
                  if (var8 < 0.12D) {
                     var3.setBlockState(var11, var12 + 1, var10, WATER_LILY);
                  }
               }
               break;
            }
         }
      }

      this.generateBiomeTerrain(var1, var2, var3, var4, var5, var6);
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      super.decorate(var1, var2, var3);
      if (var2.nextInt(64) == 0) {
         (new WorldGenFossils()).generate(var1, var2, var3);
      }

   }
}
