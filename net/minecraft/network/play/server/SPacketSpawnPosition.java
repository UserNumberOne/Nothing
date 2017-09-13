package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSpawnPosition implements Packet {
   private BlockPos spawnBlockPos;

   public SPacketSpawnPosition() {
   }

   public SPacketSpawnPosition(BlockPos var1) {
      this.spawnBlockPos = posIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.spawnBlockPos = buf.readBlockPos();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeBlockPos(this.spawnBlockPos);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleSpawnPosition(this);
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getSpawnPos() {
      return this.spawnBlockPos;
   }
}
