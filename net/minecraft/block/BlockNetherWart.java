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

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return NETHER_WART_AABB[((Integer)var1.getValue(AGE)).intValue()];
   }

   protected boolean canSustainBush(IBlockState var1) {
      return var1.getBlock() == Blocks.SOUL_SAND;
   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      return this.canSustainBush(var1.getBlockState(var2.down()));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      int var5 = ((Integer)var3.getValue(AGE)).intValue();
      if (var5 < 3 && var4.nextInt(10) == 0) {
         var3 = var3.withProperty(AGE, Integer.valueOf(var5 + 1));
         CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, this.getMetaFromState(var3));
      }

      super.updateTick(var1, var2, var3, var4);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      if (!var1.isRemote) {
         int var6 = 1;
         if (((Integer)var3.getValue(AGE)).intValue() >= 3) {
            var6 = 2 + var1.rand.nextInt(3);
            if (var5 > 0) {
               var6 += var1.rand.nextInt(var5 + 1);
            }
         }

         for(int var7 = 0; var7 < var6; ++var7) {
            spawnAsEntity(var1, var2, new ItemStack(Items.NETHER_WART));
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.NETHER_WART);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
