package net.minecraft.client.renderer;

import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class StitcherException extends RuntimeException {
   private final Stitcher.Holder holder;

   public StitcherException(Stitcher.Holder var1, String var2) {
      super(var2);
      this.holder = var1;
   }
}
