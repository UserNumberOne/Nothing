package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityPolarBear extends EntityAnimal {
   private static final DataParameter IS_STANDING = EntityDataManager.createKey(EntityPolarBear.class, DataSerializers.BOOLEAN);
   private float clientSideStandAnimation0;
   private float clientSideStandAnimation;
   private int warningSoundTicks;

   public EntityPolarBear(World var1) {
      super(var1);
      this.setSize(1.3F, 1.4F);
   }

   public EntityAgeable createChild(EntityAgeable var1) {
      return new EntityPolarBear(this.world);
   }

   public boolean isBreedingItem(ItemStack var1) {
      return false;
   }

   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new EntityPolarBear.AIMeleeAttack());
      this.tasks.addTask(1, new EntityPolarBear.AIPanic());
      this.tasks.addTask(4, new EntityAIFollowParent(this, 1.25D));
      this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(7, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityPolarBear.AIHurtByTarget());
      this.targetTasks.addTask(2, new EntityPolarBear.AIAttackPlayer());
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
   }

   protected SoundEvent getAmbientSound() {
      return this.isChild() ? SoundEvents.ENTITY_POLAR_BEAR_BABY_AMBIENT : SoundEvents.ENTITY_POLAR_BEAR_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_POLAR_BEAR_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_POLAR_BEAR_DEATH;
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.15F, 1.0F);
   }

   protected void playWarningSound() {
      if (this.warningSoundTicks <= 0) {
         this.playSound(SoundEvents.ENTITY_POLAR_BEAR_WARNING, 1.0F, 1.0F);
         this.warningSoundTicks = 40;
      }

   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_POLAR_BEAR;
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(IS_STANDING, Boolean.valueOf(false));
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         this.clientSideStandAnimation0 = this.clientSideStandAnimation;
         if (this.isStanding()) {
            this.clientSideStandAnimation = MathHelper.clamp(this.clientSideStandAnimation + 1.0F, 0.0F, 6.0F);
         } else {
            this.clientSideStandAnimation = MathHelper.clamp(this.clientSideStandAnimation - 1.0F, 0.0F, 6.0F);
         }
      }

      if (this.warningSoundTicks > 0) {
         --this.warningSoundTicks;
      }

   }

   public boolean attackEntityAsMob(Entity var1) {
      boolean var2 = var1.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
      if (var2) {
         this.applyEnchantments(this, var1);
      }

      return var2;
   }

   public boolean isStanding() {
      return ((Boolean)this.dataManager.get(IS_STANDING)).booleanValue();
   }

   public void setStanding(boolean var1) {
      this.dataManager.set(IS_STANDING, Boolean.valueOf(var1));
   }

   @SideOnly(Side.CLIENT)
   public float getStandingAnimationScale(float var1) {
      return (this.clientSideStandAnimation0 + (this.clientSideStandAnimation - this.clientSideStandAnimation0) * var1) / 6.0F;
   }

   protected float getWaterSlowDown() {
      return 0.98F;
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, IEntityLivingData var2) {
      if (var2 instanceof EntityPolarBear.GroupData) {
         if (((EntityPolarBear.GroupData)var2).madeParent) {
            this.setGrowingAge(-24000);
         }
      } else {
         EntityPolarBear.GroupData var3 = new EntityPolarBear.GroupData();
         var3.madeParent = true;
         var2 = var3;
      }

      return (IEntityLivingData)var2;
   }

   class AIAttackPlayer extends EntityAINearestAttackableTarget {
      public AIAttackPlayer() {
         super(EntityPolarBear.this, EntityPlayer.class, 20, true, true, (Predicate)null);
      }

      public boolean shouldExecute() {
         if (EntityPolarBear.this.isChild()) {
            return false;
         } else {
            if (super.shouldExecute()) {
               for(EntityPolarBear var2 : EntityPolarBear.this.world.getEntitiesWithinAABB(EntityPolarBear.class, EntityPolarBear.this.getEntityBoundingBox().expand(8.0D, 4.0D, 8.0D))) {
                  if (var2.isChild()) {
                     return true;
                  }
               }
            }

            EntityPolarBear.this.setAttackTarget((EntityLivingBase)null);
            return false;
         }
      }

      protected double getTargetDistance() {
         return super.getTargetDistance() * 0.5D;
      }
   }

   class AIHurtByTarget extends EntityAIHurtByTarget {
      public AIHurtByTarget() {
         super(EntityPolarBear.this, false);
      }

      public void startExecuting() {
         super.startExecuting();
         if (EntityPolarBear.this.isChild()) {
            this.alertOthers();
            this.resetTask();
         }

      }

      protected void setEntityAttackTarget(EntityCreature var1, EntityLivingBase var2) {
         if (var1 instanceof EntityPolarBear && !((EntityPolarBear)var1).isChild()) {
            super.setEntityAttackTarget(var1, var2);
         }

      }
   }

   class AIMeleeAttack extends EntityAIAttackMelee {
      public AIMeleeAttack() {
         super(EntityPolarBear.this, 1.25D, true);
      }

      protected void checkAndPerformAttack(EntityLivingBase var1, double var2) {
         double var4 = this.getAttackReachSqr(var1);
         if (var2 <= var4 && this.attackTick <= 0) {
            this.attackTick = 20;
            this.attacker.attackEntityAsMob(var1);
            EntityPolarBear.this.setStanding(false);
         } else if (var2 <= var4 * 2.0D) {
            if (this.attackTick <= 0) {
               EntityPolarBear.this.setStanding(false);
               this.attackTick = 20;
            }

            if (this.attackTick <= 10) {
               EntityPolarBear.this.setStanding(true);
               EntityPolarBear.this.playWarningSound();
            }
         } else {
            this.attackTick = 20;
            EntityPolarBear.this.setStanding(false);
         }

      }

      public void resetTask() {
         EntityPolarBear.this.setStanding(false);
         super.resetTask();
      }

      protected double getAttackReachSqr(EntityLivingBase var1) {
         return (double)(4.0F + var1.width);
      }
   }

   class AIPanic extends EntityAIPanic {
      public AIPanic() {
         super(EntityPolarBear.this, 2.0D);
      }

      public boolean shouldExecute() {
         return !EntityPolarBear.this.isChild() && !EntityPolarBear.this.isBurning() ? false : super.shouldExecute();
      }
   }

   static class GroupData implements IEntityLivingData {
      public boolean madeParent;

      private GroupData() {
      }
   }
}
