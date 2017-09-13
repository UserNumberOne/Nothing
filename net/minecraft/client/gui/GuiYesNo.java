package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiYesNo extends GuiScreen {
   protected GuiYesNoCallback parentScreen;
   protected String messageLine1;
   private final String messageLine2;
   private final List listLines = Lists.newArrayList();
   protected String confirmButtonText;
   protected String cancelButtonText;
   protected int parentButtonClickedId;
   private int ticksUntilEnable;

   public GuiYesNo(GuiYesNoCallback var1, String var2, String var3, int var4) {
      this.parentScreen = p_i1082_1_;
      this.messageLine1 = p_i1082_2_;
      this.messageLine2 = p_i1082_3_;
      this.parentButtonClickedId = p_i1082_4_;
      this.confirmButtonText = I18n.format("gui.yes");
      this.cancelButtonText = I18n.format("gui.no");
   }

   public GuiYesNo(GuiYesNoCallback var1, String var2, String var3, String var4, String var5, int var6) {
      this.parentScreen = p_i1083_1_;
      this.messageLine1 = p_i1083_2_;
      this.messageLine2 = p_i1083_3_;
      this.confirmButtonText = p_i1083_4_;
      this.cancelButtonText = p_i1083_5_;
      this.parentButtonClickedId = p_i1083_6_;
   }

   public void initGui() {
      this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 155, this.height / 6 + 96, this.confirmButtonText));
      this.buttonList.add(new GuiOptionButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, this.cancelButtonText));
      this.listLines.clear();
      this.listLines.addAll(this.fontRendererObj.listFormattedStringToWidth(this.messageLine2, this.width - 50));
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      this.parentScreen.confirmClicked(button.id == 0, this.parentButtonClickedId);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, this.messageLine1, this.width / 2, 70, 16777215);
      int i = 90;

      for(String s : this.listLines) {
         this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 16777215);
         i += this.fontRendererObj.FONT_HEIGHT;
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public void setButtonDelay(int var1) {
      this.ticksUntilEnable = p_146350_1_;

      for(GuiButton guibutton : this.buttonList) {
         guibutton.enabled = false;
      }

   }

   public void updateScreen() {
      super.updateScreen();
      if (--this.ticksUntilEnable == 0) {
         for(GuiButton guibutton : this.buttonList) {
            guibutton.enabled = true;
         }
      }

   }
}
