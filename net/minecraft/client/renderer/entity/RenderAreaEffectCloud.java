package net.minecraft.client.renderer.entity;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAreaEffectCloud extends Render {
   public RenderAreaEffectCloud(RenderManager var1) {
      super(manager);
   }

   public void doRender(EntityAreaEffectCloud var1, double var2, double var4, double var6, float var8, float var9) {
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityAreaEffectCloud var1) {
      return null;
   }
}
