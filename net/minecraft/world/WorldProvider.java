package net.minecraft.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderDebug;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraft.world.gen.FlatGeneratorInfo;

public abstract class WorldProvider {
   public static final float[] MOON_PHASE_FACTORS = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
   protected World world;
   private WorldType terrainType;
   private String generatorSettings;
   protected BiomeProvider biomeProvider;
   protected boolean doesWaterVaporize;
   protected boolean hasNoSky;
   protected final float[] lightBrightnessTable = new float[16];
   private final float[] colorsSunriseSunset = new float[4];

   public final void registerWorld(World var1) {
      this.world = var1;
      this.terrainType = var1.getWorldInfo().getTerrainType();
      this.generatorSettings = var1.getWorldInfo().getGeneratorOptions();
      this.createBiomeProvider();
      this.generateLightBrightnessTable();
   }

   protected void generateLightBrightnessTable() {
      float var1 = 0.0F;

      for(int var2 = 0; var2 <= 15; ++var2) {
         float var3 = 1.0F - (float)var2 / 15.0F;
         this.lightBrightnessTable[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * 1.0F + 0.0F;
      }

   }

   protected void createBiomeProvider() {
      WorldType var1 = this.world.getWorldInfo().getTerrainType();
      if (var1 == WorldType.FLAT) {
         FlatGeneratorInfo var2 = FlatGeneratorInfo.createFlatGeneratorFromString(this.world.getWorldInfo().getGeneratorOptions());
         this.biomeProvider = new BiomeProviderSingle(Biome.getBiome(var2.getBiome(), Biomes.DEFAULT));
      } else if (var1 == WorldType.DEBUG_WORLD) {
         this.biomeProvider = new BiomeProviderSingle(Biomes.PLAINS);
      } else {
         this.biomeProvider = new BiomeProvider(this.world.getWorldInfo());
      }

   }

   public IChunkGenerator createChunkGenerator() {
      if (this.terrainType == WorldType.FLAT) {
         return new ChunkProviderFlat(this.world, this.world.getSeed(), this.world.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings);
      } else if (this.terrainType == WorldType.DEBUG_WORLD) {
         return new ChunkProviderDebug(this.world);
      } else {
         return this.terrainType == WorldType.CUSTOMIZED ? new ChunkProviderOverworld(this.world, this.world.getSeed(), this.world.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings) : new ChunkProviderOverworld(this.world, this.world.getSeed(), this.world.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings);
      }
   }

   public boolean canCoordinateBeSpawn(int var1, int var2) {
      BlockPos var3 = new BlockPos(var1, 0, var2);
      if (this.world.getBiome(var3).ignorePlayerSpawnSuitability()) {
         return true;
      } else {
         return this.world.getGroundAboveSeaLevel(var3).getBlock() == Blocks.GRASS;
      }
   }

   public float calculateCelestialAngle(long var1, float var3) {
      int var4 = (int)(var1 % 24000L);
      float var5 = ((float)var4 + var3) / 24000.0F - 0.25F;
      if (var5 < 0.0F) {
         ++var5;
      }

      if (var5 > 1.0F) {
         --var5;
      }

      float var7 = 1.0F - (float)((Math.cos((double)var5 * 3.141592653589793D) + 1.0D) / 2.0D);
      var5 = var5 + (var7 - var5) / 3.0F;
      return var5;
   }

   public int getMoonPhase(long var1) {
      return (int)(var1 / 24000L % 8L + 8L) % 8;
   }

   public boolean isSurfaceWorld() {
      return true;
   }

   public boolean canRespawnHere() {
      return true;
   }

   public BlockPos getSpawnCoordinate() {
      return null;
   }

   public int getAverageGroundLevel() {
      return this.terrainType == WorldType.FLAT ? 4 : this.world.getSeaLevel() + 1;
   }

   public BiomeProvider getBiomeProvider() {
      return this.biomeProvider;
   }

   public boolean doesWaterVaporize() {
      return this.doesWaterVaporize;
   }

   public boolean hasNoSky() {
      return this.hasNoSky;
   }

   public float[] getLightBrightnessTable() {
      return this.lightBrightnessTable;
   }

   public WorldBorder createWorldBorder() {
      return new WorldBorder();
   }

   public void onPlayerAdded(EntityPlayerMP var1) {
   }

   public void onPlayerRemoved(EntityPlayerMP var1) {
   }

   public abstract DimensionType getDimensionType();

   public void onWorldSave() {
   }

   public void onWorldUpdateEntities() {
   }

   public boolean canDropChunk(int var1, int var2) {
      return true;
   }
}
