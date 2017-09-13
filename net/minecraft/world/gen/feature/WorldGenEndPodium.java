package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenEndPodium extends WorldGenerator {
   public static final BlockPos END_PODIUM_LOCATION = BlockPos.ORIGIN;
   public static final BlockPos END_PODIUM_CHUNK_POS = new BlockPos(END_PODIUM_LOCATION.getX() - 4 & -16, 0, END_PODIUM_LOCATION.getZ() - 4 & -16);
   private final boolean activePortal;

   public WorldGenEndPodium(boolean var1) {
      this.activePortal = var1;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(BlockPos.MutableBlockPos var5 : BlockPos.getAllInBoxMutable(new BlockPos(var3.getX() - 4, var3.getY() - 1, var3.getZ() - 4), new BlockPos(var3.getX() + 4, var3.getY() + 32, var3.getZ() + 4))) {
         double var6 = var5.getDistance(var3.getX(), var5.getY(), var3.getZ());
         if (var6 <= 3.5D) {
            if (var5.getY() < var3.getY()) {
               if (var6 <= 2.5D) {
                  this.setBlockAndNotifyAdequately(var1, var5, Blocks.BEDROCK.getDefaultState());
               } else if (var5.getY() < var3.getY()) {
                  this.setBlockAndNotifyAdequately(var1, var5, Blocks.END_STONE.getDefaultState());
               }
            } else if (var5.getY() > var3.getY()) {
               this.setBlockAndNotifyAdequately(var1, var5, Blocks.AIR.getDefaultState());
            } else if (var6 > 2.5D) {
               this.setBlockAndNotifyAdequately(var1, var5, Blocks.BEDROCK.getDefaultState());
            } else if (this.activePortal) {
               this.setBlockAndNotifyAdequately(var1, new BlockPos(var5), Blocks.END_PORTAL.getDefaultState());
            } else {
               this.setBlockAndNotifyAdequately(var1, new BlockPos(var5), Blocks.AIR.getDefaultState());
            }
         }
      }

      for(int var9 = 0; var9 < 4; ++var9) {
         this.setBlockAndNotifyAdequately(var1, var3.up(var9), Blocks.BEDROCK.getDefaultState());
      }

      BlockPos var10 = var3.up(2);

      for(EnumFacing var8 : EnumFacing.Plane.HORIZONTAL) {
         this.setBlockAndNotifyAdequately(var1, var10.offset(var8), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, var8));
      }

      return true;
   }
}
