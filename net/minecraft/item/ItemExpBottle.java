package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemExpBottle extends Item {
   public ItemExpBottle() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack var1) {
      return true;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (!var3.capabilities.isCreativeMode) {
         --var1.stackSize;
      }

      var2.playSound((EntityPlayer)null, var3.posX, var3.posY, var3.posZ, SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
      if (!var2.isRemote) {
         EntityExpBottle var5 = new EntityExpBottle(var2, var3);
         var5.setHeadingFromThrower(var3, var3.rotationPitch, var3.rotationYaw, -20.0F, 0.7F, 1.0F);
         var2.spawnEntity(var5);
      }

      var3.addStat(StatList.getObjectUseStats(this));
      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }
}
