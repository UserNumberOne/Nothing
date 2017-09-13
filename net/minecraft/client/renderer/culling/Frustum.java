package net.minecraft.client.renderer.culling;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Frustum implements ICamera {
   private final ClippingHelper clippingHelper;
   private double xPosition;
   private double yPosition;
   private double zPosition;

   public Frustum() {
      this(ClippingHelperImpl.getInstance());
   }

   public Frustum(ClippingHelper var1) {
      this.clippingHelper = p_i46196_1_;
   }

   public void setPosition(double var1, double var3, double var5) {
      this.xPosition = p_78547_1_;
      this.yPosition = p_78547_3_;
      this.zPosition = p_78547_5_;
   }

   public boolean isBoxInFrustum(double var1, double var3, double var5, double var7, double var9, double var11) {
      return this.clippingHelper.isBoxInFrustum(p_78548_1_ - this.xPosition, p_78548_3_ - this.yPosition, p_78548_5_ - this.zPosition, p_78548_7_ - this.xPosition, p_78548_9_ - this.yPosition, p_78548_11_ - this.zPosition);
   }

   public boolean isBoundingBoxInFrustum(AxisAlignedBB var1) {
      return this.isBoxInFrustum(p_78546_1_.minX, p_78546_1_.minY, p_78546_1_.minZ, p_78546_1_.maxX, p_78546_1_.maxY, p_78546_1_.maxZ);
   }
}
