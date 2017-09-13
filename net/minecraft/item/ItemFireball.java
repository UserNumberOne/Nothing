package net.minecraft.item;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class ItemFireball extends Item {
   public ItemFireball() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var3.isRemote) {
         return EnumActionResult.SUCCESS;
      } else {
         var4 = var4.offset(var6);
         if (!var2.canPlayerEdit(var4, var6, var1)) {
            return EnumActionResult.FAIL;
         } else {
            if (var3.getBlockState(var4).getMaterial() == Material.AIR) {
               if (CraftEventFactory.callBlockIgniteEvent(var3, var4.getX(), var4.getY(), var4.getZ(), IgniteCause.FIREBALL, var2).isCancelled()) {
                  if (!var2.capabilities.isCreativeMode) {
                     --var1.stackSize;
                  }

                  return EnumActionResult.PASS;
               }

               var3.playSound((EntityPlayer)null, var4, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (itemRand.nextFloat() - itemRand.nextFloat()) * 0.2F + 1.0F);
               var3.setBlockState(var4, Blocks.FIRE.getDefaultState());
            }

            if (!var2.capabilities.isCreativeMode) {
               --var1.stackSize;
            }

            return EnumActionResult.SUCCESS;
         }
      }
   }
}
