package net.minecraft.network.play.client;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketCustomPayload implements Packet {
   private String channel;
   private PacketBuffer data;

   public CPacketCustomPayload() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketCustomPayload(String var1, PacketBuffer var2) {
      this.channel = var1;
      this.data = var2;
      if (var2.writerIndex() > 32767) {
         throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
      }
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.channel = var1.readString(20);
      int var2 = var1.readableBytes();
      if (var2 >= 0 && var2 <= 32767) {
         this.data = new PacketBuffer(var1.readBytes(var2));
      } else {
         throw new IOException("Payload may not be larger than 32767 bytes");
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

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processCustomPayload(this);
      if (this.data != null) {
         this.data.release();
      }

   }

   public String getChannelName() {
      return this.channel;
   }

   public PacketBuffer getBufferData() {
      return this.data;
   }
}
