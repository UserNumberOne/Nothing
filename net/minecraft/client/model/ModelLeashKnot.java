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
      this.textureWidth = var3;
      this.textureHeight = var4;
      this.knotRenderer = new ModelRenderer(this, var1, var2);
      this.knotRenderer.addBox(-3.0F, -6.0F, -3.0F, 6, 8, 6, 0.0F);
      this.knotRenderer.setRotationPoint(0.0F, 0.0F, 0.0F);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.setRotationAngles(var2, var3, var4, var5, var6, var7, var1);
      this.knotRenderer.render(var7);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      super.setRotationAngles(var1, var2, var3, var4, var5, var6, var7);
      this.knotRenderer.rotateAngleY = var4 * 0.017453292F;
      this.knotRenderer.rotateAngleX = var5 * 0.017453292F;
   }
}
