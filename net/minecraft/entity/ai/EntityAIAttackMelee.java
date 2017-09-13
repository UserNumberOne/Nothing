package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityAIAttackMelee extends EntityAIBase {
   World world;
   protected EntityCreature attacker;
   protected int attackTick;
   double speedTowardsTarget;
   boolean longMemory;
   Path entityPathEntity;
   private int delayCounter;
   private double targetX;
   private double targetY;
   private double targetZ;
   protected final int attackInterval = 20;

   public EntityAIAttackMelee(EntityCreature var1, double var2, boolean var4) {
      this.attacker = var1;
      this.world = var1.world;
      this.speedTowardsTarget = var2;
      this.longMemory = var4;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      EntityLivingBase var1 = this.attacker.getAttackTarget();
      if (var1 == null) {
         return false;
      } else if (!var1.isEntityAlive()) {
         return false;
      } else {
         this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(var1);
         return this.entityPathEntity != null;
      }
   }

   public boolean continueExecuting() {
      EntityLivingBase var1 = this.attacker.getAttackTarget();
      if (var1 == null) {
         return false;
      } else if (!var1.isEntityAlive()) {
         return false;
      } else if (!this.longMemory) {
         return !this.attacker.getNavigator().noPath();
      } else if (!this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(var1))) {
         return false;
      } else {
         return !(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).isSpectator() && !((EntityPlayer)var1).isCreative();
      }
   }

   public void startExecuting() {
      this.attacker.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
      this.delayCounter = 0;
   }

   public void resetTask() {
      EntityLivingBase var1 = this.attacker.getAttackTarget();
      if (var1 instanceof EntityPlayer && (((EntityPlayer)var1).isSpectator() || ((EntityPlayer)var1).isCreative())) {
         this.attacker.setAttackTarget((EntityLivingBase)null);
      }

      this.attacker.getNavigator().clearPathEntity();
   }

   public void updateTask() {
      EntityLivingBase var1 = this.attacker.getAttackTarget();
      this.attacker.getLookHelper().setLookPositionWithEntity(var1, 30.0F, 30.0F);
      double var2 = this.attacker.getDistanceSq(var1.posX, var1.getEntityBoundingBox().minY, var1.posZ);
      --this.delayCounter;
      if ((this.longMemory || this.attacker.getEntitySenses().canSee(var1)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || var1.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.attacker.getRNG().nextFloat() < 0.05F)) {
         this.targetX = var1.posX;
         this.targetY = var1.getEntityBoundingBox().minY;
         this.targetZ = var1.posZ;
         this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
         if (var2 > 1024.0D) {
            this.delayCounter += 10;
         } else if (var2 > 256.0D) {
            this.delayCounter += 5;
         }

         if (!this.attacker.getNavigator().tryMoveToEntityLiving(var1, this.speedTowardsTarget)) {
            this.delayCounter += 15;
         }
      }

      this.attackTick = Math.max(this.attackTick - 1, 0);
      this.checkAndPerformAttack(var1, var2);
   }

   protected void checkAndPerformAttack(EntityLivingBase var1, double var2) {
      double var4 = this.getAttackReachSqr(var1);
      if (var2 <= var4 && this.attackTick <= 0) {
         this.attackTick = 20;
         this.attacker.swingArm(EnumHand.MAIN_HAND);
         this.attacker.attackEntityAsMob(var1);
      }

   }

   protected double getAttackReachSqr(EntityLivingBase var1) {
      return (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F + var1.width);
   }
}
