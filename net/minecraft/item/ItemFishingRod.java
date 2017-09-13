package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFishingRod extends Item {
   public ItemFishingRod() {
      this.setMaxDamage(64);
      this.setMaxStackSize(1);
      this.setCreativeTab(CreativeTabs.TOOLS);
      this.addPropertyOverride(new ResourceLocation("cast"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            return var3 == null ? 0.0F : (var3.getHeldItemMainhand() == var1 && var3 instanceof EntityPlayer && ((EntityPlayer)var3).fishEntity != null ? 1.0F : 0.0F);
         }
      });
   }

   @SideOnly(Side.CLIENT)
   public boolean isFull3D() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldRotateAroundWhenRendering() {
      return true;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (var3.fishEntity != null) {
         int var5 = var3.fishEntity.handleHookRetraction();
         var1.damageItem(var5, var3);
         var3.swingArm(var4);
      } else {
         var2.playSound((EntityPlayer)null, var3.posX, var3.posY, var3.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
         if (!var2.isRemote) {
            var2.spawnEntity(new EntityFishHook(var2, var3));
         }

         var3.swingArm(var4);
         var3.addStat(StatList.getObjectUseStats(this));
      }

      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }

   public boolean isEnchantable(ItemStack var1) {
      return super.isEnchantable(var1);
   }

   public int getItemEnchantability() {
      return 1;
   }
}
