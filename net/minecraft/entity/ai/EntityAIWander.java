package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.Vec3d;

public class EntityAIWander extends EntityAIBase {
   private final EntityCreature entity;
   private double xPosition;
   private double yPosition;
   private double zPosition;
   private final double speed;
   private int executionChance;
   private boolean mustUpdate;

   public EntityAIWander(EntityCreature var1, double var2) {
      this(var1, var2, 120);
   }

   public EntityAIWander(EntityCreature var1, double var2, int var4) {
      this.entity = var1;
      this.speed = var2;
      this.executionChance = var4;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (!this.mustUpdate) {
         if (this.entity.getAge() >= 100) {
            return false;
         }

         if (this.entity.getRNG().nextInt(this.executionChance) != 0) {
            return false;
         }
      }

      Vec3d var1 = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
      if (var1 == null) {
         return false;
      } else {
         this.xPosition = var1.xCoord;
         this.yPosition = var1.yCoord;
         this.zPosition = var1.zCoord;
         this.mustUpdate = false;
         return true;
      }
   }

   public boolean continueExecuting() {
      return !this.entity.getNavigator().noPath();
   }

   public void startExecuting() {
      this.entity.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
   }

   public void makeUpdate() {
      this.mustUpdate = true;
   }

   public void setExecutionChance(int var1) {
      this.executionChance = var1;
   }
}
