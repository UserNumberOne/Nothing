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
            return entityIn == null ? 0.0F : (entityIn.getHeldItemMainhand() == stack && entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).fishEntity != null ? 1.0F : 0.0F);
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
      if (playerIn.fishEntity != null) {
         int i = playerIn.fishEntity.handleHookRetraction();
         itemStackIn.damageItem(i, playerIn);
         playerIn.swingArm(hand);
      } else {
         worldIn.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
         if (!worldIn.isRemote) {
            worldIn.spawnEntity(new EntityFishHook(worldIn, playerIn));
         }

         playerIn.swingArm(hand);
         playerIn.addStat(StatList.getObjectUseStats(this));
      }

      return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
   }

   public boolean isEnchantable(ItemStack var1) {
      return super.isEnchantable(stack);
   }

   public int getItemEnchantability() {
      return 1;
   }
}
