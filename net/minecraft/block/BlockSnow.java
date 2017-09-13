package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSnow extends Block {
   public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 8);
   protected static final AxisAlignedBB[] SNOW_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};

   protected BlockSnow() {
      super(Material.SNOW);
      this.setDefaultState(this.blockState.getBaseState().withProperty(LAYERS, Integer.valueOf(1)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return SNOW_AABB[((Integer)state.getValue(LAYERS)).intValue()];
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return ((Integer)worldIn.getBlockState(pos).getValue(LAYERS)).intValue() < 5;
   }

   public boolean isFullyOpaque(IBlockState var1) {
      return ((Integer)state.getValue(LAYERS)).intValue() == 7;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      int i = ((Integer)blockState.getValue(LAYERS)).intValue() - 1;
      float f = 0.125F;
      AxisAlignedBB axisalignedbb = blockState.getBoundingBox(worldIn, pos);
      return new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.maxX, (double)((float)i * 0.125F), axisalignedbb.maxZ);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      IBlockState iblockstate = worldIn.getBlockState(pos.down());
      Block block = iblockstate.getBlock();
      return block != Blocks.ICE && block != Blocks.PACKED_ICE ? (iblockstate.getBlock().isLeaves(iblockstate, worldIn, pos.down()) ? true : (block == this && ((Integer)iblockstate.getValue(LAYERS)).intValue() >= 7 ? true : iblockstate.isOpaqueCube() && iblockstate.getMaterial().blocksMovement())) : false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      this.checkAndDropBlock(worldIn, pos, state);
   }

   private boolean checkAndDropBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!this.canPlaceBlockAt(worldIn, pos)) {
         worldIn.setBlockToAir(pos);
         return false;
      } else {
         return true;
      }
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      super.harvestBlock(worldIn, player, pos, state, te, stack);
      worldIn.setBlockToAir(pos);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.SNOWBALL;
   }

   public int quantityDropped(Random var1) {
      return 1;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11) {
         worldIn.setBlockToAir(pos);
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (side == EnumFacing.UP) {
         return true;
      } else {
         IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
         return iblockstate.getBlock() == this && ((Integer)iblockstate.getValue(LAYERS)).intValue() >= ((Integer)blockState.getValue(LAYERS)).intValue() ? true : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(LAYERS, Integer.valueOf((meta & 7) + 1));
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return ((Integer)worldIn.getBlockState(pos).getValue(LAYERS)).intValue() == 1;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(LAYERS)).intValue() - 1;
   }

   public int quantityDropped(IBlockState var1, int var2, Random var3) {
      return ((Integer)state.getValue(LAYERS)).intValue() + 1;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{LAYERS});
   }
}
