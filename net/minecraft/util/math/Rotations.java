package net.minecraft.util.math;

import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

public class Rotations {
   protected final float x;
   protected final float y;
   protected final float z;

   public Rotations(float var1, float var2, float var3) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Rotations(NBTTagList var1) {
      this.x = nbt.getFloatAt(0);
      this.y = nbt.getFloatAt(1);
      this.z = nbt.getFloatAt(2);
   }

   public NBTTagList writeToNBT() {
      NBTTagList nbttaglist = new NBTTagList();
      nbttaglist.appendTag(new NBTTagFloat(this.x));
      nbttaglist.appendTag(new NBTTagFloat(this.y));
      nbttaglist.appendTag(new NBTTagFloat(this.z));
      return nbttaglist;
   }

   public boolean equals(Object var1) {
      if (!(p_equals_1_ instanceof Rotations)) {
         return false;
      } else {
         Rotations rotations = (Rotations)p_equals_1_;
         return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
      }
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getZ() {
      return this.z;
   }
}
