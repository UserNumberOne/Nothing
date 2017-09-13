package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomePlains extends Biome {
   protected boolean sunflowers;

   protected BiomePlains(boolean var1, Biome.BiomeProperties var2) {
      super(var2);
      this.sunflowers = var1;
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHorse.class, 5, 2, 6));
      this.theBiomeDecorator.treesPerChunk = 0;
      this.theBiomeDecorator.extraTreeChance = 0.05F;
      this.theBiomeDecorator.flowersPerChunk = 4;
      this.theBiomeDecorator.grassPerChunk = 10;
   }

   public BlockFlower.EnumFlowerType pickRandomFlower(Random var1, BlockPos var2) {
      double var3 = GRASS_COLOR_NOISE.getValue((double)var2.getX() / 200.0D, (double)var2.getZ() / 200.0D);
      if (var3 < -0.8D) {
         int var6 = var1.nextInt(4);
         switch(var6) {
         case 0:
            return BlockFlower.EnumFlowerType.ORANGE_TULIP;
         case 1:
            return BlockFlower.EnumFlowerType.RED_TULIP;
         case 2:
            return BlockFlower.EnumFlowerType.PINK_TULIP;
         case 3:
         default:
            return BlockFlower.EnumFlowerType.WHITE_TULIP;
         }
      } else if (var1.nextInt(3) > 0) {
         int var5 = var1.nextInt(3);
         if (var5 == 0) {
            return BlockFlower.EnumFlowerType.POPPY;
         } else {
            return var5 == 1 ? BlockFlower.EnumFlowerType.HOUSTONIA : BlockFlower.EnumFlowerType.OXEYE_DAISY;
         }
      } else {
         return BlockFlower.EnumFlowerType.DANDELION;
      }
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      double var4 = GRASS_COLOR_NOISE.getValue((double)(var3.getX() + 8) / 200.0D, (double)(var3.getZ() + 8) / 200.0D);
      if (var4 < -0.8D) {
         this.theBiomeDecorator.flowersPerChunk = 15;
         this.theBiomeDecorator.grassPerChunk = 5;
      } else {
         this.theBiomeDecorator.flowersPerChunk = 4;
         this.theBiomeDecorator.grassPerChunk = 10;
         DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);

         for(int var6 = 0; var6 < 7; ++var6) {
            int var7 = var2.nextInt(16) + 8;
            int var8 = var2.nextInt(16) + 8;
            int var9 = var2.nextInt(var1.getHeight(var3.add(var7, 0, var8)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(var1, var2, var3.add(var7, var9, var8));
         }
      }

      if (this.sunflowers) {
         DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SUNFLOWER);

         for(int var10 = 0; var10 < 10; ++var10) {
            int var11 = var2.nextInt(16) + 8;
            int var12 = var2.nextInt(16) + 8;
            int var13 = var2.nextInt(var1.getHeight(var3.add(var11, 0, var12)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(var1, var2, var3.add(var11, var13, var12));
         }
      }

      super.decorate(var1, var2, var3);
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(var1.nextInt(3) == 0 ? BIG_TREE_FEATURE : TREE_FEATURE);
   }
}
