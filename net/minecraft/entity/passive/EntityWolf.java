package net.minecraft.entity.passive;

import com.google.common.base.Predicate;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBeg;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
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

public class EntityWolf extends EntityTameable {
   private static final DataParameter DATA_HEALTH_ID = EntityDataManager.createKey(EntityWolf.class, DataSerializers.FLOAT);
   private static final DataParameter BEGGING = EntityDataManager.createKey(EntityWolf.class, DataSerializers.BOOLEAN);
   private static final DataParameter COLLAR_COLOR = EntityDataManager.createKey(EntityWolf.class, DataSerializers.VARINT);
   private float headRotationCourse;
   private float headRotationCourseOld;
   private boolean isWet;
   private boolean isShaking;
   private float timeWolfIsShaking;
   private float prevTimeWolfIsShaking;

   public EntityWolf(World var1) {
      super(var1);
      this.setSize(0.6F, 0.85F);
      this.setTamed(false);
   }

   protected void initEntityAI() {
      this.aiSit = new EntityAISit(this);
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, this.aiSit);
      this.tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
      this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, true));
      this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
      this.tasks.addTask(6, new EntityAIMate(this, 1.0D));
      this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(8, new EntityAIBeg(this, 8.0F));
      this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(9, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
      this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
      this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
      this.targetTasks.addTask(4, new EntityAITargetNonTamed(this, EntityAnimal.class, false, new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            return var1 instanceof EntitySheep || var1 instanceof EntityRabbit;
         }
      }));
      this.targetTasks.addTask(5, new EntityAINearestAttackableTarget(this, EntitySkeleton.class, false));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
      if (this.isTamed()) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
      } else {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
      }

      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
   }

   public void setAttackTarget(@Nullable EntityLivingBase var1) {
      super.setAttackTarget(var1);
      if (var1 == null) {
         this.setAngry(false);
      } else if (!this.isTamed()) {
         this.setAngry(true);
      }

   }

   protected void updateAITasks() {
      this.dataManager.set(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
      this.dataManager.register(BEGGING, Boolean.valueOf(false));
      this.dataManager.register(COLLAR_COLOR, Integer.valueOf(EnumDyeColor.RED.getDyeDamage()));
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15F, 1.0F);
   }

   public static void registerFixesWolf(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Wolf");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setBoolean("Angry", this.isAngry());
      var1.setByte("CollarColor", (byte)this.getCollarColor().getDyeDamage());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setAngry(var1.getBoolean("Angry"));
      if (var1.hasKey("CollarColor", 99)) {
         this.setCollarColor(EnumDyeColor.byDyeDamage(var1.getByte("CollarColor")));
      }

   }

   protected SoundEvent getAmbientSound() {
      return this.isAngry() ? SoundEvents.ENTITY_WOLF_GROWL : (this.rand.nextInt(3) == 0 ? (this.isTamed() && ((Float)this.dataManager.get(DATA_HEALTH_ID)).floatValue() < 10.0F ? SoundEvents.ENTITY_WOLF_WHINE : SoundEvents.ENTITY_WOLF_PANT) : SoundEvents.ENTITY_WOLF_AMBIENT);
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_WOLF_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_WOLF_DEATH;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_WOLF;
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (!this.world.isRemote && this.isWet && !this.isShaking && !this.hasPath() && this.onGround) {
         this.isShaking = true;
         this.timeWolfIsShaking = 0.0F;
         this.prevTimeWolfIsShaking = 0.0F;
         this.world.setEntityState(this, (byte)8);
      }

      if (!this.world.isRemote && this.getAttackTarget() == null && this.isAngry()) {
         this.setAngry(false);
      }

   }

   public void onUpdate() {
      super.onUpdate();
      this.headRotationCourseOld = this.headRotationCourse;
      if (this.isBegging()) {
         this.headRotationCourse += (1.0F - this.headRotationCourse) * 0.4F;
      } else {
         this.headRotationCourse += (0.0F - this.headRotationCourse) * 0.4F;
      }

      if (this.isWet()) {
         this.isWet = true;
         this.isShaking = false;
         this.timeWolfIsShaking = 0.0F;
         this.prevTimeWolfIsShaking = 0.0F;
      } else if ((this.isWet || this.isShaking) && this.isShaking) {
         if (this.timeWolfIsShaking == 0.0F) {
            this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         }

         this.prevTimeWolfIsShaking = this.timeWolfIsShaking;
         this.timeWolfIsShaking += 0.05F;
         if (this.prevTimeWolfIsShaking >= 2.0F) {
            this.isWet = false;
            this.isShaking = false;
            this.prevTimeWolfIsShaking = 0.0F;
            this.timeWolfIsShaking = 0.0F;
         }

         if (this.timeWolfIsShaking > 0.4F) {
            float var1 = (float)this.getEntityBoundingBox().minY;
            int var2 = (int)(MathHelper.sin((this.timeWolfIsShaking - 0.4F) * 3.1415927F) * 7.0F);

            for(int var3 = 0; var3 < var2; ++var3) {
               float var4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
               float var5 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)var4, (double)(var1 + 0.8F), this.posZ + (double)var5, this.motionX, this.motionY, this.motionZ);
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean isWolfWet() {
      return this.isWet;
   }

   @SideOnly(Side.CLIENT)
   public float getShadingWhileWet(float var1) {
      return 0.75F + (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * var1) / 2.0F * 0.25F;
   }

   @SideOnly(Side.CLIENT)
   public float getShakeAngle(float var1, float var2) {
      float var3 = (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * var1 + var2) / 1.8F;
      if (var3 < 0.0F) {
         var3 = 0.0F;
      } else if (var3 > 1.0F) {
         var3 = 1.0F;
      }

      return MathHelper.sin(var3 * 3.1415927F) * MathHelper.sin(var3 * 3.1415927F * 11.0F) * 0.15F * 3.1415927F;
   }

   @SideOnly(Side.CLIENT)
   public float getInterestedAngle(float var1) {
      return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * var1) * 0.15F * 3.1415927F;
   }

   public float getEyeHeight() {
      return this.height * 0.8F;
   }

   public int getVerticalFaceSpeed() {
      return this.isSitting() ? 20 : super.getVerticalFaceSpeed();
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         Entity var3 = var1.getEntity();
         if (this.aiSit != null) {
            this.aiSit.setSitting(false);
         }

         if (var3 != null && !(var3 instanceof EntityPlayer) && !(var3 instanceof EntityArrow)) {
            var2 = (var2 + 1.0F) / 2.0F;
         }

         return super.attackEntityFrom(var1, var2);
      }
   }

   public boolean attackEntityAsMob(Entity var1) {
      boolean var2 = var1.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
      if (var2) {
         this.applyEnchantments(this, var1);
      }

      return var2;
   }

   public void setTamed(boolean var1) {
      super.setTamed(var1);
      if (var1) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
      } else {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
      }

      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (this.isTamed()) {
         if (var3 != null) {
            if (var3.getItem() instanceof ItemFood) {
               ItemFood var4 = (ItemFood)var3.getItem();
               if (var4.isWolfsFavoriteMeat() && ((Float)this.dataManager.get(DATA_HEALTH_ID)).floatValue() < 20.0F) {
                  if (!var1.capabilities.isCreativeMode) {
                     --var3.stackSize;
                  }

                  this.heal((float)var4.getHealAmount(var3));
                  return true;
               }
            } else if (var3.getItem() == Items.DYE) {
               EnumDyeColor var5 = EnumDyeColor.byDyeDamage(var3.getMetadata());
               if (var5 != this.getCollarColor()) {
                  this.setCollarColor(var5);
                  if (!var1.capabilities.isCreativeMode) {
                     --var3.stackSize;
                  }

                  return true;
               }
            }
         }

         if (this.isOwner(var1) && !this.world.isRemote && !this.isBreedingItem(var3)) {
            this.aiSit.setSitting(!this.isSitting());
            this.isJumping = false;
            this.navigator.clearPathEntity();
            this.setAttackTarget((EntityLivingBase)null);
         }
      } else if (var3 != null && var3.getItem() == Items.BONE && !this.isAngry()) {
         if (!var1.capabilities.isCreativeMode) {
            --var3.stackSize;
         }

         if (!this.world.isRemote) {
            if (this.rand.nextInt(3) == 0) {
               this.setTamed(true);
               this.navigator.clearPathEntity();
               this.setAttackTarget((EntityLivingBase)null);
               this.aiSit.setSitting(true);
               this.setHealth(20.0F);
               this.setOwnerId(var1.getUniqueID());
               this.playTameEffect(true);
               this.world.setEntityState(this, (byte)7);
            } else {
               this.playTameEffect(false);
               this.world.setEntityState(this, (byte)6);
            }
         }

         return true;
      }

      return super.processInteract(var1, var2, var3);
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 8) {
         this.isShaking = true;
         this.timeWolfIsShaking = 0.0F;
         this.prevTimeWolfIsShaking = 0.0F;
      } else {
         super.handleStatusUpdate(var1);
      }

   }

   @SideOnly(Side.CLIENT)
   public float getTailRotation() {
      return this.isAngry() ? 1.5393804F : (this.isTamed() ? (0.55F - (this.getMaxHealth() - ((Float)this.dataManager.get(DATA_HEALTH_ID)).floatValue()) * 0.02F) * 3.1415927F : 0.62831855F);
   }

   public boolean isBreedingItem(@Nullable ItemStack var1) {
      return var1 == null ? false : (!(var1.getItem() instanceof ItemFood) ? false : ((ItemFood)var1.getItem()).isWolfsFavoriteMeat());
   }

   public int getMaxSpawnedInChunk() {
      return 8;
   }

   public boolean isAngry() {
      return (((Byte)this.dataManager.get(TAMED)).byteValue() & 2) != 0;
   }

   public void setAngry(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(TAMED)).byteValue();
      if (var1) {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(var2 | 2)));
      } else {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(var2 & -3)));
      }

   }

   public EnumDyeColor getCollarColor() {
      return EnumDyeColor.byDyeDamage(((Integer)this.dataManager.get(COLLAR_COLOR)).intValue() & 15);
   }

   public void setCollarColor(EnumDyeColor var1) {
      this.dataManager.set(COLLAR_COLOR, Integer.valueOf(var1.getDyeDamage()));
   }

   public EntityWolf createChild(EntityAgeable var1) {
      EntityWolf var2 = new EntityWolf(this.world);
      UUID var3 = this.getOwnerId();
      if (var3 != null) {
         var2.setOwnerId(var3);
         var2.setTamed(true);
      }

      return var2;
   }

   public void setBegging(boolean var1) {
      this.dataManager.set(BEGGING, Boolean.valueOf(var1));
   }

   public boolean canMateWith(EntityAnimal var1) {
      if (var1 == this) {
         return false;
      } else if (!this.isTamed()) {
         return false;
      } else if (!(var1 instanceof EntityWolf)) {
         return false;
      } else {
         EntityWolf var2 = (EntityWolf)var1;
         return !var2.isTamed() ? false : (var2.isSitting() ? false : this.isInLove() && var2.isInLove());
      }
   }

   public boolean isBegging() {
      return ((Boolean)this.dataManager.get(BEGGING)).booleanValue();
   }

   public boolean shouldAttackEntity(EntityLivingBase var1, EntityLivingBase var2) {
      if (!(var1 instanceof EntityCreeper) && !(var1 instanceof EntityGhast)) {
         if (var1 instanceof EntityWolf) {
            EntityWolf var3 = (EntityWolf)var1;
            if (var3.isTamed() && var3.getOwner() == var2) {
               return false;
            }
         }

         return var1 instanceof EntityPlayer && var2 instanceof EntityPlayer && !((EntityPlayer)var2).canAttackPlayer((EntityPlayer)var1) ? false : !(var1 instanceof EntityHorse) || !((EntityHorse)var1).isTame();
      } else {
         return false;
      }
   }

   public boolean canBeLeashedTo(EntityPlayer var1) {
      return !this.isAngry() && super.canBeLeashedTo(var1);
   }
}
