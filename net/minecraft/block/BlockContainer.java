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
      this(materialIn, materialIn.getMaterialMapColor());
   }

   protected BlockContainer(Material var1, MapColor var2) {
      super(materialIn, color);
      this.isBlockContainer = true;
   }

   protected boolean isInvalidNeighbor(World var1, BlockPos var2, EnumFacing var3) {
      return worldIn.getBlockState(pos.offset(facing)).getMaterial() == Material.CACTUS;
   }

   protected boolean hasInvalidNeighbor(World var1, BlockPos var2) {
      return this.isInvalidNeighbor(worldIn, pos, EnumFacing.NORTH) || this.isInvalidNeighbor(worldIn, pos, EnumFacing.SOUTH) || this.isInvalidNeighbor(worldIn, pos, EnumFacing.WEST) || this.isInvalidNeighbor(worldIn, pos, EnumFacing.EAST);
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.INVISIBLE;
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(worldIn, pos, state);
      worldIn.removeTileEntity(pos);
   }

   public boolean eventReceived(IBlockState var1, World var2, BlockPos var3, int var4, int var5) {
      super.eventReceived(state, worldIn, pos, id, param);
      TileEntity tileentity = worldIn.getTileEntity(pos);
      return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
   }
}
