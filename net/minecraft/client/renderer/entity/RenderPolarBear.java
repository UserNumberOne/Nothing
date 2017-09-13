package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPolarBear extends RenderLiving {
   private static final ResourceLocation POLAR_BEAR_TEXTURE = new ResourceLocation("textures/entity/bear/polarbear.png");

   public RenderPolarBear(RenderManager var1, ModelBase var2, float var3) {
      super(p_i47132_1_, p_i47132_2_, p_i47132_3_);
   }

   protected ResourceLocation getEntityTexture(EntityPolarBear var1) {
      return POLAR_BEAR_TEXTURE;
   }

   public void doRender(EntityPolarBear var1, double var2, double var4, double var6, float var8, float var9) {
      super.doRender((EntityLiving)entity, x, y, z, entityYaw, partialTicks);
   }

   protected void preRenderCallback(EntityPolarBear var1, float var2) {
      GlStateManager.scale(1.2F, 1.2F, 1.2F);
      super.preRenderCallback(entitylivingbaseIn, partialTickTime);
   }
}
