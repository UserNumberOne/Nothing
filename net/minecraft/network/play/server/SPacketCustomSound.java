package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

public class SPacketCustomSound implements Packet {
   private String soundName;
   private SoundCategory category;
   private int x;
   private int y = Integer.MAX_VALUE;
   private int z;
   private float volume;
   private float pitch;

   public SPacketCustomSound() {
   }

   public SPacketCustomSound(String var1, SoundCategory var2, double var3, double var5, double var7, float var9, float var10) {
      Validate.notNull(soundNameIn, "name", new Object[0]);
      this.soundName = soundNameIn;
      this.category = categoryIn;
      this.x = (int)(xIn * 8.0D);
      this.y = (int)(yIn * 8.0D);
      this.z = (int)(zIn * 8.0D);
      this.volume = volumeIn;
      this.pitch = pitchIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.soundName = buf.readString(256);
      this.category = (SoundCategory)buf.readEnumValue(SoundCategory.class);
      this.x = buf.readInt();
      this.y = buf.readInt();
      this.z = buf.readInt();
      this.volume = buf.readFloat();
      this.pitch = buf.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(this.soundName);
      buf.writeEnumValue(this.category);
      buf.writeInt(this.x);
      buf.writeInt(this.y);
      buf.writeInt(this.z);
      buf.writeFloat(this.volume);
      buf.writeFloat(this.pitch);
   }

   @SideOnly(Side.CLIENT)
   public String getSoundName() {
      return this.soundName;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleCustomSound(this);
   }

   @SideOnly(Side.CLIENT)
   public SoundCategory getCategory() {
      return this.category;
   }

   @SideOnly(Side.CLIENT)
   public double getX() {
      return (double)((float)this.x / 8.0F);
   }

   @SideOnly(Side.CLIENT)
   public double getY() {
      return (double)((float)this.y / 8.0F);
   }

   @SideOnly(Side.CLIENT)
   public double getZ() {
      return (double)((float)this.z / 8.0F);
   }

   @SideOnly(Side.CLIENT)
   public float getVolume() {
      return this.volume;
   }

   @SideOnly(Side.CLIENT)
   public float getPitch() {
      return this.pitch;
   }
}
