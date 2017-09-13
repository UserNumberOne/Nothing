package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketCollectItem implements Packet {
   private int collectedItemEntityId;
   private int entityId;

   public SPacketCollectItem() {
   }

   public SPacketCollectItem(int collectedItemEntityIdIn, int entityIdIn) {
      this.collectedItemEntityId = collectedItemEntityIdIn;
      this.entityId = entityIdIn;
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.collectedItemEntityId = buf.readVarInt();
      this.entityId = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeVarInt(this.collectedItemEntityId);
      buf.writeVarInt(this.entityId);
   }

   public void processPacket(INetHandlerPlayClient handler) {
      handler.handleCollectItem(this);
   }

   @SideOnly(Side.CLIENT)
   public int getCollectedItemEntityID() {
      return this.collectedItemEntityId;
   }

   @SideOnly(Side.CLIENT)
   public int getEntityID() {
      return this.entityId;
   }
}
