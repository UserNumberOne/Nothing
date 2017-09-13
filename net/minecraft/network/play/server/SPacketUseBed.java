package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

public class SPacketUseBed implements Packet {
   private int playerID;
   private BlockPos bedPos;

   public SPacketUseBed() {
   }

   public SPacketUseBed(EntityPlayer var1, BlockPos var2) {
      this.playerID = var1.getEntityId();
      this.bedPos = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.playerID = var1.readVarInt();
      this.bedPos = var1.readBlockPos();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.playerID);
      var1.writeBlockPos(this.bedPos);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleUseBed(this);
   }
}
