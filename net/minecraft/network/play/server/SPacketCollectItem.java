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
      this.collectedItemEntityId = var1;
      this.entityId = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.collectedItemEntityId = var1.readVarInt();
      this.entityId = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.collectedItemEntityId);
      var1.writeVarInt(this.entityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleCollectItem(this);
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
