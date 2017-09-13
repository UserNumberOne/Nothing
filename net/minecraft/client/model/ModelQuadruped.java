package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelQuadruped extends ModelBase {
   public ModelRenderer head = new ModelRenderer(this, 0, 0);
   public ModelRenderer body;
   public ModelRenderer leg1;
   public ModelRenderer leg2;
   public ModelRenderer leg3;
   public ModelRenderer leg4;
   protected float childYOffset = 8.0F;
   protected float childZOffset = 4.0F;

   public ModelQuadruped(int var1, float var2) {
      this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, scale);
      this.head.setRotationPoint(0.0F, (float)(18 - height), -6.0F);
      this.body = new ModelRenderer(this, 28, 8);
      this.body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, scale);
      this.body.setRotationPoint(0.0F, (float)(17 - height), 2.0F);
      this.leg1 = new ModelRenderer(this, 0, 16);
      this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, height, 4, scale);
      this.leg1.setRotationPoint(-3.0F, (float)(24 - height), 7.0F);
      this.leg2 = new ModelRenderer(this, 0, 16);
      this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, height, 4, scale);
      this.leg2.setRotationPoint(3.0F, (float)(24 - height), 7.0F);
      this.leg3 = new ModelRenderer(this, 0, 16);
      this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, height, 4, scale);
      this.leg3.setRotationPoint(-3.0F, (float)(24 - height), -5.0F);
      this.leg4 = new ModelRenderer(this, 0, 16);
      this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, height, 4, scale);
      this.leg4.setRotationPoint(3.0F, (float)(24 - height), -5.0F);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
      if (this.isChild) {
         float f = 2.0F;
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, this.childYOffset * scale, this.childZOffset * scale);
         this.head.render(scale);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
         this.body.render(scale);
         this.leg1.render(scale);
         this.leg2.render(scale);
         this.leg3.render(scale);
         this.leg4.render(scale);
         GlStateManager.popMatrix();
      } else {
         this.head.render(scale);
         this.body.render(scale);
         this.leg1.render(scale);
         this.leg2.render(scale);
         this.leg3.render(scale);
         this.leg4.render(scale);
      }

   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      this.head.rotateAngleX = headPitch * 0.017453292F;
      this.head.rotateAngleY = netHeadYaw * 0.017453292F;
      this.body.rotateAngleX = 1.5707964F;
      this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
      this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
      this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
   }
}
