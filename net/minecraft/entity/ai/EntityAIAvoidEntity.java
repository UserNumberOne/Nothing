package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.Vec3d;

public class EntityAIAvoidEntity extends EntityAIBase {
   private final Predicate canBeSeenSelector;
   protected EntityCreature theEntity;
   private final double farSpeed;
   private final double nearSpeed;
   protected Entity closestLivingEntity;
   private final float avoidDistance;
   private Path entityPathEntity;
   private final PathNavigate entityPathNavigate;
   private final Class classToAvoid;
   private final Predicate avoidTargetSelector;

   public EntityAIAvoidEntity(EntityCreature var1, Class var2, float var3, double var4, double var6) {
      this(var1, var2, Predicates.alwaysTrue(), var3, var4, var6);
   }

   public EntityAIAvoidEntity(EntityCreature var1, Class var2, Predicate var3, float var4, double var5, double var7) {
      this.canBeSeenSelector = new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            return var1.isEntityAlive() && EntityAIAvoidEntity.this.theEntity.getEntitySenses().canSee(var1);
         }
      };
      this.theEntity = var1;
      this.classToAvoid = var2;
      this.avoidTargetSelector = var3;
      this.avoidDistance = var4;
      this.farSpeed = var5;
      this.nearSpeed = var7;
      this.entityPathNavigate = var1.getNavigator();
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      List var1 = this.theEntity.world.getEntitiesWithinAABB(this.classToAvoid, this.theEntity.getEntityBoundingBox().expand((double)this.avoidDistance, 3.0D, (double)this.avoidDistance), Predicates.and(new Predicate[]{EntitySelectors.CAN_AI_TARGET, this.canBeSeenSelector, this.avoidTargetSelector}));
      if (var1.isEmpty()) {
         return false;
      } else {
         this.closestLivingEntity = (Entity)var1.get(0);
         Vec3d var2 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.theEntity, 16, 7, new Vec3d(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ));
         if (var2 == null) {
            return false;
         } else if (this.closestLivingEntity.getDistanceSq(var2.xCoord, var2.yCoord, var2.zCoord) < this.closestLivingEntity.getDistanceSqToEntity(this.theEntity)) {
            return false;
         } else {
            this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(var2.xCoord, var2.yCoord, var2.zCoord);
            return this.entityPathEntity != null;
         }
      }
   }

   public boolean continueExecuting() {
      return !this.entityPathNavigate.noPath();
   }

   public void startExecuting() {
      this.entityPathNavigate.setPath(this.entityPathEntity, this.farSpeed);
   }

   public void resetTask() {
      this.closestLivingEntity = null;
   }

   public void updateTask() {
      if (this.theEntity.getDistanceSqToEntity(this.closestLivingEntity) < 49.0D) {
         this.theEntity.getNavigator().setSpeed(this.nearSpeed);
      } else {
         this.theEntity.getNavigator().setSpeed(this.farSpeed);
      }

   }
}
