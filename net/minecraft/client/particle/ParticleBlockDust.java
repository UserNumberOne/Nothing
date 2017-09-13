package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBlockDust extends ParticleDigging {
   protected ParticleBlockDust(World var1, double var2, double var4, double var6, double var8, double var10, double var12, IBlockState var14) {
      super(var1, var2, var4, var6, var8, var10, var12, var14);
      this.motionX = var8;
      this.motionY = var10;
      this.motionZ = var12;
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         IBlockState var16 = Block.getStateById(var15[0]);
         return var16.getRenderType() == EnumBlockRenderType.INVISIBLE ? null : (new ParticleBlockDust(var2, var3, var5, var7, var9, var11, var13, var16)).init();
      }
   }
}
