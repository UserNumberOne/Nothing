package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGravel extends BlockFalling {
   @Nullable
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      if (fortune > 3) {
         fortune = 3;
      }

      return rand.nextInt(10 - fortune * 3) == 0 ? Items.FLINT : Item.getItemFromBlock(this);
   }

   public MapColor getMapColor(IBlockState state) {
      return MapColor.STONE;
   }

   @SideOnly(Side.CLIENT)
   public int getDustColor(IBlockState p_189876_1_) {
      return -8356741;
   }
}
