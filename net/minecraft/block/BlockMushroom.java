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
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockMushroom extends BlockBush implements IGrowable {
   protected static final AxisAlignedBB MUSHROOM_AABB = new AxisAlignedBB(0.30000001192092896D, 0.0D, 0.30000001192092896D, 0.699999988079071D, 0.4000000059604645D, 0.699999988079071D);

   protected BlockMushroom() {
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return MUSHROOM_AABB;
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      int sourceX = blockposition.getX();
      int sourceY = blockposition.getY();
      int sourceZ = blockposition.getZ();
      if (random.nextInt(25) == 0) {
         int i = 5;

         for(BlockPos blockposition1 : BlockPos.getAllInBoxMutable(blockposition.add(-4, -1, -4), blockposition.add(4, 1, 4))) {
            if (world.getBlockState(blockposition1).getBlock() == this) {
               --i;
               if (i <= 0) {
                  return;
               }
            }
         }

         BlockPos blockposition2 = blockposition.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

         for(int j = 0; j < 4; ++j) {
            if (world.isAirBlock(blockposition2) && this.canBlockStay(world, blockposition2, this.getDefaultState())) {
               blockposition = blockposition2;
            }

            blockposition2 = blockposition.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
         }

         if (world.isAirBlock(blockposition2) && this.canBlockStay(world, blockposition2, this.getDefaultState())) {
            org.bukkit.World bworld = world.getWorld();
            BlockState blockState = bworld.getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ()).getState();
            blockState.setType(CraftMagicNumbers.getMaterial(this));
            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(sourceX, sourceY, sourceZ), blockState);
            world.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               blockState.update(true);
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return super.canPlaceBlockAt(world, blockposition) && this.canBlockStay(world, blockposition, this.getDefaultState());
   }

   protected boolean canSustainBush(IBlockState iblockdata) {
      return iblockdata.isFullBlock();
   }

   public boolean canBlockStay(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (blockposition.getY() >= 0 && blockposition.getY() < 256) {
         IBlockState iblockdata1 = world.getBlockState(blockposition.down());
         return iblockdata1.getBlock() == Blocks.MYCELIUM ? true : (iblockdata1.getBlock() == Blocks.DIRT && iblockdata1.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL ? true : world.getLight(blockposition) < 13 && this.canSustainBush(iblockdata1));
      } else {
         return false;
      }
   }

   public boolean generateBigMushroom(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      world.setBlockToAir(blockposition);
      WorldGenBigMushroom worldgenhugemushroom = null;
      if (this == Blocks.BROWN_MUSHROOM) {
         BlockSapling.treeType = TreeType.BROWN_MUSHROOM;
         worldgenhugemushroom = new WorldGenBigMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
      } else if (this == Blocks.RED_MUSHROOM) {
         BlockSapling.treeType = TreeType.RED_MUSHROOM;
         worldgenhugemushroom = new WorldGenBigMushroom(Blocks.RED_MUSHROOM_BLOCK);
      }

      if (worldgenhugemushroom != null && worldgenhugemushroom.generate(world, random, blockposition)) {
         return true;
      } else {
         world.setBlockState(blockposition, iblockdata, 3);
         return false;
      }
   }

   public boolean canGrow(World world, BlockPos blockposition, IBlockState iblockdata, boolean flag) {
      return true;
   }

   public boolean canUseBonemeal(World world, Random random, BlockPos blockposition, IBlockState iblockdata) {
      return (double)random.nextFloat() < 0.4D;
   }

   public void grow(World world, Random random, BlockPos blockposition, IBlockState iblockdata) {
      this.generateBigMushroom(world, blockposition, iblockdata, random);
   }
}
