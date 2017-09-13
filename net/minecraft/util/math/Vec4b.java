package net.minecraft.util.math;

public class Vec4b {
   private byte type;
   private byte x;
   private byte y;
   private byte rotation;

   public Vec4b(byte var1, byte var2, byte var3, byte var4) {
      this.type = var1;
      this.x = var2;
      this.y = var3;
      this.rotation = var4;
   }

   public Vec4b(Vec4b var1) {
      this.type = var1.type;
      this.x = var1.x;
      this.y = var1.y;
      this.rotation = var1.rotation;
   }

   public byte getType() {
      return this.type;
   }

   public byte getX() {
      return this.x;
   }

   public byte getY() {
      return this.y;
   }

   public byte getRotation() {
      return this.rotation;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Vec4b)) {
         return false;
      } else {
         Vec4b var2 = (Vec4b)var1;
         if (this.type != var2.type) {
            return false;
         } else if (this.rotation != var2.rotation) {
            return false;
         } else if (this.x != var2.x) {
            return false;
         } else {
            return this.y == var2.y;
         }
      }
   }

   public int hashCode() {
      int var1 = this.type;
      var1 = 31 * var1 + this.x;
      var1 = 31 * var1 + this.y;
      var1 = 31 * var1 + this.rotation;
      return var1;
   }
}
