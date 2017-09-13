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
      var1.scheduleUpdate(var2, this, this.tickRate(var1));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      var2.scheduleUpdate(var3, this, this.tickRate(var2));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.checkFall(var1, var2);
   }

   private void checkFall(World var1, BlockPos var2) {
      if (var1.isAirBlock(var2.down()) && BlockFalling.canFallThrough(var1.getBlockState(var2.down())) && var2.getY() >= 0) {
         boolean var3 = true;
         if (!BlockFalling.fallInstantly && var1.isAreaLoaded(var2.add(-32, -32, -32), var2.add(32, 32, 32))) {
            var1.spawnEntity(new EntityFallingBlock(var1, (double)((float)var2.getX() + 0.5F), (double)var2.getY(), (double)((float)var2.getZ() + 0.5F), this.getDefaultState()));
         } else {
            var1.setBlockToAir(var2);

            BlockPos var4;
            for(var4 = var2; var1.isAirBlock(var4) && BlockFalling.canFallThrough(var1.getBlockState(var4)) && var4.getY() > 0; var4 = var4.down()) {
               ;
            }

            if (var4.getY() > 0) {
               var1.setBlockState(var4, this.getDefaultState(), 2);
            }
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      this.teleport(var1, var2);
      return true;
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      this.teleport(var1, var2);
   }

   private void teleport(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      if (var3.getBlock() == this) {
         for(int var4 = 0; var4 < 1000; ++var4) {
            BlockPos var5 = var2.add(var1.rand.nextInt(16) - var1.rand.nextInt(16), var1.rand.nextInt(8) - var1.rand.nextInt(8), var1.rand.nextInt(16) - var1.rand.nextInt(16));
            if (var1.getBlockState(var5).getBlock().blockMaterial == Material.AIR) {
               if (var1.isRemote) {
                  for(int var6 = 0; var6 < 128; ++var6) {
                     double var7 = var1.rand.nextDouble();
                     float var9 = (var1.rand.nextFloat() - 0.5F) * 0.2F;
                     float var10 = (var1.rand.nextFloat() - 0.5F) * 0.2F;
                     float var11 = (var1.rand.nextFloat() - 0.5F) * 0.2F;
                     double var12 = (double)var5.getX() + (double)(var2.getX() - var5.getX()) * var7 + (var1.rand.nextDouble() - 0.5D) + 0.5D;
                     double var14 = (double)var5.getY() + (double)(var2.getY() - var5.getY()) * var7 + var1.rand.nextDouble() - 0.5D;
                     double var16 = (double)var5.getZ() + (double)(var2.getZ() - var5.getZ()) * var7 + (var1.rand.nextDouble() - 0.5D) + 0.5D;
                     var1.spawnParticle(EnumParticleTypes.PORTAL, var12, var14, var16, (double)var9, (double)var10, (double)var11);
                  }
               } else {
                  var1.setBlockState(var5, var3, 2);
                  var1.setBlockToAir(var2);
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
