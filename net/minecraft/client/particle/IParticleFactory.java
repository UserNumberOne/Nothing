package net.minecraft.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IParticleFactory {
   Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15);
}
