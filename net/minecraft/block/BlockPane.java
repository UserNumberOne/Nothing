package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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

public class BlockPane extends Block {
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   protected static final AxisAlignedBB[] AABB_BY_INDEX = new AxisAlignedBB[]{new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};
   private final boolean canDrop;

   protected BlockPane(Material var1, boolean var2) {
      super(materialIn);
      this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)));
      this.canDrop = canDrop;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      state = this.getActualState(state, worldIn, pos);
      addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[0]);
      if (((Boolean)state.getValue(NORTH)).booleanValue()) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.NORTH)]);
      }

      if (((Boolean)state.getValue(SOUTH)).booleanValue()) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.SOUTH)]);
      }

      if (((Boolean)state.getValue(EAST)).booleanValue()) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.EAST)]);
      }

      if (((Boolean)state.getValue(WEST)).booleanValue()) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.WEST)]);
      }

   }

   private static int getBoundingBoxIndex(EnumFacing var0) {
      return 1 << p_185729_0_.getHorizontalIndex();
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      state = this.getActualState(state, source, pos);
      return AABB_BY_INDEX[getBoundingBoxIndex(state)];
   }

   private static int getBoundingBoxIndex(IBlockState var0) {
      int i = 0;
      if (((Boolean)state.getValue(NORTH)).booleanValue()) {
         i |= getBoundingBoxIndex(EnumFacing.NORTH);
      }

      if (((Boolean)state.getValue(EAST)).booleanValue()) {
         i |= getBoundingBoxIndex(EnumFacing.EAST);
      }

      if (((Boolean)state.getValue(SOUTH)).booleanValue()) {
         i |= getBoundingBoxIndex(EnumFacing.SOUTH);
      }

      if (((Boolean)state.getValue(WEST)).booleanValue()) {
         i |= getBoundingBoxIndex(EnumFacing.WEST);
      }

      return i;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.withProperty(NORTH, Boolean.valueOf(this.canPaneConnectTo(worldIn, pos, EnumFacing.NORTH))).withProperty(SOUTH, Boolean.valueOf(this.canPaneConnectTo(worldIn, pos, EnumFacing.SOUTH))).withProperty(WEST, Boolean.valueOf(this.canPaneConnectTo(worldIn, pos, EnumFacing.WEST))).withProperty(EAST, Boolean.valueOf(this.canPaneConnectTo(worldIn, pos, EnumFacing.EAST)));
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return !this.canDrop ? null : super.getItemDropped(state, rand, fortune);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public final boolean canPaneConnectToBlock(Block var1) {
      return blockIn.getDefaultState().isFullCube() || blockIn == this || blockIn == Blocks.GLASS || blockIn == Blocks.STAINED_GLASS || blockIn == Blocks.STAINED_GLASS_PANE || blockIn instanceof BlockPane;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return blockAccess.getBlockState(pos.offset(side)).getBlock() == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
   }

   protected boolean canSilkHarvest() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT_MIPPED;
   }

   public int getMetaFromState(IBlockState var1) {
      return 0;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(rot) {
      case CLOCKWISE_180:
         return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(EAST, state.getValue(WEST)).withProperty(SOUTH, state.getValue(NORTH)).withProperty(WEST, state.getValue(EAST));
      case COUNTERCLOCKWISE_90:
         return state.withProperty(NORTH, state.getValue(EAST)).withProperty(EAST, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(WEST)).withProperty(WEST, state.getValue(NORTH));
      case CLOCKWISE_90:
         return state.withProperty(NORTH, state.getValue(WEST)).withProperty(EAST, state.getValue(NORTH)).withProperty(SOUTH, state.getValue(EAST)).withProperty(WEST, state.getValue(SOUTH));
      default:
         return state;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      switch(mirrorIn) {
      case LEFT_RIGHT:
         return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
      case FRONT_BACK:
         return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
      default:
         return super.withMirror(state, mirrorIn);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, WEST, SOUTH});
   }

   public boolean canPaneConnectTo(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      BlockPos off = pos.offset(dir);
      IBlockState state = world.getBlockState(off);
      return this.canPaneConnectToBlock(state.getBlock()) || state.isSideSolid(world, off, dir.getOpposite());
   }
}
