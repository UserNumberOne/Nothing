package net.minecraft.world;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderHell;

public class WorldProviderHell extends WorldProvider {
   public void createBiomeProvider() {
      this.biomeProvider = new BiomeProviderSingle(Biomes.HELL);
      this.doesWaterVaporize = true;
      this.hasNoSky = true;
   }

   protected void generateLightBrightnessTable() {
      float var1 = 0.1F;

      for(int var2 = 0; var2 <= 15; ++var2) {
         float var3 = 1.0F - (float)var2 / 15.0F;
         this.lightBrightnessTable[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * 0.9F + 0.1F;
      }

   }

   public IChunkGenerator createChunkGenerator() {
      return new ChunkProviderHell(this.world, this.world.getWorldInfo().isMapFeaturesEnabled(), this.world.getSeed());
   }

   public boolean isSurfaceWorld() {
      return false;
   }

   public boolean canCoordinateBeSpawn(int var1, int var2) {
      return false;
   }

   public float calculateCelestialAngle(long var1, float var3) {
      return 0.5F;
   }

   public boolean canRespawnHere() {
      return false;
   }

   public WorldBorder createWorldBorder() {
      return new WorldBorder() {
         public double getCenterX() {
            return super.getCenterX() / 8.0D;
         }

         public double getCenterZ() {
            return super.getCenterZ() / 8.0D;
         }
      };
   }

   public DimensionType getDimensionType() {
      return DimensionType.NETHER;
   }
}
