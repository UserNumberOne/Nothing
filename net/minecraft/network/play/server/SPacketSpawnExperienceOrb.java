package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketSpawnExperienceOrb implements Packet {
   private int entityID;
   private double posX;
   private double posY;
   private double posZ;
   private int xpValue;

   public SPacketSpawnExperienceOrb() {
   }

   public SPacketSpawnExperienceOrb(EntityXPOrb var1) {
      this.entityID = var1.getEntityId();
      this.posX = var1.posX;
      this.posY = var1.posY;
      this.posZ = var1.posZ;
      this.xpValue = var1.getXpValue();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityID = var1.readVarInt();
      this.posX = var1.readDouble();
      this.posY = var1.readDouble();
      this.posZ = var1.readDouble();
      this.xpValue = var1.readShort();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityID);
      var1.writeDouble(this.posX);
      var1.writeDouble(this.posY);
      var1.writeDouble(this.posZ);
      var1.writeShort(this.xpValue);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSpawnExperienceOrb(this);
   }
}
