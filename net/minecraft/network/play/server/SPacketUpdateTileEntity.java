package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;

public class SPacketUpdateTileEntity implements Packet {
   private BlockPos blockPos;
   private int tileEntityType;
   private NBTTagCompound nbt;

   public SPacketUpdateTileEntity() {
   }

   public SPacketUpdateTileEntity(BlockPos var1, int var2, NBTTagCompound var3) {
      this.blockPos = var1;
      this.tileEntityType = var2;
      this.nbt = var3;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.blockPos = var1.readBlockPos();
      this.tileEntityType = var1.readUnsignedByte();
      this.nbt = var1.readCompoundTag();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBlockPos(this.blockPos);
      var1.writeByte((byte)this.tileEntityType);
      var1.writeCompoundTag(this.nbt);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleUpdateTileEntity(this);
   }
}
