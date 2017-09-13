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
      var1 = var1.getActualState(var2, var3);
      int var4 = 0;
      AxisAlignedBB var5 = FULL_BLOCK_AABB;
      if (((Boolean)var1.getValue(UP)).booleanValue()) {
         var5 = UP_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(NORTH)).booleanValue()) {
         var5 = NORTH_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(EAST)).booleanValue()) {
         var5 = EAST_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(SOUTH)).booleanValue()) {
         var5 = SOUTH_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(WEST)).booleanValue()) {
         var5 = WEST_AABB;
         ++var4;
      }

      return var4 == 1 ? var5 : FULL_BLOCK_AABB;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return var1.withProperty(UP, Boolean.valueOf(var2.getBlockState(var3.up()).isBlockNormalCube()));
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
      switch(var3) {
      case UP:
         return this.canAttachVineOn(var1.getBlockState(var2.up()));
      case NORTH:
      case SOUTH:
      case EAST:
      case WEST:
         return this.canAttachVineOn(var1.getBlockState(var2.offset(var3.getOpposite())));
      default:
         return false;
      }
   }

   private boolean canAttachVineOn(IBlockState var1) {
      return var1.isFullCube() && var1.getMaterial().blocksMovement();
   }

   private boolean recheckGrownSides(World var1, BlockPos var2, IBlockState var3) {
      IBlockState var4 = var3;

      for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
         PropertyBool var7 = getPropertyFor(var6);
         if (((Boolean)var3.getValue(var7)).booleanValue() && !this.canAttachVineOn(var1.getBlockState(var2.offset(var6)))) {
            IBlockState var8 = var1.getBlockState(var2.up());
            if (var8.getBlock() != this || !((Boolean)var8.getValue(var7)).booleanValue()) {
               var3 = var3.withProperty(var7, Boolean.valueOf(false));
            }
         }
      }

      if (getNumGrownFaces(var3) == 0) {
         return false;
      } else {
         if (var4 != var3) {
            var1.setBlockState(var2, var3, 2);
         }

         return true;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote && !this.recheckGrownSides(var2, var3, var1)) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && var1.rand.nextInt(4) == 0) {
         boolean var5 = true;
         int var6 = 5;
         boolean var7 = false;

         label184:
         for(int var8 = -4; var8 <= 4; ++var8) {
            for(int var9 = -4; var9 <= 4; ++var9) {
               for(int var10 = -1; var10 <= 1; ++var10) {
                  if (var1.getBlockState(var2.add(var8, var10, var9)).getBlock() == this) {
                     --var6;
                     if (var6 <= 0) {
                        var7 = true;
                        break label184;
                     }
                  }
               }
            }
         }

         EnumFacing var19 = EnumFacing.random(var4);
         BlockPos var20 = var2.up();
         if (var19 == EnumFacing.UP && var2.getY() < 255 && var1.isAirBlock(var20)) {
            if (!var7) {
               IBlockState var23 = var3;

               for(EnumFacing var27 : EnumFacing.Plane.HORIZONTAL) {
                  if (var4.nextBoolean() || !this.canAttachVineOn(var1.getBlockState(var20.offset(var27)))) {
                     var23 = var23.withProperty(getPropertyFor(var27), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)var23.getValue(NORTH)).booleanValue() || ((Boolean)var23.getValue(EAST)).booleanValue() || ((Boolean)var23.getValue(SOUTH)).booleanValue() || ((Boolean)var23.getValue(WEST)).booleanValue()) {
                  var1.setBlockState(var20, var23, 2);
               }
            }
         } else if (var19.getAxis().isHorizontal() && !((Boolean)var3.getValue(getPropertyFor(var19))).booleanValue()) {
            if (!var7) {
               BlockPos var22 = var2.offset(var19);
               IBlockState var24 = var1.getBlockState(var22);
               Block var26 = var24.getBlock();
               if (var26.blockMaterial == Material.AIR) {
                  EnumFacing var29 = var19.rotateY();
                  EnumFacing var31 = var19.rotateYCCW();
                  boolean var33 = ((Boolean)var3.getValue(getPropertyFor(var29))).booleanValue();
                  boolean var34 = ((Boolean)var3.getValue(getPropertyFor(var31))).booleanValue();
                  BlockPos var17 = var22.offset(var29);
                  BlockPos var18 = var22.offset(var31);
                  if (var33 && this.canAttachVineOn(var1.getBlockState(var17))) {
                     var1.setBlockState(var22, this.getDefaultState().withProperty(getPropertyFor(var29), Boolean.valueOf(true)), 2);
                  } else if (var34 && this.canAttachVineOn(var1.getBlockState(var18))) {
                     var1.setBlockState(var22, this.getDefaultState().withProperty(getPropertyFor(var31), Boolean.valueOf(true)), 2);
                  } else if (var33 && var1.isAirBlock(var17) && this.canAttachVineOn(var1.getBlockState(var2.offset(var29)))) {
                     var1.setBlockState(var17, this.getDefaultState().withProperty(getPropertyFor(var19.getOpposite()), Boolean.valueOf(true)), 2);
                  } else if (var34 && var1.isAirBlock(var18) && this.canAttachVineOn(var1.getBlockState(var2.offset(var31)))) {
                     var1.setBlockState(var18, this.getDefaultState().withProperty(getPropertyFor(var19.getOpposite()), Boolean.valueOf(true)), 2);
                  } else if (this.canAttachVineOn(var1.getBlockState(var22.up()))) {
                     var1.setBlockState(var22, this.getDefaultState(), 2);
                  }
               } else if (var26.blockMaterial.isOpaque() && var24.isFullCube()) {
                  var1.setBlockState(var2, var3.withProperty(getPropertyFor(var19), Boolean.valueOf(true)), 2);
               }
            }
         } else if (var2.getY() > 1) {
            BlockPos var21 = var2.down();
            IBlockState var11 = var1.getBlockState(var21);
            Block var12 = var11.getBlock();
            if (var12.blockMaterial == Material.AIR) {
               IBlockState var13 = var3;

               for(EnumFacing var15 : EnumFacing.Plane.HORIZONTAL) {
                  if (var4.nextBoolean()) {
                     var13 = var13.withProperty(getPropertyFor(var15), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)var13.getValue(NORTH)).booleanValue() || ((Boolean)var13.getValue(EAST)).booleanValue() || ((Boolean)var13.getValue(SOUTH)).booleanValue() || ((Boolean)var13.getValue(WEST)).booleanValue()) {
                  var1.setBlockState(var21, var13, 2);
               }
            } else if (var12 == this) {
               IBlockState var28 = var11;

               for(EnumFacing var32 : EnumFacing.Plane.HORIZONTAL) {
                  PropertyBool var16 = getPropertyFor(var32);
                  if (var4.nextBoolean() && ((Boolean)var3.getValue(var16)).booleanValue()) {
                     var28 = var28.withProperty(var16, Boolean.valueOf(true));
                  }
               }

               if (((Boolean)var28.getValue(NORTH)).booleanValue() || ((Boolean)var28.getValue(EAST)).booleanValue() || ((Boolean)var28.getValue(SOUTH)).booleanValue() || ((Boolean)var28.getValue(WEST)).booleanValue()) {
                  var1.setBlockState(var21, var28, 2);
               }
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = this.getDefaultState().withProperty(UP, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false));
      return var3.getAxis().isHorizontal() ? var9.withProperty(getPropertyFor(var3.getOpposite()), Boolean.valueOf(true)) : var9;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      super.harvestBlock(var1, var2, var3, var4, var5, var6);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SOUTH, Boolean.valueOf((var1 & 1) > 0)).withProperty(WEST, Boolean.valueOf((var1 & 2) > 0)).withProperty(NORTH, Boolean.valueOf((var1 & 4) > 0)).withProperty(EAST, Boolean.valueOf((var1 & 8) > 0));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      if (((Boolean)var1.getValue(SOUTH)).booleanValue()) {
         var2 |= 1;
      }

      if (((Boolean)var1.getValue(WEST)).booleanValue()) {
         var2 |= 2;
      }

      if (((Boolean)var1.getValue(NORTH)).booleanValue()) {
         var2 |= 4;
      }

      if (((Boolean)var1.getValue(EAST)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{UP, NORTH, EAST, SOUTH, WEST});
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case CLOCKWISE_180:
         return var1.withProperty(NORTH, var1.getValue(SOUTH)).withProperty(EAST, var1.getValue(WEST)).withProperty(SOUTH, var1.getValue(NORTH)).withProperty(WEST, var1.getValue(EAST));
      case COUNTERCLOCKWISE_90:
         return var1.withProperty(NORTH, var1.getValue(EAST)).withProperty(EAST, var1.getValue(SOUTH)).withProperty(SOUTH, var1.getValue(WEST)).withProperty(WEST, var1.getValue(NORTH));
      case CLOCKWISE_90:
         return var1.withProperty(NORTH, var1.getValue(WEST)).withProperty(EAST, var1.getValue(NORTH)).withProperty(SOUTH, var1.getValue(EAST)).withProperty(WEST, var1.getValue(SOUTH));
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      switch(var2) {
      case LEFT_RIGHT:
         return var1.withProperty(NORTH, var1.getValue(SOUTH)).withProperty(SOUTH, var1.getValue(NORTH));
      case FRONT_BACK:
         return var1.withProperty(EAST, var1.getValue(WEST)).withProperty(WEST, var1.getValue(EAST));
      default:
         return super.withMirror(var1, var2);
      }
   }

   public static PropertyBool getPropertyFor(EnumFacing var0) {
      switch(var0) {
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
         throw new IllegalArgumentException(var0 + " is an invalid choice");
      }
   }

   public static int getNumGrownFaces(IBlockState var0) {
      int var1 = 0;

      for(PropertyBool var5 : ALL_FACES) {
         if (((Boolean)var0.getValue(var5)).booleanValue()) {
            ++var1;
         }
      }

      return var1;
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
