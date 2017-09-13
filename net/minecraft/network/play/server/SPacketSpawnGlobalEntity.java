package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSpawnGlobalEntity implements Packet {
   private int entityId;
   private double x;
   private double y;
   private double z;
   private int type;

   public SPacketSpawnGlobalEntity() {
   }

   public SPacketSpawnGlobalEntity(Entity var1) {
      this.entityId = var1.getEntityId();
      this.x = var1.posX;
      this.y = var1.posY;
      this.z = var1.posZ;
      if (var1 instanceof EntityLightningBolt) {
         this.type = 1;
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.type = var1.readByte();
      this.x = var1.readDouble();
      this.y = var1.readDouble();
      this.z = var1.readDouble();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeByte(this.type);
      var1.writeDouble(this.x);
      var1.writeDouble(this.y);
      var1.writeDouble(this.z);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSpawnGlobalEntity(this);
   }

   @SideOnly(Side.CLIENT)
   public int getEntityId() {
      return this.entityId;
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
   public int getType() {
      return this.type;
   }
}
