package net.minecraft.pathfinding;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PathPoint {
   public final int xCoord;
   public final int yCoord;
   public final int zCoord;
   private final int hash;
   public int index = -1;
   public float totalPathDistance;
   public float distanceToNext;
   public float distanceToTarget;
   public PathPoint previous;
   public boolean visited;
   public float distanceFromOrigin;
   public float cost;
   public float costMalus;
   public PathNodeType nodeType = PathNodeType.BLOCKED;

   public PathPoint(int var1, int var2, int var3) {
      this.xCoord = var1;
      this.yCoord = var2;
      this.zCoord = var3;
      this.hash = makeHash(var1, var2, var3);
   }

   public PathPoint cloneMove(int var1, int var2, int var3) {
      PathPoint var4 = new PathPoint(var1, var2, var3);
      var4.index = this.index;
      var4.totalPathDistance = this.totalPathDistance;
      var4.distanceToNext = this.distanceToNext;
      var4.distanceToTarget = this.distanceToTarget;
      var4.previous = this.previous;
      var4.visited = this.visited;
      var4.distanceFromOrigin = this.distanceFromOrigin;
      var4.cost = this.cost;
      var4.costMalus = this.costMalus;
      var4.nodeType = this.nodeType;
      return var4;
   }

   public static int makeHash(int var0, int var1, int var2) {
      return var1 & 255 | (var0 & 32767) << 8 | (var2 & 32767) << 24 | (var0 < 0 ? Integer.MIN_VALUE : 0) | (var2 < 0 ? '耀' : 0);
   }

   public float distanceTo(PathPoint var1) {
      float var2 = (float)(var1.xCoord - this.xCoord);
      float var3 = (float)(var1.yCoord - this.yCoord);
      float var4 = (float)(var1.zCoord - this.zCoord);
      return MathHelper.sqrt(var2 * var2 + var3 * var3 + var4 * var4);
   }

   public float distanceToSquared(PathPoint var1) {
      float var2 = (float)(var1.xCoord - this.xCoord);
      float var3 = (float)(var1.yCoord - this.yCoord);
      float var4 = (float)(var1.zCoord - this.zCoord);
      return var2 * var2 + var3 * var3 + var4 * var4;
   }

   public float distanceManhattan(PathPoint var1) {
      float var2 = (float)Math.abs(var1.xCoord - this.xCoord);
      float var3 = (float)Math.abs(var1.yCoord - this.yCoord);
      float var4 = (float)Math.abs(var1.zCoord - this.zCoord);
      return var2 + var3 + var4;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof PathPoint)) {
         return false;
      } else {
         PathPoint var2 = (PathPoint)var1;
         return this.hash == var2.hash && this.xCoord == var2.xCoord && this.yCoord == var2.yCoord && this.zCoord == var2.zCoord;
      }
   }

   public int hashCode() {
      return this.hash;
   }

   public boolean isAssigned() {
      return this.index >= 0;
   }

   public String toString() {
      return this.xCoord + ", " + this.yCoord + ", " + this.zCoord;
   }

   @SideOnly(Side.CLIENT)
   public static PathPoint createFromBuffer(PacketBuffer var0) {
      PathPoint var1 = new PathPoint(var0.readInt(), var0.readInt(), var0.readInt());
      var1.distanceFromOrigin = var0.readFloat();
      var1.cost = var0.readFloat();
      var1.costMalus = var0.readFloat();
      var1.visited = var0.readBoolean();
      var1.nodeType = PathNodeType.values()[var0.readInt()];
      var1.distanceToTarget = var0.readFloat();
      return var1;
   }
}
