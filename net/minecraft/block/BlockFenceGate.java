package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFenceGate extends BlockHorizontal {
   public static final PropertyBool OPEN = PropertyBool.create("open");
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   public static final PropertyBool IN_WALL = PropertyBool.create("in_wall");
   protected static final AxisAlignedBB AABB_COLLIDE_ZAXIS = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
   protected static final AxisAlignedBB AABB_COLLIDE_XAXIS = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_COLLIDE_ZAXIS_INWALL = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 0.8125D, 0.625D);
   protected static final AxisAlignedBB AABB_COLLIDE_XAXIS_INWALL = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 0.8125D, 1.0D);
   protected static final AxisAlignedBB AABB_CLOSED_SELECTED_ZAXIS = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.5D, 0.625D);
   protected static final AxisAlignedBB AABB_CLOSED_SELECTED_XAXIS = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.5D, 1.0D);

   public BlockFenceGate(BlockPlanks.EnumType var1) {
      super(Material.WOOD, var1.getMapColor());
      this.setDefaultState(this.blockState.getBaseState().withProperty(OPEN, Boolean.valueOf(false)).withProperty(POWERED, Boolean.valueOf(false)).withProperty(IN_WALL, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      var1 = this.getActualState(var1, var2, var3);
      return ((Boolean)var1.getValue(IN_WALL)).booleanValue() ? (((EnumFacing)var1.getValue(FACING)).getAxis() == EnumFacing.Axis.X ? AABB_COLLIDE_XAXIS_INWALL : AABB_COLLIDE_ZAXIS_INWALL) : (((EnumFacing)var1.getValue(FACING)).getAxis() == EnumFacing.Axis.X ? AABB_COLLIDE_XAXIS : AABB_COLLIDE_ZAXIS);
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      EnumFacing.Axis var4 = ((EnumFacing)var1.getValue(FACING)).getAxis();
      if (var4 == EnumFacing.Axis.Z && (var2.getBlockState(var3.west()).getBlock() == Blocks.COBBLESTONE_WALL || var2.getBlockState(var3.east()).getBlock() == Blocks.COBBLESTONE_WALL) || var4 == EnumFacing.Axis.X && (var2.getBlockState(var3.north()).getBlock() == Blocks.COBBLESTONE_WALL || var2.getBlockState(var3.south()).getBlock() == Blocks.COBBLESTONE_WALL)) {
         var1 = var1.withProperty(IN_WALL, Boolean.valueOf(true));
      }

      return var1;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).getMaterial().isSolid() ? super.canPlaceBlockAt(var1, var2) : false;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return ((Boolean)var1.getValue(OPEN)).booleanValue() ? NULL_AABB : (((EnumFacing)var1.getValue(FACING)).getAxis() == EnumFacing.Axis.Z ? AABB_CLOSED_SELECTED_ZAXIS : AABB_CLOSED_SELECTED_XAXIS);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return ((Boolean)var1.getBlockState(var2).getValue(OPEN)).booleanValue();
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, var8.getHorizontalFacing()).withProperty(OPEN, Boolean.valueOf(false)).withProperty(POWERED, Boolean.valueOf(false)).withProperty(IN_WALL, Boolean.valueOf(false));
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (((Boolean)var3.getValue(OPEN)).booleanValue()) {
         var3 = var3.withProperty(OPEN, Boolean.valueOf(false));
         var1.setBlockState(var2, var3, 10);
      } else {
         EnumFacing var11 = EnumFacing.fromAngle((double)var4.rotationYaw);
         if (var3.getValue(FACING) == var11.getOpposite()) {
            var3 = var3.withProperty(FACING, var11);
         }

         var3 = var3.withProperty(OPEN, Boolean.valueOf(true));
         var1.setBlockState(var2, var3, 10);
      }

      var1.playEvent(var4, ((Boolean)var3.getValue(OPEN)).booleanValue() ? 1008 : 1014, var2, 0);
      return true;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         boolean var5 = var2.isBlockPowered(var3);
         if (var5 || var4.getDefaultState().canProvidePower()) {
            if (var5 && !((Boolean)var1.getValue(OPEN)).booleanValue() && !((Boolean)var1.getValue(POWERED)).booleanValue()) {
               var2.setBlockState(var3, var1.withProperty(OPEN, Boolean.valueOf(true)).withProperty(POWERED, Boolean.valueOf(true)), 2);
               var2.playEvent((EntityPlayer)null, 1008, var3, 0);
            } else if (!var5 && ((Boolean)var1.getValue(OPEN)).booleanValue() && ((Boolean)var1.getValue(POWERED)).booleanValue()) {
               var2.setBlockState(var3, var1.withProperty(OPEN, Boolean.valueOf(false)).withProperty(POWERED, Boolean.valueOf(false)), 2);
               var2.playEvent((EntityPlayer)null, 1014, var3, 0);
            } else if (var5 != ((Boolean)var1.getValue(POWERED)).booleanValue()) {
               var2.setBlockState(var3, var1.withProperty(POWERED, Boolean.valueOf(var5)), 2);
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return true;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(var1)).withProperty(OPEN, Boolean.valueOf((var1 & 4) != 0)).withProperty(POWERED, Boolean.valueOf((var1 & 8) != 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getHorizontalIndex();
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var2 |= 8;
      }

      if (((Boolean)var1.getValue(OPEN)).booleanValue()) {
         var2 |= 4;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, OPEN, POWERED, IN_WALL});
   }
}
