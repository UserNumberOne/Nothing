package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public abstract class NodeProcessor {
   protected IBlockAccess blockaccess;
   protected EntityLiving entity;
   protected final IntHashMap pointMap = new IntHashMap();
   protected int entitySizeX;
   protected int entitySizeY;
   protected int entitySizeZ;
   protected boolean canEnterDoors;
   protected boolean canBreakDoors;
   protected boolean canSwim;

   public void initProcessor(IBlockAccess var1, EntityLiving var2) {
      this.blockaccess = var1;
      this.entity = var2;
      this.pointMap.clearMap();
      this.entitySizeX = MathHelper.floor(var2.width + 1.0F);
      this.entitySizeY = MathHelper.floor(var2.height + 1.0F);
      this.entitySizeZ = MathHelper.floor(var2.width + 1.0F);
   }

   public void postProcess() {
      this.blockaccess = null;
      this.entity = null;
   }

   protected PathPoint openPoint(int var1, int var2, int var3) {
      int var4 = PathPoint.makeHash(var1, var2, var3);
      PathPoint var5 = (PathPoint)this.pointMap.lookup(var4);
      if (var5 == null) {
         var5 = new PathPoint(var1, var2, var3);
         this.pointMap.addKey(var4, var5);
      }

      return var5;
   }

   public abstract PathPoint getStart();

   public abstract PathPoint getPathPointToCoords(double var1, double var3, double var5);

   public abstract int findPathOptions(PathPoint[] var1, PathPoint var2, PathPoint var3, float var4);

   public abstract PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4, EntityLiving var5, int var6, int var7, int var8, boolean var9, boolean var10);

   public abstract PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4);

   public void setCanEnterDoors(boolean var1) {
      this.canEnterDoors = var1;
   }

   public void setCanBreakDoors(boolean var1) {
      this.canBreakDoors = var1;
   }

   public void setCanSwim(boolean var1) {
      this.canSwim = var1;
   }

   public boolean getCanEnterDoors() {
      return this.canEnterDoors;
   }

   public boolean getCanBreakDoors() {
      return this.canBreakDoors;
   }

   public boolean getCanSwim() {
      return this.canSwim;
   }
}
