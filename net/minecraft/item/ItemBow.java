package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBow extends Item {
   public ItemBow() {
      this.maxStackSize = 1;
      this.setMaxDamage(384);
      this.setCreativeTab(CreativeTabs.COMBAT);
      this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            if (var3 == null) {
               return 0.0F;
            } else {
               ItemStack var4 = var3.getActiveItemStack();
               return var4 != null && var4.getItem() == Items.BOW ? (float)(var1.getMaxItemUseDuration() - var3.getItemInUseCount()) / 20.0F : 0.0F;
            }
         }
      });
      this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            return var3 != null && var3.isHandActive() && var3.getActiveItemStack() == var1 ? 1.0F : 0.0F;
         }
      });
   }

   private ItemStack findAmmo(EntityPlayer var1) {
      if (this.isArrow(var1.getHeldItem(EnumHand.OFF_HAND))) {
         return var1.getHeldItem(EnumHand.OFF_HAND);
      } else if (this.isArrow(var1.getHeldItem(EnumHand.MAIN_HAND))) {
         return var1.getHeldItem(EnumHand.MAIN_HAND);
      } else {
         for(int var2 = 0; var2 < var1.inventory.getSizeInventory(); ++var2) {
            ItemStack var3 = var1.inventory.getStackInSlot(var2);
            if (this.isArrow(var3)) {
               return var3;
            }
         }

         return null;
      }
   }

   protected boolean isArrow(@Nullable ItemStack var1) {
      return var1 != null && var1.getItem() instanceof ItemArrow;
   }

   public void onPlayerStoppedUsing(ItemStack var1, World var2, EntityLivingBase var3, int var4) {
      if (var3 instanceof EntityPlayer) {
         EntityPlayer var5 = (EntityPlayer)var3;
         boolean var6 = var5.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, var1) > 0;
         ItemStack var7 = this.findAmmo(var5);
         int var8 = this.getMaxItemUseDuration(var1) - var4;
         var8 = ForgeEventFactory.onArrowLoose(var1, var2, (EntityPlayer)var3, var8, var7 != null || var6);
         if (var8 < 0) {
            return;
         }

         if (var7 != null || var6) {
            if (var7 == null) {
               var7 = new ItemStack(Items.ARROW);
            }

            float var9 = getArrowVelocity(var8);
            if ((double)var9 >= 0.1D) {
               boolean var10 = var5.capabilities.isCreativeMode || var7.getItem() instanceof ItemArrow && ((ItemArrow)var7.getItem()).isInfinite(var7, var1, var5);
               if (!var2.isRemote) {
                  ItemArrow var11 = (ItemArrow)(var7.getItem() instanceof ItemArrow ? var7.getItem() : Items.ARROW);
                  EntityArrow var12 = var11.createArrow(var2, var7, var5);
                  var12.setAim(var5, var5.rotationPitch, var5.rotationYaw, 0.0F, var9 * 3.0F, 1.0F);
                  if (var9 == 1.0F) {
                     var12.setIsCritical(true);
                  }

                  int var13 = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, var1);
                  if (var13 > 0) {
                     var12.setDamage(var12.getDamage() + (double)var13 * 0.5D + 0.5D);
                  }

                  int var14 = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, var1);
                  if (var14 > 0) {
                     var12.setKnockbackStrength(var14);
                  }

                  if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, var1) > 0) {
                     var12.setFire(100);
                  }

                  var1.damageItem(1, var5);
                  if (var10) {
                     var12.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                  }

                  var2.spawnEntity(var12);
               }

               var2.playSound((EntityPlayer)null, var5.posX, var5.posY, var5.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + var9 * 0.5F);
               if (!var10) {
                  --var7.stackSize;
                  if (var7.stackSize == 0) {
                     var5.inventory.deleteStack(var7);
                  }
               }

               var5.addStat(StatList.getObjectUseStats(this));
            }
         }
      }

   }

   public static float getArrowVelocity(int var0) {
      float var1 = (float)var0 / 20.0F;
      var1 = (var1 * var1 + var1 * 2.0F) / 3.0F;
      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      return var1;
   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 72000;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.BOW;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      boolean var5 = this.findAmmo(var3) != null;
      ActionResult var6 = ForgeEventFactory.onArrowNock(var1, var2, var3, var4, var5);
      if (var6 != null) {
         return var6;
      } else if (!var3.capabilities.isCreativeMode && !var5) {
         return !var5 ? new ActionResult(EnumActionResult.FAIL, var1) : new ActionResult(EnumActionResult.PASS, var1);
      } else {
         var3.setActiveHand(var4);
         return new ActionResult(EnumActionResult.SUCCESS, var1);
      }
   }

   public int getItemEnchantability() {
      return 1;
   }
}
