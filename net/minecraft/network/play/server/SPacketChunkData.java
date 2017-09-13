package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketChunkData implements Packet {
   private int chunkX;
   private int chunkZ;
   private int availableSections;
   private byte[] buffer;
   private List tileEntityTags;
   private boolean loadChunk;

   public SPacketChunkData() {
   }

   public SPacketChunkData(Chunk var1, int var2) {
      this.chunkX = p_i47124_1_.xPosition;
      this.chunkZ = p_i47124_1_.zPosition;
      this.loadChunk = p_i47124_2_ == 65535;
      boolean flag = !p_i47124_1_.getWorld().provider.hasNoSky();
      this.buffer = new byte[this.calculateChunkSize(p_i47124_1_, flag, p_i47124_2_)];
      this.availableSections = this.extractChunkData(new PacketBuffer(this.getWriteBuffer()), p_i47124_1_, flag, p_i47124_2_);
      this.tileEntityTags = Lists.newArrayList();

      for(Entry entry : p_i47124_1_.getTileEntityMap().entrySet()) {
         BlockPos blockpos = (BlockPos)entry.getKey();
         TileEntity tileentity = (TileEntity)entry.getValue();
         int i = blockpos.getY() >> 4;
         if (this.doChunkLoad() || (p_i47124_2_ & 1 << i) != 0) {
            NBTTagCompound nbttagcompound = tileentity.getUpdateTag();
            this.tileEntityTags.add(nbttagcompound);
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.chunkX = buf.readInt();
      this.chunkZ = buf.readInt();
      this.loadChunk = buf.readBoolean();
      this.availableSections = buf.readVarInt();
      int i = buf.readVarInt();
      if (i > 2097152) {
         throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
      } else {
         this.buffer = new byte[i];
         buf.readBytes(this.buffer);
         int j = buf.readVarInt();
         this.tileEntityTags = Lists.newArrayList();

         for(int k = 0; k < j; ++k) {
            this.tileEntityTags.add(buf.readCompoundTag());
         }

      }
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeInt(this.chunkX);
      buf.writeInt(this.chunkZ);
      buf.writeBoolean(this.loadChunk);
      buf.writeVarInt(this.availableSections);
      buf.writeVarInt(this.buffer.length);
      buf.writeBytes(this.buffer);
      buf.writeVarInt(this.tileEntityTags.size());

      for(NBTTagCompound nbttagcompound : this.tileEntityTags) {
         buf.writeCompoundTag(nbttagcompound);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleChunkData(this);
   }

   @SideOnly(Side.CLIENT)
   public PacketBuffer getReadBuffer() {
      return new PacketBuffer(Unpooled.wrappedBuffer(this.buffer));
   }

   private ByteBuf getWriteBuffer() {
      ByteBuf bytebuf = Unpooled.wrappedBuffer(this.buffer);
      bytebuf.writerIndex(0);
      return bytebuf;
   }

   public int extractChunkData(PacketBuffer var1, Chunk var2, boolean var3, int var4) {
      int i = 0;
      ExtendedBlockStorage[] aextendedblockstorage = p_189555_2_.getBlockStorageArray();
      int j = 0;

      for(int k = aextendedblockstorage.length; j < k; ++j) {
         ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];
         if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (!this.doChunkLoad() || !extendedblockstorage.isEmpty()) && (p_189555_4_ & 1 << j) != 0) {
            i |= 1 << j;
            extendedblockstorage.getData().write(p_189555_1_);
            p_189555_1_.writeBytes(extendedblockstorage.getBlocklightArray().getData());
            if (p_189555_3_) {
               p_189555_1_.writeBytes(extendedblockstorage.getSkylightArray().getData());
            }
         }
      }

      if (this.doChunkLoad()) {
         p_189555_1_.writeBytes(p_189555_2_.getBiomeArray());
      }

      return i;
   }

   protected int calculateChunkSize(Chunk var1, boolean var2, int var3) {
      int i = 0;
      ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
      int j = 0;

      for(int k = aextendedblockstorage.length; j < k; ++j) {
         ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];
         if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (!this.doChunkLoad() || !extendedblockstorage.isEmpty()) && (p_189556_3_ & 1 << j) != 0) {
            i = i + extendedblockstorage.getData().getSerializedSize();
            i = i + extendedblockstorage.getBlocklightArray().getData().length;
            if (p_189556_2_) {
               i += extendedblockstorage.getSkylightArray().getData().length;
            }
         }
      }

      if (this.doChunkLoad()) {
         i += chunkIn.getBiomeArray().length;
      }

      return i;
   }

   @SideOnly(Side.CLIENT)
   public int getChunkX() {
      return this.chunkX;
   }

   @SideOnly(Side.CLIENT)
   public int getChunkZ() {
      return this.chunkZ;
   }

   @SideOnly(Side.CLIENT)
   public int getExtractedSize() {
      return this.availableSections;
   }

   public boolean doChunkLoad() {
      return this.loadChunk;
   }

   @SideOnly(Side.CLIENT)
   public List getTileEntityTags() {
      return this.tileEntityTags;
   }
}
