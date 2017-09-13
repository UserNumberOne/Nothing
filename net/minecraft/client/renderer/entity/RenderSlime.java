package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerSlimeGel;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSlime extends RenderLiving {
   private static final ResourceLocation SLIME_TEXTURES = new ResourceLocation("textures/entity/slime/slime.png");

   public RenderSlime(RenderManager var1, ModelBase var2, float var3) {
      super(renderManagerIn, modelBaseIn, shadowSizeIn);
      this.addLayer(new LayerSlimeGel(this));
   }

   public void doRender(EntitySlime var1, double var2, double var4, double var6, float var8, float var9) {
      this.shadowSize = 0.25F * (float)entity.getSlimeSize();
      super.doRender((EntityLiving)entity, x, y, z, entityYaw, partialTicks);
   }

   protected void preRenderCallback(EntitySlime var1, float var2) {
      float f = 0.999F;
      GlStateManager.scale(0.999F, 0.999F, 0.999F);
      float f1 = (float)entitylivingbaseIn.getSlimeSize();
      float f2 = (entitylivingbaseIn.prevSquishFactor + (entitylivingbaseIn.squishFactor - entitylivingbaseIn.prevSquishFactor) * partialTickTime) / (f1 * 0.5F + 1.0F);
      float f3 = 1.0F / (f2 + 1.0F);
      GlStateManager.scale(f3 * f1, 1.0F / f3 * f1, f3 * f1);
   }

   protected ResourceLocation getEntityTexture(EntitySlime var1) {
      return SLIME_TEXTURES;
   }
}
