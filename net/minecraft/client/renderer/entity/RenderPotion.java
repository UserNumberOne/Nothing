package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPotion extends RenderSnowball {
   public RenderPotion(RenderManager var1, RenderItem var2) {
      super(var1, Items.POTIONITEM, var2);
   }

   public ItemStack getStackToRender(EntityPotion var1) {
      return var1.getPotion();
   }
}
