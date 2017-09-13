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
      this.entityId = entityIn.getEntityId();
      List list = entityIn.getPassengers();
      this.passengerIds = new int[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         this.passengerIds[i] = ((Entity)list.get(i)).getEntityId();
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = buf.readVarInt();
      this.passengerIds = buf.readVarIntArray();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityId);
      buf.writeVarIntArray(this.passengerIds);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleSetPassengers(this);
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
