package net.minecraft.world.gen.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
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

   protected final synchronized void recursiveGenerate(World world, final int i, final int j, int k, int l, ChunkPrimer chunksnapshot) {
      this.initializeStructureData(world);
      if (!this.structureMap.containsKey(ChunkPos.asLong(i, j))) {
         this.rand.nextInt();

         try {
            if (this.canSpawnStructureAtCoords(i, j)) {
               StructureStart structurestart = this.getStructureStart(i, j);
               this.structureMap.put(ChunkPos.asLong(i, j), structurestart);
               if (structurestart.isSizeableStructure()) {
                  this.setStructureStart(i, j, structurestart);
               }
            }
         } catch (Throwable var10) {
            CrashReport crashreport = CrashReport.makeCrashReport(var10, "Exception preparing structure feature");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Feature being prepared");
            crashreportsystemdetails.setDetail("Is feature chunk", new ICrashReportDetail() {
               public String call() throws Exception {
                  return MapGenStructure.this.canSpawnStructureAtCoords(i, j) ? "True" : "False";
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            crashreportsystemdetails.addCrashSection("Chunk location", String.format("%d,%d", i, j));
            crashreportsystemdetails.setDetail("Chunk pos hash", new ICrashReportDetail() {
               public String call() throws Exception {
                  return String.valueOf(ChunkPos.asLong(i, j));
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            crashreportsystemdetails.setDetail("Structure type", new ICrashReportDetail() {
               public String call() throws Exception {
                  return MapGenStructure.this.getClass().getCanonicalName();
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(crashreport);
         }
      }

   }

   public synchronized boolean generateStructure(World world, Random random, ChunkPos chunkcoordintpair) {
      this.initializeStructureData(world);
      int i = (chunkcoordintpair.chunkXPos << 4) + 8;
      int j = (chunkcoordintpair.chunkZPos << 4) + 8;
      boolean flag = false;
      Iterator iterator = this.structureMap.values().iterator();

      while(iterator.hasNext()) {
         StructureStart structurestart = (StructureStart)iterator.next();
         if (structurestart.isSizeableStructure() && structurestart.isValidForPostProcess(chunkcoordintpair) && structurestart.getBoundingBox().intersectsWith(i, j, i + 15, j + 15)) {
            structurestart.generateStructure(world, random, new StructureBoundingBox(i, j, i + 15, j + 15));
            structurestart.notifyPostProcessAt(chunkcoordintpair);
            flag = true;
            this.setStructureStart(structurestart.getChunkPosX(), structurestart.getChunkPosZ(), structurestart);
         }
      }

      return flag;
   }

   public boolean isInsideStructure(BlockPos blockposition) {
      this.initializeStructureData(this.world);
      return this.getStructureAt(blockposition) != null;
   }

   protected synchronized StructureStart getStructureAt(BlockPos blockposition) {
      Iterator iterator = this.structureMap.values().iterator();

      label29:
      while(iterator.hasNext()) {
         StructureStart structurestart = (StructureStart)iterator.next();
         if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside(blockposition)) {
            Iterator iterator1 = structurestart.getComponents().iterator();

            while(true) {
               if (!iterator1.hasNext()) {
                  continue label29;
               }

               StructureComponent structurepiece = (StructureComponent)iterator1.next();
               if (structurepiece.getBoundingBox().isVecInside(blockposition)) {
                  break;
               }
            }

            return structurestart;
         }
      }

      return null;
   }

   public synchronized boolean isPositionInStructure(World world, BlockPos blockposition) {
      this.initializeStructureData(world);
      Iterator iterator = this.structureMap.values().iterator();

      while(iterator.hasNext()) {
         StructureStart structurestart = (StructureStart)iterator.next();
         if (structurestart.isSizeableStructure() && structurestart.getBoundingBox().isVecInside(blockposition)) {
            return true;
         }
      }

      return false;
   }

   public synchronized BlockPos getClosestStrongholdPos(World world, BlockPos blockposition) {
      this.world = world;
      this.initializeStructureData(world);
      this.rand.setSeed(world.getSeed());
      long i = this.rand.nextLong();
      long j = this.rand.nextLong();
      long k = (long)(blockposition.getX() >> 4) * i;
      long l = (long)(blockposition.getZ() >> 4) * j;
      this.rand.setSeed(k ^ l ^ world.getSeed());
      this.recursiveGenerate(world, blockposition.getX() >> 4, blockposition.getZ() >> 4, 0, 0, (ChunkPrimer)null);
      double d0 = Double.MAX_VALUE;
      BlockPos blockposition1 = null;
      Iterator iterator = this.structureMap.values().iterator();

      while(iterator.hasNext()) {
         StructureStart structurestart = (StructureStart)iterator.next();
         if (structurestart.isSizeableStructure()) {
            StructureComponent structurepiece = (StructureComponent)structurestart.getComponents().get(0);
            BlockPos blockposition2 = structurepiece.getBoundingBoxCenter();
            double d1 = blockposition2.distanceSq(blockposition);
            if (d1 < d0) {
               d0 = d1;
               blockposition1 = blockposition2;
            }
         }
      }

      if (blockposition1 != null) {
         return blockposition1;
      } else {
         List list = this.getCoordList();
         if (list != null) {
            BlockPos blockposition3 = null;

            for(BlockPos blockposition2 : list) {
               double d1 = blockposition2.distanceSq(blockposition);
               if (d1 < d0) {
                  d0 = d1;
                  blockposition3 = blockposition2;
               }
            }

            return blockposition3;
         } else {
            return null;
         }
      }
   }

   protected List getCoordList() {
      return null;
   }

   protected synchronized void initializeStructureData(World world) {
      if (this.structureData == null) {
         this.structureData = (MapGenStructureData)world.loadData(MapGenStructureData.class, this.getStructureName());
         if (this.structureData == null) {
            this.structureData = new MapGenStructureData(this.getStructureName());
            world.setData(this.getStructureName(), this.structureData);
         } else {
            NBTTagCompound nbttagcompound = this.structureData.getTagCompound();

            for(String s : nbttagcompound.getKeySet()) {
               NBTBase nbtbase = nbttagcompound.getTag(s);
               if (nbtbase.getId() == 10) {
                  NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbtbase;
                  if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ")) {
                     int i = nbttagcompound1.getInteger("ChunkX");
                     int j = nbttagcompound1.getInteger("ChunkZ");
                     StructureStart structurestart = MapGenStructureIO.getStructureStart(nbttagcompound1, world);
                     if (structurestart != null) {
                        this.structureMap.put(ChunkPos.asLong(i, j), structurestart);
                     }
                  }
               }
            }
         }
      }

   }

   private void setStructureStart(int i, int j, StructureStart structurestart) {
      this.structureData.writeInstance(structurestart.writeStructureComponentsToNBT(i, j), i, j);
      this.structureData.markDirty();
   }

   protected abstract boolean canSpawnStructureAtCoords(int var1, int var2);

   protected abstract StructureStart getStructureStart(int var1, int var2);
}
