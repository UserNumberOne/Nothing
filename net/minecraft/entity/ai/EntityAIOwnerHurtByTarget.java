package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAIOwnerHurtByTarget extends EntityAITarget {
   EntityTameable theDefendingTameable;
   EntityLivingBase theOwnerAttacker;
   private int timestamp;

   public EntityAIOwnerHurtByTarget(EntityTameable entitytameableanimal) {
      super(entitytameableanimal, false);
      this.theDefendingTameable = entitytameableanimal;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (!this.theDefendingTameable.isTamed()) {
         return false;
      } else {
         EntityLivingBase entityliving = this.theDefendingTameable.getOwner();
         if (entityliving == null) {
            return false;
         } else {
            this.theOwnerAttacker = entityliving.getAITarget();
            int i = entityliving.getRevengeTimer();
            return i != this.timestamp && this.isSuitableTarget(this.theOwnerAttacker, false) && this.theDefendingTameable.shouldAttackEntity(this.theOwnerAttacker, entityliving);
         }
      }
   }

   public void startExecuting() {
      this.taskOwner.setGoalTarget(this.theOwnerAttacker, TargetReason.TARGET_ATTACKED_OWNER, true);
      EntityLivingBase entityliving = this.theDefendingTameable.getOwner();
      if (entityliving != null) {
         this.timestamp = entityliving.getRevengeTimer();
      }

      super.startExecuting();
   }
}
