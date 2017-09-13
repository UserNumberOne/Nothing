package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public enum EnumFacing implements IStringSerializable {
   DOWN(0, 1, -1, "down", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Y, new Vec3i(0, -1, 0)),
   UP(1, 0, -1, "up", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Y, new Vec3i(0, 1, 0)),
   NORTH(2, 3, 2, "north", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, -1)),
   SOUTH(3, 2, 0, "south", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, 1)),
   WEST(4, 5, 1, "west", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X, new Vec3i(-1, 0, 0)),
   EAST(5, 4, 3, "east", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X, new Vec3i(1, 0, 0));

   private final int index;
   private final int opposite;
   private final int horizontalIndex;
   private final String name;
   private final EnumFacing.Axis axis;
   private final EnumFacing.AxisDirection axisDirection;
   private final Vec3i directionVec;
   public static final EnumFacing[] VALUES = new EnumFacing[6];
   public static final EnumFacing[] HORIZONTALS = new EnumFacing[4];
   private static final Map NAME_LOOKUP = Maps.newHashMap();

   private EnumFacing(int var3, int var4, int var5, String var6, EnumFacing.AxisDirection var7, EnumFacing.Axis var8, Vec3i var9) {
      this.index = var3;
      this.horizontalIndex = var5;
      this.opposite = var4;
      this.name = var6;
      this.axis = var8;
      this.axisDirection = var7;
      this.directionVec = var9;
   }

   public int getIndex() {
      return this.index;
   }

   public int getHorizontalIndex() {
      return this.horizontalIndex;
   }

   public EnumFacing.AxisDirection getAxisDirection() {
      return this.axisDirection;
   }

   public EnumFacing getOpposite() {
      return getFront(this.opposite);
   }

   public EnumFacing rotateAround(EnumFacing.Axis var1) {
      switch(var1) {
      case X:
         if (this != WEST && this != EAST) {
            return this.rotateX();
         }

         return this;
      case Y:
         if (this != UP && this != DOWN) {
            return this.rotateY();
         }

         return this;
      case Z:
         if (this != NORTH && this != SOUTH) {
            return this.rotateZ();
         }

         return this;
      default:
         throw new IllegalStateException("Unable to get CW facing for axis " + var1);
      }
   }

   public EnumFacing rotateY() {
      switch(this) {
      case NORTH:
         return EAST;
      case EAST:
         return SOUTH;
      case SOUTH:
         return WEST;
      case WEST:
         return NORTH;
      default:
         throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
      }
   }

   private EnumFacing rotateX() {
      switch(this) {
      case NORTH:
         return DOWN;
      case EAST:
      case WEST:
      default:
         throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      case SOUTH:
         return UP;
      case UP:
         return NORTH;
      case DOWN:
         return SOUTH;
      }
   }

   private EnumFacing rotateZ() {
      switch(this) {
      case EAST:
         return DOWN;
      case SOUTH:
      default:
         throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
      case WEST:
         return UP;
      case UP:
         return EAST;
      case DOWN:
         return WEST;
      }
   }

   public EnumFacing rotateYCCW() {
      switch(this) {
      case NORTH:
         return WEST;
      case EAST:
         return NORTH;
      case SOUTH:
         return EAST;
      case WEST:
         return SOUTH;
      default:
         throw new IllegalStateException("Unable to get CCW facing of " + this);
      }
   }

   public int getFrontOffsetX() {
      return this.axis == EnumFacing.Axis.X ? this.axisDirection.getOffset() : 0;
   }

   public int getFrontOffsetY() {
      return this.axis == EnumFacing.Axis.Y ? this.axisDirection.getOffset() : 0;
   }

   public int getFrontOffsetZ() {
      return this.axis == EnumFacing.Axis.Z ? this.axisDirection.getOffset() : 0;
   }

   public String getName2() {
      return this.name;
   }

   public EnumFacing.Axis getAxis() {
      return this.axis;
   }

   @Nullable
   public static EnumFacing byName(String var0) {
      return var0 == null ? null : (EnumFacing)NAME_LOOKUP.get(var0.toLowerCase());
   }

   public static EnumFacing getFront(int var0) {
      return VALUES[MathHelper.abs(var0 % VALUES.length)];
   }

   public static EnumFacing getHorizontal(int var0) {
      return HORIZONTALS[MathHelper.abs(var0 % HORIZONTALS.length)];
   }

   public static EnumFacing fromAngle(double var0) {
      return getHorizontal(MathHelper.floor(var0 / 90.0D + 0.5D) & 3);
   }

   public float getHorizontalAngle() {
      return (float)((this.horizontalIndex & 3) * 90);
   }

   public static EnumFacing random(Random var0) {
      return values()[var0.nextInt(values().length)];
   }

   public static EnumFacing getFacingFromVector(float var0, float var1, float var2) {
      EnumFacing var3 = NORTH;
      float var4 = Float.MIN_VALUE;

      for(EnumFacing var8 : values()) {
         float var9 = var0 * (float)var8.directionVec.getX() + var1 * (float)var8.directionVec.getY() + var2 * (float)var8.directionVec.getZ();
         if (var9 > var4) {
            var4 = var9;
            var3 = var8;
         }
      }

      return var3;
   }

   public String toString() {
      return this.name;
   }

   public String getName() {
      return this.name;
   }

   public static EnumFacing getFacingFromAxis(EnumFacing.AxisDirection var0, EnumFacing.Axis var1) {
      for(EnumFacing var5 : values()) {
         if (var5.getAxisDirection() == var0 && var5.getAxis() == var1) {
            return var5;
         }
      }

      throw new IllegalArgumentException("No such direction: " + var0 + " " + var1);
   }

   public Vec3i getDirectionVec() {
      return this.directionVec;
   }

   static {
      for(EnumFacing var3 : values()) {
         VALUES[var3.index] = var3;
         if (var3.getAxis().isHorizontal()) {
            HORIZONTALS[var3.horizontalIndex] = var3;
         }

         NAME_LOOKUP.put(var3.getName2().toLowerCase(), var3);
      }

   }

   public static enum Axis implements Predicate, IStringSerializable {
      X("x", EnumFacing.Plane.HORIZONTAL),
      Y("y", EnumFacing.Plane.VERTICAL),
      Z("z", EnumFacing.Plane.HORIZONTAL);

      private static final Map NAME_LOOKUP = Maps.newHashMap();
      private final String name;
      private final EnumFacing.Plane plane;

      private Axis(String var3, EnumFacing.Plane var4) {
         this.name = var3;
         this.plane = var4;
      }

      @Nullable
      public static EnumFacing.Axis byName(String var0) {
         return var0 == null ? null : (EnumFacing.Axis)NAME_LOOKUP.get(var0.toLowerCase());
      }

      public String getName2() {
         return this.name;
      }

      public boolean isVertical() {
         return this.plane == EnumFacing.Plane.VERTICAL;
      }

      public boolean isHorizontal() {
         return this.plane == EnumFacing.Plane.HORIZONTAL;
      }

      public String toString() {
         return this.name;
      }

      public boolean apply(@Nullable EnumFacing var1) {
         return var1 != null && var1.getAxis() == this;
      }

      public EnumFacing.Plane getPlane() {
         return this.plane;
      }

      public String getName() {
         return this.name;
      }

      static {
         for(EnumFacing.Axis var3 : values()) {
            NAME_LOOKUP.put(var3.getName2().toLowerCase(), var3);
         }

      }
   }

   public static enum AxisDirection {
      POSITIVE(1, "Towards positive"),
      NEGATIVE(-1, "Towards negative");

      private final int offset;
      private final String description;

      private AxisDirection(int var3, String var4) {
         this.offset = var3;
         this.description = var4;
      }

      public int getOffset() {
         return this.offset;
      }

      public String toString() {
         return this.description;
      }
   }

   public static enum Plane implements Predicate, Iterable {
      HORIZONTAL,
      VERTICAL;

      public EnumFacing[] facings() {
         switch(this) {
         case HORIZONTAL:
            return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
         case VERTICAL:
            return new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
         default:
            throw new Error("Someone's been tampering with the universe!");
         }
      }

      public EnumFacing random(Random var1) {
         EnumFacing[] var2 = this.facings();
         return var2[var1.nextInt(var2.length)];
      }

      public boolean apply(@Nullable EnumFacing var1) {
         return var1 != null && var1.getAxis().getPlane() == this;
      }

      public Iterator iterator() {
         return Iterators.forArray(this.facings());
      }
   }
}
