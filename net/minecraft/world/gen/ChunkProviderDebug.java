package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;

public class ChunkProviderDebug implements IChunkGenerator {
   private static final List ALL_VALID_STATES = Lists.newArrayList();
   private static final int GRID_WIDTH;
   private static final int GRID_HEIGHT;
   protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
   protected static final IBlockState BARRIER = Blocks.BARRIER.getDefaultState();
   private final World world;

   public ChunkProviderDebug(World var1) {
      this.world = var1;
   }

   public Chunk provideChunk(int var1, int var2) {
      ChunkPrimer var3 = new ChunkPrimer();

      for(int var4 = 0; var4 < 16; ++var4) {
         for(int var5 = 0; var5 < 16; ++var5) {
            int var6 = var1 * 16 + var4;
            int var7 = var2 * 16 + var5;
            var3.setBlockState(var4, 60, var5, BARRIER);
            IBlockState var8 = getBlockStateFor(var6, var7);
            if (var8 != null) {
               var3.setBlockState(var4, 70, var5, var8);
            }
         }
      }

      Chunk var9 = new Chunk(this.world, var3, var1, var2);
      var9.generateSkylightMap();
      Biome[] var10 = this.world.getBiomeProvider().getBiomes((Biome[])null, var1 * 16, var2 * 16, 16, 16);
      byte[] var11 = var9.getBiomeArray();

      for(int var12 = 0; var12 < var11.length; ++var12) {
         var11[var12] = (byte)Biome.getIdForBiome(var10[var12]);
      }

      var9.generateSkylightMap();
      return var9;
   }

   public static IBlockState getBlockStateFor(int var0, int var1) {
      IBlockState var2 = AIR;
      if (var0 > 0 && var1 > 0 && var0 % 2 != 0 && var1 % 2 != 0) {
         var0 = var0 / 2;
         var1 = var1 / 2;
         if (var0 <= GRID_WIDTH && var1 <= GRID_HEIGHT) {
            int var3 = MathHelper.abs(var0 * GRID_WIDTH + var1);
            if (var3 < ALL_VALID_STATES.size()) {
               var2 = (IBlockState)ALL_VALID_STATES.get(var3);
            }
         }
      }

      return var2;
   }

   public void populate(int var1, int var2) {
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
      return null;
   }

   public void recreateStructures(Chunk var1, int var2, int var3) {
   }

   static {
      for(Block var1 : Block.REGISTRY) {
         ALL_VALID_STATES.addAll(var1.getBlockState().getValidStates());
      }

      GRID_WIDTH = MathHelper.ceil(MathHelper.sqrt((float)ALL_VALID_STATES.size()));
      GRID_HEIGHT = MathHelper.ceil((float)ALL_VALID_STATES.size() / (float)GRID_WIDTH);
   }
}
