package net.minecraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStandingSign extends BlockSign {
   public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);

   public BlockStandingSign() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION, Integer.valueOf(0)));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.getBlockState(var3.down()).getMaterial().isSolid()) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

      super.neighborChanged(var1, var2, var3, var4);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(ROTATION, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(ROTATION)).intValue();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(ROTATION, Integer.valueOf(var2.rotate(((Integer)var1.getValue(ROTATION)).intValue(), 16)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withProperty(ROTATION, Integer.valueOf(var2.mirrorRotation(((Integer)var1.getValue(ROTATION)).intValue(), 16)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{ROTATION});
   }
}
