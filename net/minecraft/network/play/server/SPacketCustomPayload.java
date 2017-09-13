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
      this.channel = var1;
      this.data = var2;
      if (var2.writerIndex() > 1048576) {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.channel = var1.readString(20);
      int var2 = var1.readableBytes();
      if (var2 >= 0 && var2 <= 1048576) {
         this.data = new PacketBuffer(var1.readBytes(var2));
      } else {
         throw new IOException("Payload may not be larger than 1048576 bytes");
      }
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeString(this.channel);
      synchronized(this.data) {
         this.data.markReaderIndex();
         var1.writeBytes((ByteBuf)this.data);
         this.data.resetReaderIndex();
      }
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleCustomPayload(this);
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
