package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAIHurtByTarget extends EntityAITarget {
   private final boolean entityCallsForHelp;
   private int revengeTimerOld;
   private final Class[] targetClasses;

   public EntityAIHurtByTarget(EntityCreature var1, boolean var2, Class... var3) {
      super(creatureIn, true);
      this.entityCallsForHelp = entityCallsForHelpIn;
      this.targetClasses = targetClassesIn;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      int i = this.taskOwner.getRevengeTimer();
      EntityLivingBase entitylivingbase = this.taskOwner.getAITarget();
      return i != this.revengeTimerOld && entitylivingbase != null && this.isSuitableTarget(entitylivingbase, false);
   }

   public void startExecuting() {
      this.taskOwner.setAttackTarget(this.taskOwner.getAITarget());
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

   protected void setEntityAttackTarget(EntityCreature var1, EntityLivingBase var2) {
      creatureIn.setAttackTarget(entityLivingBaseIn);
   }
}
