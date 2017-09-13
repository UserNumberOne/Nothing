package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

public class SPacketSoundEffect implements Packet {
   private SoundEvent sound;
   private SoundCategory category;
   private int posX;
   private int posY;
   private int posZ;
   private float soundVolume;
   private float soundPitch;

   public SPacketSoundEffect() {
   }

   public SPacketSoundEffect(SoundEvent var1, SoundCategory var2, double var3, double var5, double var7, float var9, float var10) {
      Validate.notNull(var1, "sound", new Object[0]);
      this.sound = var1;
      this.category = var2;
      this.posX = (int)(var3 * 8.0D);
      this.posY = (int)(var5 * 8.0D);
      this.posZ = (int)(var7 * 8.0D);
      this.soundVolume = var9;
      this.soundPitch = var10;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.sound = (SoundEvent)SoundEvent.REGISTRY.getObjectById(var1.readVarInt());
      this.category = (SoundCategory)var1.readEnumValue(SoundCategory.class);
      this.posX = var1.readInt();
      this.posY = var1.readInt();
      this.posZ = var1.readInt();
      this.soundVolume = var1.readFloat();
      this.soundPitch = var1.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(SoundEvent.REGISTRY.getIDForObject(this.sound));
      var1.writeEnumValue(this.category);
      var1.writeInt(this.posX);
      var1.writeInt(this.posY);
      var1.writeInt(this.posZ);
      var1.writeFloat(this.soundVolume);
      var1.writeFloat(this.soundPitch);
   }

   @SideOnly(Side.CLIENT)
   public SoundEvent getSound() {
      return this.sound;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSoundEffect(this);
   }

   @SideOnly(Side.CLIENT)
   public SoundCategory getCategory() {
      return this.category;
   }

   @SideOnly(Side.CLIENT)
   public double getX() {
      return (double)((float)this.posX / 8.0F);
   }

   @SideOnly(Side.CLIENT)
   public double getY() {
      return (double)((float)this.posY / 8.0F);
   }

   @SideOnly(Side.CLIENT)
   public double getZ() {
      return (double)((float)this.posZ / 8.0F);
   }

   @SideOnly(Side.CLIENT)
   public float getVolume() {
      return this.soundVolume;
   }

   @SideOnly(Side.CLIENT)
   public float getPitch() {
      return this.soundPitch;
   }
}
