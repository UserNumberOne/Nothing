package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketUpdateHealth implements Packet {
   private float health;
   private int foodLevel;
   private float saturationLevel;

   public SPacketUpdateHealth() {
   }

   public SPacketUpdateHealth(float var1, int var2, float var3) {
      this.health = var1;
      this.foodLevel = var2;
      this.saturationLevel = var3;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.health = var1.readFloat();
      this.foodLevel = var1.readVarInt();
      this.saturationLevel = var1.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeFloat(this.health);
      var1.writeVarInt(this.foodLevel);
      var1.writeFloat(this.saturationLevel);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleUpdateHealth(this);
   }

   @SideOnly(Side.CLIENT)
   public float getHealth() {
      return this.health;
   }

   @SideOnly(Side.CLIENT)
   public int getFoodLevel() {
      return this.foodLevel;
   }

   @SideOnly(Side.CLIENT)
   public float getSaturationLevel() {
      return this.saturationLevel;
   }
}
