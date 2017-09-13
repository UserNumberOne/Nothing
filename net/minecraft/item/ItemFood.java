package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemFood extends Item {
   public final int itemUseDuration;
   private final int healAmount;
   private final float saturationModifier;
   private final boolean isWolfsFavoriteMeat;
   private boolean alwaysEdible;
   private PotionEffect potionId;
   private float potionEffectProbability;

   public ItemFood(int var1, float var2, boolean var3) {
      this.itemUseDuration = 32;
      this.healAmount = var1;
      this.isWolfsFavoriteMeat = var3;
      this.saturationModifier = var2;
      this.setCreativeTab(CreativeTabs.FOOD);
   }

   public ItemFood(int var1, boolean var2) {
      this(var1, 0.6F, var2);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3) {
      --var1.stackSize;
      if (var3 instanceof EntityPlayer) {
         EntityPlayer var4 = (EntityPlayer)var3;
         var4.getFoodStats().addStats(this, var1);
         var2.playSound((EntityPlayer)null, var4.posX, var4.posY, var4.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, var2.rand.nextFloat() * 0.1F + 0.9F);
         this.onFoodEaten(var1, var2, var4);
         var4.addStat(StatList.getObjectUseStats(this));
      }

      return var1;
   }

   protected void onFoodEaten(ItemStack var1, World var2, EntityPlayer var3) {
      if (!var2.isRemote && this.potionId != null && var2.rand.nextFloat() < this.potionEffectProbability) {
         var3.addPotionEffect(new PotionEffect(this.potionId));
      }

   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 32;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.EAT;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (var3.canEat(this.alwaysEdible)) {
         var3.setActiveHand(var4);
         return new ActionResult(EnumActionResult.SUCCESS, var1);
      } else {
         return new ActionResult(EnumActionResult.FAIL, var1);
      }
   }

   public int getHealAmount(ItemStack var1) {
      return this.healAmount;
   }

   public float getSaturationModifier(ItemStack var1) {
      return this.saturationModifier;
   }

   public boolean isWolfsFavoriteMeat() {
      return this.isWolfsFavoriteMeat;
   }

   public ItemFood setPotionEffect(PotionEffect var1, float var2) {
      this.potionId = var1;
      this.potionEffectProbability = var2;
      return this;
   }

   public ItemFood setAlwaysEdible() {
      this.alwaysEdible = true;
      return this;
   }
}
