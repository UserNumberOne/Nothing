package net.minecraft.network.login.client;

import java.io.IOException;
import java.security.PrivateKey;
import javax.crypto.SecretKey;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginServer;
import net.minecraft.util.CryptManager;

public class CPacketEncryptionResponse implements Packet {
   private byte[] secretKeyEncrypted = new byte[0];
   private byte[] verifyTokenEncrypted = new byte[0];

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.secretKeyEncrypted = var1.readByteArray();
      this.verifyTokenEncrypted = var1.readByteArray();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByteArray(this.secretKeyEncrypted);
      var1.writeByteArray(this.verifyTokenEncrypted);
   }

   public void processPacket(INetHandlerLoginServer var1) {
      var1.processEncryptionResponse(this);
   }

   public SecretKey getSecretKey(PrivateKey var1) {
      return CryptManager.decryptSharedKey(var1, this.secretKeyEncrypted);
   }

   public byte[] getVerifyToken(PrivateKey var1) {
      return var1 == null ? this.verifyTokenEncrypted : CryptManager.decryptData(var1, this.verifyTokenEncrypted);
   }
}
