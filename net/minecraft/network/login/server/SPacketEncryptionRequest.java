package net.minecraft.network.login.server;

import java.io.IOException;
import java.security.PublicKey;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.util.CryptManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEncryptionRequest implements Packet {
   private String hashedServerId;
   private PublicKey publicKey;
   private byte[] verifyToken;

   public SPacketEncryptionRequest() {
   }

   public SPacketEncryptionRequest(String var1, PublicKey var2, byte[] var3) {
      this.hashedServerId = serverIdIn;
      this.publicKey = publicKeyIn;
      this.verifyToken = verifyTokenIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.hashedServerId = buf.readString(20);
      this.publicKey = CryptManager.decodePublicKey(buf.readByteArray());
      this.verifyToken = buf.readByteArray();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(this.hashedServerId);
      buf.writeByteArray(this.publicKey.getEncoded());
      buf.writeByteArray(this.verifyToken);
   }

   public void processPacket(INetHandlerLoginClient var1) {
      handler.handleEncryptionRequest(this);
   }

   @SideOnly(Side.CLIENT)
   public String getServerId() {
      return this.hashedServerId;
   }

   @SideOnly(Side.CLIENT)
   public PublicKey getPublicKey() {
      return this.publicKey;
   }

   @SideOnly(Side.CLIENT)
   public byte[] getVerifyToken() {
      return this.verifyToken;
   }
}
