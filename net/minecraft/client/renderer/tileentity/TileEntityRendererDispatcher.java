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

      for(TileEntitySpecialRenderer var2 : this.mapSpecialRenderers.values()) {
         var2.setRendererDispatcher(this);
      }

   }

   public TileEntitySpecialRenderer getSpecialRendererByClass(Class var1) {
      TileEntitySpecialRenderer var2 = (TileEntitySpecialRenderer)this.mapSpecialRenderers.get(var1);
      if (var2 == null && var1 != TileEntity.class) {
         var2 = this.getSpecialRendererByClass(var1.getSuperclass());
         this.mapSpecialRenderers.put(var1, var2);
      }

      return var2;
   }

   @Nullable
   public TileEntitySpecialRenderer getSpecialRenderer(@Nullable TileEntity var1) {
      return var1 == null ? null : this.getSpecialRendererByClass(var1.getClass());
   }

   public void prepare(World var1, TextureManager var2, FontRenderer var3, Entity var4, RayTraceResult var5, float var6) {
      if (this.world != var1) {
         this.setWorld(var1);
      }

      this.renderEngine = var2;
      this.entity = var4;
      this.fontRenderer = var3;
      this.cameraHitResult = var5;
      this.entityYaw = var4.prevRotationYaw + (var4.rotationYaw - var4.prevRotationYaw) * var6;
      this.entityPitch = var4.prevRotationPitch + (var4.rotationPitch - var4.prevRotationPitch) * var6;
      this.entityX = var4.lastTickPosX + (var4.posX - var4.lastTickPosX) * (double)var6;
      this.entityY = var4.lastTickPosY + (var4.posY - var4.lastTickPosY) * (double)var6;
      this.entityZ = var4.lastTickPosZ + (var4.posZ - var4.lastTickPosZ) * (double)var6;
   }

   public void renderTileEntity(TileEntity var1, float var2, int var3) {
      if (var1.getDistanceSq(this.entityX, this.entityY, this.entityZ) < var1.getMaxRenderDistanceSquared()) {
         RenderHelper.enableStandardItemLighting();
         if (!this.drawingBatch || !var1.hasFastRenderer()) {
            int var4 = this.world.getCombinedLight(var1.getPos(), 0);
            int var5 = var4 % 65536;
            int var6 = var4 / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)var5, (float)var6);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         }

         BlockPos var7 = var1.getPos();
         this.renderTileEntityAt(var1, (double)var7.getX() - staticPlayerX, (double)var7.getY() - staticPlayerY, (double)var7.getZ() - staticPlayerZ, var2, var3);
      }

   }

   public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8) {
      this.renderTileEntityAt(var1, var2, var4, var6, var8, -1);
   }

   public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8, int var9) {
      TileEntitySpecialRenderer var10 = this.getSpecialRenderer(var1);
      if (var10 != null) {
         try {
            if (this.drawingBatch && var1.hasFastRenderer()) {
               var10.renderTileEntityFast(var1, var2, var4, var6, var8, var9, this.batchBuffer.getBuffer());
            } else {
               var10.renderTileEntityAt(var1, var2, var4, var6, var8, var9);
            }
         } catch (Throwable var14) {
            CrashReport var12 = CrashReport.makeCrashReport(var14, "Rendering Block Entity");
            CrashReportCategory var13 = var12.makeCategory("Block Entity Details");
            var1.addInfoToCrashReport(var13);
            throw new ReportedException(var12);
         }
      }

   }

   public void setWorld(@Nullable World var1) {
      this.world = var1;
      if (var1 == null) {
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

      if (var1 > 0) {
         this.batchBuffer.getBuffer().sortVertexData((float)staticPlayerX, (float)staticPlayerY, (float)staticPlayerZ);
      }

      this.batchBuffer.draw();
      RenderHelper.enableStandardItemLighting();
      this.drawingBatch = false;
   }
}
