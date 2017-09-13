package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.EnumFacing;

public abstract class BlockHorizontal extends Block {
   public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

   protected BlockHorizontal(Material var1) {
      super(var1);
   }

   protected BlockHorizontal(Material var1, MapColor var2) {
      super(var1, var2);
   }
}
