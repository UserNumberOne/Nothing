package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBanner extends BlockContainer {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);
   protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);

   protected BlockBanner() {
      super(Material.WOOD);
   }

   public String getLocalizedName() {
      return I18n.translateToLocal("item.banner.white.name");
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean canSpawnInBlock() {
      return true;
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityBanner();
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.BANNER;
   }

   @Nullable
   private ItemStack getTileDataItemStack(World var1, BlockPos var2, IBlockState var3) {
      TileEntity var4 = var1.getTileEntity(var2);
      if (var4 instanceof TileEntityBanner) {
         ItemStack var5 = new ItemStack(Items.BANNER, 1, ((TileEntityBanner)var4).getBaseColor());
         NBTTagCompound var6 = var4.writeToNBT(new NBTTagCompound());
         var6.removeTag("x");
         var6.removeTag("y");
         var6.removeTag("z");
         var6.removeTag("id");
         var5.setTagInfo("BlockEntityTag", var6);
         return var5;
      } else {
         return null;
      }
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      ItemStack var4 = this.getTileDataItemStack(var1, var2, var3);
      return var4 != null ? var4 : new ItemStack(Items.BANNER);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      ItemStack var6 = this.getTileDataItemStack(var1, var2, var3);
      if (var6 != null) {
         spawnAsEntity(var1, var2, var6);
      } else {
         super.dropBlockAsItemWithChance(var1, var2, var3, var4, var5);
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return !this.hasInvalidNeighbor(var1, var2) && super.canPlaceBlockAt(var1, var2);
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      if (var5 instanceof TileEntityBanner) {
         TileEntityBanner var7 = (TileEntityBanner)var5;
         ItemStack var8 = new ItemStack(Items.BANNER, 1, ((TileEntityBanner)var5).getBaseColor());
         NBTTagCompound var9 = new NBTTagCompound();
         TileEntityBanner.setBaseColorAndPatterns(var9, var7.getBaseColor(), var7.getPatterns());
         var8.setTagInfo("BlockEntityTag", var9);
         spawnAsEntity(var1, var3, var8);
      } else {
         super.harvestBlock(var1, var2, var3, var4, (TileEntity)null, var6);
      }

   }

   public static class BlockBannerHanging extends BlockBanner {
      protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 0.78125D, 1.0D);
      protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.78125D, 0.125D);
      protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 0.78125D, 1.0D);
      protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 0.78125D, 1.0D);

      public BlockBannerHanging() {
         this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
      }

      public IBlockState withRotation(IBlockState var1, Rotation var2) {
         return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
      }

      public IBlockState withMirror(IBlockState var1, Mirror var2) {
         return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
      }

      public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
         switch((EnumFacing)var1.getValue(FACING)) {
         case NORTH:
         default:
            return NORTH_AABB;
         case SOUTH:
            return SOUTH_AABB;
         case WEST:
            return WEST_AABB;
         case EAST:
            return EAST_AABB;
         }
      }

      public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
         EnumFacing var5 = (EnumFacing)var1.getValue(FACING);
         if (!var2.getBlockState(var3.offset(var5.getOpposite())).getMaterial().isSolid()) {
            this.dropBlockAsItem(var2, var3, var1, 0);
            var2.setBlockToAir(var3);
         }

         super.neighborChanged(var1, var2, var3, var4);
      }

      public IBlockState getStateFromMeta(int var1) {
         EnumFacing var2 = EnumFacing.getFront(var1);
         if (var2.getAxis() == EnumFacing.Axis.Y) {
            var2 = EnumFacing.NORTH;
         }

         return this.getDefaultState().withProperty(FACING, var2);
      }

      public int getMetaFromState(IBlockState var1) {
         return ((EnumFacing)var1.getValue(FACING)).getIndex();
      }

      protected BlockStateContainer createBlockState() {
         return new BlockStateContainer(this, new IProperty[]{FACING});
      }
   }

   public static class BlockBannerStanding extends BlockBanner {
      public BlockBannerStanding() {
         this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION, Integer.valueOf(0)));
      }

      public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
         return STANDING_AABB;
      }

      public IBlockState withRotation(IBlockState var1, Rotation var2) {
         return var1.withProperty(ROTATION, Integer.valueOf(var2.rotate(((Integer)var1.getValue(ROTATION)).intValue(), 16)));
      }

      public IBlockState withMirror(IBlockState var1, Mirror var2) {
         return var1.withProperty(ROTATION, Integer.valueOf(var2.mirrorRotation(((Integer)var1.getValue(ROTATION)).intValue(), 16)));
      }

      public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
         if (!var2.getBlockState(var3.down()).getMaterial().isSolid()) {
            this.dropBlockAsItem(var2, var3, var1, 0);
            var2.setBlockToAir(var3);
         }

         super.neighborChanged(var1, var2, var3, var4);
      }

      public IBlockState getStateFromMeta(int var1) {
         return this.getDefaultState().withProperty(ROTATION, Integer.valueOf(var1));
      }

      public int getMetaFromState(IBlockState var1) {
         return ((Integer)var1.getValue(ROTATION)).intValue();
      }

      protected BlockStateContainer createBlockState() {
         return new BlockStateContainer(this, new IProperty[]{ROTATION});
      }
   }
}
