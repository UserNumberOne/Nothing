package net.minecraft.network.rcon;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RConOutputStream {
   private final ByteArrayOutputStream byteArrayOutput;
   private final DataOutputStream output;

   public RConOutputStream(int var1) {
      this.byteArrayOutput = new ByteArrayOutputStream(var1);
      this.output = new DataOutputStream(this.byteArrayOutput);
   }

   public void writeByteArray(byte[] var1) throws IOException {
      this.output.write(var1, 0, var1.length);
   }

   public void writeString(String var1) throws IOException {
      this.output.writeBytes(var1);
      this.output.write(0);
   }

   public void writeInt(int var1) throws IOException {
      this.output.write(var1);
   }

   public void writeShort(short var1) throws IOException {
      this.output.writeShort(Short.reverseBytes(var1));
   }

   public byte[] toByteArray() {
      return this.byteArrayOutput.toByteArray();
   }

   public void reset() {
      this.byteArrayOutput.reset();
   }
}
