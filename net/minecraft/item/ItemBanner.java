package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBanner extends ItemBlock {
   public ItemBanner() {
      super(Blocks.STANDING_BANNER);
      this.maxStackSize = 16;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      boolean flag = iblockstate.getBlock().isReplaceable(worldIn, pos);
      if (facing != EnumFacing.DOWN && (iblockstate.getMaterial().isSolid() || flag) && (!flag || facing == EnumFacing.UP)) {
         pos = pos.offset(facing);
         if (playerIn.canPlayerEdit(pos, facing, stack) && Blocks.STANDING_BANNER.canPlaceBlockAt(worldIn, pos)) {
            if (worldIn.isRemote) {
               return EnumActionResult.SUCCESS;
            } else {
               pos = flag ? pos.down() : pos;
               if (facing == EnumFacing.UP) {
                  int i = MathHelper.floor((double)((playerIn.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                  worldIn.setBlockState(pos, Blocks.STANDING_BANNER.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(i)), 3);
               } else {
                  worldIn.setBlockState(pos, Blocks.WALL_BANNER.getDefaultState().withProperty(BlockWallSign.FACING, facing), 3);
               }

               --stack.stackSize;
               TileEntity tileentity = worldIn.getTileEntity(pos);
               if (tileentity instanceof TileEntityBanner) {
                  ((TileEntityBanner)tileentity).setItemValues(stack);
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
      String s = "item.banner.";
      EnumDyeColor enumdyecolor = getBaseColor(stack);
      s = s + enumdyecolor.getUnlocalizedName() + ".name";
      return I18n.translateToLocal(s);
   }

   @SideOnly(Side.CLIENT)
   public static void appendHoverTextFromTileEntityTag(ItemStack var0, List var1) {
      NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);
      if (nbttagcompound != null && nbttagcompound.hasKey("Patterns")) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Patterns", 10);

         for(int i = 0; i < nbttaglist.tagCount() && i < 6; ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(nbttagcompound1.getInteger("Color"));
            TileEntityBanner.EnumBannerPattern tileentitybanner$enumbannerpattern = TileEntityBanner.EnumBannerPattern.getPatternByID(nbttagcompound1.getString("Pattern"));
            if (tileentitybanner$enumbannerpattern != null) {
               p_185054_1_.add(I18n.translateToLocal("item.banner." + tileentitybanner$enumbannerpattern.getPatternName() + "." + enumdyecolor.getUnlocalizedName()));
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      appendHoverTextFromTileEntityTag(stack, tooltip);
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         TileEntityBanner.setBaseColorAndPatterns(nbttagcompound, enumdyecolor.getDyeDamage(), (NBTTagList)null);
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         nbttagcompound1.setTag("BlockEntityTag", nbttagcompound);
         ItemStack itemstack = new ItemStack(itemIn, 1, enumdyecolor.getDyeDamage());
         itemstack.setTagCompound(nbttagcompound1);
         subItems.add(itemstack);
      }

   }

   @SideOnly(Side.CLIENT)
   public CreativeTabs getCreativeTab() {
      return CreativeTabs.DECORATIONS;
   }

   public static EnumDyeColor getBaseColor(ItemStack var0) {
      NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);
      EnumDyeColor enumdyecolor;
      if (nbttagcompound != null && nbttagcompound.hasKey("Base")) {
         enumdyecolor = EnumDyeColor.byDyeDamage(nbttagcompound.getInteger("Base"));
      } else {
         enumdyecolor = EnumDyeColor.byDyeDamage(stack.getMetadata());
      }

      return enumdyecolor;
   }
}
