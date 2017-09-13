package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemElytra extends Item {
   public ItemElytra() {
      this.maxStackSize = 1;
      this.setMaxDamage(432);
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
      this.addPropertyOverride(new ResourceLocation("broken"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            return ItemElytra.isBroken(var1) ? 0.0F : 1.0F;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
   }

   public static boolean isBroken(ItemStack var0) {
      return var0.getItemDamage() < var0.getMaxDamage() - 1;
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      return var2.getItem() == Items.LEATHER;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      EntityEquipmentSlot var5 = EntityLiving.getSlotForItemStack(var1);
      ItemStack var6 = var3.getItemStackFromSlot(var5);
      if (var6 == null) {
         var3.setItemStackToSlot(var5, var1.copy());
         var1.stackSize = 0;
         return new ActionResult(EnumActionResult.SUCCESS, var1);
      } else {
         return new ActionResult(EnumActionResult.FAIL, var1);
      }
   }
}
