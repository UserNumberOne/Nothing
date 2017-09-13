package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.ListChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VboChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

@SideOnly(Side.CLIENT)
public class RenderGlobal implements IWorldEventListener, IResourceManagerReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("textures/environment/moon_phases.png");
   private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
   private static final ResourceLocation CLOUDS_TEXTURES = new ResourceLocation("textures/environment/clouds.png");
   private static final ResourceLocation END_SKY_TEXTURES = new ResourceLocation("textures/environment/end_sky.png");
   private static final ResourceLocation FORCEFIELD_TEXTURES = new ResourceLocation("textures/misc/forcefield.png");
   private final Minecraft mc;
   private final TextureManager renderEngine;
   private final RenderManager renderManager;
   private WorldClient world;
   private Set chunksToUpdate = Sets.newLinkedHashSet();
   private List renderInfos = Lists.newArrayListWithCapacity(69696);
   private final Set setTileEntities = Sets.newHashSet();
   private ViewFrustum viewFrustum;
   private int starGLCallList = -1;
   private int glSkyList = -1;
   private int glSkyList2 = -1;
   private final VertexFormat vertexBufferFormat;
   private net.minecraft.client.renderer.vertex.VertexBuffer starVBO;
   private net.minecraft.client.renderer.vertex.VertexBuffer skyVBO;
   private net.minecraft.client.renderer.vertex.VertexBuffer sky2VBO;
   private int cloudTickCounter;
   private final Map damagedBlocks = Maps.newHashMap();
   private final Map mapSoundPositions = Maps.newHashMap();
   private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
   private Framebuffer entityOutlineFramebuffer;
   private ShaderGroup entityOutlineShader;
   private double frustumUpdatePosX = Double.MIN_VALUE;
   private double frustumUpdatePosY = Double.MIN_VALUE;
   private double frustumUpdatePosZ = Double.MIN_VALUE;
   private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
   private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
   private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
   private double lastViewEntityX = Double.MIN_VALUE;
   private double lastViewEntityY = Double.MIN_VALUE;
   private double lastViewEntityZ = Double.MIN_VALUE;
   private double lastViewEntityPitch = Double.MIN_VALUE;
   private double lastViewEntityYaw = Double.MIN_VALUE;
   private ChunkRenderDispatcher renderDispatcher;
   private ChunkRenderContainer renderContainer;
   private int renderDistanceChunks = -1;
   private int renderEntitiesStartupCounter = 2;
   private int countEntitiesTotal;
   private int countEntitiesRendered;
   private int countEntitiesHidden;
   private boolean debugFixTerrainFrustum;
   private ClippingHelper debugFixedClippingHelper;
   private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
   private final Vector3d debugTerrainFrustumPosition = new Vector3d();
   private boolean vboEnabled;
   IRenderChunkFactory renderChunkFactory;
   private double prevRenderSortX;
   private double prevRenderSortY;
   private double prevRenderSortZ;
   private boolean displayListEntitiesDirty = true;
   private boolean entityOutlinesRendered;
   private final Set setLightUpdates = Sets.newHashSet();

   public RenderGlobal(Minecraft var1) {
      this.mc = var1;
      this.renderManager = var1.getRenderManager();
      this.renderEngine = var1.getTextureManager();
      this.renderEngine.bindTexture(FORCEFIELD_TEXTURES);
      GlStateManager.glTexParameteri(3553, 10242, 10497);
      GlStateManager.glTexParameteri(3553, 10243, 10497);
      GlStateManager.bindTexture(0);
      this.updateDestroyBlockIcons();
      this.vboEnabled = OpenGlHelper.useVbo();
      if (this.vboEnabled) {
         this.renderContainer = new VboRenderList();
         this.renderChunkFactory = new VboChunkFactory();
      } else {
         this.renderContainer = new RenderList();
         this.renderChunkFactory = new ListChunkFactory();
      }

      this.vertexBufferFormat = new VertexFormat();
      this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
      this.generateStars();
      this.generateSky();
      this.generateSky2();
   }

   public void onResourceManagerReload(IResourceManager var1) {
      this.updateDestroyBlockIcons();
   }

   private void updateDestroyBlockIcons() {
      TextureMap var1 = this.mc.getTextureMapBlocks();

      for(int var2 = 0; var2 < this.destroyBlockIcons.length; ++var2) {
         this.destroyBlockIcons[var2] = var1.getAtlasSprite("minecraft:blocks/destroy_stage_" + var2);
      }

   }

   public void makeEntityOutlineShader() {
      if (OpenGlHelper.shadersSupported) {
         if (ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
         }

         ResourceLocation var1 = new ResourceLocation("shaders/post/entity_outline.json");

         try {
            this.entityOutlineShader = new ShaderGroup(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getFramebuffer(), var1);
            this.entityOutlineShader.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
            this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");
         } catch (IOException var3) {
            LOGGER.warn("Failed to load shader: {}", new Object[]{var1, var3});
            this.entityOutlineShader = null;
            this.entityOutlineFramebuffer = null;
         } catch (JsonSyntaxException var4) {
            LOGGER.warn("Failed to load shader: {}", new Object[]{var1, var4});
            this.entityOutlineShader = null;
            this.entityOutlineFramebuffer = null;
         }
      } else {
         this.entityOutlineShader = null;
         this.entityOutlineFramebuffer = null;
      }

   }

   public void renderEntityOutlineFramebuffer() {
      if (this.isRenderEntityOutlines()) {
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         this.entityOutlineFramebuffer.framebufferRenderExt(this.mc.displayWidth, this.mc.displayHeight, false);
         GlStateManager.disableBlend();
      }

   }

   protected boolean isRenderEntityOutlines() {
      return this.entityOutlineFramebuffer != null && this.entityOutlineShader != null && this.mc.player != null;
   }

   private void generateSky2() {
      Tessellator var1 = Tessellator.getInstance();
      VertexBuffer var2 = var1.getBuffer();
      if (this.sky2VBO != null) {
         this.sky2VBO.deleteGlBuffers();
      }

      if (this.glSkyList2 >= 0) {
         GLAllocation.deleteDisplayLists(this.glSkyList2);
         this.glSkyList2 = -1;
      }

      if (this.vboEnabled) {
         this.sky2VBO = new net.minecraft.client.renderer.vertex.VertexBuffer(this.vertexBufferFormat);
         this.renderSky(var2, -16.0F, true);
         var2.finishDrawing();
         var2.reset();
         this.sky2VBO.bufferData(var2.getByteBuffer());
      } else {
         this.glSkyList2 = GLAllocation.generateDisplayLists(1);
         GlStateManager.glNewList(this.glSkyList2, 4864);
         this.renderSky(var2, -16.0F, true);
         var1.draw();
         GlStateManager.glEndList();
      }

   }

   private void generateSky() {
      Tessellator var1 = Tessellator.getInstance();
      VertexBuffer var2 = var1.getBuffer();
      if (this.skyVBO != null) {
         this.skyVBO.deleteGlBuffers();
      }

      if (this.glSkyList >= 0) {
         GLAllocation.deleteDisplayLists(this.glSkyList);
         this.glSkyList = -1;
      }

      if (this.vboEnabled) {
         this.skyVBO = new net.minecraft.client.renderer.vertex.VertexBuffer(this.vertexBufferFormat);
         this.renderSky(var2, 16.0F, false);
         var2.finishDrawing();
         var2.reset();
         this.skyVBO.bufferData(var2.getByteBuffer());
      } else {
         this.glSkyList = GLAllocation.generateDisplayLists(1);
         GlStateManager.glNewList(this.glSkyList, 4864);
         this.renderSky(var2, 16.0F, false);
         var1.draw();
         GlStateManager.glEndList();
      }

   }

   private void renderSky(VertexBuffer var1, float var2, boolean var3) {
      boolean var4 = true;
      boolean var5 = true;
      var1.begin(7, DefaultVertexFormats.POSITION);

      for(int var6 = -384; var6 <= 384; var6 += 64) {
         for(int var7 = -384; var7 <= 384; var7 += 64) {
            float var8 = (float)var6;
            float var9 = (float)(var6 + 64);
            if (var3) {
               var9 = (float)var6;
               var8 = (float)(var6 + 64);
            }

            var1.pos((double)var8, (double)var2, (double)var7).endVertex();
            var1.pos((double)var9, (double)var2, (double)var7).endVertex();
            var1.pos((double)var9, (double)var2, (double)(var7 + 64)).endVertex();
            var1.pos((double)var8, (double)var2, (double)(var7 + 64)).endVertex();
         }
      }

   }

   private void generateStars() {
      Tessellator var1 = Tessellator.getInstance();
      VertexBuffer var2 = var1.getBuffer();
      if (this.starVBO != null) {
         this.starVBO.deleteGlBuffers();
      }

      if (this.starGLCallList >= 0) {
         GLAllocation.deleteDisplayLists(this.starGLCallList);
         this.starGLCallList = -1;
      }

      if (this.vboEnabled) {
         this.starVBO = new net.minecraft.client.renderer.vertex.VertexBuffer(this.vertexBufferFormat);
         this.renderStars(var2);
         var2.finishDrawing();
         var2.reset();
         this.starVBO.bufferData(var2.getByteBuffer());
      } else {
         this.starGLCallList = GLAllocation.generateDisplayLists(1);
         GlStateManager.pushMatrix();
         GlStateManager.glNewList(this.starGLCallList, 4864);
         this.renderStars(var2);
         var1.draw();
         GlStateManager.glEndList();
         GlStateManager.popMatrix();
      }

   }

   private void renderStars(VertexBuffer var1) {
      Random var2 = new Random(10842L);
      var1.begin(7, DefaultVertexFormats.POSITION);

      for(int var3 = 0; var3 < 1500; ++var3) {
         double var4 = (double)(var2.nextFloat() * 2.0F - 1.0F);
         double var6 = (double)(var2.nextFloat() * 2.0F - 1.0F);
         double var8 = (double)(var2.nextFloat() * 2.0F - 1.0F);
         double var10 = (double)(0.15F + var2.nextFloat() * 0.1F);
         double var12 = var4 * var4 + var6 * var6 + var8 * var8;
         if (var12 < 1.0D && var12 > 0.01D) {
            var12 = 1.0D / Math.sqrt(var12);
            var4 = var4 * var12;
            var6 = var6 * var12;
            var8 = var8 * var12;
            double var14 = var4 * 100.0D;
            double var16 = var6 * 100.0D;
            double var18 = var8 * 100.0D;
            double var20 = Math.atan2(var4, var8);
            double var22 = Math.sin(var20);
            double var24 = Math.cos(var20);
            double var26 = Math.atan2(Math.sqrt(var4 * var4 + var8 * var8), var6);
            double var28 = Math.sin(var26);
            double var30 = Math.cos(var26);
            double var32 = var2.nextDouble() * 3.141592653589793D * 2.0D;
            double var34 = Math.sin(var32);
            double var36 = Math.cos(var32);

            for(int var38 = 0; var38 < 4; ++var38) {
               double var39 = 0.0D;
               double var41 = (double)((var38 & 2) - 1) * var10;
               double var43 = (double)((var38 + 1 & 2) - 1) * var10;
               double var45 = 0.0D;
               double var47 = var41 * var36 - var43 * var34;
               double var49 = var43 * var36 + var41 * var34;
               double var51 = var47 * var28 + 0.0D * var30;
               double var53 = 0.0D * var28 - var47 * var30;
               double var55 = var53 * var22 - var49 * var24;
               double var57 = var49 * var22 + var53 * var24;
               var1.pos(var14 + var55, var16 + var51, var18 + var57).endVertex();
            }
         }
      }

   }

   public void setWorldAndLoadRenderers(@Nullable WorldClient var1) {
      if (this.world != null) {
         this.world.removeEventListener(this);
      }

      this.frustumUpdatePosX = Double.MIN_VALUE;
      this.frustumUpdatePosY = Double.MIN_VALUE;
      this.frustumUpdatePosZ = Double.MIN_VALUE;
      this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
      this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
      this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
      this.renderManager.setWorld(var1);
      this.world = var1;
      if (var1 != null) {
         var1.addEventListener(this);
         this.loadRenderers();
      } else {
         this.chunksToUpdate.clear();
         this.renderInfos.clear();
         if (this.viewFrustum != null) {
            this.viewFrustum.deleteGlResources();
         }

         this.viewFrustum = null;
         if (this.renderDispatcher != null) {
            this.renderDispatcher.stopWorkerThreads();
         }

         this.renderDispatcher = null;
      }

   }

   public void loadRenderers() {
      if (this.world != null) {
         if (this.renderDispatcher == null) {
            this.renderDispatcher = new ChunkRenderDispatcher();
         }

         this.displayListEntitiesDirty = true;
         Blocks.LEAVES.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
         Blocks.LEAVES2.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
         this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
         boolean var1 = this.vboEnabled;
         this.vboEnabled = OpenGlHelper.useVbo();
         if (var1 && !this.vboEnabled) {
            this.renderContainer = new RenderList();
            this.renderChunkFactory = new ListChunkFactory();
         } else if (!var1 && this.vboEnabled) {
            this.renderContainer = new VboRenderList();
            this.renderChunkFactory = new VboChunkFactory();
         }

         if (var1 != this.vboEnabled) {
            this.generateStars();
            this.generateSky();
            this.generateSky2();
         }

         if (this.viewFrustum != null) {
            this.viewFrustum.deleteGlResources();
         }

         this.stopChunkUpdates();
         synchronized(this.setTileEntities) {
            this.setTileEntities.clear();
         }

         this.viewFrustum = new ViewFrustum(this.world, this.mc.gameSettings.renderDistanceChunks, this, this.renderChunkFactory);
         if (this.world != null) {
            Entity var5 = this.mc.getRenderViewEntity();
            if (var5 != null) {
               this.viewFrustum.updateChunkPositions(var5.posX, var5.posZ);
            }
         }

         this.renderEntitiesStartupCounter = 2;
      }

   }

   protected void stopChunkUpdates() {
      this.chunksToUpdate.clear();
      this.renderDispatcher.stopChunkUpdates();
   }

   public void createBindEntityOutlineFbs(int var1, int var2) {
      if (OpenGlHelper.shadersSupported && this.entityOutlineShader != null) {
         this.entityOutlineShader.createBindFramebuffers(var1, var2);
      }

   }

   public void renderEntities(Entity var1, ICamera var2, float var3) {
      int var4 = MinecraftForgeClient.getRenderPass();
      if (this.renderEntitiesStartupCounter > 0) {
         if (var4 > 0) {
            return;
         }

         --this.renderEntitiesStartupCounter;
      } else {
         double var5 = var1.prevPosX + (var1.posX - var1.prevPosX) * (double)var3;
         double var7 = var1.prevPosY + (var1.posY - var1.prevPosY) * (double)var3;
         double var9 = var1.prevPosZ + (var1.posZ - var1.prevPosZ) * (double)var3;
         this.world.theProfiler.startSection("prepare");
         TileEntityRendererDispatcher.instance.prepare(this.world, this.mc.getTextureManager(), this.mc.fontRendererObj, this.mc.getRenderViewEntity(), this.mc.objectMouseOver, var3);
         this.renderManager.cacheActiveRenderInfo(this.world, this.mc.fontRendererObj, this.mc.getRenderViewEntity(), this.mc.pointedEntity, this.mc.gameSettings, var3);
         if (var4 == 0) {
            this.countEntitiesTotal = 0;
            this.countEntitiesRendered = 0;
            this.countEntitiesHidden = 0;
         }

         Entity var11 = this.mc.getRenderViewEntity();
         double var12 = var11.lastTickPosX + (var11.posX - var11.lastTickPosX) * (double)var3;
         double var14 = var11.lastTickPosY + (var11.posY - var11.lastTickPosY) * (double)var3;
         double var16 = var11.lastTickPosZ + (var11.posZ - var11.lastTickPosZ) * (double)var3;
         TileEntityRendererDispatcher.staticPlayerX = var12;
         TileEntityRendererDispatcher.staticPlayerY = var14;
         TileEntityRendererDispatcher.staticPlayerZ = var16;
         this.renderManager.setRenderPosition(var12, var14, var16);
         this.mc.entityRenderer.enableLightmap();
         this.world.theProfiler.endStartSection("global");
         List var18 = this.world.getLoadedEntityList();
         if (var4 == 0) {
            this.countEntitiesTotal = var18.size();
         }

         for(int var19 = 0; var19 < this.world.weatherEffects.size(); ++var19) {
            Entity var20 = (Entity)this.world.weatherEffects.get(var19);
            if (var20.shouldRenderInPass(var4)) {
               ++this.countEntitiesRendered;
               if (var20.isInRangeToRender3d(var5, var7, var9)) {
                  this.renderManager.renderEntityStatic(var20, var3, false);
               }
            }
         }

         this.world.theProfiler.endStartSection("entities");
         ArrayList var32 = Lists.newArrayList();
         ArrayList var33 = Lists.newArrayList();
         BlockPos.PooledMutableBlockPos var21 = BlockPos.PooledMutableBlockPos.retain();

         for(RenderGlobal.ContainerLocalRenderInformation var23 : this.renderInfos) {
            Chunk var24 = this.world.getChunkFromBlockCoords(var23.renderChunk.getPosition());
            ClassInheritanceMultiMap var25 = var24.getEntityLists()[var23.renderChunk.getPosition().getY() / 16];
            if (!var25.isEmpty()) {
               for(Entity var27 : var25) {
                  if (var27.shouldRenderInPass(var4)) {
                     boolean var28 = this.renderManager.shouldRender(var27, var2, var5, var7, var9) || var27.isRidingOrBeingRiddenBy(this.mc.player);
                     if (var28) {
                        boolean var29 = this.mc.getRenderViewEntity() instanceof EntityLivingBase ? ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping() : false;
                        if ((var27 != this.mc.getRenderViewEntity() || this.mc.gameSettings.thirdPersonView != 0 || var29) && (var27.posY < 0.0D || var27.posY >= 256.0D || this.world.isBlockLoaded(var21.setPos(var27)))) {
                           ++this.countEntitiesRendered;
                           this.renderManager.renderEntityStatic(var27, var3, false);
                           if (this.isOutlineActive(var27, var11, var2)) {
                              var32.add(var27);
                           }

                           if (this.renderManager.isRenderMultipass(var27)) {
                              var33.add(var27);
                           }
                        }
                     }
                  }
               }
            }
         }

         var21.release();
         if (!var33.isEmpty()) {
            for(Entity var39 : var33) {
               this.renderManager.renderMultipass(var39, var3);
            }
         }

         if (var4 == 0 && this.isRenderEntityOutlines() && (!var32.isEmpty() || this.entityOutlinesRendered)) {
            this.world.theProfiler.endStartSection("entityOutlines");
            this.entityOutlineFramebuffer.framebufferClear();
            this.entityOutlinesRendered = !var32.isEmpty();
            if (!var32.isEmpty()) {
               GlStateManager.depthFunc(519);
               GlStateManager.disableFog();
               this.entityOutlineFramebuffer.bindFramebuffer(false);
               RenderHelper.disableStandardItemLighting();
               this.renderManager.setRenderOutlines(true);

               for(int var35 = 0; var35 < var32.size(); ++var35) {
                  this.renderManager.renderEntityStatic((Entity)var32.get(var35), var3, false);
               }

               this.renderManager.setRenderOutlines(false);
               RenderHelper.enableStandardItemLighting();
               GlStateManager.depthMask(false);
               this.entityOutlineShader.loadShaderGroup(var3);
               GlStateManager.enableLighting();
               GlStateManager.depthMask(true);
               GlStateManager.enableFog();
               GlStateManager.enableBlend();
               GlStateManager.enableColorMaterial();
               GlStateManager.depthFunc(515);
               GlStateManager.enableDepth();
               GlStateManager.enableAlpha();
            }

            this.mc.getFramebuffer().bindFramebuffer(false);
         }

         this.world.theProfiler.endStartSection("blockentities");
         RenderHelper.enableStandardItemLighting();
         TileEntityRendererDispatcher.instance.preDrawBatch();

         for(RenderGlobal.ContainerLocalRenderInformation var40 : this.renderInfos) {
            List var43 = var40.renderChunk.getCompiledChunk().getTileEntities();
            if (!var43.isEmpty()) {
               for(TileEntity var48 : var43) {
                  if (var48.shouldRenderInPass(var4) && var2.isBoundingBoxInFrustum(var48.getRenderBoundingBox())) {
                     TileEntityRendererDispatcher.instance.renderTileEntity(var48, var3, -1);
                  }
               }
            }
         }

         synchronized(this.setTileEntities) {
            for(TileEntity var44 : this.setTileEntities) {
               if (var44.shouldRenderInPass(var4) && var2.isBoundingBoxInFrustum(var44.getRenderBoundingBox())) {
                  TileEntityRendererDispatcher.instance.renderTileEntity(var44, var3, -1);
               }
            }
         }

         TileEntityRendererDispatcher.instance.drawBatch(var4);
         this.preRenderDamagedBlocks();

         for(DestroyBlockProgress var42 : this.damagedBlocks.values()) {
            BlockPos var45 = var42.getPosition();
            TileEntity var47 = this.world.getTileEntity(var45);
            if (var47 instanceof TileEntityChest) {
               TileEntityChest var49 = (TileEntityChest)var47;
               if (var49.adjacentChestXNeg != null) {
                  var45 = var45.offset(EnumFacing.WEST);
                  var47 = this.world.getTileEntity(var45);
               } else if (var49.adjacentChestZNeg != null) {
                  var45 = var45.offset(EnumFacing.NORTH);
                  var47 = this.world.getTileEntity(var45);
               }
            }

            Block var50 = this.world.getBlockState(var45).getBlock();
            if (var47 != null && var47.shouldRenderInPass(var4) && var47.canRenderBreaking() && var2.isBoundingBoxInFrustum(var47.getRenderBoundingBox())) {
               TileEntityRendererDispatcher.instance.renderTileEntity(var47, var3, var42.getPartialBlockDamage());
            }
         }

         this.postRenderDamagedBlocks();
         this.mc.entityRenderer.disableLightmap();
         this.mc.mcProfiler.endSection();
      }

   }

   private boolean isOutlineActive(Entity var1, Entity var2, ICamera var3) {
      boolean var4 = var2 instanceof EntityLivingBase && ((EntityLivingBase)var2).isPlayerSleeping();
      return var1 == var2 && this.mc.gameSettings.thirdPersonView == 0 && !var4 ? false : (var1.isGlowing() ? true : (this.mc.player.isSpectator() && this.mc.gameSettings.keyBindSpectatorOutlines.isKeyDown() && var1 instanceof EntityPlayer ? var1.ignoreFrustumCheck || var3.isBoundingBoxInFrustum(var1.getEntityBoundingBox()) || var1.isRidingOrBeingRiddenBy(this.mc.player) : false));
   }

   public String getDebugInfoRenders() {
      int var1 = this.viewFrustum.renderChunks.length;
      int var2 = this.getRenderedChunks();
      return String.format("C: %d/%d %sD: %d, L: %d, %s", var2, var1, this.mc.renderChunksMany ? "(s) " : "", this.renderDistanceChunks, this.setLightUpdates.size(), this.renderDispatcher == null ? "null" : this.renderDispatcher.getDebugInfo());
   }

   protected int getRenderedChunks() {
      int var1 = 0;

      for(RenderGlobal.ContainerLocalRenderInformation var3 : this.renderInfos) {
         CompiledChunk var4 = var3.renderChunk.compiledChunk;
         if (var4 != CompiledChunk.DUMMY && !var4.isEmpty()) {
            ++var1;
         }
      }

      return var1;
   }

   public String getDebugInfoEntities() {
      return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ", B: " + this.countEntitiesHidden;
   }

   public void setupTerrain(Entity var1, double var2, ICamera var4, int var5, boolean var6) {
      if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
         this.loadRenderers();
      }

      this.world.theProfiler.startSection("camera");
      double var7 = var1.posX - this.frustumUpdatePosX;
      double var9 = var1.posY - this.frustumUpdatePosY;
      double var11 = var1.posZ - this.frustumUpdatePosZ;
      if (this.frustumUpdatePosChunkX != var1.chunkCoordX || this.frustumUpdatePosChunkY != var1.chunkCoordY || this.frustumUpdatePosChunkZ != var1.chunkCoordZ || var7 * var7 + var9 * var9 + var11 * var11 > 16.0D) {
         this.frustumUpdatePosX = var1.posX;
         this.frustumUpdatePosY = var1.posY;
         this.frustumUpdatePosZ = var1.posZ;
         this.frustumUpdatePosChunkX = var1.chunkCoordX;
         this.frustumUpdatePosChunkY = var1.chunkCoordY;
         this.frustumUpdatePosChunkZ = var1.chunkCoordZ;
         this.viewFrustum.updateChunkPositions(var1.posX, var1.posZ);
      }

      this.world.theProfiler.endStartSection("renderlistcamera");
      double var13 = var1.lastTickPosX + (var1.posX - var1.lastTickPosX) * var2;
      double var15 = var1.lastTickPosY + (var1.posY - var1.lastTickPosY) * var2;
      double var17 = var1.lastTickPosZ + (var1.posZ - var1.lastTickPosZ) * var2;
      this.renderContainer.initialize(var13, var15, var17);
      this.world.theProfiler.endStartSection("cull");
      if (this.debugFixedClippingHelper != null) {
         Frustum var19 = new Frustum(this.debugFixedClippingHelper);
         var19.setPosition(this.debugTerrainFrustumPosition.x, this.debugTerrainFrustumPosition.y, this.debugTerrainFrustumPosition.z);
         var4 = var19;
      }

      this.mc.mcProfiler.endStartSection("culling");
      BlockPos var34 = new BlockPos(var13, var15 + (double)var1.getEyeHeight(), var17);
      RenderChunk var20 = this.viewFrustum.getRenderChunk(var34);
      BlockPos var21 = new BlockPos(MathHelper.floor(var13 / 16.0D) * 16, MathHelper.floor(var15 / 16.0D) * 16, MathHelper.floor(var17 / 16.0D) * 16);
      this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || var1.posX != this.lastViewEntityX || var1.posY != this.lastViewEntityY || var1.posZ != this.lastViewEntityZ || (double)var1.rotationPitch != this.lastViewEntityPitch || (double)var1.rotationYaw != this.lastViewEntityYaw;
      this.lastViewEntityX = var1.posX;
      this.lastViewEntityY = var1.posY;
      this.lastViewEntityZ = var1.posZ;
      this.lastViewEntityPitch = (double)var1.rotationPitch;
      this.lastViewEntityYaw = (double)var1.rotationYaw;
      boolean var22 = this.debugFixedClippingHelper != null;
      this.mc.mcProfiler.endStartSection("update");
      if (!var22 && this.displayListEntitiesDirty) {
         this.displayListEntitiesDirty = false;
         this.renderInfos = Lists.newArrayList();
         ArrayDeque var23 = Queues.newArrayDeque();
         Entity.setRenderDistanceWeight(MathHelper.clamp((double)this.mc.gameSettings.renderDistanceChunks / 8.0D, 1.0D, 2.5D));
         boolean var24 = this.mc.renderChunksMany;
         if (var20 != null) {
            boolean var37 = false;
            RenderGlobal.ContainerLocalRenderInformation var40 = new RenderGlobal.ContainerLocalRenderInformation(var20, (EnumFacing)null, 0);
            Set var43 = this.getVisibleFacings(var34);
            if (var43.size() == 1) {
               Vector3f var46 = this.getViewVector(var1, var2);
               EnumFacing var29 = EnumFacing.getFacingFromVector(var46.x, var46.y, var46.z).getOpposite();
               var43.remove(var29);
            }

            if (var43.isEmpty()) {
               var37 = true;
            }

            if (var37 && !var6) {
               this.renderInfos.add(var40);
            } else {
               if (var6 && this.world.getBlockState(var34).isOpaqueCube()) {
                  var24 = false;
               }

               var20.setFrameIndex(var5);
               var23.add(var40);
            }
         } else {
            int var25 = var34.getY() > 0 ? 248 : 8;

            for(int var26 = -this.renderDistanceChunks; var26 <= this.renderDistanceChunks; ++var26) {
               for(int var27 = -this.renderDistanceChunks; var27 <= this.renderDistanceChunks; ++var27) {
                  RenderChunk var28 = this.viewFrustum.getRenderChunk(new BlockPos((var26 << 4) + 8, var25, (var27 << 4) + 8));
                  if (var28 != null && ((ICamera)var4).isBoundingBoxInFrustum(var28.boundingBox)) {
                     var28.setFrameIndex(var5);
                     var23.add(new RenderGlobal.ContainerLocalRenderInformation(var28, (EnumFacing)null, 0));
                  }
               }
            }
         }

         this.mc.mcProfiler.startSection("iteration");

         while(!var23.isEmpty()) {
            RenderGlobal.ContainerLocalRenderInformation var38 = (RenderGlobal.ContainerLocalRenderInformation)var23.poll();
            RenderChunk var41 = var38.renderChunk;
            EnumFacing var44 = var38.facing;
            this.renderInfos.add(var38);

            for(EnumFacing var31 : EnumFacing.values()) {
               RenderChunk var32 = this.getRenderChunkOffset(var21, var41, var31);
               if ((!var24 || !var38.hasDirection(var31.getOpposite())) && (!var24 || var44 == null || var41.getCompiledChunk().isVisible(var44.getOpposite(), var31)) && var32 != null && var32.setFrameIndex(var5) && ((ICamera)var4).isBoundingBoxInFrustum(var32.boundingBox)) {
                  RenderGlobal.ContainerLocalRenderInformation var33 = new RenderGlobal.ContainerLocalRenderInformation(var32, var31, var38.counter + 1);
                  var33.setDirection(var38.setFacing, var31);
                  var23.add(var33);
               }
            }
         }

         this.mc.mcProfiler.endSection();
      }

      this.mc.mcProfiler.endStartSection("captureFrustum");
      if (this.debugFixTerrainFrustum) {
         this.fixTerrainFrustum(var13, var15, var17);
         this.debugFixTerrainFrustum = false;
      }

      this.mc.mcProfiler.endStartSection("rebuildNear");
      Set var35 = this.chunksToUpdate;
      this.chunksToUpdate = Sets.newLinkedHashSet();

      for(RenderGlobal.ContainerLocalRenderInformation var39 : this.renderInfos) {
         RenderChunk var42 = var39.renderChunk;
         if (var42.isNeedsUpdate() || var35.contains(var42)) {
            this.displayListEntitiesDirty = true;
            BlockPos var45 = var42.getPosition().add(8, 8, 8);
            boolean var48 = var45.distanceSq(var34) < 768.0D;
            if (!var42.isNeedsUpdateCustom() && !var48) {
               this.chunksToUpdate.add(var42);
            } else {
               this.mc.mcProfiler.startSection("build near");
               this.renderDispatcher.updateChunkNow(var42);
               var42.clearNeedsUpdate();
               this.mc.mcProfiler.endSection();
            }
         }
      }

      this.chunksToUpdate.addAll(var35);
      this.mc.mcProfiler.endSection();
   }

   private Set getVisibleFacings(BlockPos var1) {
      VisGraph var2 = new VisGraph();
      BlockPos var3 = new BlockPos(var1.getX() >> 4 << 4, var1.getY() >> 4 << 4, var1.getZ() >> 4 << 4);
      Chunk var4 = this.world.getChunkFromBlockCoords(var3);

      for(BlockPos.MutableBlockPos var6 : BlockPos.getAllInBoxMutable(var3, var3.add(15, 15, 15))) {
         if (var4.getBlockState(var6).isOpaqueCube()) {
            var2.setOpaqueCube(var6);
         }
      }

      return var2.getVisibleFacings(var1);
   }

   @Nullable
   private RenderChunk getRenderChunkOffset(BlockPos var1, RenderChunk var2, EnumFacing var3) {
      BlockPos var4 = var2.getBlockPosOffset16(var3);
      return MathHelper.abs(var1.getX() - var4.getX()) > this.renderDistanceChunks * 16 ? null : (var4.getY() >= 0 && var4.getY() < 256 ? (MathHelper.abs(var1.getZ() - var4.getZ()) > this.renderDistanceChunks * 16 ? null : this.viewFrustum.getRenderChunk(var4)) : null);
   }

   private void fixTerrainFrustum(double var1, double var3, double var5) {
      this.debugFixedClippingHelper = new ClippingHelperImpl();
      ((ClippingHelperImpl)this.debugFixedClippingHelper).init();
      Matrix4f var7 = new Matrix4f(this.debugFixedClippingHelper.modelviewMatrix);
      var7.transpose();
      Matrix4f var8 = new Matrix4f(this.debugFixedClippingHelper.projectionMatrix);
      var8.transpose();
      Matrix4f var9 = new Matrix4f();
      Matrix4f.mul(var8, var7, var9);
      var9.invert();
      this.debugTerrainFrustumPosition.x = var1;
      this.debugTerrainFrustumPosition.y = var3;
      this.debugTerrainFrustumPosition.z = var5;
      this.debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
      this.debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
      this.debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

      for(int var10 = 0; var10 < 8; ++var10) {
         Matrix4f.transform(var9, this.debugTerrainMatrix[var10], this.debugTerrainMatrix[var10]);
         this.debugTerrainMatrix[var10].x /= this.debugTerrainMatrix[var10].w;
         this.debugTerrainMatrix[var10].y /= this.debugTerrainMatrix[var10].w;
         this.debugTerrainMatrix[var10].z /= this.debugTerrainMatrix[var10].w;
         this.debugTerrainMatrix[var10].w = 1.0F;
      }

   }

   protected Vector3f getViewVector(Entity var1, double var2) {
      float var4 = (float)((double)var1.prevRotationPitch + (double)(var1.rotationPitch - var1.prevRotationPitch) * var2);
      float var5 = (float)((double)var1.prevRotationYaw + (double)(var1.rotationYaw - var1.prevRotationYaw) * var2);
      if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
         var4 += 180.0F;
      }

      float var6 = MathHelper.cos(-var5 * 0.017453292F - 3.1415927F);
      float var7 = MathHelper.sin(-var5 * 0.017453292F - 3.1415927F);
      float var8 = -MathHelper.cos(-var4 * 0.017453292F);
      float var9 = MathHelper.sin(-var4 * 0.017453292F);
      return new Vector3f(var7 * var8, var9, var6 * var8);
   }

   public int renderBlockLayer(BlockRenderLayer var1, double var2, int var4, Entity var5) {
      RenderHelper.disableStandardItemLighting();
      if (var1 == BlockRenderLayer.TRANSLUCENT) {
         this.mc.mcProfiler.startSection("translucent_sort");
         double var6 = var5.posX - this.prevRenderSortX;
         double var8 = var5.posY - this.prevRenderSortY;
         double var10 = var5.posZ - this.prevRenderSortZ;
         if (var6 * var6 + var8 * var8 + var10 * var10 > 1.0D) {
            this.prevRenderSortX = var5.posX;
            this.prevRenderSortY = var5.posY;
            this.prevRenderSortZ = var5.posZ;
            int var12 = 0;

            for(RenderGlobal.ContainerLocalRenderInformation var14 : this.renderInfos) {
               if (var14.renderChunk.compiledChunk.isLayerStarted(var1) && var12++ < 15) {
                  this.renderDispatcher.updateTransparencyLater(var14.renderChunk);
               }
            }
         }

         this.mc.mcProfiler.endSection();
      }

      this.mc.mcProfiler.startSection("filterempty");
      int var15 = 0;
      boolean var7 = var1 == BlockRenderLayer.TRANSLUCENT;
      int var16 = var7 ? this.renderInfos.size() - 1 : 0;
      int var9 = var7 ? -1 : this.renderInfos.size();
      int var17 = var7 ? -1 : 1;

      for(int var11 = var16; var11 != var9; var11 += var17) {
         RenderChunk var18 = ((RenderGlobal.ContainerLocalRenderInformation)this.renderInfos.get(var11)).renderChunk;
         if (!var18.getCompiledChunk().isLayerEmpty(var1)) {
            ++var15;
            this.renderContainer.addRenderChunk(var18, var1);
         }
      }

      this.mc.mcProfiler.endStartSection("render_" + var1);
      this.renderBlockLayer(var1);
      this.mc.mcProfiler.endSection();
      return var15;
   }

   private void renderBlockLayer(BlockRenderLayer var1) {
      this.mc.entityRenderer.enableLightmap();
      if (OpenGlHelper.useVbo()) {
         GlStateManager.glEnableClientState(32884);
         OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
         GlStateManager.glEnableClientState(32888);
         OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
         GlStateManager.glEnableClientState(32888);
         OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
         GlStateManager.glEnableClientState(32886);
      }

      this.renderContainer.renderChunkLayer(var1);
      if (OpenGlHelper.useVbo()) {
         for(VertexFormatElement var3 : DefaultVertexFormats.BLOCK.getElements()) {
            VertexFormatElement.EnumUsage var4 = var3.getUsage();
            int var5 = var3.getIndex();
            switch(var4) {
            case POSITION:
               GlStateManager.glDisableClientState(32884);
               break;
            case UV:
               OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + var5);
               GlStateManager.glDisableClientState(32888);
               OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
               break;
            case COLOR:
               GlStateManager.glDisableClientState(32886);
               GlStateManager.resetColor();
            }
         }
      }

      this.mc.entityRenderer.disableLightmap();
   }

   private void cleanupDamagedBlocks(Iterator var1) {
      while(var1.hasNext()) {
         DestroyBlockProgress var2 = (DestroyBlockProgress)var1.next();
         int var3 = var2.getCreationCloudUpdateTick();
         if (this.cloudTickCounter - var3 > 400) {
            var1.remove();
         }
      }

   }

   public void updateClouds() {
      ++this.cloudTickCounter;
      if (this.cloudTickCounter % 20 == 0) {
         this.cleanupDamagedBlocks(this.damagedBlocks.values().iterator());
      }

      if (!this.setLightUpdates.isEmpty() && !this.renderDispatcher.hasNoFreeRenderBuilders() && this.chunksToUpdate.isEmpty()) {
         Iterator var1 = this.setLightUpdates.iterator();

         while(var1.hasNext()) {
            BlockPos var2 = (BlockPos)var1.next();
            var1.remove();
            int var3 = var2.getX();
            int var4 = var2.getY();
            int var5 = var2.getZ();
            this.markBlocksForUpdate(var3 - 1, var4 - 1, var5 - 1, var3 + 1, var4 + 1, var5 + 1, false);
         }
      }

   }

   private void renderSkyEnd() {
      GlStateManager.disableFog();
      GlStateManager.disableAlpha();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.depthMask(false);
      this.renderEngine.bindTexture(END_SKY_TEXTURES);
      Tessellator var1 = Tessellator.getInstance();
      VertexBuffer var2 = var1.getBuffer();

      for(int var3 = 0; var3 < 6; ++var3) {
         GlStateManager.pushMatrix();
         if (var3 == 1) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         }

         if (var3 == 2) {
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
         }

         if (var3 == 3) {
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         }

         if (var3 == 4) {
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
         }

         if (var3 == 5) {
            GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
         }

         var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         var2.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(40, 40, 40, 255).endVertex();
         var2.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(40, 40, 40, 255).endVertex();
         var2.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(40, 40, 40, 255).endVertex();
         var2.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(40, 40, 40, 255).endVertex();
         var1.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.depthMask(true);
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
   }

   public void renderSky(float var1, int var2) {
      IRenderHandler var3 = this.world.provider.getSkyRenderer();
      if (var3 != null) {
         var3.render(var1, this.world, this.mc);
      } else {
         if (this.mc.world.provider.getDimensionType().getId() == 1) {
            this.renderSkyEnd();
         } else if (this.mc.world.provider.isSurfaceWorld()) {
            GlStateManager.disableTexture2D();
            Vec3d var4 = this.world.getSkyColor(this.mc.getRenderViewEntity(), var1);
            float var5 = (float)var4.xCoord;
            float var6 = (float)var4.yCoord;
            float var7 = (float)var4.zCoord;
            if (var2 != 2) {
               float var8 = (var5 * 30.0F + var6 * 59.0F + var7 * 11.0F) / 100.0F;
               float var9 = (var5 * 30.0F + var6 * 70.0F) / 100.0F;
               float var10 = (var5 * 30.0F + var7 * 70.0F) / 100.0F;
               var5 = var8;
               var6 = var9;
               var7 = var10;
            }

            GlStateManager.color(var5, var6, var7);
            Tessellator var26 = Tessellator.getInstance();
            VertexBuffer var27 = var26.getBuffer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();
            GlStateManager.color(var5, var6, var7);
            if (this.vboEnabled) {
               this.skyVBO.bindBuffer();
               GlStateManager.glEnableClientState(32884);
               GlStateManager.glVertexPointer(3, 5126, 12, 0);
               this.skyVBO.drawArrays(7);
               this.skyVBO.unbindBuffer();
               GlStateManager.glDisableClientState(32884);
            } else {
               GlStateManager.callList(this.glSkyList);
            }

            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.disableStandardItemLighting();
            float[] var28 = this.world.provider.calcSunriseSunsetColors(this.world.getCelestialAngle(var1), var1);
            if (var28 != null) {
               GlStateManager.disableTexture2D();
               GlStateManager.shadeModel(7425);
               GlStateManager.pushMatrix();
               GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate(MathHelper.sin(this.world.getCelestialAngleRadians(var1)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
               GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
               float var11 = var28[0];
               float var12 = var28[1];
               float var13 = var28[2];
               if (var2 != 2) {
                  float var14 = (var11 * 30.0F + var12 * 59.0F + var13 * 11.0F) / 100.0F;
                  float var15 = (var11 * 30.0F + var12 * 70.0F) / 100.0F;
                  float var16 = (var11 * 30.0F + var13 * 70.0F) / 100.0F;
                  var11 = var14;
                  var12 = var15;
                  var13 = var16;
               }

               var27.begin(6, DefaultVertexFormats.POSITION_COLOR);
               var27.pos(0.0D, 100.0D, 0.0D).color(var11, var12, var13, var28[3]).endVertex();
               boolean var33 = true;

               for(int var35 = 0; var35 <= 16; ++var35) {
                  float var37 = (float)var35 * 6.2831855F / 16.0F;
                  float var17 = MathHelper.sin(var37);
                  float var18 = MathHelper.cos(var37);
                  var27.pos((double)(var17 * 120.0F), (double)(var18 * 120.0F), (double)(-var18 * 40.0F * var28[3])).color(var28[0], var28[1], var28[2], 0.0F).endVertex();
               }

               var26.draw();
               GlStateManager.popMatrix();
               GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            float var29 = 1.0F - this.world.getRainStrength(var1);
            GlStateManager.color(1.0F, 1.0F, 1.0F, var29);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.world.getCelestialAngle(var1) * 360.0F, 1.0F, 0.0F, 0.0F);
            float var30 = 30.0F;
            this.renderEngine.bindTexture(SUN_TEXTURES);
            var27.begin(7, DefaultVertexFormats.POSITION_TEX);
            var27.pos((double)(-var30), 100.0D, (double)(-var30)).tex(0.0D, 0.0D).endVertex();
            var27.pos((double)var30, 100.0D, (double)(-var30)).tex(1.0D, 0.0D).endVertex();
            var27.pos((double)var30, 100.0D, (double)var30).tex(1.0D, 1.0D).endVertex();
            var27.pos((double)(-var30), 100.0D, (double)var30).tex(0.0D, 1.0D).endVertex();
            var26.draw();
            var30 = 20.0F;
            this.renderEngine.bindTexture(MOON_PHASES_TEXTURES);
            int var32 = this.world.getMoonPhase();
            int var34 = var32 % 4;
            int var36 = var32 / 4 % 2;
            float var38 = (float)(var34 + 0) / 4.0F;
            float var39 = (float)(var36 + 0) / 2.0F;
            float var40 = (float)(var34 + 1) / 4.0F;
            float var19 = (float)(var36 + 1) / 2.0F;
            var27.begin(7, DefaultVertexFormats.POSITION_TEX);
            var27.pos((double)(-var30), -100.0D, (double)var30).tex((double)var40, (double)var19).endVertex();
            var27.pos((double)var30, -100.0D, (double)var30).tex((double)var38, (double)var19).endVertex();
            var27.pos((double)var30, -100.0D, (double)(-var30)).tex((double)var38, (double)var39).endVertex();
            var27.pos((double)(-var30), -100.0D, (double)(-var30)).tex((double)var40, (double)var39).endVertex();
            var26.draw();
            GlStateManager.disableTexture2D();
            float var20 = this.world.getStarBrightness(var1) * var29;
            if (var20 > 0.0F) {
               GlStateManager.color(var20, var20, var20, var20);
               if (this.vboEnabled) {
                  this.starVBO.bindBuffer();
                  GlStateManager.glEnableClientState(32884);
                  GlStateManager.glVertexPointer(3, 5126, 12, 0);
                  this.starVBO.drawArrays(7);
                  this.starVBO.unbindBuffer();
                  GlStateManager.glDisableClientState(32884);
               } else {
                  GlStateManager.callList(this.starGLCallList);
               }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();
            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double var21 = this.mc.player.getPositionEyes(var1).yCoord - this.world.getHorizon();
            if (var21 < 0.0D) {
               GlStateManager.pushMatrix();
               GlStateManager.translate(0.0F, 12.0F, 0.0F);
               if (this.vboEnabled) {
                  this.sky2VBO.bindBuffer();
                  GlStateManager.glEnableClientState(32884);
                  GlStateManager.glVertexPointer(3, 5126, 12, 0);
                  this.sky2VBO.drawArrays(7);
                  this.sky2VBO.unbindBuffer();
                  GlStateManager.glDisableClientState(32884);
               } else {
                  GlStateManager.callList(this.glSkyList2);
               }

               GlStateManager.popMatrix();
               float var23 = 1.0F;
               float var24 = -((float)(var21 + 65.0D));
               float var25 = -1.0F;
               var27.begin(7, DefaultVertexFormats.POSITION_COLOR);
               var27.pos(-1.0D, (double)var24, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, (double)var24, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, (double)var24, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, (double)var24, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, (double)var24, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, (double)var24, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, (double)var24, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, (double)var24, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
               var27.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
               var26.draw();
            }

            if (this.world.provider.isSkyColored()) {
               GlStateManager.color(var5 * 0.2F + 0.04F, var6 * 0.2F + 0.04F, var7 * 0.6F + 0.1F);
            } else {
               GlStateManager.color(var5, var6, var7);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float)(var21 - 16.0D)), 0.0F);
            GlStateManager.callList(this.glSkyList2);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
         }

      }
   }

   public void renderClouds(float var1, int var2) {
      IRenderHandler var3 = this.mc.world.provider.getCloudRenderer();
      if (var3 != null) {
         var3.render(var1, this.mc.world, this.mc);
      } else {
         if (this.mc.world.provider.isSurfaceWorld()) {
            if (this.mc.gameSettings.shouldRenderClouds() == 2) {
               this.renderCloudsFancy(var1, var2);
            } else {
               GlStateManager.disableCull();
               Entity var4 = this.mc.getRenderViewEntity();
               float var5 = (float)(var4.lastTickPosY + (var4.posY - var4.lastTickPosY) * (double)var1);
               boolean var6 = true;
               boolean var7 = true;
               Tessellator var8 = Tessellator.getInstance();
               VertexBuffer var9 = var8.getBuffer();
               this.renderEngine.bindTexture(CLOUDS_TEXTURES);
               GlStateManager.enableBlend();
               GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
               Vec3d var10 = this.world.getCloudColour(var1);
               float var11 = (float)var10.xCoord;
               float var12 = (float)var10.yCoord;
               float var13 = (float)var10.zCoord;
               if (var2 != 2) {
                  float var14 = (var11 * 30.0F + var12 * 59.0F + var13 * 11.0F) / 100.0F;
                  float var15 = (var11 * 30.0F + var12 * 70.0F) / 100.0F;
                  float var16 = (var11 * 30.0F + var13 * 70.0F) / 100.0F;
                  var11 = var14;
                  var12 = var15;
                  var13 = var16;
               }

               float var28 = 4.8828125E-4F;
               double var29 = (double)((float)this.cloudTickCounter + var1);
               double var17 = var4.prevPosX + (var4.posX - var4.prevPosX) * (double)var1 + var29 * 0.029999999329447746D;
               double var19 = var4.prevPosZ + (var4.posZ - var4.prevPosZ) * (double)var1;
               int var21 = MathHelper.floor(var17 / 2048.0D);
               int var22 = MathHelper.floor(var19 / 2048.0D);
               var17 = var17 - (double)(var21 * 2048);
               var19 = var19 - (double)(var22 * 2048);
               float var23 = this.world.provider.getCloudHeight() - var5 + 0.33F;
               float var24 = (float)(var17 * 4.8828125E-4D);
               float var25 = (float)(var19 * 4.8828125E-4D);
               var9.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

               for(int var26 = -256; var26 < 256; var26 += 32) {
                  for(int var27 = -256; var27 < 256; var27 += 32) {
                     var9.pos((double)(var26 + 0), (double)var23, (double)(var27 + 32)).tex((double)((float)(var26 + 0) * 4.8828125E-4F + var24), (double)((float)(var27 + 32) * 4.8828125E-4F + var25)).color(var11, var12, var13, 0.8F).endVertex();
                     var9.pos((double)(var26 + 32), (double)var23, (double)(var27 + 32)).tex((double)((float)(var26 + 32) * 4.8828125E-4F + var24), (double)((float)(var27 + 32) * 4.8828125E-4F + var25)).color(var11, var12, var13, 0.8F).endVertex();
                     var9.pos((double)(var26 + 32), (double)var23, (double)(var27 + 0)).tex((double)((float)(var26 + 32) * 4.8828125E-4F + var24), (double)((float)(var27 + 0) * 4.8828125E-4F + var25)).color(var11, var12, var13, 0.8F).endVertex();
                     var9.pos((double)(var26 + 0), (double)var23, (double)(var27 + 0)).tex((double)((float)(var26 + 0) * 4.8828125E-4F + var24), (double)((float)(var27 + 0) * 4.8828125E-4F + var25)).color(var11, var12, var13, 0.8F).endVertex();
                  }
               }

               var8.draw();
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.disableBlend();
               GlStateManager.enableCull();
            }
         }

      }
   }

   public boolean hasCloudFog(double var1, double var3, double var5, float var7) {
      return false;
   }

   private void renderCloudsFancy(float var1, int var2) {
      GlStateManager.disableCull();
      Entity var3 = this.mc.getRenderViewEntity();
      float var4 = (float)(var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double)var1);
      Tessellator var5 = Tessellator.getInstance();
      VertexBuffer var6 = var5.getBuffer();
      float var7 = 12.0F;
      float var8 = 4.0F;
      double var9 = (double)((float)this.cloudTickCounter + var1);
      double var11 = (var3.prevPosX + (var3.posX - var3.prevPosX) * (double)var1 + var9 * 0.029999999329447746D) / 12.0D;
      double var13 = (var3.prevPosZ + (var3.posZ - var3.prevPosZ) * (double)var1) / 12.0D + 0.33000001311302185D;
      float var15 = this.world.provider.getCloudHeight() - var4 + 0.33F;
      int var16 = MathHelper.floor(var11 / 2048.0D);
      int var17 = MathHelper.floor(var13 / 2048.0D);
      var11 = var11 - (double)(var16 * 2048);
      var13 = var13 - (double)(var17 * 2048);
      this.renderEngine.bindTexture(CLOUDS_TEXTURES);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      Vec3d var18 = this.world.getCloudColour(var1);
      float var19 = (float)var18.xCoord;
      float var20 = (float)var18.yCoord;
      float var21 = (float)var18.zCoord;
      if (var2 != 2) {
         float var22 = (var19 * 30.0F + var20 * 59.0F + var21 * 11.0F) / 100.0F;
         float var23 = (var19 * 30.0F + var20 * 70.0F) / 100.0F;
         float var24 = (var19 * 30.0F + var21 * 70.0F) / 100.0F;
         var19 = var22;
         var20 = var23;
         var21 = var24;
      }

      float var49 = var19 * 0.9F;
      float var50 = var20 * 0.9F;
      float var51 = var21 * 0.9F;
      float var25 = var19 * 0.7F;
      float var26 = var20 * 0.7F;
      float var27 = var21 * 0.7F;
      float var28 = var19 * 0.8F;
      float var29 = var20 * 0.8F;
      float var30 = var21 * 0.8F;
      float var31 = 0.00390625F;
      float var32 = (float)MathHelper.floor(var11) * 0.00390625F;
      float var33 = (float)MathHelper.floor(var13) * 0.00390625F;
      float var34 = (float)(var11 - (double)MathHelper.floor(var11));
      float var35 = (float)(var13 - (double)MathHelper.floor(var13));
      boolean var36 = true;
      boolean var37 = true;
      float var38 = 9.765625E-4F;
      GlStateManager.scale(12.0F, 1.0F, 12.0F);

      for(int var39 = 0; var39 < 2; ++var39) {
         if (var39 == 0) {
            GlStateManager.colorMask(false, false, false, false);
         } else {
            switch(var2) {
            case 0:
               GlStateManager.colorMask(false, true, true, true);
               break;
            case 1:
               GlStateManager.colorMask(true, false, false, true);
               break;
            case 2:
               GlStateManager.colorMask(true, true, true, true);
            }
         }

         for(int var40 = -3; var40 <= 4; ++var40) {
            for(int var41 = -3; var41 <= 4; ++var41) {
               var6.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               float var42 = (float)(var40 * 8);
               float var43 = (float)(var41 * 8);
               float var44 = var42 - var34;
               float var45 = var43 - var35;
               if (var15 > -5.0F) {
                  var6.pos((double)(var44 + 0.0F), (double)(var15 + 0.0F), (double)(var45 + 8.0F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var25, var26, var27, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  var6.pos((double)(var44 + 8.0F), (double)(var15 + 0.0F), (double)(var45 + 8.0F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var25, var26, var27, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  var6.pos((double)(var44 + 8.0F), (double)(var15 + 0.0F), (double)(var45 + 0.0F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var25, var26, var27, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  var6.pos((double)(var44 + 0.0F), (double)(var15 + 0.0F), (double)(var45 + 0.0F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var25, var26, var27, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               }

               if (var15 <= 5.0F) {
                  var6.pos((double)(var44 + 0.0F), (double)(var15 + 4.0F - 9.765625E-4F), (double)(var45 + 8.0F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var19, var20, var21, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  var6.pos((double)(var44 + 8.0F), (double)(var15 + 4.0F - 9.765625E-4F), (double)(var45 + 8.0F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var19, var20, var21, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  var6.pos((double)(var44 + 8.0F), (double)(var15 + 4.0F - 9.765625E-4F), (double)(var45 + 0.0F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var19, var20, var21, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  var6.pos((double)(var44 + 0.0F), (double)(var15 + 4.0F - 9.765625E-4F), (double)(var45 + 0.0F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var19, var20, var21, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
               }

               if (var40 > -1) {
                  for(int var46 = 0; var46 < 8; ++var46) {
                     var6.pos((double)(var44 + (float)var46 + 0.0F), (double)(var15 + 0.0F), (double)(var45 + 8.0F)).tex((double)((var42 + (float)var46 + 0.5F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     var6.pos((double)(var44 + (float)var46 + 0.0F), (double)(var15 + 4.0F), (double)(var45 + 8.0F)).tex((double)((var42 + (float)var46 + 0.5F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     var6.pos((double)(var44 + (float)var46 + 0.0F), (double)(var15 + 4.0F), (double)(var45 + 0.0F)).tex((double)((var42 + (float)var46 + 0.5F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     var6.pos((double)(var44 + (float)var46 + 0.0F), (double)(var15 + 0.0F), (double)(var45 + 0.0F)).tex((double)((var42 + (float)var46 + 0.5F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (var40 <= 1) {
                  for(int var52 = 0; var52 < 8; ++var52) {
                     var6.pos((double)(var44 + (float)var52 + 1.0F - 9.765625E-4F), (double)(var15 + 0.0F), (double)(var45 + 8.0F)).tex((double)((var42 + (float)var52 + 0.5F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     var6.pos((double)(var44 + (float)var52 + 1.0F - 9.765625E-4F), (double)(var15 + 4.0F), (double)(var45 + 8.0F)).tex((double)((var42 + (float)var52 + 0.5F) * 0.00390625F + var32), (double)((var43 + 8.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     var6.pos((double)(var44 + (float)var52 + 1.0F - 9.765625E-4F), (double)(var15 + 4.0F), (double)(var45 + 0.0F)).tex((double)((var42 + (float)var52 + 0.5F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     var6.pos((double)(var44 + (float)var52 + 1.0F - 9.765625E-4F), (double)(var15 + 0.0F), (double)(var45 + 0.0F)).tex((double)((var42 + (float)var52 + 0.5F) * 0.00390625F + var32), (double)((var43 + 0.0F) * 0.00390625F + var33)).color(var49, var50, var51, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (var41 > -1) {
                  for(int var53 = 0; var53 < 8; ++var53) {
                     var6.pos((double)(var44 + 0.0F), (double)(var15 + 4.0F), (double)(var45 + (float)var53 + 0.0F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + (float)var53 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     var6.pos((double)(var44 + 8.0F), (double)(var15 + 4.0F), (double)(var45 + (float)var53 + 0.0F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + (float)var53 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     var6.pos((double)(var44 + 8.0F), (double)(var15 + 0.0F), (double)(var45 + (float)var53 + 0.0F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + (float)var53 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     var6.pos((double)(var44 + 0.0F), (double)(var15 + 0.0F), (double)(var45 + (float)var53 + 0.0F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + (float)var53 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                  }
               }

               if (var41 <= 1) {
                  for(int var54 = 0; var54 < 8; ++var54) {
                     var6.pos((double)(var44 + 0.0F), (double)(var15 + 4.0F), (double)(var45 + (float)var54 + 1.0F - 9.765625E-4F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + (float)var54 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     var6.pos((double)(var44 + 8.0F), (double)(var15 + 4.0F), (double)(var45 + (float)var54 + 1.0F - 9.765625E-4F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + (float)var54 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     var6.pos((double)(var44 + 8.0F), (double)(var15 + 0.0F), (double)(var45 + (float)var54 + 1.0F - 9.765625E-4F)).tex((double)((var42 + 8.0F) * 0.00390625F + var32), (double)((var43 + (float)var54 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     var6.pos((double)(var44 + 0.0F), (double)(var15 + 0.0F), (double)(var45 + (float)var54 + 1.0F - 9.765625E-4F)).tex((double)((var42 + 0.0F) * 0.00390625F + var32), (double)((var43 + (float)var54 + 0.5F) * 0.00390625F + var33)).color(var28, var29, var30, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                  }
               }

               var5.draw();
            }
         }
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
   }

   public void updateChunks(long var1) {
      this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(var1);
      if (!this.chunksToUpdate.isEmpty()) {
         Iterator var3 = this.chunksToUpdate.iterator();

         while(var3.hasNext()) {
            RenderChunk var4 = (RenderChunk)var3.next();
            boolean var5;
            if (var4.isNeedsUpdateCustom()) {
               var5 = this.renderDispatcher.updateChunkNow(var4);
            } else {
               var5 = this.renderDispatcher.updateChunkLater(var4);
            }

            if (!var5) {
               break;
            }

            var4.clearNeedsUpdate();
            var3.remove();
            long var6 = var1 - System.nanoTime();
            if (var6 < 0L) {
               break;
            }
         }
      }

   }

   public void renderWorldBorder(Entity var1, float var2) {
      Tessellator var3 = Tessellator.getInstance();
      VertexBuffer var4 = var3.getBuffer();
      WorldBorder var5 = this.world.getWorldBorder();
      double var6 = (double)(this.mc.gameSettings.renderDistanceChunks * 16);
      if (var1.posX >= var5.maxX() - var6 || var1.posX <= var5.minX() + var6 || var1.posZ >= var5.maxZ() - var6 || var1.posZ <= var5.minZ() + var6) {
         double var8 = 1.0D - var5.getClosestDistance(var1) / var6;
         var8 = Math.pow(var8, 4.0D);
         double var10 = var1.lastTickPosX + (var1.posX - var1.lastTickPosX) * (double)var2;
         double var12 = var1.lastTickPosY + (var1.posY - var1.lastTickPosY) * (double)var2;
         double var14 = var1.lastTickPosZ + (var1.posZ - var1.lastTickPosZ) * (double)var2;
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         this.renderEngine.bindTexture(FORCEFIELD_TEXTURES);
         GlStateManager.depthMask(false);
         GlStateManager.pushMatrix();
         int var16 = var5.getStatus().getID();
         float var17 = (float)(var16 >> 16 & 255) / 255.0F;
         float var18 = (float)(var16 >> 8 & 255) / 255.0F;
         float var19 = (float)(var16 & 255) / 255.0F;
         GlStateManager.color(var17, var18, var19, (float)var8);
         GlStateManager.doPolygonOffset(-3.0F, -3.0F);
         GlStateManager.enablePolygonOffset();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.enableAlpha();
         GlStateManager.disableCull();
         float var20 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
         float var21 = 0.0F;
         float var22 = 0.0F;
         float var23 = 128.0F;
         var4.begin(7, DefaultVertexFormats.POSITION_TEX);
         var4.setTranslation(-var10, -var12, -var14);
         double var24 = Math.max((double)MathHelper.floor(var14 - var6), var5.minZ());
         double var26 = Math.min((double)MathHelper.ceil(var14 + var6), var5.maxZ());
         if (var10 > var5.maxX() - var6) {
            float var28 = 0.0F;

            for(double var29 = var24; var29 < var26; var28 += 0.5F) {
               double var31 = Math.min(1.0D, var26 - var29);
               float var33 = (float)var31 * 0.5F;
               var4.pos(var5.maxX(), 256.0D, var29).tex((double)(var20 + var28), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var5.maxX(), 256.0D, var29 + var31).tex((double)(var20 + var33 + var28), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var5.maxX(), 0.0D, var29 + var31).tex((double)(var20 + var33 + var28), (double)(var20 + 128.0F)).endVertex();
               var4.pos(var5.maxX(), 0.0D, var29).tex((double)(var20 + var28), (double)(var20 + 128.0F)).endVertex();
               ++var29;
            }
         }

         if (var10 < var5.minX() + var6) {
            float var37 = 0.0F;

            for(double var40 = var24; var40 < var26; var37 += 0.5F) {
               double var43 = Math.min(1.0D, var26 - var40);
               float var46 = (float)var43 * 0.5F;
               var4.pos(var5.minX(), 256.0D, var40).tex((double)(var20 + var37), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var5.minX(), 256.0D, var40 + var43).tex((double)(var20 + var46 + var37), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var5.minX(), 0.0D, var40 + var43).tex((double)(var20 + var46 + var37), (double)(var20 + 128.0F)).endVertex();
               var4.pos(var5.minX(), 0.0D, var40).tex((double)(var20 + var37), (double)(var20 + 128.0F)).endVertex();
               ++var40;
            }
         }

         var24 = Math.max((double)MathHelper.floor(var10 - var6), var5.minX());
         var26 = Math.min((double)MathHelper.ceil(var10 + var6), var5.maxX());
         if (var14 > var5.maxZ() - var6) {
            float var38 = 0.0F;

            for(double var41 = var24; var41 < var26; var38 += 0.5F) {
               double var44 = Math.min(1.0D, var26 - var41);
               float var47 = (float)var44 * 0.5F;
               var4.pos(var41, 256.0D, var5.maxZ()).tex((double)(var20 + var38), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var41 + var44, 256.0D, var5.maxZ()).tex((double)(var20 + var47 + var38), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var41 + var44, 0.0D, var5.maxZ()).tex((double)(var20 + var47 + var38), (double)(var20 + 128.0F)).endVertex();
               var4.pos(var41, 0.0D, var5.maxZ()).tex((double)(var20 + var38), (double)(var20 + 128.0F)).endVertex();
               ++var41;
            }
         }

         if (var14 < var5.minZ() + var6) {
            float var39 = 0.0F;

            for(double var42 = var24; var42 < var26; var39 += 0.5F) {
               double var45 = Math.min(1.0D, var26 - var42);
               float var48 = (float)var45 * 0.5F;
               var4.pos(var42, 256.0D, var5.minZ()).tex((double)(var20 + var39), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var42 + var45, 256.0D, var5.minZ()).tex((double)(var20 + var48 + var39), (double)(var20 + 0.0F)).endVertex();
               var4.pos(var42 + var45, 0.0D, var5.minZ()).tex((double)(var20 + var48 + var39), (double)(var20 + 128.0F)).endVertex();
               var4.pos(var42, 0.0D, var5.minZ()).tex((double)(var20 + var39), (double)(var20 + 128.0F)).endVertex();
               ++var42;
            }
         }

         var3.draw();
         var4.setTranslation(0.0D, 0.0D, 0.0D);
         GlStateManager.enableCull();
         GlStateManager.disableAlpha();
         GlStateManager.doPolygonOffset(0.0F, 0.0F);
         GlStateManager.disablePolygonOffset();
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         GlStateManager.depthMask(true);
      }

   }

   private void preRenderDamagedBlocks() {
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.enableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.doPolygonOffset(-3.0F, -3.0F);
      GlStateManager.enablePolygonOffset();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.enableAlpha();
      GlStateManager.pushMatrix();
   }

   private void postRenderDamagedBlocks() {
      GlStateManager.disableAlpha();
      GlStateManager.doPolygonOffset(0.0F, 0.0F);
      GlStateManager.disablePolygonOffset();
      GlStateManager.enableAlpha();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   public void drawBlockDamageTexture(Tessellator var1, VertexBuffer var2, Entity var3, float var4) {
      double var5 = var3.lastTickPosX + (var3.posX - var3.lastTickPosX) * (double)var4;
      double var7 = var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double)var4;
      double var9 = var3.lastTickPosZ + (var3.posZ - var3.lastTickPosZ) * (double)var4;
      if (!this.damagedBlocks.isEmpty()) {
         this.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         this.preRenderDamagedBlocks();
         var2.begin(7, DefaultVertexFormats.BLOCK);
         var2.setTranslation(-var5, -var7, -var9);
         var2.noColor();
         Iterator var11 = this.damagedBlocks.values().iterator();

         while(var11.hasNext()) {
            DestroyBlockProgress var12 = (DestroyBlockProgress)var11.next();
            BlockPos var13 = var12.getPosition();
            double var14 = (double)var13.getX() - var5;
            double var16 = (double)var13.getY() - var7;
            double var18 = (double)var13.getZ() - var9;
            Block var20 = this.world.getBlockState(var13).getBlock();
            TileEntity var21 = this.world.getTileEntity(var13);
            boolean var22 = var20 instanceof BlockChest || var20 instanceof BlockEnderChest || var20 instanceof BlockSign || var20 instanceof BlockSkull;
            if (!var22) {
               var22 = var21 != null && var21.canRenderBreaking();
            }

            if (!var22) {
               if (var14 * var14 + var16 * var16 + var18 * var18 > 1024.0D) {
                  var11.remove();
               } else {
                  IBlockState var23 = this.world.getBlockState(var13);
                  if (var23.getMaterial() != Material.AIR) {
                     int var24 = var12.getPartialBlockDamage();
                     TextureAtlasSprite var25 = this.destroyBlockIcons[var24];
                     BlockRendererDispatcher var26 = this.mc.getBlockRendererDispatcher();
                     var26.renderBlockDamage(var23, var13, var25, this.world);
                  }
               }
            }
         }

         var1.draw();
         var2.setTranslation(0.0D, 0.0D, 0.0D);
         this.postRenderDamagedBlocks();
      }

   }

   public void drawSelectionBox(EntityPlayer var1, RayTraceResult var2, int var3, float var4) {
      if (var3 == 0 && var2.typeOfHit == RayTraceResult.Type.BLOCK) {
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         GlStateManager.glLineWidth(2.0F);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         BlockPos var5 = var2.getBlockPos();
         IBlockState var6 = this.world.getBlockState(var5);
         if (var6.getMaterial() != Material.AIR && this.world.getWorldBorder().contains(var5)) {
            double var7 = var1.lastTickPosX + (var1.posX - var1.lastTickPosX) * (double)var4;
            double var9 = var1.lastTickPosY + (var1.posY - var1.lastTickPosY) * (double)var4;
            double var11 = var1.lastTickPosZ + (var1.posZ - var1.lastTickPosZ) * (double)var4;
            drawSelectionBoundingBox(var6.getSelectedBoundingBox(this.world, var5).expandXyz(0.0020000000949949026D).offset(-var7, -var9, -var11), 0.0F, 0.0F, 0.0F, 0.4F);
         }

         GlStateManager.depthMask(true);
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
      }

   }

   public static void drawSelectionBoundingBox(AxisAlignedBB var0, float var1, float var2, float var3, float var4) {
      drawBoundingBox(var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxY, var0.maxZ, var1, var2, var3, var4);
   }

   public static void drawBoundingBox(double var0, double var2, double var4, double var6, double var8, double var10, float var12, float var13, float var14, float var15) {
      Tessellator var16 = Tessellator.getInstance();
      VertexBuffer var17 = var16.getBuffer();
      var17.begin(3, DefaultVertexFormats.POSITION_COLOR);
      drawBoundingBox(var17, var0, var2, var4, var6, var8, var10, var12, var13, var14, var15);
      var16.draw();
   }

   public static void drawBoundingBox(VertexBuffer var0, double var1, double var3, double var5, double var7, double var9, double var11, float var13, float var14, float var15, float var16) {
      var0.pos(var1, var3, var5).color(var13, var14, var15, 0.0F).endVertex();
      var0.pos(var1, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var11).color(var13, var14, var15, 0.0F).endVertex();
      var0.pos(var1, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var11).color(var13, var14, var15, 0.0F).endVertex();
      var0.pos(var7, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var5).color(var13, var14, var15, 0.0F).endVertex();
      var0.pos(var7, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var5).color(var13, var14, var15, 0.0F).endVertex();
   }

   public static void renderFilledBox(AxisAlignedBB var0, float var1, float var2, float var3, float var4) {
      renderFilledBox(var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxY, var0.maxZ, var1, var2, var3, var4);
   }

   public static void renderFilledBox(double var0, double var2, double var4, double var6, double var8, double var10, float var12, float var13, float var14, float var15) {
      Tessellator var16 = Tessellator.getInstance();
      VertexBuffer var17 = var16.getBuffer();
      var17.begin(5, DefaultVertexFormats.POSITION_COLOR);
      addChainedFilledBoxVertices(var17, var0, var2, var4, var6, var8, var10, var12, var13, var14, var15);
      var16.draw();
   }

   public static void addChainedFilledBoxVertices(VertexBuffer var0, double var1, double var3, double var5, double var7, double var9, double var11, float var13, float var14, float var15, float var16) {
      var0.pos(var1, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var3, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var1, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var5).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var11).color(var13, var14, var15, var16).endVertex();
      var0.pos(var7, var9, var11).color(var13, var14, var15, var16).endVertex();
   }

   private void markBlocksForUpdate(int var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
      this.viewFrustum.markBlocksForUpdate(var1, var2, var3, var4, var5, var6, var7);
   }

   public void notifyBlockUpdate(World var1, BlockPos var2, IBlockState var3, IBlockState var4, int var5) {
      int var6 = var2.getX();
      int var7 = var2.getY();
      int var8 = var2.getZ();
      this.markBlocksForUpdate(var6 - 1, var7 - 1, var8 - 1, var6 + 1, var7 + 1, var8 + 1, (var5 & 8) != 0);
   }

   public void notifyLightSet(BlockPos var1) {
      this.setLightUpdates.add(var1.toImmutable());
   }

   public void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.markBlocksForUpdate(var1 - 1, var2 - 1, var3 - 1, var4 + 1, var5 + 1, var6 + 1, false);
   }

   public void playRecord(@Nullable SoundEvent var1, BlockPos var2) {
      ISound var3 = (ISound)this.mapSoundPositions.get(var2);
      if (var3 != null) {
         this.mc.getSoundHandler().stopSound(var3);
         this.mapSoundPositions.remove(var2);
      }

      if (var1 != null) {
         ItemRecord var4 = ItemRecord.getBySound(var1);
         if (var4 != null) {
            this.mc.ingameGUI.setRecordPlayingMessage(var4.getRecordNameLocal());
         }

         PositionedSoundRecord var5 = PositionedSoundRecord.getRecordSoundRecord(var1, (float)var2.getX(), (float)var2.getY(), (float)var2.getZ());
         this.mapSoundPositions.put(var2, var5);
         this.mc.getSoundHandler().playSound(var5);
      }

   }

   public void playSoundToAllNearExcept(@Nullable EntityPlayer var1, SoundEvent var2, SoundCategory var3, double var4, double var6, double var8, float var10, float var11) {
   }

   public void spawnParticle(int var1, boolean var2, final double var3, final double var5, final double var7, double var9, double var11, double var13, int... var15) {
      try {
         this.spawnEntityFX(var1, var2, var3, var5, var7, var9, var11, var13, var15);
      } catch (Throwable var19) {
         CrashReport var17 = CrashReport.makeCrashReport(var19, "Exception while adding particle");
         CrashReportCategory var18 = var17.makeCategory("Particle being added");
         var18.addCrashSection("ID", Integer.valueOf(var1));
         if (var15 != null) {
            var18.addCrashSection("Parameters", var15);
         }

         var18.setDetail("Position", new ICrashReportDetail() {
            public String call() throws Exception {
               return CrashReportCategory.getCoordinateInfo(var3, var5, var7);
            }
         });
         throw new ReportedException(var17);
      }
   }

   private void spawnParticle(EnumParticleTypes var1, double var2, double var4, double var6, double var8, double var10, double var12, int... var14) {
      this.spawnParticle(var1.getParticleID(), var1.getShouldIgnoreRange(), var2, var4, var6, var8, var10, var12, var14);
   }

   @Nullable
   private Particle spawnEntityFX(int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
      Entity var16 = this.mc.getRenderViewEntity();
      if (this.mc != null && var16 != null && this.mc.effectRenderer != null) {
         int var17 = this.mc.gameSettings.particleSetting;
         if (var17 == 1 && this.world.rand.nextInt(3) == 0) {
            var17 = 2;
         }

         double var18 = var16.posX - var3;
         double var20 = var16.posY - var5;
         double var22 = var16.posZ - var7;
         return var2 ? this.mc.effectRenderer.spawnEffectParticle(var1, var3, var5, var7, var9, var11, var13, var15) : (var18 * var18 + var20 * var20 + var22 * var22 > 1024.0D ? null : (var17 > 1 ? null : this.mc.effectRenderer.spawnEffectParticle(var1, var3, var5, var7, var9, var11, var13, var15)));
      } else {
         return null;
      }
   }

   public void onEntityAdded(Entity var1) {
   }

   public void onEntityRemoved(Entity var1) {
   }

   public void deleteAllDisplayLists() {
   }

   public void broadcastSound(int var1, BlockPos var2, int var3) {
      switch(var1) {
      case 1023:
      case 1028:
         Entity var4 = this.mc.getRenderViewEntity();
         if (var4 != null) {
            double var5 = (double)var2.getX() - var4.posX;
            double var7 = (double)var2.getY() - var4.posY;
            double var9 = (double)var2.getZ() - var4.posZ;
            double var11 = Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
            double var13 = var4.posX;
            double var15 = var4.posY;
            double var17 = var4.posZ;
            if (var11 > 0.0D) {
               var13 += var5 / var11 * 2.0D;
               var15 += var7 / var11 * 2.0D;
               var17 += var9 / var11 * 2.0D;
            }

            if (var1 == 1023) {
               this.world.playSound(var13, var15, var17, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0F, 1.0F, false);
            } else {
               this.world.playSound(var13, var15, var17, SoundEvents.ENTITY_ENDERDRAGON_DEATH, SoundCategory.HOSTILE, 5.0F, 1.0F, false);
            }
         }
      default:
      }
   }

   public void playEvent(EntityPlayer var1, int var2, BlockPos var3, int var4) {
      Random var5 = this.world.rand;
      switch(var2) {
      case 1000:
         this.world.playSound(var3, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         break;
      case 1001:
         this.world.playSound(var3, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.2F, false);
         break;
      case 1002:
         this.world.playSound(var3, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0F, 1.2F, false);
         break;
      case 1003:
         this.world.playSound(var3, SoundEvents.ENTITY_ENDEREYE_LAUNCH, SoundCategory.NEUTRAL, 1.0F, 1.2F, false);
         break;
      case 1004:
         this.world.playSound(var3, SoundEvents.ENTITY_FIREWORK_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.2F, false);
         break;
      case 1005:
         this.world.playSound(var3, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1006:
         this.world.playSound(var3, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1007:
         this.world.playSound(var3, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1008:
         this.world.playSound(var3, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1009:
         this.world.playSound(var3, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (var5.nextFloat() - var5.nextFloat()) * 0.8F, false);
         break;
      case 1010:
         if (Item.getItemById(var4) instanceof ItemRecord) {
            this.world.playRecord(var3, ((ItemRecord)Item.getItemById(var4)).getSound());
         } else {
            this.world.playRecord(var3, (SoundEvent)null);
         }
         break;
      case 1011:
         this.world.playSound(var3, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1012:
         this.world.playSound(var3, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1013:
         this.world.playSound(var3, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1014:
         this.world.playSound(var3, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1015:
         this.world.playSound(var3, SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1016:
         this.world.playSound(var3, SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1017:
         this.world.playSound(var3, SoundEvents.ENTITY_ENDERDRAGON_SHOOT, SoundCategory.HOSTILE, 10.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1018:
         this.world.playSound(var3, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1019:
         this.world.playSound(var3, SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, SoundCategory.HOSTILE, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1020:
         this.world.playSound(var3, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.HOSTILE, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1021:
         this.world.playSound(var3, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.HOSTILE, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1022:
         this.world.playSound(var3, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1024:
         this.world.playSound(var3, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1025:
         this.world.playSound(var3, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 0.05F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1026:
         this.world.playSound(var3, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1027:
         this.world.playSound(var3, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.NEUTRAL, 2.0F, (var5.nextFloat() - var5.nextFloat()) * 0.2F + 1.0F, false);
         break;
      case 1029:
         this.world.playSound(var3, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1030:
         this.world.playSound(var3, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1031:
         this.world.playSound(var3, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.3F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1032:
         this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_PORTAL_TRAVEL, var5.nextFloat() * 0.4F + 0.8F));
         break;
      case 1033:
         this.world.playSound(var3, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         break;
      case 1034:
         this.world.playSound(var3, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         break;
      case 1035:
         this.world.playSound(var3, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         break;
      case 1036:
         this.world.playSound(var3, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 1037:
         this.world.playSound(var3, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 2000:
         int var6 = var4 % 3 - 1;
         int var7 = var4 / 3 % 3 - 1;
         double var8 = (double)var3.getX() + (double)var6 * 0.6D + 0.5D;
         double var10 = (double)var3.getY() + 0.5D;
         double var12 = (double)var3.getZ() + (double)var7 * 0.6D + 0.5D;

         for(int var43 = 0; var43 < 10; ++var43) {
            double var45 = var5.nextDouble() * 0.2D + 0.01D;
            double var46 = var8 + (double)var6 * 0.01D + (var5.nextDouble() - 0.5D) * (double)var7 * 0.5D;
            double var47 = var10 + (var5.nextDouble() - 0.5D) * 0.5D;
            double var49 = var12 + (double)var7 * 0.01D + (var5.nextDouble() - 0.5D) * (double)var6 * 0.5D;
            double var50 = (double)var6 * var45 + var5.nextGaussian() * 0.01D;
            double var51 = -0.03D + var5.nextGaussian() * 0.01D;
            double var53 = (double)var7 * var45 + var5.nextGaussian() * 0.01D;
            this.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var46, var47, var49, var50, var51, var53);
         }

         return;
      case 2001:
         Block var14 = Block.getBlockById(var4 & 4095);
         if (var14.getDefaultState().getMaterial() != Material.AIR) {
            SoundType var44 = var14.getSoundType(Block.getStateById(var4), this.world, var3, (Entity)null);
            this.world.playSound(var3, var44.getBreakSound(), SoundCategory.BLOCKS, (var44.getVolume() + 1.0F) / 2.0F, var44.getPitch() * 0.8F, false);
         }

         this.mc.effectRenderer.addBlockDestroyEffects(var3, var14.getStateFromMeta(var4 >> 12 & 255));
         break;
      case 2002:
         double var15 = (double)var3.getX();
         double var17 = (double)var3.getY();
         double var19 = (double)var3.getZ();

         for(int var21 = 0; var21 < 8; ++var21) {
            this.spawnParticle(EnumParticleTypes.ITEM_CRACK, var15, var17, var19, var5.nextGaussian() * 0.15D, var5.nextDouble() * 0.2D, var5.nextGaussian() * 0.15D, Item.getIdFromItem(Items.SPLASH_POTION));
         }

         PotionType var48 = PotionType.getPotionTypeForID(var4);
         int var22 = PotionUtils.getPotionColor(var48);
         float var23 = (float)(var22 >> 16 & 255) / 255.0F;
         float var24 = (float)(var22 >> 8 & 255) / 255.0F;
         float var25 = (float)(var22 >> 0 & 255) / 255.0F;
         EnumParticleTypes var26 = var48.hasInstantEffect() ? EnumParticleTypes.SPELL_INSTANT : EnumParticleTypes.SPELL;

         for(int var52 = 0; var52 < 100; ++var52) {
            double var28 = var5.nextDouble() * 4.0D;
            double var30 = var5.nextDouble() * 3.141592653589793D * 2.0D;
            double var32 = Math.cos(var30) * var28;
            double var58 = 0.01D + var5.nextDouble() * 0.5D;
            double var60 = Math.sin(var30) * var28;
            Particle var62 = this.spawnEntityFX(var26.getParticleID(), var26.getShouldIgnoreRange(), var15 + var32 * 0.1D, var17 + 0.3D, var19 + var60 * 0.1D, var32, var58, var60);
            if (var62 != null) {
               float var39 = 0.75F + var5.nextFloat() * 0.25F;
               var62.setRBGColorF(var23 * var39, var24 * var39, var25 * var39);
               var62.multiplyVelocity((float)var28);
            }
         }

         this.world.playSound(var3, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 2003:
         double var27 = (double)var3.getX() + 0.5D;
         double var29 = (double)var3.getY();
         double var31 = (double)var3.getZ() + 0.5D;

         for(int var55 = 0; var55 < 8; ++var55) {
            this.spawnParticle(EnumParticleTypes.ITEM_CRACK, var27, var29, var31, var5.nextGaussian() * 0.15D, var5.nextDouble() * 0.2D, var5.nextGaussian() * 0.15D, Item.getIdFromItem(Items.ENDER_EYE));
         }

         for(double var56 = 0.0D; var56 < 6.283185307179586D; var56 += 0.15707963267948966D) {
            this.spawnParticle(EnumParticleTypes.PORTAL, var27 + Math.cos(var56) * 5.0D, var29 - 0.4D, var31 + Math.sin(var56) * 5.0D, Math.cos(var56) * -5.0D, 0.0D, Math.sin(var56) * -5.0D);
            this.spawnParticle(EnumParticleTypes.PORTAL, var27 + Math.cos(var56) * 5.0D, var29 - 0.4D, var31 + Math.sin(var56) * 5.0D, Math.cos(var56) * -7.0D, 0.0D, Math.sin(var56) * -7.0D);
         }

         return;
      case 2004:
         for(int var54 = 0; var54 < 20; ++var54) {
            double var57 = (double)var3.getX() + 0.5D + ((double)this.world.rand.nextFloat() - 0.5D) * 2.0D;
            double var59 = (double)var3.getY() + 0.5D + ((double)this.world.rand.nextFloat() - 0.5D) * 2.0D;
            double var61 = (double)var3.getZ() + 0.5D + ((double)this.world.rand.nextFloat() - 0.5D) * 2.0D;
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var57, var59, var61, 0.0D, 0.0D, 0.0D, new int[0]);
            this.world.spawnParticle(EnumParticleTypes.FLAME, var57, var59, var61, 0.0D, 0.0D, 0.0D, new int[0]);
         }

         return;
      case 2005:
         ItemDye.spawnBonemealParticles(this.world, var3, var4);
         break;
      case 2006:
         for(int var33 = 0; var33 < 200; ++var33) {
            float var34 = var5.nextFloat() * 4.0F;
            float var35 = var5.nextFloat() * 6.2831855F;
            double var36 = (double)(MathHelper.cos(var35) * var34);
            double var38 = 0.01D + var5.nextDouble() * 0.5D;
            double var40 = (double)(MathHelper.sin(var35) * var34);
            Particle var42 = this.spawnEntityFX(EnumParticleTypes.DRAGON_BREATH.getParticleID(), false, (double)var3.getX() + var36 * 0.1D, (double)var3.getY() + 0.3D, (double)var3.getZ() + var40 * 0.1D, var36, var38, var40);
            if (var42 != null) {
               var42.multiplyVelocity(var34);
            }
         }

         this.world.playSound(var3, SoundEvents.ENTITY_ENDERDRAGON_FIREBALL_EPLD, SoundCategory.HOSTILE, 1.0F, this.world.rand.nextFloat() * 0.1F + 0.9F, false);
         break;
      case 3000:
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, true, (double)var3.getX() + 0.5D, (double)var3.getY() + 0.5D, (double)var3.getZ() + 0.5D, 0.0D, 0.0D, 0.0D, new int[0]);
         this.world.playSound(var3, SoundEvents.BLOCK_END_GATEWAY_SPAWN, SoundCategory.BLOCKS, 10.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F, false);
         break;
      case 3001:
         this.world.playSound(var3, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 64.0F, 0.8F + this.world.rand.nextFloat() * 0.3F, false);
      }

   }

   public void sendBlockBreakProgress(int var1, BlockPos var2, int var3) {
      if (var3 >= 0 && var3 < 10) {
         DestroyBlockProgress var4 = (DestroyBlockProgress)this.damagedBlocks.get(Integer.valueOf(var1));
         if (var4 == null || var4.getPosition().getX() != var2.getX() || var4.getPosition().getY() != var2.getY() || var4.getPosition().getZ() != var2.getZ()) {
            var4 = new DestroyBlockProgress(var1, var2);
            this.damagedBlocks.put(Integer.valueOf(var1), var4);
         }

         var4.setPartialBlockDamage(var3);
         var4.setCloudUpdateTick(this.cloudTickCounter);
      } else {
         this.damagedBlocks.remove(Integer.valueOf(var1));
      }

   }

   public boolean hasNoChunkUpdates() {
      return this.chunksToUpdate.isEmpty() && this.renderDispatcher.hasChunkUpdates();
   }

   public void setDisplayListEntitiesDirty() {
      this.displayListEntitiesDirty = true;
   }

   public void updateTileEntities(Collection var1, Collection var2) {
      synchronized(this.setTileEntities) {
         this.setTileEntities.removeAll(var1);
         this.setTileEntities.addAll(var2);
      }
   }

   @SideOnly(Side.CLIENT)
   class ContainerLocalRenderInformation {
      final RenderChunk renderChunk;
      final EnumFacing facing;
      byte setFacing;
      final int counter;

      private ContainerLocalRenderInformation(RenderChunk var2, EnumFacing var3, @Nullable int var4) {
         this.renderChunk = var2;
         this.facing = var3;
         this.counter = var4;
      }

      public void setDirection(byte var1, EnumFacing var2) {
         this.setFacing = (byte)(this.setFacing | var1 | 1 << var2.ordinal());
      }

      public boolean hasDirection(EnumFacing var1) {
         return (this.setFacing & 1 << var1.ordinal()) > 0;
      }
   }
}
