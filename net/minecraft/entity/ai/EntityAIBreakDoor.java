package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.EnumDifficulty;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityAIBreakDoor extends EntityAIDoorInteract {
   private int breakingTime;
   private int previousBreakProgress = -1;

   public EntityAIBreakDoor(EntityLiving entityinsentient) {
      super(entityinsentient);
   }

   public boolean shouldExecute() {
      if (!super.shouldExecute()) {
         return false;
      } else if (!this.theEntity.world.getGameRules().getBoolean("mobGriefing")) {
         return false;
      } else {
         return !BlockDoor.isOpen(this.theEntity.world, this.doorPosition);
      }
   }

   public void startExecuting() {
      super.startExecuting();
      this.breakingTime = 0;
   }

   public boolean continueExecuting() {
      double d0 = this.theEntity.getDistanceSq(this.doorPosition);
      if (this.breakingTime <= 240 && !BlockDoor.isOpen(this.theEntity.world, this.doorPosition) && d0 < 4.0D) {
         boolean flag = true;
         return flag;
      } else {
         boolean flag = false;
         return flag;
      }
   }

   public void resetTask() {
      super.resetTask();
      this.theEntity.world.sendBlockBreakProgress(this.theEntity.getEntityId(), this.doorPosition, -1);
   }

   public void updateTask() {
      super.updateTask();
      if (this.theEntity.getRNG().nextInt(20) == 0) {
         this.theEntity.world.playEvent(1019, this.doorPosition, 0);
      }

      ++this.breakingTime;
      int i = (int)((float)this.breakingTime / 240.0F * 10.0F);
      if (i != this.previousBreakProgress) {
         this.theEntity.world.sendBlockBreakProgress(this.theEntity.getEntityId(), this.doorPosition, i);
         this.previousBreakProgress = i;
      }

      if (this.breakingTime == 240 && this.theEntity.world.getDifficulty() == EnumDifficulty.HARD) {
         if (CraftEventFactory.callEntityBreakDoorEvent(this.theEntity, this.doorPosition.getX(), this.doorPosition.getY(), this.doorPosition.getZ()).isCancelled()) {
            this.startExecuting();
            return;
         }

         this.theEntity.world.setBlockToAir(this.doorPosition);
         this.theEntity.world.playEvent(1021, this.doorPosition, 0);
         this.theEntity.world.playEvent(2001, this.doorPosition, Block.getIdFromBlock(this.doorBlock));
      }

   }
}
