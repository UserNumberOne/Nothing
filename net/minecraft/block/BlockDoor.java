package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDoor extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyBool OPEN = PropertyBool.create("open");
   public static final PropertyEnum HINGE = PropertyEnum.create("hinge", BlockDoor.EnumHingePosition.class);
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   public static final PropertyEnum HALF = PropertyEnum.create("half", BlockDoor.EnumDoorHalf.class);
   protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
   protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);

   protected BlockDoor(Material var1) {
      super(var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, Boolean.valueOf(false)).withProperty(HINGE, BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, Boolean.valueOf(false)).withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER));
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      var1 = var1.getActualState(var2, var3);
      EnumFacing var4 = (EnumFacing)var1.getValue(FACING);
      boolean var5 = !((Boolean)var1.getValue(OPEN)).booleanValue();
      boolean var6 = var1.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT;
      switch(var4) {
      case EAST:
      default:
         return var5 ? EAST_AABB : (var6 ? NORTH_AABB : SOUTH_AABB);
      case SOUTH:
         return var5 ? SOUTH_AABB : (var6 ? EAST_AABB : WEST_AABB);
      case WEST:
         return var5 ? WEST_AABB : (var6 ? SOUTH_AABB : NORTH_AABB);
      case NORTH:
         return var5 ? NORTH_AABB : (var6 ? WEST_AABB : EAST_AABB);
      }
   }

   public String getLocalizedName() {
      return I18n.translateToLocal((this.getUnlocalizedName() + ".name").replaceAll("tile", "item"));
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return isOpen(combineMetadata(var1, var2));
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   private int getCloseSound() {
      return this.blockMaterial == Material.IRON ? 1011 : 1012;
   }

   private int getOpenSound() {
      return this.blockMaterial == Material.IRON ? 1005 : 1006;
   }

   public MapColor getMapColor(IBlockState var1) {
      return var1.getBlock() == Blocks.IRON_DOOR ? MapColor.IRON : (var1.getBlock() == Blocks.OAK_DOOR ? BlockPlanks.EnumType.OAK.getMapColor() : (var1.getBlock() == Blocks.SPRUCE_DOOR ? BlockPlanks.EnumType.SPRUCE.getMapColor() : (var1.getBlock() == Blocks.BIRCH_DOOR ? BlockPlanks.EnumType.BIRCH.getMapColor() : (var1.getBlock() == Blocks.JUNGLE_DOOR ? BlockPlanks.EnumType.JUNGLE.getMapColor() : (var1.getBlock() == Blocks.ACACIA_DOOR ? BlockPlanks.EnumType.ACACIA.getMapColor() : (var1.getBlock() == Blocks.DARK_OAK_DOOR ? BlockPlanks.EnumType.DARK_OAK.getMapColor() : super.getMapColor(var1)))))));
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (this.blockMaterial == Material.IRON) {
         return false;
      } else {
         BlockPos var11 = var3.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? var2 : var2.down();
         IBlockState var12 = var2.equals(var11) ? var3 : var1.getBlockState(var11);
         if (var12.getBlock() != this) {
            return false;
         } else {
            var3 = var12.cycleProperty(OPEN);
            var1.setBlockState(var11, var3, 10);
            var1.markBlockRangeForRenderUpdate(var11, var2);
            var1.playEvent(var4, ((Boolean)var3.getValue(OPEN)).booleanValue() ? this.getOpenSound() : this.getCloseSound(), var2, 0);
            return true;
         }
      }
   }

   public void toggleDoor(World var1, BlockPos var2, boolean var3) {
      IBlockState var4 = var1.getBlockState(var2);
      if (var4.getBlock() == this) {
         BlockPos var5 = var4.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? var2 : var2.down();
         IBlockState var6 = var2 == var5 ? var4 : var1.getBlockState(var5);
         if (var6.getBlock() == this && ((Boolean)var6.getValue(OPEN)).booleanValue() != var3) {
            var1.setBlockState(var5, var6.withProperty(OPEN, Boolean.valueOf(var3)), 10);
            var1.markBlockRangeForRenderUpdate(var5, var2);
            var1.playEvent((EntityPlayer)null, var3 ? this.getOpenSound() : this.getCloseSound(), var2, 0);
         }
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (var1.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
         BlockPos var5 = var3.down();
         IBlockState var6 = var2.getBlockState(var5);
         if (var6.getBlock() != this) {
            var2.setBlockToAir(var3);
         } else if (var4 != this) {
            var6.neighborChanged(var2, var5, var4);
         }
      } else {
         boolean var9 = false;
         BlockPos var10 = var3.up();
         IBlockState var7 = var2.getBlockState(var10);
         if (var7.getBlock() != this) {
            var2.setBlockToAir(var3);
            var9 = true;
         }

         if (!var2.getBlockState(var3.down()).isSideSolid(var2, var3.down(), EnumFacing.UP)) {
            var2.setBlockToAir(var3);
            var9 = true;
            if (var7.getBlock() == this) {
               var2.setBlockToAir(var10);
            }
         }

         if (var9) {
            if (!var2.isRemote) {
               this.dropBlockAsItem(var2, var3, var1, 0);
            }
         } else {
            boolean var8 = var2.isBlockPowered(var3) || var2.isBlockPowered(var10);
            if (var4 != this && (var8 || var4.getDefaultState().canProvidePower()) && var8 != ((Boolean)var7.getValue(POWERED)).booleanValue()) {
               var2.setBlockState(var10, var7.withProperty(POWERED, Boolean.valueOf(var8)), 2);
               if (var8 != ((Boolean)var1.getValue(OPEN)).booleanValue()) {
                  var2.setBlockState(var3, var1.withProperty(OPEN, Boolean.valueOf(var8)), 2);
                  var2.markBlockRangeForRenderUpdate(var3, var3);
                  var2.playEvent((EntityPlayer)null, var8 ? this.getOpenSound() : this.getCloseSound(), var3, 0);
               }
            }
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return var1.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? null : this.getItem();
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var2.getY() >= var1.getHeight() - 1 ? false : var1.getBlockState(var2.down()).isSideSolid(var1, var2.down(), EnumFacing.UP) && super.canPlaceBlockAt(var1, var2) && super.canPlaceBlockAt(var1, var2.up());
   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.DESTROY;
   }

   public static int combineMetadata(IBlockAccess var0, BlockPos var1) {
      IBlockState var2 = var0.getBlockState(var1);
      int var3 = var2.getBlock().getMetaFromState(var2);
      boolean var4 = isTop(var3);
      IBlockState var5 = var0.getBlockState(var1.down());
      int var6 = var5.getBlock().getMetaFromState(var5);
      int var7 = var4 ? var6 : var3;
      IBlockState var8 = var0.getBlockState(var1.up());
      int var9 = var8.getBlock().getMetaFromState(var8);
      int var10 = var4 ? var3 : var9;
      boolean var11 = (var10 & 1) != 0;
      boolean var12 = (var10 & 2) != 0;
      return removeHalfBit(var7) | (var4 ? 8 : 0) | (var11 ? 16 : 0) | (var12 ? 32 : 0);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this.getItem());
   }

   private Item getItem() {
      return this == Blocks.IRON_DOOR ? Items.IRON_DOOR : (this == Blocks.SPRUCE_DOOR ? Items.SPRUCE_DOOR : (this == Blocks.BIRCH_DOOR ? Items.BIRCH_DOOR : (this == Blocks.JUNGLE_DOOR ? Items.JUNGLE_DOOR : (this == Blocks.ACACIA_DOOR ? Items.ACACIA_DOOR : (this == Blocks.DARK_OAK_DOOR ? Items.DARK_OAK_DOOR : Items.OAK_DOOR)))));
   }

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      BlockPos var5 = var2.down();
      BlockPos var6 = var2.up();
      if (var4.capabilities.isCreativeMode && var3.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER && var1.getBlockState(var5).getBlock() == this) {
         var1.setBlockToAir(var5);
      }

      if (var3.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER && var1.getBlockState(var6).getBlock() == this) {
         if (var4.capabilities.isCreativeMode) {
            var1.setBlockToAir(var2);
         }

         var1.setBlockToAir(var6);
      }

   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      if (var1.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER) {
         IBlockState var4 = var2.getBlockState(var3.up());
         if (var4.getBlock() == this) {
            var1 = var1.withProperty(HINGE, var4.getValue(HINGE)).withProperty(POWERED, var4.getValue(POWERED));
         }
      } else {
         IBlockState var5 = var2.getBlockState(var3.down());
         if (var5.getBlock() == this) {
            var1 = var1.withProperty(FACING, var5.getValue(FACING)).withProperty(OPEN, var5.getValue(OPEN));
         }
      }

      return var1;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.getValue(HALF) != BlockDoor.EnumDoorHalf.LOWER ? var1 : var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var2 == Mirror.NONE ? var1 : var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING))).cycleProperty(HINGE);
   }

   public IBlockState getStateFromMeta(int var1) {
      return (var1 & 8) > 0 ? this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.UPPER).withProperty(HINGE, (var1 & 1) > 0 ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, Boolean.valueOf((var1 & 2) > 0)) : this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER).withProperty(FACING, EnumFacing.getHorizontal(var1 & 3).rotateYCCW()).withProperty(OPEN, Boolean.valueOf((var1 & 4) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      if (var1.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
         var2 = var2 | 8;
         if (var1.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT) {
            var2 |= 1;
         }

         if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
            var2 |= 2;
         }
      } else {
         var2 = var2 | ((EnumFacing)var1.getValue(FACING)).rotateY().getHorizontalIndex();
         if (((Boolean)var1.getValue(OPEN)).booleanValue()) {
            var2 |= 4;
         }
      }

      return var2;
   }

   protected static int removeHalfBit(int var0) {
      return var0 & 7;
   }

   public static boolean isOpen(IBlockAccess var0, BlockPos var1) {
      return isOpen(combineMetadata(var0, var1));
   }

   public static EnumFacing getFacing(IBlockAccess var0, BlockPos var1) {
      return getFacing(combineMetadata(var0, var1));
   }

   public static EnumFacing getFacing(int var0) {
      return EnumFacing.getHorizontal(var0 & 3).rotateYCCW();
   }

   protected static boolean isOpen(int var0) {
      return (var0 & 4) != 0;
   }

   protected static boolean isTop(int var0) {
      return (var0 & 8) != 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HALF, FACING, OPEN, HINGE, POWERED});
   }

   public static enum EnumDoorHalf implements IStringSerializable {
      UPPER,
      LOWER;

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this == UPPER ? "upper" : "lower";
      }
   }

   public static enum EnumHingePosition implements IStringSerializable {
      LEFT,
      RIGHT;

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this == LEFT ? "left" : "right";
      }
   }
}
