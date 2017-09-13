package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemShield extends Item {
   public ItemShield() {
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.COMBAT);
      this.setMaxDamage(336);
      this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            return var3 != null && var3.isHandActive() && var3.getActiveItemStack() == var1 ? 1.0F : 0.0F;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      return super.onItemUse(var1, var2, var3, var4, var5, var6, var7, var8, var9);
   }

   public String getItemStackDisplayName(ItemStack var1) {
      if (var1.getSubCompound("BlockEntityTag", false) != null) {
         String var2 = "item.shield.";
         EnumDyeColor var3 = ItemBanner.getBaseColor(var1);
         var2 = var2 + var3.getUnlocalizedName() + ".name";
         return I18n.translateToLocal(var2);
      } else {
         return I18n.translateToLocal("item.shield.name");
      }
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      ItemBanner.appendHoverTextFromTileEntityTag(var1, var3);
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      ItemStack var4 = new ItemStack(var1, 1, 0);
      var3.add(var4);
   }

   @SideOnly(Side.CLIENT)
   public CreativeTabs getCreativeTab() {
      return CreativeTabs.COMBAT;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.BLOCK;
   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 72000;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      var3.setActiveHand(var4);
      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      return var2.getItem() == Item.getItemFromBlock(Blocks.PLANKS) ? true : super.getIsRepairable(var1, var2);
   }
}
