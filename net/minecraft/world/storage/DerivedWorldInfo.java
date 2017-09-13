package net.minecraft.world.storage;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DerivedWorldInfo extends WorldInfo {
   private final WorldInfo delegate;

   public DerivedWorldInfo(WorldInfo var1) {
      this.delegate = var1;
   }

   public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound var1) {
      return this.delegate.cloneNBTCompound(var1);
   }

   public long getSeed() {
      return this.delegate.getSeed();
   }

   public int getSpawnX() {
      return this.delegate.getSpawnX();
   }

   public int getSpawnY() {
      return this.delegate.getSpawnY();
   }

   public int getSpawnZ() {
      return this.delegate.getSpawnZ();
   }

   public long getWorldTotalTime() {
      return this.delegate.getWorldTotalTime();
   }

   public long getWorldTime() {
      return this.delegate.getWorldTime();
   }

   @SideOnly(Side.CLIENT)
   public long getSizeOnDisk() {
      return this.delegate.getSizeOnDisk();
   }

   public NBTTagCompound getPlayerNBTTagCompound() {
      return this.delegate.getPlayerNBTTagCompound();
   }

   public String getWorldName() {
      return this.delegate.getWorldName();
   }

   public int getSaveVersion() {
      return this.delegate.getSaveVersion();
   }

   @SideOnly(Side.CLIENT)
   public long getLastTimePlayed() {
      return this.delegate.getLastTimePlayed();
   }

   public boolean isThundering() {
      return this.delegate.isThundering();
   }

   public int getThunderTime() {
      return this.delegate.getThunderTime();
   }

   public boolean isRaining() {
      return this.delegate.isRaining();
   }

   public int getRainTime() {
      return this.delegate.getRainTime();
   }

   public GameType getGameType() {
      return this.delegate.getGameType();
   }

   @SideOnly(Side.CLIENT)
   public void setSpawnX(int var1) {
   }

   @SideOnly(Side.CLIENT)
   public void setSpawnY(int var1) {
   }

   public void setWorldTotalTime(long var1) {
   }

   @SideOnly(Side.CLIENT)
   public void setSpawnZ(int var1) {
   }

   public void setWorldTime(long var1) {
   }

   public void setSpawn(BlockPos var1) {
   }

   public void setWorldName(String var1) {
   }

   public void setSaveVersion(int var1) {
   }

   public void setThundering(boolean var1) {
   }

   public void setThunderTime(int var1) {
   }

   public void setRaining(boolean var1) {
   }

   public void setRainTime(int var1) {
   }

   public boolean isMapFeaturesEnabled() {
      return this.delegate.isMapFeaturesEnabled();
   }

   public boolean isHardcoreModeEnabled() {
      return this.delegate.isHardcoreModeEnabled();
   }

   public WorldType getTerrainType() {
      return this.delegate.getTerrainType();
   }

   public void setTerrainType(WorldType var1) {
   }

   public boolean areCommandsAllowed() {
      return this.delegate.areCommandsAllowed();
   }

   public void setAllowCommands(boolean var1) {
   }

   public boolean isInitialized() {
      return this.delegate.isInitialized();
   }

   public void setServerInitialized(boolean var1) {
   }

   public GameRules getGameRulesInstance() {
      return this.delegate.getGameRulesInstance();
   }

   public EnumDifficulty getDifficulty() {
      return this.delegate.getDifficulty();
   }

   public void setDifficulty(EnumDifficulty var1) {
   }

   public boolean isDifficultyLocked() {
      return this.delegate.isDifficultyLocked();
   }

   public void setDifficultyLocked(boolean var1) {
   }

   public void setDimensionData(DimensionType var1, NBTTagCompound var2) {
      this.delegate.setDimensionData(var1, var2);
   }

   public NBTTagCompound getDimensionData(DimensionType var1) {
      return this.delegate.getDimensionData(var1);
   }
}
