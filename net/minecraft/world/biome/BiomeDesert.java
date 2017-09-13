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
      super(var1);
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
      super.decorate(var1, var2, var3);
      if (TerrainGen.decorate(var1, var2, var3, EventType.DESERT_WELL) && var2.nextInt(1000) == 0) {
         int var4 = var2.nextInt(16) + 8;
         int var5 = var2.nextInt(16) + 8;
         BlockPos var6 = var1.getHeight(var3.add(var4, 0, var5)).up();
         (new WorldGenDesertWells()).generate(var1, var2, var6);
      }

      if (TerrainGen.decorate(var1, var2, var3, EventType.FOSSIL) && var2.nextInt(64) == 0) {
         (new WorldGenFossils()).generate(var1, var2, var3);
      }

   }
}
