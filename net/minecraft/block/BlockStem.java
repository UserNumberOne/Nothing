package net.minecraft.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class BlockStem extends BlockBush implements IGrowable {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
   public static final PropertyDirection FACING = BlockTorch.FACING;
   private final Block crop;
   protected static final AxisAlignedBB[] STEM_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.125D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.25D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.625D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.75D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.875D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D)};

   protected BlockStem(Block var1) {
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(FACING, EnumFacing.UP));
      this.crop = crop;
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return STEM_AABB[((Integer)state.getValue(AGE)).intValue()];
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      int i = ((Integer)state.getValue(AGE)).intValue();
      state = state.withProperty(FACING, EnumFacing.UP);

      for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
         if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() == this.crop && i == 7) {
            state = state.withProperty(FACING, enumfacing);
            break;
         }
      }

      return state;
   }

   protected boolean canSustainBush(IBlockState var1) {
      return state.getBlock() == Blocks.FARMLAND;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      super.updateTick(worldIn, pos, state, rand);
      if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
         float f = BlockCrops.getGrowthChance(this, worldIn, pos);
         if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((int)(25.0F / f) + 1) == 0)) {
            int i = ((Integer)state.getValue(AGE)).intValue();
            if (i < 7) {
               state = state.withProperty(AGE, Integer.valueOf(i + 1));
               worldIn.setBlockState(pos, state, 2);
            } else {
               for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                  if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() == this.crop) {
                     return;
                  }
               }

               pos = pos.offset(EnumFacing.Plane.HORIZONTAL.random(rand));
               IBlockState soil = worldIn.getBlockState(pos.down());
               Block block = soil.getBlock();
               if (worldIn.isAirBlock(pos) && (block.canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this) || block == Blocks.DIRT || block == Blocks.GRASS)) {
                  worldIn.setBlockState(pos, this.crop.getDefaultState());
               }
            }

            ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
         }
      }

   }

   public void growStem(World var1, BlockPos var2, IBlockState var3) {
      int i = ((Integer)state.getValue(AGE)).intValue() + MathHelper.getInt(worldIn.rand, 2, 5);
      worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(Math.min(7, i))), 2);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      List ret = new ArrayList();
      Item item = this.getSeedItem();
      if (item != null) {
         int i = ((Integer)state.getValue(AGE)).intValue();

         for(int j = 0; j < 3; ++j) {
            if (RANDOM.nextInt(15) <= i) {
               ret.add(new ItemStack(item));
            }
         }
      }

      return ret;
   }

   @Nullable
   protected Item getSeedItem() {
      return this.crop == Blocks.PUMPKIN ? Items.PUMPKIN_SEEDS : (this.crop == Blocks.MELON_BLOCK ? Items.MELON_SEEDS : null);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      Item item = this.getSeedItem();
      return item == null ? null : new ItemStack(item);
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return ((Integer)state.getValue(AGE)).intValue() != 7;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return true;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.growStem(worldIn, pos, state);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE, FACING});
   }
}
