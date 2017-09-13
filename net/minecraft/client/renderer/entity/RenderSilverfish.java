package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelSilverfish;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSilverfish extends RenderLiving {
   private static final ResourceLocation SILVERFISH_TEXTURES = new ResourceLocation("textures/entity/silverfish.png");

   public RenderSilverfish(RenderManager var1) {
      super(var1, new ModelSilverfish(), 0.3F);
   }

   protected float getDeathMaxRotation(EntitySilverfish var1) {
      return 180.0F;
   }

   protected ResourceLocation getEntityTexture(EntitySilverfish var1) {
      return SILVERFISH_TEXTURES;
   }
}
