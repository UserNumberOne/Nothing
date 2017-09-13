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
      for(String s : this.tagMap.keySet()) {
         NBTBase nbtbase = (NBTBase)this.tagMap.get(s);
         writeEntry(s, nbtbase, output);
      }

      output.writeByte(0);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(384L);
      if (depth > 512) {
         throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
      } else {
         this.tagMap.clear();

         byte b0;
         while((b0 = readType(input, sizeTracker)) != 0) {
            String s = readKey(input, sizeTracker);
            sizeTracker.read((long)(224 + 16 * s.length()));
            NBTBase nbtbase = readNBT(b0, s, input, depth + 1, sizeTracker);
            if (this.tagMap.put(s, nbtbase) != null) {
               sizeTracker.read(288L);
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
      this.tagMap.put(key, value);
   }

   public void setByte(String var1, byte var2) {
      this.tagMap.put(key, new NBTTagByte(value));
   }

   public void setShort(String var1, short var2) {
      this.tagMap.put(key, new NBTTagShort(value));
   }

   public void setInteger(String var1, int var2) {
      this.tagMap.put(key, new NBTTagInt(value));
   }

   public void setLong(String var1, long var2) {
      this.tagMap.put(key, new NBTTagLong(value));
   }

   public void setUniqueId(String var1, UUID var2) {
      this.setLong(key + "Most", value.getMostSignificantBits());
      this.setLong(key + "Least", value.getLeastSignificantBits());
   }

   @Nullable
   public UUID getUniqueId(String var1) {
      return new UUID(this.getLong(key + "Most"), this.getLong(key + "Least"));
   }

   public boolean hasUniqueId(String var1) {
      return this.hasKey(key + "Most", 99) && this.hasKey(key + "Least", 99);
   }

   public void setFloat(String var1, float var2) {
      this.tagMap.put(key, new NBTTagFloat(value));
   }

   public void setDouble(String var1, double var2) {
      this.tagMap.put(key, new NBTTagDouble(value));
   }

   public void setString(String var1, String var2) {
      this.tagMap.put(key, new NBTTagString(value));
   }

   public void setByteArray(String var1, byte[] var2) {
      this.tagMap.put(key, new NBTTagByteArray(value));
   }

   public void setIntArray(String var1, int[] var2) {
      this.tagMap.put(key, new NBTTagIntArray(value));
   }

   public void setBoolean(String var1, boolean var2) {
      this.setByte(key, (byte)(value ? 1 : 0));
   }

   public NBTBase getTag(String var1) {
      return (NBTBase)this.tagMap.get(key);
   }

   public byte getTagId(String var1) {
      NBTBase nbtbase = (NBTBase)this.tagMap.get(key);
      return nbtbase == null ? 0 : nbtbase.getId();
   }

   public boolean hasKey(String var1) {
      return this.tagMap.containsKey(key);
   }

   public boolean hasKey(String var1, int var2) {
      int i = this.getTagId(key);
      return i == type ? true : (type != 99 ? false : i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6);
   }

   public byte getByte(String var1) {
      try {
         if (this.hasKey(key, 99)) {
            return ((NBTPrimitive)this.tagMap.get(key)).getByte();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0;
   }

   public short getShort(String var1) {
      try {
         if (this.hasKey(key, 99)) {
            return ((NBTPrimitive)this.tagMap.get(key)).getShort();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0;
   }

   public int getInteger(String var1) {
      try {
         if (this.hasKey(key, 99)) {
            return ((NBTPrimitive)this.tagMap.get(key)).getInt();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0;
   }

   public long getLong(String var1) {
      try {
         if (this.hasKey(key, 99)) {
            return ((NBTPrimitive)this.tagMap.get(key)).getLong();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0L;
   }

   public float getFloat(String var1) {
      try {
         if (this.hasKey(key, 99)) {
            return ((NBTPrimitive)this.tagMap.get(key)).getFloat();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0.0F;
   }

   public double getDouble(String var1) {
      try {
         if (this.hasKey(key, 99)) {
            return ((NBTPrimitive)this.tagMap.get(key)).getDouble();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return 0.0D;
   }

   public String getString(String var1) {
      try {
         if (this.hasKey(key, 8)) {
            return ((NBTBase)this.tagMap.get(key)).getString();
         }
      } catch (ClassCastException var3) {
         ;
      }

      return "";
   }

   public byte[] getByteArray(String var1) {
      try {
         if (this.hasKey(key, 7)) {
            return ((NBTTagByteArray)this.tagMap.get(key)).getByteArray();
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createCrashReport(key, 7, var3));
      }

      return new byte[0];
   }

   public int[] getIntArray(String var1) {
      try {
         if (this.hasKey(key, 11)) {
            return ((NBTTagIntArray)this.tagMap.get(key)).getIntArray();
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createCrashReport(key, 11, var3));
      }

      return new int[0];
   }

   public NBTTagCompound getCompoundTag(String var1) {
      try {
         if (this.hasKey(key, 10)) {
            return (NBTTagCompound)this.tagMap.get(key);
         }
      } catch (ClassCastException var3) {
         throw new ReportedException(this.createCrashReport(key, 10, var3));
      }

      return new NBTTagCompound();
   }

   public NBTTagList getTagList(String var1, int var2) {
      try {
         if (this.getTagId(key) == 9) {
            NBTTagList nbttaglist = (NBTTagList)this.tagMap.get(key);
            if (!nbttaglist.hasNoTags() && nbttaglist.getTagType() != type) {
               return new NBTTagList();
            }

            return nbttaglist;
         }
      } catch (ClassCastException var4) {
         throw new ReportedException(this.createCrashReport(key, 9, var4));
      }

      return new NBTTagList();
   }

   public boolean getBoolean(String var1) {
      return this.getByte(key) != 0;
   }

   public void removeTag(String var1) {
      this.tagMap.remove(key);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder("{");

      for(Entry entry : this.tagMap.entrySet()) {
         if (stringbuilder.length() != 1) {
            stringbuilder.append(',');
         }

         stringbuilder.append((String)entry.getKey()).append(':').append(entry.getValue());
      }

      return stringbuilder.append('}').toString();
   }

   public boolean hasNoTags() {
      return this.tagMap.isEmpty();
   }

   private CrashReport createCrashReport(final String var1, final int var2, ClassCastException var3) {
      CrashReport crashreport = CrashReport.makeCrashReport(ex, "Reading NBT data");
      CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
      crashreportcategory.setDetail("Tag type found", new ICrashReportDetail() {
         public String call() throws Exception {
            return NBTBase.NBT_TYPES[((NBTBase)NBTTagCompound.this.tagMap.get(key)).getId()];
         }
      });
      crashreportcategory.setDetail("Tag type expected", new ICrashReportDetail() {
         public String call() throws Exception {
            return NBTBase.NBT_TYPES[expectedType];
         }
      });
      crashreportcategory.addCrashSection("Tag name", key);
      return crashreport;
   }

   public NBTTagCompound copy() {
      NBTTagCompound nbttagcompound = new NBTTagCompound();

      for(String s : this.tagMap.keySet()) {
         nbttagcompound.setTag(s, ((NBTBase)this.tagMap.get(s)).copy());
      }

      return nbttagcompound;
   }

   public boolean equals(Object var1) {
      if (super.equals(p_equals_1_)) {
         NBTTagCompound nbttagcompound = (NBTTagCompound)p_equals_1_;
         return this.tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return super.hashCode() ^ this.tagMap.hashCode();
   }

   private static void writeEntry(String var0, NBTBase var1, DataOutput var2) throws IOException {
      output.writeByte(data.getId());
      if (data.getId() != 0) {
         output.writeUTF(name);
         data.write(output);
      }

   }

   private static byte readType(DataInput var0, NBTSizeTracker var1) throws IOException {
      sizeTracker.read(8L);
      return input.readByte();
   }

   private static String readKey(DataInput var0, NBTSizeTracker var1) throws IOException {
      return input.readUTF();
   }

   static NBTBase readNBT(byte var0, String var1, DataInput var2, int var3, NBTSizeTracker var4) throws IOException {
      sizeTracker.read(32L);
      NBTBase nbtbase = NBTBase.createNewByType(id);

      try {
         nbtbase.read(input, depth, sizeTracker);
         return nbtbase;
      } catch (IOException var9) {
         CrashReport crashreport = CrashReport.makeCrashReport(var9, "Loading NBT data");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
         crashreportcategory.addCrashSection("Tag name", key);
         crashreportcategory.addCrashSection("Tag type", Byte.valueOf(id));
         throw new ReportedException(crashreport);
      }
   }

   public void merge(NBTTagCompound var1) {
      for(String s : other.tagMap.keySet()) {
         NBTBase nbtbase = (NBTBase)other.tagMap.get(s);
         if (nbtbase.getId() == 10) {
            if (this.hasKey(s, 10)) {
               NBTTagCompound nbttagcompound = this.getCompoundTag(s);
               nbttagcompound.merge((NBTTagCompound)nbtbase);
            } else {
               this.setTag(s, nbtbase.copy());
            }
         } else {
            this.setTag(s, nbtbase.copy());
         }
      }

   }
}
