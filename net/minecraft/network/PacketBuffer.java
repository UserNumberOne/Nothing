package net.minecraft.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufProcessor;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketBuffer extends ByteBuf {
   private final ByteBuf buf;

   public PacketBuffer(ByteBuf var1) {
      this.buf = wrapped;
   }

   public static int getVarIntSize(int var0) {
      for(int i = 1; i < 5; ++i) {
         if ((input & -1 << i * 7) == 0) {
            return i;
         }
      }

      return 5;
   }

   public PacketBuffer writeByteArray(byte[] var1) {
      this.writeVarInt(array.length);
      this.writeBytes(array);
      return this;
   }

   public byte[] readByteArray() {
      return this.readByteArray(this.readableBytes());
   }

   public byte[] readByteArray(int var1) {
      int i = this.readVarInt();
      if (i > maxLength) {
         throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxLength);
      } else {
         byte[] abyte = new byte[i];
         this.readBytes(abyte);
         return abyte;
      }
   }

   public PacketBuffer writeVarIntArray(int[] var1) {
      this.writeVarInt(array.length);

      for(int i : array) {
         this.writeVarInt(i);
      }

      return this;
   }

   public int[] readVarIntArray() {
      return this.readVarIntArray(this.readableBytes());
   }

   public int[] readVarIntArray(int var1) {
      int i = this.readVarInt();
      if (i > maxLength) {
         throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + maxLength);
      } else {
         int[] aint = new int[i];

         for(int j = 0; j < aint.length; ++j) {
            aint[j] = this.readVarInt();
         }

         return aint;
      }
   }

   public PacketBuffer writeLongArray(long[] var1) {
      this.writeVarInt(array.length);

      for(long i : array) {
         this.writeLong(i);
      }

      return this;
   }

   @SideOnly(Side.CLIENT)
   public long[] readLongArray(@Nullable long[] var1) {
      return this.readLongArray(array, this.readableBytes() / 8);
   }

   @SideOnly(Side.CLIENT)
   public long[] readLongArray(@Nullable long[] var1, int var2) {
      int i = this.readVarInt();
      if (p_189423_1_ == null || p_189423_1_.length != i) {
         if (i > p_189423_2_) {
            throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + p_189423_2_);
         }

         p_189423_1_ = new long[i];
      }

      for(int j = 0; j < p_189423_1_.length; ++j) {
         p_189423_1_[j] = this.readLong();
      }

      return p_189423_1_;
   }

   public BlockPos readBlockPos() {
      return BlockPos.fromLong(this.readLong());
   }

   public PacketBuffer writeBlockPos(BlockPos var1) {
      this.writeLong(pos.toLong());
      return this;
   }

   public ITextComponent readTextComponent() throws IOException {
      return ITextComponent.Serializer.jsonToComponent(this.readString(32767));
   }

   public PacketBuffer writeTextComponent(ITextComponent var1) {
      return this.writeString(ITextComponent.Serializer.componentToJson(component));
   }

   public Enum readEnumValue(Class var1) {
      return ((Enum[])((Enum[])enumClass.getEnumConstants()))[this.readVarInt()];
   }

   public PacketBuffer writeEnumValue(Enum var1) {
      return this.writeVarInt(value.ordinal());
   }

   public int readVarInt() {
      int i = 0;
      int j = 0;

      while(true) {
         byte b0 = this.readByte();
         i |= (b0 & 127) << j++ * 7;
         if (j > 5) {
            throw new RuntimeException("VarInt too big");
         }

         if ((b0 & 128) != 128) {
            break;
         }
      }

      return i;
   }

   public long readVarLong() {
      long i = 0L;
      int j = 0;

      while(true) {
         byte b0 = this.readByte();
         i |= (long)(b0 & 127) << j++ * 7;
         if (j > 10) {
            throw new RuntimeException("VarLong too big");
         }

         if ((b0 & 128) != 128) {
            break;
         }
      }

      return i;
   }

   public PacketBuffer writeUniqueId(UUID var1) {
      this.writeLong(uuid.getMostSignificantBits());
      this.writeLong(uuid.getLeastSignificantBits());
      return this;
   }

   public UUID readUniqueId() {
      return new UUID(this.readLong(), this.readLong());
   }

   public PacketBuffer writeVarInt(int var1) {
      while((input & -128) != 0) {
         this.writeByte(input & 127 | 128);
         input >>>= 7;
      }

      this.writeByte(input);
      return this;
   }

   public PacketBuffer writeVarLong(long var1) {
      while((value & -128L) != 0L) {
         this.writeByte((int)(value & 127L) | 128);
         value >>>= 7;
      }

      this.writeByte((int)value);
      return this;
   }

   public PacketBuffer writeCompoundTag(@Nullable NBTTagCompound var1) {
      if (nbt == null) {
         this.writeByte(0);
      } else {
         try {
            CompressedStreamTools.write(nbt, new ByteBufOutputStream(this));
         } catch (IOException var3) {
            throw new EncoderException(var3);
         }
      }

      return this;
   }

   @Nullable
   public NBTTagCompound readCompoundTag() throws IOException {
      int i = this.readerIndex();
      byte b0 = this.readByte();
      if (b0 == 0) {
         return null;
      } else {
         this.readerIndex(i);

         try {
            return CompressedStreamTools.read(new ByteBufInputStream(this), new NBTSizeTracker(2097152L));
         } catch (IOException var4) {
            throw new EncoderException(var4);
         }
      }
   }

   public PacketBuffer writeItemStack(@Nullable ItemStack var1) {
      if (stack == null) {
         this.writeShort(-1);
      } else {
         this.writeShort(Item.getIdFromItem(stack.getItem()));
         this.writeByte(stack.stackSize);
         this.writeShort(stack.getMetadata());
         NBTTagCompound nbttagcompound = null;
         if (stack.getItem().isDamageable() || stack.getItem().getShareTag()) {
            nbttagcompound = stack.getItem().getNBTShareTag(stack);
         }

         this.writeCompoundTag(nbttagcompound);
      }

      return this;
   }

   @Nullable
   public ItemStack readItemStack() throws IOException {
      ItemStack itemstack = null;
      int i = this.readShort();
      if (i >= 0) {
         int j = this.readByte();
         int k = this.readShort();
         itemstack = new ItemStack(Item.getItemById(i), j, k);
         itemstack.setTagCompound(this.readCompoundTag());
      }

      return itemstack;
   }

   public String readString(int var1) {
      int i = this.readVarInt();
      if (i > maxLength * 4) {
         throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
      } else if (i < 0) {
         throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
      } else {
         String s = new String(this.readBytes(i).array(), Charsets.UTF_8);
         if (s.length() > maxLength) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
         } else {
            return s;
         }
      }
   }

   public PacketBuffer writeString(String var1) {
      byte[] abyte = string.getBytes(Charsets.UTF_8);
      if (abyte.length > 32767) {
         throw new EncoderException("String too big (was " + string.length() + " bytes encoded, max " + 32767 + ")");
      } else {
         this.writeVarInt(abyte.length);
         this.writeBytes(abyte);
         return this;
      }
   }

   public int capacity() {
      return this.buf.capacity();
   }

   public ByteBuf capacity(int var1) {
      return this.buf.capacity(p_capacity_1_);
   }

   public int maxCapacity() {
      return this.buf.maxCapacity();
   }

   public ByteBufAllocator alloc() {
      return this.buf.alloc();
   }

   public ByteOrder order() {
      return this.buf.order();
   }

   public ByteBuf order(ByteOrder var1) {
      return this.buf.order(p_order_1_);
   }

   public ByteBuf unwrap() {
      return this.buf.unwrap();
   }

   public boolean isDirect() {
      return this.buf.isDirect();
   }

   public int readerIndex() {
      return this.buf.readerIndex();
   }

   public ByteBuf readerIndex(int var1) {
      return this.buf.readerIndex(p_readerIndex_1_);
   }

   public int writerIndex() {
      return this.buf.writerIndex();
   }

   public ByteBuf writerIndex(int var1) {
      return this.buf.writerIndex(p_writerIndex_1_);
   }

   public ByteBuf setIndex(int var1, int var2) {
      return this.buf.setIndex(p_setIndex_1_, p_setIndex_2_);
   }

   public int readableBytes() {
      return this.buf.readableBytes();
   }

   public int writableBytes() {
      return this.buf.writableBytes();
   }

   public int maxWritableBytes() {
      return this.buf.maxWritableBytes();
   }

   public boolean isReadable() {
      return this.buf.isReadable();
   }

   public boolean isReadable(int var1) {
      return this.buf.isReadable(p_isReadable_1_);
   }

   public boolean isWritable() {
      return this.buf.isWritable();
   }

   public boolean isWritable(int var1) {
      return this.buf.isWritable(p_isWritable_1_);
   }

   public ByteBuf clear() {
      return this.buf.clear();
   }

   public ByteBuf markReaderIndex() {
      return this.buf.markReaderIndex();
   }

   public ByteBuf resetReaderIndex() {
      return this.buf.resetReaderIndex();
   }

   public ByteBuf markWriterIndex() {
      return this.buf.markWriterIndex();
   }

   public ByteBuf resetWriterIndex() {
      return this.buf.resetWriterIndex();
   }

   public ByteBuf discardReadBytes() {
      return this.buf.discardReadBytes();
   }

   public ByteBuf discardSomeReadBytes() {
      return this.buf.discardSomeReadBytes();
   }

   public ByteBuf ensureWritable(int var1) {
      return this.buf.ensureWritable(p_ensureWritable_1_);
   }

   public int ensureWritable(int var1, boolean var2) {
      return this.buf.ensureWritable(p_ensureWritable_1_, p_ensureWritable_2_);
   }

   public boolean getBoolean(int var1) {
      return this.buf.getBoolean(p_getBoolean_1_);
   }

   public byte getByte(int var1) {
      return this.buf.getByte(p_getByte_1_);
   }

   public short getUnsignedByte(int var1) {
      return this.buf.getUnsignedByte(p_getUnsignedByte_1_);
   }

   public short getShort(int var1) {
      return this.buf.getShort(p_getShort_1_);
   }

   public int getUnsignedShort(int var1) {
      return this.buf.getUnsignedShort(p_getUnsignedShort_1_);
   }

   public int getMedium(int var1) {
      return this.buf.getMedium(p_getMedium_1_);
   }

   public int getUnsignedMedium(int var1) {
      return this.buf.getUnsignedMedium(p_getUnsignedMedium_1_);
   }

   public int getInt(int var1) {
      return this.buf.getInt(p_getInt_1_);
   }

   public long getUnsignedInt(int var1) {
      return this.buf.getUnsignedInt(p_getUnsignedInt_1_);
   }

   public long getLong(int var1) {
      return this.buf.getLong(p_getLong_1_);
   }

   public char getChar(int var1) {
      return this.buf.getChar(p_getChar_1_);
   }

   public float getFloat(int var1) {
      return this.buf.getFloat(p_getFloat_1_);
   }

   public double getDouble(int var1) {
      return this.buf.getDouble(p_getDouble_1_);
   }

   public ByteBuf getBytes(int var1, ByteBuf var2) {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_);
   }

   public ByteBuf getBytes(int var1, ByteBuf var2, int var3) {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
   }

   public ByteBuf getBytes(int var1, ByteBuf var2, int var3, int var4) {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_);
   }

   public ByteBuf getBytes(int var1, byte[] var2) {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_);
   }

   public ByteBuf getBytes(int var1, byte[] var2, int var3, int var4) {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_);
   }

   public ByteBuf getBytes(int var1, ByteBuffer var2) {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_);
   }

   public ByteBuf getBytes(int var1, OutputStream var2, int var3) throws IOException {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
   }

   public int getBytes(int var1, GatheringByteChannel var2, int var3) throws IOException {
      return this.buf.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
   }

   public ByteBuf setBoolean(int var1, boolean var2) {
      return this.buf.setBoolean(p_setBoolean_1_, p_setBoolean_2_);
   }

   public ByteBuf setByte(int var1, int var2) {
      return this.buf.setByte(p_setByte_1_, p_setByte_2_);
   }

   public ByteBuf setShort(int var1, int var2) {
      return this.buf.setShort(p_setShort_1_, p_setShort_2_);
   }

   public ByteBuf setMedium(int var1, int var2) {
      return this.buf.setMedium(p_setMedium_1_, p_setMedium_2_);
   }

   public ByteBuf setInt(int var1, int var2) {
      return this.buf.setInt(p_setInt_1_, p_setInt_2_);
   }

   public ByteBuf setLong(int var1, long var2) {
      return this.buf.setLong(p_setLong_1_, p_setLong_2_);
   }

   public ByteBuf setChar(int var1, int var2) {
      return this.buf.setChar(p_setChar_1_, p_setChar_2_);
   }

   public ByteBuf setFloat(int var1, float var2) {
      return this.buf.setFloat(p_setFloat_1_, p_setFloat_2_);
   }

   public ByteBuf setDouble(int var1, double var2) {
      return this.buf.setDouble(p_setDouble_1_, p_setDouble_2_);
   }

   public ByteBuf setBytes(int var1, ByteBuf var2) {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_);
   }

   public ByteBuf setBytes(int var1, ByteBuf var2, int var3) {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
   }

   public ByteBuf setBytes(int var1, ByteBuf var2, int var3, int var4) {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_);
   }

   public ByteBuf setBytes(int var1, byte[] var2) {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_);
   }

   public ByteBuf setBytes(int var1, byte[] var2, int var3, int var4) {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_);
   }

   public ByteBuf setBytes(int var1, ByteBuffer var2) {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_);
   }

   public int setBytes(int var1, InputStream var2, int var3) throws IOException {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
   }

   public int setBytes(int var1, ScatteringByteChannel var2, int var3) throws IOException {
      return this.buf.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
   }

   public ByteBuf setZero(int var1, int var2) {
      return this.buf.setZero(p_setZero_1_, p_setZero_2_);
   }

   public boolean readBoolean() {
      return this.buf.readBoolean();
   }

   public byte readByte() {
      return this.buf.readByte();
   }

   public short readUnsignedByte() {
      return this.buf.readUnsignedByte();
   }

   public short readShort() {
      return this.buf.readShort();
   }

   public int readUnsignedShort() {
      return this.buf.readUnsignedShort();
   }

   public int readMedium() {
      return this.buf.readMedium();
   }

   public int readUnsignedMedium() {
      return this.buf.readUnsignedMedium();
   }

   public int readInt() {
      return this.buf.readInt();
   }

   public long readUnsignedInt() {
      return this.buf.readUnsignedInt();
   }

   public long readLong() {
      return this.buf.readLong();
   }

   public char readChar() {
      return this.buf.readChar();
   }

   public float readFloat() {
      return this.buf.readFloat();
   }

   public double readDouble() {
      return this.buf.readDouble();
   }

   public ByteBuf readBytes(int var1) {
      return this.buf.readBytes(p_readBytes_1_);
   }

   public ByteBuf readSlice(int var1) {
      return this.buf.readSlice(p_readSlice_1_);
   }

   public ByteBuf readBytes(ByteBuf var1) {
      return this.buf.readBytes(p_readBytes_1_);
   }

   public ByteBuf readBytes(ByteBuf var1, int var2) {
      return this.buf.readBytes(p_readBytes_1_, p_readBytes_2_);
   }

   public ByteBuf readBytes(ByteBuf var1, int var2, int var3) {
      return this.buf.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_3_);
   }

   public ByteBuf readBytes(byte[] var1) {
      return this.buf.readBytes(p_readBytes_1_);
   }

   public ByteBuf readBytes(byte[] var1, int var2, int var3) {
      return this.buf.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_3_);
   }

   public ByteBuf readBytes(ByteBuffer var1) {
      return this.buf.readBytes(p_readBytes_1_);
   }

   public ByteBuf readBytes(OutputStream var1, int var2) throws IOException {
      return this.buf.readBytes(p_readBytes_1_, p_readBytes_2_);
   }

   public int readBytes(GatheringByteChannel var1, int var2) throws IOException {
      return this.buf.readBytes(p_readBytes_1_, p_readBytes_2_);
   }

   public ByteBuf skipBytes(int var1) {
      return this.buf.skipBytes(p_skipBytes_1_);
   }

   public ByteBuf writeBoolean(boolean var1) {
      return this.buf.writeBoolean(p_writeBoolean_1_);
   }

   public ByteBuf writeByte(int var1) {
      return this.buf.writeByte(p_writeByte_1_);
   }

   public ByteBuf writeShort(int var1) {
      return this.buf.writeShort(p_writeShort_1_);
   }

   public ByteBuf writeMedium(int var1) {
      return this.buf.writeMedium(p_writeMedium_1_);
   }

   public ByteBuf writeInt(int var1) {
      return this.buf.writeInt(p_writeInt_1_);
   }

   public ByteBuf writeLong(long var1) {
      return this.buf.writeLong(p_writeLong_1_);
   }

   public ByteBuf writeChar(int var1) {
      return this.buf.writeChar(p_writeChar_1_);
   }

   public ByteBuf writeFloat(float var1) {
      return this.buf.writeFloat(p_writeFloat_1_);
   }

   public ByteBuf writeDouble(double var1) {
      return this.buf.writeDouble(p_writeDouble_1_);
   }

   public ByteBuf writeBytes(ByteBuf var1) {
      return this.buf.writeBytes(p_writeBytes_1_);
   }

   public ByteBuf writeBytes(ByteBuf var1, int var2) {
      return this.buf.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
   }

   public ByteBuf writeBytes(ByteBuf var1, int var2, int var3) {
      return this.buf.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_);
   }

   public ByteBuf writeBytes(byte[] var1) {
      return this.buf.writeBytes(p_writeBytes_1_);
   }

   public ByteBuf writeBytes(byte[] var1, int var2, int var3) {
      return this.buf.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_);
   }

   public ByteBuf writeBytes(ByteBuffer var1) {
      return this.buf.writeBytes(p_writeBytes_1_);
   }

   public int writeBytes(InputStream var1, int var2) throws IOException {
      return this.buf.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
   }

   public int writeBytes(ScatteringByteChannel var1, int var2) throws IOException {
      return this.buf.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
   }

   public ByteBuf writeZero(int var1) {
      return this.buf.writeZero(p_writeZero_1_);
   }

   public int indexOf(int var1, int var2, byte var3) {
      return this.buf.indexOf(p_indexOf_1_, p_indexOf_2_, p_indexOf_3_);
   }

   public int bytesBefore(byte var1) {
      return this.buf.bytesBefore(p_bytesBefore_1_);
   }

   public int bytesBefore(int var1, byte var2) {
      return this.buf.bytesBefore(p_bytesBefore_1_, p_bytesBefore_2_);
   }

   public int bytesBefore(int var1, int var2, byte var3) {
      return this.buf.bytesBefore(p_bytesBefore_1_, p_bytesBefore_2_, p_bytesBefore_3_);
   }

   public int forEachByte(ByteBufProcessor var1) {
      return this.buf.forEachByte(p_forEachByte_1_);
   }

   public int forEachByte(int var1, int var2, ByteBufProcessor var3) {
      return this.buf.forEachByte(p_forEachByte_1_, p_forEachByte_2_, p_forEachByte_3_);
   }

   public int forEachByteDesc(ByteBufProcessor var1) {
      return this.buf.forEachByteDesc(p_forEachByteDesc_1_);
   }

   public int forEachByteDesc(int var1, int var2, ByteBufProcessor var3) {
      return this.buf.forEachByteDesc(p_forEachByteDesc_1_, p_forEachByteDesc_2_, p_forEachByteDesc_3_);
   }

   public ByteBuf copy() {
      return this.buf.copy();
   }

   public ByteBuf copy(int var1, int var2) {
      return this.buf.copy(p_copy_1_, p_copy_2_);
   }

   public ByteBuf slice() {
      return this.buf.slice();
   }

   public ByteBuf slice(int var1, int var2) {
      return this.buf.slice(p_slice_1_, p_slice_2_);
   }

   public ByteBuf duplicate() {
      return this.buf.duplicate();
   }

   public int nioBufferCount() {
      return this.buf.nioBufferCount();
   }

   public ByteBuffer nioBuffer() {
      return this.buf.nioBuffer();
   }

   public ByteBuffer nioBuffer(int var1, int var2) {
      return this.buf.nioBuffer(p_nioBuffer_1_, p_nioBuffer_2_);
   }

   public ByteBuffer internalNioBuffer(int var1, int var2) {
      return this.buf.internalNioBuffer(p_internalNioBuffer_1_, p_internalNioBuffer_2_);
   }

   public ByteBuffer[] nioBuffers() {
      return this.buf.nioBuffers();
   }

   public ByteBuffer[] nioBuffers(int var1, int var2) {
      return this.buf.nioBuffers(p_nioBuffers_1_, p_nioBuffers_2_);
   }

   public boolean hasArray() {
      return this.buf.hasArray();
   }

   public byte[] array() {
      return this.buf.array();
   }

   public int arrayOffset() {
      return this.buf.arrayOffset();
   }

   public boolean hasMemoryAddress() {
      return this.buf.hasMemoryAddress();
   }

   public long memoryAddress() {
      return this.buf.memoryAddress();
   }

   public String toString(Charset var1) {
      return this.buf.toString(p_toString_1_);
   }

   public String toString(int var1, int var2, Charset var3) {
      return this.buf.toString(p_toString_1_, p_toString_2_, p_toString_3_);
   }

   public int hashCode() {
      return this.buf.hashCode();
   }

   public boolean equals(Object var1) {
      return this.buf.equals(p_equals_1_);
   }

   public int compareTo(ByteBuf var1) {
      return this.buf.compareTo(p_compareTo_1_);
   }

   public String toString() {
      return this.buf.toString();
   }

   public ByteBuf retain(int var1) {
      return this.buf.retain(p_retain_1_);
   }

   public ByteBuf retain() {
      return this.buf.retain();
   }

   public int refCnt() {
      return this.buf.refCnt();
   }

   public boolean release() {
      return this.buf.release();
   }

   public boolean release(int var1) {
      return this.buf.release(p_release_1_);
   }
}
