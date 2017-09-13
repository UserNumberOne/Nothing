package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketConfirmTeleport implements Packet {
   private int telportId;

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.telportId = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.telportId);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processConfirmTeleport(this);
   }

   public int getTeleportId() {
      return this.telportId;
   }
}
