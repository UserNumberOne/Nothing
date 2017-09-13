package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPotion extends Item {
   public ItemPotion() {
      this.setMaxStackSize(1);
      this.setCreativeTab(CreativeTabs.BREWING);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3) {
      EntityPlayer var4 = var3 instanceof EntityPlayer ? (EntityPlayer)var3 : null;
      if (var4 == null || !var4.capabilities.isCreativeMode) {
         --var1.stackSize;
      }

      if (!var2.isRemote) {
         for(PotionEffect var6 : PotionUtils.getEffectsFromStack(var1)) {
            var3.addPotionEffect(new PotionEffect(var6));
         }
      }

      if (var4 != null) {
         var4.addStat(StatList.getObjectUseStats(this));
      }

      if (var4 == null || !var4.capabilities.isCreativeMode) {
         if (var1.stackSize <= 0) {
            return new ItemStack(Items.GLASS_BOTTLE);
         }

         if (var4 != null) {
            var4.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      return var1;
   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 32;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.DRINK;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      var3.setActiveHand(var4);
      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }

   public String getItemStackDisplayName(ItemStack var1) {
      return I18n.translateToLocal(PotionUtils.getPotionFromItem(var1).getNamePrefixed("potion.effect."));
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      PotionUtils.addPotionTooltip(var1, var3, 1.0F);
   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack var1) {
      return !PotionUtils.getEffectsFromStack(var1).isEmpty();
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(PotionType var5 : PotionType.REGISTRY) {
         var3.add(PotionUtils.addPotionToItemStack(new ItemStack(var1), var5));
      }

   }
}
