package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsEditBox {
   private final GuiTextField editBox;

   public RealmsEditBox(int var1, int var2, int var3, int var4, int var5) {
      this.editBox = new GuiTextField(id, Minecraft.getMinecraft().fontRendererObj, x, y, width, height);
   }

   public String getValue() {
      return this.editBox.getText();
   }

   public void tick() {
      this.editBox.updateCursorCounter();
   }

   public void setFocus(boolean var1) {
      this.editBox.setFocused(p_setFocus_1_);
   }

   public void setValue(String var1) {
      this.editBox.setText(p_setValue_1_);
   }

   public void keyPressed(char var1, int var2) {
      this.editBox.textboxKeyTyped(p_keyPressed_1_, p_keyPressed_2_);
   }

   public boolean isFocused() {
      return this.editBox.isFocused();
   }

   public void mouseClicked(int var1, int var2, int var3) {
      this.editBox.mouseClicked(p_mouseClicked_1_, p_mouseClicked_2_, p_mouseClicked_3_);
   }

   public void render() {
      this.editBox.drawTextBox();
   }

   public void setMaxLength(int var1) {
      this.editBox.setMaxStringLength(p_setMaxLength_1_);
   }

   public void setIsEditable(boolean var1) {
      this.editBox.setEnabled(p_setIsEditable_1_);
   }
}
