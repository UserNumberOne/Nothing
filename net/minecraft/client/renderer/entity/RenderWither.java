package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelWither;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerWitherAura;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWither extends RenderLiving {
   private static final ResourceLocation INVULNERABLE_WITHER_TEXTURES = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
   private static final ResourceLocation WITHER_TEXTURES = new ResourceLocation("textures/entity/wither/wither.png");

   public RenderWither(RenderManager var1) {
      super(var1, new ModelWither(0.0F), 1.0F);
      this.addLayer(new LayerWitherAura(this));
   }

   protected ResourceLocation getEntityTexture(EntityWither var1) {
      int var2 = var1.getInvulTime();
      return var2 <= 0 || var2 <= 80 && var2 / 5 % 2 == 1 ? WITHER_TEXTURES : INVULNERABLE_WITHER_TEXTURES;
   }

   protected void preRenderCallback(EntityWither var1, float var2) {
      float var3 = 2.0F;
      int var4 = var1.getInvulTime();
      if (var4 > 0) {
         var3 -= ((float)var4 - var2) / 220.0F * 0.5F;
      }

      GlStateManager.scale(var3, var3, var3);
   }
}
