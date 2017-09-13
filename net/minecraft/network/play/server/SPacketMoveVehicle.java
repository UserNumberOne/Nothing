package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketMoveVehicle implements Packet {
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;

   public SPacketMoveVehicle() {
   }

   public SPacketMoveVehicle(Entity var1) {
      this.x = entityIn.posX;
      this.y = entityIn.posY;
      this.z = entityIn.posZ;
      this.yaw = entityIn.rotationYaw;
      this.pitch = entityIn.rotationPitch;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.x = buf.readDouble();
      this.y = buf.readDouble();
      this.z = buf.readDouble();
      this.yaw = buf.readFloat();
      this.pitch = buf.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.writeFloat(this.yaw);
      buf.writeFloat(this.pitch);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleMoveVehicle(this);
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
   public float getYaw() {
      return this.yaw;
   }

   @SideOnly(Side.CLIENT)
   public float getPitch() {
      return this.pitch;
   }
}
