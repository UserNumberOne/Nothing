package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockNetherWart extends BlockBush {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
   private static final AxisAlignedBB[] NETHER_WART_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.6875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D)};

   protected BlockNetherWart() {
      super(Material.PLANTS, MapColor.RED);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return NETHER_WART_AABB[((Integer)iblockdata.getValue(AGE)).intValue()];
   }

   protected boolean canSustainBush(IBlockState iblockdata) {
      return iblockdata.getBlock() == Blocks.SOUL_SAND;
   }

   public boolean canBlockStay(World world, BlockPos blockposition, IBlockState iblockdata) {
      return this.canSustainBush(world.getBlockState(blockposition.down()));
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      int i = ((Integer)iblockdata.getValue(AGE)).intValue();
      if (i < 3 && random.nextInt(10) == 0) {
         iblockdata = iblockdata.withProperty(AGE, Integer.valueOf(i + 1));
         CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, this.getMetaFromState(iblockdata));
      }

      super.updateTick(world, blockposition, iblockdata, random);
   }

   public void dropBlockAsItemWithChance(World world, BlockPos blockposition, IBlockState iblockdata, float f, int i) {
      if (!world.isRemote) {
         int j = 1;
         if (((Integer)iblockdata.getValue(AGE)).intValue() >= 3) {
            j = 2 + world.rand.nextInt(3);
            if (i > 0) {
               j += world.rand.nextInt(i + 1);
            }
         }

         for(int k = 0; k < j; ++k) {
            spawnAsEntity(world, blockposition, new ItemStack(Items.NETHER_WART));
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return null;
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Items.NETHER_WART);
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
