package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.realms.RealmsSimpleScrolledSelectionList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSimpleScrolledSelectionListProxy extends GuiSlot {
   private final RealmsSimpleScrolledSelectionList realmsScrolledSelectionList;

   public GuiSimpleScrolledSelectionListProxy(RealmsSimpleScrolledSelectionList var1, int var2, int var3, int var4, int var5, int var6) {
      super(Minecraft.getMinecraft(), var2, var3, var4, var5, var6);
      this.realmsScrolledSelectionList = var1;
   }

   protected int getSize() {
      return this.realmsScrolledSelectionList.getItemCount();
   }

   protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      this.realmsScrolledSelectionList.selectItem(var1, var2, var3, var4);
   }

   protected boolean isSelected(int var1) {
      return this.realmsScrolledSelectionList.isSelectedItem(var1);
   }

   protected void drawBackground() {
      this.realmsScrolledSelectionList.renderBackground();
   }

   protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.realmsScrolledSelectionList.renderItem(var1, var2, var3, var4, var5, var6);
   }

   public int getWidth() {
      return super.width;
   }

   public int getMouseY() {
      return super.mouseY;
   }

   public int getMouseX() {
      return super.mouseX;
   }

   protected int getContentHeight() {
      return this.realmsScrolledSelectionList.getMaxPosition();
   }

   protected int getScrollBarX() {
      return this.realmsScrolledSelectionList.getScrollbarPosition();
   }

   public void handleMouseInput() {
      super.handleMouseInput();
   }

   public void drawScreen(int var1, int var2, float var3) {
      if (this.visible) {
         this.mouseX = var1;
         this.mouseY = var2;
         this.drawBackground();
         int var4 = this.getScrollBarX();
         int var5 = var4 + 6;
         this.bindAmountScrolled();
         GlStateManager.disableLighting();
         GlStateManager.disableFog();
         Tessellator var6 = Tessellator.getInstance();
         VertexBuffer var7 = var6.getBuffer();
         int var8 = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
         int var9 = this.top + 4 - (int)this.amountScrolled;
         if (this.hasListHeader) {
            this.drawListHeader(var8, var9, var6);
         }

         this.drawSelectionBox(var8, var9, var1, var2);
         GlStateManager.disableDepth();
         this.overlayBackground(0, this.top, 255, 255);
         this.overlayBackground(this.bottom, this.height, 255, 255);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         GlStateManager.disableAlpha();
         GlStateManager.shadeModel(7425);
         GlStateManager.disableTexture2D();
         int var10 = this.getMaxScroll();
         if (var10 > 0) {
            int var11 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            var11 = MathHelper.clamp(var11, 32, this.bottom - this.top - 8);
            int var12 = (int)this.amountScrolled * (this.bottom - this.top - var11) / var10 + this.top;
            if (var12 < this.top) {
               var12 = this.top;
            }

            var7.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            var7.pos((double)var4, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            var7.pos((double)var5, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            var7.pos((double)var5, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            var7.pos((double)var4, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            var6.draw();
            var7.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            var7.pos((double)var4, (double)(var12 + var11), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            var7.pos((double)var5, (double)(var12 + var11), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            var7.pos((double)var5, (double)var12, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            var7.pos((double)var4, (double)var12, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            var6.draw();
            var7.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            var7.pos((double)var4, (double)(var12 + var11 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            var7.pos((double)(var5 - 1), (double)(var12 + var11 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            var7.pos((double)(var5 - 1), (double)var12, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            var7.pos((double)var4, (double)var12, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            var6.draw();
         }

         this.renderDecorations(var1, var2);
         GlStateManager.enableTexture2D();
         GlStateManager.shadeModel(7424);
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
      }

   }
}
