package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenBase {
   protected int range = 8;
   protected Random rand = new Random();
   protected World world;

   public void generate(World var1, int var2, int var3, ChunkPrimer var4) {
      int i = this.range;
      this.world = worldIn;
      this.rand.setSeed(worldIn.getSeed());
      long j = this.rand.nextLong();
      long k = this.rand.nextLong();

      for(int l = x - i; l <= x + i; ++l) {
         for(int i1 = z - i; i1 <= z + i; ++i1) {
            long j1 = (long)l * j;
            long k1 = (long)i1 * k;
            this.rand.setSeed(j1 ^ k1 ^ worldIn.getSeed());
            this.recursiveGenerate(worldIn, l, i1, x, z, primer);
         }
      }

   }

   protected void recursiveGenerate(World var1, int var2, int var3, int var4, int var5, ChunkPrimer var6) {
   }
}
