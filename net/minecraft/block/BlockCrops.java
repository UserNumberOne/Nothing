package net.minecraft.block;

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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

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
      return CROPS_AABB[((Integer)var1.getValue(this.getAgeProperty())).intValue()];
   }

   protected boolean canSustainBush(IBlockState var1) {
      return var1.getBlock() == Blocks.FARMLAND;
   }

   protected PropertyInteger getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   protected int getAge(IBlockState var1) {
      return ((Integer)var1.getValue(this.getAgeProperty())).intValue();
   }

   public IBlockState withAge(int var1) {
      return this.getDefaultState().withProperty(this.getAgeProperty(), Integer.valueOf(var1));
   }

   public boolean isMaxAge(IBlockState var1) {
      return ((Integer)var1.getValue(this.getAgeProperty())).intValue() >= this.getMaxAge();
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      super.updateTick(var1, var2, var3, var4);
      if (var1.getLightFromNeighbors(var2.up()) >= 9) {
         int var5 = this.getAge(var3);
         if (var5 < this.getMaxAge()) {
            float var6 = getGrowthChance(this, var1, var2);
            if (var4.nextInt((int)(25.0F / var6) + 1) == 0) {
               IBlockState var7 = this.withAge(var5 + 1);
               CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, this.getMetaFromState(var7));
            }
         }
      }

   }

   public void grow(World var1, BlockPos var2, IBlockState var3) {
      int var4 = this.getAge(var3) + this.getBonemealAgeIncrease(var1);
      int var5 = this.getMaxAge();
      if (var4 > var5) {
         var4 = var5;
      }

      IBlockState var6 = this.withAge(var4);
      CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, this.getMetaFromState(var6));
   }

   protected int getBonemealAgeIncrease(World var1) {
      return MathHelper.getInt(var1.rand, 2, 5);
   }

   protected static float getGrowthChance(Block var0, World var1, BlockPos var2) {
      float var3 = 1.0F;
      BlockPos var4 = var2.down();

      for(int var5 = -1; var5 <= 1; ++var5) {
         for(int var6 = -1; var6 <= 1; ++var6) {
            float var7 = 0.0F;
            IBlockState var8 = var1.getBlockState(var4.add(var5, 0, var6));
            if (var8.getBlock() == Blocks.FARMLAND) {
               var7 = 1.0F;
               if (((Integer)var8.getValue(BlockFarmland.MOISTURE)).intValue() > 0) {
                  var7 = 3.0F;
               }
            }

            if (var5 != 0 || var6 != 0) {
               var7 /= 4.0F;
            }

            var3 += var7;
         }
      }

      BlockPos var12 = var2.north();
      BlockPos var13 = var2.south();
      BlockPos var14 = var2.west();
      BlockPos var15 = var2.east();
      boolean var9 = var0 == var1.getBlockState(var14).getBlock() || var0 == var1.getBlockState(var15).getBlock();
      boolean var10 = var0 == var1.getBlockState(var12).getBlock() || var0 == var1.getBlockState(var13).getBlock();
      if (var9 && var10) {
         var3 /= 2.0F;
      } else {
         boolean var11 = var0 == var1.getBlockState(var14.north()).getBlock() || var0 == var1.getBlockState(var15.north()).getBlock() || var0 == var1.getBlockState(var15.south()).getBlock() || var0 == var1.getBlockState(var14.south()).getBlock();
         if (var11) {
            var3 /= 2.0F;
         }
      }

      return var3;
   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      return (var1.getLight(var2) >= 8 || var1.canSeeSky(var2)) && this.canSustainBush(var1.getBlockState(var2.down()));
   }

   protected Item getSeed() {
      return Items.WHEAT_SEEDS;
   }

   protected Item getCrop() {
      return Items.WHEAT;
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(var1, var2, var3, var4, 0);
      if (!var1.isRemote) {
         int var6 = this.getAge(var3);
         if (var6 >= this.getMaxAge()) {
            int var7 = 3 + var5;

            for(int var8 = 0; var8 < var7; ++var8) {
               if (var1.rand.nextInt(2 * this.getMaxAge()) <= var6) {
                  spawnAsEntity(var1, var2, new ItemStack(this.getSeed()));
               }
            }
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return this.isMaxAge(var1) ? this.getCrop() : this.getSeed();
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this.getSeed());
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return !this.isMaxAge(var3);
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return true;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.grow(var1, var3, var4);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.withAge(var1);
   }

   public int getMetaFromState(IBlockState var1) {
      return this.getAge(var1);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
