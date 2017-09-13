package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketCamera implements Packet {
   public int entityId;

   public SPacketCamera() {
   }

   public SPacketCamera(Entity var1) {
      this.entityId = var1.getEntityId();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleCamera(this);
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public Entity getEntity(World var1) {
      return var1.getEntityByID(this.entityId);
   }
}
