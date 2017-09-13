package net.minecraft.network.play.client;

import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.world.WorldServer;

public class CPacketSpectate implements Packet {
   private UUID id;

   public CPacketSpectate() {
   }

   public CPacketSpectate(UUID var1) {
      this.id = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.id = var1.readUniqueId();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeUniqueId(this.id);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.handleSpectate(this);
   }

   @Nullable
   public Entity getEntity(WorldServer var1) {
      return var1.getEntityFromUuid(this.id);
   }
}
