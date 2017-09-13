package net.minecraft.util;

public enum Mirror {
   NONE("no_mirror"),
   LEFT_RIGHT("mirror_left_right"),
   FRONT_BACK("mirror_front_back");

   private final String name;
   private static final String[] mirrorNames = new String[values().length];

   private Mirror(String var3) {
      this.name = var3;
   }

   public int mirrorRotation(int var1, int var2) {
      int var3 = var2 / 2;
      int var4 = var1 > var3 ? var1 - var2 : var1;
      switch(this) {
      case FRONT_BACK:
         return (var2 - var4) % var2;
      case LEFT_RIGHT:
         return (var3 - var4 + var2) % var2;
      default:
         return var1;
      }
   }

   public Rotation toRotation(EnumFacing var1) {
      EnumFacing.Axis var2 = var1.getAxis();
      return this == LEFT_RIGHT && var2 == EnumFacing.Axis.Z || this == FRONT_BACK && var2 == EnumFacing.Axis.X ? Rotation.CLOCKWISE_180 : Rotation.NONE;
   }

   public EnumFacing mirror(EnumFacing var1) {
      switch(this) {
      case FRONT_BACK:
         if (var1 == EnumFacing.WEST) {
            return EnumFacing.EAST;
         } else {
            if (var1 == EnumFacing.EAST) {
               return EnumFacing.WEST;
            }

            return var1;
         }
      case LEFT_RIGHT:
         if (var1 == EnumFacing.NORTH) {
            return EnumFacing.SOUTH;
         } else {
            if (var1 == EnumFacing.SOUTH) {
               return EnumFacing.NORTH;
            }

            return var1;
         }
      default:
         return var1;
      }
   }

   static {
      int var0 = 0;

      for(Mirror var4 : values()) {
         mirrorNames[var0++] = var4.name;
      }

   }
}
