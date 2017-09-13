package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTrapDoor extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyBool OPEN = PropertyBool.create("open");
   public static final PropertyEnum HALF = PropertyEnum.create("half", BlockTrapDoor.DoorHalf.class);
   protected static final AxisAlignedBB EAST_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
   protected static final AxisAlignedBB WEST_OPEN_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB SOUTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
   protected static final AxisAlignedBB NORTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);
   protected static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0D, 0.8125D, 0.0D, 1.0D, 1.0D, 1.0D);

   protected BlockTrapDoor(Material var1) {
      super(materialIn);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, Boolean.valueOf(false)).withProperty(HALF, BlockTrapDoor.DoorHalf.BOTTOM));
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      AxisAlignedBB axisalignedbb;
      if (((Boolean)state.getValue(OPEN)).booleanValue()) {
         switch((EnumFacing)state.getValue(FACING)) {
         case NORTH:
         default:
            axisalignedbb = NORTH_OPEN_AABB;
            break;
         case SOUTH:
            axisalignedbb = SOUTH_OPEN_AABB;
            break;
         case WEST:
            axisalignedbb = WEST_OPEN_AABB;
            break;
         case EAST:
            axisalignedbb = EAST_OPEN_AABB;
         }
      } else if (state.getValue(HALF) == BlockTrapDoor.DoorHalf.TOP) {
         axisalignedbb = TOP_AABB;
      } else {
         axisalignedbb = BOTTOM_AABB;
      }

      return axisalignedbb;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return !((Boolean)worldIn.getBlockState(pos).getValue(OPEN)).booleanValue();
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (this.blockMaterial == Material.IRON) {
         return true;
      } else {
         state = state.cycleProperty(OPEN);
         worldIn.setBlockState(pos, state, 2);
         this.playSound(playerIn, worldIn, pos, ((Boolean)state.getValue(OPEN)).booleanValue());
         return true;
      }
   }

   protected void playSound(@Nullable EntityPlayer var1, World var2, BlockPos var3, boolean var4) {
      if (p_185731_4_) {
         int i = this.blockMaterial == Material.IRON ? 1037 : 1007;
         worldIn.playEvent(player, i, pos, 0);
      } else {
         int j = this.blockMaterial == Material.IRON ? 1036 : 1013;
         worldIn.playEvent(player, j, pos, 0);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!worldIn.isRemote) {
         boolean flag = worldIn.isBlockPowered(pos);
         if (flag || blockIn.getDefaultState().canProvidePower()) {
            boolean flag1 = ((Boolean)state.getValue(OPEN)).booleanValue();
            if (flag1 != flag) {
               worldIn.setBlockState(pos, state.withProperty(OPEN, Boolean.valueOf(flag)), 2);
               this.playSound((EntityPlayer)null, worldIn, pos, flag);
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState iblockstate = this.getDefaultState();
      if (facing.getAxis().isHorizontal()) {
         iblockstate = iblockstate.withProperty(FACING, facing).withProperty(OPEN, Boolean.valueOf(false));
         iblockstate = iblockstate.withProperty(HALF, hitY > 0.5F ? BlockTrapDoor.DoorHalf.TOP : BlockTrapDoor.DoorHalf.BOTTOM);
      } else {
         iblockstate = iblockstate.withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(OPEN, Boolean.valueOf(false));
         iblockstate = iblockstate.withProperty(HALF, facing == EnumFacing.UP ? BlockTrapDoor.DoorHalf.BOTTOM : BlockTrapDoor.DoorHalf.TOP);
      }

      return iblockstate;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return true;
   }

   protected static EnumFacing getFacing(int var0) {
      switch(meta & 3) {
      case 0:
         return EnumFacing.NORTH;
      case 1:
         return EnumFacing.SOUTH;
      case 2:
         return EnumFacing.WEST;
      case 3:
      default:
         return EnumFacing.EAST;
      }
   }

   protected static int getMetaForFacing(EnumFacing var0) {
      switch(facing) {
      case NORTH:
         return 0;
      case SOUTH:
         return 1;
      case WEST:
         return 2;
      case EAST:
      default:
         return 3;
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, getFacing(meta)).withProperty(OPEN, Boolean.valueOf((meta & 4) != 0)).withProperty(HALF, (meta & 8) == 0 ? BlockTrapDoor.DoorHalf.BOTTOM : BlockTrapDoor.DoorHalf.TOP);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | getMetaForFacing((EnumFacing)state.getValue(FACING));
      if (((Boolean)state.getValue(OPEN)).booleanValue()) {
         i |= 4;
      }

      if (state.getValue(HALF) == BlockTrapDoor.DoorHalf.TOP) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, OPEN, HALF});
   }

   public boolean isLadder(IBlockState var1, IBlockAccess var2, BlockPos var3, EntityLivingBase var4) {
      if (((Boolean)state.getValue(OPEN)).booleanValue()) {
         IBlockState down = world.getBlockState(pos.down());
         if (down.getBlock() == Blocks.LADDER) {
            return down.getValue(BlockLadder.FACING) == state.getValue(FACING);
         }
      }

      return false;
   }

   public static enum DoorHalf implements IStringSerializable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private DoorHalf(String var3) {
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
