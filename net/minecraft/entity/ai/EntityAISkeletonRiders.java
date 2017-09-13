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

   public EntityAISkeletonRiders(EntityHorse var1) {
      this.horse = var1;
   }

   public boolean shouldExecute() {
      return this.horse.world.isAnyPlayerWithinRangeAt(this.horse.posX, this.horse.posY, this.horse.posZ, 10.0D);
   }

   public void updateTask() {
      DifficultyInstance var1 = this.horse.world.getDifficultyForLocation(new BlockPos(this.horse));
      this.horse.setSkeletonTrap(false);
      this.horse.setType(HorseType.SKELETON);
      this.horse.setHorseTamed(true);
      this.horse.setGrowingAge(0);
      this.horse.world.addWeatherEffect(new EntityLightningBolt(this.horse.world, this.horse.posX, this.horse.posY, this.horse.posZ, true));
      EntitySkeleton var2 = this.createSkeleton(var1, this.horse);
      if (var2 != null) {
         var2.startRiding(this.horse);
      }

      for(int var3 = 0; var3 < 3; ++var3) {
         EntityHorse var4 = this.createHorse(var1);
         if (var4 != null) {
            EntitySkeleton var5 = this.createSkeleton(var1, var4);
            if (var5 != null) {
               var5.startRiding(var4);
            }

            var4.addVelocity(this.horse.getRNG().nextGaussian() * 0.5D, 0.0D, this.horse.getRNG().nextGaussian() * 0.5D);
         }
      }

   }

   private EntityHorse createHorse(DifficultyInstance var1) {
      EntityHorse var2 = new EntityHorse(this.horse.world);
      var2.onInitialSpawn(var1, (IEntityLivingData)null);
      var2.setPosition(this.horse.posX, this.horse.posY, this.horse.posZ);
      var2.hurtResistantTime = 60;
      var2.enablePersistence();
      var2.setType(HorseType.SKELETON);
      var2.setHorseTamed(true);
      var2.setGrowingAge(0);
      return !var2.world.addEntity(var2, SpawnReason.TRAP) ? null : var2;
   }

   private EntitySkeleton createSkeleton(DifficultyInstance var1, EntityHorse var2) {
      EntitySkeleton var3 = new EntitySkeleton(var2.world);
      var3.onInitialSpawn(var1, (IEntityLivingData)null);
      var3.setPosition(var2.posX, var2.posY, var2.posZ);
      var3.hurtResistantTime = 60;
      var3.enablePersistence();
      if (var3.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         var3.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
      }

      EnchantmentHelper.addRandomEnchantment(var3.getRNG(), var3.getHeldItemMainhand(), (int)(5.0F + var1.getClampedAdditionalDifficulty() * (float)var3.getRNG().nextInt(18)), false);
      EnchantmentHelper.addRandomEnchantment(var3.getRNG(), var3.getItemStackFromSlot(EntityEquipmentSlot.HEAD), (int)(5.0F + var1.getClampedAdditionalDifficulty() * (float)var3.getRNG().nextInt(18)), false);
      return !var3.world.addEntity(var3, SpawnReason.JOCKEY) ? null : var3;
   }
}
