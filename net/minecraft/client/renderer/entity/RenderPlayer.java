package net.minecraft.client.renderer.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent.Post;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPlayer extends RenderLivingBase {
   private final boolean smallArms;

   public RenderPlayer(RenderManager var1) {
      this(var1, false);
   }

   public RenderPlayer(RenderManager var1, boolean var2) {
      super(var1, new ModelPlayer(0.0F, var2), 0.5F);
      this.smallArms = var2;
      this.addLayer(new LayerBipedArmor(this));
      this.addLayer(new LayerHeldItem(this));
      this.addLayer(new LayerArrow(this));
      this.addLayer(new LayerDeadmau5Head(this));
      this.addLayer(new LayerCape(this));
      this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
      this.addLayer(new LayerElytra(this));
   }

   public ModelPlayer getMainModel() {
      return (ModelPlayer)super.getMainModel();
   }

   public void doRender(AbstractClientPlayer var1, double var2, double var4, double var6, float var8, float var9) {
      if (!MinecraftForge.EVENT_BUS.post(new Pre(var1, this, var9, var2, var4, var6))) {
         if (!var1.isUser() || this.renderManager.renderViewEntity == var1) {
            double var10 = var4;
            if (var1.isSneaking() && !(var1 instanceof EntityPlayerSP)) {
               var10 = var4 - 0.125D;
            }

            this.setModelVisibilities(var1);
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            super.doRender((EntityLivingBase)var1, var2, var10, var6, var8, var9);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
         }

         MinecraftForge.EVENT_BUS.post(new Post(var1, this, var9, var2, var4, var6));
      }
   }

   private void setModelVisibilities(AbstractClientPlayer var1) {
      ModelPlayer var2 = this.getMainModel();
      if (var1.isSpectator()) {
         var2.setInvisible(false);
         var2.bipedHead.showModel = true;
         var2.bipedHeadwear.showModel = true;
      } else {
         ItemStack var3 = var1.getHeldItemMainhand();
         ItemStack var4 = var1.getHeldItemOffhand();
         var2.setInvisible(true);
         var2.bipedHeadwear.showModel = var1.isWearing(EnumPlayerModelParts.HAT);
         var2.bipedBodyWear.showModel = var1.isWearing(EnumPlayerModelParts.JACKET);
         var2.bipedLeftLegwear.showModel = var1.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
         var2.bipedRightLegwear.showModel = var1.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
         var2.bipedLeftArmwear.showModel = var1.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
         var2.bipedRightArmwear.showModel = var1.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
         var2.isSneak = var1.isSneaking();
         ModelBiped.ArmPose var5 = ModelBiped.ArmPose.EMPTY;
         ModelBiped.ArmPose var6 = ModelBiped.ArmPose.EMPTY;
         if (var3 != null) {
            var5 = ModelBiped.ArmPose.ITEM;
            if (var1.getItemInUseCount() > 0) {
               EnumAction var7 = var3.getItemUseAction();
               if (var7 == EnumAction.BLOCK) {
                  var5 = ModelBiped.ArmPose.BLOCK;
               } else if (var7 == EnumAction.BOW) {
                  var5 = ModelBiped.ArmPose.BOW_AND_ARROW;
               }
            }
         }

         if (var4 != null) {
            var6 = ModelBiped.ArmPose.ITEM;
            if (var1.getItemInUseCount() > 0) {
               EnumAction var8 = var4.getItemUseAction();
               if (var8 == EnumAction.BLOCK) {
                  var6 = ModelBiped.ArmPose.BLOCK;
               } else if (var8 == EnumAction.BOW) {
                  var6 = ModelBiped.ArmPose.BOW_AND_ARROW;
               }
            }
         }

         if (var1.getPrimaryHand() == EnumHandSide.RIGHT) {
            var2.rightArmPose = var5;
            var2.leftArmPose = var6;
         } else {
            var2.rightArmPose = var6;
            var2.leftArmPose = var5;
         }
      }

   }

   protected ResourceLocation getEntityTexture(AbstractClientPlayer var1) {
      return var1.getLocationSkin();
   }

   public void transformHeldFull3DItemLayer() {
      GlStateManager.translate(0.0F, 0.1875F, 0.0F);
   }

   protected void preRenderCallback(AbstractClientPlayer var1, float var2) {
      float var3 = 0.9375F;
      GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
   }

   protected void renderEntityName(AbstractClientPlayer var1, double var2, double var4, double var6, String var8, double var9) {
      if (var9 < 100.0D) {
         Scoreboard var11 = var1.getWorldScoreboard();
         ScoreObjective var12 = var11.getObjectiveInDisplaySlot(2);
         if (var12 != null) {
            Score var13 = var11.getOrCreateScore(var1.getName(), var12);
            this.renderLivingLabel(var1, var13.getScorePoints() + " " + var12.getDisplayName(), var2, var4, var6, 64);
            var4 += (double)((float)this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F);
         }
      }

      super.renderEntityName(var1, var2, var4, var6, var8, var9);
   }

   public void renderRightArm(AbstractClientPlayer var1) {
      float var2 = 1.0F;
      GlStateManager.color(1.0F, 1.0F, 1.0F);
      float var3 = 0.0625F;
      ModelPlayer var4 = this.getMainModel();
      this.setModelVisibilities(var1);
      GlStateManager.enableBlend();
      var4.swingProgress = 0.0F;
      var4.isSneak = false;
      var4.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, var1);
      var4.bipedRightArm.rotateAngleX = 0.0F;
      var4.bipedRightArm.render(0.0625F);
      var4.bipedRightArmwear.rotateAngleX = 0.0F;
      var4.bipedRightArmwear.render(0.0625F);
      GlStateManager.disableBlend();
   }

   public void renderLeftArm(AbstractClientPlayer var1) {
      float var2 = 1.0F;
      GlStateManager.color(1.0F, 1.0F, 1.0F);
      float var3 = 0.0625F;
      ModelPlayer var4 = this.getMainModel();
      this.setModelVisibilities(var1);
      GlStateManager.enableBlend();
      var4.isSneak = false;
      var4.swingProgress = 0.0F;
      var4.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, var1);
      var4.bipedLeftArm.rotateAngleX = 0.0F;
      var4.bipedLeftArm.render(0.0625F);
      var4.bipedLeftArmwear.rotateAngleX = 0.0F;
      var4.bipedLeftArmwear.render(0.0625F);
      GlStateManager.disableBlend();
   }

   protected void renderLivingAt(AbstractClientPlayer var1, double var2, double var4, double var6) {
      if (var1.isEntityAlive() && var1.isPlayerSleeping()) {
         super.renderLivingAt(var1, var2 + (double)var1.renderOffsetX, var4 + (double)var1.renderOffsetY, var6 + (double)var1.renderOffsetZ);
      } else {
         super.renderLivingAt(var1, var2, var4, var6);
      }

   }

   protected void applyRotations(AbstractClientPlayer var1, float var2, float var3, float var4) {
      if (var1.isEntityAlive() && var1.isPlayerSleeping()) {
         GlStateManager.rotate(var1.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(this.getDeathMaxRotation(var1), 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
      } else if (var1.isElytraFlying()) {
         super.applyRotations(var1, var2, var3, var4);
         float var5 = (float)var1.getTicksElytraFlying() + var4;
         float var6 = MathHelper.clamp(var5 * var5 / 100.0F, 0.0F, 1.0F);
         GlStateManager.rotate(var6 * (-90.0F - var1.rotationPitch), 1.0F, 0.0F, 0.0F);
         Vec3d var7 = var1.getLook(var4);
         double var8 = var1.motionX * var1.motionX + var1.motionZ * var1.motionZ;
         double var10 = var7.xCoord * var7.xCoord + var7.zCoord * var7.zCoord;
         if (var8 > 0.0D && var10 > 0.0D) {
            double var12 = (var1.motionX * var7.xCoord + var1.motionZ * var7.zCoord) / (Math.sqrt(var8) * Math.sqrt(var10));
            double var14 = var1.motionX * var7.zCoord - var1.motionZ * var7.xCoord;
            GlStateManager.rotate((float)(Math.signum(var14) * Math.acos(var12)) * 180.0F / 3.1415927F, 0.0F, 1.0F, 0.0F);
         }
      } else {
         super.applyRotations(var1, var2, var3, var4);
      }

   }
}
