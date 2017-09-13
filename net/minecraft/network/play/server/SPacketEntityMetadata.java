package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketEntityMetadata implements Packet {
   private int entityId;
   private List dataManagerEntries;

   public SPacketEntityMetadata() {
   }

   public SPacketEntityMetadata(int var1, EntityDataManager var2, boolean var3) {
      this.entityId = var1;
      if (var3) {
         this.dataManagerEntries = var2.getAll();
      } else {
         this.dataManagerEntries = var2.getDirty();
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.dataManagerEntries = EntityDataManager.readEntries(var1);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      EntityDataManager.writeEntries(this.dataManagerEntries, var1);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityMetadata(this);
   }
}
