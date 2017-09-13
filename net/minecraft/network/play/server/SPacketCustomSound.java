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
      Validate.notNull(var1, "name", new Object[0]);
      this.soundName = var1;
      this.category = var2;
      this.x = (int)(var3 * 8.0D);
      this.y = (int)(var5 * 8.0D);
      this.z = (int)(var7 * 8.0D);
      this.volume = var9;
      this.pitch = var10;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.soundName = var1.readString(256);
      this.category = (SoundCategory)var1.readEnumValue(SoundCategory.class);
      this.x = var1.readInt();
      this.y = var1.readInt();
      this.z = var1.readInt();
      this.volume = var1.readFloat();
      this.pitch = var1.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeString(this.soundName);
      var1.writeEnumValue(this.category);
      var1.writeInt(this.x);
      var1.writeInt(this.y);
      var1.writeInt(this.z);
      var1.writeFloat(this.volume);
      var1.writeFloat(this.pitch);
   }

   @SideOnly(Side.CLIENT)
   public String getSoundName() {
      return this.soundName;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleCustomSound(this);
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
