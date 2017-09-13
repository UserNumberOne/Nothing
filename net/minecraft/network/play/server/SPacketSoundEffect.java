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
      Validate.notNull(soundIn, "sound", new Object[0]);
      this.sound = soundIn;
      this.category = categoryIn;
      this.posX = (int)(xIn * 8.0D);
      this.posY = (int)(yIn * 8.0D);
      this.posZ = (int)(zIn * 8.0D);
      this.soundVolume = volumeIn;
      this.soundPitch = pitchIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.sound = (SoundEvent)SoundEvent.REGISTRY.getObjectById(buf.readVarInt());
      this.category = (SoundCategory)buf.readEnumValue(SoundCategory.class);
      this.posX = buf.readInt();
      this.posY = buf.readInt();
      this.posZ = buf.readInt();
      this.soundVolume = buf.readFloat();
      this.soundPitch = buf.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(SoundEvent.REGISTRY.getIDForObject(this.sound));
      buf.writeEnumValue(this.category);
      buf.writeInt(this.posX);
      buf.writeInt(this.posY);
      buf.writeInt(this.posZ);
      buf.writeFloat(this.soundVolume);
      buf.writeFloat(this.soundPitch);
   }

   @SideOnly(Side.CLIENT)
   public SoundEvent getSound() {
      return this.sound;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleSoundEffect(this);
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
