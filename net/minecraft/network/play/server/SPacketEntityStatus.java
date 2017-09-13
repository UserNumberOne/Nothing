package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketEntityStatus implements Packet {
   private int entityId;
   private byte logicOpcode;

   public SPacketEntityStatus() {
   }

   public SPacketEntityStatus(Entity var1, byte var2) {
      this.entityId = var1.getEntityId();
      this.logicOpcode = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readInt();
      this.logicOpcode = var1.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeInt(this.entityId);
      var1.writeByte(this.logicOpcode);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityStatus(this);
   }
}
