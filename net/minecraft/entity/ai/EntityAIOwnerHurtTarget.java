package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAIOwnerHurtTarget extends EntityAITarget {
   EntityTameable theEntityTameable;
   EntityLivingBase theTarget;
   private int timestamp;

   public EntityAIOwnerHurtTarget(EntityTameable entitytameableanimal) {
      super(entitytameableanimal, false);
      this.theEntityTameable = entitytameableanimal;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (!this.theEntityTameable.isTamed()) {
         return false;
      } else {
         EntityLivingBase entityliving = this.theEntityTameable.getOwner();
         if (entityliving == null) {
            return false;
         } else {
            this.theTarget = entityliving.getLastAttacker();
            int i = entityliving.getLastAttackerTime();
            return i != this.timestamp && this.isSuitableTarget(this.theTarget, false) && this.theEntityTameable.shouldAttackEntity(this.theTarget, entityliving);
         }
      }
   }

   public void startExecuting() {
      this.taskOwner.setGoalTarget(this.theTarget, TargetReason.OWNER_ATTACKED_TARGET, true);
      EntityLivingBase entityliving = this.theEntityTameable.getOwner();
      if (entityliving != null) {
         this.timestamp = entityliving.getLastAttackerTime();
      }

      super.startExecuting();
   }
}
