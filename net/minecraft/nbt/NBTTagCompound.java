package net.minecraft.nbt;

import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.ReportedException;

public class NBTTagCompound extends NBTBase {
   private final Map tagMap = Maps.newHashMap();

   void write(DataOutput var1) throws IOException {
      for(String var3 : this.tagMap.keySet()) {
         NBTBase var4 = (NBTBase)this.tagMap.get(var3);
         writeEntry(var3, var4, var1);
      }

      var1.writeByte(0);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      var3.read(384L);
      if (var2 > 512) {
         throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
      } else {
         this.tagMap.clear();

         byte var4;
         while((var4 = readType(var1, var3)) != 0) {
            String var5 = readKey(var1, var3);
            var3.read((long)(224 + 16 * var5.length()));
            NBTBase var6 = readNBT(var4, var5, var1, var2 + 1, var3);
            if (this.tagMap.put(var5, var6) != null) {
               var3.read(288L);
            }
         }

      }
   }

   public Set getKeySet() {
      return this.tagMap.keySet();
   }

   public byte getId() {
      return 10;
   }

   public int getSize() {
      return this.tagMap.size();
   }

   public void setTag(String var1, NBTBase var2) {
      this.tagMap.put(var1, var2);
   }

   public void setByte(String var1, byte var2) {
      this.tagMap.put(var1, new NBTTagByte(var2));
   }

   public void setShort(String var1, short var2) {
      this.tagMap.put(var1, new NBTTagShort(var2));
   }

   public void setInteger(String var1, int var2) {
      this.tagMap.put(var1, new NBTTagInt(var2));
   }

   public void setLong(String var1, long var2) {
      this.tagMap.put(var1, new NBTTagLong(var2));
   }

   public void setUniqueId(String var1, UUID var2) {
      this.setLong(var1 + "Most", var2.getMostSignificantBits());
      this.setLong(var1 + "Least", var2.getLeastSignificantBits());
   }

   @Nullable
   public UUID getUniqueId(String var1) {
      return new UUID(this.getLong(var1 + "Most"), this.getLong(var1 + "Least"));
   }

   public boolean hasUniqueId(String var1) {
      return this.hasKey(var1 + "Most", 99) && this.hasKey(var1 + "Least", 99);
   }

   public void setFloat(String var1, float var2) {
      this.tagMap.put(var1, new NBTTagFloat(var2));
   }

   public void setDouble(String var1, double var2) {
      this.tagMap.put(var1, new NBTTagDouble(var2));
   }

   public void setString(String var1, String var2) {
      this.tagMap.put(var1, new NBTTagString(var2));
   }

   public void setByteArray(String var1, byte[] var2) {
      this.tagMap.put(var1, new NBTTagByteArray(var2));
   }

   public void setIntArray(String var1, int[] var2) {
      this.tagMap.put(var1, new NBTTagIntArray(var2));
   }

   public void setBoolean(String var1, boolean var2) {
      this.setByte(var1, (byte)(var2 ? 1 : 0));
   }

   public NBTBase getTag(String var1) {
      return (NBTBase)this.tagMap.get(var1);
   }

   public byte getTagId(String var1) {
      NBTBase var2 = (NBTBase)this.tagMap.get(var1);
      return var2 == null ? 0 : var2.getId();
   }

   public boolean hasKey(String var1) {
      return this.tagMap.containsKey(var1);
   }

   public boolean hasKey(String var1, int var2) {
      byte var3 = this.getTagId(var1);
      return var3 == var2 ? true : (var2 != 99 ? false : var3 == 1 || var3 == 2 || var3 == 3 || var3 == 4 || var3 == 5 || var3 == 6);
   }

