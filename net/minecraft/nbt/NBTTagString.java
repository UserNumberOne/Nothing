package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

public class NBTTagString extends NBTBase {
   private String data;

   public NBTTagString() {
      this.data = "";
   }

   public NBTTagString(String var1) {
      this.data = data;
      if (data == null) {
         throw new IllegalArgumentException("Empty string not allowed");
      }
   }

   void write(DataOutput var1) throws IOException {
      output.writeUTF(this.data);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(288L);
      this.data = input.readUTF();
      NBTSizeTracker.readUTF(sizeTracker, this.data);
   }

   public byte getId() {
      return 8;
   }

   public String toString() {
      return "\"" + StringUtils.replaceEach(this.data, new String[]{"\\", "\""}, new String[]{"\\\\", "\\\""}) + "\"";
   }

   public NBTTagString copy() {
      return new NBTTagString(this.data);
   }

   public boolean hasNoTags() {
      return this.data.isEmpty();
   }

   public boolean equals(Object var1) {
      if (!super.equals(p_equals_1_)) {
         return false;
      } else {
         NBTTagString nbttagstring = (NBTTagString)p_equals_1_;
         return this.data == null && nbttagstring.data == null || this.data != null && this.data.equals(nbttagstring.data);
      }
   }

   public int hashCode() {
      return super.hashCode() ^ this.data.hashCode();
   }

   public String getString() {
      return this.data;
   }
}
