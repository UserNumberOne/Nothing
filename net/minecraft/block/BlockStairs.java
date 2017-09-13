package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStairs extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyEnum HALF = PropertyEnum.create("half", BlockStairs.EnumHalf.class);
   public static final PropertyEnum SHAPE = PropertyEnum.create("shape", BlockStairs.EnumShape.class);
   protected static final AxisAlignedBB AABB_SLAB_TOP = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_TOP_WEST = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_TOP_EAST = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_TOP_NORTH = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
   protected static final AxisAlignedBB AABB_QTR_TOP_SOUTH = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_TOP_NW = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_TOP_NE = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_TOP_SW = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 0.5D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_TOP_SE = new AxisAlignedBB(0.5D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_SLAB_BOTTOM = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_BOT_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_BOT_EAST = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_BOT_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
   protected static final AxisAlignedBB AABB_QTR_BOT_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_BOT_NW = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_BOT_NE = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_BOT_SW = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 0.5D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_BOT_SE = new AxisAlignedBB(0.5D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
   private final Block modelBlock;
   private final IBlockState modelState;

   protected BlockStairs(IBlockState var1) {
      super(modelState.getBlock().blockMaterial);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT));
      this.modelBlock = modelState.getBlock();
      this.modelState = modelState;
      this.setHardness(this.modelBlock.blockHardness);
      this.setResistance(this.modelBlock.blockResistance / 3.0F);
      this.setSoundType(this.modelBlock.blockSoundType);
      this.setLightOpacity(255);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      state = this.getActualState(state, worldIn, pos);

      for(AxisAlignedBB axisalignedbb : getCollisionBoxList(state)) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, axisalignedbb);
      }

   }

   private static List getCollisionBoxList(IBlockState var0) {
      List list = Lists.newArrayList();
      boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      list.add(flag ? AABB_SLAB_TOP : AABB_SLAB_BOTTOM);
      BlockStairs.EnumShape blockstairs$enumshape = (BlockStairs.EnumShape)bstate.getValue(SHAPE);
      if (blockstairs$enumshape == BlockStairs.EnumShape.STRAIGHT || blockstairs$enumshape == BlockStairs.EnumShape.INNER_LEFT || blockstairs$enumshape == BlockStairs.EnumShape.INNER_RIGHT) {
         list.add(getCollQuarterBlock(bstate));
      }

      if (blockstairs$enumshape != BlockStairs.EnumShape.STRAIGHT) {
         list.add(getCollEighthBlock(bstate));
      }

      return list;
   }

   private static AxisAlignedBB getCollQuarterBlock(IBlockState var0) {
      boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      switch((EnumFacing)bstate.getValue(FACING)) {
      case NORTH:
      default:
         return flag ? AABB_QTR_BOT_NORTH : AABB_QTR_TOP_NORTH;
      case SOUTH:
         return flag ? AABB_QTR_BOT_SOUTH : AABB_QTR_TOP_SOUTH;
      case WEST:
         return flag ? AABB_QTR_BOT_WEST : AABB_QTR_TOP_WEST;
      case EAST:
         return flag ? AABB_QTR_BOT_EAST : AABB_QTR_TOP_EAST;
      }
   }

   private static AxisAlignedBB getCollEighthBlock(IBlockState var0) {
      EnumFacing enumfacing = (EnumFacing)bstate.getValue(FACING);
      EnumFacing enumfacing1;
      switch((BlockStairs.EnumShape)bstate.getValue(SHAPE)) {
      case OUTER_LEFT:
      default:
         enumfacing1 = enumfacing;
         break;
      case OUTER_RIGHT:
         enumfacing1 = enumfacing.rotateY();
         break;
      case INNER_RIGHT:
         enumfacing1 = enumfacing.getOpposite();
         break;
      case INNER_LEFT:
         enumfacing1 = enumfacing.rotateYCCW();
      }

      boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      switch(enumfacing1) {
      case NORTH:
      default:
         return flag ? AABB_OCT_BOT_NW : AABB_OCT_TOP_NW;
      case SOUTH:
         return flag ? AABB_OCT_BOT_SE : AABB_OCT_TOP_SE;
      case WEST:
         return flag ? AABB_OCT_BOT_SW : AABB_OCT_TOP_SW;
      case EAST:
         return flag ? AABB_OCT_BOT_NE : AABB_OCT_TOP_NE;
      }
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      this.modelBlock.randomDisplayTick(stateIn, worldIn, pos, rand);
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      this.modelBlock.onBlockClicked(worldIn, pos, playerIn);
   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
      this.modelBlock.onBlockDestroyedByPlayer(worldIn, pos, state);
   }

   @SideOnly(Side.CLIENT)
   public int getPackedLightmapCoords(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return this.modelState.getPackedLightmapCoords(source, pos);
   }

   public float getExplosionResistance(Entity var1) {
      return this.modelBlock.getExplosionResistance(exploder);
   }

   public int tickRate(World var1) {
      return this.modelBlock.tickRate(worldIn);
   }

   public Vec3d modifyAcceleration(World var1, BlockPos var2, Entity var3, Vec3d var4) {
      return this.modelBlock.modifyAcceleration(worldIn, pos, entityIn, motion);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return this.modelBlock.getBlockLayer();
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getSelectedBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return this.modelState.getSelectedBoundingBox(worldIn, pos);
   }

   public boolean isCollidable() {
      return this.modelBlock.isCollidable();
   }

   public boolean canCollideCheck(IBlockState var1, boolean var2) {
      return this.modelBlock.canCollideCheck(state, hitIfLiquid);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return this.modelBlock.canPlaceBlockAt(worldIn, pos);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.modelState.neighborChanged(worldIn, pos, Blocks.AIR);
      this.modelBlock.onBlockAdded(worldIn, pos, this.modelState);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      this.modelBlock.breakBlock(worldIn, pos, this.modelState);
   }

   public void onEntityWalk(World var1, BlockPos var2, Entity var3) {
      this.modelBlock.onEntityWalk(worldIn, pos, entityIn);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.modelBlock.updateTick(worldIn, pos, state, rand);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      return this.modelBlock.onBlockActivated(worldIn, pos, this.modelState, playerIn, hand, heldItem, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
   }

   public void onBlockDestroyedByExplosion(World var1, BlockPos var2, Explosion var3) {
      this.modelBlock.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);
   }

   public boolean isFullyOpaque(IBlockState var1) {
      return state.getValue(HALF) == BlockStairs.EnumHalf.TOP;
   }

   public MapColor getMapColor(IBlockState var1) {
      return this.modelBlock.getMapColor(this.modelState);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
      iblockstate = iblockstate.withProperty(FACING, placer.getHorizontalFacing()).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT);
      return facing == EnumFacing.DOWN || facing != EnumFacing.UP && (double)hitY > 0.5D ? iblockstate.withProperty(HALF, BlockStairs.EnumHalf.TOP) : iblockstate.withProperty(HALF, BlockStairs.EnumHalf.BOTTOM);
   }

   @Nullable
   public RayTraceResult collisionRayTrace(IBlockState var1, World var2, BlockPos var3, Vec3d var4, Vec3d var5) {
      List list = Lists.newArrayList();

      for(AxisAlignedBB axisalignedbb : getCollisionBoxList(this.getActualState(blockState, worldIn, pos))) {
         list.add(this.rayTrace(pos, start, end, axisalignedbb));
      }

      RayTraceResult raytraceresult1 = null;
      double d1 = 0.0D;

      for(RayTraceResult raytraceresult : list) {
         if (raytraceresult != null) {
            double d0 = raytraceresult.hitVec.squareDistanceTo(end);
            if (d0 > d1) {
               raytraceresult1 = raytraceresult;
               d1 = d0;
            }
         }
      }

      return raytraceresult1;
   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState iblockstate = this.getDefaultState().withProperty(HALF, (meta & 4) > 0 ? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
      iblockstate = iblockstate.withProperty(FACING, EnumFacing.getFront(5 - (meta & 3)));
      return iblockstate;
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      if (state.getValue(HALF) == BlockStairs.EnumHalf.TOP) {
         i |= 4;
      }

      i = i | 5 - ((EnumFacing)state.getValue(FACING)).getIndex();
      return i;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.withProperty(SHAPE, getStairsShape(state, worldIn, pos));
   }

   private static BlockStairs.EnumShape getStairsShape(IBlockState var0, IBlockAccess var1, BlockPos var2) {
      EnumFacing enumfacing = (EnumFacing)p_185706_0_.getValue(FACING);
      IBlockState iblockstate = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing));
      if (isBlockStairs(iblockstate) && p_185706_0_.getValue(HALF) == iblockstate.getValue(HALF)) {
         EnumFacing enumfacing1 = (EnumFacing)iblockstate.getValue(FACING);
         if (enumfacing1.getAxis() != ((EnumFacing)p_185706_0_.getValue(FACING)).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing1.getOpposite())) {
            if (enumfacing1 == enumfacing.rotateYCCW()) {
               return BlockStairs.EnumShape.OUTER_LEFT;
            }

            return BlockStairs.EnumShape.OUTER_RIGHT;
         }
      }

      IBlockState iblockstate1 = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing.getOpposite()));
      if (isBlockStairs(iblockstate1) && p_185706_0_.getValue(HALF) == iblockstate1.getValue(HALF)) {
         EnumFacing enumfacing2 = (EnumFacing)iblockstate1.getValue(FACING);
         if (enumfacing2.getAxis() != ((EnumFacing)p_185706_0_.getValue(FACING)).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing2)) {
            if (enumfacing2 == enumfacing.rotateYCCW()) {
               return BlockStairs.EnumShape.INNER_LEFT;
            }

            return BlockStairs.EnumShape.INNER_RIGHT;
         }
      }

      return BlockStairs.EnumShape.STRAIGHT;
   }

   private static boolean isDifferentStairs(IBlockState var0, IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      IBlockState iblockstate = p_185704_1_.getBlockState(p_185704_2_.offset(p_185704_3_));
      return !isBlockStairs(iblockstate) || iblockstate.getValue(FACING) != p_185704_0_.getValue(FACING) || iblockstate.getValue(HALF) != p_185704_0_.getValue(HALF);
   }

   public static boolean isBlockStairs(IBlockState var0) {
      return state.getBlock() instanceof BlockStairs;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      BlockStairs.EnumShape blockstairs$enumshape = (BlockStairs.EnumShape)state.getValue(SHAPE);
      switch(mirrorIn) {
      case LEFT_RIGHT:
         if (enumfacing.getAxis() == EnumFacing.Axis.Z) {
            switch(blockstairs$enumshape) {
            case OUTER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
            case OUTER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
            case INNER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
            case INNER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
            default:
               return state.withRotation(Rotation.CLOCKWISE_180);
            }
         }
         break;
      case FRONT_BACK:
         if (enumfacing.getAxis() == EnumFacing.Axis.X) {
            switch(blockstairs$enumshape) {
            case OUTER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
            case OUTER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
            case INNER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
            case INNER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
            case STRAIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180);
            }
         }
      }

      return super.withMirror(state, mirrorIn);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, HALF, SHAPE});
   }

   public boolean doesSideBlockRendering(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (ForgeModContainer.disableStairSlabCulling) {
         return super.doesSideBlockRendering(state, world, pos, face);
      } else if (state.isOpaqueCube()) {
         return true;
      } else {
         state = this.getActualState(state, world, pos);
         BlockStairs.EnumHalf half = (BlockStairs.EnumHalf)state.getValue(HALF);
         EnumFacing side = (EnumFacing)state.getValue(FACING);
         BlockStairs.EnumShape shape = (BlockStairs.EnumShape)state.getValue(SHAPE);
         if (face == EnumFacing.UP) {
            return half == BlockStairs.EnumHalf.TOP;
         } else if (face == EnumFacing.DOWN) {
            return half == BlockStairs.EnumHalf.BOTTOM;
         } else if (shape != BlockStairs.EnumShape.OUTER_LEFT && shape != BlockStairs.EnumShape.OUTER_RIGHT) {
            if (face == side) {
               return true;
            } else if (shape == BlockStairs.EnumShape.INNER_LEFT && face.rotateY() == side) {
               return true;
            } else {
               return shape == BlockStairs.EnumShape.INNER_RIGHT && face.rotateYCCW() == side;
            }
         } else {
            return false;
         }
      }
   }

   public static enum EnumHalf implements IStringSerializable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private EnumHalf(String var3) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }

   public static enum EnumShape implements IStringSerializable {
      STRAIGHT("straight"),
      INNER_LEFT("inner_left"),
      INNER_RIGHT("inner_right"),
      OUTER_LEFT("outer_left"),
      OUTER_RIGHT("outer_right");

      private final String name;

      private EnumShape(String var3) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
