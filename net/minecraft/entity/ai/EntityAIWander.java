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
      this(creatureIn, speedIn, 120);
   }

   public EntityAIWander(EntityCreature var1, double var2, int var4) {
      this.entity = creatureIn;
      this.speed = speedIn;
      this.executionChance = chance;
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

      Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
      if (vec3d == null) {
         return false;
      } else {
         this.xPosition = vec3d.xCoord;
         this.yPosition = vec3d.yCoord;
         this.zPosition = vec3d.zCoord;
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
      this.executionChance = newchance;
   }
}
