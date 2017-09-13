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
      this.leftWing.render(var7);
      this.rightWing.render(var7);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      super.setRotationAngles(var1, var2, var3, var4, var5, var6, var7);
      float var8 = 0.2617994F;
      float var9 = -0.2617994F;
      float var10 = 0.0F;
      float var11 = 0.0F;
      if (var7 instanceof EntityLivingBase && ((EntityLivingBase)var7).isElytraFlying()) {
         float var12 = 1.0F;
         if (var7.motionY < 0.0D) {
            Vec3d var13 = (new Vec3d(var7.motionX, var7.motionY, var7.motionZ)).normalize();
            var12 = 1.0F - (float)Math.pow(-var13.yCoord, 1.5D);
         }

         var8 = var12 * 0.34906584F + (1.0F - var12) * var8;
         var9 = var12 * -1.5707964F + (1.0F - var12) * var9;
      } else if (var7.isSneaking()) {
         var8 = 0.69813174F;
         var9 = -0.7853982F;
         var10 = 3.0F;
         var11 = 0.08726646F;
      }

      this.leftWing.rotationPointX = 5.0F;
      this.leftWing.rotationPointY = var10;
      if (var7 instanceof AbstractClientPlayer) {
         AbstractClientPlayer var14 = (AbstractClientPlayer)var7;
         var14.rotateElytraX = (float)((double)var14.rotateElytraX + (double)(var8 - var14.rotateElytraX) * 0.1D);
         var14.rotateElytraY = (float)((double)var14.rotateElytraY + (double)(var11 - var14.rotateElytraY) * 0.1D);
         var14.rotateElytraZ = (float)((double)var14.rotateElytraZ + (double)(var9 - var14.rotateElytraZ) * 0.1D);
         this.leftWing.rotateAngleX = var14.rotateElytraX;
         this.leftWing.rotateAngleY = var14.rotateElytraY;
         this.leftWing.rotateAngleZ = var14.rotateElytraZ;
      } else {
         this.leftWing.rotateAngleX = var8;
         this.leftWing.rotateAngleZ = var9;
         this.leftWing.rotateAngleY = var11;
      }

      this.rightWing.rotationPointX = -this.leftWing.rotationPointX;
      this.rightWing.rotateAngleY = -this.leftWing.rotateAngleY;
      this.rightWing.rotationPointY = this.leftWing.rotationPointY;
      this.rightWing.rotateAngleX = this.leftWing.rotateAngleX;
      this.rightWing.rotateAngleZ = -this.leftWing.rotateAngleZ;
   }

   public void setLivingAnimations(EntityLivingBase var1, float var2, float var3, float var4) {
      super.setLivingAnimations(var1, var2, var3, var4);
   }
}
