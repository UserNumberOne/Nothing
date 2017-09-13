package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRedstoneTorch extends BlockTorch {
   private static final Map toggles = new WeakHashMap();
   private final boolean isOn;

   private boolean isBurnedOut(World var1, BlockPos var2, boolean var3) {
      if (!toggles.containsKey(worldIn)) {
         toggles.put(worldIn, Lists.newArrayList());
      }

      List list = (List)toggles.get(worldIn);
      if (turnOff) {
         list.add(new BlockRedstoneTorch.Toggle(pos, worldIn.getTotalWorldTime()));
      }

      int i = 0;

      for(int j = 0; j < list.size(); ++j) {
         BlockRedstoneTorch.Toggle blockredstonetorch$toggle = (BlockRedstoneTorch.Toggle)list.get(j);
         if (blockredstonetorch$toggle.pos.equals(pos)) {
            ++i;
            if (i >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   protected BlockRedstoneTorch(boolean var1) {
      this.isOn = isOn;
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
   }

   public int tickRate(World var1) {
      return 2;
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (this.isOn) {
         for(EnumFacing enumfacing : EnumFacing.values()) {
            worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (this.isOn) {
         for(EnumFacing enumfacing : EnumFacing.values()) {
            worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
         }
      }

   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return this.isOn && blockState.getValue(FACING) != side ? 15 : 0;
   }

   private boolean shouldBeOff(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing enumfacing = ((EnumFacing)state.getValue(FACING)).getOpposite();
      return worldIn.isSidePowered(pos.offset(enumfacing), enumfacing);
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      boolean flag = this.shouldBeOff(worldIn, pos, state);
      List list = (List)toggles.get(worldIn);

      while(list != null && !list.isEmpty() && worldIn.getTotalWorldTime() - ((BlockRedstoneTorch.Toggle)list.get(0)).time > 60L) {
         list.remove(0);
      }

      if (this.isOn) {
         if (flag) {
            worldIn.setBlockState(pos, Blocks.UNLIT_REDSTONE_TORCH.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);
            if (this.isBurnedOut(worldIn, pos, true)) {
               worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

               for(int i = 0; i < 5; ++i) {
                  double d0 = (double)pos.getX() + rand.nextDouble() * 0.6D + 0.2D;
                  double d1 = (double)pos.getY() + rand.nextDouble() * 0.6D + 0.2D;
                  double d2 = (double)pos.getZ() + rand.nextDouble() * 0.6D + 0.2D;
                  worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
               }

               worldIn.scheduleUpdate(pos, worldIn.getBlockState(pos).getBlock(), 160);
            }
         }
      } else if (!flag && !this.isBurnedOut(worldIn, pos, false)) {
         worldIn.setBlockState(pos, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.onNeighborChangeInternal(worldIn, pos, state) && this.isOn == this.shouldBeOff(worldIn, pos, state)) {
         worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
      }

   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return side == EnumFacing.DOWN ? blockState.getWeakPower(blockAccess, pos, side) : 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (this.isOn) {
         double d0 = (double)pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
         double d1 = (double)pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
         double d2 = (double)pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
         EnumFacing enumfacing = (EnumFacing)stateIn.getValue(FACING);
         if (enumfacing.getAxis().isHorizontal()) {
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            double d3 = 0.27D;
            d0 += 0.27D * (double)enumfacing1.getFrontOffsetX();
            d1 += 0.22D;
            d2 += 0.27D * (double)enumfacing1.getFrontOffsetZ();
         }

         worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.REDSTONE_TORCH);
   }

   public boolean isAssociatedBlock(Block var1) {
      return other == Blocks.UNLIT_REDSTONE_TORCH || other == Blocks.REDSTONE_TORCH;
   }

   static class Toggle {
      BlockPos pos;
      long time;

      public Toggle(BlockPos var1, long var2) {
         this.pos = pos;
         this.time = time;
      }
   }
}
