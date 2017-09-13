package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import net.minecraft.world.gen.feature.WorldGenFossils;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

public class BiomeDesert extends Biome {
   public BiomeDesert(Biome.BiomeProperties var1) {
      super(properties);
      this.spawnableCreatureList.clear();
      this.topBlock = Blocks.SAND.getDefaultState();
      this.fillerBlock = Blocks.SAND.getDefaultState();
      this.theBiomeDecorator.treesPerChunk = -999;
      this.theBiomeDecorator.deadBushPerChunk = 2;
      this.theBiomeDecorator.reedsPerChunk = 50;
      this.theBiomeDecorator.cactiPerChunk = 10;
      this.spawnableCreatureList.clear();
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityRabbit.class, 4, 2, 3));
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      super.decorate(worldIn, rand, pos);
      if (TerrainGen.decorate(worldIn, rand, pos, EventType.DESERT_WELL) && rand.nextInt(1000) == 0) {
         int i = rand.nextInt(16) + 8;
         int j = rand.nextInt(16) + 8;
         BlockPos blockpos = worldIn.getHeight(pos.add(i, 0, j)).up();
         (new WorldGenDesertWells()).generate(worldIn, rand, blockpos);
      }

      if (TerrainGen.decorate(worldIn, rand, pos, EventType.FOSSIL) && rand.nextInt(64) == 0) {
         (new WorldGenFossils()).generate(worldIn, rand, pos);
      }

   }
}
