package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChunk {
   private World world;
   private final RenderGlobal renderGlobal;
   public static int renderChunksUpdated;
   public CompiledChunk compiledChunk = CompiledChunk.DUMMY;
   private final ReentrantLock lockCompileTask = new ReentrantLock();
   private final ReentrantLock lockCompiledChunk = new ReentrantLock();
   private ChunkCompileTaskGenerator compileTask;
   private final Set setTileEntities = Sets.newHashSet();
   private final int index;
   private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
   private final VertexBuffer[] vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];
   public AxisAlignedBB boundingBox;
   private int frameIndex = -1;
   private boolean needsUpdate = true;
   private BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos(-1, -1, -1);
   private BlockPos.MutableBlockPos[] mapEnumFacing = new BlockPos.MutableBlockPos[6];
   private boolean needsUpdateCustom;
   private ChunkCache region;

   public RenderChunk(World var1, RenderGlobal var2, int var3) {
      for(int var4 = 0; var4 < this.mapEnumFacing.length; ++var4) {
         this.mapEnumFacing[var4] = new BlockPos.MutableBlockPos();
      }

      this.world = var1;
      this.renderGlobal = var2;
      this.index = var3;
      if (OpenGlHelper.useVbo()) {
         for(int var5 = 0; var5 < BlockRenderLayer.values().length; ++var5) {
            this.vertexBuffers[var5] = new VertexBuffer(DefaultVertexFormats.BLOCK);
         }
      }

   }

   public boolean setFrameIndex(int var1) {
      if (this.frameIndex == var1) {
         return false;
      } else {
         this.frameIndex = var1;
         return true;
      }
   }

   public VertexBuffer getVertexBufferByLayer(int var1) {
      return this.vertexBuffers[var1];
   }

   public void setPosition(int var1, int var2, int var3) {
      if (var1 != this.position.getX() || var2 != this.position.getY() || var3 != this.position.getZ()) {
         this.stopCompileTask();
         this.position.setPos(var1, var2, var3);
         this.boundingBox = new AxisAlignedBB((double)var1, (double)var2, (double)var3, (double)(var1 + 16), (double)(var2 + 16), (double)(var3 + 16));

         for(EnumFacing var7 : EnumFacing.values()) {
            this.mapEnumFacing[var7.ordinal()].setPos(this.position).move(var7, 16);
         }

         this.initModelviewMatrix();
      }

   }

   public void resortTransparency(float var1, float var2, float var3, ChunkCompileTaskGenerator var4) {
      CompiledChunk var5 = var4.getCompiledChunk();
      if (var5.getState() != null && !var5.isLayerEmpty(BlockRenderLayer.TRANSLUCENT)) {
         this.preRenderBlocks(var4.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), this.position);
         var4.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT).setVertexState(var5.getState());
         this.postRenderBlocks(BlockRenderLayer.TRANSLUCENT, var1, var2, var3, var4.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), var5);
      }

   }

   public void rebuildChunk(float var1, float var2, float var3, ChunkCompileTaskGenerator var4) {
      CompiledChunk var5 = new CompiledChunk();
      boolean var6 = true;
      BlockPos.MutableBlockPos var7 = this.position;
      BlockPos var8 = var7.add(15, 15, 15);
      var4.getLock().lock();

      try {
         if (var4.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING) {
            return;
         }

         var4.setCompiledChunk(var5);
      } finally {
         var4.getLock().unlock();
      }

      VisGraph var9 = new VisGraph();
      HashSet var10 = Sets.newHashSet();
      if (!this.region.extendedLevelsInChunkCache()) {
         ++renderChunksUpdated;
         boolean[] var11 = new boolean[BlockRenderLayer.values().length];
         BlockRendererDispatcher var12 = Minecraft.getMinecraft().getBlockRendererDispatcher();

         for(BlockPos.MutableBlockPos var14 : BlockPos.getAllInBoxMutable(var7, var8)) {
            IBlockState var15 = this.region.getBlockState(var14);
            Block var16 = var15.getBlock();
            if (var15.isOpaqueCube()) {
               var9.setOpaqueCube(var14);
            }

            if (var16.hasTileEntity(var15)) {
               TileEntity var17 = this.region.getTileEntity(var14, Chunk.EnumCreateEntityType.CHECK);
               if (var17 != null) {
                  TileEntitySpecialRenderer var18 = TileEntityRendererDispatcher.instance.getSpecialRenderer(var17);
                  if (var18 != null) {
                     var5.addTileEntity(var17);
                     if (var18.isGlobalRenderer(var17)) {
                        var10.add(var17);
                     }
                  }
               }
            }

            for(BlockRenderLayer var20 : BlockRenderLayer.values()) {
               if (var16.canRenderInLayer(var15, var20)) {
                  ForgeHooksClient.setRenderLayer(var20);
                  int var21 = var20.ordinal();
                  if (var16.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
                     net.minecraft.client.renderer.VertexBuffer var22 = var4.getRegionRenderCacheBuilder().getWorldRendererByLayerId(var21);
                     if (!var5.isLayerStarted(var20)) {
                        var5.setLayerStarted(var20);
                        this.preRenderBlocks(var22, var7);
                     }

                     var11[var21] |= var12.renderBlock(var15, var14, this.region, var22);
                  }
               }
            }

            ForgeHooksClient.setRenderLayer((BlockRenderLayer)null);
         }

         for(BlockRenderLayer var35 : BlockRenderLayer.values()) {
            if (var11[var35.ordinal()]) {
               var5.setLayerUsed(var35);
            }

            if (var5.isLayerStarted(var35)) {
               this.postRenderBlocks(var35, var1, var2, var3, var4.getRegionRenderCacheBuilder().getWorldRendererByLayer(var35), var5);
            }
         }
      }

      var5.setVisibility(var9.computeVisibility());
      this.lockCompileTask.lock();

      try {
         HashSet var30 = Sets.newHashSet(var10);
         HashSet var31 = Sets.newHashSet(this.setTileEntities);
         var30.removeAll(this.setTileEntities);
         var31.removeAll(var10);
         this.setTileEntities.clear();
         this.setTileEntities.addAll(var10);
         this.renderGlobal.updateTileEntities(var31, var30);
      } finally {
         this.lockCompileTask.unlock();
      }

   }

   protected void finishCompileTask() {
      this.lockCompileTask.lock();

      try {
         if (this.compileTask != null && this.compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
            this.compileTask.finish();
            this.compileTask = null;
         }
      } finally {
         this.lockCompileTask.unlock();
      }

   }

   public ReentrantLock getLockCompileTask() {
      return this.lockCompileTask;
   }

   public ChunkCompileTaskGenerator makeCompileTaskChunk() {
      this.lockCompileTask.lock();

      ChunkCompileTaskGenerator var1;
      try {
         this.finishCompileTask();
         this.compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.REBUILD_CHUNK, this.getDistanceSq());
         this.resetChunkCache();
         var1 = this.compileTask;
      } finally {
         this.lockCompileTask.unlock();
      }

      return var1;
   }

   private void resetChunkCache() {
      boolean var1 = true;
      ChunkCache var2 = this.createRegionRenderCache(this.world, this.position.add(-1, -1, -1), this.position.add(16, 16, 16), 1);
      MinecraftForgeClient.onRebuildChunk(this.world, this.position, var2);
      this.region = var2;
   }

   @Nullable
   public ChunkCompileTaskGenerator makeCompileTaskTransparency() {
      this.lockCompileTask.lock();

      ChunkCompileTaskGenerator var2;
      try {
         if (this.compileTask != null && this.compileTask.getStatus() == ChunkCompileTaskGenerator.Status.PENDING) {
            Object var6 = null;
            return (ChunkCompileTaskGenerator)var6;
         }

         if (this.compileTask != null && this.compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
            this.compileTask.finish();
            this.compileTask = null;
         }

         this.compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY, this.getDistanceSq());
         this.compileTask.setCompiledChunk(this.compiledChunk);
         ChunkCompileTaskGenerator var1 = this.compileTask;
         var2 = var1;
      } finally {
         this.lockCompileTask.unlock();
      }

      return var2;
   }

   protected double getDistanceSq() {
      EntityPlayerSP var1 = Minecraft.getMinecraft().player;
      double var2 = this.boundingBox.minX + 8.0D - var1.posX;
      double var4 = this.boundingBox.minY + 8.0D - var1.posY;
      double var6 = this.boundingBox.minZ + 8.0D - var1.posZ;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   private void preRenderBlocks(net.minecraft.client.renderer.VertexBuffer var1, BlockPos var2) {
      var1.begin(7, DefaultVertexFormats.BLOCK);
      var1.setTranslation((double)(-var2.getX()), (double)(-var2.getY()), (double)(-var2.getZ()));
   }

   private void postRenderBlocks(BlockRenderLayer var1, float var2, float var3, float var4, net.minecraft.client.renderer.VertexBuffer var5, CompiledChunk var6) {
      if (var1 == BlockRenderLayer.TRANSLUCENT && !var6.isLayerEmpty(var1)) {
         var5.sortVertexData(var2, var3, var4);
         var6.setState(var5.getVertexState());
      }

      var5.finishDrawing();
   }

   private void initModelviewMatrix() {
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      float var1 = 1.000001F;
      GlStateManager.translate(-8.0F, -8.0F, -8.0F);
      GlStateManager.scale(1.000001F, 1.000001F, 1.000001F);
      GlStateManager.translate(8.0F, 8.0F, 8.0F);
      GlStateManager.getFloat(2982, this.modelviewMatrix);
      GlStateManager.popMatrix();
   }

   public void multModelviewMatrix() {
      GlStateManager.multMatrix(this.modelviewMatrix);
   }

   public CompiledChunk getCompiledChunk() {
      return this.compiledChunk;
   }

   public void setCompiledChunk(CompiledChunk var1) {
      this.lockCompiledChunk.lock();

      try {
         this.compiledChunk = var1;
      } finally {
         this.lockCompiledChunk.unlock();
      }

   }

   public void stopCompileTask() {
      this.finishCompileTask();
      this.compiledChunk = CompiledChunk.DUMMY;
   }

   public void deleteGlResources() {
      this.stopCompileTask();
      this.world = null;

      for(int var1 = 0; var1 < BlockRenderLayer.values().length; ++var1) {
         if (this.vertexBuffers[var1] != null) {
            this.vertexBuffers[var1].deleteGlBuffers();
         }
      }

   }

   public BlockPos getPosition() {
      return this.position;
   }

   public void setNeedsUpdate(boolean var1) {
      if (this.needsUpdate) {
         var1 |= this.needsUpdateCustom;
      }

      this.needsUpdate = true;
      this.needsUpdateCustom = var1;
   }

   public void clearNeedsUpdate() {
      this.needsUpdate = false;
      this.needsUpdateCustom = false;
   }

   public boolean isNeedsUpdate() {
      return this.needsUpdate;
   }

   public boolean isNeedsUpdateCustom() {
      return this.needsUpdate && this.needsUpdateCustom;
   }

   protected ChunkCache createRegionRenderCache(World var1, BlockPos var2, BlockPos var3, int var4) {
      return new ChunkCache(var1, var2, var3, var4);
   }

   public BlockPos getBlockPosOffset16(EnumFacing var1) {
      return this.mapEnumFacing[var1.ordinal()];
   }

   public World getWorld() {
      return this.world;
   }
}
