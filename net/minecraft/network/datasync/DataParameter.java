package net.minecraft.network.datasync;

public class DataParameter {
   private final int id;
   private final DataSerializer serializer;

   public DataParameter(int var1, DataSerializer var2) {
      this.id = var1;
      this.serializer = var2;
   }

   public int getId() {
      return this.id;
   }

   public DataSerializer getSerializer() {
      return this.serializer;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         DataParameter var2 = (DataParameter)var1;
         return this.id == var2.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id;
   }
}
