package net.minecraft.entity;

public interface IJumpingMount {
   boolean canJump();

   void handleStartJump(int var1);

   void handleStopJump();
}
