package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCow extends RenderLiving {
   private static final ResourceLocation COW_TEXTURES = new ResourceLocation("textures/entity/cow/cow.png");

   public RenderCow(RenderManager var1, ModelBase var2, float var3) {
      super(renderManagerIn, modelBaseIn, shadowSizeIn);
   }

   protected ResourceLocation getEntityTexture(EntityCow var1) {
      return COW_TEXTURES;
   }
}
