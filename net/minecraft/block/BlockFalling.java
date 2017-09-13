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
      super(materialIn);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!worldIn.isRemote) {
         this.checkFallable(worldIn, pos);
      }

   }

   private void checkFallable(World var1, BlockPos var2) {
      if ((worldIn.isAirBlock(pos.down()) || canFallThrough(worldIn.getBlockState(pos.down()))) && pos.getY() >= 0) {
         int i = 32;
         if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
            if (!worldIn.isRemote) {
               EntityFallingBlock entityfallingblock = new EntityFallingBlock(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, worldIn.getBlockState(pos));
               this.onStartFalling(entityfallingblock);
               worldIn.spawnEntity(entityfallingblock);
            }
         } else {
            IBlockState state = worldIn.getBlockState(pos);
            worldIn.setBlockToAir(pos);

            BlockPos blockpos;
            for(blockpos = pos.down(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) && blockpos.getY() > 0; blockpos = blockpos.down()) {
               ;
            }

            if (blockpos.getY() > 0) {
               worldIn.setBlockState(blockpos.up(), state);
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
      Block block = state.getBlock();
      Material material = state.getMaterial();
      return block == Blocks.FIRE || material == Material.AIR || material == Material.WATER || material == Material.LAVA;
   }

   public void onEndFalling(World var1, BlockPos var2) {
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (rand.nextInt(16) == 0) {
         BlockPos blockpos = pos.down();
         if (canFallThrough(worldIn.getBlockState(blockpos))) {
            double d0 = (double)((float)pos.getX() + rand.nextFloat());
            double d1 = (double)pos.getY() - 0.05D;
            double d2 = (double)((float)pos.getZ() + rand.nextFloat());
            worldIn.spawnParticle(EnumParticleTypes.FALLING_DUST, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(stateIn));
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public int getDustColor(IBlockState var1) {
      return -16777216;
   }
}
