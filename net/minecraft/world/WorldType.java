package net.minecraft.world;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateFlatWorld;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiCustomizeWorldScreen;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderDebug;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerBiome;
import net.minecraft.world.gen.layer.GenLayerBiomeEdge;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldType {
   public static WorldType[] WORLD_TYPES = new WorldType[16];
   public static final WorldType DEFAULT = (new WorldType(0, "default", 1)).setVersioned();
   public static final WorldType FLAT = new WorldType(1, "flat");
   public static final WorldType LARGE_BIOMES = new WorldType(2, "largeBiomes");
   public static final WorldType AMPLIFIED = (new WorldType(3, "amplified")).setNotificationData();
   public static final WorldType CUSTOMIZED = new WorldType(4, "customized");
   public static final WorldType DEBUG_WORLD = new WorldType(5, "debug_all_block_states");
   public static final WorldType DEFAULT_1_1 = (new WorldType(8, "default_1_1", 0)).setCanBeCreated(false);
   private final int worldTypeId;
   private final String worldType;
   private final int generatorVersion;
   private boolean canBeCreated;
   private boolean isWorldTypeVersioned;
   private boolean hasNotificationData;

   private WorldType(int var1, String var2) {
      this(var1, var2, 0);
   }

   private WorldType(int var1, String var2, int var3) {
      if (var2.length() > 16 && DEBUG_WORLD != null) {
         throw new IllegalArgumentException("World type names must not be longer then 16: " + var2);
      } else {
         this.worldType = var2;
         this.generatorVersion = var3;
         this.canBeCreated = true;
         this.worldTypeId = var1;
         WORLD_TYPES[var1] = this;
      }
   }

   public String getName() {
      return this.worldType;
   }

   @SideOnly(Side.CLIENT)
   public String getTranslateName() {
      return "generator." + this.worldType;
   }

   @SideOnly(Side.CLIENT)
   public String getTranslatedInfo() {
      return this.getTranslateName() + ".info";
   }

   public int getGeneratorVersion() {
      return this.generatorVersion;
   }

   public WorldType getWorldTypeForGeneratorVersion(int var1) {
      return this == DEFAULT && var1 == 0 ? DEFAULT_1_1 : this;
   }

   private WorldType setCanBeCreated(boolean var1) {
      this.canBeCreated = var1;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean canBeCreated() {
      return this.canBeCreated;
   }

   private WorldType setVersioned() {
      this.isWorldTypeVersioned = true;
      return this;
   }

   public boolean isVersioned() {
      return this.isWorldTypeVersioned;
   }

   public static WorldType parseWorldType(String var0) {
      for(WorldType var4 : WORLD_TYPES) {
         if (var4 != null && var4.worldType.equalsIgnoreCase(var0)) {
            return var4;
         }
      }

      return null;
   }

   public int getWorldTypeID() {
      return this.worldTypeId;
   }

   @SideOnly(Side.CLIENT)
   public boolean showWorldInfoNotice() {
      return this.hasNotificationData;
   }

   private WorldType setNotificationData() {
      this.hasNotificationData = true;
      return this;
   }

   public BiomeProvider getBiomeProvider(World var1) {
      if (this == FLAT) {
         FlatGeneratorInfo var2 = FlatGeneratorInfo.createFlatGeneratorFromString(var1.getWorldInfo().getGeneratorOptions());
         return new BiomeProviderSingle(Biome.getBiome(var2.getBiome(), Biomes.DEFAULT));
      } else {
         return (BiomeProvider)(this == DEBUG_WORLD ? new BiomeProviderSingle(Biomes.PLAINS) : new BiomeProvider(var1.getWorldInfo()));
      }
   }

   public IChunkGenerator getChunkGenerator(World var1, String var2) {
      if (this == FLAT) {
         return new ChunkProviderFlat(var1, var1.getSeed(), var1.getWorldInfo().isMapFeaturesEnabled(), var2);
      } else if (this == DEBUG_WORLD) {
         return new ChunkProviderDebug(var1);
      } else {
         return this == CUSTOMIZED ? new ChunkProviderOverworld(var1, var1.getSeed(), var1.getWorldInfo().isMapFeaturesEnabled(), var2) : new ChunkProviderOverworld(var1, var1.getSeed(), var1.getWorldInfo().isMapFeaturesEnabled(), var2);
      }
   }

   public int getMinimumSpawnHeight(World var1) {
      return this == FLAT ? 4 : var1.getSeaLevel() + 1;
   }

   public double getHorizon(World var1) {
      return this == FLAT ? 0.0D : 63.0D;
   }

   public double voidFadeMagnitude() {
      return this == FLAT ? 1.0D : 0.03125D;
   }

   public boolean handleSlimeSpawnReduction(Random var1, World var2) {
      return this == FLAT ? var1.nextInt(4) != 1 : false;
   }

   private static int getNextID() {
      for(int var0 = 0; var0 < WORLD_TYPES.length; ++var0) {
         if (WORLD_TYPES[var0] == null) {
            return var0;
         }
      }

      int var1 = WORLD_TYPES.length;
      WORLD_TYPES = (WorldType[])Arrays.copyOf(WORLD_TYPES, var1 + 16);
      return var1;
   }

   public WorldType(String var1) {
      this(getNextID(), var1);
   }

   public void onGUICreateWorldPress() {
   }

   public int getSpawnFuzz(WorldServer var1, MinecraftServer var2) {
      return Math.max(0, var2.getSpawnRadius(var1));
   }

   @SideOnly(Side.CLIENT)
   public void onCustomizeButton(Minecraft var1, GuiCreateWorld var2) {
      if (this == FLAT) {
         var1.displayGuiScreen(new GuiCreateFlatWorld(var2, var2.chunkProviderSettingsJson));
      } else if (this == CUSTOMIZED) {
         var1.displayGuiScreen(new GuiCustomizeWorldScreen(var2, var2.chunkProviderSettingsJson));
      }

   }

   public boolean isCustomizable() {
      return this == FLAT || this == CUSTOMIZED;
   }

   public float getCloudHeight() {
      return 128.0F;
   }

   public GenLayer getBiomeLayer(long var1, GenLayer var3, String var4) {
      GenLayerBiome var5 = new GenLayerBiome(200L, var3, this, var4);
      GenLayer var6 = GenLayerZoom.magnify(1000L, var5, 2);
      GenLayerBiomeEdge var7 = new GenLayerBiomeEdge(1000L, var6);
      return var7;
   }
}
