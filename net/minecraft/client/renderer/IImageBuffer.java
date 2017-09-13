package net.minecraft.client.renderer;

import java.awt.image.BufferedImage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IImageBuffer {
   BufferedImage parseUserSkin(BufferedImage var1);

   void skinAvailable();
}
