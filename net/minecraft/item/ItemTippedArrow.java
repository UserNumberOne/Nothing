package net.minecraft.item;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTippedArrow extends ItemArrow {
   public EntityArrow createArrow(World var1, ItemStack var2, EntityLivingBase var3) {
      EntityTippedArrow entitytippedarrow = new EntityTippedArrow(worldIn, shooter);
      entitytippedarrow.setPotionEffect(stack);
      return entitytippedarrow;
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(PotionType potiontype : PotionType.REGISTRY) {
         subItems.add(PotionUtils.addPotionToItemStack(new ItemStack(itemIn), potiontype));
      }

   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      PotionUtils.addPotionTooltip(stack, tooltip, 0.125F);
   }

   public String getItemStackDisplayName(ItemStack var1) {
      return I18n.translateToLocal(PotionUtils.getPotionFromItem(stack).getNamePrefixed("tipped_arrow.effect."));
   }
}
