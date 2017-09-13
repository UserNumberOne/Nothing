package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerWolfCollar implements LayerRenderer {
   private static final ResourceLocation WOLF_COLLAR = new ResourceLocation("textures/entity/wolf/wolf_collar.png");
   private final RenderWolf wolfRenderer;

   public LayerWolfCollar(RenderWolf var1) {
      this.wolfRenderer = var1;
   }

   public void doRenderLayer(EntityWolf var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (var1.isTamed() && !var1.isInvisible()) {
         this.wolfRenderer.bindTexture(WOLF_COLLAR);
         EnumDyeColor var9 = EnumDyeColor.byMetadata(var1.getCollarColor().getMetadata());
         float[] var10 = EntitySheep.getDyeRgb(var9);
         GlStateManager.color(var10[0], var10[1], var10[2]);
         this.wolfRenderer.getMainModel().render(var1, var2, var3, var5, var6, var7, var8);
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
