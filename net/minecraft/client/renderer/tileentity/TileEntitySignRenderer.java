package net.minecraft.client.renderer.tileentity;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntitySignRenderer extends TileEntitySpecialRenderer {
   private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");
   private final ModelSign model = new ModelSign();

   public void renderTileEntityAt(TileEntitySign var1, double var2, double var4, double var6, float var8, int var9) {
      Block var10 = var1.getBlockType();
      GlStateManager.pushMatrix();
      float var11 = 0.6666667F;
      if (var10 == Blocks.STANDING_SIGN) {
         GlStateManager.translate((float)var2 + 0.5F, (float)var4 + 0.5F, (float)var6 + 0.5F);
         float var12 = (float)(var1.getBlockMetadata() * 360) / 16.0F;
         GlStateManager.rotate(-var12, 0.0F, 1.0F, 0.0F);
         this.model.signStick.showModel = true;
      } else {
         int var19 = var1.getBlockMetadata();
         float var13 = 0.0F;
         if (var19 == 2) {
            var13 = 180.0F;
         }

         if (var19 == 4) {
            var13 = 90.0F;
         }

         if (var19 == 5) {
            var13 = -90.0F;
         }

         GlStateManager.translate((float)var2 + 0.5F, (float)var4 + 0.5F, (float)var6 + 0.5F);
         GlStateManager.rotate(-var13, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
         this.model.signStick.showModel = false;
      }

      if (var9 >= 0) {
         this.bindTexture(DESTROY_STAGES[var9]);
         GlStateManager.matrixMode(5890);
         GlStateManager.pushMatrix();
         GlStateManager.scale(4.0F, 2.0F, 1.0F);
         GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
         GlStateManager.matrixMode(5888);
      } else {
         this.bindTexture(SIGN_TEXTURE);
      }

      GlStateManager.enableRescaleNormal();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
      this.model.renderSign();
      GlStateManager.popMatrix();
      FontRenderer var20 = this.getFontRenderer();
      float var21 = 0.010416667F;
      GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
      GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
      GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
      GlStateManager.depthMask(false);
      boolean var14 = false;
      if (var9 < 0) {
         for(int var15 = 0; var15 < var1.signText.length; ++var15) {
            if (var1.signText[var15] != null) {
               ITextComponent var16 = var1.signText[var15];
               List var17 = GuiUtilRenderComponents.splitText(var16, 90, var20, false, true);
               String var18 = var17 != null && !var17.isEmpty() ? ((ITextComponent)var17.get(0)).getFormattedText() : "";
               if (var15 == var1.lineBeingEdited) {
                  var18 = "> " + var18 + " <";
                  var20.drawString(var18, -var20.getStringWidth(var18) / 2, var15 * 10 - var1.signText.length * 5, 0);
               } else {
                  var20.drawString(var18, -var20.getStringWidth(var18) / 2, var15 * 10 - var1.signText.length * 5, 0);
               }
            }
         }
      }

      GlStateManager.depthMask(true);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
      if (var9 >= 0) {
         GlStateManager.matrixMode(5890);
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5888);
      }

   }
}
