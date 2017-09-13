package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEntityHeadLook implements Packet {
   private int entityId;
   private byte yaw;

   public SPacketEntityHeadLook() {
   }

   public SPacketEntityHeadLook(Entity var1, byte var2) {
      this.entityId = entityIn.getEntityId();
      this.yaw = yawIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = buf.readVarInt();
      this.yaw = buf.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityId);
      buf.writeByte(this.yaw);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleEntityHeadLook(this);
   }

   @SideOnly(Side.CLIENT)
   public Entity getEntity(World var1) {
      return worldIn.getEntityByID(this.entityId);
   }

   @SideOnly(Side.CLIENT)
   public byte getYaw() {
      return this.yaw;
   }
}
