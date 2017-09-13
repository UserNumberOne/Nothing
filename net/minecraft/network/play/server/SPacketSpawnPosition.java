package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

public class SPacketSpawnPosition implements Packet {
   public BlockPos spawnBlockPos;

   public SPacketSpawnPosition() {
   }

   public SPacketSpawnPosition(BlockPos var1) {
      this.spawnBlockPos = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.spawnBlockPos = var1.readBlockPos();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBlockPos(this.spawnBlockPos);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSpawnPosition(this);
   }
}
