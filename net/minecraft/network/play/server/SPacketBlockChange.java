package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SPacketBlockChange implements Packet {
   private BlockPos blockPosition;
   public IBlockState blockState;

   public SPacketBlockChange() {
   }

   public SPacketBlockChange(World var1, BlockPos var2) {
      this.blockPosition = var2;
      this.blockState = var1.getBlockState(var2);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.blockPosition = var1.readBlockPos();
      this.blockState = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(var1.readVarInt());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBlockPos(this.blockPosition);
      var1.writeVarInt(Block.BLOCK_STATE_IDS.get(this.blockState));
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleBlockChange(this);
   }
}
