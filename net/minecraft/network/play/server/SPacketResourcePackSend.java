package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketResourcePackSend implements Packet {
   private String url;
   private String hash;

   public SPacketResourcePackSend() {
   }

   public SPacketResourcePackSend(String var1, String var2) {
      this.url = urlIn;
      this.hash = hashIn;
      if (hashIn.length() > 40) {
         throw new IllegalArgumentException("Hash is too long (max 40, was " + hashIn.length() + ")");
      }
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.url = buf.readString(32767);
      this.hash = buf.readString(40);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(this.url);
      buf.writeString(this.hash);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleResourcePack(this);
   }

   @SideOnly(Side.CLIENT)
   public String getURL() {
      return this.url;
   }

   @SideOnly(Side.CLIENT)
   public String getHash() {
      return this.hash;
   }
}
