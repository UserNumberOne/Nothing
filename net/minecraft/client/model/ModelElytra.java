package net.minecraft.client.model;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelElytra extends ModelBase {
   private final ModelRenderer rightWing;
   private final ModelRenderer leftWing = new ModelRenderer(this, 22, 0);

   public ModelElytra() {
      this.leftWing.addBox(-10.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
      this.rightWing = new ModelRenderer(this, 22, 0);
      this.rightWing.mirror = true;
      this.rightWing.addBox(0.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.disableRescaleNormal();
      GlStateManager.disableCull();
      this.leftWing.render(scale);
      this.rightWing.render(scale);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
      float f = 0.2617994F;
      float f1 = -0.2617994F;
      float f2 = 0.0F;
      float f3 = 0.0F;
      if (entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isElytraFlying()) {
         float f4 = 1.0F;
         if (entityIn.motionY < 0.0D) {
            Vec3d vec3d = (new Vec3d(entityIn.motionX, entityIn.motionY, entityIn.motionZ)).normalize();
            f4 = 1.0F - (float)Math.pow(-vec3d.yCoord, 1.5D);
         }

         f = f4 * 0.34906584F + (1.0F - f4) * f;
         f1 = f4 * -1.5707964F + (1.0F - f4) * f1;
      } else if (entityIn.isSneaking()) {
         f = 0.69813174F;
         f1 = -0.7853982F;
         f2 = 3.0F;
         f3 = 0.08726646F;
      }

      this.leftWing.rotationPointX = 5.0F;
      this.leftWing.rotationPointY = f2;
      if (entityIn instanceof AbstractClientPlayer) {
         AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)entityIn;
         abstractclientplayer.rotateElytraX = (float)((double)abstractclientplayer.rotateElytraX + (double)(f - abstractclientplayer.rotateElytraX) * 0.1D);
         abstractclientplayer.rotateElytraY = (float)((double)abstractclientplayer.rotateElytraY + (double)(f3 - abstractclientplayer.rotateElytraY) * 0.1D);
         abstractclientplayer.rotateElytraZ = (float)((double)abstractclientplayer.rotateElytraZ + (double)(f1 - abstractclientplayer.rotateElytraZ) * 0.1D);
         this.leftWing.rotateAngleX = abstractclientplayer.rotateElytraX;
         this.leftWing.rotateAngleY = abstractclientplayer.rotateElytraY;
         this.leftWing.rotateAngleZ = abstractclientplayer.rotateElytraZ;
      } else {
         this.leftWing.rotateAngleX = f;
         this.leftWing.rotateAngleZ = f1;
         this.leftWing.rotateAngleY = f3;
      }

      this.rightWing.rotationPointX = -this.leftWing.rotationPointX;
      this.rightWing.rotateAngleY = -this.leftWing.rotateAngleY;
      this.rightWing.rotationPointY = this.leftWing.rotationPointY;
      this.rightWing.rotateAngleX = this.leftWing.rotateAngleX;
      this.rightWing.rotateAngleZ = -this.leftWing.rotateAngleZ;
   }

   public void setLivingAnimations(EntityLivingBase var1, float var2, float var3, float var4) {
      super.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
   }
}
