package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntIdentityHashBiMap;

public class BlockStatePaletteHashMap implements IBlockStatePalette {
   private final IntIdentityHashBiMap statePaletteMap;
   private final IBlockStatePaletteResizer paletteResizer;
   private final int bits;

   public BlockStatePaletteHashMap(int var1, IBlockStatePaletteResizer var2) {
      this.bits = var1;
      this.paletteResizer = var2;
      this.statePaletteMap = new IntIdentityHashBiMap(1 << var1);
   }

   public int idFor(IBlockState var1) {
      int var2 = this.statePaletteMap.getId(var1);
      if (var2 == -1) {
         var2 = this.statePaletteMap.add(var1);
         if (var2 >= 1 << this.bits) {
            var2 = this.paletteResizer.onResize(this.bits + 1, var1);
         }
      }

      return var2;
   }

   @Nullable
   public IBlockState getBlockState(int var1) {
      return (IBlockState)this.statePaletteMap.get(var1);
   }

   public void write(PacketBuffer var1) {
      int var2 = this.statePaletteMap.size();
      var1.writeVarInt(var2);

      for(int var3 = 0; var3 < var2; ++var3) {
         var1.writeVarInt(Block.BLOCK_STATE_IDS.get(this.statePaletteMap.get(var3)));
      }

   }

   public int getSerializedState() {
      int var1 = PacketBuffer.getVarIntSize(this.statePaletteMap.size());

      for(int var2 = 0; var2 < this.statePaletteMap.size(); ++var2) {
         var1 += PacketBuffer.getVarIntSize(Block.BLOCK_STATE_IDS.get(this.statePaletteMap.get(var2)));
      }

      return var1;
   }
}
