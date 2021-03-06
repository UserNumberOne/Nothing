package net.minecraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.world.World;

public class ItemAppleGold extends ItemFood {
   public ItemAppleGold(int var1, float var2, boolean var3) {
      super(var1, var2, var3);
      this.setHasSubtypes(true);
   }

   public EnumRarity getRarity(ItemStack var1) {
      return var1.getMetadata() == 0 ? EnumRarity.RARE : EnumRarity.EPIC;
   }

   protected void onFoodEaten(ItemStack var1, World var2, EntityPlayer var3) {
      if (!var2.isRemote) {
         if (var1.getMetadata() > 0) {
            var3.addStat(AchievementList.OVERPOWERED);
            var3.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 400, 1));
            var3.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 6000, 0));
            var3.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 6000, 0));
            var3.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 3));
         } else {
            var3.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 1));
            var3.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 0));
         }
      }

   }
}
