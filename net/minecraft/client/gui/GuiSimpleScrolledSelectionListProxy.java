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
      super(Minecraft.getMinecraft(), widthIn, heightIn, topIn, bottomIn, slotHeightIn);
      this.realmsScrolledSelectionList = p_i45525_1_;
   }

   protected int getSize() {
      return this.realmsScrolledSelectionList.getItemCount();
   }

   protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      this.realmsScrolledSelectionList.selectItem(slotIndex, isDoubleClick, mouseX, mouseY);
   }

   protected boolean isSelected(int var1) {
      return this.realmsScrolledSelectionList.isSelectedItem(slotIndex);
   }

   protected void drawBackground() {
      this.realmsScrolledSelectionList.renderBackground();
   }

   protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.realmsScrolledSelectionList.renderItem(entryID, insideLeft, yPos, insideSlotHeight, mouseXIn, mouseYIn);
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
         this.mouseX = mouseXIn;
         this.mouseY = mouseYIn;
         this.drawBackground();
         int i = this.getScrollBarX();
         int j = i + 6;
         this.bindAmountScrolled();
         GlStateManager.disableLighting();
         GlStateManager.disableFog();
         Tessellator tessellator = Tessellator.getInstance();
         VertexBuffer vertexbuffer = tessellator.getBuffer();
         int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
         int l = this.top + 4 - (int)this.amountScrolled;
         if (this.hasListHeader) {
            this.drawListHeader(k, l, tessellator);
         }

         this.drawSelectionBox(k, l, mouseXIn, mouseYIn);
         GlStateManager.disableDepth();
         this.overlayBackground(0, this.top, 255, 255);
         this.overlayBackground(this.bottom, this.height, 255, 255);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         GlStateManager.disableAlpha();
         GlStateManager.shadeModel(7425);
         GlStateManager.disableTexture2D();
         int i1 = this.getMaxScroll();
         if (i1 > 0) {
            int j1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            j1 = MathHelper.clamp(j1, 32, this.bottom - this.top - 8);
            int k1 = (int)this.amountScrolled * (this.bottom - this.top - j1) / i1 + this.top;
            if (k1 < this.top) {
               k1 = this.top;
            }

            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)i, (double)(k1 + j1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)j, (double)(k1 + j1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)j, (double)k1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)i, (double)k1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)i, (double)(k1 + j1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            vertexbuffer.pos((double)(j - 1), (double)(k1 + j1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            vertexbuffer.pos((double)(j - 1), (double)k1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            vertexbuffer.pos((double)i, (double)k1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
         }

         this.renderDecorations(mouseXIn, mouseYIn);
         GlStateManager.enableTexture2D();
         GlStateManager.shadeModel(7424);
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
      }

   }
}
