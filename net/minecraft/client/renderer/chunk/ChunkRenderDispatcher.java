package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
      int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / 10485760);
      int j = Math.max(1, MathHelper.clamp(Runtime.getRuntime().availableProcessors(), 1, i / 5));
      if (countRenderBuilders < 0) {
         countRenderBuilders = MathHelper.clamp(j * 10, 1, i);
      }

      this.countRenderBuilders = countRenderBuilders;
      if (j > 1) {
         for(int k = 0; k < j; ++k) {
            ChunkRenderWorker chunkrenderworker = new ChunkRenderWorker(this);
            Thread thread = THREAD_FACTORY.newThread(chunkrenderworker);
            thread.start();
            this.listThreadedWorkers.add(chunkrenderworker);
            this.listWorkerThreads.add(thread);
         }
      }

      this.queueFreeRenderBuilders = Queues.newArrayBlockingQueue(this.countRenderBuilders);

      for(int l = 0; l < this.countRenderBuilders; ++l) {
         this.queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
      }

      this.renderWorker = new ChunkRenderWorker(this, new RegionRenderCacheBuilder());
   }

   public String getDebugInfo() {
      return this.listWorkerThreads.isEmpty() ? String.format("pC: %03d, single-threaded", this.queueChunkUpdates.size()) : String.format("pC: %03d, pU: %1d, aB: %1d", this.queueChunkUpdates.size(), this.queueChunkUploads.size(), this.queueFreeRenderBuilders.size());
   }

   public boolean runChunkUploads(long var1) {
      boolean flag = false;

      while(true) {
         boolean flag1 = false;
         if (this.listWorkerThreads.isEmpty()) {
            ChunkCompileTaskGenerator chunkcompiletaskgenerator = (ChunkCompileTaskGenerator)this.queueChunkUpdates.poll();
            if (chunkcompiletaskgenerator != null) {
               try {
                  this.renderWorker.processTask(chunkcompiletaskgenerator);
                  flag1 = true;
               } catch (InterruptedException var8) {
                  LOGGER.warn("Skipped task due to interrupt");
               }
            }
         }

         synchronized(this.queueChunkUploads) {
            if (!this.queueChunkUploads.isEmpty()) {
               ((ChunkRenderDispatcher.PendingUpload)this.queueChunkUploads.poll()).uploadTask.run();
               flag1 = true;
               flag = true;
            }
         }

         if (p_178516_1_ == 0L || !flag1 || p_178516_1_ < System.nanoTime()) {
            break;
         }
      }

      return flag;
   }

   public boolean updateChunkLater(RenderChunk var1) {
      chunkRenderer.getLockCompileTask().lock();

      boolean flag1;
      try {
         final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();
         chunkcompiletaskgenerator.addFinishRunnable(new Runnable() {
            public void run() {
               ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator);
            }
         });
         boolean flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);
         if (!flag) {
            chunkcompiletaskgenerator.finish();
         }

         flag1 = flag;
      } finally {
         chunkRenderer.getLockCompileTask().unlock();
      }

      return flag1;
   }

   public boolean updateChunkNow(RenderChunk var1) {
      chunkRenderer.getLockCompileTask().lock();

      boolean flag;
      try {
         ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();

         try {
            this.renderWorker.processTask(chunkcompiletaskgenerator);
         } catch (InterruptedException var8) {
            ;
         }

         flag = true;
      } finally {
         chunkRenderer.getLockCompileTask().unlock();
      }

      return flag;
   }

   public void stopChunkUpdates() {
      this.clearChunkUpdates();
      List list = Lists.newArrayList();

      while(((List)list).size() != this.countRenderBuilders) {
         this.runChunkUploads(Long.MAX_VALUE);

         try {
            list.add(this.allocateRenderBuilder());
         } catch (InterruptedException var3) {
            ;
         }
      }

      this.queueFreeRenderBuilders.addAll(list);
   }

   public void freeRenderBuilder(RegionRenderCacheBuilder var1) {
      this.queueFreeRenderBuilders.add(p_178512_1_);
   }

   public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {
      return (RegionRenderCacheBuilder)this.queueFreeRenderBuilders.take();
   }

   public ChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException {
      return (ChunkCompileTaskGenerator)this.queueChunkUpdates.take();
   }

   public boolean updateTransparencyLater(RenderChunk var1) {
      chunkRenderer.getLockCompileTask().lock();

      boolean var4;
      try {
         final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskTransparency();
         if (chunkcompiletaskgenerator != null) {
            chunkcompiletaskgenerator.addFinishRunnable(new Runnable() {
               public void run() {
                  ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator);
               }
            });
            boolean flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);
            return flag;
         }

         boolean flag = true;
         var4 = flag;
      } finally {
         chunkRenderer.getLockCompileTask().unlock();
      }

      return var4;
   }

   public ListenableFuture uploadChunk(final BlockRenderLayer var1, final VertexBuffer var2, final RenderChunk var3, final CompiledChunk var4, final double var5) {
      if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
         if (OpenGlHelper.useVbo()) {
            this.uploadVertexBuffer(p_188245_2_, p_188245_3_.getVertexBufferByLayer(p_188245_1_.ordinal()));
         } else {
            this.uploadDisplayList(p_188245_2_, ((ListedRenderChunk)p_188245_3_).getDisplayList(p_188245_1_, p_188245_4_), p_188245_3_);
         }

         p_188245_2_.setTranslation(0.0D, 0.0D, 0.0D);
         return Futures.immediateFuture((Object)null);
      } else {
         ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(new Runnable() {
            public void run() {
               ChunkRenderDispatcher.this.uploadChunk(p_188245_1_, p_188245_2_, p_188245_3_, p_188245_4_, p_188245_5_);
            }
         }, (Object)null);
         synchronized(this.queueChunkUploads) {
            this.queueChunkUploads.add(new ChunkRenderDispatcher.PendingUpload(listenablefuturetask, p_188245_5_));
            return listenablefuturetask;
         }
      }
   }

   private void uploadDisplayList(VertexBuffer var1, int var2, RenderChunk var3) {
      GlStateManager.glNewList(p_178510_2_, 4864);
      GlStateManager.pushMatrix();
      chunkRenderer.multModelviewMatrix();
      this.worldVertexUploader.draw(p_178510_1_);
      GlStateManager.popMatrix();
      GlStateManager.glEndList();
   }

   private void uploadVertexBuffer(VertexBuffer var1, net.minecraft.client.renderer.vertex.VertexBuffer var2) {
      this.vertexUploader.setVertexBuffer(vertexBufferIn);
      this.vertexUploader.draw(p_178506_1_);
   }

   public void clearChunkUpdates() {
      while(!this.queueChunkUpdates.isEmpty()) {
         ChunkCompileTaskGenerator chunkcompiletaskgenerator = (ChunkCompileTaskGenerator)this.queueChunkUpdates.poll();
         if (chunkcompiletaskgenerator != null) {
            chunkcompiletaskgenerator.finish();
         }
      }

   }

   public boolean hasChunkUpdates() {
      return this.queueChunkUpdates.isEmpty() && this.queueChunkUploads.isEmpty();
   }

   public void stopWorkerThreads() {
      this.clearChunkUpdates();

      for(ChunkRenderWorker chunkrenderworker : this.listThreadedWorkers) {
         chunkrenderworker.notifyToStop();
      }

      for(Thread thread : this.listWorkerThreads) {
         try {
            thread.interrupt();
            thread.join();
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
         this.uploadTask = p_i46994_2_;
         this.distanceSq = p_i46994_3_;
      }

      public int compareTo(ChunkRenderDispatcher.PendingUpload var1) {
         return Doubles.compare(this.distanceSq, p_compareTo_1_.distanceSq);
      }
   }
}
