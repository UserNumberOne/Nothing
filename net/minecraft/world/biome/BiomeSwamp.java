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
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeSwamp extends Biome {
   protected static final IBlockState WATER_LILY = Blocks.WATERLILY.getDefaultState();

   protected BiomeSwamp(Biome.BiomeProperties var1) {
      super(properties);
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
      double d0 = GRASS_COLOR_NOISE.getValue((double)x * 0.25D, (double)z * 0.25D);
      if (d0 > 0.0D) {
         int i = x & 15;
         int j = z & 15;

         for(int k = 255; k >= 0; --k) {
            if (chunkPrimerIn.getBlockState(j, k, i).getMaterial() != Material.AIR) {
               if (k == 62 && chunkPrimerIn.getBlockState(j, k, i).getBlock() != Blocks.WATER) {
                  chunkPrimerIn.setBlockState(j, k, i, WATER);
                  if (d0 < 0.12D) {
                     chunkPrimerIn.setBlockState(j, k + 1, i, WATER_LILY);
                  }
               }
               break;
            }
         }
      }

      this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      super.decorate(worldIn, rand, pos);
      if (TerrainGen.decorate(worldIn, rand, pos, EventType.FOSSIL) && rand.nextInt(64) == 0) {
         (new WorldGenFossils()).generate(worldIn, rand, pos);
      }

   }

   @SideOnly(Side.CLIENT)
   public int getGrassColorAtPos(BlockPos var1) {
      double d0 = GRASS_COLOR_NOISE.getValue((double)pos.getX() * 0.0225D, (double)pos.getZ() * 0.0225D);
      return d0 < -0.1D ? 5011004 : 6975545;
   }

   @SideOnly(Side.CLIENT)
   public int getFoliageColorAtPos(BlockPos var1) {
      return 6975545;
   }

   public void addDefaultFlowers() {
      this.addFlower(Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.BLUE_ORCHID), 10);
   }
}
