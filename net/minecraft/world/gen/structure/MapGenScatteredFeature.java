package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class MapGenScatteredFeature extends MapGenStructure {
   private static final List BIOMELIST = Arrays.asList(Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.SWAMPLAND, Biomes.ICE_PLAINS, Biomes.COLD_TAIGA);
   private final List scatteredFeatureSpawnList;
   private int maxDistanceBetweenScatteredFeatures;
   private final int minDistanceBetweenScatteredFeatures;

   public MapGenScatteredFeature() {
      this.scatteredFeatureSpawnList = Lists.newArrayList();
      this.maxDistanceBetweenScatteredFeatures = 32;
      this.minDistanceBetweenScatteredFeatures = 8;
      this.scatteredFeatureSpawnList.add(new Biome.SpawnListEntry(EntityWitch.class, 1, 1, 1));
   }

   public MapGenScatteredFeature(Map var1) {
      this();

      for(Entry var3 : var1.entrySet()) {
         if (((String)var3.getKey()).equals("distance")) {
            this.maxDistanceBetweenScatteredFeatures = MathHelper.getInt((String)var3.getValue(), this.maxDistanceBetweenScatteredFeatures, 9);
         }
      }

   }

   public String getStructureName() {
      return "Temple";
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int var3 = var1;
      int var4 = var2;
      if (var1 < 0) {
         var1 -= this.maxDistanceBetweenScatteredFeatures - 1;
      }

      if (var2 < 0) {
         var2 -= this.maxDistanceBetweenScatteredFeatures - 1;
      }

      int var5 = var1 / this.maxDistanceBetweenScatteredFeatures;
      int var6 = var2 / this.maxDistanceBetweenScatteredFeatures;
      Random var7 = this.world.setRandomSeed(var5, var6, 14357617);
      var5 = var5 * this.maxDistanceBetweenScatteredFeatures;
      var6 = var6 * this.maxDistanceBetweenScatteredFeatures;
      var5 = var5 + var7.nextInt(this.maxDistanceBetweenScatteredFeatures - 8);
      var6 = var6 + var7.nextInt(this.maxDistanceBetweenScatteredFeatures - 8);
      if (var3 == var5 && var4 == var6) {
         Biome var8 = this.world.getBiomeProvider().getBiome(new BlockPos(var3 * 16 + 8, 0, var4 * 16 + 8));
         if (var8 == null) {
            return false;
         }

         for(Biome var10 : BIOMELIST) {
            if (var8 == var10) {
               return true;
            }
         }
      }

      return false;
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new MapGenScatteredFeature.Start(this.world, this.rand, var1, var2);
   }

   public boolean isSwampHut(BlockPos var1) {
      StructureStart var2 = this.getStructureAt(var1);
      if (var2 != null && var2 instanceof MapGenScatteredFeature.Start && !var2.components.isEmpty()) {
         StructureComponent var3 = (StructureComponent)var2.components.get(0);
         return var3 instanceof ComponentScatteredFeaturePieces.SwampHut;
      } else {
         return false;
      }
   }

   public List getScatteredFeatureSpawnList() {
      return this.scatteredFeatureSpawnList;
   }

   public static class Start extends StructureStart {
      public Start() {
      }

      public Start(World var1, Random var2, int var3, int var4) {
         this(var1, var2, var3, var4, var1.getBiome(new BlockPos(var3 * 16 + 8, 0, var4 * 16 + 8)));
      }

      public Start(World var1, Random var2, int var3, int var4, Biome var5) {
         super(var3, var4);
         if (var5 != Biomes.JUNGLE && var5 != Biomes.JUNGLE_HILLS) {
            if (var5 == Biomes.SWAMPLAND) {
               ComponentScatteredFeaturePieces.SwampHut var7 = new ComponentScatteredFeaturePieces.SwampHut(var2, var3 * 16, var4 * 16);
               this.components.add(var7);
            } else if (var5 != Biomes.DESERT && var5 != Biomes.DESERT_HILLS) {
               if (var5 == Biomes.ICE_PLAINS || var5 == Biomes.COLD_TAIGA) {
                  ComponentScatteredFeaturePieces.Igloo var9 = new ComponentScatteredFeaturePieces.Igloo(var2, var3 * 16, var4 * 16);
                  this.components.add(var9);
               }
            } else {
               ComponentScatteredFeaturePieces.DesertPyramid var8 = new ComponentScatteredFeaturePieces.DesertPyramid(var2, var3 * 16, var4 * 16);
               this.components.add(var8);
            }
         } else {
            ComponentScatteredFeaturePieces.JunglePyramid var6 = new ComponentScatteredFeaturePieces.JunglePyramid(var2, var3 * 16, var4 * 16);
            this.components.add(var6);
         }

         this.updateBoundingBox();
      }
   }
}
