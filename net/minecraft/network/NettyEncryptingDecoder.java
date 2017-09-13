package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptingDecoder extends MessageToMessageDecoder {
   private final NettyEncryptionTranslator decryptionCodec;

   public NettyEncryptingDecoder(Cipher var1) {
      this.decryptionCodec = new NettyEncryptionTranslator(cipher);
   }

   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List var3) throws ShortBufferException, Exception {
      p_decode_3_.add(this.decryptionCodec.decipher(p_decode_1_, p_decode_2_));
   }
}
