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

   public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
      if (target instanceof EntityPig) {
         EntityPig entitypig = (EntityPig)target;
         if (!entitypig.getSaddled() && !entitypig.isChild()) {
            entitypig.setSaddled(true);
            entitypig.world.playSound(playerIn, entitypig.posX, entitypig.posY, entitypig.posZ, SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.NEUTRAL, 0.5F, 1.0F);
            --stack.stackSize;
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
      this.itemInteractionForEntity(stack, (EntityPlayer)null, target, EnumHand.MAIN_HAND);
      return true;
   }
}
