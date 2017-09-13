package net.minecraft.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IProgressUpdate {
   void displaySavingString(String var1);

   @SideOnly(Side.CLIENT)
   void resetProgressAndMessage(String var1);

   void displayLoadingString(String var1);

   void setLoadingProgress(int var1);

   @SideOnly(Side.CLIENT)
   void setDoneWorking();
}
