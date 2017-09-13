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
      this.threshold = thresholdIn;
      this.deflater = new Deflater();
   }

   protected void encode(ChannelHandlerContext var1, ByteBuf var2, ByteBuf var3) throws Exception {
      int i = p_encode_2_.readableBytes();
      PacketBuffer packetbuffer = new PacketBuffer(p_encode_3_);
      if (i < this.threshold) {
         packetbuffer.writeVarInt(0);
         packetbuffer.writeBytes(p_encode_2_);
      } else {
         byte[] abyte = new byte[i];
         p_encode_2_.readBytes(abyte);
         packetbuffer.writeVarInt(abyte.length);
         this.deflater.setInput(abyte, 0, i);
         this.deflater.finish();

         while(!this.deflater.finished()) {
            int j = this.deflater.deflate(this.buffer);
            packetbuffer.writeBytes(this.buffer, 0, j);
         }

         this.deflater.reset();
      }

   }

   public void setCompressionThreshold(int var1) {
      this.threshold = thresholdIn;
   }
}
