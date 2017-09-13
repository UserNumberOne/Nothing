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
      super(var1, var2, var3);
      this.addLayer(new LayerSlimeGel(this));
   }

   public void doRender(EntitySlime var1, double var2, double var4, double var6, float var8, float var9) {
      this.shadowSize = 0.25F * (float)var1.getSlimeSize();
      super.doRender((EntityLiving)var1, var2, var4, var6, var8, var9);
   }

   protected void preRenderCallback(EntitySlime var1, float var2) {
      float var3 = 0.999F;
      GlStateManager.scale(0.999F, 0.999F, 0.999F);
      float var4 = (float)var1.getSlimeSize();
      float var5 = (var1.prevSquishFactor + (var1.squishFactor - var1.prevSquishFactor) * var2) / (var4 * 0.5F + 1.0F);
      float var6 = 1.0F / (var5 + 1.0F);
      GlStateManager.scale(var6 * var4, 1.0F / var6 * var4, var6 * var4);
   }

   protected ResourceLocation getEntityTexture(EntitySlime var1) {
      return SLIME_TEXTURES;
   }
}
