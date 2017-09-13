package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOptionSlider extends GuiButton {
   private float sliderValue;
   public boolean dragging;
   private final GameSettings.Options options;
   private final float minValue;
   private final float maxValue;

   public GuiOptionSlider(int var1, int var2, int var3, GameSettings.Options var4) {
      this(buttonId, x, y, optionIn, 0.0F, 1.0F);
   }

   public GuiOptionSlider(int var1, int var2, int var3, GameSettings.Options var4, float var5, float var6) {
      super(buttonId, x, y, 150, 20, "");
      this.sliderValue = 1.0F;
      this.options = optionIn;
      this.minValue = minValueIn;
      this.maxValue = maxValue;
      Minecraft minecraft = Minecraft.getMinecraft();
      this.sliderValue = optionIn.normalizeValue(minecraft.gameSettings.getOptionFloatValue(optionIn));
      this.displayString = minecraft.gameSettings.getKeyBinding(optionIn);
   }

   protected int getHoverState(boolean var1) {
      return 0;
   }

   protected void mouseDragged(Minecraft var1, int var2, int var3) {
      if (this.visible) {
         if (this.dragging) {
            this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            float f = this.options.denormalizeValue(this.sliderValue);
            mc.gameSettings.setOptionFloatValue(this.options, f);
            this.sliderValue = this.options.normalizeValue(f);
            this.displayString = mc.gameSettings.getKeyBinding(this.options);
         }

         mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
         this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
      }

   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      if (super.mousePressed(mc, mouseX, mouseY)) {
         this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
         this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
         mc.gameSettings.setOptionFloatValue(this.options, this.options.denormalizeValue(this.sliderValue));
         this.displayString = mc.gameSettings.getKeyBinding(this.options);
         this.dragging = true;
         return true;
      } else {
         return false;
      }
   }

   public void mouseReleased(int var1, int var2) {
      this.dragging = false;
   }
}
