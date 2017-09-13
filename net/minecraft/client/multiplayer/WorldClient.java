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
      super(new SaveHandlerMP(), new WorldInfo(settings, "MpServer"), DimensionManager.createProviderFor(dimension), profilerIn, true);
      this.ambienceTicks = this.rand.nextInt(12000);
      this.viewableChunks = Sets.newHashSet();
      this.connection = netHandler;
      this.getWorldInfo().setDifficulty(difficulty);
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

      for(int i = 0; i < 10 && !this.entitySpawnQueue.isEmpty(); ++i) {
         Entity entity = (Entity)this.entitySpawnQueue.iterator().next();
         this.entitySpawnQueue.remove(entity);
         if (!this.loadedEntityList.contains(entity)) {
            this.spawnEntity(entity);
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
      return allowEmpty || !this.getChunkProvider().provideChunk(x, z).isEmpty();
   }

   protected void buildChunkCoordList() {
      this.viewableChunks.clear();
      int i = this.mc.gameSettings.renderDistanceChunks;
      this.theProfiler.startSection("buildList");
      int j = MathHelper.floor(this.mc.player.posX / 16.0D);
      int k = MathHelper.floor(this.mc.player.posZ / 16.0D);

      for(int l = -i; l <= i; ++l) {
         for(int i1 = -i; i1 <= i; ++i1) {
            this.viewableChunks.add(new ChunkPos(l + j, i1 + k));
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

      int i = 0;

      for(ChunkPos chunkpos : this.viewableChunks) {
         if (!this.previousActiveChunkSet.contains(chunkpos)) {
            int j = chunkpos.chunkXPos * 16;
            int k = chunkpos.chunkZPos * 16;
            this.theProfiler.startSection("getChunk");
            Chunk chunk = this.getChunkFromChunkCoords(chunkpos.chunkXPos, chunkpos.chunkZPos);
            this.playMoodSoundAndCheckLight(j, k, chunk);
            this.theProfiler.endSection();
            this.previousActiveChunkSet.add(chunkpos);
            ++i;
            if (i >= 10) {
               return;
            }
         }
      }

   }

   public void doPreChunk(int var1, int var2, boolean var3) {
      if (loadChunk) {
         this.clientChunkProvider.loadChunk(chunkX, chunkZ);
      } else {
         this.clientChunkProvider.unloadChunk(chunkX, chunkZ);
         this.markBlockRangeForRenderUpdate(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, 256, chunkZ * 16 + 15);
      }

   }

   public boolean spawnEntity(Entity var1) {
      boolean flag = super.spawnEntity(entityIn);
      this.entityList.add(entityIn);
      if (flag) {
         if (entityIn instanceof EntityMinecart) {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart)entityIn));
         }
      } else {
         this.entitySpawnQueue.add(entityIn);
      }

      return flag;
   }

   public void removeEntity(Entity var1) {
      super.removeEntity(entityIn);
      this.entityList.remove(entityIn);
   }

   public void onEntityAdded(Entity var1) {
      super.onEntityAdded(entityIn);
      if (this.entitySpawnQueue.contains(entityIn)) {
         this.entitySpawnQueue.remove(entityIn);
      }

   }

   public void onEntityRemoved(Entity var1) {
      super.onEntityRemoved(entityIn);
      if (this.entityList.contains(entityIn)) {
         if (entityIn.isEntityAlive()) {
            this.entitySpawnQueue.add(entityIn);
         } else {
            this.entityList.remove(entityIn);
         }
      }

   }

   public void addEntityToWorld(int var1, Entity var2) {
      Entity entity = this.getEntityByID(entityID);
      if (entity != null) {
         this.removeEntity(entity);
      }

      this.entityList.add(entityToSpawn);
      entityToSpawn.setEntityId(entityID);
      if (!this.spawnEntity(entityToSpawn)) {
         this.entitySpawnQueue.add(entityToSpawn);
      }

      this.entitiesById.addKey(entityID, entityToSpawn);
   }

   @Nullable
   public Entity getEntityByID(int var1) {
      return (Entity)(id == this.mc.player.getEntityId() ? this.mc.player : super.getEntityByID(id));
   }

   public Entity removeEntityFromWorld(int var1) {
      Entity entity = (Entity)this.entitiesById.removeObject(entityID);
      if (entity != null) {
         this.entityList.remove(entity);
         this.removeEntity(entity);
      }

      return entity;
   }

   /** @deprecated */
   @Deprecated
   public boolean invalidateRegionAndSetBlock(BlockPos var1, IBlockState var2) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      this.invalidateBlockReceiveRegion(i, j, k, i, j, k);
      return super.setBlockState(pos, state, 3);
   }

   public void sendQuittingDisconnectingPacket() {
      this.connection.getNetworkManager().closeChannel(new TextComponentString("Quitting"));
   }

   protected void updateWeather() {
   }

   protected void playMoodSoundAndCheckLight(int var1, int var2, Chunk var3) {
      super.playMoodSoundAndCheckLight(p_147467_1_, p_147467_2_, chunkIn);
      if (this.ambienceTicks == 0) {
         this.updateLCG = this.updateLCG * 3 + 1013904223;
         int i = this.updateLCG >> 2;
         int j = i & 15;
         int k = i >> 8 & 15;
         int l = i >> 16 & 255;
         BlockPos blockpos = new BlockPos(j + p_147467_1_, l, k + p_147467_2_);
         IBlockState iblockstate = chunkIn.getBlockState(blockpos);
         j = j + p_147467_1_;
         k = k + p_147467_2_;
         if (iblockstate.getMaterial() == Material.AIR && this.getLight(blockpos) <= this.rand.nextInt(8) && this.getLightFor(EnumSkyBlock.SKY, blockpos) <= 0 && this.mc.player != null && this.mc.player.getDistanceSq((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D) > 4.0D) {
            this.playSound((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + this.rand.nextFloat() * 0.2F, false);
            this.ambienceTicks = this.rand.nextInt(12000) + 6000;
         }
      }

   }

   public void doVoidFogParticles(int var1, int var2, int var3) {
      int i = 32;
      Random random = new Random();
      ItemStack itemstack = this.mc.player.getHeldItemMainhand();
      boolean flag = this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && itemstack != null && Block.getBlockFromItem(itemstack.getItem()) == Blocks.BARRIER;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < 667; ++j) {
         this.showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
         this.showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
      }

   }

   public void showBarrierParticles(int var1, int var2, int var3, int var4, Random var5, boolean var6, BlockPos.MutableBlockPos var7) {
      int i = p_184153_1_ + this.rand.nextInt(p_184153_4_) - this.rand.nextInt(p_184153_4_);
      int j = p_184153_2_ + this.rand.nextInt(p_184153_4_) - this.rand.nextInt(p_184153_4_);
      int k = p_184153_3_ + this.rand.nextInt(p_184153_4_) - this.rand.nextInt(p_184153_4_);
      pos.setPos(i, j, k);
      IBlockState iblockstate = this.getBlockState(pos);
      iblockstate.getBlock().randomDisplayTick(iblockstate, this, pos, random);
      if (p_184153_6_ && iblockstate.getBlock() == Blocks.BARRIER) {
         this.spawnParticle(EnumParticleTypes.BARRIER, (double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
      }

   }

   public void removeAllEntities() {
      this.loadedEntityList.removeAll(this.unloadedEntityList);

      for(int i = 0; i < this.unloadedEntityList.size(); ++i) {
         Entity entity = (Entity)this.unloadedEntityList.get(i);
         int j = entity.chunkCoordX;
         int k = entity.chunkCoordZ;
         if (entity.addedToChunk && this.isChunkLoaded(j, k, true)) {
            this.getChunkFromChunkCoords(j, k).removeEntity(entity);
         }
      }

      for(int i1 = 0; i1 < this.unloadedEntityList.size(); ++i1) {
         this.onEntityRemoved((Entity)this.unloadedEntityList.get(i1));
      }

      this.unloadedEntityList.clear();

      for(int j1 = 0; j1 < this.loadedEntityList.size(); ++j1) {
         Entity entity1 = (Entity)this.loadedEntityList.get(j1);
         Entity entity2 = entity1.getRidingEntity();
         if (entity2 != null) {
            if (!entity2.isDead && entity2.isPassenger(entity1)) {
               continue;
            }

            entity1.dismountRidingEntity();
         }

         if (entity1.isDead) {
            int k1 = entity1.chunkCoordX;
            int l = entity1.chunkCoordZ;
            if (entity1.addedToChunk && this.isChunkLoaded(k1, l, true)) {
               this.getChunkFromChunkCoords(k1, l).removeEntity(entity1);
            }

            this.loadedEntityList.remove(j1--);
            this.onEntityRemoved(entity1);
         }
      }

   }

   public CrashReportCategory addWorldInfoToCrashReport(CrashReport var1) {
      CrashReportCategory crashreportcategory = super.addWorldInfoToCrashReport(report);
      crashreportcategory.setDetail("Forced entities", new ICrashReportDetail() {
         public String call() {
            return WorldClient.this.entityList.size() + " total; " + WorldClient.this.entityList;
         }
      });
      crashreportcategory.setDetail("Retry entities", new ICrashReportDetail() {
         public String call() {
            return WorldClient.this.entitySpawnQueue.size() + " total; " + WorldClient.this.entitySpawnQueue;
         }
      });
      crashreportcategory.setDetail("Server brand", new ICrashReportDetail() {
         public String call() throws Exception {
            return WorldClient.this.mc.player.getServerBrand();
         }
      });
      crashreportcategory.setDetail("Server type", new ICrashReportDetail() {
         public String call() throws Exception {
            return WorldClient.this.mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
         }
      });
      return crashreportcategory;
   }

   public void playSound(@Nullable EntityPlayer var1, double var2, double var4, double var6, SoundEvent var8, SoundCategory var9, float var10, float var11) {
      if (player == this.mc.player) {
         this.playSound(x, y, z, soundIn, category, volume, pitch, false);
      }

   }

   public void playSound(BlockPos var1, SoundEvent var2, SoundCategory var3, float var4, float var5, boolean var6) {
      this.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, soundIn, category, volume, pitch, distanceDelay);
   }

   public void playSound(double var1, double var3, double var5, SoundEvent var7, SoundCategory var8, float var9, float var10, boolean var11) {
      double d0 = this.mc.getRenderViewEntity().getDistanceSq(x, y, z);
      PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(soundIn, category, volume, pitch, (float)x, (float)y, (float)z);
      if (distanceDelay && d0 > 100.0D) {
         double d1 = Math.sqrt(d0) / 40.0D;
         this.mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int)(d1 * 20.0D));
      } else {
         this.mc.getSoundHandler().playSound(positionedsoundrecord);
      }

   }

   public void makeFireworks(double var1, double var3, double var5, double var7, double var9, double var11, @Nullable NBTTagCompound var13) {
      this.mc.effectRenderer.addEffect(new ParticleFirework.Starter(this, x, y, z, motionX, motionY, motionZ, this.mc.effectRenderer, compund));
   }

   public void sendPacketToServer(Packet var1) {
      this.connection.sendPacket(packetIn);
   }

   public void setWorldScoreboard(Scoreboard var1) {
      this.worldScoreboard = scoreboardIn;
   }

   public void setWorldTime(long var1) {
      if (time < 0L) {
         time = -time;
         this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
      } else {
         this.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
      }

      super.setWorldTime(time);
   }

   public ChunkProviderClient getChunkProvider() {
      return (ChunkProviderClient)super.getChunkProvider();
   }
}
