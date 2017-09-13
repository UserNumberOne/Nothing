package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemBucketMilk extends Item {
   public ItemBucketMilk() {
      this.setMaxStackSize(1);
      this.setCreativeTab(CreativeTabs.MISC);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3) {
      if (var3 instanceof EntityPlayer && !((EntityPlayer)var3).capabilities.isCreativeMode) {
         --var1.stackSize;
      }

      if (!var2.isRemote) {
         var3.clearActivePotions();
      }

      if (var3 instanceof EntityPlayer) {
         ((EntityPlayer)var3).addStat(StatList.getObjectUseStats(this));
      }

      return var1.stackSize <= 0 ? new ItemStack(Items.BUCKET) : var1;
   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 32;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.DRINK;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      var3.setActiveHand(var4);
      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }
}
