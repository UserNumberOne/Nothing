package net.minecraft.client.renderer.entity;

import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTippedArrow extends RenderArrow {
   public static final ResourceLocation RES_ARROW = new ResourceLocation("textures/entity/projectiles/arrow.png");
   public static final ResourceLocation RES_TIPPED_ARROW = new ResourceLocation("textures/entity/projectiles/tipped_arrow.png");

   public RenderTippedArrow(RenderManager var1) {
      super(manager);
   }

   protected ResourceLocation getEntityTexture(EntityTippedArrow var1) {
      return entity.getColor() > 0 ? RES_TIPPED_ARROW : RES_ARROW;
   }
}
