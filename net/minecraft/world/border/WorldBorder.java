package net.minecraft.world.border;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import org.bukkit.craftbukkit.v1_10_R1.util.LongHash;

public class WorldBorder {
   private final List listeners = Lists.newArrayList();
   private double centerX;
   private double centerZ;
   private double startDiameter = 6.0E7D;
   private double endDiameter;
   private long endTime;
   private long startTime;
   private int worldSize;
   private double damageAmount;
   private double damageBuffer;
   private int warningTime;
   private int warningDistance;
   public WorldServer world;

   public WorldBorder() {
      this.endDiameter = this.startDiameter;
      this.worldSize = 29999984;
      this.damageAmount = 0.2D;
      this.damageBuffer = 5.0D;
      this.warningTime = 15;
      this.warningDistance = 5;
   }

   public boolean contains(BlockPos blockposition) {
      return (double)(blockposition.getX() + 1) > this.minX() && (double)blockposition.getX() < this.maxX() && (double)(blockposition.getZ() + 1) > this.minZ() && (double)blockposition.getZ() < this.maxZ();
   }

   public boolean contains(ChunkPos chunkcoordintpair) {
      return this.isInBounds(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
   }

   public boolean isInBounds(long chunkcoords) {
      return this.isInBounds(LongHash.msw(chunkcoords), LongHash.lsw(chunkcoords));
   }

   public boolean isInBounds(int x, int z) {
      return (double)((x << 4) + 15) > this.minX() && (double)(x << 4) < this.maxX() && (double)((z << 4) + 15) > this.minZ() && (double)(x << 4) < this.maxZ();
   }

   public boolean contains(AxisAlignedBB axisalignedbb) {
      return axisalignedbb.maxX > this.minX() && axisalignedbb.minX < this.maxX() && axisalignedbb.maxZ > this.minZ() && axisalignedbb.minZ < this.maxZ();
   }

   public double getClosestDistance(Entity entity) {
      return this.getClosestDistance(entity.posX, entity.posZ);
   }

   public double getClosestDistance(double d0, double d1) {
      double d2 = d1 - this.minZ();
      double d3 = this.maxZ() - d1;
      double d4 = d0 - this.minX();
      double d5 = this.maxX() - d0;
      double d6 = Math.min(d4, d5);
      d6 = Math.min(d6, d2);
      return Math.min(d6, d3);
   }

   public EnumBorderStatus getStatus() {
      return this.endDiameter < this.startDiameter ? EnumBorderStatus.SHRINKING : (this.endDiameter > this.startDiameter ? EnumBorderStatus.GROWING : EnumBorderStatus.STATIONARY);
   }

   public double minX() {
      double d0 = this.getCenterX() - this.getDiameter() / 2.0D;
      if (d0 < (double)(-this.worldSize)) {
         d0 = (double)(-this.worldSize);
      }

      return d0;
   }

   public double minZ() {
      double d0 = this.getCenterZ() - this.getDiameter() / 2.0D;
      if (d0 < (double)(-this.worldSize)) {
         d0 = (double)(-this.worldSize);
      }

      return d0;
   }

   public double maxX() {
      double d0 = this.getCenterX() + this.getDiameter() / 2.0D;
      if (d0 > (double)this.worldSize) {
         d0 = (double)this.worldSize;
      }

      return d0;
   }

   public double maxZ() {
      double d0 = this.getCenterZ() + this.getDiameter() / 2.0D;
      if (d0 > (double)this.worldSize) {
         d0 = (double)this.worldSize;
      }

      return d0;
   }

   public double getCenterX() {
      return this.centerX;
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   public void setCenter(double d0, double d1) {
      this.centerX = d0;
      this.centerZ = d1;

      for(IBorderListener iworldborderlistener : this.getListeners()) {
         iworldborderlistener.onCenterChanged(this, d0, d1);
      }

   }

   public double getDiameter() {
      if (this.getStatus() != EnumBorderStatus.STATIONARY) {
         double d0 = (double)((float)(System.currentTimeMillis() - this.startTime) / (float)(this.endTime - this.startTime));
         if (d0 < 1.0D) {
            return this.startDiameter + (this.endDiameter - this.startDiameter) * d0;
         }

         this.setTransition(this.endDiameter);
      }

      return this.startDiameter;
   }

   public long getTimeUntilTarget() {
      return this.getStatus() == EnumBorderStatus.STATIONARY ? 0L : this.endTime - System.currentTimeMillis();
   }

   public double getTargetSize() {
      return this.endDiameter;
   }

   public void setTransition(double d0) {
      this.startDiameter = d0;
      this.endDiameter = d0;
      this.endTime = System.currentTimeMillis();
      this.startTime = this.endTime;

      for(IBorderListener iworldborderlistener : this.getListeners()) {
         iworldborderlistener.onSizeChanged(this, d0);
      }

   }

   public void setTransition(double d0, double d1, long i) {
      this.startDiameter = d0;
      this.endDiameter = d1;
      this.startTime = System.currentTimeMillis();
      this.endTime = this.startTime + i;

      for(IBorderListener iworldborderlistener : this.getListeners()) {
         iworldborderlistener.onTransitionStarted(this, d0, d1, i);
      }

   }

   protected List getListeners() {
      return Lists.newArrayList(this.listeners);
   }

   public void addListener(IBorderListener iworldborderlistener) {
      if (!this.listeners.contains(iworldborderlistener)) {
         this.listeners.add(iworldborderlistener);
      }
   }

   public void setSize(int i) {
      this.worldSize = i;
   }

   public int getSize() {
      return this.worldSize;
   }

   public double getDamageBuffer() {
      return this.damageBuffer;
   }

   public void setDamageBuffer(double d0) {
      this.damageBuffer = d0;

      for(IBorderListener iworldborderlistener : this.getListeners()) {
         iworldborderlistener.onDamageBufferChanged(this, d0);
      }

   }

   public double getDamageAmount() {
      return this.damageAmount;
   }

   public void setDamageAmount(double d0) {
      this.damageAmount = d0;

      for(IBorderListener iworldborderlistener : this.getListeners()) {
         iworldborderlistener.onDamageAmountChanged(this, d0);
      }

   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public void setWarningTime(int i) {
      this.warningTime = i;

      for(IBorderListener iworldborderlistener : this.getListeners()) {
         iworldborderlistener.onWarningTimeChanged(this, i);
      }

   }

   public int getWarningDistance() {
      return this.warningDistance;
   }

   public void setWarningDistance(int i) {
      this.warningDistance = i;

      for(IBorderListener iworldborderlistener : this.getListeners()) {
         iworldborderlistener.onWarningDistanceChanged(this, i);
      }

   }
}
