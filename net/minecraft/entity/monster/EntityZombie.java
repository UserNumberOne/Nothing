package net.minecraft.entity.monster;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityZombie extends EntityMob {
   protected static final IAttribute SPAWN_REINFORCEMENTS_CHANCE = (new RangedAttribute((IAttribute)null, "zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).setDescription("Spawn Reinforcements Chance");
   private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
   private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, 1);
   private static final DataParameter IS_CHILD = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
   private static final DataParameter VILLAGER_TYPE = EntityDataManager.createKey(EntityZombie.class, DataSerializers.VARINT);
   private static final DataParameter CONVERTING = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
   private static final DataParameter ARMS_RAISED = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
   private final EntityAIBreakDoor breakDoor = new EntityAIBreakDoor(this);
   private int conversionTime;
   private boolean isBreakDoorsTaskSet;
   private float zombieWidth = -1.0F;
   private float zombieHeight;
   private int lastTick = MinecraftServer.currentTick;

   public EntityZombie(World world) {
      super(world);
      this.setSize(0.6F, 1.95F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAIZombieAttack(this, 1.0D, false));
      this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
      this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.applyEntityAI();
   }

   protected void applyEntityAI() {
      this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[]{EntityPigZombie.class}));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, false));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, true));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
      this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
      this.getAttributeMap().registerAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.rand.nextDouble() * 0.10000000149011612D);
   }

   protected void entityInit() {
      super.entityInit();
      this.getDataManager().register(IS_CHILD, Boolean.valueOf(false));
      this.getDataManager().register(VILLAGER_TYPE, Integer.valueOf(0));
      this.getDataManager().register(CONVERTING, Boolean.valueOf(false));
      this.getDataManager().register(ARMS_RAISED, Boolean.valueOf(false));
   }

   public void setArmsRaised(boolean flag) {
      this.getDataManager().set(ARMS_RAISED, Boolean.valueOf(flag));
   }

   public boolean isBreakDoorsTaskSet() {
      return this.isBreakDoorsTaskSet;
   }

   public void setBreakDoorsAItask(boolean flag) {
      if (this.isBreakDoorsTaskSet != flag) {
         this.isBreakDoorsTaskSet = flag;
         ((PathNavigateGround)this.getNavigator()).setBreakDoors(flag);
         if (flag) {
            this.tasks.addTask(1, this.breakDoor);
         } else {
            this.tasks.removeTask(this.breakDoor);
         }
      }

   }

   public boolean isChild() {
      return ((Boolean)this.getDataManager().get(IS_CHILD)).booleanValue();
   }

   protected int getExperiencePoints(EntityPlayer entityhuman) {
      if (this.isChild()) {
         this.experienceValue = (int)((float)this.experienceValue * 2.5F);
      }

      return super.getExperiencePoints(entityhuman);
   }

   public void setChild(boolean flag) {
      this.getDataManager().set(IS_CHILD, Boolean.valueOf(flag));
      if (this.world != null && !this.world.isRemote) {
         IAttributeInstance attributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         attributeinstance.removeModifier(BABY_SPEED_BOOST);
         if (flag) {
            attributeinstance.applyModifier(BABY_SPEED_BOOST);
         }
      }

      this.setChildSize(flag);
   }

   public ZombieType getZombieType() {
      return ZombieType.getByOrdinal(((Integer)this.getDataManager().get(VILLAGER_TYPE)).intValue());
   }

   public boolean isVillager() {
      return this.getZombieType().isVillager();
   }

   public int getVillagerType() {
      return this.getZombieType().getVillagerId();
   }

   public void setZombieType(ZombieType enumzombietype) {
      this.getDataManager().set(VILLAGER_TYPE, Integer.valueOf(enumzombietype.getId()));
   }

   public void notifyDataManagerChange(DataParameter datawatcherobject) {
      if (IS_CHILD.equals(datawatcherobject)) {
         this.setChildSize(this.isChild());
      }

      super.notifyDataManagerChange(datawatcherobject);
   }

   public void onLivingUpdate() {
      if (this.world.isDaytime() && !this.world.isRemote && !this.isChild() && this.getZombieType().isSunSensitive()) {
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

      super.onLivingUpdate();
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (super.attackEntityFrom(damagesource, f)) {
         EntityLivingBase entityliving = this.getAttackTarget();
         if (entityliving == null && damagesource.getEntity() instanceof EntityLivingBase) {
            entityliving = (EntityLivingBase)damagesource.getEntity();
         }

         if (entityliving != null && this.world.getDifficulty() == EnumDifficulty.HARD && (double)this.rand.nextFloat() < this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).getAttributeValue() && this.world.getGameRules().getBoolean("doMobSpawning")) {
            int i = MathHelper.floor(this.posX);
            int j = MathHelper.floor(this.posY);
            int k = MathHelper.floor(this.posZ);
            EntityZombie entityzombie = new EntityZombie(this.world);

            for(int l = 0; l < 50; ++l) {
               int i1 = i + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
               int j1 = j + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
               int k1 = k + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
               if (this.world.getBlockState(new BlockPos(i1, j1 - 1, k1)).isFullyOpaque() && this.world.getLightFromNeighbors(new BlockPos(i1, j1, k1)) < 10) {
                  entityzombie.setPosition((double)i1, (double)j1, (double)k1);
                  if (!this.world.isAnyPlayerWithinRangeAt((double)i1, (double)j1, (double)k1, 7.0D) && this.world.checkNoEntityCollision(entityzombie.getEntityBoundingBox(), entityzombie) && this.world.getCollisionBoxes(entityzombie, entityzombie.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(entityzombie.getEntityBoundingBox())) {
                     this.world.addEntity(entityzombie, SpawnReason.REINFORCEMENTS);
                     entityzombie.setGoalTarget(entityliving, TargetReason.REINFORCEMENT_TARGET, true);
                     entityzombie.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entityzombie)), (IEntityLivingData)null);
                     this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
                     entityzombie.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
                     break;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void onUpdate() {
      if (!this.world.isRemote && this.isConverting()) {
         int i = this.getConversionTimeBoost();
         int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
         this.lastTick = MinecraftServer.currentTick;
         i = i * elapsedTicks;
         this.conversionTime -= i;
         if (this.conversionTime <= 0) {
            this.convertToVillager();
         }
      }

      super.onUpdate();
   }

   public boolean attackEntityAsMob(Entity entity) {
      boolean flag = super.attackEntityAsMob(entity);
      if (flag) {
         float f = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
         if (this.getHeldItemMainhand() == null) {
            if (this.isBurning() && this.rand.nextFloat() < f * 0.3F) {
               EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), 2 * (int)f);
               this.world.getServer().getPluginManager().callEvent(event);
               if (!event.isCancelled()) {
                  entity.setFire(event.getDuration());
               }
            }

            if (this.getZombieType() == ZombieType.HUSK && entity instanceof EntityLivingBase) {
               ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.HUNGER, 140 * (int)f));
            }
         }
      }

      return flag;
   }

   protected SoundEvent getAmbientSound() {
      return this.getZombieType().getAmbientSound();
   }

   protected SoundEvent getHurtSound() {
      return this.getZombieType().getHurtSound();
   }

   protected SoundEvent getDeathSound() {
      return this.getZombieType().getDeathSound();
   }

   protected void playStepSound(BlockPos blockposition, Block block) {
      SoundEvent soundeffect = this.getZombieType().getStepSound();
      this.playSound(soundeffect, 0.15F, 1.0F);
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEAD;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_ZOMBIE;
   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficultydamagescaler) {
      super.setEquipmentBasedOnDifficulty(difficultydamagescaler);
      if (this.rand.nextFloat() < (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.05F : 0.01F)) {
         int i = this.rand.nextInt(3);
         if (i == 0) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
         } else {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
         }
      }

   }

   public static void registerFixesZombie(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Zombie");
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      if (this.isChild()) {
         nbttagcompound.setBoolean("IsBaby", true);
      }

      nbttagcompound.setInteger("ZombieType", this.getZombieType().getId());
      nbttagcompound.setInteger("ConversionTime", this.isConverting() ? this.conversionTime : -1);
      nbttagcompound.setBoolean("CanBreakDoors", this.isBreakDoorsTaskSet());
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      if (nbttagcompound.getBoolean("IsBaby")) {
         this.setChild(true);
      }

      if (nbttagcompound.getBoolean("IsVillager")) {
         if (nbttagcompound.hasKey("VillagerProfession", 99)) {
            this.setZombieType(ZombieType.getByOrdinal(nbttagcompound.getInteger("VillagerProfession") + 1));
         } else {
            this.setZombieType(ZombieType.getByOrdinal(this.world.rand.nextInt(5) + 1));
         }
      }

      if (nbttagcompound.hasKey("ZombieType")) {
         this.setZombieType(ZombieType.getByOrdinal(nbttagcompound.getInteger("ZombieType")));
      }

      if (nbttagcompound.hasKey("ConversionTime", 99) && nbttagcompound.getInteger("ConversionTime") > -1) {
         this.startConversion(nbttagcompound.getInteger("ConversionTime"));
      }

      this.setBreakDoorsAItask(nbttagcompound.getBoolean("CanBreakDoors"));
   }

   public void onKillEntity(EntityLivingBase entityliving) {
      super.onKillEntity(entityliving);
      if ((this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) && entityliving instanceof EntityVillager) {
         if (this.world.getDifficulty() != EnumDifficulty.HARD && this.rand.nextBoolean()) {
            return;
         }

         EntityVillager entityvillager = (EntityVillager)entityliving;
         EntityZombie entityzombie = new EntityZombie(this.world);
         entityzombie.copyLocationAndAnglesFrom(entityliving);
         this.world.removeEntity(entityliving);
         entityzombie.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entityzombie)), new EntityZombie.GroupData(false, true, (EntityZombie.SyntheticClass_1)null));
         entityzombie.setZombieType(ZombieType.getByOrdinal(entityvillager.getProfession() + 1));
         entityzombie.setChild(entityliving.isChild());
         entityzombie.setNoAI(entityvillager.isAIDisabled());
         if (entityvillager.hasCustomName()) {
            entityzombie.setCustomNameTag(entityvillager.getCustomNameTag());
            entityzombie.setAlwaysRenderNameTag(entityvillager.getAlwaysRenderNameTag());
         }

         this.world.addEntity(entityzombie, SpawnReason.INFECTION);
         this.world.playEvent((EntityPlayer)null, 1026, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);
      }

   }

   public float getEyeHeight() {
      float f = 1.74F;
      if (this.isChild()) {
         f = (float)((double)f - 0.81D);
      }

      return f;
   }

   protected boolean canEquipItem(ItemStack itemstack) {
      return itemstack.getItem() == Items.EGG && this.isChild() && this.isRiding() ? false : super.canEquipItem(itemstack);
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficultydamagescaler, @Nullable IEntityLivingData groupdataentity) {
      Object object = super.onInitialSpawn(difficultydamagescaler, groupdataentity);
      float f = difficultydamagescaler.getClampedAdditionalDifficulty();
      this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * f);
      if (object == null) {
         object = new EntityZombie.GroupData(this.world.rand.nextFloat() < 0.05F, this.world.rand.nextFloat() < 0.05F, (EntityZombie.SyntheticClass_1)null);
      }

      if (object instanceof EntityZombie.GroupData) {
         EntityZombie.GroupData entityzombie_groupdatazombie = (EntityZombie.GroupData)object;
         boolean flag = false;
         Biome biomebase = this.world.getBiome(new BlockPos(this));
         if (biomebase instanceof BiomeDesert && this.world.canSeeSky(new BlockPos(this)) && this.rand.nextInt(5) != 0) {
            this.setZombieType(ZombieType.HUSK);
            flag = true;
         }

         if (!flag && entityzombie_groupdatazombie.isVillager) {
            this.setZombieType(ZombieType.getByOrdinal(this.rand.nextInt(5) + 1));
         }

         if (entityzombie_groupdatazombie.isChild) {
            this.setChild(true);
            if ((double)this.world.rand.nextFloat() < 0.05D) {
               List list = this.world.getEntitiesWithinAABB(EntityChicken.class, this.getEntityBoundingBox().expand(5.0D, 3.0D, 5.0D), EntitySelectors.IS_STANDALONE);
               if (!list.isEmpty()) {
                  EntityChicken entitychicken = (EntityChicken)list.get(0);
                  entitychicken.setChickenJockey(true);
                  this.startRiding(entitychicken);
               }
            } else if ((double)this.world.rand.nextFloat() < 0.05D) {
               EntityChicken entitychicken1 = new EntityChicken(this.world);
               entitychicken1.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
               entitychicken1.onInitialSpawn(difficultydamagescaler, (IEntityLivingData)null);
               entitychicken1.setChickenJockey(true);
               this.world.addEntity(entitychicken1, SpawnReason.MOUNT);
               this.startRiding(entitychicken1);
            }
         }
      }

      this.setBreakDoorsAItask(this.rand.nextFloat() < f * 0.1F);
      this.setEquipmentBasedOnDifficulty(difficultydamagescaler);
      this.setEnchantmentBasedOnDifficulty(difficultydamagescaler);
      if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         Calendar calendar = this.world.getCurrentDate();
         if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
            this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * 0.05000000074505806D, 0));
      double d0 = this.rand.nextDouble() * 1.5D * (double)f;
      if (d0 > 1.0D) {
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
      }

      if (this.rand.nextFloat() < f * 0.05F) {
         this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 0.25D + 0.5D, 0));
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 3.0D + 1.0D, 2));
         this.setBreakDoorsAItask(true);
      }

      return (IEntityLivingData)object;
   }

   public boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (itemstack != null && itemstack.getItem() == Items.GOLDEN_APPLE && itemstack.getMetadata() == 0 && this.isVillager() && this.isPotionActive(MobEffects.WEAKNESS)) {
         if (!entityhuman.capabilities.isCreativeMode) {
            --itemstack.stackSize;
         }

         if (!this.world.isRemote) {
            this.startConversion(this.rand.nextInt(2401) + 3600);
         }

         return true;
      } else {
         return false;
      }
   }

   protected void startConversion(int i) {
      this.conversionTime = i;
      this.getDataManager().set(CONVERTING, Boolean.valueOf(true));
      this.removePotionEffect(MobEffects.WEAKNESS);
      this.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, i, Math.min(this.world.getDifficulty().getDifficultyId() - 1, 0)));
      this.world.setEntityState(this, (byte)16);
   }

   protected boolean canDespawn() {
      return !this.isConverting();
   }

   public boolean isConverting() {
      return ((Boolean)this.getDataManager().get(CONVERTING)).booleanValue();
   }

   protected void convertToVillager() {
      EntityVillager entityvillager = new EntityVillager(this.world);
      entityvillager.copyLocationAndAnglesFrom(this);
      entityvillager.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entityvillager)), (IEntityLivingData)null);
      entityvillager.setLookingForHome();
      if (this.isChild()) {
         entityvillager.setGrowingAge(-24000);
      }

      this.world.removeEntity(this);
      entityvillager.setNoAI(this.isAIDisabled());
      entityvillager.setProfession(this.getVillagerType());
      if (this.hasCustomName()) {
         entityvillager.setCustomNameTag(this.getCustomNameTag());
         entityvillager.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
      }

      this.world.addEntity(entityvillager, SpawnReason.CURED);
      entityvillager.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
      this.world.playEvent((EntityPlayer)null, 1027, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);
   }

   protected int getConversionTimeBoost() {
      int i = 1;
      if (this.rand.nextFloat() < 0.01F) {
         int j = 0;
         BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();

         for(int k = (int)this.posX - 4; k < (int)this.posX + 4 && j < 14; ++k) {
            for(int l = (int)this.posY - 4; l < (int)this.posY + 4 && j < 14; ++l) {
               for(int i1 = (int)this.posZ - 4; i1 < (int)this.posZ + 4 && j < 14; ++i1) {
                  Block block = this.world.getBlockState(blockposition_mutableblockposition.setPos(k, l, i1)).getBlock();
                  if (block == Blocks.IRON_BARS || block == Blocks.BED) {
                     if (this.rand.nextFloat() < 0.3F) {
                        ++i;
                     }

                     ++j;
                  }
               }
            }
         }
      }

      return i;
   }

   public void setChildSize(boolean flag) {
      this.multiplySize(flag ? 0.5F : 1.0F);
   }

   public final void setSize(float f, float f1) {
      boolean flag = this.zombieWidth > 0.0F && this.zombieHeight > 0.0F;
      this.zombieWidth = f;
      this.zombieHeight = f1;
      if (!flag) {
         this.multiplySize(1.0F);
      }

   }

   protected final void multiplySize(float f) {
      super.setSize(this.zombieWidth * f, this.zombieHeight * f);
   }

   public double getYOffset() {
      return this.isChild() ? 0.0D : -0.35D;
   }

   public void onDeath(DamageSource damagesource) {
      if (damagesource.getEntity() instanceof EntityCreeper && !(this instanceof EntityPigZombie) && ((EntityCreeper)damagesource.getEntity()).getPowered() && ((EntityCreeper)damagesource.getEntity()).isAIEnabled()) {
         ((EntityCreeper)damagesource.getEntity()).incrementDroppedSkulls();
         this.entityDropItem(new ItemStack(Items.SKULL, 1, 2), 0.0F);
      }

      super.onDeath(damagesource);
   }

   public String getName() {
      return this.hasCustomName() ? this.getCustomNameTag() : this.getZombieType().getName().getUnformattedText();
   }

   class GroupData implements IEntityLivingData {
      public boolean isChild;
      public boolean isVillager;

      private GroupData(boolean flag, boolean flag1) {
         this.isChild = flag;
         this.isVillager = flag1;
      }

      GroupData(boolean flag, boolean flag1, EntityZombie.SyntheticClass_1 entityzombie_syntheticclass_1) {
         this(flag, flag1);
      }
   }

   static class SyntheticClass_1 {
   }
}
