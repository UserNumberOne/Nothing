package net.minecraft.client.renderer.culling;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ICamera {
   boolean isBoundingBoxInFrustum(AxisAlignedBB var1);

   void setPosition(double var1, double var3, double var5);
}
