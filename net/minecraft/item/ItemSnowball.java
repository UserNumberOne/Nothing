package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

   public ActionResult onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityhuman, EnumHand enumhand) {
      if (!world.isRemote) {
         EntitySnowball entitysnowball = new EntitySnowball(world, entityhuman);
         entitysnowball.setHeadingFromThrower(entityhuman, entityhuman.rotationPitch, entityhuman.rotationYaw, 0.0F, 1.5F, 1.0F);
         if (world.spawnEntity(entitysnowball)) {
            if (!entityhuman.capabilities.isCreativeMode) {
               --itemstack.stackSize;
            }

            world.playSound((EntityPlayer)null, entityhuman.posX, entityhuman.posY, entityhuman.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
         } else if (entityhuman instanceof EntityPlayerMP) {
            ((EntityPlayerMP)entityhuman).getBukkitEntity().updateInventory();
         }
      }

      entityhuman.addStat(StatList.getObjectUseStats(this));
      return new ActionResult(EnumActionResult.SUCCESS, itemstack);
   }
}
