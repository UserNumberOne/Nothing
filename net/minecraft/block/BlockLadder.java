package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLadder extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   protected static final AxisAlignedBB LADDER_EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
   protected static final AxisAlignedBB LADDER_WEST_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB LADDER_SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
   protected static final AxisAlignedBB LADDER_NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);

   protected BlockLadder() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch((EnumFacing)var1.getValue(FACING)) {
      case NORTH:
         return LADDER_NORTH_AABB;
      case SOUTH:
         return LADDER_SOUTH_AABB;
      case WEST:
         return LADDER_WEST_AABB;
      case EAST:
      default:
         return LADDER_EAST_AABB;
      }
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2.west()).isSideSolid(var1, var2.west(), EnumFacing.EAST) || var1.getBlockState(var2.east()).isSideSolid(var1, var2.east(), EnumFacing.WEST) || var1.getBlockState(var2.north()).isSideSolid(var1, var2.north(), EnumFacing.SOUTH) || var1.getBlockState(var2.south()).isSideSolid(var1, var2.south(), EnumFacing.NORTH);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      if (var3.getAxis().isHorizontal() && this.canBlockStay(var1, var2, var3)) {
         return this.getDefaultState().withProperty(FACING, var3);
      } else {
         for(EnumFacing var10 : EnumFacing.Plane.HORIZONTAL) {
            if (this.canBlockStay(var1, var2, var10)) {
               return this.getDefaultState().withProperty(FACING, var10);
            }
         }

         return this.getDefaultState();
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      EnumFacing var5 = (EnumFacing)var1.getValue(FACING);
      if (!this.canBlockStay(var2, var3, var5)) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

      super.neighborChanged(var1, var2, var3, var4);
   }

   protected boolean canBlockStay(World var1, BlockPos var2, EnumFacing var3) {
      return var1.getBlockState(var2.offset(var3.getOpposite())).isSideSolid(var1, var2.offset(var3.getOpposite()), var3);
   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing var2 = EnumFacing.getFront(var1);
      if (var2.getAxis() == EnumFacing.Axis.Y) {
         var2 = EnumFacing.NORTH;
      }

      return this.getDefaultState().withProperty(FACING, var2);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumFacing)var1.getValue(FACING)).getIndex();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   public boolean isLadder(IBlockState var1, IBlockAccess var2, BlockPos var3, EntityLivingBase var4) {
      return true;
   }
}
