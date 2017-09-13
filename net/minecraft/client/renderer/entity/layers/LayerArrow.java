package net.minecraft.client.renderer.entity.layers;

import java.util.Random;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerArrow implements LayerRenderer {
   private final RenderLivingBase renderer;

   public LayerArrow(RenderLivingBase var1) {
      this.renderer = var1;
   }

   public void doRenderLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      int var9 = var1.getArrowCountInEntity();
      if (var9 > 0) {
         EntityTippedArrow var10 = new EntityTippedArrow(var1.world, var1.posX, var1.posY, var1.posZ);
         Random var11 = new Random((long)var1.getEntityId());
         RenderHelper.disableStandardItemLighting();

         for(int var12 = 0; var12 < var9; ++var12) {
            GlStateManager.pushMatrix();
            ModelRenderer var13 = this.renderer.getMainModel().getRandomModelBox(var11);
            ModelBox var14 = (ModelBox)var13.cubeList.get(var11.nextInt(var13.cubeList.size()));
            var13.postRender(0.0625F);
            float var15 = var11.nextFloat();
            float var16 = var11.nextFloat();
            float var17 = var11.nextFloat();
            float var18 = (var14.posX1 + (var14.posX2 - var14.posX1) * var15) / 16.0F;
            float var19 = (var14.posY1 + (var14.posY2 - var14.posY1) * var16) / 16.0F;
            float var20 = (var14.posZ1 + (var14.posZ2 - var14.posZ1) * var17) / 16.0F;
            GlStateManager.translate(var18, var19, var20);
            var15 = var15 * 2.0F - 1.0F;
            var16 = var16 * 2.0F - 1.0F;
            var17 = var17 * 2.0F - 1.0F;
            var15 = var15 * -1.0F;
            var16 = var16 * -1.0F;
            var17 = var17 * -1.0F;
            float var21 = MathHelper.sqrt(var15 * var15 + var17 * var17);
            var10.rotationYaw = (float)(Math.atan2((double)var15, (double)var17) * 57.29577951308232D);
            var10.rotationPitch = (float)(Math.atan2((double)var16, (double)var21) * 57.29577951308232D);
            var10.prevRotationYaw = var10.rotationYaw;
            var10.prevRotationPitch = var10.rotationPitch;
            double var22 = 0.0D;
            double var24 = 0.0D;
            double var26 = 0.0D;
            this.renderer.getRenderManager().doRenderEntity(var10, 0.0D, 0.0D, 0.0D, 0.0F, var4, false);
            GlStateManager.popMatrix();
         }

         RenderHelper.enableStandardItemLighting();
      }

   }

   public boolean shouldCombineTextures() {
      return false;
   }
}
