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
      this.world = worldIn;
      this.terrainType = worldIn.getWorldInfo().getTerrainType();
      this.generatorSettings = worldIn.getWorldInfo().getGeneratorOptions();
      this.createBiomeProvider();
      this.generateLightBrightnessTable();
   }

   protected void generateLightBrightnessTable() {
      float f = 0.0F;

      for(int i = 0; i <= 15; ++i) {
         float f1 = 1.0F - (float)i / 15.0F;
         this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F + 0.0F;
      }

   }

   protected void createBiomeProvider() {
      this.biomeProvider = this.terrainType.getBiomeProvider(this.world);
   }

   public IChunkGenerator createChunkGenerator() {
      return this.terrainType.getChunkGenerator(this.world, this.generatorSettings);
   }

   public boolean canCoordinateBeSpawn(int var1, int var2) {
      BlockPos blockpos = new BlockPos(x, 0, z);
      return this.world.getBiome(blockpos).ignorePlayerSpawnSuitability() ? true : this.world.getGroundAboveSeaLevel(blockpos).getBlock() == Blocks.GRASS;
   }

   public float calculateCelestialAngle(long var1, float var3) {
      int i = (int)(worldTime % 24000L);
      float f = ((float)i + partialTicks) / 24000.0F - 0.25F;
      if (f < 0.0F) {
         ++f;
      }

      if (f > 1.0F) {
         --f;
      }

      float f1 = 1.0F - (float)((Math.cos((double)f * 3.141592653589793D) + 1.0D) / 2.0D);
      f = f + (f1 - f) / 3.0F;
      return f;
   }

   public int getMoonPhase(long var1) {
      return (int)(worldTime / 24000L % 8L + 8L) % 8;
   }

   public boolean isSurfaceWorld() {
      return true;
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public float[] calcSunriseSunsetColors(float var1, float var2) {
      float f = 0.4F;
      float f1 = MathHelper.cos(celestialAngle * 6.2831855F) - 0.0F;
      float f2 = -0.0F;
      if (f1 >= -0.4F && f1 <= 0.4F) {
         float f3 = (f1 - -0.0F) / 0.4F * 0.5F + 0.5F;
         float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * 3.1415927F)) * 0.99F;
         f4 = f4 * f4;
         this.colorsSunriseSunset[0] = f3 * 0.3F + 0.7F;
         this.colorsSunriseSunset[1] = f3 * f3 * 0.7F + 0.2F;
         this.colorsSunriseSunset[2] = f3 * f3 * 0.0F + 0.2F;
         this.colorsSunriseSunset[3] = f4;
         return this.colorsSunriseSunset;
      } else {
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getFogColor(float var1, float var2) {
      float f = MathHelper.cos(p_76562_1_ * 6.2831855F) * 2.0F + 0.5F;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      float f1 = 0.7529412F;
      float f2 = 0.84705883F;
      float f3 = 1.0F;
      f1 = f1 * (f * 0.94F + 0.06F);
      f2 = f2 * (f * 0.94F + 0.06F);
      f3 = f3 * (f * 0.91F + 0.09F);
      return new Vec3d((double)f1, (double)f2, (double)f3);
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
      this.dimensionId = dim;
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
      this.skyRenderer = skyRenderer;
   }

   @SideOnly(Side.CLIENT)
   public IRenderHandler getCloudRenderer() {
      return this.cloudRenderer;
   }

   @SideOnly(Side.CLIENT)
   public void setCloudRenderer(IRenderHandler var1) {
      this.cloudRenderer = renderer;
   }

   @SideOnly(Side.CLIENT)
   public IRenderHandler getWeatherRenderer() {
      return this.weatherRenderer;
   }

   @SideOnly(Side.CLIENT)
   public void setWeatherRenderer(IRenderHandler var1) {
      this.weatherRenderer = renderer;
   }

   public BlockPos getRandomizedSpawnPoint() {
      BlockPos ret = this.world.getSpawnPoint();
      boolean isAdventure = this.world.getWorldInfo().getGameType() == GameType.ADVENTURE;
      int spawnFuzz = this.world instanceof WorldServer ? this.terrainType.getSpawnFuzz((WorldServer)this.world, this.world.getMinecraftServer()) : 1;
      int border = MathHelper.floor(this.world.getWorldBorder().getClosestDistance((double)ret.getX(), (double)ret.getZ()));
      if (border < spawnFuzz) {
         spawnFuzz = border;
      }

      if (!this.hasNoSky() && !isAdventure && spawnFuzz != 0) {
         if (spawnFuzz < 2) {
            spawnFuzz = 2;
         }

         int spawnFuzzHalf = spawnFuzz / 2;
         ret = this.world.getTopSolidOrLiquidBlock(ret.add(this.world.rand.nextInt(spawnFuzzHalf) - spawnFuzz, 0, this.world.rand.nextInt(spawnFuzzHalf) - spawnFuzz));
      }

      return ret;
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
      return this.world.getBiomeForCoordsBody(pos);
   }

   public boolean isDaytime() {
      return this.world.getSkylightSubtracted() < 4;
   }

   public float getSunBrightnessFactor(float var1) {
      return this.world.getSunBrightnessFactor(par1);
   }

   public float getCurrentMoonPhaseFactor() {
      return this.world.getCurrentMoonPhaseFactorBody();
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getSkyColor(Entity var1, float var2) {
      return this.world.getSkyColorBody(cameraEntity, partialTicks);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getCloudColor(float var1) {
      return this.world.getCloudColorBody(partialTicks);
   }

   @SideOnly(Side.CLIENT)
   public float getSunBrightness(float var1) {
      return this.world.getSunBrightnessBody(par1);
   }

   @SideOnly(Side.CLIENT)
   public float getStarBrightness(float var1) {
      return this.world.getStarBrightnessBody(par1);
   }

   public void setAllowedSpawnTypes(boolean var1, boolean var2) {
      this.world.spawnHostileMobs = allowHostile;
      this.world.spawnPeacefulMobs = allowPeaceful;
   }

   public void calculateInitialWeather() {
      this.world.calculateInitialWeatherBody();
   }

   public void updateWeather() {
      this.world.updateWeatherBody();
   }

   public boolean canBlockFreeze(BlockPos var1, boolean var2) {
      return this.world.canBlockFreezeBody(pos, byWater);
   }

   public boolean canSnowAt(BlockPos var1, boolean var2) {
      return this.world.canSnowAtBody(pos, checkLight);
   }

   public void setWorldTime(long var1) {
      this.world.worldInfo.setWorldTime(time);
   }

   public long getSeed() {
      return this.world.worldInfo.getSeed();
   }

   public long getWorldTime() {
      return this.world.worldInfo.getWorldTime();
   }

   public BlockPos getSpawnPoint() {
      WorldInfo info = this.world.worldInfo;
      return new BlockPos(info.getSpawnX(), info.getSpawnY(), info.getSpawnZ());
   }

   public void setSpawnPoint(BlockPos var1) {
      this.world.worldInfo.setSpawn(pos);
   }

   public boolean canMineBlock(EntityPlayer var1, BlockPos var2) {
      return this.world.canMineBlockBody(player, pos);
   }

   public boolean isBlockHighHumidity(BlockPos var1) {
      return this.world.getBiome(pos).isHighHumidity();
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
