package net.minecraft.village;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSavedData;

public class VillageCollection extends WorldSavedData {
   private World world;
   private final List villagerPositionsList = Lists.newArrayList();
   private final List newDoors = Lists.newArrayList();
   private final List villageList = Lists.newArrayList();
   private int tickCounter;

   public VillageCollection(String var1) {
      super(var1);
   }

   public VillageCollection(World var1) {
      super(fileNameForProvider(var1.provider));
      this.world = var1;
      this.markDirty();
   }

   public void setWorldsForAll(World var1) {
      this.world = var1;

      for(Village var3 : this.villageList) {
         var3.setWorld(var1);
      }

   }

   public void addToVillagerPositionList(BlockPos var1) {
      if (this.villagerPositionsList.size() <= 64 && !this.positionInList(var1)) {
         this.villagerPositionsList.add(var1);
      }

   }

   public void tick() {
      ++this.tickCounter;

      for(Village var2 : this.villageList) {
         var2.tick(this.tickCounter);
      }

      this.removeAnnihilatedVillages();
      this.dropOldestVillagerPosition();
      this.addNewDoorsToVillageOrCreateVillage();
      if (this.tickCounter % 400 == 0) {
         this.markDirty();
      }

   }

   private void removeAnnihilatedVillages() {
      Iterator var1 = this.villageList.iterator();

      while(var1.hasNext()) {
         Village var2 = (Village)var1.next();
         if (var2.isAnnihilated()) {
            var1.remove();
            this.markDirty();
         }
      }

   }

   public List getVillageList() {
      return this.villageList;
   }

   public Village getNearestVillage(BlockPos var1, int var2) {
      Village var3 = null;
      double var4 = 3.4028234663852886E38D;

      for(Village var7 : this.villageList) {
         double var8 = var7.getCenter().distanceSq(var1);
         if (var8 < var4) {
            float var10 = (float)(var2 + var7.getVillageRadius());
            if (var8 <= (double)(var10 * var10)) {
               var3 = var7;
               var4 = var8;
            }
         }
      }

      return var3;
   }

   private void dropOldestVillagerPosition() {
      if (!this.villagerPositionsList.isEmpty()) {
         this.addDoorsAround((BlockPos)this.villagerPositionsList.remove(0));
      }

   }

   private void addNewDoorsToVillageOrCreateVillage() {
      for(int var1 = 0; var1 < this.newDoors.size(); ++var1) {
         VillageDoorInfo var2 = (VillageDoorInfo)this.newDoors.get(var1);
         Village var3 = this.getNearestVillage(var2.getDoorBlockPos(), 32);
         if (var3 == null) {
            var3 = new Village(this.world);
            this.villageList.add(var3);
            this.markDirty();
         }

         var3.addVillageDoorInfo(var2);
      }

      this.newDoors.clear();
   }

   private void addDoorsAround(BlockPos var1) {
      boolean var2 = true;
      boolean var3 = true;
      boolean var4 = true;

      for(int var5 = -16; var5 < 16; ++var5) {
         for(int var6 = -4; var6 < 4; ++var6) {
            for(int var7 = -16; var7 < 16; ++var7) {
               BlockPos var8 = var1.add(var5, var6, var7);
               if (this.isWoodDoor(var8)) {
                  VillageDoorInfo var9 = this.checkDoorExistence(var8);
                  if (var9 == null) {
                     this.addToNewDoorsList(var8);
                  } else {
                     var9.setLastActivityTimestamp(this.tickCounter);
                  }
               }
            }
         }
      }

   }

   private VillageDoorInfo checkDoorExistence(BlockPos var1) {
      for(VillageDoorInfo var3 : this.newDoors) {
         if (var3.getDoorBlockPos().getX() == var1.getX() && var3.getDoorBlockPos().getZ() == var1.getZ() && Math.abs(var3.getDoorBlockPos().getY() - var1.getY()) <= 1) {
            return var3;
         }
      }

      for(Village var6 : this.villageList) {
         VillageDoorInfo var4 = var6.getExistedDoor(var1);
         if (var4 != null) {
            return var4;
         }
      }

      return null;
   }

   private void addToNewDoorsList(BlockPos var1) {
      EnumFacing var2 = BlockDoor.getFacing(this.world, var1);
      EnumFacing var3 = var2.getOpposite();
      int var4 = this.countBlocksCanSeeSky(var1, var2, 5);
      int var5 = this.countBlocksCanSeeSky(var1, var3, var4 + 1);
      if (var4 != var5) {
         this.newDoors.add(new VillageDoorInfo(var1, var4 < var5 ? var2 : var3, this.tickCounter));
      }

   }

   private int countBlocksCanSeeSky(BlockPos var1, EnumFacing var2, int var3) {
      int var4 = 0;

      for(int var5 = 1; var5 <= 5; ++var5) {
         if (this.world.canSeeSky(var1.offset(var2, var5))) {
            ++var4;
            if (var4 >= var3) {
               return var4;
            }
         }
      }

      return var4;
   }

   private boolean positionInList(BlockPos var1) {
      for(BlockPos var3 : this.villagerPositionsList) {
         if (var3.equals(var1)) {
            return true;
         }
      }

      return false;
   }

   private boolean isWoodDoor(BlockPos var1) {
      IBlockState var2 = this.world.getBlockState(var1);
      Block var3 = var2.getBlock();
      return var3 instanceof BlockDoor ? var2.getMaterial() == Material.WOOD : false;
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.tickCounter = var1.getInteger("Tick");
      NBTTagList var2 = var1.getTagList("Villages", 10);

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         Village var5 = new Village();
         var5.readVillageDataFromNBT(var4);
         this.villageList.add(var5);
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      var1.setInteger("Tick", this.tickCounter);
      NBTTagList var2 = new NBTTagList();

      for(Village var4 : this.villageList) {
         NBTTagCompound var5 = new NBTTagCompound();
         var4.writeVillageDataToNBT(var5);
         var2.appendTag(var5);
      }

      var1.setTag("Villages", var2);
      return var1;
   }

   public static String fileNameForProvider(WorldProvider var0) {
      return "villages" + var0.getDimensionType().getSuffix();
   }
}
