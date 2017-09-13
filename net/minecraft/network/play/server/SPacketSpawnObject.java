package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSpawnObject implements Packet {
   private int entityId;
   private UUID uniqueId;
   private double x;
   private double y;
   private double z;
   private int speedX;
   private int speedY;
   private int speedZ;
   private int pitch;
   private int yaw;
   private int type;
   private int data;

   public SPacketSpawnObject() {
   }

   public SPacketSpawnObject(Entity var1, int var2) {
      this(var1, var2, 0);
   }

   public SPacketSpawnObject(Entity var1, int var2, int var3) {
      this.entityId = var1.getEntityId();
      this.uniqueId = var1.getUniqueID();
      this.x = var1.posX;
      this.y = var1.posY;
      this.z = var1.posZ;
      this.pitch = MathHelper.floor(var1.rotationPitch * 256.0F / 360.0F);
      this.yaw = MathHelper.floor(var1.rotationYaw * 256.0F / 360.0F);
      this.type = var2;
      this.data = var3;
      double var4 = 3.9D;
      this.speedX = (int)(MathHelper.clamp(var1.motionX, -3.9D, 3.9D) * 8000.0D);
      this.speedY = (int)(MathHelper.clamp(var1.motionY, -3.9D, 3.9D) * 8000.0D);
      this.speedZ = (int)(MathHelper.clamp(var1.motionZ, -3.9D, 3.9D) * 8000.0D);
   }

   public SPacketSpawnObject(Entity var1, int var2, int var3, BlockPos var4) {
      this(var1, var2, var3);
      this.x = (double)var4.getX();
      this.y = (double)var4.getY();
      this.z = (double)var4.getZ();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.uniqueId = var1.readUniqueId();
      this.type = var1.readByte();
      this.x = var1.readDouble();
      this.y = var1.readDouble();
      this.z = var1.readDouble();
      this.pitch = var1.readByte();
      this.yaw = var1.readByte();
      this.data = var1.readInt();
      this.speedX = var1.readShort();
      this.speedY = var1.readShort();
      this.speedZ = var1.readShort();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeUniqueId(this.uniqueId);
      var1.writeByte(this.type);
      var1.writeDouble(this.x);
      var1.writeDouble(this.y);
      var1.writeDouble(this.z);
      var1.writeByte(this.pitch);
      var1.writeByte(this.yaw);
      var1.writeInt(this.data);
      var1.writeShort(this.speedX);
      var1.writeShort(this.speedY);
      var1.writeShort(this.speedZ);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSpawnObject(this);
   }

   @SideOnly(Side.CLIENT)
   public int getEntityID() {
      return this.entityId;
   }

   public void setSpeedX(int var1) {
      this.speedX = var1;
   }

   @SideOnly(Side.CLIENT)
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public void setSpeedY(int var1) {
      this.speedY = var1;
   }

   @SideOnly(Side.CLIENT)
   public double getX() {
      return this.x;
   }

   public void setSpeedZ(int var1) {
      this.speedZ = var1;
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
   public int getSpeedX() {
      return this.speedX;
   }

   @SideOnly(Side.CLIENT)
   public int getSpeedY() {
      return this.speedY;
   }

   @SideOnly(Side.CLIENT)
   public int getSpeedZ() {
      return this.speedZ;
   }

   @SideOnly(Side.CLIENT)
   public int getPitch() {
      return this.pitch;
   }

   @SideOnly(Side.CLIENT)
   public int getYaw() {
      return this.yaw;
   }

   @SideOnly(Side.CLIENT)
   public int getType() {
      return this.type;
   }

   @SideOnly(Side.CLIENT)
   public int getData() {
      return this.data;
   }

   @SideOnly(Side.CLIENT)
   public void setData(int var1) {
      this.data = var1;
   }
}
