package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStatePaletteRegistry implements IBlockStatePalette {
   public int idFor(IBlockState var1) {
      int var2 = Block.BLOCK_STATE_IDS.get(var1);
      return var2 == -1 ? 0 : var2;
   }

   public IBlockState getBlockState(int var1) {
      IBlockState var2 = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(var1);
      return var2 == null ? Blocks.AIR.getDefaultState() : var2;
   }

   @SideOnly(Side.CLIENT)
   public void read(PacketBuffer var1) {
      var1.readVarInt();
   }

   public void write(PacketBuffer var1) {
      var1.writeVarInt(0);
   }

   public int getSerializedState() {
      return PacketBuffer.getVarIntSize(0);
   }
}
