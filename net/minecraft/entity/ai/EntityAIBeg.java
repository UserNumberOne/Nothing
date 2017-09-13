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
      this.theWolf = var1;
      this.world = var1.world;
      this.minPlayerDistance = var2;
      this.setMutexBits(2);
   }

   public boolean shouldExecute() {
      this.player = this.world.getClosestPlayerToEntity(this.theWolf, (double)this.minPlayerDistance);
      return this.player == null ? false : this.hasTemptationItemInHand(this.player);
   }

   public boolean continueExecuting() {
      if (!this.player.isEntityAlive()) {
         return false;
      } else if (this.theWolf.getDistanceSqToEntity(this.player) > (double)(this.minPlayerDistance * this.minPlayerDistance)) {
         return false;
      } else {
         return this.timeoutCounter > 0 && this.hasTemptationItemInHand(this.player);
      }
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
      for(EnumHand var5 : EnumHand.values()) {
         ItemStack var6 = var1.getHeldItem(var5);
         if (var6 != null) {
            if (this.theWolf.isTamed() && var6.getItem() == Items.BONE) {
               return true;
            }

            if (this.theWolf.isBreedingItem(var6)) {
               return true;
            }
         }
      }

      return false;
   }
}
