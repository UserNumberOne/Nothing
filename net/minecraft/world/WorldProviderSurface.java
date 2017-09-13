package net.minecraft.world;

public class WorldProviderSurface extends WorldProvider {
   public DimensionType getDimensionType() {
      return DimensionType.OVERWORLD;
   }

   public boolean canDropChunk(int var1, int var2) {
      return !this.world.isSpawnChunk(var1, var2) || !this.world.provider.getDimensionType().shouldLoadSpawn();
   }
}
