package net.minecraft.client.renderer.color;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IBlockColor {
   int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4);
}
