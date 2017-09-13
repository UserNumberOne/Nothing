package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerElytra implements LayerRenderer {
   private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
   private final RenderPlayer renderPlayer;
   private final ModelElytra modelElytra = new ModelElytra();

   public LayerElytra(RenderPlayer var1) {
      this.renderPlayer = var1;
   }

   public void doRenderLayer(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      ItemStack var9 = var1.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      if (var9 != null && var9.getItem() == Items.ELYTRA) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableBlend();
         if (var1.isPlayerInfoSet() && var1.getLocationElytra() != null) {
            this.renderPlayer.bindTexture(var1.getLocationElytra());
         } else if (var1.hasPlayerInfo() && var1.getLocationCape() != null && var1.isWearing(EnumPlayerModelParts.CAPE)) {
            this.renderPlayer.bindTexture(var1.getLocationCape());
         } else {
            this.renderPlayer.bindTexture(TEXTURE_ELYTRA);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.0F, 0.125F);
         this.modelElytra.setRotationAngles(var2, var3, var5, var6, var7, var8, var1);
         this.modelElytra.render(var1, var2, var3, var5, var6, var7, var8);
         if (var9.isItemEnchanted()) {
            LayerArmorBase.renderEnchantedGlint(this.renderPlayer, var1, this.modelElytra, var2, var3, var4, var5, var6, var7, var8);
         }

         GlStateManager.popMatrix();
      }

   }

   public boolean shouldCombineTextures() {
      return false;
   }
}
