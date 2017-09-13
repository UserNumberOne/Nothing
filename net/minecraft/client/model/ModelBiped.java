package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBiped extends ModelBase {
   public ModelRenderer bipedHead;
   public ModelRenderer bipedHeadwear;
   public ModelRenderer bipedBody;
   public ModelRenderer bipedRightArm;
   public ModelRenderer bipedLeftArm;
   public ModelRenderer bipedRightLeg;
   public ModelRenderer bipedLeftLeg;
   public ModelBiped.ArmPose leftArmPose;
   public ModelBiped.ArmPose rightArmPose;
   public boolean isSneak;

   public ModelBiped() {
      this(0.0F);
   }

   public ModelBiped(float var1) {
      this(var1, 0.0F, 64, 32);
   }

   public ModelBiped(float var1, float var2, int var3, int var4) {
      this.leftArmPose = ModelBiped.ArmPose.EMPTY;
      this.rightArmPose = ModelBiped.ArmPose.EMPTY;
      this.textureWidth = var3;
      this.textureHeight = var4;
      this.bipedHead = new ModelRenderer(this, 0, 0);
      this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, var1);
      this.bipedHead.setRotationPoint(0.0F, 0.0F + var2, 0.0F);
      this.bipedHeadwear = new ModelRenderer(this, 32, 0);
      this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, var1 + 0.5F);
      this.bipedHeadwear.setRotationPoint(0.0F, 0.0F + var2, 0.0F);
      this.bipedBody = new ModelRenderer(this, 16, 16);
      this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, var1);
      this.bipedBody.setRotationPoint(0.0F, 0.0F + var2, 0.0F);
      this.bipedRightArm = new ModelRenderer(this, 40, 16);
      this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, var1);
      this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + var2, 0.0F);
      this.bipedLeftArm = new ModelRenderer(this, 40, 16);
      this.bipedLeftArm.mirror = true;
      this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, var1);
      this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + var2, 0.0F);
      this.bipedRightLeg = new ModelRenderer(this, 0, 16);
      this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, var1);
      this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F + var2, 0.0F);
      this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
      this.bipedLeftLeg.mirror = true;
      this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, var1);
      this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F + var2, 0.0F);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.setRotationAngles(var2, var3, var4, var5, var6, var7, var1);
      GlStateManager.pushMatrix();
      if (this.isChild) {
         float var8 = 2.0F;
         GlStateManager.scale(0.75F, 0.75F, 0.75F);
         GlStateManager.translate(0.0F, 16.0F * var7, 0.0F);
         this.bipedHead.render(var7);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         GlStateManager.translate(0.0F, 24.0F * var7, 0.0F);
         this.bipedBody.render(var7);
         this.bipedRightArm.render(var7);
         this.bipedLeftArm.render(var7);
         this.bipedRightLeg.render(var7);
         this.bipedLeftLeg.render(var7);
         this.bipedHeadwear.render(var7);
      } else {
         if (var1.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
         }

         this.bipedHead.render(var7);
         this.bipedBody.render(var7);
         this.bipedRightArm.render(var7);
         this.bipedLeftArm.render(var7);
         this.bipedRightLeg.render(var7);
         this.bipedLeftLeg.render(var7);
         this.bipedHeadwear.render(var7);
      }

      GlStateManager.popMatrix();
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      boolean var8 = var7 instanceof EntityLivingBase && ((EntityLivingBase)var7).getTicksElytraFlying() > 4;
      this.bipedHead.rotateAngleY = var4 * 0.017453292F;
      if (var8) {
         this.bipedHead.rotateAngleX = -0.7853982F;
      } else {
         this.bipedHead.rotateAngleX = var5 * 0.017453292F;
      }

      this.bipedBody.rotateAngleY = 0.0F;
      this.bipedRightArm.rotationPointZ = 0.0F;
      this.bipedRightArm.rotationPointX = -5.0F;
      this.bipedLeftArm.rotationPointZ = 0.0F;
      this.bipedLeftArm.rotationPointX = 5.0F;
      float var9 = 1.0F;
      if (var8) {
         var9 = (float)(var7.motionX * var7.motionX + var7.motionY * var7.motionY + var7.motionZ * var7.motionZ);
         var9 = var9 / 0.2F;
         var9 = var9 * var9 * var9;
      }

      if (var9 < 1.0F) {
         var9 = 1.0F;
      }

      this.bipedRightArm.rotateAngleX = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 2.0F * var2 * 0.5F / var9;
      this.bipedLeftArm.rotateAngleX = MathHelper.cos(var1 * 0.6662F) * 2.0F * var2 * 0.5F / var9;
      this.bipedRightArm.rotateAngleZ = 0.0F;
      this.bipedLeftArm.rotateAngleZ = 0.0F;
      this.bipedRightLeg.rotateAngleX = MathHelper.cos(var1 * 0.6662F) * 1.4F * var2 / var9;
      this.bipedLeftLeg.rotateAngleX = MathHelper.cos(var1 * 0.6662F + 3.1415927F) * 1.4F * var2 / var9;
      this.bipedRightLeg.rotateAngleY = 0.0F;
      this.bipedLeftLeg.rotateAngleY = 0.0F;
      this.bipedRightLeg.rotateAngleZ = 0.0F;
      this.bipedLeftLeg.rotateAngleZ = 0.0F;
      if (this.isRiding) {
         this.bipedRightArm.rotateAngleX += -0.62831855F;
         this.bipedLeftArm.rotateAngleX += -0.62831855F;
         this.bipedRightLeg.rotateAngleX = -1.4137167F;
         this.bipedRightLeg.rotateAngleY = 0.31415927F;
         this.bipedRightLeg.rotateAngleZ = 0.07853982F;
         this.bipedLeftLeg.rotateAngleX = -1.4137167F;
         this.bipedLeftLeg.rotateAngleY = -0.31415927F;
         this.bipedLeftLeg.rotateAngleZ = -0.07853982F;
      }

      this.bipedRightArm.rotateAngleY = 0.0F;
      this.bipedRightArm.rotateAngleZ = 0.0F;
      switch(this.leftArmPose) {
      case EMPTY:
         this.bipedLeftArm.rotateAngleY = 0.0F;
         break;
      case BLOCK:
         this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - 0.9424779F;
         this.bipedLeftArm.rotateAngleY = 0.5235988F;
         break;
      case ITEM:
         this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - 0.31415927F;
         this.bipedLeftArm.rotateAngleY = 0.0F;
      }

      switch(this.rightArmPose) {
      case EMPTY:
         this.bipedRightArm.rotateAngleY = 0.0F;
         break;
      case BLOCK:
         this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - 0.9424779F;
         this.bipedRightArm.rotateAngleY = -0.5235988F;
         break;
      case ITEM:
         this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - 0.31415927F;
         this.bipedRightArm.rotateAngleY = 0.0F;
      }

      if (this.swingProgress > 0.0F) {
         EnumHandSide var10 = this.getMainHand(var7);
         ModelRenderer var11 = this.getArmForSide(var10);
         float var12 = this.swingProgress;
         this.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(var12) * 6.2831855F) * 0.2F;
         if (var10 == EnumHandSide.LEFT) {
            this.bipedBody.rotateAngleY *= -1.0F;
         }

         this.bipedRightArm.rotationPointZ = MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
         this.bipedRightArm.rotationPointX = -MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
         this.bipedLeftArm.rotationPointZ = -MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
         this.bipedLeftArm.rotationPointX = MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
         this.bipedRightArm.rotateAngleY += this.bipedBody.rotateAngleY;
         this.bipedLeftArm.rotateAngleY += this.bipedBody.rotateAngleY;
         this.bipedLeftArm.rotateAngleX += this.bipedBody.rotateAngleY;
         var12 = 1.0F - this.swingProgress;
         var12 = var12 * var12;
         var12 = var12 * var12;
         var12 = 1.0F - var12;
         float var13 = MathHelper.sin(var12 * 3.1415927F);
         float var14 = MathHelper.sin(this.swingProgress * 3.1415927F) * -(this.bipedHead.rotateAngleX - 0.7F) * 0.75F;
         var11.rotateAngleX = (float)((double)var11.rotateAngleX - ((double)var13 * 1.2D + (double)var14));
         var11.rotateAngleY += this.bipedBody.rotateAngleY * 2.0F;
         var11.rotateAngleZ += MathHelper.sin(this.swingProgress * 3.1415927F) * -0.4F;
      }

      if (this.isSneak) {
         this.bipedBody.rotateAngleX = 0.5F;
         this.bipedRightArm.rotateAngleX += 0.4F;
         this.bipedLeftArm.rotateAngleX += 0.4F;
         this.bipedRightLeg.rotationPointZ = 4.0F;
         this.bipedLeftLeg.rotationPointZ = 4.0F;
         this.bipedRightLeg.rotationPointY = 9.0F;
         this.bipedLeftLeg.rotationPointY = 9.0F;
         this.bipedHead.rotationPointY = 1.0F;
      } else {
         this.bipedBody.rotateAngleX = 0.0F;
         this.bipedRightLeg.rotationPointZ = 0.1F;
         this.bipedLeftLeg.rotationPointZ = 0.1F;
         this.bipedRightLeg.rotationPointY = 12.0F;
         this.bipedLeftLeg.rotationPointY = 12.0F;
         this.bipedHead.rotationPointY = 0.0F;
      }

      this.bipedRightArm.rotateAngleZ += MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
      this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
      this.bipedRightArm.rotateAngleX += MathHelper.sin(var3 * 0.067F) * 0.05F;
      this.bipedLeftArm.rotateAngleX -= MathHelper.sin(var3 * 0.067F) * 0.05F;
      if (this.rightArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
         this.bipedRightArm.rotateAngleY = -0.1F + this.bipedHead.rotateAngleY;
         this.bipedLeftArm.rotateAngleY = 0.1F + this.bipedHead.rotateAngleY + 0.4F;
         this.bipedRightArm.rotateAngleX = -1.5707964F + this.bipedHead.rotateAngleX;
         this.bipedLeftArm.rotateAngleX = -1.5707964F + this.bipedHead.rotateAngleX;
      } else if (this.leftArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
         this.bipedRightArm.rotateAngleY = -0.1F + this.bipedHead.rotateAngleY - 0.4F;
         this.bipedLeftArm.rotateAngleY = 0.1F + this.bipedHead.rotateAngleY;
         this.bipedRightArm.rotateAngleX = -1.5707964F + this.bipedHead.rotateAngleX;
         this.bipedLeftArm.rotateAngleX = -1.5707964F + this.bipedHead.rotateAngleX;
      }

      copyModelAngles(this.bipedHead, this.bipedHeadwear);
   }

   public void setModelAttributes(ModelBase var1) {
      super.setModelAttributes(var1);
      if (var1 instanceof ModelBiped) {
         ModelBiped var2 = (ModelBiped)var1;
         this.leftArmPose = var2.leftArmPose;
         this.rightArmPose = var2.rightArmPose;
         this.isSneak = var2.isSneak;
      }

   }

   public void setInvisible(boolean var1) {
      this.bipedHead.showModel = var1;
      this.bipedHeadwear.showModel = var1;
      this.bipedBody.showModel = var1;
      this.bipedRightArm.showModel = var1;
      this.bipedLeftArm.showModel = var1;
      this.bipedRightLeg.showModel = var1;
      this.bipedLeftLeg.showModel = var1;
   }

   public void postRenderArm(float var1, EnumHandSide var2) {
      this.getArmForSide(var2).postRender(var1);
   }

   protected ModelRenderer getArmForSide(EnumHandSide var1) {
      return var1 == EnumHandSide.LEFT ? this.bipedLeftArm : this.bipedRightArm;
   }

   protected EnumHandSide getMainHand(Entity var1) {
      return var1 instanceof EntityLivingBase ? ((EntityLivingBase)var1).getPrimaryHand() : EnumHandSide.RIGHT;
   }

   @SideOnly(Side.CLIENT)
   public static enum ArmPose {
      EMPTY,
      ITEM,
      BLOCK,
      BOW_AND_ARROW;
   }
}
