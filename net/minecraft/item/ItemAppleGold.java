package net.minecraft.item;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemAppleGold extends ItemFood {
   public ItemAppleGold(int var1, float var2, boolean var3) {
      super(amount, saturation, isWolfFood);
      this.setHasSubtypes(true);
   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack var1) {
      return stack.getMetadata() > 0;
   }

   public EnumRarity getRarity(ItemStack var1) {
      return stack.getMetadata() == 0 ? EnumRarity.RARE : EnumRarity.EPIC;
   }

   protected void onFoodEaten(ItemStack var1, World var2, EntityPlayer var3) {
      if (!worldIn.isRemote) {
         if (stack.getMetadata() > 0) {
            player.addStat(AchievementList.OVERPOWERED);
            player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 400, 1));
            player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 6000, 0));
            player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 6000, 0));
            player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 3));
         } else {
            player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 1));
            player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 0));
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      subItems.add(new ItemStack(itemIn));
      subItems.add(new ItemStack(itemIn, 1, 1));
   }
}
