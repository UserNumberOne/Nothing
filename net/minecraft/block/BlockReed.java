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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockReed extends Block implements IPlantable {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   protected static final AxisAlignedBB REED_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);

   protected BlockReed() {
      super(Material.PLANTS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return REED_AABB;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if ((worldIn.getBlockState(pos.down()).getBlock() == Blocks.REEDS || this.checkForDrop(worldIn, pos, state)) && worldIn.isAirBlock(pos.up())) {
         int i;
         for(i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; ++i) {
            ;
         }

         if (i < 3) {
            int j = ((Integer)state.getValue(AGE)).intValue();
            if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, true)) {
               if (j == 15) {
                  worldIn.setBlockState(pos.up(), this.getDefaultState());
                  worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(0)), 4);
               } else {
                  worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(j + 1)), 4);
               }

               ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      IBlockState state = worldIn.getBlockState(pos.down());
      Block block = state.getBlock();
      if (block.canSustainPlant(state, worldIn, pos.down(), EnumFacing.UP, this)) {
         return true;
      } else if (block == this) {
         return true;
      } else if (block != Blocks.GRASS && block != Blocks.DIRT && block != Blocks.SAND) {
         return false;
      } else {
         BlockPos blockpos = pos.down();

         for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            IBlockState iblockstate = worldIn.getBlockState(blockpos.offset(enumfacing));
            if (iblockstate.getMaterial() == Material.WATER || iblockstate.getBlock() == Blocks.FROSTED_ICE) {
               return true;
            }
         }

         return false;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      this.checkForDrop(worldIn, pos, state);
   }

   protected final boolean checkForDrop(World var1, BlockPos var2, IBlockState var3) {
      if (this.canBlockStay(worldIn, pos)) {
         return true;
      } else {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);
         return false;
      }
   }

   public boolean canBlockStay(World var1, BlockPos var2) {
      return this.canPlaceBlockAt(worldIn, pos);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.REEDS;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.REEDS);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(AGE)).intValue();
   }

   public EnumPlantType getPlantType(IBlockAccess var1, BlockPos var2) {
      return EnumPlantType.Beach;
   }

   public IBlockState getPlant(IBlockAccess var1, BlockPos var2) {
      return this.getDefaultState();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
