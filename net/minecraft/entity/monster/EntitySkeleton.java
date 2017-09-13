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

   public EntitySkeleton(World var1) {
      super(var1);
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

   protected void playStepSound(BlockPos var1, Block var2) {
      SoundEvent var3 = this.getSkeletonType().getStepSound();
      this.playSound(var3, 0.15F, 1.0F);
   }

   public boolean attackEntityAsMob(Entity var1) {
      if (super.attackEntityAsMob(var1)) {
         if (this.getSkeletonType() == SkeletonType.WITHER && var1 instanceof EntityLivingBase) {
            ((EntityLivingBase)var1).addPotionEffect(new PotionEffect(MobEffects.WITHER, 200));
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
         float var1 = this.getBrightness(1.0F);
         BlockPos var2 = this.getRidingEntity() instanceof EntityBoat ? (new BlockPos(this.posX, (double)Math.round(this.posY), this.posZ)).up() : new BlockPos(this.posX, (double)Math.round(this.posY), this.posZ);
         if (var1 > 0.5F && this.rand.nextFloat() * 30.0F < (var1 - 0.4F) * 2.0F && this.world.canSeeSky(var2)) {
            boolean var3 = true;
            ItemStack var4 = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (var4 != null) {
               if (var4.isItemStackDamageable()) {
                  var4.setItemDamage(var4.getItemDamage() + this.rand.nextInt(2));
                  if (var4.getItemDamage() >= var4.getMaxDamage()) {
                     this.renderBrokenItemStack(var4);
                     this.setItemStackToSlot(EntityEquipmentSlot.HEAD, (ItemStack)null);
                  }
               }

               var3 = false;
            }

            if (var3) {
               EntityCombustEvent var5 = new EntityCombustEvent(this.getBukkitEntity(), 8);
               this.world.getServer().getPluginManager().callEvent(var5);
               if (!var5.isCancelled()) {
                  this.setFire(var5.getDuration());
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
         EntityCreature var1 = (EntityCreature)this.getRidingEntity();
         this.renderYawOffset = var1.renderYawOffset;
      }

   }

   public void onDeath(DamageSource var1) {
      if (var1.getSourceOfDamage() instanceof EntityArrow && var1.getEntity() instanceof EntityPlayer) {
         EntityPlayer var2 = (EntityPlayer)var1.getEntity();
         double var3 = var2.posX - this.posX;
         double var5 = var2.posZ - this.posZ;
         if (var3 * var3 + var5 * var5 >= 2500.0D) {
            var2.addStat(AchievementList.SNIPE_SKELETON);
         }
      } else if (var1.getEntity() instanceof EntityCreeper && ((EntityCreeper)var1.getEntity()).getPowered() && ((EntityCreeper)var1.getEntity()).isAIEnabled()) {
         ((EntityCreeper)var1.getEntity()).incrementDroppedSkulls();
         this.entityDropItem(new ItemStack(Items.SKULL, 1, this.getSkeletonType() == SkeletonType.WITHER ? 1 : 0), 0.0F);
      }

      super.onDeath(var1);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return this.getSkeletonType().getLootTable();
   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance var1) {
      super.setEquipmentBasedOnDifficulty(var1);
      this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      var2 = super.onInitialSpawn(var1, var2);
      if (this.world.provider instanceof WorldProviderHell && this.getRNG().nextInt(5) > 0) {
         this.tasks.addTask(4, this.aiAttackOnCollide);
         this.setSkeletonType(SkeletonType.WITHER);
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
      } else {
         Biome var3 = this.world.getBiome(new BlockPos(this));
         if (var3 instanceof BiomeSnow && this.world.canSeeSky(new BlockPos(this)) && this.rand.nextInt(5) != 0) {
            this.setSkeletonType(SkeletonType.STRAY);
         }

         this.tasks.addTask(4, this.aiArrowAttack);
         this.setEquipmentBasedOnDifficulty(var1);
         this.setEnchantmentBasedOnDifficulty(var1);
      }

      this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * var1.getClampedAdditionalDifficulty());
      if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         Calendar var5 = this.world.getCurrentDate();
         if (var5.get(2) + 1 == 10 && var5.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
            this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      return var2;
   }

   public void setCombatTask() {
      if (this.world != null && !this.world.isRemote) {
         this.tasks.removeTask(this.aiAttackOnCollide);
         this.tasks.removeTask(this.aiArrowAttack);
         ItemStack var1 = this.getHeldItemMainhand();
         if (var1 != null && var1.getItem() == Items.BOW) {
            byte var2 = 20;
            if (this.world.getDifficulty() != EnumDifficulty.HARD) {
               var2 = 40;
            }

            this.aiArrowAttack.setAttackCooldown(var2);
            this.tasks.addTask(4, this.aiArrowAttack);
         } else {
            this.tasks.addTask(4, this.aiAttackOnCollide);
         }
      }

   }

   public void attackEntityWithRangedAttack(EntityLivingBase var1, float var2) {
      EntityTippedArrow var3 = new EntityTippedArrow(this.world, this);
      double var4 = var1.posX - this.posX;
      double var6 = var1.getEntityBoundingBox().minY + (double)(var1.height / 3.0F) - var3.posY;
      double var8 = var1.posZ - this.posZ;
      double var10 = (double)MathHelper.sqrt(var4 * var4 + var8 * var8);
      var3.setThrowableHeading(var4, var6 + var10 * 0.20000000298023224D, var8, 1.6F, (float)(14 - this.world.getDifficulty().getDifficultyId() * 4));
      int var12 = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
      int var13 = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
      DifficultyInstance var14 = this.world.getDifficultyForLocation(new BlockPos(this));
      var3.setDamage((double)(var2 * 2.0F) + this.rand.nextGaussian() * 0.25D + (double)((float)this.world.getDifficulty().getDifficultyId() * 0.11F));
      if (var12 > 0) {
         var3.setDamage(var3.getDamage() + (double)var12 * 0.5D + 0.5D);
      }

      if (var13 > 0) {
         var3.setKnockbackStrength(var13);
      }

      boolean var15 = this.isBurning() && var14.isHard() && this.rand.nextBoolean() || this.getSkeletonType() == SkeletonType.WITHER;
      var15 = var15 || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this) > 0;
      if (var15) {
         EntityCombustEvent var16 = new EntityCombustEvent(var3.getBukkitEntity(), 100);
         this.world.getServer().getPluginManager().callEvent(var16);
         if (!var16.isCancelled()) {
            var3.setFire(var16.getDuration());
         }
      }

      ItemStack var19 = this.getHeldItem(EnumHand.OFF_HAND);
      if (var19 != null && var19.getItem() == Items.TIPPED_ARROW) {
         var3.setPotionEffect(var19);
      } else if (this.getSkeletonType() == SkeletonType.STRAY) {
         var3.addEffect(new PotionEffect(MobEffects.SLOWNESS, 600));
      }

      EntityShootBowEvent var17 = CraftEventFactory.callEntityShootBowEvent(this, this.getHeldItemMainhand(), var3, 0.8F);
      if (var17.isCancelled()) {
         var17.getProjectile().remove();
      } else {
         if (var17.getProjectile() == var3.getBukkitEntity()) {
            this.world.spawnEntity(var3);
         }

         this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
      }
   }

   public SkeletonType getSkeletonType() {
      return SkeletonType.getByOrdinal(((Integer)this.dataManager.get(SKELETON_VARIANT)).intValue());
   }

   public void setSkeletonType(SkeletonType var1) {
      this.dataManager.set(SKELETON_VARIANT, Integer.valueOf(var1.getId()));
      this.isImmuneToFire = var1 == SkeletonType.WITHER;
      this.updateSize(var1);
   }

   private void updateSize(SkeletonType var1) {
      if (var1 == SkeletonType.WITHER) {
         this.setSize(0.7F, 2.4F);
      } else {
         this.setSize(0.6F, 1.99F);
      }

   }

   public static void registerFixesSkeleton(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Skeleton");
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("SkeletonType", 99)) {
         byte var2 = var1.getByte("SkeletonType");
         this.setSkeletonType(SkeletonType.getByOrdinal(var2));
      }

      this.setCombatTask();
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setByte("SkeletonType", (byte)this.getSkeletonType().getId());
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2) {
      super.setItemStackToSlot(var1, var2);
      if (!this.world.isRemote && var1 == EntityEquipmentSlot.MAINHAND) {
         this.setCombatTask();
      }

   }

   public float getEyeHeight() {
      return this.getSkeletonType() == SkeletonType.WITHER ? 2.1F : 1.74F;
   }

   public double getYOffset() {
      return -0.35D;
   }

   public void setSwingingArms(boolean var1) {
      this.dataManager.set(SWINGING_ARMS, Boolean.valueOf(var1));
   }
}
