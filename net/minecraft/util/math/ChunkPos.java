package net.minecraft.util.math;

import net.minecraft.entity.Entity;

public class ChunkPos {
   public final int chunkXPos;
   public final int chunkZPos;

   public ChunkPos(int var1, int var2) {
      this.chunkXPos = x;
      this.chunkZPos = z;
   }

   public ChunkPos(BlockPos var1) {
      this.chunkXPos = pos.getX() >> 4;
      this.chunkZPos = pos.getZ() >> 4;
   }

   public static long asLong(int var0, int var1) {
      return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
   }

   public int hashCode() {
      int i = 1664525 * this.chunkXPos + 1013904223;
      int j = 1664525 * (this.chunkZPos ^ -559038737) + 1013904223;
      return i ^ j;
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof ChunkPos)) {
         return false;
      } else {
         ChunkPos chunkpos = (ChunkPos)p_equals_1_;
         return this.chunkXPos == chunkpos.chunkXPos && this.chunkZPos == chunkpos.chunkZPos;
      }
   }

   public double getDistanceSq(Entity var1) {
      double d0 = (double)(this.chunkXPos * 16 + 8);
      double d1 = (double)(this.chunkZPos * 16 + 8);
      double d2 = d0 - entityIn.posX;
      double d3 = d1 - entityIn.posZ;
      return d2 * d2 + d3 * d3;
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
      return new BlockPos((this.chunkXPos << 4) + x, y, (this.chunkZPos << 4) + z);
   }

   public BlockPos getCenterBlock(int var1) {
      return new BlockPos(this.getXCenter(), y, this.getZCenter());
   }

   public String toString() {
      return "[" + this.chunkXPos + ", " + this.chunkZPos + "]";
   }
}
