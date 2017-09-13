package net.minecraft.entity.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
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
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.src.MinecraftServer;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.CooldownTrackerServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.FoodStats;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.ILootContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftChatMessage;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;

public class EntityPlayerMP extends EntityPlayer implements IContainerListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private String language = "en_US";
   public NetHandlerPlayServer connection;
   public final MinecraftServer mcServer;
   public final PlayerInteractionManager interactionManager;
   public double managedPosX;
   public double managedPosZ;
   public final List entityRemoveQueue = Lists.newLinkedList();
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
   public int lastExperience = -99999999;
   public int respawnInvulnerabilityTicks = 60;
   private EntityPlayer.EnumChatVisibility chatVisibility;
   private boolean chatColours = true;
   private long playerLastActiveTime = System.currentTimeMillis();
   private Entity spectatingEntity;
   public boolean invulnerableDimensionChange;
   private int currentWindowId;
   public boolean isChangingQuantityOnly;
   public int ping;
   public boolean playerConqueredTheEnd;
   public String displayName;
   public ITextComponent listName;
   public Location compassTarget;
   public int newExp = 0;
   public int newLevel = 0;
   public int newTotalExp = 0;
   public boolean keepLevel = false;
   public double maxHealthCache;
   public boolean joining = true;
   public long timeOffset = 0L;
   public boolean relativeTime = true;
   public WeatherType weather = null;
   private float pluginRainPosition;
   private float pluginRainPositionPrevious;

   public EntityPlayerMP(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractionManager playerinteractmanager) {
      super(worldserver, gameprofile);
      playerinteractmanager.player = this;
      this.interactionManager = playerinteractmanager;
      BlockPos blockposition = worldserver.getSpawnPoint();
      if (!worldserver.provider.hasNoSky() && worldserver.getWorldInfo().getGameType() != GameType.ADVENTURE) {
         int i = Math.max(0, minecraftserver.a(worldserver));
         int j = MathHelper.floor(worldserver.getWorldBorder().getClosestDistance((double)blockposition.getX(), (double)blockposition.getZ()));
         if (j < i) {
            i = j;
         }

         if (j <= 1) {
            i = 1;
         }

         blockposition = worldserver.getTopSolidOrLiquidBlock(blockposition.add(this.rand.nextInt(i * 2 + 1) - i, 0, this.rand.nextInt(i * 2 + 1) - i));
      }

      this.mcServer = minecraftserver;
      this.statsFile = minecraftserver.getPlayerList().getPlayerStatsFile(this);
      this.stepHeight = 0.0F;
      this.moveToBlockPosAndAngles(blockposition, 0.0F, 0.0F);

      while(!worldserver.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.posY < 255.0D) {
         this.setPosition(this.posX, this.posY + 1.0D, this.posZ);
      }

      this.displayName = this.getName();
      this.maxHealthCache = (double)this.getMaxHealth();
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("playerGameType", 99)) {
         if (this.h().getForceGamemode()) {
            this.interactionManager.setGameType(this.h().getGamemode());
         } else {
            this.interactionManager.setGameType(GameType.getByID(nbttagcompound.getInteger("playerGameType")));
         }
      }

      this.getBukkitEntity().readExtraData(nbttagcompound);
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setInteger("playerGameType", this.interactionManager.getGameType().getID());
      Entity entity = this.getLowestRidingEntity();
      if (this.getRidingEntity() != null && entity != this & entity.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         NBTTagCompound nbttagcompound2 = new NBTTagCompound();
         entity.writeToNBTOptional(nbttagcompound2);
         nbttagcompound1.setUniqueId("Attach", this.getRidingEntity().getUniqueID());
         nbttagcompound1.setTag("Entity", nbttagcompound2);
         nbttagcompound.setTag("RootVehicle", nbttagcompound1);
      }

      this.getBukkitEntity().setExtraData(nbttagcompound);
   }

   public void setWorld(World world) {
      super.setWorld(world);
      if (world == null) {
         this.isDead = false;
         BlockPos position = null;
         if (this.spawnWorld != null && !this.spawnWorld.equals("")) {
            CraftWorld cworld = (CraftWorld)Bukkit.getServer().getWorld(this.spawnWorld);
            if (cworld != null && this.getBedLocation() != null) {
               world = cworld.getHandle();
               position = EntityPlayer.getBedSpawnLocation(cworld.getHandle(), this.getBedLocation(), false);
            }
         }

         if (world == null || position == null) {
            world = ((CraftWorld)Bukkit.getServer().getWorlds().get(0)).getHandle();
            position = world.getSpawnPoint();
         }

         this.world = world;
         this.setPosition((double)position.getX() + 0.5D, (double)position.getY(), (double)position.getZ() + 0.5D);
      }

      this.dimension = ((WorldServer)this.world).dimension;
      this.interactionManager.setWorld((WorldServer)world);
   }

   public void addExperienceLevel(int i) {
      super.addExperienceLevel(i);
      this.lastExperience = -1;
   }

   public void removeExperienceLevel(int i) {
      super.removeExperienceLevel(i);
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
      if (this.joining) {
         this.joining = false;
      }

      this.interactionManager.updateBlockRemoving();
      --this.respawnInvulnerabilityTicks;
      if (this.hurtResistantTime > 0) {
         --this.hurtResistantTime;
      }

      this.openContainer.detectAndSendChanges();
      if (!this.world.isRemote && !this.openContainer.canInteractWith(this)) {
         this.closeScreen();
         this.openContainer = this.inventoryContainer;
      }

      while(!this.entityRemoveQueue.isEmpty()) {
         int i = Math.min(this.entityRemoveQueue.size(), Integer.MAX_VALUE);
         int[] aint = new int[i];
         Iterator iterator = this.entityRemoveQueue.iterator();
         int j = 0;

         while(iterator.hasNext() && j < i) {
            aint[j++] = ((Integer)iterator.next()).intValue();
            iterator.remove();
         }

         this.connection.sendPacket(new SPacketDestroyEntities(aint));
      }

      Entity entity = this.getSpectatingEntity();
      if (entity != this) {
         if (entity.isEntityAlive()) {
            this.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
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

         for(int i = 0; i < this.inventory.getSizeInventory(); ++i) {
            net.minecraft.item.ItemStack itemstack = this.inventory.getStackInSlot(i);
            if (itemstack != null && itemstack.getItem().isMap()) {
               Packet packet = ((ItemMapBase)itemstack.getItem()).createMapDataPacket(itemstack, this.world, this);
               if (packet != null) {
                  this.connection.sendPacket(packet);
               }
            }
         }

         if (this.getHealth() != this.lastHealth || this.lastFoodLevel != this.foodStats.getFoodLevel() || this.foodStats.getSaturationLevel() == 0.0F != this.wasHungry) {
            this.connection.sendPacket(new SPacketUpdateHealth(this.getBukkitEntity().getScaledHealth(), this.foodStats.getFoodLevel(), this.foodStats.getSaturationLevel()));
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

         if (this.maxHealthCache != (double)this.getMaxHealth()) {
            this.getBukkitEntity().updateScaledHealth();
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

         if (this.oldLevel == -1) {
            this.oldLevel = this.experienceLevel;
         }

         if (this.oldLevel != this.experienceLevel) {
            CraftEventFactory.callPlayerLevelChangeEvent(this.world.getServer().getPlayer(this), this.oldLevel, this.experienceLevel);
            this.oldLevel = this.experienceLevel;
         }

      } catch (Throwable var4) {
         CrashReport crashreport = CrashReport.makeCrashReport(var4, "Ticking player");
         CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Player being ticked");
         this.addEntityCrashInfo(crashreportsystemdetails);
         throw new ReportedException(crashreport);
      }
   }

   private void updateScorePoints(IScoreCriteria iscoreboardcriteria, int i) {
      for(Score scoreboardscore : this.world.getServer().getScoreboardManager().getScoreboardScores(iscoreboardcriteria, this.getName(), new ArrayList())) {
         scoreboardscore.setScorePoints(i);
      }

   }

   protected void updateBiomesExplored() {
      Biome biomebase = this.world.getBiome(new BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ)));
      String s = biomebase.getBiomeName();
      JsonSerializableSet achievementset = (JsonSerializableSet)this.getStatFile().getProgress(AchievementList.EXPLORE_ALL_BIOMES);
      if (achievementset == null) {
         achievementset = (JsonSerializableSet)this.getStatFile().setProgress(AchievementList.EXPLORE_ALL_BIOMES, new JsonSerializableSet());
      }

      achievementset.add(s);
      if (this.getStatFile().canUnlockAchievement(AchievementList.EXPLORE_ALL_BIOMES) && achievementset.size() >= Biome.EXPLORATION_BIOMES_LIST.size()) {
         HashSet hashset = Sets.newHashSet(Biome.EXPLORATION_BIOMES_LIST);

         for(String s1 : achievementset) {
            Iterator iterator1 = hashset.iterator();

            while(iterator1.hasNext()) {
               Biome biomebase1 = (Biome)iterator1.next();
               if (biomebase1.getBiomeName().equals(s1)) {
                  iterator1.remove();
               }
            }

            if (hashset.isEmpty()) {
               break;
            }
         }

         if (hashset.isEmpty()) {
            this.addStat(AchievementList.EXPLORE_ALL_BIOMES);
         }
      }

   }

   public void onDeath(DamageSource damagesource) {
      boolean flag = this.world.getGameRules().getBoolean("showDeathMessages");
      this.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));
      if (!this.isDead) {
         List loot = new ArrayList();
         boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");
         if (!keepInventory) {
            for(int i = 0; i < this.inventory.mainInventory.length; ++i) {
               if (this.inventory.mainInventory[i] != null) {
                  loot.add(CraftItemStack.asCraftMirror(this.inventory.mainInventory[i]));
               }
            }

            for(int i = 0; i < this.inventory.armorInventory.length; ++i) {
               if (this.inventory.armorInventory[i] != null) {
                  loot.add(CraftItemStack.asCraftMirror(this.inventory.armorInventory[i]));
               }
            }

            for(int i = 0; i < this.inventory.offHandInventory.length; ++i) {
               if (this.inventory.offHandInventory[i] != null) {
                  loot.add(CraftItemStack.asCraftMirror(this.inventory.offHandInventory[i]));
               }
            }
         }

         ITextComponent chatmessage = this.getCombatTracker().getDeathMessage();
         String deathmessage = chatmessage.getUnformattedText();
         PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(this, loot, deathmessage, keepInventory);
         String deathMessage = event.getDeathMessage();
         if (deathMessage != null && deathMessage.length() > 0 && flag) {
            if (deathMessage.equals(deathmessage)) {
               Team scoreboardteambase = this.getTeam();
               if (scoreboardteambase != null && scoreboardteambase.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                  if (scoreboardteambase.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                     this.mcServer.getPlayerList().sendMessageToAllTeamMembers(this, chatmessage);
                  } else if (scoreboardteambase.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                     this.mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(this, chatmessage);
                  }
               } else {
                  this.mcServer.getPlayerList().sendChatMsg(chatmessage);
               }
            } else {
               this.mcServer.getPlayerList().sendMessage(CraftChatMessage.fromString(deathMessage));
            }
         }

         if (!event.getKeepInventory()) {
            for(int i = 0; i < this.inventory.mainInventory.length; ++i) {
               this.inventory.mainInventory[i] = null;
            }

            for(int i = 0; i < this.inventory.armorInventory.length; ++i) {
               this.inventory.armorInventory[i] = null;
            }

            for(int i = 0; i < this.inventory.offHandInventory.length; ++i) {
               this.inventory.offHandInventory[i] = null;
            }
         }

         this.closeScreen();
         this.setSpectatingEntity(this);

         for(Score scoreboardscore : this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreCriteria.DEATH_COUNT, this.getName(), new ArrayList())) {
            scoreboardscore.incrementScore();
         }

         EntityLivingBase entityliving = this.getAttackingEntity();
         if (entityliving != null) {
            EntityList.EntityEggInfo entitytypes_monsteregginfo = (EntityList.EntityEggInfo)EntityList.ENTITY_EGGS.get(EntityList.getEntityString(entityliving));
            if (entitytypes_monsteregginfo != null) {
               this.addStat(entitytypes_monsteregginfo.entityKilledByStat);
            }

            entityliving.addToPlayerScore(this, this.scoreValue);
         }

         this.addStat(StatList.DEATHS);
         this.takeStat(StatList.TIME_SINCE_DEATH);
         this.getCombatTracker().reset();
      }
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else {
         boolean flag = this.mcServer.aa() && this.canPlayersAttack() && "fall".equals(damagesource.damageType);
         if (!flag && this.respawnInvulnerabilityTicks > 0 && damagesource != DamageSource.outOfWorld) {
            return false;
         } else {
            if (damagesource instanceof EntityDamageSource) {
               Entity entity = damagesource.getEntity();
               if (entity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entity)) {
                  return false;
               }

               if (entity instanceof EntityArrow) {
                  EntityArrow entityarrow = (EntityArrow)entity;
                  if (entityarrow.shootingEntity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entityarrow.shootingEntity)) {
                     return false;
                  }
               }
            }

            return super.attackEntityFrom(damagesource, f);
         }
      }
   }

   public boolean canAttackPlayer(EntityPlayer entityhuman) {
      return !this.canPlayersAttack() ? false : super.canAttackPlayer(entityhuman);
   }

   private boolean canPlayersAttack() {
      return this.world.pvpMode;
   }

   @Nullable
   public Entity changeDimension(int i) {
      if (this.dimension == 1 && i == 1) {
         this.invulnerableDimensionChange = true;
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
         if (this.dimension == 0 && i == 1) {
            this.addStat(AchievementList.THE_END);
            i = 1;
         } else {
            this.addStat(AchievementList.PORTAL);
         }

         TeleportCause cause = this.dimension != 1 && i != 1 ? TeleportCause.NETHER_PORTAL : TeleportCause.END_PORTAL;
         this.mcServer.getPlayerList().changeDimension(this, i, cause);
         this.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
         this.lastExperience = -1;
         this.lastHealth = -1.0F;
         this.lastFoodLevel = -1;
         return this;
      }
   }

   public boolean isSpectatedByPlayer(EntityPlayerMP entityplayer) {
      return entityplayer.isSpectator() ? this.getSpectatingEntity() == this : (this.isSpectator() ? false : super.isSpectatedByPlayer(entityplayer));
   }

   private void sendTileEntityUpdate(TileEntity tileentity) {
      if (tileentity != null) {
         SPacketUpdateTileEntity packetplayouttileentitydata = tileentity.getUpdatePacket();
         if (packetplayouttileentitydata != null) {
            this.connection.sendPacket(packetplayouttileentitydata);
         }
      }

   }

   public void onItemPickup(Entity entity, int i) {
      super.onItemPickup(entity, i);
      this.openContainer.detectAndSendChanges();
   }

   public EntityPlayer.SleepResult trySleep(BlockPos blockposition) {
      EntityPlayer.SleepResult entityhuman_enumbedresult = super.trySleep(blockposition);
      if (entityhuman_enumbedresult == EntityPlayer.SleepResult.OK) {
         this.addStat(StatList.SLEEP_IN_BED);
         SPacketUseBed packetplayoutbed = new SPacketUseBed(this, blockposition);
         this.getServerWorld().getEntityTracker().sendToTracking(this, packetplayoutbed);
         this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         this.connection.sendPacket(packetplayoutbed);
      }

      return entityhuman_enumbedresult;
   }

   public void wakeUpPlayer(boolean flag, boolean flag1, boolean flag2) {
      if (this.sleeping) {
         if (this.isPlayerSleeping()) {
            this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(this, 2));
         }

         super.wakeUpPlayer(flag, flag1, flag2);
         if (this.connection != null) {
            this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         }

      }
   }

   public boolean startRiding(Entity entity, boolean flag) {
      Entity entity1 = this.getRidingEntity();
      if (!super.startRiding(entity, flag)) {
         return false;
      } else {
         Entity entity2 = this.getRidingEntity();
         if (entity2 != entity1 && this.connection != null) {
            this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         }

         return true;
      }
   }

   public void dismountRidingEntity() {
      Entity entity = this.getRidingEntity();
      super.dismountRidingEntity();
      Entity entity1 = this.getRidingEntity();
      if (entity1 != entity && this.connection != null) {
         this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      }

   }

   public boolean isEntityInvulnerable(DamageSource damagesource) {
      return super.isEntityInvulnerable(damagesource) || this.isInvulnerableDimensionChange();
   }

   protected void updateFallState(double d0, boolean flag, IBlockState iblockdata, BlockPos blockposition) {
   }

   protected void frostWalk(BlockPos blockposition) {
      if (!this.isSpectator()) {
         super.frostWalk(blockposition);
      }

   }

   public void handleFalling(double d0, boolean flag) {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posY - 0.20000000298023224D);
      int k = MathHelper.floor(this.posZ);
      BlockPos blockposition = new BlockPos(i, j, k);
      IBlockState iblockdata = this.world.getBlockState(blockposition);
      if (iblockdata.getMaterial() == Material.AIR) {
         BlockPos blockposition1 = blockposition.down();
         IBlockState iblockdata1 = this.world.getBlockState(blockposition1);
         Block block = iblockdata1.getBlock();
         if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
            blockposition = blockposition1;
            iblockdata = iblockdata1;
         }
      }

      super.updateFallState(d0, flag, iblockdata, blockposition);
   }

   public void openEditSign(TileEntitySign tileentitysign) {
      tileentitysign.setPlayer(this);
      this.connection.sendPacket(new SPacketSignEditorOpen(tileentitysign.getPos()));
   }

   public int nextContainerCounter() {
      this.currentWindowId = this.currentWindowId % 100 + 1;
      return this.currentWindowId;
   }

   public void displayGui(IInteractionObject itileentitycontainer) {
      Container container = CraftEventFactory.callInventoryOpenEvent(this, itileentitycontainer.createContainer(this.inventory, this));
      if (container != null) {
         this.nextContainerCounter();
         this.openContainer = container;
         this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, itileentitycontainer.getGuiID(), itileentitycontainer.getDisplayName()));
         this.openContainer.windowId = this.currentWindowId;
         this.openContainer.addListener(this);
      }
   }

   public void displayGUIChest(IInventory iinventory) {
      boolean cancelled = false;
      if (iinventory instanceof ILockableContainer) {
         ILockableContainer itileinventory = (ILockableContainer)iinventory;
         cancelled = itileinventory.isLocked() && !this.canOpen(itileinventory.getLockCode()) && !this.isSpectator();
      }

      Container container;
      if (iinventory instanceof IInteractionObject) {
         container = ((IInteractionObject)iinventory).createContainer(this.inventory, this);
      } else {
         container = new ContainerChest(this.inventory, iinventory, this);
      }

      container = CraftEventFactory.callInventoryOpenEvent(this, container, cancelled);
      if (container == null && !cancelled) {
         iinventory.closeInventory(this);
      } else {
         if (iinventory instanceof ILootContainer && ((ILootContainer)iinventory).getLootTable() != null && this.isSpectator()) {
            this.sendMessage((new TextComponentTranslation("container.spectatorCantOpen", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)));
         } else {
            if (this.openContainer != this.inventoryContainer) {
               this.closeScreen();
            }

            if (iinventory instanceof ILockableContainer) {
               ILockableContainer itileinventory = (ILockableContainer)iinventory;
               if (itileinventory.isLocked() && !this.canOpen(itileinventory.getLockCode()) && !this.isSpectator()) {
                  this.connection.sendPacket(new SPacketChat(new TextComponentTranslation("container.isLocked", new Object[]{iinventory.getDisplayName()}), (byte)2));
                  this.connection.sendPacket(new SPacketSoundEffect(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, this.posX, this.posY, this.posZ, 1.0F, 1.0F));
                  iinventory.closeInventory(this);
                  return;
               }
            }

            this.nextContainerCounter();
            if (iinventory instanceof IInteractionObject) {
               this.openContainer = container;
               this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, ((IInteractionObject)iinventory).getGuiID(), iinventory.getDisplayName(), iinventory.getSizeInventory()));
            } else {
               this.openContainer = container;
               this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "minecraft:container", iinventory.getDisplayName(), iinventory.getSizeInventory()));
            }

            this.openContainer.windowId = this.currentWindowId;
            this.openContainer.addListener(this);
         }

      }
   }

   public void displayVillagerTradeGui(IMerchant imerchant) {
      Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerMerchant(this.inventory, imerchant, this.world));
      if (container != null) {
         this.nextContainerCounter();
         this.openContainer = container;
         this.openContainer.windowId = this.currentWindowId;
         this.openContainer.addListener(this);
         InventoryMerchant inventorymerchant = ((ContainerMerchant)this.openContainer).getMerchantInventory();
         ITextComponent ichatbasecomponent = imerchant.getDisplayName();
         this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "minecraft:villager", ichatbasecomponent, inventorymerchant.getSizeInventory()));
         MerchantRecipeList merchantrecipelist = imerchant.getRecipes(this);
         if (merchantrecipelist != null) {
            PacketBuffer packetdataserializer = new PacketBuffer(Unpooled.buffer());
            packetdataserializer.writeInt(this.currentWindowId);
            merchantrecipelist.writeToBuf(packetdataserializer);
            this.connection.sendPacket(new SPacketCustomPayload("MC|TrList", packetdataserializer));
         }

      }
   }

   public void openGuiHorseInventory(EntityHorse entityhorse, IInventory iinventory) {
      Container container = CraftEventFactory.callInventoryOpenEvent(this, new ContainerHorseInventory(this.inventory, iinventory, entityhorse, this));
      if (container == null) {
         iinventory.closeInventory(this);
      } else {
         if (this.openContainer != this.inventoryContainer) {
            this.closeScreen();
         }

         this.nextContainerCounter();
         this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "EntityHorse", iinventory.getDisplayName(), iinventory.getSizeInventory(), entityhorse.getEntityId()));
         this.openContainer = container;
         this.openContainer.windowId = this.currentWindowId;
         this.openContainer.addListener(this);
      }
   }

   public void openBook(net.minecraft.item.ItemStack itemstack, EnumHand enumhand) {
      Item item = itemstack.getItem();
      if (item == Items.WRITTEN_BOOK) {
         PacketBuffer packetdataserializer = new PacketBuffer(Unpooled.buffer());
         packetdataserializer.writeEnumValue(enumhand);
         this.connection.sendPacket(new SPacketCustomPayload("MC|BOpen", packetdataserializer));
      }

   }

   public void displayGuiCommandBlock(TileEntityCommandBlock tileentitycommand) {
      tileentitycommand.setSendToClient(true);
      this.sendTileEntityUpdate(tileentitycommand);
   }

   public void sendSlotContents(Container container, int i, net.minecraft.item.ItemStack itemstack) {
      if (!(container.getSlot(i) instanceof SlotCrafting) && !this.isChangingQuantityOnly) {
         this.connection.sendPacket(new SPacketSetSlot(container.windowId, i, itemstack));
      }

   }

   public void sendContainerToPlayer(Container container) {
      this.updateCraftingInventory(container, container.getInventory());
   }

   public void updateCraftingInventory(Container container, List list) {
      this.connection.sendPacket(new SPacketWindowItems(container.windowId, list));
      this.connection.sendPacket(new SPacketSetSlot(-1, -1, this.inventory.getItemStack()));
      if (EnumSet.of(InventoryType.CRAFTING, InventoryType.WORKBENCH).contains(container.getBukkitView().getType())) {
         this.connection.sendPacket(new SPacketSetSlot(container.windowId, 0, container.getSlot(0).getStack()));
      }

   }

   public void sendProgressBarUpdate(Container container, int i, int j) {
      this.connection.sendPacket(new SPacketWindowProperty(container.windowId, i, j));
   }

   public void sendAllWindowProperties(Container container, IInventory iinventory) {
      for(int i = 0; i < iinventory.getFieldCount(); ++i) {
         this.connection.sendPacket(new SPacketWindowProperty(container.windowId, i, iinventory.getField(i)));
      }

   }

   public void closeScreen() {
      CraftEventFactory.handleInventoryCloseEvent(this);
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
      this.openContainer = this.inventoryContainer;
   }

   public void setEntityActionState(float f, float f1, boolean flag, boolean flag1) {
      if (this.isRiding()) {
         if (f >= -1.0F && f <= 1.0F) {
            this.moveStrafing = f;
         }

         if (f1 >= -1.0F && f1 <= 1.0F) {
            this.moveForward = f1;
         }

         this.isJumping = flag;
         this.setSneaking(flag1);
      }

   }

   public boolean hasAchievement(Achievement achievement) {
      return this.statsFile.hasAchievementUnlocked(achievement);
   }

   public void addStat(StatBase statistic, int i) {
      if (statistic != null) {
         this.statsFile.increaseStat(this, statistic, i);

         for(ScoreObjective scoreboardobjective : this.getWorldScoreboard().getObjectivesFromCriteria(statistic.getCriteria())) {
            this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreboardobjective).increaseScore(i);
         }

         if (this.statsFile.hasUnsentAchievement()) {
            this.statsFile.sendStats(this);
         }
      }

   }

   public void takeStat(StatBase statistic) {
      if (statistic != null) {
         this.statsFile.unlockAchievement(this, statistic, 0);

         for(ScoreObjective scoreboardobjective : this.getWorldScoreboard().getObjectivesFromCriteria(statistic.getCriteria())) {
            this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreboardobjective).setScorePoints(0);
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
      this.lastExperience = -1;
   }

   public void sendMessage(ITextComponent[] ichatbasecomponent) {
      for(ITextComponent component : ichatbasecomponent) {
         this.sendMessage(component);
      }

   }

   public void sendStatusMessage(ITextComponent ichatbasecomponent) {
      this.connection.sendPacket(new SPacketChat(ichatbasecomponent));
   }

   protected void onItemUseFinish() {
      if (this.activeItemStack != null && this.isHandActive()) {
         this.connection.sendPacket(new SPacketEntityStatus(this, (byte)9));
         super.onItemUseFinish();
      }

   }

   public void clonePlayer(EntityPlayer entityhuman, boolean flag) {
      super.clonePlayer(entityhuman, flag);
      this.lastExperience = -1;
      this.lastHealth = -1.0F;
      this.lastFoodLevel = -1;
      this.entityRemoveQueue.addAll(((EntityPlayerMP)entityhuman).entityRemoveQueue);
   }

   protected void onNewPotionEffect(PotionEffect mobeffect) {
      super.onNewPotionEffect(mobeffect);
      this.connection.sendPacket(new SPacketEntityEffect(this.getEntityId(), mobeffect));
   }

   protected void onChangedPotionEffect(PotionEffect mobeffect, boolean flag) {
      super.onChangedPotionEffect(mobeffect, flag);
      this.connection.sendPacket(new SPacketEntityEffect(this.getEntityId(), mobeffect));
   }

   protected void onFinishedPotionEffect(PotionEffect mobeffect) {
      super.onFinishedPotionEffect(mobeffect);
      this.connection.sendPacket(new SPacketRemoveEntityEffect(this.getEntityId(), mobeffect.getPotion()));
   }

   public void setPositionAndUpdate(double d0, double d1, double d2) {
      this.connection.setPlayerLocation(d0, d1, d2, this.rotationYaw, this.rotationPitch);
   }

   public void onCriticalHit(Entity entity) {
      this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(entity, 4));
   }

   public void onEnchantmentCritical(Entity entity) {
      this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(entity, 5));
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

   public void setGameType(GameType enumgamemode) {
      this.getBukkitEntity().setGameMode(GameMode.getByValue(enumgamemode.getID()));
   }

   public boolean isSpectator() {
      return this.interactionManager.getGameType() == GameType.SPECTATOR;
   }

   public boolean isCreative() {
      return this.interactionManager.getGameType() == GameType.CREATIVE;
   }

   public void sendMessage(ITextComponent ichatbasecomponent) {
      this.connection.sendPacket(new SPacketChat(ichatbasecomponent));
   }

   public boolean canUseCommand(int i, String s) {
      if ("@".equals(s)) {
         return this.getBukkitEntity().hasPermission("minecraft.command.selector");
      } else {
         return "".equals(s) ? this.getBukkitEntity().isOp() : this.getBukkitEntity().hasPermission("minecraft.command." + s);
      }
   }

   public String getPlayerIP() {
      String s = this.connection.netManager.getRemoteAddress().toString();
      s = s.substring(s.indexOf("/") + 1);
      s = s.substring(0, s.indexOf(":"));
      return s;
   }

   public void handleClientSettings(CPacketClientSettings packetplayinsettings) {
      if (this.getPrimaryHand() != packetplayinsettings.getMainHand()) {
         PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent(this.getBukkitEntity(), this.getPrimaryHand() == EnumHandSide.LEFT ? MainHand.LEFT : MainHand.RIGHT);
         this.mcServer.server.getPluginManager().callEvent(event);
      }

      this.language = packetplayinsettings.getLang();
      this.chatVisibility = packetplayinsettings.getChatVisibility();
      this.chatColours = packetplayinsettings.isColorsEnabled();
      this.getDataManager().set(PLAYER_MODEL_FLAG, Byte.valueOf((byte)packetplayinsettings.getModelPartFlags()));
      this.getDataManager().set(MAIN_HAND, Byte.valueOf((byte)(packetplayinsettings.getMainHand() == EnumHandSide.LEFT ? 0 : 1)));
   }

   public EntityPlayer.EnumChatVisibility getChatVisibility() {
      return this.chatVisibility;
   }

   public void loadResourcePack(String s, String s1) {
      this.connection.sendPacket(new SPacketResourcePackSend(s, s1));
   }

   public BlockPos getPosition() {
      return new BlockPos(this.posX, this.posY + 0.5D, this.posZ);
   }

   public void markPlayerActive() {
      this.playerLastActiveTime = MinecraftServer.av();
   }

   public StatisticsManagerServer getStatFile() {
      return this.statsFile;
   }

   public void removeEntity(Entity entity) {
      if (entity instanceof EntityPlayer) {
         this.connection.sendPacket(new SPacketDestroyEntities(new int[]{entity.getEntityId()}));
      } else {
         this.entityRemoveQueue.add(Integer.valueOf(entity.getEntityId()));
      }

   }

   public void addEntity(Entity entity) {
      this.entityRemoveQueue.remove(Integer.valueOf(entity.getEntityId()));
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

   public void setSpectatingEntity(Entity entity) {
      Entity entity1 = this.getSpectatingEntity();
      this.spectatingEntity = (Entity)(entity == null ? this : entity);
      if (entity1 != this.spectatingEntity) {
         this.connection.sendPacket(new SPacketCamera(this.spectatingEntity));
         this.setPositionAndUpdate(this.spectatingEntity.posX, this.spectatingEntity.posY, this.spectatingEntity.posZ);
      }

   }

   protected void decrementTimeUntilPortal() {
      if (this.timeUntilPortal > 0 && !this.invulnerableDimensionChange) {
         --this.timeUntilPortal;
      }

   }

   public void attackTargetEntityWithCurrentItem(Entity entity) {
      if (this.interactionManager.getGameType() == GameType.SPECTATOR) {
         this.setSpectatingEntity(entity);
      } else {
         super.attackTargetEntityWithCurrentItem(entity);
      }

   }

   public long getLastActiveTime() {
      return this.playerLastActiveTime;
   }

   @Nullable
   public ITextComponent getTabListDisplayName() {
      return this.listName;
   }

   public void swingArm(EnumHand enumhand) {
      super.swingArm(enumhand);
      this.resetCooldown();
   }

   public boolean isInvulnerableDimensionChange() {
      return this.invulnerableDimensionChange;
   }

   public void clearInvulnerableDimensionChange() {
      this.invulnerableDimensionChange = false;
   }

   public void setElytraFlying() {
      if (!CraftEventFactory.callToggleGlideEvent(this, true).isCancelled()) {
         this.setFlag(7, true);
      }

   }

   public void clearElytraFlying() {
      if (!CraftEventFactory.callToggleGlideEvent(this, false).isCancelled()) {
         this.setFlag(7, true);
         this.setFlag(7, false);
      }

   }

   public long getPlayerTime() {
      return this.relativeTime ? this.world.getWorldTime() + this.timeOffset : this.world.getWorldTime() - this.world.getWorldTime() % 24000L + this.timeOffset;
   }

   public WeatherType getPlayerWeather() {
      return this.weather;
   }

   public void setPlayerWeather(WeatherType type, boolean plugin) {
      if (plugin || this.weather == null) {
         if (plugin) {
            this.weather = type;
         }

         if (type == WeatherType.DOWNFALL) {
            this.connection.sendPacket(new SPacketChangeGameState(2, 0.0F));
         } else {
            this.connection.sendPacket(new SPacketChangeGameState(1, 0.0F));
         }

      }
   }

   public void updateWeather(float oldRain, float newRain, float oldThunder, float newThunder) {
      if (this.weather == null) {
         if (oldRain != newRain) {
            this.connection.sendPacket(new SPacketChangeGameState(7, newRain));
         }
      } else if (this.pluginRainPositionPrevious != this.pluginRainPosition) {
         this.connection.sendPacket(new SPacketChangeGameState(7, this.pluginRainPosition));
      }

      if (oldThunder != newThunder) {
         if (this.weather != WeatherType.DOWNFALL && this.weather != null) {
            this.connection.sendPacket(new SPacketChangeGameState(8, 0.0F));
         } else {
            this.connection.sendPacket(new SPacketChangeGameState(8, newThunder));
         }
      }

   }

   public void tickWeather() {
      if (this.weather != null) {
         this.pluginRainPositionPrevious = this.pluginRainPosition;
         if (this.weather == WeatherType.DOWNFALL) {
            this.pluginRainPosition = (float)((double)this.pluginRainPosition + 0.01D);
         } else {
            this.pluginRainPosition = (float)((double)this.pluginRainPosition - 0.01D);
         }

         this.pluginRainPosition = MathHelper.clamp(this.pluginRainPosition, 0.0F, 1.0F);
      }
   }

   public void resetPlayerWeather() {
      this.weather = null;
      this.setPlayerWeather(this.world.getWorldInfo().isRaining() ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
   }

   public String toString() {
      return super.toString() + "(" + this.getName() + " at " + this.posX + "," + this.posY + "," + this.posZ + ")";
   }

   public void forceSetPositionRotation(double x, double y, double z, float yaw, float pitch) {
      this.setLocationAndAngles(x, y, z, yaw, pitch);
      this.connection.captureCurrentPosition();
   }

   public void reset() {
      float exp = 0.0F;
      boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");
      if (this.keepLevel || keepInventory) {
         exp = this.experience;
         this.newTotalExp = this.experienceTotal;
         this.newLevel = this.experienceLevel;
      }

      this.setHealth(this.getMaxHealth());
      this.arrowHitTimer = 0;
      this.fallDistance = 0.0F;
      this.foodStats = new FoodStats(this);
      this.experienceLevel = this.newLevel;
      this.experienceTotal = this.newTotalExp;
      this.experience = 0.0F;
      this.deathTime = 0;
      this.clearActivePotions();
      this.foodStats = true;
      this.openContainer = this.inventoryContainer;
      this.attackingPlayer = null;
      this.flyToggleTimer = null;
      this.MAIN_HAND = new CombatTracker(this);
      this.lastExperience = -1;
      if (!this.keepLevel && !keepInventory) {
         this.addExperience(this.newExp);
      } else {
         this.experience = exp;
      }

      this.keepLevel = false;
   }

   public CraftPlayer getBukkitEntity() {
      return (CraftPlayer)super.getBukkitEntity();
   }
}
