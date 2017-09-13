package net.minecraft.entity.player;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;

public abstract class EntityPlayer extends EntityLivingBase {
   public static final String PERSISTED_NBT_TAG = "PlayerPersisted";
   private HashMap spawnChunkMap = new HashMap();
   private HashMap spawnForcedMap = new HashMap();
   public float eyeHeight = this.getDefaultEyeHeight();
   private static final DataParameter ABSORPTION = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.FLOAT);
   private static final DataParameter PLAYER_SCORE = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.VARINT);
   protected static final DataParameter PLAYER_MODEL_FLAG = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BYTE);
   protected static final DataParameter MAIN_HAND = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BYTE);
   public InventoryPlayer inventory = new InventoryPlayer(this);
   private InventoryEnderChest theInventoryEnderChest = new InventoryEnderChest();
   public Container inventoryContainer;
   public Container openContainer;
   protected FoodStats foodStats = new FoodStats();
   protected int flyToggleTimer;
   public float prevCameraYaw;
   public float cameraYaw;
   public int xpCooldown;
   public double prevChasingPosX;
   public double prevChasingPosY;
   public double prevChasingPosZ;
   public double chasingPosX;
   public double chasingPosY;
   public double chasingPosZ;
   protected boolean sleeping;
   public BlockPos bedLocation;
   private int sleepTimer;
   public float renderOffsetX;
   @SideOnly(Side.CLIENT)
   public float renderOffsetY;
   public float renderOffsetZ;
   private BlockPos spawnChunk;
   private boolean spawnForced;
   private BlockPos startMinecartRidingCoordinate;
   public PlayerCapabilities capabilities = new PlayerCapabilities();
   public int experienceLevel;
   public int experienceTotal;
   public float experience;
   private int xpSeed;
   protected float speedOnGround = 0.1F;
   protected float speedInAir = 0.02F;
   private int lastXPSound;
   private final GameProfile gameProfile;
   @SideOnly(Side.CLIENT)
   private boolean hasReducedDebug;
   private ItemStack itemStackMainHand;
   private final CooldownTracker cooldownTracker = this.createCooldownTracker();
   public EntityFishHook fishEntity;
   private String displayname;
   private final Collection prefixes = new LinkedList();
   private final Collection suffixes = new LinkedList();
   private final IItemHandler playerMainHandler;
   private final IItemHandler playerEquipmentHandler;
   private final IItemHandler playerJoinedHandler;

   protected CooldownTracker createCooldownTracker() {
      return new CooldownTracker();
   }

   public EntityPlayer(World var1, GameProfile var2) {
      super(var1);
      this.playerMainHandler = new PlayerMainInvWrapper(this.inventory);
      this.playerEquipmentHandler = new CombinedInvWrapper(new IItemHandlerModifiable[]{new PlayerArmorInvWrapper(this.inventory), new PlayerOffhandInvWrapper(this.inventory)});
      this.playerJoinedHandler = new PlayerInvWrapper(this.inventory);
      this.setUniqueId(getUUID(var2));
      this.gameProfile = var2;
      this.inventoryContainer = new ContainerPlayer(this.inventory, !var1.isRemote, this);
      this.openContainer = this.inventoryContainer;
      BlockPos var3 = var1.getSpawnPoint();
      this.setLocationAndAngles((double)var3.getX() + 0.5D, (double)(var3.getY() + 1), (double)var3.getZ() + 0.5D, 0.0F, 0.0F);
      this.unused180 = 180.0F;
      this.fireResistance = 20;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612D);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.LUCK);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(ABSORPTION, Float.valueOf(0.0F));
      this.dataManager.register(PLAYER_SCORE, Integer.valueOf(0));
      this.dataManager.register(PLAYER_MODEL_FLAG, Byte.valueOf((byte)0));
      this.dataManager.register(MAIN_HAND, Byte.valueOf((byte)1));
   }

   public void onUpdate() {
      FMLCommonHandler.instance().onPlayerPreTick(this);
      this.noClip = this.isSpectator();
      if (this.isSpectator()) {
         this.onGround = false;
      }

      if (this.xpCooldown > 0) {
         --this.xpCooldown;
      }

      if (this.isPlayerSleeping()) {
         ++this.sleepTimer;
         if (this.sleepTimer > 100) {
            this.sleepTimer = 100;
         }

         if (!this.world.isRemote) {
            if (!this.isInBed()) {
               this.wakeUpPlayer(true, true, false);
            } else if (this.world.isDaytime()) {
               this.wakeUpPlayer(false, true, true);
            }
         }
      } else if (this.sleepTimer > 0) {
         ++this.sleepTimer;
         if (this.sleepTimer >= 110) {
            this.sleepTimer = 0;
         }
      }

      super.onUpdate();
      if (!this.world.isRemote && this.openContainer != null && !this.openContainer.canInteractWith(this)) {
         this.closeScreen();
         this.openContainer = this.inventoryContainer;
      }

      if (this.isBurning() && this.capabilities.disableDamage) {
         this.extinguish();
      }

      this.updateCape();
      if (!this.isRiding()) {
         this.startMinecartRidingCoordinate = null;
      }

      if (!this.world.isRemote) {
         this.foodStats.onUpdate(this);
         this.addStat(StatList.PLAY_ONE_MINUTE);
         if (this.isEntityAlive()) {
            this.addStat(StatList.TIME_SINCE_DEATH);
         }

         if (this.isSneaking()) {
            this.addStat(StatList.SNEAK_TIME);
         }
      }

      int var1 = 29999999;
      double var2 = MathHelper.clamp(this.posX, -2.9999999E7D, 2.9999999E7D);
      double var4 = MathHelper.clamp(this.posZ, -2.9999999E7D, 2.9999999E7D);
      if (var2 != this.posX || var4 != this.posZ) {
         this.setPosition(var2, this.posY, var4);
      }

      ++this.ticksSinceLastSwing;
      ItemStack var6 = this.getHeldItemMainhand();
      if (!ItemStack.areItemStacksEqual(this.itemStackMainHand, var6)) {
         if (!ItemStack.areItemsEqualIgnoreDurability(this.itemStackMainHand, var6)) {
            this.resetCooldown();
         }

         this.itemStackMainHand = var6 == null ? null : var6.copy();
      }

      this.cooldownTracker.tick();
      this.updateSize();
   }

   private void updateCape() {
      this.prevChasingPosX = this.chasingPosX;
      this.prevChasingPosY = this.chasingPosY;
      this.prevChasingPosZ = this.chasingPosZ;
      double var1 = this.posX - this.chasingPosX;
      double var3 = this.posY - this.chasingPosY;
      double var5 = this.posZ - this.chasingPosZ;
      double var7 = 10.0D;
      if (var1 > 10.0D) {
         this.chasingPosX = this.posX;
         this.prevChasingPosX = this.chasingPosX;
      }

      if (var5 > 10.0D) {
         this.chasingPosZ = this.posZ;
         this.prevChasingPosZ = this.chasingPosZ;
      }

      if (var3 > 10.0D) {
         this.chasingPosY = this.posY;
         this.prevChasingPosY = this.chasingPosY;
      }

      if (var1 < -10.0D) {
         this.chasingPosX = this.posX;
         this.prevChasingPosX = this.chasingPosX;
      }

      if (var5 < -10.0D) {
         this.chasingPosZ = this.posZ;
         this.prevChasingPosZ = this.chasingPosZ;
      }

      if (var3 < -10.0D) {
         this.chasingPosY = this.posY;
         this.prevChasingPosY = this.chasingPosY;
      }

      this.chasingPosX += var1 * 0.25D;
      this.chasingPosZ += var5 * 0.25D;
      this.chasingPosY += var3 * 0.25D;
   }

   protected void updateSize() {
      float var1;
      float var2;
      if (this.isElytraFlying()) {
         var1 = 0.6F;
         var2 = 0.6F;
      } else if (this.isPlayerSleeping()) {
         var1 = 0.2F;
         var2 = 0.2F;
      } else if (this.isSneaking()) {
         var1 = 0.6F;
         var2 = 1.65F;
      } else {
         var1 = 0.6F;
         var2 = 1.8F;
      }

      if (var1 != this.width || var2 != this.height) {
         AxisAlignedBB var3 = this.getEntityBoundingBox();
         var3 = new AxisAlignedBB(var3.minX, var3.minY, var3.minZ, var3.minX + (double)var1, var3.minY + (double)var2, var3.minZ + (double)var1);
         if (!this.world.collidesWithAnyBlock(var3)) {
            this.setSize(var1, var2);
         }
      }

      FMLCommonHandler.instance().onPlayerPostTick(this);
   }

   public int getMaxInPortalTime() {
      return this.capabilities.disableDamage ? 1 : 80;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_PLAYER_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_PLAYER_SPLASH;
   }

   public int getPortalCooldown() {
      return 10;
   }

   public void playSound(SoundEvent var1, float var2, float var3) {
      this.world.playSound(this, this.posX, this.posY, this.posZ, var1, this.getSoundCategory(), var2, var3);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.PLAYERS;
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 9) {
         this.onItemUseFinish();
      } else if (var1 == 23) {
         this.hasReducedDebug = false;
      } else if (var1 == 22) {
         this.hasReducedDebug = true;
      } else {
         super.handleStatusUpdate(var1);
      }

   }

   protected boolean isMovementBlocked() {
      return this.getHealth() <= 0.0F || this.isPlayerSleeping();
   }

   public void closeScreen() {
      this.openContainer = this.inventoryContainer;
   }

   public void updateRidden() {
      if (!this.world.isRemote && this.isSneaking() && this.isRiding()) {
         this.dismountRidingEntity();
         this.setSneaking(false);
      } else {
         double var1 = this.posX;
         double var3 = this.posY;
         double var5 = this.posZ;
         float var7 = this.rotationYaw;
         float var8 = this.rotationPitch;
         super.updateRidden();
         this.prevCameraYaw = this.cameraYaw;
         this.cameraYaw = 0.0F;
         this.addMountedMovementStat(this.posX - var1, this.posY - var3, this.posZ - var5);
         if (this.getRidingEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.getRidingEntity()).shouldRiderFaceForward(this)) {
            this.rotationPitch = var8;
            this.rotationYaw = var7;
            this.renderYawOffset = ((EntityLivingBase)this.getRidingEntity()).renderYawOffset;
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void preparePlayerToSpawn() {
      this.setSize(0.6F, 1.8F);
      super.preparePlayerToSpawn();
      this.setHealth(this.getMaxHealth());
      this.deathTime = 0;
   }

   protected void updateEntityActionState() {
      super.updateEntityActionState();
      this.updateArmSwingProgress();
      this.rotationYawHead = this.rotationYaw;
   }

   public void onLivingUpdate() {
      if (this.flyToggleTimer > 0) {
         --this.flyToggleTimer;
      }

      if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.world.getGameRules().getBoolean("naturalRegeneration")) {
         if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 == 0) {
            this.heal(1.0F);
         }

         if (this.foodStats.needFood() && this.ticksExisted % 10 == 0) {
            this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
         }
      }

      this.inventory.decrementAnimations();
      this.prevCameraYaw = this.cameraYaw;
      super.onLivingUpdate();
      IAttributeInstance var1 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (!this.world.isRemote) {
         var1.setBaseValue((double)this.capabilities.getWalkSpeed());
      }

      this.jumpMovementFactor = this.speedInAir;
      if (this.isSprinting()) {
         this.jumpMovementFactor = (float)((double)this.jumpMovementFactor + (double)this.speedInAir * 0.3D);
      }

      this.setAIMoveSpeed((float)var1.getAttributeValue());
      float var2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      float var3 = (float)(Math.atan(-this.motionY * 0.20000000298023224D) * 15.0D);
      if (var2 > 0.1F) {
         var2 = 0.1F;
      }

      if (!this.onGround || this.getHealth() <= 0.0F) {
         var2 = 0.0F;
      }

      if (this.onGround || this.getHealth() <= 0.0F) {
         var3 = 0.0F;
      }

      this.cameraYaw += (var2 - this.cameraYaw) * 0.4F;
      this.cameraPitch += (var3 - this.cameraPitch) * 0.8F;
      if (this.getHealth() > 0.0F && !this.isSpectator()) {
         AxisAlignedBB var4;
         if (this.isRiding() && !this.getRidingEntity().isDead) {
            var4 = this.getEntityBoundingBox().union(this.getRidingEntity().getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);
         } else {
            var4 = this.getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
         }

         List var5 = this.world.getEntitiesWithinAABBExcludingEntity(this, var4);

         for(int var6 = 0; var6 < var5.size(); ++var6) {
            Entity var7 = (Entity)var5.get(var6);
            if (!var7.isDead) {
               this.collideWithPlayer(var7);
            }
         }
      }

   }

   private void collideWithPlayer(Entity var1) {
      var1.onCollideWithPlayer(this);
   }

   public int getScore() {
      return ((Integer)this.dataManager.get(PLAYER_SCORE)).intValue();
   }

   public void setScore(int var1) {
      this.dataManager.set(PLAYER_SCORE, Integer.valueOf(var1));
   }

   public void addScore(int var1) {
      int var2 = this.getScore();
      this.dataManager.set(PLAYER_SCORE, Integer.valueOf(var2 + var1));
   }

   public void onDeath(DamageSource var1) {
      if (!ForgeHooks.onLivingDeath(this, var1)) {
         super.onDeath(var1);
         this.setSize(0.2F, 0.2F);
         this.setPosition(this.posX, this.posY, this.posZ);
         this.motionY = 0.10000000149011612D;
         this.captureDrops = true;
         this.capturedDrops.clear();
         if ("Notch".equals(this.getName())) {
            this.dropItem(new ItemStack(Items.APPLE, 1), true, false);
         }

         if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            this.closeScreen();
            this.inventory.dropAllItems();
         }

         this.captureDrops = false;
         if (!this.world.isRemote) {
            ForgeEventFactory.onPlayerDrops(this, var1, this.capturedDrops, this.recentlyHit > 0);
         }

         if (var1 != null) {
            this.motionX = (double)(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
            this.motionZ = (double)(-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
         } else {
            this.motionX = 0.0D;
            this.motionZ = 0.0D;
         }

         this.addStat(StatList.DEATHS);
         this.takeStat(StatList.TIME_SINCE_DEATH);
      }
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_PLAYER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PLAYER_DEATH;
   }

   public void addToPlayerScore(Entity var1, int var2) {
      if (var1 != this) {
         this.addScore(var2);
         Collection var3 = this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TOTAL_KILL_COUNT);
         if (var1 instanceof EntityPlayer) {
            this.addStat(StatList.PLAYER_KILLS);
            var3.addAll(this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.PLAYER_KILL_COUNT));
         } else {
            this.addStat(StatList.MOB_KILLS);
         }

         var3.addAll(this.giveTeamKillScores(var1));

         for(ScoreObjective var5 : var3) {
            this.getWorldScoreboard().getOrCreateScore(this.getName(), var5).incrementScore();
         }
      }

   }

   private Collection giveTeamKillScores(Entity var1) {
      String var2 = var1 instanceof EntityPlayer ? var1.getName() : var1.getCachedUniqueIdString();
      ScorePlayerTeam var3 = this.getWorldScoreboard().getPlayersTeam(this.getName());
      if (var3 != null) {
         int var4 = var3.getChatFormat().getColorIndex();
         if (var4 >= 0 && var4 < IScoreCriteria.KILLED_BY_TEAM.length) {
            for(ScoreObjective var6 : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.KILLED_BY_TEAM[var4])) {
               Score var7 = this.getWorldScoreboard().getOrCreateScore(var2, var6);
               var7.incrementScore();
            }
         }
      }

      ScorePlayerTeam var8 = this.getWorldScoreboard().getPlayersTeam(var2);
      if (var8 != null) {
         int var9 = var8.getChatFormat().getColorIndex();
         if (var9 >= 0 && var9 < IScoreCriteria.TEAM_KILL.length) {
            return this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TEAM_KILL[var9]);
         }
      }

      return Lists.newArrayList();
   }

   @Nullable
   public EntityItem dropItem(boolean var1) {
      ItemStack var2 = this.inventory.getCurrentItem();
      if (var2 == null) {
         return null;
      } else if (!var2.getItem().onDroppedByPlayer(var2, this)) {
         return null;
      } else {
         int var3 = var1 && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1;
         return ForgeHooks.onPlayerTossEvent(this, this.inventory.decrStackSize(this.inventory.currentItem, var3), true);
      }
   }

   @Nullable
   public EntityItem dropItem(@Nullable ItemStack var1, boolean var2) {
      return ForgeHooks.onPlayerTossEvent(this, var1, false);
   }

   @Nullable
   public EntityItem dropItem(@Nullable ItemStack var1, boolean var2, boolean var3) {
      if (var1 == null) {
         return null;
      } else if (var1.stackSize == 0) {
         return null;
      } else {
         double var4 = this.posY - 0.30000001192092896D + (double)this.getEyeHeight();
         EntityItem var6 = new EntityItem(this.world, this.posX, var4, this.posZ, var1);
         var6.setPickupDelay(40);
         if (var3) {
            var6.setThrower(this.getName());
         }

         if (var2) {
            float var7 = this.rand.nextFloat() * 0.5F;
            float var8 = this.rand.nextFloat() * 6.2831855F;
            var6.motionX = (double)(-MathHelper.sin(var8) * var7);
            var6.motionZ = (double)(MathHelper.cos(var8) * var7);
            var6.motionY = 0.20000000298023224D;
         } else {
            float var9 = 0.3F;
            var6.motionX = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * var9);
            var6.motionZ = (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * var9);
            var6.motionY = (double)(-MathHelper.sin(this.rotationPitch * 0.017453292F) * var9 + 0.1F);
            float var12 = this.rand.nextFloat() * 6.2831855F;
            var9 = 0.02F * this.rand.nextFloat();
            var6.motionX += Math.cos((double)var12) * (double)var9;
            var6.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
            var6.motionZ += Math.sin((double)var12) * (double)var9;
         }

         ItemStack var11 = this.dropItemAndGetStack(var6);
         if (var3) {
            if (var11 != null) {
               this.addStat(StatList.getDroppedObjectStats(var11.getItem()), var1.stackSize);
            }

            this.addStat(StatList.DROP);
         }

         return var6;
      }
   }

   @Nullable
   public ItemStack dropItemAndGetStack(EntityItem var1) {
      if (this.captureDrops) {
         this.capturedDrops.add(var1);
      } else {
         this.world.spawnEntity(var1);
      }

      return var1.getEntityItem();
   }

   /** @deprecated */
   @Deprecated
   public float getDigSpeed(IBlockState var1) {
      return this.getDigSpeed(var1, (BlockPos)null);
   }

   public float getDigSpeed(IBlockState var1, BlockPos var2) {
      float var3 = this.inventory.getStrVsBlock(var1);
      if (var3 > 1.0F) {
         int var4 = EnchantmentHelper.getEfficiencyModifier(this);
         ItemStack var5 = this.getHeldItemMainhand();
         if (var4 > 0 && var5 != null) {
            var3 += (float)(var4 * var4 + 1);
         }
      }

      if (this.isPotionActive(MobEffects.HASTE)) {
         var3 *= 1.0F + (float)(this.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
      }

      if (this.isPotionActive(MobEffects.MINING_FATIGUE)) {
         float var7;
         switch(this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
         case 0:
            var7 = 0.3F;
            break;
         case 1:
            var7 = 0.09F;
            break;
         case 2:
            var7 = 0.0027F;
            break;
         case 3:
         default:
            var7 = 8.1E-4F;
         }

         var3 *= var7;
      }

      if (this.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
         var3 /= 5.0F;
      }

      if (!this.onGround) {
         var3 /= 5.0F;
      }

      var3 = ForgeEventFactory.getBreakSpeed(this, var1, var3, var2);
      return var3 < 0.0F ? 0.0F : var3;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      return ForgeEventFactory.doPlayerHarvestCheck(this, var1, this.inventory.canHarvestBlock(var1));
   }

   public static void registerFixesPlayer(DataFixer var0) {
      var0.registerWalker(FixTypes.PLAYER, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            DataFixesManager.processInventory(var1, var2, var3, "Inventory");
            DataFixesManager.processInventory(var1, var2, var3, "EnderItems");
            return var2;
         }
      });
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setUniqueId(getUUID(this.gameProfile));
      NBTTagList var2 = var1.getTagList("Inventory", 10);
      this.inventory.readFromNBT(var2);
      this.inventory.currentItem = var1.getInteger("SelectedItemSlot");
      this.sleeping = var1.getBoolean("Sleeping");
      this.sleepTimer = var1.getShort("SleepTimer");
      this.experience = var1.getFloat("XpP");
      this.experienceLevel = var1.getInteger("XpLevel");
      this.experienceTotal = var1.getInteger("XpTotal");
      this.xpSeed = var1.getInteger("XpSeed");
      if (this.xpSeed == 0) {
         this.xpSeed = this.rand.nextInt();
      }

      this.setScore(var1.getInteger("Score"));
      if (this.sleeping) {
         this.bedLocation = new BlockPos(this);
         this.wakeUpPlayer(true, true, false);
      }

      if (var1.hasKey("SpawnX", 99) && var1.hasKey("SpawnY", 99) && var1.hasKey("SpawnZ", 99)) {
         this.spawnChunk = new BlockPos(var1.getInteger("SpawnX"), var1.getInteger("SpawnY"), var1.getInteger("SpawnZ"));
         this.spawnForced = var1.getBoolean("SpawnForced");
      }

      NBTTagList var3 = null;
      var3 = var1.getTagList("Spawns", 10);

      for(int var4 = 0; var4 < var3.tagCount(); ++var4) {
         NBTTagCompound var5 = var3.getCompoundTagAt(var4);
         int var6 = var5.getInteger("Dim");
         this.spawnChunkMap.put(Integer.valueOf(var6), new BlockPos(var5.getInteger("SpawnX"), var5.getInteger("SpawnY"), var5.getInteger("SpawnZ")));
         this.spawnForcedMap.put(Integer.valueOf(var6), Boolean.valueOf(var5.getBoolean("SpawnForced")));
      }

      this.foodStats.readNBT(var1);
      this.capabilities.readCapabilitiesFromNBT(var1);
      if (var1.hasKey("EnderItems", 9)) {
         NBTTagList var8 = var1.getTagList("EnderItems", 10);
         this.theInventoryEnderChest.loadInventoryFromNBT(var8);
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("DataVersion", 512);
      var1.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
      var1.setInteger("SelectedItemSlot", this.inventory.currentItem);
      var1.setBoolean("Sleeping", this.sleeping);
      var1.setShort("SleepTimer", (short)this.sleepTimer);
      var1.setFloat("XpP", this.experience);
      var1.setInteger("XpLevel", this.experienceLevel);
      var1.setInteger("XpTotal", this.experienceTotal);
      var1.setInteger("XpSeed", this.xpSeed);
      var1.setInteger("Score", this.getScore());
      if (this.spawnChunk != null) {
         var1.setInteger("SpawnX", this.spawnChunk.getX());
         var1.setInteger("SpawnY", this.spawnChunk.getY());
         var1.setInteger("SpawnZ", this.spawnChunk.getZ());
         var1.setBoolean("SpawnForced", this.spawnForced);
      }

      NBTTagList var2 = new NBTTagList();

      for(Entry var4 : this.spawnChunkMap.entrySet()) {
         BlockPos var5 = (BlockPos)var4.getValue();
         if (var5 != null) {
            Boolean var6 = (Boolean)this.spawnForcedMap.get(var4.getKey());
            if (var6 == null) {
               var6 = false;
            }

            NBTTagCompound var7 = new NBTTagCompound();
            var7.setInteger("Dim", ((Integer)var4.getKey()).intValue());
            var7.setInteger("SpawnX", var5.getX());
            var7.setInteger("SpawnY", var5.getY());
            var7.setInteger("SpawnZ", var5.getZ());
            var7.setBoolean("SpawnForced", var6.booleanValue());
            var2.appendTag(var7);
         }
      }

      var1.setTag("Spawns", var2);
      this.foodStats.writeNBT(var1);
      this.capabilities.writeCapabilitiesToNBT(var1);
      var1.setTag("EnderItems", this.theInventoryEnderChest.saveInventoryToNBT());
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!ForgeHooks.onLivingAttack(this, var1, var2)) {
         return false;
      } else if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (this.capabilities.disableDamage && !var1.canHarmInCreative()) {
         return false;
      } else {
         this.entityAge = 0;
         if (this.getHealth() <= 0.0F) {
            return false;
         } else {
            if (this.isPlayerSleeping() && !this.world.isRemote) {
               this.wakeUpPlayer(true, true, false);
            }

            if (var1.isDifficultyScaled()) {
               if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                  var2 = 0.0F;
               }

               if (this.world.getDifficulty() == EnumDifficulty.EASY) {
                  var2 = var2 / 2.0F + 1.0F;
               }

               if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                  var2 = var2 * 3.0F / 2.0F;
               }
            }

            return var2 == 0.0F ? false : super.attackEntityFrom(var1, var2);
         }
      }
   }

   public boolean canAttackPlayer(EntityPlayer var1) {
      Team var2 = this.getTeam();
      Team var3 = var1.getTeam();
      return var2 == null ? true : (!var2.isSameTeam(var3) ? true : var2.getAllowFriendlyFire());
   }

   protected void damageArmor(float var1) {
      this.inventory.damageArmor(var1);
   }

   protected void damageShield(float var1) {
      if (var1 >= 3.0F && this.activeItemStack != null && this.activeItemStack.getItem() == Items.SHIELD) {
         int var2 = 1 + MathHelper.floor(var1);
         this.activeItemStack.damageItem(var2, this);
         if (this.activeItemStack.stackSize <= 0) {
            EnumHand var3 = this.getActiveHand();
            ForgeEventFactory.onPlayerDestroyItem(this, this.activeItemStack, var3);
            if (var3 == EnumHand.MAIN_HAND) {
               this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, (ItemStack)null);
            } else {
               this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, (ItemStack)null);
            }

            this.activeItemStack = null;
            this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
         }
      }

   }

   public float getArmorVisibility() {
      int var1 = 0;

      for(ItemStack var5 : this.inventory.armorInventory) {
         if (var5 != null) {
            ++var1;
         }
      }

      return (float)var1 / (float)this.inventory.armorInventory.length;
   }

   protected void damageEntity(DamageSource var1, float var2) {
      if (!this.isEntityInvulnerable(var1)) {
         var2 = ForgeHooks.onLivingHurt(this, var1, var2);
         if (var2 <= 0.0F) {
            return;
         }

         var2 = ArmorProperties.applyArmor(this, this.inventory.armorInventory, var1, (double)var2);
         if (var2 <= 0.0F) {
            return;
         }

         var2 = this.applyPotionDamageCalculations(var1, var2);
         float var3 = var2;
         var2 = Math.max(var2 - this.getAbsorptionAmount(), 0.0F);
         this.setAbsorptionAmount(this.getAbsorptionAmount() - (var3 - var2));
         if (var2 != 0.0F) {
            this.addExhaustion(var1.getHungerDamage());
            float var4 = this.getHealth();
            this.setHealth(this.getHealth() - var2);
            this.getCombatTracker().trackDamage(var1, var4, var2);
            if (var2 < 3.4028235E37F) {
               this.addStat(StatList.DAMAGE_TAKEN, Math.round(var2 * 10.0F));
            }
         }
      }

   }

   public void openEditSign(TileEntitySign var1) {
   }

   public void displayGuiEditCommandCart(CommandBlockBaseLogic var1) {
   }

   public void displayGuiCommandBlock(TileEntityCommandBlock var1) {
   }

   public void openEditStructure(TileEntityStructure var1) {
   }

   public void displayVillagerTradeGui(IMerchant var1) {
   }

   public void displayGUIChest(IInventory var1) {
   }

   public void openGuiHorseInventory(EntityHorse var1, IInventory var2) {
   }

   public void displayGui(IInteractionObject var1) {
   }

   public void openBook(ItemStack var1, EnumHand var2) {
   }

   public EnumActionResult interact(Entity var1, @Nullable ItemStack var2, EnumHand var3) {
      if (this.isSpectator()) {
         if (var1 instanceof IInventory) {
            this.displayGUIChest((IInventory)var1);
         }

         return EnumActionResult.PASS;
      } else if (ForgeHooks.onInteractEntity(this, var1, var2, var3)) {
         return EnumActionResult.PASS;
      } else {
         ItemStack var4 = var2 != null ? var2.copy() : null;
         if (!var1.processInitialInteract(this, var2, var3)) {
            if (var2 != null && var1 instanceof EntityLivingBase) {
               if (this.capabilities.isCreativeMode) {
                  var2 = var4;
               }

               if (var2.interactWithEntity(this, (EntityLivingBase)var1, var3)) {
                  if (var2.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                     ForgeEventFactory.onPlayerDestroyItem(this, var2, var3);
                     this.setHeldItem(var3, (ItemStack)null);
                  }

                  return EnumActionResult.SUCCESS;
               }
            }

            return EnumActionResult.PASS;
         } else {
            if (var2 != null && var2 == this.getHeldItem(var3)) {
               if (var2.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                  ForgeEventFactory.onPlayerDestroyItem(this, var2, var3);
                  this.setHeldItem(var3, (ItemStack)null);
               } else if (var2.stackSize < var4.stackSize && this.capabilities.isCreativeMode) {
                  var2.stackSize = var4.stackSize;
               }
            }

            return EnumActionResult.SUCCESS;
         }
      }
   }

   public double getYOffset() {
      return -0.35D;
   }

   public void dismountRidingEntity() {
      super.dismountRidingEntity();
      this.rideCooldown = 0;
   }

   public void attackTargetEntityWithCurrentItem(Entity var1) {
      if (ForgeHooks.onPlayerAttackTarget(this, var1)) {
         if (var1.canBeAttackedWithItem() && !var1.hitByEntity(this)) {
            float var2 = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            float var3;
            if (var1 instanceof EntityLivingBase) {
               var3 = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)var1).getCreatureAttribute());
            } else {
               var3 = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
            }

            float var4 = this.getCooledAttackStrength(0.5F);
            var2 = var2 * (0.2F + var4 * var4 * 0.8F);
            var3 = var3 * var4;
            this.resetCooldown();
            if (var2 > 0.0F || var3 > 0.0F) {
               boolean var5 = var4 > 0.9F;
               boolean var6 = false;
               int var7 = 0;
               var7 = var7 + EnchantmentHelper.getKnockbackModifier(this);
               if (this.isSprinting() && var5) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0F, 1.0F);
                  ++var7;
                  var6 = true;
               }

               boolean var8 = var5 && this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(MobEffects.BLINDNESS) && !this.isRiding() && var1 instanceof EntityLivingBase;
               var8 = var8 && !this.isSprinting();
               if (var8) {
                  var2 *= 1.5F;
               }

               var2 = var2 + var3;
               boolean var9 = false;
               double var10 = (double)(this.distanceWalkedModified - this.prevDistanceWalkedModified);
               if (var5 && !var8 && !var6 && this.onGround && var10 < (double)this.getAIMoveSpeed()) {
                  ItemStack var12 = this.getHeldItem(EnumHand.MAIN_HAND);
                  if (var12 != null && var12.getItem() instanceof ItemSword) {
                     var9 = true;
                  }
               }

               float var31 = 0.0F;
               boolean var13 = false;
               int var14 = EnchantmentHelper.getFireAspectModifier(this);
               if (var1 instanceof EntityLivingBase) {
                  var31 = ((EntityLivingBase)var1).getHealth();
                  if (var14 > 0 && !var1.isBurning()) {
                     var13 = true;
                     var1.setFire(1);
                  }
               }

               double var15 = var1.motionX;
               double var17 = var1.motionY;
               double var19 = var1.motionZ;
               boolean var21 = var1.attackEntityFrom(DamageSource.causePlayerDamage(this), var2);
               if (var21) {
                  if (var7 > 0) {
                     if (var1 instanceof EntityLivingBase) {
                        ((EntityLivingBase)var1).knockBack(this, (float)var7 * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                     } else {
                        var1.addVelocity((double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * (float)var7 * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * (float)var7 * 0.5F));
                     }

                     this.motionX *= 0.6D;
                     this.motionZ *= 0.6D;
                     this.setSprinting(false);
                  }

                  if (var9) {
                     for(EntityLivingBase var23 : this.world.getEntitiesWithinAABB(EntityLivingBase.class, var1.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D))) {
                        if (var23 != this && var23 != var1 && !this.isOnSameTeam(var23) && this.getDistanceSqToEntity(var23) < 9.0D) {
                           var23.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                           var23.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F);
                        }
                     }

                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
                     this.spawnSweepParticles();
                  }

                  if (var1 instanceof EntityPlayerMP && var1.velocityChanged) {
                     ((EntityPlayerMP)var1).connection.sendPacket(new SPacketEntityVelocity(var1));
                     var1.velocityChanged = false;
                     var1.motionX = var15;
                     var1.motionY = var17;
                     var1.motionZ = var19;
                  }

                  if (var8) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
                     this.onCriticalHit(var1);
                  }

                  if (!var8 && !var9) {
                     if (var5) {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
                     } else {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, this.getSoundCategory(), 1.0F, 1.0F);
                     }
                  }

                  if (var3 > 0.0F) {
                     this.onEnchantmentCritical(var1);
                  }

                  if (!this.world.isRemote && var1 instanceof EntityPlayer) {
                     EntityPlayer var32 = (EntityPlayer)var1;
                     ItemStack var34 = this.getHeldItemMainhand();
                     ItemStack var24 = var32.isHandActive() ? var32.getActiveItemStack() : null;
                     if (var34 != null && var24 != null && var34.getItem() instanceof ItemAxe && var24.getItem() == Items.SHIELD) {
                        float var25 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
                        if (var6) {
                           var25 += 0.75F;
                        }

                        if (this.rand.nextFloat() < var25) {
                           var32.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                           this.world.setEntityState(var32, (byte)30);
                        }
                     }
                  }

                  if (var2 >= 18.0F) {
                     this.addStat(AchievementList.OVERKILL);
                  }

                  this.setLastAttacker(var1);
                  if (var1 instanceof EntityLivingBase) {
                     EnchantmentHelper.applyThornEnchantments((EntityLivingBase)var1, this);
                  }

                  EnchantmentHelper.applyArthropodEnchantments(this, var1);
                  ItemStack var33 = this.getHeldItemMainhand();
                  Object var35 = var1;
                  if (var1 instanceof EntityDragonPart) {
                     IEntityMultiPart var36 = ((EntityDragonPart)var1).entityDragonObj;
                     if (var36 instanceof EntityLivingBase) {
                        var35 = (EntityLivingBase)var36;
                     }
                  }

                  if (var33 != null && var35 instanceof EntityLivingBase) {
                     var33.hitEntity((EntityLivingBase)var35, this);
                     if (var33.stackSize <= 0) {
                        this.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                        ForgeEventFactory.onPlayerDestroyItem(this, var33, EnumHand.MAIN_HAND);
                     }
                  }

                  if (var1 instanceof EntityLivingBase) {
                     float var37 = var31 - ((EntityLivingBase)var1).getHealth();
                     this.addStat(StatList.DAMAGE_DEALT, Math.round(var37 * 10.0F));
                     if (var14 > 0) {
                        var1.setFire(var14 * 4);
                     }

                     if (this.world instanceof WorldServer && var37 > 2.0F) {
                        int var38 = (int)((double)var37 * 0.5D);
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, var1.posX, var1.posY + (double)(var1.height * 0.5F), var1.posZ, var38, 0.1D, 0.0D, 0.1D, 0.2D);
                     }
                  }

                  this.addExhaustion(0.3F);
               } else {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);
                  if (var13) {
                     var1.extinguish();
                  }
               }
            }
         }

      }
   }

   public void onCriticalHit(Entity var1) {
   }

   public void onEnchantmentCritical(Entity var1) {
   }

   public void spawnSweepParticles() {
      double var1 = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F));
      double var3 = (double)MathHelper.cos(this.rotationYaw * 0.017453292F);
      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX + var1, this.posY + (double)this.height * 0.5D, this.posZ + var3, 0, var1, 0.0D, var3, 0.0D);
      }

   }

   @SideOnly(Side.CLIENT)
   public void respawnPlayer() {
   }

   public void setDead() {
      super.setDead();
      this.inventoryContainer.onContainerClosed(this);
      if (this.openContainer != null) {
         this.openContainer.onContainerClosed(this);
      }

   }

   public boolean isEntityInsideOpaqueBlock() {
      return !this.sleeping && super.isEntityInsideOpaqueBlock();
   }

   public boolean isUser() {
      return false;
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }

   public EntityPlayer.SleepResult trySleep(BlockPos var1) {
      EntityPlayer.SleepResult var2 = ForgeEventFactory.onPlayerSleepInBed(this, var1);
      if (var2 != null) {
         return var2;
      } else {
         if (!this.world.isRemote) {
            if (this.isPlayerSleeping() || !this.isEntityAlive()) {
               return EntityPlayer.SleepResult.OTHER_PROBLEM;
            }

            if (!this.world.provider.isSurfaceWorld()) {
               return EntityPlayer.SleepResult.NOT_POSSIBLE_HERE;
            }

            if (this.world.isDaytime()) {
               return EntityPlayer.SleepResult.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(this.posX - (double)var1.getX()) > 3.0D || Math.abs(this.posY - (double)var1.getY()) > 2.0D || Math.abs(this.posZ - (double)var1.getZ()) > 3.0D) {
               return EntityPlayer.SleepResult.TOO_FAR_AWAY;
            }

            double var3 = 8.0D;
            double var5 = 5.0D;
            List var7 = this.world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)var1.getX() - 8.0D, (double)var1.getY() - 5.0D, (double)var1.getZ() - 8.0D, (double)var1.getX() + 8.0D, (double)var1.getY() + 5.0D, (double)var1.getZ() + 8.0D));
            if (!var7.isEmpty()) {
               return EntityPlayer.SleepResult.NOT_SAFE;
            }
         }

         if (this.isRiding()) {
            this.dismountRidingEntity();
         }

         this.setSize(0.2F, 0.2F);
         IBlockState var8 = null;
         if (this.world.isBlockLoaded(var1)) {
            var8 = this.world.getBlockState(var1);
         }

         if (var8 != null && var8.getBlock().isBed(var8, this.world, var1, this)) {
            EnumFacing var4 = var8.getBlock().getBedDirection(var8, this.world, var1);
            float var9 = 0.5F;
            float var6 = 0.5F;
            switch(var4) {
            case SOUTH:
               var6 = 0.9F;
               break;
            case NORTH:
               var6 = 0.1F;
               break;
            case WEST:
               var9 = 0.1F;
               break;
            case EAST:
               var9 = 0.9F;
            }

            this.setRenderOffsetForSleep(var4);
            this.setPosition((double)((float)var1.getX() + var9), (double)((float)var1.getY() + 0.6875F), (double)((float)var1.getZ() + var6));
         } else {
            this.setPosition((double)((float)var1.getX() + 0.5F), (double)((float)var1.getY() + 0.6875F), (double)((float)var1.getZ() + 0.5F));
         }

         this.sleeping = true;
         this.sleepTimer = 0;
         this.bedLocation = var1;
         this.motionX = 0.0D;
         this.motionY = 0.0D;
         this.motionZ = 0.0D;
         if (!this.world.isRemote) {
            this.world.updateAllPlayersSleepingFlag();
         }

         return EntityPlayer.SleepResult.OK;
      }
   }

   private void setRenderOffsetForSleep(EnumFacing var1) {
      this.renderOffsetX = 0.0F;
      this.renderOffsetZ = 0.0F;
      switch(var1) {
      case SOUTH:
         this.renderOffsetZ = -1.8F;
         break;
      case NORTH:
         this.renderOffsetZ = 1.8F;
         break;
      case WEST:
         this.renderOffsetX = 1.8F;
         break;
      case EAST:
         this.renderOffsetX = -1.8F;
      }

   }

   public void wakeUpPlayer(boolean var1, boolean var2, boolean var3) {
      ForgeEventFactory.onPlayerWakeup(this, var1, var2, var3);
      this.setSize(0.6F, 1.8F);
      IBlockState var4 = this.world.getBlockState(this.bedLocation);
      if (this.bedLocation != null && var4.getBlock().isBed(var4, this.world, this.bedLocation, this)) {
         var4.getBlock().setBedOccupied(this.world, this.bedLocation, this, false);
         BlockPos var5 = var4.getBlock().getBedSpawnPosition(var4, this.world, this.bedLocation, this);
         if (var5 == null) {
            var5 = this.bedLocation.up();
         }

         this.setPosition((double)((float)var5.getX() + 0.5F), (double)((float)var5.getY() + 0.1F), (double)((float)var5.getZ() + 0.5F));
      } else {
         var3 = false;
      }

      this.sleeping = false;
      if (!this.world.isRemote && var2) {
         this.world.updateAllPlayersSleepingFlag();
      }

      this.sleepTimer = var1 ? 0 : 100;
      if (var3) {
         this.setSpawnPoint(this.bedLocation, false);
      }

   }

   private boolean isInBed() {
      return ForgeEventFactory.fireSleepingLocationCheck(this, this.bedLocation);
   }

   @Nullable
   public static BlockPos getBedSpawnLocation(World var0, BlockPos var1, boolean var2) {
      IBlockState var3 = var0.getBlockState(var1);
      Block var4 = var3.getBlock();
      if (!var4.isBed(var3, var0, var1, (Entity)null)) {
         if (!var2) {
            return null;
         } else {
            boolean var5 = var4.canSpawnInBlock();
            boolean var6 = var0.getBlockState(var1.up()).getBlock().canSpawnInBlock();
            return var5 && var6 ? var1 : null;
         }
      } else {
         return var4.getBedSpawnPosition(var3, var0, var1, (EntityPlayer)null);
      }
   }

   @SideOnly(Side.CLIENT)
   public float getBedOrientationInDegrees() {
      IBlockState var1 = this.bedLocation == null ? null : this.world.getBlockState(this.bedLocation);
      if (var1 != null && var1.getBlock().isBed(var1, this.world, this.bedLocation, this)) {
         EnumFacing var2 = var1.getBlock().getBedDirection(var1, this.world, this.bedLocation);
         switch(var2) {
         case SOUTH:
            return 90.0F;
         case NORTH:
            return 270.0F;
         case WEST:
            return 0.0F;
         case EAST:
            return 180.0F;
         }
      }

      return 0.0F;
   }

   public boolean isPlayerSleeping() {
      return this.sleeping;
   }

   public boolean isPlayerFullyAsleep() {
      return this.sleeping && this.sleepTimer >= 100;
   }

   @SideOnly(Side.CLIENT)
   public int getSleepTimer() {
      return this.sleepTimer;
   }

   public void sendStatusMessage(ITextComponent var1) {
   }

   public BlockPos getBedLocation() {
      return this.getBedLocation(this.dimension);
   }

   /** @deprecated */
   @Deprecated
   public boolean isSpawnForced() {
      return this.isSpawnForced(this.dimension);
   }

   public void setSpawnPoint(BlockPos var1, boolean var2) {
      if (!ForgeEventFactory.onPlayerSpawnSet(this, var1, var2)) {
         if (this.dimension != 0) {
            this.setSpawnChunk(var1, var2, this.dimension);
         } else {
            if (var1 != null) {
               this.spawnChunk = var1;
               this.spawnForced = var2;
            } else {
               this.spawnChunk = null;
               this.spawnForced = false;
            }

         }
      }
   }

   public boolean hasAchievement(Achievement var1) {
      return false;
   }

   public void addStat(StatBase var1) {
      this.addStat(var1, 1);
   }

   public void addStat(StatBase var1, int var2) {
   }

   public void takeStat(StatBase var1) {
   }

   public void jump() {
      super.jump();
      this.addStat(StatList.JUMP);
      if (this.isSprinting()) {
         this.addExhaustion(0.8F);
      } else {
         this.addExhaustion(0.2F);
      }

   }

   public void moveEntityWithHeading(float var1, float var2) {
      double var3 = this.posX;
      double var5 = this.posY;
      double var7 = this.posZ;
      if (this.capabilities.isFlying && !this.isRiding()) {
         double var9 = this.motionY;
         float var11 = this.jumpMovementFactor;
         this.jumpMovementFactor = this.capabilities.getFlySpeed() * (float)(this.isSprinting() ? 2 : 1);
         super.moveEntityWithHeading(var1, var2);
         this.motionY = var9 * 0.6D;
         this.jumpMovementFactor = var11;
         this.fallDistance = 0.0F;
         this.setFlag(7, false);
      } else {
         super.moveEntityWithHeading(var1, var2);
      }

      this.addMovementStat(this.posX - var3, this.posY - var5, this.posZ - var7);
   }

   public float getAIMoveSpeed() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
   }

   public void addMovementStat(double var1, double var3, double var5) {
      if (!this.isRiding()) {
         if (this.isInsideOfMaterial(Material.WATER)) {
            int var7 = Math.round(MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5) * 100.0F);
            if (var7 > 0) {
               this.addStat(StatList.DIVE_ONE_CM, var7);
               this.addExhaustion(0.015F * (float)var7 * 0.01F);
            }
         } else if (this.isInWater()) {
            int var8 = Math.round(MathHelper.sqrt(var1 * var1 + var5 * var5) * 100.0F);
            if (var8 > 0) {
               this.addStat(StatList.SWIM_ONE_CM, var8);
               this.addExhaustion(0.015F * (float)var8 * 0.01F);
            }
         } else if (this.isOnLadder()) {
            if (var3 > 0.0D) {
               this.addStat(StatList.CLIMB_ONE_CM, (int)Math.round(var3 * 100.0D));
            }
         } else if (this.onGround) {
            int var9 = Math.round(MathHelper.sqrt(var1 * var1 + var5 * var5) * 100.0F);
            if (var9 > 0) {
               if (this.isSprinting()) {
                  this.addStat(StatList.SPRINT_ONE_CM, var9);
                  this.addExhaustion(0.099999994F * (float)var9 * 0.01F);
               } else if (this.isSneaking()) {
                  this.addStat(StatList.CROUCH_ONE_CM, var9);
                  this.addExhaustion(0.005F * (float)var9 * 0.01F);
               } else {
                  this.addStat(StatList.WALK_ONE_CM, var9);
                  this.addExhaustion(0.01F * (float)var9 * 0.01F);
               }
            }
         } else if (this.isElytraFlying()) {
            int var10 = Math.round(MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5) * 100.0F);
            this.addStat(StatList.AVIATE_ONE_CM, var10);
         } else {
            int var11 = Math.round(MathHelper.sqrt(var1 * var1 + var5 * var5) * 100.0F);
            if (var11 > 25) {
               this.addStat(StatList.FLY_ONE_CM, var11);
            }
         }
      }

   }

   private void addMountedMovementStat(double var1, double var3, double var5) {
      if (this.isRiding()) {
         int var7 = Math.round(MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5) * 100.0F);
         if (var7 > 0) {
            if (this.getRidingEntity() instanceof EntityMinecart) {
               this.addStat(StatList.MINECART_ONE_CM, var7);
               if (this.startMinecartRidingCoordinate == null) {
                  this.startMinecartRidingCoordinate = new BlockPos(this);
               } else if (this.startMinecartRidingCoordinate.distanceSq((double)MathHelper.floor(this.posX), (double)MathHelper.floor(this.posY), (double)MathHelper.floor(this.posZ)) >= 1000000.0D) {
                  this.addStat(AchievementList.ON_A_RAIL);
               }
            } else if (this.getRidingEntity() instanceof EntityBoat) {
               this.addStat(StatList.BOAT_ONE_CM, var7);
            } else if (this.getRidingEntity() instanceof EntityPig) {
               this.addStat(StatList.PIG_ONE_CM, var7);
            } else if (this.getRidingEntity() instanceof EntityHorse) {
               this.addStat(StatList.HORSE_ONE_CM, var7);
            }
         }
      }

   }

   public void fall(float var1, float var2) {
      if (!this.capabilities.allowFlying) {
         if (var1 >= 2.0F) {
            this.addStat(StatList.FALL_ONE_CM, (int)Math.round((double)var1 * 100.0D));
         }

         super.fall(var1, var2);
      } else {
         ForgeEventFactory.onPlayerFall(this, var1, var2);
      }

   }

   protected void resetHeight() {
      if (!this.isSpectator()) {
         super.resetHeight();
      }

   }

   protected SoundEvent getFallSound(int var1) {
      return var1 > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
   }

   public void onKillEntity(EntityLivingBase var1) {
      if (var1 instanceof IMob) {
         this.addStat(AchievementList.KILL_ENEMY);
      }

      EntityList.EntityEggInfo var2 = (EntityList.EntityEggInfo)EntityList.ENTITY_EGGS.get(EntityList.getEntityString(var1));
      if (var2 != null) {
         this.addStat(var2.killEntityStat);
      }

   }

   public void setInWeb() {
      if (!this.capabilities.isFlying) {
         super.setInWeb();
      }

   }

   public void addExperience(int var1) {
      this.addScore(var1);
      int var2 = Integer.MAX_VALUE - this.experienceTotal;
      if (var1 > var2) {
         var1 = var2;
      }

      this.experience += (float)var1 / (float)this.xpBarCap();

      for(this.experienceTotal += var1; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap()) {
         this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
         this.addExperienceLevel(1);
      }

   }

   public int getXPSeed() {
      return this.xpSeed;
   }

   public void removeExperienceLevel(int var1) {
      this.experienceLevel -= var1;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experience = 0.0F;
         this.experienceTotal = 0;
      }

      this.xpSeed = this.rand.nextInt();
   }

   public void addExperienceLevel(int var1) {
      this.experienceLevel += var1;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experience = 0.0F;
         this.experienceTotal = 0;
      }

      if (var1 > 0 && this.experienceLevel % 5 == 0 && (float)this.lastXPSound < (float)this.ticksExisted - 100.0F) {
         float var2 = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), var2 * 0.75F, 1.0F);
         this.lastXPSound = this.ticksExisted;
      }

   }

   public int xpBarCap() {
      return this.experienceLevel >= 30 ? 112 + (this.experienceLevel - 30) * 9 : (this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2);
   }

   public void addExhaustion(float var1) {
      if (!this.capabilities.disableDamage && !this.world.isRemote) {
         this.foodStats.addExhaustion(var1);
      }

   }

   public FoodStats getFoodStats() {
      return this.foodStats;
   }

   public boolean canEat(boolean var1) {
      return (var1 || this.foodStats.needFood()) && !this.capabilities.disableDamage;
   }

   public boolean shouldHeal() {
      return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
   }

   public boolean isAllowEdit() {
      return this.capabilities.allowEdit;
   }

   public boolean canPlayerEdit(BlockPos var1, EnumFacing var2, @Nullable ItemStack var3) {
      if (this.capabilities.allowEdit) {
         return true;
      } else if (var3 == null) {
         return false;
      } else {
         BlockPos var4 = var1.offset(var2.getOpposite());
         Block var5 = this.world.getBlockState(var4).getBlock();
         return var3.canPlaceOn(var5) || var3.canEditBlocks();
      }
   }

   protected int getExperiencePoints(EntityPlayer var1) {
      if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
         int var2 = this.experienceLevel * 7;
         return var2 > 100 ? 100 : var2;
      } else {
         return 0;
      }
   }

   protected boolean isPlayer() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean getAlwaysRenderNameTagForRender() {
      return true;
   }

   public void clonePlayer(EntityPlayer var1, boolean var2) {
      if (var2) {
         this.inventory.copyInventory(var1.inventory);
         this.setHealth(var1.getHealth());
         this.foodStats = var1.foodStats;
         this.experienceLevel = var1.experienceLevel;
         this.experienceTotal = var1.experienceTotal;
         this.experience = var1.experience;
         this.setScore(var1.getScore());
         this.lastPortalPos = var1.lastPortalPos;
         this.lastPortalVec = var1.lastPortalVec;
         this.teleportDirection = var1.teleportDirection;
      } else if (this.world.getGameRules().getBoolean("keepInventory") || var1.isSpectator()) {
         this.inventory.copyInventory(var1.inventory);
         this.experienceLevel = var1.experienceLevel;
         this.experienceTotal = var1.experienceTotal;
         this.experience = var1.experience;
         this.setScore(var1.getScore());
      }

      this.xpSeed = var1.xpSeed;
      this.theInventoryEnderChest = var1.theInventoryEnderChest;
      this.getDataManager().set(PLAYER_MODEL_FLAG, var1.getDataManager().get(PLAYER_MODEL_FLAG));
      this.spawnChunkMap = var1.spawnChunkMap;
      this.spawnForcedMap = var1.spawnForcedMap;
      NBTTagCompound var3 = var1.getEntityData();
      if (var3.hasKey("PlayerPersisted")) {
         this.getEntityData().setTag("PlayerPersisted", var3.getCompoundTag("PlayerPersisted"));
      }

      ForgeEventFactory.onPlayerClone(this, var1, !var2);
   }

   protected boolean canTriggerWalking() {
      return !this.capabilities.isFlying;
   }

   public void sendPlayerAbilities() {
   }

   public void setGameType(GameType var1) {
   }

   public String getName() {
      return this.gameProfile.getName();
   }

   public InventoryEnderChest getInventoryEnderChest() {
      return this.theInventoryEnderChest;
   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityEquipmentSlot var1) {
      return var1 == EntityEquipmentSlot.MAINHAND ? this.inventory.getCurrentItem() : (var1 == EntityEquipmentSlot.OFFHAND ? this.inventory.offHandInventory[0] : (var1.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? this.inventory.armorInventory[var1.getIndex()] : null));
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2) {
      if (var1 == EntityEquipmentSlot.MAINHAND) {
         this.playEquipSound(var2);
         this.inventory.mainInventory[this.inventory.currentItem] = var2;
      } else if (var1 == EntityEquipmentSlot.OFFHAND) {
         this.playEquipSound(var2);
         this.inventory.offHandInventory[0] = var2;
      } else if (var1.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
         this.playEquipSound(var2);
         this.inventory.armorInventory[var1.getIndex()] = var2;
      }

   }

   public Iterable getHeldEquipment() {
      return Lists.newArrayList(new ItemStack[]{this.getHeldItemMainhand(), this.getHeldItemOffhand()});
   }

   public Iterable getArmorInventoryList() {
      return Arrays.asList(this.inventory.armorInventory);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInvisibleToPlayer(EntityPlayer var1) {
      if (!this.isInvisible()) {
         return false;
      } else if (var1.isSpectator()) {
         return false;
      } else {
         Team var2 = this.getTeam();
         return var2 == null || var1 == null || var1.getTeam() != var2 || !var2.getSeeFriendlyInvisiblesEnabled();
      }
   }

   public abstract boolean isSpectator();

   public abstract boolean isCreative();

   public boolean isPushedByWater() {
      return !this.capabilities.isFlying;
   }

   public Scoreboard getWorldScoreboard() {
      return this.world.getScoreboard();
   }

   public Team getTeam() {
      return this.getWorldScoreboard().getPlayersTeam(this.getName());
   }

   public ITextComponent getDisplayName() {
      TextComponentString var1 = new TextComponentString("");
      if (!this.prefixes.isEmpty()) {
         for(ITextComponent var3 : this.prefixes) {
            var1.appendSibling(var3);
         }
      }

      var1.appendSibling(new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getDisplayNameString())));
      if (!this.suffixes.isEmpty()) {
         for(ITextComponent var5 : this.suffixes) {
            var1.appendSibling(var5);
         }
      }

      var1.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.getName() + " "));
      var1.getStyle().setHoverEvent(this.getHoverEvent());
      var1.getStyle().setInsertion(this.getName());
      return var1;
   }

   public float getEyeHeight() {
      float var1 = this.eyeHeight;
      if (this.isPlayerSleeping()) {
         var1 = 0.2F;
      } else if (!this.isSneaking() && this.height != 1.65F) {
         if (this.isElytraFlying() || this.height == 0.6F) {
            var1 = 0.4F;
         }
      } else {
         var1 -= 0.08F;
      }

      return var1;
   }

   public void setAbsorptionAmount(float var1) {
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      this.getDataManager().set(ABSORPTION, Float.valueOf(var1));
   }

   public float getAbsorptionAmount() {
      return ((Float)this.getDataManager().get(ABSORPTION)).floatValue();
   }

   public static UUID getUUID(GameProfile var0) {
      UUID var1 = var0.getId();
      if (var1 == null) {
         var1 = getOfflineUUID(var0.getName());
      }

      return var1;
   }

   public static UUID getOfflineUUID(String var0) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + var0).getBytes(Charsets.UTF_8));
   }

   public boolean canOpen(LockCode var1) {
      if (var1.isEmpty()) {
         return true;
      } else {
         ItemStack var2 = this.getHeldItemMainhand();
         return var2 != null && var2.hasDisplayName() ? var2.getDisplayName().equals(var1.getLock()) : false;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean isWearing(EnumPlayerModelParts var1) {
      return (((Byte)this.getDataManager().get(PLAYER_MODEL_FLAG)).byteValue() & var1.getPartMask()) == var1.getPartMask();
   }

   public boolean sendCommandFeedback() {
      return this.getServer().worlds[0].getGameRules().getBoolean("sendCommandFeedback");
   }

   public boolean replaceItemInInventory(int var1, ItemStack var2) {
      if (var1 >= 0 && var1 < this.inventory.mainInventory.length) {
         this.inventory.setInventorySlotContents(var1, var2);
         return true;
      } else {
         EntityEquipmentSlot var3;
         if (var1 == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
            var3 = EntityEquipmentSlot.HEAD;
         } else if (var1 == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
            var3 = EntityEquipmentSlot.CHEST;
         } else if (var1 == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
            var3 = EntityEquipmentSlot.LEGS;
         } else if (var1 == 100 + EntityEquipmentSlot.FEET.getIndex()) {
            var3 = EntityEquipmentSlot.FEET;
         } else {
            var3 = null;
         }

         if (var1 == 98) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, var2);
            return true;
         } else if (var1 == 99) {
            this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, var2);
            return true;
         } else if (var3 == null) {
            int var4 = var1 - 200;
            if (var4 >= 0 && var4 < this.theInventoryEnderChest.getSizeInventory()) {
               this.theInventoryEnderChest.setInventorySlotContents(var4, var2);
               return true;
            } else {
               return false;
            }
         } else {
            if (var2 != null && var2.getItem() != null) {
               if (!(var2.getItem() instanceof ItemArmor) && !(var2.getItem() instanceof ItemElytra)) {
                  if (var3 != EntityEquipmentSlot.HEAD) {
                     return false;
                  }
               } else if (EntityLiving.getSlotForItemStack(var2) != var3) {
                  return false;
               }
            }

            this.inventory.setInventorySlotContents(var3.getIndex() + this.inventory.mainInventory.length, var2);
            return true;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean hasReducedDebug() {
      return this.hasReducedDebug;
   }

   @SideOnly(Side.CLIENT)
   public void setReducedDebug(boolean var1) {
      this.hasReducedDebug = var1;
   }

   public EnumHandSide getPrimaryHand() {
      return ((Byte)this.dataManager.get(MAIN_HAND)).byteValue() == 0 ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
   }

   public void setPrimaryHand(EnumHandSide var1) {
      this.dataManager.set(MAIN_HAND, Byte.valueOf((byte)(var1 == EnumHandSide.LEFT ? 0 : 1)));
   }

   public float getCooldownPeriod() {
      return (float)(1.0D / this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
   }

   public float getCooledAttackStrength(float var1) {
      return MathHelper.clamp(((float)this.ticksSinceLastSwing + var1) / this.getCooldownPeriod(), 0.0F, 1.0F);
   }

   public void resetCooldown() {
      this.ticksSinceLastSwing = 0;
   }

   public CooldownTracker getCooldownTracker() {
      return this.cooldownTracker;
   }

   public void applyEntityCollision(Entity var1) {
      if (!this.isPlayerSleeping()) {
         super.applyEntityCollision(var1);
      }

   }

   public float getLuck() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue();
   }

   public boolean canUseCommandBlock() {
      return this.capabilities.isCreativeMode && this.canUseCommand(2, "");
   }

   public void openGui(Object var1, int var2, World var3, int var4, int var5, int var6) {
      FMLNetworkHandler.openGui(this, var1, var2, var3, var4, var5, var6);
   }

   public BlockPos getBedLocation(int var1) {
      return var1 == 0 ? this.spawnChunk : (BlockPos)this.spawnChunkMap.get(Integer.valueOf(var1));
   }

   public boolean isSpawnForced(int var1) {
      if (var1 == 0) {
         return this.spawnForced;
      } else {
         Boolean var2 = (Boolean)this.spawnForcedMap.get(Integer.valueOf(var1));
         return var2 == null ? false : var2.booleanValue();
      }
   }

   public void setSpawnChunk(BlockPos var1, boolean var2, int var3) {
      if (var3 == 0) {
         if (var1 != null) {
            this.spawnChunk = var1;
            this.spawnForced = var2;
         } else {
            this.spawnChunk = null;
            this.spawnForced = false;
         }

      } else {
         if (var1 != null) {
            this.spawnChunkMap.put(Integer.valueOf(var3), var1);
            this.spawnForcedMap.put(Integer.valueOf(var3), Boolean.valueOf(var2));
         } else {
            this.spawnChunkMap.remove(Integer.valueOf(var3));
            this.spawnForcedMap.remove(Integer.valueOf(var3));
         }

      }
   }

   public float getDefaultEyeHeight() {
      return 1.62F;
   }

   public String getDisplayNameString() {
      if (this.displayname == null) {
         this.displayname = ForgeEventFactory.getPlayerDisplayName(this, this.getName());
      }

      return this.displayname;
   }

   public void refreshDisplayName() {
      this.displayname = ForgeEventFactory.getPlayerDisplayName(this, this.getName());
   }

   public void addPrefix(ITextComponent var1) {
      this.prefixes.add(var1);
   }

   public void addSuffix(ITextComponent var1) {
      this.suffixes.add(var1);
   }

   public Collection getPrefixes() {
      return this.prefixes;
   }

   public Collection getSuffixes() {
      return this.suffixes;
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      if (var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (var2 == null) {
            return this.playerJoinedHandler;
         }

         if (var2.getAxis().isVertical()) {
            return this.playerMainHandler;
         }

         if (var2.getAxis().isHorizontal()) {
            return this.playerEquipmentHandler;
         }
      }

      return super.getCapability(var1, var2);
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(var1, var2);
   }

   public static enum EnumChatVisibility {
      FULL(0, "options.chat.visibility.full"),
      SYSTEM(1, "options.chat.visibility.system"),
      HIDDEN(2, "options.chat.visibility.hidden");

      private static final EntityPlayer.EnumChatVisibility[] ID_LOOKUP = new EntityPlayer.EnumChatVisibility[values().length];
      private final int chatVisibility;
      private final String resourceKey;

      private EnumChatVisibility(int var3, String var4) {
         this.chatVisibility = var3;
         this.resourceKey = var4;
      }

      @SideOnly(Side.CLIENT)
      public int getChatVisibility() {
         return this.chatVisibility;
      }

      @SideOnly(Side.CLIENT)
      public static EntityPlayer.EnumChatVisibility getEnumChatVisibility(int var0) {
         return ID_LOOKUP[var0 % ID_LOOKUP.length];
      }

      @SideOnly(Side.CLIENT)
      public String getResourceKey() {
         return this.resourceKey;
      }

      static {
         for(EntityPlayer.EnumChatVisibility var3 : values()) {
            ID_LOOKUP[var3.chatVisibility] = var3;
         }

      }
   }

   public static enum SleepResult {
      OK,
      NOT_POSSIBLE_HERE,
      NOT_POSSIBLE_NOW,
      TOO_FAR_AWAY,
      OTHER_PROBLEM,
      NOT_SAFE;
   }
}
