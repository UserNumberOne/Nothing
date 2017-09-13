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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockReed extends Block implements IPlantable {
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
            if (ForgeHooks.onCropsGrowPre(var1, var2, var3, true)) {
               if (var6 == 15) {
                  var1.setBlockState(var2.up(), this.getDefaultState());
                  var1.setBlockState(var2, var3.withProperty(AGE, Integer.valueOf(0)), 4);
               } else {
                  var1.setBlockState(var2, var3.withProperty(AGE, Integer.valueOf(var6 + 1)), 4);
               }

               ForgeHooks.onCropsGrowPost(var1, var2, var3, var1.getBlockState(var2));
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2.down());
      Block var4 = var3.getBlock();
      if (var4.canSustainPlant(var3, var1, var2.down(), EnumFacing.UP, this)) {
         return true;
      } else if (var4 == this) {
         return true;
      } else if (var4 != Blocks.GRASS && var4 != Blocks.DIRT && var4 != Blocks.SAND) {
         return false;
      } else {
         BlockPos var5 = var2.down();

         for(EnumFacing var7 : EnumFacing.Plane.HORIZONTAL) {
            IBlockState var8 = var1.getBlockState(var5.offset(var7));
            if (var8.getMaterial() == Material.WATER || var8.getBlock() == Blocks.FROSTED_ICE) {
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

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(AGE)).intValue();
   }

   public EnumPlantType getPlantType(IBlockAccess var1, BlockPos var2) {
      return EnumPlantType.Beach;
   }

   public IBlockState getPlant(IBlockAccess var1, BlockPos var2) {
      return this.getDefaultState();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
