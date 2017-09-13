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

   public ItemBlock(Block block) {
      this.block = block;
   }

   public ItemBlock setUnlocalizedName(String s) {
      super.setUnlocalizedName(s);
      return this;
   }

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      IBlockState iblockdata = world.getBlockState(blockposition);
      Block block = iblockdata.getBlock();
      if (!block.isReplaceable(world, blockposition)) {
         blockposition = blockposition.offset(enumdirection);
      }

      if (itemstack.stackSize != 0 && entityhuman.canPlayerEdit(blockposition, enumdirection, itemstack) && world.canBlockBePlaced(this.block, blockposition, false, enumdirection, (Entity)null, itemstack)) {
         int i = this.getMetadata(itemstack.getMetadata());
         IBlockState iblockdata1 = this.block.getStateForPlacement(world, blockposition, enumdirection, f, f1, f2, i, entityhuman);
         if (world.setBlockState(blockposition, iblockdata1, 11)) {
            iblockdata1 = world.getBlockState(blockposition);
            if (iblockdata1.getBlock() == this.block) {
               setTileEntityNBT(world, entityhuman, blockposition, itemstack);
               this.block.onBlockPlacedBy(world, blockposition, iblockdata1, entityhuman, itemstack);
            }

            this.block.getSoundType();
            --itemstack.stackSize;
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public static boolean setTileEntityNBT(World world, @Nullable EntityPlayer entityhuman, BlockPos blockposition, ItemStack itemstack) {
      MinecraftServer minecraftserver = world.getMinecraftServer();
      if (minecraftserver == null) {
         return false;
      } else {
         if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
            TileEntity tileentity = world.getTileEntity(blockposition);
            if (tileentity != null) {
               if (!world.isRemote && tileentity.onlyOpsCanSetNbt() && (entityhuman == null || !entityhuman.canUseCommandBlock())) {
                  return false;
               }

               NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
               NBTTagCompound nbttagcompound1 = nbttagcompound.copy();
               NBTTagCompound nbttagcompound2 = (NBTTagCompound)itemstack.getTagCompound().getTag("BlockEntityTag");
               nbttagcompound.merge(nbttagcompound2);
               nbttagcompound.setInteger("x", blockposition.getX());
               nbttagcompound.setInteger("y", blockposition.getY());
               nbttagcompound.setInteger("z", blockposition.getZ());
               if (!nbttagcompound.equals(nbttagcompound1)) {
                  tileentity.readFromNBT(nbttagcompound);
                  tileentity.markDirty();
                  return true;
               }
            }
         }

         return false;
      }
   }

   public String getUnlocalizedName(ItemStack itemstack) {
      return this.block.getUnlocalizedName();
   }

   public String getUnlocalizedName() {
      return this.block.getUnlocalizedName();
   }

   public Block getBlock() {
      return this.block;
   }

   public Item setUnlocalizedName(String s) {
      return this.setUnlocalizedName(s);
   }
}
