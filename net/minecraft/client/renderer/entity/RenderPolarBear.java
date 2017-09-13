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

   public RenderPolarBear(RenderManager p_i47132_1_, ModelBase p_i47132_2_, float p_i47132_3_) {
      super(p_i47132_1_, p_i47132_2_, p_i47132_3_);
   }

   protected ResourceLocation getEntityTexture(EntityPolarBear entity) {
      return POLAR_BEAR_TEXTURE;
   }

   public void doRender(EntityPolarBear entity, double x, double y, double z, float entityYaw, float partialTicks) {
      super.doRender((EntityLiving)entity, x, y, z, entityYaw, partialTicks);
   }

   protected void preRenderCallback(EntityPolarBear entitylivingbaseIn, float partialTickTime) {
      GlStateManager.scale(1.2F, 1.2F, 1.2F);
      super.preRenderCallback(entitylivingbaseIn, partialTickTime);
   }
}
