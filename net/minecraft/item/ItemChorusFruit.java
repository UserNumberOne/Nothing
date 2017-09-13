package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemChorusFruit extends ItemFood {
   public ItemChorusFruit(int var1, float var2) {
      super(var1, var2, false);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3) {
      ItemStack var4 = super.onItemUseFinish(var1, var2, var3);
      if (!var2.isRemote) {
         double var5 = var3.posX;
         double var7 = var3.posY;
         double var9 = var3.posZ;

         for(int var11 = 0; var11 < 16; ++var11) {
            double var12 = var3.posX + (var3.getRNG().nextDouble() - 0.5D) * 16.0D;
            double var14 = MathHelper.clamp(var3.posY + (double)(var3.getRNG().nextInt(16) - 8), 0.0D, (double)(var2.getActualHeight() - 1));
            double var16 = var3.posZ + (var3.getRNG().nextDouble() - 0.5D) * 16.0D;
            if (var3.isRiding()) {
               var3.dismountRidingEntity();
            }

            if (var3.attemptTeleport(var12, var14, var16)) {
               var2.playSound((EntityPlayer)null, var5, var7, var9, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
               var3.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
               break;
            }
         }

         if (var3 instanceof EntityPlayer) {
            ((EntityPlayer)var3).getCooldownTracker().setCooldown(this, 20);
         }
      }

      return var4;
   }
}
