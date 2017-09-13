package net.minecraft.client.renderer;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gson.JsonSyntaxException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MouseFilter;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

@SideOnly(Side.CLIENT)
public class EntityRenderer implements IResourceManagerReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
   private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");
   public static boolean anaglyphEnable;
   public static int anaglyphField;
   private final Minecraft mc;
   private final IResourceManager resourceManager;
   private final Random random = new Random();
   private float farPlaneDistance;
   public final ItemRenderer itemRenderer;
   private final MapItemRenderer theMapItemRenderer;
   private int rendererUpdateCount;
   private Entity pointedEntity;
   private final MouseFilter mouseFilterXAxis = new MouseFilter();
   private final MouseFilter mouseFilterYAxis = new MouseFilter();
   private final float thirdPersonDistance = 4.0F;
   private float thirdPersonDistancePrev = 4.0F;
   private float smoothCamYaw;
   private float smoothCamPitch;
   private float smoothCamFilterX;
   private float smoothCamFilterY;
   private float smoothCamPartialTicks;
   private float fovModifierHand;
   private float fovModifierHandPrev;
   private float bossColorModifier;
   private float bossColorModifierPrev;
   private boolean cloudFog;
   private boolean renderHand = true;
   private boolean drawBlockOutline = true;
   private long timeWorldIcon;
   private long prevFrameTime = Minecraft.getSystemTime();
   private long renderEndNanoTime;
   private final DynamicTexture lightmapTexture;
   private final int[] lightmapColors;
   private final ResourceLocation locationLightMap;
   private boolean lightmapUpdateNeeded;
   private float torchFlickerX;
   private float torchFlickerDX;
   private int rainSoundCounter;
   private final float[] rainXCoords = new float[1024];
   private final float[] rainYCoords = new float[1024];
   private final FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
   private float fogColorRed;
   private float fogColorGreen;
   private float fogColorBlue;
   private float fogColor2;
   private float fogColor1;
   private int debugViewDirection;
   private boolean debugView;
   private double cameraZoom = 1.0D;
   private double cameraYaw;
   private double cameraPitch;
   private ShaderGroup theShaderGroup;
   private static final ResourceLocation[] SHADERS_TEXTURES = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
   public static final int SHADER_COUNT = SHADERS_TEXTURES.length;
   private int shaderIndex;
   private boolean useShader;
   private int frameCount;

   public EntityRenderer(Minecraft var1, IResourceManager var2) {
      this.shaderIndex = SHADER_COUNT;
      this.mc = var1;
      this.resourceManager = var2;
      this.itemRenderer = var1.getItemRenderer();
      this.theMapItemRenderer = new MapItemRenderer(var1.getTextureManager());
      this.lightmapTexture = new DynamicTexture(16, 16);
      this.locationLightMap = var1.getTextureManager().getDynamicTextureLocation("lightMap", this.lightmapTexture);
      this.lightmapColors = this.lightmapTexture.getTextureData();
      this.theShaderGroup = null;

      for(int var3 = 0; var3 < 32; ++var3) {
         for(int var4 = 0; var4 < 32; ++var4) {
            float var5 = (float)(var4 - 16);
            float var6 = (float)(var3 - 16);
            float var7 = MathHelper.sqrt(var5 * var5 + var6 * var6);
            this.rainXCoords[var3 << 5 | var4] = -var6 / var7;
            this.rainYCoords[var3 << 5 | var4] = var5 / var7;
         }
      }

   }

   public boolean isShaderActive() {
      return OpenGlHelper.shadersSupported && this.theShaderGroup != null;
   }

   public void stopUseShader() {
      if (this.theShaderGroup != null) {
         this.theShaderGroup.deleteShaderGroup();
      }

      this.theShaderGroup = null;
      this.shaderIndex = SHADER_COUNT;
   }

   public void switchUseShader() {
      this.useShader = !this.useShader;
   }

   public void loadEntityShader(Entity var1) {
      if (OpenGlHelper.shadersSupported) {
         if (this.theShaderGroup != null) {
            this.theShaderGroup.deleteShaderGroup();
         }

         this.theShaderGroup = null;
         if (var1 instanceof EntityCreeper) {
            this.loadShader(new ResourceLocation("shaders/post/creeper.json"));
         } else if (var1 instanceof EntitySpider) {
            this.loadShader(new ResourceLocation("shaders/post/spider.json"));
         } else if (var1 instanceof EntityEnderman) {
            this.loadShader(new ResourceLocation("shaders/post/invert.json"));
         } else {
            ForgeHooksClient.loadEntityShader(var1, this);
         }
      }

   }

   public void loadShader(ResourceLocation var1) {
      try {
         this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), this.resourceManager, this.mc.getFramebuffer(), var1);
         this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
         this.useShader = true;
      } catch (IOException var3) {
         LOGGER.warn("Failed to load shader: {}", new Object[]{var1, var3});
         this.shaderIndex = SHADER_COUNT;
         this.useShader = false;
      } catch (JsonSyntaxException var4) {
         LOGGER.warn("Failed to load shader: {}", new Object[]{var1, var4});
         this.shaderIndex = SHADER_COUNT;
         this.useShader = false;
      }

   }

   public void onResourceManagerReload(IResourceManager var1) {
      if (this.theShaderGroup != null) {
         this.theShaderGroup.deleteShaderGroup();
      }

      this.theShaderGroup = null;
      if (this.shaderIndex == SHADER_COUNT) {
         this.loadEntityShader(this.mc.getRenderViewEntity());
      } else {
         this.loadShader(SHADERS_TEXTURES[this.shaderIndex]);
      }

   }

   public void updateRenderer() {
      if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
         ShaderLinkHelper.setNewStaticShaderLinkHelper();
      }

      this.updateFovModifierHand();
      this.updateTorchFlicker();
      this.fogColor2 = this.fogColor1;
      this.thirdPersonDistancePrev = 4.0F;
      if (this.mc.gameSettings.smoothCamera) {
         float var1 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
         float var2 = var1 * var1 * var1 * 8.0F;
         this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * var2);
         this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * var2);
         this.smoothCamPartialTicks = 0.0F;
         this.smoothCamYaw = 0.0F;
         this.smoothCamPitch = 0.0F;
      } else {
         this.smoothCamFilterX = 0.0F;
         this.smoothCamFilterY = 0.0F;
         this.mouseFilterXAxis.reset();
         this.mouseFilterYAxis.reset();
      }

      if (this.mc.getRenderViewEntity() == null) {
         this.mc.setRenderViewEntity(this.mc.player);
      }

      float var4 = this.mc.world.getLightBrightness(new BlockPos(this.mc.getRenderViewEntity()));
      float var5 = (float)this.mc.gameSettings.renderDistanceChunks / 32.0F;
      float var3 = var4 * (1.0F - var5) + var5;
      this.fogColor1 += (var3 - this.fogColor1) * 0.1F;
      ++this.rendererUpdateCount;
      this.itemRenderer.updateEquippedItem();
      this.addRainParticles();
      this.bossColorModifierPrev = this.bossColorModifier;
      if (this.mc.ingameGUI.getBossOverlay().shouldDarkenSky()) {
         this.bossColorModifier += 0.05F;
         if (this.bossColorModifier > 1.0F) {
            this.bossColorModifier = 1.0F;
         }
      } else if (this.bossColorModifier > 0.0F) {
         this.bossColorModifier -= 0.0125F;
      }

   }

   public ShaderGroup getShaderGroup() {
      return this.theShaderGroup;
   }

   public void updateShaderGroupSize(int var1, int var2) {
      if (OpenGlHelper.shadersSupported) {
         if (this.theShaderGroup != null) {
            this.theShaderGroup.createBindFramebuffers(var1, var2);
         }

         this.mc.renderGlobal.createBindEntityOutlineFbs(var1, var2);
      }

   }

   public void getMouseOver(float var1) {
      Entity var2 = this.mc.getRenderViewEntity();
      if (var2 != null && this.mc.world != null) {
         this.mc.mcProfiler.startSection("pick");
         this.mc.pointedEntity = null;
         double var3 = (double)this.mc.playerController.getBlockReachDistance();
         this.mc.objectMouseOver = var2.rayTrace(var3, var1);
         Vec3d var5 = var2.getPositionEyes(var1);
         boolean var6 = false;
         boolean var7 = true;
         double var8 = var3;
         if (this.mc.playerController.extendedReach()) {
            var8 = 6.0D;
            var3 = var8;
         } else if (var3 > 3.0D) {
            var6 = true;
         }

         if (this.mc.objectMouseOver != null) {
            var8 = this.mc.objectMouseOver.hitVec.distanceTo(var5);
         }

         Vec3d var10 = var2.getLook(var1);
         Vec3d var11 = var5.addVector(var10.xCoord * var3, var10.yCoord * var3, var10.zCoord * var3);
         this.pointedEntity = null;
         Vec3d var12 = null;
         float var13 = 1.0F;
         List var14 = this.mc.world.getEntitiesInAABBexcluding(var2, var2.getEntityBoundingBox().addCoord(var10.xCoord * var3, var10.yCoord * var3, var10.zCoord * var3).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               return var1 != null && var1.canBeCollidedWith();
            }
         }));
         double var15 = var8;

         for(int var17 = 0; var17 < var14.size(); ++var17) {
            Entity var18 = (Entity)var14.get(var17);
            AxisAlignedBB var19 = var18.getEntityBoundingBox().expandXyz((double)var18.getCollisionBorderSize());
            RayTraceResult var20 = var19.calculateIntercept(var5, var11);
            if (var19.isVecInside(var5)) {
               if (var15 >= 0.0D) {
                  this.pointedEntity = var18;
                  var12 = var20 == null ? var5 : var20.hitVec;
                  var15 = 0.0D;
               }
            } else if (var20 != null) {
               double var21 = var5.distanceTo(var20.hitVec);
               if (var21 < var15 || var15 == 0.0D) {
                  if (var18.getLowestRidingEntity() == var2.getLowestRidingEntity() && !var2.canRiderInteract()) {
                     if (var15 == 0.0D) {
                        this.pointedEntity = var18;
                        var12 = var20.hitVec;
                     }
                  } else {
                     this.pointedEntity = var18;
                     var12 = var20.hitVec;
                     var15 = var21;
                  }
               }
            }
         }

         if (this.pointedEntity != null && var6 && var5.distanceTo(var12) > 3.0D) {
            this.pointedEntity = null;
            this.mc.objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, var12, (EnumFacing)null, new BlockPos(var12));
         }

         if (this.pointedEntity != null && (var15 < var8 || this.mc.objectMouseOver == null)) {
            this.mc.objectMouseOver = new RayTraceResult(this.pointedEntity, var12);
            if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
               this.mc.pointedEntity = this.pointedEntity;
            }
         }

         this.mc.mcProfiler.endSection();
      }

   }

   private void updateFovModifierHand() {
      float var1 = 1.0F;
      if (this.mc.getRenderViewEntity() instanceof AbstractClientPlayer) {
         AbstractClientPlayer var2 = (AbstractClientPlayer)this.mc.getRenderViewEntity();
         var1 = var2.getFovModifier();
      }

      this.fovModifierHandPrev = this.fovModifierHand;
      this.fovModifierHand += (var1 - this.fovModifierHand) * 0.5F;
      if (this.fovModifierHand > 1.5F) {
         this.fovModifierHand = 1.5F;
      }

      if (this.fovModifierHand < 0.1F) {
         this.fovModifierHand = 0.1F;
      }

   }

   private float getFOVModifier(float var1, boolean var2) {
      if (this.debugView) {
         return 90.0F;
      } else {
         Entity var3 = this.mc.getRenderViewEntity();
         float var4 = 70.0F;
         if (var2) {
            var4 = this.mc.gameSettings.fovSetting;
            var4 = var4 * (this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * var1);
         }

         if (var3 instanceof EntityLivingBase && ((EntityLivingBase)var3).getHealth() <= 0.0F) {
            float var5 = (float)((EntityLivingBase)var3).deathTime + var1;
            var4 /= (1.0F - 500.0F / (var5 + 500.0F)) * 2.0F + 1.0F;
         }

         IBlockState var7 = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, var3, var1);
         if (var7.getMaterial() == Material.WATER) {
            var4 = var4 * 60.0F / 70.0F;
         }

         return ForgeHooksClient.getFOVModifier(this, var3, var7, (double)var1, var4);
      }
   }

   private void hurtCameraEffect(float var1) {
      if (this.mc.getRenderViewEntity() instanceof EntityLivingBase) {
         EntityLivingBase var2 = (EntityLivingBase)this.mc.getRenderViewEntity();
         float var3 = (float)var2.hurtTime - var1;
         if (var2.getHealth() <= 0.0F) {
            float var4 = (float)var2.deathTime + var1;
            GlStateManager.rotate(40.0F - 8000.0F / (var4 + 200.0F), 0.0F, 0.0F, 1.0F);
         }

         if (var3 < 0.0F) {
            return;
         }

         var3 = var3 / (float)var2.maxHurtTime;
         var3 = MathHelper.sin(var3 * var3 * var3 * var3 * 3.1415927F);
         float var7 = var2.attackedAtYaw;
         GlStateManager.rotate(-var7, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(-var3 * 14.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(var7, 0.0F, 1.0F, 0.0F);
      }

   }

   private void applyBobbing(float var1) {
      if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
         EntityPlayer var2 = (EntityPlayer)this.mc.getRenderViewEntity();
         float var3 = var2.distanceWalkedModified - var2.prevDistanceWalkedModified;
         float var4 = -(var2.distanceWalkedModified + var3 * var1);
         float var5 = var2.prevCameraYaw + (var2.cameraYaw - var2.prevCameraYaw) * var1;
         float var6 = var2.prevCameraPitch + (var2.cameraPitch - var2.prevCameraPitch) * var1;
         GlStateManager.translate(MathHelper.sin(var4 * 3.1415927F) * var5 * 0.5F, -Math.abs(MathHelper.cos(var4 * 3.1415927F) * var5), 0.0F);
         GlStateManager.rotate(MathHelper.sin(var4 * 3.1415927F) * var5 * 3.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(Math.abs(MathHelper.cos(var4 * 3.1415927F - 0.2F) * var5) * 5.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(var6, 1.0F, 0.0F, 0.0F);
      }

   }

   private void orientCamera(float var1) {
      Entity var2 = this.mc.getRenderViewEntity();
      float var3 = var2.getEyeHeight();
      double var4 = var2.prevPosX + (var2.posX - var2.prevPosX) * (double)var1;
      double var6 = var2.prevPosY + (var2.posY - var2.prevPosY) * (double)var1 + (double)var3;
      double var8 = var2.prevPosZ + (var2.posZ - var2.prevPosZ) * (double)var1;
      if (var2 instanceof EntityLivingBase && ((EntityLivingBase)var2).isPlayerSleeping()) {
         var3 = (float)((double)var3 + 1.0D);
         GlStateManager.translate(0.0F, 0.3F, 0.0F);
         if (!this.mc.gameSettings.debugCamEnable) {
            BlockPos var30 = new BlockPos(var2);
            IBlockState var11 = this.mc.world.getBlockState(var30);
            ForgeHooksClient.orientBedCamera(this.mc.world, var30, var11, var2);
            GlStateManager.rotate(var2.prevRotationYaw + (var2.rotationYaw - var2.prevRotationYaw) * var1 + 180.0F, 0.0F, -1.0F, 0.0F);
            GlStateManager.rotate(var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var1, -1.0F, 0.0F, 0.0F);
         }
      } else if (this.mc.gameSettings.thirdPersonView > 0) {
         double var10 = (double)(this.thirdPersonDistancePrev + (4.0F - this.thirdPersonDistancePrev) * var1);
         if (this.mc.gameSettings.debugCamEnable) {
            GlStateManager.translate(0.0F, 0.0F, (float)(-var10));
         } else {
            float var12 = var2.rotationYaw;
            float var13 = var2.rotationPitch;
            if (this.mc.gameSettings.thirdPersonView == 2) {
               var13 += 180.0F;
            }

            double var14 = (double)(-MathHelper.sin(var12 * 0.017453292F) * MathHelper.cos(var13 * 0.017453292F)) * var10;
            double var16 = (double)(MathHelper.cos(var12 * 0.017453292F) * MathHelper.cos(var13 * 0.017453292F)) * var10;
            double var18 = (double)(-MathHelper.sin(var13 * 0.017453292F)) * var10;

            for(int var20 = 0; var20 < 8; ++var20) {
               float var21 = (float)((var20 & 1) * 2 - 1);
               float var22 = (float)((var20 >> 1 & 1) * 2 - 1);
               float var23 = (float)((var20 >> 2 & 1) * 2 - 1);
               var21 = var21 * 0.1F;
               var22 = var22 * 0.1F;
               var23 = var23 * 0.1F;
               RayTraceResult var24 = this.mc.world.rayTraceBlocks(new Vec3d(var4 + (double)var21, var6 + (double)var22, var8 + (double)var23), new Vec3d(var4 - var14 + (double)var21 + (double)var23, var6 - var18 + (double)var22, var8 - var16 + (double)var23));
               if (var24 != null) {
                  double var25 = var24.hitVec.distanceTo(new Vec3d(var4, var6, var8));
                  if (var25 < var10) {
                     var10 = var25;
                  }
               }
            }

            if (this.mc.gameSettings.thirdPersonView == 2) {
               GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.rotate(var2.rotationPitch - var13, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(var2.rotationYaw - var12, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.0F, (float)(-var10));
            GlStateManager.rotate(var12 - var2.rotationYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(var13 - var2.rotationPitch, 1.0F, 0.0F, 0.0F);
         }
      } else {
         GlStateManager.translate(0.0F, 0.0F, 0.05F);
      }

      if (!this.mc.gameSettings.debugCamEnable) {
         float var31 = var2.prevRotationYaw + (var2.rotationYaw - var2.prevRotationYaw) * var1 + 180.0F;
         float var32 = var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var1;
         float var33 = 0.0F;
         if (var2 instanceof EntityAnimal) {
            EntityAnimal var34 = (EntityAnimal)var2;
            var31 = var34.prevRotationYawHead + (var34.rotationYawHead - var34.prevRotationYawHead) * var1 + 180.0F;
         }

         IBlockState var35 = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, var2, var1);
         CameraSetup var36 = new CameraSetup(this, var2, var35, (double)var1, var31, var32, var33);
         MinecraftForge.EVENT_BUS.post(var36);
         GlStateManager.rotate(var36.getRoll(), 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(var36.getPitch(), 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(var36.getYaw(), 0.0F, 1.0F, 0.0F);
      }

      GlStateManager.translate(0.0F, -var3, 0.0F);
      var4 = var2.prevPosX + (var2.posX - var2.prevPosX) * (double)var1;
      var6 = var2.prevPosY + (var2.posY - var2.prevPosY) * (double)var1 + (double)var3;
      var8 = var2.prevPosZ + (var2.posZ - var2.prevPosZ) * (double)var1;
      this.cloudFog = this.mc.renderGlobal.hasCloudFog(var4, var6, var8, var1);
   }

   private void setupCameraTransform(float var1, int var2) {
      this.farPlaneDistance = (float)(this.mc.gameSettings.renderDistanceChunks * 16);
      GlStateManager.matrixMode(5889);
      GlStateManager.loadIdentity();
      float var3 = 0.07F;
      if (this.mc.gameSettings.anaglyph) {
         GlStateManager.translate((float)(-(var2 * 2 - 1)) * 0.07F, 0.0F, 0.0F);
      }

      if (this.cameraZoom != 1.0D) {
         GlStateManager.translate((float)this.cameraYaw, (float)(-this.cameraPitch), 0.0F);
         GlStateManager.scale(this.cameraZoom, this.cameraZoom, 1.0D);
      }

      Project.gluPerspective(this.getFOVModifier(var1, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * MathHelper.SQRT_2);
      GlStateManager.matrixMode(5888);
      GlStateManager.loadIdentity();
      if (this.mc.gameSettings.anaglyph) {
         GlStateManager.translate((float)(var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
      }

      this.hurtCameraEffect(var1);
      if (this.mc.gameSettings.viewBobbing) {
         this.applyBobbing(var1);
      }

      float var4 = this.mc.player.prevTimeInPortal + (this.mc.player.timeInPortal - this.mc.player.prevTimeInPortal) * var1;
      if (var4 > 0.0F) {
         byte var5 = 20;
         if (this.mc.player.isPotionActive(MobEffects.NAUSEA)) {
            var5 = 7;
         }

         float var6 = 5.0F / (var4 * var4 + 5.0F) - var4 * 0.04F;
         var6 = var6 * var6;
         GlStateManager.rotate(((float)this.rendererUpdateCount + var1) * (float)var5, 0.0F, 1.0F, 1.0F);
         GlStateManager.scale(1.0F / var6, 1.0F, 1.0F);
         GlStateManager.rotate(-((float)this.rendererUpdateCount + var1) * (float)var5, 0.0F, 1.0F, 1.0F);
      }

      this.orientCamera(var1);
      if (this.debugView) {
         switch(this.debugViewDirection) {
         case 0:
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 1:
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 2:
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 3:
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            break;
         case 4:
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
         }
      }

   }

   private void renderHand(float var1, int var2) {
      if (!this.debugView) {
         GlStateManager.matrixMode(5889);
         GlStateManager.loadIdentity();
         float var3 = 0.07F;
         if (this.mc.gameSettings.anaglyph) {
            GlStateManager.translate((float)(-(var2 * 2 - 1)) * 0.07F, 0.0F, 0.0F);
         }

         Project.gluPerspective(this.getFOVModifier(var1, false), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
         GlStateManager.matrixMode(5888);
         GlStateManager.loadIdentity();
         if (this.mc.gameSettings.anaglyph) {
            GlStateManager.translate((float)(var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
         }

         GlStateManager.pushMatrix();
         this.hurtCameraEffect(var1);
         if (this.mc.gameSettings.viewBobbing) {
            this.applyBobbing(var1);
         }

         boolean var4 = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping();
         if (!ForgeHooksClient.renderFirstPersonHand(this.mc.renderGlobal, var1, var2) && this.mc.gameSettings.thirdPersonView == 0 && !var4 && !this.mc.gameSettings.hideGUI && !this.mc.playerController.isSpectator()) {
            this.enableLightmap();
            this.itemRenderer.renderItemInFirstPerson(var1);
            this.disableLightmap();
         }

         GlStateManager.popMatrix();
         if (this.mc.gameSettings.thirdPersonView == 0 && !var4) {
            this.itemRenderer.renderOverlays(var1);
            this.hurtCameraEffect(var1);
         }

         if (this.mc.gameSettings.viewBobbing) {
            this.applyBobbing(var1);
         }
      }

   }

   public void disableLightmap() {
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   public void enableLightmap() {
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.matrixMode(5890);
      GlStateManager.loadIdentity();
      float var1 = 0.00390625F;
      GlStateManager.scale(0.00390625F, 0.00390625F, 0.00390625F);
      GlStateManager.translate(8.0F, 8.0F, 8.0F);
      GlStateManager.matrixMode(5888);
      this.mc.getTextureManager().bindTexture(this.locationLightMap);
      GlStateManager.glTexParameteri(3553, 10241, 9729);
      GlStateManager.glTexParameteri(3553, 10240, 9729);
      GlStateManager.glTexParameteri(3553, 10242, 10496);
      GlStateManager.glTexParameteri(3553, 10243, 10496);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   private void updateTorchFlicker() {
      this.torchFlickerDX = (float)((double)this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
      this.torchFlickerDX = (float)((double)this.torchFlickerDX * 0.9D);
      this.torchFlickerX += this.torchFlickerDX - this.torchFlickerX;
      this.lightmapUpdateNeeded = true;
   }

   private void updateLightmap(float var1) {
      if (this.lightmapUpdateNeeded) {
         this.mc.mcProfiler.startSection("lightTex");
         WorldClient var2 = this.mc.world;
         if (var2 != null) {
            float var3 = var2.getSunBrightness(1.0F);
            float var4 = var3 * 0.95F + 0.05F;

            for(int var5 = 0; var5 < 256; ++var5) {
               float var6 = var2.provider.getLightBrightnessTable()[var5 / 16] * var4;
               float var7 = var2.provider.getLightBrightnessTable()[var5 % 16] * (this.torchFlickerX * 0.1F + 1.5F);
               if (var2.getLastLightningBolt() > 0) {
                  var6 = var2.provider.getLightBrightnessTable()[var5 / 16];
               }

               float var8 = var6 * (var3 * 0.65F + 0.35F);
               float var9 = var6 * (var3 * 0.65F + 0.35F);
               float var10 = var7 * ((var7 * 0.6F + 0.4F) * 0.6F + 0.4F);
               float var11 = var7 * (var7 * var7 * 0.6F + 0.4F);
               float var12 = var8 + var7;
               float var13 = var9 + var10;
               float var14 = var6 + var11;
               var12 = var12 * 0.96F + 0.03F;
               var13 = var13 * 0.96F + 0.03F;
               var14 = var14 * 0.96F + 0.03F;
               if (this.bossColorModifier > 0.0F) {
                  float var15 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * var1;
                  var12 = var12 * (1.0F - var15) + var12 * 0.7F * var15;
                  var13 = var13 * (1.0F - var15) + var13 * 0.6F * var15;
                  var14 = var14 * (1.0F - var15) + var14 * 0.6F * var15;
               }

               if (var2.provider.getDimensionType().getId() == 1) {
                  var12 = 0.22F + var7 * 0.75F;
                  var13 = 0.28F + var10 * 0.75F;
                  var14 = 0.25F + var11 * 0.75F;
               }

               if (this.mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                  float var32 = this.getNightVisionBrightness(this.mc.player, var1);
                  float var16 = 1.0F / var12;
                  if (var16 > 1.0F / var13) {
                     var16 = 1.0F / var13;
                  }

                  if (var16 > 1.0F / var14) {
                     var16 = 1.0F / var14;
                  }

                  var12 = var12 * (1.0F - var32) + var12 * var16 * var32;
                  var13 = var13 * (1.0F - var32) + var13 * var16 * var32;
                  var14 = var14 * (1.0F - var32) + var14 * var16 * var32;
               }

               if (var12 > 1.0F) {
                  var12 = 1.0F;
               }

               if (var13 > 1.0F) {
                  var13 = 1.0F;
               }

               if (var14 > 1.0F) {
                  var14 = 1.0F;
               }

               float var33 = this.mc.gameSettings.gammaSetting;
               float var34 = 1.0F - var12;
               float var17 = 1.0F - var13;
               float var18 = 1.0F - var14;
               var34 = 1.0F - var34 * var34 * var34 * var34;
               var17 = 1.0F - var17 * var17 * var17 * var17;
               var18 = 1.0F - var18 * var18 * var18 * var18;
               var12 = var12 * (1.0F - var33) + var34 * var33;
               var13 = var13 * (1.0F - var33) + var17 * var33;
               var14 = var14 * (1.0F - var33) + var18 * var33;
               var12 = var12 * 0.96F + 0.03F;
               var13 = var13 * 0.96F + 0.03F;
               var14 = var14 * 0.96F + 0.03F;
               if (var12 > 1.0F) {
                  var12 = 1.0F;
               }

               if (var13 > 1.0F) {
                  var13 = 1.0F;
               }

               if (var14 > 1.0F) {
                  var14 = 1.0F;
               }

               if (var12 < 0.0F) {
                  var12 = 0.0F;
               }

               if (var13 < 0.0F) {
                  var13 = 0.0F;
               }

               if (var14 < 0.0F) {
                  var14 = 0.0F;
               }

               boolean var19 = true;
               int var20 = (int)(var12 * 255.0F);
               int var21 = (int)(var13 * 255.0F);
               int var22 = (int)(var14 * 255.0F);
               this.lightmapColors[var5] = -16777216 | var20 << 16 | var21 << 8 | var22;
            }

            this.lightmapTexture.updateDynamicTexture();
            this.lightmapUpdateNeeded = false;
            this.mc.mcProfiler.endSection();
         }
      }

   }

   private float getNightVisionBrightness(EntityLivingBase var1, float var2) {
      int var3 = var1.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
      return var3 > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)var3 - var2) * 3.1415927F * 0.2F) * 0.3F;
   }

   public void updateCameraAndRender(float var1, long var2) {
      boolean var4 = Display.isActive();
      if (!var4 && this.mc.gameSettings.pauseOnLostFocus && (!this.mc.gameSettings.touchscreen || !Mouse.isButtonDown(1))) {
         if (Minecraft.getSystemTime() - this.prevFrameTime > 500L) {
            this.mc.displayInGameMenu();
         }
      } else {
         this.prevFrameTime = Minecraft.getSystemTime();
      }

      this.mc.mcProfiler.startSection("mouse");
      if (var4 && Minecraft.IS_RUNNING_ON_MAC && this.mc.inGameHasFocus && !Mouse.isInsideWindow()) {
         Mouse.setGrabbed(false);
         Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2 - 20);
         Mouse.setGrabbed(true);
      }

      if (this.mc.inGameHasFocus && var4) {
         this.mc.mouseHelper.mouseXYChange();
         float var5 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
         float var6 = var5 * var5 * var5 * 8.0F;
         float var7 = (float)this.mc.mouseHelper.deltaX * var6;
         float var8 = (float)this.mc.mouseHelper.deltaY * var6;
         byte var9 = 1;
         if (this.mc.gameSettings.invertMouse) {
            var9 = -1;
         }

         if (this.mc.gameSettings.smoothCamera) {
            this.smoothCamYaw += var7;
            this.smoothCamPitch += var8;
            float var10 = var1 - this.smoothCamPartialTicks;
            this.smoothCamPartialTicks = var1;
            var7 = this.smoothCamFilterX * var10;
            var8 = this.smoothCamFilterY * var10;
            this.mc.player.turn(var7, var8 * (float)var9);
         } else {
            this.smoothCamYaw = 0.0F;
            this.smoothCamPitch = 0.0F;
            this.mc.player.turn(var7, var8 * (float)var9);
         }
      }

      this.mc.mcProfiler.endSection();
      if (!this.mc.skipRenderWorld) {
         anaglyphEnable = this.mc.gameSettings.anaglyph;
         final ScaledResolution var17 = new ScaledResolution(this.mc);
         int var18 = var17.getScaledWidth();
         int var20 = var17.getScaledHeight();
         final int var22 = Mouse.getX() * var18 / this.mc.displayWidth;
         final int var23 = var20 - Mouse.getY() * var20 / this.mc.displayHeight - 1;
         int var24 = this.mc.gameSettings.limitFramerate;
         if (this.mc.world == null) {
            GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            this.setupOverlayRendering();
            this.renderEndNanoTime = System.nanoTime();
         } else {
            this.mc.mcProfiler.startSection("level");
            int var11 = Math.min(Minecraft.getDebugFPS(), var24);
            var11 = Math.max(var11, 60);
            long var12 = System.nanoTime() - var2;
            long var14 = Math.max((long)(1000000000 / var11 / 4) - var12, 0L);
            this.renderWorld(var1, System.nanoTime() + var14);
            if (this.mc.isSingleplayer() && this.timeWorldIcon < Minecraft.getSystemTime() - 1000L) {
               this.timeWorldIcon = Minecraft.getSystemTime();
               if (!this.mc.getIntegratedServer().isWorldIconSet()) {
                  this.createWorldIcon();
               }
            }

            if (OpenGlHelper.shadersSupported) {
               this.mc.renderGlobal.renderEntityOutlineFramebuffer();
               if (this.theShaderGroup != null && this.useShader) {
                  GlStateManager.matrixMode(5890);
                  GlStateManager.pushMatrix();
                  GlStateManager.loadIdentity();
                  this.theShaderGroup.loadShaderGroup(var1);
                  GlStateManager.popMatrix();
               }

               this.mc.getFramebuffer().bindFramebuffer(true);
            }

            this.renderEndNanoTime = System.nanoTime();
            this.mc.mcProfiler.endStartSection("gui");
            if (!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null) {
               GlStateManager.alphaFunc(516, 0.1F);
               this.mc.ingameGUI.renderGameOverlay(var1);
            }

            this.mc.mcProfiler.endSection();
         }

         if (this.mc.currentScreen != null) {
            GlStateManager.clear(256);

            try {
               ForgeHooksClient.drawScreen(this.mc.currentScreen, var22, var23, var1);
            } catch (Throwable var16) {
               CrashReport var26 = CrashReport.makeCrashReport(var16, "Rendering screen");
               CrashReportCategory var13 = var26.makeCategory("Screen render details");
               var13.setDetail("Screen name", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return EntityRenderer.this.mc.currentScreen.getClass().getCanonicalName();
                  }
               });
               var13.setDetail("Mouse location", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return String.format("Scaled: (%d, %d). Absolute: (%d, %d)", var22, var23, Mouse.getX(), Mouse.getY());
                  }
               });
               var13.setDetail("Screen size", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", var17.getScaledWidth(), var17.getScaledHeight(), EntityRenderer.this.mc.displayWidth, EntityRenderer.this.mc.displayHeight, var17.getScaleFactor());
                  }
               });
               throw new ReportedException(var26);
            }
         }
      }

   }

   private void createWorldIcon() {
      if (this.mc.renderGlobal.getRenderedChunks() > 10 && this.mc.renderGlobal.hasNoChunkUpdates() && !this.mc.getIntegratedServer().isWorldIconSet()) {
         BufferedImage var1 = ScreenShotHelper.createScreenshot(this.mc.displayWidth, this.mc.displayHeight, this.mc.getFramebuffer());
         int var2 = var1.getWidth();
         int var3 = var1.getHeight();
         int var4 = 0;
         int var5 = 0;
         if (var2 > var3) {
            var4 = (var2 - var3) / 2;
            var2 = var3;
         } else {
            var5 = (var3 - var2) / 2;
         }

         try {
            BufferedImage var6 = new BufferedImage(64, 64, 1);
            Graphics2D var7 = var6.createGraphics();
            var7.drawImage(var1, 0, 0, 64, 64, var4, var5, var4 + var2, var5 + var2, (ImageObserver)null);
            var7.dispose();
            ImageIO.write(var6, "png", this.mc.getIntegratedServer().getWorldIconFile());
         } catch (IOException var8) {
            LOGGER.warn("Couldn't save auto screenshot", var8);
         }
      }

   }

   public void renderStreamIndicator(float var1) {
      this.setupOverlayRendering();
   }

   private boolean isDrawBlockOutline() {
      if (!this.drawBlockOutline) {
         return false;
      } else {
         Entity var1 = this.mc.getRenderViewEntity();
         boolean var2 = var1 instanceof EntityPlayer && !this.mc.gameSettings.hideGUI;
         if (var2 && !((EntityPlayer)var1).capabilities.allowEdit) {
            ItemStack var3 = ((EntityPlayer)var1).getHeldItemMainhand();
            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
               BlockPos var4 = this.mc.objectMouseOver.getBlockPos();
               Block var5 = this.mc.world.getBlockState(var4).getBlock();
               if (this.mc.playerController.getCurrentGameType() == GameType.SPECTATOR) {
                  var2 = var5.hasTileEntity(this.mc.world.getBlockState(var4)) && this.mc.world.getTileEntity(var4) instanceof IInventory;
               } else {
                  var2 = var3 != null && (var3.canDestroy(var5) || var3.canPlaceOn(var5));
               }
            }
         }

         return var2;
      }
   }

   public void renderWorld(float var1, long var2) {
      this.updateLightmap(var1);
      if (this.mc.getRenderViewEntity() == null) {
         this.mc.setRenderViewEntity(this.mc.player);
      }

      this.getMouseOver(var1);
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.alphaFunc(516, 0.5F);
      this.mc.mcProfiler.startSection("center");
      if (this.mc.gameSettings.anaglyph) {
         anaglyphField = 0;
         GlStateManager.colorMask(false, true, true, false);
         this.renderWorldPass(0, var1, var2);
         anaglyphField = 1;
         GlStateManager.colorMask(true, false, false, false);
         this.renderWorldPass(1, var1, var2);
         GlStateManager.colorMask(true, true, true, false);
      } else {
         this.renderWorldPass(2, var1, var2);
      }

      this.mc.mcProfiler.endSection();
   }

   private void renderWorldPass(int var1, float var2, long var3) {
      RenderGlobal var5 = this.mc.renderGlobal;
      ParticleManager var6 = this.mc.effectRenderer;
      boolean var7 = this.isDrawBlockOutline();
      GlStateManager.enableCull();
      this.mc.mcProfiler.endStartSection("clear");
      GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
      this.updateFogColor(var2);
      GlStateManager.clear(16640);
      this.mc.mcProfiler.endStartSection("camera");
      this.setupCameraTransform(var2, var1);
      ActiveRenderInfo.updateRenderInfo(this.mc.player, this.mc.gameSettings.thirdPersonView == 2);
      this.mc.mcProfiler.endStartSection("frustum");
      ClippingHelperImpl.getInstance();
      this.mc.mcProfiler.endStartSection("culling");
      Frustum var8 = new Frustum();
      Entity var9 = this.mc.getRenderViewEntity();
      double var10 = var9.lastTickPosX + (var9.posX - var9.lastTickPosX) * (double)var2;
      double var12 = var9.lastTickPosY + (var9.posY - var9.lastTickPosY) * (double)var2;
      double var14 = var9.lastTickPosZ + (var9.posZ - var9.lastTickPosZ) * (double)var2;
      var8.setPosition(var10, var12, var14);
      if (this.mc.gameSettings.renderDistanceChunks >= 4) {
         this.setupFog(-1, var2);
         this.mc.mcProfiler.endStartSection("sky");
         GlStateManager.matrixMode(5889);
         GlStateManager.loadIdentity();
         Project.gluPerspective(this.getFOVModifier(var2, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
         GlStateManager.matrixMode(5888);
         var5.renderSky(var2, var1);
         GlStateManager.matrixMode(5889);
         GlStateManager.loadIdentity();
         Project.gluPerspective(this.getFOVModifier(var2, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * MathHelper.SQRT_2);
         GlStateManager.matrixMode(5888);
      }

      this.setupFog(0, var2);
      GlStateManager.shadeModel(7425);
      if (var9.posY + (double)var9.getEyeHeight() < 128.0D) {
         this.renderCloudsCheck(var5, var2, var1);
      }

      this.mc.mcProfiler.endStartSection("prepareterrain");
      this.setupFog(0, var2);
      this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      RenderHelper.disableStandardItemLighting();
      this.mc.mcProfiler.endStartSection("terrain_setup");
      var5.setupTerrain(var9, (double)var2, var8, this.frameCount++, this.mc.player.isSpectator());
      if (var1 == 0 || var1 == 2) {
         this.mc.mcProfiler.endStartSection("updatechunks");
         this.mc.renderGlobal.updateChunks(var3);
      }

      this.mc.mcProfiler.endStartSection("terrain");
      GlStateManager.matrixMode(5888);
      GlStateManager.pushMatrix();
      GlStateManager.disableAlpha();
      var5.renderBlockLayer(BlockRenderLayer.SOLID, (double)var2, var1, var9);
      GlStateManager.enableAlpha();
      var5.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, (double)var2, var1, var9);
      this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
      var5.renderBlockLayer(BlockRenderLayer.CUTOUT, (double)var2, var1, var9);
      this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
      GlStateManager.shadeModel(7424);
      GlStateManager.alphaFunc(516, 0.1F);
      if (!this.debugView) {
         GlStateManager.matrixMode(5888);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         RenderHelper.enableStandardItemLighting();
         this.mc.mcProfiler.endStartSection("entities");
         ForgeHooksClient.setRenderPass(0);
         var5.renderEntities(var9, var8, var2);
         ForgeHooksClient.setRenderPass(0);
         RenderHelper.disableStandardItemLighting();
         this.disableLightmap();
      }

      GlStateManager.matrixMode(5888);
      GlStateManager.popMatrix();
      if (var7 && this.mc.objectMouseOver != null && !var9.isInsideOfMaterial(Material.WATER)) {
         EntityPlayer var16 = (EntityPlayer)var9;
         GlStateManager.disableAlpha();
         this.mc.mcProfiler.endStartSection("outline");
         if (!ForgeHooksClient.onDrawBlockHighlight(var5, var16, this.mc.objectMouseOver, 0, var2)) {
            var5.drawSelectionBox(var16, this.mc.objectMouseOver, 0, var2);
         }

         GlStateManager.enableAlpha();
      }

      if (this.mc.debugRenderer.shouldRender()) {
         this.mc.debugRenderer.renderDebug(var2, var3);
      }

      this.mc.mcProfiler.endStartSection("destroyProgress");
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
      var5.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), var9, var2);
      this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
      GlStateManager.disableBlend();
      if (!this.debugView) {
         this.enableLightmap();
         this.mc.mcProfiler.endStartSection("litParticles");
         var6.renderLitParticles(var9, var2);
         RenderHelper.disableStandardItemLighting();
         this.setupFog(0, var2);
         this.mc.mcProfiler.endStartSection("particles");
         var6.renderParticles(var9, var2);
         this.disableLightmap();
      }

      GlStateManager.depthMask(false);
      GlStateManager.enableCull();
      this.mc.mcProfiler.endStartSection("weather");
      this.renderRainSnow(var2);
      GlStateManager.depthMask(true);
      var5.renderWorldBorder(var9, var2);
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.alphaFunc(516, 0.1F);
      this.setupFog(0, var2);
      GlStateManager.enableBlend();
      GlStateManager.depthMask(false);
      this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      GlStateManager.shadeModel(7425);
      this.mc.mcProfiler.endStartSection("translucent");
      var5.renderBlockLayer(BlockRenderLayer.TRANSLUCENT, (double)var2, var1, var9);
      if (!this.debugView) {
         RenderHelper.enableStandardItemLighting();
         this.mc.mcProfiler.endStartSection("entities");
         ForgeHooksClient.setRenderPass(1);
         var5.renderEntities(var9, var8, var2);
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         ForgeHooksClient.setRenderPass(-1);
         RenderHelper.disableStandardItemLighting();
      }

      GlStateManager.shadeModel(7424);
      GlStateManager.depthMask(true);
      GlStateManager.enableCull();
      GlStateManager.disableBlend();
      GlStateManager.disableFog();
      if (var9.posY + (double)var9.getEyeHeight() >= 128.0D) {
         this.mc.mcProfiler.endStartSection("aboveClouds");
         this.renderCloudsCheck(var5, var2, var1);
      }

      this.mc.mcProfiler.endStartSection("forge_render_last");
      ForgeHooksClient.dispatchRenderLast(var5, var2);
      this.mc.mcProfiler.endStartSection("hand");
      if (this.renderHand) {
         GlStateManager.clear(256);
         this.renderHand(var2, var1);
      }

   }

   private void renderCloudsCheck(RenderGlobal var1, float var2, int var3) {
      if (this.mc.gameSettings.shouldRenderClouds() != 0) {
         this.mc.mcProfiler.endStartSection("clouds");
         GlStateManager.matrixMode(5889);
         GlStateManager.loadIdentity();
         Project.gluPerspective(this.getFOVModifier(var2, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 4.0F);
         GlStateManager.matrixMode(5888);
         GlStateManager.pushMatrix();
         this.setupFog(0, var2);
         var1.renderClouds(var2, var3);
         GlStateManager.disableFog();
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5889);
         GlStateManager.loadIdentity();
         Project.gluPerspective(this.getFOVModifier(var2, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * MathHelper.SQRT_2);
         GlStateManager.matrixMode(5888);
      }

   }

   private void addRainParticles() {
      float var1 = this.mc.world.getRainStrength(1.0F);
      if (!this.mc.gameSettings.fancyGraphics) {
         var1 /= 2.0F;
      }

      if (var1 != 0.0F) {
         this.random.setSeed((long)this.rendererUpdateCount * 312987231L);
         Entity var2 = this.mc.getRenderViewEntity();
         WorldClient var3 = this.mc.world;
         BlockPos var4 = new BlockPos(var2);
         boolean var5 = true;
         double var6 = 0.0D;
         double var8 = 0.0D;
         double var10 = 0.0D;
         int var12 = 0;
         int var13 = (int)(100.0F * var1 * var1);
         if (this.mc.gameSettings.particleSetting == 1) {
            var13 >>= 1;
         } else if (this.mc.gameSettings.particleSetting == 2) {
            var13 = 0;
         }

         for(int var14 = 0; var14 < var13; ++var14) {
            BlockPos var15 = var3.getPrecipitationHeight(var4.add(this.random.nextInt(10) - this.random.nextInt(10), 0, this.random.nextInt(10) - this.random.nextInt(10)));
            Biome var16 = var3.getBiome(var15);
            BlockPos var17 = var15.down();
            IBlockState var18 = var3.getBlockState(var17);
            if (var15.getY() <= var4.getY() + 10 && var15.getY() >= var4.getY() - 10 && var16.canRain() && var16.getFloatTemperature(var15) >= 0.15F) {
               double var19 = this.random.nextDouble();
               double var21 = this.random.nextDouble();
               AxisAlignedBB var23 = var18.getBoundingBox(var3, var17);
               if (var18.getMaterial() != Material.LAVA && var18.getBlock() != Blocks.MAGMA) {
                  if (var18.getMaterial() != Material.AIR) {
                     ++var12;
                     if (this.random.nextInt(var12) == 0) {
                        var6 = (double)var17.getX() + var19;
                        var8 = (double)((float)var17.getY() + 0.1F) + var23.maxY - 1.0D;
                        var10 = (double)var17.getZ() + var21;
                     }

                     this.mc.world.spawnParticle(EnumParticleTypes.WATER_DROP, (double)var17.getX() + var19, (double)((float)var17.getY() + 0.1F) + var23.maxY, (double)var17.getZ() + var21, 0.0D, 0.0D, 0.0D, new int[0]);
                  }
               } else {
                  this.mc.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)var15.getX() + var19, (double)((float)var15.getY() + 0.1F) - var23.minY, (double)var15.getZ() + var21, 0.0D, 0.0D, 0.0D, new int[0]);
               }
            }
         }

         if (var12 > 0 && this.random.nextInt(3) < this.rainSoundCounter++) {
            this.rainSoundCounter = 0;
            if (var8 > (double)(var4.getY() + 1) && var3.getPrecipitationHeight(var4).getY() > MathHelper.floor((float)var4.getY())) {
               this.mc.world.playSound(var6, var8, var10, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
            } else {
               this.mc.world.playSound(var6, var8, var10, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
            }
         }
      }

   }

   protected void renderRainSnow(float var1) {
      IRenderHandler var2 = this.mc.world.provider.getWeatherRenderer();
      if (var2 != null) {
         var2.render(var1, this.mc.world, this.mc);
      } else {
         float var3 = this.mc.world.getRainStrength(var1);
         if (var3 > 0.0F) {
            this.enableLightmap();
            Entity var4 = this.mc.getRenderViewEntity();
            WorldClient var5 = this.mc.world;
            int var6 = MathHelper.floor(var4.posX);
            int var7 = MathHelper.floor(var4.posY);
            int var8 = MathHelper.floor(var4.posZ);
            Tessellator var9 = Tessellator.getInstance();
            VertexBuffer var10 = var9.getBuffer();
            GlStateManager.disableCull();
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.alphaFunc(516, 0.1F);
            double var11 = var4.lastTickPosX + (var4.posX - var4.lastTickPosX) * (double)var1;
            double var13 = var4.lastTickPosY + (var4.posY - var4.lastTickPosY) * (double)var1;
            double var15 = var4.lastTickPosZ + (var4.posZ - var4.lastTickPosZ) * (double)var1;
            int var17 = MathHelper.floor(var13);
            byte var18 = 5;
            if (this.mc.gameSettings.fancyGraphics) {
               var18 = 10;
            }

            byte var19 = -1;
            float var20 = (float)this.rendererUpdateCount + var1;
            var10.setTranslation(-var11, -var13, -var15);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos var21 = new BlockPos.MutableBlockPos();

            for(int var22 = var8 - var18; var22 <= var8 + var18; ++var22) {
               for(int var23 = var6 - var18; var23 <= var6 + var18; ++var23) {
                  int var24 = (var22 - var8 + 16) * 32 + var23 - var6 + 16;
                  double var25 = (double)this.rainXCoords[var24] * 0.5D;
                  double var27 = (double)this.rainYCoords[var24] * 0.5D;
                  var21.setPos(var23, 0, var22);
                  Biome var29 = var5.getBiome(var21);
                  if (var29.canRain() || var29.getEnableSnow()) {
                     int var30 = var5.getPrecipitationHeight(var21).getY();
                     int var31 = var7 - var18;
                     int var32 = var7 + var18;
                     if (var31 < var30) {
                        var31 = var30;
                     }

                     if (var32 < var30) {
                        var32 = var30;
                     }

                     int var33 = var30;
                     if (var30 < var17) {
                        var33 = var17;
                     }

                     if (var31 != var32) {
                        this.random.setSeed((long)(var23 * var23 * 3121 + var23 * 45238971 ^ var22 * var22 * 418711 + var22 * 13761));
                        var21.setPos(var23, var31, var22);
                        float var34 = var29.getFloatTemperature(var21);
                        if (var5.getBiomeProvider().getTemperatureAtHeight(var34, var30) >= 0.15F) {
                           if (var19 != 0) {
                              if (var19 >= 0) {
                                 var9.draw();
                              }

                              var19 = 0;
                              this.mc.getTextureManager().bindTexture(RAIN_TEXTURES);
                              var10.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                           }

                           double var35 = -((double)(this.rendererUpdateCount + var23 * var23 * 3121 + var23 * 45238971 + var22 * var22 * 418711 + var22 * 13761 & 31) + (double)var1) / 32.0D * (3.0D + this.random.nextDouble());
                           double var37 = (double)((float)var23 + 0.5F) - var4.posX;
                           double var39 = (double)((float)var22 + 0.5F) - var4.posZ;
                           float var41 = MathHelper.sqrt(var37 * var37 + var39 * var39) / (float)var18;
                           float var42 = ((1.0F - var41 * var41) * 0.5F + 0.5F) * var3;
                           var21.setPos(var23, var33, var22);
                           int var43 = var5.getCombinedLight(var21, 0);
                           int var44 = var43 >> 16 & '\uffff';
                           int var45 = var43 & '\uffff';
                           var10.pos((double)var23 - var25 + 0.5D, (double)var32, (double)var22 - var27 + 0.5D).tex(0.0D, (double)var31 * 0.25D + var35).color(1.0F, 1.0F, 1.0F, var42).lightmap(var44, var45).endVertex();
                           var10.pos((double)var23 + var25 + 0.5D, (double)var32, (double)var22 + var27 + 0.5D).tex(1.0D, (double)var31 * 0.25D + var35).color(1.0F, 1.0F, 1.0F, var42).lightmap(var44, var45).endVertex();
                           var10.pos((double)var23 + var25 + 0.5D, (double)var31, (double)var22 + var27 + 0.5D).tex(1.0D, (double)var32 * 0.25D + var35).color(1.0F, 1.0F, 1.0F, var42).lightmap(var44, var45).endVertex();
                           var10.pos((double)var23 - var25 + 0.5D, (double)var31, (double)var22 - var27 + 0.5D).tex(0.0D, (double)var32 * 0.25D + var35).color(1.0F, 1.0F, 1.0F, var42).lightmap(var44, var45).endVertex();
                        } else {
                           if (var19 != 1) {
                              if (var19 >= 0) {
                                 var9.draw();
                              }

                              var19 = 1;
                              this.mc.getTextureManager().bindTexture(SNOW_TEXTURES);
                              var10.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                           }

                           double var50 = (double)(-((float)(this.rendererUpdateCount & 511) + var1) / 512.0F);
                           double var51 = this.random.nextDouble() + (double)var20 * 0.01D * (double)((float)this.random.nextGaussian());
                           double var52 = this.random.nextDouble() + (double)(var20 * (float)this.random.nextGaussian()) * 0.001D;
                           double var53 = (double)((float)var23 + 0.5F) - var4.posX;
                           double var54 = (double)((float)var22 + 0.5F) - var4.posZ;
                           float var55 = MathHelper.sqrt(var53 * var53 + var54 * var54) / (float)var18;
                           float var46 = ((1.0F - var55 * var55) * 0.3F + 0.5F) * var3;
                           var21.setPos(var23, var33, var22);
                           int var47 = (var5.getCombinedLight(var21, 0) * 3 + 15728880) / 4;
                           int var48 = var47 >> 16 & '\uffff';
                           int var49 = var47 & '\uffff';
                           var10.pos((double)var23 - var25 + 0.5D, (double)var32, (double)var22 - var27 + 0.5D).tex(0.0D + var51, (double)var31 * 0.25D + var50 + var52).color(1.0F, 1.0F, 1.0F, var46).lightmap(var48, var49).endVertex();
                           var10.pos((double)var23 + var25 + 0.5D, (double)var32, (double)var22 + var27 + 0.5D).tex(1.0D + var51, (double)var31 * 0.25D + var50 + var52).color(1.0F, 1.0F, 1.0F, var46).lightmap(var48, var49).endVertex();
                           var10.pos((double)var23 + var25 + 0.5D, (double)var31, (double)var22 + var27 + 0.5D).tex(1.0D + var51, (double)var32 * 0.25D + var50 + var52).color(1.0F, 1.0F, 1.0F, var46).lightmap(var48, var49).endVertex();
                           var10.pos((double)var23 - var25 + 0.5D, (double)var31, (double)var22 - var27 + 0.5D).tex(0.0D + var51, (double)var32 * 0.25D + var50 + var52).color(1.0F, 1.0F, 1.0F, var46).lightmap(var48, var49).endVertex();
                        }
                     }
                  }
               }
            }

            if (var19 >= 0) {
               var9.draw();
            }

            var10.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            this.disableLightmap();
         }

      }
   }

   public void setupOverlayRendering() {
      ScaledResolution var1 = new ScaledResolution(this.mc);
      GlStateManager.clear(256);
      GlStateManager.matrixMode(5889);
      GlStateManager.loadIdentity();
      GlStateManager.ortho(0.0D, var1.getScaledWidth_double(), var1.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
      GlStateManager.matrixMode(5888);
      GlStateManager.loadIdentity();
      GlStateManager.translate(0.0F, 0.0F, -2000.0F);
   }

   private void updateFogColor(float var1) {
      WorldClient var2 = this.mc.world;
      Entity var3 = this.mc.getRenderViewEntity();
      float var4 = 0.25F + 0.75F * (float)this.mc.gameSettings.renderDistanceChunks / 32.0F;
      var4 = 1.0F - (float)Math.pow((double)var4, 0.25D);
      Vec3d var5 = var2.getSkyColor(this.mc.getRenderViewEntity(), var1);
      float var6 = (float)var5.xCoord;
      float var7 = (float)var5.yCoord;
      float var8 = (float)var5.zCoord;
      Vec3d var9 = var2.getFogColor(var1);
      this.fogColorRed = (float)var9.xCoord;
      this.fogColorGreen = (float)var9.yCoord;
      this.fogColorBlue = (float)var9.zCoord;
      if (this.mc.gameSettings.renderDistanceChunks >= 4) {
         double var10 = MathHelper.sin(var2.getCelestialAngleRadians(var1)) > 0.0F ? -1.0D : 1.0D;
         Vec3d var12 = new Vec3d(var10, 0.0D, 0.0D);
         float var13 = (float)var3.getLook(var1).dotProduct(var12);
         if (var13 < 0.0F) {
            var13 = 0.0F;
         }

         if (var13 > 0.0F) {
            float[] var14 = var2.provider.calcSunriseSunsetColors(var2.getCelestialAngle(var1), var1);
            if (var14 != null) {
               var13 = var13 * var14[3];
               this.fogColorRed = this.fogColorRed * (1.0F - var13) + var14[0] * var13;
               this.fogColorGreen = this.fogColorGreen * (1.0F - var13) + var14[1] * var13;
               this.fogColorBlue = this.fogColorBlue * (1.0F - var13) + var14[2] * var13;
            }
         }
      }

      this.fogColorRed += (var6 - this.fogColorRed) * var4;
      this.fogColorGreen += (var7 - this.fogColorGreen) * var4;
      this.fogColorBlue += (var8 - this.fogColorBlue) * var4;
      float var20 = var2.getRainStrength(var1);
      if (var20 > 0.0F) {
         float var11 = 1.0F - var20 * 0.5F;
         float var22 = 1.0F - var20 * 0.4F;
         this.fogColorRed *= var11;
         this.fogColorGreen *= var11;
         this.fogColorBlue *= var22;
      }

      float var21 = var2.getThunderStrength(var1);
      if (var21 > 0.0F) {
         float var23 = 1.0F - var21 * 0.5F;
         this.fogColorRed *= var23;
         this.fogColorGreen *= var23;
         this.fogColorBlue *= var23;
      }

      IBlockState var24 = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, var3, var1);
      if (this.cloudFog) {
         Vec3d var26 = var2.getCloudColour(var1);
         this.fogColorRed = (float)var26.xCoord;
         this.fogColorGreen = (float)var26.yCoord;
         this.fogColorBlue = (float)var26.zCoord;
      } else if (var24.getMaterial() == Material.WATER) {
         float var27 = 0.0F;
         if (var3 instanceof EntityLivingBase) {
            var27 = (float)EnchantmentHelper.getRespirationModifier((EntityLivingBase)var3) * 0.2F;
            if (((EntityLivingBase)var3).isPotionActive(MobEffects.WATER_BREATHING)) {
               var27 = var27 * 0.3F + 0.6F;
            }
         }

         this.fogColorRed = 0.02F + var27;
         this.fogColorGreen = 0.02F + var27;
         this.fogColorBlue = 0.2F + var27;
      } else if (var24.getMaterial() == Material.LAVA) {
         this.fogColorRed = 0.6F;
         this.fogColorGreen = 0.1F;
         this.fogColorBlue = 0.0F;
      }

      float var28 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * var1;
      this.fogColorRed *= var28;
      this.fogColorGreen *= var28;
      this.fogColorBlue *= var28;
      double var29 = (var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double)var1) * var2.provider.getVoidFogYFactor();
      if (var3 instanceof EntityLivingBase && ((EntityLivingBase)var3).isPotionActive(MobEffects.BLINDNESS)) {
         int var16 = ((EntityLivingBase)var3).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
         if (var16 < 20) {
            var29 *= (double)(1.0F - (float)var16 / 20.0F);
         } else {
            var29 = 0.0D;
         }
      }

      if (var29 < 1.0D) {
         if (var29 < 0.0D) {
            var29 = 0.0D;
         }

         var29 = var29 * var29;
         this.fogColorRed = (float)((double)this.fogColorRed * var29);
         this.fogColorGreen = (float)((double)this.fogColorGreen * var29);
         this.fogColorBlue = (float)((double)this.fogColorBlue * var29);
      }

      if (this.bossColorModifier > 0.0F) {
         float var31 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * var1;
         this.fogColorRed = this.fogColorRed * (1.0F - var31) + this.fogColorRed * 0.7F * var31;
         this.fogColorGreen = this.fogColorGreen * (1.0F - var31) + this.fogColorGreen * 0.6F * var31;
         this.fogColorBlue = this.fogColorBlue * (1.0F - var31) + this.fogColorBlue * 0.6F * var31;
      }

      if (var3 instanceof EntityLivingBase && ((EntityLivingBase)var3).isPotionActive(MobEffects.NIGHT_VISION)) {
         float var32 = this.getNightVisionBrightness((EntityLivingBase)var3, var1);
         float var17 = 1.0F / this.fogColorRed;
         if (var17 > 1.0F / this.fogColorGreen) {
            var17 = 1.0F / this.fogColorGreen;
         }

         if (var17 > 1.0F / this.fogColorBlue) {
            var17 = 1.0F / this.fogColorBlue;
         }

         this.fogColorRed = this.fogColorRed * (1.0F - var32) + this.fogColorRed * var17 * var32;
         this.fogColorGreen = this.fogColorGreen * (1.0F - var32) + this.fogColorGreen * var17 * var32;
         this.fogColorBlue = this.fogColorBlue * (1.0F - var32) + this.fogColorBlue * var17 * var32;
      }

      if (this.mc.gameSettings.anaglyph) {
         float var33 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
         float var35 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
         float var18 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
         this.fogColorRed = var33;
         this.fogColorGreen = var35;
         this.fogColorBlue = var18;
      }

      FogColors var34 = new FogColors(this, var3, var24, (double)var1, this.fogColorRed, this.fogColorGreen, this.fogColorBlue);
      MinecraftForge.EVENT_BUS.post(var34);
      this.fogColorRed = var34.getRed();
      this.fogColorGreen = var34.getGreen();
      this.fogColorBlue = var34.getBlue();
      GlStateManager.clearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
   }

   private void setupFog(int var1, float var2) {
      Entity var3 = this.mc.getRenderViewEntity();
      GlStateManager.glFog(2918, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
      GlStateManager.glNormal3f(0.0F, -1.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      IBlockState var4 = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, var3, var2);
      float var5 = ForgeHooksClient.getFogDensity(this, var3, var4, var2, 0.1F);
      if (var5 >= 0.0F) {
         GlStateManager.setFogDensity(var5);
      } else if (var3 instanceof EntityLivingBase && ((EntityLivingBase)var3).isPotionActive(MobEffects.BLINDNESS)) {
         float var8 = 5.0F;
         int var7 = ((EntityLivingBase)var3).getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
         if (var7 < 20) {
            var8 = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float)var7 / 20.0F);
         }

         GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
         if (var1 == -1) {
            GlStateManager.setFogStart(0.0F);
            GlStateManager.setFogEnd(var8 * 0.8F);
         } else {
            GlStateManager.setFogStart(var8 * 0.25F);
            GlStateManager.setFogEnd(var8);
         }

         if (GLContext.getCapabilities().GL_NV_fog_distance) {
            GlStateManager.glFogi(34138, 34139);
         }
      } else if (this.cloudFog) {
         GlStateManager.setFog(GlStateManager.FogMode.EXP);
         GlStateManager.setFogDensity(0.1F);
      } else if (var4.getMaterial() == Material.WATER) {
         GlStateManager.setFog(GlStateManager.FogMode.EXP);
         if (var3 instanceof EntityLivingBase) {
            if (((EntityLivingBase)var3).isPotionActive(MobEffects.WATER_BREATHING)) {
               GlStateManager.setFogDensity(0.01F);
            } else {
               GlStateManager.setFogDensity(0.1F - (float)EnchantmentHelper.getRespirationModifier((EntityLivingBase)var3) * 0.03F);
            }
         } else {
            GlStateManager.setFogDensity(0.1F);
         }
      } else if (var4.getMaterial() == Material.LAVA) {
         GlStateManager.setFog(GlStateManager.FogMode.EXP);
         GlStateManager.setFogDensity(2.0F);
      } else {
         float var6 = this.farPlaneDistance;
         GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
         if (var1 == -1) {
            GlStateManager.setFogStart(0.0F);
            GlStateManager.setFogEnd(var6);
         } else {
            GlStateManager.setFogStart(var6 * 0.75F);
            GlStateManager.setFogEnd(var6);
         }

         if (GLContext.getCapabilities().GL_NV_fog_distance) {
            GlStateManager.glFogi(34138, 34139);
         }

         if (this.mc.world.provider.doesXZShowFog((int)var3.posX, (int)var3.posZ) || this.mc.ingameGUI.getBossOverlay().shouldCreateFog()) {
            GlStateManager.setFogStart(var6 * 0.05F);
            GlStateManager.setFogEnd(Math.min(var6, 192.0F) * 0.5F);
         }

         ForgeHooksClient.onFogRender(this, var3, var4, var2, var1, var6);
      }

      GlStateManager.enableColorMaterial();
      GlStateManager.enableFog();
      GlStateManager.colorMaterial(1028, 4608);
   }

   private FloatBuffer setFogColorBuffer(float var1, float var2, float var3, float var4) {
      this.fogColorBuffer.clear();
      this.fogColorBuffer.put(var1).put(var2).put(var3).put(var4);
      this.fogColorBuffer.flip();
      return this.fogColorBuffer;
   }

   public MapItemRenderer getMapItemRenderer() {
      return this.theMapItemRenderer;
   }

   public static void drawNameplate(FontRenderer var0, String var1, float var2, float var3, float var4, int var5, float var6, float var7, boolean var8, boolean var9) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(var2, var3, var4);
      GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-var6, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((float)(var8 ? -1 : 1) * var7, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(-0.025F, -0.025F, 0.025F);
      GlStateManager.disableLighting();
      GlStateManager.depthMask(false);
      if (!var9) {
         GlStateManager.disableDepth();
      }

      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      int var10 = var0.getStringWidth(var1) / 2;
      GlStateManager.disableTexture2D();
      Tessellator var11 = Tessellator.getInstance();
      VertexBuffer var12 = var11.getBuffer();
      var12.begin(7, DefaultVertexFormats.POSITION_COLOR);
      var12.pos((double)(-var10 - 1), (double)(-1 + var5), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
      var12.pos((double)(-var10 - 1), (double)(8 + var5), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
      var12.pos((double)(var10 + 1), (double)(8 + var5), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
      var12.pos((double)(var10 + 1), (double)(-1 + var5), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
      var11.draw();
      GlStateManager.enableTexture2D();
      if (!var9) {
         var0.drawString(var1, -var0.getStringWidth(var1) / 2, var5, 553648127);
         GlStateManager.enableDepth();
      }

      GlStateManager.depthMask(true);
      var0.drawString(var1, -var0.getStringWidth(var1) / 2, var5, var9 ? 553648127 : -1);
      GlStateManager.enableLighting();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }
}
