package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsSliderButton extends RealmsButton {
   public float value;
   public boolean sliding;
   private final float minValue;
   private final float maxValue;
   private int steps;

   public RealmsSliderButton(int var1, int var2, int var3, int var4, int var5, int var6) {
      this(buttonId, x, y, width, p_i1056_6_, 0, 1.0F, (float)maxValueIn);
   }

   public RealmsSliderButton(int var1, int var2, int var3, int var4, int var5, int var6, float var7, float var8) {
      super(buttonId, x, y, width, 20, "");
      this.value = 1.0F;
      this.minValue = minValueIn;
      this.maxValue = maxValueIn;
      this.value = this.toPct((float)valueIn);
      this.getProxy().displayString = this.getMessage();
   }

   public String getMessage() {
      return "";
   }

   public float toPct(float var1) {
      return MathHelper.clamp((this.clamp(p_toPct_1_) - this.minValue) / (this.maxValue - this.minValue), 0.0F, 1.0F);
   }

   public float toValue(float var1) {
      return this.clamp(this.minValue + (this.maxValue - this.minValue) * MathHelper.clamp(p_toValue_1_, 0.0F, 1.0F));
   }

   public float clamp(float var1) {
      p_clamp_1_ = this.clampSteps(p_clamp_1_);
      return MathHelper.clamp(p_clamp_1_, this.minValue, this.maxValue);
   }

   protected float clampSteps(float var1) {
      if (this.steps > 0) {
         p_clampSteps_1_ = (float)(this.steps * Math.round(p_clampSteps_1_ / (float)this.steps));
      }

      return p_clampSteps_1_;
   }

   public int getYImage(boolean var1) {
      return 0;
   }

   public void renderBg(int var1, int var2) {
      if (this.getProxy().visible) {
         if (this.sliding) {
            this.value = (float)(p_renderBg_1_ - (this.getProxy().xPosition + 4)) / (float)(this.getProxy().getButtonWidth() - 8);
            this.value = MathHelper.clamp(this.value, 0.0F, 1.0F);
            float f = this.toValue(this.value);
            this.clicked(f);
            this.value = this.toPct(f);
            this.getProxy().displayString = this.getMessage();
         }

         Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS_LOCATION);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.blit(this.getProxy().xPosition + (int)(this.value * (float)(this.getProxy().getButtonWidth() - 8)), this.getProxy().yPosition, 0, 66, 4, 20);
         this.blit(this.getProxy().xPosition + (int)(this.value * (float)(this.getProxy().getButtonWidth() - 8)) + 4, this.getProxy().yPosition, 196, 66, 4, 20);
      }

   }

   public void clicked(int var1, int var2) {
      this.value = (float)(p_clicked_1_ - (this.getProxy().xPosition + 4)) / (float)(this.getProxy().getButtonWidth() - 8);
      this.value = MathHelper.clamp(this.value, 0.0F, 1.0F);
      this.clicked(this.toValue(this.value));
      this.getProxy().displayString = this.getMessage();
      this.sliding = true;
   }

   public void clicked(float var1) {
   }

   public void released(int var1, int var2) {
      this.sliding = false;
   }
}
