package net.minecraft.util;

public enum Rotation {
   NONE("rotate_0"),
   CLOCKWISE_90("rotate_90"),
   CLOCKWISE_180("rotate_180"),
   COUNTERCLOCKWISE_90("rotate_270");

   private final String name;
   private static final String[] rotationNames = new String[values().length];

   private Rotation(String var3) {
      this.name = var3;
   }

   public Rotation add(Rotation var1) {
      switch(var1) {
      case CLOCKWISE_180:
         switch(this) {
         case NONE:
            return CLOCKWISE_180;
         case CLOCKWISE_90:
            return COUNTERCLOCKWISE_90;
         case CLOCKWISE_180:
            return NONE;
         case COUNTERCLOCKWISE_90:
            return CLOCKWISE_90;
         }
      case COUNTERCLOCKWISE_90:
         switch(this) {
         case NONE:
            return COUNTERCLOCKWISE_90;
         case CLOCKWISE_90:
            return NONE;
         case CLOCKWISE_180:
            return CLOCKWISE_90;
         case COUNTERCLOCKWISE_90:
            return CLOCKWISE_180;
         }
      case CLOCKWISE_90:
         switch(this) {
         case NONE:
            return CLOCKWISE_90;
         case CLOCKWISE_90:
            return CLOCKWISE_180;
         case CLOCKWISE_180:
            return COUNTERCLOCKWISE_90;
         case COUNTERCLOCKWISE_90:
            return NONE;
         }
      default:
         return this;
      }
   }

   public EnumFacing rotate(EnumFacing var1) {
      if (var1.getAxis() == EnumFacing.Axis.Y) {
         return var1;
      } else {
         switch(this) {
         case CLOCKWISE_90:
            return var1.rotateY();
         case CLOCKWISE_180:
            return var1.getOpposite();
         case COUNTERCLOCKWISE_90:
            return var1.rotateYCCW();
         default:
            return var1;
         }
      }
   }

   public int rotate(int var1, int var2) {
      switch(this) {
      case CLOCKWISE_90:
         return (var1 + var2 / 4) % var2;
      case CLOCKWISE_180:
         return (var1 + var2 / 2) % var2;
      case COUNTERCLOCKWISE_90:
         return (var1 + var2 * 3 / 4) % var2;
      default:
         return var1;
      }
   }

   static {
      int var0 = 0;

      for(Rotation var4 : values()) {
         rotationNames[var0++] = var4.name;
      }

   }
}
