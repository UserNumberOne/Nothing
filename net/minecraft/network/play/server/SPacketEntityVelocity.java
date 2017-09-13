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
      this(var1.getEntityId(), var1.motionX, var1.motionY, var1.motionZ);
   }

   public SPacketEntityVelocity(int var1, double var2, double var4, double var6) {
      this.entityID = var1;
      double var8 = 3.9D;
      if (var2 < -3.9D) {
         var2 = -3.9D;
      }

      if (var4 < -3.9D) {
         var4 = -3.9D;
      }

      if (var6 < -3.9D) {
         var6 = -3.9D;
      }

      if (var2 > 3.9D) {
         var2 = 3.9D;
      }

      if (var4 > 3.9D) {
         var4 = 3.9D;
      }

      if (var6 > 3.9D) {
         var6 = 3.9D;
      }

      this.motionX = (int)(var2 * 8000.0D);
      this.motionY = (int)(var4 * 8000.0D);
      this.motionZ = (int)(var6 * 8000.0D);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityID = var1.readVarInt();
      this.motionX = var1.readShort();
      this.motionY = var1.readShort();
      this.motionZ = var1.readShort();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityID);
      var1.writeShort(this.motionX);
      var1.writeShort(this.motionY);
      var1.writeShort(this.motionZ);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityVelocity(this);
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
