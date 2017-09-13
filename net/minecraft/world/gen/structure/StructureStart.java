package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public abstract class StructureStart {
   protected List components = Lists.newLinkedList();
   protected StructureBoundingBox boundingBox;
   private int chunkPosX;
   private int chunkPosZ;

   public StructureStart() {
   }

   public StructureStart(int var1, int var2) {
      this.chunkPosX = var1;
      this.chunkPosZ = var2;
   }

   public StructureBoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public List getComponents() {
      return this.components;
   }

   public void generateStructure(World var1, Random var2, StructureBoundingBox var3) {
      Iterator var4 = this.components.iterator();

      while(var4.hasNext()) {
         StructureComponent var5 = (StructureComponent)var4.next();
         if (var5.getBoundingBox().intersectsWith(var3) && !var5.addComponentParts(var1, var2, var3)) {
            var4.remove();
         }
      }

   }

   protected void updateBoundingBox() {
      this.boundingBox = StructureBoundingBox.getNewBoundingBox();

      for(StructureComponent var2 : this.components) {
         this.boundingBox.expandTo(var2.getBoundingBox());
      }

   }

   public NBTTagCompound writeStructureComponentsToNBT(int var1, int var2) {
      if (MapGenStructureIO.getStructureStartName(this) == null) {
         throw new RuntimeException("StructureStart \"" + this.getClass().getName() + "\" missing ID Mapping, Modder see MapGenStructureIO");
      } else {
         NBTTagCompound var3 = new NBTTagCompound();
         var3.setString("id", MapGenStructureIO.getStructureStartName(this));
         var3.setInteger("ChunkX", var1);
         var3.setInteger("ChunkZ", var2);
         var3.setTag("BB", this.boundingBox.toNBTTagIntArray());
         NBTTagList var4 = new NBTTagList();

         for(StructureComponent var6 : this.components) {
            var4.appendTag(var6.createStructureBaseNBT());
         }

         var3.setTag("Children", var4);
         this.writeToNBT(var3);
         return var3;
      }
   }

   public void writeToNBT(NBTTagCompound var1) {
   }

   public void readStructureComponentsFromNBT(World var1, NBTTagCompound var2) {
      this.chunkPosX = var2.getInteger("ChunkX");
      this.chunkPosZ = var2.getInteger("ChunkZ");
      if (var2.hasKey("BB")) {
         this.boundingBox = new StructureBoundingBox(var2.getIntArray("BB"));
      }

      NBTTagList var3 = var2.getTagList("Children", 10);

      for(int var4 = 0; var4 < var3.tagCount(); ++var4) {
         StructureComponent var5 = MapGenStructureIO.getStructureComponent(var3.getCompoundTagAt(var4), var1);
         if (var5 != null) {
            this.components.add(var5);
         }
      }

      this.readFromNBT(var2);
   }

   public void readFromNBT(NBTTagCompound var1) {
   }

   protected void markAvailableHeight(World var1, Random var2, int var3) {
      int var4 = var1.getSeaLevel() - var3;
      int var5 = this.boundingBox.getYSize() + 1;
      if (var5 < var4) {
         var5 += var2.nextInt(var4 - var5);
      }

      int var6 = var5 - this.boundingBox.maxY;
      this.boundingBox.offset(0, var6, 0);

      for(StructureComponent var8 : this.components) {
         var8.offset(0, var6, 0);
      }

   }

   protected void setRandomHeight(World var1, Random var2, int var3, int var4) {
      int var5 = var4 - var3 + 1 - this.boundingBox.getYSize();
      int var6;
      if (var5 > 1) {
         var6 = var3 + var2.nextInt(var5);
      } else {
         var6 = var3;
      }

      int var7 = var6 - this.boundingBox.minY;
      this.boundingBox.offset(0, var7, 0);

      for(StructureComponent var9 : this.components) {
         var9.offset(0, var7, 0);
      }

   }

   public boolean isSizeableStructure() {
      return true;
   }

   public boolean isValidForPostProcess(ChunkPos var1) {
      return true;
   }

   public void notifyPostProcessAt(ChunkPos var1) {
   }

   public int getChunkPosX() {
      return this.chunkPosX;
   }

   public int getChunkPosZ() {
      return this.chunkPosZ;
   }
}
