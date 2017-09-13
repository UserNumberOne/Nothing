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
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof EntityHorse && ((EntityHorse)var1).isBreeding();
      }

      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
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

   public EntityHorse(World var1) {
      super(var1);
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

   public void setType(HorseType var1) {
      this.dataManager.set(HORSE_TYPE, Integer.valueOf(var1.getOrdinal()));
      this.resetTexturePrefix();
   }

   public HorseType getType() {
      return HorseType.getArmorType(((Integer)this.dataManager.get(HORSE_TYPE)).intValue());
   }

   public void setHorseVariant(int var1) {
      this.dataManager.set(HORSE_VARIANT, Integer.valueOf(var1));
      this.resetTexturePrefix();
   }

   public int getHorseVariant() {
      return ((Integer)this.dataManager.get(HORSE_VARIANT)).intValue();
   }

   public String getName() {
      return this.hasCustomName() ? this.getCustomNameTag() : this.getType().getDefaultName().getUnformattedText();
   }

   private boolean getHorseWatchableBoolean(int var1) {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & var1) != 0;
   }

   private void setHorseWatchableBoolean(int var1, boolean var2) {
      byte var3 = ((Byte)this.dataManager.get(STATUS)).byteValue();
      if (var2) {
         this.dataManager.set(STATUS, Byte.valueOf((byte)(var3 | var1)));
      } else {
         this.dataManager.set(STATUS, Byte.valueOf((byte)(var3 & ~var1)));
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

   public void setOwnerUniqueId(@Nullable UUID var1) {
      this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(var1));
   }

   public float getHorseSize() {
      return 0.5F;
   }

   public void setScaleForAge(boolean var1) {
      if (var1) {
         this.setScale(this.getHorseSize());
      } else {
         this.setScale(1.0F);
      }

   }

   public boolean isHorseJumping() {
      return this.horseJumping;
   }

   public void setHorseTamed(boolean var1) {
      this.setHorseWatchableBoolean(2, var1);
   }

   public void setHorseJumping(boolean var1) {
      this.horseJumping = var1;
   }

   public boolean canBeLeashedTo(EntityPlayer var1) {
      return !this.getType().isUndead() && super.canBeLeashedTo(var1);
   }

   protected void onLeashDistance(float var1) {
      if (var1 > 6.0F && this.isEatingHaystack()) {
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

   public void setHorseArmorStack(ItemStack var1) {
      HorseArmorType var2 = HorseArmorType.getByItemStack(var1);
      this.dataManager.set(HORSE_ARMOR, Integer.valueOf(var2.getOrdinal()));
      this.resetTexturePrefix();
      if (!this.world.isRemote) {
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
         int var3 = var2.getProtection();
         if (var3 != 0) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier((new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)var3, 0)).setSaved(false));
         }
      }

   }

   public void setBreeding(boolean var1) {
      this.setHorseWatchableBoolean(16, var1);
   }

   public void setChested(boolean var1) {
      this.setHorseWatchableBoolean(8, var1);
   }

   public void setHasReproduced(boolean var1) {
      this.hasReproduced = var1;
   }

   public void setHorseSaddled(boolean var1) {
      this.setHorseWatchableBoolean(4, var1);
   }

   public int getTemper() {
      return this.temper;
   }

   public void setTemper(int var1) {
      this.temper = var1;
   }

   public int increaseTemper(int var1) {
      int var2 = MathHelper.clamp(this.getTemper() + var1, 0, this.getMaxTemper());
      this.setTemper(var2);
      return var2;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      Entity var3 = var1.getEntity();
      return this.isBeingRidden() && var3 != null && this.isRidingOrBeingRiddenBy(var3) ? false : super.attackEntityFrom(var1, var2);
   }

   public boolean canBePushed() {
      return !this.isBeingRidden();
   }

   public boolean prepareChunkForSpawn() {
      int var1 = MathHelper.floor(this.posX);
      int var2 = MathHelper.floor(this.posZ);
      this.world.getBiome(new BlockPos(var1, 0, var2));
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

   public void fall(float var1, float var2) {
      if (var1 > 1.0F) {
         this.playSound(SoundEvents.ENTITY_HORSE_LAND, 0.4F, 1.0F);
      }

      int var3 = MathHelper.ceil((var1 * 0.5F - 3.0F) * var2);
      if (var3 > 0) {
         this.attackEntityFrom(DamageSource.fall, (float)var3);
         if (this.isBeingRidden()) {
            for(Entity var5 : this.getRecursivePassengers()) {
               var5.attackEntityFrom(DamageSource.fall, (float)var3);
            }
         }

         IBlockState var7 = this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.2D - (double)this.prevRotationYaw, this.posZ));
         Block var8 = var7.getBlock();
         if (var7.getMaterial() != Material.AIR && !this.isSilent()) {
            SoundType var6 = var8.getSoundType();
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, var6.getStepSound(), this.getSoundCategory(), var6.getVolume() * 0.5F, var6.getPitch() * 0.75F);
         }
      }

   }

   private int getChestSize() {
      HorseType var1 = this.getType();
      return this.isChested() && var1.canBeChested() ? 17 : 2;
   }

   public void initHorseChest() {
      AnimalChest var1 = this.horseChest;
      this.horseChest = new AnimalChest("HorseChest", this.getChestSize(), this);
      this.horseChest.setCustomName(this.getName());
      if (var1 != null) {
         var1.removeInventoryChangeListener(this);
         int var2 = Math.min(var1.getSizeInventory(), this.horseChest.getSizeInventory());

         for(int var3 = 0; var3 < var2; ++var3) {
            ItemStack var4 = var1.getStackInSlot(var3);
            if (var4 != null) {
               this.horseChest.setInventorySlotContents(var3, var4.copy());
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

   public void onInventoryChanged(InventoryBasic var1) {
      HorseArmorType var2 = this.getHorseArmorType();
      boolean var3 = this.isHorseSaddled();
      this.updateHorseSlots();
      if (this.ticksExisted > 20) {
         if (var2 == HorseArmorType.NONE && var2 != this.getHorseArmorType()) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
         } else if (var2 != this.getHorseArmorType()) {
            this.playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
         }

         if (!var3 && this.isHorseSaddled()) {
            this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, 0.5F, 1.0F);
         }
      }

   }

   public boolean getCanSpawnHere() {
      this.prepareChunkForSpawn();
      return super.getCanSpawnHere();
   }

   protected EntityHorse getClosestHorse(Entity var1, double var2) {
      double var4 = Double.MAX_VALUE;
      Entity var6 = null;

      for(Entity var9 : this.world.getEntitiesInAABBexcluding(var1, var1.getEntityBoundingBox().addCoord(var2, var2, var2), IS_HORSE_BREEDING)) {
         double var10 = var9.getDistanceSq(var1.posX, var1.posY, var1.posZ);
         if (var10 < var4) {
            var6 = var9;
            var4 = var10;
         }
      }

      return (EntityHorse)var6;
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
      HorseType var1 = this.getType();
      return var1.isUndead() ? null : (var1.hasMuleEars() ? SoundEvents.ENTITY_DONKEY_ANGRY : SoundEvents.ENTITY_HORSE_ANGRY);
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      SoundType var3 = var2.getSoundType();
      if (this.world.getBlockState(var1.up()).getBlock() == Blocks.SNOW_LAYER) {
         var3 = Blocks.SNOW_LAYER.getSoundType();
      }

      if (!var2.getDefaultState().getMaterial().isLiquid()) {
         HorseType var4 = this.getType();
         if (this.isBeingRidden() && !var4.hasMuleEars()) {
            ++this.gallopTime;
            if (this.gallopTime > 5 && this.gallopTime % 3 == 0) {
               this.playSound(SoundEvents.ENTITY_HORSE_GALLOP, var3.getVolume() * 0.15F, var3.getPitch());
               if (var4 == HorseType.HORSE && this.rand.nextInt(10) == 0) {
                  this.playSound(SoundEvents.ENTITY_HORSE_BREATHE, var3.getVolume() * 0.6F, var3.getPitch());
               }
            } else if (this.gallopTime <= 5) {
               this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, var3.getVolume() * 0.15F, var3.getPitch());
            }
         } else if (var3 == SoundType.WOOD) {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP_WOOD, var3.getVolume() * 0.15F, var3.getPitch());
         } else {
            this.playSound(SoundEvents.ENTITY_HORSE_STEP, var3.getVolume() * 0.15F, var3.getPitch());
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

   public void openGUI(EntityPlayer var1) {
      if (!this.world.isRemote && (!this.isBeingRidden() || this.isPassenger(var1)) && this.isTame()) {
         this.horseChest.setCustomName(this.getName());
         var1.openGuiHorseInventory(this, this.horseChest);
      }

   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (var3 != null && var3.getItem() == Items.SPAWN_EGG) {
         return super.processInteract(var1, var2, var3);
      } else if (!this.isTame() && this.getType().isUndead()) {
         return false;
      } else if (this.isTame() && this.isAdultHorse() && var1.isSneaking()) {
         this.openGUI(var1);
         return true;
      } else if (this.isRidable() && this.isBeingRidden()) {
         return super.processInteract(var1, var2, var3);
      } else {
         if (var3 != null) {
            if (this.getType().isHorse()) {
               HorseArmorType var4 = HorseArmorType.getByItemStack(var3);
               if (var4 != HorseArmorType.NONE) {
                  if (!this.isTame()) {
                     this.makeHorseRearWithSound();
                     return true;
                  }

                  this.openGUI(var1);
                  return true;
               }
            }

            boolean var8 = false;
            if (!this.getType().isUndead()) {
               float var5 = 0.0F;
               short var6 = 0;
               byte var7 = 0;
               if (var3.getItem() == Items.WHEAT) {
                  var5 = 2.0F;
                  var6 = 20;
                  var7 = 3;
               } else if (var3.getItem() == Items.SUGAR) {
                  var5 = 1.0F;
                  var6 = 30;
                  var7 = 3;
               } else if (Block.getBlockFromItem(var3.getItem()) == Blocks.HAY_BLOCK) {
                  var5 = 20.0F;
                  var6 = 180;
               } else if (var3.getItem() == Items.APPLE) {
                  var5 = 3.0F;
                  var6 = 60;
                  var7 = 3;
               } else if (var3.getItem() == Items.GOLDEN_CARROT) {
                  var5 = 4.0F;
                  var6 = 60;
                  var7 = 5;
                  if (this.isTame() && this.getGrowingAge() == 0) {
                     var8 = true;
                     this.setInLove(var1);
                  }
               } else if (var3.getItem() == Items.GOLDEN_APPLE) {
                  var5 = 10.0F;
                  var6 = 240;
                  var7 = 10;
                  if (this.isTame() && this.getGrowingAge() == 0 && !this.isInLove()) {
                     var8 = true;
                     this.setInLove(var1);
                  }
               }

               if (this.getHealth() < this.getMaxHealth() && var5 > 0.0F) {
                  this.heal(var5, RegainReason.EATING);
                  var8 = true;
               }

               if (!this.isAdultHorse() && var6 > 0) {
                  if (!this.world.isRemote) {
                     this.addGrowth(var6);
                  }

                  var8 = true;
               }

               if (var7 > 0 && (var8 || !this.isTame()) && this.getTemper() < this.getMaxTemper()) {
                  var8 = true;
                  if (!this.world.isRemote) {
                     this.increaseTemper(var7);
                  }
               }

               if (var8) {
                  this.eatingHorse();
               }
            }

            if (!this.isTame() && !var8) {
               if (var3.interactWithEntity(var1, this, var2)) {
                  return true;
               }

               this.makeHorseRearWithSound();
               return true;
            }

            if (!var8 && this.getType().canBeChested() && !this.isChested() && var3.getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
               this.setChested(true);
               this.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
               var8 = true;
               this.initHorseChest();
            }

            if (!var8 && this.isRidable() && !this.isHorseSaddled() && var3.getItem() == Items.SADDLE) {
               this.openGUI(var1);
               return true;
            }

            if (var8) {
               if (!var1.capabilities.isCreativeMode) {
                  --var3.stackSize;
               }

               return true;
            }
         }

         if (this.isRidable() && !this.isBeingRidden()) {
            if (var3 != null && var3.interactWithEntity(var1, this, var2)) {
               return true;
            } else {
               this.mountTo(var1);
               return true;
            }
         } else {
            return super.processInteract(var1, var2, var3);
         }
      }
   }

   private void mountTo(EntityPlayer var1) {
      var1.rotationYaw = this.rotationYaw;
      var1.rotationPitch = this.rotationPitch;
      this.setEatingHaystack(false);
      this.setRearing(false);
      if (!this.world.isRemote) {
         var1.startRiding(this);
      }

   }

   protected boolean isMovementBlocked() {
      return this.isBeingRidden() && this.isHorseSaddled() ? true : this.isEatingHaystack() || this.isRearing();
   }

   public boolean isBreedingItem(@Nullable ItemStack var1) {
      return false;
   }

   private void moveTail() {
      this.tailCounter = 1;
   }

   public void onDeath(DamageSource var1) {
      if (!this.world.isRemote) {
         this.dropChestItems();
      }

      super.onDeath(var1);
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
            EntityHorse var1 = this.getClosestHorse(this, 16.0D);
            if (var1 != null && this.getDistanceSqToEntity(var1) > 4.0D) {
               this.navigator.getPathToEntityLiving(var1);
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

   public void setEatingHaystack(boolean var1) {
      this.setHorseWatchableBoolean(32, var1);
   }

   public void setRearing(boolean var1) {
      if (var1) {
         this.setEatingHaystack(false);
      }

      this.setHorseWatchableBoolean(64, var1);
   }

   private void makeHorseRear() {
      if (this.canPassengerSteer()) {
         this.jumpRearingCounter = 1;
         this.setRearing(true);
      }

   }

   public void makeHorseRearWithSound() {
      this.makeHorseRear();
      SoundEvent var1 = this.getAngrySound();
      if (var1 != null) {
         this.playSound(var1, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public void dropChestItems() {
      this.dropItemsInChest(this, this.horseChest);
      this.dropChests();
   }

   private void dropItemsInChest(Entity var1, AnimalChest var2) {
      if (var2 != null && !this.world.isRemote) {
         for(int var3 = 0; var3 < var2.getSizeInventory(); ++var3) {
            ItemStack var4 = var2.getStackInSlot(var3);
            if (var4 != null) {
               this.entityDropItem(var4, 0.0F);
            }
         }
      }

   }

   public boolean setTamedBy(EntityPlayer var1) {
      this.setOwnerUniqueId(var1.getUniqueID());
      this.setHorseTamed(true);
      return true;
   }

   public void moveEntityWithHeading(float var1, float var2) {
      if (this.isBeingRidden() && this.canBeSteered() && this.isHorseSaddled()) {
         EntityLivingBase var3 = (EntityLivingBase)this.getControllingPassenger();
         this.rotationYaw = var3.rotationYaw;
         this.prevRotationYaw = this.rotationYaw;
         this.rotationPitch = var3.rotationPitch * 0.5F;
         this.setRotation(this.rotationYaw, this.rotationPitch);
         this.renderYawOffset = this.rotationYaw;
         this.rotationYawHead = this.renderYawOffset;
         var1 = var3.moveStrafing * 0.5F;
         var2 = var3.moveForward;
         if (var2 <= 0.0F) {
            var2 *= 0.25F;
            this.gallopTime = 0;
         }

         if (this.onGround && this.jumpPower == 0.0F && this.isRearing() && !this.allowStandSliding) {
            var1 = 0.0F;
            var2 = 0.0F;
         }

         if (this.jumpPower > 0.0F && !this.isHorseJumping() && this.onGround) {
            this.motionY = this.getHorseJumpStrength() * (double)this.jumpPower;
            if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
               this.motionY += (double)((float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
            }

            this.setHorseJumping(true);
            this.isAirBorne = true;
            if (var2 > 0.0F) {
               float var4 = MathHelper.sin(this.rotationYaw * 0.017453292F);
               float var5 = MathHelper.cos(this.rotationYaw * 0.017453292F);
               this.motionX += (double)(-0.4F * var4 * this.jumpPower);
               this.motionZ += (double)(0.4F * var5 * this.jumpPower);
               this.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.4F, 1.0F);
            }

            this.jumpPower = 0.0F;
         }

         this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
         if (this.canPassengerSteer()) {
            this.setAIMoveSpeed((float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            super.moveEntityWithHeading(var1, var2);
         } else if (var3 instanceof EntityPlayer) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
         }

         if (this.onGround) {
            this.jumpPower = 0.0F;
            this.setHorseJumping(false);
         }

         this.prevLimbSwingAmount = this.limbSwingAmount;
         double var6 = this.posX - this.prevPosX;
         double var8 = this.posZ - this.prevPosZ;
         float var10 = MathHelper.sqrt(var6 * var6 + var8 * var8) * 4.0F;
         if (var10 > 1.0F) {
            var10 = 1.0F;
         }

         this.limbSwingAmount += (var10 - this.limbSwingAmount) * 0.4F;
         this.limbSwing += this.limbSwingAmount;
      } else {
         this.jumpMovementFactor = 0.02F;
         super.moveEntityWithHeading(var1, var2);
      }

   }

   public static void registerFixesHorse(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "EntityHorse");
      var0.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("EntityHorse", new String[]{"Items"}));
      var0.registerWalker(FixTypes.ENTITY, new ItemStackData("EntityHorse", new String[]{"ArmorItem", "SaddleItem"}));
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setBoolean("EatingHaystack", this.isEatingHaystack());
      var1.setBoolean("ChestedHorse", this.isChested());
      var1.setBoolean("HasReproduced", this.getHasReproduced());
      var1.setBoolean("Bred", this.isBreeding());
      var1.setInteger("Type", this.getType().getOrdinal());
      var1.setInteger("Variant", this.getHorseVariant());
      var1.setInteger("Temper", this.getTemper());
      var1.setBoolean("Tame", this.isTame());
      var1.setBoolean("SkeletonTrap", this.isSkeletonTrap());
      var1.setInteger("SkeletonTrapTime", this.skeletonTrapTime);
      if (this.getOwnerUniqueId() != null) {
         var1.setString("OwnerUUID", this.getOwnerUniqueId().toString());
      }

      var1.setInteger("Bukkit.MaxDomestication", this.maxDomestication);
      if (this.isChested()) {
         NBTTagList var2 = new NBTTagList();

         for(int var3 = 2; var3 < this.horseChest.getSizeInventory(); ++var3) {
            ItemStack var4 = this.horseChest.getStackInSlot(var3);
            if (var4 != null) {
               NBTTagCompound var5 = new NBTTagCompound();
               var5.setByte("Slot", (byte)var3);
               var4.writeToNBT(var5);
               var2.appendTag(var5);
            }
         }

         var1.setTag("Items", var2);
      }

      if (this.horseChest.getStackInSlot(1) != null) {
         var1.setTag("ArmorItem", this.horseChest.getStackInSlot(1).writeToNBT(new NBTTagCompound()));
      }

      if (this.horseChest.getStackInSlot(0) != null) {
         var1.setTag("SaddleItem", this.horseChest.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setEatingHaystack(var1.getBoolean("EatingHaystack"));
      this.setBreeding(var1.getBoolean("Bred"));
      this.setChested(var1.getBoolean("ChestedHorse"));
      this.setHasReproduced(var1.getBoolean("HasReproduced"));
      this.setType(HorseType.getArmorType(var1.getInteger("Type")));
      this.setHorseVariant(var1.getInteger("Variant"));
      this.setTemper(var1.getInteger("Temper"));
      this.setHorseTamed(var1.getBoolean("Tame"));
      this.setSkeletonTrap(var1.getBoolean("SkeletonTrap"));
      this.skeletonTrapTime = var1.getInteger("SkeletonTrapTime");
      String var2;
      if (var1.hasKey("OwnerUUID", 8)) {
         var2 = var1.getString("OwnerUUID");
      } else {
         String var3 = var1.getString("Owner");
         var2 = PreYggdrasilConverter.a(this.h(), var3);
      }

      if (!var2.isEmpty()) {
         this.setOwnerUniqueId(UUID.fromString(var2));
      }

      if (var1.hasKey("Bukkit.MaxDomestication")) {
         this.maxDomestication = var1.getInteger("Bukkit.MaxDomestication");
      }

      IAttributeInstance var8 = this.getAttributeMap().getAttributeInstanceByName("Speed");
      if (var8 != null) {
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(var8.getBaseValue() * 0.25D);
      }

      if (this.isChested()) {
         NBTTagList var4 = var1.getTagList("Items", 10);
         this.initHorseChest();

         for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
            NBTTagCompound var6 = var4.getCompoundTagAt(var5);
            int var7 = var6.getByte("Slot") & 255;
            if (var7 >= 2 && var7 < this.horseChest.getSizeInventory()) {
               this.horseChest.setInventorySlotContents(var7, ItemStack.loadItemStackFromNBT(var6));
            }
         }
      }

      if (var1.hasKey("ArmorItem", 10)) {
         ItemStack var9 = ItemStack.loadItemStackFromNBT(var1.getCompoundTag("ArmorItem"));
         if (var9 != null && HorseArmorType.isHorseArmor(var9.getItem())) {
            this.horseChest.setInventorySlotContents(1, var9);
         }
      }

      if (var1.hasKey("SaddleItem", 10)) {
         ItemStack var10 = ItemStack.loadItemStackFromNBT(var1.getCompoundTag("SaddleItem"));
         if (var10 != null && var10.getItem() == Items.SADDLE) {
            this.horseChest.setInventorySlotContents(0, var10);
         }
      }

      this.updateHorseSlots();
   }

   public boolean canMateWith(EntityAnimal var1) {
      if (var1 == this) {
         return false;
      } else if (var1.getClass() != this.getClass()) {
         return false;
      } else {
         EntityHorse var2 = (EntityHorse)var1;
         if (this.canMate() && var2.canMate()) {
            HorseType var3 = this.getType();
            HorseType var4 = var2.getType();
            return var3 == var4 || var3 == HorseType.HORSE && var4 == HorseType.DONKEY || var3 == HorseType.DONKEY && var4 == HorseType.HORSE;
         } else {
            return false;
         }
      }
   }

   public EntityAgeable createChild(EntityAgeable var1) {
      EntityHorse var2 = (EntityHorse)var1;
      EntityHorse var3 = new EntityHorse(this.world);
      HorseType var4 = this.getType();
      HorseType var5 = var2.getType();
      HorseType var6 = HorseType.HORSE;
      if (var4 == var5) {
         var6 = var4;
      } else if (var4 == HorseType.HORSE && var5 == HorseType.DONKEY || var4 == HorseType.DONKEY && var5 == HorseType.HORSE) {
         var6 = HorseType.MULE;
      }

      if (var6 == HorseType.HORSE) {
         int var7 = this.rand.nextInt(9);
         int var8;
         if (var7 < 4) {
            var8 = this.getHorseVariant() & 255;
         } else if (var7 < 8) {
            var8 = var2.getHorseVariant() & 255;
         } else {
            var8 = this.rand.nextInt(7);
         }

         int var9 = this.rand.nextInt(5);
         if (var9 < 2) {
            var8 = var8 | this.getHorseVariant() & '\uff00';
         } else if (var9 < 4) {
            var8 = var8 | var2.getHorseVariant() & '\uff00';
         } else {
            var8 = var8 | this.rand.nextInt(5) << 8 & '\uff00';
         }

         var3.setHorseVariant(var8);
      }

      var3.setType(var6);
      double var10 = this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() + var1.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() + (double)this.getModifiedMaxHealth();
      var3.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(var10 / 3.0D);
      double var12 = this.getEntityAttribute(JUMP_STRENGTH).getBaseValue() + var1.getEntityAttribute(JUMP_STRENGTH).getBaseValue() + this.getModifiedJumpStrength();
      var3.getEntityAttribute(JUMP_STRENGTH).setBaseValue(var12 / 3.0D);
      double var14 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() + var1.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() + this.getModifiedMovementSpeed();
      var3.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(var14 / 3.0D);
      return var3;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      Object var3 = super.onInitialSpawn(var1, var2);
      int var4 = 0;
      HorseType var5;
      if (var3 instanceof EntityHorse.GroupData) {
         var5 = ((EntityHorse.GroupData)var3).horseType;
         var4 = ((EntityHorse.GroupData)var3).horseVariant & 255 | this.rand.nextInt(5) << 8;
      } else {
         if (this.rand.nextInt(10) == 0) {
            var5 = HorseType.DONKEY;
         } else {
            int var6 = this.rand.nextInt(7);
            int var7 = this.rand.nextInt(5);
            var5 = HorseType.HORSE;
            var4 = var6 | var7 << 8;
         }

         var3 = new EntityHorse.GroupData(var5, var4);
      }

      this.setType(var5);
      this.setHorseVariant(var4);
      if (this.rand.nextInt(5) == 0) {
         this.setGrowingAge(-24000);
      }

      if (var5.isUndead()) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
      } else {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)this.getModifiedMaxHealth());
         if (var5 == HorseType.HORSE) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getModifiedMovementSpeed());
         } else {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.17499999701976776D);
         }
      }

      if (var5.hasMuleEars()) {
         this.getEntityAttribute(JUMP_STRENGTH).setBaseValue(0.5D);
      } else {
         this.getEntityAttribute(JUMP_STRENGTH).setBaseValue(this.getModifiedJumpStrength());
      }

      this.setHealth(this.getMaxHealth());
      return (IEntityLivingData)var3;
   }

   public boolean canBeSteered() {
      Entity var1 = this.getControllingPassenger();
      return var1 instanceof EntityLivingBase;
   }

   public boolean canJump() {
      return this.isHorseSaddled();
   }

   public void handleStartJump(int var1) {
      float var2;
      if (var1 >= 90) {
         var2 = 1.0F;
      } else {
         var2 = 0.4F + 0.4F * (float)var1 / 90.0F;
      }

      HorseJumpEvent var3 = CraftEventFactory.callHorseJumpEvent(this, var2);
      if (!var3.isCancelled()) {
         this.allowStandSliding = true;
         this.makeHorseRear();
      }
   }

   public void handleStopJump() {
   }

   public void updatePassenger(Entity var1) {
      super.updatePassenger(var1);
      if (var1 instanceof EntityLiving) {
         EntityLiving var2 = (EntityLiving)var1;
         this.renderYawOffset = var2.renderYawOffset;
      }

      if (this.prevRearingAmount > 0.0F) {
         float var6 = MathHelper.sin(this.renderYawOffset * 0.017453292F);
         float var3 = MathHelper.cos(this.renderYawOffset * 0.017453292F);
         float var4 = 0.7F * this.prevRearingAmount;
         float var5 = 0.15F * this.prevRearingAmount;
         var1.setPosition(this.posX + (double)(var4 * var6), this.posY + this.getMountedYOffset() + var1.getYOffset() + (double)var5, this.posZ - (double)(var4 * var3));
         if (var1 instanceof EntityLivingBase) {
            ((EntityLivingBase)var1).renderYawOffset = this.renderYawOffset;
         }
      }

   }

   public double getMountedYOffset() {
      double var1 = super.getMountedYOffset();
      if (this.getType() == HorseType.SKELETON) {
         var1 -= 0.1875D;
      } else if (this.getType() == HorseType.DONKEY) {
         var1 -= 0.25D;
      }

      return var1;
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

   public void setSkeletonTrap(boolean var1) {
      if (var1 != this.skeletonTrap) {
         this.skeletonTrap = var1;
         if (var1) {
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

   public boolean replaceItemInInventory(int var1, @Nullable ItemStack var2) {
      if (var1 == 499 && this.getType().canBeChested()) {
         if (var2 == null && this.isChested()) {
            this.setChested(false);
            this.initHorseChest();
            return true;
         }

         if (var2 != null && var2.getItem() == Item.getItemFromBlock(Blocks.CHEST) && !this.isChested()) {
            this.setChested(true);
            this.initHorseChest();
            return true;
         }
      }

      int var3 = var1 - 400;
      if (var3 >= 0 && var3 < 2 && var3 < this.horseChest.getSizeInventory()) {
         if (var3 == 0 && var2 != null && var2.getItem() != Items.SADDLE) {
            return false;
         } else if (var3 != 1 || (var2 == null || HorseArmorType.isHorseArmor(var2.getItem())) && this.getType().isHorse()) {
            this.horseChest.setInventorySlotContents(var3, var2);
            this.updateHorseSlots();
            return true;
         } else {
            return false;
         }
      } else {
         int var4 = var1 - 500 + 2;
         if (var4 >= 2 && var4 < this.horseChest.getSizeInventory()) {
            this.horseChest.setInventorySlotContents(var4, var2);
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

      public GroupData(HorseType var1, int var2) {
         this.horseType = var1;
         this.horseVariant = var2;
      }
   }
}
