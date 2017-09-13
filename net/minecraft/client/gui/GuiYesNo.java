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
      this.parentScreen = var1;
      this.messageLine1 = var2;
      this.messageLine2 = var3;
      this.parentButtonClickedId = var4;
      this.confirmButtonText = I18n.format("gui.yes");
      this.cancelButtonText = I18n.format("gui.no");
   }

   public GuiYesNo(GuiYesNoCallback var1, String var2, String var3, String var4, String var5, int var6) {
      this.parentScreen = var1;
      this.messageLine1 = var2;
      this.messageLine2 = var3;
      this.confirmButtonText = var4;
      this.cancelButtonText = var5;
      this.parentButtonClickedId = var6;
   }

   public void initGui() {
      this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 155, this.height / 6 + 96, this.confirmButtonText));
      this.buttonList.add(new GuiOptionButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, this.cancelButtonText));
      this.listLines.clear();
      this.listLines.addAll(this.fontRendererObj.listFormattedStringToWidth(this.messageLine2, this.width - 50));
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      this.parentScreen.confirmClicked(var1.id == 0, this.parentButtonClickedId);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, this.messageLine1, this.width / 2, 70, 16777215);
      int var4 = 90;

      for(String var6 : this.listLines) {
         this.drawCenteredString(this.fontRendererObj, var6, this.width / 2, var4, 16777215);
         var4 += this.fontRendererObj.FONT_HEIGHT;
      }

      super.drawScreen(var1, var2, var3);
   }

   public void setButtonDelay(int var1) {
      this.ticksUntilEnable = var1;

      for(GuiButton var3 : this.buttonList) {
         var3.enabled = false;
      }

   }

   public void updateScreen() {
      super.updateScreen();
      if (--this.ticksUntilEnable == 0) {
         for(GuiButton var2 : this.buttonList) {
            var2.enabled = true;
         }
      }

   }
}
