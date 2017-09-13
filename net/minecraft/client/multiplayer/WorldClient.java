package net.minecraft.client.multiplayer;

import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldClient extends World {
   private final NetHandlerPlayClient connection;
   private ChunkProviderClient clientChunkProvider;
   private final Set entityList = Sets.newHashSet();
   private final Set entitySpawnQueue = Sets.newHashSet();
   private final Minecraft mc = Minecraft.getMinecraft();
   private final Set previousActiveChunkSet = Sets.newHashSet();
   private int ambienceTicks;
   protected Set viewableChunks;

   public WorldClient(NetHandlerPlayClient var1, WorldSettings var2, int var3, EnumDifficulty var4, Profiler var5) {
      super(new SaveHandlerMP(), new WorldInfo(var2, "MpServer"), DimensionManager.createProviderFor(var3), var5, true);
      this.ambienceTicks = this.rand.nextInt(12000);
      this.viewableChunks = Sets.newHashSet();
      this.connection = var1;
      this.getWorldInfo().setDifficulty(var4);
      this.provider.registerWorld(this);
      this.setSpawnPoint(new BlockPos(8, 64, 8));
      this.chunkProvider = this.createChunkProvider();
      this.mapStorage = new SaveDataMemoryStorage();
      this.calculateInitialSkylight();
      this.calculateInitialWeather();
      this.initCapabilities();
      MinecraftForge.EVENT_BUS.post(new Load(this));
   }

   public void tick() {
      super.tick();
      this.setTotalWorldTime(this.getTotalWorldTime() + 1L);
      if (this.getGameRules().getBoolean("doDaylightCycle")) {
         this.setWorldTime(this.getWorldTime() + 1L);
      }

      this.theProfiler.startSection("reEntryProcessing");

      for(int var1 = 0; var1 < 10 && !this.entitySpawnQueue.isEmpty(); ++var1) {
         Entity var2 = (Entity)this.entitySpawnQueue.iterator().next();
         this.entitySpawnQueue.remove(var2);
         if (!this.loadedEntityList.contains(var2)) {
            this.spawnEntity(var2);
         }
      }

      this.theProfiler.endStartSection("chunkCache");
      this.clientChunkProvider.tick();
      this.theProfiler.endStartSection("blocks");
      this.updateBlocks();
      this.theProfiler.endSection();
   }

   public void invalidateBlockReceiveRegion(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   protected IChunkProvider createChunkProvider() {
      this.clientChunkProvider = new ChunkProviderClient(this);
      return this.clientChunkProvider;
   }

   protected boolean isChunkLoaded(int var1, int var2, boolean var3) {
      return var3 || !this.getChunkProvider().provideChunk(var1, var2).isEmpty();
   }

   protected void buildChunkCoordList() {
      this.viewableChunks.clear();
      int var1 = this.mc.gameSettings.renderDistanceChunks;
      this.theProfiler.startSection("buildList");
      int var2 = MathHelper.floor(this.mc.player.posX / 16.0D);
      int var3 = MathHelper.floor(this.mc.player.posZ / 16.0D);

      for(int var4 = -var1; var4 <= var1; ++var4) {
         for(int var5 = -var1; var5 <= var1; ++var5) {
            this.viewableChunks.add(new ChunkPos(var4 + var2, var5 + var3));
         }
      }

      this.theProfiler.endSection();
   }

   protected void updateBlocks() {
      this.buildChunkCoordList();
      if (this.ambienceTicks > 0) {
         --this.ambienceTicks;
      }

      this.previousActiveChunkSet.retainAll(this.viewableChunks);
      if (this.previousActiveChunkSet.size() == this.viewableChunks.size()) {
         this.previousActiveChunkSet.clear();
      }

      int var1 = 0;

      for(ChunkPos var3 : this.viewableChunks) {
         if (!this.previousActiveChunkSet.contains(var3)) {
            int var4 = var3.chunkXPos * 16;
            int var5 = var3.chunkZPos * 16;
            this.theProfiler.startSection("getChunk");
            Chunk var6 = this.getChunkFromChunkCoords(var3.chunkXPos, var3.chunkZPos);
            this.playMoodSoundAndCheckLight(var4, var5, var6);
            this.theProfiler.endSection();
            this.previousActiveChunkSet.add(var3);
            ++var1;
            if (var1 >= 10) {
               return;
            }
         }
      }

   }

   public void doPreChunk(int var1, int var2, boolean var3) {
      if (var3) {
         this.clientChunkProvider.loadChunk(var1, var2);
      } else {
         this.clientChunkProvider.unloadChunk(var1, var2);
         this.markBlockRangeForRenderUpdate(var1 * 16, 0, var2 * 16, var1 * 16 + 15, 256, var2 * 16 + 15);
      }

   }

   public boolean spawnEntity(Entity var1) {
      boolean var2 = super.spawnEntity(var1);
      this.entityList.add(var1);
      if (var2) {
         if (var1 instanceof EntityMinecart) {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart)var1));
         }
      } else {
         this.entitySpawnQueue.add(var1);
      }

      return var2;
   }

   public void removeEntity(Entity var1) {
      super.removeEntity(var1);
      this.entityList.remove(var1);
   }

   public void onEntityAdded(Entity var1) {
      super.onEntityAdded(var1);
      if (this.entitySpawnQueue.contains(var1)) {
         this.entitySpawnQueue.remove(var1);
      }

   }

   public void onEntityRemoved(Entity var1) {
      super.onEntityRemoved(var1);
      if (this.entityList.contains(var1)) {
         if (var1.isEntityAlive()) {
            this.entitySpawnQueue.add(var1);
         } else {
            this.entityList.remove(var1);
         }
      }

   }

   public void addEntityToWorld(int var1, Entity var2) {
      Entity var3 = this.getEntityByID(var1);
      if (var3 != null) {
         this.removeEntity(var3);
      }

      this.entityList.add(var2);
      var2.setEntityId(var1);
      if (!this.spawnEntity(var2)) {
         this.entitySpawnQueue.add(var2);
      }

      this.entitiesById.addKey(var1, var2);
   }

   @Nullable
   public Entity getEntityByID(int var1) {
      return (Entity)(var1 == this.mc.player.getEntityId() ? this.mc.player : super.getEntityByID(var1));
   }

   public Entity removeEntityFromWorld(int var1) {
      Entity var2 = (Entity)this.entitiesById.removeObject(var1);
      if (var2 != null) {
         this.entityList.remove(var2);
         this.removeEntity(var2);
      }

      return var2;
   }

   /** @deprecated */
   @Deprecated
   public boolean invalidateRegionAndSetBlock(BlockPos var1, IBlockState var2) {
      int var3 = var1.getX();
      int var4 = var1.getY();
      int var5 = var1.getZ();
      this.invalidateBlockReceiveRegion(var3, var4, var5, var3, var4, var5);
      return super.setBlockState(var1, var2, 3);
   }

   public void sendQuittingDisconnectingPacket() {
      this.connection.getNetworkManager().closeChannel(new TextComponentString("Quitting"));
   }

   protected void updateWeather() {
   }

   protected void playMoodSoundAndCheckLight(int var1, int var2, Chunk var3) {
      super.playMoodSoundAndCheckLight(var1, var2, var3);
      if (this.ambienceTicks == 0) {
         this.updateLCG = this.updateLCG * 3 + 1013904223;
         int var4 = this.updateLCG >> 2;
         int var5 = var4 & 15;
         int var6 = var4 >> 8 & 15;
         int var7 = var4 >> 16 & 255;
         BlockPos var8 = new BlockPos(var5 + var1, var7, var6 + var2);
         IBlockState var9 = var3.getBlockState(var8);
         var5 = var5 + var1;
         var6 = var6 + var2;
         if (var9.getMaterial() == Material.AIR && this.getLight(var8) <= this.rand.nextInt(8) && this.getLightFor(EnumSkyBlock.SKY, var8) <= 0 && this.mc.player != null && this.mc.player.getDistanceSq((double)var5 + 0.5D, (double)var7 + 0.5D, (double)var6 + 0.5D) > 4.0D) {
            this.playSound((double)var5 + 0.5D, (double)var7 + 0.5D, (double)var6 + 0.5D, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + this.rand.nextFloat() * 0.2F, false);
            this.ambienceTicks = this.rand.nextInt(12000) + 6000;
         }
      }

   }

   public void doVoidFogParticles(int var1, int var2, int var3) {
      boolean var4 = true;
      Random var5 = new Random();
      ItemStack var6 = this.mc.player.getHeldItemMainhand();
      boolean var7 = this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && var6 != null && Block.getBlockFromItem(var6.getItem()) == Blocks.BARRIER;
      BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

      for(int var9 = 0; var9 < 667; ++var9) {
         this.showBarrierParticles(var1, var2, var3, 16, var5, var7, var8);
         this.showBarrierParticles(var1, var2, var3, 32, var5, var7, var8);
      }

   }

   public void showBarrierParticles(int var1, int var2, int var3, int var4, Random var5, boolean var6, BlockPos.MutableBlockPos var7) {
      int var8 = var1 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
      int var9 = var2 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
      int var10 = var3 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
      var7.setPos(var8, var9, var10);
      IBlockState var11 = this.getBlockState(var7);
      var11.getBlock().randomDisplayTick(var11, this, var7, var5);
      if (var6 && var11.getBlock() == Blocks.BARRIER) {
         this.spawnParticle(EnumParticleTypes.BARRIER, (double)((float)var8 + 0.5F), (double)((float)var9 + 0.5F), (double)((float)var10 + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
      }

   }

   public void removeAllEntities() {
      this.loadedEntityList.removeAll(this.unloadedEntityList);

      for(int var1 = 0; var1 < this.unloadedEntityList.size(); ++var1) {
         Entity var2 = (Entity)this.unloadedEntityList.get(var1);
         int var3 = var2.chunkCoordX;
         int var4 = var2.chunkCoordZ;
         if (var2.addedToChunk && this.isChunkLoaded(var3, var4, true)) {
            this.getChunkFromChunkCoords(var3, var4).removeEntity(var2);
         }
      }

      for(int var6 = 0; var6 < this.unloadedEntityList.size(); ++var6) {
         this.onEntityRemoved((Entity)this.unloadedEntityList.get(var6));
      }

      this.unloadedEntityList.clear();

      for(int var7 = 0; var7 < this.loadedEntityList.size(); ++var7) {
         Entity var8 = (Entity)this.loadedEntityList.get(var7);
         Entity var9 = var8.getRidingEntity();
         if (var9 != null) {
            if (!var9.isDead && var9.isPassenger(var8)) {
               continue;
            }

            var8.dismountRidingEntity();
         }

         if (var8.isDead) {
            int var10 = var8.chunkCoordX;
            int var5 = var8.chunkCoordZ;
            if (var8.addedToChunk && this.isChunkLoaded(var10, var5, true)) {
               this.getChunkFromChunkCoords(var10, var5).removeEntity(var8);
            }

            this.loadedEntityList.remove(var7--);
            this.onEntityRemoved(var8);
         }
      }

   }

   public CrashReportCategory addWorldInfoToCrashReport(CrashReport var1) {
      CrashReportCategory var2 = super.addWorldInfoToCrashReport(var1);
      var2.setDetail("Forced entities", new ICrashReportDetail() {
         public String call() {
            return WorldClient.this.entityList.size() + " total; " + WorldClient.this.entityList;
         }
      });
      var2.setDetail("Retry entities", new ICrashReportDetail() {
         public String call() {
            return WorldClient.this.entitySpawnQueue.size() + " total; " + WorldClient.this.entitySpawnQueue;
         }
      });
      var2.setDetail("Server brand", new ICrashReportDetail() {
         public String call() throws Exception {
            return WorldClient.this.mc.player.getServerBrand();
         }
      });
      var2.setDetail("Server type", new ICrashReportDetail() {
         public String call() throws Exception {
            return WorldClient.this.mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
         }
      });
      return var2;
   }

   public void playSound(@Nullable EntityPlayer var1, double var2, double var4, double var6, SoundEvent var8, SoundCategory var9, float var10, float var11) {
      if (var1 == this.mc.player) {
         this.playSound(var2, var4, var6, var8, var9, var10, var11, false);
      }

   }

   public void playSound(BlockPos var1, SoundEvent var2, SoundCategory var3, float var4, float var5, boolean var6) {
      this.playSound((double)var1.getX() + 0.5D, (double)var1.getY() + 0.5D, (double)var1.getZ() + 0.5D, var2, var3, var4, var5, var6);
   }

   public void playSound(double var1, double var3, double var5, SoundEvent var7, SoundCategory var8, float var9, float var10, boolean var11) {
      double var12 = this.mc.getRenderViewEntity().getDistanceSq(var1, var3, var5);
      PositionedSoundRecord var14 = new PositionedSoundRecord(var7, var8, var9, var10, (float)var1, (float)var3, (float)var5);
      if (var11 && var12 > 100.0D) {
         double var15 = Math.sqrt(var12) / 40.0D;
         this.mc.getSoundHandler().playDelayedSound(var14, (int)(var15 * 20.0D));
      } else {
         this.mc.getSoundHandler().playSound(var14);
      }

   }

   public void makeFireworks(double var1, double var3, double var5, double var7, double var9, double var11, @Nullable NBTTagCompound var13) {
      this.mc.effectRenderer.addEffect(new ParticleFirework.Starter(this, var1, var3, var5, var7, var9, var11, this.mc.effectRenderer, var13));
   }

   public void sendPacketToServer(Packet var1) {
      this.connection.sendPacket(var1);
   }

   public void setWorldScoreboard(Scoreboard var1) {
      this.worldScoreboard = var1;
   }

   public void setWorldTime(long var1) {
      if (var1 < 0L) {
         var1 = -var1;
         this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
      } else {
         this.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
      }

      super.setWorldTime(var1);
   }

   public ChunkProviderClient getChunkProvider() {
      return (ChunkProviderClient)super.getChunkProvider();
   }
}
