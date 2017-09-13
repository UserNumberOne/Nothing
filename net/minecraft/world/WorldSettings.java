package net.minecraft.world;

import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class WorldSettings {
   private final long seed;
   private final GameType theGameType;
   private final boolean mapFeaturesEnabled;
   private final boolean hardcoreEnabled;
   private final WorldType terrainType;
   private boolean commandsAllowed;
   private boolean bonusChestEnabled;
   private String generatorOptions;

   public WorldSettings(long var1, GameType var3, boolean var4, boolean var5, WorldType var6) {
      this.generatorOptions = "";
      this.seed = seedIn;
      this.theGameType = gameType;
      this.mapFeaturesEnabled = enableMapFeatures;
      this.hardcoreEnabled = hardcoreMode;
      this.terrainType = worldTypeIn;
   }

   public WorldSettings(WorldInfo var1) {
      this(info.getSeed(), info.getGameType(), info.isMapFeaturesEnabled(), info.isHardcoreModeEnabled(), info.getTerrainType());
   }

   public WorldSettings enableBonusChest() {
      this.bonusChestEnabled = true;
      return this;
   }

   public WorldSettings setGeneratorOptions(String var1) {
      this.generatorOptions = options;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public WorldSettings enableCommands() {
      this.commandsAllowed = true;
      return this;
   }

   public boolean isBonusChestEnabled() {
      return this.bonusChestEnabled;
   }

   public long getSeed() {
      return this.seed;
   }

   public GameType getGameType() {
      return this.theGameType;
   }

   public boolean getHardcoreEnabled() {
      return this.hardcoreEnabled;
   }

   public boolean isMapFeaturesEnabled() {
      return this.mapFeaturesEnabled;
   }

   public WorldType getTerrainType() {
      return this.terrainType;
   }

   public boolean areCommandsAllowed() {
      return this.commandsAllowed;
   }

   public static GameType getGameTypeById(int var0) {
      return GameType.getByID(id);
   }

   public String getGeneratorOptions() {
      return this.generatorOptions;
   }
}
