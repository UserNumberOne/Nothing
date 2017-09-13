package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class NettyPacketDecoder extends ByteToMessageDecoder {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_RECEIVED", NetworkManager.NETWORK_PACKETS_MARKER);
   private final EnumPacketDirection direction;

   public NettyPacketDecoder(EnumPacketDirection var1) {
      this.direction = var1;
   }

   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List var3) throws IOException, InstantiationException, IllegalAccessException, Exception {
      if (var2.readableBytes() != 0) {
         PacketBuffer var4 = new PacketBuffer(var2);
         int var5 = var4.readVarInt();
         Packet var6 = ((EnumConnectionState)var1.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get()).getPacket(this.direction, var5);
         if (var6 == null) {
            throw new IOException("Bad packet id " + var5);
         }

         var6.readPacketData(var4);
         if (var4.readableBytes() > 0) {
            throw new IOException("Packet " + ((EnumConnectionState)var1.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get()).getId() + "/" + var5 + " (" + var6.getClass().getSimpleName() + ") was larger than I expected, found " + var4.readableBytes() + " bytes extra whilst reading packet " + var5);
         }

         var3.add(var6);
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(RECEIVED_PACKET_MARKER, " IN: [{}:{}] {}", new Object[]{var1.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get(), var5, var6.getClass().getName()});
         }
      }

   }
}
