package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraftforge.event.ForgeEventFactory;

public class ChunkProviderFlat implements IChunkGenerator {
   private final World world;
   private final Random random;
   private final IBlockState[] cachedBlockIDs = new IBlockState[256];
   private final FlatGeneratorInfo flatWorldGenInfo;
   private final List structureGenerators = Lists.newArrayList();
   private final boolean hasDecoration;
   private final boolean hasDungeons;
   private WorldGenLakes waterLakeGenerator;
   private WorldGenLakes lavaLakeGenerator;

   public ChunkProviderFlat(World var1, long var2, boolean var4, String var5) {
      this.world = var1;
      this.random = new Random(var2);
      this.flatWorldGenInfo = FlatGeneratorInfo.createFlatGeneratorFromString(var5);
      if (var4) {
         Map var6 = this.flatWorldGenInfo.getWorldFeatures();
         if (var6.containsKey("village")) {
            Map var7 = (Map)var6.get("village");
            if (!var7.containsKey("size")) {
               var7.put("size", "1");
            }

            this.structureGenerators.add(new MapGenVillage(var7));
         }

         if (var6.containsKey("biome_1")) {
            this.structureGenerators.add(new MapGenScatteredFeature((Map)var6.get("biome_1")));
         }

         if (var6.containsKey("mineshaft")) {
            this.structureGenerators.add(new MapGenMineshaft((Map)var6.get("mineshaft")));
         }

         if (var6.containsKey("stronghold")) {
            this.structureGenerators.add(new MapGenStronghold((Map)var6.get("stronghold")));
         }

         if (var6.containsKey("oceanmonument")) {
            this.structureGenerators.add(new StructureOceanMonument((Map)var6.get("oceanmonument")));
         }
      }

      if (this.flatWorldGenInfo.getWorldFeatures().containsKey("lake")) {
         this.waterLakeGenerator = new WorldGenLakes(Blocks.WATER);
      }

      if (this.flatWorldGenInfo.getWorldFeatures().containsKey("lava_lake")) {
         this.lavaLakeGenerator = new WorldGenLakes(Blocks.LAVA);
      }

      this.hasDungeons = this.flatWorldGenInfo.getWorldFeatures().containsKey("dungeon");
      int var13 = 0;
      int var14 = 0;
      boolean var8 = true;

      for(FlatLayerInfo var10 : this.flatWorldGenInfo.getFlatLayers()) {
         for(int var11 = var10.getMinY(); var11 < var10.getMinY() + var10.getLayerCount(); ++var11) {
            IBlockState var12 = var10.getLayerMaterial();
            if (var12.getBlock() != Blocks.AIR) {
               var8 = false;
               this.cachedBlockIDs[var11] = var12;
            }
         }

         if (var10.getLayerMaterial().getBlock() == Blocks.AIR) {
            var14 += var10.getLayerCount();
         } else {
            var13 += var10.getLayerCount() + var14;
            var14 = 0;
         }
      }

      var1.setSeaLevel(var13);
      this.hasDecoration = var8 && this.flatWorldGenInfo.getBiome() != Biome.getIdForBiome(Biomes.VOID) ? false : this.flatWorldGenInfo.getWorldFeatures().containsKey("decoration");
   }

   public Chunk provideChunk(int var1, int var2) {
      ChunkPrimer var3 = new ChunkPrimer();

      for(int var4 = 0; var4 < this.cachedBlockIDs.length; ++var4) {
         IBlockState var5 = this.cachedBlockIDs[var4];
         if (var5 != null) {
            for(int var6 = 0; var6 < 16; ++var6) {
               for(int var7 = 0; var7 < 16; ++var7) {
                  var3.setBlockState(var6, var4, var7, var5);
               }
            }
         }
      }

      for(MapGenBase var10 : this.structureGenerators) {
         var10.generate(this.world, var1, var2, var3);
      }

      Chunk var9 = new Chunk(this.world, var3, var1, var2);
      Biome[] var11 = this.world.getBiomeProvider().getBiomes((Biome[])null, var1 * 16, var2 * 16, 16, 16);
      byte[] var12 = var9.getBiomeArray();

      for(int var13 = 0; var13 < var12.length; ++var13) {
         var12[var13] = (byte)Biome.getIdForBiome(var11[var13]);
      }

      var9.generateSkylightMap();
      return var9;
   }

   public void populate(int var1, int var2) {
      BlockFalling.fallInstantly = true;
      int var3 = var1 * 16;
      int var4 = var2 * 16;
      BlockPos var5 = new BlockPos(var3, 0, var4);
      Biome var6 = this.world.getBiome(new BlockPos(var3 + 16, 0, var4 + 16));
      boolean var7 = false;
      this.random.setSeed(this.world.getSeed());
      long var8 = this.random.nextLong() / 2L * 2L + 1L;
      long var10 = this.random.nextLong() / 2L * 2L + 1L;
      this.random.setSeed((long)var1 * var8 + (long)var2 * var10 ^ this.world.getSeed());
      ChunkPos var12 = new ChunkPos(var1, var2);
      ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, var1, var2, var7);

      for(MapGenStructure var14 : this.structureGenerators) {
         boolean var15 = var14.generateStructure(this.world, this.random, var12);
         if (var14 instanceof MapGenVillage) {
            var7 |= var15;
         }
      }

      if (this.waterLakeGenerator != null && !var7 && this.random.nextInt(4) == 0) {
         this.waterLakeGenerator.generate(this.world, this.random, var5.add(this.random.nextInt(16) + 8, this.random.nextInt(256), this.random.nextInt(16) + 8));
      }

      if (this.lavaLakeGenerator != null && !var7 && this.random.nextInt(8) == 0) {
         BlockPos var16 = var5.add(this.random.nextInt(16) + 8, this.random.nextInt(this.random.nextInt(248) + 8), this.random.nextInt(16) + 8);
         if (var16.getY() < this.world.getSeaLevel() || this.random.nextInt(10) == 0) {
            this.lavaLakeGenerator.generate(this.world, this.random, var16);
         }
      }

      if (this.hasDungeons) {
         for(int var17 = 0; var17 < 8; ++var17) {
            (new WorldGenDungeons()).generate(this.world, this.random, var5.add(this.random.nextInt(16) + 8, this.random.nextInt(256), this.random.nextInt(16) + 8));
         }
      }

      if (this.hasDecoration) {
         var6.decorate(this.world, this.random, var5);
      }

      ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, var1, var2, var7);
      BlockFalling.fallInstantly = false;
   }

   public boolean generateStructures(Chunk var1, int var2, int var3) {
      return false;
   }

   public List getPossibleCreatures(EnumCreatureType var1, BlockPos var2) {
      Biome var3 = this.world.getBiome(var2);
      return var3.getSpawnableList(var1);
   }

   @Nullable
   public BlockPos getStrongholdGen(World var1, String var2, BlockPos var3) {
      if ("Stronghold".equals(var2)) {
         for(MapGenStructure var5 : this.structureGenerators) {
            if (var5 instanceof MapGenStronghold) {
               return var5.getClosestStrongholdPos(var1, var3);
            }
         }
      }

      return null;
   }

   public void recreateStructures(Chunk var1, int var2, int var3) {
      for(MapGenStructure var5 : this.structureGenerators) {
         var5.generate(this.world, var2, var3, (ChunkPrimer)null);
      }

   }
}
