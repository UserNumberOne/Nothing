package net.minecraft.client.particle;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDrip extends Particle {
   private final Material materialType;
   private int bobTimer;

   protected ParticleDrip(World var1, double var2, double var4, double var6, Material var8) {
      super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      if (var8 == Material.WATER) {
         this.particleRed = 0.0F;
         this.particleGreen = 0.0F;
         this.particleBlue = 1.0F;
      } else {
         this.particleRed = 1.0F;
         this.particleGreen = 0.0F;
         this.particleBlue = 0.0F;
      }

      this.setParticleTextureIndex(113);
      this.setSize(0.01F, 0.01F);
      this.particleGravity = 0.06F;
      this.materialType = var8;
      this.bobTimer = 40;
      this.particleMaxAge = (int)(64.0D / (Math.random() * 0.8D + 0.2D));
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
   }

   public int getBrightnessForRender(float var1) {
      return this.materialType == Material.WATER ? super.getBrightnessForRender(var1) : 257;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.materialType == Material.WATER) {
         this.particleRed = 0.2F;
         this.particleGreen = 0.3F;
         this.particleBlue = 1.0F;
      } else {
         this.particleRed = 1.0F;
         this.particleGreen = 16.0F / (float)(40 - this.bobTimer + 16);
         this.particleBlue = 4.0F / (float)(40 - this.bobTimer + 8);
      }

      this.motionY -= (double)this.particleGravity;
      if (this.bobTimer-- > 0) {
         this.motionX *= 0.02D;
         this.motionY *= 0.02D;
         this.motionZ *= 0.02D;
         this.setParticleTextureIndex(113);
      } else {
         this.setParticleTextureIndex(112);
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863D;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= 0.9800000190734863D;
      if (this.particleMaxAge-- <= 0) {
         this.setExpired();
      }

      if (this.isCollided) {
         if (this.materialType == Material.WATER) {
            this.setExpired();
            this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
         } else {
            this.setParticleTextureIndex(114);
         }

         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

      BlockPos var1 = new BlockPos(this.posX, this.posY, this.posZ);
      IBlockState var2 = this.world.getBlockState(var1);
      Material var3 = var2.getMaterial();
      if (var3.isLiquid() || var3.isSolid()) {
         double var4 = 0.0D;
         if (var2.getBlock() instanceof BlockLiquid) {
            var4 = (double)BlockLiquid.getLiquidHeightPercent(((Integer)var2.getValue(BlockLiquid.LEVEL)).intValue());
         }

         double var6 = (double)(MathHelper.floor(this.posY) + 1) - var4;
         if (this.posY < var6) {
            this.setExpired();
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public static class LavaFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleDrip(var2, var3, var5, var7, Material.LAVA);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class WaterFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleDrip(var2, var3, var5, var7, Material.WATER);
      }
   }
}
