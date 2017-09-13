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

      for(Entry var3 : var1.entrySet()) {
         if (((String)var3.getKey()).equals("size")) {
            this.size = MathHelper.getInt((String)var3.getValue(), this.size, 0);
         } else if (((String)var3.getKey()).equals("distance")) {
            this.distance = MathHelper.getInt((String)var3.getValue(), this.distance, 9);
         }
      }

   }

   public String getStructureName() {
      return "Village";
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int var3 = var1;
      int var4 = var2;
      if (var1 < 0) {
         var1 -= this.distance - 1;
      }

      if (var2 < 0) {
         var2 -= this.distance - 1;
      }

      int var5 = var1 / this.distance;
      int var6 = var2 / this.distance;
      Random var7 = this.world.setRandomSeed(var5, var6, 10387312);
      var5 = var5 * this.distance;
      var6 = var6 * this.distance;
      var5 = var5 + var7.nextInt(this.distance - 8);
      var6 = var6 + var7.nextInt(this.distance - 8);
      if (var3 == var5 && var4 == var6) {
         boolean var8 = this.world.getBiomeProvider().areBiomesViable(var3 * 16 + 8, var4 * 16 + 8, 0, VILLAGE_SPAWN_BIOMES);
         if (var8) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new MapGenVillage.Start(this.world, this.rand, var1, var2, this.size);
   }

   public static class Start extends StructureStart {
      private boolean hasMoreThanTwoComponents;

      public Start() {
      }

      public Start(World var1, Random var2, int var3, int var4, int var5) {
         super(var3, var4);
         List var6 = StructureVillagePieces.getStructureVillageWeightedPieceList(var2, var5);
         StructureVillagePieces.Start var7 = new StructureVillagePieces.Start(var1.getBiomeProvider(), 0, var2, (var3 << 4) + 2, (var4 << 4) + 2, var6, var5);
         this.components.add(var7);
         var7.buildComponent(var7, this.components, var2);
         List var8 = var7.pendingRoads;
         List var9 = var7.pendingHouses;

         while(!var8.isEmpty() || !var9.isEmpty()) {
            if (var8.isEmpty()) {
               int var10 = var2.nextInt(var9.size());
               StructureComponent var11 = (StructureComponent)var9.remove(var10);
               var11.buildComponent(var7, this.components, var2);
            } else {
               int var13 = var2.nextInt(var8.size());
               StructureComponent var15 = (StructureComponent)var8.remove(var13);
               var15.buildComponent(var7, this.components, var2);
            }
         }

         this.updateBoundingBox();
         int var14 = 0;

         for(StructureComponent var12 : this.components) {
            if (!(var12 instanceof StructureVillagePieces.Road)) {
               ++var14;
            }
         }

         this.hasMoreThanTwoComponents = var14 > 2;
      }

      public boolean isSizeableStructure() {
         return this.hasMoreThanTwoComponents;
      }

      public void writeToNBT(NBTTagCompound var1) {
         super.writeToNBT(var1);
         var1.setBoolean("Valid", this.hasMoreThanTwoComponents);
      }

      public void readFromNBT(NBTTagCompound var1) {
         super.readFromNBT(var1);
         this.hasMoreThanTwoComponents = var1.getBoolean("Valid");
      }
   }
}
