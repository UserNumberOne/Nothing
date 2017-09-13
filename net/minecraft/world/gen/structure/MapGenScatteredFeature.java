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

      for(Entry entry : p_i2061_1_.entrySet()) {
         if (((String)entry.getKey()).equals("distance")) {
            this.maxDistanceBetweenScatteredFeatures = MathHelper.getInt((String)entry.getValue(), this.maxDistanceBetweenScatteredFeatures, 9);
         }
      }

   }

   public String getStructureName() {
      return "Temple";
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int i = chunkX;
      int j = chunkZ;
      if (chunkX < 0) {
         chunkX -= this.maxDistanceBetweenScatteredFeatures - 1;
      }

      if (chunkZ < 0) {
         chunkZ -= this.maxDistanceBetweenScatteredFeatures - 1;
      }

      int k = chunkX / this.maxDistanceBetweenScatteredFeatures;
      int l = chunkZ / this.maxDistanceBetweenScatteredFeatures;
      Random random = this.world.setRandomSeed(k, l, 14357617);
      k = k * this.maxDistanceBetweenScatteredFeatures;
      l = l * this.maxDistanceBetweenScatteredFeatures;
      k = k + random.nextInt(this.maxDistanceBetweenScatteredFeatures - 8);
      l = l + random.nextInt(this.maxDistanceBetweenScatteredFeatures - 8);
      if (i == k && j == l) {
         Biome biome = this.world.getBiomeProvider().getBiome(new BlockPos(i * 16 + 8, 0, j * 16 + 8));
         if (biome == null) {
            return false;
         }

         for(Biome biome1 : BIOMELIST) {
            if (biome == biome1) {
               return true;
            }
         }
      }

      return false;
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new MapGenScatteredFeature.Start(this.world, this.rand, chunkX, chunkZ);
   }

   public boolean isSwampHut(BlockPos var1) {
      StructureStart structurestart = this.getStructureAt(p_175798_1_);
      if (structurestart != null && structurestart instanceof MapGenScatteredFeature.Start && !structurestart.components.isEmpty()) {
         StructureComponent structurecomponent = (StructureComponent)structurestart.components.get(0);
         return structurecomponent instanceof ComponentScatteredFeaturePieces.SwampHut;
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
         this(worldIn, random, chunkX, chunkZ, worldIn.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)));
      }

      public Start(World var1, Random var2, int var3, int var4, Biome var5) {
         super(chunkX, chunkZ);
         if (biomeIn != Biomes.JUNGLE && biomeIn != Biomes.JUNGLE_HILLS) {
            if (biomeIn == Biomes.SWAMPLAND) {
               ComponentScatteredFeaturePieces.SwampHut componentscatteredfeaturepieces$swamphut = new ComponentScatteredFeaturePieces.SwampHut(random, chunkX * 16, chunkZ * 16);
               this.components.add(componentscatteredfeaturepieces$swamphut);
            } else if (biomeIn != Biomes.DESERT && biomeIn != Biomes.DESERT_HILLS) {
               if (biomeIn == Biomes.ICE_PLAINS || biomeIn == Biomes.COLD_TAIGA) {
                  ComponentScatteredFeaturePieces.Igloo componentscatteredfeaturepieces$igloo = new ComponentScatteredFeaturePieces.Igloo(random, chunkX * 16, chunkZ * 16);
                  this.components.add(componentscatteredfeaturepieces$igloo);
               }
            } else {
               ComponentScatteredFeaturePieces.DesertPyramid componentscatteredfeaturepieces$desertpyramid = new ComponentScatteredFeaturePieces.DesertPyramid(random, chunkX * 16, chunkZ * 16);
               this.components.add(componentscatteredfeaturepieces$desertpyramid);
            }
         } else {
            ComponentScatteredFeaturePieces.JunglePyramid componentscatteredfeaturepieces$junglepyramid = new ComponentScatteredFeaturePieces.JunglePyramid(random, chunkX * 16, chunkZ * 16);
            this.components.add(componentscatteredfeaturepieces$junglepyramid);
         }

         this.updateBoundingBox();
      }
   }
}
