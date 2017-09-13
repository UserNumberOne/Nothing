package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelEnderMite;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEndermite extends RenderLiving {
   private static final ResourceLocation ENDERMITE_TEXTURES = new ResourceLocation("textures/entity/endermite.png");

   public RenderEndermite(RenderManager var1) {
      super(renderManagerIn, new ModelEnderMite(), 0.3F);
   }

   protected float getDeathMaxRotation(EntityEndermite var1) {
      return 180.0F;
   }

   protected ResourceLocation getEntityTexture(EntityEndermite var1) {
      return ENDERMITE_TEXTURES;
   }
}
