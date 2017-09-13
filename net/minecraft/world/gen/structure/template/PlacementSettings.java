package net.minecraft.world.gen.structure.template;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class PlacementSettings {
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private boolean ignoreEntities = false;
   @Nullable
   private Block replacedBlock;
   @Nullable
   private ChunkPos chunk;
   @Nullable
   private StructureBoundingBox boundingBox;
   private boolean ignoreStructureBlock = true;
   private float integrity = 1.0F;
   @Nullable
   private Random random;
   @Nullable
   private Long setSeed;

   public PlacementSettings copy() {
      PlacementSettings var1 = new PlacementSettings();
      var1.mirror = this.mirror;
      var1.rotation = this.rotation;
      var1.ignoreEntities = this.ignoreEntities;
      var1.replacedBlock = this.replacedBlock;
      var1.chunk = this.chunk;
      var1.boundingBox = this.boundingBox;
      var1.ignoreStructureBlock = this.ignoreStructureBlock;
      var1.integrity = this.integrity;
      var1.random = this.random;
      var1.setSeed = this.setSeed;
      return var1;
   }

   public PlacementSettings setMirror(Mirror var1) {
      this.mirror = var1;
      return this;
   }

   public PlacementSettings setRotation(Rotation var1) {
      this.rotation = var1;
      return this;
   }

   public PlacementSettings setIgnoreEntities(boolean var1) {
      this.ignoreEntities = var1;
      return this;
   }

   public PlacementSettings setReplacedBlock(Block var1) {
      this.replacedBlock = var1;
      return this;
   }

   public PlacementSettings setChunk(ChunkPos var1) {
      this.chunk = var1;
      return this;
   }

   public PlacementSettings setBoundingBox(StructureBoundingBox var1) {
      this.boundingBox = var1;
      return this;
   }

   public PlacementSettings setSeed(@Nullable Long var1) {
      this.setSeed = var1;
      return this;
   }

   public PlacementSettings setRandom(@Nullable Random var1) {
      this.random = var1;
      return this;
   }

   public PlacementSettings setIntegrity(float var1) {
      this.integrity = var1;
      return this;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public PlacementSettings setIgnoreStructureBlock(boolean var1) {
      this.ignoreStructureBlock = var1;
      return this;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public Random getRandom(@Nullable BlockPos var1) {
      if (this.random != null) {
         return this.random;
      } else if (this.setSeed != null) {
         return this.setSeed.longValue() == 0L ? new Random(System.currentTimeMillis()) : new Random(this.setSeed.longValue());
      } else if (var1 == null) {
         return new Random(System.currentTimeMillis());
      } else {
         int var2 = var1.getX();
         int var3 = var1.getZ();
         return new Random((long)(var2 * var2 * 4987142 + var2 * 5947611) + (long)(var3 * var3) * 4392871L + (long)(var3 * 389711) ^ 987234911L);
      }
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public boolean getIgnoreEntities() {
      return this.ignoreEntities;
   }

   @Nullable
   public Block getReplacedBlock() {
      return this.replacedBlock;
   }

   @Nullable
   public StructureBoundingBox getBoundingBox() {
      if (this.boundingBox == null && this.chunk != null) {
         this.setBoundingBoxFromChunk();
      }

      return this.boundingBox;
   }

   public boolean getIgnoreStructureBlock() {
      return this.ignoreStructureBlock;
   }

   void setBoundingBoxFromChunk() {
      this.boundingBox = this.getBoundingBoxFromChunk(this.chunk);
   }

   @Nullable
   private StructureBoundingBox getBoundingBoxFromChunk(@Nullable ChunkPos var1) {
      if (var1 == null) {
         return null;
      } else {
         int var2 = var1.chunkXPos * 16;
         int var3 = var1.chunkZPos * 16;
         return new StructureBoundingBox(var2, 0, var3, var2 + 16 - 1, 255, var3 + 16 - 1);
      }
   }
}
