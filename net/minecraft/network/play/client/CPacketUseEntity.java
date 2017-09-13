package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketUseEntity implements Packet {
   private int entityId;
   private CPacketUseEntity.Action action;
   private Vec3d hitVec;
   private EnumHand hand;

   public CPacketUseEntity() {
   }

   public CPacketUseEntity(Entity var1) {
      this.entityId = var1.getEntityId();
      this.action = CPacketUseEntity.Action.ATTACK;
   }

   @SideOnly(Side.CLIENT)
   public CPacketUseEntity(Entity var1, EnumHand var2) {
      this.entityId = var1.getEntityId();
      this.action = CPacketUseEntity.Action.INTERACT;
      this.hand = var2;
   }

   @SideOnly(Side.CLIENT)
   public CPacketUseEntity(Entity var1, EnumHand var2, Vec3d var3) {
      this.entityId = var1.getEntityId();
      this.action = CPacketUseEntity.Action.INTERACT_AT;
      this.hand = var2;
      this.hitVec = var3;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.action = (CPacketUseEntity.Action)var1.readEnumValue(CPacketUseEntity.Action.class);
      if (this.action == CPacketUseEntity.Action.INTERACT_AT) {
         this.hitVec = new Vec3d((double)var1.readFloat(), (double)var1.readFloat(), (double)var1.readFloat());
      }

      if (this.action == CPacketUseEntity.Action.INTERACT || this.action == CPacketUseEntity.Action.INTERACT_AT) {
         this.hand = (EnumHand)var1.readEnumValue(EnumHand.class);
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeEnumValue(this.action);
      if (this.action == CPacketUseEntity.Action.INTERACT_AT) {
         var1.writeFloat((float)this.hitVec.xCoord);
         var1.writeFloat((float)this.hitVec.yCoord);
         var1.writeFloat((float)this.hitVec.zCoord);
      }

      if (this.action == CPacketUseEntity.Action.INTERACT || this.action == CPacketUseEntity.Action.INTERACT_AT) {
         var1.writeEnumValue(this.hand);
      }

   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processUseEntity(this);
   }

   public Entity getEntityFromWorld(World var1) {
      return var1.getEntityByID(this.entityId);
   }

   public CPacketUseEntity.Action getAction() {
      return this.action;
   }

   public EnumHand getHand() {
      return this.hand;
   }

   public Vec3d getHitVec() {
      return this.hitVec;
   }

   public static enum Action {
      INTERACT,
      ATTACK,
      INTERACT_AT;
   }
}
