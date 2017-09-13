package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEntityVelocity implements Packet {
   private int entityID;
   private int motionX;
   private int motionY;
   private int motionZ;

   public SPacketEntityVelocity() {
   }

   public SPacketEntityVelocity(Entity var1) {
      this(entityIn.getEntityId(), entityIn.motionX, entityIn.motionY, entityIn.motionZ);
   }

   public SPacketEntityVelocity(int var1, double var2, double var4, double var6) {
      this.entityID = entityIdIn;
      double d0 = 3.9D;
      if (motionXIn < -3.9D) {
         motionXIn = -3.9D;
      }

      if (motionYIn < -3.9D) {
         motionYIn = -3.9D;
      }

      if (motionZIn < -3.9D) {
         motionZIn = -3.9D;
      }

      if (motionXIn > 3.9D) {
         motionXIn = 3.9D;
      }

      if (motionYIn > 3.9D) {
         motionYIn = 3.9D;
      }

      if (motionZIn > 3.9D) {
         motionZIn = 3.9D;
      }

      this.motionX = (int)(motionXIn * 8000.0D);
      this.motionY = (int)(motionYIn * 8000.0D);
      this.motionZ = (int)(motionZIn * 8000.0D);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityID = buf.readVarInt();
      this.motionX = buf.readShort();
      this.motionY = buf.readShort();
      this.motionZ = buf.readShort();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityID);
      buf.writeShort(this.motionX);
      buf.writeShort(this.motionY);
      buf.writeShort(this.motionZ);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleEntityVelocity(this);
   }

   @SideOnly(Side.CLIENT)
   public int getEntityID() {
      return this.entityID;
   }

   @SideOnly(Side.CLIENT)
   public int getMotionX() {
      return this.motionX;
   }

   @SideOnly(Side.CLIENT)
   public int getMotionY() {
      return this.motionY;
   }

   @SideOnly(Side.CLIENT)
   public int getMotionZ() {
      return this.motionZ;
   }
}
