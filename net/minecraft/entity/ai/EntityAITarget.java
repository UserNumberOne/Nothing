package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class EntityAITarget extends EntityAIBase {
   protected final EntityCreature taskOwner;
   protected boolean shouldCheckSight;
   private final boolean nearbyOnly;
   private int targetSearchStatus;
   private int targetSearchDelay;
   private int targetUnseenTicks;
   protected EntityLivingBase target;
   protected int unseenMemoryTicks;

   public EntityAITarget(EntityCreature var1, boolean var2) {
      this(var1, var2, false);
   }

   public EntityAITarget(EntityCreature var1, boolean var2, boolean var3) {
      this.unseenMemoryTicks = 60;
      this.taskOwner = var1;
      this.shouldCheckSight = var2;
      this.nearbyOnly = var3;
   }

   public boolean continueExecuting() {
      EntityLivingBase var1 = this.taskOwner.getAttackTarget();
      if (var1 == null) {
         var1 = this.target;
      }

      if (var1 == null) {
         return false;
      } else if (!var1.isEntityAlive()) {
         return false;
      } else {
         Team var2 = this.taskOwner.getTeam();
         Team var3 = var1.getTeam();
         if (var2 != null && var3 == var2) {
            return false;
         } else {
            double var4 = this.getTargetDistance();
            if (this.taskOwner.getDistanceSqToEntity(var1) > var4 * var4) {
               return false;
            } else {
               if (this.shouldCheckSight) {
                  if (this.taskOwner.getEntitySenses().canSee(var1)) {
                     this.targetUnseenTicks = 0;
                  } else if (++this.targetUnseenTicks > this.unseenMemoryTicks) {
                     return false;
                  }
               }

               if (var1 instanceof EntityPlayer && ((EntityPlayer)var1).capabilities.disableDamage) {
                  return false;
               } else {
                  this.taskOwner.setAttackTarget(var1);
                  return true;
               }
            }
         }
      }
   }

   protected double getTargetDistance() {
      IAttributeInstance var1 = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
      return var1 == null ? 16.0D : var1.getAttributeValue();
   }

   public void startExecuting() {
      this.targetSearchStatus = 0;
      this.targetSearchDelay = 0;
      this.targetUnseenTicks = 0;
   }

   public void resetTask() {
      this.taskOwner.setAttackTarget((EntityLivingBase)null);
      this.target = null;
   }

   public static boolean isSuitableTarget(EntityLiving var0, EntityLivingBase var1, boolean var2, boolean var3) {
      if (var1 == null) {
         return false;
      } else if (var1 == var0) {
         return false;
      } else if (!var1.isEntityAlive()) {
         return false;
      } else if (!var0.canAttackClass(var1.getClass())) {
         return false;
      } else if (var0.isOnSameTeam(var1)) {
         return false;
      } else {
         if (var0 instanceof IEntityOwnable && ((IEntityOwnable)var0).getOwnerId() != null) {
            if (var1 instanceof IEntityOwnable && ((IEntityOwnable)var0).getOwnerId().equals(var1.getUniqueID())) {
               return false;
            }

            if (var1 == ((IEntityOwnable)var0).getOwner()) {
               return false;
            }
         } else if (var1 instanceof EntityPlayer && !var2 && ((EntityPlayer)var1).capabilities.disableDamage) {
            return false;
         }

         return !var3 || var0.getEntitySenses().canSee(var1);
      }
   }

   protected boolean isSuitableTarget(EntityLivingBase var1, boolean var2) {
      if (!isSuitableTarget(this.taskOwner, var1, var2, this.shouldCheckSight)) {
         return false;
      } else if (!this.taskOwner.isWithinHomeDistanceFromPosition(new BlockPos(var1))) {
         return false;
      } else {
         if (this.nearbyOnly) {
            if (--this.targetSearchDelay <= 0) {
               this.targetSearchStatus = 0;
            }

            if (this.targetSearchStatus == 0) {
               this.targetSearchStatus = this.canEasilyReach(var1) ? 1 : 2;
            }

            if (this.targetSearchStatus == 2) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean canEasilyReach(EntityLivingBase var1) {
      this.targetSearchDelay = 10 + this.taskOwner.getRNG().nextInt(5);
      Path var2 = this.taskOwner.getNavigator().getPathToEntityLiving(var1);
      if (var2 == null) {
         return false;
      } else {
         PathPoint var3 = var2.getFinalPathPoint();
         if (var3 == null) {
            return false;
         } else {
            int var4 = var3.xCoord - MathHelper.floor(var1.posX);
            int var5 = var3.zCoord - MathHelper.floor(var1.posZ);
            return (double)(var4 * var4 + var5 * var5) <= 2.25D;
         }
      }
   }
}
