package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.math.AxisAlignedBB;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAIHurtByTarget extends EntityAITarget {
   private final boolean entityCallsForHelp;
   private int revengeTimerOld;
   private final Class[] targetClasses;

   public EntityAIHurtByTarget(EntityCreature entitycreature, boolean flag, Class... aclass) {
      super(entitycreature, true);
      this.entityCallsForHelp = flag;
      this.targetClasses = aclass;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      int i = this.taskOwner.getRevengeTimer();
      EntityLivingBase entityliving = this.taskOwner.getAITarget();
      return i != this.revengeTimerOld && entityliving != null && this.isSuitableTarget(entityliving, false);
   }

   public void startExecuting() {
      this.taskOwner.setGoalTarget(this.taskOwner.getAITarget(), TargetReason.TARGET_ATTACKED_ENTITY, true);
      this.target = this.taskOwner.getAttackTarget();
      this.revengeTimerOld = this.taskOwner.getRevengeTimer();
      this.unseenMemoryTicks = 300;
      if (this.entityCallsForHelp) {
         this.alertOthers();
      }

      super.startExecuting();
   }

   protected void alertOthers() {
      double d0 = this.getTargetDistance();

      for(EntityCreature entitycreature : this.taskOwner.world.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)).expand(d0, 10.0D, d0))) {
         if (this.taskOwner != entitycreature && entitycreature.getAttackTarget() == null && (!(this.taskOwner instanceof EntityTameable) || ((EntityTameable)this.taskOwner).getOwner() == ((EntityTameable)entitycreature).getOwner()) && !entitycreature.isOnSameTeam(this.taskOwner.getAITarget())) {
            boolean flag = false;

            for(Class oclass : this.targetClasses) {
               if (entitycreature.getClass() == oclass) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               this.setEntityAttackTarget(entitycreature, this.taskOwner.getAITarget());
            }
         }
      }

   }

   protected void setEntityAttackTarget(EntityCreature entitycreature, EntityLivingBase entityliving) {
      entitycreature.setGoalTarget(entityliving, TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
   }
}
