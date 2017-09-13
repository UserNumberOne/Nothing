package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.util.math.MathHelper;

public class EntityAIAttackRanged extends EntityAIBase {
   private final EntityLiving entityHost;
   private final IRangedAttackMob rangedAttackEntityHost;
   private EntityLivingBase attackTarget;
   private int rangedAttackTime;
   private final double entityMoveSpeed;
   private int seeTime;
   private final int attackIntervalMin;
   private final int maxRangedAttackTime;
   private final float attackRadius;
   private final float maxAttackDistance;

   public EntityAIAttackRanged(IRangedAttackMob var1, double var2, int var4, float var5) {
      this(var1, var2, var4, var4, var5);
   }

   public EntityAIAttackRanged(IRangedAttackMob var1, double var2, int var4, int var5, float var6) {
      this.rangedAttackTime = -1;
      if (!(var1 instanceof EntityLivingBase)) {
         throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
      } else {
         this.rangedAttackEntityHost = var1;
         this.entityHost = (EntityLiving)var1;
         this.entityMoveSpeed = var2;
         this.attackIntervalMin = var4;
         this.maxRangedAttackTime = var5;
         this.attackRadius = var6;
         this.maxAttackDistance = var6 * var6;
         this.setMutexBits(3);
      }
   }

   public boolean shouldExecute() {
      EntityLivingBase var1 = this.entityHost.getAttackTarget();
      if (var1 == null) {
         return false;
      } else {
         this.attackTarget = var1;
         return true;
      }
   }

   public boolean continueExecuting() {
      return this.shouldExecute() || !this.entityHost.getNavigator().noPath();
   }

   public void resetTask() {
      this.attackTarget = null;
      this.seeTime = 0;
      this.rangedAttackTime = -1;
   }

   public void updateTask() {
      double var1 = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.getEntityBoundingBox().minY, this.attackTarget.posZ);
      boolean var3 = this.entityHost.getEntitySenses().canSee(this.attackTarget);
      if (var3) {
         ++this.seeTime;
      } else {
         this.seeTime = 0;
      }

      if (var1 <= (double)this.maxAttackDistance && this.seeTime >= 20) {
         this.entityHost.getNavigator().clearPathEntity();
      } else {
         this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, this.entityMoveSpeed);
      }

      this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
      if (--this.rangedAttackTime == 0) {
         if (var1 > (double)this.maxAttackDistance || !var3) {
            return;
         }

         float var4 = MathHelper.sqrt(var1) / this.attackRadius;
         float var5 = MathHelper.clamp(var4, 0.1F, 1.0F);
         this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, var5);
         this.rangedAttackTime = MathHelper.floor(var4 * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin);
      } else if (this.rangedAttackTime < 0) {
         float var6 = MathHelper.sqrt(var1) / this.attackRadius;
         this.rangedAttackTime = MathHelper.floor(var6 * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin);
      }

   }
}
