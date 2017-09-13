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

   private static int getIndex(int var0, int var1, int var2) {
      return var1 << 8 | var2 << 4 | var0;
   }

   private void setBits(int var1) {
      if (var1 != this.bits) {
         this.bits = var1;
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

   public int onResize(int var1, IBlockState var2) {
      BitArray var3 = this.storage;
      IBlockStatePalette var4 = this.palette;
      this.setBits(var1);

      for(int var5 = 0; var5 < var3.size(); ++var5) {
         IBlockState var6 = var4.getBlockState(var3.getAt(var5));
         if (var6 != null) {
            this.set(var5, var6);
         }
      }

      return this.palette.idFor(var2);
   }

   public void set(int var1, int var2, int var3, IBlockState var4) {
      this.set(getIndex(var1, var2, var3), var4);
   }

   protected void set(int var1, IBlockState var2) {
      int var3 = this.palette.idFor(var2);
      this.storage.setAt(var1, var3);
   }

   public IBlockState get(int var1, int var2, int var3) {
      return this.get(getIndex(var1, var2, var3));
   }

   protected IBlockState get(int var1) {
      IBlockState var2 = this.palette.getBlockState(this.storage.getAt(var1));
      return var2 == null ? AIR_BLOCK_STATE : var2;
   }

   public void write(PacketBuffer var1) {
      var1.writeByte(this.bits);
      this.palette.write(var1);
      var1.writeLongArray(this.storage.getBackingLongArray());
   }

   @Nullable
   public NibbleArray getDataForNBT(byte[] var1, NibbleArray var2) {
      NibbleArray var3 = null;

      for(int var4 = 0; var4 < 4096; ++var4) {
         int var5 = Block.BLOCK_STATE_IDS.get(this.get(var4));
         int var6 = var4 & 15;
         int var7 = var4 >> 8 & 15;
         int var8 = var4 >> 4 & 15;
         if ((var5 >> 12 & 15) != 0) {
            if (var3 == null) {
               var3 = new NibbleArray();
            }

            var3.set(var6, var7, var8, var5 >> 12 & 15);
         }

         var1[var4] = (byte)(var5 >> 4 & 255);
         var2.set(var6, var7, var8, var5 & 15);
      }

      return var3;
   }

   public void setDataFromNBT(byte[] var1, NibbleArray var2, @Nullable NibbleArray var3) {
      for(int var4 = 0; var4 < 4096; ++var4) {
         int var5 = var4 & 15;
         int var6 = var4 >> 8 & 15;
         int var7 = var4 >> 4 & 15;
         int var8 = var3 == null ? 0 : var3.get(var5, var6, var7);
         int var9 = var8 << 12 | (var1[var4] & 255) << 4 | var2.get(var5, var6, var7);
         IBlockState var10 = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(var9);
         if (var10 == null) {
            Block var11 = Block.getBlockById(var9 >> 4);
            if (var11 != null) {
               try {
                  var10 = var11.getStateFromMeta(var9 & 15);
               } catch (Exception var12) {
                  var10 = var11.getDefaultState();
               }
            }
         }

         this.set(var4, var10);
      }

   }

   public int getSerializedSize() {
      return 1 + this.palette.getSerializedState() + PacketBuffer.getVarIntSize(this.storage.size()) + this.storage.getBackingLongArray().length * 8;
   }
}
