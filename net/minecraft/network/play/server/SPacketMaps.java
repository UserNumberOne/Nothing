package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Collection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.Vec4b;

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
      this.mapId = var1;
      this.mapScale = var2;
      this.trackingPosition = var3;
      this.icons = (Vec4b[])var4.toArray(new Vec4b[var4.size()]);
      this.minX = var6;
      this.minZ = var7;
      this.columns = var8;
      this.rows = var9;
      this.mapDataBytes = new byte[var8 * var9];

      for(int var10 = 0; var10 < var8; ++var10) {
         for(int var11 = 0; var11 < var9; ++var11) {
            this.mapDataBytes[var10 + var11 * var8] = var5[var6 + var10 + (var7 + var11) * 128];
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.mapId = var1.readVarInt();
      this.mapScale = var1.readByte();
      this.trackingPosition = var1.readBoolean();
      this.icons = new Vec4b[var1.readVarInt()];

      for(int var2 = 0; var2 < this.icons.length; ++var2) {
         short var3 = (short)var1.readByte();
         this.icons[var2] = new Vec4b((byte)(var3 >> 4 & 15), var1.readByte(), var1.readByte(), (byte)(var3 & 15));
      }

      this.columns = var1.readUnsignedByte();
      if (this.columns > 0) {
         this.rows = var1.readUnsignedByte();
         this.minX = var1.readUnsignedByte();
         this.minZ = var1.readUnsignedByte();
         this.mapDataBytes = var1.readByteArray();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.mapId);
      var1.writeByte(this.mapScale);
      var1.writeBoolean(this.trackingPosition);
      var1.writeVarInt(this.icons.length);

      for(Vec4b var5 : this.icons) {
         var1.writeByte((var5.getType() & 15) << 4 | var5.getRotation() & 15);
         var1.writeByte(var5.getX());
         var1.writeByte(var5.getY());
      }

      var1.writeByte(this.columns);
      if (this.columns > 0) {
         var1.writeByte(this.rows);
         var1.writeByte(this.minX);
         var1.writeByte(this.minZ);
         var1.writeByteArray(this.mapDataBytes);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleMaps(this);
   }
}
