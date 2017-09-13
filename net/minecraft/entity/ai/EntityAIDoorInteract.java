package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public abstract class EntityAIDoorInteract extends EntityAIBase {
   protected EntityLiving theEntity;
   protected BlockPos doorPosition = BlockPos.ORIGIN;
   protected BlockDoor doorBlock;
   boolean hasStoppedDoorInteraction;
   float entityPositionX;
   float entityPositionZ;

   public EntityAIDoorInteract(EntityLiving var1) {
      this.theEntity = var1;
      if (!(var1.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   public boolean shouldExecute() {
      if (!this.theEntity.isCollidedHorizontally) {
         return false;
      } else {
         PathNavigateGround var1 = (PathNavigateGround)this.theEntity.getNavigator();
         Path var2 = var1.getPath();
         if (var2 != null && !var2.isFinished() && var1.getEnterDoors()) {
            for(int var3 = 0; var3 < Math.min(var2.getCurrentPathIndex() + 2, var2.getCurrentPathLength()); ++var3) {
               PathPoint var4 = var2.getPathPointFromIndex(var3);
               this.doorPosition = new BlockPos(var4.xCoord, var4.yCoord + 1, var4.zCoord);
               if (this.theEntity.getDistanceSq((double)this.doorPosition.getX(), this.theEntity.posY, (double)this.doorPosition.getZ()) <= 2.25D) {
                  this.doorBlock = this.getBlockDoor(this.doorPosition);
                  if (this.doorBlock != null) {
                     return true;
                  }
               }
            }

            this.doorPosition = (new BlockPos(this.theEntity)).up();
            this.doorBlock = this.getBlockDoor(this.doorPosition);
            return this.doorBlock != null;
         } else {
            return false;
         }
      }
   }

   public boolean continueExecuting() {
      return !this.hasStoppedDoorInteraction;
   }

   public void startExecuting() {
      this.hasStoppedDoorInteraction = false;
      this.entityPositionX = (float)((double)((float)this.doorPosition.getX() + 0.5F) - this.theEntity.posX);
      this.entityPositionZ = (float)((double)((float)this.doorPosition.getZ() + 0.5F) - this.theEntity.posZ);
   }

   public void updateTask() {
      float var1 = (float)((double)((float)this.doorPosition.getX() + 0.5F) - this.theEntity.posX);
      float var2 = (float)((double)((float)this.doorPosition.getZ() + 0.5F) - this.theEntity.posZ);
      float var3 = this.entityPositionX * var1 + this.entityPositionZ * var2;
      if (var3 < 0.0F) {
         this.hasStoppedDoorInteraction = true;
      }

   }

   private BlockDoor getBlockDoor(BlockPos var1) {
      IBlockState var2 = this.theEntity.world.getBlockState(var1);
      Block var3 = var2.getBlock();
      return var3 instanceof BlockDoor && var2.getMaterial() == Material.WOOD ? (BlockDoor)var3 : null;
   }
}
