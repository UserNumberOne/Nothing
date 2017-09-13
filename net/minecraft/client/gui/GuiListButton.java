package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiListButton extends GuiButton {
   private boolean value;
   private final String localizationStr;
   private final GuiPageButtonList.GuiResponder guiResponder;

   public GuiListButton(GuiPageButtonList.GuiResponder var1, int var2, int var3, int var4, String var5, boolean var6) {
      super(var2, var3, var4, 150, 20, "");
      this.localizationStr = var5;
      this.value = var6;
      this.displayString = this.buildDisplayString();
      this.guiResponder = var1;
   }

   private String buildDisplayString() {
      return I18n.format(this.localizationStr) + ": " + I18n.format(this.value ? "gui.yes" : "gui.no");
   }

   public void setValue(boolean var1) {
      this.value = var1;
      this.displayString = this.buildDisplayString();
      this.guiResponder.setEntryValue(this.id, var1);
   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      if (super.mousePressed(var1, var2, var3)) {
         this.value = !this.value;
         this.displayString = this.buildDisplayString();
         this.guiResponder.setEntryValue(this.id, this.value);
         return true;
      } else {
         return false;
      }
   }
}
