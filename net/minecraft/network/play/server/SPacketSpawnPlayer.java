package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketSpawnPlayer implements Packet {
   private int entityId;
   private UUID uniqueId;
   private double x;
   private double y;
   private double z;
   private byte yaw;
   private byte pitch;
   private EntityDataManager watcher;
   private List dataManagerEntries;

   public SPacketSpawnPlayer() {
   }

   public SPacketSpawnPlayer(EntityPlayer var1) {
      this.entityId = var1.getEntityId();
      this.uniqueId = var1.getGameProfile().getId();
      this.x = var1.posX;
      this.y = var1.posY;
      this.z = var1.posZ;
      this.yaw = (byte)((int)(var1.rotationYaw * 256.0F / 360.0F));
      this.pitch = (byte)((int)(var1.rotationPitch * 256.0F / 360.0F));
      this.watcher = var1.getDataManager();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.uniqueId = var1.readUniqueId();
      this.x = var1.readDouble();
      this.y = var1.readDouble();
      this.z = var1.readDouble();
      this.yaw = var1.readByte();
      this.pitch = var1.readByte();
      this.dataManagerEntries = EntityDataManager.readEntries(var1);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeUniqueId(this.uniqueId);
      var1.writeDouble(this.x);
      var1.writeDouble(this.y);
      var1.writeDouble(this.z);
      var1.writeByte(this.yaw);
      var1.writeByte(this.pitch);
      this.watcher.writeEntries(var1);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSpawnPlayer(this);
   }
}
