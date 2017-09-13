package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
      this.entityId = entityIdIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleEntityMovement(this);
   }

   public String toString() {
      return "Entity_" + super.toString();
   }

   @SideOnly(Side.CLIENT)
   public Entity getEntity(World var1) {
      return worldIn.getEntityByID(this.entityId);
   }

   @SideOnly(Side.CLIENT)
   public int getX() {
      return this.posX;
   }

   @SideOnly(Side.CLIENT)
   public int getY() {
      return this.posY;
   }

   @SideOnly(Side.CLIENT)
   public int getZ() {
      return this.posZ;
   }

   @SideOnly(Side.CLIENT)
   public byte getYaw() {
      return this.yaw;
   }

   @SideOnly(Side.CLIENT)
   public byte getPitch() {
      return this.pitch;
   }

   @SideOnly(Side.CLIENT)
   public boolean isRotating() {
      return this.rotating;
   }

   @SideOnly(Side.CLIENT)
   public boolean getOnGround() {
      return this.onGround;
   }

   public static class S15PacketEntityRelMove extends SPacketEntity {
      public S15PacketEntityRelMove() {
      }

      public S15PacketEntityRelMove(int var1, long var2, long var4, long var6, boolean var8) {
         super(entityIdIn);
         this.posX = (int)xIn;
         this.posY = (int)yIn;
         this.posZ = (int)zIn;
         this.onGround = onGroundIn;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         super.readPacketData(buf);
         this.posX = buf.readShort();
         this.posY = buf.readShort();
         this.posZ = buf.readShort();
         this.onGround = buf.readBoolean();
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         super.writePacketData(buf);
         buf.writeShort(this.posX);
         buf.writeShort(this.posY);
         buf.writeShort(this.posZ);
         buf.writeBoolean(this.onGround);
      }
   }

   public static class S16PacketEntityLook extends SPacketEntity {
      public S16PacketEntityLook() {
         this.rotating = true;
      }

      public S16PacketEntityLook(int var1, byte var2, byte var3, boolean var4) {
         super(entityIdIn);
         this.yaw = yawIn;
         this.pitch = pitchIn;
         this.rotating = true;
         this.onGround = onGroundIn;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         super.readPacketData(buf);
         this.yaw = buf.readByte();
         this.pitch = buf.readByte();
         this.onGround = buf.readBoolean();
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         super.writePacketData(buf);
         buf.writeByte(this.yaw);
         buf.writeByte(this.pitch);
         buf.writeBoolean(this.onGround);
      }
   }

   public static class S17PacketEntityLookMove extends SPacketEntity {
      public S17PacketEntityLookMove() {
         this.rotating = true;
      }

      public S17PacketEntityLookMove(int var1, long var2, long var4, long var6, byte var8, byte var9, boolean var10) {
         super(entityIdIn);
         this.posX = (int)xIn;
         this.posY = (int)yIn;
         this.posZ = (int)zIn;
         this.yaw = yawIn;
         this.pitch = pitchIn;
         this.onGround = onGroundIn;
         this.rotating = true;
      }

      public void readPacketData(PacketBuffer var1) throws IOException {
         super.readPacketData(buf);
         this.posX = buf.readShort();
         this.posY = buf.readShort();
         this.posZ = buf.readShort();
         this.yaw = buf.readByte();
         this.pitch = buf.readByte();
         this.onGround = buf.readBoolean();
      }

      public void writePacketData(PacketBuffer var1) throws IOException {
         super.writePacketData(buf);
         buf.writeShort(this.posX);
         buf.writeShort(this.posY);
         buf.writeShort(this.posZ);
         buf.writeByte(this.yaw);
         buf.writeByte(this.pitch);
         buf.writeBoolean(this.onGround);
      }
   }
}
