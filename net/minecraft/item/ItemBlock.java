package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlock extends Item {
   public final Block block;

   public ItemBlock(Block var1) {
      this.block = var1;
   }

   public ItemBlock setUnlocalizedName(String var1) {
      super.setUnlocalizedName(var1);
      return this;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      Block var11 = var10.getBlock();
      if (!var11.isReplaceable(var3, var4)) {
         var4 = var4.offset(var6);
      }

      if (var1.stackSize != 0 && var2.canPlayerEdit(var4, var6, var1) && var3.canBlockBePlaced(this.block, var4, false, var6, (Entity)null, var1)) {
         int var12 = this.getMetadata(var1.getMetadata());
         IBlockState var13 = this.block.getStateForPlacement(var3, var4, var6, var7, var8, var9, var12, var2, var1);
         if (this.placeBlockAt(var1, var2, var3, var4, var6, var7, var8, var9, var13)) {
            SoundType var14 = var3.getBlockState(var4).getBlock().getSoundType(var3.getBlockState(var4), var3, var4, var2);
            var3.playSound(var2, var4, var14.getPlaceSound(), SoundCategory.BLOCKS, (var14.getVolume() + 1.0F) / 2.0F, var14.getPitch() * 0.8F);
            --var1.stackSize;
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public static boolean setTileEntityNBT(World var0, @Nullable EntityPlayer var1, BlockPos var2, ItemStack var3) {
      MinecraftServer var4 = var0.getMinecraftServer();
      if (var4 == null) {
         return false;
      } else {
         if (var3.hasTagCompound() && var3.getTagCompound().hasKey("BlockEntityTag", 10)) {
            TileEntity var5 = var0.getTileEntity(var2);
            if (var5 != null) {
               if (!var0.isRemote && var5.onlyOpsCanSetNbt() && (var1 == null || !var1.canUseCommandBlock())) {
                  return false;
               }

               NBTTagCompound var6 = var5.writeToNBT(new NBTTagCompound());
               NBTTagCompound var7 = var6.copy();
               NBTTagCompound var8 = (NBTTagCompound)var3.getTagCompound().getTag("BlockEntityTag");
               var6.merge(var8);
               var6.setInteger("x", var2.getX());
               var6.setInteger("y", var2.getY());
               var6.setInteger("z", var2.getZ());
               if (!var6.equals(var7)) {
                  var5.readFromNBT(var6);
                  var5.markDirty();
                  return true;
               }
            }
         }

         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3, EntityPlayer var4, ItemStack var5) {
      Block var6 = var1.getBlockState(var2).getBlock();
      if (var6 == Blocks.SNOW_LAYER && var6.isReplaceable(var1, var2)) {
         var3 = EnumFacing.UP;
      } else if (!var6.isReplaceable(var1, var2)) {
         var2 = var2.offset(var3);
      }

      return var1.canBlockBePlaced(this.block, var2, false, var3, (Entity)null, var5);
   }

   public String getUnlocalizedName(ItemStack var1) {
      return this.block.getUnlocalizedName();
   }

   public String getUnlocalizedName() {
      return this.block.getUnlocalizedName();
   }

   @SideOnly(Side.CLIENT)
   public CreativeTabs getCreativeTab() {
      return this.block.getCreativeTabToDisplayOn();
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      this.block.getSubBlocks(var1, var2, var3);
   }

   public Block getBlock() {
      return this.block;
   }

   public boolean placeBlockAt(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumFacing var5, float var6, float var7, float var8, IBlockState var9) {
      if (!var3.setBlockState(var4, var9, 3)) {
         return false;
      } else {
         IBlockState var10 = var3.getBlockState(var4);
         if (var10.getBlock() == this.block) {
            setTileEntityNBT(var3, var2, var4, var1);
            this.block.onBlockPlacedBy(var3, var4, var10, var2, var1);
         }

         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      this.block.addInformation(var1, var2, var3, var4);
   }
}
