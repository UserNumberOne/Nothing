package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChicken extends RenderLiving {
   private static final ResourceLocation CHICKEN_TEXTURES = new ResourceLocation("textures/entity/chicken.png");

   public RenderChicken(RenderManager var1, ModelBase var2, float var3) {
      super(renderManagerIn, modelBaseIn, shadowSizeIn);
   }

   protected ResourceLocation getEntityTexture(EntityChicken var1) {
      return CHICKEN_TEXTURES;
   }

   protected float handleRotationFloat(EntityChicken var1, float var2) {
      float f = livingBase.oFlap + (livingBase.wingRotation - livingBase.oFlap) * partialTicks;
      float f1 = livingBase.oFlapSpeed + (livingBase.destPos - livingBase.oFlapSpeed) * partialTicks;
      return (MathHelper.sin(f) + 1.0F) * f1;
   }
}
