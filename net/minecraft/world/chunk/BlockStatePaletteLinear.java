package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;

public class BlockStatePaletteLinear implements IBlockStatePalette {
   private final IBlockState[] states;
   private final IBlockStatePaletteResizer resizeHandler;
   private final int bits;
   private int arraySize;

   public BlockStatePaletteLinear(int var1, IBlockStatePaletteResizer var2) {
      this.states = new IBlockState[1 << var1];
      this.bits = var1;
      this.resizeHandler = var2;
   }

   public int idFor(IBlockState var1) {
      for(int var2 = 0; var2 < this.arraySize; ++var2) {
         if (this.states[var2] == var1) {
            return var2;
         }
      }

      int var3 = this.arraySize;
      if (var3 < this.states.length) {
         this.states[var3] = var1;
         ++this.arraySize;
         return var3;
      } else {
         return this.resizeHandler.onResize(this.bits + 1, var1);
      }
   }

   @Nullable
   public IBlockState getBlockState(int var1) {
      return var1 >= 0 && var1 < this.arraySize ? this.states[var1] : null;
   }

   public void write(PacketBuffer var1) {
      var1.writeVarInt(this.arraySize);

      for(int var2 = 0; var2 < this.arraySize; ++var2) {
         var1.writeVarInt(Block.BLOCK_STATE_IDS.get(this.states[var2]));
      }

   }

   public int getSerializedState() {
      int var1 = PacketBuffer.getVarIntSize(this.arraySize);

      for(int var2 = 0; var2 < this.arraySize; ++var2) {
         var1 += PacketBuffer.getVarIntSize(Block.BLOCK_STATE_IDS.get(this.states[var2]));
      }

      return var1;
   }
}
