package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemSnowball extends Item {
   public ItemSnowball() {
      this.maxStackSize = 16;
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (!var3.capabilities.isCreativeMode) {
         --var1.stackSize;
      }

      var2.playSound((EntityPlayer)null, var3.posX, var3.posY, var3.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
      if (!var2.isRemote) {
         EntitySnowball var5 = new EntitySnowball(var2, var3);
         var5.setHeadingFromThrower(var3, var3.rotationPitch, var3.rotationYaw, 0.0F, 1.5F, 1.0F);
         var2.spawnEntity(var5);
      }

      var3.addStat(StatList.getObjectUseStats(this));
      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }
}
