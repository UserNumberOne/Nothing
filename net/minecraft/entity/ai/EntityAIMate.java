package net.minecraft.entity.ai;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

public class EntityAIMate extends EntityAIBase {
   private final EntityAnimal theAnimal;
   World world;
   private EntityAnimal targetMate;
   int spawnBabyDelay;
   double moveSpeed;

   public EntityAIMate(EntityAnimal var1, double var2) {
      this.theAnimal = var1;
      this.world = var1.world;
      this.moveSpeed = var2;
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
      List var1 = this.world.getEntitiesWithinAABB(this.theAnimal.getClass(), this.theAnimal.getEntityBoundingBox().expandXyz(8.0D));
      double var2 = Double.MAX_VALUE;
      EntityAnimal var4 = null;

      for(EntityAnimal var6 : var1) {
         if (this.theAnimal.canMateWith(var6) && this.theAnimal.getDistanceSqToEntity(var6) < var2) {
            var4 = var6;
            var2 = this.theAnimal.getDistanceSqToEntity(var6);
         }
      }

      return var4;
   }

   private void spawnBaby() {
      EntityAgeable var1 = this.theAnimal.createChild(this.targetMate);
      BabyEntitySpawnEvent var2 = new BabyEntitySpawnEvent(this.theAnimal, this.targetMate, var1);
      boolean var3 = MinecraftForge.EVENT_BUS.post(var2);
      var1 = var2.getChild();
      if (var3) {
         this.theAnimal.setGrowingAge(6000);
         this.targetMate.setGrowingAge(6000);
         this.theAnimal.resetInLove();
         this.targetMate.resetInLove();
      } else {
         if (var1 != null) {
            EntityPlayer var4 = this.theAnimal.getPlayerInLove();
            if (var4 == null && this.targetMate.getPlayerInLove() != null) {
               var4 = this.targetMate.getPlayerInLove();
            }

            if (var4 != null) {
               var4.addStat(StatList.ANIMALS_BRED);
               if (this.theAnimal instanceof EntityCow) {
                  var4.addStat(AchievementList.BREED_COW);
               }
            }

            this.theAnimal.setGrowingAge(6000);
            this.targetMate.setGrowingAge(6000);
            this.theAnimal.resetInLove();
            this.targetMate.resetInLove();
            var1.setGrowingAge(-24000);
            var1.setLocationAndAngles(this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, 0.0F, 0.0F);
            this.world.spawnEntity(var1);
            Random var5 = this.theAnimal.getRNG();

            for(int var6 = 0; var6 < 7; ++var6) {
               double var7 = var5.nextGaussian() * 0.02D;
               double var9 = var5.nextGaussian() * 0.02D;
               double var11 = var5.nextGaussian() * 0.02D;
               double var13 = var5.nextDouble() * (double)this.theAnimal.width * 2.0D - (double)this.theAnimal.width;
               double var15 = 0.5D + var5.nextDouble() * (double)this.theAnimal.height;
               double var17 = var5.nextDouble() * (double)this.theAnimal.width * 2.0D - (double)this.theAnimal.width;
               this.world.spawnParticle(EnumParticleTypes.HEART, this.theAnimal.posX + var13, this.theAnimal.posY + var15, this.theAnimal.posZ + var17, var7, var9, var11);
            }

            if (this.world.getGameRules().getBoolean("doMobLoot")) {
               this.world.spawnEntity(new EntityXPOrb(this.world, this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, var5.nextInt(7) + 1));
            }
         }

      }
   }
}
