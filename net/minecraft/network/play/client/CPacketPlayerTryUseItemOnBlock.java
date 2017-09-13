package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class CPacketPlayerTryUseItemOnBlock implements Packet {
   private BlockPos position;
   private EnumFacing placedBlockDirection;
   private EnumHand hand;
   private float facingX;
   private float facingY;
   private float facingZ;

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.position = var1.readBlockPos();
      this.placedBlockDirection = (EnumFacing)var1.readEnumValue(EnumFacing.class);
      this.hand = (EnumHand)var1.readEnumValue(EnumHand.class);
      this.facingX = (float)var1.readUnsignedByte() / 16.0F;
      this.facingY = (float)var1.readUnsignedByte() / 16.0F;
      this.facingZ = (float)var1.readUnsignedByte() / 16.0F;
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBlockPos(this.position);
      var1.writeEnumValue(this.placedBlockDirection);
      var1.writeEnumValue(this.hand);
      var1.writeByte((int)(this.facingX * 16.0F));
      var1.writeByte((int)(this.facingY * 16.0F));
      var1.writeByte((int)(this.facingZ * 16.0F));
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processTryUseItemOnBlock(this);
   }

   public BlockPos getPos() {
      return this.position;
   }

   public EnumFacing getDirection() {
      return this.placedBlockDirection;
   }

   public EnumHand getHand() {
      return this.hand;
   }

   public float getFacingX() {
      return this.facingX;
   }

   public float getFacingY() {
      return this.facingY;
   }

   public float getFacingZ() {
      return this.facingZ;
   }
}
