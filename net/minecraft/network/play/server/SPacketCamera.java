package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketCamera implements Packet {
   public int entityId;

   public SPacketCamera() {
   }

   public SPacketCamera(Entity var1) {
      this.entityId = var1.getEntityId();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleCamera(this);
   }
}
