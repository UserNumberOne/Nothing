package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketAnimation implements Packet {
   private int entityId;
   private int type;

   public SPacketAnimation() {
   }

   public SPacketAnimation(Entity var1, int var2) {
      this.entityId = var1.getEntityId();
      this.type = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.type = var1.readUnsignedByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeByte(this.type);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleAnimation(this);
   }
}
