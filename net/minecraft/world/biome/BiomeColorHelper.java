package net.minecraft.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BiomeColorHelper {
   private static final BiomeColorHelper.ColorResolver GRASS_COLOR = new BiomeColorHelper.ColorResolver() {
      public int getColorAtPos(Biome var1, BlockPos var2) {
         return var1.getGrassColorAtPos(var2);
      }
   };
   private static final BiomeColorHelper.ColorResolver FOLIAGE_COLOR = new BiomeColorHelper.ColorResolver() {
      public int getColorAtPos(Biome var1, BlockPos var2) {
         return var1.getFoliageColorAtPos(var2);
      }
   };
   private static final BiomeColorHelper.ColorResolver WATER_COLOR = new BiomeColorHelper.ColorResolver() {
      public int getColorAtPos(Biome var1, BlockPos var2) {
         return var1.getWaterColor();
      }
   };

   private static int getColorAtPos(IBlockAccess var0, BlockPos var1, BiomeColorHelper.ColorResolver var2) {
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;

      for(BlockPos.MutableBlockPos var7 : BlockPos.getAllInBoxMutable(var1.add(-1, 0, -1), var1.add(1, 0, 1))) {
         int var8 = var2.getColorAtPos(var0.getBiome(var7), var7);
         var3 += (var8 & 16711680) >> 16;
         var4 += (var8 & '\uff00') >> 8;
         var5 += var8 & 255;
      }

      return (var3 / 9 & 255) << 16 | (var4 / 9 & 255) << 8 | var5 / 9 & 255;
   }

   public static int getGrassColorAtPos(IBlockAccess var0, BlockPos var1) {
      return getColorAtPos(var0, var1, GRASS_COLOR);
   }

   public static int getFoliageColorAtPos(IBlockAccess var0, BlockPos var1) {
      return getColorAtPos(var0, var1, FOLIAGE_COLOR);
   }

   public static int getWaterColorAtPos(IBlockAccess var0, BlockPos var1) {
      return getColorAtPos(var0, var1, WATER_COLOR);
   }

   @SideOnly(Side.CLIENT)
   interface ColorResolver {
      int getColorAtPos(Biome var1, BlockPos var2);
   }
}
