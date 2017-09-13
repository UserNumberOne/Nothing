package net.minecraft.entity.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityMinecartMobSpawner extends EntityMinecart {
   private final MobSpawnerBaseLogic mobSpawnerLogic = new MobSpawnerBaseLogic() {
      public void broadcastEvent(int var1) {
         EntityMinecartMobSpawner.this.world.setEntityState(EntityMinecartMobSpawner.this, (byte)var1);
      }

      public World getSpawnerWorld() {
         return EntityMinecartMobSpawner.this.world;
      }

      public BlockPos getSpawnerPosition() {
         return new BlockPos(EntityMinecartMobSpawner.this);
      }
   };

   public EntityMinecartMobSpawner(World var1) {
      super(var1);
   }

   public EntityMinecartMobSpawner(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public static void registerFixesMinecartMobSpawner(DataFixer var0) {
      registerFixesMinecart(var0, "MinecartSpawner");
      var0.registerWalker(FixTypes.ENTITY, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if ("MinecartSpawner".equals(var2.getString("id"))) {
               var2.setString("id", "MobSpawner");
               var1.process(FixTypes.BLOCK_ENTITY, var2, var3);
               var2.setString("id", "MinecartSpawner");
            }

            return var2;
         }
      });
   }

   public EntityMinecart.Type getType() {
      return EntityMinecart.Type.SPAWNER;
   }

   public IBlockState getDefaultDisplayTile() {
      return Blocks.MOB_SPAWNER.getDefaultState();
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.mobSpawnerLogic.readFromNBT(var1);
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      this.mobSpawnerLogic.writeToNBT(var1);
   }

   public void onUpdate() {
      super.onUpdate();
      this.mobSpawnerLogic.updateSpawner();
   }
}
