package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemPropertyGetter {
   @SideOnly(Side.CLIENT)
   float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3);
}
