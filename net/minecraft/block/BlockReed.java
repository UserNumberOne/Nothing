package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockReed extends Block {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   protected static final AxisAlignedBB REED_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);

   protected BlockReed() {
      super(Material.PLANTS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return REED_AABB;
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if ((world.getBlockState(blockposition.down()).getBlock() == Blocks.REEDS || this.checkForDrop(world, blockposition, iblockdata)) && world.isAirBlock(blockposition.up())) {
         int i;
         for(i = 1; world.getBlockState(blockposition.down(i)).getBlock() == this; ++i) {
            ;
         }

         if (i < 3) {
            int j = ((Integer)iblockdata.getValue(AGE)).intValue();
            if (j == 15) {
               BlockPos upPos = blockposition.up();
               CraftEventFactory.handleBlockGrowEvent(world, upPos.getX(), upPos.getY(), upPos.getZ(), this, 0);
               world.setBlockState(blockposition, iblockdata.withProperty(AGE, Integer.valueOf(0)), 4);
            } else {
               world.setBlockState(blockposition, iblockdata.withProperty(AGE, Integer.valueOf(j + 1)), 4);
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      Block block = world.getBlockState(blockposition.down()).getBlock();
      if (block == this) {
         return true;
      } else if (block != Blocks.GRASS && block != Blocks.DIRT && block != Blocks.SAND) {
         return false;
      } else {
         BlockPos blockposition1 = blockposition.down();

         for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
            IBlockState iblockdata = world.getBlockState(blockposition1.offset(enumdirection));
            if (iblockdata.getMaterial() == Material.WATER || iblockdata.getBlock() == Blocks.FROSTED_ICE) {
               return true;
            }
         }

         return false;
      }
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      this.checkForDrop(world, blockposition, iblockdata);
   }

   protected final boolean checkForDrop(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (this.canBlockStay(world, blockposition)) {
         return true;
      } else {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
         return false;
      }
   }

   public boolean canBlockStay(World world, BlockPos blockposition) {
      return this.canPlaceBlockAt(world, blockposition);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Items.REEDS;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Items.REEDS);
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
