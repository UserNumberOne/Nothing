package net.minecraft.world.end;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeEndDecorator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.WorldGenEndGateway;
import net.minecraft.world.gen.feature.WorldGenEndPodium;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DragonFightManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Predicate VALID_PLAYER = Predicates.and(EntitySelectors.IS_ALIVE, EntitySelectors.withinRange(0.0D, 128.0D, 0.0D, 192.0D));
   private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(new TextComponentTranslation("entity.EnderDragon.name", new Object[0]), BossInfo.Color.PINK, BossInfo.Overlay.PROGRESS)).setPlayEndBossMusic(true).setCreateFog(true);
   private final WorldServer world;
   private final List gateways = Lists.newArrayList();
   private final BlockPattern portalPattern;
   private int ticksSinceDragonSeen;
   private int aliveCrystals;
   private int ticksSinceCrystalsScanned;
   private int ticksSinceLastPlayerScan;
   private boolean dragonKilled;
   private boolean previouslyKilled;
   private UUID dragonUniqueId;
   private boolean scanForLegacyFight = true;
   private BlockPos exitPortalLocation;
   private DragonSpawnManager respawnState;
   private int respawnStateTicks;
   private List crystals;

   public DragonFightManager(WorldServer var1, NBTTagCompound var2) {
      this.world = var1;
      if (var2.hasKey("DragonKilled", 99)) {
         if (var2.hasUniqueId("DragonUUID")) {
            this.dragonUniqueId = var2.getUniqueId("DragonUUID");
         }

         this.dragonKilled = var2.getBoolean("DragonKilled");
         this.previouslyKilled = var2.getBoolean("PreviouslyKilled");
         if (var2.getBoolean("IsRespawning")) {
            this.respawnState = DragonSpawnManager.START;
         }

         if (var2.hasKey("ExitPortalLocation", 10)) {
            this.exitPortalLocation = NBTUtil.getPosFromTag(var2.getCompoundTag("ExitPortalLocation"));
         }
      } else {
         this.dragonKilled = true;
         this.previouslyKilled = true;
      }

      if (var2.hasKey("Gateways", 9)) {
         NBTTagList var3 = var2.getTagList("Gateways", 3);

         for(int var4 = 0; var4 < var3.tagCount(); ++var4) {
            this.gateways.add(Integer.valueOf(var3.getIntAt(var4)));
         }
      } else {
         this.gateways.addAll(ContiguousSet.create(Range.closedOpen(Integer.valueOf(0), Integer.valueOf(20)), DiscreteDomain.integers()));
         Collections.shuffle(this.gateways, new Random(var1.getSeed()));
      }

      this.portalPattern = FactoryBlockPattern.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', BlockWorldState.hasState(BlockMatcher.forBlock(Blocks.BEDROCK))).build();
   }

   public NBTTagCompound getCompound() {
      NBTTagCompound var1 = new NBTTagCompound();
      if (this.dragonUniqueId != null) {
         var1.setUniqueId("DragonUUID", this.dragonUniqueId);
      }

      var1.setBoolean("DragonKilled", this.dragonKilled);
      var1.setBoolean("PreviouslyKilled", this.previouslyKilled);
      if (this.exitPortalLocation != null) {
         var1.setTag("ExitPortalLocation", NBTUtil.createPosTag(this.exitPortalLocation));
      }

      NBTTagList var2 = new NBTTagList();
      Iterator var3 = this.gateways.iterator();

      while(var3.hasNext()) {
         int var4 = ((Integer)var3.next()).intValue();
         var2.appendTag(new NBTTagInt(var4));
      }

      var1.setTag("Gateways", var2);
      return var1;
   }

   public void tick() {
      this.bossInfo.setVisible(!this.dragonKilled);
      if (++this.ticksSinceLastPlayerScan >= 20) {
         this.updateplayers();
         this.ticksSinceLastPlayerScan = 0;
      }

      if (!this.bossInfo.getPlayers().isEmpty()) {
         if (this.scanForLegacyFight) {
            LOGGER.info("Scanning for legacy world dragon fight...");
            this.loadChunks();
            this.scanForLegacyFight = false;
            boolean var1 = this.hasDragonBeenKilled();
            if (var1) {
               LOGGER.info("Found that the dragon has been killed in this world already.");
               this.previouslyKilled = true;
            } else {
               LOGGER.info("Found that the dragon has not yet been killed in this world.");
               this.previouslyKilled = false;
               this.generatePortal(false);
            }

            List var2 = this.world.getEntities(EntityDragon.class, EntitySelectors.IS_ALIVE);
            if (var2.isEmpty()) {
               this.dragonKilled = true;
            } else {
               EntityDragon var3 = (EntityDragon)var2.get(0);
               this.dragonUniqueId = var3.getUniqueID();
               LOGGER.info("Found that there's a dragon still alive ({})", new Object[]{var3});
               this.dragonKilled = false;
               if (!var1) {
                  LOGGER.info("But we didn't have a portal, let's remove it.");
                  var3.setDead();
                  this.dragonUniqueId = null;
               }
            }

            if (!this.previouslyKilled && this.dragonKilled) {
               this.dragonKilled = false;
            }
         }

         if (this.respawnState != null) {
            if (this.crystals == null) {
               this.respawnState = null;
               this.respawnDragon();
            }

            this.respawnState.process(this.world, this, this.crystals, this.respawnStateTicks++, this.exitPortalLocation);
         }

         if (!this.dragonKilled) {
            if (this.dragonUniqueId == null || ++this.ticksSinceDragonSeen >= 1200) {
               this.loadChunks();
               List var4 = this.world.getEntities(EntityDragon.class, EntitySelectors.IS_ALIVE);
               if (var4.isEmpty()) {
                  LOGGER.debug("Haven't seen the dragon, respawning it");
                  this.spawnDragon();
               } else {
                  LOGGER.debug("Haven't seen our dragon, but found another one to use.");
                  this.dragonUniqueId = ((EntityDragon)var4.get(0)).getUniqueID();
               }

               this.ticksSinceDragonSeen = 0;
            }

            if (++this.ticksSinceCrystalsScanned >= 100) {
               this.findAliveCrystals();
               this.ticksSinceCrystalsScanned = 0;
            }
         }
      }

   }

   protected void setRespawnState(DragonSpawnManager var1) {
      if (this.respawnState == null) {
         throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
      } else {
         this.respawnStateTicks = 0;
         if (var1 == DragonSpawnManager.END) {
            this.respawnState = null;
            this.dragonKilled = false;
            this.spawnDragon();
         } else {
            this.respawnState = var1;
         }

      }
   }

   private boolean hasDragonBeenKilled() {
      for(int var1 = -8; var1 <= 8; ++var1) {
         for(int var2 = -8; var2 <= 8; ++var2) {
            Chunk var3 = this.world.getChunkFromChunkCoords(var1, var2);

            for(TileEntity var5 : var3.getTileEntityMap().values()) {
               if (var5 instanceof TileEntityEndPortal) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Nullable
   private BlockPattern.PatternHelper findExitPortal() {
      for(int var1 = -8; var1 <= 8; ++var1) {
         for(int var2 = -8; var2 <= 8; ++var2) {
            Chunk var3 = this.world.getChunkFromChunkCoords(var1, var2);

            for(TileEntity var5 : var3.getTileEntityMap().values()) {
               if (var5 instanceof TileEntityEndPortal) {
                  BlockPattern.PatternHelper var6 = this.portalPattern.match(this.world, var5.getPos());
                  if (var6 != null) {
                     BlockPos var7 = var6.translateOffset(3, 3, 3).getPos();
                     if (this.exitPortalLocation == null && var7.getX() == 0 && var7.getZ() == 0) {
                        this.exitPortalLocation = var7;
                     }

                     return var6;
                  }
               }
            }
         }
      }

      int var8 = this.world.getHeight(WorldGenEndPodium.END_PODIUM_LOCATION).getY();

      for(int var9 = var8; var9 >= 0; --var9) {
         BlockPattern.PatternHelper var10 = this.portalPattern.match(this.world, new BlockPos(WorldGenEndPodium.END_PODIUM_LOCATION.getX(), var9, WorldGenEndPodium.END_PODIUM_LOCATION.getZ()));
         if (var10 != null) {
            if (this.exitPortalLocation == null) {
               this.exitPortalLocation = var10.translateOffset(3, 3, 3).getPos();
            }

            return var10;
         }
      }

      return null;
   }

   private void loadChunks() {
      for(int var1 = -8; var1 <= 8; ++var1) {
         for(int var2 = -8; var2 <= 8; ++var2) {
            this.world.getChunkFromChunkCoords(var1, var2);
         }
      }

   }

   private void updateplayers() {
      HashSet var1 = Sets.newHashSet();

      for(EntityPlayerMP var3 : this.world.getPlayers(EntityPlayerMP.class, VALID_PLAYER)) {
         this.bossInfo.addPlayer(var3);
         var1.add(var3);
      }

      HashSet var5 = Sets.newHashSet(this.bossInfo.getPlayers());
      var5.removeAll(var1);

      for(EntityPlayerMP var4 : var5) {
         this.bossInfo.removePlayer(var4);
      }

   }

   private void findAliveCrystals() {
      this.ticksSinceCrystalsScanned = 0;
      this.aliveCrystals = 0;

      for(WorldGenSpikes.EndSpike var4 : BiomeEndDecorator.getSpikesForWorld(this.world)) {
         this.aliveCrystals += this.world.getEntitiesWithinAABB(EntityEnderCrystal.class, var4.getTopBoundingBox()).size();
      }

      LOGGER.debug("Found {} end crystals still alive", new Object[]{this.aliveCrystals});
   }

   public void processDragonDeath(EntityDragon var1) {
      if (var1.getUniqueID().equals(this.dragonUniqueId)) {
         this.bossInfo.setPercent(0.0F);
         this.bossInfo.setVisible(false);
         this.generatePortal(true);
         this.spawnNewGateway();
         if (!this.previouslyKilled) {
            this.world.setBlockState(this.world.getHeight(WorldGenEndPodium.END_PODIUM_LOCATION), Blocks.DRAGON_EGG.getDefaultState());
         }

         this.previouslyKilled = true;
         this.dragonKilled = true;
      }

   }

   private void spawnNewGateway() {
      if (!this.gateways.isEmpty()) {
         int var1 = ((Integer)this.gateways.remove(this.gateways.size() - 1)).intValue();
         int var2 = (int)(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)var1)));
         int var3 = (int)(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)var1)));
         this.generateGateway(new BlockPos(var2, 75, var3));
      }
   }

   private void generateGateway(BlockPos var1) {
      this.world.playEvent(3000, var1, 0);
      (new WorldGenEndGateway()).generate(this.world, new Random(), var1);
   }

   private void generatePortal(boolean var1) {
      WorldGenEndPodium var2 = new WorldGenEndPodium(var1);
      if (this.exitPortalLocation == null) {
         for(this.exitPortalLocation = this.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION).down(); this.world.getBlockState(this.exitPortalLocation).getBlock() == Blocks.BEDROCK && this.exitPortalLocation.getY() > this.world.getSeaLevel(); this.exitPortalLocation = this.exitPortalLocation.down()) {
            ;
         }
      }

      var2.generate(this.world, new Random(), this.exitPortalLocation);
   }

   private void spawnDragon() {
      this.world.getChunkFromBlockCoords(new BlockPos(0, 128, 0));
      EntityDragon var1 = new EntityDragon(this.world);
      var1.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
      var1.setLocationAndAngles(0.0D, 128.0D, 0.0D, this.world.rand.nextFloat() * 360.0F, 0.0F);
      this.world.spawnEntity(var1);
      this.dragonUniqueId = var1.getUniqueID();
   }

   public void dragonUpdate(EntityDragon var1) {
      if (var1.getUniqueID().equals(this.dragonUniqueId)) {
         this.bossInfo.setPercent(var1.getHealth() / var1.getMaxHealth());
         this.ticksSinceDragonSeen = 0;
      }

   }

   public int getNumAliveCrystals() {
      return this.aliveCrystals;
   }

   public void onCrystalDestroyed(EntityEnderCrystal var1, DamageSource var2) {
      if (this.respawnState != null && this.crystals.contains(var1)) {
         LOGGER.debug("Aborting respawn sequence");
         this.respawnState = null;
         this.respawnStateTicks = 0;
         this.resetSpikeCrystals();
         this.generatePortal(true);
      } else {
         this.findAliveCrystals();
         Entity var3 = this.world.getEntityFromUuid(this.dragonUniqueId);
         if (var3 instanceof EntityDragon) {
            ((EntityDragon)var3).onCrystalDestroyed(var1, new BlockPos(var1), var2);
         }
      }

   }

   public boolean hasPreviouslyKilledDragon() {
      return this.previouslyKilled;
   }

   public void respawnDragon() {
      if (this.dragonKilled && this.respawnState == null) {
         BlockPos var1 = this.exitPortalLocation;
         if (var1 == null) {
            LOGGER.debug("Tried to respawn, but need to find the portal first.");
            BlockPattern.PatternHelper var2 = this.findExitPortal();
            if (var2 == null) {
               LOGGER.debug("Couldn't find a portal, so we made one.");
               this.generatePortal(true);
            } else {
               LOGGER.debug("Found the exit portal & temporarily using it.");
            }

            var1 = this.exitPortalLocation;
         }

         ArrayList var7 = Lists.newArrayList();
         BlockPos var3 = var1.up(1);

         for(EnumFacing var5 : EnumFacing.Plane.HORIZONTAL) {
            List var6 = this.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(var3.offset(var5, 2)));
            if (var6.isEmpty()) {
               return;
            }

            var7.addAll(var6);
         }

         LOGGER.debug("Found all crystals, respawning dragon.");
         this.respawnDragon(var7);
      }

   }

   private void respawnDragon(List var1) {
      if (this.dragonKilled && this.respawnState == null) {
         for(BlockPattern.PatternHelper var2 = this.findExitPortal(); var2 != null; var2 = this.findExitPortal()) {
            for(int var3 = 0; var3 < this.portalPattern.getPalmLength(); ++var3) {
               for(int var4 = 0; var4 < this.portalPattern.getThumbLength(); ++var4) {
                  for(int var5 = 0; var5 < this.portalPattern.getFingerLength(); ++var5) {
                     BlockWorldState var6 = var2.translateOffset(var3, var4, var5);
                     if (var6.getBlockState().getBlock() == Blocks.BEDROCK || var6.getBlockState().getBlock() == Blocks.END_PORTAL) {
                        this.world.setBlockState(var6.getPos(), Blocks.END_STONE.getDefaultState());
                     }
                  }
               }
            }
         }

         this.respawnState = DragonSpawnManager.START;
         this.respawnStateTicks = 0;
         this.generatePortal(false);
         this.crystals = var1;
      }

   }

   public void resetSpikeCrystals() {
      for(WorldGenSpikes.EndSpike var4 : BiomeEndDecorator.getSpikesForWorld(this.world)) {
         for(EntityEnderCrystal var7 : this.world.getEntitiesWithinAABB(EntityEnderCrystal.class, var4.getTopBoundingBox())) {
            var7.setEntityInvulnerable(false);
            var7.setBeamTarget((BlockPos)null);
         }
      }

   }
}
