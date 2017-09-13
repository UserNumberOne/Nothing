package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Biomes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens;

public class BiomeProvider {
   public static List allowedBiomes = Lists.newArrayList(new Biome[]{Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.FOREST_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS});
   private GenLayer genBiomes;
   private GenLayer biomeIndexLayer;
   private final BiomeCache biomeCache;
   private final List biomesToSpawnIn;

   protected BiomeProvider() {
      this.biomeCache = new BiomeCache(this);
      this.biomesToSpawnIn = Lists.newArrayList(allowedBiomes);
   }

   private BiomeProvider(long var1, WorldType var3, String var4) {
      this();
      GenLayer[] var5 = GenLayer.initializeAllBiomeGenerators(var1, var3, var4);
      var5 = this.getModdedBiomeGenerators(var3, var1, var5);
      this.genBiomes = var5[0];
      this.biomeIndexLayer = var5[1];
   }

   public BiomeProvider(WorldInfo var1) {
      this(var1.getSeed(), var1.getTerrainType(), var1.getGeneratorOptions());
   }

   public List getBiomesToSpawnIn() {
      return this.biomesToSpawnIn;
   }

   public Biome getBiome(BlockPos var1) {
      return this.getBiome(var1, (Biome)null);
   }

   public Biome getBiome(BlockPos var1, Biome var2) {
      return this.biomeCache.getBiome(var1.getX(), var1.getZ(), var2);
   }

   public float getTemperatureAtHeight(float var1, int var2) {
      return var1;
   }

   public Biome[] getBiomesForGeneration(Biome[] var1, int var2, int var3, int var4, int var5) {
      IntCache.resetIntCache();
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new Biome[var4 * var5];
      }

      int[] var6 = this.genBiomes.getInts(var2, var3, var4, var5);

      try {
         for(int var7 = 0; var7 < var4 * var5; ++var7) {
            var1[var7] = Biome.getBiome(var6[var7], Biomes.DEFAULT);
         }

         return var1;
      } catch (Throwable var10) {
         CrashReport var8 = CrashReport.makeCrashReport(var10, "Invalid Biome id");
         CrashReportCategory var9 = var8.makeCategory("RawBiomeBlock");
         var9.addCrashSection("biomes[] size", Integer.valueOf(var1.length));
         var9.addCrashSection("x", Integer.valueOf(var2));
         var9.addCrashSection("z", Integer.valueOf(var3));
         var9.addCrashSection("w", Integer.valueOf(var4));
         var9.addCrashSection("h", Integer.valueOf(var5));
         throw new ReportedException(var8);
      }
   }

   public Biome[] getBiomes(@Nullable Biome[] var1, int var2, int var3, int var4, int var5) {
      return this.getBiomes(var1, var2, var3, var4, var5, true);
   }

   public Biome[] getBiomes(@Nullable Biome[] var1, int var2, int var3, int var4, int var5, boolean var6) {
      IntCache.resetIntCache();
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new Biome[var4 * var5];
      }

      if (var6 && var4 == 16 && var5 == 16 && (var2 & 15) == 0 && (var3 & 15) == 0) {
         Biome[] var9 = this.biomeCache.getCachedBiomes(var2, var3);
         System.arraycopy(var9, 0, var1, 0, var4 * var5);
         return var1;
      } else {
         int[] var7 = this.biomeIndexLayer.getInts(var2, var3, var4, var5);

         for(int var8 = 0; var8 < var4 * var5; ++var8) {
            var1[var8] = Biome.getBiome(var7[var8], Biomes.DEFAULT);
         }

         return var1;
      }
   }

   public boolean areBiomesViable(int var1, int var2, int var3, List var4) {
      IntCache.resetIntCache();
      int var5 = var1 - var3 >> 2;
      int var6 = var2 - var3 >> 2;
      int var7 = var1 + var3 >> 2;
      int var8 = var2 + var3 >> 2;
      int var9 = var7 - var5 + 1;
      int var10 = var8 - var6 + 1;
      int[] var11 = this.genBiomes.getInts(var5, var6, var9, var10);

      try {
         for(int var12 = 0; var12 < var9 * var10; ++var12) {
            Biome var16 = Biome.getBiome(var11[var12]);
            if (!var4.contains(var16)) {
               return false;
            }
         }

         return true;
      } catch (Throwable var15) {
         CrashReport var13 = CrashReport.makeCrashReport(var15, "Invalid Biome id");
         CrashReportCategory var14 = var13.makeCategory("Layer");
         var14.addCrashSection("Layer", this.genBiomes.toString());
         var14.addCrashSection("x", Integer.valueOf(var1));
         var14.addCrashSection("z", Integer.valueOf(var2));
         var14.addCrashSection("radius", Integer.valueOf(var3));
         var14.addCrashSection("allowed", var4);
         throw new ReportedException(var13);
      }
   }

   @Nullable
   public BlockPos findBiomePosition(int var1, int var2, int var3, List var4, Random var5) {
      IntCache.resetIntCache();
      int var6 = var1 - var3 >> 2;
      int var7 = var2 - var3 >> 2;
      int var8 = var1 + var3 >> 2;
      int var9 = var2 + var3 >> 2;
      int var10 = var8 - var6 + 1;
      int var11 = var9 - var7 + 1;
      int[] var12 = this.genBiomes.getInts(var6, var7, var10, var11);
      BlockPos var13 = null;
      int var14 = 0;

      for(int var15 = 0; var15 < var10 * var11; ++var15) {
         int var16 = var6 + var15 % var10 << 2;
         int var17 = var7 + var15 / var10 << 2;
         Biome var18 = Biome.getBiome(var12[var15]);
         if (var4.contains(var18) && (var13 == null || var5.nextInt(var14 + 1) == 0)) {
            var13 = new BlockPos(var16, 0, var17);
            ++var14;
         }
      }

      return var13;
   }

   public void cleanupCache() {
      this.biomeCache.cleanupCache();
   }

   public GenLayer[] getModdedBiomeGenerators(WorldType var1, long var2, GenLayer[] var4) {
      InitBiomeGens var5 = new InitBiomeGens(var1, var2, var4);
      MinecraftForge.TERRAIN_GEN_BUS.post(var5);
      return var5.getNewBiomeGens();
   }
}
