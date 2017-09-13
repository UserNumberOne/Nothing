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
      this.block = block;
   }

   public ItemBlock setUnlocalizedName(String var1) {
      super.setUnlocalizedName(unlocalizedName);
      return this;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      Block block = iblockstate.getBlock();
      if (!block.isReplaceable(worldIn, pos)) {
         pos = pos.offset(facing);
      }

      if (stack.stackSize != 0 && playerIn.canPlayerEdit(pos, facing, stack) && worldIn.canBlockBePlaced(this.block, pos, false, facing, (Entity)null, stack)) {
         int i = this.getMetadata(stack.getMetadata());
         IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, playerIn, stack);
         if (this.placeBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1)) {
            SoundType soundtype = worldIn.getBlockState(pos).getBlock().getSoundType(worldIn.getBlockState(pos), worldIn, pos, playerIn);
            worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            --stack.stackSize;
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public static boolean setTileEntityNBT(World var0, @Nullable EntityPlayer var1, BlockPos var2, ItemStack var3) {
      MinecraftServer minecraftserver = worldIn.getMinecraftServer();
      if (minecraftserver == null) {
         return false;
      } else {
         if (stackIn.hasTagCompound() && stackIn.getTagCompound().hasKey("BlockEntityTag", 10)) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity != null) {
               if (!worldIn.isRemote && tileentity.onlyOpsCanSetNbt() && (player == null || !player.canUseCommandBlock())) {
                  return false;
               }

               NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
               NBTTagCompound nbttagcompound1 = nbttagcompound.copy();
               NBTTagCompound nbttagcompound2 = (NBTTagCompound)stackIn.getTagCompound().getTag("BlockEntityTag");
               nbttagcompound.merge(nbttagcompound2);
               nbttagcompound.setInteger("x", pos.getX());
               nbttagcompound.setInteger("y", pos.getY());
               nbttagcompound.setInteger("z", pos.getZ());
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

   @SideOnly(Side.CLIENT)
   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3, EntityPlayer var4, ItemStack var5) {
      Block block = worldIn.getBlockState(pos).getBlock();
      if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
         side = EnumFacing.UP;
      } else if (!block.isReplaceable(worldIn, pos)) {
         pos = pos.offset(side);
      }

      return worldIn.canBlockBePlaced(this.block, pos, false, side, (Entity)null, stack);
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
      this.block.getSubBlocks(itemIn, tab, subItems);
   }

   public Block getBlock() {
      return this.block;
   }

   public boolean placeBlockAt(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumFacing var5, float var6, float var7, float var8, IBlockState var9) {
      if (!world.setBlockState(pos, newState, 3)) {
         return false;
      } else {
         IBlockState state = world.getBlockState(pos);
         if (state.getBlock() == this.block) {
            setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
         }

         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      this.block.addInformation(stack, playerIn, tooltip, advanced);
   }
}
