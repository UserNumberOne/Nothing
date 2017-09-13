package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlock extends Item {
   protected final Block block;

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
         IBlockState var13 = this.block.getStateForPlacement(var3, var4, var6, var7, var8, var9, var12, var2);
         if (var3.setBlockState(var4, var13, 11)) {
            var13 = var3.getBlockState(var4);
            if (var13.getBlock() == this.block) {
               setTileEntityNBT(var3, var2, var4, var1);
               this.block.onBlockPlacedBy(var3, var4, var13, var2, var1);
            }

            this.block.getSoundType();
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

   public String getUnlocalizedName(ItemStack var1) {
      return this.block.getUnlocalizedName();
   }

   public String getUnlocalizedName() {
      return this.block.getUnlocalizedName();
   }

   public Block getBlock() {
      return this.block;
   }

   public Item setUnlocalizedName(String var1) {
      return this.setUnlocalizedName(var1);
   }
}
