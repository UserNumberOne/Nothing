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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPane extends Block {
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   protected static final AxisAlignedBB[] AABB_BY_INDEX = new AxisAlignedBB[]{new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D), new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};
   private final boolean canDrop;

   protected BlockPane(Material var1, boolean var2) {
      super(var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)));
      this.canDrop = var2;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      var1 = this.getActualState(var1, var2, var3);
      addCollisionBoxToList(var3, var4, var5, AABB_BY_INDEX[0]);
      if (((Boolean)var1.getValue(NORTH)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.NORTH)]);
      }

      if (((Boolean)var1.getValue(SOUTH)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.SOUTH)]);
      }

      if (((Boolean)var1.getValue(EAST)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.EAST)]);
      }

      if (((Boolean)var1.getValue(WEST)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.WEST)]);
      }

   }

   private static int getBoundingBoxIndex(EnumFacing var0) {
      return 1 << var0.getHorizontalIndex();
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      var1 = this.getActualState(var1, var2, var3);
      return AABB_BY_INDEX[getBoundingBoxIndex(var1)];
   }

   private static int getBoundingBoxIndex(IBlockState var0) {
      int var1 = 0;
      if (((Boolean)var0.getValue(NORTH)).booleanValue()) {
         var1 |= getBoundingBoxIndex(EnumFacing.NORTH);
      }

      if (((Boolean)var0.getValue(EAST)).booleanValue()) {
         var1 |= getBoundingBoxIndex(EnumFacing.EAST);
      }

      if (((Boolean)var0.getValue(SOUTH)).booleanValue()) {
         var1 |= getBoundingBoxIndex(EnumFacing.SOUTH);
      }

      if (((Boolean)var0.getValue(WEST)).booleanValue()) {
         var1 |= getBoundingBoxIndex(EnumFacing.WEST);
      }

      return var1;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return var1.withProperty(NORTH, Boolean.valueOf(this.canPaneConnectToBlock(var2.getBlockState(var3.north()).getBlock()))).withProperty(SOUTH, Boolean.valueOf(this.canPaneConnectToBlock(var2.getBlockState(var3.south()).getBlock()))).withProperty(WEST, Boolean.valueOf(this.canPaneConnectToBlock(var2.getBlockState(var3.west()).getBlock()))).withProperty(EAST, Boolean.valueOf(this.canPaneConnectToBlock(var2.getBlockState(var3.east()).getBlock())));
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return !this.canDrop ? null : super.getItemDropped(var1, var2, var3);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public final boolean canPaneConnectToBlock(Block var1) {
      return var1.getDefaultState().isFullCube() || var1 == this || var1 == Blocks.GLASS || var1 == Blocks.STAINED_GLASS || var1 == Blocks.STAINED_GLASS_PANE || var1 instanceof BlockPane;
   }

   protected boolean canSilkHarvest() {
      return true;
   }

   public int getMetaFromState(IBlockState var1) {
      return 0;
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

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, WEST, SOUTH});
   }
}
