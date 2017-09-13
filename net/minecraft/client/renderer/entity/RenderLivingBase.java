package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import java.nio.FloatBuffer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public abstract class RenderLivingBase extends Render {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DynamicTexture TEXTURE_BRIGHTNESS = new DynamicTexture(16, 16);
   protected ModelBase mainModel;
   protected FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);
   protected List layerRenderers = Lists.newArrayList();
   protected boolean renderMarker;
   public static float NAME_TAG_RANGE = 64.0F;
   public static float NAME_TAG_RANGE_SNEAK = 32.0F;

   public RenderLivingBase(RenderManager var1, ModelBase var2, float var3) {
      super(var1);
      this.mainModel = var2;
      this.shadowSize = var3;
   }

   public boolean addLayer(LayerRenderer var1) {
      return this.layerRenderers.add(var1);
   }

   public boolean removeLayer(LayerRenderer var1) {
      return this.layerRenderers.remove(var1);
   }

   public ModelBase getMainModel() {
      return this.mainModel;
   }

   protected float interpolateRotation(float var1, float var2, float var3) {
      float var4;
      for(var4 = var2 - var1; var4 < -180.0F; var4 += 360.0F) {
         ;
      }

      while(var4 >= 180.0F) {
         var4 -= 360.0F;
      }

      return var1 + var3 * var4;
   }

   public void transformHeldFull3DItemLayer() {
   }

   public void doRender(EntityLivingBase var1, double var2, double var4, double var6, float var8, float var9) {
      if (!MinecraftForge.EVENT_BUS.post(new Pre(var1, this, var2, var4, var6))) {
         GlStateManager.pushMatrix();
         GlStateManager.disableCull();
         this.mainModel.swingProgress = this.getSwingProgress(var1, var9);
         boolean var10 = var1.isRiding() && var1.getRidingEntity() != null && var1.getRidingEntity().shouldRiderSit();
         this.mainModel.isRiding = var10;
         this.mainModel.isChild = var1.isChild();

         try {
            float var11 = this.interpolateRotation(var1.prevRenderYawOffset, var1.renderYawOffset, var9);
            float var12 = this.interpolateRotation(var1.prevRotationYawHead, var1.rotationYawHead, var9);
            float var13 = var12 - var11;
            if (var10 && var1.getRidingEntity() instanceof EntityLivingBase) {
               EntityLivingBase var14 = (EntityLivingBase)var1.getRidingEntity();
               var11 = this.interpolateRotation(var14.prevRenderYawOffset, var14.renderYawOffset, var9);
               var13 = var12 - var11;
               float var15 = MathHelper.wrapDegrees(var13);
               if (var15 < -85.0F) {
                  var15 = -85.0F;
               }

               if (var15 >= 85.0F) {
                  var15 = 85.0F;
               }

               var11 = var12 - var15;
               if (var15 * var15 > 2500.0F) {
                  var11 += var15 * 0.2F;
               }
            }

            float var22 = var1.prevRotationPitch + (var1.rotationPitch - var1.prevRotationPitch) * var9;
            this.renderLivingAt(var1, var2, var4, var6);
            float var23 = this.handleRotationFloat(var1, var9);
            this.applyRotations(var1, var23, var11, var9);
            float var16 = this.prepareScale(var1, var9);
            float var17 = 0.0F;
            float var18 = 0.0F;
            if (!var1.isRiding()) {
               var17 = var1.prevLimbSwingAmount + (var1.limbSwingAmount - var1.prevLimbSwingAmount) * var9;
               var18 = var1.limbSwing - var1.limbSwingAmount * (1.0F - var9);
               if (var1.isChild()) {
                  var18 *= 3.0F;
               }

               if (var17 > 1.0F) {
                  var17 = 1.0F;
               }
            }

            GlStateManager.enableAlpha();
            this.mainModel.setLivingAnimations(var1, var18, var17, var9);
            this.mainModel.setRotationAngles(var18, var17, var23, var13, var22, var16, var1);
            if (this.renderOutlines) {
               boolean var24 = this.setScoreTeamColor(var1);
               GlStateManager.enableColorMaterial();
               GlStateManager.enableOutlineMode(this.getTeamColor(var1));
               if (!this.renderMarker) {
                  this.renderModel(var1, var18, var17, var23, var13, var22, var16);
               }

               if (!(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).isSpectator()) {
                  this.renderLayers(var1, var18, var17, var9, var23, var13, var22, var16);
               }

               GlStateManager.disableOutlineMode();
               GlStateManager.disableColorMaterial();
               if (var24) {
                  this.unsetScoreTeamColor();
               }
            } else {
               boolean var19 = this.setDoRenderBrightness(var1, var9);
               this.renderModel(var1, var18, var17, var23, var13, var22, var16);
               if (var19) {
                  this.unsetBrightness();
               }

               GlStateManager.depthMask(true);
               if (!(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).isSpectator()) {
                  this.renderLayers(var1, var18, var17, var9, var23, var13, var22, var16);
               }
            }

            GlStateManager.disableRescaleNormal();
         } catch (Exception var20) {
            LOGGER.error("Couldn't render entity", var20);
         }

         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         GlStateManager.enableTexture2D();
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
         super.doRender(var1, var2, var4, var6, var8, var9);
         MinecraftForge.EVENT_BUS.post(new Post(var1, this, var2, var4, var6));
      }
   }

   public float prepareScale(EntityLivingBase var1, float var2) {
      GlStateManager.enableRescaleNormal();
      GlStateManager.scale(-1.0F, -1.0F, 1.0F);
      this.preRenderCallback(var1, var2);
      float var3 = 0.0625F;
      GlStateManager.translate(0.0F, -1.501F, 0.0F);
      return 0.0625F;
   }

   protected boolean setScoreTeamColor(EntityLivingBase var1) {
      GlStateManager.disableLighting();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      return true;
   }

   protected void unsetScoreTeamColor() {
      GlStateManager.enableLighting();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.enableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   protected void renderModel(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      boolean var8 = !var1.isInvisible() || this.renderOutlines;
      boolean var9 = !var8 && !var1.isInvisibleToPlayer(Minecraft.getMinecraft().player);
      if (var8 || var9) {
         if (!this.bindEntityTexture(var1)) {
            return;
         }

         if (var9) {
            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
         }

         this.mainModel.render(var1, var2, var3, var4, var5, var6, var7);
         if (var9) {
            GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
         }
      }

   }

   protected boolean setDoRenderBrightness(EntityLivingBase var1, float var2) {
      return this.setBrightness(var1, var2, true);
   }

   protected boolean setBrightness(EntityLivingBase var1, float var2, boolean var3) {
      float var4 = var1.getBrightness(var2);
      int var5 = this.getColorMultiplier(var1, var4, var2);
      boolean var6 = (var5 >> 24 & 255) > 0;
      boolean var7 = var1.hurtTime > 0 || var1.deathTime > 0;
      if (!var6 && !var7) {
         return false;
      } else if (!var6 && !var3) {
         return false;
      } else {
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         GlStateManager.enableTexture2D();
         GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         GlStateManager.enableTexture2D();
         GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
         this.brightnessBuffer.position(0);
         if (var7) {
            this.brightnessBuffer.put(1.0F);
            this.brightnessBuffer.put(0.0F);
            this.brightnessBuffer.put(0.0F);
            this.brightnessBuffer.put(0.3F);
         } else {
            float var8 = (float)(var5 >> 24 & 255) / 255.0F;
            float var9 = (float)(var5 >> 16 & 255) / 255.0F;
            float var10 = (float)(var5 >> 8 & 255) / 255.0F;
            float var11 = (float)(var5 & 255) / 255.0F;
            this.brightnessBuffer.put(var9);
            this.brightnessBuffer.put(var10);
            this.brightnessBuffer.put(var11);
            this.brightnessBuffer.put(1.0F - var8);
         }

         this.brightnessBuffer.flip();
         GlStateManager.glTexEnv(8960, 8705, this.brightnessBuffer);
         GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
         GlStateManager.enableTexture2D();
         GlStateManager.bindTexture(TEXTURE_BRIGHTNESS.getGlTextureId());
         GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
         GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         return true;
      }
   }

   protected void unsetBrightness() {
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.enableTexture2D();
      GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_ALPHA, 770);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
      GlStateManager.disableTexture2D();
      GlStateManager.bindTexture(0);
      GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
      GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   protected void renderLivingAt(EntityLivingBase var1, double var2, double var4, double var6) {
      GlStateManager.translate((float)var2, (float)var4, (float)var6);
   }

   protected void applyRotations(EntityLivingBase var1, float var2, float var3, float var4) {
      GlStateManager.rotate(180.0F - var3, 0.0F, 1.0F, 0.0F);
      if (var1.deathTime > 0) {
         float var5 = ((float)var1.deathTime + var4 - 1.0F) / 20.0F * 1.6F;
         var5 = MathHelper.sqrt(var5);
         if (var5 > 1.0F) {
            var5 = 1.0F;
         }

         GlStateManager.rotate(var5 * this.getDeathMaxRotation(var1), 0.0F, 0.0F, 1.0F);
      } else {
         String var7 = TextFormatting.getTextWithoutFormattingCodes(var1.getName());
         if (var7 != null && ("Dinnerbone".equals(var7) || "Grumm".equals(var7)) && (!(var1 instanceof EntityPlayer) || ((EntityPlayer)var1).isWearing(EnumPlayerModelParts.CAPE))) {
            GlStateManager.translate(0.0F, var1.height + 0.1F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         }
      }

   }

   protected float getSwingProgress(EntityLivingBase var1, float var2) {
      return var1.getSwingProgress(var2);
   }

   protected float handleRotationFloat(EntityLivingBase var1, float var2) {
      return (float)var1.ticksExisted + var2;
   }

   protected void renderLayers(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      for(LayerRenderer var10 : this.layerRenderers) {
         boolean var11 = this.setBrightness(var1, var4, var10.shouldCombineTextures());
         var10.doRenderLayer(var1, var2, var3, var4, var5, var6, var7, var8);
         if (var11) {
            this.unsetBrightness();
         }
      }

   }

   protected float getDeathMaxRotation(EntityLivingBase var1) {
      return 90.0F;
   }

   protected int getColorMultiplier(EntityLivingBase var1, float var2, float var3) {
      return 0;
   }

   protected void preRenderCallback(EntityLivingBase var1, float var2) {
   }

   public void renderName(EntityLivingBase var1, double var2, double var4, double var6) {
      if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Specials.Pre(var1, this, var2, var4, var6))) {
         if (this.canRenderName(var1)) {
            double var8 = var1.getDistanceSqToEntity(this.renderManager.renderViewEntity);
            float var10 = var1.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;
            if (var8 < (double)(var10 * var10)) {
               String var11 = var1.getDisplayName().getFormattedText();
               GlStateManager.alphaFunc(516, 0.1F);
               this.renderEntityName(var1, var2, var4, var6, var11, var8);
            }
         }

         MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Specials.Post(var1, this, var2, var4, var6));
      }
   }

   protected boolean canRenderName(EntityLivingBase var1) {
      EntityPlayerSP var2 = Minecraft.getMinecraft().player;
      boolean var3 = !var1.isInvisibleToPlayer(var2);
      if (var1 != var2) {
         Team var4 = var1.getTeam();
         Team var5 = var2.getTeam();
         if (var4 != null) {
            Team.EnumVisible var6 = var4.getNameTagVisibility();
            switch(var6) {
            case ALWAYS:
               return var3;
            case NEVER:
               return false;
            case HIDE_FOR_OTHER_TEAMS:
               return var5 == null ? var3 : var4.isSameTeam(var5) && (var4.getSeeFriendlyInvisiblesEnabled() || var3);
            case HIDE_FOR_OWN_TEAM:
               return var5 == null ? var3 : !var4.isSameTeam(var5) && var3;
            default:
               return true;
            }
         }
      }

      return Minecraft.isGuiEnabled() && var1 != this.renderManager.renderViewEntity && var3 && !var1.isBeingRidden();
   }

   static {
      int[] var0 = TEXTURE_BRIGHTNESS.getTextureData();

      for(int var1 = 0; var1 < 256; ++var1) {
         var0[var1] = -1;
      }

      TEXTURE_BRIGHTNESS.updateDynamicTexture();
   }
}
