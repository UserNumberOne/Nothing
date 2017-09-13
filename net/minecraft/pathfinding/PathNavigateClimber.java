package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PathNavigateClimber extends PathNavigateGround {
   private BlockPos targetPosition;

   public PathNavigateClimber(EntityLiving var1, World var2) {
      super(entityLivingIn, worldIn);
   }

   public Path getPathToPos(BlockPos var1) {
      this.targetPosition = pos;
      return super.getPathToPos(pos);
   }

   public Path getPathToEntityLiving(Entity var1) {
      this.targetPosition = new BlockPos(entityIn);
      return super.getPathToEntityLiving(entityIn);
   }

   public boolean tryMoveToEntityLiving(Entity var1, double var2) {
      Path path = this.getPathToEntityLiving(entityIn);
      if (path != null) {
         return this.setPath(path, speedIn);
      } else {
         this.targetPosition = new BlockPos(entityIn);
         this.speed = speedIn;
         return true;
      }
   }

   public void onUpdateNavigation() {
      if (!this.noPath()) {
         super.onUpdateNavigation();
      } else if (this.targetPosition != null) {
         double d0 = (double)(this.theEntity.width * this.theEntity.width);
         if (this.theEntity.getDistanceSqToCenter(this.targetPosition) < d0 || this.theEntity.posY > (double)this.targetPosition.getY() && this.theEntity.getDistanceSqToCenter(new BlockPos(this.targetPosition.getX(), MathHelper.floor(this.theEntity.posY), this.targetPosition.getZ())) < d0) {
            this.targetPosition = null;
         } else {
            this.theEntity.getMoveHelper().setMoveTo((double)this.targetPosition.getX(), (double)this.targetPosition.getY(), (double)this.targetPosition.getZ(), this.speed);
         }
      }

   }
}
