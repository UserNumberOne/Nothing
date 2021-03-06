package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.layers.LayerMooshroomMushroom;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMooshroom extends RenderLiving {
   private static final ResourceLocation MOOSHROOM_TEXTURES = new ResourceLocation("textures/entity/cow/mooshroom.png");

   public RenderMooshroom(RenderManager var1, ModelBase var2, float var3) {
      super(var1, var2, var3);
      this.addLayer(new LayerMooshroomMushroom(this));
   }

   protected ResourceLocation getEntityTexture(EntityMooshroom var1) {
      return MOOSHROOM_TEXTURES;
   }
}
