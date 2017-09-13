package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelLeashKnot extends ModelBase {
   public ModelRenderer knotRenderer;

   public ModelLeashKnot() {
      this(0, 0, 32, 32);
   }

   public ModelLeashKnot(int var1, int var2, int var3, int var4) {
      this.textureWidth = p_i46365_3_;
      this.textureHeight = p_i46365_4_;
      this.knotRenderer = new ModelRenderer(this, p_i46365_1_, p_i46365_2_);
      this.knotRenderer.addBox(-3.0F, -6.0F, -3.0F, 6, 8, 6, 0.0F);
      this.knotRenderer.setRotationPoint(0.0F, 0.0F, 0.0F);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
      this.knotRenderer.render(scale);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
      this.knotRenderer.rotateAngleY = netHeadYaw * 0.017453292F;
      this.knotRenderer.rotateAngleX = headPitch * 0.017453292F;
   }
}
