package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class NettyPacketEncoder extends MessageToByteEncoder {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_SENT", NetworkManager.NETWORK_PACKETS_MARKER);
   private final EnumPacketDirection direction;

   public NettyPacketEncoder(EnumPacketDirection var1) {
      this.direction = var1;
   }

   protected void encode(ChannelHandlerContext var1, Packet var2, ByteBuf var3) throws IOException, Exception {
      Integer var4 = ((EnumConnectionState)var1.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get()).getPacketId(this.direction, var2);
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug(RECEIVED_PACKET_MARKER, "OUT: [{}:{}] {}", new Object[]{var1.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get(), var4, var2.getClass().getName()});
      }

      if (var4 == null) {
         throw new IOException("Can't serialize unregistered packet");
      } else {
         PacketBuffer var5 = new PacketBuffer(var3);
         var5.writeVarInt(var4.intValue());

         try {
            var2.writePacketData(var5);
         } catch (Throwable var7) {
            LOGGER.error(var7);
         }

      }
   }
}
