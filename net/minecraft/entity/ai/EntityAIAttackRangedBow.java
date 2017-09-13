package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.util.EnumHand;

public class EntityAIAttackRangedBow extends EntityAIBase {
   private final EntitySkeleton entity;
   private final double moveSpeedAmp;
   private int attackCooldown;
   private final float maxAttackDistance;
   private int attackTime = -1;
   private int seeTime;
   private boolean strafingClockwise;
   private boolean strafingBackwards;
   private int strafingTime = -1;

   public EntityAIAttackRangedBow(EntitySkeleton var1, double var2, int var4, float var5) {
      this.entity = var1;
      this.moveSpeedAmp = var2;
      this.attackCooldown = var4;
      this.maxAttackDistance = var5 * var5;
      this.setMutexBits(3);
   }

   public void setAttackCooldown(int var1) {
      this.attackCooldown = var1;
   }

   public boolean shouldExecute() {
      return this.entity.getAttackTarget() == null ? false : this.isBowInMainhand();
   }

   protected boolean isBowInMainhand() {
      return this.entity.getHeldItemMainhand() != null && this.entity.getHeldItemMainhand().getItem() == Items.BOW;
   }

   public boolean continueExecuting() {
      return (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.isBowInMainhand();
   }

   public void startExecuting() {
      super.startExecuting();
      this.entity.setSwingingArms(true);
   }

   public void resetTask() {
      super.resetTask();
      this.entity.setSwingingArms(false);
      this.seeTime = 0;
      this.attackTime = -1;
      this.entity.resetActiveHand();
   }

   public void updateTask() {
      EntityLivingBase var1 = this.entity.getAttackTarget();
      if (var1 != null) {
         double var2 = this.entity.getDistanceSq(var1.posX, var1.getEntityBoundingBox().minY, var1.posZ);
         boolean var4 = this.entity.getEntitySenses().canSee(var1);
         boolean var5 = this.seeTime > 0;
         if (var4 != var5) {
            this.seeTime = 0;
         }

         if (var4) {
            ++this.seeTime;
         } else {
            --this.seeTime;
         }

         if (var2 <= (double)this.maxAttackDistance && this.seeTime >= 20) {
            this.entity.getNavigator().clearPathEntity();
            ++this.strafingTime;
         } else {
            this.entity.getNavigator().tryMoveToEntityLiving(var1, this.moveSpeedAmp);
            this.strafingTime = -1;
         }

         if (this.strafingTime >= 20) {
            if ((double)this.entity.getRNG().nextFloat() < 0.3D) {
               this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double)this.entity.getRNG().nextFloat() < 0.3D) {
               this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
         }

         if (this.strafingTime > -1) {
            if (var2 > (double)(this.maxAttackDistance * 0.75F)) {
               this.strafingBackwards = false;
            } else if (var2 < (double)(this.maxAttackDistance * 0.25F)) {
               this.strafingBackwards = true;
            }

            this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.entity.faceEntity(var1, 30.0F, 30.0F);
         } else {
            this.entity.getLookHelper().setLookPositionWithEntity(var1, 30.0F, 30.0F);
         }

         if (this.entity.isHandActive()) {
            if (!var4 && this.seeTime < -60) {
               this.entity.resetActiveHand();
            } else if (var4) {
               int var6 = this.entity.getItemInUseMaxCount();
               if (var6 >= 20) {
                  this.entity.resetActiveHand();
                  this.entity.attackEntityWithRangedAttack(var1, ItemBow.getArrowVelocity(var6));
                  this.attackTime = this.attackCooldown;
               }
            }
         } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            this.entity.setActiveHand(EnumHand.MAIN_HAND);
         }
      }

   }
}
