package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonLanguage extends GuiButton {
   public GuiButtonLanguage(int var1, int var2, int var3) {
      super(buttonID, xPos, yPos, 20, 20, "");
   }

   public void drawButton(Minecraft var1, int var2, int var3) {
      if (this.visible) {
         mc.getTextureManager().bindTexture(GuiButton.BUTTON_TEXTURES);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
         int i = 106;
         if (flag) {
            i += this.height;
         }

         this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, i, this.width, this.height);
      }

   }
}
