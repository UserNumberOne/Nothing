package net.minecraft.network.play.server;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketCustomPayload implements Packet {
   private String channel;
   private PacketBuffer data;

   public SPacketCustomPayload() {
   }

   public SPacketCustomPayload(String var1, PacketBuffer var2) {
      this.channel = channelIn;
      this.data = bufIn;
      if (bufIn.writerIndex() > 1048576) {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.channel = buf.readString(20);
      int i = buf.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.data = new PacketBuffer(buf.readBytes(i));
      } else {
         throw new IOException("Payload may not be larger than 1048576 bytes");
      }
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(this.channel);
      synchronized(this.data) {
         this.data.markReaderIndex();
         buf.writeBytes((ByteBuf)this.data);
         this.data.resetReaderIndex();
      }
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleCustomPayload(this);
   }

   @SideOnly(Side.CLIENT)
   public String getChannelName() {
      return this.channel;
   }

   @SideOnly(Side.CLIENT)
   public PacketBuffer getBufferData() {
      return this.data;
   }
}
