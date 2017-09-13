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
      super(var1, true);
      this.entityCallsForHelp = var2;
      this.targetClasses = var3;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      int var1 = this.taskOwner.getRevengeTimer();
      EntityLivingBase var2 = this.taskOwner.getAITarget();
      return var1 != this.revengeTimerOld && var2 != null && this.isSuitableTarget(var2, false);
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
      double var1 = this.getTargetDistance();

      for(EntityCreature var4 : this.taskOwner.world.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)).expand(var1, 10.0D, var1))) {
         if (this.taskOwner != var4 && var4.getAttackTarget() == null && (!(this.taskOwner instanceof EntityTameable) || ((EntityTameable)this.taskOwner).getOwner() == ((EntityTameable)var4).getOwner()) && !var4.isOnSameTeam(this.taskOwner.getAITarget())) {
            boolean var5 = false;

            for(Class var9 : this.targetClasses) {
               if (var4.getClass() == var9) {
                  var5 = true;
                  break;
               }
            }

            if (!var5) {
               this.setEntityAttackTarget(var4, this.taskOwner.getAITarget());
            }
         }
      }

   }

   protected void setEntityAttackTarget(EntityCreature var1, EntityLivingBase var2) {
      var1.setAttackTarget(var2);
   }
}
