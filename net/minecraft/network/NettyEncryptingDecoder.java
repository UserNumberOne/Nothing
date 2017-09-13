package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;

public class NettyEncryptingDecoder extends MessageToMessageDecoder {
   private final NettyEncryptionTranslator decryptionCodec;

   public NettyEncryptingDecoder(Cipher var1) {
      this.decryptionCodec = new NettyEncryptionTranslator(var1);
   }

   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List var3) throws Exception {
      var3.add(this.decryptionCodec.decipher(var1, var2));
   }

   // $FF: synthetic method
   protected void decode(ChannelHandlerContext var1, Object var2, List var3) throws Exception {
      this.decode(var1, (ByteBuf)var2, var3);
   }
}
