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
      super(var1, new ModelMagmaCube(), 0.25F);
   }

   protected ResourceLocation getEntityTexture(EntityMagmaCube var1) {
      return MAGMA_CUBE_TEXTURES;
   }

   protected void preRenderCallback(EntityMagmaCube var1, float var2) {
      int var3 = var1.getSlimeSize();
      float var4 = (var1.prevSquishFactor + (var1.squishFactor - var1.prevSquishFactor) * var2) / ((float)var3 * 0.5F + 1.0F);
      float var5 = 1.0F / (var4 + 1.0F);
      GlStateManager.scale(var5 * (float)var3, 1.0F / var5 * (float)var3, var5 * (float)var3);
   }
}
