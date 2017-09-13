package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
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
import net.minecraftforge.common.IPlantable;

public class BlockCrops extends BlockBush implements IGrowable {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
   private static final AxisAlignedBB[] CROPS_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};

   protected BlockCrops() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(this.getAgeProperty(), Integer.valueOf(0)));
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
      this.setHardness(0.0F);
      this.setSoundType(SoundType.PLANT);
      this.disableStats();
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return CROPS_AABB[((Integer)state.getValue(this.getAgeProperty())).intValue()];
   }

   protected boolean canSustainBush(IBlockState var1) {
      return state.getBlock() == Blocks.FARMLAND;
   }

   protected PropertyInteger getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   protected int getAge(IBlockState var1) {
      return ((Integer)state.getValue(this.getAgeProperty())).intValue();
   }

   public IBlockState withAge(int var1) {
      return this.getDefaultState().withProperty(this.getAgeProperty(), Integer.valueOf(age));
   }

   public boolean isMaxAge(IBlockState var1) {
      return ((Integer)state.getValue(this.getAgeProperty())).intValue() >= this.getMaxAge();
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      super.updateTick(worldIn, pos, state, rand);
      if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
         int i = this.getAge(state);
         if (i < this.getMaxAge()) {
            float f = getGrowthChance(this, worldIn, pos);
            if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((int)(25.0F / f) + 1) == 0)) {
               worldIn.setBlockState(pos, this.withAge(i + 1), 2);
               ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
            }
         }
      }

   }

   public void grow(World var1, BlockPos var2, IBlockState var3) {
      int i = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
      int j = this.getMaxAge();
      if (i > j) {
         i = j;
      }

      worldIn.setBlockState(pos, this.withAge(i), 2);
   }

   protected int getBonemealAgeIncrease(World var1) {
      return MathHelper.getInt(worldIn.rand, 2, 5);
   }

   protected static float getGrowthChance(Block var0, World var1, BlockPos var2) {
      float f = 1.0F;
      BlockPos blockpos = pos.down();

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            float f1 = 0.0F;
            IBlockState iblockstate = worldIn.getBlockState(blockpos.add(i, 0, j));
            if (iblockstate.getBlock().canSustainPlant(iblockstate, worldIn, blockpos.add(i, 0, j), EnumFacing.UP, (IPlantable)blockIn)) {
               f1 = 1.0F;
               if (iblockstate.getBlock().isFertile(worldIn, blockpos.add(i, 0, j))) {
                  f1 = 3.0F;
               }
            }

            if (i != 0 || j != 0) {
               f1 /= 4.0F;
            }

            f += f1;
         }
      }

      BlockPos blockpos1 = pos.north();
      BlockPos blockpos2 = pos.south();
      BlockPos blockpos3 = pos.west();
      BlockPos blockpos4 = pos.east();
      boolean flag = blockIn == worldIn.getBlockState(blockpos3).getBlock() || blockIn == worldIn.getBlockState(blockpos4).getBlock();
      boolean flag1 = blockIn == worldIn.getBlockState(blockpos1).getBlock() || blockIn == worldIn.getBlockState(blockpos2).getBlock();
      if (flag && flag1) {
         f /= 2.0F;
      } else {
         boolean flag2 = blockIn == worldIn.getBlockState(blockpos3.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.south()).getBlock() || blockIn == worldIn.getBlockState(blockpos3.south()).getBlock();
         if (flag2) {
            f /= 2.0F;
         }
      }

      return f;
   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      IBlockState soil = worldIn.getBlockState(pos.down());
      return (worldIn.getLight(pos) >= 8 || worldIn.canSeeSky(pos)) && soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this);
   }

   protected Item getSeed() {
      return Items.WHEAT_SEEDS;
   }

   protected Item getCrop() {
      return Items.WHEAT;
   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      List ret = super.getDrops(world, pos, state, fortune);
      int age = this.getAge(state);
      Random rand = world instanceof World ? ((World)world).rand : new Random();
      if (age >= this.getMaxAge()) {
         int k = 3 + fortune;

         for(int i = 0; i < 3 + fortune; ++i) {
            if (rand.nextInt(2 * this.getMaxAge()) <= age) {
               ret.add(new ItemStack(this.getSeed(), 1, 0));
            }
         }
      }

      return ret;
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return this.isMaxAge(state) ? this.getCrop() : this.getSeed();
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this.getSeed());
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return !this.isMaxAge(state);
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return true;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.grow(worldIn, pos, state);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.withAge(meta);
   }

   public int getMetaFromState(IBlockState var1) {
      return this.getAge(state);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
