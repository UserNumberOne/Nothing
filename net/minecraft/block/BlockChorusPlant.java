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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockChorusPlant extends Block {
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   public static final PropertyBool UP = PropertyBool.create("up");
   public static final PropertyBool DOWN = PropertyBool.create("down");

   protected BlockChorusPlant() {
      super(Material.PLANTS);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)).withProperty(UP, Boolean.valueOf(false)).withProperty(DOWN, Boolean.valueOf(false)));
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      Block var4 = var2.getBlockState(var3.down()).getBlock();
      Block var5 = var2.getBlockState(var3.up()).getBlock();
      Block var6 = var2.getBlockState(var3.north()).getBlock();
      Block var7 = var2.getBlockState(var3.east()).getBlock();
      Block var8 = var2.getBlockState(var3.south()).getBlock();
      Block var9 = var2.getBlockState(var3.west()).getBlock();
      return var1.withProperty(DOWN, Boolean.valueOf(var4 == this || var4 == Blocks.CHORUS_FLOWER || var4 == Blocks.END_STONE)).withProperty(UP, Boolean.valueOf(var5 == this || var5 == Blocks.CHORUS_FLOWER)).withProperty(NORTH, Boolean.valueOf(var6 == this || var6 == Blocks.CHORUS_FLOWER)).withProperty(EAST, Boolean.valueOf(var7 == this || var7 == Blocks.CHORUS_FLOWER)).withProperty(SOUTH, Boolean.valueOf(var8 == this || var8 == Blocks.CHORUS_FLOWER)).withProperty(WEST, Boolean.valueOf(var9 == this || var9 == Blocks.CHORUS_FLOWER));
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      var1 = var1.getActualState(var2, var3);
      float var4 = 0.1875F;
      float var5 = ((Boolean)var1.getValue(WEST)).booleanValue() ? 0.0F : 0.1875F;
      float var6 = ((Boolean)var1.getValue(DOWN)).booleanValue() ? 0.0F : 0.1875F;
      float var7 = ((Boolean)var1.getValue(NORTH)).booleanValue() ? 0.0F : 0.1875F;
      float var8 = ((Boolean)var1.getValue(EAST)).booleanValue() ? 1.0F : 0.8125F;
      float var9 = ((Boolean)var1.getValue(UP)).booleanValue() ? 1.0F : 0.8125F;
      float var10 = ((Boolean)var1.getValue(SOUTH)).booleanValue() ? 1.0F : 0.8125F;
      return new AxisAlignedBB((double)var5, (double)var6, (double)var7, (double)var8, (double)var9, (double)var10);
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      var1 = var1.getActualState(var2, var3);
      float var7 = 0.1875F;
      float var8 = 0.8125F;
      addCollisionBoxToList(var3, var4, var5, new AxisAlignedBB(0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D));
      if (((Boolean)var1.getValue(WEST)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, new AxisAlignedBB(0.0D, 0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D));
      }

      if (((Boolean)var1.getValue(EAST)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, new AxisAlignedBB(0.8125D, 0.1875D, 0.1875D, 1.0D, 0.8125D, 0.8125D));
      }

      if (((Boolean)var1.getValue(UP)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, new AxisAlignedBB(0.1875D, 0.8125D, 0.1875D, 0.8125D, 1.0D, 0.8125D));
      }

      if (((Boolean)var1.getValue(DOWN)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.1875D, 0.8125D));
      }

      if (((Boolean)var1.getValue(NORTH)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, new AxisAlignedBB(0.1875D, 0.1875D, 0.0D, 0.8125D, 0.8125D, 0.1875D));
      }

      if (((Boolean)var1.getValue(SOUTH)).booleanValue()) {
         addCollisionBoxToList(var3, var4, var5, new AxisAlignedBB(0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D, 1.0D));
      }

   }

   public int getMetaFromState(IBlockState var1) {
      return 0;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!this.canSurviveAt(var1, var2)) {
         var1.destroyBlock(var2, true);
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.CHORUS_FRUIT;
   }

   public int quantityDropped(Random var1) {
      return var1.nextInt(2);
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) ? this.canSurviveAt(var1, var2) : false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canSurviveAt(var2, var3)) {
         var2.scheduleUpdate(var3, this, 1);
      }

   }

   public boolean canSurviveAt(World var1, BlockPos var2) {
      boolean var3 = var1.isAirBlock(var2.up());
      boolean var4 = var1.isAirBlock(var2.down());

      for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
         BlockPos var7 = var2.offset(var6);
         Block var8 = var1.getBlockState(var7).getBlock();
         if (var8 == this) {
            if (!var3 && !var4) {
               return false;
            }

            Block var9 = var1.getBlockState(var7.down()).getBlock();
            if (var9 == this || var9 == Blocks.END_STONE) {
               return true;
            }
         }
      }

      Block var10 = var1.getBlockState(var2.down()).getBlock();
      return var10 == this || var10 == Blocks.END_STONE;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, SOUTH, WEST, UP, DOWN});
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      Block var5 = var2.getBlockState(var3.offset(var4)).getBlock();
      return var5 != this && var5 != Blocks.CHORUS_FLOWER && (var4 != EnumFacing.DOWN || var5 != Blocks.END_STONE);
   }
}
