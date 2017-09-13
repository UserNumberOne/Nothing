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
      this.chunkPosX = chunkX;
      this.chunkPosZ = chunkZ;
   }

   public StructureBoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public List getComponents() {
      return this.components;
   }

   public void generateStructure(World var1, Random var2, StructureBoundingBox var3) {
      Iterator iterator = this.components.iterator();

      while(iterator.hasNext()) {
         StructureComponent structurecomponent = (StructureComponent)iterator.next();
         if (structurecomponent.getBoundingBox().intersectsWith(structurebb) && !structurecomponent.addComponentParts(worldIn, rand, structurebb)) {
            iterator.remove();
         }
      }

   }

   protected void updateBoundingBox() {
      this.boundingBox = StructureBoundingBox.getNewBoundingBox();

      for(StructureComponent structurecomponent : this.components) {
         this.boundingBox.expandTo(structurecomponent.getBoundingBox());
      }

   }

   public NBTTagCompound writeStructureComponentsToNBT(int var1, int var2) {
      if (MapGenStructureIO.getStructureStartName(this) == null) {
         throw new RuntimeException("StructureStart \"" + this.getClass().getName() + "\" missing ID Mapping, Modder see MapGenStructureIO");
      } else {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         nbttagcompound.setString("id", MapGenStructureIO.getStructureStartName(this));
         nbttagcompound.setInteger("ChunkX", chunkX);
         nbttagcompound.setInteger("ChunkZ", chunkZ);
         nbttagcompound.setTag("BB", this.boundingBox.toNBTTagIntArray());
         NBTTagList nbttaglist = new NBTTagList();

         for(StructureComponent structurecomponent : this.components) {
            nbttaglist.appendTag(structurecomponent.createStructureBaseNBT());
         }

         nbttagcompound.setTag("Children", nbttaglist);
         this.writeToNBT(nbttagcompound);
         return nbttagcompound;
      }
   }

   public void writeToNBT(NBTTagCompound var1) {
   }

   public void readStructureComponentsFromNBT(World var1, NBTTagCompound var2) {
      this.chunkPosX = tagCompound.getInteger("ChunkX");
      this.chunkPosZ = tagCompound.getInteger("ChunkZ");
      if (tagCompound.hasKey("BB")) {
         this.boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB"));
      }

      NBTTagList nbttaglist = tagCompound.getTagList("Children", 10);

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         StructureComponent tmp = MapGenStructureIO.getStructureComponent(nbttaglist.getCompoundTagAt(i), worldIn);
         if (tmp != null) {
            this.components.add(tmp);
         }
      }

      this.readFromNBT(tagCompound);
   }

   public void readFromNBT(NBTTagCompound var1) {
   }

   protected void markAvailableHeight(World var1, Random var2, int var3) {
      int i = worldIn.getSeaLevel() - p_75067_3_;
      int j = this.boundingBox.getYSize() + 1;
      if (j < i) {
         j += rand.nextInt(i - j);
      }

      int k = j - this.boundingBox.maxY;
      this.boundingBox.offset(0, k, 0);

      for(StructureComponent structurecomponent : this.components) {
         structurecomponent.offset(0, k, 0);
      }

   }

   protected void setRandomHeight(World var1, Random var2, int var3, int var4) {
      int i = p_75070_4_ - p_75070_3_ + 1 - this.boundingBox.getYSize();
      int j;
      if (i > 1) {
         j = p_75070_3_ + rand.nextInt(i);
      } else {
         j = p_75070_3_;
      }

      int k = j - this.boundingBox.minY;
      this.boundingBox.offset(0, k, 0);

      for(StructureComponent structurecomponent : this.components) {
         structurecomponent.offset(0, k, 0);
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
