package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSlider extends GuiButton {
   private float sliderPosition = 1.0F;
   public boolean isMouseDown;
   private final String name;
   private final float min;
   private final float max;
   private final GuiPageButtonList.GuiResponder responder;
   private GuiSlider.FormatHelper formatHelper;

   public GuiSlider(GuiPageButtonList.GuiResponder var1, int var2, int var3, int var4, String var5, float var6, float var7, float var8, GuiSlider.FormatHelper var9) {
      super(idIn, x, y, 150, 20, "");
      this.name = name;
      this.min = min;
      this.max = max;
      this.sliderPosition = (defaultValue - min) / (max - min);
      this.formatHelper = formatter;
      this.responder = guiResponder;
      this.displayString = this.getDisplayString();
   }

   public float getSliderValue() {
      return this.min + (this.max - this.min) * this.sliderPosition;
   }

   public void setSliderValue(float var1, boolean var2) {
      this.sliderPosition = (p_175218_1_ - this.min) / (this.max - this.min);
      this.displayString = this.getDisplayString();
      if (p_175218_2_) {
         this.responder.setEntryValue(this.id, this.getSliderValue());
      }

   }

   public float getSliderPosition() {
      return this.sliderPosition;
   }

   private String getDisplayString() {
      return this.formatHelper == null ? I18n.format(this.name) + ": " + this.getSliderValue() : this.formatHelper.getText(this.id, I18n.format(this.name), this.getSliderValue());
   }

   protected int getHoverState(boolean var1) {
      return 0;
   }

   protected void mouseDragged(Minecraft var1, int var2, int var3) {
      if (this.visible) {
         if (this.isMouseDown) {
            this.sliderPosition = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            if (this.sliderPosition < 0.0F) {
               this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
               this.sliderPosition = 1.0F;
            }

            this.displayString = this.getDisplayString();
            this.responder.setEntryValue(this.id, this.getSliderValue());
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.drawTexturedModalRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
         this.drawTexturedModalRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
      }

   }

   public void setSliderPosition(float var1) {
      this.sliderPosition = p_175219_1_;
      this.displayString = this.getDisplayString();
      this.responder.setEntryValue(this.id, this.getSliderValue());
   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      if (super.mousePressed(mc, mouseX, mouseY)) {
         this.sliderPosition = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
         if (this.sliderPosition < 0.0F) {
            this.sliderPosition = 0.0F;
         }

         if (this.sliderPosition > 1.0F) {
            this.sliderPosition = 1.0F;
         }

         this.displayString = this.getDisplayString();
         this.responder.setEntryValue(this.id, this.getSliderValue());
         this.isMouseDown = true;
         return true;
      } else {
         return false;
      }
   }

   public void mouseReleased(int var1, int var2) {
      this.isMouseDown = false;
   }

   @SideOnly(Side.CLIENT)
   public interface FormatHelper {
      String getText(int var1, String var2, float var3);
   }
}
