package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Biome {
   private static final Logger LOGGER = LogManager.getLogger();
   protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
   protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
   protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
   protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
   protected static final IBlockState RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();
   protected static final IBlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();
   protected static final IBlockState ICE = Blocks.ICE.getDefaultState();
   protected static final IBlockState WATER = Blocks.WATER.getDefaultState();
   public static final Set EXPLORATION_BIOMES_LIST = Sets.newHashSet();
   public static final ObjectIntIdentityMap MUTATION_TO_BASE_ID_MAP = new ObjectIntIdentityMap();
   protected static final NoiseGeneratorPerlin TEMPERATURE_NOISE = new NoiseGeneratorPerlin(new Random(1234L), 1);
   protected static final NoiseGeneratorPerlin GRASS_COLOR_NOISE = new NoiseGeneratorPerlin(new Random(2345L), 1);
   protected static final WorldGenDoublePlant DOUBLE_PLANT_GENERATOR = new WorldGenDoublePlant();
   protected static final WorldGenTrees TREE_FEATURE = new WorldGenTrees(false);
   protected static final WorldGenBigTree BIG_TREE_FEATURE = new WorldGenBigTree(false);
   protected static final WorldGenSwamp SWAMP_FEATURE = new WorldGenSwamp();
   public static final RegistryNamespaced REGISTRY = new RegistryNamespaced();
   private final String biomeName;
   private final float baseHeight;
   private final float heightVariation;
   private final float temperature;
   private final float rainfall;
   private final int waterColor;
   private final boolean enableSnow;
   private final boolean enableRain;
   @Nullable
   private final String baseBiomeRegName;
   public IBlockState topBlock = Blocks.GRASS.getDefaultState();
   public IBlockState fillerBlock = Blocks.DIRT.getDefaultState();
   public BiomeDecorator theBiomeDecorator;
   protected List spawnableMonsterList = Lists.newArrayList();
   protected List spawnableCreatureList = Lists.newArrayList();
   protected List spawnableWaterCreatureList = Lists.newArrayList();
   protected List spawnableCaveCreatureList = Lists.newArrayList();

   public static int getIdForBiome(Biome var0) {
      return REGISTRY.getIDForObject(var0);
   }

   @Nullable
   public static Biome getBiomeForId(int var0) {
      return (Biome)REGISTRY.getObjectById(var0);
   }

   @Nullable
   public static Biome getMutationForBiome(Biome var0) {
      return (Biome)MUTATION_TO_BASE_ID_MAP.getByValue(getIdForBiome(var0));
   }

   protected Biome(Biome.BiomeProperties var1) {
      this.biomeName = var1.biomeName;
      this.baseHeight = var1.baseHeight;
      this.heightVariation = var1.heightVariation;
      this.temperature = var1.temperature;
      this.rainfall = var1.rainfall;
      this.waterColor = var1.waterColor;
      this.enableSnow = var1.enableSnow;
      this.enableRain = var1.enableRain;
      this.baseBiomeRegName = var1.baseBiomeRegName;
      this.theBiomeDecorator = this.createBiomeDecorator();
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntitySheep.class, 12, 4, 4));
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityPig.class, 10, 4, 4));
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityChicken.class, 10, 4, 4));
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityCow.class, 8, 4, 4));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySpider.class, 100, 4, 4));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityZombie.class, 100, 4, 4));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 100, 4, 4));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityCreeper.class, 100, 4, 4));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySlime.class, 100, 4, 4));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityEnderman.class, 10, 1, 4));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWitch.class, 5, 1, 1));
      this.spawnableWaterCreatureList.add(new Biome.SpawnListEntry(EntitySquid.class, 10, 4, 4));
      this.spawnableCaveCreatureList.add(new Biome.SpawnListEntry(EntityBat.class, 10, 8, 8));
   }

   protected BiomeDecorator createBiomeDecorator() {
      return new BiomeDecorator();
   }

   public boolean isMutation() {
      return this.baseBiomeRegName != null;
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(var1.nextInt(10) == 0 ? BIG_TREE_FEATURE : TREE_FEATURE);
   }

   public WorldGenerator getRandomWorldGenForGrass(Random var1) {
      return new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
   }

   public BlockFlower.EnumFlowerType pickRandomFlower(Random var1, BlockPos var2) {
      return var1.nextInt(3) > 0 ? BlockFlower.EnumFlowerType.DANDELION : BlockFlower.EnumFlowerType.POPPY;
   }

   public List getSpawnableList(EnumCreatureType var1) {
      switch(var1) {
      case MONSTER:
         return this.spawnableMonsterList;
      case CREATURE:
         return this.spawnableCreatureList;
      case WATER_CREATURE:
         return this.spawnableWaterCreatureList;
      case AMBIENT:
         return this.spawnableCaveCreatureList;
      default:
         return Collections.emptyList();
      }
   }

   public boolean getEnableSnow() {
      return this.isSnowyBiome();
   }

   public boolean canRain() {
      return this.isSnowyBiome() ? false : this.enableRain;
   }

   public boolean isHighHumidity() {
      return this.getRainfall() > 0.85F;
   }

   public float getSpawningChance() {
      return 0.1F;
   }

   public final float getFloatTemperature(BlockPos var1) {
      if (var1.getY() > 64) {
         float var2 = (float)(TEMPERATURE_NOISE.getValue((double)((float)var1.getX() / 8.0F), (double)((float)var1.getZ() / 8.0F)) * 4.0D);
         return this.getTemperature() - (var2 + (float)var1.getY() - 64.0F) * 0.05F / 30.0F;
      } else {
         return this.getTemperature();
      }
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      this.theBiomeDecorator.decorate(var1, var2, this, var3);
   }

   public void genTerrainBlocks(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      this.generateBiomeTerrain(var1, var2, var3, var4, var5, var6);
   }

   public final void generateBiomeTerrain(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      int var8 = var1.getSeaLevel();
      IBlockState var9 = this.topBlock;
      IBlockState var10 = this.fillerBlock;
      int var11 = -1;
      int var12 = (int)(var6 / 3.0D + 3.0D + var2.nextDouble() * 0.25D);
      int var13 = var4 & 15;
      int var14 = var5 & 15;
      BlockPos.MutableBlockPos var15 = new BlockPos.MutableBlockPos();

      for(int var16 = 255; var16 >= 0; --var16) {
         if (var16 <= var2.nextInt(5)) {
            var3.setBlockState(var14, var16, var13, BEDROCK);
         } else {
            IBlockState var17 = var3.getBlockState(var14, var16, var13);
            if (var17.getMaterial() == Material.AIR) {
               var11 = -1;
            } else if (var17.getBlock() == Blocks.STONE) {
               if (var11 == -1) {
                  if (var12 <= 0) {
                     var9 = AIR;
                     var10 = STONE;
                  } else if (var16 >= var8 - 4 && var16 <= var8 + 1) {
                     var9 = this.topBlock;
                     var10 = this.fillerBlock;
                  }

                  if (var16 < var8 && (var9 == null || var9.getMaterial() == Material.AIR)) {
                     if (this.getFloatTemperature(var15.setPos(var4, var16, var5)) < 0.15F) {
                        var9 = ICE;
                     } else {
                        var9 = WATER;
                     }
                  }

                  var11 = var12;
                  if (var16 >= var8 - 1) {
                     var3.setBlockState(var14, var16, var13, var9);
                  } else if (var16 < var8 - 7 - var12) {
                     var9 = AIR;
                     var10 = STONE;
                     var3.setBlockState(var14, var16, var13, GRAVEL);
                  } else {
                     var3.setBlockState(var14, var16, var13, var10);
                  }
               } else if (var11 > 0) {
                  --var11;
                  var3.setBlockState(var14, var16, var13, var10);
                  if (var11 == 0 && var10.getBlock() == Blocks.SAND && var12 > 1) {
                     var11 = var2.nextInt(4) + Math.max(0, var16 - 63);
                     var10 = var10.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
                  }
               }
            }
         }
      }

   }

   public Class getBiomeClass() {
      return this.getClass();
   }

   public Biome.TempCategory getTempCategory() {
      if ((double)this.getTemperature() < 0.2D) {
         return Biome.TempCategory.COLD;
      } else {
         return (double)this.getTemperature() < 1.0D ? Biome.TempCategory.MEDIUM : Biome.TempCategory.WARM;
      }
   }

   @Nullable
   public static Biome getBiome(int var0) {
      return getBiome(var0, (Biome)null);
   }

   public static Biome getBiome(int var0, Biome var1) {
      Biome var2 = getBiomeForId(var0);
      return var2 == null ? var1 : var2;
   }

   public boolean ignorePlayerSpawnSuitability() {
      return false;
   }

   public final float getBaseHeight() {
      return this.baseHeight;
   }

   public final float getRainfall() {
      return this.rainfall;
   }

   public final String getBiomeName() {
      return this.biomeName;
   }

   public final float getHeightVariation() {
      return this.heightVariation;
   }

   public final float getTemperature() {
      return this.temperature;
   }

   public final boolean isSnowyBiome() {
      return this.enableSnow;
   }

   public static void registerBiomes() {
      registerBiome(0, "ocean", new BiomeOcean((new Biome.BiomeProperties("Ocean")).setBaseHeight(-1.0F).setHeightVariation(0.1F)));
      registerBiome(1, "plains", new BiomePlains(false, (new Biome.BiomeProperties("Plains")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.8F).setRainfall(0.4F)));
      registerBiome(2, "desert", new BiomeDesert((new Biome.BiomeProperties("Desert")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(3, "extreme_hills", new BiomeHills(BiomeHills.Type.NORMAL, (new Biome.BiomeProperties("Extreme Hills")).setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
      registerBiome(4, "forest", new BiomeForest(BiomeForest.Type.NORMAL, (new Biome.BiomeProperties("Forest")).setTemperature(0.7F).setRainfall(0.8F)));
      registerBiome(5, "taiga", new BiomeTaiga(BiomeTaiga.Type.NORMAL, (new Biome.BiomeProperties("Taiga")).setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(0.25F).setRainfall(0.8F)));
      registerBiome(6, "swampland", new BiomeSwamp((new Biome.BiomeProperties("Swampland")).setBaseHeight(-0.2F).setHeightVariation(0.1F).setTemperature(0.8F).setRainfall(0.9F).setWaterColor(14745518)));
      registerBiome(7, "river", new BiomeRiver((new Biome.BiomeProperties("River")).setBaseHeight(-0.5F).setHeightVariation(0.0F)));
      registerBiome(8, "hell", new BiomeHell((new Biome.BiomeProperties("Hell")).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(9, "sky", new BiomeEnd((new Biome.BiomeProperties("The End")).setRainDisabled()));
      registerBiome(10, "frozen_ocean", new BiomeOcean((new Biome.BiomeProperties("FrozenOcean")).setBaseHeight(-1.0F).setHeightVariation(0.1F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
      registerBiome(11, "frozen_river", new BiomeRiver((new Biome.BiomeProperties("FrozenRiver")).setBaseHeight(-0.5F).setHeightVariation(0.0F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
      registerBiome(12, "ice_flats", new BiomeSnow(false, (new Biome.BiomeProperties("Ice Plains")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
      registerBiome(13, "ice_mountains", new BiomeSnow(false, (new Biome.BiomeProperties("Ice Mountains")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
      registerBiome(14, "mushroom_island", new BiomeMushroomIsland((new Biome.BiomeProperties("MushroomIsland")).setBaseHeight(0.2F).setHeightVariation(0.3F).setTemperature(0.9F).setRainfall(1.0F)));
      registerBiome(15, "mushroom_island_shore", new BiomeMushroomIsland((new Biome.BiomeProperties("MushroomIslandShore")).setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.9F).setRainfall(1.0F)));
      registerBiome(16, "beaches", new BiomeBeach((new Biome.BiomeProperties("Beach")).setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.8F).setRainfall(0.4F)));
      registerBiome(17, "desert_hills", new BiomeDesert((new Biome.BiomeProperties("DesertHills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(18, "forest_hills", new BiomeForest(BiomeForest.Type.NORMAL, (new Biome.BiomeProperties("ForestHills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.7F).setRainfall(0.8F)));
      registerBiome(19, "taiga_hills", new BiomeTaiga(BiomeTaiga.Type.NORMAL, (new Biome.BiomeProperties("TaigaHills")).setTemperature(0.25F).setRainfall(0.8F).setBaseHeight(0.45F).setHeightVariation(0.3F)));
      registerBiome(20, "smaller_extreme_hills", new BiomeHills(BiomeHills.Type.EXTRA_TREES, (new Biome.BiomeProperties("Extreme Hills Edge")).setBaseHeight(0.8F).setHeightVariation(0.3F).setTemperature(0.2F).setRainfall(0.3F)));
      registerBiome(21, "jungle", new BiomeJungle(false, (new Biome.BiomeProperties("Jungle")).setTemperature(0.95F).setRainfall(0.9F)));
      registerBiome(22, "jungle_hills", new BiomeJungle(false, (new Biome.BiomeProperties("JungleHills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.95F).setRainfall(0.9F)));
      registerBiome(23, "jungle_edge", new BiomeJungle(true, (new Biome.BiomeProperties("JungleEdge")).setTemperature(0.95F).setRainfall(0.8F)));
      registerBiome(24, "deep_ocean", new BiomeOcean((new Biome.BiomeProperties("Deep Ocean")).setBaseHeight(-1.8F).setHeightVariation(0.1F)));
      registerBiome(25, "stone_beach", new BiomeStoneBeach((new Biome.BiomeProperties("Stone Beach")).setBaseHeight(0.1F).setHeightVariation(0.8F).setTemperature(0.2F).setRainfall(0.3F)));
      registerBiome(26, "cold_beach", new BiomeBeach((new Biome.BiomeProperties("Cold Beach")).setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.05F).setRainfall(0.3F).setSnowEnabled()));
      registerBiome(27, "birch_forest", new BiomeForest(BiomeForest.Type.BIRCH, (new Biome.BiomeProperties("Birch Forest")).setTemperature(0.6F).setRainfall(0.6F)));
      registerBiome(28, "birch_forest_hills", new BiomeForest(BiomeForest.Type.BIRCH, (new Biome.BiomeProperties("Birch Forest Hills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.6F).setRainfall(0.6F)));
      registerBiome(29, "roofed_forest", new BiomeForest(BiomeForest.Type.ROOFED, (new Biome.BiomeProperties("Roofed Forest")).setTemperature(0.7F).setRainfall(0.8F)));
      registerBiome(30, "taiga_cold", new BiomeTaiga(BiomeTaiga.Type.NORMAL, (new Biome.BiomeProperties("Cold Taiga")).setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled()));
      registerBiome(31, "taiga_cold_hills", new BiomeTaiga(BiomeTaiga.Type.NORMAL, (new Biome.BiomeProperties("Cold Taiga Hills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled()));
      registerBiome(32, "redwood_taiga", new BiomeTaiga(BiomeTaiga.Type.MEGA, (new Biome.BiomeProperties("Mega Taiga")).setTemperature(0.3F).setRainfall(0.8F).setBaseHeight(0.2F).setHeightVariation(0.2F)));
      registerBiome(33, "redwood_taiga_hills", new BiomeTaiga(BiomeTaiga.Type.MEGA, (new Biome.BiomeProperties("Mega Taiga Hills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.3F).setRainfall(0.8F)));
      registerBiome(34, "extreme_hills_with_trees", new BiomeHills(BiomeHills.Type.EXTRA_TREES, (new Biome.BiomeProperties("Extreme Hills+")).setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
      registerBiome(35, "savanna", new BiomeSavanna((new Biome.BiomeProperties("Savanna")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(1.2F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(36, "savanna_rock", new BiomeSavanna((new Biome.BiomeProperties("Savanna Plateau")).setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(1.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(37, "mesa", new BiomeMesa(false, false, (new Biome.BiomeProperties("Mesa")).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(38, "mesa_rock", new BiomeMesa(false, true, (new Biome.BiomeProperties("Mesa Plateau F")).setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(39, "mesa_clear_rock", new BiomeMesa(false, false, (new Biome.BiomeProperties("Mesa Plateau")).setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(127, "void", new BiomeVoid((new Biome.BiomeProperties("The Void")).setRainDisabled()));
      registerBiome(129, "mutated_plains", new BiomePlains(true, (new Biome.BiomeProperties("Sunflower Plains")).setBaseBiome("plains").setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.8F).setRainfall(0.4F)));
      registerBiome(130, "mutated_desert", new BiomeDesert((new Biome.BiomeProperties("Desert M")).setBaseBiome("desert").setBaseHeight(0.225F).setHeightVariation(0.25F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(131, "mutated_extreme_hills", new BiomeHills(BiomeHills.Type.MUTATED, (new Biome.BiomeProperties("Extreme Hills M")).setBaseBiome("extreme_hills").setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
      registerBiome(132, "mutated_forest", new BiomeForest(BiomeForest.Type.FLOWER, (new Biome.BiomeProperties("Flower Forest")).setBaseBiome("forest").setHeightVariation(0.4F).setTemperature(0.7F).setRainfall(0.8F)));
      registerBiome(133, "mutated_taiga", new BiomeTaiga(BiomeTaiga.Type.NORMAL, (new Biome.BiomeProperties("Taiga M")).setBaseBiome("taiga").setBaseHeight(0.3F).setHeightVariation(0.4F).setTemperature(0.25F).setRainfall(0.8F)));
      registerBiome(134, "mutated_swampland", new BiomeSwamp((new Biome.BiomeProperties("Swampland M")).setBaseBiome("swampland").setBaseHeight(-0.1F).setHeightVariation(0.3F).setTemperature(0.8F).setRainfall(0.9F).setWaterColor(14745518)));
      registerBiome(140, "mutated_ice_flats", new BiomeSnow(true, (new Biome.BiomeProperties("Ice Plains Spikes")).setBaseBiome("ice_flats").setBaseHeight(0.425F).setHeightVariation(0.45000002F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
      registerBiome(149, "mutated_jungle", new BiomeJungle(false, (new Biome.BiomeProperties("Jungle M")).setBaseBiome("jungle").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.95F).setRainfall(0.9F)));
      registerBiome(151, "mutated_jungle_edge", new BiomeJungle(true, (new Biome.BiomeProperties("JungleEdge M")).setBaseBiome("jungle_edge").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.95F).setRainfall(0.8F)));
      registerBiome(155, "mutated_birch_forest", new BiomeForestMutated((new Biome.BiomeProperties("Birch Forest M")).setBaseBiome("birch_forest").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.6F).setRainfall(0.6F)));
      registerBiome(156, "mutated_birch_forest_hills", new BiomeForestMutated((new Biome.BiomeProperties("Birch Forest Hills M")).setBaseBiome("birch_forest").setBaseHeight(0.55F).setHeightVariation(0.5F).setTemperature(0.6F).setRainfall(0.6F)));
      registerBiome(157, "mutated_roofed_forest", new BiomeForest(BiomeForest.Type.ROOFED, (new Biome.BiomeProperties("Roofed Forest M")).setBaseBiome("roofed_forest").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.7F).setRainfall(0.8F)));
      registerBiome(158, "mutated_taiga_cold", new BiomeTaiga(BiomeTaiga.Type.NORMAL, (new Biome.BiomeProperties("Cold Taiga M")).setBaseBiome("taiga_cold").setBaseHeight(0.3F).setHeightVariation(0.4F).setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled()));
      registerBiome(160, "mutated_redwood_taiga", new BiomeTaiga(BiomeTaiga.Type.MEGA_SPRUCE, (new Biome.BiomeProperties("Mega Spruce Taiga")).setBaseBiome("redwood_taiga").setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(0.25F).setRainfall(0.8F)));
      registerBiome(161, "mutated_redwood_taiga_hills", new BiomeTaiga(BiomeTaiga.Type.MEGA_SPRUCE, (new Biome.BiomeProperties("Redwood Taiga Hills M")).setBaseBiome("redwood_taiga_hills").setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(0.25F).setRainfall(0.8F)));
      registerBiome(162, "mutated_extreme_hills_with_trees", new BiomeHills(BiomeHills.Type.MUTATED, (new Biome.BiomeProperties("Extreme Hills+ M")).setBaseBiome("extreme_hills_with_trees").setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
      registerBiome(163, "mutated_savanna", new BiomeSavannaMutated((new Biome.BiomeProperties("Savanna M")).setBaseBiome("savanna").setBaseHeight(0.3625F).setHeightVariation(1.225F).setTemperature(1.1F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(164, "mutated_savanna_rock", new BiomeSavannaMutated((new Biome.BiomeProperties("Savanna Plateau M")).setBaseBiome("savanna_rock").setBaseHeight(1.05F).setHeightVariation(1.2125001F).setTemperature(1.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(165, "mutated_mesa", new BiomeMesa(true, false, (new Biome.BiomeProperties("Mesa (Bryce)")).setBaseBiome("mesa").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(166, "mutated_mesa_rock", new BiomeMesa(false, true, (new Biome.BiomeProperties("Mesa Plateau F M")).setBaseBiome("mesa_rock").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      registerBiome(167, "mutated_mesa_clear_rock", new BiomeMesa(false, false, (new Biome.BiomeProperties("Mesa Plateau M")).setBaseBiome("mesa_clear_rock").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
      Collections.addAll(EXPLORATION_BIOMES_LIST, new Biome[]{Biomes.OCEAN, Biomes.PLAINS, Biomes.DESERT, Biomes.EXTREME_HILLS, Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMPLAND, Biomes.RIVER, Biomes.FROZEN_RIVER, Biomes.ICE_PLAINS, Biomes.ICE_MOUNTAINS, Biomes.MUSHROOM_ISLAND, Biomes.MUSHROOM_ISLAND_SHORE, Biomes.BEACH, Biomes.DESERT_HILLS, Biomes.FOREST_HILLS, Biomes.TAIGA_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.JUNGLE_EDGE, Biomes.DEEP_OCEAN, Biomes.STONE_BEACH, Biomes.COLD_BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.ROOFED_FOREST, Biomes.COLD_TAIGA, Biomes.COLD_TAIGA_HILLS, Biomes.REDWOOD_TAIGA, Biomes.REDWOOD_TAIGA_HILLS, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.MESA, Biomes.MESA_ROCK, Biomes.MESA_CLEAR_ROCK});
   }

   private static void registerBiome(int var0, String var1, Biome var2) {
      REGISTRY.register(var0, new ResourceLocation(var1), var2);
      if (var2.isMutation()) {
         MUTATION_TO_BASE_ID_MAP.put(var2, getIdForBiome((Biome)REGISTRY.getObject(new ResourceLocation(var2.baseBiomeRegName))));
      }

   }

   public static class BiomeProperties {
      private final String biomeName;
      private float baseHeight = 0.1F;
      private float heightVariation = 0.2F;
      private float temperature = 0.5F;
      private float rainfall = 0.5F;
      private int waterColor = 16777215;
      private boolean enableSnow;
      private boolean enableRain = true;
      @Nullable
      private String baseBiomeRegName;

      public BiomeProperties(String var1) {
         this.biomeName = var1;
      }

      protected Biome.BiomeProperties setTemperature(float var1) {
         if (var1 > 0.1F && var1 < 0.2F) {
            throw new IllegalArgumentException("Please avoid temperatures in the range 0.1 - 0.2 because of snow");
         } else {
            this.temperature = var1;
            return this;
         }
      }

      protected Biome.BiomeProperties setRainfall(float var1) {
         this.rainfall = var1;
         return this;
      }

      protected Biome.BiomeProperties setBaseHeight(float var1) {
         this.baseHeight = var1;
         return this;
      }

      protected Biome.BiomeProperties setHeightVariation(float var1) {
         this.heightVariation = var1;
         return this;
      }

      protected Biome.BiomeProperties setRainDisabled() {
         this.enableRain = false;
         return this;
      }

      protected Biome.BiomeProperties setSnowEnabled() {
         this.enableSnow = true;
         return this;
      }

      protected Biome.BiomeProperties setWaterColor(int var1) {
         this.waterColor = var1;
         return this;
      }

      protected Biome.BiomeProperties setBaseBiome(String var1) {
         this.baseBiomeRegName = var1;
         return this;
      }
   }

   public static class SpawnListEntry extends WeightedRandom.Item {
      public Class entityClass;
      public int minGroupCount;
      public int maxGroupCount;

      public SpawnListEntry(Class var1, int var2, int var3, int var4) {
         super(var2);
         this.entityClass = var1;
         this.minGroupCount = var3;
         this.maxGroupCount = var4;
      }

      public String toString() {
         return this.entityClass.getSimpleName() + "*(" + this.minGroupCount + "-" + this.maxGroupCount + "):" + this.itemWeight;
      }
   }

   public static enum TempCategory {
      OCEAN,
      COLD,
      MEDIUM,
      WARM;
   }
}
