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

   public SPacketCollectItem(int var1, int var2) {
      this.collectedItemEntityId = collectedItemEntityIdIn;
      this.entityId = entityIdIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.collectedItemEntityId = buf.readVarInt();
      this.entityId = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.collectedItemEntityId);
      buf.writeVarInt(this.entityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
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
