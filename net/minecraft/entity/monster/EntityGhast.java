package net.minecraft.entity.monster;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityGhast extends EntityFlying implements IMob {
   private static final DataParameter ATTACKING = EntityDataManager.createKey(EntityGhast.class, DataSerializers.BOOLEAN);
   private int explosionStrength = 1;

   public EntityGhast(World var1) {
      super(var1);
      this.setSize(4.0F, 4.0F);
      this.isImmuneToFire = true;
      this.experienceValue = 5;
      this.moveHelper = new EntityGhast.GhastMoveHelper(this);
   }

   protected void initEntityAI() {
      this.tasks.addTask(5, new EntityGhast.AIRandomFly(this));
      this.tasks.addTask(7, new EntityGhast.AILookAround(this));
      this.tasks.addTask(7, new EntityGhast.AIFireballAttack(this));
      this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
   }

   public void setAttacking(boolean var1) {
      this.dataManager.set(ATTACKING, Boolean.valueOf(var1));
   }

   public int getFireballStrength() {
      return this.explosionStrength;
   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
         this.setDead();
      }

   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if ("fireball".equals(var1.getDamageType()) && var1.getEntity() instanceof EntityPlayer) {
         super.attackEntityFrom(var1, 1000.0F);
         ((EntityPlayer)var1.getEntity()).addStat(AchievementList.GHAST);
         return true;
      } else {
         return super.attackEntityFrom(var1, var2);
      }
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(ATTACKING, Boolean.valueOf(false));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_GHAST_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_GHAST_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_GHAST_DEATH;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_GHAST;
   }

   protected float getSoundVolume() {
      return 10.0F;
   }

   public boolean getCanSpawnHere() {
      return this.rand.nextInt(20) == 0 && super.getCanSpawnHere() && this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
   }

   public int getMaxSpawnedInChunk() {
      return 1;
   }

   public static void registerFixesGhast(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Ghast");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("ExplosionPower", this.explosionStrength);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("ExplosionPower", 99)) {
         this.explosionStrength = var1.getInteger("ExplosionPower");
      }

   }

   public float getEyeHeight() {
      return 2.6F;
   }

   static class AIFireballAttack extends EntityAIBase {
      private final EntityGhast parentEntity;
      public int attackTimer;

      public AIFireballAttack(EntityGhast var1) {
         this.parentEntity = var1;
      }

      public boolean shouldExecute() {
         return this.parentEntity.getAttackTarget() != null;
      }

      public void startExecuting() {
         this.attackTimer = 0;
      }

      public void resetTask() {
         this.parentEntity.setAttacking(false);
      }

      public void updateTask() {
         EntityLivingBase var1 = this.parentEntity.getAttackTarget();
         if (var1.getDistanceSqToEntity(this.parentEntity) < 4096.0D && this.parentEntity.canEntityBeSeen(var1)) {
            World var2 = this.parentEntity.world;
            ++this.attackTimer;
            if (this.attackTimer == 10) {
               var2.playEvent((EntityPlayer)null, 1015, new BlockPos(this.parentEntity), 0);
            }

            if (this.attackTimer == 20) {
               Vec3d var3 = this.parentEntity.getLook(1.0F);
               double var4 = var1.posX - (this.parentEntity.posX + var3.xCoord * 4.0D);
               double var6 = var1.getEntityBoundingBox().minY + (double)(var1.height / 2.0F) - (0.5D + this.parentEntity.posY + (double)(this.parentEntity.height / 2.0F));
               double var8 = var1.posZ - (this.parentEntity.posZ + var3.zCoord * 4.0D);
               var2.playEvent((EntityPlayer)null, 1016, new BlockPos(this.parentEntity), 0);
               EntityLargeFireball var10 = new EntityLargeFireball(var2, this.parentEntity, var4, var6, var8);
               var10.bukkitYield = (float)(var10.explosionPower = this.parentEntity.getFireballStrength());
               var10.posX = this.parentEntity.posX + var3.xCoord * 4.0D;
               var10.posY = this.parentEntity.posY + (double)(this.parentEntity.height / 2.0F) + 0.5D;
               var10.posZ = this.parentEntity.posZ + var3.zCoord * 4.0D;
               var2.spawnEntity(var10);
               this.attackTimer = -40;
            }
         } else if (this.attackTimer > 0) {
            --this.attackTimer;
         }

         this.parentEntity.setAttacking(this.attackTimer > 10);
      }
   }

   static class AILookAround extends EntityAIBase {
      private final EntityGhast parentEntity;

      public AILookAround(EntityGhast var1) {
         this.parentEntity = var1;
         this.setMutexBits(2);
      }

      public boolean shouldExecute() {
         return true;
      }

      public void updateTask() {
         if (this.parentEntity.getAttackTarget() == null) {
            this.parentEntity.rotationYaw = -((float)MathHelper.atan2(this.parentEntity.motionX, this.parentEntity.motionZ)) * 57.295776F;
            this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
         } else {
            EntityLivingBase var1 = this.parentEntity.getAttackTarget();
            if (var1.getDistanceSqToEntity(this.parentEntity) < 4096.0D) {
               double var2 = var1.posX - this.parentEntity.posX;
               double var4 = var1.posZ - this.parentEntity.posZ;
               this.parentEntity.rotationYaw = -((float)MathHelper.atan2(var2, var4)) * 57.295776F;
               this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
            }
         }

      }
   }

   static class AIRandomFly extends EntityAIBase {
      private final EntityGhast parentEntity;

      public AIRandomFly(EntityGhast var1) {
         this.parentEntity = var1;
         this.setMutexBits(1);
      }

      public boolean shouldExecute() {
         EntityMoveHelper var1 = this.parentEntity.getMoveHelper();
         if (!var1.isUpdating()) {
            return true;
         } else {
            double var2 = var1.getX() - this.parentEntity.posX;
            double var4 = var1.getY() - this.parentEntity.posY;
            double var6 = var1.getZ() - this.parentEntity.posZ;
            double var8 = var2 * var2 + var4 * var4 + var6 * var6;
            return var8 < 1.0D || var8 > 3600.0D;
         }
      }

      public boolean continueExecuting() {
         return false;
      }

      public void startExecuting() {
         Random var1 = this.parentEntity.getRNG();
         double var2 = this.parentEntity.posX + (double)((var1.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double var4 = this.parentEntity.posY + (double)((var1.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double var6 = this.parentEntity.posZ + (double)((var1.nextFloat() * 2.0F - 1.0F) * 16.0F);
         this.parentEntity.getMoveHelper().setMoveTo(var2, var4, var6, 1.0D);
      }
   }

   static class GhastMoveHelper extends EntityMoveHelper {
      private final EntityGhast parentEntity;
      private int courseChangeCooldown;

      public GhastMoveHelper(EntityGhast var1) {
         super(var1);
         this.parentEntity = var1;
      }

      public void onUpdateMoveHelper() {
         if (this.action == EntityMoveHelper.Action.MOVE_TO) {
            double var1 = this.posX - this.parentEntity.posX;
            double var3 = this.posY - this.parentEntity.posY;
            double var5 = this.posZ - this.parentEntity.posZ;
            double var7 = var1 * var1 + var3 * var3 + var5 * var5;
            if (this.courseChangeCooldown-- <= 0) {
               this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
               var7 = (double)MathHelper.sqrt(var7);
               if (this.isNotColliding(this.posX, this.posY, this.posZ, var7)) {
                  this.parentEntity.motionX += var1 / var7 * 0.1D;
                  this.parentEntity.motionY += var3 / var7 * 0.1D;
                  this.parentEntity.motionZ += var5 / var7 * 0.1D;
               } else {
                  this.action = EntityMoveHelper.Action.WAIT;
               }
            }
         }

      }

      private boolean isNotColliding(double var1, double var3, double var5, double var7) {
         double var9 = (var1 - this.parentEntity.posX) / var7;
         double var11 = (var3 - this.parentEntity.posY) / var7;
         double var13 = (var5 - this.parentEntity.posZ) / var7;
         AxisAlignedBB var15 = this.parentEntity.getEntityBoundingBox();

         for(int var16 = 1; (double)var16 < var7; ++var16) {
            var15 = var15.offset(var9, var11, var13);
            if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, var15).isEmpty()) {
               return false;
            }
         }

         return true;
      }
   }
}
