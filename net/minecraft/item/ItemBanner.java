package net.minecraft.item;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemBanner extends ItemBlock {
   public ItemBanner() {
      super(Blocks.STANDING_BANNER);
      this.maxStackSize = 16;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      boolean var11 = var10.getBlock().isReplaceable(var3, var4);
      if (var6 != EnumFacing.DOWN && (var10.getMaterial().isSolid() || var11) && (!var11 || var6 == EnumFacing.UP)) {
         var4 = var4.offset(var6);
         if (var2.canPlayerEdit(var4, var6, var1) && Blocks.STANDING_BANNER.canPlaceBlockAt(var3, var4)) {
            if (var3.isRemote) {
               return EnumActionResult.SUCCESS;
            } else {
               var4 = var11 ? var4.down() : var4;
               if (var6 == EnumFacing.UP) {
                  int var12 = MathHelper.floor((double)((var2.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                  var3.setBlockState(var4, Blocks.STANDING_BANNER.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(var12)), 3);
               } else {
                  var3.setBlockState(var4, Blocks.WALL_BANNER.getDefaultState().withProperty(BlockWallSign.FACING, var6), 3);
               }

               --var1.stackSize;
               TileEntity var15 = var3.getTileEntity(var4);
               if (var15 instanceof TileEntityBanner) {
                  ((TileEntityBanner)var15).setItemValues(var1);
               }

               return EnumActionResult.SUCCESS;
            }
         } else {
            return EnumActionResult.FAIL;
         }
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public String getItemStackDisplayName(ItemStack var1) {
      String var2 = "item.banner.";
      EnumDyeColor var3 = getBaseColor(var1);
      var2 = var2 + var3.getUnlocalizedName() + ".name";
      return I18n.translateToLocal(var2);
   }

   public static EnumDyeColor getBaseColor(ItemStack var0) {
      NBTTagCompound var1 = var0.getSubCompound("BlockEntityTag", false);
      EnumDyeColor var2;
      if (var1 != null && var1.hasKey("Base")) {
         var2 = EnumDyeColor.byDyeDamage(var1.getInteger("Base"));
      } else {
         var2 = EnumDyeColor.byDyeDamage(var0.getMetadata());
      }

      return var2;
   }
}
