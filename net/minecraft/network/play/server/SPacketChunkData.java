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
      this.chunkX = var1.xPosition;
      this.chunkZ = var1.zPosition;
      this.loadChunk = var2 == 65535;
      boolean var3 = !var1.getWorld().provider.hasNoSky();
      this.buffer = new byte[this.calculateChunkSize(var1, var3, var2)];
      this.availableSections = this.extractChunkData(new PacketBuffer(this.getWriteBuffer()), var1, var3, var2);
      this.tileEntityTags = Lists.newArrayList();

      for(Entry var5 : var1.getTileEntityMap().entrySet()) {
         BlockPos var6 = (BlockPos)var5.getKey();
         TileEntity var7 = (TileEntity)var5.getValue();
         int var8 = var6.getY() >> 4;
         if (this.doChunkLoad() || (var2 & 1 << var8) != 0) {
            NBTTagCompound var9 = var7.getUpdateTag();
            this.tileEntityTags.add(var9);
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.chunkX = var1.readInt();
      this.chunkZ = var1.readInt();
      this.loadChunk = var1.readBoolean();
      this.availableSections = var1.readVarInt();
      int var2 = var1.readVarInt();
      if (var2 > 2097152) {
         throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
      } else {
         this.buffer = new byte[var2];
         var1.readBytes(this.buffer);
         int var3 = var1.readVarInt();
         this.tileEntityTags = Lists.newArrayList();

         for(int var4 = 0; var4 < var3; ++var4) {
            this.tileEntityTags.add(var1.readCompoundTag());
         }

      }
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeInt(this.chunkX);
      var1.writeInt(this.chunkZ);
      var1.writeBoolean(this.loadChunk);
      var1.writeVarInt(this.availableSections);
      var1.writeVarInt(this.buffer.length);
      var1.writeBytes(this.buffer);
      var1.writeVarInt(this.tileEntityTags.size());

      for(NBTTagCompound var3 : this.tileEntityTags) {
         var1.writeCompoundTag(var3);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleChunkData(this);
   }

   private ByteBuf getWriteBuffer() {
      ByteBuf var1 = Unpooled.wrappedBuffer(this.buffer);
      var1.writerIndex(0);
      return var1;
   }

   public int extractChunkData(PacketBuffer var1, Chunk var2, boolean var3, int var4) {
      int var5 = 0;
      ExtendedBlockStorage[] var6 = var2.getBlockStorageArray();
      int var7 = 0;

      for(int var8 = var6.length; var7 < var8; ++var7) {
         ExtendedBlockStorage var9 = var6[var7];
         if (var9 != Chunk.NULL_BLOCK_STORAGE && (!this.doChunkLoad() || !var9.isEmpty()) && (var4 & 1 << var7) != 0) {
            var5 |= 1 << var7;
            var9.getData().write(var1);
            var1.writeBytes(var9.getBlocklightArray().getData());
            if (var3) {
               var1.writeBytes(var9.getSkylightArray().getData());
            }
         }
      }

      if (this.doChunkLoad()) {
         var1.writeBytes(var2.getBiomeArray());
      }

      return var5;
   }

   protected int calculateChunkSize(Chunk var1, boolean var2, int var3) {
      int var4 = 0;
      ExtendedBlockStorage[] var5 = var1.getBlockStorageArray();
      int var6 = 0;

      for(int var7 = var5.length; var6 < var7; ++var6) {
         ExtendedBlockStorage var8 = var5[var6];
         if (var8 != Chunk.NULL_BLOCK_STORAGE && (!this.doChunkLoad() || !var8.isEmpty()) && (var3 & 1 << var6) != 0) {
            var4 = var4 + var8.getData().getSerializedSize();
            var4 = var4 + var8.getBlocklightArray().getData().length;
            if (var2) {
               var4 += var8.getSkylightArray().getData().length;
            }
         }
      }

      if (this.doChunkLoad()) {
         var4 += var1.getBiomeArray().length;
      }

      return var4;
   }

   public boolean doChunkLoad() {
      return this.loadChunk;
   }
}
