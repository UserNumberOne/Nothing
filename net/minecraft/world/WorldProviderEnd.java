package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.ChunkProviderEnd;

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

   public boolean canRespawnHere() {
      return false;
   }

   public boolean isSurfaceWorld() {
      return false;
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
