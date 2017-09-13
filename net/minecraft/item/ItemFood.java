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
      this.healAmount = amount;
      this.isWolfsFavoriteMeat = isWolfFood;
      this.saturationModifier = saturation;
      this.setCreativeTab(CreativeTabs.FOOD);
   }

   public ItemFood(int var1, boolean var2) {
      this(amount, 0.6F, isWolfFood);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3) {
      --stack.stackSize;
      if (entityLiving instanceof EntityPlayer) {
         EntityPlayer entityplayer = (EntityPlayer)entityLiving;
         entityplayer.getFoodStats().addStats(this, stack);
         worldIn.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
         this.onFoodEaten(stack, worldIn, entityplayer);
         entityplayer.addStat(StatList.getObjectUseStats(this));
      }

      return stack;
   }

   protected void onFoodEaten(ItemStack var1, World var2, EntityPlayer var3) {
      if (!worldIn.isRemote && this.potionId != null && worldIn.rand.nextFloat() < this.potionEffectProbability) {
         player.addPotionEffect(new PotionEffect(this.potionId));
      }

   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 32;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.EAT;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (playerIn.canEat(this.alwaysEdible)) {
         playerIn.setActiveHand(hand);
         return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
      } else {
         return new ActionResult(EnumActionResult.FAIL, itemStackIn);
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
      this.potionId = p_185070_1_;
      this.potionEffectProbability = p_185070_2_;
      return this;
   }

   public ItemFood setAlwaysEdible() {
      this.alwaysEdible = true;
      return this;
   }
}
