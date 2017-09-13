package net.minecraft.entity.monster;

import java.util.Calendar;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeSnow;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

public class EntitySkeleton extends EntityMob implements IRangedAttackMob {
   private static final DataParameter SKELETON_VARIANT = EntityDataManager.createKey(EntitySkeleton.class, DataSerializers.VARINT);
   private static final DataParameter SWINGING_ARMS = EntityDataManager.createKey(EntitySkeleton.class, DataSerializers.BOOLEAN);
   private final EntityAIAttackRangedBow aiArrowAttack = new EntityAIAttackRangedBow(this, 1.0D, 20, 15.0F);
   private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false) {
      public void resetTask() {
         super.resetTask();
         EntitySkeleton.this.setSwingingArms(false);
      }

      public void startExecuting() {
         super.startExecuting();
         EntitySkeleton.this.setSwingingArms(true);
      }
   };

   public EntitySkeleton(World world) {
      super(world);
      this.setCombatTask();
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAIRestrictSun(this));
      this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
      this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
      this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(6, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, true));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SKELETON_VARIANT, Integer.valueOf(SkeletonType.NORMAL.getId()));
      this.dataManager.register(SWINGING_ARMS, Boolean.valueOf(false));
   }

   protected SoundEvent getAmbientSound() {
      return this.getSkeletonType().getAmbientSound();
   }

   protected SoundEvent getHurtSound() {
      return this.getSkeletonType().getHurtSound();
   }

   protected SoundEvent getDeathSound() {
      return this.getSkeletonType().getDeathSound();
   }

   protected void playStepSound(BlockPos blockposition, Block block) {
      SoundEvent soundeffect = this.getSkeletonType().getStepSound();
      this.playSound(soundeffect, 0.15F, 1.0F);
   }

   public boolean attackEntityAsMob(Entity entity) {
      if (super.attackEntityAsMob(entity)) {
         if (this.getSkeletonType() == SkeletonType.WITHER && entity instanceof EntityLivingBase) {
            ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.WITHER, 200));
         }

         return true;
      } else {
         return false;
      }
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEAD;
   }

   public void onLivingUpdate() {
      if (this.world.isDaytime() && !this.world.isRemote) {
         float f = this.getBrightness(1.0F);
         BlockPos blockposition = this.getRidingEntity() instanceof EntityBoat ? (new BlockPos(this.posX, (double)Math.round(this.posY), this.posZ)).up() : new BlockPos(this.posX, (double)Math.round(this.posY), this.posZ);
         if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canSeeSky(blockposition)) {
            boolean flag = true;
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (itemstack != null) {
               if (itemstack.isItemStackDamageable()) {
                  itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));
                  if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
                     this.renderBrokenItemStack(itemstack);
                     this.setItemStackToSlot(EntityEquipmentSlot.HEAD, (ItemStack)null);
                  }
               }

               flag = false;
            }

            if (flag) {
               EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), 8);
               this.world.getServer().getPluginManager().callEvent(event);
               if (!event.isCancelled()) {
                  this.setFire(event.getDuration());
               }
            }
         }
      }

      if (this.world.isRemote) {
         this.updateSize(this.getSkeletonType());
      }

      super.onLivingUpdate();
   }

   public void updateRidden() {
      super.updateRidden();
      if (this.getRidingEntity() instanceof EntityCreature) {
         EntityCreature entitycreature = (EntityCreature)this.getRidingEntity();
         this.renderYawOffset = entitycreature.renderYawOffset;
      }

   }

   public void onDeath(DamageSource damagesource) {
      if (damagesource.getSourceOfDamage() instanceof EntityArrow && damagesource.getEntity() instanceof EntityPlayer) {
         EntityPlayer entityhuman = (EntityPlayer)damagesource.getEntity();
         double d0 = entityhuman.posX - this.posX;
         double d1 = entityhuman.posZ - this.posZ;
         if (d0 * d0 + d1 * d1 >= 2500.0D) {
            entityhuman.addStat(AchievementList.SNIPE_SKELETON);
         }
      } else if (damagesource.getEntity() instanceof EntityCreeper && ((EntityCreeper)damagesource.getEntity()).getPowered() && ((EntityCreeper)damagesource.getEntity()).isAIEnabled()) {
         ((EntityCreeper)damagesource.getEntity()).incrementDroppedSkulls();
         this.entityDropItem(new ItemStack(Items.SKULL, 1, this.getSkeletonType() == SkeletonType.WITHER ? 1 : 0), 0.0F);
      }

      super.onDeath(damagesource);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return this.getSkeletonType().getLootTable();
   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficultydamagescaler) {
      super.setEquipmentBasedOnDifficulty(difficultydamagescaler);
      this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficultydamagescaler, @Nullable IEntityLivingData groupdataentity) {
      groupdataentity = super.onInitialSpawn(difficultydamagescaler, groupdataentity);
      if (this.world.provider instanceof WorldProviderHell && this.getRNG().nextInt(5) > 0) {
         this.tasks.addTask(4, this.aiAttackOnCollide);
         this.setSkeletonType(SkeletonType.WITHER);
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
      } else {
         Biome biomebase = this.world.getBiome(new BlockPos(this));
         if (biomebase instanceof BiomeSnow && this.world.canSeeSky(new BlockPos(this)) && this.rand.nextInt(5) != 0) {
            this.setSkeletonType(SkeletonType.STRAY);
         }

         this.tasks.addTask(4, this.aiArrowAttack);
         this.setEquipmentBasedOnDifficulty(difficultydamagescaler);
         this.setEnchantmentBasedOnDifficulty(difficultydamagescaler);
      }

      this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * difficultydamagescaler.getClampedAdditionalDifficulty());
      if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         Calendar calendar = this.world.getCurrentDate();
         if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
            this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      return groupdataentity;
   }

   public void setCombatTask() {
      if (this.world != null && !this.world.isRemote) {
         this.tasks.removeTask(this.aiAttackOnCollide);
         this.tasks.removeTask(this.aiArrowAttack);
         ItemStack itemstack = this.getHeldItemMainhand();
         if (itemstack != null && itemstack.getItem() == Items.BOW) {
            byte b0 = 20;
            if (this.world.getDifficulty() != EnumDifficulty.HARD) {
               b0 = 40;
            }

            this.aiArrowAttack.setAttackCooldown(b0);
            this.tasks.addTask(4, this.aiArrowAttack);
         } else {
            this.tasks.addTask(4, this.aiAttackOnCollide);
         }
      }

   }

   public void attackEntityWithRangedAttack(EntityLivingBase entityliving, float f) {
      EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
      double d0 = entityliving.posX - this.posX;
      double d1 = entityliving.getEntityBoundingBox().minY + (double)(entityliving.height / 3.0F) - entitytippedarrow.posY;
      double d2 = entityliving.posZ - this.posZ;
      double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
      entitytippedarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.world.getDifficulty().getDifficultyId() * 4));
      int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
      int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
      DifficultyInstance difficultydamagescaler = this.world.getDifficultyForLocation(new BlockPos(this));
      entitytippedarrow.setDamage((double)(f * 2.0F) + this.rand.nextGaussian() * 0.25D + (double)((float)this.world.getDifficulty().getDifficultyId() * 0.11F));
      if (i > 0) {
         entitytippedarrow.setDamage(entitytippedarrow.getDamage() + (double)i * 0.5D + 0.5D);
      }

      if (j > 0) {
         entitytippedarrow.setKnockbackStrength(j);
      }

      boolean flag = this.isBurning() && difficultydamagescaler.isHard() && this.rand.nextBoolean() || this.getSkeletonType() == SkeletonType.WITHER;
      flag = flag || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this) > 0;
      if (flag) {
         EntityCombustEvent event = new EntityCombustEvent(entitytippedarrow.getBukkitEntity(), 100);
         this.world.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            entitytippedarrow.setFire(event.getDuration());
         }
      }

      ItemStack itemstack = this.getHeldItem(EnumHand.OFF_HAND);
      if (itemstack != null && itemstack.getItem() == Items.TIPPED_ARROW) {
         entitytippedarrow.setPotionEffect(itemstack);
      } else if (this.getSkeletonType() == SkeletonType.STRAY) {
         entitytippedarrow.addEffect(new PotionEffect(MobEffects.SLOWNESS, 600));
      }

      EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(this, this.getHeldItemMainhand(), entitytippedarrow, 0.8F);
      if (event.isCancelled()) {
         event.getProjectile().remove();
      } else {
         if (event.getProjectile() == entitytippedarrow.getBukkitEntity()) {
            this.world.spawnEntity(entitytippedarrow);
         }

         this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
      }
   }

   public SkeletonType getSkeletonType() {
      return SkeletonType.getByOrdinal(((Integer)this.dataManager.get(SKELETON_VARIANT)).intValue());
   }

   public void setSkeletonType(SkeletonType enumskeletontype) {
      this.dataManager.set(SKELETON_VARIANT, Integer.valueOf(enumskeletontype.getId()));
      this.isImmuneToFire = enumskeletontype == SkeletonType.WITHER;
      this.updateSize(enumskeletontype);
   }

   private void updateSize(SkeletonType enumskeletontype) {
      if (enumskeletontype == SkeletonType.WITHER) {
         this.setSize(0.7F, 2.4F);
      } else {
         this.setSize(0.6F, 1.99F);
      }

   }

   public static void registerFixesSkeleton(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Skeleton");
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("SkeletonType", 99)) {
         byte b0 = nbttagcompound.getByte("SkeletonType");
         this.setSkeletonType(SkeletonType.getByOrdinal(b0));
      }

      this.setCombatTask();
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setByte("SkeletonType", (byte)this.getSkeletonType().getId());
   }

   public void setItemStackToSlot(EntityEquipmentSlot enumitemslot, @Nullable ItemStack itemstack) {
      super.setItemStackToSlot(enumitemslot, itemstack);
      if (!this.world.isRemote && enumitemslot == EntityEquipmentSlot.MAINHAND) {
         this.setCombatTask();
      }

   }

   public float getEyeHeight() {
      return this.getSkeletonType() == SkeletonType.WITHER ? 2.1F : 1.74F;
   }

   public double getYOffset() {
      return -0.35D;
   }

   public void setSwingingArms(boolean flag) {
      this.dataManager.set(SWINGING_ARMS, Boolean.valueOf(flag));
   }
}
