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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMinecartMobSpawner extends EntityMinecart {
   private final MobSpawnerBaseLogic mobSpawnerLogic = new MobSpawnerBaseLogic() {
      public void broadcastEvent(int var1) {
         EntityMinecartMobSpawner.this.world.setEntityState(EntityMinecartMobSpawner.this, (byte)id);
      }

      public World getSpawnerWorld() {
         return EntityMinecartMobSpawner.this.world;
      }

      public BlockPos getSpawnerPosition() {
         return new BlockPos(EntityMinecartMobSpawner.this);
      }
   };

   public EntityMinecartMobSpawner(World var1) {
      super(worldIn);
   }

   public EntityMinecartMobSpawner(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesMinecartMobSpawner(DataFixer var0) {
      registerFixesMinecart(fixer, "MinecartSpawner");
      fixer.registerWalker(FixTypes.ENTITY, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if ("MinecartSpawner".equals(compound.getString("id"))) {
               compound.setString("id", "MobSpawner");
               fixer.process(FixTypes.BLOCK_ENTITY, compound, versionIn);
               compound.setString("id", "MinecartSpawner");
            }

            return compound;
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
      super.readEntityFromNBT(compound);
      this.mobSpawnerLogic.readFromNBT(compound);
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      this.mobSpawnerLogic.writeToNBT(compound);
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      this.mobSpawnerLogic.setDelayToMin(id);
   }

   public void onUpdate() {
      super.onUpdate();
      this.mobSpawnerLogic.updateSpawner();
   }
}
