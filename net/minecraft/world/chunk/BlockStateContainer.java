package net.minecraft.world.chunk;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BitArray;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStateContainer implements IBlockStatePaletteResizer {
   private static final IBlockStatePalette REGISTRY_BASED_PALETTE = new BlockStatePaletteRegistry();
   protected static final IBlockState AIR_BLOCK_STATE = Blocks.AIR.getDefaultState();
   protected BitArray storage;
   protected IBlockStatePalette palette;
   private int bits;

   public BlockStateContainer() {
      this.setBits(4);
   }

   private static int getIndex(int var0, int var1, int var2) {
      return y << 8 | z << 4 | x;
   }

   private void setBits(int var1) {
      this.setBits(bitsIn, false);
   }

   private void setBits(int var1, boolean var2) {
      if (bitsIn != this.bits) {
         this.bits = bitsIn;
         if (this.bits <= 4) {
            this.bits = 4;
            this.palette = new BlockStatePaletteLinear(this.bits, this);
         } else if (this.bits <= 8) {
            this.palette = new BlockStatePaletteHashMap(this.bits, this);
         } else {
            this.palette = REGISTRY_BASED_PALETTE;
            this.bits = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
            if (forceBits) {
               this.bits = bitsIn;
            }
         }

         this.palette.idFor(AIR_BLOCK_STATE);
         this.storage = new BitArray(this.bits, 4096);
      }

   }

   public int onResize(int var1, IBlockState var2) {
      BitArray bitarray = this.storage;
      IBlockStatePalette iblockstatepalette = this.palette;
      this.setBits(p_186008_1_);

      for(int i = 0; i < bitarray.size(); ++i) {
         IBlockState iblockstate = iblockstatepalette.getBlockState(bitarray.getAt(i));
         if (iblockstate != null) {
            this.set(i, iblockstate);
         }
      }

      return this.palette.idFor(state);
   }

   public void set(int var1, int var2, int var3, IBlockState var4) {
      this.set(getIndex(x, y, z), state);
   }

   protected void set(int var1, IBlockState var2) {
      int i = this.palette.idFor(state);
      this.storage.setAt(index, i);
   }

   public IBlockState get(int var1, int var2, int var3) {
      return this.get(getIndex(x, y, z));
   }

   protected IBlockState get(int var1) {
      IBlockState iblockstate = this.palette.getBlockState(this.storage.getAt(index));
      return iblockstate == null ? AIR_BLOCK_STATE : iblockstate;
   }

   @SideOnly(Side.CLIENT)
   public void read(PacketBuffer var1) {
      int i = buf.readByte();
      if (this.bits != i) {
         this.setBits(i, true);
      }

      this.palette.read(buf);
      buf.readLongArray(this.storage.getBackingLongArray());
      int regSize = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
      if (this.palette == REGISTRY_BASED_PALETTE && this.bits != regSize) {
         this.onResize(regSize, AIR_BLOCK_STATE);
      }

   }

   public void write(PacketBuffer var1) {
      buf.writeByte(this.bits);
      this.palette.write(buf);
      buf.writeLongArray(this.storage.getBackingLongArray());
   }

   @Nullable
   public NibbleArray getDataForNBT(byte[] var1, NibbleArray var2) {
      NibbleArray nibblearray = null;

      for(int i = 0; i < 4096; ++i) {
         int j = Block.BLOCK_STATE_IDS.get(this.get(i));
         int k = i & 15;
         int l = i >> 8 & 15;
         int i1 = i >> 4 & 15;
         if ((j >> 12 & 15) != 0) {
            if (nibblearray == null) {
               nibblearray = new NibbleArray();
            }

            nibblearray.set(k, l, i1, j >> 12 & 15);
         }

         p_186017_1_[i] = (byte)(j >> 4 & 255);
         p_186017_2_.set(k, l, i1, j & 15);
      }

      return nibblearray;
   }

   public void setDataFromNBT(byte[] var1, NibbleArray var2, @Nullable NibbleArray var3) {
      for(int i = 0; i < 4096; ++i) {
         int j = i & 15;
         int k = i >> 8 & 15;
         int l = i >> 4 & 15;
         int i1 = p_186019_3_ == null ? 0 : p_186019_3_.get(j, k, l);
         int j1 = i1 << 12 | (p_186019_1_[i] & 255) << 4 | p_186019_2_.get(j, k, l);
         this.set(i, (IBlockState)Block.BLOCK_STATE_IDS.getByValue(j1));
      }

   }

   public int getSerializedSize() {
      return 1 + this.palette.getSerializedState() + PacketBuffer.getVarIntSize(this.storage.size()) + this.storage.getBackingLongArray().length * 8;
   }
}
