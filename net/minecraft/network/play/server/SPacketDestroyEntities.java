package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketDestroyEntities implements Packet {
   private int[] entityIDs;

   public SPacketDestroyEntities() {
   }

   public SPacketDestroyEntities(int... var1) {
      this.entityIDs = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityIDs = new int[var1.readVarInt()];

      for(int var2 = 0; var2 < this.entityIDs.length; ++var2) {
         this.entityIDs[var2] = var1.readVarInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityIDs.length);

      for(int var5 : this.entityIDs) {
         var1.writeVarInt(var5);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleDestroyEntities(this);
   }
}
