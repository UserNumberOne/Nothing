package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

public class BiomeTaiga extends Biome {
   private static final WorldGenTaiga1 PINE_GENERATOR = new WorldGenTaiga1();
   private static final WorldGenTaiga2 SPRUCE_GENERATOR = new WorldGenTaiga2(false);
   private static final WorldGenMegaPineTree MEGA_PINE_GENERATOR = new WorldGenMegaPineTree(false, false);
   private static final WorldGenMegaPineTree MEGA_SPRUCE_GENERATOR = new WorldGenMegaPineTree(false, true);
   private static final WorldGenBlockBlob FOREST_ROCK_GENERATOR = new WorldGenBlockBlob(Blocks.MOSSY_COBBLESTONE, 0);
   private final BiomeTaiga.Type type;

   public BiomeTaiga(BiomeTaiga.Type var1, Biome.BiomeProperties var2) {
      super(var2);
      this.type = var1;
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityWolf.class, 8, 4, 4));
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityRabbit.class, 4, 2, 3));
      this.theBiomeDecorator.treesPerChunk = 10;
      if (var1 != BiomeTaiga.Type.MEGA && var1 != BiomeTaiga.Type.MEGA_SPRUCE) {
         this.theBiomeDecorator.grassPerChunk = 1;
         this.theBiomeDecorator.mushroomsPerChunk = 1;
      } else {
         this.theBiomeDecorator.grassPerChunk = 7;
         this.theBiomeDecorator.deadBushPerChunk = 1;
         this.theBiomeDecorator.mushroomsPerChunk = 3;
      }

   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)((this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) && var1.nextInt(3) == 0 ? (this.type != BiomeTaiga.Type.MEGA_SPRUCE && var1.nextInt(13) != 0 ? MEGA_PINE_GENERATOR : MEGA_SPRUCE_GENERATOR) : (var1.nextInt(3) == 0 ? PINE_GENERATOR : SPRUCE_GENERATOR));
   }

   public WorldGenerator getRandomWorldGenForGrass(Random var1) {
      return var1.nextInt(5) > 0 ? new WorldGenTallGrass(BlockTallGrass.EnumType.FERN) : new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      if ((this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) && TerrainGen.decorate(var1, var2, var3, EventType.ROCK)) {
         int var4 = var2.nextInt(3);

         for(int var5 = 0; var5 < var4; ++var5) {
            int var6 = var2.nextInt(16) + 8;
            int var7 = var2.nextInt(16) + 8;
            BlockPos var8 = var1.getHeight(var3.add(var6, 0, var7));
            FOREST_ROCK_GENERATOR.generate(var1, var2, var8);
         }
      }

      DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.FERN);
      if (TerrainGen.decorate(var1, var2, var3, EventType.FLOWERS)) {
         for(int var9 = 0; var9 < 7; ++var9) {
            int var10 = var2.nextInt(16) + 8;
            int var11 = var2.nextInt(16) + 8;
            int var12 = var2.nextInt(var1.getHeight(var3.add(var10, 0, var11)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(var1, var2, var3.add(var10, var12, var11));
         }
      }

      super.decorate(var1, var2, var3);
   }

   public void genTerrainBlocks(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      if (this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) {
         this.topBlock = Blocks.GRASS.getDefaultState();
         this.fillerBlock = Blocks.DIRT.getDefaultState();
         if (var6 > 1.75D) {
            this.topBlock = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
         } else if (var6 > -0.95D) {
            this.topBlock = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
         }
      }

      this.generateBiomeTerrain(var1, var2, var3, var4, var5, var6);
   }

   public static enum Type {
      NORMAL,
      MEGA,
      MEGA_SPRUCE;
   }
}
