package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStatePaletteHashMap implements IBlockStatePalette {
   private final IntIdentityHashBiMap statePaletteMap;
   private final IBlockStatePaletteResizer paletteResizer;
   private final int bits;

   public BlockStatePaletteHashMap(int var1, IBlockStatePaletteResizer var2) {
      this.bits = bitsIn;
      this.paletteResizer = p_i47089_2_;
      this.statePaletteMap = new IntIdentityHashBiMap(1 << bitsIn);
   }

   public int idFor(IBlockState var1) {
      int i = this.statePaletteMap.getId(state);
      if (i == -1) {
         i = this.statePaletteMap.add(state);
         if (i >= 1 << this.bits) {
            i = this.paletteResizer.onResize(this.bits + 1, state);
         }
      }

      return i;
   }

   @Nullable
   public IBlockState getBlockState(int var1) {
      return (IBlockState)this.statePaletteMap.get(indexKey);
   }

   @SideOnly(Side.CLIENT)
   public void read(PacketBuffer var1) {
      this.statePaletteMap.clear();
      int i = buf.readVarInt();

      for(int j = 0; j < i; ++j) {
         this.statePaletteMap.add(Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt()));
      }

   }

   public void write(PacketBuffer var1) {
      int i = this.statePaletteMap.size();
      buf.writeVarInt(i);

      for(int j = 0; j < i; ++j) {
         buf.writeVarInt(Block.BLOCK_STATE_IDS.get(this.statePaletteMap.get(j)));
      }

   }

   public int getSerializedState() {
      int i = PacketBuffer.getVarIntSize(this.statePaletteMap.size());

      for(int j = 0; j < this.statePaletteMap.size(); ++j) {
         i += PacketBuffer.getVarIntSize(Block.BLOCK_STATE_IDS.get(this.statePaletteMap.get(j)));
      }

      return i;
   }
}
