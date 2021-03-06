package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMinecartMobSpawner extends RenderMinecart {
   public RenderMinecartMobSpawner(RenderManager var1) {
      super(var1);
   }

   protected void renderCartContents(EntityMinecartMobSpawner var1, float var2, IBlockState var3) {
      super.renderCartContents(var1, var2, var3);
   }
}
