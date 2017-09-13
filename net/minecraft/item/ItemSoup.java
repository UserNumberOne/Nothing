package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.world.World;

public class ItemSoup extends ItemFood {
   public ItemSoup(int var1) {
      super(healAmount, false);
      this.setMaxStackSize(1);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3) {
      super.onItemUseFinish(stack, worldIn, entityLiving);
      return new ItemStack(Items.BOWL);
   }
}
