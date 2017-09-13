package net.minecraft.world.gen.structure;

import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkProviderEnd;

public class MapGenEndCity extends MapGenStructure {
   private final int citySpacing = 20;
   private final int minCitySeparation = 11;
   private final ChunkProviderEnd endProvider;

   public MapGenEndCity(ChunkProviderEnd var1) {
      this.endProvider = var1;
   }

   public String getStructureName() {
      return "EndCity";
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int var3 = var1;
      int var4 = var2;
      if (var1 < 0) {
         var1 -= 19;
      }

      if (var2 < 0) {
         var2 -= 19;
      }

      int var5 = var1 / 20;
      int var6 = var2 / 20;
      Random var7 = this.world.setRandomSeed(var5, var6, 10387313);
      var5 = var5 * 20;
      var6 = var6 * 20;
      var5 = var5 + (var7.nextInt(9) + var7.nextInt(9)) / 2;
      var6 = var6 + (var7.nextInt(9) + var7.nextInt(9)) / 2;
      return var3 == var5 && var4 == var6 && this.endProvider.isIslandChunk(var3, var4);
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new MapGenEndCity.Start(this.world, this.endProvider, this.rand, var1, var2);
   }

   public static class Start extends StructureStart {
      private boolean isSizeable;

      public Start() {
      }

      public Start(World var1, ChunkProviderEnd var2, Random var3, int var4, int var5) {
         super(var4, var5);
         this.create(var1, var2, var3, var4, var5);
      }

      private void create(World var1, ChunkProviderEnd var2, Random var3, int var4, int var5) {
         Rotation var6 = Rotation.values()[var3.nextInt(Rotation.values().length)];
         ChunkPrimer var7 = new ChunkPrimer();
         var2.setBlocksInChunk(var4, var5, var7);
         byte var8 = 5;
         byte var9 = 5;
         if (var6 == Rotation.CLOCKWISE_90) {
            var8 = -5;
         } else if (var6 == Rotation.CLOCKWISE_180) {
            var8 = -5;
            var9 = -5;
         } else if (var6 == Rotation.COUNTERCLOCKWISE_90) {
            var9 = -5;
         }

         int var10 = var7.findGroundBlockIdx(7, 7);
         int var11 = var7.findGroundBlockIdx(7, 7 + var9);
         int var12 = var7.findGroundBlockIdx(7 + var8, 7);
         int var13 = var7.findGroundBlockIdx(7 + var8, 7 + var9);
         int var14 = Math.min(Math.min(var10, var11), Math.min(var12, var13));
         if (var14 < 60) {
            this.isSizeable = false;
         } else {
            BlockPos var15 = new BlockPos(var4 * 16 + 8, var14, var5 * 16 + 8);
            StructureEndCityPieces.beginHouseTower(var15, var6, this.components, var3);
            this.updateBoundingBox();
            this.isSizeable = true;
         }
      }

      public boolean isSizeableStructure() {
         return this.isSizeable;
      }

      public void writeToNBT(NBTTagCompound var1) {
         super.writeToNBT(var1);
      }

      public void readFromNBT(NBTTagCompound var1) {
         super.readFromNBT(var1);
      }
   }
}
