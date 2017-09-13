package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeForest extends Biome {
   protected static final WorldGenBirchTree SUPER_BIRCH_TREE = new WorldGenBirchTree(false, true);
   protected static final WorldGenBirchTree BIRCH_TREE = new WorldGenBirchTree(false, false);
   protected static final WorldGenCanopyTree ROOF_TREE = new WorldGenCanopyTree(false);
   private final BiomeForest.Type type;

   public BiomeForest(BiomeForest.Type var1, Biome.BiomeProperties var2) {
      super(var2);
      this.type = var1;
      this.theBiomeDecorator.treesPerChunk = 10;
      this.theBiomeDecorator.grassPerChunk = 2;
      if (this.type == BiomeForest.Type.FLOWER) {
         this.theBiomeDecorator.treesPerChunk = 6;
         this.theBiomeDecorator.flowersPerChunk = 100;
         this.theBiomeDecorator.grassPerChunk = 1;
         this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityRabbit.class, 4, 2, 3));
      }

      if (this.type == BiomeForest.Type.NORMAL) {
         this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityWolf.class, 5, 4, 4));
      }

      if (this.type == BiomeForest.Type.ROOFED) {
         this.theBiomeDecorator.treesPerChunk = -999;
      }

   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(this.type == BiomeForest.Type.ROOFED && var1.nextInt(3) > 0 ? ROOF_TREE : (this.type != BiomeForest.Type.BIRCH && var1.nextInt(5) != 0 ? (var1.nextInt(10) == 0 ? BIG_TREE_FEATURE : TREE_FEATURE) : BIRCH_TREE));
   }

   public BlockFlower.EnumFlowerType pickRandomFlower(Random var1, BlockPos var2) {
      if (this.type == BiomeForest.Type.FLOWER) {
         double var3 = MathHelper.clamp((1.0D + GRASS_COLOR_NOISE.getValue((double)var2.getX() / 48.0D, (double)var2.getZ() / 48.0D)) / 2.0D, 0.0D, 0.9999D);
         BlockFlower.EnumFlowerType var5 = BlockFlower.EnumFlowerType.values()[(int)(var3 * (double)BlockFlower.EnumFlowerType.values().length)];
         return var5 == BlockFlower.EnumFlowerType.BLUE_ORCHID ? BlockFlower.EnumFlowerType.POPPY : var5;
      } else {
         return super.pickRandomFlower(var1, var2);
      }
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      if (this.type == BiomeForest.Type.ROOFED) {
         this.addMushrooms(var1, var2, var3);
      }

      if (TerrainGen.decorate(var1, var2, var3, EventType.FLOWERS)) {
         int var4 = var2.nextInt(5) - 3;
         if (this.type == BiomeForest.Type.FLOWER) {
            var4 += 2;
         }

         this.addDoublePlants(var1, var2, var3, var4);
      }

      super.decorate(var1, var2, var3);
   }

   public void addMushrooms(World var1, Random var2, BlockPos var3) {
      for(int var4 = 0; var4 < 4; ++var4) {
         for(int var5 = 0; var5 < 4; ++var5) {
            int var6 = var4 * 4 + 1 + 8 + var2.nextInt(3);
            int var7 = var5 * 4 + 1 + 8 + var2.nextInt(3);
            BlockPos var8 = var1.getHeight(var3.add(var6, 0, var7));
            if (var2.nextInt(20) == 0 && TerrainGen.decorate(var1, var2, var8, EventType.BIG_SHROOM)) {
               WorldGenBigMushroom var10 = new WorldGenBigMushroom();
               var10.generate(var1, var2, var8);
            } else if (TerrainGen.decorate(var1, var2, var8, EventType.TREE)) {
               WorldGenAbstractTree var9 = this.genBigTreeChance(var2);
               var9.setDecorationDefaults();
               if (var9.generate(var1, var2, var8)) {
                  var9.generateSaplings(var1, var2, var8);
               }
            }
         }
      }

   }

   public void addDoublePlants(World var1, Random var2, BlockPos var3, int var4) {
      for(int var5 = 0; var5 < var4; ++var5) {
         int var6 = var2.nextInt(3);
         if (var6 == 0) {
            DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SYRINGA);
         } else if (var6 == 1) {
            DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.ROSE);
         } else if (var6 == 2) {
            DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.PAEONIA);
         }

         for(int var7 = 0; var7 < 5; ++var7) {
            int var8 = var2.nextInt(16) + 8;
            int var9 = var2.nextInt(16) + 8;
            int var10 = var2.nextInt(var1.getHeight(var3.add(var8, 0, var9)).getY() + 32);
            if (DOUBLE_PLANT_GENERATOR.generate(var1, var2, new BlockPos(var3.getX() + var8, var10, var3.getZ() + var9))) {
               break;
            }
         }
      }

   }

   public void addDefaultFlowers() {
      if (this.type != BiomeForest.Type.FLOWER) {
         super.addDefaultFlowers();
      } else {
         for(BlockFlower.EnumFlowerType var4 : BlockFlower.EnumFlowerType.values()) {
            if (var4.getBlockType() != BlockFlower.EnumFlowerColor.YELLOW) {
               if (var4 == BlockFlower.EnumFlowerType.BLUE_ORCHID) {
                  var4 = BlockFlower.EnumFlowerType.POPPY;
               }

               this.addFlower(Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), var4), 10);
            }
         }

      }
   }

   public Class getBiomeClass() {
      return BiomeForest.class;
   }

   @SideOnly(Side.CLIENT)
   public int getGrassColorAtPos(BlockPos var1) {
      int var2 = super.getGrassColorAtPos(var1);
      return this.type == BiomeForest.Type.ROOFED ? (var2 & 16711422) + 2634762 >> 1 : var2;
   }

   public static enum Type {
      NORMAL,
      FLOWER,
      BIRCH,
      ROOFED;
   }
}
