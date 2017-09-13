package net.minecraft.entity;

import java.util.UUID;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;

public abstract class EntityCreature extends EntityLiving {
   public static final UUID FLEEING_SPEED_MODIFIER_UUID = UUID.fromString("E199AD21-BA8A-4C53-8D13-6182D5C69D3A");
   public static final AttributeModifier FLEEING_SPEED_MODIFIER = (new AttributeModifier(FLEEING_SPEED_MODIFIER_UUID, "Fleeing speed bonus", 2.0D, 2)).setSaved(false);
   private BlockPos homePosition = BlockPos.ORIGIN;
   private float maximumHomeDistance = -1.0F;
   private final EntityAIBase aiBase = new EntityAIMoveTowardsRestriction(this, 1.0D);
   private boolean isMovementAITaskSet;
   private float restoreWaterCost = PathNodeType.WATER.getPriority();

   public EntityCreature(World world) {
      super(world);
   }

   public float getBlockPathWeight(BlockPos blockposition) {
      return 0.0F;
   }

   public boolean getCanSpawnHere() {
      return super.getCanSpawnHere() && this.getBlockPathWeight(new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ)) >= 0.0F;
   }

   public boolean hasPath() {
      return !this.navigator.noPath();
   }

   public boolean isWithinHomeDistanceCurrentPosition() {
      return this.isWithinHomeDistanceFromPosition(new BlockPos(this));
   }

   public boolean isWithinHomeDistanceFromPosition(BlockPos blockposition) {
      return this.maximumHomeDistance == -1.0F ? true : this.homePosition.distanceSq(blockposition) < (double)(this.maximumHomeDistance * this.maximumHomeDistance);
   }

   public void setHomePosAndDistance(BlockPos blockposition, int i) {
      this.homePosition = blockposition;
      this.maximumHomeDistance = (float)i;
   }

   public BlockPos getHomePosition() {
      return this.homePosition;
   }

   public float getMaximumHomeDistance() {
      return this.maximumHomeDistance;
   }

   public void detachHome() {
      this.maximumHomeDistance = -1.0F;
   }

   public boolean hasHome() {
      return this.maximumHomeDistance != -1.0F;
   }

   protected void updateLeashedState() {
      super.updateLeashedState();
      if (this.getLeashed() && this.getLeashedToEntity() != null && this.getLeashedToEntity().world == this.world) {
         Entity entity = this.getLeashedToEntity();
         this.setHomePosAndDistance(new BlockPos((int)entity.posX, (int)entity.posY, (int)entity.posZ), 5);
         float f = this.getDistanceToEntity(entity);
         if (this instanceof EntityTameable && ((EntityTameable)this).isSitting()) {
            if (f > 10.0F) {
               this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.DISTANCE));
               this.clearLeashed(true, true);
            }

            return;
         }

         if (!this.isMovementAITaskSet) {
            this.tasks.addTask(2, this.aiBase);
            if (this.getNavigator() instanceof PathNavigateGround) {
               this.restoreWaterCost = this.getPathPriority(PathNodeType.WATER);
               this.setPathPriority(PathNodeType.WATER, 0.0F);
            }

            this.isMovementAITaskSet = true;
         }

         this.onLeashDistance(f);
         if (f > 4.0F) {
            this.getNavigator().tryMoveToEntityLiving(entity, 1.0D);
         }

         if (f > 6.0F) {
            double d0 = (entity.posX - this.posX) / (double)f;
            double d1 = (entity.posY - this.posY) / (double)f;
            double d2 = (entity.posZ - this.posZ) / (double)f;
            this.motionX += d0 * Math.abs(d0) * 0.4D;
            this.motionY += d1 * Math.abs(d1) * 0.4D;
            this.motionZ += d2 * Math.abs(d2) * 0.4D;
         }

         if (f > 10.0F) {
            this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.DISTANCE));
            this.clearLeashed(true, true);
         }
      } else if (!this.getLeashed() && this.isMovementAITaskSet) {
         this.isMovementAITaskSet = false;
         this.tasks.removeTask(this.aiBase);
         if (this.getNavigator() instanceof PathNavigateGround) {
            this.setPathPriority(PathNodeType.WATER, this.restoreWaterCost);
         }

         this.detachHome();
      }

   }

   protected void onLeashDistance(float f) {
   }
}
