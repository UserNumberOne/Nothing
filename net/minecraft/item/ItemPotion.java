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
      EntityPlayer entityplayer = entityLiving instanceof EntityPlayer ? (EntityPlayer)entityLiving : null;
      if (entityplayer == null || !entityplayer.capabilities.isCreativeMode) {
         --stack.stackSize;
      }

      if (!worldIn.isRemote) {
         for(PotionEffect potioneffect : PotionUtils.getEffectsFromStack(stack)) {
            entityLiving.addPotionEffect(new PotionEffect(potioneffect));
         }
      }

      if (entityplayer != null) {
         entityplayer.addStat(StatList.getObjectUseStats(this));
      }

      if (entityplayer == null || !entityplayer.capabilities.isCreativeMode) {
         if (stack.stackSize <= 0) {
            return new ItemStack(Items.GLASS_BOTTLE);
         }

         if (entityplayer != null) {
            entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      return stack;
   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 32;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.DRINK;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      playerIn.setActiveHand(hand);
      return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
   }

   public String getItemStackDisplayName(ItemStack var1) {
      return I18n.translateToLocal(PotionUtils.getPotionFromItem(stack).getNamePrefixed("potion.effect."));
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      PotionUtils.addPotionTooltip(stack, tooltip, 1.0F);
   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack var1) {
      return !PotionUtils.getEffectsFromStack(stack).isEmpty();
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(PotionType potiontype : PotionType.REGISTRY) {
         subItems.add(PotionUtils.addPotionToItemStack(new ItemStack(itemIn), potiontype));
      }

   }
}
