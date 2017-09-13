package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketWorldBorder implements Packet {
   private SPacketWorldBorder.Action action;
   private int size;
   private double centerX;
   private double centerZ;
   private double targetSize;
   private double diameter;
   private long timeUntilTarget;
   private int warningTime;
   private int warningDistance;

   public SPacketWorldBorder() {
   }

   public SPacketWorldBorder(WorldBorder var1, SPacketWorldBorder.Action var2) {
      this.action = var2;
      this.centerX = var1.getCenterX();
      this.centerZ = var1.getCenterZ();
      this.diameter = var1.getDiameter();
      this.targetSize = var1.getTargetSize();
      this.timeUntilTarget = var1.getTimeUntilTarget();
      this.size = var1.getSize();
      this.warningDistance = var1.getWarningDistance();
      this.warningTime = var1.getWarningTime();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.action = (SPacketWorldBorder.Action)var1.readEnumValue(SPacketWorldBorder.Action.class);
      switch(this.action) {
      case SET_SIZE:
         this.targetSize = var1.readDouble();
         break;
      case LERP_SIZE:
         this.diameter = var1.readDouble();
         this.targetSize = var1.readDouble();
         this.timeUntilTarget = var1.readVarLong();
         break;
      case SET_CENTER:
         this.centerX = var1.readDouble();
         this.centerZ = var1.readDouble();
         break;
      case SET_WARNING_BLOCKS:
         this.warningDistance = var1.readVarInt();
         break;
      case SET_WARNING_TIME:
         this.warningTime = var1.readVarInt();
         break;
      case INITIALIZE:
         this.centerX = var1.readDouble();
         this.centerZ = var1.readDouble();
         this.diameter = var1.readDouble();
         this.targetSize = var1.readDouble();
         this.timeUntilTarget = var1.readVarLong();
         this.size = var1.readVarInt();
         this.warningDistance = var1.readVarInt();
         this.warningTime = var1.readVarInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeEnumValue(this.action);
      switch(this.action) {
      case SET_SIZE:
         var1.writeDouble(this.targetSize);
         break;
      case LERP_SIZE:
         var1.writeDouble(this.diameter);
         var1.writeDouble(this.targetSize);
         var1.writeVarLong(this.timeUntilTarget);
         break;
      case SET_CENTER:
         var1.writeDouble(this.centerX);
         var1.writeDouble(this.centerZ);
         break;
      case SET_WARNING_BLOCKS:
         var1.writeVarInt(this.warningDistance);
         break;
      case SET_WARNING_TIME:
         var1.writeVarInt(this.warningTime);
         break;
      case INITIALIZE:
         var1.writeDouble(this.centerX);
         var1.writeDouble(this.centerZ);
         var1.writeDouble(this.diameter);
         var1.writeDouble(this.targetSize);
         var1.writeVarLong(this.timeUntilTarget);
         var1.writeVarInt(this.size);
         var1.writeVarInt(this.warningDistance);
         var1.writeVarInt(this.warningTime);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleWorldBorder(this);
   }

   @SideOnly(Side.CLIENT)
   public void apply(WorldBorder var1) {
      switch(this.action) {
      case SET_SIZE:
         var1.setTransition(this.targetSize);
         break;
      case LERP_SIZE:
         var1.setTransition(this.diameter, this.targetSize, this.timeUntilTarget);
         break;
      case SET_CENTER:
         var1.setCenter(this.centerX, this.centerZ);
         break;
      case SET_WARNING_BLOCKS:
         var1.setWarningDistance(this.warningDistance);
         break;
      case SET_WARNING_TIME:
         var1.setWarningTime(this.warningTime);
         break;
      case INITIALIZE:
         var1.setCenter(this.centerX, this.centerZ);
         if (this.timeUntilTarget > 0L) {
            var1.setTransition(this.diameter, this.targetSize, this.timeUntilTarget);
         } else {
            var1.setTransition(this.targetSize);
         }

         var1.setSize(this.size);
         var1.setWarningDistance(this.warningDistance);
         var1.setWarningTime(this.warningTime);
      }

   }

   public static enum Action {
      SET_SIZE,
      LERP_SIZE,
      SET_CENTER,
      INITIALIZE,
      SET_WARNING_TIME,
      SET_WARNING_BLOCKS;
   }
}
