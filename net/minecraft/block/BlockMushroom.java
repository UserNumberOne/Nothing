package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockMushroom extends BlockBush implements IGrowable {
   protected static final AxisAlignedBB MUSHROOM_AABB = new AxisAlignedBB(0.30000001192092896D, 0.0D, 0.30000001192092896D, 0.699999988079071D, 0.4000000059604645D, 0.699999988079071D);

   protected BlockMushroom() {
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return MUSHROOM_AABB;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      int var5 = var2.getX();
      int var6 = var2.getY();
      int var7 = var2.getZ();
      if (var4.nextInt(25) == 0) {
         int var8 = 5;

         for(BlockPos var10 : BlockPos.getAllInBoxMutable(var2.add(-4, -1, -4), var2.add(4, 1, 4))) {
            if (var1.getBlockState(var10).getBlock() == this) {
               --var8;
               if (var8 <= 0) {
                  return;
               }
            }
         }

         BlockPos var14 = var2.add(var4.nextInt(3) - 1, var4.nextInt(2) - var4.nextInt(2), var4.nextInt(3) - 1);

         for(int var11 = 0; var11 < 4; ++var11) {
            if (var1.isAirBlock(var14) && this.canBlockStay(var1, var14, this.getDefaultState())) {
               var2 = var14;
            }

            var14 = var2.add(var4.nextInt(3) - 1, var4.nextInt(2) - var4.nextInt(2), var4.nextInt(3) - 1);
         }

         if (var1.isAirBlock(var14) && this.canBlockStay(var1, var14, this.getDefaultState())) {
            CraftWorld var15 = var1.getWorld();
            BlockState var12 = var15.getBlockAt(var14.getX(), var14.getY(), var14.getZ()).getState();
            var12.setType(CraftMagicNumbers.getMaterial(this));
            BlockSpreadEvent var13 = new BlockSpreadEvent(var12.getBlock(), var15.getBlockAt(var5, var6, var7), var12);
            var1.getServer().getPluginManager().callEvent(var13);
            if (!var13.isCancelled()) {
               var12.update(true);
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) && this.canBlockStay(var1, var2, this.getDefaultState());
   }

   protected boolean canSustainBush(IBlockState var1) {
      return var1.isFullBlock();
   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      if (var2.getY() >= 0 && var2.getY() < 256) {
         IBlockState var4 = var1.getBlockState(var2.down());
         return var4.getBlock() == Blocks.MYCELIUM ? true : (var4.getBlock() == Blocks.DIRT && var4.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL ? true : var1.getLight(var2) < 13 && this.canSustainBush(var4));
      } else {
         return false;
      }
   }

   public boolean generateBigMushroom(World var1, BlockPos var2, IBlockState var3, Random var4) {
      var1.setBlockToAir(var2);
      WorldGenBigMushroom var5 = null;
      if (this == Blocks.BROWN_MUSHROOM) {
         BlockSapling.treeType = TreeType.BROWN_MUSHROOM;
         var5 = new WorldGenBigMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
      } else if (this == Blocks.RED_MUSHROOM) {
         BlockSapling.treeType = TreeType.RED_MUSHROOM;
         var5 = new WorldGenBigMushroom(Blocks.RED_MUSHROOM_BLOCK);
      }

      if (var5 != null && var5.generate(var1, var4, var2)) {
         return true;
      } else {
         var1.setBlockState(var2, var3, 3);
         return false;
      }
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return true;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return (double)var2.nextFloat() < 0.4D;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.generateBigMushroom(var1, var3, var4, var2);
   }
}
