package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockReed extends Block {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   protected static final AxisAlignedBB REED_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);

   protected BlockReed() {
      super(Material.PLANTS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return REED_AABB;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if ((var1.getBlockState(var2.down()).getBlock() == Blocks.REEDS || this.checkForDrop(var1, var2, var3)) && var1.isAirBlock(var2.up())) {
         int var5;
         for(var5 = 1; var1.getBlockState(var2.down(var5)).getBlock() == this; ++var5) {
            ;
         }

         if (var5 < 3) {
            int var6 = ((Integer)var3.getValue(AGE)).intValue();
            if (var6 == 15) {
               BlockPos var7 = var2.up();
               CraftEventFactory.handleBlockGrowEvent(var1, var7.getX(), var7.getY(), var7.getZ(), this, 0);
               var1.setBlockState(var2, var3.withProperty(AGE, Integer.valueOf(0)), 4);
            } else {
               var1.setBlockState(var2, var3.withProperty(AGE, Integer.valueOf(var6 + 1)), 4);
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      Block var3 = var1.getBlockState(var2.down()).getBlock();
      if (var3 == this) {
         return true;
      } else if (var3 != Blocks.GRASS && var3 != Blocks.DIRT && var3 != Blocks.SAND) {
         return false;
      } else {
         BlockPos var4 = var2.down();

         for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
            IBlockState var7 = var1.getBlockState(var4.offset(var6));
            if (var7.getMaterial() == Material.WATER || var7.getBlock() == Blocks.FROSTED_ICE) {
               return true;
            }
         }

         return false;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      this.checkForDrop(var2, var3, var1);
   }

   protected final boolean checkForDrop(World var1, BlockPos var2, IBlockState var3) {
      if (this.canBlockStay(var1, var2)) {
         return true;
      } else {
         this.dropBlockAsItem(var1, var2, var3, 0);
         var1.setBlockToAir(var2);
         return false;
      }
   }

   public boolean canBlockStay(World var1, BlockPos var2) {
      return this.canPlaceBlockAt(var1, var2);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.REEDS;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.REEDS);
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
