package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class ItemBucket extends Item {
   private final Block containedBlock;

   public ItemBucket(Block var1) {
      this.maxStackSize = 1;
      this.containedBlock = var1;
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      boolean var5 = this.containedBlock == Blocks.AIR;
      RayTraceResult var6 = this.rayTrace(var2, var3, var5);
      if (var6 == null) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else if (var6.typeOfHit != RayTraceResult.Type.BLOCK) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else {
         BlockPos var7 = var6.getBlockPos();
         if (!var2.isBlockModifiable(var3, var7)) {
            return new ActionResult(EnumActionResult.FAIL, var1);
         } else if (var5) {
            if (!var3.canPlayerEdit(var7.offset(var6.sideHit), var6.sideHit, var1)) {
               return new ActionResult(EnumActionResult.FAIL, var1);
            } else {
               IBlockState var11 = var2.getBlockState(var7);
               Material var12 = var11.getMaterial();
               if (var12 == Material.WATER && ((Integer)var11.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                  PlayerBucketFillEvent var13 = CraftEventFactory.callPlayerBucketFillEvent(var3, var7.getX(), var7.getY(), var7.getZ(), (EnumFacing)null, var1, Items.WATER_BUCKET);
                  if (var13.isCancelled()) {
                     return new ActionResult(EnumActionResult.FAIL, var1);
                  } else {
                     var2.setBlockState(var7, Blocks.AIR.getDefaultState(), 11);
                     var3.addStat(StatList.getObjectUseStats(this));
                     var3.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                     return new ActionResult(EnumActionResult.SUCCESS, this.a(var1, var3, Items.WATER_BUCKET, var13.getItemStack()));
                  }
               } else if (var12 == Material.LAVA && ((Integer)var11.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                  PlayerBucketFillEvent var10 = CraftEventFactory.callPlayerBucketFillEvent(var3, var7.getX(), var7.getY(), var7.getZ(), (EnumFacing)null, var1, Items.LAVA_BUCKET);
                  if (var10.isCancelled()) {
                     return new ActionResult(EnumActionResult.FAIL, var1);
                  } else {
                     var3.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                     var2.setBlockState(var7, Blocks.AIR.getDefaultState(), 11);
                     var3.addStat(StatList.getObjectUseStats(this));
                     return new ActionResult(EnumActionResult.SUCCESS, this.a(var1, var3, Items.LAVA_BUCKET, var10.getItemStack()));
                  }
               } else {
                  return new ActionResult(EnumActionResult.FAIL, var1);
               }
            }
         } else {
            boolean var8 = var2.getBlockState(var7).getBlock().isReplaceable(var2, var7);
            BlockPos var9 = var8 && var6.sideHit == EnumFacing.UP ? var7 : var7.offset(var6.sideHit);
            if (!var3.canPlayerEdit(var9, var6.sideHit, var1)) {
               return new ActionResult(EnumActionResult.FAIL, var1);
            } else if (this.a(var3, var2, var9, var6.sideHit, var7, var1)) {
               var3.addStat(StatList.getObjectUseStats(this));
               return !var3.capabilities.isCreativeMode ? new ActionResult(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(EnumActionResult.SUCCESS, var1);
            } else {
               return new ActionResult(EnumActionResult.FAIL, var1);
            }
         }
      }
   }

   private ItemStack a(ItemStack var1, EntityPlayer var2, Item var3, org.bukkit.inventory.ItemStack var4) {
      if (var2.capabilities.isCreativeMode) {
         return var1;
      } else if (--var1.stackSize <= 0) {
         return CraftItemStack.asNMSCopy(var4);
      } else {
         if (!var2.inventory.addItemStackToInventory(CraftItemStack.asNMSCopy(var4))) {
            var2.dropItem(CraftItemStack.asNMSCopy(var4), false);
         }

         return var1;
      }
   }

   public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer var1, World var2, BlockPos var3) {
      return this.a(var1, var2, var3, (EnumFacing)null, var3, (ItemStack)null);
   }

   public boolean a(EntityPlayer var1, World var2, BlockPos var3, EnumFacing var4, BlockPos var5, ItemStack var6) {
      if (this.containedBlock == Blocks.AIR) {
         return false;
      } else {
         IBlockState var7 = var2.getBlockState(var3);
         Material var8 = var7.getMaterial();
         boolean var9 = !var8.isSolid();
         boolean var10 = var7.getBlock().isReplaceable(var2, var3);
         if (!var2.isAirBlock(var3) && !var9 && !var10) {
            return false;
         } else {
            if (var1 != null) {
               PlayerBucketEmptyEvent var11 = CraftEventFactory.callPlayerBucketEmptyEvent(var1, var5.getX(), var5.getY(), var5.getZ(), var4, var6);
               if (var11.isCancelled()) {
                  return false;
               }
            }

            if (var2.provider.doesWaterVaporize() && this.containedBlock == Blocks.FLOWING_WATER) {
               int var16 = var3.getX();
               int var12 = var3.getY();
               int var13 = var3.getZ();
               var2.playSound(var1, var3, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (var2.rand.nextFloat() - var2.rand.nextFloat()) * 0.8F);

               for(int var14 = 0; var14 < 8; ++var14) {
                  var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)var16 + Math.random(), (double)var12 + Math.random(), (double)var13 + Math.random(), 0.0D, 0.0D, 0.0D);
               }
            } else {
               if (!var2.isRemote && (var9 || var10) && !var8.isLiquid()) {
                  var2.destroyBlock(var3, true);
               }

               SoundEvent var15 = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
               var2.playSound(var1, var3, var15, SoundCategory.BLOCKS, 1.0F, 1.0F);
               var2.setBlockState(var3, this.containedBlock.getDefaultState(), 11);
            }

            return true;
         }
      }
   }
}
