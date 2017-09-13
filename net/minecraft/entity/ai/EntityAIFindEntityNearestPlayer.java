package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAIFindEntityNearestPlayer extends EntityAIBase {
   private static final Logger LOGGER = LogManager.getLogger();
   private final EntityLiving entityLiving;
   private final Predicate predicate;
   private final EntityAINearestAttackableTarget.Sorter sorter;
   private EntityLivingBase entityTarget;

   public EntityAIFindEntityNearestPlayer(EntityLiving var1) {
      this.entityLiving = var1;
      if (var1 instanceof EntityCreature) {
         LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!");
      }

      this.predicate = new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            if (!(var1 instanceof EntityPlayer)) {
               return false;
            } else if (((EntityPlayer)var1).capabilities.disableDamage) {
               return false;
            } else {
               double var2 = EntityAIFindEntityNearestPlayer.this.maxTargetRange();
               if (var1.isSneaking()) {
                  var2 *= 0.800000011920929D;
               }

               if (var1.isInvisible()) {
                  float var4 = ((EntityPlayer)var1).getArmorVisibility();
                  if (var4 < 0.1F) {
                     var4 = 0.1F;
                  }

                  var2 *= (double)(0.7F * var4);
               }

               return (double)var1.getDistanceToEntity(EntityAIFindEntityNearestPlayer.this.entityLiving) > var2 ? false : EntityAITarget.isSuitableTarget(EntityAIFindEntityNearestPlayer.this.entityLiving, (EntityLivingBase)var1, false, true);
            }
         }

         public boolean apply(Object var1) {
            return this.apply((Entity)var1);
         }
      };
      this.sorter = new EntityAINearestAttackableTarget.Sorter(var1);
   }

   public boolean shouldExecute() {
      double var1 = this.maxTargetRange();
      List var3 = this.entityLiving.world.getEntitiesWithinAABB(EntityPlayer.class, this.entityLiving.getEntityBoundingBox().expand(var1, 4.0D, var1), this.predicate);
      Collections.sort(var3, this.sorter);
      if (var3.isEmpty()) {
         return false;
      } else {
         this.entityTarget = (EntityLivingBase)var3.get(0);
         return true;
      }
   }

   public boolean continueExecuting() {
      EntityLivingBase var1 = this.entityLiving.getAttackTarget();
      if (var1 == null) {
         return false;
      } else if (!var1.isEntityAlive()) {
         return false;
      } else if (var1 instanceof EntityPlayer && ((EntityPlayer)var1).capabilities.disableDamage) {
         return false;
      } else {
         Team var2 = this.entityLiving.getTeam();
         Team var3 = var1.getTeam();
         if (var2 != null && var3 == var2) {
            return false;
         } else {
            double var4 = this.maxTargetRange();
            return this.entityLiving.getDistanceSqToEntity(var1) > var4 * var4 ? false : !(var1 instanceof EntityPlayerMP) || !((EntityPlayerMP)var1).interactionManager.isCreative();
         }
      }
   }

   public void startExecuting() {
      this.entityLiving.setGoalTarget(this.entityTarget, TargetReason.CLOSEST_PLAYER, true);
      super.startExecuting();
   }

   public void resetTask() {
      this.entityLiving.setAttackTarget((EntityLivingBase)null);
      super.startExecuting();
   }

   protected double maxTargetRange() {
      IAttributeInstance var1 = this.entityLiving.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
      return var1 == null ? 16.0D : var1.getAttributeValue();
   }
}
