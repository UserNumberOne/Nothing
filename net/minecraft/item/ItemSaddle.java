package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;

public class ItemSaddle extends Item {
   public ItemSaddle() {
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      if (var3 instanceof EntityPig) {
         EntityPig var5 = (EntityPig)var3;
         if (!var5.getSaddled() && !var5.isChild()) {
            var5.setSaddled(true);
            var5.world.playSound(var2, var5.posX, var5.posY, var5.posZ, SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.NEUTRAL, 0.5F, 1.0F);
            --var1.stackSize;
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3) {
      this.itemInteractionForEntity(var1, (EntityPlayer)null, var2, EnumHand.MAIN_HAND);
      return true;
   }
}
