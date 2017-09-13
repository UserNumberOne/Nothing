package net.minecraft.village;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Village {
   private World world;
   private final List villageDoorInfoList = Lists.newArrayList();
   private BlockPos centerHelper = BlockPos.ORIGIN;
   private BlockPos center = BlockPos.ORIGIN;
   private int villageRadius;
   private int lastAddDoorTimestamp;
   private int tickCounter;
   private int numVillagers;
   private int noBreedTicks;
   private final TreeMap playerReputation = new TreeMap();
   private final List villageAgressors = Lists.newArrayList();
   private int numIronGolems;

   public Village() {
   }

   public Village(World var1) {
      this.world = var1;
   }

   public void setWorld(World var1) {
      this.world = var1;
   }

   public void tick(int var1) {
      this.tickCounter = var1;
      this.removeDeadAndOutOfRangeDoors();
      this.removeDeadAndOldAgressors();
      if (var1 % 20 == 0) {
         this.updateNumVillagers();
      }

      if (var1 % 30 == 0) {
         this.updateNumIronGolems();
      }

      int var2 = this.numVillagers / 10;
      if (this.numIronGolems < var2 && this.villageDoorInfoList.size() > 20 && this.world.rand.nextInt(7000) == 0) {
         Vec3d var3 = this.findRandomSpawnPos(this.center, 2, 4, 2);
         if (var3 != null) {
            EntityIronGolem var4 = new EntityIronGolem(this.world);
            var4.setPosition(var3.xCoord, var3.yCoord, var3.zCoord);
            this.world.spawnEntity(var4);
            ++this.numIronGolems;
         }
      }

   }

   private Vec3d findRandomSpawnPos(BlockPos var1, int var2, int var3, int var4) {
      for(int var5 = 0; var5 < 10; ++var5) {
         BlockPos var6 = var1.add(this.world.rand.nextInt(16) - 8, this.world.rand.nextInt(6) - 3, this.world.rand.nextInt(16) - 8);
         if (this.isBlockPosWithinSqVillageRadius(var6) && this.isAreaClearAround(new BlockPos(var2, var3, var4), var6)) {
            return new Vec3d((double)var6.getX(), (double)var6.getY(), (double)var6.getZ());
         }
      }

      return null;
   }

   private boolean isAreaClearAround(BlockPos var1, BlockPos var2) {
      if (!this.world.getBlockState(var2.down()).isFullyOpaque()) {
         return false;
      } else {
         int var3 = var2.getX() - var1.getX() / 2;
         int var4 = var2.getZ() - var1.getZ() / 2;

         for(int var5 = var3; var5 < var3 + var1.getX(); ++var5) {
            for(int var6 = var2.getY(); var6 < var2.getY() + var1.getY(); ++var6) {
               for(int var7 = var4; var7 < var4 + var1.getZ(); ++var7) {
                  if (this.world.getBlockState(new BlockPos(var5, var6, var7)).isNormalCube()) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   private void updateNumIronGolems() {
      List var1 = this.world.getEntitiesWithinAABB(EntityIronGolem.class, new AxisAlignedBB((double)(this.center.getX() - this.villageRadius), (double)(this.center.getY() - 4), (double)(this.center.getZ() - this.villageRadius), (double)(this.center.getX() + this.villageRadius), (double)(this.center.getY() + 4), (double)(this.center.getZ() + this.villageRadius)));
      this.numIronGolems = var1.size();
   }

   private void updateNumVillagers() {
      List var1 = this.world.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB((double)(this.center.getX() - this.villageRadius), (double)(this.center.getY() - 4), (double)(this.center.getZ() - this.villageRadius), (double)(this.center.getX() + this.villageRadius), (double)(this.center.getY() + 4), (double)(this.center.getZ() + this.villageRadius)));
      this.numVillagers = var1.size();
      if (this.numVillagers == 0) {
         this.playerReputation.clear();
      }

   }

   public BlockPos getCenter() {
      return this.center;
   }

   public int getVillageRadius() {
      return this.villageRadius;
   }

   public int getNumVillageDoors() {
      return this.villageDoorInfoList.size();
   }

   public int getTicksSinceLastDoorAdding() {
      return this.tickCounter - this.lastAddDoorTimestamp;
   }

   public int getNumVillagers() {
      return this.numVillagers;
   }

   public boolean isBlockPosWithinSqVillageRadius(BlockPos var1) {
      return this.center.distanceSq(var1) < (double)(this.villageRadius * this.villageRadius);
   }

   public List getVillageDoorInfoList() {
      return this.villageDoorInfoList;
   }

   public VillageDoorInfo getNearestDoor(BlockPos var1) {
      VillageDoorInfo var2 = null;
      int var3 = Integer.MAX_VALUE;

      for(VillageDoorInfo var5 : this.villageDoorInfoList) {
         int var6 = var5.getDistanceToDoorBlockSq(var1);
         if (var6 < var3) {
            var2 = var5;
            var3 = var6;
         }
      }

      return var2;
   }

   public VillageDoorInfo getDoorInfo(BlockPos var1) {
      VillageDoorInfo var2 = null;
      int var3 = Integer.MAX_VALUE;

      for(VillageDoorInfo var5 : this.villageDoorInfoList) {
         int var6 = var5.getDistanceToDoorBlockSq(var1);
         if (var6 > 256) {
            var6 = var6 * 1000;
         } else {
            var6 = var5.getDoorOpeningRestrictionCounter();
         }

         if (var6 < var3) {
            BlockPos var7 = var5.getDoorBlockPos();
            EnumFacing var8 = var5.getInsideDirection();
            if (this.world.getBlockState(var7.offset(var8, 1)).getBlock().isPassable(this.world, var7.offset(var8, 1)) && this.world.getBlockState(var7.offset(var8, -1)).getBlock().isPassable(this.world, var7.offset(var8, -1)) && this.world.getBlockState(var7.up().offset(var8, 1)).getBlock().isPassable(this.world, var7.up().offset(var8, 1)) && this.world.getBlockState(var7.up().offset(var8, -1)).getBlock().isPassable(this.world, var7.up().offset(var8, -1))) {
               var2 = var5;
               var3 = var6;
            }
         }
      }

      return var2;
   }

   public VillageDoorInfo getExistedDoor(BlockPos var1) {
      if (this.center.distanceSq(var1) > (double)(this.villageRadius * this.villageRadius)) {
         return null;
      } else {
         for(VillageDoorInfo var3 : this.villageDoorInfoList) {
            if (var3.getDoorBlockPos().getX() == var1.getX() && var3.getDoorBlockPos().getZ() == var1.getZ() && Math.abs(var3.getDoorBlockPos().getY() - var1.getY()) <= 1) {
               return var3;
            }
         }

         return null;
      }
   }

   public void addVillageDoorInfo(VillageDoorInfo var1) {
      this.villageDoorInfoList.add(var1);
      this.centerHelper = this.centerHelper.add(var1.getDoorBlockPos());
      this.updateVillageRadiusAndCenter();
      this.lastAddDoorTimestamp = var1.getInsidePosY();
   }

   public boolean isAnnihilated() {
      return this.villageDoorInfoList.isEmpty();
   }

   public void addOrRenewAgressor(EntityLivingBase var1) {
      for(Village.VillageAggressor var3 : this.villageAgressors) {
         if (var3.agressor == var1) {
            var3.agressionTime = this.tickCounter;
            return;
         }
      }

      this.villageAgressors.add(new Village.VillageAggressor(var1, this.tickCounter));
   }

   public EntityLivingBase findNearestVillageAggressor(EntityLivingBase var1) {
      double var2 = Double.MAX_VALUE;
      Village.VillageAggressor var4 = null;

      for(int var5 = 0; var5 < this.villageAgressors.size(); ++var5) {
         Village.VillageAggressor var6 = (Village.VillageAggressor)this.villageAgressors.get(var5);
         double var7 = var6.agressor.getDistanceSqToEntity(var1);
         if (var7 <= var2) {
            var4 = var6;
            var2 = var7;
         }
      }

      return var4 != null ? var4.agressor : null;
   }

   public EntityPlayer getNearestTargetPlayer(EntityLivingBase var1) {
      double var2 = Double.MAX_VALUE;
      EntityPlayer var4 = null;

      for(String var6 : this.playerReputation.keySet()) {
         if (this.isPlayerReputationTooLow(var6)) {
            EntityPlayer var7 = this.world.getPlayerEntityByName(var6);
            if (var7 != null) {
               double var8 = var7.getDistanceSqToEntity(var1);
               if (var8 <= var2) {
                  var4 = var7;
                  var2 = var8;
               }
            }
         }
      }

      return var4;
   }

   private void removeDeadAndOldAgressors() {
      Iterator var1 = this.villageAgressors.iterator();

      while(var1.hasNext()) {
         Village.VillageAggressor var2 = (Village.VillageAggressor)var1.next();
         if (!var2.agressor.isEntityAlive() || Math.abs(this.tickCounter - var2.agressionTime) > 300) {
            var1.remove();
         }
      }

   }

   private void removeDeadAndOutOfRangeDoors() {
      boolean var1 = false;
      boolean var2 = this.world.rand.nextInt(50) == 0;
      Iterator var3 = this.villageDoorInfoList.iterator();

      while(var3.hasNext()) {
         VillageDoorInfo var4 = (VillageDoorInfo)var3.next();
         if (var2) {
            var4.resetDoorOpeningRestrictionCounter();
         }

         if (!this.isWoodDoor(var4.getDoorBlockPos()) || Math.abs(this.tickCounter - var4.getInsidePosY()) > 1200) {
            this.centerHelper = this.centerHelper.subtract(var4.getDoorBlockPos());
            var1 = true;
            var4.setIsDetachedFromVillageFlag(true);
            var3.remove();
         }
      }

      if (var1) {
         this.updateVillageRadiusAndCenter();
      }

   }

   private boolean isWoodDoor(BlockPos var1) {
      IBlockState var2 = this.world.getBlockState(var1);
      Block var3 = var2.getBlock();
      return var3 instanceof BlockDoor ? var2.getMaterial() == Material.WOOD : false;
   }

   private void updateVillageRadiusAndCenter() {
      int var1 = this.villageDoorInfoList.size();
      if (var1 == 0) {
         this.center = BlockPos.ORIGIN;
         this.villageRadius = 0;
      } else {
         this.center = new BlockPos(this.centerHelper.getX() / var1, this.centerHelper.getY() / var1, this.centerHelper.getZ() / var1);
         int var2 = 0;

         for(VillageDoorInfo var4 : this.villageDoorInfoList) {
            var2 = Math.max(var4.getDistanceToDoorBlockSq(this.center), var2);
         }

         this.villageRadius = Math.max(32, (int)Math.sqrt((double)var2) + 1);
      }

   }

   public int getPlayerReputation(String var1) {
      Integer var2 = (Integer)this.playerReputation.get(var1);
      return var2 != null ? var2.intValue() : 0;
   }

   public int modifyPlayerReputation(String var1, int var2) {
      int var3 = this.getPlayerReputation(var1);
      int var4 = MathHelper.clamp(var3 + var2, -30, 10);
      this.playerReputation.put(var1, Integer.valueOf(var4));
      return var4;
   }

   public boolean isPlayerReputationTooLow(String var1) {
      return this.getPlayerReputation(var1) <= -15;
   }

   public void readVillageDataFromNBT(NBTTagCompound var1) {
      this.numVillagers = var1.getInteger("PopSize");
      this.villageRadius = var1.getInteger("Radius");
      this.numIronGolems = var1.getInteger("Golems");
      this.lastAddDoorTimestamp = var1.getInteger("Stable");
      this.tickCounter = var1.getInteger("Tick");
      this.noBreedTicks = var1.getInteger("MTick");
      this.center = new BlockPos(var1.getInteger("CX"), var1.getInteger("CY"), var1.getInteger("CZ"));
      this.centerHelper = new BlockPos(var1.getInteger("ACX"), var1.getInteger("ACY"), var1.getInteger("ACZ"));
      NBTTagList var2 = var1.getTagList("Doors", 10);

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         VillageDoorInfo var5 = new VillageDoorInfo(new BlockPos(var4.getInteger("X"), var4.getInteger("Y"), var4.getInteger("Z")), var4.getInteger("IDX"), var4.getInteger("IDZ"), var4.getInteger("TS"));
         this.villageDoorInfoList.add(var5);
      }

      NBTTagList var8 = var1.getTagList("Players", 10);

      for(int var9 = 0; var9 < var8.tagCount(); ++var9) {
         NBTTagCompound var10 = var8.getCompoundTagAt(var9);
         if (var10.hasKey("UUID") && this.world != null && this.world.getMinecraftServer() != null) {
            PlayerProfileCache var6 = this.world.getMinecraftServer().getPlayerProfileCache();
            GameProfile var7 = var6.getProfileByUUID(UUID.fromString(var10.getString("UUID")));
            if (var7 != null) {
               this.playerReputation.put(var7.getName(), Integer.valueOf(var10.getInteger("S")));
            }
         } else {
            this.playerReputation.put(var10.getString("Name"), Integer.valueOf(var10.getInteger("S")));
         }
      }

   }

   public void writeVillageDataToNBT(NBTTagCompound var1) {
      var1.setInteger("PopSize", this.numVillagers);
      var1.setInteger("Radius", this.villageRadius);
      var1.setInteger("Golems", this.numIronGolems);
      var1.setInteger("Stable", this.lastAddDoorTimestamp);
      var1.setInteger("Tick", this.tickCounter);
      var1.setInteger("MTick", this.noBreedTicks);
      var1.setInteger("CX", this.center.getX());
      var1.setInteger("CY", this.center.getY());
      var1.setInteger("CZ", this.center.getZ());
      var1.setInteger("ACX", this.centerHelper.getX());
      var1.setInteger("ACY", this.centerHelper.getY());
      var1.setInteger("ACZ", this.centerHelper.getZ());
      NBTTagList var2 = new NBTTagList();

      for(VillageDoorInfo var4 : this.villageDoorInfoList) {
         NBTTagCompound var5 = new NBTTagCompound();
         var5.setInteger("X", var4.getDoorBlockPos().getX());
         var5.setInteger("Y", var4.getDoorBlockPos().getY());
         var5.setInteger("Z", var4.getDoorBlockPos().getZ());
         var5.setInteger("IDX", var4.getInsideOffsetX());
         var5.setInteger("IDZ", var4.getInsideOffsetZ());
         var5.setInteger("TS", var4.getInsidePosY());
         var2.appendTag(var5);
      }

      var1.setTag("Doors", var2);
      NBTTagList var9 = new NBTTagList();

      for(String var11 : this.playerReputation.keySet()) {
         NBTTagCompound var6 = new NBTTagCompound();
         PlayerProfileCache var7 = this.world.getMinecraftServer().getPlayerProfileCache();
         GameProfile var8 = var7.getGameProfileForUsername(var11);
         if (var8 != null) {
            var6.setString("UUID", var8.getId().toString());
            var6.setInteger("S", ((Integer)this.playerReputation.get(var11)).intValue());
            var9.appendTag(var6);
         }
      }

      var1.setTag("Players", var9);
   }

   public void endMatingSeason() {
      this.noBreedTicks = this.tickCounter;
   }

   public boolean isMatingSeason() {
      return this.noBreedTicks == 0 || this.tickCounter - this.noBreedTicks >= 3600;
   }

   public void setDefaultPlayerReputation(int var1) {
      for(String var3 : this.playerReputation.keySet()) {
         this.modifyPlayerReputation(var3, var1);
      }

   }

   class VillageAggressor {
      public EntityLivingBase agressor;
      public int agressionTime;

      VillageAggressor(EntityLivingBase var2, int var3) {
         this.agressor = var2;
         this.agressionTime = var3;
      }
   }
}
