package net.minecraft.client.renderer.tileentity;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityRendererDispatcher {
   public final Map mapSpecialRenderers = Maps.newHashMap();
   public static TileEntityRendererDispatcher instance = new TileEntityRendererDispatcher();
   private FontRenderer fontRenderer;
   public static double staticPlayerX;
   public static double staticPlayerY;
   public static double staticPlayerZ;
   public TextureManager renderEngine;
   public World world;
   public Entity entity;
   public float entityYaw;
   public float entityPitch;
   public RayTraceResult cameraHitResult;
   public double entityX;
   public double entityY;
   public double entityZ;
   private Tessellator batchBuffer = new Tessellator(2097152);
   private boolean drawingBatch = false;

   private TileEntityRendererDispatcher() {
      this.mapSpecialRenderers.put(TileEntitySign.class, new TileEntitySignRenderer());
      this.mapSpecialRenderers.put(TileEntityMobSpawner.class, new TileEntityMobSpawnerRenderer());
      this.mapSpecialRenderers.put(TileEntityPiston.class, new TileEntityPistonRenderer());
      this.mapSpecialRenderers.put(TileEntityChest.class, new TileEntityChestRenderer());
      this.mapSpecialRenderers.put(TileEntityEnderChest.class, new TileEntityEnderChestRenderer());
      this.mapSpecialRenderers.put(TileEntityEnchantmentTable.class, new TileEntityEnchantmentTableRenderer());
      this.mapSpecialRenderers.put(TileEntityEndPortal.class, new TileEntityEndPortalRenderer());
      this.mapSpecialRenderers.put(TileEntityEndGateway.class, new TileEntityEndGatewayRenderer());
      this.mapSpecialRenderers.put(TileEntityBeacon.class, new TileEntityBeaconRenderer());
      this.mapSpecialRenderers.put(TileEntitySkull.class, new TileEntitySkullRenderer());
      this.mapSpecialRenderers.put(TileEntityBanner.class, new TileEntityBannerRenderer());
      this.mapSpecialRenderers.put(TileEntityStructure.class, new TileEntityStructureRenderer());

      for(TileEntitySpecialRenderer tileentityspecialrenderer : this.mapSpecialRenderers.values()) {
         tileentityspecialrenderer.setRendererDispatcher(this);
      }

   }

   public TileEntitySpecialRenderer getSpecialRendererByClass(Class var1) {
      TileEntitySpecialRenderer tileentityspecialrenderer = (TileEntitySpecialRenderer)this.mapSpecialRenderers.get(teClass);
      if (tileentityspecialrenderer == null && teClass != TileEntity.class) {
         tileentityspecialrenderer = this.getSpecialRendererByClass(teClass.getSuperclass());
         this.mapSpecialRenderers.put(teClass, tileentityspecialrenderer);
      }

      return tileentityspecialrenderer;
   }

   @Nullable
   public TileEntitySpecialRenderer getSpecialRenderer(@Nullable TileEntity var1) {
      return tileEntityIn == null ? null : this.getSpecialRendererByClass(tileEntityIn.getClass());
   }

   public void prepare(World var1, TextureManager var2, FontRenderer var3, Entity var4, RayTraceResult var5, float var6) {
      if (this.world != p_190056_1_) {
         this.setWorld(p_190056_1_);
      }

      this.renderEngine = p_190056_2_;
      this.entity = p_190056_4_;
      this.fontRenderer = p_190056_3_;
      this.cameraHitResult = p_190056_5_;
      this.entityYaw = p_190056_4_.prevRotationYaw + (p_190056_4_.rotationYaw - p_190056_4_.prevRotationYaw) * p_190056_6_;
      this.entityPitch = p_190056_4_.prevRotationPitch + (p_190056_4_.rotationPitch - p_190056_4_.prevRotationPitch) * p_190056_6_;
      this.entityX = p_190056_4_.lastTickPosX + (p_190056_4_.posX - p_190056_4_.lastTickPosX) * (double)p_190056_6_;
      this.entityY = p_190056_4_.lastTickPosY + (p_190056_4_.posY - p_190056_4_.lastTickPosY) * (double)p_190056_6_;
      this.entityZ = p_190056_4_.lastTickPosZ + (p_190056_4_.posZ - p_190056_4_.lastTickPosZ) * (double)p_190056_6_;
   }

   public void renderTileEntity(TileEntity var1, float var2, int var3) {
      if (tileentityIn.getDistanceSq(this.entityX, this.entityY, this.entityZ) < tileentityIn.getMaxRenderDistanceSquared()) {
         RenderHelper.enableStandardItemLighting();
         if (!this.drawingBatch || !tileentityIn.hasFastRenderer()) {
            int i = this.world.getCombinedLight(tileentityIn.getPos(), 0);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         }

         BlockPos blockpos = tileentityIn.getPos();
         this.renderTileEntityAt(tileentityIn, (double)blockpos.getX() - staticPlayerX, (double)blockpos.getY() - staticPlayerY, (double)blockpos.getZ() - staticPlayerZ, partialTicks, destroyStage);
      }

   }

   public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8) {
      this.renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, -1);
   }

   public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8, int var9) {
      TileEntitySpecialRenderer tileentityspecialrenderer = this.getSpecialRenderer(tileEntityIn);
      if (tileentityspecialrenderer != null) {
         try {
            if (this.drawingBatch && tileEntityIn.hasFastRenderer()) {
               tileentityspecialrenderer.renderTileEntityFast(tileEntityIn, x, y, z, partialTicks, destroyStage, this.batchBuffer.getBuffer());
            } else {
               tileentityspecialrenderer.renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, destroyStage);
            }
         } catch (Throwable var14) {
            CrashReport crashreport = CrashReport.makeCrashReport(var14, "Rendering Block Entity");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
            tileEntityIn.addInfoToCrashReport(crashreportcategory);
            throw new ReportedException(crashreport);
         }
      }

   }

   public void setWorld(@Nullable World var1) {
      this.world = worldIn;
      if (worldIn == null) {
         this.entity = null;
      }

   }

   public FontRenderer getFontRenderer() {
      return this.fontRenderer;
   }

   public void preDrawBatch() {
      this.batchBuffer.getBuffer().begin(7, DefaultVertexFormats.BLOCK);
      this.drawingBatch = true;
   }

   public void drawBatch(int var1) {
      this.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.enableBlend();
      GlStateManager.disableCull();
      if (Minecraft.isAmbientOcclusionEnabled()) {
         GlStateManager.shadeModel(7425);
      } else {
         GlStateManager.shadeModel(7424);
      }

      if (pass > 0) {
         this.batchBuffer.getBuffer().sortVertexData((float)staticPlayerX, (float)staticPlayerY, (float)staticPlayerZ);
      }

      this.batchBuffer.draw();
      RenderHelper.enableStandardItemLighting();
      this.drawingBatch = false;
   }
}
