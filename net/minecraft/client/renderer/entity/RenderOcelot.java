package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderOcelot extends RenderLiving {
   private static final ResourceLocation BLACK_OCELOT_TEXTURES = new ResourceLocation("textures/entity/cat/black.png");
   private static final ResourceLocation OCELOT_TEXTURES = new ResourceLocation("textures/entity/cat/ocelot.png");
   private static final ResourceLocation RED_OCELOT_TEXTURES = new ResourceLocation("textures/entity/cat/red.png");
   private static final ResourceLocation SIAMESE_OCELOT_TEXTURES = new ResourceLocation("textures/entity/cat/siamese.png");

   public RenderOcelot(RenderManager var1, ModelBase var2, float var3) {
      super(var1, var2, var3);
   }

   protected ResourceLocation getEntityTexture(EntityOcelot var1) {
      switch(var1.getTameSkin()) {
      case 0:
      default:
         return OCELOT_TEXTURES;
      case 1:
         return BLACK_OCELOT_TEXTURES;
      case 2:
         return RED_OCELOT_TEXTURES;
      case 3:
         return SIAMESE_OCELOT_TEXTURES;
      }
   }

   protected void preRenderCallback(EntityOcelot var1, float var2) {
      super.preRenderCallback(var1, var2);
      if (var1.isTamed()) {
         GlStateManager.scale(0.8F, 0.8F, 0.8F);
      }

   }
}
