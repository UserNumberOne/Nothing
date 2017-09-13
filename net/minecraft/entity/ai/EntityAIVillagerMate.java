package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntityAIVillagerMate extends EntityAIBase {
   private final EntityVillager villagerObj;
   private EntityVillager mate;
   private final World world;
   private int matingTimeout;
   Village villageObj;

   public EntityAIVillagerMate(EntityVillager var1) {
      this.villagerObj = var1;
      this.world = var1.world;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (this.villagerObj.getGrowingAge() != 0) {
         return false;
      } else if (this.villagerObj.getRNG().nextInt(500) != 0) {
         return false;
      } else {
         this.villageObj = this.world.getVillageCollection().getNearestVillage(new BlockPos(this.villagerObj), 0);
         if (this.villageObj == null) {
            return false;
         } else if (this.checkSufficientDoorsPresentForNewVillager() && this.villagerObj.getIsWillingToMate(true)) {
            Entity var1 = this.world.findNearestEntityWithinAABB(EntityVillager.class, this.villagerObj.getEntityBoundingBox().expand(8.0D, 3.0D, 8.0D), this.villagerObj);
            if (var1 == null) {
               return false;
            } else {
               this.mate = (EntityVillager)var1;
               return this.mate.getGrowingAge() == 0 && this.mate.getIsWillingToMate(true);
            }
         } else {
            return false;
         }
      }
   }

   public void startExecuting() {
      this.matingTimeout = 300;
      this.villagerObj.setMating(true);
   }

   public void resetTask() {
      this.villageObj = null;
      this.mate = null;
      this.villagerObj.setMating(false);
   }

   public boolean continueExecuting() {
      return this.matingTimeout >= 0 && this.checkSufficientDoorsPresentForNewVillager() && this.villagerObj.getGrowingAge() == 0 && this.villagerObj.getIsWillingToMate(false);
   }

   public void updateTask() {
      --this.matingTimeout;
      this.villagerObj.getLookHelper().setLookPositionWithEntity(this.mate, 10.0F, 30.0F);
      if (this.villagerObj.getDistanceSqToEntity(this.mate) > 2.25D) {
         this.villagerObj.getNavigator().tryMoveToEntityLiving(this.mate, 0.25D);
      } else if (this.matingTimeout == 0 && this.mate.isMating()) {
         this.giveBirth();
      }

      if (this.villagerObj.getRNG().nextInt(35) == 0) {
         this.world.setEntityState(this.villagerObj, (byte)12);
      }

   }

   private boolean checkSufficientDoorsPresentForNewVillager() {
      if (!this.villageObj.isMatingSeason()) {
         return false;
      } else {
         int var1 = (int)((double)((float)this.villageObj.getNumVillageDoors()) * 0.35D);
         return this.villageObj.getNumVillagers() < var1;
      }
   }

   private void giveBirth() {
      EntityVillager var1 = this.villagerObj.createChild(this.mate);
      if (!CraftEventFactory.callEntityBreedEvent(var1, this.villagerObj, this.mate, (EntityLivingBase)null, (ItemStack)null, 0).isCancelled()) {
         this.mate.setGrowingAge(6000);
         this.villagerObj.setGrowingAge(6000);
         this.mate.setIsWillingToMate(false);
         this.villagerObj.setIsWillingToMate(false);
         var1.setGrowingAge(-24000);
         var1.setLocationAndAngles(this.villagerObj.posX, this.villagerObj.posY, this.villagerObj.posZ, 0.0F, 0.0F);
         this.world.addEntity(var1, SpawnReason.BREEDING);
         this.world.setEntityState(var1, (byte)12);
      }
   }
}
