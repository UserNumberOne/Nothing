package net.minecraft.entity.passive;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy;
import net.minecraft.entity.ai.EntityAISkeletonRiders;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class EntityHorse extends EntityAnimal implements IInventoryChangedListener, IJumpingMount {
   private static final Predicate IS_HORSE_BREEDING = new Predicate() {
      public boolean apply(@Nullable Entity entity) {
         return entity instanceof EntityHorse && ((EntityHorse)entity).isBreeding();
      }

      public boolean apply(Object object) {
         return this.apply((Entity)object);
      }
   };
   public static final IAttribute JUMP_STRENGTH = (new RangedAttribute((IAttribute)null, "horse.jumpStrength", 0.7D, 0.0D, 2.0D)).setDescription("Jump Strength").setShouldWatch(true);
   private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
   private static final DataParameter STATUS = EntityDataManager.createKey(EntityHorse.class, DataSerializers.BYTE);
   private static final DataParameter HORSE_TYPE = EntityDataManager.createKey(EntityHorse.class, DataSerializers.VARINT);
   private static final DataParameter HORSE_VARIANT = EntityDataManager.createKey(EntityHorse.class, DataSerializers.VARINT);
   private static final DataParameter OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityHorse.class, DataSerializers.OPTIONAL_UNIQUE_ID);
   private static final DataParameter HORSE_ARMOR = EntityDataManager.createKey(EntityHorse.class, DataSerializers.VARINT);
   private static final String[] HORSE_TEXTURES = new String[]{"textures/entity/horse/horse_white.png", "textures/entity/horse/horse_creamy.png", "textures/entity/horse/horse_chestnut.png", "textures/entity/horse/horse_brown.png", "textures/entity/horse/horse_black.png", "textures/entity/horse/horse_gray.png", "textures/entity/horse/horse_darkbrown.png"};
   private static final String[] HORSE_TEXTURES_ABBR = new String[]{"hwh", "hcr", "hch", "hbr", "hbl", "hgr", "hdb"};
   private static final String[] HORSE_MARKING_TEXTURES = new String[]{null, "textures/entity/horse/horse_markings_white.png", "textures/entity/horse/horse_markings_whitefield.png", "textures/entity/horse/horse_markings_whitedots.png", "textures/entity/horse/horse_markings_blackdots.png"};
   private static final String[] HORSE_MARKING_TEXTURES_ABBR = new String[]{"", "wo_", "wmo", "wdo", "bdo"};
   private final EntityAISkeletonRiders skeletonTrapAI = new EntityAISkeletonRiders(this);
   private int eatingHaystackCounter;
   private int openMouthCounter;
   private int jumpRearingCounter;
   public int tailCounter;
   public int sprintCounter;
   protected boolean horseJumping;
   public AnimalChest horseChest;
   private boolean hasReproduced;
   protected int temper;
   protected float jumpPower;
   private boolean allowStandSliding;
   private boolean skeletonTrap;
   private int skeletonTrapTime;
   private float headLean;
   private float prevHeadLean;
   private float rearingAmount;
   private float prevRearingAmount;
   private float mouthOpenness;
   private float prevMouthOpenness;
   private int gallopTime;
   private String texturePrefix;
   private final String[] horseTexturesArray = new String[3];
   public int maxDomestication = 100;

   public EntityHorse(World world) {
      super(world);
      this.setSize(1.3964844F, 1.6F);
      this.isImmuneToFire = false;
      this.setChested(false);
      this.stepHeight = 1.0F;
      this.initHorseChest();
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new EntityAIPanic(this, 1.2D));
      this.tasks.addTask(1, new EntityAIRunAroundLikeCrazy(this, 1.2D));
      this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
      this.tasks.addTask(4, new EntityAIFollowParent(this, 1.0D));
      this.tasks.addTask(6, new EntityAIWander(this, 0.7D));
      this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(STATUS, Byte.valueOf((byte)0));
      this.dataManager.register(HORSE_TYPE, Integer.valueOf(HorseType.HORSE.getOrdinal()));
      this.dataManager.register(HORSE_VARIANT, Integer.valueOf(0));
      this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
      this.dataManager.register(HORSE_ARMOR, Integer.valueOf(HorseArmorType.NONE.getOrdinal()));
   }

   public void setType(HorseType enumhorsetype) {
      this.dataManager.set(HORSE_TYPE, Integer.valueOf(enumhorsetype.getOrdinal()));
      this.resetTexturePrefix();
   }

   public HorseType getType() {
      return HorseType.getArmorType(((Integer)this.dataManager.get(HORSE_TYPE)).intValue());
   }

   public void setHorseVariant(int i) {
      this.dataManager.set(HORSE_VARIANT, Integer.valueOf(i));
      this.resetTexturePrefix();
   }

   public int getHorseVariant() {
      return ((Integer)this.dataManager.get(HORSE_VARIANT)).intValue();
   }

   public String getName() {
      return this.hasCustomName() ? this.getCustomNameTag() : this.getType().getDefaultName().getUnformattedText();
   }

   private boolean getHorseWatchableBoolean(int i) {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & i) != 0;
   }

   private void setHorseWatchableBoolean(int i, boolean flag) {
      byte b0 = ((Byte)this.dataManager.get(STATUS)).byteValue();
      if (flag) {
         this.dataManager.set(STATUS, Byte.valueOf((byte)(b0 | i)));
      } else {
         this.dataManager.set(STATUS, Byte.valueOf((byte)(b0 & ~i)));
      }

   }

   public boolean isAdultHorse() {
      return !this.isChild();
   }

   public boolean isTame() {
      return this.getHorseWatchableBoolean(2);
   }

   public boolean isRidable() {
      return this.isAdultHorse();
   }

   @Nullable
   public UUID getOwnerUniqueId() {
      return (UUID)((Optional)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
   }

   public void setOwnerUniqueId(@Nullable UUID uuid) {
      this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(uuid));
   }

   public float getHorseSize() {
      return 0.5F;
   }

   public void setScaleForAge(boolean flag) {
      if (flag) {
         this.setScale(this.getHorseSize());
      } else {
         this.setScale(1.0F);
      }

   }

   public boolean isHorseJumping() {
      return this.horseJumping;
   }

   public void setHorseTamed(boolean flag) {
      this.setHorseWatchableBoolean(2, flag);
   }

   public void setHorseJumping(boolean flag) {
      this.horseJumping = flag;
   }

   public boolean canBeLeashedTo(EntityPlayer entityhuman) {
      return !this.getType().isUndead() && super.canBeLeashedTo(entityhuman);
   }

   protected void onLeashDistance(float f) {
      if (f > 6.0F && this.isEatingHaystack()) {
         this.setEatingHaystack(false);
      }

   }

   public boolean isChested() {
      return this.getType().canBeChested() && this.getHorseWatchableBoolean(8);
   }

   public HorseArmorType getHorseArmorType() {
      return HorseArmorType.getByOrdinal(((Integer)this.dataManager.get(HORSE_ARMOR)).intValue());
   }

   public boolean isEatingHaystack() {
      return this.getHorseWatchableBoolean(32);
   }

   public boolean isRearing() {
      return this.getHorseWatchableBoolean(64);
   }

   public boolean isBreeding() {
      return this.getHorseWatchableBoolean(16);
   }

   public boolean getHasReproduced() {
      return this.hasReproduced;
   }

   public void setHorseArmorStack(ItemStack itemstack) {
      HorseArmorType enumhorsearmor = HorseArmorType.getByItemStack(itemstack);
      this.dataManager.set(HORSE_ARMOR, Integer.valueOf(enumhorsearmor.getOrdinal()));
      this.resetTexturePrefix();
      if (!this.world.isRemote) {
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
         int i = enumhorsearmor.getProtection();
         if (i != 0) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier((new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)i, 0)).setSaved(false));
         }
      }

   }

   public void setBreeding(boolean flag) {
      this.setHorseWatchableBoolean(16, flag);
   }

   public void setChested(boolean flag) {
      this.setHorseWatchableBoolean(8, flag);
   }

   public void setHasReproduced(boolean flag) {
      this.hasReproduced = flag;
   }

   public void setHorseSaddled(boolean flag) {
      this.setHorseWatchableBoolean(4, flag);
   }

   public int getTemper() {
      return this.temper;
   }

   public void setTemper(int i) {
      this.temper = i;
   }

   public int increaseTemper(int i) {
      int j = MathHelper.clamp(this.getTemper() + i, 0, this.getMaxTemper());
      this.setTemper(j);
      return j;
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      Entity entity = damagesource.getEntity();
      return this.isBeingRidden() && entity != null && this.isRidingOrBeingRiddenBy(entity) ? false : super.attackEntityFrom(damagesource, f);
   }

   public boolean canBePushed() {
      return !this.isBeingRidden();
   }

   public boolean prepareChunkForSpawn() {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posZ);
      this.world.getBiome(new BlockPos(i, 0, j));
      return true;
   }

   public void dropChests() {
      if (!this.world.isRemote && this.isChested()) {
         this.dropItem(Item.getItemFromBlock(Blocks.CHEST), 1);
         this.setChested(false);
      }

   }

   private void eatingHorse() {
      this.openHorseMouth();
      if (!this.isSilent()) {
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_HORSE_EAT, this.getSoundCategory(), 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
      }

   }

   public void fall(float f, float f1) {
      if (f > 1.0F) {
         this.playSound(SoundEvents.ENTITY_HORSE_LAND, 0.4F, 1.0F);
      }

      int i = MathHelper.ceil((f * 0.5F - 3.0F) * f1);
      if (i > 0) {
         this.attackEntityFrom(DamageSource.fall, (float)i);
         if (this.isBeingRidden()) {
            for(Entity entity : this.getRecursivePassengers()) {
               entity.attackEntityFrom(DamageSource.fall, (float)i);
            }
         }

         IBlockState iblockdata = this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.2D - (double)this.prevRotationYaw, this.posZ));
         Block block = iblockdata.getBlock();
         if (iblockdata.getMaterial() != Material.AIR && !this.isSilent()) {
            SoundType soundeffecttype = block.getSoundType();
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, soundeffecttype.getStepSound(), this.getSoundCategory(), soundeffecttype.getVolume() * 0.5F, soundeffecttype.getPitch() * 0.75F);
         }
      }

   }

   private int getChestSize() {
      HorseType enumhorsetype = this.getType();
      return this.isChested() && enumhorsetype.canBeChested() ? 17 : 2;
   }

   public void initHorseChest() {
      AnimalChest inventoryhorsechest = this.horseChest;
      this.horseChest = new AnimalChest("HorseChest", this.getChestSize(), this);
      this.horseChest.setCustomName(this.getName());
      if (inventoryhorsechest != null) {
         inventoryhorsechest.removeInventoryChangeListener(this);
         int i = Math.min(inventoryhorsechest.getSizeInventory(), this.horseChest.getSizeInventory());

         for(int j = 0; j < i; ++j) {
            ItemStack itemstack = inventoryhorsechest.getStackInSlot(j);
            if (itemstack != null) {
               this.horseChest.setInventorySlotContents(j, itemstack.copy());
            }
         }
      }

      this.horseChest.addInventoryChangeListener(this);
      this.updateHorseSlots();
   }

   private void updateHorseSlots() {
      if (!this.world.isRemote) {
         this.setHorseSaddled(this.horseChest.getStackInSlot(0) != null);
         if (this.getType().isHorse()) {
            this.setHorseArmorStack(this.horseChest.getStackInSlot(1));
         }
      }

   }

   public void onInventoryChanged(InventoryBasic inventorysubcontainer) {
      HorseArmorType enumhorsearmor = this.getHorseArmorType();
      boolean flag = this.isHorseSaddled();
      this.updateHorseSlots();
      if (this.ticksExisted > 20) {
         if (enumhorsearmor == HorseArmorType.NONE && enumhorsearmor != this.getHorseArmorType()) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
         } else if (enumhorsearmor != this.getHorseArmorType()) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
         }

         if (!flag && this.isHorseSaddled()) {
            this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, 0.5F, 1.0F);
         }
      }

   }

   public boolean getCanSpawnHere() {
      this.prepareChunkForSpawn();
      return super.getCanSpawnHere();
   }

   protected EntityHorse getClosestHorse(Entity entity, double d0) {
      double d1 = Double.MAX_VALUE;
      Entity entity1 = null;

      for(Entity entity2 : this.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(d0, d0, d0), IS_HORSE_BREEDING)) {
         double d2 = entity2.getDistanceSq(entity.posX, entity.posY, entity.posZ);
         if (d2 < d1) {
            entity1 = entity2;
            d1 = d2;
         }
      }

      return (EntityHorse)entity1;
   }

   public double getHorseJumpStrength() {
      return this.getEntityAttribute(JUMP_STRENGTH).getAttributeValue();
   }

   protected SoundEvent getDeathSound() {
      this.openHorseMouth();
      return this.getType().getDeathSound();
   }

   protected SoundEvent getHurtSound() {
      this.openHorseMouth();
      if (this.rand.nextInt(3) == 0) {
         this.makeHorseRear();
      }

      return this.getType().getHurtSound();
   }

   public boolean isHorseSaddled() {
      return this.getHorseWatchableBoolean(4);
   }

   protected SoundEvent getAmbientSound() {
      this.openHorseMouth();
      if (this.rand.nextInt(10) == 0 && !this.isMovementBlocked()) {
         this.makeHorseRear();
      }

      return this.getType().getAmbientSound();
   }

   @Nullable
   protected SoundEvent getAngrySound() {
      this.openHorseMouth();
      this.makeHorseRear();
      HorseType enumhorsetype = this.getType();
      return enumhorsetype.isUndead() ? null : (enumhorsetype.hasMuleEars() ? SoundEvents.ENTITY_DONKEY_ANGRY : SoundEvents.ENTITY_HORSE_ANGRY);
   }

   protected void playStepSound(BlockPos blockposition, Block block) {
      SoundType soundeffecttype = block.getSoundType();
      if (this.world.getBlockState(blockposition.up()).getBlock() == Blocks.SNOW_LAYER) {
         soundeffecttype = Blocks.SNOW_LAYER.getSoundType();
      }

      if (!block.getDefaultState().getMaterial().isLiquid()) {
         HorseType enumhorsetype = this.getType();
         if (this.isBeingRidden() && !enumhorsetype.hasMuleEars()) {
            ++this.gallopTime;
            if (this.gallopTime > 5 && this.gallopTime % 3 == 0) {
               this.playSound(SoundEvents.ENTITY_HORSE_GALLOP, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
               if (enumhorsetype == HorseType.HORSE && this.rand.nextInt(10) == 0) {
                  this.playSound(SoundEvents.ENTITY_HORSE_BREATHE, soundeffecttype.getVolume() * 0.6F, soundeffecttype.getPitch());
               }
            } else if (this.gallopTime <= 5) {
               this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
            }
         } else if (soundeffecttype == SoundType.WOOD) {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
         } else {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP, soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
         }
      }

   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(JUMP_STRENGTH);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(53.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22499999403953552D);
   }

   public int getMaxSpawnedInChunk() {
      return 6;
   }

   public int getMaxTemper() {
      return this.maxDomestication;
   }

   protected float getSoundVolume() {
      return 0.8F;
   }

   public int getTalkInterval() {
      return 400;
   }

   private void resetTexturePrefix() {
      this.texturePrefix = null;
   }

   public void openGUI(EntityPlayer entityhuman) {
      if (!this.world.isRemote && (!this.isBeingRidden() || this.isPassenger(entityhuman)) && this.isTame()) {
         this.horseChest.setCustomName(this.getName());
         entityhuman.openGuiHorseInventory(this, this.horseChest);
      }

   }

   public boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (itemstack != null && itemstack.getItem() == Items.SPAWN_EGG) {
         return super.processInteract(entityhuman, enumhand, itemstack);
      } else if (!this.isTame() && this.getType().isUndead()) {
         return false;
      } else if (this.isTame() && this.isAdultHorse() && entityhuman.isSneaking()) {
         this.openGUI(entityhuman);
         return true;
      } else if (this.isRidable() && this.isBeingRidden()) {
         return super.processInteract(entityhuman, enumhand, itemstack);
      } else {
         if (itemstack != null) {
            if (this.getType().isHorse()) {
               HorseArmorType enumhorsearmor = HorseArmorType.getByItemStack(itemstack);
               if (enumhorsearmor != HorseArmorType.NONE) {
                  if (!this.isTame()) {
                     this.makeHorseRearWithSound();
                     return true;
                  }

                  this.openGUI(entityhuman);
                  return true;
               }
            }

            boolean flag = false;
            if (!this.getType().isUndead()) {
               float f = 0.0F;
               short short0 = 0;
               byte b0 = 0;
               if (itemstack.getItem() == Items.WHEAT) {
                  f = 2.0F;
                  short0 = 20;
                  b0 = 3;
               } else if (itemstack.getItem() == Items.SUGAR) {
                  f = 1.0F;
                  short0 = 30;
                  b0 = 3;
               } else if (Block.getBlockFromItem(itemstack.getItem()) == Blocks.HAY_BLOCK) {
                  f = 20.0F;
                  short0 = 180;
               } else if (itemstack.getItem() == Items.APPLE) {
                  f = 3.0F;
                  short0 = 60;
                  b0 = 3;
               } else if (itemstack.getItem() == Items.GOLDEN_CARROT) {
                  f = 4.0F;
                  short0 = 60;
                  b0 = 5;
                  if (this.isTame() && this.getGrowingAge() == 0) {
                     flag = true;
                     this.setInLove(entityhuman);
                  }
               } else if (itemstack.getItem() == Items.GOLDEN_APPLE) {
                  f = 10.0F;
                  short0 = 240;
                  b0 = 10;
                  if (this.isTame() && this.getGrowingAge() == 0 && !this.isInLove()) {
                     flag = true;
                     this.setInLove(entityhuman);
                  }
               }

               if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
                  this.heal(f, RegainReason.EATING);
                  flag = true;
               }

               if (!this.isAdultHorse() && short0 > 0) {
                  if (!this.world.isRemote) {
                     this.addGrowth(short0);
                  }

                  flag = true;
               }

               if (b0 > 0 && (flag || !this.isTame()) && this.getTemper() < this.getMaxTemper()) {
                  flag = true;
                  if (!this.world.isRemote) {
                     this.increaseTemper(b0);
                  }
               }

               if (flag) {
                  this.eatingHorse();
               }
            }

            if (!this.isTame() && !flag) {
               if (itemstack.interactWithEntity(entityhuman, this, enumhand)) {
                  return true;
               }

               this.makeHorseRearWithSound();
               return true;
            }

            if (!flag && this.getType().canBeChested() && !this.isChested() && itemstack.getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
               this.setChested(true);
               this.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
               flag = true;
               this.initHorseChest();
            }

            if (!flag && this.isRidable() && !this.isHorseSaddled() && itemstack.getItem() == Items.SADDLE) {
               this.openGUI(entityhuman);
               return true;
            }

            if (flag) {
               if (!entityhuman.capabilities.isCreativeMode) {
                  --itemstack.stackSize;
               }

               return true;
            }
         }

         if (this.isRidable() && !this.isBeingRidden()) {
            if (itemstack != null && itemstack.interactWithEntity(entityhuman, this, enumhand)) {
               return true;
            } else {
               this.mountTo(entityhuman);
               return true;
            }
         } else {
            return super.processInteract(entityhuman, enumhand, itemstack);
         }
      }
   }

   private void mountTo(EntityPlayer entityhuman) {
      entityhuman.rotationYaw = this.rotationYaw;
      entityhuman.rotationPitch = this.rotationPitch;
      this.setEatingHaystack(false);
      this.setRearing(false);
      if (!this.world.isRemote) {
         entityhuman.startRiding(this);
      }

   }

   protected boolean isMovementBlocked() {
      return this.isBeingRidden() && this.isHorseSaddled() ? true : this.isEatingHaystack() || this.isRearing();
   }

   public boolean isBreedingItem(@Nullable ItemStack itemstack) {
      return false;
   }

   private void moveTail() {
      this.tailCounter = 1;
   }

   public void onDeath(DamageSource damagesource) {
      if (!this.world.isRemote) {
         this.dropChestItems();
      }

      super.onDeath(damagesource);
   }

   public void onLivingUpdate() {
      if (this.rand.nextInt(200) == 0) {
         this.moveTail();
      }

      super.onLivingUpdate();
      if (!this.world.isRemote) {
         if (this.rand.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0F, RegainReason.REGEN);
         }

         if (!this.isEatingHaystack() && !this.isBeingRidden() && this.rand.nextInt(300) == 0 && this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY) - 1, MathHelper.floor(this.posZ))).getBlock() == Blocks.GRASS) {
            this.setEatingHaystack(true);
         }

         if (this.isEatingHaystack() && ++this.eatingHaystackCounter > 50) {
            this.eatingHaystackCounter = 0;
            this.setEatingHaystack(false);
         }

         if (this.isBreeding() && !this.isAdultHorse() && !this.isEatingHaystack()) {
            EntityHorse entityhorse = this.getClosestHorse(this, 16.0D);
            if (entityhorse != null && this.getDistanceSqToEntity(entityhorse) > 4.0D) {
               this.navigator.getPathToEntityLiving(entityhorse);
            }
         }

         if (this.isSkeletonTrap() && this.skeletonTrapTime++ >= 18000) {
            this.setDead();
         }
      }

   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote && this.dataManager.isDirty()) {
         this.dataManager.setClean();
         this.resetTexturePrefix();
      }

      if (this.openMouthCounter > 0 && ++this.openMouthCounter > 30) {
         this.openMouthCounter = 0;
         this.setHorseWatchableBoolean(128, false);
      }

      if (this.canPassengerSteer() && this.jumpRearingCounter > 0 && ++this.jumpRearingCounter > 20) {
         this.jumpRearingCounter = 0;
         this.setRearing(false);
      }

      if (this.tailCounter > 0 && ++this.tailCounter > 8) {
         this.tailCounter = 0;
      }

      if (this.sprintCounter > 0) {
         ++this.sprintCounter;
         if (this.sprintCounter > 300) {
            this.sprintCounter = 0;
         }
      }

      this.prevHeadLean = this.headLean;
      if (this.isEatingHaystack()) {
         this.headLean += (1.0F - this.headLean) * 0.4F + 0.05F;
         if (this.headLean > 1.0F) {
            this.headLean = 1.0F;
         }
      } else {
         this.headLean += (0.0F - this.headLean) * 0.4F - 0.05F;
         if (this.headLean < 0.0F) {
            this.headLean = 0.0F;
         }
      }

      this.prevRearingAmount = this.rearingAmount;
      if (this.isRearing()) {
         this.headLean = 0.0F;
         this.prevHeadLean = this.headLean;
         this.rearingAmount += (1.0F - this.rearingAmount) * 0.4F + 0.05F;
         if (this.rearingAmount > 1.0F) {
            this.rearingAmount = 1.0F;
         }
      } else {
         this.allowStandSliding = false;
         this.rearingAmount += (0.8F * this.rearingAmount * this.rearingAmount * this.rearingAmount - this.rearingAmount) * 0.6F - 0.05F;
         if (this.rearingAmount < 0.0F) {
            this.rearingAmount = 0.0F;
         }
      }

      this.prevMouthOpenness = this.mouthOpenness;
      if (this.getHorseWatchableBoolean(128)) {
         this.mouthOpenness += (1.0F - this.mouthOpenness) * 0.7F + 0.05F;
         if (this.mouthOpenness > 1.0F) {
            this.mouthOpenness = 1.0F;
         }
      } else {
         this.mouthOpenness += (0.0F - this.mouthOpenness) * 0.7F - 0.05F;
         if (this.mouthOpenness < 0.0F) {
            this.mouthOpenness = 0.0F;
         }
      }

   }

   private void openHorseMouth() {
      if (!this.world.isRemote) {
         this.openMouthCounter = 1;
         this.setHorseWatchableBoolean(128, true);
      }

   }

   private boolean canMate() {
      return !this.isBeingRidden() && !this.isRiding() && this.isTame() && this.isAdultHorse() && this.getType().canMate() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
   }

   public void setEatingHaystack(boolean flag) {
      this.setHorseWatchableBoolean(32, flag);
   }

   public void setRearing(boolean flag) {
      if (flag) {
         this.setEatingHaystack(false);
      }

      this.setHorseWatchableBoolean(64, flag);
   }

   private void makeHorseRear() {
      if (this.canPassengerSteer()) {
         this.jumpRearingCounter = 1;
         this.setRearing(true);
      }

   }

   public void makeHorseRearWithSound() {
      this.makeHorseRear();
      SoundEvent soundeffect = this.getAngrySound();
      if (soundeffect != null) {
         this.playSound(soundeffect, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public void dropChestItems() {
      this.dropItemsInChest(this, this.horseChest);
      this.dropChests();
   }

   private void dropItemsInChest(Entity entity, AnimalChest inventoryhorsechest) {
      if (inventoryhorsechest != null && !this.world.isRemote) {
         for(int i = 0; i < inventoryhorsechest.getSizeInventory(); ++i) {
            ItemStack itemstack = inventoryhorsechest.getStackInSlot(i);
            if (itemstack != null) {
               this.entityDropItem(itemstack, 0.0F);
            }
         }
      }

   }

   public boolean setTamedBy(EntityPlayer entityhuman) {
      this.setOwnerUniqueId(entityhuman.getUniqueID());
      this.setHorseTamed(true);
      return true;
   }

   public void moveEntityWithHeading(float f, float f1) {
      if (this.isBeingRidden() && this.canBeSteered() && this.isHorseSaddled()) {
         EntityLivingBase entityliving = (EntityLivingBase)this.getControllingPassenger();
         this.rotationYaw = entityliving.rotationYaw;
         this.prevRotationYaw = this.rotationYaw;
         this.rotationPitch = entityliving.rotationPitch * 0.5F;
         this.setRotation(this.rotationYaw, this.rotationPitch);
         this.renderYawOffset = this.rotationYaw;
         this.rotationYawHead = this.renderYawOffset;
         f = entityliving.moveStrafing * 0.5F;
         f1 = entityliving.moveForward;
         if (f1 <= 0.0F) {
            f1 *= 0.25F;
            this.gallopTime = 0;
         }

         if (this.onGround && this.jumpPower == 0.0F && this.isRearing() && !this.allowStandSliding) {
            f = 0.0F;
            f1 = 0.0F;
         }

         if (this.jumpPower > 0.0F && !this.isHorseJumping() && this.onGround) {
            this.motionY = this.getHorseJumpStrength() * (double)this.jumpPower;
            if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
               this.motionY += (double)((float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
            }

            this.setHorseJumping(true);
            this.isAirBorne = true;
            if (f1 > 0.0F) {
               float f2 = MathHelper.sin(this.rotationYaw * 0.017453292F);
               float f3 = MathHelper.cos(this.rotationYaw * 0.017453292F);
               this.motionX += (double)(-0.4F * f2 * this.jumpPower);
               this.motionZ += (double)(0.4F * f3 * this.jumpPower);
               this.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.4F, 1.0F);
            }

            this.jumpPower = 0.0F;
         }

         this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
         if (this.canPassengerSteer()) {
            this.setAIMoveSpeed((float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            super.moveEntityWithHeading(f, f1);
         } else if (entityliving instanceof EntityPlayer) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
         }

         if (this.onGround) {
            this.jumpPower = 0.0F;
            this.setHorseJumping(false);
         }

         this.prevLimbSwingAmount = this.limbSwingAmount;
         double d0 = this.posX - this.prevPosX;
         double d1 = this.posZ - this.prevPosZ;
         float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
         if (f4 > 1.0F) {
            f4 = 1.0F;
         }

         this.limbSwingAmount += (f4 - this.limbSwingAmount) * 0.4F;
         this.limbSwing += this.limbSwingAmount;
      } else {
         this.jumpMovementFactor = 0.02F;
         super.moveEntityWithHeading(f, f1);
      }

   }

   public static void registerFixesHorse(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "EntityHorse");
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("EntityHorse", new String[]{"Items"}));
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackData("EntityHorse", new String[]{"ArmorItem", "SaddleItem"}));
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setBoolean("EatingHaystack", this.isEatingHaystack());
      nbttagcompound.setBoolean("ChestedHorse", this.isChested());
      nbttagcompound.setBoolean("HasReproduced", this.getHasReproduced());
      nbttagcompound.setBoolean("Bred", this.isBreeding());
      nbttagcompound.setInteger("Type", this.getType().getOrdinal());
      nbttagcompound.setInteger("Variant", this.getHorseVariant());
      nbttagcompound.setInteger("Temper", this.getTemper());
      nbttagcompound.setBoolean("Tame", this.isTame());
      nbttagcompound.setBoolean("SkeletonTrap", this.isSkeletonTrap());
      nbttagcompound.setInteger("SkeletonTrapTime", this.skeletonTrapTime);
      if (this.getOwnerUniqueId() != null) {
         nbttagcompound.setString("OwnerUUID", this.getOwnerUniqueId().toString());
      }

      nbttagcompound.setInteger("Bukkit.MaxDomestication", this.maxDomestication);
      if (this.isChested()) {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 2; i < this.horseChest.getSizeInventory(); ++i) {
            ItemStack itemstack = this.horseChest.getStackInSlot(i);
            if (itemstack != null) {
               NBTTagCompound nbttagcompound1 = new NBTTagCompound();
               nbttagcompound1.setByte("Slot", (byte)i);
               itemstack.writeToNBT(nbttagcompound1);
               nbttaglist.appendTag(nbttagcompound1);
            }
         }

         nbttagcompound.setTag("Items", nbttaglist);
      }

      if (this.horseChest.getStackInSlot(1) != null) {
         nbttagcompound.setTag("ArmorItem", this.horseChest.getStackInSlot(1).writeToNBT(new NBTTagCompound()));
      }

      if (this.horseChest.getStackInSlot(0) != null) {
         nbttagcompound.setTag("SaddleItem", this.horseChest.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
      }

   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.setEatingHaystack(nbttagcompound.getBoolean("EatingHaystack"));
      this.setBreeding(nbttagcompound.getBoolean("Bred"));
      this.setChested(nbttagcompound.getBoolean("ChestedHorse"));
      this.setHasReproduced(nbttagcompound.getBoolean("HasReproduced"));
      this.setType(HorseType.getArmorType(nbttagcompound.getInteger("Type")));
      this.setHorseVariant(nbttagcompound.getInteger("Variant"));
      this.setTemper(nbttagcompound.getInteger("Temper"));
      this.setHorseTamed(nbttagcompound.getBoolean("Tame"));
      this.setSkeletonTrap(nbttagcompound.getBoolean("SkeletonTrap"));
      this.skeletonTrapTime = nbttagcompound.getInteger("SkeletonTrapTime");
      String s;
      if (nbttagcompound.hasKey("OwnerUUID", 8)) {
         s = nbttagcompound.getString("OwnerUUID");
      } else {
         String s1 = nbttagcompound.getString("Owner");
         s = PreYggdrasilConverter.a(this.h(), s1);
      }

      if (!s.isEmpty()) {
         this.setOwnerUniqueId(UUID.fromString(s));
      }

      if (nbttagcompound.hasKey("Bukkit.MaxDomestication")) {
         this.maxDomestication = nbttagcompound.getInteger("Bukkit.MaxDomestication");
      }

      IAttributeInstance attributeinstance = this.getAttributeMap().getAttributeInstanceByName("Speed");
      if (attributeinstance != null) {
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(attributeinstance.getBaseValue() * 0.25D);
      }

      if (this.isChested()) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
         this.initHorseChest();

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;
            if (j >= 2 && j < this.horseChest.getSizeInventory()) {
               this.horseChest.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
            }
         }
      }

      if (nbttagcompound.hasKey("ArmorItem", 10)) {
         ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("ArmorItem"));
         if (itemstack != null && HorseArmorType.isHorseArmor(itemstack.getItem())) {
            this.horseChest.setInventorySlotContents(1, itemstack);
         }
      }

      if (nbttagcompound.hasKey("SaddleItem", 10)) {
         ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("SaddleItem"));
         if (itemstack != null && itemstack.getItem() == Items.SADDLE) {
            this.horseChest.setInventorySlotContents(0, itemstack);
         }
      }

      this.updateHorseSlots();
   }

   public boolean canMateWith(EntityAnimal entityanimal) {
      if (entityanimal == this) {
         return false;
      } else if (entityanimal.getClass() != this.getClass()) {
         return false;
      } else {
         EntityHorse entityhorse = (EntityHorse)entityanimal;
         if (this.canMate() && entityhorse.canMate()) {
            HorseType enumhorsetype = this.getType();
            HorseType enumhorsetype1 = entityhorse.getType();
            return enumhorsetype == enumhorsetype1 || enumhorsetype == HorseType.HORSE && enumhorsetype1 == HorseType.DONKEY || enumhorsetype == HorseType.DONKEY && enumhorsetype1 == HorseType.HORSE;
         } else {
            return false;
         }
      }
   }

   public EntityAgeable createChild(EntityAgeable entityageable) {
      EntityHorse entityhorse = (EntityHorse)entityageable;
      EntityHorse entityhorse1 = new EntityHorse(this.world);
      HorseType enumhorsetype = this.getType();
      HorseType enumhorsetype1 = entityhorse.getType();
      HorseType enumhorsetype2 = HorseType.HORSE;
      if (enumhorsetype == enumhorsetype1) {
         enumhorsetype2 = enumhorsetype;
      } else if (enumhorsetype == HorseType.HORSE && enumhorsetype1 == HorseType.DONKEY || enumhorsetype == HorseType.DONKEY && enumhorsetype1 == HorseType.HORSE) {
         enumhorsetype2 = HorseType.MULE;
      }

      if (enumhorsetype2 == HorseType.HORSE) {
         int i = this.rand.nextInt(9);
         int j;
         if (i < 4) {
            j = this.getHorseVariant() & 255;
         } else if (i < 8) {
            j = entityhorse.getHorseVariant() & 255;
         } else {
            j = this.rand.nextInt(7);
         }

         int k = this.rand.nextInt(5);
         if (k < 2) {
            j = j | this.getHorseVariant() & '\uff00';
         } else if (k < 4) {
            j = j | entityhorse.getHorseVariant() & '\uff00';
         } else {
            j = j | this.rand.nextInt(5) << 8 & '\uff00';
         }

         entityhorse1.setHorseVariant(j);
      }

      entityhorse1.setType(enumhorsetype2);
      double d0 = this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() + entityageable.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() + (double)this.getModifiedMaxHealth();
      entityhorse1.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(d0 / 3.0D);
      double d1 = this.getEntityAttribute(JUMP_STRENGTH).getBaseValue() + entityageable.getEntityAttribute(JUMP_STRENGTH).getBaseValue() + this.getModifiedJumpStrength();
      entityhorse1.getEntityAttribute(JUMP_STRENGTH).setBaseValue(d1 / 3.0D);
      double d2 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() + entityageable.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() + this.getModifiedMovementSpeed();
      entityhorse1.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(d2 / 3.0D);
      return entityhorse1;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficultydamagescaler, @Nullable IEntityLivingData groupdataentity) {
      Object object = super.onInitialSpawn(difficultydamagescaler, groupdataentity);
      int i = 0;
      HorseType enumhorsetype;
      if (object instanceof EntityHorse.GroupData) {
         enumhorsetype = ((EntityHorse.GroupData)object).horseType;
         i = ((EntityHorse.GroupData)object).horseVariant & 255 | this.rand.nextInt(5) << 8;
      } else {
         if (this.rand.nextInt(10) == 0) {
            enumhorsetype = HorseType.DONKEY;
         } else {
            int j = this.rand.nextInt(7);
            int k = this.rand.nextInt(5);
            enumhorsetype = HorseType.HORSE;
            i = j | k << 8;
         }

         object = new EntityHorse.GroupData(enumhorsetype, i);
      }

      this.setType(enumhorsetype);
      this.setHorseVariant(i);
      if (this.rand.nextInt(5) == 0) {
         this.setGrowingAge(-24000);
      }

      if (enumhorsetype.isUndead()) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
      } else {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)this.getModifiedMaxHealth());
         if (enumhorsetype == HorseType.HORSE) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getModifiedMovementSpeed());
         } else {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.17499999701976776D);
         }
      }

      if (enumhorsetype.hasMuleEars()) {
         this.getEntityAttribute(JUMP_STRENGTH).setBaseValue(0.5D);
      } else {
         this.getEntityAttribute(JUMP_STRENGTH).setBaseValue(this.getModifiedJumpStrength());
      }

      this.setHealth(this.getMaxHealth());
      return (IEntityLivingData)object;
   }

   public boolean canBeSteered() {
      Entity entity = this.getControllingPassenger();
      return entity instanceof EntityLivingBase;
   }

   public boolean canJump() {
      return this.isHorseSaddled();
   }

   public void handleStartJump(int i) {
      float power;
      if (i >= 90) {
         power = 1.0F;
      } else {
         power = 0.4F + 0.4F * (float)i / 90.0F;
      }

      HorseJumpEvent event = CraftEventFactory.callHorseJumpEvent(this, power);
      if (!event.isCancelled()) {
         this.allowStandSliding = true;
         this.makeHorseRear();
      }
   }

   public void handleStopJump() {
   }

   public void updatePassenger(Entity entity) {
      super.updatePassenger(entity);
      if (entity instanceof EntityLiving) {
         EntityLiving entityinsentient = (EntityLiving)entity;
         this.renderYawOffset = entityinsentient.renderYawOffset;
      }

      if (this.prevRearingAmount > 0.0F) {
         float f = MathHelper.sin(this.renderYawOffset * 0.017453292F);
         float f1 = MathHelper.cos(this.renderYawOffset * 0.017453292F);
         float f2 = 0.7F * this.prevRearingAmount;
         float f3 = 0.15F * this.prevRearingAmount;
         entity.setPosition(this.posX + (double)(f2 * f), this.posY + this.getMountedYOffset() + entity.getYOffset() + (double)f3, this.posZ - (double)(f2 * f1));
         if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase)entity).renderYawOffset = this.renderYawOffset;
         }
      }

   }

   public double getMountedYOffset() {
      double d0 = super.getMountedYOffset();
      if (this.getType() == HorseType.SKELETON) {
         d0 -= 0.1875D;
      } else if (this.getType() == HorseType.DONKEY) {
         d0 -= 0.25D;
      }

      return d0;
   }

   private float getModifiedMaxHealth() {
      return 15.0F + (float)this.rand.nextInt(8) + (float)this.rand.nextInt(9);
   }

   private double getModifiedJumpStrength() {
      return 0.4000000059604645D + this.rand.nextDouble() * 0.2D + this.rand.nextDouble() * 0.2D + this.rand.nextDouble() * 0.2D;
   }

   private double getModifiedMovementSpeed() {
      return (0.44999998807907104D + this.rand.nextDouble() * 0.3D + this.rand.nextDouble() * 0.3D + this.rand.nextDouble() * 0.3D) * 0.25D;
   }

   public boolean isSkeletonTrap() {
      return this.skeletonTrap;
   }

   public void setSkeletonTrap(boolean flag) {
      if (flag != this.skeletonTrap) {
         this.skeletonTrap = flag;
         if (flag) {
            this.tasks.addTask(1, this.skeletonTrapAI);
         } else {
            this.tasks.removeTask(this.skeletonTrapAI);
         }
      }

   }

   public boolean isOnLadder() {
      return false;
   }

   public float getEyeHeight() {
      return this.height;
   }

   public boolean replaceItemInInventory(int i, @Nullable ItemStack itemstack) {
      if (i == 499 && this.getType().canBeChested()) {
         if (itemstack == null && this.isChested()) {
            this.setChested(false);
            this.initHorseChest();
            return true;
         }

         if (itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !this.isChested()) {
            this.setChested(true);
            this.initHorseChest();
            return true;
         }
      }

      int j = i - 400;
      if (j >= 0 && j < 2 && j < this.horseChest.getSizeInventory()) {
         if (j == 0 && itemstack != null && itemstack.getItem() != Items.SADDLE) {
            return false;
         } else if (j != 1 || (itemstack == null || HorseArmorType.isHorseArmor(itemstack.getItem())) && this.getType().isHorse()) {
            this.horseChest.setInventorySlotContents(j, itemstack);
            this.updateHorseSlots();
            return true;
         } else {
            return false;
         }
      } else {
         int k = i - 500 + 2;
         if (k >= 2 && k < this.horseChest.getSizeInventory()) {
            this.horseChest.setInventorySlotContents(k, itemstack);
            return true;
         } else {
            return false;
         }
      }
   }

   @Nullable
   public Entity getControllingPassenger() {
      return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return this.getType().isUndead() ? EnumCreatureAttribute.UNDEAD : EnumCreatureAttribute.UNDEFINED;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return this.getType().getLootTable();
   }

   public static class GroupData implements IEntityLivingData {
      public HorseType horseType;
      public int horseVariant;

      public GroupData(HorseType enumhorsetype, int i) {
         this.horseType = enumhorsetype;
         this.horseVariant = i;
      }
   }
}
