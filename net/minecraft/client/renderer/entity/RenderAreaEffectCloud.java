package net.minecraft.client.renderer.entity;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAreaEffectCloud extends Render {
   public RenderAreaEffectCloud(RenderManager manager) {
      super(manager);
   }

   public void doRender(EntityAreaEffectCloud entity, double x, double y, double z, float entityYaw, float partialTicks) {
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityAreaEffectCloud entity) {
      return null;
   }
}
