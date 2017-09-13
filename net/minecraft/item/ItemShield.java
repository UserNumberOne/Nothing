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
            return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
   }

   public String getItemStackDisplayName(ItemStack var1) {
      if (stack.getSubCompound("BlockEntityTag", false) != null) {
         String s = "item.shield.";
         EnumDyeColor enumdyecolor = ItemBanner.getBaseColor(stack);
         s = s + enumdyecolor.getUnlocalizedName() + ".name";
         return I18n.translateToLocal(s);
      } else {
         return I18n.translateToLocal("item.shield.name");
      }
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      ItemBanner.appendHoverTextFromTileEntityTag(stack, tooltip);
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      ItemStack itemstack = new ItemStack(itemIn, 1, 0);
      subItems.add(itemstack);
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
      playerIn.setActiveHand(hand);
      return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      return repair.getItem() == Item.getItemFromBlock(Blocks.PLANKS) ? true : super.getIsRepairable(toRepair, repair);
   }
}
