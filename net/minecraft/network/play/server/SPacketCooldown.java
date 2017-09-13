package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketCooldown implements Packet {
   private Item item;
   private int ticks;

   public SPacketCooldown() {
   }

   public SPacketCooldown(Item var1, int var2) {
      this.item = itemIn;
      this.ticks = ticksIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.item = Item.getItemById(buf.readVarInt());
      this.ticks = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(Item.getIdFromItem(this.item));
      buf.writeVarInt(this.ticks);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleCooldown(this);
   }

   @SideOnly(Side.CLIENT)
   public Item getItem() {
      return this.item;
   }

   @SideOnly(Side.CLIENT)
   public int getTicks() {
      return this.ticks;
   }
}
