package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class Render {
   private static final ResourceLocation SHADOW_TEXTURES = new ResourceLocation("textures/misc/shadow.png");
   protected final RenderManager renderManager;
   protected float shadowSize;
   protected float shadowOpaque = 1.0F;
   protected boolean renderOutlines;

   protected Render(RenderManager var1) {
      this.renderManager = renderManager;
   }

   public void setRenderOutlines(boolean var1) {
      this.renderOutlines = renderOutlinesIn;
   }

   public boolean shouldRender(Entity var1, ICamera var2, double var3, double var5, double var7) {
      AxisAlignedBB axisalignedbb = livingEntity.getRenderBoundingBox().expandXyz(0.5D);
      if (axisalignedbb.hasNaN() || axisalignedbb.getAverageEdgeLength() == 0.0D) {
         axisalignedbb = new AxisAlignedBB(livingEntity.posX - 2.0D, livingEntity.posY - 2.0D, livingEntity.posZ - 2.0D, livingEntity.posX + 2.0D, livingEntity.posY + 2.0D, livingEntity.posZ + 2.0D);
      }

      return livingEntity.isInRangeToRender3d(camX, camY, camZ) && (livingEntity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(axisalignedbb));
   }

   public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      if (!this.renderOutlines) {
         this.renderName(entity, x, y, z);
      }

   }

   protected int getTeamColor(Entity var1) {
      int i = 16777215;
      ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)entityIn.getTeam();
      if (scoreplayerteam != null) {
         String s = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());
         if (s.length() >= 2) {
            i = this.getFontRendererFromRenderManager().getColorCode(s.charAt(1));
         }
      }

      return i;
   }

   protected void renderName(Entity var1, double var2, double var4, double var6) {
      if (this.canRenderName(entity)) {
         this.renderLivingLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, 64);
      }

   }

   protected boolean canRenderName(Entity var1) {
      return entity.getAlwaysRenderNameTagForRender() && entity.hasCustomName();
   }

   protected void renderEntityName(Entity var1, double var2, double var4, double var6, String var8, double var9) {
      this.renderLivingLabel(entityIn, name, x, y, z, 64);
   }

   protected abstract ResourceLocation getEntityTexture(Entity var1);

   protected boolean bindEntityTexture(Entity var1) {
      ResourceLocation resourcelocation = this.getEntityTexture(entity);
      if (resourcelocation == null) {
         return false;
      } else {
         this.bindTexture(resourcelocation);
         return true;
      }
   }

   public void bindTexture(ResourceLocation var1) {
      this.renderManager.renderEngine.bindTexture(location);
   }

   private void renderEntityOnFire(Entity var1, double var2, double var4, double var6, float var8) {
      GlStateManager.disableLighting();
      TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
      TextureAtlasSprite textureatlassprite = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_0");
      TextureAtlasSprite textureatlassprite1 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_1");
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)x, (float)y, (float)z);
      float f = entity.width * 1.4F;
      GlStateManager.scale(f, f, f);
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();
      float f1 = 0.5F;
      float f2 = 0.0F;
      float f3 = entity.height / f;
      float f4 = (float)(entity.posY - entity.getEntityBoundingBox().minY);
      GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, -0.3F + (float)((int)f3) * 0.02F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      float f5 = 0.0F;
      int i = 0;
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);

      while(f3 > 0.0F) {
         TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
         this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         float f6 = textureatlassprite2.getMinU();
         float f7 = textureatlassprite2.getMinV();
         float f8 = textureatlassprite2.getMaxU();
         float f9 = textureatlassprite2.getMaxV();
         if (i / 2 % 2 == 0) {
            float f10 = f8;
            f8 = f6;
            f6 = f10;
         }

         vertexbuffer.pos((double)(f1 - 0.0F), (double)(0.0F - f4), (double)f5).tex((double)f8, (double)f9).endVertex();
         vertexbuffer.pos((double)(-f1 - 0.0F), (double)(0.0F - f4), (double)f5).tex((double)f6, (double)f9).endVertex();
         vertexbuffer.pos((double)(-f1 - 0.0F), (double)(1.4F - f4), (double)f5).tex((double)f6, (double)f7).endVertex();
         vertexbuffer.pos((double)(f1 - 0.0F), (double)(1.4F - f4), (double)f5).tex((double)f8, (double)f7).endVertex();
         f3 -= 0.45F;
         f4 -= 0.45F;
         f1 *= 0.9F;
         f5 += 0.03F;
         ++i;
      }

      tessellator.draw();
      GlStateManager.popMatrix();
      GlStateManager.enableLighting();
   }

   private void renderShadow(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      this.renderManager.renderEngine.bindTexture(SHADOW_TEXTURES);
      World world = this.getWorldFromRenderManager();
      GlStateManager.depthMask(false);
      float f = this.shadowSize;
      if (entityIn instanceof EntityLiving) {
         EntityLiving entityliving = (EntityLiving)entityIn;
         f *= entityliving.getRenderSizeModifier();
         if (entityliving.isChild()) {
            f *= 0.5F;
         }
      }

      double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
      double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
      double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
      int i = MathHelper.floor(d5 - (double)f);
      int j = MathHelper.floor(d5 + (double)f);
      int k = MathHelper.floor(d0 - (double)f);
      int l = MathHelper.floor(d0);
      int i1 = MathHelper.floor(d1 - (double)f);
      int j1 = MathHelper.floor(d1 + (double)f);
      double d2 = x - d5;
      double d3 = y - d0;
      double d4 = z - d1;
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

      for(BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
         IBlockState iblockstate = world.getBlockState(blockpos.down());
         if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && world.getLightFromNeighbors(blockpos) > 3) {
            this.renderShadowSingle(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
         }
      }

      tessellator.draw();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
   }

   private World getWorldFromRenderManager() {
      return this.renderManager.world;
   }

   private void renderShadowSingle(IBlockState var1, double var2, double var4, double var6, BlockPos var8, float var9, float var10, double var11, double var13, double var15) {
      if (state.isFullCube()) {
         Tessellator tessellator = Tessellator.getInstance();
         VertexBuffer vertexbuffer = tessellator.getBuffer();
         double d0 = ((double)p_188299_9_ - (p_188299_4_ - ((double)p_188299_8_.getY() + p_188299_13_)) / 2.0D) * 0.5D * (double)this.getWorldFromRenderManager().getLightBrightness(p_188299_8_);
         if (d0 >= 0.0D) {
            if (d0 > 1.0D) {
               d0 = 1.0D;
            }

            AxisAlignedBB axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
            double d1 = (double)p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
            double d2 = (double)p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
            double d3 = (double)p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_ + 0.015625D;
            double d4 = (double)p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
            double d5 = (double)p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
            float f = (float)((p_188299_2_ - d1) / 2.0D / (double)p_188299_10_ + 0.5D);
            float f1 = (float)((p_188299_2_ - d2) / 2.0D / (double)p_188299_10_ + 0.5D);
            float f2 = (float)((p_188299_6_ - d4) / 2.0D / (double)p_188299_10_ + 0.5D);
            float f3 = (float)((p_188299_6_ - d5) / 2.0D / (double)p_188299_10_ + 0.5D);
            vertexbuffer.pos(d1, d3, d4).tex((double)f, (double)f2).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
            vertexbuffer.pos(d1, d3, d5).tex((double)f, (double)f3).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
            vertexbuffer.pos(d2, d3, d5).tex((double)f1, (double)f3).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
            vertexbuffer.pos(d2, d3, d4).tex((double)f1, (double)f2).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
         }
      }

   }

   public static void renderOffsetAABB(AxisAlignedBB var0, double var1, double var3, double var5) {
      GlStateManager.disableTexture2D();
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      vertexbuffer.setTranslation(x, y, z);
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_NORMAL);
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      tessellator.draw();
      vertexbuffer.setTranslation(0.0D, 0.0D, 0.0D);
      GlStateManager.enableTexture2D();
   }

   public void doRenderShadowAndFire(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      if (this.renderManager.options != null) {
         if (this.renderManager.options.entityShadows && this.shadowSize > 0.0F && !entityIn.isInvisible() && this.renderManager.isRenderShadow()) {
            double d0 = this.renderManager.getDistanceToCamera(entityIn.posX, entityIn.posY, entityIn.posZ);
            float f = (float)((1.0D - d0 / 256.0D) * (double)this.shadowOpaque);
            if (f > 0.0F) {
               this.renderShadow(entityIn, x, y, z, f, partialTicks);
            }
         }

         if (entityIn.canRenderOnFire() && (!(entityIn instanceof EntityPlayer) || !((EntityPlayer)entityIn).isSpectator())) {
            this.renderEntityOnFire(entityIn, x, y, z, partialTicks);
         }
      }

   }

   public FontRenderer getFontRendererFromRenderManager() {
      return this.renderManager.getFontRenderer();
   }

   protected void renderLivingLabel(Entity var1, String var2, double var3, double var5, double var7, int var9) {
      double d0 = entityIn.getDistanceSqToEntity(this.renderManager.renderViewEntity);
      if (d0 <= (double)(maxDistance * maxDistance)) {
         boolean flag = entityIn.isSneaking();
         float f = this.renderManager.playerViewY;
         float f1 = this.renderManager.playerViewX;
         boolean flag1 = this.renderManager.options.thirdPersonView == 2;
         float f2 = entityIn.height + 0.5F - (flag ? 0.25F : 0.0F);
         int i = "deadmau5".equals(str) ? -10 : 0;
         EntityRenderer.drawNameplate(this.getFontRendererFromRenderManager(), str, (float)x, (float)y + f2, (float)z, i, f, f1, flag1, flag);
      }

   }

   public RenderManager getRenderManager() {
      return this.renderManager;
   }

   public boolean isMultipass() {
      return false;
   }

   public void renderMultipass(Entity var1, double var2, double var4, double var6, float var8, float var9) {
   }
}
