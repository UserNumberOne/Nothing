package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelSheep1;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderSheep;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSheepWool implements LayerRenderer {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
   private final RenderSheep sheepRenderer;
   private final ModelSheep1 sheepModel = new ModelSheep1();

   public LayerSheepWool(RenderSheep var1) {
      this.sheepRenderer = var1;
   }

   public void doRenderLayer(EntitySheep var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (!var1.getSheared() && !var1.isInvisible()) {
         this.sheepRenderer.bindTexture(TEXTURE);
         if (var1.hasCustomName() && "jeb_".equals(var1.getCustomNameTag())) {
            boolean var17 = true;
            int var10 = var1.ticksExisted / 25 + var1.getEntityId();
            int var11 = EnumDyeColor.values().length;
            int var12 = var10 % var11;
            int var13 = (var10 + 1) % var11;
            float var14 = ((float)(var1.ticksExisted % 25) + var4) / 25.0F;
            float[] var15 = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(var12));
            float[] var16 = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(var13));
            GlStateManager.color(var15[0] * (1.0F - var14) + var16[0] * var14, var15[1] * (1.0F - var14) + var16[1] * var14, var15[2] * (1.0F - var14) + var16[2] * var14);
         } else {
            float[] var9 = EntitySheep.getDyeRgb(var1.getFleeceColor());
            GlStateManager.color(var9[0], var9[1], var9[2]);
         }

         this.sheepModel.setModelAttributes(this.sheepRenderer.getMainModel());
         this.sheepModel.setLivingAnimations(var1, var2, var3, var4);
         this.sheepModel.render(var1, var2, var3, var5, var6, var7, var8);
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
