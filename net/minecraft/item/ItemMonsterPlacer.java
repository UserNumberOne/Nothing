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

   public String getItemStackDisplayName(ItemStack var1) {
      String var2 = I18n.translateToLocal(this.getUnlocalizedName() + ".name").trim();
      String var3 = getEntityIdFromItem(var1);
      if (var3 != null) {
         var2 = var2 + " " + I18n.translateToLocal("entity." + var3 + ".name");
      }

      return var2;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var3.isRemote) {
         return EnumActionResult.SUCCESS;
      } else if (!var2.canPlayerEdit(var4.offset(var6), var6, var1)) {
         return EnumActionResult.FAIL;
      } else {
         IBlockState var10 = var3.getBlockState(var4);
         if (var10.getBlock() == Blocks.MOB_SPAWNER) {
            TileEntity var11 = var3.getTileEntity(var4);
            if (var11 instanceof TileEntityMobSpawner) {
               MobSpawnerBaseLogic var12 = ((TileEntityMobSpawner)var11).getSpawnerBaseLogic();
               var12.setEntityName(getEntityIdFromItem(var1));
               var11.markDirty();
               var3.notifyBlockUpdate(var4, var10, var10, 3);
               if (!var2.capabilities.isCreativeMode) {
                  --var1.stackSize;
               }

               return EnumActionResult.SUCCESS;
            }
         }

         var4 = var4.offset(var6);
         double var13 = 0.0D;
         if (var6 == EnumFacing.UP && var10 instanceof BlockFence) {
            var13 = 0.5D;
         }

         Entity var15 = spawnCreature(var3, getEntityIdFromItem(var1), (double)var4.getX() + 0.5D, (double)var4.getY() + var13, (double)var4.getZ() + 0.5D);
         if (var15 != null) {
            if (var15 instanceof EntityLivingBase && var1.hasDisplayName()) {
               var15.setCustomNameTag(var1.getDisplayName());
            }

            applyItemEntityDataToEntity(var3, var2, var1, var15);
            if (!var2.capabilities.isCreativeMode) {
               --var1.stackSize;
            }
         }

         return EnumActionResult.SUCCESS;
      }
   }

   public static void applyItemEntityDataToEntity(World var0, @Nullable EntityPlayer var1, ItemStack var2, @Nullable Entity var3) {
      MinecraftServer var4 = var0.getMinecraftServer();
      if (var4 != null && var3 != null) {
         NBTTagCompound var5 = var2.getTagCompound();
         if (var5 != null && var5.hasKey("EntityTag", 10)) {
            if (!var0.isRemote && var3.ignoreItemEntityData() && (var1 == null || !var4.getPlayerList().canSendCommands(var1.getGameProfile()))) {
               return;
            }

            NBTTagCompound var6 = var3.writeToNBT(new NBTTagCompound());
            UUID var7 = var3.getUniqueID();
            var6.merge(var5.getCompoundTag("EntityTag"));
            var3.setUniqueId(var7);
            var3.readFromNBT(var6);
         }
      }

   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (var2.isRemote) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else {
         RayTraceResult var5 = this.rayTrace(var2, var3, true);
         if (var5 != null && var5.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos var6 = var5.getBlockPos();
            if (!(var2.getBlockState(var6).getBlock() instanceof BlockLiquid)) {
               return new ActionResult(EnumActionResult.PASS, var1);
            } else if (var2.isBlockModifiable(var3, var6) && var3.canPlayerEdit(var6, var5.sideHit, var1)) {
               Entity var7 = spawnCreature(var2, getEntityIdFromItem(var1), (double)var6.getX() + 0.5D, (double)var6.getY() + 0.5D, (double)var6.getZ() + 0.5D);
               if (var7 == null) {
                  return new ActionResult(EnumActionResult.PASS, var1);
               } else {
                  if (var7 instanceof EntityLivingBase && var1.hasDisplayName()) {
                     var7.setCustomNameTag(var1.getDisplayName());
                  }

                  applyItemEntityDataToEntity(var2, var3, var1, var7);
                  if (!var3.capabilities.isCreativeMode) {
                     --var1.stackSize;
                  }

                  var3.addStat(StatList.getObjectUseStats(this));
                  return new ActionResult(EnumActionResult.SUCCESS, var1);
               }
            } else {
               return new ActionResult(EnumActionResult.FAIL, var1);
            }
         } else {
            return new ActionResult(EnumActionResult.PASS, var1);
         }
      }
   }

   @Nullable
   public static Entity spawnCreature(World var0, @Nullable String var1, double var2, double var4, double var6) {
      return spawnCreature(var0, var1, var2, var4, var6, SpawnReason.SPAWNER_EGG);
   }

   public static Entity spawnCreature(World var0, String var1, double var2, double var4, double var6, SpawnReason var8) {
      if (var1 != null && EntityList.ENTITY_EGGS.containsKey(var1)) {
         Entity var9 = null;

         for(int var10 = 0; var10 < 1; ++var10) {
            var9 = EntityList.createEntityByIDFromName(var1, var0);
            if (var9 instanceof EntityLivingBase) {
               EntityLiving var11 = (EntityLiving)var9;
               var9.setLocationAndAngles(var2, var4, var6, MathHelper.wrapDegrees(var0.rand.nextFloat() * 360.0F), 0.0F);
               var11.rotationYawHead = var11.rotationYaw;
               var11.renderYawOffset = var11.rotationYaw;
               var11.onInitialSpawn(var0.getDifficultyForLocation(new BlockPos(var11)), (IEntityLivingData)null);
               if (!var0.addEntity(var9, var8)) {
                  var9 = null;
               } else {
                  var11.playLivingSound();
               }
            }
         }

         return var9;
      } else {
         return null;
      }
   }

   @Nullable
   public static String getEntityIdFromItem(ItemStack var0) {
      NBTTagCompound var1 = var0.getTagCompound();
      if (var1 == null) {
         return null;
      } else if (!var1.hasKey("EntityTag", 10)) {
         return null;
      } else {
         NBTTagCompound var2 = var1.getCompoundTag("EntityTag");
         return !var2.hasKey("id", 8) ? null : var2.getString("id");
      }
   }
}