   public byte getByte(String var1) {
      try {
         if (this.hasKey(var1, 99)) {
            return ((NBTPrimitive)this.tagMap.get(var1)).getByte();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0;
   }

   public short getShort(String var1) {
      try {
         if (this.hasKey(var1, 99)) {
            return ((NBTPrimitive)this.tagMap.get(var1)).getShort();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0;
   }

   public int getInteger(String var1) {
      try {
         if (this.hasKey(var1, 99)) {
            return ((NBTPrimitive)this.tagMap.get(var1)).getInt();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0;
   }

   public long getLong(String var1) {
      try {
         if (this.hasKey(var1, 99)) {
            return ((NBTPrimitive)this.tagMap.get(var1)).getLong();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0L;
   }

   public float getFloat(String var1) {
      try {
         if (this.hasKey(var1, 99)) {
            return ((NBTPrimitive)this.tagMap.get(var1)).getFloat();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0.0F;
   }

   public double getDouble(String var1) {
      try {
         if (this.hasKey(var1, 99)) {
            return ((NBTPrimitive)this.tagMap.get(var1)).getDouble();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0.0D;
   }

   public String getString(String var1) {
      try {
         if (this.hasKey(var1, 8)) {
            return ((NBTBase)this.tagMap.get(var1)).getString();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return "";
   }

   public byte[] getByteArray(String var1) {
      try {
         if (this.hasKey(var1, 7)) {
            return ((NBTTagByteArray)this.tagMap.get(var1)).getByteArray();
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createCrashReport(var1, 7, var3));
      }

      return new byte[0];
   }

   public int[] getIntArray(String var1) {
      try {
         if (this.hasKey(var1, 11)) {
            return ((NBTTagIntArray)this.tagMap.get(var1)).getIntArray();
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createCrashReport(var1, 11, var3));
      }

      return new int[0];
   }

   public NBTTagCompound getCompoundTag(String var1) {
      try {
         if (this.hasKey(var1, 10)) {
            return (NBTTagCompound)this.tagMap.get(var1);
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createCrashReport(var1, 10, var3));
      }

      return new NBTTagCompound();
   }

   public NBTTagList getTagList(String var1, int var2) {
      try {
         if (this.getTagId(var1) == 9) {
            NBTTagList var3 = (NBTTagList)this.tagMap.get(var1);
            if (!var3.hasNoTags() && var3.getTagType() != var2) {
               return new NBTTagList();
            }

            return var3;
         }
      } catch (ClassCastException var4) {
         throw new ReportedException(this.createCrashReport(var1, 9, var4));
      }

      return new NBTTagList();
   }

   public boolean getBoolean(String var1) {
      return this.getByte(var1) != 0;
   }

   public void removeTag(String var1) {
      this.tagMap.remove(var1);
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder("{");

      for(Entry var3 : this.tagMap.entrySet()) {
         if (var1.length() != 1) {
            var1.append(',');
         }

         var1.append((String)var3.getKey()).append(':').append(var3.getValue());
      }

      return var1.append('}').toString();
   }

   public boolean hasNoTags() {
      return this.tagMap.isEmpty();
   }

   private CrashReport createCrashReport(final String var1, final int var2, ClassCastException var3) {
      CrashReport var4 = CrashReport.makeCrashReport(var3, "Reading NBT data");
      CrashReportCategory var5 = var4.makeCategoryDepth("Corrupt NBT tag", 1);
      var5.setDetail("Tag type found", new ICrashReportDetail() {
         public String call() throws Exception {
            return NBTBase.NBT_TYPES[((NBTBase)NBTTagCompound.this.tagMap.get(var1)).getId()];
         }
      });
      var5.setDetail("Tag type expected", new ICrashReportDetail() {
         public String call() throws Exception {
            return NBTBase.NBT_TYPES[var2];
         }
      });
      var5.addCrashSection("Tag name", var1);
      return var4;
   }

   public NBTTagCompound copy() {
      NBTTagCompound var1 = new NBTTagCompound();

      for(String var3 : this.tagMap.keySet()) {
         var1.setTag(var3, ((NBTBase)this.tagMap.get(var3)).copy());
      }

      return var1;
   }

   public boolean equals(Object var1) {
      if (super.equals(var1)) {
         NBTTagCompound var2 = (NBTTagCompound)var1;
         return this.tagMap.entrySet().equals(var2.tagMap.entrySet());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return super.hashCode() ^ this.tagMap.hashCode();
   }

   private static void writeEntry(String var0, NBTBase var1, DataOutput var2) throws IOException {
      var2.writeByte(var1.getId());
      if (var1.getId() != 0) {
         var2.writeUTF(var0);
         var1.write(var2);
      }

   }

   private static byte readType(DataInput var0, NBTSizeTracker var1) throws IOException {
      var1.read(8L);
      return var0.readByte();
   }

   private static String readKey(DataInput var0, NBTSizeTracker var1) throws IOException {
      return var0.readUTF();
   }

   static NBTBase readNBT(byte var0, String var1, DataInput var2, int var3, NBTSizeTracker var4) throws IOException {
      var4.read(32L);
      NBTBase var5 = NBTBase.createNewByType(var0);

      try {
         var5.read(var2, var3, var4);
         return var5;
      } catch (IOException var9) {
         CrashReport var7 = CrashReport.makeCrashReport(var9, "Loading NBT data");
         CrashReportCategory var8 = var7.makeCategory("NBT Tag");
         var8.addCrashSection("Tag name", var1);
         var8.addCrashSection("Tag type", Byte.valueOf(var0));
         throw new ReportedException(var7);
      }
   }

   public void merge(NBTTagCompound var1) {
      for(String var3 : var1.tagMap.keySet()) {
         NBTBase var4 = (NBTBase)var1.tagMap.get(var3);
         if (var4.getId() == 10) {
            if (this.hasKey(var3, 10)) {
               NBTTagCompound var5 = this.getCompoundTag(var3);
               var5.merge((NBTTagCompound)var4);
            } else {
               this.setTag(var3, var4.copy());
            }
         } else {
            this.setTag(var3, var4.copy());
         }
      }

   }
}
