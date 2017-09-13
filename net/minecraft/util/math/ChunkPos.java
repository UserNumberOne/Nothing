package net.minecraft.util.math;

import net.minecraft.entity.Entity;

public class ChunkPos {
   public final int chunkXPos;
   public final int chunkZPos;

   public ChunkPos(int var1, int var2) {
      this.chunkXPos = var1;
      this.chunkZPos = var2;
   }

   public ChunkPos(BlockPos var1) {
      this.chunkXPos = var1.getX() >> 4;
      this.chunkZPos = var1.getZ() >> 4;
   }

   public static long asLong(int var0, int var1) {
      return (long)var0 & 4294967295L | ((long)var1 & 4294967295L) << 32;
   }

   public int hashCode() {
      int var1 = 1664525 * this.chunkXPos + 1013904223;
      int var2 = 1664525 * (this.chunkZPos ^ -559038737) + 1013904223;
      return var1 ^ var2;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof ChunkPos)) {
         return false;
      } else {
         ChunkPos var2 = (ChunkPos)var1;
         return this.chunkXPos == var2.chunkXPos && this.chunkZPos == var2.chunkZPos;
      }
   }

   public double getDistanceSq(Entity var1) {
      double var2 = (double)(this.chunkXPos * 16 + 8);
      double var4 = (double)(this.chunkZPos * 16 + 8);
      double var6 = var2 - var1.posX;
      double var8 = var4 - var1.posZ;
      return var6 * var6 + var8 * var8;
   }

   public int getXCenter() {
      return (this.chunkXPos << 4) + 8;
   }

   public int getZCenter() {
      return (this.chunkZPos << 4) + 8;
   }

   public int getXStart() {
      return this.chunkXPos << 4;
   }

   public int getZStart() {
      return this.chunkZPos << 4;
   }

   public int getXEnd() {
      return (this.chunkXPos << 4) + 15;
   }

   public int getZEnd() {
      return (this.chunkZPos << 4) + 15;
   }

   public BlockPos getBlock(int var1, int var2, int var3) {
      return new BlockPos((this.chunkXPos << 4) + var1, var2, (this.chunkZPos << 4) + var3);
   }

   public BlockPos getCenterBlock(int var1) {
      return new BlockPos(this.getXCenter(), var1, this.getZCenter());
   }

   public String toString() {
      return "[" + this.chunkXPos + ", " + this.chunkZPos + "]";
   }
}
