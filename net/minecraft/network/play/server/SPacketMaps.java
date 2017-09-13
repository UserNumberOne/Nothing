package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Collection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.Vec4b;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketMaps implements Packet {
   private int mapId;
   private byte mapScale;
   private boolean trackingPosition;
   private Vec4b[] icons;
   private int minX;
   private int minZ;
   private int columns;
   private int rows;
   private byte[] mapDataBytes;

   public SPacketMaps() {
   }

   public SPacketMaps(int var1, byte var2, boolean var3, Collection var4, byte[] var5, int var6, int var7, int var8, int var9) {
      this.mapId = mapIdIn;
      this.mapScale = mapScaleIn;
      this.trackingPosition = trackingPositionIn;
      this.icons = (Vec4b[])iconsIn.toArray(new Vec4b[iconsIn.size()]);
      this.minX = minXIn;
      this.minZ = minZIn;
      this.columns = columnsIn;
      this.rows = rowsIn;
      this.mapDataBytes = new byte[columnsIn * rowsIn];

      for(int i = 0; i < columnsIn; ++i) {
         for(int j = 0; j < rowsIn; ++j) {
            this.mapDataBytes[i + j * columnsIn] = p_i46937_5_[minXIn + i + (minZIn + j) * 128];
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.mapId = buf.readVarInt();
      this.mapScale = buf.readByte();
      this.trackingPosition = buf.readBoolean();
      this.icons = new Vec4b[buf.readVarInt()];

      for(int i = 0; i < this.icons.length; ++i) {
         short short1 = (short)buf.readByte();
         this.icons[i] = new Vec4b((byte)(short1 >> 4 & 15), buf.readByte(), buf.readByte(), (byte)(short1 & 15));
      }

      this.columns = buf.readUnsignedByte();
      if (this.columns > 0) {
         this.rows = buf.readUnsignedByte();
         this.minX = buf.readUnsignedByte();
         this.minZ = buf.readUnsignedByte();
         this.mapDataBytes = buf.readByteArray();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.mapId);
      buf.writeByte(this.mapScale);
      buf.writeBoolean(this.trackingPosition);
      buf.writeVarInt(this.icons.length);

      for(Vec4b vec4b : this.icons) {
         buf.writeByte((vec4b.getType() & 15) << 4 | vec4b.getRotation() & 15);
         buf.writeByte(vec4b.getX());
         buf.writeByte(vec4b.getY());
      }

      buf.writeByte(this.columns);
      if (this.columns > 0) {
         buf.writeByte(this.rows);
         buf.writeByte(this.minX);
         buf.writeByte(this.minZ);
         buf.writeByteArray(this.mapDataBytes);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleMaps(this);
   }

   @SideOnly(Side.CLIENT)
   public int getMapId() {
      return this.mapId;
   }

   @SideOnly(Side.CLIENT)
   public void setMapdataTo(MapData var1) {
      mapdataIn.scale = this.mapScale;
      mapdataIn.trackingPosition = this.trackingPosition;
      mapdataIn.mapDecorations.clear();

      for(int i = 0; i < this.icons.length; ++i) {
         Vec4b vec4b = this.icons[i];
         mapdataIn.mapDecorations.put("icon-" + i, vec4b);
      }

      for(int j = 0; j < this.columns; ++j) {
         for(int k = 0; k < this.rows; ++k) {
            mapdataIn.colors[this.minX + j + (this.minZ + k) * 128] = this.mapDataBytes[j + k * this.columns];
         }
      }

   }
}
