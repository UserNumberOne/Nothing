package net.minecraft.client.renderer.texture;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.resources.IResource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

@SideOnly(Side.CLIENT)
public class PngSizeInfo {
   public final int pngWidth;
   public final int pngHeight;

   public PngSizeInfo(InputStream var1) throws IOException {
      DataInputStream var2 = new DataInputStream(var1);
      if (var2.readLong() != -8552249625308161526L) {
         throw new IOException("Bad PNG Signature");
      } else if (var2.readInt() != 13) {
         throw new IOException("Bad length for IHDR chunk!");
      } else if (var2.readInt() != 1229472850) {
         throw new IOException("Bad type for IHDR chunk!");
      } else {
         this.pngWidth = var2.readInt();
         this.pngHeight = var2.readInt();
         IOUtils.closeQuietly(var2);
      }
   }

   public static PngSizeInfo makeFromResource(IResource var0) throws IOException {
      PngSizeInfo var1;
      try {
         var1 = new PngSizeInfo(var0.getInputStream());
      } finally {
         IOUtils.closeQuietly(var0);
      }

      return var1;
   }
}
