package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class NettyCompressionEncoder extends MessageToByteEncoder {
   private final byte[] buffer = new byte[8192];
   private final Deflater deflater;
   private int threshold;

   public NettyCompressionEncoder(int var1) {
      this.threshold = var1;
      this.deflater = new Deflater();
   }

   protected void encode(ChannelHandlerContext var1, ByteBuf var2, ByteBuf var3) throws Exception {
      int var4 = var2.readableBytes();
      PacketBuffer var5 = new PacketBuffer(var3);
      if (var4 < this.threshold) {
         var5.writeVarInt(0);
         var5.writeBytes(var2);
      } else {
         byte[] var6 = new byte[var4];
         var2.readBytes(var6);
         var5.writeVarInt(var6.length);
         this.deflater.setInput(var6, 0, var4);
         this.deflater.finish();

         while(!this.deflater.finished()) {
            int var7 = this.deflater.deflate(this.buffer);
            var5.writeBytes(this.buffer, 0, var7);
         }

         this.deflater.reset();
      }

   }

   public void setCompressionThreshold(int var1) {
      this.threshold = var1;
   }
}
