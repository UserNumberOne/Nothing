package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAIFindEntityNearest extends EntityAIBase {
   private static final Logger LOGGER = LogManager.getLogger();
   private final EntityLiving mob;
   private final Predicate predicate;
   private final EntityAINearestAttackableTarget.Sorter sorter;
   private EntityLivingBase target;
   private final Class classToCheck;

   public EntityAIFindEntityNearest(EntityLiving entityinsentient, Class oclass) {
      this.mob = entityinsentient;
      this.classToCheck = oclass;
      if (entityinsentient instanceof EntityCreature) {
         LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!");
      }

      this.predicate = new Predicate() {
         public boolean apply(@Nullable EntityLivingBase entityliving) {
            double d0 = EntityAIFindEntityNearest.this.getFollowRange();
            if (entityliving.isSneaking()) {
               d0 *= 0.800000011920929D;
            }

            return entityliving.isInvisible() ? false : ((double)entityliving.getDistanceToEntity(EntityAIFindEntityNearest.this.mob) > d0 ? false : EntityAITarget.isSuitableTarget(EntityAIFindEntityNearest.this.mob, entityliving, false, true));
         }

         public boolean apply(Object object) {
            return this.apply((EntityLivingBase)object);
         }
      };
      this.sorter = new EntityAINearestAttackableTarget.Sorter(entityinsentient);
   }

   public boolean shouldExecute() {
      double d0 = this.getFollowRange();
      List list = this.mob.world.getEntitiesWithinAABB(this.classToCheck, this.mob.getEntityBoundingBox().expand(d0, 4.0D, d0), this.predicate);
      Collections.sort(list, this.sorter);
      if (list.isEmpty()) {
         return false;
      } else {
         this.target = (EntityLivingBase)list.get(0);
         return true;
      }
   }

   public boolean continueExecuting() {
      EntityLivingBase entityliving = this.mob.getAttackTarget();
      if (entityliving == null) {
         return false;
      } else if (!entityliving.isEntityAlive()) {
         return false;
      } else {
         double d0 = this.getFollowRange();
         return this.mob.getDistanceSqToEntity(entityliving) > d0 * d0 ? false : !(entityliving instanceof EntityPlayerMP) || !((EntityPlayerMP)entityliving).interactionManager.isCreative();
      }
   }

   public void startExecuting() {
      this.mob.setGoalTarget(this.target, TargetReason.CLOSEST_ENTITY, true);
      super.startExecuting();
   }

   public void resetTask() {
      this.mob.setAttackTarget((EntityLivingBase)null);
      super.startExecuting();
   }

   protected double getFollowRange() {
      IAttributeInstance attributeinstance = this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
      return attributeinstance == null ? 16.0D : attributeinstance.getAttributeValue();
   }
}
