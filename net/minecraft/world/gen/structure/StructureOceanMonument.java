package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class StructureOceanMonument extends MapGenStructure {
   private int spacing;
   private int separation;
   public static final List WATER_BIOMES = Arrays.asList(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER);
   public static final List SPAWN_BIOMES = Arrays.asList(Biomes.DEEP_OCEAN);
   private static final List MONUMENT_ENEMIES = Lists.newArrayList();

   public StructureOceanMonument() {
      this.spacing = 32;
      this.separation = 5;
   }

   public StructureOceanMonument(Map var1) {
      this();

      for(Entry var3 : var1.entrySet()) {
         if (((String)var3.getKey()).equals("spacing")) {
            this.spacing = MathHelper.getInt((String)var3.getValue(), this.spacing, 1);
         } else if (((String)var3.getKey()).equals("separation")) {
            this.separation = MathHelper.getInt((String)var3.getValue(), this.separation, 1);
         }
      }

   }

   public String getStructureName() {
      return "Monument";
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int var3 = var1;
      int var4 = var2;
      if (var1 < 0) {
         var1 -= this.spacing - 1;
      }

      if (var2 < 0) {
         var2 -= this.spacing - 1;
      }

      int var5 = var1 / this.spacing;
      int var6 = var2 / this.spacing;
      Random var7 = this.world.setRandomSeed(var5, var6, 10387313);
      var5 = var5 * this.spacing;
      var6 = var6 * this.spacing;
      var5 = var5 + (var7.nextInt(this.spacing - this.separation) + var7.nextInt(this.spacing - this.separation)) / 2;
      var6 = var6 + (var7.nextInt(this.spacing - this.separation) + var7.nextInt(this.spacing - this.separation)) / 2;
      if (var3 == var5 && var4 == var6) {
         if (!this.world.getBiomeProvider().areBiomesViable(var3 * 16 + 8, var4 * 16 + 8, 16, SPAWN_BIOMES)) {
            return false;
         }

         boolean var8 = this.world.getBiomeProvider().areBiomesViable(var3 * 16 + 8, var4 * 16 + 8, 29, WATER_BIOMES);
         if (var8) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new StructureOceanMonument.StartMonument(this.world, this.rand, var1, var2);
   }

   public List getScatteredFeatureSpawnList() {
      return MONUMENT_ENEMIES;
   }

   static {
      MONUMENT_ENEMIES.add(new Biome.SpawnListEntry(EntityGuardian.class, 1, 2, 4));
   }

   public static class StartMonument extends StructureStart {
      private final Set processed = Sets.newHashSet();
      private boolean wasCreated;

      public StartMonument() {
      }

      public StartMonument(World var1, Random var2, int var3, int var4) {
         super(var3, var4);
         this.create(var1, var2, var3, var4);
      }

      private void create(World var1, Random var2, int var3, int var4) {
         var2.setSeed(var1.getSeed());
         long var5 = var2.nextLong();
         long var7 = var2.nextLong();
         long var9 = (long)var3 * var5;
         long var11 = (long)var4 * var7;
         var2.setSeed(var9 ^ var11 ^ var1.getSeed());
         int var13 = var3 * 16 + 8 - 29;
         int var14 = var4 * 16 + 8 - 29;
         EnumFacing var15 = EnumFacing.Plane.HORIZONTAL.random(var2);
         this.components.add(new StructureOceanMonumentPieces.MonumentBuilding(var2, var13, var14, var15));
         this.updateBoundingBox();
         this.wasCreated = true;
      }

      public void generateStructure(World var1, Random var2, StructureBoundingBox var3) {
         if (!this.wasCreated) {
            this.components.clear();
            this.create(var1, var2, this.getChunkPosX(), this.getChunkPosZ());
         }

         super.generateStructure(var1, var2, var3);
      }

      public boolean isValidForPostProcess(ChunkPos var1) {
         return this.processed.contains(var1) ? false : super.isValidForPostProcess(var1);
      }

      public void notifyPostProcessAt(ChunkPos var1) {
         super.notifyPostProcessAt(var1);
         this.processed.add(var1);
      }

      public void writeToNBT(NBTTagCompound var1) {
         super.writeToNBT(var1);
         NBTTagList var2 = new NBTTagList();

         for(ChunkPos var4 : this.processed) {
            NBTTagCompound var5 = new NBTTagCompound();
            var5.setInteger("X", var4.chunkXPos);
            var5.setInteger("Z", var4.chunkZPos);
            var2.appendTag(var5);
         }

         var1.setTag("Processed", var2);
      }

      public void readFromNBT(NBTTagCompound var1) {
         super.readFromNBT(var1);
         if (var1.hasKey("Processed", 9)) {
            NBTTagList var2 = var1.getTagList("Processed", 10);

            for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
               NBTTagCompound var4 = var2.getCompoundTagAt(var3);
               this.processed.add(new ChunkPos(var4.getInteger("X"), var4.getInteger("Z")));
            }
         }

      }
   }
}
