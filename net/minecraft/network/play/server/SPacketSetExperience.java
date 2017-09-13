package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSetExperience implements Packet {
   private float experienceBar;
   private int totalExperience;
   private int level;

   public SPacketSetExperience() {
   }

   public SPacketSetExperience(float var1, int var2, int var3) {
      this.experienceBar = experienceBarIn;
      this.totalExperience = totalExperienceIn;
      this.level = levelIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.experienceBar = buf.readFloat();
      this.level = buf.readVarInt();
      this.totalExperience = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeFloat(this.experienceBar);
      buf.writeVarInt(this.level);
      buf.writeVarInt(this.totalExperience);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleSetExperience(this);
   }

   @SideOnly(Side.CLIENT)
   public float getExperienceBar() {
      return this.experienceBar;
   }

   @SideOnly(Side.CLIENT)
   public int getTotalExperience() {
      return this.totalExperience;
   }

   @SideOnly(Side.CLIENT)
   public int getLevel() {
      return this.level;
   }
}
