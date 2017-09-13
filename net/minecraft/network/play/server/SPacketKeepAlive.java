package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketKeepAlive implements Packet {
   private int id;

   public SPacketKeepAlive() {
   }

   public SPacketKeepAlive(int var1) {
      this.id = var1;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleKeepAlive(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.id = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.id);
   }
}
