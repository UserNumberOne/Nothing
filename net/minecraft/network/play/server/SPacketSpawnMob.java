package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSpawnMob implements Packet {
   private int entityId;
   private UUID uniqueId;
   private int type;
   private double x;
   private double y;
   private double z;
   private int velocityX;
   private int velocityY;
   private int velocityZ;
   private byte yaw;
   private byte pitch;
   private byte headPitch;
   private EntityDataManager dataManager;
   private List dataManagerEntries;

   public SPacketSpawnMob() {
   }

   public SPacketSpawnMob(EntityLivingBase var1) {
      this.entityId = var1.getEntityId();
      this.uniqueId = var1.getUniqueID();
      this.type = (byte)EntityList.getEntityID(var1);
      this.x = var1.posX;
      this.y = var1.posY;
      this.z = var1.posZ;
      this.yaw = (byte)((int)(var1.rotationYaw * 256.0F / 360.0F));
      this.pitch = (byte)((int)(var1.rotationPitch * 256.0F / 360.0F));
      this.headPitch = (byte)((int)(var1.rotationYawHead * 256.0F / 360.0F));
      double var2 = 3.9D;
      double var4 = var1.motionX;
      double var6 = var1.motionY;
      double var8 = var1.motionZ;
      if (var4 < -3.9D) {
         var4 = -3.9D;
      }

      if (var6 < -3.9D) {
         var6 = -3.9D;
      }

      if (var8 < -3.9D) {
         var8 = -3.9D;
      }

      if (var4 > 3.9D) {
         var4 = 3.9D;
      }

      if (var6 > 3.9D) {
         var6 = 3.9D;
      }

      if (var8 > 3.9D) {
         var8 = 3.9D;
      }

      this.velocityX = (int)(var4 * 8000.0D);
      this.velocityY = (int)(var6 * 8000.0D);
      this.velocityZ = (int)(var8 * 8000.0D);
      this.dataManager = var1.getDataManager();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.uniqueId = var1.readUniqueId();
      this.type = var1.readByte() & 255;
      this.x = var1.readDouble();
      this.y = var1.readDouble();
      this.z = var1.readDouble();
      this.yaw = var1.readByte();
      this.pitch = var1.readByte();
      this.headPitch = var1.readByte();
      this.velocityX = var1.readShort();
      this.velocityY = var1.readShort();
      this.velocityZ = var1.readShort();
      this.dataManagerEntries = EntityDataManager.readEntries(var1);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeUniqueId(this.uniqueId);
      var1.writeByte(this.type & 255);
      var1.writeDouble(this.x);
      var1.writeDouble(this.y);
      var1.writeDouble(this.z);
      var1.writeByte(this.yaw);
      var1.writeByte(this.pitch);
      var1.writeByte(this.headPitch);
      var1.writeShort(this.velocityX);
      var1.writeShort(this.velocityY);
      var1.writeShort(this.velocityZ);
      this.dataManager.writeEntries(var1);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSpawnMob(this);
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public List getDataManagerEntries() {
      if (this.dataManagerEntries == null) {
         this.dataManagerEntries = this.dataManager.getAll();
      }

      return this.dataManagerEntries;
   }

   @SideOnly(Side.CLIENT)
   public int getEntityID() {
      return this.entityId;
   }

   @SideOnly(Side.CLIENT)
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @SideOnly(Side.CLIENT)
   public int getEntityType() {
      return this.type;
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
   public int getVelocityX() {
      return this.velocityX;
   }

   @SideOnly(Side.CLIENT)
   public int getVelocityY() {
      return this.velocityY;
   }

   @SideOnly(Side.CLIENT)
   public int getVelocityZ() {
      return this.velocityZ;
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
   public byte getHeadPitch() {
      return this.headPitch;
   }
}
