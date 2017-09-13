package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketEntity implements Packet {
   protected int entityId;
   protected int posX;
   protected int posY;
   protected int posZ;
   protected byte yaw;
   protected byte pitch;
   protected boolean onGround;
   protected boolean rotating;

   public SPacketEntity() {
   }

   public SPacketEntity(int var1) {
      this.entityId = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityMovement(this);
   }

   public String toString() {
      return "Entity_" + super.toString();
   }

   public static class S15PacketEntityRelMove extends SPacketEntity {
      public S15PacketEntityRelMove() {
      }

      public S15PacketEntityRelMove(int var1, long var2, long var4, long var6, boolean var8) {
         super(var1);
         this.posX = (int)var2;
         this.posY = (int)var4;
         this.posZ = (int)var6;
         this.onGround = var8;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         super.readPacketData(var1);
         this.posX = var1.readShort();
         this.posY = var1.readShort();
         this.posZ = var1.readShort();
         this.onGround = var1.readBoolean();
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         super.writePacketData(var1);
         var1.writeShort(this.posX);
         var1.writeShort(this.posY);
         var1.writeShort(this.posZ);
         var1.writeBoolean(this.onGround);
      }
   }

   public static class S16PacketEntityLook extends SPacketEntity {
      public S16PacketEntityLook() {
         this.rotating = true;
      }

      public S16PacketEntityLook(int var1, byte var2, byte var3, boolean var4) {
         super(var1);
         this.yaw = var2;
         this.pitch = var3;
         this.rotating = true;
         this.onGround = var4;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         super.readPacketData(var1);
         this.yaw = var1.readByte();
         this.pitch = var1.readByte();
         this.onGround = var1.readBoolean();
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         super.writePacketData(var1);
         var1.writeByte(this.yaw);
         var1.writeByte(this.pitch);
         var1.writeBoolean(this.onGround);
      }
   }

   public static class S17PacketEntityLookMove extends SPacketEntity {
      public S17PacketEntityLookMove() {
         this.rotating = true;
      }

      public S17PacketEntityLookMove(int var1, long var2, long var4, long var6, byte var8, byte var9, boolean var10) {
         super(var1);
         this.posX = (int)var2;
         this.posY = (int)var4;
         this.posZ = (int)var6;
         this.yaw = var8;
         this.pitch = var9;
         this.onGround = var10;
         this.rotating = true;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         super.readPacketData(var1);
         this.posX = var1.readShort();
         this.posY = var1.readShort();
         this.posZ = var1.readShort();
         this.yaw = var1.readByte();
         this.pitch = var1.readByte();
         this.onGround = var1.readBoolean();
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         super.writePacketData(var1);
         var1.writeShort(this.posX);
         var1.writeShort(this.posY);
         var1.writeShort(this.posZ);
         var1.writeByte(this.yaw);
         var1.writeByte(this.pitch);
         var1.writeBoolean(this.onGround);
      }
   }
}
