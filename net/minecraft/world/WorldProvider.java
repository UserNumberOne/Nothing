package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
   private IRenderHandler skyRenderer = null;
   private IRenderHandler cloudRenderer = null;
   private IRenderHandler weatherRenderer = null;
   private int dimensionId;

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
      this.biomeProvider = this.terrainType.getBiomeProvider(this.world);
   }

   public IChunkGenerator createChunkGenerator() {
      return this.terrainType.getChunkGenerator(this.world, this.generatorSettings);
   }

   public boolean canCoordinateBeSpawn(int var1, int var2) {
      BlockPos var3 = new BlockPos(var1, 0, var2);
      return this.world.getBiome(var3).ignorePlayerSpawnSuitability() ? true : this.world.getGroundAboveSeaLevel(var3).getBlock() == Blocks.GRASS;
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

      float var6 = 1.0F - (float)((Math.cos((double)var5 * 3.141592653589793D) + 1.0D) / 2.0D);
      var5 = var5 + (var6 - var5) / 3.0F;
      return var5;
   }

   public int getMoonPhase(long var1) {
      return (int)(var1 / 24000L % 8L + 8L) % 8;
   }

   public boolean isSurfaceWorld() {
      return true;
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public float[] calcSunriseSunsetColors(float var1, float var2) {
      float var3 = 0.4F;
      float var4 = MathHelper.cos(var1 * 6.2831855F) - 0.0F;
      float var5 = -0.0F;
      if (var4 >= -0.4F && var4 <= 0.4F) {
         float var6 = (var4 - -0.0F) / 0.4F * 0.5F + 0.5F;
         float var7 = 1.0F - (1.0F - MathHelper.sin(var6 * 3.1415927F)) * 0.99F;
         var7 = var7 * var7;
         this.colorsSunriseSunset[0] = var6 * 0.3F + 0.7F;
         this.colorsSunriseSunset[1] = var6 * var6 * 0.7F + 0.2F;
         this.colorsSunriseSunset[2] = var6 * var6 * 0.0F + 0.2F;
         this.colorsSunriseSunset[3] = var7;
         return this.colorsSunriseSunset;
      } else {
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getFogColor(float var1, float var2) {
      float var3 = MathHelper.cos(var1 * 6.2831855F) * 2.0F + 0.5F;
      var3 = MathHelper.clamp(var3, 0.0F, 1.0F);
      float var4 = 0.7529412F;
      float var5 = 0.84705883F;
      float var6 = 1.0F;
      var4 = var4 * (var3 * 0.94F + 0.06F);
      var5 = var5 * (var3 * 0.94F + 0.06F);
      var6 = var6 * (var3 * 0.91F + 0.09F);
      return new Vec3d((double)var4, (double)var5, (double)var6);
   }

   public boolean canRespawnHere() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public float getCloudHeight() {
      return this.terrainType.getCloudHeight();
   }

   @SideOnly(Side.CLIENT)
   public boolean isSkyColored() {
      return true;
   }

   public BlockPos getSpawnCoordinate() {
      return null;
   }

   public int getAverageGroundLevel() {
      return this.terrainType.getMinimumSpawnHeight(this.world);
   }

   @SideOnly(Side.CLIENT)
   public double getVoidFogYFactor() {
      return this.terrainType.voidFadeMagnitude();
   }

   @SideOnly(Side.CLIENT)
   public boolean doesXZShowFog(int var1, int var2) {
      return false;
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

   public void setDimension(int var1) {
      this.dimensionId = var1;
   }

   public int getDimension() {
      return this.dimensionId;
   }

   public String getSaveFolder() {
      return this.dimensionId == 0 ? null : "DIM" + this.dimensionId;
   }

   public String getWelcomeMessage() {
      if (this instanceof WorldProviderEnd) {
         return "Entering the End";
      } else {
         return this instanceof WorldProviderHell ? "Entering the Nether" : null;
      }
   }

   public String getDepartMessage() {
      if (this instanceof WorldProviderEnd) {
         return "Leaving the End";
      } else {
         return this instanceof WorldProviderHell ? "Leaving the Nether" : null;
      }
   }

   public double getMovementFactor() {
      return this instanceof WorldProviderHell ? 8.0D : 1.0D;
   }

   @SideOnly(Side.CLIENT)
   public IRenderHandler getSkyRenderer() {
      return this.skyRenderer;
   }

   @SideOnly(Side.CLIENT)
   public void setSkyRenderer(IRenderHandler var1) {
      this.skyRenderer = var1;
   }

   @SideOnly(Side.CLIENT)
   public IRenderHandler getCloudRenderer() {
      return this.cloudRenderer;
   }

   @SideOnly(Side.CLIENT)
   public void setCloudRenderer(IRenderHandler var1) {
      this.cloudRenderer = var1;
   }

   @SideOnly(Side.CLIENT)
   public IRenderHandler getWeatherRenderer() {
      return this.weatherRenderer;
   }

   @SideOnly(Side.CLIENT)
   public void setWeatherRenderer(IRenderHandler var1) {
      this.weatherRenderer = var1;
   }

   public BlockPos getRandomizedSpawnPoint() {
      BlockPos var1 = this.world.getSpawnPoint();
      boolean var2 = this.world.getWorldInfo().getGameType() == GameType.ADVENTURE;
      int var3 = this.world instanceof WorldServer ? this.terrainType.getSpawnFuzz((WorldServer)this.world, this.world.getMinecraftServer()) : 1;
      int var4 = MathHelper.floor(this.world.getWorldBorder().getClosestDistance((double)var1.getX(), (double)var1.getZ()));
      if (var4 < var3) {
         var3 = var4;
      }

      if (!this.hasNoSky() && !var2 && var3 != 0) {
         if (var3 < 2) {
            var3 = 2;
         }

         int var5 = var3 / 2;
         var1 = this.world.getTopSolidOrLiquidBlock(var1.add(this.world.rand.nextInt(var5) - var3, 0, this.world.rand.nextInt(var5) - var3));
      }

      return var1;
   }

   public boolean shouldMapSpin(String var1, double var2, double var4, double var6) {
      return this.dimensionId < 0;
   }

   public int getRespawnDimension(EntityPlayerMP var1) {
      return 0;
   }

   public ICapabilityProvider initCapabilities() {
      return null;
   }

   public Biome getBiomeForCoords(BlockPos var1) {
      return this.world.getBiomeForCoordsBody(var1);
   }

   public boolean isDaytime() {
      return this.world.getSkylightSubtracted() < 4;
   }

   public float getSunBrightnessFactor(float var1) {
      return this.world.getSunBrightnessFactor(var1);
   }

   public float getCurrentMoonPhaseFactor() {
      return this.world.getCurrentMoonPhaseFactorBody();
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getSkyColor(Entity var1, float var2) {
      return this.world.getSkyColorBody(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getCloudColor(float var1) {
      return this.world.getCloudColorBody(var1);
   }

   @SideOnly(Side.CLIENT)
   public float getSunBrightness(float var1) {
      return this.world.getSunBrightnessBody(var1);
   }

   @SideOnly(Side.CLIENT)
   public float getStarBrightness(float var1) {
      return this.world.getStarBrightnessBody(var1);
   }

   public void setAllowedSpawnTypes(boolean var1, boolean var2) {
      this.world.spawnHostileMobs = var1;
      this.world.spawnPeacefulMobs = var2;
   }

   public void calculateInitialWeather() {
      this.world.calculateInitialWeatherBody();
   }

   public void updateWeather() {
      this.world.updateWeatherBody();
   }

   public boolean canBlockFreeze(BlockPos var1, boolean var2) {
      return this.world.canBlockFreezeBody(var1, var2);
   }

   public boolean canSnowAt(BlockPos var1, boolean var2) {
      return this.world.canSnowAtBody(var1, var2);
   }

   public void setWorldTime(long var1) {
      this.world.worldInfo.setWorldTime(var1);
   }

   public long getSeed() {
      return this.world.worldInfo.getSeed();
   }

   public long getWorldTime() {
      return this.world.worldInfo.getWorldTime();
   }

   public BlockPos getSpawnPoint() {
      WorldInfo var1 = this.world.worldInfo;
      return new BlockPos(var1.getSpawnX(), var1.getSpawnY(), var1.getSpawnZ());
   }

   public void setSpawnPoint(BlockPos var1) {
      this.world.worldInfo.setSpawn(var1);
   }

   public boolean canMineBlock(EntityPlayer var1, BlockPos var2) {
      return this.world.canMineBlockBody(var1, var2);
   }

   public boolean isBlockHighHumidity(BlockPos var1) {
      return this.world.getBiome(var1).isHighHumidity();
   }

   public int getHeight() {
      return 256;
   }

   public int getActualHeight() {
      return this.hasNoSky ? 128 : 256;
   }

   public double getHorizon() {
      return this.world.worldInfo.getTerrainType().getHorizon(this.world);
   }

   public void resetRainAndThunder() {
      this.world.worldInfo.setRainTime(0);
      this.world.worldInfo.setRaining(false);
      this.world.worldInfo.setThunderTime(0);
      this.world.worldInfo.setThundering(false);
   }

   public boolean canDoLightning(Chunk var1) {
      return true;
   }

   public boolean canDoRainSnowIce(Chunk var1) {
      return true;
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
