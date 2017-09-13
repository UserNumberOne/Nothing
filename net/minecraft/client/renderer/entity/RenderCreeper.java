package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCreeper extends RenderLiving {
   private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");

   public RenderCreeper(RenderManager var1) {
      super(renderManagerIn, new ModelCreeper(), 0.5F);
      this.addLayer(new LayerCreeperCharge(this));
   }

   protected void preRenderCallback(EntityCreeper var1, float var2) {
      float f = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);
      float f1 = 1.0F + MathHelper.sin(f * 100.0F) * f * 0.01F;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      f = f * f;
      f = f * f;
      float f2 = (1.0F + f * 0.4F) * f1;
      float f3 = (1.0F + f * 0.1F) / f1;
      GlStateManager.scale(f2, f3, f2);
   }

   protected int getColorMultiplier(EntityCreeper var1, float var2, float var3) {
      float f = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);
      if ((int)(f * 10.0F) % 2 == 0) {
         return 0;
      } else {
         int i = (int)(f * 0.2F * 255.0F);
         i = MathHelper.clamp(i, 0, 255);
         return i << 24 | 822083583;
      }
   }

   protected ResourceLocation getEntityTexture(EntityCreeper var1) {
      return CREEPER_TEXTURES;
   }
}
