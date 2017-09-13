package net.minecraft.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IJumpingMount {
   @SideOnly(Side.CLIENT)
   void setJumpPower(int var1);

   boolean canJump();

   void handleStartJump(int var1);

   void handleStopJump();
}
