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
      PlacementSettings placementsettings = new PlacementSettings();
      placementsettings.mirror = this.mirror;
      placementsettings.rotation = this.rotation;
      placementsettings.ignoreEntities = this.ignoreEntities;
      placementsettings.replacedBlock = this.replacedBlock;
      placementsettings.chunk = this.chunk;
      placementsettings.boundingBox = this.boundingBox;
      placementsettings.ignoreStructureBlock = this.ignoreStructureBlock;
      placementsettings.integrity = this.integrity;
      placementsettings.random = this.random;
      placementsettings.setSeed = this.setSeed;
      return placementsettings;
   }

   public PlacementSettings setMirror(Mirror var1) {
      this.mirror = mirrorIn;
      return this;
   }

   public PlacementSettings setRotation(Rotation var1) {
      this.rotation = rotationIn;
      return this;
   }

   public PlacementSettings setIgnoreEntities(boolean var1) {
      this.ignoreEntities = ignoreEntitiesIn;
      return this;
   }

   public PlacementSettings setReplacedBlock(Block var1) {
      this.replacedBlock = replacedBlockIn;
      return this;
   }

   public PlacementSettings setChunk(ChunkPos var1) {
      this.chunk = chunkPosIn;
      return this;
   }

   public PlacementSettings setBoundingBox(StructureBoundingBox var1) {
      this.boundingBox = boundingBoxIn;
      return this;
   }

   public PlacementSettings setSeed(@Nullable Long var1) {
      this.setSeed = p_189949_1_;
      return this;
   }

   public PlacementSettings setRandom(@Nullable Random var1) {
      this.random = p_189950_1_;
      return this;
   }

   public PlacementSettings setIntegrity(float var1) {
      this.integrity = p_189946_1_;
      return this;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public PlacementSettings setIgnoreStructureBlock(boolean var1) {
      this.ignoreStructureBlock = ignoreStructureBlockIn;
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
      } else if (p_189947_1_ == null) {
         return new Random(System.currentTimeMillis());
      } else {
         int i = p_189947_1_.getX();
         int j = p_189947_1_.getZ();
         return new Random((long)(i * i * 4987142 + i * 5947611) + (long)(j * j) * 4392871L + (long)(j * 389711) ^ 987234911L);
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
      if (pos == null) {
         return null;
      } else {
         int i = pos.chunkXPos * 16;
         int j = pos.chunkZPos * 16;
         return new StructureBoundingBox(i, 0, j, i + 16 - 1, 255, j + 16 - 1);
      }
   }
}
