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
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
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
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityZombie extends EntityMob {
   protected static final IAttribute SPAWN_REINFORCEMENTS_CHANCE = (new RangedAttribute((IAttribute)null, "zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).setDescription("Spawn Reinforcements Chance");
   private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
   private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, 1);
   private static final DataParameter IS_CHILD = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
   private static final DataParameter VILLAGER_TYPE = EntityDataManager.createKey(EntityZombie.class, DataSerializers.VARINT);
   private static final DataParameter CONVERTING = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
   private static final DataParameter ARMS_RAISED = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
   private static final DataParameter VILLAGER_TYPE_STR = EntityDataManager.createKey(EntityZombie.class, DataSerializers.STRING);
   private final EntityAIBreakDoor breakDoor = new EntityAIBreakDoor(this);
   private int conversionTime;
   private boolean isBreakDoorsTaskSet;
   private float zombieWidth = -1.0F;
   private float zombieHeight;
   private VillagerProfession prof;

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
      this.getAttributeMap().registerAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.rand.nextDouble() * ForgeModContainer.zombieSummonBaseChance);
   }

   protected void entityInit() {
      super.entityInit();
      this.getDataManager().register(IS_CHILD, Boolean.valueOf(false));
      this.getDataManager().register(VILLAGER_TYPE, Integer.valueOf(0));
      this.getDataManager().register(VILLAGER_TYPE_STR, "");
      this.getDataManager().register(CONVERTING, Boolean.valueOf(false));
      this.getDataManager().register(ARMS_RAISED, Boolean.valueOf(false));
   }

   public void setArmsRaised(boolean var1) {
      this.getDataManager().set(ARMS_RAISED, Boolean.valueOf(var1));
   }

   @SideOnly(Side.CLIENT)
   public boolean isArmsRaised() {
      return ((Boolean)this.getDataManager().get(ARMS_RAISED)).booleanValue();
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

   /** @deprecated */
   @Deprecated
   @Nullable
   public ZombieType getZombieType() {
      return ZombieType.getByOrdinal(((Integer)this.getDataManager().get(VILLAGER_TYPE)).intValue());
   }

   public boolean isVillager() {
      return this.getVillagerTypeForge() != null;
   }

   @Nullable
   public VillagerProfession getVillagerTypeForge() {
      return this.prof;
   }

   /** @deprecated */
   @Deprecated
   public void setZombieType(ZombieType var1) {
      this.getDataManager().set(VILLAGER_TYPE, Integer.valueOf(var1.getId()));
      VillagerRegistry.onSetProfession(this, var1, var1.getId());
   }

   public void setVillagerType(@Nullable VillagerProfession var1) {
      this.prof = var1;
      this.getDataManager().set(VILLAGER_TYPE_STR, var1 == null ? "" : var1.getRegistryName().toString());
      VillagerRegistry.onSetProfession(this, var1);
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (IS_CHILD.equals(var1)) {
         this.setChildSize(this.isChild());
      } else if (VILLAGER_TYPE.equals(var1)) {
         VillagerRegistry.onSetProfession(this, ZombieType.getByOrdinal(((Integer)this.getDataManager().get(VILLAGER_TYPE)).intValue()), ((Integer)this.getDataManager().get(VILLAGER_TYPE)).intValue());
      } else if (VILLAGER_TYPE_STR.equals(var1)) {
         String var2 = (String)this.getDataManager().get(VILLAGER_TYPE_STR);
         VillagerProfession var3 = "".equals(var2) ? null : (VillagerProfession)ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation(var2));
         this.setVillagerType(var3);
      }

      super.notifyDataManagerChange(var1);
   }

   public void onLivingUpdate() {
      if (this.world.isDaytime() && !this.world.isRemote && !this.isChild() && (this.getZombieType() == null || this.getZombieType().isSunSensitive())) {
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
               this.setFire(8);
            }
         }
      }

      super.onLivingUpdate();
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!super.attackEntityFrom(var1, var2)) {
         return false;
      } else {
         EntityLivingBase var3 = this.getAttackTarget();
         if (var3 == null && var1.getEntity() instanceof EntityLivingBase) {
            var3 = (EntityLivingBase)var1.getEntity();
         }

         int var4 = MathHelper.floor(this.posX);
         int var5 = MathHelper.floor(this.posY);
         int var6 = MathHelper.floor(this.posZ);
         SummonAidEvent var7 = ForgeEventFactory.fireZombieSummonAid(this, this.world, var4, var5, var6, var3, this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).getAttributeValue());
         if (var7.getResult() == Result.DENY) {
            return true;
         } else {
            if (var7.getResult() == Result.ALLOW || var3 != null && this.world.getDifficulty() == EnumDifficulty.HARD && (double)this.rand.nextFloat() < this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).getAttributeValue() && this.world.getGameRules().getBoolean("doMobSpawning")) {
               EntityZombie var8;
               if (var7.getCustomSummonedAid() != null && var7.getResult() == Result.ALLOW) {
                  var8 = var7.getCustomSummonedAid();
               } else {
                  var8 = new EntityZombie(this.world);
               }

               for(int var9 = 0; var9 < 50; ++var9) {
                  int var10 = var4 + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
                  int var11 = var5 + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
                  int var12 = var6 + MathHelper.getInt(this.rand, 7, 40) * MathHelper.getInt(this.rand, -1, 1);
                  if (this.world.getBlockState(new BlockPos(var10, var11 - 1, var12)).isSideSolid(this.world, new BlockPos(var10, var11 - 1, var12), EnumFacing.UP) && this.world.getLightFromNeighbors(new BlockPos(var10, var11, var12)) < 10) {
                     var8.setPosition((double)var10, (double)var11, (double)var12);
                     if (!this.world.isAnyPlayerWithinRangeAt((double)var10, (double)var11, (double)var12, 7.0D) && this.world.checkNoEntityCollision(var8.getEntityBoundingBox(), var8) && this.world.getCollisionBoxes(var8, var8.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(var8.getEntityBoundingBox())) {
                        this.world.spawnEntity(var8);
                        if (var3 != null) {
                           var8.setAttackTarget(var3);
                        }

                        var8.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var8)), (IEntityLivingData)null);
                        this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
                        var8.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
                        break;
                     }
                  }
               }
            }

            return true;
         }
      }
   }

   public void onUpdate() {
      if (!this.world.isRemote && this.isConverting()) {
         int var1 = this.getConversionTimeBoost();
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
               var1.setFire(2 * (int)var3);
            }

            if (this.getZombieType() == ZombieType.HUSK && var1 instanceof EntityLivingBase) {
               ((EntityLivingBase)var1).addPotionEffect(new PotionEffect(MobEffects.HUNGER, 140 * (int)var3));
            }
         }
      }

      return var2;
   }

   protected SoundEvent getAmbientSound() {
      return this.getZombieType() == null ? SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT : this.getZombieType().getAmbientSound();
   }

   protected SoundEvent getHurtSound() {
      return this.getZombieType() == null ? SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT : this.getZombieType().getHurtSound();
   }

   protected SoundEvent getDeathSound() {
      return this.getZombieType() == null ? SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH : this.getZombieType().getDeathSound();
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      SoundEvent var3 = this.getZombieType() == null ? SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP : this.getZombieType().getStepSound();
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

      var1.setInteger("ZombieType", ((Integer)this.getDataManager().get(VILLAGER_TYPE)).intValue());
      var1.setString("VillagerProfessionName", this.getVillagerTypeForge() == null ? "" : this.getVillagerTypeForge().getRegistryName().toString());
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
            int var2 = var1.getInteger("VillagerProfession") + 1;
            this.getDataManager().set(VILLAGER_TYPE, Integer.valueOf(var2));
            VillagerRegistry.onSetProfession(this, ZombieType.getByOrdinal(var2), var2);
         } else {
            VillagerRegistry.setRandomProfession(this, this.world.rand);
         }
      }

      if (var1.hasKey("ZombieType")) {
         int var4 = var1.getInteger("ZombieType");
         this.getDataManager().set(VILLAGER_TYPE, Integer.valueOf(var4));
         VillagerRegistry.onSetProfession(this, ZombieType.getByOrdinal(var4), var4);
      }

      String var5 = var1.getString("VillagerProfessionName");
      if (!"".equals(var5)) {
         VillagerProfession var3 = (VillagerProfession)ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation(var5));
         if (var3 == null) {
            var3 = (VillagerProfession)ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation("minecraft:farmer"));
         }

         this.setVillagerType(var3);
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
         var3.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var3)), new EntityZombie.GroupData(false, true));
         var3.setVillagerType(var2.getProfessionForge());
         var3.setChild(var1.isChild());
         var3.setNoAI(var2.isAIDisabled());
         if (var2.hasCustomName()) {
            var3.setCustomNameTag(var2.getCustomNameTag());
            var3.setAlwaysRenderNameTag(var2.getAlwaysRenderNameTag());
         }

         this.world.spawnEntity(var3);
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
      var2 = super.onInitialSpawn(var1, var2);
      float var3 = var1.getClampedAdditionalDifficulty();
      this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * var3);
      if (var2 == null) {
         var2 = new EntityZombie.GroupData(this.world.rand.nextFloat() < ForgeModContainer.zombieBabyChance, this.world.rand.nextFloat() < 0.05F);
      }

      if (var2 instanceof EntityZombie.GroupData) {
         EntityZombie.GroupData var4 = (EntityZombie.GroupData)var2;
         boolean var5 = false;
         Biome var6 = this.world.getBiome(new BlockPos(this));
         if (var6 instanceof BiomeDesert && this.world.canSeeSky(new BlockPos(this)) && this.rand.nextInt(5) != 0) {
            this.setZombieType(ZombieType.HUSK);
            var5 = true;
         }

         if (!var5 && var4.isVillager) {
            VillagerRegistry.setRandomProfession(this, this.rand);
         }

         if (var4.isChild) {
            this.setChild(true);
            if ((double)this.world.rand.nextFloat() < 0.05D) {
               List var7 = this.world.getEntitiesWithinAABB(EntityChicken.class, this.getEntityBoundingBox().expand(5.0D, 3.0D, 5.0D), EntitySelectors.IS_STANDALONE);
               if (!var7.isEmpty()) {
                  EntityChicken var8 = (EntityChicken)var7.get(0);
                  var8.setChickenJockey(true);
                  this.startRiding(var8);
               }
            } else if ((double)this.world.rand.nextFloat() < 0.05D) {
               EntityChicken var12 = new EntityChicken(this.world);
               var12.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
               var12.onInitialSpawn(var1, (IEntityLivingData)null);
               var12.setChickenJockey(true);
               this.world.spawnEntity(var12);
               this.startRiding(var12);
            }
         }
      }

      this.setBreakDoorsAItask(this.rand.nextFloat() < var3 * 0.1F);
      this.setEquipmentBasedOnDifficulty(var1);
      this.setEnchantmentBasedOnDifficulty(var1);
      if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         Calendar var10 = this.world.getCurrentDate();
         if (var10.get(2) + 1 == 10 && var10.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
            this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * 0.05000000074505806D, 0));
      double var11 = this.rand.nextDouble() * 1.5D * (double)var3;
      if (var11 > 1.0D) {
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random zombie-spawn bonus", var11, 2));
      }

      if (this.rand.nextFloat() < var3 * 0.05F) {
         this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 0.25D + 0.5D, 0));
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 3.0D + 1.0D, 2));
         this.setBreakDoorsAItask(true);
      }

      return var2;
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

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 16) {
         if (!this.isSilent()) {
            this.world.playSound(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.getSoundCategory(), 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F, false);
         }
      } else {
         super.handleStatusUpdate(var1);
      }

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
      if (this.getVillagerTypeForge() != null) {
         var1.setProfession(this.getVillagerTypeForge());
      } else {
         var1.setProfession(0);
      }

      if (this.hasCustomName()) {
         var1.setCustomNameTag(this.getCustomNameTag());
         var1.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
      }

      this.world.spawnEntity(var1);
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

   protected final void setSize(float var1, float var2) {
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
      super.onDeath(var1);
      if (var1.getEntity() instanceof EntityCreeper && !(this instanceof EntityPigZombie) && ((EntityCreeper)var1.getEntity()).getPowered() && ((EntityCreeper)var1.getEntity()).isAIEnabled()) {
         ((EntityCreeper)var1.getEntity()).incrementDroppedSkulls();
         this.entityDropItem(new ItemStack(Items.SKULL, 1, 2), 0.0F);
      }

   }

   public String getName() {
      if (this.hasCustomName()) {
         return this.getCustomNameTag();
      } else {
         return this.getVillagerTypeForge() != null ? "entity.Zombie.name" : this.getZombieType().getName().getUnformattedText();
      }
   }

   class GroupData implements IEntityLivingData {
      public boolean isChild;
      public boolean isVillager;

      private GroupData(boolean var2, boolean var3) {
         this.isChild = var2;
         this.isVillager = var3;
      }
   }
}
