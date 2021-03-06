package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class SPacketEntityEffect implements Packet {
   private int entityId;
   private byte effectId;
   private byte amplifier;
   private int duration;
   private byte flags;

   public SPacketEntityEffect() {
   }

   public SPacketEntityEffect(int var1, PotionEffect var2) {
      this.entityId = var1;
      this.effectId = (byte)(Potion.getIdFromPotion(var2.getPotion()) & 255);
      this.amplifier = (byte)(var2.getAmplifier() & 255);
      if (var2.getDuration() > 32767) {
         this.duration = 32767;
      } else {
         this.duration = var2.getDuration();
      }

      this.flags = 0;
      if (var2.getIsAmbient()) {
         this.flags = (byte)(this.flags | 1);
      }

      if (var2.doesShowParticles()) {
         this.flags = (byte)(this.flags | 2);
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      this.effectId = var1.readByte();
      this.amplifier = var1.readByte();
      this.duration = var1.readVarInt();
      this.flags = var1.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeByte(this.effectId);
      var1.writeByte(this.amplifier);
      var1.writeVarInt(this.duration);
      var1.writeByte(this.flags);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityEffect(this);
   }
}
