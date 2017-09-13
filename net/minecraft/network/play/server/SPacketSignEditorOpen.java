package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

public class SPacketSignEditorOpen implements Packet {
   private BlockPos signPosition;

   public SPacketSignEditorOpen() {
   }

   public SPacketSignEditorOpen(BlockPos var1) {
      this.signPosition = var1;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSignEditorOpen(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.signPosition = var1.readBlockPos();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBlockPos(this.signPosition);
   }
}
