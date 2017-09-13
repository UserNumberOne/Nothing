package net.minecraft.world.gen.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public abstract class MapGenStructure extends MapGenBase {
   private MapGenStructureData structureData;
   protected Long2ObjectMap structureMap = new Long2ObjectOpenHashMap(1024);

   public abstract String getStructureName();

   protected final synchronized void recursiveGenerate(World var1, final int var2, final int var3, int var4, int var5, ChunkPrimer var6) {
      this.initializeStructureData(var1);
      if (!this.structureMap.containsKey(ChunkPos.asLong(var2, var3))) {
         this.rand.nextInt();

         try {
            if (this.canSpawnStructureAtCoords(var2, var3)) {
               StructureStart var7 = this.getStructureStart(var2, var3);
               this.structureMap.put(ChunkPos.asLong(var2, var3), var7);
               if (var7.isSizeableStructure()) {
                  this.setStructureStart(var2, var3, var7);
               }
            }
         } catch (Throwable var10) {
            CrashReport var8 = CrashReport.makeCrashReport(var10, "Exception preparing structure feature");
            CrashReportCategory var9 = var8.makeCategory("Feature being prepared");
            var9.setDetail("Is feature chunk", new ICrashReportDetail() {
               public String call() throws Exception {
                  return MapGenStructure.this.canSpawnStructureAtCoords(var2, var3) ? "True" : "False";
               }
            });
            var9.addCrashSection("Chunk location", String.format("%d,%d", var2, var3));
            var9.setDetail("Chunk pos hash", new ICrashReportDetail() {
               public String call() throws Exception {
                  return String.valueOf(ChunkPos.asLong(var2, var3));
               }
            });
            var9.setDetail("Structure type", new ICrashReportDetail() {
               public String call() throws Exception {
                  return MapGenStructure.this.getClass().getCanonicalName();
               }
            });
            throw new ReportedException(var8);
         }
      }

   }

   public synchronized boolean generateStructure(World var1, Random var2, ChunkPos var3) {
      this.initializeStructureData(var1);
      int var4 = (var3.chunkXPos << 4) + 8;
      int var5 = (var3.chunkZPos << 4) + 8;
      boolean var6 = false;
      ObjectIterator var7 = this.structureMap.values().iterator();

      while(var7.hasNext()) {
         StructureStart var8 = (StructureStart)var7.next();
         if (var8.isSizeableStructure() && var8.isValidForPostProcess(var3) && var8.getBoundingBox().intersectsWith(var4, var5, var4 + 15, var5 + 15)) {
            var8.generateStructure(var1, var2, new StructureBoundingBox(var4, var5, var4 + 15, var5 + 15));
            var8.notifyPostProcessAt(var3);
            var6 = true;
            this.setStructureStart(var8.getChunkPosX(), var8.getChunkPosZ(), var8);
         }
      }

      return var6;
   }

   public boolean isInsideStructure(BlockPos var1) {
      this.initializeStructureData(this.world);
      return this.getStructureAt(var1) != null;
   }

   protected StructureStart getStructureAt(BlockPos var1) {
      ObjectIterator var2 = this.structureMap.values().iterator();

      label33:
      while(var2.hasNext()) {
         StructureStart var3 = (StructureStart)var2.next();
         if (var3.isSizeableStructure() && var3.getBoundingBox().isVecInside(var1)) {
            Iterator var4 = var3.getComponents().iterator();

            while(true) {
               if (!var4.hasNext()) {
                  continue label33;
               }

               StructureComponent var5 = (StructureComponent)var4.next();
               if (var5.getBoundingBox().isVecInside(var1)) {
                  break;
               }
            }

            return var3;
         }
      }

      return null;
   }

   public boolean isPositionInStructure(World var1, BlockPos var2) {
      this.initializeStructureData(var1);
      ObjectIterator var3 = this.structureMap.values().iterator();

      while(var3.hasNext()) {
         StructureStart var4 = (StructureStart)var3.next();
         if (var4.isSizeableStructure() && var4.getBoundingBox().isVecInside(var2)) {
            return true;
         }
      }

      return false;
   }

   public BlockPos getClosestStrongholdPos(World var1, BlockPos var2) {
      this.world = var1;
      this.initializeStructureData(var1);
      this.rand.setSeed(var1.getSeed());
      long var3 = this.rand.nextLong();
      long var5 = this.rand.nextLong();
      long var7 = (long)(var2.getX() >> 4) * var3;
      long var9 = (long)(var2.getZ() >> 4) * var5;
      this.rand.setSeed(var7 ^ var9 ^ var1.getSeed());
      this.recursiveGenerate(var1, var2.getX() >> 4, var2.getZ() >> 4, 0, 0, (ChunkPrimer)null);
      double var11 = Double.MAX_VALUE;
      BlockPos var13 = null;
      ObjectIterator var14 = this.structureMap.values().iterator();

      while(var14.hasNext()) {
         StructureStart var15 = (StructureStart)var14.next();
         if (var15.isSizeableStructure()) {
            StructureComponent var16 = (StructureComponent)var15.getComponents().get(0);
            BlockPos var17 = var16.getBoundingBoxCenter();
            double var18 = var17.distanceSq(var2);
            if (var18 < var11) {
               var11 = var18;
               var13 = var17;
            }
         }
      }

      if (var13 != null) {
         return var13;
      } else {
         List var20 = this.getCoordList();
         if (var20 != null) {
            BlockPos var21 = null;

            for(BlockPos var23 : var20) {
               double var24 = var23.distanceSq(var2);
               if (var24 < var11) {
                  var11 = var24;
                  var21 = var23;
               }
            }

            return var21;
         } else {
            return null;
         }
      }
   }

   protected List getCoordList() {
      return null;
   }

   protected void initializeStructureData(World var1) {
      if (this.structureData == null) {
         this.structureData = (MapGenStructureData)var1.getPerWorldStorage().getOrLoadData(MapGenStructureData.class, this.getStructureName());
         if (this.structureData == null) {
            this.structureData = new MapGenStructureData(this.getStructureName());
            var1.getPerWorldStorage().setData(this.getStructureName(), this.structureData);
         } else {
            NBTTagCompound var2 = this.structureData.getTagCompound();

            for(String var4 : var2.getKeySet()) {
               NBTBase var5 = var2.getTag(var4);
               if (var5.getId() == 10) {
                  NBTTagCompound var6 = (NBTTagCompound)var5;
                  if (var6.hasKey("ChunkX") && var6.hasKey("ChunkZ")) {
                     int var7 = var6.getInteger("ChunkX");
                     int var8 = var6.getInteger("ChunkZ");
                     StructureStart var9 = MapGenStructureIO.getStructureStart(var6, var1);
                     if (var9 != null) {
                        this.structureMap.put(ChunkPos.asLong(var7, var8), var9);
                     }
                  }
               }
            }
         }
      }

   }

   private void setStructureStart(int var1, int var2, StructureStart var3) {
      this.structureData.writeInstance(var3.writeStructureComponentsToNBT(var1, var2), var1, var2);
      this.structureData.markDirty();
   }

   protected abstract boolean canSpawnStructureAtCoords(int var1, int var2);

   protected abstract StructureStart getStructureStart(int var1, int var2);
}
