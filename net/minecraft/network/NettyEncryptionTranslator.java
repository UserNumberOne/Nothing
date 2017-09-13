package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptionTranslator {
   private final Cipher cipher;
   private byte[] inputBuffer = new byte[0];
   private byte[] outputBuffer = new byte[0];

   protected NettyEncryptionTranslator(Cipher var1) {
      this.cipher = var1;
   }

   private byte[] bufToBytes(ByteBuf var1) {
      int var2 = var1.readableBytes();
      if (this.inputBuffer.length < var2) {
         this.inputBuffer = new byte[var2];
      }

      var1.readBytes(this.inputBuffer, 0, var2);
      return this.inputBuffer;
   }

   protected ByteBuf decipher(ChannelHandlerContext var1, ByteBuf var2) throws ShortBufferException {
      int var3 = var2.readableBytes();
      byte[] var4 = this.bufToBytes(var2);
      ByteBuf var5 = var1.alloc().heapBuffer(this.cipher.getOutputSize(var3));
      var5.writerIndex(this.cipher.update(var4, 0, var3, var5.array(), var5.arrayOffset()));
      return var5;
   }

   protected void cipher(ByteBuf var1, ByteBuf var2) throws ShortBufferException {
      int var3 = var1.readableBytes();
      byte[] var4 = this.bufToBytes(var1);
      int var5 = this.cipher.getOutputSize(var3);
      if (this.outputBuffer.length < var5) {
         this.outputBuffer = new byte[var5];
      }

      var2.writeBytes(this.outputBuffer, 0, this.cipher.update(var4, 0, var3, this.outputBuffer));
   }
}
