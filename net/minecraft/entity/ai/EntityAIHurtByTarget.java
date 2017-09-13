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
      double var1 = this.getTargetDistance();

      for(EntityCreature var5 : this.taskOwner.world.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)).expand(var1, 10.0D, var1))) {
         if (this.taskOwner != var5 && var5.getAttackTarget() == null && (!(this.taskOwner instanceof EntityTameable) || ((EntityTameable)this.taskOwner).getOwner() == ((EntityTameable)var5).getOwner()) && !var5.isOnSameTeam(this.taskOwner.getAITarget())) {
            boolean var6 = false;

            for(Class var10 : this.targetClasses) {
               if (var5.getClass() == var10) {
                  var6 = true;
                  break;
               }
            }

            if (!var6) {
               this.setEntityAttackTarget(var5, this.taskOwner.getAITarget());
            }
         }
      }

   }

   protected void setEntityAttackTarget(EntityCreature var1, EntityLivingBase var2) {
      var1.setGoalTarget(var2, TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
   }
}
