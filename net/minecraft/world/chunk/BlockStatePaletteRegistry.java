package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStatePaletteRegistry implements IBlockStatePalette {
   public int idFor(IBlockState var1) {
      int i = Block.BLOCK_STATE_IDS.get(state);
      return i == -1 ? 0 : i;
   }

   public IBlockState getBlockState(int var1) {
      IBlockState iblockstate = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(indexKey);
      return iblockstate == null ? Blocks.AIR.getDefaultState() : iblockstate;
   }

   @SideOnly(Side.CLIENT)
   public void read(PacketBuffer var1) {
      buf.readVarInt();
   }

   public void write(PacketBuffer var1) {
      buf.writeVarInt(0);
   }

   public int getSerializedState() {
      return PacketBuffer.getVarIntSize(0);
   }
}
