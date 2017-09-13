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
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidContainerRegistryWrapper;

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
      ActionResult var7 = ForgeEventFactory.onBucketUse(var3, var2, var1, var6);
      if (var7 != null) {
         return var7;
      } else if (var6 == null) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else if (var6.typeOfHit != RayTraceResult.Type.BLOCK) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else {
         BlockPos var8 = var6.getBlockPos();
         if (!var2.isBlockModifiable(var3, var8)) {
            return new ActionResult(EnumActionResult.FAIL, var1);
         } else if (var5) {
            if (!var3.canPlayerEdit(var8.offset(var6.sideHit), var6.sideHit, var1)) {
               return new ActionResult(EnumActionResult.FAIL, var1);
            } else {
               IBlockState var11 = var2.getBlockState(var8);
               Material var12 = var11.getMaterial();
               if (var12 == Material.WATER && ((Integer)var11.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                  var2.setBlockState(var8, Blocks.AIR.getDefaultState(), 11);
                  var3.addStat(StatList.getObjectUseStats(this));
                  var3.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                  return new ActionResult(EnumActionResult.SUCCESS, this.fillBucket(var1, var3, Items.WATER_BUCKET));
               } else if (var12 == Material.LAVA && ((Integer)var11.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                  var3.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                  var2.setBlockState(var8, Blocks.AIR.getDefaultState(), 11);
                  var3.addStat(StatList.getObjectUseStats(this));
                  return new ActionResult(EnumActionResult.SUCCESS, this.fillBucket(var1, var3, Items.LAVA_BUCKET));
               } else {
                  return new ActionResult(EnumActionResult.FAIL, var1);
               }
            }
         } else {
            boolean var9 = var2.getBlockState(var8).getBlock().isReplaceable(var2, var8);
            BlockPos var10 = var9 && var6.sideHit == EnumFacing.UP ? var8 : var8.offset(var6.sideHit);
            if (!var3.canPlayerEdit(var10, var6.sideHit, var1)) {
               return new ActionResult(EnumActionResult.FAIL, var1);
            } else if (this.tryPlaceContainedLiquid(var3, var2, var10)) {
               var3.addStat(StatList.getObjectUseStats(this));
               return !var3.capabilities.isCreativeMode ? new ActionResult(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(EnumActionResult.SUCCESS, var1);
            } else {
               return new ActionResult(EnumActionResult.FAIL, var1);
            }
         }
      }
   }

   private ItemStack fillBucket(ItemStack var1, EntityPlayer var2, Item var3) {
      if (var2.capabilities.isCreativeMode) {
         return var1;
      } else if (--var1.stackSize <= 0) {
         return new ItemStack(var3);
      } else {
         if (!var2.inventory.addItemStackToInventory(new ItemStack(var3))) {
            var2.dropItem(new ItemStack(var3), false);
         }

         return var1;
      }
   }

   public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer var1, World var2, BlockPos var3) {
      if (this.containedBlock == Blocks.AIR) {
         return false;
      } else {
         IBlockState var4 = var2.getBlockState(var3);
         Material var5 = var4.getMaterial();
         boolean var6 = !var5.isSolid();
         boolean var7 = var4.getBlock().isReplaceable(var2, var3);
         if (!var2.isAirBlock(var3) && !var6 && !var7) {
            return false;
         } else {
            if (var2.provider.doesWaterVaporize() && this.containedBlock == Blocks.FLOWING_WATER) {
               int var12 = var3.getX();
               int var9 = var3.getY();
               int var10 = var3.getZ();
               var2.playSound(var1, var3, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (var2.rand.nextFloat() - var2.rand.nextFloat()) * 0.8F);

               for(int var11 = 0; var11 < 8; ++var11) {
                  var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)var12 + Math.random(), (double)var9 + Math.random(), (double)var10 + Math.random(), 0.0D, 0.0D, 0.0D);
               }
            } else {
               if (!var2.isRemote && (var6 || var7) && !var5.isLiquid()) {
                  var2.destroyBlock(var3, true);
               }

               SoundEvent var8 = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
               var2.playSound(var1, var3, var8, SoundCategory.BLOCKS, 1.0F, 1.0F);
               var2.setBlockState(var3, this.containedBlock.getDefaultState(), 11);
            }

            return true;
         }
      }
   }

   public ICapabilityProvider initCapabilities(ItemStack var1, NBTTagCompound var2) {
      if (this.getClass() == ItemBucket.class) {
         return (ICapabilityProvider)(FluidRegistry.isUniversalBucketEnabled() ? new FluidBucketWrapper(var1) : new FluidContainerRegistryWrapper(var1));
      } else {
         return super.initCapabilities(var1, var2);
      }
   }
}
