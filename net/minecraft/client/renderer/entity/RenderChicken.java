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
      super(var1, var2, var3);
   }

   protected ResourceLocation getEntityTexture(EntityChicken var1) {
      return CHICKEN_TEXTURES;
   }

   protected float handleRotationFloat(EntityChicken var1, float var2) {
      float var3 = var1.oFlap + (var1.wingRotation - var1.oFlap) * var2;
      float var4 = var1.oFlapSpeed + (var1.destPos - var1.oFlapSpeed) * var2;
      return (MathHelper.sin(var3) + 1.0F) * var4;
   }
}
