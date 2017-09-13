package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleFallingDust extends Particle {
   float oSize;
   final float rotSpeed;

   protected ParticleFallingDust(World var1, double var2, double var4, double var6, float var8, float var9, float var10) {
      super(p_i47135_1_, p_i47135_2_, p_i47135_4_, p_i47135_6_, 0.0D, 0.0D, 0.0D);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.particleRed = p_i47135_8_;
      this.particleGreen = p_i47135_9_;
      this.particleBlue = p_i47135_10_;
      float f = 0.9F;
      this.particleScale *= 0.75F;
      this.particleScale *= 0.9F;
      this.oSize = this.particleScale;
      this.particleMaxAge = (int)(32.0D / (Math.random() * 0.8D + 0.2D));
      this.particleMaxAge = (int)((float)this.particleMaxAge * 0.9F);
      this.rotSpeed = ((float)Math.random() - 0.5F) * 0.1F;
      this.particleAngle = (float)Math.random() * 6.2831855F;
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      this.particleScale = this.oSize * f;
      super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.prevParticleAngle = this.particleAngle;
      this.particleAngle += 3.1415927F * this.rotSpeed * 2.0F;
      if (this.isCollided) {
         this.prevParticleAngle = this.particleAngle = 0.0F;
      }

      this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionY -= 0.003000000026077032D;
      this.motionY = Math.max(this.motionY, -0.14000000059604645D);
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         IBlockState iblockstate = Block.getStateById(p_178902_15_[0]);
         if (iblockstate.getBlock() != Blocks.AIR && iblockstate.getRenderType() == EnumBlockRenderType.INVISIBLE) {
            return null;
         } else {
            int i = Minecraft.getMinecraft().getBlockColors().getColor(iblockstate);
            if (iblockstate.getBlock() instanceof BlockFalling) {
               i = ((BlockFalling)iblockstate.getBlock()).getDustColor(iblockstate);
            }

            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            return new ParticleFallingDust(worldIn, xCoordIn, yCoordIn, zCoordIn, f, f1, f2);
         }
      }
   }
}
