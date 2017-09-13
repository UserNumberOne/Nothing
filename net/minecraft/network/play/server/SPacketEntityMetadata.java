package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEntityMetadata implements Packet {
   private int entityId;
   private List dataManagerEntries;

   public SPacketEntityMetadata() {
   }

   public SPacketEntityMetadata(int var1, EntityDataManager var2, boolean var3) {
      this.entityId = entityIdIn;
      if (sendAll) {
         this.dataManagerEntries = dataManagerIn.getAll();
      } else {
         this.dataManagerEntries = dataManagerIn.getDirty();
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = buf.readVarInt();
      this.dataManagerEntries = EntityDataManager.readEntries(buf);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityId);
      EntityDataManager.writeEntries(this.dataManagerEntries, buf);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleEntityMetadata(this);
   }

   @SideOnly(Side.CLIENT)
   public List getDataManagerEntries() {
      return this.dataManagerEntries;
   }

   @SideOnly(Side.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }
}
