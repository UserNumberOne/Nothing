package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ChunkRenderWorker implements Runnable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ChunkRenderDispatcher chunkRenderDispatcher;
   private final RegionRenderCacheBuilder regionRenderCacheBuilder;
   private boolean shouldRun;

   public ChunkRenderWorker(ChunkRenderDispatcher var1) {
      this(var1, (RegionRenderCacheBuilder)null);
   }

   public ChunkRenderWorker(ChunkRenderDispatcher var1, @Nullable RegionRenderCacheBuilder var2) {
      this.shouldRun = true;
      this.chunkRenderDispatcher = var1;
      this.regionRenderCacheBuilder = var2;
   }

   public void run() {
      while(this.shouldRun) {
         try {
            this.processTask(this.chunkRenderDispatcher.getNextChunkUpdate());
         } catch (InterruptedException var3) {
            LOGGER.debug("Stopping chunk worker due to interrupt");
            return;
         } catch (Throwable var4) {
            CrashReport var2 = CrashReport.makeCrashReport(var4, "Batching chunks");
            Minecraft.getMinecraft().crashed(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(var2));
            return;
         }
      }

   }

   protected void processTask(final ChunkCompileTaskGenerator var1) throws InterruptedException {
      var1.getLock().lock();

      try {
         if (var1.getStatus() != ChunkCompileTaskGenerator.Status.PENDING) {
            if (!var1.isFinished()) {
               LOGGER.warn("Chunk render task was {} when I expected it to be pending; ignoring task", new Object[]{var1.getStatus()});
            }

            return;
         }

         BlockPos var2 = new BlockPos(Minecraft.getMinecraft().player);
         BlockPos var3 = var1.getRenderChunk().getPosition();
         boolean var4 = true;
         boolean var5 = true;
         boolean var6 = true;
         if (var3.add(8, 8, 8).distanceSq(var2) > 576.0D) {
            World var7 = var1.getRenderChunk().getWorld();
            BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos(var3);
            if (!this.isChunkExisting(var8.setPos(var3).move(EnumFacing.WEST, 16), var7) || !this.isChunkExisting(var8.setPos(var3).move(EnumFacing.NORTH, 16), var7) || !this.isChunkExisting(var8.setPos(var3).move(EnumFacing.EAST, 16), var7) || !this.isChunkExisting(var8.setPos(var3).move(EnumFacing.SOUTH, 16), var7)) {
               return;
            }
         }

         var1.setStatus(ChunkCompileTaskGenerator.Status.COMPILING);
      } finally {
         var1.getLock().unlock();
      }

      Entity var19 = Minecraft.getMinecraft().getRenderViewEntity();
      if (var19 == null) {
         var1.finish();
      } else {
         var1.setRegionRenderCacheBuilder(this.getRegionRenderCacheBuilder());
         float var20 = (float)var19.posX;
         float var21 = (float)var19.posY + var19.getEyeHeight();
         float var22 = (float)var19.posZ;
         ChunkCompileTaskGenerator.Type var23 = var1.getType();
         if (var23 == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK) {
            var1.getRenderChunk().rebuildChunk(var20, var21, var22, var1);
         } else if (var23 == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY) {
            var1.getRenderChunk().resortTransparency(var20, var21, var22, var1);
         }

         var1.getLock().lock();

         try {
            if (var1.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING) {
               if (!var1.isFinished()) {
                  LOGGER.warn("Chunk render task was {} when I expected it to be compiling; aborting task", new Object[]{var1.getStatus()});
               }

               this.freeRenderBuilder(var1);
               return;
            }

            var1.setStatus(ChunkCompileTaskGenerator.Status.UPLOADING);
         } finally {
            var1.getLock().unlock();
         }

         final CompiledChunk var24 = var1.getCompiledChunk();
         ArrayList var25 = Lists.newArrayList();
         if (var23 == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK) {
            for(BlockRenderLayer var12 : BlockRenderLayer.values()) {
               if (var24.isLayerStarted(var12)) {
                  var25.add(this.chunkRenderDispatcher.uploadChunk(var12, var1.getRegionRenderCacheBuilder().getWorldRendererByLayer(var12), var1.getRenderChunk(), var24, var1.getDistanceSq()));
               }
            }
         } else if (var23 == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY) {
            var25.add(this.chunkRenderDispatcher.uploadChunk(BlockRenderLayer.TRANSLUCENT, var1.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), var1.getRenderChunk(), var24, var1.getDistanceSq()));
         }

         final ListenableFuture var26 = Futures.allAsList(var25);
         var1.addFinishRunnable(new Runnable() {
            public void run() {
               var26.cancel(false);
            }
         });
         Futures.addCallback(var26, new FutureCallback() {
            public void onSuccess(@Nullable List var1x) {
               ChunkRenderWorker.this.freeRenderBuilder(var1);
               var1.getLock().lock();

               try {
                  if (var1.getStatus() != ChunkCompileTaskGenerator.Status.UPLOADING) {
                     if (!var1.isFinished()) {
                        ChunkRenderWorker.LOGGER.warn("Chunk render task was {} when I expected it to be uploading; aborting task", new Object[]{var1.getStatus()});
                     }

                     return;
                  }

                  var1.setStatus(ChunkCompileTaskGenerator.Status.DONE);
               } finally {
                  var1.getLock().unlock();
               }

               var1.getRenderChunk().setCompiledChunk(var24);
            }

            public void onFailure(Throwable var1x) {
               ChunkRenderWorker.this.freeRenderBuilder(var1);
               if (!(var1x instanceof CancellationException) && !(var1x instanceof InterruptedException)) {
                  Minecraft.getMinecraft().crashed(CrashReport.makeCrashReport(var1x, "Rendering chunk"));
               }

            }
         });
      }
   }

   private boolean isChunkExisting(BlockPos var1, World var2) {
      return !var2.getChunkFromChunkCoords(var1.getX() >> 4, var1.getZ() >> 4).isEmpty();
   }

   private RegionRenderCacheBuilder getRegionRenderCacheBuilder() throws InterruptedException {
      return this.regionRenderCacheBuilder != null ? this.regionRenderCacheBuilder : this.chunkRenderDispatcher.allocateRenderBuilder();
   }

   private void freeRenderBuilder(ChunkCompileTaskGenerator var1) {
      if (this.regionRenderCacheBuilder == null) {
         this.chunkRenderDispatcher.freeRenderBuilder(var1.getRegionRenderCacheBuilder());
      }

   }

   public void notifyToStop() {
      this.shouldRun = false;
   }
}
