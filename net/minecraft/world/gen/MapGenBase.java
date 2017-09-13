package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenBase {
   protected int range = 8;
   protected Random rand = new Random();
   protected World world;

   public void generate(World var1, int var2, int var3, ChunkPrimer var4) {
      int var5 = this.range;
      this.world = var1;
      this.rand.setSeed(var1.getSeed());
      long var6 = this.rand.nextLong();
      long var8 = this.rand.nextLong();

      for(int var10 = var2 - var5; var10 <= var2 + var5; ++var10) {
         for(int var11 = var3 - var5; var11 <= var3 + var5; ++var11) {
            long var12 = (long)var10 * var6;
            long var14 = (long)var11 * var8;
            this.rand.setSeed(var12 ^ var14 ^ var1.getSeed());
            this.recursiveGenerate(var1, var10, var11, var2, var3, var4);
         }
      }

   }

   protected void recursiveGenerate(World var1, int var2, int var3, int var4, int var5, ChunkPrimer var6) {
   }
}
