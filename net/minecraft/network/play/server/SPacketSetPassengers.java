package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSetPassengers implements Packet {
   private int entityId;
   private int[] passengerIds;

   public SPacketSetPassengers() {
   }

   public SPacketSetPassengers(Entity var1) {
      this.entityId = var1.getEntityId();
      List var2 = var1.getPassengers();
      this.passengerIds = new int[var2.size()];

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         this.passengerIds[var3] = ((Entity)var2.get(var3)).getEntityId();
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.passengerIds = var1.readVarIntArray();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeVarIntArray(this.passengerIds);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSetPassengers(this);
   }

   @SideOnly(Side.CLIENT)
   public int[] getPassengerIds() {
      return this.passengerIds;
   }

   @SideOnly(Side.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }
}
