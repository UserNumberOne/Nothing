package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketPlayer implements Packet {
   protected double x;
   protected double y;
   protected double z;
   protected float yaw;
   protected float pitch;
   protected boolean onGround;
   protected boolean moving;
   protected boolean rotating;

   public CPacketPlayer() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketPlayer(boolean var1) {
      this.onGround = var1;
   }

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

      @SideOnly(Side.CLIENT)
      public Position(double var1, double var3, double var5, boolean var7) {
         this.x = var1;
         this.y = var3;
         this.z = var5;
         this.onGround = var7;
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

      @SideOnly(Side.CLIENT)
      public PositionRotation(double var1, double var3, double var5, float var7, float var8, boolean var9) {
         this.x = var1;
         this.y = var3;
         this.z = var5;
         this.yaw = var7;
         this.pitch = var8;
         this.onGround = var9;
         this.rotating = true;
         this.moving = true;
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

      @SideOnly(Side.CLIENT)
      public Rotation(float var1, float var2, boolean var3) {
         this.yaw = var1;
         this.pitch = var2;
         this.onGround = var3;
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
