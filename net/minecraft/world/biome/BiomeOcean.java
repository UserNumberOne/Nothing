package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeOcean extends Biome {
   public BiomeOcean(Biome.BiomeProperties var1) {
      super(var1);
      this.spawnableCreatureList.clear();
   }

   public Biome.TempCategory getTempCategory() {
      return Biome.TempCategory.OCEAN;
   }

   public void genTerrainBlocks(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      super.genTerrainBlocks(var1, var2, var3, var4, var5, var6);
   }
}
