package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketPlayer implements Packet {
   protected double x;
   protected double y;
   protected double z;
   protected float yaw;
   protected float pitch;
   protected boolean onGround;
   protected boolean moving;
   protected boolean rotating;

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processPlayer(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.onGround = var1.readUnsignedByte() != 0;
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.onGround ? 1 : 0);
   }

   public double getX(double var1) {
      return this.moving ? this.x : var1;
   }

   public double getY(double var1) {
      return this.moving ? this.y : var1;
   }

   public double getZ(double var1) {
      return this.moving ? this.z : var1;
   }

   public float getYaw(float var1) {
      return this.rotating ? this.yaw : var1;
   }

   public float getPitch(float var1) {
      return this.rotating ? this.pitch : var1;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public static class Position extends CPacketPlayer {
      public Position() {
         this.moving = true;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         this.x = var1.readDouble();
         this.y = var1.readDouble();
         this.z = var1.readDouble();
         super.readPacketData(var1);
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         var1.writeDouble(this.x);
         var1.writeDouble(this.y);
         var1.writeDouble(this.z);
         super.writePacketData(var1);
      }
   }

   public static class PositionRotation extends CPacketPlayer {
      public PositionRotation() {
         this.moving = true;
         this.rotating = true;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         this.x = var1.readDouble();
         this.y = var1.readDouble();
         this.z = var1.readDouble();
         this.yaw = var1.readFloat();
         this.pitch = var1.readFloat();
         super.readPacketData(var1);
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         var1.writeDouble(this.x);
         var1.writeDouble(this.y);
         var1.writeDouble(this.z);
         var1.writeFloat(this.yaw);
         var1.writeFloat(this.pitch);
         super.writePacketData(var1);
      }
   }

   public static class Rotation extends CPacketPlayer {
      public Rotation() {
         this.rotating = true;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         this.yaw = var1.readFloat();
         this.pitch = var1.readFloat();
         super.readPacketData(var1);
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         var1.writeFloat(this.yaw);
         var1.writeFloat(this.pitch);
         super.writePacketData(var1);
      }
   }
}
