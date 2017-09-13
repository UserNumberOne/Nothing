package net.minecraft.entity.ai;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntityAIMate extends EntityAIBase {
   private final EntityAnimal theAnimal;
   World world;
   private EntityAnimal targetMate;
   int spawnBabyDelay;
   double moveSpeed;

   public EntityAIMate(EntityAnimal entityanimal, double d0) {
      this.theAnimal = entityanimal;
      this.world = entityanimal.world;
      this.moveSpeed = d0;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (!this.theAnimal.isInLove()) {
         return false;
      } else {
         this.targetMate = this.getNearbyMate();
         return this.targetMate != null;
      }
   }

   public boolean continueExecuting() {
      return this.targetMate.isEntityAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
   }

   public void resetTask() {
      this.targetMate = null;
      this.spawnBabyDelay = 0;
   }

   public void updateTask() {
      this.theAnimal.getLookHelper().setLookPositionWithEntity(this.targetMate, 10.0F, (float)this.theAnimal.getVerticalFaceSpeed());
      this.theAnimal.getNavigator().tryMoveToEntityLiving(this.targetMate, this.moveSpeed);
      ++this.spawnBabyDelay;
      if (this.spawnBabyDelay >= 60 && this.theAnimal.getDistanceSqToEntity(this.targetMate) < 9.0D) {
         this.spawnBaby();
      }

   }

   private EntityAnimal getNearbyMate() {
      List list = this.world.getEntitiesWithinAABB(this.theAnimal.getClass(), this.theAnimal.getEntityBoundingBox().expandXyz(8.0D));
      double d0 = Double.MAX_VALUE;
      EntityAnimal entityanimal = null;

      for(EntityAnimal entityanimal1 : list) {
         if (this.theAnimal.canMateWith(entityanimal1) && this.theAnimal.getDistanceSqToEntity(entityanimal1) < d0) {
            entityanimal = entityanimal1;
            d0 = this.theAnimal.getDistanceSqToEntity(entityanimal1);
         }
      }

      return entityanimal;
   }

   private void spawnBaby() {
      EntityAgeable entityageable = this.theAnimal.createChild(this.targetMate);
      if (entityageable != null) {
         if (entityageable instanceof EntityTameable && ((EntityTameable)entityageable).isTamed()) {
            entityageable.persistenceRequired = true;
         }

         EntityPlayer entityhuman = this.theAnimal.getPlayerInLove();
         if (entityhuman == null && this.targetMate.getPlayerInLove() != null) {
            entityhuman = this.targetMate.getPlayerInLove();
         }

         int experience = this.theAnimal.getRNG().nextInt(7) + 1;
         EntityBreedEvent entityBreedEvent = CraftEventFactory.callEntityBreedEvent(entityageable, this.theAnimal, this.targetMate, entityhuman, this.theAnimal.breedItem, experience);
         if (entityBreedEvent.isCancelled()) {
            return;
         }

         if (entityhuman != null) {
            entityhuman.addStat(StatList.ANIMALS_BRED);
            if (this.theAnimal instanceof EntityCow) {
               entityhuman.addStat(AchievementList.BREED_COW);
            }
         }

         this.theAnimal.setGrowingAge(6000);
         this.targetMate.setGrowingAge(6000);
         this.theAnimal.resetInLove();
         this.targetMate.resetInLove();
         entityageable.setGrowingAge(-24000);
         entityageable.setLocationAndAngles(this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, 0.0F, 0.0F);
         this.world.addEntity(entityageable, SpawnReason.BREEDING);
         Random random = this.theAnimal.getRNG();

         for(int i = 0; i < 7; ++i) {
            double d0 = random.nextGaussian() * 0.02D;
            double d1 = random.nextGaussian() * 0.02D;
            double d2 = random.nextGaussian() * 0.02D;
            double d3 = random.nextDouble() * (double)this.theAnimal.width * 2.0D - (double)this.theAnimal.width;
            double d4 = 0.5D + random.nextDouble() * (double)this.theAnimal.height;
            double d5 = random.nextDouble() * (double)this.theAnimal.width * 2.0D - (double)this.theAnimal.width;
            this.world.spawnParticle(EnumParticleTypes.HEART, this.theAnimal.posX + d3, this.theAnimal.posY + d4, this.theAnimal.posZ + d5, d0, d1, d2);
         }

         if (this.world.getGameRules().getBoolean("doMobLoot")) {
            this.world.spawnEntity(new EntityXPOrb(this.world, this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, entityBreedEvent.getExperience()));
         }
      }

   }
}
