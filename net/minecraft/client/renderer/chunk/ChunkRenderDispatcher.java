package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ChunkRenderDispatcher {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ThreadFactory THREAD_FACTORY = (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
   private final int countRenderBuilders;
   private final List listWorkerThreads;
   private final List listThreadedWorkers;
   private final PriorityBlockingQueue queueChunkUpdates;
   private final BlockingQueue queueFreeRenderBuilders;
   private final WorldVertexBufferUploader worldVertexUploader;
   private final VertexBufferUploader vertexUploader;
   private final Queue queueChunkUploads;
   private final ChunkRenderWorker renderWorker;

   public ChunkRenderDispatcher() {
      this(-1);
   }

   public ChunkRenderDispatcher(int var1) {
      this.listWorkerThreads = Lists.newArrayList();
      this.listThreadedWorkers = Lists.newArrayList();
      this.queueChunkUpdates = Queues.newPriorityBlockingQueue();
      this.worldVertexUploader = new WorldVertexBufferUploader();
      this.vertexUploader = new VertexBufferUploader();
      this.queueChunkUploads = Queues.newPriorityQueue();
      int var2 = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / 10485760);
      int var3 = Math.max(1, MathHelper.clamp(Runtime.getRuntime().availableProcessors(), 1, var2 / 5));
      if (var1 < 0) {
         var1 = MathHelper.clamp(var3 * 10, 1, var2);
      }

      this.countRenderBuilders = var1;
      if (var3 > 1) {
         for(int var4 = 0; var4 < var3; ++var4) {
            ChunkRenderWorker var5 = new ChunkRenderWorker(this);
            Thread var6 = THREAD_FACTORY.newThread(var5);
            var6.start();
            this.listThreadedWorkers.add(var5);
            this.listWorkerThreads.add(var6);
         }
      }

      this.queueFreeRenderBuilders = Queues.newArrayBlockingQueue(this.countRenderBuilders);

      for(int var7 = 0; var7 < this.countRenderBuilders; ++var7) {
         this.queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
      }

      this.renderWorker = new ChunkRenderWorker(this, new RegionRenderCacheBuilder());
   }

   public String getDebugInfo() {
      return this.listWorkerThreads.isEmpty() ? String.format("pC: %03d, single-threaded", this.queueChunkUpdates.size()) : String.format("pC: %03d, pU: %1d, aB: %1d", this.queueChunkUpdates.size(), this.queueChunkUploads.size(), this.queueFreeRenderBuilders.size());
   }

   public boolean runChunkUploads(long var1) {
      boolean var3 = false;

      while(true) {
         boolean var4 = false;
         if (this.listWorkerThreads.isEmpty()) {
            ChunkCompileTaskGenerator var5 = (ChunkCompileTaskGenerator)this.queueChunkUpdates.poll();
            if (var5 != null) {
               try {
                  this.renderWorker.processTask(var5);
                  var4 = true;
               } catch (InterruptedException var8) {
                  LOGGER.warn("Skipped task due to interrupt");
               }
            }
         }

         synchronized(this.queueChunkUploads) {
            if (!this.queueChunkUploads.isEmpty()) {
               ((ChunkRenderDispatcher.PendingUpload)this.queueChunkUploads.poll()).uploadTask.run();
               var4 = true;
               var3 = true;
            }
         }

         if (var1 == 0L || !var4 || var1 < System.nanoTime()) {
            break;
         }
      }

      return var3;
   }

   public boolean updateChunkLater(RenderChunk var1) {
      var1.getLockCompileTask().lock();

      boolean var2;
      try {
         final ChunkCompileTaskGenerator var3 = var1.makeCompileTaskChunk();
         var3.addFinishRunnable(new Runnable() {
            public void run() {
               ChunkRenderDispatcher.this.queueChunkUpdates.remove(var3);
            }
         });
         boolean var4 = this.queueChunkUpdates.offer(var3);
         if (!var4) {
            var3.finish();
         }

         var2 = var4;
      } finally {
         var1.getLockCompileTask().unlock();
      }

      return var2;
   }

   public boolean updateChunkNow(RenderChunk var1) {
      var1.getLockCompileTask().lock();

      boolean var2;
      try {
         ChunkCompileTaskGenerator var3 = var1.makeCompileTaskChunk();

         try {
            this.renderWorker.processTask(var3);
         } catch (InterruptedException var8) {
            ;
         }

         var2 = true;
      } finally {
         var1.getLockCompileTask().unlock();
      }

      return var2;
   }

   public void stopChunkUpdates() {
      this.clearChunkUpdates();
      ArrayList var1 = Lists.newArrayList();

      while(var1.size() != this.countRenderBuilders) {
         this.runChunkUploads(Long.MAX_VALUE);

         try {
            var1.add(this.allocateRenderBuilder());
         } catch (InterruptedException var3) {
            ;
         }
      }

      this.queueFreeRenderBuilders.addAll(var1);
   }

   public void freeRenderBuilder(RegionRenderCacheBuilder var1) {
      this.queueFreeRenderBuilders.add(var1);
   }

   public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {
      return (RegionRenderCacheBuilder)this.queueFreeRenderBuilders.take();
   }

   public ChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException {
      return (ChunkCompileTaskGenerator)this.queueChunkUpdates.take();
   }

   public boolean updateTransparencyLater(RenderChunk var1) {
      var1.getLockCompileTask().lock();

      boolean var4;
      try {
         final ChunkCompileTaskGenerator var3 = var1.makeCompileTaskTransparency();
         if (var3 != null) {
            var3.addFinishRunnable(new Runnable() {
               public void run() {
                  ChunkRenderDispatcher.this.queueChunkUpdates.remove(var3);
               }
            });
            boolean var8 = this.queueChunkUpdates.offer(var3);
            return var8;
         }

         boolean var2 = true;
         var4 = var2;
      } finally {
         var1.getLockCompileTask().unlock();
      }

      return var4;
   }

   public ListenableFuture uploadChunk(final BlockRenderLayer var1, final VertexBuffer var2, final RenderChunk var3, final CompiledChunk var4, final double var5) {
      if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
         if (OpenGlHelper.useVbo()) {
            this.uploadVertexBuffer(var2, var3.getVertexBufferByLayer(var1.ordinal()));
         } else {
            this.uploadDisplayList(var2, ((ListedRenderChunk)var3).getDisplayList(var1, var4), var3);
         }

         var2.setTranslation(0.0D, 0.0D, 0.0D);
         return Futures.immediateFuture((Object)null);
      } else {
         ListenableFutureTask var7 = ListenableFutureTask.create(new Runnable() {
            public void run() {
               ChunkRenderDispatcher.this.uploadChunk(var1, var2, var3, var4, var5);
            }
         }, (Object)null);
         synchronized(this.queueChunkUploads) {
            this.queueChunkUploads.add(new ChunkRenderDispatcher.PendingUpload(var7, var5));
            return var7;
         }
      }
   }

   private void uploadDisplayList(VertexBuffer var1, int var2, RenderChunk var3) {
      GlStateManager.glNewList(var2, 4864);
      GlStateManager.pushMatrix();
      var3.multModelviewMatrix();
      this.worldVertexUploader.draw(var1);
      GlStateManager.popMatrix();
      GlStateManager.glEndList();
   }

   private void uploadVertexBuffer(VertexBuffer var1, net.minecraft.client.renderer.vertex.VertexBuffer var2) {
      this.vertexUploader.setVertexBuffer(var2);
      this.vertexUploader.draw(var1);
   }

   public void clearChunkUpdates() {
      while(!this.queueChunkUpdates.isEmpty()) {
         ChunkCompileTaskGenerator var1 = (ChunkCompileTaskGenerator)this.queueChunkUpdates.poll();
         if (var1 != null) {
            var1.finish();
         }
      }

   }

   public boolean hasChunkUpdates() {
      return this.queueChunkUpdates.isEmpty() && this.queueChunkUploads.isEmpty();
   }

   public void stopWorkerThreads() {
      this.clearChunkUpdates();

      for(ChunkRenderWorker var2 : this.listThreadedWorkers) {
         var2.notifyToStop();
      }

      for(Thread var6 : this.listWorkerThreads) {
         try {
            var6.interrupt();
            var6.join();
         } catch (InterruptedException var4) {
            LOGGER.warn("Interrupted whilst waiting for worker to die", var4);
         }
      }

      this.queueFreeRenderBuilders.clear();
   }

   public boolean hasNoFreeRenderBuilders() {
      return this.queueFreeRenderBuilders.size() == 0;
   }

   @SideOnly(Side.CLIENT)
   class PendingUpload implements Comparable {
      private final ListenableFutureTask uploadTask;
      private final double distanceSq;

      public PendingUpload(ListenableFutureTask var2, double var3) {
         this.uploadTask = var2;
         this.distanceSq = var3;
      }

      public int compareTo(ChunkRenderDispatcher.PendingUpload var1) {
         return Doubles.compare(this.distanceSq, var1.distanceSq);
      }
   }
}
