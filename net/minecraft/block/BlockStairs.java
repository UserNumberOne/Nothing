package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.ArrayList;
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
      super(var1.getBlock().blockMaterial);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT));
      this.modelBlock = var1.getBlock();
      this.modelState = var1;
      this.setHardness(this.modelBlock.blockHardness);
      this.setResistance(this.modelBlock.blockResistance / 3.0F);
      this.setSoundType(this.modelBlock.blockSoundType);
      this.setLightOpacity(255);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      var1 = this.getActualState(var1, var2, var3);

      for(AxisAlignedBB var8 : getCollisionBoxList(var1)) {
         addCollisionBoxToList(var3, var4, var5, var8);
      }

   }

   private static List getCollisionBoxList(IBlockState var0) {
      ArrayList var1 = Lists.newArrayList();
      boolean var2 = var0.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      var1.add(var2 ? AABB_SLAB_TOP : AABB_SLAB_BOTTOM);
      BlockStairs.EnumShape var3 = (BlockStairs.EnumShape)var0.getValue(SHAPE);
      if (var3 == BlockStairs.EnumShape.STRAIGHT || var3 == BlockStairs.EnumShape.INNER_LEFT || var3 == BlockStairs.EnumShape.INNER_RIGHT) {
         var1.add(getCollQuarterBlock(var0));
      }

      if (var3 != BlockStairs.EnumShape.STRAIGHT) {
         var1.add(getCollEighthBlock(var0));
      }

      return var1;
   }

   private static AxisAlignedBB getCollQuarterBlock(IBlockState var0) {
      boolean var1 = var0.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      switch((EnumFacing)var0.getValue(FACING)) {
      case NORTH:
      default:
         return var1 ? AABB_QTR_BOT_NORTH : AABB_QTR_TOP_NORTH;
      case SOUTH:
         return var1 ? AABB_QTR_BOT_SOUTH : AABB_QTR_TOP_SOUTH;
      case WEST:
         return var1 ? AABB_QTR_BOT_WEST : AABB_QTR_TOP_WEST;
      case EAST:
         return var1 ? AABB_QTR_BOT_EAST : AABB_QTR_TOP_EAST;
      }
   }

   private static AxisAlignedBB getCollEighthBlock(IBlockState var0) {
      EnumFacing var1 = (EnumFacing)var0.getValue(FACING);
      EnumFacing var2;
      switch((BlockStairs.EnumShape)var0.getValue(SHAPE)) {
      case OUTER_LEFT:
      default:
         var2 = var1;
         break;
      case OUTER_RIGHT:
         var2 = var1.rotateY();
         break;
      case INNER_RIGHT:
         var2 = var1.getOpposite();
         break;
      case INNER_LEFT:
         var2 = var1.rotateYCCW();
      }

      boolean var3 = var0.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      switch(var2) {
      case NORTH:
      default:
         return var3 ? AABB_OCT_BOT_NW : AABB_OCT_TOP_NW;
      case SOUTH:
         return var3 ? AABB_OCT_BOT_SE : AABB_OCT_TOP_SE;
      case WEST:
         return var3 ? AABB_OCT_BOT_SW : AABB_OCT_TOP_SW;
      case EAST:
         return var3 ? AABB_OCT_BOT_NE : AABB_OCT_TOP_NE;
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
      this.modelBlock.randomDisplayTick(var1, var2, var3, var4);
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      this.modelBlock.onBlockClicked(var1, var2, var3);
   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
      this.modelBlock.onBlockDestroyedByPlayer(var1, var2, var3);
   }

   @SideOnly(Side.CLIENT)
   public int getPackedLightmapCoords(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return this.modelState.getPackedLightmapCoords(var2, var3);
   }

   public float getExplosionResistance(Entity var1) {
      return this.modelBlock.getExplosionResistance(var1);
   }

   public int tickRate(World var1) {
      return this.modelBlock.tickRate(var1);
   }

   public Vec3d modifyAcceleration(World var1, BlockPos var2, Entity var3, Vec3d var4) {
      return this.modelBlock.modifyAcceleration(var1, var2, var3, var4);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return this.modelBlock.getBlockLayer();
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getSelectedBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return this.modelState.getSelectedBoundingBox(var2, var3);
   }

   public boolean isCollidable() {
      return this.modelBlock.isCollidable();
   }

   public boolean canCollideCheck(IBlockState var1, boolean var2) {
      return this.modelBlock.canCollideCheck(var1, var2);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return this.modelBlock.canPlaceBlockAt(var1, var2);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.modelState.neighborChanged(var1, var2, Blocks.AIR);
      this.modelBlock.onBlockAdded(var1, var2, this.modelState);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      this.modelBlock.breakBlock(var1, var2, this.modelState);
   }

   public void onEntityWalk(World var1, BlockPos var2, Entity var3) {
      this.modelBlock.onEntityWalk(var1, var2, var3);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.modelBlock.updateTick(var1, var2, var3, var4);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      return this.modelBlock.onBlockActivated(var1, var2, this.modelState, var4, var5, var6, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
   }

   public void onBlockDestroyedByExplosion(World var1, BlockPos var2, Explosion var3) {
      this.modelBlock.onBlockDestroyedByExplosion(var1, var2, var3);
   }

   public boolean isFullyOpaque(IBlockState var1) {
      return var1.getValue(HALF) == BlockStairs.EnumHalf.TOP;
   }

   public MapColor getMapColor(IBlockState var1) {
      return this.modelBlock.getMapColor(this.modelState);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = super.getStateForPlacement(var1, var2, var3, var4, var5, var6, var7, var8);
      var9 = var9.withProperty(FACING, var8.getHorizontalFacing()).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT);
      return var3 == EnumFacing.DOWN || var3 != EnumFacing.UP && (double)var5 > 0.5D ? var9.withProperty(HALF, BlockStairs.EnumHalf.TOP) : var9.withProperty(HALF, BlockStairs.EnumHalf.BOTTOM);
   }

   @Nullable
   public RayTraceResult collisionRayTrace(IBlockState var1, World var2, BlockPos var3, Vec3d var4, Vec3d var5) {
      ArrayList var6 = Lists.newArrayList();

      for(AxisAlignedBB var8 : getCollisionBoxList(this.getActualState(var1, var2, var3))) {
         var6.add(this.rayTrace(var3, var4, var5, var8));
      }

      RayTraceResult var14 = null;
      double var15 = 0.0D;

      for(RayTraceResult var11 : var6) {
         if (var11 != null) {
            double var12 = var11.hitVec.squareDistanceTo(var5);
            if (var12 > var15) {
               var14 = var11;
               var15 = var12;
            }
         }
      }

      return var14;
   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState var2 = this.getDefaultState().withProperty(HALF, (var1 & 4) > 0 ? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
      var2 = var2.withProperty(FACING, EnumFacing.getFront(5 - (var1 & 3)));
      return var2;
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      if (var1.getValue(HALF) == BlockStairs.EnumHalf.TOP) {
         var2 |= 4;
      }

      var2 = var2 | 5 - ((EnumFacing)var1.getValue(FACING)).getIndex();
      return var2;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return var1.withProperty(SHAPE, getStairsShape(var1, var2, var3));
   }

   private static BlockStairs.EnumShape getStairsShape(IBlockState var0, IBlockAccess var1, BlockPos var2) {
      EnumFacing var3 = (EnumFacing)var0.getValue(FACING);
      IBlockState var4 = var1.getBlockState(var2.offset(var3));
      if (isBlockStairs(var4) && var0.getValue(HALF) == var4.getValue(HALF)) {
         EnumFacing var5 = (EnumFacing)var4.getValue(FACING);
         if (var5.getAxis() != ((EnumFacing)var0.getValue(FACING)).getAxis() && isDifferentStairs(var0, var1, var2, var5.getOpposite())) {
            if (var5 == var3.rotateYCCW()) {
               return BlockStairs.EnumShape.OUTER_LEFT;
            }

            return BlockStairs.EnumShape.OUTER_RIGHT;
         }
      }

      IBlockState var7 = var1.getBlockState(var2.offset(var3.getOpposite()));
      if (isBlockStairs(var7) && var0.getValue(HALF) == var7.getValue(HALF)) {
         EnumFacing var6 = (EnumFacing)var7.getValue(FACING);
         if (var6.getAxis() != ((EnumFacing)var0.getValue(FACING)).getAxis() && isDifferentStairs(var0, var1, var2, var6)) {
            if (var6 == var3.rotateYCCW()) {
               return BlockStairs.EnumShape.INNER_LEFT;
            }

            return BlockStairs.EnumShape.INNER_RIGHT;
         }
      }

      return BlockStairs.EnumShape.STRAIGHT;
   }

   private static boolean isDifferentStairs(IBlockState var0, IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      IBlockState var4 = var1.getBlockState(var2.offset(var3));
      return !isBlockStairs(var4) || var4.getValue(FACING) != var0.getValue(FACING) || var4.getValue(HALF) != var0.getValue(HALF);
   }

   public static boolean isBlockStairs(IBlockState var0) {
      return var0.getBlock() instanceof BlockStairs;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      EnumFacing var3 = (EnumFacing)var1.getValue(FACING);
      BlockStairs.EnumShape var4 = (BlockStairs.EnumShape)var1.getValue(SHAPE);
      switch(var2) {
      case LEFT_RIGHT:
         if (var3.getAxis() == EnumFacing.Axis.Z) {
            switch(var4) {
            case OUTER_LEFT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
            case OUTER_RIGHT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
            case INNER_RIGHT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
            case INNER_LEFT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
            default:
               return var1.withRotation(Rotation.CLOCKWISE_180);
            }
         }
         break;
      case FRONT_BACK:
         if (var3.getAxis() == EnumFacing.Axis.X) {
            switch(var4) {
            case OUTER_LEFT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
            case OUTER_RIGHT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
            case INNER_RIGHT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
            case INNER_LEFT:
               return var1.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
            case STRAIGHT:
               return var1.withRotation(Rotation.CLOCKWISE_180);
            }
         }
      }

      return super.withMirror(var1, var2);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, HALF, SHAPE});
   }

   public boolean doesSideBlockRendering(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (ForgeModContainer.disableStairSlabCulling) {
         return super.doesSideBlockRendering(var1, var2, var3, var4);
      } else if (var1.isOpaqueCube()) {
         return true;
      } else {
         var1 = this.getActualState(var1, var2, var3);
         BlockStairs.EnumHalf var5 = (BlockStairs.EnumHalf)var1.getValue(HALF);
         EnumFacing var6 = (EnumFacing)var1.getValue(FACING);
         BlockStairs.EnumShape var7 = (BlockStairs.EnumShape)var1.getValue(SHAPE);
         if (var4 == EnumFacing.UP) {
            return var5 == BlockStairs.EnumHalf.TOP;
         } else if (var4 == EnumFacing.DOWN) {
            return var5 == BlockStairs.EnumHalf.BOTTOM;
         } else if (var7 != BlockStairs.EnumShape.OUTER_LEFT && var7 != BlockStairs.EnumShape.OUTER_RIGHT) {
            if (var4 == var6) {
               return true;
            } else if (var7 == BlockStairs.EnumShape.INNER_LEFT && var4.rotateY() == var6) {
               return true;
            } else {
               return var7 == BlockStairs.EnumShape.INNER_RIGHT && var4.rotateYCCW() == var6;
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
         this.name = var3;
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
         this.name = var3;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
