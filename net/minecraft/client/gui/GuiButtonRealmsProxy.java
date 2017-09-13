package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonRealmsProxy extends GuiButton {
   private final RealmsButton realmsButton;

   public GuiButtonRealmsProxy(RealmsButton var1, int var2, int var3, int var4, String var5) {
      super(buttonId, x, y, text);
      this.realmsButton = realmsButtonIn;
   }

   public GuiButtonRealmsProxy(RealmsButton var1, int var2, int var3, int var4, String var5, int var6, int var7) {
      super(buttonId, x, y, widthIn, heightIn, text);
      this.realmsButton = realmsButtonIn;
   }

   public int getId() {
      return super.id;
   }

   public boolean getEnabled() {
      return super.enabled;
   }

   public void setEnabled(boolean var1) {
      super.enabled = isEnabled;
   }

   public void setText(String var1) {
      super.displayString = text;
   }

   public int getButtonWidth() {
      return super.getButtonWidth();
   }

   public int getPositionY() {
      return super.yPosition;
   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      if (super.mousePressed(mc, mouseX, mouseY)) {
         this.realmsButton.clicked(mouseX, mouseY);
      }

      return super.mousePressed(mc, mouseX, mouseY);
   }

   public void mouseReleased(int var1, int var2) {
      this.realmsButton.released(mouseX, mouseY);
   }

   public void mouseDragged(Minecraft var1, int var2, int var3) {
      this.realmsButton.renderBg(mouseX, mouseY);
   }

   public RealmsButton getRealmsButton() {
      return this.realmsButton;
   }

   public int getHoverState(boolean var1) {
      return this.realmsButton.getYImage(mouseOver);
   }

   public int getYImage(boolean var1) {
      return super.getHoverState(p_154312_1_);
   }

   public int getHeight() {
      return this.height;
   }
}
