package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockEndPortal extends BlockContainer {
   protected static final AxisAlignedBB END_PORTAL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

   protected BlockEndPortal(Material var1) {
      super(var1);
      this.setLightLevel(1.0F);
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityEndPortal();
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return END_PORTAL_AABB;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return var4 == EnumFacing.DOWN ? super.shouldSideBeRendered(var1, var2, var3, var4) : false;
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
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

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!var4.isRiding() && !var4.isBeingRidden() && var4.isNonBoss() && !var1.isRemote && var4.getEntityBoundingBox().intersectsWith(var3.getBoundingBox(var1, var2).offset(var2))) {
         var4.changeDimension(1);
      }

   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      double var5 = (double)((float)var3.getX() + var4.nextFloat());
      double var7 = (double)((float)var3.getY() + 0.8F);
      double var9 = (double)((float)var3.getZ() + var4.nextFloat());
      double var11 = 0.0D;
      double var13 = 0.0D;
      double var15 = 0.0D;
      var2.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var5, var7, var9, 0.0D, 0.0D, 0.0D);
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return null;
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.BLACK;
   }
}
