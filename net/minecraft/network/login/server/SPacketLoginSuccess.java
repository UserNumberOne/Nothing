package net.minecraft.network.login.server;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketLoginSuccess implements Packet {
   private GameProfile profile;

   public SPacketLoginSuccess() {
   }

   public SPacketLoginSuccess(GameProfile var1) {
      this.profile = profileIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      String s = buf.readString(36);
      String s1 = buf.readString(16);
      UUID uuid = UUID.fromString(s);
      this.profile = new GameProfile(uuid, s1);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      UUID uuid = this.profile.getId();
      buf.writeString(uuid == null ? "" : uuid.toString());
      buf.writeString(this.profile.getName());
   }

   public void processPacket(INetHandlerLoginClient var1) {
      handler.handleLoginSuccess(this);
   }

   @SideOnly(Side.CLIENT)
   public GameProfile getProfile() {
      return this.profile;
   }
}
