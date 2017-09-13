package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class SPacketSpawnPainting implements Packet {
   private int entityID;
   private UUID uniqueId;
   private BlockPos position;
   private EnumFacing facing;
   private String title;

   public SPacketSpawnPainting() {
   }

   public SPacketSpawnPainting(EntityPainting var1) {
      this.entityID = var1.getEntityId();
      this.uniqueId = var1.getUniqueID();
      this.position = var1.getHangingPosition();
      this.facing = var1.facingDirection;
      this.title = var1.art.title;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityID = var1.readVarInt();
      this.uniqueId = var1.readUniqueId();
      this.title = var1.readString(EntityPainting.EnumArt.MAX_NAME_LENGTH);
      this.position = var1.readBlockPos();
      this.facing = EnumFacing.getHorizontal(var1.readUnsignedByte());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityID);
      var1.writeUniqueId(this.uniqueId);
      var1.writeString(this.title);
      var1.writeBlockPos(this.position);
      var1.writeByte(this.facing.getHorizontalIndex());
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSpawnPainting(this);
   }
}
