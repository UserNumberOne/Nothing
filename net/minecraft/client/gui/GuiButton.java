package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButton extends Gui {
   protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
   public int width;
   public int height;
   public int xPosition;
   public int yPosition;
   public String displayString;
   public int id;
   public boolean enabled;
   public boolean visible;
   protected boolean hovered;
   public int packedFGColour;

   public GuiButton(int var1, int var2, int var3, String var4) {
      this(var1, var2, var3, 200, 20, var4);
   }

   public GuiButton(int var1, int var2, int var3, int var4, int var5, String var6) {
      this.width = 200;
      this.height = 20;
      this.enabled = true;
      this.visible = true;
      this.id = var1;
      this.xPosition = var2;
      this.yPosition = var3;
      this.width = var4;
      this.height = var5;
      this.displayString = var6;
   }

   protected int getHoverState(boolean var1) {
      byte var2 = 1;
      if (!this.enabled) {
         var2 = 0;
      } else if (var1) {
         var2 = 2;
      }

      return var2;
   }

   public void drawButton(Minecraft var1, int var2, int var3) {
      if (this.visible) {
         FontRenderer var4 = var1.fontRendererObj;
         var1.getTextureManager().bindTexture(BUTTON_TEXTURES);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.hovered = var2 >= this.xPosition && var3 >= this.yPosition && var2 < this.xPosition + this.width && var3 < this.yPosition + this.height;
         int var5 = this.getHoverState(this.hovered);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + var5 * 20, this.width / 2, this.height);
         this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + var5 * 20, this.width / 2, this.height);
         this.mouseDragged(var1, var2, var3);
         int var6 = 14737632;
         if (this.packedFGColour != 0) {
            var6 = this.packedFGColour;
         } else if (!this.enabled) {
            var6 = 10526880;
         } else if (this.hovered) {
            var6 = 16777120;
         }

         this.drawCenteredString(var4, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, var6);
      }

   }

   protected void mouseDragged(Minecraft var1, int var2, int var3) {
   }

   public void mouseReleased(int var1, int var2) {
   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      return this.enabled && this.visible && var2 >= this.xPosition && var3 >= this.yPosition && var2 < this.xPosition + this.width && var3 < this.yPosition + this.height;
   }

   public boolean isMouseOver() {
      return this.hovered;
   }

   public void drawButtonForegroundLayer(int var1, int var2) {
   }

   public void playPressSound(SoundHandler var1) {
      var1.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   public int getButtonWidth() {
      return this.width;
   }

   public void setWidth(int var1) {
      this.width = var1;
   }
}
