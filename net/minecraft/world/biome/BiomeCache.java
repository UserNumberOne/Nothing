package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.src.MinecraftServer;

public class BiomeCache {
   private final BiomeProvider chunkManager;
   private long lastCleanupTime;
   private final Long2ObjectMap cacheMap = new Long2ObjectOpenHashMap(4096);
   private final List cache = Lists.newArrayList();

   public BiomeCache(BiomeProvider var1) {
      this.chunkManager = var1;
   }

   public BiomeCache.Block getBiomeCacheBlock(int var1, int var2) {
      var1 = var1 >> 4;
      var2 = var2 >> 4;
      long var3 = (long)var1 & 4294967295L | ((long)var2 & 4294967295L) << 32;
      BiomeCache.Block var5 = (BiomeCache.Block)this.cacheMap.get(var3);
      if (var5 == null) {
         var5 = new BiomeCache.Block(var1, var2);
         this.cacheMap.put(var3, var5);
         this.cache.add(var5);
      }

      var5.lastAccessTime = MinecraftServer.av();
      return var5;
   }

   public Biome getBiome(int var1, int var2, Biome var3) {
      Biome var4 = this.getBiomeCacheBlock(var1, var2).getBiome(var1, var2);
      return var4 == null ? var3 : var4;
   }

   public void cleanupCache() {
      long var1 = MinecraftServer.av();
      long var3 = var1 - this.lastCleanupTime;
      if (var3 > 7500L || var3 < 0L) {
         this.lastCleanupTime = var1;

         for(int var5 = 0; var5 < this.cache.size(); ++var5) {
            BiomeCache.Block var6 = (BiomeCache.Block)this.cache.get(var5);
            long var7 = var1 - var6.lastAccessTime;
            if (var7 > 30000L || var7 < 0L) {
               this.cache.remove(var5--);
               long var9 = (long)var6.xPosition & 4294967295L | ((long)var6.zPosition & 4294967295L) << 32;
               this.cacheMap.remove(var9);
            }
         }
      }

   }

   public Biome[] getCachedBiomes(int var1, int var2) {
      return this.getBiomeCacheBlock(var1, var2).biomes;
   }

   public class Block {
      public Biome[] biomes = new Biome[256];
      public int xPosition;
      public int zPosition;
      public long lastAccessTime;

      public Block(int var2, int var3) {
         this.xPosition = var2;
         this.zPosition = var3;
         BiomeCache.this.chunkManager.getBiomes(this.biomes, var2 << 4, var3 << 4, 16, 16, false);
      }

      public Biome getBiome(int var1, int var2) {
         return this.biomes[var1 & 15 | (var2 & 15) << 4];
      }
   }
}
