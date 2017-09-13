package net.minecraft.client.renderer.block.model;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

@SideOnly(Side.CLIENT)
public class BlockPartRotation {
   public final Vector3f origin;
   public final EnumFacing.Axis axis;
   public final float angle;
   public final boolean rescale;

   public BlockPartRotation(Vector3f var1, EnumFacing.Axis var2, float var3, boolean var4) {
      this.origin = originIn;
      this.axis = axisIn;
      this.angle = angleIn;
      this.rescale = rescaleIn;
   }
}
