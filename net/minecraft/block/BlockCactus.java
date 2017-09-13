package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockCactus extends Block {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   protected static final AxisAlignedBB CACTUS_COLLISION_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.9375D, 0.9375D);
   protected static final AxisAlignedBB CACTUS_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

   protected BlockCactus() {
      super(Material.CACTUS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      BlockPos var5 = var2.up();
      if (var1.isAirBlock(var5)) {
         int var6;
         for(var6 = 1; var1.getBlockState(var2.down(var6)).getBlock() == this; ++var6) {
            ;
         }

         if (var6 < 3) {
            int var7 = ((Integer)var3.getValue(AGE)).intValue();
            if (var7 == 15) {
               IBlockState var8 = var3.withProperty(AGE, Integer.valueOf(0));
               CraftEventFactory.handleBlockGrowEvent(var1, var5.getX(), var5.getY(), var5.getZ(), this, 0);
               var1.setBlockState(var2, var8, 4);
               var8.neighborChanged(var1, var5, this);
            } else {
               var1.setBlockState(var2, var3.withProperty(AGE, Integer.valueOf(var7 + 1)), 4);
            }
         }
      }

   }

   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return CACTUS_COLLISION_AABB;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) ? this.canBlockStay(var1, var2) : false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canBlockStay(var2, var3)) {
         var2.destroyBlock(var3, true);
      }

   }

   public boolean canBlockStay(World var1, BlockPos var2) {
      for(EnumFacing var4 : EnumFacing.Plane.HORIZONTAL) {
         Material var5 = var1.getBlockState(var2.offset(var4)).getMaterial();
         if (var5.isSolid() || var5 == Material.LAVA) {
            return false;
         }
      }

      Block var6 = var1.getBlockState(var2.down()).getBlock();
      if (var6 == Blocks.CACTUS || var6 == Blocks.SAND && !var1.getBlockState(var2.up()).getMaterial().isLiquid()) {
         return true;
      } else {
         return false;
      }
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      CraftEventFactory.blockDamage = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
      var4.attackEntityFrom(DamageSource.cactus, 1.0F);
      CraftEventFactory.blockDamage = null;
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
