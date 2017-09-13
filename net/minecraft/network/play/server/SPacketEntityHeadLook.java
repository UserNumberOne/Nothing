package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketEntityHeadLook implements Packet {
   private int entityId;
   private byte yaw;

   public SPacketEntityHeadLook() {
   }

   public SPacketEntityHeadLook(Entity var1, byte var2) {
      this.entityId = var1.getEntityId();
      this.yaw = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.yaw = var1.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeByte(this.yaw);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityHeadLook(this);
   }
}
