package net.minecraft.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BiomeColorHelper {
   private static final BiomeColorHelper.ColorResolver GRASS_COLOR = new BiomeColorHelper.ColorResolver() {
      public int getColorAtPos(Biome var1, BlockPos var2) {
         return biome.getGrassColorAtPos(blockPosition);
      }
   };
   private static final BiomeColorHelper.ColorResolver FOLIAGE_COLOR = new BiomeColorHelper.ColorResolver() {
      public int getColorAtPos(Biome var1, BlockPos var2) {
         return biome.getFoliageColorAtPos(blockPosition);
      }
   };
   private static final BiomeColorHelper.ColorResolver WATER_COLOR = new BiomeColorHelper.ColorResolver() {
      public int getColorAtPos(Biome var1, BlockPos var2) {
         return biome.getWaterColor();
      }
   };

   private static int getColorAtPos(IBlockAccess var0, BlockPos var1, BiomeColorHelper.ColorResolver var2) {
      int i = 0;
      int j = 0;
      int k = 0;

      for(BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
         int l = colorResolver.getColorAtPos(blockAccess.getBiome(blockpos$mutableblockpos), blockpos$mutableblockpos);
         i += (l & 16711680) >> 16;
         j += (l & '\uff00') >> 8;
         k += l & 255;
      }

      return (i / 9 & 255) << 16 | (j / 9 & 255) << 8 | k / 9 & 255;
   }

   public static int getGrassColorAtPos(IBlockAccess var0, BlockPos var1) {
      return getColorAtPos(blockAccess, pos, GRASS_COLOR);
   }

   public static int getFoliageColorAtPos(IBlockAccess var0, BlockPos var1) {
      return getColorAtPos(blockAccess, pos, FOLIAGE_COLOR);
   }

   public static int getWaterColorAtPos(IBlockAccess var0, BlockPos var1) {
      return getColorAtPos(blockAccess, pos, WATER_COLOR);
   }

   @SideOnly(Side.CLIENT)
   interface ColorResolver {
      int getColorAtPos(Biome var1, BlockPos var2);
   }
}
