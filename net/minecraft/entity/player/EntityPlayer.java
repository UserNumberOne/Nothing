package net.minecraft.entity.player;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
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
import net.minecraft.init.Blocks;
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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.TrigMath;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftItem;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public abstract class EntityPlayer extends EntityLivingBase {
   private static final DataParameter ABSORPTION = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.FLOAT);
   private static final DataParameter PLAYER_SCORE = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.VARINT);
   protected static final DataParameter PLAYER_MODEL_FLAG = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BYTE);
   protected static final DataParameter MAIN_HAND = EntityDataManager.createKey(EntityPlayer.class, DataSerializers.BYTE);
   public InventoryPlayer inventory = new InventoryPlayer(this);
   private InventoryEnderChest theInventoryEnderChest = new InventoryEnderChest();
   public Container inventoryContainer;
   public Container openContainer;
   protected FoodStats foodStats = new FoodStats(this);
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
   public boolean sleeping;
   public BlockPos bedLocation;
   public int sleepTimer;
   public float renderOffsetX;
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
   private ItemStack itemStackMainHand;
   private final CooldownTracker cooldownTracker = this.createCooldownTracker();
   public EntityFishHook fishEntity;
   public boolean fauxSleeping;
   public String spawnWorld = "";
   public int oldLevel = -1;

   public CraftHumanEntity getBukkitEntity() {
      return (CraftHumanEntity)super.getBukkitEntity();
   }

   protected CooldownTracker createCooldownTracker() {
      return new CooldownTracker();
   }

   public EntityPlayer(World world, GameProfile gameprofile) {
      super(world);
      this.setUniqueId(getUUID(gameprofile));
      this.gameProfile = gameprofile;
      this.inventoryContainer = new ContainerPlayer(this.inventory, !world.isRemote, this);
      this.openContainer = this.inventoryContainer;
      BlockPos blockposition = world.getSpawnPoint();
      this.setLocationAndAngles((double)blockposition.getX() + 0.5D, (double)(blockposition.getY() + 1), (double)blockposition.getZ() + 0.5D, 0.0F, 0.0F);
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

   public void playSound(SoundEvent soundeffect, float f, float f1) {
      this.world.playSound(this, this.posX, this.posY, this.posZ, soundeffect, this.getSoundCategory(), f, f1);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.PLAYERS;
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
         if (this.getRidingEntity() instanceof EntityPig) {
            this.rotationPitch = f1;
            this.rotationYaw = f;
            this.renderYawOffset = ((EntityPig)this.getRidingEntity()).renderYawOffset;
         }
      }

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
            this.heal(1.0F, RegainReason.REGEN);
         }

         if (this.foodStats.needFood() && this.ticksExisted % 10 == 0) {
            this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
         }
      }

      this.inventory.decrementAnimations();
      this.prevCameraYaw = this.cameraYaw;
      super.onLivingUpdate();
      IAttributeInstance attributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (!this.world.isRemote) {
         attributeinstance.setBaseValue((double)this.capabilities.getWalkSpeed());
      }

      this.jumpMovementFactor = this.speedInAir;
      if (this.isSprinting()) {
         this.jumpMovementFactor = (float)((double)this.jumpMovementFactor + (double)this.speedInAir * 0.3D);
      }

      this.setAIMoveSpeed((float)attributeinstance.getAttributeValue());
      float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      float f1 = (float)(TrigMath.atan(-this.motionY * 0.20000000298023224D) * 15.0D);
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

   private void collideWithPlayer(Entity entity) {
      entity.onCollideWithPlayer(this);
   }

   public int getScore() {
      return ((Integer)this.dataManager.get(PLAYER_SCORE)).intValue();
   }

   public void setScore(int i) {
      this.dataManager.set(PLAYER_SCORE, Integer.valueOf(i));
   }

   public void addScore(int i) {
      int j = this.getScore();
      this.dataManager.set(PLAYER_SCORE, Integer.valueOf(j + i));
   }

   public void onDeath(DamageSource damagesource) {
      super.onDeath(damagesource);
      this.setSize(0.2F, 0.2F);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.motionY = 0.10000000149011612D;
      if ("Notch".equals(this.getName())) {
         this.dropItem(new ItemStack(Items.APPLE, 1), true, false);
      }

      if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
         this.inventory.dropAllItems();
      }

      if (damagesource != null) {
         this.motionX = (double)(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
         this.motionZ = (double)(-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
      } else {
         this.motionX = 0.0D;
         this.motionZ = 0.0D;
      }

      this.addStat(StatList.DEATHS);
      this.takeStat(StatList.TIME_SINCE_DEATH);
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_PLAYER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PLAYER_DEATH;
   }

   public void addToPlayerScore(Entity entity, int i) {
      if (entity != this) {
         this.addScore(i);
         Collection collection = this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreCriteria.TOTAL_KILL_COUNT, this.getName(), new ArrayList());
         if (entity instanceof EntityPlayer) {
            this.addStat(StatList.PLAYER_KILLS);
            this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreCriteria.PLAYER_KILL_COUNT, this.getName(), collection);
         } else {
            this.addStat(StatList.MOB_KILLS);
         }

         collection.addAll(this.giveTeamKillScores(entity));
         Iterator iterator = collection.iterator();

         while(iterator.hasNext()) {
            ((Score)iterator.next()).incrementScore();
         }
      }

   }

   private Collection giveTeamKillScores(Entity entity) {
      String s = entity instanceof EntityPlayer ? entity.getName() : entity.getCachedUniqueIdString();
      ScorePlayerTeam scoreboardteam = this.getWorldScoreboard().getPlayersTeam(this.getName());
      if (scoreboardteam != null) {
         int i = scoreboardteam.getChatFormat().getColorIndex();
         if (i >= 0 && i < IScoreCriteria.KILLED_BY_TEAM.length) {
            for(ScoreObjective scoreboardobjective : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.KILLED_BY_TEAM[i])) {
               Score scoreboardscore = this.getWorldScoreboard().getOrCreateScore(s, scoreboardobjective);
               scoreboardscore.incrementScore();
            }
         }
      }

      ScorePlayerTeam scoreboardteam1 = this.getWorldScoreboard().getPlayersTeam(s);
      if (scoreboardteam1 != null) {
         int j = scoreboardteam1.getChatFormat().getColorIndex();
         if (j >= 0 && j < IScoreCriteria.TEAM_KILL.length) {
            return this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreCriteria.TEAM_KILL[j], this.getName(), new ArrayList());
         }
      }

      return Lists.newArrayList();
   }

   @Nullable
   public EntityItem dropItem(boolean flag) {
      return this.dropItem(this.inventory.decrStackSize(this.inventory.currentItem, flag && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1), false, true);
   }

   @Nullable
   public EntityItem dropItem(@Nullable ItemStack itemstack, boolean flag) {
      return this.dropItem(itemstack, false, false);
   }

   @Nullable
   public EntityItem dropItem(@Nullable ItemStack itemstack, boolean flag, boolean flag1) {
      if (itemstack == null) {
         return null;
      } else if (itemstack.stackSize == 0) {
         return null;
      } else {
         double d0 = this.posY - 0.30000001192092896D + (double)this.getEyeHeight();
         EntityItem entityitem = new EntityItem(this.world, this.posX, d0, this.posZ, itemstack);
         entityitem.setPickupDelay(40);
         if (flag1) {
            entityitem.setThrower(this.getName());
         }

         if (flag) {
            float f = this.rand.nextFloat() * 0.5F;
            float f1 = this.rand.nextFloat() * 6.2831855F;
            entityitem.motionX = (double)(-MathHelper.sin(f1) * f);
            entityitem.motionZ = (double)(MathHelper.cos(f1) * f);
            entityitem.motionY = 0.20000000298023224D;
         } else {
            float f = 0.3F;
            entityitem.motionX = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f);
            entityitem.motionZ = (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f);
            entityitem.motionY = (double)(-MathHelper.sin(this.rotationPitch * 0.017453292F) * f + 0.1F);
            float f1 = this.rand.nextFloat() * 6.2831855F;
            f = 0.02F * this.rand.nextFloat();
            entityitem.motionX += Math.cos((double)f1) * (double)f;
            entityitem.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
            entityitem.motionZ += Math.sin((double)f1) * (double)f;
         }

         Player player = (Player)this.getBukkitEntity();
         CraftItem drop = new CraftItem(this.world.getServer(), entityitem);
         PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
         this.world.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            ItemStack itemstack1 = this.dropItemAndGetStack(entityitem);
            if (flag1) {
               if (itemstack1 != null) {
                  this.addStat(StatList.getDroppedObjectStats(itemstack1.getItem()), itemstack.stackSize);
               }

               this.addStat(StatList.DROP);
            }

            return entityitem;
         } else {
            org.bukkit.inventory.ItemStack cur = player.getInventory().getItemInHand();
            if (!flag1 || cur != null && cur.getAmount() != 0) {
               if (flag1 && cur.isSimilar(drop.getItemStack()) && drop.getItemStack().getAmount() == 1) {
                  cur.setAmount(cur.getAmount() + 1);
                  player.getInventory().setItemInHand(cur);
               } else {
                  player.getInventory().addItem(new org.bukkit.inventory.ItemStack[]{drop.getItemStack()});
               }
            } else {
               player.getInventory().setItemInHand(drop.getItemStack());
            }

            return null;
         }
      }
   }

   @Nullable
   protected ItemStack dropItemAndGetStack(EntityItem entityitem) {
      this.world.spawnEntity(entityitem);
      return entityitem.getEntityItem();
   }

   public float getDigSpeed(IBlockState iblockdata) {
      float f = this.inventory.getStrVsBlock(iblockdata);
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

      return f;
   }

   public boolean canHarvestBlock(IBlockState iblockdata) {
      return this.inventory.canHarvestBlock(iblockdata);
   }

   public static void registerFixesPlayer(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.PLAYER, new IDataWalker() {
         public NBTTagCompound process(IDataFixer dataconverter, NBTTagCompound nbttagcompound, int i) {
            DataFixesManager.processInventory(dataconverter, nbttagcompound, i, "Inventory");
            DataFixesManager.processInventory(dataconverter, nbttagcompound, i, "EnderItems");
            return nbttagcompound;
         }
      });
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.setUniqueId(getUUID(this.gameProfile));
      NBTTagList nbttaglist = nbttagcompound.getTagList("Inventory", 10);
      this.inventory.readFromNBT(nbttaglist);
      this.inventory.currentItem = nbttagcompound.getInteger("SelectedItemSlot");
      this.sleeping = nbttagcompound.getBoolean("Sleeping");
      this.sleepTimer = nbttagcompound.getShort("SleepTimer");
      this.experience = nbttagcompound.getFloat("XpP");
      this.experienceLevel = nbttagcompound.getInteger("XpLevel");
      this.experienceTotal = nbttagcompound.getInteger("XpTotal");
      this.xpSeed = nbttagcompound.getInteger("XpSeed");
      if (this.xpSeed == 0) {
         this.xpSeed = this.rand.nextInt();
      }

      this.setScore(nbttagcompound.getInteger("Score"));
      if (this.sleeping) {
         this.bedLocation = new BlockPos(this);
         this.wakeUpPlayer(true, true, false);
      }

      this.spawnWorld = nbttagcompound.getString("SpawnWorld");
      if ("".equals(this.spawnWorld)) {
         this.spawnWorld = ((org.bukkit.World)this.world.getServer().getWorlds().get(0)).getName();
      }

      if (nbttagcompound.hasKey("SpawnX", 99) && nbttagcompound.hasKey("SpawnY", 99) && nbttagcompound.hasKey("SpawnZ", 99)) {
         this.spawnChunk = new BlockPos(nbttagcompound.getInteger("SpawnX"), nbttagcompound.getInteger("SpawnY"), nbttagcompound.getInteger("SpawnZ"));
         this.spawnForced = nbttagcompound.getBoolean("SpawnForced");
      }

      this.foodStats.readNBT(nbttagcompound);
      this.capabilities.readCapabilitiesFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("EnderItems", 9)) {
         NBTTagList nbttaglist1 = nbttagcompound.getTagList("EnderItems", 10);
         this.theInventoryEnderChest.loadInventoryFromNBT(nbttaglist1);
      }

   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setInteger("DataVersion", 512);
      nbttagcompound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
      nbttagcompound.setInteger("SelectedItemSlot", this.inventory.currentItem);
      nbttagcompound.setBoolean("Sleeping", this.sleeping);
      nbttagcompound.setShort("SleepTimer", (short)this.sleepTimer);
      nbttagcompound.setFloat("XpP", this.experience);
      nbttagcompound.setInteger("XpLevel", this.experienceLevel);
      nbttagcompound.setInteger("XpTotal", this.experienceTotal);
      nbttagcompound.setInteger("XpSeed", this.xpSeed);
      nbttagcompound.setInteger("Score", this.getScore());
      if (this.spawnChunk != null) {
         nbttagcompound.setInteger("SpawnX", this.spawnChunk.getX());
         nbttagcompound.setInteger("SpawnY", this.spawnChunk.getY());
         nbttagcompound.setInteger("SpawnZ", this.spawnChunk.getZ());
         nbttagcompound.setBoolean("SpawnForced", this.spawnForced);
      }

      this.foodStats.writeNBT(nbttagcompound);
      this.capabilities.writeCapabilitiesToNBT(nbttagcompound);
      nbttagcompound.setTag("EnderItems", this.theInventoryEnderChest.saveInventoryToNBT());
      ItemStack itemstack = this.inventory.getCurrentItem();
      if (itemstack != null && itemstack.getItem() != null) {
         nbttagcompound.setTag("SelectedItem", itemstack.writeToNBT(new NBTTagCompound()));
      }

      nbttagcompound.setString("SpawnWorld", this.spawnWorld);
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else if (this.capabilities.disableDamage && !damagesource.canHarmInCreative()) {
         return false;
      } else {
         this.entityAge = 0;
         if (this.getHealth() <= 0.0F) {
            return false;
         } else {
            if (this.isPlayerSleeping() && !this.world.isRemote) {
               this.wakeUpPlayer(true, true, false);
            }

            if (damagesource.isDifficultyScaled()) {
               if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                  return false;
               }

               if (this.world.getDifficulty() == EnumDifficulty.EASY) {
                  f = f / 2.0F + 1.0F;
               }

               if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                  f = f * 3.0F / 2.0F;
               }
            }

            return super.attackEntityFrom(damagesource, f);
         }
      }
   }

   public boolean canAttackPlayer(EntityPlayer entityhuman) {
      Team team;
      if (entityhuman instanceof EntityPlayerMP) {
         EntityPlayerMP thatPlayer = (EntityPlayerMP)entityhuman;
         team = thatPlayer.getBukkitEntity().getScoreboard().getPlayerTeam(thatPlayer.getBukkitEntity());
         if (team == null || team.allowFriendlyFire()) {
            return true;
         }
      } else {
         OfflinePlayer thisPlayer = entityhuman.world.getServer().getOfflinePlayer(entityhuman.getName());
         team = entityhuman.world.getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(thisPlayer);
         if (team == null || team.allowFriendlyFire()) {
            return true;
         }
      }

      if (this instanceof EntityPlayerMP) {
         return !team.hasPlayer(((EntityPlayerMP)this).getBukkitEntity());
      } else {
         return !team.hasPlayer(this.world.getServer().getOfflinePlayer(this.getName()));
      }
   }

   protected void damageArmor(float f) {
      this.inventory.damageArmor(f);
   }

   protected void damageShield(float f) {
      if (f >= 3.0F && this.activeItemStack != null && this.activeItemStack.getItem() == Items.SHIELD) {
         int i = 1 + MathHelper.floor(f);
         this.activeItemStack.damageItem(i, this);
         if (this.activeItemStack.stackSize <= 0) {
            EnumHand enumhand = this.getActiveHand();
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

   protected boolean damageEntity0(DamageSource damagesource, float f) {
      return super.damageEntity0(damagesource, f);
   }

   public void openEditSign(TileEntitySign tileentitysign) {
   }

   public void displayGuiEditCommandCart(CommandBlockBaseLogic commandblocklistenerabstract) {
   }

   public void displayGuiCommandBlock(TileEntityCommandBlock tileentitycommand) {
   }

   public void openEditStructure(TileEntityStructure tileentitystructure) {
   }

   public void displayVillagerTradeGui(IMerchant imerchant) {
   }

   public void displayGUIChest(IInventory iinventory) {
   }

   public void openGuiHorseInventory(EntityHorse entityhorse, IInventory iinventory) {
   }

   public void displayGui(IInteractionObject itileentitycontainer) {
   }

   public void openBook(ItemStack itemstack, EnumHand enumhand) {
   }

   public EnumActionResult interact(Entity entity, @Nullable ItemStack itemstack, EnumHand enumhand) {
      if (this.isSpectator()) {
         if (entity instanceof IInventory) {
            this.displayGUIChest((IInventory)entity);
         }

         return EnumActionResult.PASS;
      } else {
         ItemStack itemstack1 = itemstack != null ? itemstack.copy() : null;
         if (!entity.processInitialInteract(this, itemstack, enumhand)) {
            if (itemstack != null && entity instanceof EntityLivingBase) {
               if (this.capabilities.isCreativeMode) {
                  itemstack = itemstack1;
               }

               if (itemstack.interactWithEntity(this, (EntityLivingBase)entity, enumhand)) {
                  if (itemstack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                     this.setHeldItem(enumhand, (ItemStack)null);
                  }

                  return EnumActionResult.SUCCESS;
               }
            }

            return EnumActionResult.PASS;
         } else {
            if (itemstack != null && itemstack == this.getHeldItem(enumhand)) {
               if (itemstack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                  this.setHeldItem(enumhand, (ItemStack)null);
               } else if (itemstack.stackSize < itemstack1.stackSize && this.capabilities.isCreativeMode) {
                  itemstack.stackSize = itemstack1.stackSize;
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

   public void attackTargetEntityWithCurrentItem(Entity entity) {
      if (entity.canBeAttackedWithItem() && !entity.hitByEntity(this)) {
         float f = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float f1;
         if (entity instanceof EntityLivingBase) {
            f1 = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)entity).getCreatureAttribute());
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
            byte b0 = 0;
            int i = b0 + EnchantmentHelper.getKnockbackModifier(this);
            if (this.isSprinting() && flag) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0F, 1.0F);
               ++i;
               flag1 = true;
            }

            boolean flag2 = flag && this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(MobEffects.BLINDNESS) && !this.isRiding() && entity instanceof EntityLivingBase;
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

            float f3 = 0.0F;
            boolean flag4 = false;
            int j = EnchantmentHelper.getFireAspectModifier(this);
            if (entity instanceof EntityLivingBase) {
               f3 = ((EntityLivingBase)entity).getHealth();
               if (j > 0 && !entity.isBurning()) {
                  EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), 1);
                  Bukkit.getPluginManager().callEvent(combustEvent);
                  if (!combustEvent.isCancelled()) {
                     flag4 = true;
                     entity.setFire(combustEvent.getDuration());
                  }
               }
            }

            double d1 = entity.motionX;
            double d2 = entity.motionY;
            double d3 = entity.motionZ;
            boolean flag5 = entity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);
            if (flag5) {
               if (i > 0) {
                  if (entity instanceof EntityLivingBase) {
                     ((EntityLivingBase)entity).knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                  } else {
                     entity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * (float)i * 0.5F));
                  }

                  this.motionX *= 0.6D;
                  this.motionZ *= 0.6D;
                  this.setSprinting(false);
               }

               if (flag3) {
                  for(EntityLivingBase entityliving : this.world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D))) {
                     if (entityliving != this && entityliving != entity && !this.isOnSameTeam(entityliving) && this.getDistanceSqToEntity(entityliving) < 9.0D && entityliving.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F)) {
                        entityliving.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                     }
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
                  this.spawnSweepParticles();
               }

               if (entity instanceof EntityPlayerMP && entity.velocityChanged) {
                  boolean cancelled = false;
                  Player player = (Player)entity.getBukkitEntity();
                  Vector velocity = new Vector(d1, d2, d3);
                  PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
                  this.world.getServer().getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     cancelled = true;
                  } else if (!velocity.equals(event.getVelocity())) {
                     player.setVelocity(event.getVelocity());
                  }

                  if (!cancelled) {
                     ((EntityPlayerMP)entity).connection.sendPacket(new SPacketEntityVelocity(entity));
                     entity.velocityChanged = false;
                     entity.motionX = d1;
                     entity.motionY = d2;
                     entity.motionZ = d3;
                  }
               }

               if (flag2) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
                  this.onCriticalHit(entity);
               }

               if (!flag2 && !flag3) {
                  if (flag) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
                  } else {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, this.getSoundCategory(), 1.0F, 1.0F);
                  }
               }

               if (f1 > 0.0F) {
                  this.onEnchantmentCritical(entity);
               }

               if (!this.world.isRemote && entity instanceof EntityPlayer) {
                  EntityPlayer entityhuman = (EntityPlayer)entity;
                  ItemStack itemstack1 = this.getHeldItemMainhand();
                  ItemStack itemstack2 = entityhuman.isHandActive() ? entityhuman.getActiveItemStack() : null;
                  if (itemstack1 != null && itemstack2 != null && itemstack1.getItem() instanceof ItemAxe && itemstack2.getItem() == Items.SHIELD) {
                     float f4 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
                     if (flag1) {
                        f4 += 0.75F;
                     }

                     if (this.rand.nextFloat() < f4) {
                        entityhuman.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                        this.world.setEntityState(entityhuman, (byte)30);
                     }
                  }
               }

               if (f >= 18.0F) {
                  this.addStat(AchievementList.OVERKILL);
               }

               this.setLastAttacker(entity);
               if (entity instanceof EntityLivingBase) {
                  EnchantmentHelper.applyThornEnchantments((EntityLivingBase)entity, this);
               }

               EnchantmentHelper.applyArthropodEnchantments(this, entity);
               ItemStack itemstack3 = this.getHeldItemMainhand();
               Object object = entity;
               if (entity instanceof EntityDragonPart) {
                  IEntityMultiPart icomplex = ((EntityDragonPart)entity).entityDragonObj;
                  if (icomplex instanceof EntityLivingBase) {
                     object = (EntityLivingBase)icomplex;
                  }
               }

               if (itemstack3 != null && object instanceof EntityLivingBase) {
                  itemstack3.hitEntity((EntityLivingBase)object, this);
                  if (itemstack3.stackSize == 0) {
                     this.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                  }
               }

               if (entity instanceof EntityLivingBase) {
                  float f5 = f3 - ((EntityLivingBase)entity).getHealth();
                  this.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                  if (j > 0) {
                     EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), j * 4);
                     Bukkit.getPluginManager().callEvent(combustEvent);
                     if (!combustEvent.isCancelled()) {
                        entity.setFire(combustEvent.getDuration());
                     }
                  }

                  if (this.world instanceof WorldServer && f5 > 2.0F) {
                     int k = (int)((double)f5 * 0.5D);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, entity.posX, entity.posY + (double)(entity.height * 0.5F), entity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                  }
               }

               this.addExhaustion(0.3F);
            } else {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);
               if (flag4) {
                  entity.extinguish();
               }
            }
         }
      }

   }

   public void onCriticalHit(Entity entity) {
   }

   public void onEnchantmentCritical(Entity entity) {
   }

   public void spawnSweepParticles() {
      double d0 = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F));
      double d1 = (double)MathHelper.cos(this.rotationYaw * 0.017453292F);
      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX + d0, this.posY + (double)this.height * 0.5D, this.posZ + d1, 0, d0, 0.0D, d1, 0.0D);
      }

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

   public EntityPlayer.SleepResult trySleep(BlockPos blockposition) {
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

         if (Math.abs(this.posX - (double)blockposition.getX()) > 3.0D || Math.abs(this.posY - (double)blockposition.getY()) > 2.0D || Math.abs(this.posZ - (double)blockposition.getZ()) > 3.0D) {
            return EntityPlayer.SleepResult.TOO_FAR_AWAY;
         }

         List list = this.world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)blockposition.getX() - 8.0D, (double)blockposition.getY() - 5.0D, (double)blockposition.getZ() - 8.0D, (double)blockposition.getX() + 8.0D, (double)blockposition.getY() + 5.0D, (double)blockposition.getZ() + 8.0D));
         if (!list.isEmpty()) {
            return EntityPlayer.SleepResult.NOT_SAFE;
         }
      }

      if (this.isRiding()) {
         this.dismountRidingEntity();
      }

      if (this.getBukkitEntity() instanceof Player) {
         Player player = (Player)this.getBukkitEntity();
         Block bed = this.world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         PlayerBedEnterEvent event = new PlayerBedEnterEvent(player, bed);
         this.world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return EntityPlayer.SleepResult.OTHER_PROBLEM;
         }
      }

      this.setSize(0.2F, 0.2F);
      if (this.world.isBlockLoaded(blockposition)) {
         EnumFacing enumdirection = (EnumFacing)this.world.getBlockState(blockposition).getValue(BlockHorizontal.FACING);
         float f = 0.5F;
         float f1 = 0.5F;
         switch(EntityPlayer.SyntheticClass_1.a[enumdirection.ordinal()]) {
         case 1:
            f1 = 0.9F;
            break;
         case 2:
            f1 = 0.1F;
            break;
         case 3:
            f = 0.1F;
            break;
         case 4:
            f = 0.9F;
         }

         this.setRenderOffsetForSleep(enumdirection);
         this.setPosition((double)((float)blockposition.getX() + f), (double)((float)blockposition.getY() + 0.6875F), (double)((float)blockposition.getZ() + f1));
      } else {
         this.setPosition((double)((float)blockposition.getX() + 0.5F), (double)((float)blockposition.getY() + 0.6875F), (double)((float)blockposition.getZ() + 0.5F));
      }

      this.sleeping = true;
      this.sleepTimer = 0;
      this.bedLocation = blockposition;
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      if (!this.world.isRemote) {
         this.world.updateAllPlayersSleepingFlag();
      }

      return EntityPlayer.SleepResult.OK;
   }

   private void setRenderOffsetForSleep(EnumFacing enumdirection) {
      this.renderOffsetX = 0.0F;
      this.renderOffsetZ = 0.0F;
      switch(EntityPlayer.SyntheticClass_1.a[enumdirection.ordinal()]) {
      case 1:
         this.renderOffsetZ = -1.8F;
         break;
      case 2:
         this.renderOffsetZ = 1.8F;
         break;
      case 3:
         this.renderOffsetX = 1.8F;
         break;
      case 4:
         this.renderOffsetX = -1.8F;
      }

   }

   public void wakeUpPlayer(boolean flag, boolean flag1, boolean flag2) {
      this.setSize(0.6F, 1.8F);
      IBlockState iblockdata = this.world.getBlockState(this.bedLocation);
      if (this.bedLocation != null && iblockdata.getBlock() == Blocks.BED) {
         this.world.setBlockState(this.bedLocation, iblockdata.withProperty(BlockBed.OCCUPIED, Boolean.valueOf(false)), 4);
         BlockPos blockposition = BlockBed.getSafeExitLocation(this.world, this.bedLocation, 0);
         if (blockposition == null) {
            blockposition = this.bedLocation.up();
         }

         this.setPosition((double)((float)blockposition.getX() + 0.5F), (double)((float)blockposition.getY() + 0.1F), (double)((float)blockposition.getZ() + 0.5F));
      }

      this.sleeping = false;
      if (!this.world.isRemote && flag1) {
         this.world.updateAllPlayersSleepingFlag();
      }

      if (this.getBukkitEntity() instanceof Player) {
         Player player = (Player)this.getBukkitEntity();
         BlockPos blockposition = this.bedLocation;
         Block bed;
         if (blockposition != null) {
            bed = this.world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         } else {
            bed = this.world.getWorld().getBlockAt(player.getLocation());
         }

         PlayerBedLeaveEvent event = new PlayerBedLeaveEvent(player, bed);
         this.world.getServer().getPluginManager().callEvent(event);
      }

      this.sleepTimer = flag ? 0 : 100;
      if (flag2) {
         this.setSpawnPoint(this.bedLocation, false);
      }

   }

   private boolean isInBed() {
      return this.world.getBlockState(this.bedLocation).getBlock() == Blocks.BED;
   }

   @Nullable
   public static BlockPos getBedSpawnLocation(World world, BlockPos blockposition, boolean flag) {
      net.minecraft.block.Block block = world.getBlockState(blockposition).getBlock();
      if (block != Blocks.BED) {
         if (!flag) {
            return null;
         } else {
            boolean flag1 = block.canSpawnInBlock();
            boolean flag2 = world.getBlockState(blockposition.up()).getBlock().canSpawnInBlock();
            return flag1 && flag2 ? blockposition : null;
         }
      } else {
         return BlockBed.getSafeExitLocation(world, blockposition, 0);
      }
   }

   public boolean isPlayerSleeping() {
      return this.sleeping;
   }

   public boolean isPlayerFullyAsleep() {
      return this.sleeping && this.sleepTimer >= 100;
   }

   public void sendStatusMessage(ITextComponent ichatbasecomponent) {
   }

   public BlockPos getBedLocation() {
      return this.spawnChunk;
   }

   public boolean isSpawnForced() {
      return this.spawnForced;
   }

   public void setSpawnPoint(BlockPos blockposition, boolean flag) {
      if (blockposition != null) {
         this.spawnChunk = blockposition;
         this.spawnForced = flag;
         this.spawnWorld = this.world.worldInfo.getWorldName();
      } else {
         this.spawnChunk = null;
         this.spawnForced = false;
         this.spawnWorld = "";
      }

   }

   public boolean hasAchievement(Achievement achievement) {
      return false;
   }

   public void addStat(StatBase statistic) {
      this.addStat(statistic, 1);
   }

   public void addStat(StatBase statistic, int i) {
   }

   public void takeStat(StatBase statistic) {
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

   public void moveEntityWithHeading(float f, float f1) {
      double d0 = this.posX;
      double d1 = this.posY;
      double d2 = this.posZ;
      if (this.capabilities.isFlying && !this.isRiding()) {
         double d3 = this.motionY;
         float f2 = this.jumpMovementFactor;
         this.jumpMovementFactor = this.capabilities.getFlySpeed() * (float)(this.isSprinting() ? 2 : 1);
         super.moveEntityWithHeading(f, f1);
         this.motionY = d3 * 0.6D;
         this.jumpMovementFactor = f2;
         this.fallDistance = 0.0F;
         this.setFlag(7, false);
      } else {
         super.moveEntityWithHeading(f, f1);
      }

      this.addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
   }

   public float getAIMoveSpeed() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
   }

   public void addMovementStat(double d0, double d1, double d2) {
      if (!this.isRiding()) {
         if (this.isInsideOfMaterial(Material.WATER)) {
            int i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
            if (i > 0) {
               this.addStat(StatList.DIVE_ONE_CM, i);
               this.addExhaustion(0.015F * (float)i * 0.01F);
            }
         } else if (this.isInWater()) {
            int i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
            if (i > 0) {
               this.addStat(StatList.SWIM_ONE_CM, i);
               this.addExhaustion(0.015F * (float)i * 0.01F);
            }
         } else if (this.isOnLadder()) {
            if (d1 > 0.0D) {
               this.addStat(StatList.CLIMB_ONE_CM, (int)Math.round(d1 * 100.0D));
            }
         } else if (this.onGround) {
            int i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
            if (i > 0) {
               if (this.isSprinting()) {
                  this.addStat(StatList.SPRINT_ONE_CM, i);
                  this.addExhaustion(0.099999994F * (float)i * 0.01F);
               } else if (this.isSneaking()) {
                  this.addStat(StatList.CROUCH_ONE_CM, i);
                  this.addExhaustion(0.005F * (float)i * 0.01F);
               } else {
                  this.addStat(StatList.WALK_ONE_CM, i);
                  this.addExhaustion(0.01F * (float)i * 0.01F);
               }
            }
         } else if (this.isElytraFlying()) {
            int i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
            this.addStat(StatList.AVIATE_ONE_CM, i);
         } else {
            int i = Math.round(MathHelper.sqrt(d0 * d0 + d2 * d2) * 100.0F);
            if (i > 25) {
               this.addStat(StatList.FLY_ONE_CM, i);
            }
         }
      }

   }

   private void addMountedMovementStat(double d0, double d1, double d2) {
      if (this.isRiding()) {
         int i = Math.round(MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
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

   public void fall(float f, float f1) {
      if (!this.capabilities.allowFlying) {
         if (f >= 2.0F) {
            this.addStat(StatList.FALL_ONE_CM, (int)Math.round((double)f * 100.0D));
         }

         super.fall(f, f1);
      }

   }

   protected void resetHeight() {
      if (!this.isSpectator()) {
         super.resetHeight();
      }

   }

   protected SoundEvent getFallSound(int i) {
      return i > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
   }

   public void onKillEntity(EntityLivingBase entityliving) {
      if (entityliving instanceof IMob) {
         this.addStat(AchievementList.KILL_ENEMY);
      }

      EntityList.EntityEggInfo entitytypes_monsteregginfo = (EntityList.EntityEggInfo)EntityList.ENTITY_EGGS.get(EntityList.getEntityString(entityliving));
      if (entitytypes_monsteregginfo != null) {
         this.addStat(entitytypes_monsteregginfo.killEntityStat);
      }

   }

   public void setInWeb() {
      if (!this.capabilities.isFlying) {
         super.setInWeb();
      }

   }

   public void addExperience(int i) {
      this.addScore(i);
      int j = Integer.MAX_VALUE - this.experienceTotal;
      if (i > j) {
         i = j;
      }

      this.experience += (float)i / (float)this.xpBarCap();

      for(this.experienceTotal += i; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap()) {
         this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
         this.addExperienceLevel(1);
      }

   }

   public int getXPSeed() {
      return this.xpSeed;
   }

   public void removeExperienceLevel(int i) {
      this.experienceLevel -= i;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experience = 0.0F;
         this.experienceTotal = 0;
      }

      this.xpSeed = this.rand.nextInt();
   }

   public void addExperienceLevel(int i) {
      this.experienceLevel += i;
      if (this.experienceLevel < 0) {
         this.experienceLevel = 0;
         this.experience = 0.0F;
         this.experienceTotal = 0;
      }

      if (i > 0 && this.experienceLevel % 5 == 0 && (float)this.lastXPSound < (float)this.ticksExisted - 100.0F) {
         float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), f * 0.75F, 1.0F);
         this.lastXPSound = this.ticksExisted;
      }

   }

   public int xpBarCap() {
      return this.experienceLevel >= 30 ? 112 + (this.experienceLevel - 30) * 9 : (this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2);
   }

   public void addExhaustion(float f) {
      if (!this.capabilities.disableDamage && !this.world.isRemote) {
         this.foodStats.addExhaustion(f);
      }

   }

   public FoodStats getFoodStats() {
      return this.foodStats;
   }

   public boolean canEat(boolean flag) {
      return (flag || this.foodStats.needFood()) && !this.capabilities.disableDamage;
   }

   public boolean shouldHeal() {
      return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
   }

   public boolean isAllowEdit() {
      return this.capabilities.allowEdit;
   }

   public boolean canPlayerEdit(BlockPos blockposition, EnumFacing enumdirection, @Nullable ItemStack itemstack) {
      if (this.capabilities.allowEdit) {
         return true;
      } else if (itemstack == null) {
         return false;
      } else {
         BlockPos blockposition1 = blockposition.offset(enumdirection.getOpposite());
         net.minecraft.block.Block block = this.world.getBlockState(blockposition1).getBlock();
         return itemstack.canPlaceOn(block) || itemstack.canEditBlocks();
      }
   }

   protected int getExperiencePoints(EntityPlayer entityhuman) {
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

   public void clonePlayer(EntityPlayer entityhuman, boolean flag) {
      if (flag) {
         this.inventory.copyInventory(entityhuman.inventory);
         this.setHealth(entityhuman.getHealth());
         this.foodStats = entityhuman.foodStats;
         this.experienceLevel = entityhuman.experienceLevel;
         this.experienceTotal = entityhuman.experienceTotal;
         this.experience = entityhuman.experience;
         this.setScore(entityhuman.getScore());
         this.lastPortalPos = entityhuman.lastPortalPos;
         this.lastPortalVec = entityhuman.lastPortalVec;
         this.teleportDirection = entityhuman.teleportDirection;
      } else if (this.world.getGameRules().getBoolean("keepInventory") || entityhuman.isSpectator()) {
         this.inventory.copyInventory(entityhuman.inventory);
         this.experienceLevel = entityhuman.experienceLevel;
         this.experienceTotal = entityhuman.experienceTotal;
         this.experience = entityhuman.experience;
         this.setScore(entityhuman.getScore());
      }

      this.xpSeed = entityhuman.xpSeed;
      this.theInventoryEnderChest = entityhuman.theInventoryEnderChest;
      this.getDataManager().set(PLAYER_MODEL_FLAG, (Byte)entityhuman.getDataManager().get(PLAYER_MODEL_FLAG));
   }

   protected boolean canTriggerWalking() {
      return !this.capabilities.isFlying;
   }

   public void sendPlayerAbilities() {
   }

   public void setGameType(GameType enumgamemode) {
   }

   public String getName() {
      return this.gameProfile.getName();
   }

   public InventoryEnderChest getInventoryEnderChest() {
      return this.theInventoryEnderChest;
   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityEquipmentSlot enumitemslot) {
      return enumitemslot == EntityEquipmentSlot.MAINHAND ? this.inventory.getCurrentItem() : (enumitemslot == EntityEquipmentSlot.OFFHAND ? this.inventory.offHandInventory[0] : (enumitemslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? this.inventory.armorInventory[enumitemslot.getIndex()] : null));
   }

   public void setItemStackToSlot(EntityEquipmentSlot enumitemslot, @Nullable ItemStack itemstack) {
      if (enumitemslot == EntityEquipmentSlot.MAINHAND) {
         this.playEquipSound(itemstack);
         this.inventory.mainInventory[this.inventory.currentItem] = itemstack;
      } else if (enumitemslot == EntityEquipmentSlot.OFFHAND) {
         this.playEquipSound(itemstack);
         this.inventory.offHandInventory[0] = itemstack;
      } else if (enumitemslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
         this.playEquipSound(itemstack);
         this.inventory.armorInventory[enumitemslot.getIndex()] = itemstack;
      }

   }

   public Iterable getHeldEquipment() {
      return Lists.newArrayList(new ItemStack[]{this.getHeldItemMainhand(), this.getHeldItemOffhand()});
   }

   public Iterable getArmorInventoryList() {
      return Arrays.asList(this.inventory.armorInventory);
   }

   public abstract boolean isSpectator();

   public abstract boolean isCreative();

   public boolean isPushedByWater() {
      return !this.capabilities.isFlying;
   }

   public Scoreboard getWorldScoreboard() {
      return this.world.getScoreboard();
   }

   public net.minecraft.scoreboard.Team getTeam() {
      return this.getWorldScoreboard().getPlayersTeam(this.getName());
   }

   public ITextComponent getDisplayName() {
      TextComponentString chatcomponenttext = new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getName()));
      chatcomponenttext.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.getName() + " "));
      chatcomponenttext.getStyle().setHoverEvent(this.getHoverEvent());
      chatcomponenttext.getStyle().setInsertion(this.getName());
      return chatcomponenttext;
   }

   public float getEyeHeight() {
      float f = 1.62F;
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

   public void setAbsorptionAmount(float f) {
      if (f < 0.0F) {
         f = 0.0F;
      }

      this.getDataManager().set(ABSORPTION, Float.valueOf(f));
   }

   public float getAbsorptionAmount() {
      return ((Float)this.getDataManager().get(ABSORPTION)).floatValue();
   }

   public static UUID getUUID(GameProfile gameprofile) {
      UUID uuid = gameprofile.getId();
      if (uuid == null) {
         uuid = getOfflineUUID(gameprofile.getName());
      }

      return uuid;
   }

   public static UUID getOfflineUUID(String s) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + s).getBytes(Charsets.UTF_8));
   }

   public boolean canOpen(LockCode chestlock) {
      if (chestlock.isEmpty()) {
         return true;
      } else {
         ItemStack itemstack = this.getHeldItemMainhand();
         return itemstack != null && itemstack.hasDisplayName() ? itemstack.getDisplayName().equals(chestlock.getLock()) : false;
      }
   }

   public boolean sendCommandFeedback() {
      return this.h().worldServer[0].getGameRules().getBoolean("sendCommandFeedback");
   }

   public boolean replaceItemInInventory(int i, ItemStack itemstack) {
      if (i >= 0 && i < this.inventory.mainInventory.length) {
         this.inventory.setInventorySlotContents(i, itemstack);
         return true;
      } else {
         EntityEquipmentSlot enumitemslot;
         if (i == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
            enumitemslot = EntityEquipmentSlot.HEAD;
         } else if (i == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
            enumitemslot = EntityEquipmentSlot.CHEST;
         } else if (i == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
            enumitemslot = EntityEquipmentSlot.LEGS;
         } else if (i == 100 + EntityEquipmentSlot.FEET.getIndex()) {
            enumitemslot = EntityEquipmentSlot.FEET;
         } else {
            enumitemslot = null;
         }

         if (i == 98) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemstack);
            return true;
         } else if (i == 99) {
            this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, itemstack);
            return true;
         } else if (enumitemslot == null) {
            int j = i - 200;
            if (j >= 0 && j < this.theInventoryEnderChest.getSizeInventory()) {
               this.theInventoryEnderChest.setInventorySlotContents(j, itemstack);
               return true;
            } else {
               return false;
            }
         } else {
            if (itemstack != null && itemstack.getItem() != null) {
               if (!(itemstack.getItem() instanceof ItemArmor) && !(itemstack.getItem() instanceof ItemElytra)) {
                  if (enumitemslot != EntityEquipmentSlot.HEAD) {
                     return false;
                  }
               } else if (EntityLiving.getSlotForItemStack(itemstack) != enumitemslot) {
                  return false;
               }
            }

            this.inventory.setInventorySlotContents(enumitemslot.getIndex() + this.inventory.mainInventory.length, itemstack);
            return true;
         }
      }
   }

   public EnumHandSide getPrimaryHand() {
      return ((Byte)this.dataManager.get(MAIN_HAND)).byteValue() == 0 ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
   }

   public void setPrimaryHand(EnumHandSide enummainhand) {
      this.dataManager.set(MAIN_HAND, Byte.valueOf((byte)(enummainhand == EnumHandSide.LEFT ? 0 : 1)));
   }

   public float getCooldownPeriod() {
      return (float)(1.0D / this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0D);
   }

   public float getCooledAttackStrength(float f) {
      return MathHelper.clamp(((float)this.ticksSinceLastSwing + f) / this.getCooldownPeriod(), 0.0F, 1.0F);
   }

   public void resetCooldown() {
      this.ticksSinceLastSwing = 0;
   }

   public CooldownTracker getCooldownTracker() {
      return this.cooldownTracker;
   }

   public void applyEntityCollision(Entity entity) {
      if (!this.isPlayerSleeping()) {
         super.applyEntityCollision(entity);
      }

   }

   public float getLuck() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue();
   }

   public boolean canUseCommandBlock() {
      return this.capabilities.isCreativeMode && this.canUseCommand(2, "");
   }

   public static enum EnumChatVisibility {
      FULL(0, "options.chat.visibility.full"),
      SYSTEM(1, "options.chat.visibility.system"),
      HIDDEN(2, "options.chat.visibility.hidden");

      private static final EntityPlayer.EnumChatVisibility[] ID_LOOKUP = new EntityPlayer.EnumChatVisibility[values().length];
      private final int chatVisibility;
      private final String resourceKey;

      static {
         for(EntityPlayer.EnumChatVisibility entityhuman_enumchatvisibility : values()) {
            ID_LOOKUP[entityhuman_enumchatvisibility.chatVisibility] = entityhuman_enumchatvisibility;
         }

      }

      private EnumChatVisibility(int i, String s) {
         this.chatVisibility = i;
         this.resourceKey = s;
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

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.SOUTH.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 4;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
