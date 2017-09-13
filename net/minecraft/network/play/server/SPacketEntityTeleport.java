package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEntityTeleport implements Packet {
   private int entityId;
   private double posX;
   private double posY;
   private double posZ;
   private byte yaw;
   private byte pitch;
   private boolean onGround;

   public SPacketEntityTeleport() {
   }

   public SPacketEntityTeleport(Entity var1) {
      this.entityId = var1.getEntityId();
      this.posX = var1.posX;
      this.posY = var1.posY;
      this.posZ = var1.posZ;
      this.yaw = (byte)((int)(var1.rotationYaw * 256.0F / 360.0F));
      this.pitch = (byte)((int)(var1.rotationPitch * 256.0F / 360.0F));
      this.onGround = var1.onGround;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.posX = var1.readDouble();
      this.posY = var1.readDouble();
      this.posZ = var1.readDouble();
      this.yaw = var1.readByte();
      this.pitch = var1.readByte();
      this.onGround = var1.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeDouble(this.posX);
      var1.writeDouble(this.posY);
      var1.writeDouble(this.posZ);
      var1.writeByte(this.yaw);
      var1.writeByte(this.pitch);
      var1.writeBoolean(this.onGround);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityTeleport(this);
   }

   @SideOnly(Side.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }

   @SideOnly(Side.CLIENT)
   public double getX() {
      return this.posX;
   }

   @SideOnly(Side.CLIENT)
   public double getY() {
      return this.posY;
   }

   @SideOnly(Side.CLIENT)
   public double getZ() {
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
   public boolean getOnGround() {
      return this.onGround;
   }
}
