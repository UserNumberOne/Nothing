package net.minecraft.entity.ai;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAITempt extends EntityAIBase {
   private final EntityCreature temptedEntity;
   private final double speed;
   private double targetX;
   private double targetY;
   private double targetZ;
   private double pitch;
   private double yaw;
   private EntityLivingBase temptingPlayer;
   private int delayTemptCounter;
   private boolean isRunning;
   private final Set temptItem;
   private final boolean scaredByPlayerMovement;

   public EntityAITempt(EntityCreature var1, double var2, Item var4, boolean var5) {
      this(var1, var2, var5, Sets.newHashSet(new Item[]{var4}));
   }

   public EntityAITempt(EntityCreature var1, double var2, boolean var4, Set var5) {
      this.temptedEntity = var1;
      this.speed = var2;
      this.temptItem = var5;
      this.scaredByPlayerMovement = var4;
      this.setMutexBits(3);
      if (!(var1.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
      }
   }

   public boolean shouldExecute() {
      if (this.delayTemptCounter > 0) {
         --this.delayTemptCounter;
         return false;
      } else {
         this.temptingPlayer = this.temptedEntity.world.getClosestPlayerToEntity(this.temptedEntity, 10.0D);
         boolean var1 = this.temptingPlayer == null ? false : this.isTempting(this.temptingPlayer.getHeldItemMainhand()) || this.isTempting(this.temptingPlayer.getHeldItemOffhand());
         if (var1) {
            EntityTargetLivingEntityEvent var2 = CraftEventFactory.callEntityTargetLivingEvent(this.temptedEntity, this.temptingPlayer, TargetReason.TEMPT);
            if (var2.isCancelled()) {
               return false;
            }

            this.temptingPlayer = ((CraftLivingEntity)var2.getTarget()).getHandle();
         }

         return var1;
      }
   }

   protected boolean isTempting(@Nullable ItemStack var1) {
      return var1 == null ? false : this.temptItem.contains(var1.getItem());
   }

   public boolean continueExecuting() {
      if (this.scaredByPlayerMovement) {
         if (this.temptedEntity.getDistanceSqToEntity(this.temptingPlayer) < 36.0D) {
            if (this.temptingPlayer.getDistanceSq(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
               return false;
            }

            if (Math.abs((double)this.temptingPlayer.rotationPitch - this.pitch) > 5.0D || Math.abs((double)this.temptingPlayer.rotationYaw - this.yaw) > 5.0D) {
               return false;
            }
         } else {
            this.targetX = this.temptingPlayer.posX;
            this.targetY = this.temptingPlayer.posY;
            this.targetZ = this.temptingPlayer.posZ;
         }

         this.pitch = (double)this.temptingPlayer.rotationPitch;
         this.yaw = (double)this.temptingPlayer.rotationYaw;
      }

      return this.shouldExecute();
   }

   public void startExecuting() {
      this.targetX = this.temptingPlayer.posX;
      this.targetY = this.temptingPlayer.posY;
      this.targetZ = this.temptingPlayer.posZ;
      this.isRunning = true;
   }

   public void resetTask() {
      this.temptingPlayer = null;
      this.temptedEntity.getNavigator().clearPathEntity();
      this.delayTemptCounter = 100;
      this.isRunning = false;
   }

   public void updateTask() {
      this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.temptingPlayer, (float)(this.temptedEntity.getHorizontalFaceSpeed() + 20), (float)this.temptedEntity.getVerticalFaceSpeed());
      if (this.temptedEntity.getDistanceSqToEntity(this.temptingPlayer) < 6.25D) {
         this.temptedEntity.getNavigator().clearPathEntity();
      } else {
         this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.speed);
      }

   }

   public boolean isRunning() {
      return this.isRunning;
   }
}
