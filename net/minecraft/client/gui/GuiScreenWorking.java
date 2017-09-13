package net.minecraft.client.gui;

import net.minecraft.util.IProgressUpdate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScreenWorking extends GuiScreen implements IProgressUpdate {
   private String title = "";
   private String stage = "";
   private int progress;
   private boolean doneWorking;

   public void displaySavingString(String var1) {
      this.resetProgressAndMessage(var1);
   }

   public void resetProgressAndMessage(String var1) {
      this.title = var1;
      this.displayLoadingString("Working...");
   }

   public void displayLoadingString(String var1) {
      this.stage = var1;
      this.setLoadingProgress(0);
   }

   public void setLoadingProgress(int var1) {
      this.progress = var1;
   }

   public void setDoneWorking() {
      this.doneWorking = true;
   }

   public void drawScreen(int var1, int var2, float var3) {
      if (this.doneWorking) {
         if (!this.mc.isConnectedToRealms()) {
            this.mc.displayGuiScreen((GuiScreen)null);
         }
      } else {
         this.drawDefaultBackground();
         this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 70, 16777215);
         this.drawCenteredString(this.fontRendererObj, this.stage + " " + this.progress + "%", this.width / 2, 90, 16777215);
         super.drawScreen(var1, var2, var3);
      }

   }
}
