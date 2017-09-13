package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBlaze extends EntityMob {
   private float heightOffset = 0.5F;
   private int heightOffsetUpdateTime;
   private static final DataParameter ON_FIRE = EntityDataManager.createKey(EntityBlaze.class, DataSerializers.BYTE);

   public EntityBlaze(World var1) {
      super(var1);
      this.setPathPriority(PathNodeType.WATER, -1.0F);
      this.setPathPriority(PathNodeType.LAVA, 8.0F);
      this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
      this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
      this.isImmuneToFire = true;
      this.experienceValue = 10;
   }

   public static void registerFixesBlaze(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Blaze");
   }

   protected void initEntityAI() {
      this.tasks.addTask(4, new EntityBlaze.AIFireballAttack(this));
      this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
      this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(ON_FIRE, Byte.valueOf((byte)0));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_BLAZE_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_BLAZE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_BLAZE_DEATH;
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float var1) {
      return 15728880;
   }

   public float getBrightness(float var1) {
      return 1.0F;
   }

   public void onLivingUpdate() {
      if (!this.onGround && this.motionY < 0.0D) {
         this.motionY *= 0.6D;
      }

      if (this.world.isRemote) {
         if (this.rand.nextInt(24) == 0 && !this.isSilent()) {
            this.world.playSound(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, SoundEvents.ENTITY_BLAZE_BURN, this.getSoundCategory(), 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F, false);
         }

         for(int var1 = 0; var1 < 2; ++var1) {
            this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0D, 0.0D, 0.0D);
         }
      }

      super.onLivingUpdate();
   }

   protected void updateAITasks() {
      if (this.isWet()) {
         this.attackEntityFrom(DamageSource.drown, 1.0F);
      }

      --this.heightOffsetUpdateTime;
      if (this.heightOffsetUpdateTime <= 0) {
         this.heightOffsetUpdateTime = 100;
         this.heightOffset = 0.5F + (float)this.rand.nextGaussian() * 3.0F;
      }

      EntityLivingBase var1 = this.getAttackTarget();
      if (var1 != null && var1.posY + (double)var1.getEyeHeight() > this.posY + (double)this.getEyeHeight() + (double)this.heightOffset) {
         this.motionY += (0.30000001192092896D - this.motionY) * 0.30000001192092896D;
         this.isAirBorne = true;
      }

      super.updateAITasks();
   }

   public void fall(float var1, float var2) {
   }

   public boolean isBurning() {
      return this.isCharged();
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_BLAZE;
   }

   public boolean isCharged() {
      return (((Byte)this.dataManager.get(ON_FIRE)).byteValue() & 1) != 0;
   }

   public void setOnFire(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(ON_FIRE)).byteValue();
      if (var1) {
         var2 = (byte)(var2 | 1);
      } else {
         var2 = (byte)(var2 & -2);
      }

      this.dataManager.set(ON_FIRE, Byte.valueOf(var2));
   }

   protected boolean isValidLightLevel() {
      return true;
   }

   static class AIFireballAttack extends EntityAIBase {
      private final EntityBlaze blaze;
      private int attackStep;
      private int attackTime;

      public AIFireballAttack(EntityBlaze var1) {
         this.blaze = var1;
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase var1 = this.blaze.getAttackTarget();
         return var1 != null && var1.isEntityAlive();
      }

      public void startExecuting() {
         this.attackStep = 0;
      }

      public void resetTask() {
         this.blaze.setOnFire(false);
      }

      public void updateTask() {
         --this.attackTime;
         EntityLivingBase var1 = this.blaze.getAttackTarget();
         double var2 = this.blaze.getDistanceSqToEntity(var1);
         if (var2 < 4.0D) {
            if (this.attackTime <= 0) {
               this.attackTime = 20;
               this.blaze.attackEntityAsMob(var1);
            }

            this.blaze.getMoveHelper().setMoveTo(var1.posX, var1.posY, var1.posZ, 1.0D);
         } else if (var2 < 256.0D) {
            double var4 = var1.posX - this.blaze.posX;
            double var6 = var1.getEntityBoundingBox().minY + (double)(var1.height / 2.0F) - (this.blaze.posY + (double)(this.blaze.height / 2.0F));
            double var8 = var1.posZ - this.blaze.posZ;
            if (this.attackTime <= 0) {
               ++this.attackStep;
               if (this.attackStep == 1) {
                  this.attackTime = 60;
                  this.blaze.setOnFire(true);
               } else if (this.attackStep <= 4) {
                  this.attackTime = 6;
               } else {
                  this.attackTime = 100;
                  this.attackStep = 0;
                  this.blaze.setOnFire(false);
               }

               if (this.attackStep > 1) {
                  float var10 = MathHelper.sqrt(MathHelper.sqrt(var2)) * 0.5F;
                  this.blaze.world.playEvent((EntityPlayer)null, 1018, new BlockPos((int)this.blaze.posX, (int)this.blaze.posY, (int)this.blaze.posZ), 0);

                  for(int var11 = 0; var11 < 1; ++var11) {
                     EntitySmallFireball var12 = new EntitySmallFireball(this.blaze.world, this.blaze, var4 + this.blaze.getRNG().nextGaussian() * (double)var10, var6, var8 + this.blaze.getRNG().nextGaussian() * (double)var10);
                     var12.posY = this.blaze.posY + (double)(this.blaze.height / 2.0F) + 0.5D;
                     this.blaze.world.spawnEntity(var12);
                  }
               }
            }

            this.blaze.getLookHelper().setLookPositionWithEntity(var1, 10.0F, 10.0F);
         } else {
            this.blaze.getNavigator().clearPathEntity();
            this.blaze.getMoveHelper().setMoveTo(var1.posX, var1.posY, var1.posZ, 1.0D);
         }

         super.updateTask();
      }
   }
}
