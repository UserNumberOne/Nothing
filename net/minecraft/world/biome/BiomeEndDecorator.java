package net.minecraft.world.biome;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenSpikes;

public class BiomeEndDecorator extends BiomeDecorator {
   private static final LoadingCache SPIKE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(new BiomeEndDecorator.SpikeCacheLoader());
   private final WorldGenSpikes spikeGen = new WorldGenSpikes();

   protected void genDecorations(Biome var1, World var2, Random var3) {
      this.generateOres(var2, var3);
      WorldGenSpikes.EndSpike[] var4 = getSpikesForWorld(var2);

      for(WorldGenSpikes.EndSpike var8 : var4) {
         if (var8.doesStartInChunk(this.chunkPos)) {
            this.spikeGen.setSpike(var8);
            this.spikeGen.generate(var2, var3, new BlockPos(var8.getCenterX(), 45, var8.getCenterZ()));
         }
      }

   }

   public static WorldGenSpikes.EndSpike[] getSpikesForWorld(World var0) {
      Random var1 = new Random(var0.getSeed());
      long var2 = var1.nextLong() & 65535L;
      return (WorldGenSpikes.EndSpike[])SPIKE_CACHE.getUnchecked(Long.valueOf(var2));
   }

   static class SpikeCacheLoader extends CacheLoader {
      private SpikeCacheLoader() {
      }

      public WorldGenSpikes.EndSpike[] load(Long var1) throws Exception {
         ArrayList var2 = Lists.newArrayList(ContiguousSet.create(Range.closedOpen(Integer.valueOf(0), Integer.valueOf(10)), DiscreteDomain.integers()));
         Collections.shuffle(var2, new Random(var1.longValue()));
         WorldGenSpikes.EndSpike[] var3 = new WorldGenSpikes.EndSpike[10];

         for(int var4 = 0; var4 < 10; ++var4) {
            int var5 = (int)(42.0D * Math.cos(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double)var4)));
            int var6 = (int)(42.0D * Math.sin(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double)var4)));
            int var7 = ((Integer)var2.get(var4)).intValue();
            int var8 = 2 + var7 / 3;
            int var9 = 76 + var7 * 3;
            boolean var10 = var7 == 1 || var7 == 2;
            var3[var4] = new WorldGenSpikes.EndSpike(var5, var6, var8, var9, var10);
         }

         return var3;
      }

      // $FF: synthetic method
      public Object load(Object var1) throws Exception {
         return this.load((Long)var1);
      }
   }
}
