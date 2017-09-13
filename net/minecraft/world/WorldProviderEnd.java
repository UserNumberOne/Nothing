package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderEnd extends WorldProvider {
   private DragonFightManager dragonFightManager;

   public void createBiomeProvider() {
      this.biomeProvider = new BiomeProviderSingle(Biomes.SKY);
      this.hasNoSky = true;
      NBTTagCompound var1 = this.world.getWorldInfo().getDimensionData(DimensionType.THE_END);
      this.dragonFightManager = this.world instanceof WorldServer ? new DragonFightManager((WorldServer)this.world, var1.getCompoundTag("DragonFight")) : null;
   }

   public IChunkGenerator createChunkGenerator() {
      return new ChunkProviderEnd(this.world, this.world.getWorldInfo().isMapFeaturesEnabled(), this.world.getSeed());
   }

   public float calculateCelestialAngle(long var1, float var3) {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   public float[] calcSunriseSunsetColors(float var1, float var2) {
      return null;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getFogColor(float var1, float var2) {
      int var3 = 10518688;
      float var4 = MathHelper.cos(var1 * 6.2831855F) * 2.0F + 0.5F;
      var4 = MathHelper.clamp(var4, 0.0F, 1.0F);
      float var5 = 0.627451F;
      float var6 = 0.5019608F;
      float var7 = 0.627451F;
      var5 = var5 * (var4 * 0.0F + 0.15F);
      var6 = var6 * (var4 * 0.0F + 0.15F);
      var7 = var7 * (var4 * 0.0F + 0.15F);
      return new Vec3d((double)var5, (double)var6, (double)var7);
   }

   @SideOnly(Side.CLIENT)
   public boolean isSkyColored() {
      return false;
   }

   public boolean canRespawnHere() {
      return false;
   }

   public boolean isSurfaceWorld() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public float getCloudHeight() {
      return 8.0F;
   }

   public boolean canCoordinateBeSpawn(int var1, int var2) {
      return this.world.getGroundAboveSeaLevel(new BlockPos(var1, 0, var2)).getMaterial().blocksMovement();
   }

   public BlockPos getSpawnCoordinate() {
      return new BlockPos(100, 50, 0);
   }

   public int getAverageGroundLevel() {
      return 50;
   }

   @SideOnly(Side.CLIENT)
   public boolean doesXZShowFog(int var1, int var2) {
      return false;
   }

   public DimensionType getDimensionType() {
      return DimensionType.THE_END;
   }

   public void onWorldSave() {
      NBTTagCompound var1 = new NBTTagCompound();
      if (this.dragonFightManager != null) {
         var1.setTag("DragonFight", this.dragonFightManager.getCompound());
      }

      this.world.getWorldInfo().setDimensionData(DimensionType.THE_END, var1);
   }

   public void onWorldUpdateEntities() {
      if (this.dragonFightManager != null) {
         this.dragonFightManager.tick();
      }

   }

   @Nullable
   public DragonFightManager getDragonFightManager() {
      return this.dragonFightManager;
   }
}
