package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBlaze;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlaze extends RenderLiving {
   private static final ResourceLocation BLAZE_TEXTURES = new ResourceLocation("textures/entity/blaze.png");

   public RenderBlaze(RenderManager var1) {
      super(var1, new ModelBlaze(), 0.5F);
   }

   protected ResourceLocation getEntityTexture(EntityBlaze var1) {
      return BLAZE_TEXTURES;
   }
}
