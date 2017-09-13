package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketPlayerTryUseItemOnBlock implements Packet {
   private BlockPos position;
   private EnumFacing placedBlockDirection;
   private EnumHand hand;
   private float facingX;
   private float facingY;
   private float facingZ;

   public CPacketPlayerTryUseItemOnBlock() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketPlayerTryUseItemOnBlock(BlockPos var1, EnumFacing var2, EnumHand var3, float var4, float var5, float var6) {
      this.position = posIn;
      this.placedBlockDirection = placedBlockDirectionIn;
      this.hand = handIn;
      this.facingX = facingXIn;
      this.facingY = facingYIn;
      this.facingZ = facingZIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.position = buf.readBlockPos();
      this.placedBlockDirection = (EnumFacing)buf.readEnumValue(EnumFacing.class);
      this.hand = (EnumHand)buf.readEnumValue(EnumHand.class);
      this.facingX = (float)buf.readUnsignedByte() / 16.0F;
      this.facingY = (float)buf.readUnsignedByte() / 16.0F;
      this.facingZ = (float)buf.readUnsignedByte() / 16.0F;
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeBlockPos(this.position);
      buf.writeEnumValue(this.placedBlockDirection);
      buf.writeEnumValue(this.hand);
      buf.writeByte((int)(this.facingX * 16.0F));
      buf.writeByte((int)(this.facingY * 16.0F));
      buf.writeByte((int)(this.facingZ * 16.0F));
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processTryUseItemOnBlock(this);
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
