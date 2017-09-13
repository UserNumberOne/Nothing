package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptingEncoder extends MessageToByteEncoder {
   private final NettyEncryptionTranslator encryptionCodec;

   public NettyEncryptingEncoder(Cipher var1) {
      this.encryptionCodec = new NettyEncryptionTranslator(cipher);
   }

   protected void encode(ChannelHandlerContext var1, ByteBuf var2, ByteBuf var3) throws ShortBufferException, Exception {
      this.encryptionCodec.cipher(p_encode_2_, p_encode_3_);
   }
}
