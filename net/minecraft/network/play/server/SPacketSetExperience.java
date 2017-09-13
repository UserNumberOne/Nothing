package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketSetExperience implements Packet {
   private float experienceBar;
   private int totalExperience;
   private int level;

   public SPacketSetExperience() {
   }

   public SPacketSetExperience(float var1, int var2, int var3) {
      this.experienceBar = var1;
      this.totalExperience = var2;
      this.level = var3;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.experienceBar = var1.readFloat();
      this.level = var1.readVarInt();
      this.totalExperience = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeFloat(this.experienceBar);
      var1.writeVarInt(this.level);
      var1.writeVarInt(this.totalExperience);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSetExperience(this);
   }
}
