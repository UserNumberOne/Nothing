package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BitArray;
import net.minecraft.util.math.MathHelper;

public class BlockStateContainer implements IBlockStatePaletteResizer {
   private static final IBlockStatePalette REGISTRY_BASED_PALETTE = new BlockStatePaletteRegistry();
   protected static final IBlockState AIR_BLOCK_STATE = Blocks.AIR.getDefaultState();
   protected BitArray storage;
   protected IBlockStatePalette palette;
   private int bits;

   public BlockStateContainer() {
      this.setBits(4);
   }

   private static int getIndex(int i, int j, int k) {
      return j << 8 | k << 4 | i;
   }

   private void setBits(int i) {
      if (i != this.bits) {
         this.bits = i;
         if (this.bits <= 4) {
            this.bits = 4;
            this.palette = new BlockStatePaletteLinear(this.bits, this);
         } else if (this.bits <= 8) {
            this.palette = new BlockStatePaletteHashMap(this.bits, this);
         } else {
            this.palette = REGISTRY_BASED_PALETTE;
            this.bits = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
         }

         this.palette.idFor(AIR_BLOCK_STATE);
         this.storage = new BitArray(this.bits, 4096);
      }

   }

   public int onResize(int i, IBlockState iblockdata) {
      BitArray databits = this.storage;
      IBlockStatePalette datapalette = this.palette;
      this.setBits(i);

      for(int j = 0; j < databits.size(); ++j) {
         IBlockState iblockdata1 = datapalette.getBlockState(databits.getAt(j));
         if (iblockdata1 != null) {
            this.set(j, iblockdata1);
         }
      }

      return this.palette.idFor(iblockdata);
   }

   public void set(int i, int j, int k, IBlockState iblockdata) {
      this.set(getIndex(i, j, k), iblockdata);
   }

   protected void set(int i, IBlockState iblockdata) {
      int j = this.palette.idFor(iblockdata);
      this.storage.setAt(i, j);
   }

   public IBlockState get(int i, int j, int k) {
      return this.get(getIndex(i, j, k));
   }

   protected IBlockState get(int i) {
      IBlockState iblockdata = this.palette.getBlockState(this.storage.getAt(i));
      return iblockdata == null ? AIR_BLOCK_STATE : iblockdata;
   }

   public void write(PacketBuffer packetdataserializer) {
      packetdataserializer.writeByte(this.bits);
      this.palette.write(packetdataserializer);
      packetdataserializer.writeLongArray(this.storage.getBackingLongArray());
   }

   @Nullable
   public NibbleArray getDataForNBT(byte[] abyte, NibbleArray nibblearray) {
      NibbleArray nibblearray1 = null;

      for(int i = 0; i < 4096; ++i) {
         int j = Block.BLOCK_STATE_IDS.get(this.get(i));
         int k = i & 15;
         int l = i >> 8 & 15;
         int i1 = i >> 4 & 15;
         if ((j >> 12 & 15) != 0) {
            if (nibblearray1 == null) {
               nibblearray1 = new NibbleArray();
            }

            nibblearray1.set(k, l, i1, j >> 12 & 15);
         }

         abyte[i] = (byte)(j >> 4 & 255);
         nibblearray.set(k, l, i1, j & 15);
      }

      return nibblearray1;
   }

   public void setDataFromNBT(byte[] abyte, NibbleArray nibblearray, @Nullable NibbleArray nibblearray1) {
      for(int i = 0; i < 4096; ++i) {
         int j = i & 15;
         int k = i >> 8 & 15;
         int l = i >> 4 & 15;
         int i1 = nibblearray1 == null ? 0 : nibblearray1.get(j, k, l);
         int j1 = i1 << 12 | (abyte[i] & 255) << 4 | nibblearray.get(j, k, l);
         IBlockState data = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(j1);
         if (data == null) {
            Block block = Block.getBlockById(j1 >> 4);
            if (block != null) {
               try {
                  data = block.getStateFromMeta(j1 & 15);
               } catch (Exception var12) {
                  data = block.getDefaultState();
               }
            }
         }

         this.set(i, data);
      }

   }

   public int getSerializedSize() {
      return 1 + this.palette.getSerializedState() + PacketBuffer.getVarIntSize(this.storage.size()) + this.storage.getBackingLongArray().length * 8;
   }
}
