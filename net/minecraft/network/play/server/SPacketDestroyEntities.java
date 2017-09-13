package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketDestroyEntities implements Packet {
   private int[] entityIDs;

   public SPacketDestroyEntities() {
   }

   public SPacketDestroyEntities(int... var1) {
      this.entityIDs = entityIdsIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityIDs = new int[buf.readVarInt()];

      for(int i = 0; i < this.entityIDs.length; ++i) {
         this.entityIDs[i] = buf.readVarInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityIDs.length);

      for(int i : this.entityIDs) {
         buf.writeVarInt(i);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleDestroyEntities(this);
   }

   @SideOnly(Side.CLIENT)
   public int[] getEntityIDs() {
      return this.entityIDs;
   }
}
