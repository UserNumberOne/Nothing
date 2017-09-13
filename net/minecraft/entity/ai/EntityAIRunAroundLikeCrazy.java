package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class EntityAIRunAroundLikeCrazy extends EntityAIBase {
   private final EntityHorse horseHost;
   private final double speed;
   private double targetX;
   private double targetY;
   private double targetZ;

   public EntityAIRunAroundLikeCrazy(EntityHorse var1, double var2) {
      this.horseHost = var1;
      this.speed = var2;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (!this.horseHost.isTame() && this.horseHost.isBeingRidden()) {
         Vec3d var1 = RandomPositionGenerator.findRandomTarget(this.horseHost, 5, 4);
         if (var1 == null) {
            return false;
         } else {
            this.targetX = var1.xCoord;
            this.targetY = var1.yCoord;
            this.targetZ = var1.zCoord;
            return true;
         }
      } else {
         return false;
      }
   }

   public void startExecuting() {
      this.horseHost.getNavigator().tryMoveToXYZ(this.targetX, this.targetY, this.targetZ, this.speed);
   }

   public boolean continueExecuting() {
      return !this.horseHost.getNavigator().noPath() && this.horseHost.isBeingRidden();
   }

   public void updateTask() {
      if (this.horseHost.getRNG().nextInt(50) == 0) {
         Entity var1 = (Entity)this.horseHost.getPassengers().get(0);
         if (var1 == null) {
            return;
         }

         if (var1 instanceof EntityPlayer) {
            int var2 = this.horseHost.getTemper();
            int var3 = this.horseHost.getMaxTemper();
            if (var3 > 0 && this.horseHost.getRNG().nextInt(var3) < var2) {
               this.horseHost.setTamedBy((EntityPlayer)var1);
               this.horseHost.world.setEntityState(this.horseHost, (byte)7);
               return;
            }

            this.horseHost.increaseTemper(5);
         }

         this.horseHost.removePassengers();
         this.horseHost.makeHorseRearWithSound();
         this.horseHost.world.setEntityState(this.horseHost, (byte)6);
      }

   }
}
