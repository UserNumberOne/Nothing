package net.minecraft.entity.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.CooldownTrackerServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.JsonSerializableSet;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AchievementEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent.Close;
import net.minecraftforge.event.entity.player.PlayerContainerEvent.Open;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityPlayerMP extends EntityPlayer implements IContainerListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private String language = "en_US";
   public NetHandlerPlayServer connection;
   public final MinecraftServer mcServer;
   public final PlayerInteractionManager interactionManager;
   public double managedPosX;
   public double managedPosZ;
   private final List entityRemoveQueue = Lists.newLinkedList();
   private final StatisticsManagerServer statsFile;
   private float lastHealthScore = Float.MIN_VALUE;
   private int lastFoodScore = Integer.MIN_VALUE;
   private int lastAirScore = Integer.MIN_VALUE;
   private int lastArmorScore = Integer.MIN_VALUE;
   private int lastLevelScore = Integer.MIN_VALUE;
   private int lastExperienceScore = Integer.MIN_VALUE;
   private float lastHealth = -1.0E8F;
   private int lastFoodLevel = -99999999;
   private boolean wasHungry = true;
   private int lastExperience = -99999999;
   private int respawnInvulnerabilityTicks = 60;
   private EntityPlayer.EnumChatVisibility chatVisibility;
   private boolean chatColours = true;
   private long playerLastActiveTime = System.currentTimeMillis();
   private Entity spectatingEntity;
   private boolean invulnerableDimensionChange;
   public int currentWindowId;
   public boolean isChangingQuantityOnly;
   public int ping;
   public boolean playerConqueredTheEnd;

   public EntityPlayerMP(MinecraftServer var1, WorldServer var2, GameProfile var3, PlayerInteractionManager var4) {
      super(var2, var3);
      var4.player = this;
      this.interactionManager = var4;
      BlockPos var5 = var2.provider.getRandomizedSpawnPoint();
      this.mcServer = var1;
      this.statsFile = var1.getPlayerList().getPlayerStatsFile(this);
      this.stepHeight = 0.0F;
      this.moveToBlockPosAndAngles(var5, 0.0F, 0.0F);

      while(!var2.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.posY < 255.0D) {
         this.setPosition(this.posX, this.posY + 1.0D, this.posZ);
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("playerGameType", 99)) {
         if (this.getServer().getForceGamemode()) {
            this.interactionManager.setGameType(this.getServer().getGameType());
         } else {
            this.interactionManager.setGameType(GameType.getByID(var1.getInteger("playerGameType")));
         }
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("playerGameType", this.interactionManager.getGameType().getID());
      Entity var2 = this.getLowestRidingEntity();
      if (this.getRidingEntity() != null && var2 != this & var2.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
         NBTTagCompound var3 = new NBTTagCompound();
         NBTTagCompound var4 = new NBTTagCompound();
         var2.writeToNBTOptional(var4);
         var3.setUniqueId("Attach", this.getRidingEntity().getUniqueID());
         var3.setTag("Entity", var4);
         var1.setTag("RootVehicle", var3);
      }

   }

   public void addExperienceLevel(int var1) {
      super.addExperienceLevel(var1);
      this.lastExperience = -1;
   }

   public void removeExperienceLevel(int var1) {
      super.removeExperienceLevel(var1);
      this.lastExperience = -1;
   }

   public void addSelfToInternalCraftingInventory() {
      this.openContainer.addListener(this);
   }

   public void sendEnterCombat() {
      super.sendEnterCombat();
      this.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTER_COMBAT));
   }

   public void sendEndCombat() {
      super.sendEndCombat();
      this.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.END_COMBAT));
   }

   protected CooldownTracker createCooldownTracker() {
      return new CooldownTrackerServer(this);
   }

   public void onUpdate() {
      this.interactionManager.updateBlockRemoving();
      --this.respawnInvulnerabilityTicks;
      if (this.hurtResistantTime > 0) {
         --this.hurtResistantTime;
      }

      this.openContainer.detectAndSendChanges();
      if (!this.world.isRemote && this.openContainer != null && !this.openContainer.canInteractWith(this)) {
         this.closeScreen();
         this.openContainer = this.inventoryContainer;
      }

      while(!this.entityRemoveQueue.isEmpty()) {
         int var1 = Math.min(this.entityRemoveQueue.size(), Integer.MAX_VALUE);
         int[] var2 = new int[var1];
         Iterator var3 = this.entityRemoveQueue.iterator();
         int var4 = 0;

         while(var3.hasNext() && var4 < var1) {
            var2[var4++] = ((Integer)var3.next()).intValue();
            var3.remove();
         }

         this.connection.sendPacket(new SPacketDestroyEntities(var2));
      }

      Entity var5 = this.getSpectatingEntity();
      if (var5 != this) {
         if (var5.isEntityAlive()) {
            this.setPositionAndRotation(var5.posX, var5.posY, var5.posZ, var5.rotationYaw, var5.rotationPitch);
            this.mcServer.getPlayerList().serverUpdateMovingPlayer(this);
            if (this.isSneaking()) {
               this.setSpectatingEntity(this);
            }
         } else {
            this.setSpectatingEntity(this);
         }
      }

   }

   public void onUpdateEntity() {
      try {
         super.onUpdate();

         for(int var1 = 0; var1 < this.inventory.getSizeInventory(); ++var1) {
            ItemStack var5 = this.inventory.getStackInSlot(var1);
            if (var5 != null && var5.getItem().isMap()) {
               Packet var6 = ((ItemMapBase)var5.getItem()).createMapDataPacket(var5, this.world, this);
               if (var6 != null) {
                  this.connection.sendPacket(var6);
               }
            }
         }

         if (this.getHealth() != this.lastHealth || this.lastFoodLevel != this.foodStats.getFoodLevel() || this.foodStats.getSaturationLevel() == 0.0F != this.wasHungry) {
            this.connection.sendPacket(new SPacketUpdateHealth(this.getHealth(), this.foodStats.getFoodLevel(), this.foodStats.getSaturationLevel()));
            this.lastHealth = this.getHealth();
            this.lastFoodLevel = this.foodStats.getFoodLevel();
            this.wasHungry = this.foodStats.getSaturationLevel() == 0.0F;
         }

         if (this.getHealth() + this.getAbsorptionAmount() != this.lastHealthScore) {
            this.lastHealthScore = this.getHealth() + this.getAbsorptionAmount();
            this.updateScorePoints(IScoreCriteria.HEALTH, MathHelper.ceil(this.lastHealthScore));
         }

         if (this.foodStats.getFoodLevel() != this.lastFoodScore) {
            this.lastFoodScore = this.foodStats.getFoodLevel();
            this.updateScorePoints(IScoreCriteria.FOOD, MathHelper.ceil((float)this.lastFoodScore));
         }

         if (this.getAir() != this.lastAirScore) {
            this.lastAirScore = this.getAir();
            this.updateScorePoints(IScoreCriteria.AIR, MathHelper.ceil((float)this.lastAirScore));
         }

         if (this.getTotalArmorValue() != this.lastArmorScore) {
            this.lastArmorScore = this.getTotalArmorValue();
            this.updateScorePoints(IScoreCriteria.ARMOR, MathHelper.ceil((float)this.lastArmorScore));
         }

         if (this.experienceTotal != this.lastExperienceScore) {
            this.lastExperienceScore = this.experienceTotal;
            this.updateScorePoints(IScoreCriteria.XP, MathHelper.ceil((float)this.lastExperienceScore));
         }

         if (this.experienceLevel != this.lastLevelScore) {
            this.lastLevelScore = this.experienceLevel;
            this.updateScorePoints(IScoreCriteria.LEVEL, MathHelper.ceil((float)this.lastLevelScore));
         }

         if (this.experienceTotal != this.lastExperience) {
            this.lastExperience = this.experienceTotal;
            this.connection.sendPacket(new SPacketSetExperience(this.experience, this.experienceTotal, this.experienceLevel));
         }

         if (this.ticksExisted % 20 * 5 == 0 && !this.getStatFile().hasAchievementUnlocked(AchievementList.EXPLORE_ALL_BIOMES)) {
            this.updateBiomesExplored();
         }

      } catch (Throwable var4) {
         CrashReport var2 = CrashReport.makeCrashReport(var4, "Ticking player");
         CrashReportCategory var3 = var2.makeCategory("Player being ticked");
         this.addEntityCrashInfo(var3);
         throw new ReportedException(var2);
      }
   }

   private void updateScorePoints(IScoreCriteria var1, int var2) {
      for(ScoreObjective var4 : this.getWorldScoreboard().getObjectivesFromCriteria(var1)) {
         Score var5 = this.getWorldScoreboard().getOrCreateScore(this.getName(), var4);
         var5.setScorePoints(var2);
      }

   }

   protected void updateBiomesExplored() {
      Biome var1 = this.world.getBiome(new BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ)));
      String var2 = var1.getBiomeName();
      JsonSerializableSet var3 = (JsonSerializableSet)this.getStatFile().getProgress(AchievementList.EXPLORE_ALL_BIOMES);
      if (var3 == null) {
         var3 = (JsonSerializableSet)this.getStatFile().setProgress(AchievementList.EXPLORE_ALL_BIOMES, new JsonSerializableSet());
      }

      var3.add(var2);
      if (this.getStatFile().canUnlockAchievement(AchievementList.EXPLORE_ALL_BIOMES) && var3.size() >= Biome.EXPLORATION_BIOMES_LIST.size()) {
         HashSet var4 = Sets.newHashSet(Biome.EXPLORATION_BIOMES_LIST);

         for(String var6 : var3) {
            Iterator var7 = var4.iterator();

            while(var7.hasNext()) {
               Biome var8 = (Biome)var7.next();
               if (var8.getBiomeName().equals(var6)) {
                  var7.remove();
               }
            }

            if (var4.isEmpty()) {
               break;
            }
         }

         if (var4.isEmpty()) {
            this.addStat(AchievementList.EXPLORE_ALL_BIOMES);
         }
      }

   }

   public void onDeath(DamageSource var1) {
      if (!ForgeHooks.onLivingDeath(this, var1)) {
         boolean var2 = this.world.getGameRules().getBoolean("showDeathMessages");
         this.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, var2));
         if (var2) {
            Team var3 = this.getTeam();
            if (var3 != null && var3.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
               if (var3.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                  this.mcServer.getPlayerList().sendMessageToAllTeamMembers(this, this.getCombatTracker().getDeathMessage());
               } else if (var3.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                  this.mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(this, this.getCombatTracker().getDeathMessage());
               }
            } else {
               this.mcServer.getPlayerList().sendChatMsg(this.getCombatTracker().getDeathMessage());
            }
         }

         if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            this.captureDrops = true;
            this.capturedDrops.clear();
            this.inventory.dropAllItems();
            this.captureDrops = false;
            PlayerDropsEvent var6 = new PlayerDropsEvent(this, var1, this.capturedDrops, this.recentlyHit > 0);
            if (!MinecraftForge.EVENT_BUS.post(var6)) {
               for(EntityItem var5 : this.capturedDrops) {
                  this.world.spawnEntity(var5);
               }
            }
         }

         for(ScoreObjective var9 : this.world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
            Score var11 = this.getWorldScoreboard().getOrCreateScore(this.getName(), var9);
            var11.incrementScore();
         }

         EntityLivingBase var8 = this.getAttackingEntity();
         if (var8 != null) {
            EntityList.EntityEggInfo var10 = (EntityList.EntityEggInfo)EntityList.ENTITY_EGGS.get(EntityList.getEntityString(var8));
            if (var10 != null) {
               this.addStat(var10.entityKilledByStat);
            }

            var8.addToPlayerScore(this, this.scoreValue);
         }

         this.addStat(StatList.DEATHS);
         this.takeStat(StatList.TIME_SINCE_DEATH);
         this.getCombatTracker().reset();
      }
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         boolean var3 = this.mcServer.isDedicatedServer() && this.canPlayersAttack() && "fall".equals(var1.damageType);
         if (!var3 && this.respawnInvulnerabilityTicks > 0 && var1 != DamageSource.outOfWorld) {
            return false;
         } else {
            if (var1 instanceof EntityDamageSource) {
               Entity var4 = var1.getEntity();
               if (var4 instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)var4)) {
                  return false;
               }

               if (var4 instanceof EntityArrow) {
                  EntityArrow var5 = (EntityArrow)var4;
                  if (var5.shootingEntity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)var5.shootingEntity)) {
                     return false;
                  }
               }
            }

            return super.attackEntityFrom(var1, var2);
         }
      }
   }

   public boolean canAttackPlayer(EntityPlayer var1) {
      return !this.canPlayersAttack() ? false : super.canAttackPlayer(var1);
   }

   private boolean canPlayersAttack() {
      return this.mcServer.isPVPEnabled();
   }

   @Nullable
   public Entity changeDimension(int var1) {
      if (!ForgeHooks.onTravelToDimension(this, var1)) {
         return this;
      } else {
         this.invulnerableDimensionChange = true;
         if (this.dimension == 1 && var1 == 1) {
            this.world.removeEntity(this);
            if (!this.playerConqueredTheEnd) {
               this.playerConqueredTheEnd = true;
               if (this.hasAchievement(AchievementList.THE_END2)) {
                  this.connection.sendPacket(new SPacketChangeGameState(4, 0.0F));
               } else {
                  this.addStat(AchievementList.THE_END2);
                  this.connection.sendPacket(new SPacketChangeGameState(4, 1.0F));
               }
            }

            return this;
         } else {
            if (this.dimension == 0 && var1 == 1) {
               this.addStat(AchievementList.THE_END);
               var1 = 1;
            } else {
               this.addStat(AchievementList.PORTAL);
            }

            this.mcServer.getPlayerList().changePlayerDimension(this, var1);
            this.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
            this.lastExperience = -1;
            this.lastHealth = -1.0F;
            this.lastFoodLevel = -1;
            return this;
         }
      }
   }

   public boolean isSpectatedByPlayer(EntityPlayerMP var1) {
      return var1.isSpectator() ? this.getSpectatingEntity() == this : (this.isSpectator() ? false : super.isSpectatedByPlayer(var1));
   }

   private void sendTileEntityUpdate(TileEntity var1) {
      if (var1 != null) {
         SPacketUpdateTileEntity var2 = var1.getUpdatePacket();
         if (var2 != null) {
            this.connection.sendPacket(var2);
         }
      }

   }

   public void onItemPickup(Entity var1, int var2) {
      super.onItemPickup(var1, var2);
      this.openContainer.detectAndSendChanges();
   }

   public EntityPlayer.SleepResult trySleep(BlockPos var1) {
      EntityPlayer.SleepResult var2 = super.trySleep(var1);
      if (var2 == EntityPlayer.SleepResult.OK) {
         this.addStat(StatList.SLEEP_IN_BED);
         SPacketUseBed var3 = new SPacketUseBed(this, var1);
         this.getServerWorld().getEntityTracker().sendToTracking(this, var3);
         this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         this.connection.sendPacket(var3);
      }

      return var2;
   }

   public void wakeUpPlayer(boolean var1, boolean var2, boolean var3) {
      if (this.isPlayerSleeping()) {
         this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(this, 2));
      }

      super.wakeUpPlayer(var1, var2, var3);
      if (this.connection != null) {
         this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      }

   }

   public boolean startRiding(Entity var1, boolean var2) {
      Entity var3 = this.getRidingEntity();
      if (!super.startRiding(var1, var2)) {
         return false;
      } else {
         Entity var4 = this.getRidingEntity();
         if (var4 != var3 && this.connection != null) {
            this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         }

         return true;
      }
   }

   public void dismountRidingEntity() {
      Entity var1 = this.getRidingEntity();
      super.dismountRidingEntity();
      Entity var2 = this.getRidingEntity();
      if (var2 != var1 && this.connection != null) {
         this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      }

   }

   public boolean isEntityInvulnerable(DamageSource var1) {
      return super.isEntityInvulnerable(var1) || this.isInvulnerableDimensionChange();
   }

   protected void updateFallState(double var1, boolean var3, IBlockState var4, BlockPos var5) {
   }

   protected void frostWalk(BlockPos var1) {
      if (!this.isSpectator()) {
         super.frostWalk(var1);
      }

   }

   public void handleFalling(double var1, boolean var3) {
      int var4 = MathHelper.floor(this.posX);
      int var5 = MathHelper.floor(this.posY - 0.20000000298023224D);
      int var6 = MathHelper.floor(this.posZ);
      BlockPos var7 = new BlockPos(var4, var5, var6);
      IBlockState var8 = this.world.getBlockState(var7);
      if (var8.getBlock().isAir(var8, this.world, var7)) {
         BlockPos var9 = var7.down();
         IBlockState var10 = this.world.getBlockState(var9);
         Block var11 = var10.getBlock();
         if (var11 instanceof BlockFence || var11 instanceof BlockWall || var11 instanceof BlockFenceGate) {
            var7 = var9;
            var8 = var10;
         }
      }

      super.updateFallState(var1, var3, var8, var7);
   }

   public void openEditSign(TileEntitySign var1) {
      var1.setPlayer(this);
      this.connection.sendPacket(new SPacketSignEditorOpen(var1.getPos()));
   }

   public void getNextWindowId() {
      this.currentWindowId = this.currentWindowId % 100 + 1;
   }

   public void displayGui(IInteractionObject var1) {
      if (var1 instanceof ILootContainer && ((ILootContainer)var1).getLootTable() != null && this.isSpectator()) {
         this.sendMessage((new TextComponentTranslation("container.spectatorCantOpen", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)));
      } else {
         this.getNextWindowId();
         this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, var1.getGuiID(), var1.getDisplayName()));
         this.openContainer = var1.createContainer(this.inventory, this);
         this.openContainer.windowId = this.currentWindowId;
         this.openContainer.addListener(this);
         MinecraftForge.EVENT_BUS.post(new Open(this, this.openContainer));
      }

   }

   public void displayGUIChest(IInventory var1) {
      if (var1 instanceof ILootContainer && ((ILootContainer)var1).getLootTable() != null && this.isSpectator()) {
         this.sendMessage((new TextComponentTranslation("container.spectatorCantOpen", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)));
      } else {
         if (this.openContainer != this.inventoryContainer) {
            this.closeScreen();
         }

         if (var1 instanceof ILockableContainer) {
            ILockableContainer var2 = (ILockableContainer)var1;
            if (var2.isLocked() && !this.canOpen(var2.getLockCode()) && !this.isSpectator()) {
               this.connection.sendPacket(new SPacketChat(new TextComponentTranslation("container.isLocked", new Object[]{var1.getDisplayName()}), (byte)2));
               this.connection.sendPacket(new SPacketSoundEffect(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, this.posX, this.posY, this.posZ, 1.0F, 1.0F));
               return;
            }
         }

         this.getNextWindowId();
         if (var1 instanceof IInteractionObject) {
            this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, ((IInteractionObject)var1).getGuiID(), var1.getDisplayName(), var1.getSizeInventory()));
            this.openContainer = ((IInteractionObject)var1).createContainer(this.inventory, this);
         } else {
            this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "minecraft:container", var1.getDisplayName(), var1.getSizeInventory()));
            this.openContainer = new ContainerChest(this.inventory, var1, this);
         }

         this.openContainer.windowId = this.currentWindowId;
         this.openContainer.addListener(this);
         MinecraftForge.EVENT_BUS.post(new Open(this, this.openContainer));
      }

   }

   public void displayVillagerTradeGui(IMerchant var1) {
      this.getNextWindowId();
      this.openContainer = new ContainerMerchant(this.inventory, var1, this.world);
      this.openContainer.windowId = this.currentWindowId;
      this.openContainer.addListener(this);
      MinecraftForge.EVENT_BUS.post(new Open(this, this.openContainer));
      InventoryMerchant var2 = ((ContainerMerchant)this.openContainer).getMerchantInventory();
      ITextComponent var3 = var1.getDisplayName();
      this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "minecraft:villager", var3, var2.getSizeInventory()));
      MerchantRecipeList var4 = var1.getRecipes(this);
      if (var4 != null) {
         PacketBuffer var5 = new PacketBuffer(Unpooled.buffer());
         var5.writeInt(this.currentWindowId);
         var4.writeToBuf(var5);
         this.connection.sendPacket(new SPacketCustomPayload("MC|TrList", var5));
      }

   }

   public void openGuiHorseInventory(EntityHorse var1, IInventory var2) {
      if (this.openContainer != this.inventoryContainer) {
         this.closeScreen();
      }

      this.getNextWindowId();
      this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "EntityHorse", var2.getDisplayName(), var2.getSizeInventory(), var1.getEntityId()));
      this.openContainer = new ContainerHorseInventory(this.inventory, var2, var1, this);
      this.openContainer.windowId = this.currentWindowId;
      this.openContainer.addListener(this);
   }

   public void openBook(ItemStack var1, EnumHand var2) {
      Item var3 = var1.getItem();
      if (var3 == Items.WRITTEN_BOOK) {
         PacketBuffer var4 = new PacketBuffer(Unpooled.buffer());
         var4.writeEnumValue(var2);
         this.connection.sendPacket(new SPacketCustomPayload("MC|BOpen", var4));
      }

   }

   public void displayGuiCommandBlock(TileEntityCommandBlock var1) {
      var1.setSendToClient(true);
      this.sendTileEntityUpdate(var1);
   }

   public void sendSlotContents(Container var1, int var2, ItemStack var3) {
      if (!(var1.getSlot(var2) instanceof SlotCrafting) && !this.isChangingQuantityOnly) {
         this.connection.sendPacket(new SPacketSetSlot(var1.windowId, var2, var3));
      }

   }

   public void sendContainerToPlayer(Container var1) {
      this.updateCraftingInventory(var1, var1.getInventory());
   }

   public void updateCraftingInventory(Container var1, List var2) {
      this.connection.sendPacket(new SPacketWindowItems(var1.windowId, var2));
      this.connection.sendPacket(new SPacketSetSlot(-1, -1, this.inventory.getItemStack()));
   }

   public void sendProgressBarUpdate(Container var1, int var2, int var3) {
      this.connection.sendPacket(new SPacketWindowProperty(var1.windowId, var2, var3));
   }

   public void sendAllWindowProperties(Container var1, IInventory var2) {
      for(int var3 = 0; var3 < var2.getFieldCount(); ++var3) {
         this.connection.sendPacket(new SPacketWindowProperty(var1.windowId, var3, var2.getField(var3)));
      }

   }

   public void closeScreen() {
      this.connection.sendPacket(new SPacketCloseWindow(this.openContainer.windowId));
      this.closeContainer();
   }

   public void updateHeldItem() {
      if (!this.isChangingQuantityOnly) {
         this.connection.sendPacket(new SPacketSetSlot(-1, -1, this.inventory.getItemStack()));
      }

   }

   public void closeContainer() {
      this.openContainer.onContainerClosed(this);
      MinecraftForge.EVENT_BUS.post(new Close(this, this.openContainer));
      this.openContainer = this.inventoryContainer;
   }

   public void setEntityActionState(float var1, float var2, boolean var3, boolean var4) {
      if (this.isRiding()) {
         if (var1 >= -1.0F && var1 <= 1.0F) {
            this.moveStrafing = var1;
         }

         if (var2 >= -1.0F && var2 <= 1.0F) {
            this.moveForward = var2;
         }

         this.isJumping = var3;
         this.setSneaking(var4);
      }

   }

   public boolean hasAchievement(Achievement var1) {
      return this.statsFile.hasAchievementUnlocked(var1);
   }

   public void addStat(StatBase var1, int var2) {
      if (var1 != null) {
         if (var1.isAchievement() && MinecraftForge.EVENT_BUS.post(new AchievementEvent(this, (Achievement)var1))) {
            return;
         }

         this.statsFile.increaseStat(this, var1, var2);

         for(ScoreObjective var4 : this.getWorldScoreboard().getObjectivesFromCriteria(var1.getCriteria())) {
            this.getWorldScoreboard().getOrCreateScore(this.getName(), var4).increaseScore(var2);
         }

         if (this.statsFile.hasUnsentAchievement()) {
            this.statsFile.sendStats(this);
         }
      }

   }

   public void takeStat(StatBase var1) {
      if (var1 != null) {
         this.statsFile.unlockAchievement(this, var1, 0);

         for(ScoreObjective var3 : this.getWorldScoreboard().getObjectivesFromCriteria(var1.getCriteria())) {
            this.getWorldScoreboard().getOrCreateScore(this.getName(), var3).setScorePoints(0);
         }

         if (this.statsFile.hasUnsentAchievement()) {
            this.statsFile.sendStats(this);
         }
      }

   }

   public void mountEntityAndWakeUp() {
      this.removePassengers();
      if (this.sleeping) {
         this.wakeUpPlayer(true, false, false);
      }

   }

   public void setPlayerHealthUpdated() {
      this.lastHealth = -1.0E8F;
   }

   public void sendStatusMessage(ITextComponent var1) {
      this.connection.sendPacket(new SPacketChat(var1));
   }

   protected void onItemUseFinish() {
      if (this.activeItemStack != null && this.isHandActive()) {
         this.connection.sendPacket(new SPacketEntityStatus(this, (byte)9));
         super.onItemUseFinish();
      }

   }

   public void clonePlayer(EntityPlayer var1, boolean var2) {
      super.clonePlayer(var1, var2);
      this.lastExperience = -1;
      this.lastHealth = -1.0F;
      this.lastFoodLevel = -1;
      this.entityRemoveQueue.addAll(((EntityPlayerMP)var1).entityRemoveQueue);
   }

   protected void onNewPotionEffect(PotionEffect var1) {
      super.onNewPotionEffect(var1);
      this.connection.sendPacket(new SPacketEntityEffect(this.getEntityId(), var1));
   }

   protected void onChangedPotionEffect(PotionEffect var1, boolean var2) {
      super.onChangedPotionEffect(var1, var2);
      this.connection.sendPacket(new SPacketEntityEffect(this.getEntityId(), var1));
   }

   protected void onFinishedPotionEffect(PotionEffect var1) {
      super.onFinishedPotionEffect(var1);
      this.connection.sendPacket(new SPacketRemoveEntityEffect(this.getEntityId(), var1.getPotion()));
   }

   public void setPositionAndUpdate(double var1, double var3, double var5) {
      this.connection.setPlayerLocation(var1, var3, var5, this.rotationYaw, this.rotationPitch);
   }

   public void onCriticalHit(Entity var1) {
      this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(var1, 4));
   }

   public void onEnchantmentCritical(Entity var1) {
      this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(var1, 5));
   }

   public void sendPlayerAbilities() {
      if (this.connection != null) {
         this.connection.sendPacket(new SPacketPlayerAbilities(this.capabilities));
         this.updatePotionMetadata();
      }

   }

   public WorldServer getServerWorld() {
      return (WorldServer)this.world;
   }

   public void setGameType(GameType var1) {
      this.interactionManager.setGameType(var1);
      this.connection.sendPacket(new SPacketChangeGameState(3, (float)var1.getID()));
      if (var1 == GameType.SPECTATOR) {
         this.dismountRidingEntity();
      } else {
         this.setSpectatingEntity(this);
      }

      this.sendPlayerAbilities();
      this.markPotionsDirty();
   }

   public boolean isSpectator() {
      return this.interactionManager.getGameType() == GameType.SPECTATOR;
   }

   public boolean isCreative() {
      return this.interactionManager.getGameType() == GameType.CREATIVE;
   }

   public void sendMessage(ITextComponent var1) {
      this.connection.sendPacket(new SPacketChat(var1));
   }

   public boolean canUseCommand(int var1, String var2) {
      if ("seed".equals(var2) && !this.mcServer.isDedicatedServer()) {
         return true;
      } else if (!"tell".equals(var2) && !"help".equals(var2) && !"me".equals(var2) && !"trigger".equals(var2)) {
         if (this.mcServer.getPlayerList().canSendCommands(this.getGameProfile())) {
            UserListOpsEntry var3 = (UserListOpsEntry)this.mcServer.getPlayerList().getOppedPlayers().getEntry(this.getGameProfile());
            return var3 != null ? var3.getPermissionLevel() >= var1 : this.mcServer.getOpPermissionLevel() >= var1;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public String getPlayerIP() {
      String var1 = this.connection.netManager.getRemoteAddress().toString();
      var1 = var1.substring(var1.indexOf("/") + 1);
      var1 = var1.substring(0, var1.indexOf(":"));
      return var1;
   }

   public void handleClientSettings(CPacketClientSettings var1) {
      this.language = var1.getLang();
      this.chatVisibility = var1.getChatVisibility();
      this.chatColours = var1.isColorsEnabled();
      this.getDataManager().set(PLAYER_MODEL_FLAG, Byte.valueOf((byte)var1.getModelPartFlags()));
      this.getDataManager().set(MAIN_HAND, Byte.valueOf((byte)(var1.getMainHand() == EnumHandSide.LEFT ? 0 : 1)));
   }

   public EntityPlayer.EnumChatVisibility getChatVisibility() {
      return this.chatVisibility;
   }

   public void loadResourcePack(String var1, String var2) {
      this.connection.sendPacket(new SPacketResourcePackSend(var1, var2));
   }

   public BlockPos getPosition() {
      return new BlockPos(this.posX, this.posY + 0.5D, this.posZ);
   }

   public void markPlayerActive() {
      this.playerLastActiveTime = MinecraftServer.getCurrentTimeMillis();
   }

   public StatisticsManagerServer getStatFile() {
      return this.statsFile;
   }

   public void removeEntity(Entity var1) {
      if (var1 instanceof EntityPlayer) {
         this.connection.sendPacket(new SPacketDestroyEntities(new int[]{var1.getEntityId()}));
      } else {
         this.entityRemoveQueue.add(Integer.valueOf(var1.getEntityId()));
      }

   }

   public void addEntity(Entity var1) {
      this.entityRemoveQueue.remove(Integer.valueOf(var1.getEntityId()));
   }

   protected void updatePotionMetadata() {
      if (this.isSpectator()) {
         this.resetPotionEffectMetadata();
         this.setInvisible(true);
      } else {
         super.updatePotionMetadata();
      }

      this.getServerWorld().getEntityTracker().updateVisibility(this);
   }

   public Entity getSpectatingEntity() {
      return (Entity)(this.spectatingEntity == null ? this : this.spectatingEntity);
   }

   public void setSpectatingEntity(Entity var1) {
      Entity var2 = this.getSpectatingEntity();
      this.spectatingEntity = (Entity)(var1 == null ? this : var1);
      if (var2 != this.spectatingEntity) {
         this.connection.sendPacket(new SPacketCamera(this.spectatingEntity));
         this.setPositionAndUpdate(this.spectatingEntity.posX, this.spectatingEntity.posY, this.spectatingEntity.posZ);
      }

   }

   protected void decrementTimeUntilPortal() {
      if (this.timeUntilPortal > 0 && !this.invulnerableDimensionChange) {
         --this.timeUntilPortal;
      }

   }

   public void attackTargetEntityWithCurrentItem(Entity var1) {
      if (this.interactionManager.getGameType() == GameType.SPECTATOR) {
         this.setSpectatingEntity(var1);
      } else {
         super.attackTargetEntityWithCurrentItem(var1);
      }

   }

   public long getLastActiveTime() {
      return this.playerLastActiveTime;
   }

   @Nullable
   public ITextComponent getTabListDisplayName() {
      return null;
   }

   public void swingArm(EnumHand var1) {
      super.swingArm(var1);
      this.resetCooldown();
   }

   public boolean isInvulnerableDimensionChange() {
      return this.invulnerableDimensionChange;
   }

   public void clearInvulnerableDimensionChange() {
      this.invulnerableDimensionChange = false;
   }

   public void setElytraFlying() {
      this.setFlag(7, true);
   }

   public void clearElytraFlying() {
      this.setFlag(7, true);
      this.setFlag(7, false);
   }
}
