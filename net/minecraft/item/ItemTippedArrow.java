package net.minecraft.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemTippedArrow extends ItemArrow {
   public EntityArrow createArrow(World var1, ItemStack var2, EntityLivingBase var3) {
      EntityTippedArrow var4 = new EntityTippedArrow(var1, var3);
      var4.setPotionEffect(var2);
      return var4;
   }

   public String getItemStackDisplayName(ItemStack var1) {
      return I18n.translateToLocal(PotionUtils.getPotionFromItem(var1).getNamePrefixed("tipped_arrow.effect."));
   }
}
