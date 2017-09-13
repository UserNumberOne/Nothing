package net.minecraft.client.renderer.tileentity;

import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityBannerRenderer extends TileEntitySpecialRenderer {
   private final ModelBanner bannerModel = new ModelBanner();

   public void renderTileEntityAt(TileEntityBanner var1, double var2, double var4, double var6, float var8, int var9) {
      boolean var10 = var1.getWorld() != null;
      boolean var11 = !var10 || var1.getBlockType() == Blocks.STANDING_BANNER;
      int var12 = var10 ? var1.getBlockMetadata() : 0;
      long var13 = var10 ? var1.getWorld().getTotalWorldTime() : 0L;
      GlStateManager.pushMatrix();
      float var15 = 0.6666667F;
      if (var11) {
         GlStateManager.translate((float)var2 + 0.5F, (float)var4 + 0.5F, (float)var6 + 0.5F);
         float var16 = (float)(var12 * 360) / 16.0F;
         GlStateManager.rotate(-var16, 0.0F, 1.0F, 0.0F);
         this.bannerModel.bannerStand.showModel = true;
      } else {
         float var19 = 0.0F;
         if (var12 == 2) {
            var19 = 180.0F;
         }

         if (var12 == 4) {
            var19 = 90.0F;
         }

         if (var12 == 5) {
            var19 = -90.0F;
         }

         GlStateManager.translate((float)var2 + 0.5F, (float)var4 - 0.16666667F, (float)var6 + 0.5F);
         GlStateManager.rotate(-var19, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
         this.bannerModel.bannerStand.showModel = false;
      }

      BlockPos var20 = var1.getPos();
      float var17 = (float)(var20.getX() * 7 + var20.getY() * 9 + var20.getZ() * 13) + (float)var13 + var8;
      this.bannerModel.bannerSlate.rotateAngleX = (-0.0125F + 0.01F * MathHelper.cos(var17 * 3.1415927F * 0.02F)) * 3.1415927F;
      GlStateManager.enableRescaleNormal();
      ResourceLocation var18 = this.getBannerResourceLocation(var1);
      if (var18 != null) {
         this.bindTexture(var18);
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
         this.bannerModel.renderBanner();
         GlStateManager.popMatrix();
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   @Nullable
   private ResourceLocation getBannerResourceLocation(TileEntityBanner var1) {
      return BannerTextures.BANNER_DESIGNS.getResourceLocation(var1.getPatternResourceLocation(), var1.getPatternList(), var1.getColorList());
   }
}
