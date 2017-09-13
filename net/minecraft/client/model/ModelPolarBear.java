package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPolarBear extends ModelQuadruped {
   public ModelPolarBear() {
      super(12, 0.0F);
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.head = new ModelRenderer(this, 0, 0);
      this.head.addBox(-3.5F, -3.0F, -3.0F, 7, 7, 7, 0.0F);
      this.head.setRotationPoint(0.0F, 10.0F, -16.0F);
      this.head.setTextureOffset(0, 44).addBox(-2.5F, 1.0F, -6.0F, 5, 3, 3, 0.0F);
      this.head.setTextureOffset(26, 0).addBox(-4.5F, -4.0F, -1.0F, 2, 2, 1, 0.0F);
      ModelRenderer var1 = this.head.setTextureOffset(26, 0);
      var1.mirror = true;
      var1.addBox(2.5F, -4.0F, -1.0F, 2, 2, 1, 0.0F);
      this.body = new ModelRenderer(this);
      this.body.setTextureOffset(0, 19).addBox(-5.0F, -13.0F, -7.0F, 14, 14, 11, 0.0F);
      this.body.setTextureOffset(39, 0).addBox(-4.0F, -25.0F, -7.0F, 12, 12, 10, 0.0F);
      this.body.setRotationPoint(-2.0F, 9.0F, 12.0F);
      boolean var2 = true;
      this.leg1 = new ModelRenderer(this, 50, 22);
      this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 10, 8, 0.0F);
      this.leg1.setRotationPoint(-3.5F, 14.0F, 6.0F);
      this.leg2 = new ModelRenderer(this, 50, 22);
      this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 10, 8, 0.0F);
      this.leg2.setRotationPoint(3.5F, 14.0F, 6.0F);
      this.leg3 = new ModelRenderer(this, 50, 40);
      this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, 10, 6, 0.0F);
      this.leg3.setRotationPoint(-2.5F, 14.0F, -7.0F);
      this.leg4 = new ModelRenderer(this, 50, 40);
      this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, 10, 6, 0.0F);
      this.leg4.setRotationPoint(2.5F, 14.0F, -7.0F);
      --this.leg1.rotationPointX;
      ++this.leg2.rotationPointX;
      this.leg1.rotationPointZ += 0.0F;
      this.leg2.rotationPointZ += 0.0F;
      --this.leg3.rotationPointX;
      ++this.leg4.rotationPointX;
      --this.leg3.rotationPointZ;
      --this.leg4.rotationPointZ;
      this.childZOffset += 2.0F;
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.setRotationAngles(var2, var3, var4, var5, var6, var7, var1);
      if (this.isChild) {
         float var8 = 2.0F;
         this.childYOffset = 16.0F;
         this.childZOffset = 4.0F;
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.6666667F, 0.6666667F, 0.6666667F);
         GlStateManager.translate(0.0F, this.childYOffset * var7, this.childZOffset * var7);
         this.head.render(var7);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         GlStateManager.translate(0.0F, 24.0F * var7, 0.0F);
         this.body.render(var7);
         this.leg1.render(var7);
         this.leg2.render(var7);
         this.leg3.render(var7);
         this.leg4.render(var7);
         GlStateManager.popMatrix();
      } else {
         this.head.render(var7);
         this.body.render(var7);
         this.leg1.render(var7);
         this.leg2.render(var7);
         this.leg3.render(var7);
         this.leg4.render(var7);
      }

   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      super.setRotationAngles(var1, var2, var3, var4, var5, var6, var7);
      float var8 = var3 - (float)var7.ticksExisted;
      float var9 = ((EntityPolarBear)var7).getStandingAnimationScale(var8);
      var9 = var9 * var9;
      float var10 = 1.0F - var9;
      this.body.rotateAngleX = 1.5707964F - var9 * 3.1415927F * 0.35F;
      this.body.rotationPointY = 9.0F * var10 + 11.0F * var9;
      this.leg3.rotationPointY = 14.0F * var10 + -6.0F * var9;
      this.leg3.rotationPointZ = -8.0F * var10 + -4.0F * var9;
      this.leg3.rotateAngleX -= var9 * 3.1415927F * 0.45F;
      this.leg4.rotationPointY = this.leg3.rotationPointY;
      this.leg4.rotationPointZ = this.leg3.rotationPointZ;
      this.leg4.rotateAngleX -= var9 * 3.1415927F * 0.45F;
      this.head.rotationPointY = 10.0F * var10 + -12.0F * var9;
      this.head.rotationPointZ = -16.0F * var10 + -3.0F * var9;
      this.head.rotateAngleX += var9 * 3.1415927F * 0.15F;
   }
}
