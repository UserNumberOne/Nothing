package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAIOwnerHurtTarget extends EntityAITarget {
   EntityTameable theEntityTameable;
   EntityLivingBase theTarget;
   private int timestamp;

   public EntityAIOwnerHurtTarget(EntityTameable var1) {
      super(var1, false);
      this.theEntityTameable = var1;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (!this.theEntityTameable.isTamed()) {
         return false;
      } else {
         EntityLivingBase var1 = this.theEntityTameable.getOwner();
         if (var1 == null) {
            return false;
         } else {
            this.theTarget = var1.getLastAttacker();
            int var2 = var1.getLastAttackerTime();
            return var2 != this.timestamp && this.isSuitableTarget(this.theTarget, false) && this.theEntityTameable.shouldAttackEntity(this.theTarget, var1);
         }
      }
   }

   public void startExecuting() {
      this.taskOwner.setGoalTarget(this.theTarget, TargetReason.OWNER_ATTACKED_TARGET, true);
      EntityLivingBase var1 = this.theEntityTameable.getOwner();
      if (var1 != null) {
         this.timestamp = var1.getLastAttackerTime();
      }

      super.startExecuting();
   }
}
