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

public class EntityAIFindEntityNearest extends EntityAIBase {
   private static final Logger LOGGER = LogManager.getLogger();
   private final EntityLiving mob;
   private final Predicate predicate;
   private final EntityAINearestAttackableTarget.Sorter sorter;
   private EntityLivingBase target;
   private final Class classToCheck;

   public EntityAIFindEntityNearest(EntityLiving var1, Class var2) {
      this.mob = var1;
      this.classToCheck = var2;
      if (var1 instanceof EntityCreature) {
         LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!");
      }

      this.predicate = new Predicate() {
         public boolean apply(@Nullable EntityLivingBase var1) {
            double var2 = EntityAIFindEntityNearest.this.getFollowRange();
            if (var1.isSneaking()) {
               var2 *= 0.800000011920929D;
            }

            return var1.isInvisible() ? false : ((double)var1.getDistanceToEntity(EntityAIFindEntityNearest.this.mob) > var2 ? false : EntityAITarget.isSuitableTarget(EntityAIFindEntityNearest.this.mob, var1, false, true));
         }
      };
      this.sorter = new EntityAINearestAttackableTarget.Sorter(var1);
   }

   public boolean shouldExecute() {
      double var1 = this.getFollowRange();
      List var3 = this.mob.world.getEntitiesWithinAABB(this.classToCheck, this.mob.getEntityBoundingBox().expand(var1, 4.0D, var1), this.predicate);
      Collections.sort(var3, this.sorter);
      if (var3.isEmpty()) {
         return false;
      } else {
         this.target = (EntityLivingBase)var3.get(0);
         return true;
      }
   }

   public boolean continueExecuting() {
      EntityLivingBase var1 = this.mob.getAttackTarget();
      if (var1 == null) {
         return false;
      } else if (!var1.isEntityAlive()) {
         return false;
      } else {
         double var2 = this.getFollowRange();
         return this.mob.getDistanceSqToEntity(var1) > var2 * var2 ? false : !(var1 instanceof EntityPlayerMP) || !((EntityPlayerMP)var1).interactionManager.isCreative();
      }
   }

   public void startExecuting() {
      this.mob.setAttackTarget(this.target);
      super.startExecuting();
   }

   public void resetTask() {
      this.mob.setAttackTarget((EntityLivingBase)null);
      super.startExecuting();
   }

   protected double getFollowRange() {
      IAttributeInstance var1 = this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
      return var1 == null ? 16.0D : var1.getAttributeValue();
   }
}
