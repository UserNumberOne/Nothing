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
      this(var1, var2, var3, var4, 0.0F, 1.0F);
   }

   public GuiOptionSlider(int var1, int var2, int var3, GameSettings.Options var4, float var5, float var6) {
      super(var1, var2, var3, 150, 20, "");
      this.sliderValue = 1.0F;
      this.options = var4;
      this.minValue = var5;
      this.maxValue = var6;
      Minecraft var7 = Minecraft.getMinecraft();
      this.sliderValue = var4.normalizeValue(var7.gameSettings.getOptionFloatValue(var4));
      this.displayString = var7.gameSettings.getKeyBinding(var4);
   }

   protected int getHoverState(boolean var1) {
      return 0;
   }

   protected void mouseDragged(Minecraft var1, int var2, int var3) {
      if (this.visible) {
         if (this.dragging) {
            this.sliderValue = (float)(var2 - (this.xPosition + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            float var4 = this.options.denormalizeValue(this.sliderValue);
            var1.gameSettings.setOptionFloatValue(this.options, var4);
            this.sliderValue = this.options.normalizeValue(var4);
            this.displayString = var1.gameSettings.getKeyBinding(this.options);
         }

         var1.getTextureManager().bindTexture(BUTTON_TEXTURES);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
         this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
      }

   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      if (super.mousePressed(var1, var2, var3)) {
         this.sliderValue = (float)(var2 - (this.xPosition + 4)) / (float)(this.width - 8);
         this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
         var1.gameSettings.setOptionFloatValue(this.options, this.options.denormalizeValue(this.sliderValue));
         this.displayString = var1.gameSettings.getKeyBinding(this.options);
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
