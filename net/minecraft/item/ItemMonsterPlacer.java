package net.minecraft.item;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class ItemMonsterPlacer extends Item {
   public ItemMonsterPlacer() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public String getItemStackDisplayName(ItemStack itemstack) {
      String s = I18n.translateToLocal(this.getUnlocalizedName() + ".name").trim();
      String s1 = getEntityIdFromItem(itemstack);
      if (s1 != null) {
         s = s + " " + I18n.translateToLocal("entity." + s1 + ".name");
      }

      return s;
   }

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      if (world.isRemote) {
         return EnumActionResult.SUCCESS;
      } else if (!entityhuman.canPlayerEdit(blockposition.offset(enumdirection), enumdirection, itemstack)) {
         return EnumActionResult.FAIL;
      } else {
         IBlockState iblockdata = world.getBlockState(blockposition);
         if (iblockdata.getBlock() == Blocks.MOB_SPAWNER) {
            TileEntity tileentity = world.getTileEntity(blockposition);
            if (tileentity instanceof TileEntityMobSpawner) {
               MobSpawnerBaseLogic mobspawnerabstract = ((TileEntityMobSpawner)tileentity).getSpawnerBaseLogic();
               mobspawnerabstract.setEntityName(getEntityIdFromItem(itemstack));
               tileentity.markDirty();
               world.notifyBlockUpdate(blockposition, iblockdata, iblockdata, 3);
               if (!entityhuman.capabilities.isCreativeMode) {
                  --itemstack.stackSize;
               }

               return EnumActionResult.SUCCESS;
            }
         }

         blockposition = blockposition.offset(enumdirection);
         double d0 = 0.0D;
         if (enumdirection == EnumFacing.UP && iblockdata instanceof BlockFence) {
            d0 = 0.5D;
         }

         Entity entity = spawnCreature(world, getEntityIdFromItem(itemstack), (double)blockposition.getX() + 0.5D, (double)blockposition.getY() + d0, (double)blockposition.getZ() + 0.5D);
         if (entity != null) {
            if (entity instanceof EntityLivingBase && itemstack.hasDisplayName()) {
               entity.setCustomNameTag(itemstack.getDisplayName());
            }

            applyItemEntityDataToEntity(world, entityhuman, itemstack, entity);
            if (!entityhuman.capabilities.isCreativeMode) {
               --itemstack.stackSize;
            }
         }

         return EnumActionResult.SUCCESS;
      }
   }

   public static void applyItemEntityDataToEntity(World world, @Nullable EntityPlayer entityhuman, ItemStack itemstack, @Nullable Entity entity) {
      MinecraftServer minecraftserver = world.getMinecraftServer();
      if (minecraftserver != null && entity != null) {
         NBTTagCompound nbttagcompound = itemstack.getTagCompound();
         if (nbttagcompound != null && nbttagcompound.hasKey("EntityTag", 10)) {
            if (!world.isRemote && entity.ignoreItemEntityData() && (entityhuman == null || !minecraftserver.getPlayerList().canSendCommands(entityhuman.getGameProfile()))) {
               return;
            }

            NBTTagCompound nbttagcompound1 = entity.writeToNBT(new NBTTagCompound());
            UUID uuid = entity.getUniqueID();
            nbttagcompound1.merge(nbttagcompound.getCompoundTag("EntityTag"));
            entity.setUniqueId(uuid);
            entity.readFromNBT(nbttagcompound1);
         }
      }

   }

   public ActionResult onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityhuman, EnumHand enumhand) {
      if (world.isRemote) {
         return new ActionResult(EnumActionResult.PASS, itemstack);
      } else {
         RayTraceResult movingobjectposition = this.rayTrace(world, entityhuman, true);
         if (movingobjectposition != null && movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockposition = movingobjectposition.getBlockPos();
            if (!(world.getBlockState(blockposition).getBlock() instanceof BlockLiquid)) {
               return new ActionResult(EnumActionResult.PASS, itemstack);
            } else if (world.isBlockModifiable(entityhuman, blockposition) && entityhuman.canPlayerEdit(blockposition, movingobjectposition.sideHit, itemstack)) {
               Entity entity = spawnCreature(world, getEntityIdFromItem(itemstack), (double)blockposition.getX() + 0.5D, (double)blockposition.getY() + 0.5D, (double)blockposition.getZ() + 0.5D);
               if (entity == null) {
                  return new ActionResult(EnumActionResult.PASS, itemstack);
               } else {
                  if (entity instanceof EntityLivingBase && itemstack.hasDisplayName()) {
                     entity.setCustomNameTag(itemstack.getDisplayName());
                  }

                  applyItemEntityDataToEntity(world, entityhuman, itemstack, entity);
                  if (!entityhuman.capabilities.isCreativeMode) {
                     --itemstack.stackSize;
                  }

                  entityhuman.addStat(StatList.getObjectUseStats(this));
                  return new ActionResult(EnumActionResult.SUCCESS, itemstack);
               }
            } else {
               return new ActionResult(EnumActionResult.FAIL, itemstack);
            }
         } else {
            return new ActionResult(EnumActionResult.PASS, itemstack);
         }
      }
   }

   @Nullable
   public static Entity spawnCreature(World world, @Nullable String s, double d0, double d1, double d2) {
      return spawnCreature(world, s, d0, d1, d2, SpawnReason.SPAWNER_EGG);
   }

   public static Entity spawnCreature(World world, String s, double d0, double d1, double d2, SpawnReason spawnReason) {
      if (s != null && EntityList.ENTITY_EGGS.containsKey(s)) {
         Entity entity = null;

         for(int i = 0; i < 1; ++i) {
            entity = EntityList.createEntityByIDFromName(s, world);
            if (entity instanceof EntityLivingBase) {
               EntityLiving entityinsentient = (EntityLiving)entity;
               entity.setLocationAndAngles(d0, d1, d2, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
               entityinsentient.rotationYawHead = entityinsentient.rotationYaw;
               entityinsentient.renderYawOffset = entityinsentient.rotationYaw;
               entityinsentient.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityinsentient)), (IEntityLivingData)null);
               if (!world.addEntity(entity, spawnReason)) {
                  entity = null;
               } else {
                  entityinsentient.playLivingSound();
               }
            }
         }

         return entity;
      } else {
         return null;
      }
   }

   @Nullable
   public static String getEntityIdFromItem(ItemStack itemstack) {
      NBTTagCompound nbttagcompound = itemstack.getTagCompound();
      if (nbttagcompound == null) {
         return null;
      } else if (!nbttagcompound.hasKey("EntityTag", 10)) {
         return null;
      } else {
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("EntityTag");
         return !nbttagcompound1.hasKey("id", 8) ? null : nbttagcompound1.getString("id");
      }
   }
}
