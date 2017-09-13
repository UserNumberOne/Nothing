package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerWolfCollar;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWolf extends RenderLiving {
   private static final ResourceLocation WOLF_TEXTURES = new ResourceLocation("textures/entity/wolf/wolf.png");
   private static final ResourceLocation TAMED_WOLF_TEXTURES = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
   private static final ResourceLocation ANRGY_WOLF_TEXTURES = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

   public RenderWolf(RenderManager var1, ModelBase var2, float var3) {
      super(var1, var2, var3);
      this.addLayer(new LayerWolfCollar(this));
   }

   protected float handleRotationFloat(EntityWolf var1, float var2) {
      return var1.getTailRotation();
   }

   public void doRender(EntityWolf var1, double var2, double var4, double var6, float var8, float var9) {
      if (var1.isWolfWet()) {
         float var10 = var1.getBrightness(var9) * var1.getShadingWhileWet(var9);
         GlStateManager.color(var10, var10, var10);
      }

      super.doRender((EntityLiving)var1, var2, var4, var6, var8, var9);
   }

   protected ResourceLocation getEntityTexture(EntityWolf var1) {
      return var1.isTamed() ? TAMED_WOLF_TEXTURES : (var1.isAngry() ? ANRGY_WOLF_TEXTURES : WOLF_TEXTURES);
   }
}
