package net.minecraft.entity.passive;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntityOcelot extends EntityTameable {
   private static final DataParameter OCELOT_VARIANT = EntityDataManager.createKey(EntityOcelot.class, DataSerializers.VARINT);
   private EntityAIAvoidEntity avoidEntity;
   private EntityAITempt aiTempt;

   public EntityOcelot(World world) {
      super(world);
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
         double d0 = this.getMoveHelper().getSpeed();
         if (d0 == 0.6D) {
            this.setSneaking(true);
            this.setSprinting(false);
         } else if (d0 == 1.33D) {
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
      return !this.isTamed();
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
   }

   public void fall(float f, float f1) {
   }

   public static void registerFixesOcelot(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Ozelot");
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setInteger("CatType", this.getTameSkin());
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.setTameSkin(nbttagcompound.getInteger("CatType"));
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

   public boolean attackEntityAsMob(Entity entity) {
      return entity.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_OCELOT;
   }

   public boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (this.isTamed()) {
         if (this.isOwner(entityhuman) && !this.world.isRemote && !this.isBreedingItem(itemstack)) {
            this.aiSit.setSitting(!this.isSitting());
         }
      } else if ((this.aiTempt == null || this.aiTempt.isRunning()) && itemstack != null && itemstack.getItem() == Items.FISH && entityhuman.getDistanceSqToEntity(this) < 9.0D) {
         if (!entityhuman.capabilities.isCreativeMode) {
            --itemstack.stackSize;
         }

         if (!this.world.isRemote) {
            if (this.rand.nextInt(3) == 0 && !CraftEventFactory.callEntityTameEvent(this, entityhuman).isCancelled()) {
               this.setTamed(true);
               this.setTameSkin(1 + this.world.rand.nextInt(3));
               this.setOwnerId(entityhuman.getUniqueID());
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

      return super.processInteract(entityhuman, enumhand, itemstack);
   }

   public EntityOcelot createChild(EntityAgeable entityageable) {
      EntityOcelot entityocelot = new EntityOcelot(this.world);
      if (this.isTamed()) {
         entityocelot.setOwnerId(this.getOwnerId());
         entityocelot.setTamed(true);
         entityocelot.setTameSkin(this.getTameSkin());
      }

      return entityocelot;
   }

   public boolean isBreedingItem(@Nullable ItemStack itemstack) {
      return itemstack != null && itemstack.getItem() == Items.FISH;
   }

   public boolean canMateWith(EntityAnimal entityanimal) {
      if (entityanimal == this) {
         return false;
      } else if (!this.isTamed()) {
         return false;
      } else if (!(entityanimal instanceof EntityOcelot)) {
         return false;
      } else {
         EntityOcelot entityocelot = (EntityOcelot)entityanimal;
         return !entityocelot.isTamed() ? false : this.isInLove() && entityocelot.isInLove();
      }
   }

   public int getTameSkin() {
      return ((Integer)this.dataManager.get(OCELOT_VARIANT)).intValue();
   }

   public void setTameSkin(int i) {
      this.dataManager.set(OCELOT_VARIANT, Integer.valueOf(i));
   }

   public boolean getCanSpawnHere() {
      return this.world.rand.nextInt(3) != 0;
   }

   public boolean isNotColliding() {
      if (this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox())) {
         BlockPos blockposition = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
         if (blockposition.getY() < this.world.getSeaLevel()) {
            return false;
         }

         IBlockState iblockdata = this.world.getBlockState(blockposition.down());
         Block block = iblockdata.getBlock();
         if (block == Blocks.GRASS || iblockdata.getMaterial() == Material.LEAVES) {
            return true;
         }
      }

      return false;
   }

   public String getName() {
      return this.hasCustomName() ? this.getCustomNameTag() : (this.isTamed() ? I18n.translateToLocal("entity.Cat.name") : super.getName());
   }

   public void setTamed(boolean flag) {
      super.setTamed(flag);
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
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficultydamagescaler, @Nullable IEntityLivingData groupdataentity) {
      groupdataentity = super.onInitialSpawn(difficultydamagescaler, groupdataentity);
      if (this.getTameSkin() == 0 && this.world.rand.nextInt(7) == 0) {
         for(int i = 0; i < 2; ++i) {
            EntityOcelot entityocelot = new EntityOcelot(this.world);
            entityocelot.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
            entityocelot.setGrowingAge(-24000);
            this.world.addEntity(entityocelot, SpawnReason.OCELOT_BABY);
         }
      }

      return groupdataentity;
   }

   public EntityAgeable createChild(EntityAgeable entityageable) {
      return this.createChild(entityageable);
   }
}
