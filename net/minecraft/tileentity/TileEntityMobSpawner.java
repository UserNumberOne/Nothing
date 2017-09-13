package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityMobSpawner extends TileEntity implements ITickable {
   private final MobSpawnerBaseLogic spawnerLogic = new MobSpawnerBaseLogic() {
      public void broadcastEvent(int var1) {
         TileEntityMobSpawner.this.world.addBlockEvent(TileEntityMobSpawner.this.pos, Blocks.MOB_SPAWNER, var1, 0);
      }

      public World getSpawnerWorld() {
         return TileEntityMobSpawner.this.world;
      }

      public BlockPos getSpawnerPosition() {
         return TileEntityMobSpawner.this.pos;
      }

      public void setNextSpawnData(WeightedSpawnerEntity var1) {
         super.setNextSpawnData(var1);
         if (this.getSpawnerWorld() != null) {
            IBlockState var2 = this.getSpawnerWorld().getBlockState(this.getSpawnerPosition());
            this.getSpawnerWorld().notifyBlockUpdate(TileEntityMobSpawner.this.pos, var2, var2, 4);
         }

      }
   };

   public static void registerFixesMobSpawner(DataFixer var0) {
      var0.registerWalker(FixTypes.BLOCK_ENTITY, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if ("MobSpawner".equals(var2.getString("id"))) {
               if (var2.hasKey("SpawnPotentials", 9)) {
                  NBTTagList var4 = var2.getTagList("SpawnPotentials", 10);

                  for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
                     NBTTagCompound var6 = var4.getCompoundTagAt(var5);
                     var6.setTag("Entity", var1.process(FixTypes.ENTITY, var6.getCompoundTag("Entity"), var3));
                  }
               }

               var2.setTag("SpawnData", var1.process(FixTypes.ENTITY, var2.getCompoundTag("SpawnData"), var3));
            }

            return var2;
         }
      });
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.spawnerLogic.readFromNBT(var1);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      this.spawnerLogic.writeToNBT(var1);
      return var1;
   }

   public void update() {
      this.spawnerLogic.updateSpawner();
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      NBTTagCompound var1 = this.writeToNBT(new NBTTagCompound());
      var1.removeTag("SpawnPotentials");
      return var1;
   }

   public boolean receiveClientEvent(int var1, int var2) {
      return this.spawnerLogic.setDelayToMin(var1) ? true : super.receiveClientEvent(var1, var2);
   }

   public boolean onlyOpsCanSetNbt() {
      return true;
   }

   public MobSpawnerBaseLogic getSpawnerBaseLogic() {
      return this.spawnerLogic;
   }
}
