package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerHeldItem implements LayerRenderer {
   protected final RenderLivingBase livingEntityRenderer;

   public LayerHeldItem(RenderLivingBase var1) {
      this.livingEntityRenderer = var1;
   }

   public void doRenderLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      boolean var9 = var1.getPrimaryHand() == EnumHandSide.RIGHT;
      ItemStack var10 = var9 ? var1.getHeldItemOffhand() : var1.getHeldItemMainhand();
      ItemStack var11 = var9 ? var1.getHeldItemMainhand() : var1.getHeldItemOffhand();
      if (var10 != null || var11 != null) {
         GlStateManager.pushMatrix();
         if (this.livingEntityRenderer.getMainModel().isChild) {
            float var12 = 0.5F;
            GlStateManager.translate(0.0F, 0.625F, 0.0F);
            GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
         }

         this.renderHeldItem(var1, var11, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
         this.renderHeldItem(var1, var10, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
         GlStateManager.popMatrix();
      }

   }

   private void renderHeldItem(EntityLivingBase var1, ItemStack var2, ItemCameraTransforms.TransformType var3, EnumHandSide var4) {
      if (var2 != null) {
         GlStateManager.pushMatrix();
         if (var1.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
         }

         ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F, var4);
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         boolean var5 = var4 == EnumHandSide.LEFT;
         GlStateManager.translate((float)(var5 ? -1 : 1) / 16.0F, 0.125F, -0.625F);
         Minecraft.getMinecraft().getItemRenderer().renderItemSide(var1, var2, var3, var5);
         GlStateManager.popMatrix();
      }

   }

   public boolean shouldCombineTextures() {
      return false;
   }
}
