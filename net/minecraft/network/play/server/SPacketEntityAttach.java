package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEntityAttach implements Packet {
   private int entityId;
   private int vehicleEntityId;

   public SPacketEntityAttach() {
   }

   public SPacketEntityAttach(Entity var1, @Nullable Entity var2) {
      this.entityId = entityIn.getEntityId();
      this.vehicleEntityId = vehicleIn != null ? vehicleIn.getEntityId() : -1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = buf.readInt();
      this.vehicleEntityId = buf.readInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeInt(this.entityId);
      buf.writeInt(this.vehicleEntityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleEntityAttach(this);
   }

   @SideOnly(Side.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }

   @SideOnly(Side.CLIENT)
   public int getVehicleEntityId() {
      return this.vehicleEntityId;
   }
}
