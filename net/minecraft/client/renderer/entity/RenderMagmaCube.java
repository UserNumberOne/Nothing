package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelMagmaCube;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMagmaCube extends RenderLiving {
   private static final ResourceLocation MAGMA_CUBE_TEXTURES = new ResourceLocation("textures/entity/slime/magmacube.png");

   public RenderMagmaCube(RenderManager var1) {
      super(renderManagerIn, new ModelMagmaCube(), 0.25F);
   }

   protected ResourceLocation getEntityTexture(EntityMagmaCube var1) {
      return MAGMA_CUBE_TEXTURES;
   }

   protected void preRenderCallback(EntityMagmaCube var1, float var2) {
      int i = entitylivingbaseIn.getSlimeSize();
      float f = (entitylivingbaseIn.prevSquishFactor + (entitylivingbaseIn.squishFactor - entitylivingbaseIn.prevSquishFactor) * partialTickTime) / ((float)i * 0.5F + 1.0F);
      float f1 = 1.0F / (f + 1.0F);
      GlStateManager.scale(f1 * (float)i, 1.0F / f1 * (float)i, f1 * (float)i);
   }
}
