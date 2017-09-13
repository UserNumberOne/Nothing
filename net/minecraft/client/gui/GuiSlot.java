package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public abstract class GuiSlot {
   protected final Minecraft mc;
   public int width;
   public int height;
   public int top;
   public int bottom;
   public int right;
   public int left;
   public final int slotHeight;
   private int scrollUpButtonID;
   private int scrollDownButtonID;
   protected int mouseX;
   protected int mouseY;
   protected boolean centerListVertically = true;
   protected int initialClickY = -2;
   protected float scrollMultiplier;
   protected float amountScrolled;
   protected int selectedElement = -1;
   protected long lastClicked;
   protected boolean visible = true;
   protected boolean showSelectionBox = true;
   protected boolean hasListHeader;
   public int headerPadding;
   private boolean enabled = true;

   public GuiSlot(Minecraft var1, int var2, int var3, int var4, int var5, int var6) {
      this.mc = mcIn;
      this.width = width;
      this.height = height;
      this.top = topIn;
      this.bottom = bottomIn;
      this.slotHeight = slotHeightIn;
      this.left = 0;
      this.right = width;
   }

   public void setDimensions(int var1, int var2, int var3, int var4) {
      this.width = widthIn;
      this.height = heightIn;
      this.top = topIn;
      this.bottom = bottomIn;
      this.left = 0;
      this.right = widthIn;
   }

   public void setShowSelectionBox(boolean var1) {
      this.showSelectionBox = showSelectionBoxIn;
   }

   protected void setHasListHeader(boolean var1, int var2) {
      this.hasListHeader = hasListHeaderIn;
      this.headerPadding = headerPaddingIn;
      if (!hasListHeaderIn) {
         this.headerPadding = 0;
      }

   }

   protected abstract int getSize();

   protected abstract void elementClicked(int var1, boolean var2, int var3, int var4);

   protected abstract boolean isSelected(int var1);

   protected int getContentHeight() {
      return this.getSize() * this.slotHeight + this.headerPadding;
   }

   protected abstract void drawBackground();

   protected void updateItemPos(int var1, int var2, int var3) {
   }

   protected abstract void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6);

   protected void drawListHeader(int var1, int var2, Tessellator var3) {
   }

   protected void clickedHeader(int var1, int var2) {
   }

   protected void renderDecorations(int var1, int var2) {
   }

   public int getSlotIndexFromScreenCoords(int var1, int var2) {
      int i = this.left + this.width / 2 - this.getListWidth() / 2;
      int j = this.left + this.width / 2 + this.getListWidth() / 2;
      int k = posY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
      int l = k / this.slotHeight;
      return posX < this.getScrollBarX() && posX >= i && posX <= j && l >= 0 && k >= 0 && l < this.getSize() ? l : -1;
   }

   public void registerScrollButtons(int var1, int var2) {
      this.scrollUpButtonID = scrollUpButtonIDIn;
      this.scrollDownButtonID = scrollDownButtonIDIn;
   }

   protected void bindAmountScrolled() {
      this.amountScrolled = MathHelper.clamp(this.amountScrolled, 0.0F, (float)this.getMaxScroll());
   }

   public int getMaxScroll() {
      return Math.max(0, this.getContentHeight() - (this.bottom - this.top - 4));
   }

   public int getAmountScrolled() {
      return (int)this.amountScrolled;
   }

   public boolean isMouseYWithinSlotBounds(int var1) {
      return p_148141_1_ >= this.top && p_148141_1_ <= this.bottom && this.mouseX >= this.left && this.mouseX <= this.right;
   }

   public void scrollBy(int var1) {
      this.amountScrolled += (float)amount;
      this.bindAmountScrolled();
      this.initialClickY = -2;
   }

   public void actionPerformed(GuiButton var1) {
      if (button.enabled) {
         if (button.id == this.scrollUpButtonID) {
            this.amountScrolled -= (float)(this.slotHeight * 2 / 3);
            this.initialClickY = -2;
            this.bindAmountScrolled();
         } else if (button.id == this.scrollDownButtonID) {
            this.amountScrolled += (float)(this.slotHeight * 2 / 3);
            this.initialClickY = -2;
            this.bindAmountScrolled();
         }
      }

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
         this.drawContainerBackground(tessellator);
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
         int i1 = 4;
         vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         vertexbuffer.pos((double)this.left, (double)(this.top + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
         vertexbuffer.pos((double)this.right, (double)(this.top + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
         vertexbuffer.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
         vertexbuffer.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
         tessellator.draw();
         vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         vertexbuffer.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
         vertexbuffer.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
         vertexbuffer.pos((double)this.right, (double)(this.bottom - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
         vertexbuffer.pos((double)this.left, (double)(this.bottom - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
         tessellator.draw();
         int j1 = this.getMaxScroll();
         if (j1 > 0) {
            int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
            int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;
            if (l1 < this.top) {
               l1 = this.top;
            }

            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)j, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            vertexbuffer.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            vertexbuffer.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            vertexbuffer.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
         }

         this.renderDecorations(mouseXIn, mouseYIn);
         GlStateManager.enableTexture2D();
         GlStateManager.shadeModel(7424);
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
      }

   }

   public void handleMouseInput() {
      if (this.isMouseYWithinSlotBounds(this.mouseY)) {
         if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom) {
            int i = (this.width - this.getListWidth()) / 2;
            int j = (this.width + this.getListWidth()) / 2;
            int k = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
            int l = k / this.slotHeight;
            if (l < this.getSize() && this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0) {
               this.elementClicked(l, false, this.mouseX, this.mouseY);
               this.selectedElement = l;
            } else if (this.mouseX >= i && this.mouseX <= j && k < 0) {
               this.clickedHeader(this.mouseX - i, this.mouseY - this.top + (int)this.amountScrolled - 4);
            }
         }

         if (Mouse.isButtonDown(0) && this.getEnabled()) {
            if (this.initialClickY != -1) {
               if (this.initialClickY >= 0) {
                  this.amountScrolled -= (float)(this.mouseY - this.initialClickY) * this.scrollMultiplier;
                  this.initialClickY = this.mouseY;
               }
            } else {
               boolean flag1 = true;
               if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
                  int j2 = (this.width - this.getListWidth()) / 2;
                  int k2 = (this.width + this.getListWidth()) / 2;
                  int l2 = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
                  int i1 = l2 / this.slotHeight;
                  if (i1 < this.getSize() && this.mouseX >= j2 && this.mouseX <= k2 && i1 >= 0 && l2 >= 0) {
                     boolean flag = i1 == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                     this.elementClicked(i1, flag, this.mouseX, this.mouseY);
                     this.selectedElement = i1;
                     this.lastClicked = Minecraft.getSystemTime();
                  } else if (this.mouseX >= j2 && this.mouseX <= k2 && l2 < 0) {
                     this.clickedHeader(this.mouseX - j2, this.mouseY - this.top + (int)this.amountScrolled - 4);
                     flag1 = false;
                  }

                  int i3 = this.getScrollBarX();
                  int j1 = i3 + 6;
                  if (this.mouseX >= i3 && this.mouseX <= j1) {
                     this.scrollMultiplier = -1.0F;
                     int k1 = this.getMaxScroll();
                     if (k1 < 1) {
                        k1 = 1;
                     }

                     int l1 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());
                     l1 = MathHelper.clamp(l1, 32, this.bottom - this.top - 8);
                     this.scrollMultiplier /= (float)(this.bottom - this.top - l1) / (float)k1;
                  } else {
                     this.scrollMultiplier = 1.0F;
                  }

                  if (flag1) {
                     this.initialClickY = this.mouseY;
                  } else {
                     this.initialClickY = -2;
                  }
               } else {
                  this.initialClickY = -2;
               }
            }
         } else {
            this.initialClickY = -1;
         }

         int i2 = Mouse.getEventDWheel();
         if (i2 != 0) {
            if (i2 > 0) {
               i2 = -1;
            } else if (i2 < 0) {
               i2 = 1;
            }

            this.amountScrolled += (float)(i2 * this.slotHeight / 2);
         }
      }

   }

   public void setEnabled(boolean var1) {
      this.enabled = enabledIn;
   }

   public boolean getEnabled() {
      return this.enabled;
   }

   public int getListWidth() {
      return 220;
   }

   protected void drawSelectionBox(int var1, int var2, int var3, int var4) {
      int i = this.getSize();
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();

      for(int j = 0; j < i; ++j) {
         int k = insideTop + j * this.slotHeight + this.headerPadding;
         int l = this.slotHeight - 4;
         if (k > this.bottom || k + l < this.top) {
            this.updateItemPos(j, insideLeft, k);
         }

         if (this.showSelectionBox && this.isSelected(j)) {
            int i1 = this.left + (this.width / 2 - this.getListWidth() / 2);
            int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableTexture2D();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)i1, (double)(k + l + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)j1, (double)(k + l + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)j1, (double)(k - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)i1, (double)(k - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            vertexbuffer.pos((double)(i1 + 1), (double)(k + l + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)(j1 - 1), (double)(k + l + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)(j1 - 1), (double)(k - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)(i1 + 1), (double)(k - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
         }

         this.drawSlot(j, insideLeft, k, l, mouseXIn, mouseYIn);
      }

   }

   protected int getScrollBarX() {
      return this.width / 2 + 124;
   }

   protected void overlayBackground(int var1, int var2, int var3, int var4) {
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();
      this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      float f = 32.0F;
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      vertexbuffer.pos((double)this.left, (double)endY, 0.0D).tex(0.0D, (double)((float)endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
      vertexbuffer.pos((double)(this.left + this.width), (double)endY, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
      vertexbuffer.pos((double)(this.left + this.width), (double)startY, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
      vertexbuffer.pos((double)this.left, (double)startY, 0.0D).tex(0.0D, (double)((float)startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
      tessellator.draw();
   }

   public void setSlotXBoundsFromLeft(int var1) {
      this.left = leftIn;
      this.right = leftIn + this.width;
   }

   public int getSlotHeight() {
      return this.slotHeight;
   }

   protected void drawContainerBackground(Tessellator var1) {
      VertexBuffer buffer = tessellator.getBuffer();
      this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      float f = 32.0F;
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos((double)this.left, (double)this.bottom, 0.0D).tex((double)((float)this.left / f), (double)((float)(this.bottom + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
      buffer.pos((double)this.right, (double)this.bottom, 0.0D).tex((double)((float)this.right / f), (double)((float)(this.bottom + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
      buffer.pos((double)this.right, (double)this.top, 0.0D).tex((double)((float)this.right / f), (double)((float)(this.top + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
      buffer.pos((double)this.left, (double)this.top, 0.0D).tex((double)((float)this.left / f), (double)((float)(this.top + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
      tessellator.draw();
   }
}
