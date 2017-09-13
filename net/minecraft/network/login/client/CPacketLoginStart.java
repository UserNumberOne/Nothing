package net.minecraft.network.login.client;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginServer;

public class CPacketLoginStart implements Packet {
   private GameProfile profile;

   public CPacketLoginStart() {
   }

   public CPacketLoginStart(GameProfile var1) {
      this.profile = profileIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.profile = new GameProfile((UUID)null, buf.readString(16));
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(this.profile.getName());
   }

   public void processPacket(INetHandlerLoginServer var1) {
      handler.processLoginStart(this);
   }

   public GameProfile getProfile() {
      return this.profile;
   }
}
