package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.EnumDifficulty;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityAIBreakDoor extends EntityAIDoorInteract {
   private int breakingTime;
   private int previousBreakProgress = -1;

   public EntityAIBreakDoor(EntityLiving var1) {
      super(var1);
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
      double var1 = this.theEntity.getDistanceSq(this.doorPosition);
      if (this.breakingTime <= 240 && !BlockDoor.isOpen(this.theEntity.world, this.doorPosition) && var1 < 4.0D) {
         boolean var4 = true;
         return var4;
      } else {
         boolean var3 = false;
         return var3;
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
      int var1 = (int)((float)this.breakingTime / 240.0F * 10.0F);
      if (var1 != this.previousBreakProgress) {
         this.theEntity.world.sendBlockBreakProgress(this.theEntity.getEntityId(), this.doorPosition, var1);
         this.previousBreakProgress = var1;
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
