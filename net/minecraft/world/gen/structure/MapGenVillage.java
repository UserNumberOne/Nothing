package net.minecraft.world.gen.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MapGenVillage extends MapGenStructure {
   public static List VILLAGE_SPAWN_BIOMES = Arrays.asList(Biomes.PLAINS, Biomes.DESERT, Biomes.SAVANNA, Biomes.TAIGA);
   private int size;
   private int distance;
   private final int minTownSeparation;

   public MapGenVillage() {
      this.distance = 32;
      this.minTownSeparation = 8;
   }

   public MapGenVillage(Map var1) {
      this();

      for(Entry entry : map.entrySet()) {
         if (((String)entry.getKey()).equals("size")) {
            this.size = MathHelper.getInt((String)entry.getValue(), this.size, 0);
         } else if (((String)entry.getKey()).equals("distance")) {
            this.distance = MathHelper.getInt((String)entry.getValue(), this.distance, 9);
         }
      }

   }

   public String getStructureName() {
      return "Village";
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int i = chunkX;
      int j = chunkZ;
      if (chunkX < 0) {
         chunkX -= this.distance - 1;
      }

      if (chunkZ < 0) {
         chunkZ -= this.distance - 1;
      }

      int k = chunkX / this.distance;
      int l = chunkZ / this.distance;
      Random random = this.world.setRandomSeed(k, l, 10387312);
      k = k * this.distance;
      l = l * this.distance;
      k = k + random.nextInt(this.distance - 8);
      l = l + random.nextInt(this.distance - 8);
      if (i == k && j == l) {
         boolean flag = this.world.getBiomeProvider().areBiomesViable(i * 16 + 8, j * 16 + 8, 0, VILLAGE_SPAWN_BIOMES);
         if (flag) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new MapGenVillage.Start(this.world, this.rand, chunkX, chunkZ, this.size);
   }

   public static class Start extends StructureStart {
      private boolean hasMoreThanTwoComponents;

      public Start() {
      }

      public Start(World var1, Random var2, int var3, int var4, int var5) {
         super(x, z);
         List list = StructureVillagePieces.getStructureVillageWeightedPieceList(rand, size);
         StructureVillagePieces.Start structurevillagepieces$start = new StructureVillagePieces.Start(worldIn.getBiomeProvider(), 0, rand, (x << 4) + 2, (z << 4) + 2, list, size);
         this.components.add(structurevillagepieces$start);
         structurevillagepieces$start.buildComponent(structurevillagepieces$start, this.components, rand);
         List list1 = structurevillagepieces$start.pendingRoads;
         List list2 = structurevillagepieces$start.pendingHouses;

         while(!list1.isEmpty() || !list2.isEmpty()) {
            if (list1.isEmpty()) {
               int i = rand.nextInt(list2.size());
               StructureComponent structurecomponent = (StructureComponent)list2.remove(i);
               structurecomponent.buildComponent(structurevillagepieces$start, this.components, rand);
            } else {
               int j = rand.nextInt(list1.size());
               StructureComponent structurecomponent2 = (StructureComponent)list1.remove(j);
               structurecomponent2.buildComponent(structurevillagepieces$start, this.components, rand);
            }
         }

         this.updateBoundingBox();
         int k = 0;

         for(StructureComponent structurecomponent1 : this.components) {
            if (!(structurecomponent1 instanceof StructureVillagePieces.Road)) {
               ++k;
            }
         }

         this.hasMoreThanTwoComponents = k > 2;
      }

      public boolean isSizeableStructure() {
         return this.hasMoreThanTwoComponents;
      }

      public void writeToNBT(NBTTagCompound var1) {
         super.writeToNBT(tagCompound);
         tagCompound.setBoolean("Valid", this.hasMoreThanTwoComponents);
      }

      public void readFromNBT(NBTTagCompound var1) {
         super.readFromNBT(tagCompound);
         this.hasMoreThanTwoComponents = tagCompound.getBoolean("Valid");
      }
   }
}
