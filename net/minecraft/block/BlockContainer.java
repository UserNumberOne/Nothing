package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockContainer extends Block implements ITileEntityProvider {
   protected BlockContainer(Material var1) {
      this(var1, var1.getMaterialMapColor());
   }

   protected BlockContainer(Material var1, MapColor var2) {
      super(var1, var2);
      this.isBlockContainer = true;
   }

   protected boolean isInvalidNeighbor(World var1, BlockPos var2, EnumFacing var3) {
      return var1.getBlockState(var2.offset(var3)).getMaterial() == Material.CACTUS;
   }

   protected boolean hasInvalidNeighbor(World var1, BlockPos var2) {
      return this.isInvalidNeighbor(var1, var2, EnumFacing.NORTH) || this.isInvalidNeighbor(var1, var2, EnumFacing.SOUTH) || this.isInvalidNeighbor(var1, var2, EnumFacing.WEST) || this.isInvalidNeighbor(var1, var2, EnumFacing.EAST);
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.INVISIBLE;
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(var1, var2, var3);
      var1.removeTileEntity(var2);
   }

   public boolean eventReceived(IBlockState var1, World var2, BlockPos var3, int var4, int var5) {
      super.eventReceived(var1, var2, var3, var4, var5);
      TileEntity var6 = var2.getTileEntity(var3);
      return var6 == null ? false : var6.receiveClientEvent(var4, var5);
   }
}
