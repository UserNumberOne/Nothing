package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

public class BiomePlains extends Biome {
   protected boolean sunflowers;

   public BiomePlains(boolean var1, Biome.BiomeProperties var2) {
      super(properties);
      this.sunflowers = p_i46699_1_;
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHorse.class, 5, 2, 6));
      this.theBiomeDecorator.treesPerChunk = 0;
      this.theBiomeDecorator.extraTreeChance = 0.05F;
      this.theBiomeDecorator.flowersPerChunk = 4;
      this.theBiomeDecorator.grassPerChunk = 10;
   }

   public BlockFlower.EnumFlowerType pickRandomFlower(Random var1, BlockPos var2) {
      double d0 = GRASS_COLOR_NOISE.getValue((double)pos.getX() / 200.0D, (double)pos.getZ() / 200.0D);
      if (d0 < -0.8D) {
         int j = rand.nextInt(4);
         switch(j) {
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
      } else if (rand.nextInt(3) > 0) {
         int i = rand.nextInt(3);
         return i == 0 ? BlockFlower.EnumFlowerType.POPPY : (i == 1 ? BlockFlower.EnumFlowerType.HOUSTONIA : BlockFlower.EnumFlowerType.OXEYE_DAISY);
      } else {
         return BlockFlower.EnumFlowerType.DANDELION;
      }
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      double d0 = GRASS_COLOR_NOISE.getValue((double)(pos.getX() + 8) / 200.0D, (double)(pos.getZ() + 8) / 200.0D);
      if (d0 < -0.8D) {
         this.theBiomeDecorator.flowersPerChunk = 15;
         this.theBiomeDecorator.grassPerChunk = 5;
      } else {
         this.theBiomeDecorator.flowersPerChunk = 4;
         this.theBiomeDecorator.grassPerChunk = 10;
         DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
         if (TerrainGen.decorate(worldIn, rand, pos, EventType.GRASS)) {
            for(int i = 0; i < 7; ++i) {
               int j = rand.nextInt(16) + 8;
               int k = rand.nextInt(16) + 8;
               int l = rand.nextInt(worldIn.getHeight(pos.add(j, 0, k)).getY() + 32);
               DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j, l, k));
            }
         }
      }

      if (this.sunflowers && TerrainGen.decorate(worldIn, rand, pos, EventType.FLOWERS)) {
         DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SUNFLOWER);

         for(int i1 = 0; i1 < 10; ++i1) {
            int j1 = rand.nextInt(16) + 8;
            int k1 = rand.nextInt(16) + 8;
            int l1 = rand.nextInt(worldIn.getHeight(pos.add(j1, 0, k1)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j1, l1, k1));
         }
      }

      super.decorate(worldIn, rand, pos);
   }

   public void addDefaultFlowers() {
      BlockFlower red = Blocks.RED_FLOWER;
      BlockFlower yel = Blocks.YELLOW_FLOWER;
      this.addFlower(red.getDefaultState().withProperty(red.getTypeProperty(), BlockFlower.EnumFlowerType.ORANGE_TULIP), 3);
      this.addFlower(red.getDefaultState().withProperty(red.getTypeProperty(), BlockFlower.EnumFlowerType.RED_TULIP), 3);
      this.addFlower(red.getDefaultState().withProperty(red.getTypeProperty(), BlockFlower.EnumFlowerType.PINK_TULIP), 3);
      this.addFlower(red.getDefaultState().withProperty(red.getTypeProperty(), BlockFlower.EnumFlowerType.WHITE_TULIP), 3);
      this.addFlower(red.getDefaultState().withProperty(red.getTypeProperty(), BlockFlower.EnumFlowerType.POPPY), 20);
      this.addFlower(red.getDefaultState().withProperty(red.getTypeProperty(), BlockFlower.EnumFlowerType.HOUSTONIA), 20);
      this.addFlower(red.getDefaultState().withProperty(red.getTypeProperty(), BlockFlower.EnumFlowerType.OXEYE_DAISY), 20);
      this.addFlower(yel.getDefaultState().withProperty(yel.getTypeProperty(), BlockFlower.EnumFlowerType.DANDELION), 30);
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(rand.nextInt(3) == 0 ? BIG_TREE_FEATURE : TREE_FEATURE);
   }
}
