package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCake extends Block {
   public static final PropertyInteger BITES = PropertyInteger.create("bites", 0, 6);
   protected static final AxisAlignedBB[] CAKE_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.1875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.3125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.4375D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.5625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.6875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.8125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D)};

   protected BlockCake() {
      super(Material.CAKE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(BITES, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return CAKE_AABB[((Integer)state.getValue(BITES)).intValue()];
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getSelectedBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return state.getCollisionBoundingBox(worldIn, pos);
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      this.eatCake(worldIn, pos, state, playerIn);
      return true;
   }

   private void eatCake(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      if (player.canEat(false)) {
         player.addStat(StatList.CAKE_SLICES_EATEN);
         player.getFoodStats().addStats(2, 0.1F);
         int i = ((Integer)state.getValue(BITES)).intValue();
         if (i < 6) {
            worldIn.setBlockState(pos, state.withProperty(BITES, Integer.valueOf(i + 1)), 3);
         } else {
            worldIn.setBlockToAir(pos);
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(worldIn, pos) ? this.canBlockStay(worldIn, pos) : false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canBlockStay(worldIn, pos)) {
         worldIn.setBlockToAir(pos);
      }

   }

   private boolean canBlockStay(World var1, BlockPos var2) {
      return worldIn.getBlockState(pos.down()).getMaterial().isSolid();
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.CAKE);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(BITES, Integer.valueOf(meta));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(BITES)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{BITES});
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return (7 - ((Integer)blockState.getValue(BITES)).intValue()) * 2;
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }
}
