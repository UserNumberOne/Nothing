package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDragonEgg extends Block {
   protected static final AxisAlignedBB DRAGON_EGG_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

   public BlockDragonEgg() {
      super(Material.DRAGON_EGG, MapColor.BLACK);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return DRAGON_EGG_AABB;
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.checkFall(worldIn, pos);
   }

   private void checkFall(World var1, BlockPos var2) {
      if (worldIn.isAirBlock(pos.down()) && BlockFalling.canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0) {
         int i = 32;
         if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
            worldIn.spawnEntity(new EntityFallingBlock(worldIn, (double)((float)pos.getX() + 0.5F), (double)pos.getY(), (double)((float)pos.getZ() + 0.5F), this.getDefaultState()));
         } else {
            worldIn.setBlockToAir(pos);

            BlockPos blockpos;
            for(blockpos = pos; worldIn.isAirBlock(blockpos) && BlockFalling.canFallThrough(worldIn.getBlockState(blockpos)) && blockpos.getY() > 0; blockpos = blockpos.down()) {
               ;
            }

            if (blockpos.getY() > 0) {
               worldIn.setBlockState(blockpos, this.getDefaultState(), 2);
            }
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      this.teleport(worldIn, pos);
      return true;
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      this.teleport(worldIn, pos);
   }

   private void teleport(World var1, BlockPos var2) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == this) {
         for(int i = 0; i < 1000; ++i) {
            BlockPos blockpos = pos.add(worldIn.rand.nextInt(16) - worldIn.rand.nextInt(16), worldIn.rand.nextInt(8) - worldIn.rand.nextInt(8), worldIn.rand.nextInt(16) - worldIn.rand.nextInt(16));
            if (worldIn.getBlockState(blockpos).getBlock().blockMaterial == Material.AIR) {
               if (worldIn.isRemote) {
                  for(int j = 0; j < 128; ++j) {
                     double d0 = worldIn.rand.nextDouble();
                     float f = (worldIn.rand.nextFloat() - 0.5F) * 0.2F;
                     float f1 = (worldIn.rand.nextFloat() - 0.5F) * 0.2F;
                     float f2 = (worldIn.rand.nextFloat() - 0.5F) * 0.2F;
                     double d1 = (double)blockpos.getX() + (double)(pos.getX() - blockpos.getX()) * d0 + (worldIn.rand.nextDouble() - 0.5D) + 0.5D;
                     double d2 = (double)blockpos.getY() + (double)(pos.getY() - blockpos.getY()) * d0 + worldIn.rand.nextDouble() - 0.5D;
                     double d3 = (double)blockpos.getZ() + (double)(pos.getZ() - blockpos.getZ()) * d0 + (worldIn.rand.nextDouble() - 0.5D) + 0.5D;
                     worldIn.spawnParticle(EnumParticleTypes.PORTAL, d1, d2, d3, (double)f, (double)f1, (double)f2);
                  }
               } else {
                  worldIn.setBlockState(blockpos, iblockstate, 2);
                  worldIn.setBlockToAir(pos);
               }

               return;
            }
         }
      }

   }

   public int tickRate(World var1) {
      return 5;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return true;
   }
}
