package net.minecraft.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemLead;
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

public class BlockFence extends Block {
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   protected static final AxisAlignedBB[] BOUNDING_BOXES = new AxisAlignedBB[]{new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};
   public static final AxisAlignedBB PILLAR_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.5D, 0.625D);
   public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.625D, 0.625D, 1.5D, 1.0D);
   public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.375D, 1.5D, 0.625D);
   public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.5D, 0.375D);
   public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.625D, 0.0D, 0.375D, 1.0D, 1.5D, 0.625D);

   public BlockFence(Material var1, MapColor var2) {
      super(var1, var2);
      this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      var1 = var1.getActualState(var2, var3);
      addCollisionBoxToList(var3, var4, var5, PILLAR_AABB);
      if (((Boolean)var1.getValue(NORTH)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, NORTH_AABB);
      }

      if (((Boolean)var1.getValue(EAST)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, EAST_AABB);
      }

      if (((Boolean)var1.getValue(SOUTH)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, SOUTH_AABB);
      }

      if (((Boolean)var1.getValue(WEST)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, WEST_AABB);
      }

   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      var1 = this.getActualState(var1, var2, var3);
      return BOUNDING_BOXES[getBoundingBoxIdx(var1)];
   }

   private static int getBoundingBoxIdx(IBlockState var0) {
      int var1 = 0;
      if (((Boolean)var0.getValue(NORTH)).booleanValue()) {
         var1 |= 1 << EnumFacing.NORTH.getHorizontalIndex();
      }

      if (((Boolean)var0.getValue(EAST)).booleanValue()) {
         var1 |= 1 << EnumFacing.EAST.getHorizontalIndex();
      }

      if (((Boolean)var0.getValue(SOUTH)).booleanValue()) {
         var1 |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
      }

      if (((Boolean)var0.getValue(WEST)).booleanValue()) {
         var1 |= 1 << EnumFacing.WEST.getHorizontalIndex();
      }

      return var1;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return false;
   }

   public boolean canConnectTo(IBlockAccess var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      Block var4 = var3.getBlock();
      return var4 == Blocks.BARRIER ? false : ((!(var4 instanceof BlockFence) || var4.blockMaterial != this.blockMaterial) && !(var4 instanceof BlockFenceGate) ? (var4.blockMaterial.isOpaque() && var3.isFullCube() ? var4.blockMaterial != Material.GOURD : false) : true);
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return true;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      return var1.isRemote ? true : ItemLead.attachToFence(var4, var1, var2);
   }

   public int getMetaFromState(IBlockState var1) {
      return 0;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return var1.withProperty(NORTH, Boolean.valueOf(this.canConnectTo(var2, var3.north()))).withProperty(EAST, Boolean.valueOf(this.canConnectTo(var2, var3.east()))).withProperty(SOUTH, Boolean.valueOf(this.canConnectTo(var2, var3.south()))).withProperty(WEST, Boolean.valueOf(this.canConnectTo(var2, var3.west())));
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
