package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockEndGateway extends BlockContainer {
   protected BlockEndGateway(Material var1) {
      super(var1);
      this.setLightLevel(1.0F);
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityEndGateway();
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      IBlockState var5 = var2.getBlockState(var3.offset(var4));
      Block var6 = var5.getBlock();
      return !var5.isOpaqueCube() && var6 != Blocks.END_GATEWAY;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      TileEntity var5 = var2.getTileEntity(var3);
      if (var5 instanceof TileEntityEndGateway) {
         int var6 = ((TileEntityEndGateway)var5).getParticleAmount();

         for(int var7 = 0; var7 < var6; ++var7) {
            double var8 = (double)((float)var3.getX() + var4.nextFloat());
            double var10 = (double)((float)var3.getY() + var4.nextFloat());
            double var12 = (double)((float)var3.getZ() + var4.nextFloat());
            double var14 = ((double)var4.nextFloat() - 0.5D) * 0.5D;
            double var16 = ((double)var4.nextFloat() - 0.5D) * 0.5D;
            double var18 = ((double)var4.nextFloat() - 0.5D) * 0.5D;
            int var20 = var4.nextInt(2) * 2 - 1;
            if (var4.nextBoolean()) {
               var12 = (double)var3.getZ() + 0.5D + 0.25D * (double)var20;
               var18 = (double)(var4.nextFloat() * 2.0F * (float)var20);
            } else {
               var8 = (double)var3.getX() + 0.5D + 0.25D * (double)var20;
               var14 = (double)(var4.nextFloat() * 2.0F * (float)var20);
            }

            var2.spawnParticle(EnumParticleTypes.PORTAL, var8, var10, var12, var14, var16, var18);
         }
      }

   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return null;
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.BLACK;
   }
}
