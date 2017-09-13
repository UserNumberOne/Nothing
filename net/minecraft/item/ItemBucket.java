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

   public ItemBucket(Block block) {
      this.maxStackSize = 1;
      this.containedBlock = block;
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public ActionResult onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityhuman, EnumHand enumhand) {
      boolean flag = this.containedBlock == Blocks.AIR;
      RayTraceResult movingobjectposition = this.rayTrace(world, entityhuman, flag);
      if (movingobjectposition == null) {
         return new ActionResult(EnumActionResult.PASS, itemstack);
      } else if (movingobjectposition.typeOfHit != RayTraceResult.Type.BLOCK) {
         return new ActionResult(EnumActionResult.PASS, itemstack);
      } else {
         BlockPos blockposition = movingobjectposition.getBlockPos();
         if (!world.isBlockModifiable(entityhuman, blockposition)) {
            return new ActionResult(EnumActionResult.FAIL, itemstack);
         } else if (flag) {
            if (!entityhuman.canPlayerEdit(blockposition.offset(movingobjectposition.sideHit), movingobjectposition.sideHit, itemstack)) {
               return new ActionResult(EnumActionResult.FAIL, itemstack);
            } else {
               IBlockState iblockdata = world.getBlockState(blockposition);
               Material material = iblockdata.getMaterial();
               if (material == Material.WATER && ((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                  PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), (EnumFacing)null, itemstack, Items.WATER_BUCKET);
                  if (event.isCancelled()) {
                     return new ActionResult(EnumActionResult.FAIL, itemstack);
                  } else {
                     world.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 11);
                     entityhuman.addStat(StatList.getObjectUseStats(this));
                     entityhuman.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                     return new ActionResult(EnumActionResult.SUCCESS, this.a(itemstack, entityhuman, Items.WATER_BUCKET, event.getItemStack()));
                  }
               } else if (material == Material.LAVA && ((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                  PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), (EnumFacing)null, itemstack, Items.LAVA_BUCKET);
                  if (event.isCancelled()) {
                     return new ActionResult(EnumActionResult.FAIL, itemstack);
                  } else {
                     entityhuman.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                     world.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 11);
                     entityhuman.addStat(StatList.getObjectUseStats(this));
                     return new ActionResult(EnumActionResult.SUCCESS, this.a(itemstack, entityhuman, Items.LAVA_BUCKET, event.getItemStack()));
                  }
               } else {
                  return new ActionResult(EnumActionResult.FAIL, itemstack);
               }
            }
         } else {
            boolean flag1 = world.getBlockState(blockposition).getBlock().isReplaceable(world, blockposition);
            BlockPos blockposition1 = flag1 && movingobjectposition.sideHit == EnumFacing.UP ? blockposition : blockposition.offset(movingobjectposition.sideHit);
            if (!entityhuman.canPlayerEdit(blockposition1, movingobjectposition.sideHit, itemstack)) {
               return new ActionResult(EnumActionResult.FAIL, itemstack);
            } else if (this.a(entityhuman, world, blockposition1, movingobjectposition.sideHit, blockposition, itemstack)) {
               entityhuman.addStat(StatList.getObjectUseStats(this));
               return !entityhuman.capabilities.isCreativeMode ? new ActionResult(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(EnumActionResult.SUCCESS, itemstack);
            } else {
               return new ActionResult(EnumActionResult.FAIL, itemstack);
            }
         }
      }
   }

   private ItemStack a(ItemStack itemstack, EntityPlayer entityhuman, Item item, org.bukkit.inventory.ItemStack result) {
      if (entityhuman.capabilities.isCreativeMode) {
         return itemstack;
      } else if (--itemstack.stackSize <= 0) {
         return CraftItemStack.asNMSCopy(result);
      } else {
         if (!entityhuman.inventory.addItemStackToInventory(CraftItemStack.asNMSCopy(result))) {
            entityhuman.dropItem(CraftItemStack.asNMSCopy(result), false);
         }

         return itemstack;
      }
   }

   public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer entityhuman, World world, BlockPos blockposition) {
      return this.a(entityhuman, world, blockposition, (EnumFacing)null, blockposition, (ItemStack)null);
   }

   public boolean a(EntityPlayer entityhuman, World world, BlockPos blockposition, EnumFacing enumdirection, BlockPos clicked, ItemStack itemstack) {
      if (this.containedBlock == Blocks.AIR) {
         return false;
      } else {
         IBlockState iblockdata = world.getBlockState(blockposition);
         Material material = iblockdata.getMaterial();
         boolean flag = !material.isSolid();
         boolean flag1 = iblockdata.getBlock().isReplaceable(world, blockposition);
         if (!world.isAirBlock(blockposition) && !flag && !flag1) {
            return false;
         } else {
            if (entityhuman != null) {
               PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman, clicked.getX(), clicked.getY(), clicked.getZ(), enumdirection, itemstack);
               if (event.isCancelled()) {
                  return false;
               }
            }

            if (world.provider.doesWaterVaporize() && this.containedBlock == Blocks.FLOWING_WATER) {
               int i = blockposition.getX();
               int j = blockposition.getY();
               int k = blockposition.getZ();
               world.playSound(entityhuman, blockposition, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

               for(int l = 0; l < 8; ++l) {
                  world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
               }
            } else {
               if (!world.isRemote && (flag || flag1) && !material.isLiquid()) {
                  world.destroyBlock(blockposition, true);
               }

               SoundEvent soundeffect = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
               world.playSound(entityhuman, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
               world.setBlockState(blockposition, this.containedBlock.getDefaultState(), 11);
            }

            return true;
         }
      }
   }
}
