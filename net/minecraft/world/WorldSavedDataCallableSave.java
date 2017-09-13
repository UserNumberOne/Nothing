package net.minecraft.world;

public class WorldSavedDataCallableSave implements Runnable {
   private final WorldSavedData data;

   public WorldSavedDataCallableSave(WorldSavedData var1) {
      this.data = var1;
   }

   public void run() {
      this.data.markDirty();
   }
}
