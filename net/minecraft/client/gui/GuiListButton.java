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
      super(p_i45539_2_, p_i45539_3_, p_i45539_4_, 150, 20, "");
      this.localizationStr = p_i45539_5_;
      this.value = p_i45539_6_;
      this.displayString = this.buildDisplayString();
      this.guiResponder = responder;
   }

   private String buildDisplayString() {
      return I18n.format(this.localizationStr) + ": " + I18n.format(this.value ? "gui.yes" : "gui.no");
   }

   public void setValue(boolean var1) {
      this.value = p_175212_1_;
      this.displayString = this.buildDisplayString();
      this.guiResponder.setEntryValue(this.id, p_175212_1_);
   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      if (super.mousePressed(mc, mouseX, mouseY)) {
         this.value = !this.value;
         this.displayString = this.buildDisplayString();
         this.guiResponder.setEntryValue(this.id, this.value);
         return true;
      } else {
         return false;
      }
   }
}
