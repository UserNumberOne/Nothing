package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFalling extends Block {
   public static boolean fallInstantly;

   public BlockFalling() {
      super(Material.SAND);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public BlockFalling(Material var1) {
      super(var1);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      var1.scheduleUpdate(var2, this, this.tickRate(var1));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      var2.scheduleUpdate(var3, this, this.tickRate(var2));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote) {
         this.checkFallable(var1, var2);
      }

   }

   private void checkFallable(World var1, BlockPos var2) {
      if ((var1.isAirBlock(var2.down()) || canFallThrough(var1.getBlockState(var2.down()))) && var2.getY() >= 0) {
         boolean var3 = true;
         if (!fallInstantly && var1.isAreaLoaded(var2.add(-32, -32, -32), var2.add(32, 32, 32))) {
            if (!var1.isRemote) {
               EntityFallingBlock var6 = new EntityFallingBlock(var1, (double)var2.getX() + 0.5D, (double)var2.getY(), (double)var2.getZ() + 0.5D, var1.getBlockState(var2));
               this.onStartFalling(var6);
               var1.spawnEntity(var6);
            }
         } else {
            IBlockState var4 = var1.getBlockState(var2);
            var1.setBlockToAir(var2);

            BlockPos var5;
            for(var5 = var2.down(); (var1.isAirBlock(var5) || canFallThrough(var1.getBlockState(var5))) && var5.getY() > 0; var5 = var5.down()) {
               ;
            }

            if (var5.getY() > 0) {
               var1.setBlockState(var5.up(), var4);
            }
         }
      }

   }

   protected void onStartFalling(EntityFallingBlock var1) {
   }

   public int tickRate(World var1) {
      return 2;
   }

   public static boolean canFallThrough(IBlockState var0) {
      Block var1 = var0.getBlock();
      Material var2 = var0.getMaterial();
      return var1 == Blocks.FIRE || var2 == Material.AIR || var2 == Material.WATER || var2 == Material.LAVA;
   }

   public void onEndFalling(World var1, BlockPos var2) {
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (var4.nextInt(16) == 0) {
         BlockPos var5 = var3.down();
         if (canFallThrough(var2.getBlockState(var5))) {
            double var6 = (double)((float)var3.getX() + var4.nextFloat());
            double var8 = (double)var3.getY() - 0.05D;
            double var10 = (double)((float)var3.getZ() + var4.nextFloat());
            var2.spawnParticle(EnumParticleTypes.FALLING_DUST, var6, var8, var10, 0.0D, 0.0D, 0.0D, Block.getStateId(var1));
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public int getDustColor(IBlockState var1) {
      return -16777216;
   }
}
