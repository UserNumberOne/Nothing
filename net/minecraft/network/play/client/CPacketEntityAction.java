package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketEntityAction implements Packet {
   private int entityID;
   private CPacketEntityAction.Action action;
   private int auxData;

   public CPacketEntityAction() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketEntityAction(Entity var1, CPacketEntityAction.Action var2) {
      this(entityIn, actionIn, 0);
   }

   @SideOnly(Side.CLIENT)
   public CPacketEntityAction(Entity var1, CPacketEntityAction.Action var2, int var3) {
      this.entityID = entityIn.getEntityId();
      this.action = actionIn;
      this.auxData = auxDataIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityID = buf.readVarInt();
      this.action = (CPacketEntityAction.Action)buf.readEnumValue(CPacketEntityAction.Action.class);
      this.auxData = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityID);
      buf.writeEnumValue(this.action);
      buf.writeVarInt(this.auxData);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processEntityAction(this);
   }

   public CPacketEntityAction.Action getAction() {
      return this.action;
   }

   public int getAuxData() {
      return this.auxData;
   }

   public static enum Action {
      START_SNEAKING,
      STOP_SNEAKING,
      STOP_SLEEPING,
      START_SPRINTING,
      STOP_SPRINTING,
      START_RIDING_JUMP,
      STOP_RIDING_JUMP,
      OPEN_INVENTORY,
      START_FALL_FLYING;
   }
}
