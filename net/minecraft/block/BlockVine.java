package net.minecraft.block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockVine extends Block implements IShearable {
   public static final PropertyBool UP = PropertyBool.create("up");
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   public static final PropertyBool[] ALL_FACES = new PropertyBool[]{UP, NORTH, SOUTH, WEST, EAST};
   protected static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0D, 0.9375D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0625D, 1.0D, 1.0D);
   protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.9375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.0625D);
   protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.9375D, 1.0D, 1.0D, 1.0D);

   public BlockVine() {
      super(Material.VINE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(UP, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      state = state.getActualState(source, pos);
      int i = 0;
      AxisAlignedBB axisalignedbb = FULL_BLOCK_AABB;
      if (((Boolean)state.getValue(UP)).booleanValue()) {
         axisalignedbb = UP_AABB;
         ++i;
      }

      if (((Boolean)state.getValue(NORTH)).booleanValue()) {
         axisalignedbb = NORTH_AABB;
         ++i;
      }

      if (((Boolean)state.getValue(EAST)).booleanValue()) {
         axisalignedbb = EAST_AABB;
         ++i;
      }

      if (((Boolean)state.getValue(SOUTH)).booleanValue()) {
         axisalignedbb = SOUTH_AABB;
         ++i;
      }

      if (((Boolean)state.getValue(WEST)).booleanValue()) {
         axisalignedbb = WEST_AABB;
         ++i;
      }

      return i == 1 ? axisalignedbb : FULL_BLOCK_AABB;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.withProperty(UP, Boolean.valueOf(worldIn.getBlockState(pos.up()).isBlockNormalCube()));
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      switch(side) {
      case UP:
         return this.canAttachVineOn(worldIn.getBlockState(pos.up()));
      case NORTH:
      case SOUTH:
      case EAST:
      case WEST:
         return this.canAttachVineOn(worldIn.getBlockState(pos.offset(side.getOpposite())));
      default:
         return false;
      }
   }

   private boolean canAttachVineOn(IBlockState var1) {
      return state.isFullCube() && state.getMaterial().blocksMovement();
   }

   private boolean recheckGrownSides(World var1, BlockPos var2, IBlockState var3) {
      IBlockState iblockstate = state;

      for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
         PropertyBool propertybool = getPropertyFor(enumfacing);
         if (((Boolean)state.getValue(propertybool)).booleanValue() && !this.canAttachVineOn(worldIn.getBlockState(pos.offset(enumfacing)))) {
            IBlockState iblockstate1 = worldIn.getBlockState(pos.up());
            if (iblockstate1.getBlock() != this || !((Boolean)iblockstate1.getValue(propertybool)).booleanValue()) {
               state = state.withProperty(propertybool, Boolean.valueOf(false));
            }
         }
      }

      if (getNumGrownFaces(state) == 0) {
         return false;
      } else {
         if (iblockstate != state) {
            worldIn.setBlockState(pos, state, 2);
         }

         return true;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!worldIn.isRemote && !this.recheckGrownSides(worldIn, pos, state)) {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!worldIn.isRemote && worldIn.rand.nextInt(4) == 0) {
         int i = 4;
         int j = 5;
         boolean flag = false;

         label184:
         for(int k = -4; k <= 4; ++k) {
            for(int l = -4; l <= 4; ++l) {
               for(int i1 = -1; i1 <= 1; ++i1) {
                  if (worldIn.getBlockState(pos.add(k, i1, l)).getBlock() == this) {
                     --j;
                     if (j <= 0) {
                        flag = true;
                        break label184;
                     }
                  }
               }
            }
         }

         EnumFacing enumfacing1 = EnumFacing.random(rand);
         BlockPos blockpos2 = pos.up();
         if (enumfacing1 == EnumFacing.UP && pos.getY() < 255 && worldIn.isAirBlock(blockpos2)) {
            if (!flag) {
               IBlockState iblockstate2 = state;

               for(EnumFacing enumfacing2 : EnumFacing.Plane.HORIZONTAL) {
                  if (rand.nextBoolean() || !this.canAttachVineOn(worldIn.getBlockState(blockpos2.offset(enumfacing2)))) {
                     iblockstate2 = iblockstate2.withProperty(getPropertyFor(enumfacing2), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)iblockstate2.getValue(NORTH)).booleanValue() || ((Boolean)iblockstate2.getValue(EAST)).booleanValue() || ((Boolean)iblockstate2.getValue(SOUTH)).booleanValue() || ((Boolean)iblockstate2.getValue(WEST)).booleanValue()) {
                  worldIn.setBlockState(blockpos2, iblockstate2, 2);
               }
            }
         } else if (enumfacing1.getAxis().isHorizontal() && !((Boolean)state.getValue(getPropertyFor(enumfacing1))).booleanValue()) {
            if (!flag) {
               BlockPos blockpos4 = pos.offset(enumfacing1);
               IBlockState iblockstate3 = worldIn.getBlockState(blockpos4);
               Block block1 = iblockstate3.getBlock();
               if (block1.blockMaterial == Material.AIR) {
                  EnumFacing enumfacing3 = enumfacing1.rotateY();
                  EnumFacing enumfacing4 = enumfacing1.rotateYCCW();
                  boolean flag1 = ((Boolean)state.getValue(getPropertyFor(enumfacing3))).booleanValue();
                  boolean flag2 = ((Boolean)state.getValue(getPropertyFor(enumfacing4))).booleanValue();
                  BlockPos blockpos = blockpos4.offset(enumfacing3);
                  BlockPos blockpos1 = blockpos4.offset(enumfacing4);
                  if (flag1 && this.canAttachVineOn(worldIn.getBlockState(blockpos))) {
                     worldIn.setBlockState(blockpos4, this.getDefaultState().withProperty(getPropertyFor(enumfacing3), Boolean.valueOf(true)), 2);
                  } else if (flag2 && this.canAttachVineOn(worldIn.getBlockState(blockpos1))) {
                     worldIn.setBlockState(blockpos4, this.getDefaultState().withProperty(getPropertyFor(enumfacing4), Boolean.valueOf(true)), 2);
                  } else if (flag1 && worldIn.isAirBlock(blockpos) && this.canAttachVineOn(worldIn.getBlockState(pos.offset(enumfacing3)))) {
                     worldIn.setBlockState(blockpos, this.getDefaultState().withProperty(getPropertyFor(enumfacing1.getOpposite()), Boolean.valueOf(true)), 2);
                  } else if (flag2 && worldIn.isAirBlock(blockpos1) && this.canAttachVineOn(worldIn.getBlockState(pos.offset(enumfacing4)))) {
                     worldIn.setBlockState(blockpos1, this.getDefaultState().withProperty(getPropertyFor(enumfacing1.getOpposite()), Boolean.valueOf(true)), 2);
                  } else if (this.canAttachVineOn(worldIn.getBlockState(blockpos4.up()))) {
                     worldIn.setBlockState(blockpos4, this.getDefaultState(), 2);
                  }
               } else if (block1.blockMaterial.isOpaque() && iblockstate3.isFullCube()) {
                  worldIn.setBlockState(pos, state.withProperty(getPropertyFor(enumfacing1), Boolean.valueOf(true)), 2);
               }
            }
         } else if (pos.getY() > 1) {
            BlockPos blockpos3 = pos.down();
            IBlockState iblockstate = worldIn.getBlockState(blockpos3);
            Block block = iblockstate.getBlock();
            if (block.blockMaterial == Material.AIR) {
               IBlockState iblockstate1 = state;

               for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                  if (rand.nextBoolean()) {
                     iblockstate1 = iblockstate1.withProperty(getPropertyFor(enumfacing), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)iblockstate1.getValue(NORTH)).booleanValue() || ((Boolean)iblockstate1.getValue(EAST)).booleanValue() || ((Boolean)iblockstate1.getValue(SOUTH)).booleanValue() || ((Boolean)iblockstate1.getValue(WEST)).booleanValue()) {
                  worldIn.setBlockState(blockpos3, iblockstate1, 2);
               }
            } else if (block == this) {
               IBlockState iblockstate4 = iblockstate;

               for(EnumFacing enumfacing5 : EnumFacing.Plane.HORIZONTAL) {
                  PropertyBool propertybool = getPropertyFor(enumfacing5);
                  if (rand.nextBoolean() && ((Boolean)state.getValue(propertybool)).booleanValue()) {
                     iblockstate4 = iblockstate4.withProperty(propertybool, Boolean.valueOf(true));
                  }
               }

               if (((Boolean)iblockstate4.getValue(NORTH)).booleanValue() || ((Boolean)iblockstate4.getValue(EAST)).booleanValue() || ((Boolean)iblockstate4.getValue(SOUTH)).booleanValue() || ((Boolean)iblockstate4.getValue(WEST)).booleanValue()) {
                  worldIn.setBlockState(blockpos3, iblockstate4, 2);
               }
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState iblockstate = this.getDefaultState().withProperty(UP, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false));
      return facing.getAxis().isHorizontal() ? iblockstate.withProperty(getPropertyFor(facing.getOpposite()), Boolean.valueOf(true)) : iblockstate;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      super.harvestBlock(worldIn, player, pos, state, te, stack);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SOUTH, Boolean.valueOf((meta & 1) > 0)).withProperty(WEST, Boolean.valueOf((meta & 2) > 0)).withProperty(NORTH, Boolean.valueOf((meta & 4) > 0)).withProperty(EAST, Boolean.valueOf((meta & 8) > 0));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      if (((Boolean)state.getValue(SOUTH)).booleanValue()) {
         i |= 1;
      }

      if (((Boolean)state.getValue(WEST)).booleanValue()) {
         i |= 2;
      }

      if (((Boolean)state.getValue(NORTH)).booleanValue()) {
         i |= 4;
      }

      if (((Boolean)state.getValue(EAST)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{UP, NORTH, EAST, SOUTH, WEST});
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

   public static PropertyBool getPropertyFor(EnumFacing var0) {
      switch(side) {
      case UP:
         return UP;
      case NORTH:
         return NORTH;
      case SOUTH:
         return SOUTH;
      case EAST:
         return EAST;
      case WEST:
         return WEST;
      default:
         throw new IllegalArgumentException(side + " is an invalid choice");
      }
   }

   public static int getNumGrownFaces(IBlockState var0) {
      int i = 0;

      for(PropertyBool propertybool : ALL_FACES) {
         if (((Boolean)state.getValue(propertybool)).booleanValue()) {
            ++i;
         }
      }

      return i;
   }

   public boolean isLadder(IBlockState var1, IBlockAccess var2, BlockPos var3, EntityLivingBase var4) {
      return true;
   }

   public boolean isShearable(ItemStack var1, IBlockAccess var2, BlockPos var3) {
      return true;
   }

   public List onSheared(ItemStack var1, IBlockAccess var2, BlockPos var3, int var4) {
      return Arrays.asList(new ItemStack(this, 1));
   }
}
