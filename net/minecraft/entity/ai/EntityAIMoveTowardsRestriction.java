package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityAIMoveTowardsRestriction extends EntityAIBase {
   private final EntityCreature theEntity;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private final double movementSpeed;

   public EntityAIMoveTowardsRestriction(EntityCreature var1, double var2) {
      this.theEntity = var1;
      this.movementSpeed = var2;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (this.theEntity.isWithinHomeDistanceCurrentPosition()) {
         return false;
      } else {
         BlockPos var1 = this.theEntity.getHomePosition();
         Vec3d var2 = RandomPositionGenerator.findRandomTargetBlockTowards(this.theEntity, 16, 7, new Vec3d((double)var1.getX(), (double)var1.getY(), (double)var1.getZ()));
         if (var2 == null) {
            return false;
         } else {
            this.movePosX = var2.xCoord;
            this.movePosY = var2.yCoord;
            this.movePosZ = var2.zCoord;
            return true;
         }
      }
   }

   public boolean continueExecuting() {
      return !this.theEntity.getNavigator().noPath();
   }

   public void startExecuting() {
      this.theEntity.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.movementSpeed);
   }
}
