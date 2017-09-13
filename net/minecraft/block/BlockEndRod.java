package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockEndRod extends BlockDirectional {
   protected static final AxisAlignedBB END_ROD_VERTICAL_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);
   protected static final AxisAlignedBB END_ROD_NS_AABB = new AxisAlignedBB(0.375D, 0.375D, 0.0D, 0.625D, 0.625D, 1.0D);
   protected static final AxisAlignedBB END_ROD_EW_AABB = new AxisAlignedBB(0.0D, 0.375D, 0.375D, 1.0D, 0.625D, 0.625D);

   protected BlockEndRod() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withProperty(FACING, mirrorIn.mirror((EnumFacing)state.getValue(FACING)));
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch(((EnumFacing)state.getValue(FACING)).getAxis()) {
      case X:
      default:
         return END_ROD_EW_AABB;
      case Z:
         return END_ROD_NS_AABB;
      case Y:
         return END_ROD_VERTICAL_AABB;
      }
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return true;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState iblockstate = worldIn.getBlockState(pos.offset(facing.getOpposite()));
      if (iblockstate.getBlock() == Blocks.END_ROD) {
         EnumFacing enumfacing = (EnumFacing)iblockstate.getValue(FACING);
         if (enumfacing == facing) {
            return this.getDefaultState().withProperty(FACING, facing.getOpposite());
         }
      }

      return this.getDefaultState().withProperty(FACING, facing);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      EnumFacing enumfacing = (EnumFacing)stateIn.getValue(FACING);
      double d0 = (double)pos.getX() + 0.55D - (double)(rand.nextFloat() * 0.1F);
      double d1 = (double)pos.getY() + 0.55D - (double)(rand.nextFloat() * 0.1F);
      double d2 = (double)pos.getZ() + 0.55D - (double)(rand.nextFloat() * 0.1F);
      double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);
      if (rand.nextInt(5) == 0) {
         worldIn.spawnParticle(EnumParticleTypes.END_ROD, d0 + (double)enumfacing.getFrontOffsetX() * d3, d1 + (double)enumfacing.getFrontOffsetY() * d3, d2 + (double)enumfacing.getFrontOffsetZ() * d3, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D);
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState iblockstate = this.getDefaultState();
      iblockstate = iblockstate.withProperty(FACING, EnumFacing.getFront(meta));
      return iblockstate;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumFacing)state.getValue(FACING)).getIndex();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.NORMAL;
   }
}
