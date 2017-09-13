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
      super(worldIn);
      this.playerMainHandler = new PlayerMainInvWrapper(this.inventory);
      this.playerEquipmentHandler = new CombinedInvWrapper(new IItemHandlerModifiable[]{new PlayerArmorInvWrapper(this.inventory), new PlayerOffhandInvWrapper(this.inventory)});
      this.playerJoinedHandler = new PlayerInvWrapper(this.inventory);
      this.setUniqueId(getUUID(gameProfileIn));
      this.gameProfile = gameProfileIn;
      this.inventoryContainer = new ContainerPlayer(this.inventory, !worldIn.isRemote, this);
      this.openContainer = this.inventoryContainer;
      BlockPos blockpos = worldIn.getSpawnPoint();
      this.setLocationAndAngles((double)blockpos.getX() + 0.5D, (double)(blockpos.getY() + 1), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
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

      int i = 29999999;
      double d0 = MathHelper.clamp(this.posX, -2.9999999E7D, 2.9999999E7D);
      double d1 = MathHelper.clamp(this.posZ, -2.9999999E7D, 2.9999999E7D);
      if (d0 != this.posX || d1 != this.posZ) {
         this.setPosition(d0, this.posY, d1);
      }

      ++this.ticksSinceLastSwing;
      ItemStack itemstack = this.getHeldItemMainhand();
      if (!ItemStack.areItemStacksEqual(this.itemStackMainHand, itemstack)) {
         if (!ItemStack.areItemsEqualIgnoreDurability(this.itemStackMainHand, itemstack)) {
            this.resetCooldown();
         }

         this.itemStackMainHand = itemstack == null ? null : itemstack.copy();
      }

      this.cooldownTracker.tick();
      this.updateSize();
   }

   private void updateCape() {
      this.prevChasingPosX = this.chasingPosX;
      this.prevChasingPosY = this.chasingPosY;
      this.prevChasingPosZ = this.chasingPosZ;
      double d0 = this.posX - this.chasingPosX;
      double d1 = this.posY - this.chasingPosY;
      double d2 = this.posZ - this.chasingPosZ;
      double d3 = 10.0D;
      if (d0 > 10.0D) {
         this.chasingPosX = this.posX;
         this.prevChasingPosX = this.chasingPosX;
      }

      if (d2 > 10.0D) {
         this.chasingPosZ = this.posZ;
         this.prevChasingPosZ = this.chasingPosZ;
      }

      if (d1 > 10.0D) {
         this.chasingPosY = this.posY;
         this.prevChasingPosY = this.chasingPosY;
      }

      if (d0 < -10.0D) {
         this.chasingPosX = this.posX;
         this.prevChasingPosX = this.chasingPosX;
      }

      if (d2 < -10.0D) {
         this.chasingPosZ = this.posZ;
         this.prevChasingPosZ = this.chasingPosZ;
      }

      if (d1 < -10.0D) {
         this.chasingPosY = this.posY;
         this.prevChasingPosY = this.chasingPosY;
      }

      this.chasingPosX += d0 * 0.25D;
      this.chasingPosZ += d2 * 0.25D;
      this.chasingPosY += d1 * 0.25D;
   }

   protected void updateSize() {
      float f;
      float f1;
      if (this.isElytraFlying()) {
         f = 0.6F;
         f1 = 0.6F;
      } else if (this.isPlayerSleeping()) {
         f = 0.2F;
         f1 = 0.2F;
      } else if (this.isSneaking()) {
         f = 0.6F;
         f1 = 1.65F;
      } else {
         f = 0.6F;
         f1 = 1.8F;
      }

      if (f != this.width || f1 != this.height) {
         AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
         axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)f, axisalignedbb.minY + (double)f1, axisalignedbb.minZ + (double)f);
         if (!this.world.collidesWithAnyBlock(axisalignedbb)) {
            this.setSize(f, f1);
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
      this.world.playSound(this, this.posX, this.posY, this.posZ, soundIn, this.getSoundCategory(), volume, pitch);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.PLAYERS;
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (id == 9) {
         this.onItemUseFinish();
      } else if (id == 23) {
         this.hasReducedDebug = false;
      } else if (id == 22) {
         this.hasReducedDebug = true;
      } else {
         super.handleStatusUpdate(id);
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
         double d0 = this.posX;
         double d1 = this.posY;
         double d2 = this.posZ;
         float f = this.rotationYaw;
         float f1 = this.rotationPitch;
         super.updateRidden();
         this.prevCameraYaw = this.cameraYaw;
         this.cameraYaw = 0.0F;
         this.addMountedMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
         if (this.getRidingEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.getRidingEntity()).shouldRiderFaceForward(this)) {
            this.rotationPitch = f1;
            this.rotationYaw = f;
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
      IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (!this.world.isRemote) {
         iattributeinstance.setBaseValue((double)this.capabilities.getWalkSpeed());
      }

      this.jumpMovementFactor = this.speedInAir;
      if (this.isSprinting()) {
         this.jumpMovementFactor = (float)((double)this.jumpMovementFactor + (double)this.speedInAir * 0.3D);
      }

      this.setAIMoveSpeed((float)iattributeinstance.getAttributeValue());
      float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      float f1 = (float)(Math.atan(-this.motionY * 0.20000000298023224D) * 15.0D);
      if (f > 0.1F) {
         f = 0.1F;
      }

      if (!this.onGround || this.getHealth() <= 0.0F) {
         f = 0.0F;
      }

      if (this.onGround || this.getHealth() <= 0.0F) {
         f1 = 0.0F;
      }

      this.cameraYaw += (f - this.cameraYaw) * 0.4F;
      this.cameraPitch += (f1 - this.cameraPitch) * 0.8F;
      if (this.getHealth() > 0.0F && !this.isSpectator()) {
         AxisAlignedBB axisalignedbb;
         if (this.isRiding() && !this.getRidingEntity().isDead) {
            axisalignedbb = this.getEntityBoundingBox().union(this.getRidingEntity().getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);
         } else {
            axisalignedbb = this.getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
         }

         List list = this.world.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);

         for(int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            if (!entity.isDead) {
               this.collideWithPlayer(entity);
            }
         }
      }

   }

   private void collideWithPlayer(Entity var1) {
      entityIn.onCollideWithPlayer(this);
   }

   public int getScore() {
      return ((Integer)this.dataManager.get(PLAYER_SCORE)).intValue();
   }

   public void setScore(int var1) {
      this.dataManager.set(PLAYER_SCORE, Integer.valueOf(scoreIn));
   }

   public void addScore(int var1) {
      int i = this.getScore();
      this.dataManager.set(PLAYER_SCORE, Integer.valueOf(i + scoreIn));
   }

   public void onDeath(DamageSource var1) {
      if (!ForgeHooks.onLivingDeath(this, cause)) {
         super.onDeath(cause);
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
            ForgeEventFactory.onPlayerDrops(this, cause, this.capturedDrops, this.recentlyHit > 0);
         }

         if (cause != null) {
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
      if (entityIn != this) {
         this.addScore(amount);
         Collection collection = this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TOTAL_KILL_COUNT);
         if (entityIn instanceof EntityPlayer) {
            this.addStat(StatList.PLAYER_KILLS);
            collection.addAll(this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.PLAYER_KILL_COUNT));
         } else {
            this.addStat(StatList.MOB_KILLS);
         }

         collection.addAll(this.giveTeamKillScores(entityIn));

         for(ScoreObjective scoreobjective : collection) {
            this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective).incrementScore();
         }
      }

   }

   private Collection giveTeamKillScores(Entity var1) {
      String s = p_175137_1_ instanceof EntityPlayer ? p_175137_1_.getName() : p_175137_1_.getCachedUniqueIdString();
      ScorePlayerTeam scoreplayerteam = this.getWorldScoreboard().getPlayersTeam(this.getName());
      if (scoreplayerteam != null) {
         int i = scoreplayerteam.getChatFormat().getColorIndex();
         if (i >= 0 && i < IScoreCriteria.KILLED_BY_TEAM.length) {
            for(ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.KILLED_BY_TEAM[i])) {
               Score score = this.getWorldScoreboard().getOrCreateScore(s, scoreobjective);
               score.incrementScore();
            }
         }
      }

      ScorePlayerTeam scoreplayerteam1 = this.getWorldScoreboard().getPlayersTeam(s);
      if (scoreplayerteam1 != null) {
         int j = scoreplayerteam1.getChatFormat().getColorIndex();
         if (j >= 0 && j < IScoreCriteria.TEAM_KILL.length) {
            return this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TEAM_KILL[j]);
         }
      }

      return Lists.newArrayList();
   }

   @Nullable
   public EntityItem dropItem(boolean var1) {
      ItemStack stack = this.inventory.getCurrentItem();
      if (stack == null) {
         return null;
      } else if (!stack.getItem().onDroppedByPlayer(stack, this)) {
         return null;
      } else {
         int count = dropAll && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1;
         return ForgeHooks.onPlayerTossEvent(this, this.inventory.decrStackSize(this.inventory.currentItem, count), true);
      }
   }

   @Nullable
   public EntityItem dropItem(@Nullable ItemStack var1, boolean var2) {
      return ForgeHooks.onPlayerTossEvent(this, itemStackIn, false);
   }

   @Nullable
   public EntityItem dropItem(@Nullable ItemStack var1, boolean var2, boolean var3) {
      if (droppedItem == null) {
         return null;
      } else if (droppedItem.stackSize == 0) {
         return null;
      } else {
         double d0 = this.posY - 0.30000001192092896D + (double)this.getEyeHeight();
         EntityItem entityitem = new EntityItem(this.world, this.posX, d0, this.posZ, droppedItem);
         entityitem.setPickupDelay(40);
         if (traceItem) {
            entityitem.setThrower(this.getName());
         }

         if (dropAround) {
            float f = this.rand.nextFloat() * 0.5F;
            float f1 = this.rand.nextFloat() * 6.2831855F;
            entityitem.motionX = (double)(-MathHelper.sin(f1) * f);
            entityitem.motionZ = (double)(MathHelper.cos(f1) * f);
            entityitem.motionY = 0.20000000298023224D;
         } else {
            float f2 = 0.3F;
            entityitem.motionX = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f2);
            entityitem.motionZ = (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f2);
            entityitem.motionY = (double)(-MathHelper.sin(this.rotationPitch * 0.017453292F) * f2 + 0.1F);
            float f3 = this.rand.nextFloat() * 6.2831855F;
            f2 = 0.02F * this.rand.nextFloat();
            entityitem.motionX += Math.cos((double)f3) * (double)f2;
            entityitem.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
            entityitem.motionZ += Math.sin((double)f3) * (double)f2;
         }

         ItemStack itemstack = this.dropItemAndGetStack(entityitem);
         if (traceItem) {
            if (itemstack != null) {
               this.addStat(StatList.getDroppedObjectStats(itemstack.getItem()), droppedItem.stackSize);
            }

            this.addStat(StatList.DROP);
         }

         return entityitem;
      }
   }

   @Nullable
   public ItemStack dropItemAndGetStack(EntityItem var1) {
      if (this.captureDrops) {
         this.capturedDrops.add(p_184816_1_);
      } else {
         this.world.spawnEntity(p_184816_1_);
      }

      return p_184816_1_.getEntityItem();
   }

   /** @deprecated */
   @Deprecated
   public float getDigSpeed(IBlockState var1) {
      return this.getDigSpeed(state, (BlockPos)null);
   }

   public float getDigSpeed(IBlockState var1, BlockPos var2) {
      float f = this.inventory.getStrVsBlock(state);
      if (f > 1.0F) {
         int i = EnchantmentHelper.getEfficiencyModifier(this);
         ItemStack itemstack = this.getHeldItemMainhand();
         if (i > 0 && itemstack != null) {
            f += (float)(i * i + 1);
         }
      }

      if (this.isPotionActive(MobEffects.HASTE)) {
         f *= 1.0F + (float)(this.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
      }

      if (this.isPotionActive(MobEffects.MINING_FATIGUE)) {
         float f1;
         switch(this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
         case 0:
            f1 = 0.3F;
            break;
         case 1:
            f1 = 0.09F;
            break;
         case 2:
            f1 = 0.0027F;
            break;
         case 3:
         default:
            f1 = 8.1E-4F;
         }

         f *= f1;
      }

      if (this.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
         f /= 5.0F;
      }

      if (!this.onGround) {
         f /= 5.0F;
      }

      f = ForgeEventFactory.getBreakSpeed(this, state, f, pos);
      return f < 0.0F ? 0.0F : f;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      return ForgeEventFactory.doPlayerHarvestCheck(this, state, this.inventory.canHarvestBlock(state));
   }

   public static void registerFixesPlayer(DataFixer var0) {
      fixer.registerWalker(FixTypes.PLAYER, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            DataFixesManager.processInventory(fixer, compound, versionIn, "Inventory");
            DataFixesManager.processInventory(fixer, compound, versionIn, "EnderItems");
            return compound;
         }
      });
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      this.setUniqueId(getUUID(this.gameProfile));
      NBTTagList nbttaglist = compound.getTagList("Inventory", 10);
      this.inventory.readFromNBT(nbttaglist);
      this.inventory.currentItem = compound.getInteger("SelectedItemSlot");
      this.sleeping = compound.getBoolean("Sleeping");
      this.sleepTimer = compound.getShort("SleepTimer");
      this.experience = compound.getFloat("XpP");
      this.experienceLevel = compound.getInteger("XpLevel");
      this.experienceTotal = compound.getInteger("XpTotal");
      this.xpSeed = compound.getInteger("XpSeed");
      if (this.xpSeed == 0) {
         this.xpSeed = this.rand.nextInt();
      }

      this.setScore(compound.getInteger("Score"));
      if (this.sleeping) {
         this.bedLocation = new BlockPos(this);
         this.wakeUpPlayer(true, true, false);
      }

      if (compound.hasKey("SpawnX", 99) && compound.hasKey("SpawnY", 99) && compound.hasKey("SpawnZ", 99)) {
         this.spawnChunk = new BlockPos(compound.getInteger("SpawnX"), compound.getInteger("SpawnY"), compound.getInteger("SpawnZ"));
         this.spawnForced = compound.getBoolean("SpawnForced");
      }

      NBTTagList spawnlist = null;
      spawnlist = compound.getTagList("Spawns", 10);

      for(int i = 0; i < spawnlist.tagCount(); ++i) {
         NBTTagCompound spawndata = spawnlist.getCompoundTagAt(i);
         int spawndim = spawndata.getInteger("Dim");
         this.spawnChunkMap.put(Integer.valueOf(spawndim), new BlockPos(spawndata.getInteger("SpawnX"), spawndata.getInteger("SpawnY"), spawndata.getInteger("SpawnZ")));
         this.spawnForcedMap.put(Integer.valueOf(spawndim), Boolean.valueOf(spawndata.getBoolean("SpawnForced")));
      }

      this.foodStats.readNBT(compound);
      this.capabilities.readCapabilitiesFromNBT(compound);
      if (compound.hasKey("EnderItems", 9)) {
         NBTTagList nbttaglist1 = compound.getTagList("EnderItems", 10);
         this.theInventoryEnderChest.loadInventoryFromNBT(nbttaglist1);
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setInteger("DataVersion", 512);
      compound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
      compound.setInteger("SelectedItemSlot", this.inventory.currentItem);
      compound.setBoolean("Sleeping", this.sleeping);
      compound.setShort("SleepTimer", (short)this.sleepTimer);
      compound.setFloat("XpP", this.experience);
      compound.setInteger("XpLevel", this.experienceLevel);
      compound.setInteger("XpTotal", this.experienceTotal);
      compound.setInteger("XpSeed", this.xpSeed);
      compound.setInteger("Score", this.getScore());
      if (this.spawnChunk != null) {
         compound.setInteger("SpawnX", this.spawnChunk.getX());
         compound.setInteger("SpawnY", this.spawnChunk.getY());
         compound.setInteger("SpawnZ", this.spawnChunk.getZ());
         compound.setBoolean("SpawnForced", this.spawnForced);
      }

      NBTTagList spawnlist = new NBTTagList();

      for(Entry entry : this.spawnChunkMap.entrySet()) {
         BlockPos spawn = (BlockPos)entry.getValue();
         if (spawn != null) {
            Boolean forced = (Boolean)this.spawnForcedMap.get(entry.getKey());
            if (forced == null) {
               forced = false;
            }

            NBTTagCompound spawndata = new NBTTagCompound();
            spawndata.setInteger("Dim", ((Integer)entry.getKey()).intValue());
            spawndata.setInteger("SpawnX", spawn.getX());
            spawndata.setInteger("SpawnY", spawn.getY());
            spawndata.setInteger("SpawnZ", spawn.getZ());
            spawndata.setBoolean("SpawnForced", forced.booleanValue());
            spawnlist.appendTag(spawndata);
         }
      }

      compound.setTag("Spawns", spawnlist);
      this.foodStats.writeNBT(compound);
      this.capabilities.writeCapabilitiesToNBT(compound);
      compound.setTag("EnderItems", this.theInventoryEnderChest.saveInventoryToNBT());
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!ForgeHooks.onLivingAttack(this, source, amount)) {
         return false;
      } else if (this.isEntityInvulnerable(source)) {
         return false;
      } else if (this.capabilities.disableDamage && !source.canHarmInCreative()) {
         return false;
      } else {
         this.entityAge = 0;
         if (this.getHealth() <= 0.0F) {
            return false;
         } else {
            if (this.isPlayerSleeping() && !this.world.isRemote) {
               this.wakeUpPlayer(true, true, false);
            }

            if (source.isDifficultyScaled()) {
               if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                  amount = 0.0F;
               }

               if (this.world.getDifficulty() == EnumDifficulty.EASY) {
                  amount = amount / 2.0F + 1.0F;
               }

               if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                  amount = amount * 3.0F / 2.0F;
               }
            }

            return amount == 0.0F ? false : super.attackEntityFrom(source, amount);
         }
      }
   }

   public boolean canAttackPlayer(EntityPlayer var1) {
      Team team = this.getTeam();
      Team team1 = other.getTeam();
      return team == null ? true : (!team.isSameTeam(team1) ? true : team.getAllowFriendlyFire());
   }

   protected void damageArmor(float var1) {
      this.inventory.damageArmor(damage);
   }

   protected void damageShield(float var1) {
      if (damage >= 3.0F && this.activeItemStack != null && this.activeItemStack.getItem() == Items.SHIELD) {
         int i = 1 + MathHelper.floor(damage);
         this.activeItemStack.damageItem(i, this);
         if (this.activeItemStack.stackSize <= 0) {
            EnumHand enumhand = this.getActiveHand();
            ForgeEventFactory.onPlayerDestroyItem(this, this.activeItemStack, enumhand);
            if (enumhand == EnumHand.MAIN_HAND) {
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
      int i = 0;

      for(ItemStack itemstack : this.inventory.armorInventory) {
         if (itemstack != null) {
            ++i;
         }
      }

      return (float)i / (float)this.inventory.armorInventory.length;
   }

   protected void damageEntity(DamageSource var1, float var2) {
      if (!this.isEntityInvulnerable(damageSrc)) {
         damageAmount = ForgeHooks.onLivingHurt(this, damageSrc, damageAmount);
         if (damageAmount <= 0.0F) {
            return;
         }

         damageAmount = ArmorProperties.applyArmor(this, this.inventory.armorInventory, damageSrc, (double)damageAmount);
         if (damageAmount <= 0.0F) {
            return;
         }

         damageAmount = this.applyPotionDamageCalculations(damageSrc, damageAmount);
         float f = damageAmount;
         damageAmount = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0F);
         this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - damageAmount));
         if (damageAmount != 0.0F) {
            this.addExhaustion(damageSrc.getHungerDamage());
            float f1 = this.getHealth();
            this.setHealth(this.getHealth() - damageAmount);
            this.getCombatTracker().trackDamage(damageSrc, f1, damageAmount);
            if (damageAmount < 3.4028235E37F) {
               this.addStat(StatList.DAMAGE_TAKEN, Math.round(damageAmount * 10.0F));
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
         if (entityIn instanceof IInventory) {
            this.displayGUIChest((IInventory)entityIn);
         }

         return EnumActionResult.PASS;
      } else if (ForgeHooks.onInteractEntity(this, entityIn, stack, hand)) {
         return EnumActionResult.PASS;
      } else {
         ItemStack itemstack = stack != null ? stack.copy() : null;
         if (!entityIn.processInitialInteract(this, stack, hand)) {
            if (stack != null && entityIn instanceof EntityLivingBase) {
               if (this.capabilities.isCreativeMode) {
                  stack = itemstack;
               }

               if (stack.interactWithEntity(this, (EntityLivingBase)entityIn, hand)) {
                  if (stack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                     ForgeEventFactory.onPlayerDestroyItem(this, stack, hand);
                     this.setHeldItem(hand, (ItemStack)null);
                  }

                  return EnumActionResult.SUCCESS;
               }
            }

            return EnumActionResult.PASS;
         } else {
            if (stack != null && stack == this.getHeldItem(hand)) {
               if (stack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                  ForgeEventFactory.onPlayerDestroyItem(this, stack, hand);
                  this.setHeldItem(hand, (ItemStack)null);
               } else if (stack.stackSize < itemstack.stackSize && this.capabilities.isCreativeMode) {
                  stack.stackSize = itemstack.stackSize;
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
      if (ForgeHooks.onPlayerAttackTarget(this, targetEntity)) {
         if (targetEntity.canBeAttackedWithItem() && !targetEntity.hitByEntity(this)) {
            float f = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            float f1;
            if (targetEntity instanceof EntityLivingBase) {
               f1 = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)targetEntity).getCreatureAttribute());
            } else {
               f1 = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
            }

            float f2 = this.getCooledAttackStrength(0.5F);
            f = f * (0.2F + f2 * f2 * 0.8F);
            f1 = f1 * f2;
            this.resetCooldown();
            if (f > 0.0F || f1 > 0.0F) {
               boolean flag = f2 > 0.9F;
               boolean flag1 = false;
               int i = 0;
               i = i + EnchantmentHelper.getKnockbackModifier(this);
               if (this.isSprinting() && flag) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0F, 1.0F);
                  ++i;
                  flag1 = true;
               }

               boolean flag2 = flag && this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(MobEffects.BLINDNESS) && !this.isRiding() && targetEntity instanceof EntityLivingBase;
               flag2 = flag2 && !this.isSprinting();
               if (flag2) {
                  f *= 1.5F;
               }

               f = f + f1;
               boolean flag3 = false;
               double d0 = (double)(this.distanceWalkedModified - this.prevDistanceWalkedModified);
               if (flag && !flag2 && !flag1 && this.onGround && d0 < (double)this.getAIMoveSpeed()) {
                  ItemStack itemstack = this.getHeldItem(EnumHand.MAIN_HAND);
                  if (itemstack != null && itemstack.getItem() instanceof ItemSword) {
                     flag3 = true;
                  }
               }

               float f4 = 0.0F;
               boolean flag4 = false;
               int j = EnchantmentHelper.getFireAspectModifier(this);
               if (targetEntity instanceof EntityLivingBase) {
                  f4 = ((EntityLivingBase)targetEntity).getHealth();
                  if (j > 0 && !targetEntity.isBurning()) {
                     flag4 = true;
                     targetEntity.setFire(1);
                  }
               }

               double d1 = targetEntity.motionX;
               double d2 = targetEntity.motionY;
               double d3 = targetEntity.motionZ;
               boolean flag5 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);
               if (flag5) {
                  if (i > 0) {
                     if (targetEntity instanceof EntityLivingBase) {
                        ((EntityLivingBase)targetEntity).knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                     } else {
                        targetEntity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * (float)i * 0.5F));
                     }

                     this.motionX *= 0.6D;
                     this.motionZ *= 0.6D;
                     this.setSprinting(false);
                  }

                  if (flag3) {
                     for(EntityLivingBase entitylivingbase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, targetEntity.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D))) {
                        if (entitylivingbase != this && entitylivingbase != targetEntity && !this.isOnSameTeam(entitylivingbase) && this.getDistanceSqToEntity(entitylivingbase) < 9.0D) {
                           entitylivingbase.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                           entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F);
                        }
                     }

                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
                     this.spawnSweepParticles();
                  }

                  if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                     ((EntityPlayerMP)targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                     targetEntity.velocityChanged = false;
                     targetEntity.motionX = d1;
                     targetEntity.motionY = d2;
                     targetEntity.motionZ = d3;
                  }

                  if (flag2) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
                     this.onCriticalHit(targetEntity);
                  }

                  if (!flag2 && !flag3) {
                     if (flag) {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
                     } else {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, this.getSoundCategory(), 1.0F, 1.0F);
                     }
                  }

                  if (f1 > 0.0F) {
                     this.onEnchantmentCritical(targetEntity);
                  }

                  if (!this.world.isRemote && targetEntity instanceof EntityPlayer) {
                     EntityPlayer entityplayer = (EntityPlayer)targetEntity;
                     ItemStack itemstack2 = this.getHeldItemMainhand();
                     ItemStack itemstack3 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;
                     if (itemstack2 != null && itemstack3 != null && itemstack2.getItem() instanceof ItemAxe && itemstack3.getItem() == Items.SHIELD) {
                        float f3 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
                        if (flag1) {
                           f3 += 0.75F;
                        }

                        if (this.rand.nextFloat() < f3) {
                           entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                           this.world.setEntityState(entityplayer, (byte)30);
                        }
                     }
                  }

                  if (f >= 18.0F) {
                     this.addStat(AchievementList.OVERKILL);
                  }

                  this.setLastAttacker(targetEntity);
                  if (targetEntity instanceof EntityLivingBase) {
                     EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, this);
                  }

                  EnchantmentHelper.applyArthropodEnchantments(this, targetEntity);
                  ItemStack itemstack1 = this.getHeldItemMainhand();
                  Entity entity = targetEntity;
                  if (targetEntity instanceof EntityDragonPart) {
                     IEntityMultiPart ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj;
                     if (ientitymultipart instanceof EntityLivingBase) {
                        entity = (EntityLivingBase)ientitymultipart;
                     }
                  }

                  if (itemstack1 != null && entity instanceof EntityLivingBase) {
                     itemstack1.hitEntity((EntityLivingBase)entity, this);
                     if (itemstack1.stackSize <= 0) {
                        this.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                        ForgeEventFactory.onPlayerDestroyItem(this, itemstack1, EnumHand.MAIN_HAND);
                     }
                  }

                  if (targetEntity instanceof EntityLivingBase) {
                     float f5 = f4 - ((EntityLivingBase)targetEntity).getHealth();
                     this.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                     if (j > 0) {
                        targetEntity.setFire(j * 4);
                     }

                     if (this.world instanceof WorldServer && f5 > 2.0F) {
                        int k = (int)((double)f5 * 0.5D);
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double)(targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                     }
                  }

                  this.addExhaustion(0.3F);
               } else {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);
                  if (flag4) {
                     targetEntity.extinguish();
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
      double d0 = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F));
      double d1 = (double)MathHelper.cos(this.rotationYaw * 0.017453292F);
      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX + d0, this.posY + (double)this.height * 0.5D, this.posZ + d1, 0, d0, 0.0D, d1, 0.0D);
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
      EntityPlayer.SleepResult ret = ForgeEventFactory.onPlayerSleepInBed(this, bedLocation);
      if (ret != null) {
         return ret;
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

            if (Math.abs(this.posX - (double)bedLocation.getX()) > 3.0D || Math.abs(this.posY - (double)bedLocation.getY()) > 2.0D || Math.abs(this.posZ - (double)bedLocation.getZ()) > 3.0D) {
               return EntityPlayer.SleepResult.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List list = this.world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)bedLocation.getX() - 8.0D, (double)bedLocation.getY() - 5.0D, (double)bedLocation.getZ() - 8.0D, (double)bedLocation.getX() + 8.0D, (double)bedLocation.getY() + 5.0D, (double)bedLocation.getZ() + 8.0D));
            if (!list.isEmpty()) {
               return EntityPlayer.SleepResult.NOT_SAFE;
            }
         }

         if (this.isRiding()) {
            this.dismountRidingEntity();
         }

         this.setSize(0.2F, 0.2F);
         IBlockState state = null;
         if (this.world.isBlockLoaded(bedLocation)) {
            state = this.world.getBlockState(bedLocation);
         }

         if (state != null && state.getBlock().isBed(state, this.world, bedLocation, this)) {
            EnumFacing enumfacing = state.getBlock().getBedDirection(state, this.world, bedLocation);
            float f = 0.5F;
            float f1 = 0.5F;
            switch(enumfacing) {
            case SOUTH:
               f1 = 0.9F;
               break;
            case NORTH:
               f1 = 0.1F;
               break;
            case WEST:
               f = 0.1F;
               break;
            case EAST:
               f = 0.9F;
            }

            this.setRenderOffsetForSleep(enumfacing);
            this.setPosition((double)((float)bedLocation.getX() + f), (double)((float)bedLocation.getY() + 0.6875F), (double)((float)bedLocation.getZ() + f1));
         } else {
            this.setPosition((double)((float)bedLocation.getX() + 0.5F), (double)((float)bedLocation.getY() + 0.6875F), (double)((float)bedLocation.getZ() + 0.5F));
         }

         this.sleeping = true;
         this.sleepTimer = 0;
         this.bedLocation = bedLocation;
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
      switch(p_175139_1_) {
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
      ForgeEventFactory.onPlayerWakeup(this, immediately, updateWorldFlag, setSpawn);
      this.setSize(0.6F, 1.8F);
      IBlockState iblockstate = this.world.getBlockState(this.bedLocation);
      if (this.bedLocation != null && iblockstate.getBlock().isBed(iblockstate, this.world, this.bedLocation, this)) {
         iblockstate.getBlock().setBedOccupied(this.world, this.bedLocation, this, false);
         BlockPos blockpos = iblockstate.getBlock().getBedSpawnPosition(iblockstate, this.world, this.bedLocation, this);
         if (blockpos == null) {
            blockpos = this.bedLocation.up();
         }

         this.setPosition((double)((float)blockpos.getX() + 0.5F), (double)((float)blockpos.getY() + 0.1F), (double)((float)blockpos.getZ() + 0.5F));
      } else {
         setSpawn = false;
      }

      this.sleeping = false;
      if (!this.world.isRemote && updateWorldFlag) {
         this.world.updateAllPlayersSleepingFlag();
      }

      this.sleepTimer = immediately ? 0 : 100;
      if (setSpawn) {
         this.setSpawnPoint(this.bedLocation, false);
      }

   }

   private boolean isInBed() {
      return ForgeEventFactory.fireSleepingLocationCheck(this, this.bedLocation);
   }

   @Nullable
   public static BlockPos getBedSpawnLocation(World var0, BlockPos var1, boolean var2) {
      IBlockState state = worldIn.getBlockState(bedLocation);
      Block block = state.getBlock();
      if (!block.isBed(state, worldIn, bedLocation, (Entity)null)) {
         if (!forceSpawn) {
            return null;
         } else {
            boolean flag = block.canSpawnInBlock();
            boolean flag1 = worldIn.getBlockState(bedLocation.up()).getBlock().canSpawnInBlock();
            return flag && flag1 ? bedLocation : null;
         }
      } else {
         return block.getBedSpawnPosition(state, worldIn, bedLocation, (EntityPlayer)null);
      }
   }

   @SideOnly(Side.CLIENT)
   public float getBedOrientationInDegrees() {
      IBlockState state = this.bedLocation == null ? null : this.world.getBlockState(this.bedLocation);
      if (state != null && state.getBlock().isBed(state, this.world, this.bedLocation, this)) {
         EnumFacing enumfacing = state.getBlock().getBedDirection(state, this.world, this.bedLocation);
         switch(enumfacing) {
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
      if (!ForgeEventFactory.onPlayerSpawnSet(this, pos, forced)) {
         if (this.dimension != 0) {
            this.setSpawnChunk(pos, forced, this.dimension);
         } else {
            if (pos != null) {
               this.spawnChunk = pos;
               this.spawnForced = forced;
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
      this.addStat(stat, 1);
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
      double d0 = this.posX;
      double d1 = this.posY;
      double d2 = this.posZ;
      if (this.capabilities.isFlying && !this.isRiding()) {
         double d3 = this.motionY;
         float f = this.jumpMovementFactor;
         this.jumpMovementFactor = this.capabilities.getFlySpeed() * (float)(this.isSprinting() ? 2 : 1);
         super.moveEntityWithHeading(strafe, forward);
         this.motionY = d3 * 0.6D;
         this.jumpMovementFactor = f;
         this.fallDistance = 0.0F;
         this.setFlag(7, false);
      } else {
         super.moveEntityWithHeading(strafe, forward);
      }

      this.addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
   }

   public float getAIMoveSpeed() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
   }

   public void addMovementStat(double var1, double var3, double var5) {
      if (!this.isRiding()) {
         if (this.isInsideOfMaterial(Material.WATER)) {
            int i = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0F);
            if (i > 0) {
               this.addStat(StatList.DIVE_ONE_CM, i);
               this.addExhaustion(0.015F * (float)i * 0.01F);
            }
         } else if (this.isInWater()) {
            int j = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);
            if (j > 0) {
               this.addStat(StatList.SWIM_ONE_CM, j);
               this.addExhaustion(0.015F * (float)j * 0.01F);
            }
         } else if (this.isOnLadder()) {
            if (p_71000_3_ > 0.0D) {
               this.addStat(StatList.CLIMB_ONE_CM, (int)Math.round(p_71000_3_ * 100.0D));
            }
         } else if (this.onGround) {
            int k = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);
            if (k > 0) {
               if (this.isSprinting()) {
                  this.addStat(StatList.SPRINT_ONE_CM, k);
                  this.addExhaustion(0.099999994F * (float)k * 0.01F);
               } else if (this.isSneaking()) {
                  this.addStat(StatList.CROUCH_ONE_CM, k);
                  this.addExhaustion(0.005F * (float)k * 0.01F);
               } else {
                  this.addStat(StatList.WALK_ONE_CM, k);
                  this.addExhaustion(0.01F * (float)k * 0.01F);
               }
            }
         } else if (this.isElytraFlying()) {
            int l = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0F);
            this.addStat(StatList.AVIATE_ONE_CM, l);
         } else {
            int i1 = Math.round(MathHelper.sqrt(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);
            if (i1 > 25) {
               this.addStat(StatList.FLY_ONE_CM, i1);
            }
         }
      }

   }

   private void addMountedMovementStat(double var1, double var3, double var5) {
      if (this.isRiding()) {
         int i = Math.round(MathHelper.sqrt(p_71015_1_ * p_71015_1_ + p_71015_3_ * p_71015_3_ + p_71015_5_ * p_71015_5_) * 100.0F);
         if (i > 0) {
            if (this.getRidingEntity() instanceof EntityMinecart) {
               this.addStat(StatList.MINECART_ONE_CM, i);
               if (this.startMinecartRidingCoordinate == null) {
                  this.startMinecartRidingCoordinate = new BlockPos(this);
               } else if (this.startMinecartRidingCoordinate.distanceSq((double)MathHelper.floor(this.posX), (double)MathHelper.floor(this.posY), (double)MathHelper.floor(this.posZ)) >= 1000000.0D) {
                  this.addStat(AchievementList.ON_A_RAIL);
               }
            } else if (this.getRidingEntity() instanceof EntityBoat) {
               this.addStat(StatList.BOAT_ONE_CM, i);
            } else if (this.getRidingEntity() instanceof EntityPig) {
               this.addStat(StatList.PIG_ONE_CM, i);
            } else if (this.getRidingEntity() instanceof EntityHorse) {
               this.addStat(StatList.HORSE_ONE_CM, i);
            }
         }
      }

   }

   public void fall(float var1, float var2) {
      if (!this.capabilities.allowFlying) {
         if (distance >= 2.0F) {
            this.addStat(StatList.FALL_ONE_CM, (int)Math.round((double)distance * 100.0D));
         }

         super.fall(distance, damageMultiplier);
      } else {
         ForgeEventFactory.onPlayerFall(this, distance, damageMultiplier);
      }

   }

   protected void resetHeight() {
      if (!this.isSpectator()) {
         super.resetHeight();
      }

   }

   protected SoundEvent getFallSound(int var1) {
      return heightIn > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
   }

   public void onKillEntity(EntityLivingBase var1) {
      if (entityLivingIn instanceof IMob) {
         this.addStat(AchievementList.KILL_ENEMY);
      }

      EntityList.EntityEggInfo entitylist$entityegginfo = (EntityList.EntityEggInfo)EntityList.ENTITY_EGGS.get(EntityList.getEntityString(entityLivingIn));
      if (entitylist$entityegginfo != null) {
         this.addStat(entitylist$entityegginfo.killEntityStat);
      }

   }

   public void setInWeb() {
      if (!this.capabilities.isFlying) {
         super.setInWeb();
      }

   }

   public void addExperience(int var1) {
      this.addScore(amount);
      int i = Integer.MAX_VALUE - this.experienceTotal;
      if (amount > i) {
         amount = i;
      }

      this.experience += (float)amount / (float)this.xpBarCap();

      for(this.experienceTotal += amount; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap()) {
         this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
         this.addExperienceLevel(1);
      }

   }

   public int getXPSeed() {
      return this.xpSeed;
   }

   public void removeExperienceLevel(int var1) {
      this.experienceLevel -= levels;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experience = 0.0F;
         this.experienceTotal = 0;
      }

      this.xpSeed = this.rand.nextInt();
   }

   public void addExperienceLevel(int var1) {
      this.experienceLevel += levels;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experience = 0.0F;
         this.experienceTotal = 0;
      }

      if (levels > 0 && this.experienceLevel % 5 == 0 && (float)this.lastXPSound < (float)this.ticksExisted - 100.0F) {
         float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), f * 0.75F, 1.0F);
         this.lastXPSound = this.ticksExisted;
      }

   }

   public int xpBarCap() {
      return this.experienceLevel >= 30 ? 112 + (this.experienceLevel - 30) * 9 : (this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2);
   }

   public void addExhaustion(float var1) {
      if (!this.capabilities.disableDamage && !this.world.isRemote) {
         this.foodStats.addExhaustion(exhaustion);
      }

   }

   public FoodStats getFoodStats() {
      return this.foodStats;
   }

   public boolean canEat(boolean var1) {
      return (ignoreHunger || this.foodStats.needFood()) && !this.capabilities.disableDamage;
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
      } else if (stack == null) {
         return false;
      } else {
         BlockPos blockpos = pos.offset(facing.getOpposite());
         Block block = this.world.getBlockState(blockpos).getBlock();
         return stack.canPlaceOn(block) || stack.canEditBlocks();
      }
   }

   protected int getExperiencePoints(EntityPlayer var1) {
      if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
         int i = this.experienceLevel * 7;
         return i > 100 ? 100 : i;
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
      if (respawnFromEnd) {
         this.inventory.copyInventory(oldPlayer.inventory);
         this.setHealth(oldPlayer.getHealth());
         this.foodStats = oldPlayer.foodStats;
         this.experienceLevel = oldPlayer.experienceLevel;
         this.experienceTotal = oldPlayer.experienceTotal;
         this.experience = oldPlayer.experience;
         this.setScore(oldPlayer.getScore());
         this.lastPortalPos = oldPlayer.lastPortalPos;
         this.lastPortalVec = oldPlayer.lastPortalVec;
         this.teleportDirection = oldPlayer.teleportDirection;
      } else if (this.world.getGameRules().getBoolean("keepInventory") || oldPlayer.isSpectator()) {
         this.inventory.copyInventory(oldPlayer.inventory);
         this.experienceLevel = oldPlayer.experienceLevel;
         this.experienceTotal = oldPlayer.experienceTotal;
         this.experience = oldPlayer.experience;
         this.setScore(oldPlayer.getScore());
      }

      this.xpSeed = oldPlayer.xpSeed;
      this.theInventoryEnderChest = oldPlayer.theInventoryEnderChest;
      this.getDataManager().set(PLAYER_MODEL_FLAG, oldPlayer.getDataManager().get(PLAYER_MODEL_FLAG));
      this.spawnChunkMap = oldPlayer.spawnChunkMap;
      this.spawnForcedMap = oldPlayer.spawnForcedMap;
      NBTTagCompound old = oldPlayer.getEntityData();
      if (old.hasKey("PlayerPersisted")) {
         this.getEntityData().setTag("PlayerPersisted", old.getCompoundTag("PlayerPersisted"));
      }

      ForgeEventFactory.onPlayerClone(this, oldPlayer, !respawnFromEnd);
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
      return slotIn == EntityEquipmentSlot.MAINHAND ? this.inventory.getCurrentItem() : (slotIn == EntityEquipmentSlot.OFFHAND ? this.inventory.offHandInventory[0] : (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? this.inventory.armorInventory[slotIn.getIndex()] : null));
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2) {
      if (slotIn == EntityEquipmentSlot.MAINHAND) {
         this.playEquipSound(stack);
         this.inventory.mainInventory[this.inventory.currentItem] = stack;
      } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
         this.playEquipSound(stack);
         this.inventory.offHandInventory[0] = stack;
      } else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
         this.playEquipSound(stack);
         this.inventory.armorInventory[slotIn.getIndex()] = stack;
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
      } else if (player.isSpectator()) {
         return false;
      } else {
         Team team = this.getTeam();
         return team == null || player == null || player.getTeam() != team || !team.getSeeFriendlyInvisiblesEnabled();
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
      ITextComponent itextcomponent = new TextComponentString("");
      if (!this.prefixes.isEmpty()) {
         for(ITextComponent prefix : this.prefixes) {
            itextcomponent.appendSibling(prefix);
         }
      }

      itextcomponent.appendSibling(new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getDisplayNameString())));
      if (!this.suffixes.isEmpty()) {
         for(ITextComponent suffix : this.suffixes) {
            itextcomponent.appendSibling(suffix);
         }
      }

      itextcomponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.getName() + " "));
      itextcomponent.getStyle().setHoverEvent(this.getHoverEvent());
      itextcomponent.getStyle().setInsertion(this.getName());
      return itextcomponent;
   }

   public float getEyeHeight() {
      float f = this.eyeHeight;
      if (this.isPlayerSleeping()) {
         f = 0.2F;
      } else if (!this.isSneaking() && this.height != 1.65F) {
         if (this.isElytraFlying() || this.height == 0.6F) {
            f = 0.4F;
         }
      } else {
         f -= 0.08F;
      }

      return f;
   }

   public void setAbsorptionAmount(float var1) {
      if (amount < 0.0F) {
         amount = 0.0F;
      }

      this.getDataManager().set(ABSORPTION, Float.valueOf(amount));
   }

   public float getAbsorptionAmount() {
      return ((Float)this.getDataManager().get(ABSORPTION)).floatValue();
   }

   public static UUID getUUID(GameProfile var0) {
      UUID uuid = profile.getId();
      if (uuid == null) {
         uuid = getOfflineUUID(profile.getName());
      }

      return uuid;
   }

   public static UUID getOfflineUUID(String var0) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
   }

   public boolean canOpen(LockCode var1) {
      if (code.isEmpty()) {
         return true;
      } else {
         ItemStack itemstack = this.getHeldItemMainhand();
         return itemstack != null && itemstack.hasDisplayName() ? itemstack.getDisplayName().equals(code.getLock()) : false;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean isWearing(EnumPlayerModelParts var1) {
      return (((Byte)this.getDataManager().get(PLAYER_MODEL_FLAG)).byteValue() & part.getPartMask()) == part.getPartMask();
   }

   public boolean sendCommandFeedback() {
      return this.getServer().worlds[0].getGameRules().getBoolean("sendCommandFeedback");
   }

   public boolean replaceItemInInventory(int var1, ItemStack var2) {
      if (inventorySlot >= 0 && inventorySlot < this.inventory.mainInventory.length) {
         this.inventory.setInventorySlotContents(inventorySlot, itemStackIn);
         return true;
      } else {
         EntityEquipmentSlot entityequipmentslot;
         if (inventorySlot == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
            entityequipmentslot = EntityEquipmentSlot.HEAD;
         } else if (inventorySlot == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
            entityequipmentslot = EntityEquipmentSlot.CHEST;
         } else if (inventorySlot == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
            entityequipmentslot = EntityEquipmentSlot.LEGS;
         } else if (inventorySlot == 100 + EntityEquipmentSlot.FEET.getIndex()) {
            entityequipmentslot = EntityEquipmentSlot.FEET;
         } else {
            entityequipmentslot = null;
         }

         if (inventorySlot == 98) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemStackIn);
            return true;
         } else if (inventorySlot == 99) {
            this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, itemStackIn);
            return true;
         } else if (entityequipmentslot == null) {
            int i = inventorySlot - 200;
            if (i >= 0 && i < this.theInventoryEnderChest.getSizeInventory()) {
               this.theInventoryEnderChest.setInventorySlotContents(i, itemStackIn);
               return true;
            } else {
               return false;
            }
         } else {
            if (itemStackIn != null && itemStackIn.getItem() != null) {
               if (!(itemStackIn.getItem() instanceof ItemArmor) && !(itemStackIn.getItem() instanceof ItemElytra)) {
                  if (entityequipmentslot != EntityEquipmentSlot.HEAD) {
                     return false;
                  }
               } else if (EntityLiving.getSlotForItemStack(itemStackIn) != entityequipmentslot) {
                  return false;
               }
            }

            this.inventory.setInventorySlotContents(entityequipmentslot.getIndex() + this.inventory.mainInventory.length, itemStackIn);
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
      this.hasReducedDebug = reducedDebug;
   }

   public EnumHandSide getPrimaryHand() {
      return ((Byte)this.dataManager.get(MAIN_HAND)).byteValue() == 0 ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
   }

   public void setPrimaryHand(EnumHandSide var1) {
      this.dataManager.set(MAIN_HAND, Byte.valueOf((byte)(hand == EnumHandSide.LEFT ? 0 : 1)));
   }

   public float getCooldownPeriod() {
      return (float)(1.0D / this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
   }

   public float getCooledAttackStrength(float var1) {
      return MathHelper.clamp(((float)this.ticksSinceLastSwing + adjustTicks) / this.getCooldownPeriod(), 0.0F, 1.0F);
   }

   public void resetCooldown() {
      this.ticksSinceLastSwing = 0;
   }

   public CooldownTracker getCooldownTracker() {
      return this.cooldownTracker;
   }

   public void applyEntityCollision(Entity var1) {
      if (!this.isPlayerSleeping()) {
         super.applyEntityCollision(entityIn);
      }

   }

   public float getLuck() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue();
   }

   public boolean canUseCommandBlock() {
      return this.capabilities.isCreativeMode && this.canUseCommand(2, "");
   }

   public void openGui(Object var1, int var2, World var3, int var4, int var5, int var6) {
      FMLNetworkHandler.openGui(this, mod, modGuiId, world, x, y, z);
   }

   public BlockPos getBedLocation(int var1) {
      return dimension == 0 ? this.spawnChunk : (BlockPos)this.spawnChunkMap.get(Integer.valueOf(dimension));
   }

   public boolean isSpawnForced(int var1) {
      if (dimension == 0) {
         return this.spawnForced;
      } else {
         Boolean forced = (Boolean)this.spawnForcedMap.get(Integer.valueOf(dimension));
         return forced == null ? false : forced.booleanValue();
      }
   }

   public void setSpawnChunk(BlockPos var1, boolean var2, int var3) {
      if (dimension == 0) {
         if (pos != null) {
            this.spawnChunk = pos;
            this.spawnForced = forced;
         } else {
            this.spawnChunk = null;
            this.spawnForced = false;
         }

      } else {
         if (pos != null) {
            this.spawnChunkMap.put(Integer.valueOf(dimension), pos);
            this.spawnForcedMap.put(Integer.valueOf(dimension), Boolean.valueOf(forced));
         } else {
            this.spawnChunkMap.remove(Integer.valueOf(dimension));
            this.spawnForcedMap.remove(Integer.valueOf(dimension));
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
      this.prefixes.add(prefix);
   }

   public void addSuffix(ITextComponent var1) {
      this.suffixes.add(suffix);
   }

   public Collection getPrefixes() {
      return this.prefixes;
   }

   public Collection getSuffixes() {
      return this.suffixes;
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (facing == null) {
            return this.playerJoinedHandler;
         }

         if (facing.getAxis().isVertical()) {
            return this.playerMainHandler;
         }

         if (facing.getAxis().isHorizontal()) {
            return this.playerEquipmentHandler;
         }
      }

      return super.getCapability(capability, facing);
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
   }

   public static enum EnumChatVisibility {
      FULL(0, "options.chat.visibility.full"),
      SYSTEM(1, "options.chat.visibility.system"),
      HIDDEN(2, "options.chat.visibility.hidden");

      private static final EntityPlayer.EnumChatVisibility[] ID_LOOKUP = new EntityPlayer.EnumChatVisibility[values().length];
      private final int chatVisibility;
      private final String resourceKey;

      private EnumChatVisibility(int var3, String var4) {
         this.chatVisibility = id;
         this.resourceKey = resourceKey;
      }

      @SideOnly(Side.CLIENT)
      public int getChatVisibility() {
         return this.chatVisibility;
      }

      @SideOnly(Side.CLIENT)
      public static EntityPlayer.EnumChatVisibility getEnumChatVisibility(int var0) {
         return ID_LOOKUP[id % ID_LOOKUP.length];
      }

      @SideOnly(Side.CLIENT)
      public String getResourceKey() {
         return this.resourceKey;
      }

      static {
         for(EntityPlayer.EnumChatVisibility entityplayer$enumchatvisibility : values()) {
            ID_LOOKUP[entityplayer$enumchatvisibility.chatVisibility] = entityplayer$enumchatvisibility;
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
