package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketCooldown implements Packet {
   private Item item;
   private int ticks;

   public SPacketCooldown() {
   }

   public SPacketCooldown(Item var1, int var2) {
      this.item = var1;
      this.ticks = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.item = Item.getItemById(var1.readVarInt());
      this.ticks = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(Item.getIdFromItem(this.item));
      var1.writeVarInt(this.ticks);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleCooldown(this);
   }
}
