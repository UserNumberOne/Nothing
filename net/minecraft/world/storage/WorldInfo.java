package net.minecraft.world.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldInfo {
   private String versionName;
   private int versionId;
   private boolean versionSnapshot;
   public static final EnumDifficulty DEFAULT_DIFFICULTY = EnumDifficulty.NORMAL;
   private long randomSeed;
   private WorldType terrainType = WorldType.DEFAULT;
   private String generatorOptions = "";
   private int spawnX;
   private int spawnY;
   private int spawnZ;
   private long totalTime;
   private long worldTime;
   private long lastTimePlayed;
   private long sizeOnDisk;
   private NBTTagCompound playerTag;
   private int dimension;
   private String levelName;
   private int saveVersion;
   private int cleanWeatherTime;
   private boolean raining;
   private int rainTime;
   private boolean thundering;
   private int thunderTime;
   private GameType theGameType;
   private boolean mapFeaturesEnabled;
   private boolean hardcore;
   private boolean allowCommands;
   private boolean initialized;
   private EnumDifficulty difficulty;
   private boolean difficultyLocked;
   private double borderCenterX;
   private double borderCenterZ;
   private double borderSize = 6.0E7D;
   private long borderSizeLerpTime;
   private double borderSizeLerpTarget;
   private double borderSafeZone = 5.0D;
   private double borderDamagePerBlock = 0.2D;
   private int borderWarningDistance = 5;
   private int borderWarningTime = 15;
   private final Map dimensionData = Maps.newEnumMap(DimensionType.class);
   private GameRules theGameRules = new GameRules();
   private Map additionalProperties;

   protected WorldInfo() {
   }

   public static void registerFixes(DataFixer var0) {
      var0.registerWalker(FixTypes.LEVEL, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if (var2.hasKey("Player", 10)) {
               var2.setTag("Player", var1.process(FixTypes.PLAYER, var2.getCompoundTag("Player"), var3));
            }

            return var2;
         }
      });
   }

   public WorldInfo(NBTTagCompound var1) {
      if (var1.hasKey("Version", 10)) {
         NBTTagCompound var2 = var1.getCompoundTag("Version");
         this.versionName = var2.getString("Name");
         this.versionId = var2.getInteger("Id");
         this.versionSnapshot = var2.getBoolean("Snapshot");
      }

      this.randomSeed = var1.getLong("RandomSeed");
      if (var1.hasKey("generatorName", 8)) {
         String var5 = var1.getString("generatorName");
         this.terrainType = WorldType.parseWorldType(var5);
         if (this.terrainType == null) {
            this.terrainType = WorldType.DEFAULT;
         } else if (this.terrainType.isVersioned()) {
            int var3 = 0;
            if (var1.hasKey("generatorVersion", 99)) {
               var3 = var1.getInteger("generatorVersion");
            }

            this.terrainType = this.terrainType.getWorldTypeForGeneratorVersion(var3);
         }

         if (var1.hasKey("generatorOptions", 8)) {
            this.generatorOptions = var1.getString("generatorOptions");
         }
      }

      this.theGameType = GameType.getByID(var1.getInteger("GameType"));
      if (var1.hasKey("MapFeatures", 99)) {
         this.mapFeaturesEnabled = var1.getBoolean("MapFeatures");
      } else {
         this.mapFeaturesEnabled = true;
      }

      this.spawnX = var1.getInteger("SpawnX");
      this.spawnY = var1.getInteger("SpawnY");
      this.spawnZ = var1.getInteger("SpawnZ");
      this.totalTime = var1.getLong("Time");
      if (var1.hasKey("DayTime", 99)) {
         this.worldTime = var1.getLong("DayTime");
      } else {
         this.worldTime = this.totalTime;
      }

      this.lastTimePlayed = var1.getLong("LastPlayed");
      this.sizeOnDisk = var1.getLong("SizeOnDisk");
      this.levelName = var1.getString("LevelName");
      this.saveVersion = var1.getInteger("version");
      this.cleanWeatherTime = var1.getInteger("clearWeatherTime");
      this.rainTime = var1.getInteger("rainTime");
      this.raining = var1.getBoolean("raining");
      this.thunderTime = var1.getInteger("thunderTime");
      this.thundering = var1.getBoolean("thundering");
      this.hardcore = var1.getBoolean("hardcore");
      if (var1.hasKey("initialized", 99)) {
         this.initialized = var1.getBoolean("initialized");
      } else {
         this.initialized = true;
      }

      if (var1.hasKey("allowCommands", 99)) {
         this.allowCommands = var1.getBoolean("allowCommands");
      } else {
         this.allowCommands = this.theGameType == GameType.CREATIVE;
      }

      if (var1.hasKey("Player", 10)) {
         this.playerTag = var1.getCompoundTag("Player");
         this.dimension = this.playerTag.getInteger("Dimension");
      }

      if (var1.hasKey("GameRules", 10)) {
         this.theGameRules.readFromNBT(var1.getCompoundTag("GameRules"));
      }

      if (var1.hasKey("Difficulty", 99)) {
         this.difficulty = EnumDifficulty.getDifficultyEnum(var1.getByte("Difficulty"));
      }

      if (var1.hasKey("DifficultyLocked", 1)) {
         this.difficultyLocked = var1.getBoolean("DifficultyLocked");
      }

      if (var1.hasKey("BorderCenterX", 99)) {
         this.borderCenterX = var1.getDouble("BorderCenterX");
      }

      if (var1.hasKey("BorderCenterZ", 99)) {
         this.borderCenterZ = var1.getDouble("BorderCenterZ");
      }

      if (var1.hasKey("BorderSize", 99)) {
         this.borderSize = var1.getDouble("BorderSize");
      }

      if (var1.hasKey("BorderSizeLerpTime", 99)) {
         this.borderSizeLerpTime = var1.getLong("BorderSizeLerpTime");
      }

      if (var1.hasKey("BorderSizeLerpTarget", 99)) {
         this.borderSizeLerpTarget = var1.getDouble("BorderSizeLerpTarget");
      }

      if (var1.hasKey("BorderSafeZone", 99)) {
         this.borderSafeZone = var1.getDouble("BorderSafeZone");
      }

      if (var1.hasKey("BorderDamagePerBlock", 99)) {
         this.borderDamagePerBlock = var1.getDouble("BorderDamagePerBlock");
      }

      if (var1.hasKey("BorderWarningBlocks", 99)) {
         this.borderWarningDistance = var1.getInteger("BorderWarningBlocks");
      }

      if (var1.hasKey("BorderWarningTime", 99)) {
         this.borderWarningTime = var1.getInteger("BorderWarningTime");
      }

      if (var1.hasKey("DimensionData", 10)) {
         NBTTagCompound var6 = var1.getCompoundTag("DimensionData");

         for(String var4 : var6.getKeySet()) {
            this.dimensionData.put(DimensionType.getById(Integer.parseInt(var4)), var6.getCompoundTag(var4));
         }
      }

   }

   public WorldInfo(WorldSettings var1, String var2) {
      this.populateFromWorldSettings(var1);
      this.levelName = var2;
      this.difficulty = DEFAULT_DIFFICULTY;
      this.initialized = false;
   }

   public void populateFromWorldSettings(WorldSettings var1) {
      this.randomSeed = var1.getSeed();
      this.theGameType = var1.getGameType();
      this.mapFeaturesEnabled = var1.isMapFeaturesEnabled();
      this.hardcore = var1.getHardcoreEnabled();
      this.terrainType = var1.getTerrainType();
      this.generatorOptions = var1.getGeneratorOptions();
      this.allowCommands = var1.areCommandsAllowed();
   }

   public WorldInfo(WorldInfo var1) {
      this.randomSeed = var1.randomSeed;
      this.terrainType = var1.terrainType;
      this.generatorOptions = var1.generatorOptions;
      this.theGameType = var1.theGameType;
      this.mapFeaturesEnabled = var1.mapFeaturesEnabled;
      this.spawnX = var1.spawnX;
      this.spawnY = var1.spawnY;
      this.spawnZ = var1.spawnZ;
      this.totalTime = var1.totalTime;
      this.worldTime = var1.worldTime;
      this.lastTimePlayed = var1.lastTimePlayed;
      this.sizeOnDisk = var1.sizeOnDisk;
      this.playerTag = var1.playerTag;
      this.dimension = var1.dimension;
      this.levelName = var1.levelName;
      this.saveVersion = var1.saveVersion;
      this.rainTime = var1.rainTime;
      this.raining = var1.raining;
      this.thunderTime = var1.thunderTime;
      this.thundering = var1.thundering;
      this.hardcore = var1.hardcore;
      this.allowCommands = var1.allowCommands;
      this.initialized = var1.initialized;
      this.theGameRules = var1.theGameRules;
      this.difficulty = var1.difficulty;
      this.difficultyLocked = var1.difficultyLocked;
      this.borderCenterX = var1.borderCenterX;
      this.borderCenterZ = var1.borderCenterZ;
      this.borderSize = var1.borderSize;
      this.borderSizeLerpTime = var1.borderSizeLerpTime;
      this.borderSizeLerpTarget = var1.borderSizeLerpTarget;
      this.borderSafeZone = var1.borderSafeZone;
      this.borderDamagePerBlock = var1.borderDamagePerBlock;
      this.borderWarningTime = var1.borderWarningTime;
      this.borderWarningDistance = var1.borderWarningDistance;
   }

   public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound var1) {
      if (var1 == null) {
         var1 = this.playerTag;
      }

      NBTTagCompound var2 = new NBTTagCompound();
      this.updateTagCompound(var2, var1);
      return var2;
   }

   private void updateTagCompound(NBTTagCompound var1, NBTTagCompound var2) {
      NBTTagCompound var3 = new NBTTagCompound();
      var3.setString("Name", "1.10.2");
      var3.setInteger("Id", 512);
      var3.setBoolean("Snapshot", false);
      var1.setTag("Version", var3);
      var1.setInteger("DataVersion", 512);
      var1.setLong("RandomSeed", this.randomSeed);
      var1.setString("generatorName", this.terrainType.getName());
      var1.setInteger("generatorVersion", this.terrainType.getGeneratorVersion());
      var1.setString("generatorOptions", this.generatorOptions);
      var1.setInteger("GameType", this.theGameType.getID());
      var1.setBoolean("MapFeatures", this.mapFeaturesEnabled);
      var1.setInteger("SpawnX", this.spawnX);
      var1.setInteger("SpawnY", this.spawnY);
      var1.setInteger("SpawnZ", this.spawnZ);
      var1.setLong("Time", this.totalTime);
      var1.setLong("DayTime", this.worldTime);
      var1.setLong("SizeOnDisk", this.sizeOnDisk);
      var1.setLong("LastPlayed", MinecraftServer.getCurrentTimeMillis());
      var1.setString("LevelName", this.levelName);
      var1.setInteger("version", this.saveVersion);
      var1.setInteger("clearWeatherTime", this.cleanWeatherTime);
      var1.setInteger("rainTime", this.rainTime);
      var1.setBoolean("raining", this.raining);
      var1.setInteger("thunderTime", this.thunderTime);
      var1.setBoolean("thundering", this.thundering);
      var1.setBoolean("hardcore", this.hardcore);
      var1.setBoolean("allowCommands", this.allowCommands);
      var1.setBoolean("initialized", this.initialized);
      var1.setDouble("BorderCenterX", this.borderCenterX);
      var1.setDouble("BorderCenterZ", this.borderCenterZ);
      var1.setDouble("BorderSize", this.borderSize);
      var1.setLong("BorderSizeLerpTime", this.borderSizeLerpTime);
      var1.setDouble("BorderSafeZone", this.borderSafeZone);
      var1.setDouble("BorderDamagePerBlock", this.borderDamagePerBlock);
      var1.setDouble("BorderSizeLerpTarget", this.borderSizeLerpTarget);
      var1.setDouble("BorderWarningBlocks", (double)this.borderWarningDistance);
      var1.setDouble("BorderWarningTime", (double)this.borderWarningTime);
      if (this.difficulty != null) {
         var1.setByte("Difficulty", (byte)this.difficulty.getDifficultyId());
      }

      var1.setBoolean("DifficultyLocked", this.difficultyLocked);
      var1.setTag("GameRules", this.theGameRules.writeToNBT());
      NBTTagCompound var4 = new NBTTagCompound();

      for(Entry var6 : this.dimensionData.entrySet()) {
         var4.setTag(String.valueOf(((DimensionType)var6.getKey()).getId()), (NBTBase)var6.getValue());
      }

      var1.setTag("DimensionData", var4);
      if (var2 != null) {
         var1.setTag("Player", var2);
      }

   }

   public long getSeed() {
      return this.randomSeed;
   }

   public int getSpawnX() {
      return this.spawnX;
   }

   public int getSpawnY() {
      return this.spawnY;
   }

   public int getSpawnZ() {
      return this.spawnZ;
   }

   public long getWorldTotalTime() {
      return this.totalTime;
   }

   public long getWorldTime() {
      return this.worldTime;
   }

   @SideOnly(Side.CLIENT)
   public long getSizeOnDisk() {
      return this.sizeOnDisk;
   }

   public NBTTagCompound getPlayerNBTTagCompound() {
      return this.playerTag;
   }

   @SideOnly(Side.CLIENT)
   public void setSpawnX(int var1) {
      this.spawnX = var1;
   }

   @SideOnly(Side.CLIENT)
   public void setSpawnY(int var1) {
      this.spawnY = var1;
   }

   public void setWorldTotalTime(long var1) {
      this.totalTime = var1;
   }

   @SideOnly(Side.CLIENT)
   public void setSpawnZ(int var1) {
      this.spawnZ = var1;
   }

   public void setWorldTime(long var1) {
      this.worldTime = var1;
   }

   public void setSpawn(BlockPos var1) {
      this.spawnX = var1.getX();
      this.spawnY = var1.getY();
      this.spawnZ = var1.getZ();
   }

   public String getWorldName() {
      return this.levelName;
   }

   public void setWorldName(String var1) {
      this.levelName = var1;
   }

   public int getSaveVersion() {
      return this.saveVersion;
   }

   public void setSaveVersion(int var1) {
      this.saveVersion = var1;
   }

   @SideOnly(Side.CLIENT)
   public long getLastTimePlayed() {
      return this.lastTimePlayed;
   }

   public int getCleanWeatherTime() {
      return this.cleanWeatherTime;
   }

   public void setCleanWeatherTime(int var1) {
      this.cleanWeatherTime = var1;
   }

   public boolean isThundering() {
      return this.thundering;
   }

   public void setThundering(boolean var1) {
      this.thundering = var1;
   }

   public int getThunderTime() {
      return this.thunderTime;
   }

   public void setThunderTime(int var1) {
      this.thunderTime = var1;
   }

   public boolean isRaining() {
      return this.raining;
   }

   public void setRaining(boolean var1) {
      this.raining = var1;
   }

   public int getRainTime() {
      return this.rainTime;
   }

   public void setRainTime(int var1) {
      this.rainTime = var1;
   }

   public GameType getGameType() {
      return this.theGameType;
   }

   public boolean isMapFeaturesEnabled() {
      return this.mapFeaturesEnabled;
   }

   public void setMapFeaturesEnabled(boolean var1) {
      this.mapFeaturesEnabled = var1;
   }

   public void setGameType(GameType var1) {
      this.theGameType = var1;
   }

   public boolean isHardcoreModeEnabled() {
      return this.hardcore;
   }

   public void setHardcore(boolean var1) {
      this.hardcore = var1;
   }

   public WorldType getTerrainType() {
      return this.terrainType;
   }

   public void setTerrainType(WorldType var1) {
      this.terrainType = var1;
   }

   public String getGeneratorOptions() {
      return this.generatorOptions == null ? "" : this.generatorOptions;
   }

   public boolean areCommandsAllowed() {
      return this.allowCommands;
   }

   public void setAllowCommands(boolean var1) {
      this.allowCommands = var1;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public void setServerInitialized(boolean var1) {
      this.initialized = var1;
   }

   public GameRules getGameRulesInstance() {
      return this.theGameRules;
   }

   public double getBorderCenterX() {
      return this.borderCenterX;
   }

   public double getBorderCenterZ() {
      return this.borderCenterZ;
   }

   public double getBorderSize() {
      return this.borderSize;
   }

   public void setBorderSize(double var1) {
      this.borderSize = var1;
   }

   public long getBorderLerpTime() {
      return this.borderSizeLerpTime;
   }

   public void setBorderLerpTime(long var1) {
      this.borderSizeLerpTime = var1;
   }

   public double getBorderLerpTarget() {
      return this.borderSizeLerpTarget;
   }

   public void setBorderLerpTarget(double var1) {
      this.borderSizeLerpTarget = var1;
   }

   public void getBorderCenterZ(double var1) {
      this.borderCenterZ = var1;
   }

   public void getBorderCenterX(double var1) {
      this.borderCenterX = var1;
   }

   public double getBorderSafeZone() {
      return this.borderSafeZone;
   }

   public void setBorderSafeZone(double var1) {
      this.borderSafeZone = var1;
   }

   public double getBorderDamagePerBlock() {
      return this.borderDamagePerBlock;
   }

   public void setBorderDamagePerBlock(double var1) {
      this.borderDamagePerBlock = var1;
   }

   public int getBorderWarningDistance() {
      return this.borderWarningDistance;
   }

   public int getBorderWarningTime() {
      return this.borderWarningTime;
   }

   public void setBorderWarningDistance(int var1) {
      this.borderWarningDistance = var1;
   }

   public void setBorderWarningTime(int var1) {
      this.borderWarningTime = var1;
   }

   public EnumDifficulty getDifficulty() {
      return this.difficulty;
   }

   public void setDifficulty(EnumDifficulty var1) {
      this.difficulty = var1;
   }

   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   public void setDifficultyLocked(boolean var1) {
      this.difficultyLocked = var1;
   }

   public void addToCrashReport(CrashReportCategory var1) {
      var1.setDetail("Level seed", new ICrashReportDetail() {
         public String call() throws Exception {
            return String.valueOf(WorldInfo.this.getSeed());
         }
      });
      var1.setDetail("Level generator", new ICrashReportDetail() {
         public String call() throws Exception {
            return String.format("ID %02d - %s, ver %d. Features enabled: %b", WorldInfo.this.terrainType.getWorldTypeID(), WorldInfo.this.terrainType.getName(), WorldInfo.this.terrainType.getGeneratorVersion(), WorldInfo.this.mapFeaturesEnabled);
         }
      });
      var1.setDetail("Level generator options", new ICrashReportDetail() {
         public String call() throws Exception {
            return WorldInfo.this.generatorOptions;
         }
      });
      var1.setDetail("Level spawn location", new ICrashReportDetail() {
         public String call() throws Exception {
            return CrashReportCategory.getCoordinateInfo(WorldInfo.this.spawnX, WorldInfo.this.spawnY, WorldInfo.this.spawnZ);
         }
      });
      var1.setDetail("Level time", new ICrashReportDetail() {
         public String call() throws Exception {
            return String.format("%d game time, %d day time", WorldInfo.this.totalTime, WorldInfo.this.worldTime);
         }
      });
      var1.setDetail("Level dimension", new ICrashReportDetail() {
         public String call() throws Exception {
            return String.valueOf(WorldInfo.this.dimension);
         }
      });
      var1.setDetail("Level storage version", new ICrashReportDetail() {
         public String call() throws Exception {
            String var1 = "Unknown?";

            try {
               switch(WorldInfo.this.saveVersion) {
               case 19132:
                  var1 = "McRegion";
                  break;
               case 19133:
                  var1 = "Anvil";
               }
            } catch (Throwable var3) {
               ;
            }

            return String.format("0x%05X - %s", WorldInfo.this.saveVersion, var1);
         }
      });
      var1.setDetail("Level weather", new ICrashReportDetail() {
         public String call() throws Exception {
            return String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", WorldInfo.this.rainTime, WorldInfo.this.raining, WorldInfo.this.thunderTime, WorldInfo.this.thundering);
         }
      });
      var1.setDetail("Level game mode", new ICrashReportDetail() {
         public String call() throws Exception {
            return String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", WorldInfo.this.theGameType.getName(), WorldInfo.this.theGameType.getID(), WorldInfo.this.hardcore, WorldInfo.this.allowCommands);
         }
      });
   }

   public void setAdditionalProperties(Map var1) {
      if (this.additionalProperties == null) {
         this.additionalProperties = var1;
      }

   }

   public NBTBase getAdditionalProperty(String var1) {
      return this.additionalProperties != null ? (NBTBase)this.additionalProperties.get(var1) : null;
   }

   public NBTTagCompound getDimensionData(DimensionType var1) {
      NBTTagCompound var2 = (NBTTagCompound)this.dimensionData.get(var1);
      return var2 == null ? new NBTTagCompound() : var2;
   }

   public void setDimensionData(DimensionType var1, NBTTagCompound var2) {
      this.dimensionData.put(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   public int getVersionId() {
      return this.versionId;
   }

   @SideOnly(Side.CLIENT)
   public boolean isVersionSnapshot() {
      return this.versionSnapshot;
   }

   @SideOnly(Side.CLIENT)
   public String getVersionName() {
      return this.versionName;
   }
}
