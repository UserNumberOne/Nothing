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
      this.entityId = var1.getEntityId();
      this.vehicleEntityId = var2 != null ? var2.getEntityId() : -1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readInt();
      this.vehicleEntityId = var1.readInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeInt(this.entityId);
      var1.writeInt(this.vehicleEntityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityAttach(this);
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
