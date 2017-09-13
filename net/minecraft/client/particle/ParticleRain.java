package net.minecraft.client.particle;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleRain extends Particle {
   protected ParticleRain(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
      this.motionX *= 0.30000001192092896D;
      this.motionY = Math.random() * 0.20000000298023224D + 0.10000000149011612D;
      this.motionZ *= 0.30000001192092896D;
      this.particleRed = 1.0F;
      this.particleGreen = 1.0F;
      this.particleBlue = 1.0F;
      this.setParticleTextureIndex(19 + this.rand.nextInt(4));
      this.setSize(0.01F, 0.01F);
      this.particleGravity = 0.06F;
      this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.motionY -= (double)this.particleGravity;
      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863D;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= 0.9800000190734863D;
      if (this.particleMaxAge-- <= 0) {
         this.setExpired();
      }

      if (this.isCollided) {
         if (Math.random() < 0.5D) {
            this.setExpired();
         }

         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

      BlockPos var1 = new BlockPos(this.posX, this.posY, this.posZ);
      IBlockState var2 = this.world.getBlockState(var1);
      Material var3 = var2.getMaterial();
      if (var3.isLiquid() || var3.isSolid()) {
         double var4;
         if (var2.getBlock() instanceof BlockLiquid) {
            var4 = (double)(1.0F - BlockLiquid.getLiquidHeightPercent(((Integer)var2.getValue(BlockLiquid.LEVEL)).intValue()));
         } else {
            var4 = var2.getBoundingBox(this.world, var1).maxY;
         }

         double var6 = (double)MathHelper.floor(this.posY) + var4;
         if (this.posY < var6) {
            this.setExpired();
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleRain(var2, var3, var5, var7);
      }
   }
}
