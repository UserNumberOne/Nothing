package net.minecraft.block;

import java.util.ArrayList;
import java.util.List;
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
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityBanner) {
         ItemStack itemstack = new ItemStack(Items.BANNER, 1, ((TileEntityBanner)tileentity).getBaseColor());
         NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
         nbttagcompound.removeTag("x");
         nbttagcompound.removeTag("y");
         nbttagcompound.removeTag("z");
         nbttagcompound.removeTag("id");
         itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
         return itemstack;
      } else {
         return null;
      }
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      ItemStack itemstack = this.getTileDataItemStack(worldIn, pos, state);
      return itemstack != null ? itemstack : new ItemStack(Items.BANNER);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return !this.hasInvalidNeighbor(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos);
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      if (te instanceof TileEntityBanner) {
         TileEntityBanner tileentitybanner = (TileEntityBanner)te;
         ItemStack itemstack = new ItemStack(Items.BANNER, 1, ((TileEntityBanner)te).getBaseColor());
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         TileEntityBanner.setBaseColorAndPatterns(nbttagcompound, tileentitybanner.getBaseColor(), tileentitybanner.getPatterns());
         itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
         spawnAsEntity(worldIn, pos, itemstack);
      } else {
         super.harvestBlock(worldIn, player, pos, state, (TileEntity)null, stack);
      }

   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      TileEntity te = world.getTileEntity(pos);
      List ret = new ArrayList();
      if (te instanceof TileEntityBanner) {
         TileEntityBanner banner = (TileEntityBanner)te;
         ItemStack item = new ItemStack(Items.BANNER, 1, banner.getBaseColor());
         NBTTagCompound nbt = new NBTTagCompound();
         TileEntityBanner.setBaseColorAndPatterns(nbt, banner.getBaseColor(), banner.getPatterns());
         item.setTagInfo("BlockEntityTag", nbt);
         ret.add(item);
      } else {
         ret.add(new ItemStack(Items.BANNER, 1, 0));
      }

      return ret;
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
         return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
      }

      public IBlockState withMirror(IBlockState var1, Mirror var2) {
         return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
      }

      public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
         switch((EnumFacing)state.getValue(FACING)) {
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
         EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
         if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getMaterial().isSolid()) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
         }

         super.neighborChanged(state, worldIn, pos, blockIn);
      }

      public IBlockState getStateFromMeta(int var1) {
         EnumFacing enumfacing = EnumFacing.getFront(meta);
         if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
         }

         return this.getDefaultState().withProperty(FACING, enumfacing);
      }

      public int getMetaFromState(IBlockState var1) {
         return ((EnumFacing)state.getValue(FACING)).getIndex();
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
         return state.withProperty(ROTATION, Integer.valueOf(rot.rotate(((Integer)state.getValue(ROTATION)).intValue(), 16)));
      }

      public IBlockState withMirror(IBlockState var1, Mirror var2) {
         return state.withProperty(ROTATION, Integer.valueOf(mirrorIn.mirrorRotation(((Integer)state.getValue(ROTATION)).intValue(), 16)));
      }

      public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
         if (!worldIn.getBlockState(pos.down()).getMaterial().isSolid()) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
         }

         super.neighborChanged(state, worldIn, pos, blockIn);
      }

      public IBlockState getStateFromMeta(int var1) {
         return this.getDefaultState().withProperty(ROTATION, Integer.valueOf(meta));
      }

      public int getMetaFromState(IBlockState var1) {
         return ((Integer)state.getValue(ROTATION)).intValue();
      }

      protected BlockStateContainer createBlockState() {
         return new BlockStateContainer(this, new IProperty[]{ROTATION});
      }
   }
}
