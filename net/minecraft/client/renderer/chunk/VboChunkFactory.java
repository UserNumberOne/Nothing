package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VboChunkFactory implements IRenderChunkFactory {
   public RenderChunk create(World var1, RenderGlobal var2, int var3) {
      return new RenderChunk(var1, var2, var3);
   }
}
