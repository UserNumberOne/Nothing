package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCarrotOnAStick extends Item {
   public ItemCarrotOnAStick() {
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
      this.setMaxStackSize(1);
      this.setMaxDamage(25);
   }

   @SideOnly(Side.CLIENT)
   public boolean isFull3D() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldRotateAroundWhenRendering() {
      return true;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (var3.isRiding() && var3.getRidingEntity() instanceof EntityPig) {
         EntityPig var5 = (EntityPig)var3.getRidingEntity();
         if (var1.getMaxDamage() - var1.getMetadata() >= 7 && var5.boost()) {
            var1.damageItem(7, var3);
            if (var1.stackSize == 0) {
               ItemStack var6 = new ItemStack(Items.FISHING_ROD);
               var6.setTagCompound(var1.getTagCompound());
               return new ActionResult(EnumActionResult.SUCCESS, var6);
            }

            return new ActionResult(EnumActionResult.SUCCESS, var1);
         }
      }

      var3.addStat(StatList.getObjectUseStats(this));
      return new ActionResult(EnumActionResult.PASS, var1);
   }
}
