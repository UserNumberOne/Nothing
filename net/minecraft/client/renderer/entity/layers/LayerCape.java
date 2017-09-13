package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCape implements LayerRenderer {
   private final RenderPlayer playerRenderer;

   public LayerCape(RenderPlayer var1) {
      this.playerRenderer = var1;
   }

   public void doRenderLayer(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (var1.hasPlayerInfo() && !var1.isInvisible() && var1.isWearing(EnumPlayerModelParts.CAPE) && var1.getLocationCape() != null) {
         ItemStack var9 = var1.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
         if (var9 == null || var9.getItem() != Items.ELYTRA) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.playerRenderer.bindTexture(var1.getLocationCape());
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            double var10 = var1.prevChasingPosX + (var1.chasingPosX - var1.prevChasingPosX) * (double)var4 - (var1.prevPosX + (var1.posX - var1.prevPosX) * (double)var4);
            double var12 = var1.prevChasingPosY + (var1.chasingPosY - var1.prevChasingPosY) * (double)var4 - (var1.prevPosY + (var1.posY - var1.prevPosY) * (double)var4);
            double var14 = var1.prevChasingPosZ + (var1.chasingPosZ - var1.prevChasingPosZ) * (double)var4 - (var1.prevPosZ + (var1.posZ - var1.prevPosZ) * (double)var4);
            float var16 = var1.prevRenderYawOffset + (var1.renderYawOffset - var1.prevRenderYawOffset) * var4;
            double var17 = (double)MathHelper.sin(var16 * 0.017453292F);
            double var19 = (double)(-MathHelper.cos(var16 * 0.017453292F));
            float var21 = (float)var12 * 10.0F;
            var21 = MathHelper.clamp(var21, -6.0F, 32.0F);
            float var22 = (float)(var10 * var17 + var14 * var19) * 100.0F;
            float var23 = (float)(var10 * var19 - var14 * var17) * 100.0F;
            if (var22 < 0.0F) {
               var22 = 0.0F;
            }

            float var24 = var1.prevCameraYaw + (var1.cameraYaw - var1.prevCameraYaw) * var4;
            var21 = var21 + MathHelper.sin((var1.prevDistanceWalkedModified + (var1.distanceWalkedModified - var1.prevDistanceWalkedModified) * var4) * 6.0F) * 32.0F * var24;
            if (var1.isSneaking()) {
               var21 += 25.0F;
            }

            GlStateManager.rotate(6.0F + var22 / 2.0F + var21, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(var23 / 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-var23 / 2.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            this.playerRenderer.getMainModel().renderCape(0.0625F);
            GlStateManager.popMatrix();
         }
      }

   }

   public boolean shouldCombineTextures() {
      return false;
   }
}
