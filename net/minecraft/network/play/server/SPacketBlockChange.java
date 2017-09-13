package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketBlockChange implements Packet {
   private BlockPos blockPosition;
   public IBlockState blockState;

   public SPacketBlockChange() {
   }

   public SPacketBlockChange(World var1, BlockPos var2) {
      this.blockPosition = posIn;
      this.blockState = worldIn.getBlockState(posIn);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.blockPosition = buf.readBlockPos();
      this.blockState = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeBlockPos(this.blockPosition);
      buf.writeVarInt(Block.BLOCK_STATE_IDS.get(this.blockState));
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleBlockChange(this);
   }

   @SideOnly(Side.CLIENT)
   public IBlockState getBlockState() {
      return this.blockState;
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getBlockPosition() {
      return this.blockPosition;
   }
}
