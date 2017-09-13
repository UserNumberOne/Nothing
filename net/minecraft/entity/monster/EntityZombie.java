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

   public EntityZombie(World var1) {
      super(var1);
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

   public void setArmsRaised(boolean var1) {
      this.getDataManager().set(ARMS_RAISED, Boolean.valueOf(var1));
   }

   public boolean isBreakDoorsTaskSet() {
      return this.isBreakDoorsTaskSet;
   }

   public void setBreakDoorsAItask(boolean var1) {
      if (this.isBreakDoorsTaskSet != var1) {
         this.isBreakDoorsTaskSet = var1;
         ((PathNavigateGround)this.getNavigator()).setBreakDoors(var1);
         if (var1) {
            this.tasks.addTask(1, this.breakDoor);
         } else {
            this.tasks.removeTask(this.breakDoor);
         }
      }

   }

   public boolean isChild() {
      return ((Boolean)this.getDataManager().get(IS_CHILD)).booleanValue();
   }

   protected int getExperiencePoints(EntityPlayer var1) {
      if (this.isChild()) {
         this.experienceValue = (int)((float)this.experienceValue * 2.5F);
      }

      return super.getExperiencePoints(var1);
   }

   public void setChild(boolean var1) {
      this.getDataManager().set(IS_CHILD, Boolean.valueOf(var1));
      if (this.world != null && !this.world.isRemote) {
         IAttributeInstance var2 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         var2.removeModifier(BABY_SPEED_BOOST);
         if (var1) {
            var2.applyModifier(BABY_SPEED_BOOST);
         }
      }

      this.setChildSize(var1);
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

   public void setZombieType(ZombieType var1) {
      this.getDataManager().set(VILLAGER_TYPE, Integer.valueOf(var1.getId()));
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (IS_CHILD.equals(var1)) {
         this.setChildSize(this.isChild());
      }

      super.notifyDataManagerChange(var1);
   }

   public void onLivingUpdate() {
      if (this.world.isDaytime() && !this.world.isRemote && !this.isChild() && this.getZombieType().isSunSensitive()) {
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

      super.onLivingUpdate();
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (super.attackEntityFrom(var1, var2)) {
         EntityLivingBase var3 = this.getAttackTarget();
         if (var3 == null && var1.getEntity() instanceof EntityLivingBase) {
            var3 = (EntityLivingBase)var1.getEntity();
         }

         if (var3 != null && this.world.getDifficulty() == EnumDifficulty.HARD && (double)this.rand.nextFloat() < this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).getAttributeValue() && this.world.getGameRules().getBoolean("doMobSpawning")) {
            int var4 = MathHelper.floor(this.posX);
            int var5 = MathHelper.floor(this.posY);
            int var6 = MathHelper.floor(this.posZ);
            EntityZombie var7 = new EntityZombie(this.world);

            for(int var8 = 0; var8 < 50; ++var8) {
               int var9 = var4 + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
               int var10 = var5 + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
               int var11 = var6 + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
               if (this.world.getBlockState(new BlockPos(var9, var10 - 1, var11)).isFullyOpaque() && this.world.getLightFromNeighbors(new BlockPos(var9, var10, var11)) < 10) {
                  var7.setPosition((double)var9, (double)var10, (double)var11);
                  if (!this.world.isAnyPlayerWithinRangeAt((double)var9, (double)var10, (double)var11, 7.0D) && this.world.checkNoEntityCollision(var7.getEntityBoundingBox(), var7) && this.world.getCollisionBoxes(var7, var7.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(var7.getEntityBoundingBox())) {
                     this.world.addEntity(var7, SpawnReason.REINFORCEMENTS);
                     var7.setGoalTarget(var3, TargetReason.REINFORCEMENT_TARGET, true);
                     var7.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var7)), (IEntityLivingData)null);
                     this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
                     var7.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
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
         int var1 = this.getConversionTimeBoost();
         int var2 = MinecraftServer.currentTick - this.lastTick;
         this.lastTick = MinecraftServer.currentTick;
         var1 = var1 * var2;
         this.conversionTime -= var1;
         if (this.conversionTime <= 0) {
            this.convertToVillager();
         }
      }

      super.onUpdate();
   }

   public boolean attackEntityAsMob(Entity var1) {
      boolean var2 = super.attackEntityAsMob(var1);
      if (var2) {
         float var3 = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
         if (this.getHeldItemMainhand() == null) {
            if (this.isBurning() && this.rand.nextFloat() < var3 * 0.3F) {
               EntityCombustByEntityEvent var4 = new EntityCombustByEntityEvent(this.getBukkitEntity(), var1.getBukkitEntity(), 2 * (int)var3);
               this.world.getServer().getPluginManager().callEvent(var4);
               if (!var4.isCancelled()) {
                  var1.setFire(var4.getDuration());
               }
            }

            if (this.getZombieType() == ZombieType.HUSK && var1 instanceof EntityLivingBase) {
               ((EntityLivingBase)var1).addPotionEffect(new PotionEffect(MobEffects.HUNGER, 140 * (int)var3));
            }
         }
      }

      return var2;
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

   protected void playStepSound(BlockPos var1, Block var2) {
      SoundEvent var3 = this.getZombieType().getStepSound();
      this.playSound(var3, 0.15F, 1.0F);
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEAD;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_ZOMBIE;
   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance var1) {
      super.setEquipmentBasedOnDifficulty(var1);
      if (this.rand.nextFloat() < (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.05F : 0.01F)) {
         int var2 = this.rand.nextInt(3);
         if (var2 == 0) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
         } else {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
         }
      }

   }

   public static void registerFixesZombie(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Zombie");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      if (this.isChild()) {
         var1.setBoolean("IsBaby", true);
      }

      var1.setInteger("ZombieType", this.getZombieType().getId());
      var1.setInteger("ConversionTime", this.isConverting() ? this.conversionTime : -1);
      var1.setBoolean("CanBreakDoors", this.isBreakDoorsTaskSet());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.getBoolean("IsBaby")) {
         this.setChild(true);
      }

      if (var1.getBoolean("IsVillager")) {
         if (var1.hasKey("VillagerProfession", 99)) {
            this.setZombieType(ZombieType.getByOrdinal(var1.getInteger("VillagerProfession") + 1));
         } else {
            this.setZombieType(ZombieType.getByOrdinal(this.world.rand.nextInt(5) + 1));
         }
      }

      if (var1.hasKey("ZombieType")) {
         this.setZombieType(ZombieType.getByOrdinal(var1.getInteger("ZombieType")));
      }

      if (var1.hasKey("ConversionTime", 99) && var1.getInteger("ConversionTime") > -1) {
         this.startConversion(var1.getInteger("ConversionTime"));
      }

      this.setBreakDoorsAItask(var1.getBoolean("CanBreakDoors"));
   }

   public void onKillEntity(EntityLivingBase var1) {
      super.onKillEntity(var1);
      if ((this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) && var1 instanceof EntityVillager) {
         if (this.world.getDifficulty() != EnumDifficulty.HARD && this.rand.nextBoolean()) {
            return;
         }

         EntityVillager var2 = (EntityVillager)var1;
         EntityZombie var3 = new EntityZombie(this.world);
         var3.copyLocationAndAnglesFrom(var1);
         this.world.removeEntity(var1);
         var3.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var3)), new EntityZombie.GroupData(false, true, (EntityZombie.SyntheticClass_1)null));
         var3.setZombieType(ZombieType.getByOrdinal(var2.getProfession() + 1));
         var3.setChild(var1.isChild());
         var3.setNoAI(var2.isAIDisabled());
         if (var2.hasCustomName()) {
            var3.setCustomNameTag(var2.getCustomNameTag());
            var3.setAlwaysRenderNameTag(var2.getAlwaysRenderNameTag());
         }

         this.world.addEntity(var3, SpawnReason.INFECTION);
         this.world.playEvent((EntityPlayer)null, 1026, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);
      }

   }

   public float getEyeHeight() {
      float var1 = 1.74F;
      if (this.isChild()) {
         var1 = (float)((double)var1 - 0.81D);
      }

      return var1;
   }

   protected boolean canEquipItem(ItemStack var1) {
      return var1.getItem() == Items.EGG && this.isChild() && this.isRiding() ? false : super.canEquipItem(var1);
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      Object var3 = super.onInitialSpawn(var1, var2);
      float var4 = var1.getClampedAdditionalDifficulty();
      this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * var4);
      if (var3 == null) {
         var3 = new EntityZombie.GroupData(this.world.rand.nextFloat() < 0.05F, this.world.rand.nextFloat() < 0.05F, (EntityZombie.SyntheticClass_1)null);
      }

      if (var3 instanceof EntityZombie.GroupData) {
         EntityZombie.GroupData var5 = (EntityZombie.GroupData)var3;
         boolean var6 = false;
         Biome var7 = this.world.getBiome(new BlockPos(this));
         if (var7 instanceof BiomeDesert && this.world.canSeeSky(new BlockPos(this)) && this.rand.nextInt(5) != 0) {
            this.setZombieType(ZombieType.HUSK);
            var6 = true;
         }

         if (!var6 && var5.isVillager) {
            this.setZombieType(ZombieType.getByOrdinal(this.rand.nextInt(5) + 1));
         }

         if (var5.isChild) {
            this.setChild(true);
            if ((double)this.world.rand.nextFloat() < 0.05D) {
               List var8 = this.world.getEntitiesWithinAABB(EntityChicken.class, this.getEntityBoundingBox().expand(5.0D, 3.0D, 5.0D), EntitySelectors.IS_STANDALONE);
               if (!var8.isEmpty()) {
                  EntityChicken var9 = (EntityChicken)var8.get(0);
                  var9.setChickenJockey(true);
                  this.startRiding(var9);
               }
            } else if ((double)this.world.rand.nextFloat() < 0.05D) {
               EntityChicken var13 = new EntityChicken(this.world);
               var13.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
               var13.onInitialSpawn(var1, (IEntityLivingData)null);
               var13.setChickenJockey(true);
               this.world.addEntity(var13, SpawnReason.MOUNT);
               this.startRiding(var13);
            }
         }
      }

      this.setBreakDoorsAItask(this.rand.nextFloat() < var4 * 0.1F);
      this.setEquipmentBasedOnDifficulty(var1);
      this.setEnchantmentBasedOnDifficulty(var1);
      if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         Calendar var12 = this.world.getCurrentDate();
         if (var12.get(2) + 1 == 10 && var12.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
            this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * 0.05000000074505806D, 0));
      double var10 = this.rand.nextDouble() * 1.5D * (double)var4;
      if (var10 > 1.0D) {
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random zombie-spawn bonus", var10, 2));
      }

      if (this.rand.nextFloat() < var4 * 0.05F) {
         this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 0.25D + 0.5D, 0));
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 3.0D + 1.0D, 2));
         this.setBreakDoorsAItask(true);
      }

      return (IEntityLivingData)var3;
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (var3 != null && var3.getItem() == Items.GOLDEN_APPLE && var3.getMetadata() == 0 && this.isVillager() && this.isPotionActive(MobEffects.WEAKNESS)) {
         if (!var1.capabilities.isCreativeMode) {
            --var3.stackSize;
         }

         if (!this.world.isRemote) {
            this.startConversion(this.rand.nextInt(2401) + 3600);
         }

         return true;
      } else {
         return false;
      }
   }

   protected void startConversion(int var1) {
      this.conversionTime = var1;
      this.getDataManager().set(CONVERTING, Boolean.valueOf(true));
      this.removePotionEffect(MobEffects.WEAKNESS);
      this.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, var1, Math.min(this.world.getDifficulty().getDifficultyId() - 1, 0)));
      this.world.setEntityState(this, (byte)16);
   }

   protected boolean canDespawn() {
      return !this.isConverting();
   }

   public boolean isConverting() {
      return ((Boolean)this.getDataManager().get(CONVERTING)).booleanValue();
   }

   protected void convertToVillager() {
      EntityVillager var1 = new EntityVillager(this.world);
      var1.copyLocationAndAnglesFrom(this);
      var1.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var1)), (IEntityLivingData)null);
      var1.setLookingForHome();
      if (this.isChild()) {
         var1.setGrowingAge(-24000);
      }

      this.world.removeEntity(this);
      var1.setNoAI(this.isAIDisabled());
      var1.setProfession(this.getVillagerType());
      if (this.hasCustomName()) {
         var1.setCustomNameTag(this.getCustomNameTag());
         var1.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
      }

      this.world.addEntity(var1, SpawnReason.CURED);
      var1.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
      this.world.playEvent((EntityPlayer)null, 1027, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);
   }

   protected int getConversionTimeBoost() {
      int var1 = 1;
      if (this.rand.nextFloat() < 0.01F) {
         int var2 = 0;
         BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

         for(int var4 = (int)this.posX - 4; var4 < (int)this.posX + 4 && var2 < 14; ++var4) {
            for(int var5 = (int)this.posY - 4; var5 < (int)this.posY + 4 && var2 < 14; ++var5) {
               for(int var6 = (int)this.posZ - 4; var6 < (int)this.posZ + 4 && var2 < 14; ++var6) {
                  Block var7 = this.world.getBlockState(var3.setPos(var4, var5, var6)).getBlock();
                  if (var7 == Blocks.IRON_BARS || var7 == Blocks.BED) {
                     if (this.rand.nextFloat() < 0.3F) {
                        ++var1;
                     }

                     ++var2;
                  }
               }
            }
         }
      }

      return var1;
   }

   public void setChildSize(boolean var1) {
      this.multiplySize(var1 ? 0.5F : 1.0F);
   }

   public final void setSize(float var1, float var2) {
      boolean var3 = this.zombieWidth > 0.0F && this.zombieHeight > 0.0F;
      this.zombieWidth = var1;
      this.zombieHeight = var2;
      if (!var3) {
         this.multiplySize(1.0F);
      }

   }

   protected final void multiplySize(float var1) {
      super.setSize(this.zombieWidth * var1, this.zombieHeight * var1);
   }

   public double getYOffset() {
      return this.isChild() ? 0.0D : -0.35D;
   }

   public void onDeath(DamageSource var1) {
      if (var1.getEntity() instanceof EntityCreeper && !(this instanceof EntityPigZombie) && ((EntityCreeper)var1.getEntity()).getPowered() && ((EntityCreeper)var1.getEntity()).isAIEnabled()) {
         ((EntityCreeper)var1.getEntity()).incrementDroppedSkulls();
         this.entityDropItem(new ItemStack(Items.SKULL, 1, 2), 0.0F);
      }

      super.onDeath(var1);
   }

   public String getName() {
      return this.hasCustomName() ? this.getCustomNameTag() : this.getZombieType().getName().getUnformattedText();
   }

   class GroupData implements IEntityLivingData {
      public boolean isChild;
      public boolean isVillager;

      private GroupData(boolean var2, boolean var3) {
         this.isChild = var2;
         this.isVillager = var3;
      }

      GroupData(boolean var2, boolean var3, EntityZombie.SyntheticClass_1 var4) {
         this(var2, var3);
      }
   }

   static class SyntheticClass_1 {
   }
}
