package net.minecraft.entity.passive;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAIOcelotSit;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityOcelot extends EntityTameable {
   private static final DataParameter OCELOT_VARIANT = EntityDataManager.createKey(EntityOcelot.class, DataSerializers.VARINT);
   private EntityAIAvoidEntity avoidEntity;
   private EntityAITempt aiTempt;

   public EntityOcelot(World var1) {
      super(var1);
      this.setSize(0.6F, 0.7F);
   }

   protected void initEntityAI() {
      this.aiSit = new EntityAISit(this);
      this.aiTempt = new EntityAITempt(this, 0.6D, Items.FISH, true);
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, this.aiSit);
      this.tasks.addTask(3, this.aiTempt);
      this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 5.0F));
      this.tasks.addTask(6, new EntityAIOcelotSit(this, 0.8D));
      this.tasks.addTask(7, new EntityAILeapAtTarget(this, 0.3F));
      this.tasks.addTask(8, new EntityAIOcelotAttack(this));
      this.tasks.addTask(9, new EntityAIMate(this, 0.8D));
      this.tasks.addTask(10, new EntityAIWander(this, 0.8D));
      this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
      this.targetTasks.addTask(1, new EntityAITargetNonTamed(this, EntityChicken.class, false, (Predicate)null));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(OCELOT_VARIANT, Integer.valueOf(0));
   }

   public void updateAITasks() {
      if (this.getMoveHelper().isUpdating()) {
         double var1 = this.getMoveHelper().getSpeed();
         if (var1 == 0.6D) {
            this.setSneaking(true);
            this.setSprinting(false);
         } else if (var1 == 1.33D) {
            this.setSneaking(false);
            this.setSprinting(true);
         } else {
            this.setSneaking(false);
            this.setSprinting(false);
         }
      } else {
         this.setSneaking(false);
         this.setSprinting(false);
      }

   }

   protected boolean canDespawn() {
      return !this.isTamed() && this.ticksExisted > 2400;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
   }

   public void fall(float var1, float var2) {
   }

   public static void registerFixesOcelot(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Ozelot");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("CatType", this.getTameSkin());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setTameSkin(var1.getInteger("CatType"));
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return this.isTamed() ? (this.isInLove() ? SoundEvents.ENTITY_CAT_PURR : (this.rand.nextInt(4) == 0 ? SoundEvents.ENTITY_CAT_PURREOW : SoundEvents.ENTITY_CAT_AMBIENT)) : null;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_CAT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CAT_DEATH;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   public boolean attackEntityAsMob(Entity var1) {
      return var1.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         if (this.aiSit != null) {
            this.aiSit.setSitting(false);
         }

         return super.attackEntityFrom(var1, var2);
      }
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_OCELOT;
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (this.isTamed()) {
         if (this.isOwner(var1) && !this.world.isRemote && !this.isBreedingItem(var3)) {
            this.aiSit.setSitting(!this.isSitting());
         }
      } else if ((this.aiTempt == null || this.aiTempt.isRunning()) && var3 != null && var3.getItem() == Items.FISH && var1.getDistanceSqToEntity(this) < 9.0D) {
         if (!var1.capabilities.isCreativeMode) {
            --var3.stackSize;
         }

         if (!this.world.isRemote) {
            if (this.rand.nextInt(3) == 0) {
               this.setTamed(true);
               this.setTameSkin(1 + this.world.rand.nextInt(3));
               this.setOwnerId(var1.getUniqueID());
               this.playTameEffect(true);
               this.aiSit.setSitting(true);
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

   public EntityOcelot createChild(EntityAgeable var1) {
      EntityOcelot var2 = new EntityOcelot(this.world);
      if (this.isTamed()) {
         var2.setOwnerId(this.getOwnerId());
         var2.setTamed(true);
         var2.setTameSkin(this.getTameSkin());
      }

      return var2;
   }

   public boolean isBreedingItem(@Nullable ItemStack var1) {
      return var1 != null && var1.getItem() == Items.FISH;
   }

   public boolean canMateWith(EntityAnimal var1) {
      if (var1 == this) {
         return false;
      } else if (!this.isTamed()) {
         return false;
      } else if (!(var1 instanceof EntityOcelot)) {
         return false;
      } else {
         EntityOcelot var2 = (EntityOcelot)var1;
         return !var2.isTamed() ? false : this.isInLove() && var2.isInLove();
      }
   }

   public int getTameSkin() {
      return ((Integer)this.dataManager.get(OCELOT_VARIANT)).intValue();
   }

   public void setTameSkin(int var1) {
      this.dataManager.set(OCELOT_VARIANT, Integer.valueOf(var1));
   }

   public boolean getCanSpawnHere() {
      return this.world.rand.nextInt(3) != 0;
   }

   public boolean isNotColliding() {
      if (this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox())) {
         BlockPos var1 = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
         if (var1.getY() < this.world.getSeaLevel()) {
            return false;
         }

         IBlockState var2 = this.world.getBlockState(var1.down());
         Block var3 = var2.getBlock();
         if (var3 == Blocks.GRASS || var3.isLeaves(var2, this.world, var1.down())) {
            return true;
         }
      }

      return false;
   }

   public String getName() {
      return this.hasCustomName() ? this.getCustomNameTag() : (this.isTamed() ? I18n.translateToLocal("entity.Cat.name") : super.getName());
   }

   public void setTamed(boolean var1) {
      super.setTamed(var1);
   }

   protected void setupTamedAI() {
      if (this.avoidEntity == null) {
         this.avoidEntity = new EntityAIAvoidEntity(this, EntityPlayer.class, 16.0F, 0.8D, 1.33D);
      }

      this.tasks.removeTask(this.avoidEntity);
      if (!this.isTamed()) {
         this.tasks.addTask(4, this.avoidEntity);
      }

   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      var2 = super.onInitialSpawn(var1, var2);
      if (this.getTameSkin() == 0 && this.world.rand.nextInt(7) == 0) {
         for(int var3 = 0; var3 < 2; ++var3) {
            EntityOcelot var4 = new EntityOcelot(this.world);
            var4.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
            var4.setGrowingAge(-24000);
            this.world.spawnEntity(var4);
         }
      }

      return var2;
   }
}
