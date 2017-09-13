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

      for(Entry entry : p_i45608_1_.entrySet()) {
         if (((String)entry.getKey()).equals("spacing")) {
            this.spacing = MathHelper.getInt((String)entry.getValue(), this.spacing, 1);
         } else if (((String)entry.getKey()).equals("separation")) {
            this.separation = MathHelper.getInt((String)entry.getValue(), this.separation, 1);
         }
      }

   }

   public String getStructureName() {
      return "Monument";
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int i = chunkX;
      int j = chunkZ;
      if (chunkX < 0) {
         chunkX -= this.spacing - 1;
      }

      if (chunkZ < 0) {
         chunkZ -= this.spacing - 1;
      }

      int k = chunkX / this.spacing;
      int l = chunkZ / this.spacing;
      Random random = this.world.setRandomSeed(k, l, 10387313);
      k = k * this.spacing;
      l = l * this.spacing;
      k = k + (random.nextInt(this.spacing - this.separation) + random.nextInt(this.spacing - this.separation)) / 2;
      l = l + (random.nextInt(this.spacing - this.separation) + random.nextInt(this.spacing - this.separation)) / 2;
      if (i == k && j == l) {
         if (!this.world.getBiomeProvider().areBiomesViable(i * 16 + 8, j * 16 + 8, 16, SPAWN_BIOMES)) {
            return false;
         }

         boolean flag = this.world.getBiomeProvider().areBiomesViable(i * 16 + 8, j * 16 + 8, 29, WATER_BIOMES);
         if (flag) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new StructureOceanMonument.StartMonument(this.world, this.rand, chunkX, chunkZ);
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
         super(chunkX, chunkZ);
         this.create(worldIn, random, chunkX, chunkZ);
      }

      private void create(World var1, Random var2, int var3, int var4) {
         random.setSeed(worldIn.getSeed());
         long i = random.nextLong();
         long j = random.nextLong();
         long k = (long)chunkX * i;
         long l = (long)chunkZ * j;
         random.setSeed(k ^ l ^ worldIn.getSeed());
         int i1 = chunkX * 16 + 8 - 29;
         int j1 = chunkZ * 16 + 8 - 29;
         EnumFacing enumfacing = EnumFacing.Plane.HORIZONTAL.random(random);
         this.components.add(new StructureOceanMonumentPieces.MonumentBuilding(random, i1, j1, enumfacing));
         this.updateBoundingBox();
         this.wasCreated = true;
      }

      public void generateStructure(World var1, Random var2, StructureBoundingBox var3) {
         if (!this.wasCreated) {
            this.components.clear();
            this.create(worldIn, rand, this.getChunkPosX(), this.getChunkPosZ());
         }

         super.generateStructure(worldIn, rand, structurebb);
      }

      public boolean isValidForPostProcess(ChunkPos var1) {
         return this.processed.contains(pair) ? false : super.isValidForPostProcess(pair);
      }

      public void notifyPostProcessAt(ChunkPos var1) {
         super.notifyPostProcessAt(pair);
         this.processed.add(pair);
      }

      public void writeToNBT(NBTTagCompound var1) {
         super.writeToNBT(tagCompound);
         NBTTagList nbttaglist = new NBTTagList();

         for(ChunkPos chunkpos : this.processed) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("X", chunkpos.chunkXPos);
            nbttagcompound.setInteger("Z", chunkpos.chunkZPos);
            nbttaglist.appendTag(nbttagcompound);
         }

         tagCompound.setTag("Processed", nbttaglist);
      }

      public void readFromNBT(NBTTagCompound var1) {
         super.readFromNBT(tagCompound);
         if (tagCompound.hasKey("Processed", 9)) {
            NBTTagList nbttaglist = tagCompound.getTagList("Processed", 10);

            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
               this.processed.add(new ChunkPos(nbttagcompound.getInteger("X"), nbttagcompound.getInteger("Z")));
            }
         }

      }
   }
}
