package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;

public class EntityAIOpenDoor extends EntityAIDoorInteract {
   boolean closeDoor;
   int closeDoorTemporisation;

   public EntityAIOpenDoor(EntityLiving var1, boolean var2) {
      super(entitylivingIn);
      this.theEntity = entitylivingIn;
      this.closeDoor = shouldClose;
   }

   public boolean continueExecuting() {
      return this.closeDoor && this.closeDoorTemporisation > 0 && super.continueExecuting();
   }

   public void startExecuting() {
      this.closeDoorTemporisation = 20;
      this.doorBlock.toggleDoor(this.theEntity.world, this.doorPosition, true);
   }

   public void resetTask() {
      if (this.closeDoor) {
         this.doorBlock.toggleDoor(this.theEntity.world, this.doorPosition, false);
      }

   }

   public void updateTask() {
      --this.closeDoorTemporisation;
      super.updateTask();
   }
}
