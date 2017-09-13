package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class MapGenStronghold extends MapGenStructure {
   private final List allowedBiomes;
   private boolean ranBiomeCheck;
   private ChunkPos[] structureCoords;
   private double distance;
   private int spread;

   public MapGenStronghold() {
      this.structureCoords = new ChunkPos[128];
      this.distance = 32.0D;
      this.spread = 3;
      this.allowedBiomes = Lists.newArrayList();

      for(Biome var2 : Biome.REGISTRY) {
         if (var2 != null && var2.getBaseHeight() > 0.0F) {
            this.allowedBiomes.add(var2);
         }
      }

   }

   public MapGenStronghold(Map var1) {
      this();

      for(Entry var3 : var1.entrySet()) {
         if (((String)var3.getKey()).equals("distance")) {
            this.distance = MathHelper.getDouble((String)var3.getValue(), this.distance, 1.0D);
         } else if (((String)var3.getKey()).equals("count")) {
            this.structureCoords = new ChunkPos[MathHelper.getInt((String)var3.getValue(), this.structureCoords.length, 1)];
         } else if (((String)var3.getKey()).equals("spread")) {
            this.spread = MathHelper.getInt((String)var3.getValue(), this.spread, 1);
         }
      }

   }

   public String getStructureName() {
      return "Stronghold";
   }

   public BlockPos getClosestStrongholdPos(World var1, BlockPos var2) {
      if (!this.ranBiomeCheck) {
         this.generatePositions();
         this.ranBiomeCheck = true;
      }

      BlockPos var3 = null;
      BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos(0, 0, 0);
      double var5 = Double.MAX_VALUE;

      for(ChunkPos var10 : this.structureCoords) {
         var4.setPos((var10.chunkXPos << 4) + 8, 32, (var10.chunkZPos << 4) + 8);
         double var11 = var4.distanceSq(var2);
         if (var3 == null) {
            var3 = new BlockPos(var4);
            var5 = var11;
         } else if (var11 < var5) {
            var3 = new BlockPos(var4);
            var5 = var11;
         }
      }

      return var3;
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      if (!this.ranBiomeCheck) {
         this.generatePositions();
         this.ranBiomeCheck = true;
      }

      for(ChunkPos var6 : this.structureCoords) {
         if (var1 == var6.chunkXPos && var2 == var6.chunkZPos) {
            return true;
         }
      }

      return false;
   }

   private void generatePositions() {
      this.initializeStructureData(this.world);
      int var1 = 0;

      for(StructureStart var3 : this.structureMap.values()) {
         if (var1 < this.structureCoords.length) {
            this.structureCoords[var1++] = new ChunkPos(var3.getChunkPosX(), var3.getChunkPosZ());
         }
      }

      Random var15 = new Random();
      var15.setSeed(this.world.getSeed());
      double var4 = var15.nextDouble() * 3.141592653589793D * 2.0D;
      int var6 = 0;
      int var7 = 0;
      int var8 = this.structureMap.size();
      if (var8 < this.structureCoords.length) {
         for(int var9 = 0; var9 < this.structureCoords.length; ++var9) {
            double var10 = 4.0D * this.distance + this.distance * (double)var6 * 6.0D + (var15.nextDouble() - 0.5D) * this.distance * 2.5D;
            int var12 = (int)Math.round(Math.cos(var4) * var10);
            int var13 = (int)Math.round(Math.sin(var4) * var10);
            BlockPos var14 = this.world.getBiomeProvider().findBiomePosition((var12 << 4) + 8, (var13 << 4) + 8, 112, this.allowedBiomes, var15);
            if (var14 != null) {
               var12 = var14.getX() >> 4;
               var13 = var14.getZ() >> 4;
            }

            if (var9 >= var8) {
               this.structureCoords[var9] = new ChunkPos(var12, var13);
            }

            var4 += 6.283185307179586D / (double)this.spread;
            ++var7;
            if (var7 == this.spread) {
               ++var6;
               var7 = 0;
               this.spread += 2 * this.spread / (var6 + 1);
               this.spread = Math.min(this.spread, this.structureCoords.length - var9);
               var4 += var15.nextDouble() * 3.141592653589793D * 2.0D;
            }
         }
      }

   }

   protected List getCoordList() {
      ArrayList var1 = Lists.newArrayList();

      for(ChunkPos var5 : this.structureCoords) {
         if (var5 != null) {
            var1.add(var5.getCenterBlock(64));
         }
      }

      return var1;
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      MapGenStronghold.Start var3;
      for(var3 = new MapGenStronghold.Start(this.world, this.rand, var1, var2); var3.getComponents().isEmpty() || ((StructureStrongholdPieces.Stairs2)var3.getComponents().get(0)).strongholdPortalRoom == null; var3 = new MapGenStronghold.Start(this.world, this.rand, var1, var2)) {
         ;
      }

      return var3;
   }

   public static class Start extends StructureStart {
      public Start() {
      }

      public Start(World var1, Random var2, int var3, int var4) {
         super(var3, var4);
         StructureStrongholdPieces.prepareStructurePieces();
         StructureStrongholdPieces.Stairs2 var5 = new StructureStrongholdPieces.Stairs2(0, var2, (var3 << 4) + 2, (var4 << 4) + 2);
         this.components.add(var5);
         var5.buildComponent(var5, this.components, var2);
         List var6 = var5.pendingChildren;

         while(!var6.isEmpty()) {
            int var7 = var2.nextInt(var6.size());
            StructureComponent var8 = (StructureComponent)var6.remove(var7);
            var8.buildComponent(var5, this.components, var2);
         }

         this.updateBoundingBox();
         this.markAvailableHeight(var1, var2, 10);
      }
   }
}
