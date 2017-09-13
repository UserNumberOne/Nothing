package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketPlayerPosLook implements Packet {
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;
   private Set flags;
   private int teleportId;

   public SPacketPlayerPosLook() {
   }

   public SPacketPlayerPosLook(double var1, double var3, double var5, float var7, float var8, Set var9, int var10) {
      this.x = var1;
      this.y = var3;
      this.z = var5;
      this.yaw = var7;
      this.pitch = var8;
      this.flags = var9;
      this.teleportId = var10;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.x = var1.readDouble();
      this.y = var1.readDouble();
      this.z = var1.readDouble();
      this.yaw = var1.readFloat();
      this.pitch = var1.readFloat();
      this.flags = SPacketPlayerPosLook.EnumFlags.unpack(var1.readUnsignedByte());
      this.teleportId = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeDouble(this.x);
      var1.writeDouble(this.y);
      var1.writeDouble(this.z);
      var1.writeFloat(this.yaw);
      var1.writeFloat(this.pitch);
      var1.writeByte(SPacketPlayerPosLook.EnumFlags.pack(this.flags));
      var1.writeVarInt(this.teleportId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handlePlayerPosLook(this);
   }

   @SideOnly(Side.CLIENT)
   public double getX() {
      return this.x;
   }

   @SideOnly(Side.CLIENT)
   public double getY() {
      return this.y;
   }

   @SideOnly(Side.CLIENT)
   public double getZ() {
      return this.z;
   }

   @SideOnly(Side.CLIENT)
   public float getYaw() {
      return this.yaw;
   }

   @SideOnly(Side.CLIENT)
   public float getPitch() {
      return this.pitch;
   }

   @SideOnly(Side.CLIENT)
   public int getTeleportId() {
      return this.teleportId;
   }

   @SideOnly(Side.CLIENT)
   public Set getFlags() {
      return this.flags;
   }

   public static enum EnumFlags {
      X(0),
      Y(1),
      Z(2),
      Y_ROT(3),
      X_ROT(4);

      private final int bit;

      private EnumFlags(int var3) {
         this.bit = var3;
      }

      private int getMask() {
         return 1 << this.bit;
      }

      private boolean isSet(int var1) {
         return (var1 & this.getMask()) == this.getMask();
      }

      public static Set unpack(int var0) {
         EnumSet var1 = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

         for(SPacketPlayerPosLook.EnumFlags var5 : values()) {
            if (var5.isSet(var0)) {
               var1.add(var5);
            }
         }

         return var1;
      }

      public static int pack(Set var0) {
         int var1 = 0;

         for(SPacketPlayerPosLook.EnumFlags var3 : var0) {
            var1 |= var3.getMask();
         }

         return var1;
      }
   }
}
