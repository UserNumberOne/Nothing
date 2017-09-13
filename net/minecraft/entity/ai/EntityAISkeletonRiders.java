package net.minecraft.entity.ai;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntityAISkeletonRiders extends EntityAIBase {
   private final EntityHorse horse;

   public EntityAISkeletonRiders(EntityHorse entityhorse) {
      this.horse = entityhorse;
   }

   public boolean shouldExecute() {
      return this.horse.world.isAnyPlayerWithinRangeAt(this.horse.posX, this.horse.posY, this.horse.posZ, 10.0D);
   }

   public void updateTask() {
      DifficultyInstance difficultydamagescaler = this.horse.world.getDifficultyForLocation(new BlockPos(this.horse));
      this.horse.setSkeletonTrap(false);
      this.horse.setType(HorseType.SKELETON);
      this.horse.setHorseTamed(true);
      this.horse.setGrowingAge(0);
      this.horse.world.addWeatherEffect(new EntityLightningBolt(this.horse.world, this.horse.posX, this.horse.posY, this.horse.posZ, true));
      EntitySkeleton entityskeleton = this.createSkeleton(difficultydamagescaler, this.horse);
      if (entityskeleton != null) {
         entityskeleton.startRiding(this.horse);
      }

      for(int i = 0; i < 3; ++i) {
         EntityHorse entityhorse = this.createHorse(difficultydamagescaler);
         if (entityhorse != null) {
            EntitySkeleton entityskeleton1 = this.createSkeleton(difficultydamagescaler, entityhorse);
            if (entityskeleton1 != null) {
               entityskeleton1.startRiding(entityhorse);
            }

            entityhorse.addVelocity(this.horse.getRNG().nextGaussian() * 0.5D, 0.0D, this.horse.getRNG().nextGaussian() * 0.5D);
         }
      }

   }

   private EntityHorse createHorse(DifficultyInstance difficultydamagescaler) {
      EntityHorse entityhorse = new EntityHorse(this.horse.world);
      entityhorse.onInitialSpawn(difficultydamagescaler, (IEntityLivingData)null);
      entityhorse.setPosition(this.horse.posX, this.horse.posY, this.horse.posZ);
      entityhorse.hurtResistantTime = 60;
      entityhorse.enablePersistence();
      entityhorse.setType(HorseType.SKELETON);
      entityhorse.setHorseTamed(true);
      entityhorse.setGrowingAge(0);
      return !entityhorse.world.addEntity(entityhorse, SpawnReason.TRAP) ? null : entityhorse;
   }

   private EntitySkeleton createSkeleton(DifficultyInstance difficultydamagescaler, EntityHorse entityhorse) {
      EntitySkeleton entityskeleton = new EntitySkeleton(entityhorse.world);
      entityskeleton.onInitialSpawn(difficultydamagescaler, (IEntityLivingData)null);
      entityskeleton.setPosition(entityhorse.posX, entityhorse.posY, entityhorse.posZ);
      entityskeleton.hurtResistantTime = 60;
      entityskeleton.enablePersistence();
      if (entityskeleton.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         entityskeleton.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
      }

      EnchantmentHelper.addRandomEnchantment(entityskeleton.getRNG(), entityskeleton.getHeldItemMainhand(), (int)(5.0F + difficultydamagescaler.getClampedAdditionalDifficulty() * (float)entityskeleton.getRNG().nextInt(18)), false);
      EnchantmentHelper.addRandomEnchantment(entityskeleton.getRNG(), entityskeleton.getItemStackFromSlot(EntityEquipmentSlot.HEAD), (int)(5.0F + difficultydamagescaler.getClampedAdditionalDifficulty() * (float)entityskeleton.getRNG().nextInt(18)), false);
      return !entityskeleton.world.addEntity(entityskeleton, SpawnReason.JOCKEY) ? null : entityskeleton;
   }
}
