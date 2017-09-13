package net.minecraft.village;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class VillageDoorInfo {
   private final BlockPos doorBlockPos;
   private final BlockPos insideBlock;
   private final EnumFacing insideDirection;
   private int lastActivityTimestamp;
   private boolean isDetachedFromVillageFlag;
   private int doorOpeningRestrictionCounter;

   public VillageDoorInfo(BlockPos var1, int var2, int var3, int var4) {
      this(pos, getFaceDirection(deltaX, deltaZ), timestamp);
   }

   private static EnumFacing getFaceDirection(int var0, int var1) {
      return deltaX < 0 ? EnumFacing.WEST : (deltaX > 0 ? EnumFacing.EAST : (deltaZ < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH));
   }

   public VillageDoorInfo(BlockPos var1, EnumFacing var2, int var3) {
      this.doorBlockPos = pos;
      this.insideDirection = facing;
      this.insideBlock = pos.offset(facing, 2);
      this.lastActivityTimestamp = timestamp;
   }

   public int getDistanceSquared(int var1, int var2, int var3) {
      return (int)this.doorBlockPos.distanceSq((double)x, (double)y, (double)z);
   }

   public int getDistanceToDoorBlockSq(BlockPos var1) {
      return (int)pos.distanceSq(this.getDoorBlockPos());
   }

   public int getDistanceToInsideBlockSq(BlockPos var1) {
      return (int)this.insideBlock.distanceSq(pos);
   }

   public boolean isInsideSide(BlockPos var1) {
      int i = pos.getX() - this.doorBlockPos.getX();
      int j = pos.getZ() - this.doorBlockPos.getY();
      return i * this.insideDirection.getFrontOffsetX() + j * this.insideDirection.getFrontOffsetZ() >= 0;
   }

   public void resetDoorOpeningRestrictionCounter() {
      this.doorOpeningRestrictionCounter = 0;
   }

   public void incrementDoorOpeningRestrictionCounter() {
      ++this.doorOpeningRestrictionCounter;
   }

   public int getDoorOpeningRestrictionCounter() {
      return this.doorOpeningRestrictionCounter;
   }

   public BlockPos getDoorBlockPos() {
      return this.doorBlockPos;
   }

   public BlockPos getInsideBlockPos() {
      return this.insideBlock;
   }

   public int getInsideOffsetX() {
      return this.insideDirection.getFrontOffsetX() * 2;
   }

   public int getInsideOffsetZ() {
      return this.insideDirection.getFrontOffsetZ() * 2;
   }

   public int getInsidePosY() {
      return this.lastActivityTimestamp;
   }

   public void setLastActivityTimestamp(int var1) {
      this.lastActivityTimestamp = timestamp;
   }

   public boolean getIsDetachedFromVillageFlag() {
      return this.isDetachedFromVillageFlag;
   }

   public void setIsDetachedFromVillageFlag(boolean var1) {
      this.isDetachedFromVillageFlag = detached;
   }

   public EnumFacing getInsideDirection() {
      return this.insideDirection;
   }
}
