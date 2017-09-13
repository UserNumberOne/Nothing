package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityAIBeg extends EntityAIBase {
   private final EntityWolf theWolf;
   private EntityPlayer player;
   private final World world;
   private final float minPlayerDistance;
   private int timeoutCounter;

   public EntityAIBeg(EntityWolf var1, float var2) {
      this.theWolf = wolf;
      this.world = wolf.world;
      this.minPlayerDistance = minDistance;
      this.setMutexBits(2);
   }

   public boolean shouldExecute() {
      this.player = this.world.getClosestPlayerToEntity(this.theWolf, (double)this.minPlayerDistance);
      return this.player == null ? false : this.hasTemptationItemInHand(this.player);
   }

   public boolean continueExecuting() {
      return !this.player.isEntityAlive() ? false : (this.theWolf.getDistanceSqToEntity(this.player) > (double)(this.minPlayerDistance * this.minPlayerDistance) ? false : this.timeoutCounter > 0 && this.hasTemptationItemInHand(this.player));
   }

   public void startExecuting() {
      this.theWolf.setBegging(true);
      this.timeoutCounter = 40 + this.theWolf.getRNG().nextInt(40);
   }

   public void resetTask() {
      this.theWolf.setBegging(false);
      this.player = null;
   }

   public void updateTask() {
      this.theWolf.getLookHelper().setLookPosition(this.player.posX, this.player.posY + (double)this.player.getEyeHeight(), this.player.posZ, 10.0F, (float)this.theWolf.getVerticalFaceSpeed());
      --this.timeoutCounter;
   }

   private boolean hasTemptationItemInHand(EntityPlayer var1) {
      for(EnumHand enumhand : EnumHand.values()) {
         ItemStack itemstack = player.getHeldItem(enumhand);
         if (itemstack != null) {
            if (this.theWolf.isTamed() && itemstack.getItem() == Items.BONE) {
               return true;
            }

            if (this.theWolf.isBreedingItem(itemstack)) {
               return true;
            }
         }
      }

      return false;
   }
}
