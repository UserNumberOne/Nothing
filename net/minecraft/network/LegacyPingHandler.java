package net.minecraft.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import net.minecraft.src.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyPingHandler extends ChannelInboundHandlerAdapter {
   private static final Logger LOGGER = LogManager.getLogger();
   private final NetworkSystem networkSystem;

   public LegacyPingHandler(NetworkSystem var1) {
      this.networkSystem = var1;
   }

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      ByteBuf var3 = (ByteBuf)var2;
      var3.markReaderIndex();
      boolean var4 = true;

      try {
         if (var3.readUnsignedByte() == 254) {
            InetSocketAddress var5 = (InetSocketAddress)var1.channel().remoteAddress();
            MinecraftServer var6 = this.networkSystem.d();
            int var7 = var3.readableBytes();
            switch(var7) {
            case 0:
               LOGGER.debug("Ping: (<1.3.x) from {}:{}", new Object[]{var5.getAddress(), var5.getPort()});
               String var23 = String.format("%s§%d§%d", var6.getMotd(), var6.H(), var6.I());
               this.writeAndFlush(var1, this.getStringBuffer(var23));
               break;
            case 1:
               if (var3.readUnsignedByte() != 1) {
                  return;
               }

               LOGGER.debug("Ping: (1.4-1.5.x) from {}:{}", new Object[]{var5.getAddress(), var5.getPort()});
               String var8 = String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", Integer.valueOf(127), var6.getVersion(), var6.getMotd(), var6.H(), var6.I());
               this.writeAndFlush(var1, this.getStringBuffer(var8));
               break;
            default:
               boolean var24 = var3.readUnsignedByte() == 1;
               var24 = var24 & var3.readUnsignedByte() == 250;
               var24 = var24 & "MC|PingHost".equals(new String(var3.readBytes(var3.readShort() * 2).array(), Charsets.UTF_16BE));
               int var9 = var3.readUnsignedShort();
               var24 = var24 & var3.readUnsignedByte() >= 73;
               var24 = var24 & 3 + var3.readBytes(var3.readShort() * 2).array().length + 4 == var9;
               var24 = var24 & var3.readInt() <= 65535;
               var24 = var24 & var3.readableBytes() == 0;
               if (!var24) {
                  return;
               }

               LOGGER.debug("Ping: (1.6) from {}:{}", new Object[]{var5.getAddress(), var5.getPort()});
               String var10 = String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", Integer.valueOf(127), var6.getVersion(), var6.getMotd(), var6.H(), var6.I());
               ByteBuf var11 = this.getStringBuffer(var10);

               try {
                  this.writeAndFlush(var1, var11);
               } finally {
                  var11.release();
               }
            }

            var3.release();
            var4 = false;
            return;
         }
      } catch (RuntimeException var21) {
         return;
      } finally {
         if (var4) {
            var3.resetReaderIndex();
            var1.channel().pipeline().remove("legacy_query");
            var1.fireChannelRead(var2);
         }

      }

   }

   private void writeAndFlush(ChannelHandlerContext var1, ByteBuf var2) {
      var1.pipeline().firstContext().writeAndFlush(var2).addListener(ChannelFutureListener.CLOSE);
   }

   private ByteBuf getStringBuffer(String var1) {
      ByteBuf var2 = Unpooled.buffer();
      var2.writeByte(255);
      char[] var3 = var1.toCharArray();
      var2.writeShort(var3.length);

      for(char var7 : var3) {
         var2.writeChar(var7);
      }

      return var2;
   }
}
