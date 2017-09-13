package net.minecraft.client.model;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBoat extends ModelBase implements IMultipassModel {
   public ModelRenderer[] boatSides = new ModelRenderer[5];
   public ModelRenderer[] paddles = new ModelRenderer[2];
   public ModelRenderer noWater;
   private final int patchList = GLAllocation.generateDisplayLists(1);

   public ModelBoat() {
      this.boatSides[0] = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
      this.boatSides[1] = (new ModelRenderer(this, 0, 19)).setTextureSize(128, 64);
      this.boatSides[2] = (new ModelRenderer(this, 0, 27)).setTextureSize(128, 64);
      this.boatSides[3] = (new ModelRenderer(this, 0, 35)).setTextureSize(128, 64);
      this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
      int i = 32;
      int j = 6;
      int k = 20;
      int l = 4;
      int i1 = 28;
      this.boatSides[0].addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
      this.boatSides[0].setRotationPoint(0.0F, 3.0F, 1.0F);
      this.boatSides[1].addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
      this.boatSides[1].setRotationPoint(-15.0F, 4.0F, 4.0F);
      this.boatSides[2].addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
      this.boatSides[2].setRotationPoint(15.0F, 4.0F, 0.0F);
      this.boatSides[3].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
      this.boatSides[3].setRotationPoint(0.0F, 4.0F, -9.0F);
      this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
      this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);
      this.boatSides[0].rotateAngleX = 1.5707964F;
      this.boatSides[1].rotateAngleY = 4.712389F;
      this.boatSides[2].rotateAngleY = 1.5707964F;
      this.boatSides[3].rotateAngleY = 3.1415927F;
      this.paddles[0] = this.makePaddle(true);
      this.paddles[0].setRotationPoint(3.0F, -5.0F, 9.0F);
      this.paddles[1] = this.makePaddle(false);
      this.paddles[1].setRotationPoint(3.0F, -5.0F, -9.0F);
      this.paddles[1].rotateAngleY = 3.1415927F;
      this.paddles[0].rotateAngleZ = 0.19634955F;
      this.paddles[1].rotateAngleZ = 0.19634955F;
      this.noWater = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
      this.noWater.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
      this.noWater.setRotationPoint(0.0F, -3.0F, 1.0F);
      this.noWater.rotateAngleX = 1.5707964F;
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      EntityBoat entityboat = (EntityBoat)entityIn;
      this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

      for(int i = 0; i < 5; ++i) {
         this.boatSides[i].render(scale);
      }

      this.renderPaddle(entityboat, 0, scale, limbSwing);
      this.renderPaddle(entityboat, 1, scale, limbSwing);
   }

   public void renderMultipass(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.colorMask(false, false, false, false);
      this.noWater.render(scale);
      GlStateManager.colorMask(true, true, true, true);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
   }

   ModelRenderer makePaddle(boolean var1) {
      ModelRenderer modelrenderer = (new ModelRenderer(this, 62, p_187056_1_ ? 0 : 20)).setTextureSize(128, 64);
      int i = 20;
      int j = 7;
      int k = 6;
      float f = -5.0F;
      modelrenderer.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
      modelrenderer.addBox(p_187056_1_ ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
      return modelrenderer;
   }

   void renderPaddle(EntityBoat var1, int var2, float var3, float var4) {
      float f = 40.0F;
      float f1 = boat.getRowingTime(paddle, limbSwing) * 40.0F;
      ModelRenderer modelrenderer = this.paddles[paddle];
      modelrenderer.rotateAngleX = (float)MathHelper.clampedLerp(-1.0471975803375244D, -0.2617993950843811D, (double)((MathHelper.sin(-f1) + 1.0F) / 2.0F));
      modelrenderer.rotateAngleY = (float)MathHelper.clampedLerp(-0.7853981633974483D, 0.7853981633974483D, (double)((MathHelper.sin(-f1 + 1.0F) + 1.0F) / 2.0F));
      if (paddle == 1) {
         modelrenderer.rotateAngleY = 3.1415927F - modelrenderer.rotateAngleY;
      }

      modelrenderer.render(scale);
   }
}
