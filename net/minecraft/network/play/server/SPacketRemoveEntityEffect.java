package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.Potion;

public class SPacketRemoveEntityEffect implements Packet {
   private int entityId;
   private Potion effectId;

   public SPacketRemoveEntityEffect() {
   }

   public SPacketRemoveEntityEffect(int var1, Potion var2) {
      this.entityId = var1;
      this.effectId = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.effectId = Potion.getPotionById(var1.readUnsignedByte());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeByte(Potion.getIdFromPotion(this.effectId));
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleRemoveEntityEffect(this);
   }
}
