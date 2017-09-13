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
      super(worldIn);
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
            return p_apply_1_ instanceof EntitySheep || p_apply_1_ instanceof EntityRabbit;
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
      super.setAttackTarget(entitylivingbaseIn);
      if (entitylivingbaseIn == null) {
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
      EntityLiving.registerFixesMob(fixer, "Wolf");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setBoolean("Angry", this.isAngry());
      compound.setByte("CollarColor", (byte)this.getCollarColor().getDyeDamage());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      this.setAngry(compound.getBoolean("Angry"));
      if (compound.hasKey("CollarColor", 99)) {
         this.setCollarColor(EnumDyeColor.byDyeDamage(compound.getByte("CollarColor")));
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
            float f = (float)this.getEntityBoundingBox().minY;
            int i = (int)(MathHelper.sin((this.timeWolfIsShaking - 0.4F) * 3.1415927F) * 7.0F);

            for(int j = 0; j < i; ++j) {
               float f1 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
               float f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)f1, (double)(f + 0.8F), this.posZ + (double)f2, this.motionX, this.motionY, this.motionZ);
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
      return 0.75F + (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * p_70915_1_) / 2.0F * 0.25F;
   }

   @SideOnly(Side.CLIENT)
   public float getShakeAngle(float var1, float var2) {
      float f = (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * p_70923_1_ + p_70923_2_) / 1.8F;
      if (f < 0.0F) {
         f = 0.0F;
      } else if (f > 1.0F) {
         f = 1.0F;
      }

      return MathHelper.sin(f * 3.1415927F) * MathHelper.sin(f * 3.1415927F * 11.0F) * 0.15F * 3.1415927F;
   }

   @SideOnly(Side.CLIENT)
   public float getInterestedAngle(float var1) {
      return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * p_70917_1_) * 0.15F * 3.1415927F;
   }

   public float getEyeHeight() {
      return this.height * 0.8F;
   }

   public int getVerticalFaceSpeed() {
      return this.isSitting() ? 20 : super.getVerticalFaceSpeed();
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(source)) {
         return false;
      } else {
         Entity entity = source.getEntity();
         if (this.aiSit != null) {
            this.aiSit.setSitting(false);
         }

         if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
            amount = (amount + 1.0F) / 2.0F;
         }

         return super.attackEntityFrom(source, amount);
      }
   }

   public boolean attackEntityAsMob(Entity var1) {
      boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
      if (flag) {
         this.applyEnchantments(this, entityIn);
      }

      return flag;
   }

   public void setTamed(boolean var1) {
      super.setTamed(tamed);
      if (tamed) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
      } else {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
      }

      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (this.isTamed()) {
         if (stack != null) {
            if (stack.getItem() instanceof ItemFood) {
               ItemFood itemfood = (ItemFood)stack.getItem();
               if (itemfood.isWolfsFavoriteMeat() && ((Float)this.dataManager.get(DATA_HEALTH_ID)).floatValue() < 20.0F) {
                  if (!player.capabilities.isCreativeMode) {
                     --stack.stackSize;
                  }

                  this.heal((float)itemfood.getHealAmount(stack));
                  return true;
               }
            } else if (stack.getItem() == Items.DYE) {
               EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(stack.getMetadata());
               if (enumdyecolor != this.getCollarColor()) {
                  this.setCollarColor(enumdyecolor);
                  if (!player.capabilities.isCreativeMode) {
                     --stack.stackSize;
                  }

                  return true;
               }
            }
         }

         if (this.isOwner(player) && !this.world.isRemote && !this.isBreedingItem(stack)) {
            this.aiSit.setSitting(!this.isSitting());
            this.isJumping = false;
            this.navigator.clearPathEntity();
            this.setAttackTarget((EntityLivingBase)null);
         }
      } else if (stack != null && stack.getItem() == Items.BONE && !this.isAngry()) {
         if (!player.capabilities.isCreativeMode) {
            --stack.stackSize;
         }

         if (!this.world.isRemote) {
            if (this.rand.nextInt(3) == 0) {
               this.setTamed(true);
               this.navigator.clearPathEntity();
               this.setAttackTarget((EntityLivingBase)null);
               this.aiSit.setSitting(true);
               this.setHealth(20.0F);
               this.setOwnerId(player.getUniqueID());
               this.playTameEffect(true);
               this.world.setEntityState(this, (byte)7);
            } else {
               this.playTameEffect(false);
               this.world.setEntityState(this, (byte)6);
            }
         }

         return true;
      }

      return super.processInteract(player, hand, stack);
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (id == 8) {
         this.isShaking = true;
         this.timeWolfIsShaking = 0.0F;
         this.prevTimeWolfIsShaking = 0.0F;
      } else {
         super.handleStatusUpdate(id);
      }

   }

   @SideOnly(Side.CLIENT)
   public float getTailRotation() {
      return this.isAngry() ? 1.5393804F : (this.isTamed() ? (0.55F - (this.getMaxHealth() - ((Float)this.dataManager.get(DATA_HEALTH_ID)).floatValue()) * 0.02F) * 3.1415927F : 0.62831855F);
   }

   public boolean isBreedingItem(@Nullable ItemStack var1) {
      return stack == null ? false : (!(stack.getItem() instanceof ItemFood) ? false : ((ItemFood)stack.getItem()).isWolfsFavoriteMeat());
   }

   public int getMaxSpawnedInChunk() {
      return 8;
   }

   public boolean isAngry() {
      return (((Byte)this.dataManager.get(TAMED)).byteValue() & 2) != 0;
   }

   public void setAngry(boolean var1) {
      byte b0 = ((Byte)this.dataManager.get(TAMED)).byteValue();
      if (angry) {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(b0 | 2)));
      } else {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(b0 & -3)));
      }

   }

   public EnumDyeColor getCollarColor() {
      return EnumDyeColor.byDyeDamage(((Integer)this.dataManager.get(COLLAR_COLOR)).intValue() & 15);
   }

   public void setCollarColor(EnumDyeColor var1) {
      this.dataManager.set(COLLAR_COLOR, Integer.valueOf(collarcolor.getDyeDamage()));
   }

   public EntityWolf createChild(EntityAgeable var1) {
      EntityWolf entitywolf = new EntityWolf(this.world);
      UUID uuid = this.getOwnerId();
      if (uuid != null) {
         entitywolf.setOwnerId(uuid);
         entitywolf.setTamed(true);
      }

      return entitywolf;
   }

   public void setBegging(boolean var1) {
      this.dataManager.set(BEGGING, Boolean.valueOf(beg));
   }

   public boolean canMateWith(EntityAnimal var1) {
      if (otherAnimal == this) {
         return false;
      } else if (!this.isTamed()) {
         return false;
      } else if (!(otherAnimal instanceof EntityWolf)) {
         return false;
      } else {
         EntityWolf entitywolf = (EntityWolf)otherAnimal;
         return !entitywolf.isTamed() ? false : (entitywolf.isSitting() ? false : this.isInLove() && entitywolf.isInLove());
      }
   }

   public boolean isBegging() {
      return ((Boolean)this.dataManager.get(BEGGING)).booleanValue();
   }

   public boolean shouldAttackEntity(EntityLivingBase var1, EntityLivingBase var2) {
      if (!(p_142018_1_ instanceof EntityCreeper) && !(p_142018_1_ instanceof EntityGhast)) {
         if (p_142018_1_ instanceof EntityWolf) {
            EntityWolf entitywolf = (EntityWolf)p_142018_1_;
            if (entitywolf.isTamed() && entitywolf.getOwner() == p_142018_2_) {
               return false;
            }
         }

         return p_142018_1_ instanceof EntityPlayer && p_142018_2_ instanceof EntityPlayer && !((EntityPlayer)p_142018_2_).canAttackPlayer((EntityPlayer)p_142018_1_) ? false : !(p_142018_1_ instanceof EntityHorse) || !((EntityHorse)p_142018_1_).isTame();
      } else {
         return false;
      }
   }

   public boolean canBeLeashedTo(EntityPlayer var1) {
      return !this.isAngry() && super.canBeLeashedTo(player);
   }
}
