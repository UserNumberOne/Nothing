package net.minecraft.nbt;

import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NBTTagList extends NBTBase {
   private static final Logger LOGGER = LogManager.getLogger();
   private List tagList = Lists.newArrayList();
   private byte tagType = 0;

   void write(DataOutput var1) throws IOException {
      if (this.tagList.isEmpty()) {
         this.tagType = 0;
      } else {
         this.tagType = ((NBTBase)this.tagList.get(0)).getId();
      }

      output.writeByte(this.tagType);
      output.writeInt(this.tagList.size());

      for(int i = 0; i < this.tagList.size(); ++i) {
         ((NBTBase)this.tagList.get(i)).write(output);
      }

   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(296L);
      if (depth > 512) {
         throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
      } else {
         this.tagType = input.readByte();
         int i = input.readInt();
         if (this.tagType == 0 && i > 0) {
            throw new RuntimeException("Missing type on ListTag");
         } else {
            sizeTracker.read(32L * (long)i);
            this.tagList = Lists.newArrayListWithCapacity(i);

            for(int j = 0; j < i; ++j) {
               NBTBase nbtbase = NBTBase.createNewByType(this.tagType);
               nbtbase.read(input, depth + 1, sizeTracker);
               this.tagList.add(nbtbase);
            }

         }
      }
   }

   public byte getId() {
      return 9;
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder("[");

      for(int i = 0; i < this.tagList.size(); ++i) {
         if (i != 0) {
            stringbuilder.append(',');
         }

         stringbuilder.append(i).append(':').append(this.tagList.get(i));
      }

      return stringbuilder.append(']').toString();
   }

   public void appendTag(NBTBase var1) {
      if (nbt.getId() == 0) {
         LOGGER.warn("Invalid TagEnd added to ListTag");
      } else {
         if (this.tagType == 0) {
            this.tagType = nbt.getId();
         } else if (this.tagType != nbt.getId()) {
            LOGGER.warn("Adding mismatching tag types to tag list");
            return;
         }

         this.tagList.add(nbt);
      }

   }

   public void set(int var1, NBTBase var2) {
      if (nbt.getId() == 0) {
         LOGGER.warn("Invalid TagEnd added to ListTag");
      } else if (idx >= 0 && idx < this.tagList.size()) {
         if (this.tagType == 0) {
            this.tagType = nbt.getId();
         } else if (this.tagType != nbt.getId()) {
            LOGGER.warn("Adding mismatching tag types to tag list");
            return;
         }

         this.tagList.set(idx, nbt);
      } else {
         LOGGER.warn("index out of bounds to set tag in tag list");
      }

   }

   public NBTBase removeTag(int var1) {
      return (NBTBase)this.tagList.remove(i);
   }

   public boolean hasNoTags() {
      return this.tagList.isEmpty();
   }

   public NBTTagCompound getCompoundTagAt(int var1) {
      if (i >= 0 && i < this.tagList.size()) {
         NBTBase nbtbase = (NBTBase)this.tagList.get(i);
         if (nbtbase.getId() == 10) {
            return (NBTTagCompound)nbtbase;
         }
      }

      return new NBTTagCompound();
   }

   public int getIntAt(int var1) {
      if (p_186858_1_ >= 0 && p_186858_1_ < this.tagList.size()) {
         NBTBase nbtbase = (NBTBase)this.tagList.get(p_186858_1_);
         if (nbtbase.getId() == 3) {
            return ((NBTTagInt)nbtbase).getInt();
         }
      }

      return 0;
   }

   public int[] getIntArrayAt(int var1) {
      if (i >= 0 && i < this.tagList.size()) {
         NBTBase nbtbase = (NBTBase)this.tagList.get(i);
         if (nbtbase.getId() == 11) {
            return ((NBTTagIntArray)nbtbase).getIntArray();
         }
      }

      return new int[0];
   }

   public double getDoubleAt(int var1) {
      if (i >= 0 && i < this.tagList.size()) {
         NBTBase nbtbase = (NBTBase)this.tagList.get(i);
         if (nbtbase.getId() == 6) {
            return ((NBTTagDouble)nbtbase).getDouble();
         }
      }

      return 0.0D;
   }

   public float getFloatAt(int var1) {
      if (i >= 0 && i < this.tagList.size()) {
         NBTBase nbtbase = (NBTBase)this.tagList.get(i);
         if (nbtbase.getId() == 5) {
            return ((NBTTagFloat)nbtbase).getFloat();
         }
      }

      return 0.0F;
   }

   public String getStringTagAt(int var1) {
      if (i >= 0 && i < this.tagList.size()) {
         NBTBase nbtbase = (NBTBase)this.tagList.get(i);
         return nbtbase.getId() == 8 ? nbtbase.getString() : nbtbase.toString();
      } else {
         return "";
      }
   }

   public NBTBase get(int var1) {
      return (NBTBase)(idx >= 0 && idx < this.tagList.size() ? (NBTBase)this.tagList.get(idx) : new NBTTagEnd());
   }

   public int tagCount() {
      return this.tagList.size();
   }

   public NBTTagList copy() {
      NBTTagList nbttaglist = new NBTTagList();
      nbttaglist.tagType = this.tagType;

      for(NBTBase nbtbase : this.tagList) {
         NBTBase nbtbase1 = nbtbase.copy();
         nbttaglist.tagList.add(nbtbase1);
      }

      return nbttaglist;
   }

   public boolean equals(Object var1) {
      if (super.equals(p_equals_1_)) {
         NBTTagList nbttaglist = (NBTTagList)p_equals_1_;
         if (this.tagType == nbttaglist.tagType) {
            return this.tagList.equals(nbttaglist.tagList);
         }
      }

      return false;
   }

   public int hashCode() {
      return super.hashCode() ^ this.tagList.hashCode();
   }

   public int getTagType() {
      return this.tagType;
   }
}
