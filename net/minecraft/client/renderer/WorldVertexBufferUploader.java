package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldVertexBufferUploader {
   public void draw(VertexBuffer var1) {
      if (var1.getVertexCount() > 0) {
         VertexFormat var2 = var1.getVertexFormat();
         int var3 = var2.getNextOffset();
         ByteBuffer var4 = var1.getByteBuffer();
         List var5 = var2.getElements();

         for(int var6 = 0; var6 < var5.size(); ++var6) {
            VertexFormatElement var7 = (VertexFormatElement)var5.get(var6);
            VertexFormatElement.EnumUsage var8 = var7.getUsage();
            int var9 = var7.getType().getGlConstant();
            int var10 = var7.getIndex();
            var4.position(var2.getOffset(var6));
            var7.getUsage().preDraw(var2, var6, var3, var4);
         }

         GlStateManager.glDrawArrays(var1.getDrawMode(), 0, var1.getVertexCount());
         int var11 = 0;

         for(int var12 = var5.size(); var11 < var12; ++var11) {
            VertexFormatElement var13 = (VertexFormatElement)var5.get(var11);
            VertexFormatElement.EnumUsage var14 = var13.getUsage();
            int var15 = var13.getIndex();
            var13.getUsage().postDraw(var2, var11, var3, var4);
         }
      }

      var1.reset();
   }
}
