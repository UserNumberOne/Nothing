package net.minecraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.PluginManager;

public class BlockRedstoneTorch extends BlockTorch {
   private static final Map toggles = Maps.newHashMap();
   private final boolean isOn;

   private boolean isBurnedOut(World var1, BlockPos var2, boolean var3) {
      if (!toggles.containsKey(var1)) {
         toggles.put(var1, Lists.newArrayList());
      }

      List var4 = (List)toggles.get(var1);
      if (var3) {
         var4.add(new BlockRedstoneTorch.Toggle(var2, var1.getTotalWorldTime()));
      }

      int var5 = 0;

      for(int var6 = 0; var6 < var4.size(); ++var6) {
         BlockRedstoneTorch.Toggle var7 = (BlockRedstoneTorch.Toggle)var4.get(var6);
         if (var7.pos.equals(var2)) {
            ++var5;
            if (var5 >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   protected BlockRedstoneTorch(boolean var1) {
      this.isOn = var1;
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
   }

   public int tickRate(World var1) {
      return 2;
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (this.isOn) {
         for(EnumFacing var7 : EnumFacing.values()) {
            var1.notifyNeighborsOfStateChange(var2.offset(var7), this);
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (this.isOn) {
         for(EnumFacing var7 : EnumFacing.values()) {
            var1.notifyNeighborsOfStateChange(var2.offset(var7), this);
         }
      }

   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return this.isOn && var1.getValue(FACING) != var4 ? 15 : 0;
   }

   private boolean shouldBeOff(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = ((EnumFacing)var3.getValue(FACING)).getOpposite();
      return var1.isSidePowered(var2.offset(var4), var4);
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      boolean var5 = this.shouldBeOff(var1, var2, var3);
      List var6 = (List)toggles.get(var1);

      while(var6 != null && !var6.isEmpty() && var1.getTotalWorldTime() - ((BlockRedstoneTorch.Toggle)var6.get(0)).time > 60L) {
         var6.remove(0);
      }

      PluginManager var7 = var1.getServer().getPluginManager();
      org.bukkit.block.Block var8 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
      int var9 = this.isOn ? 15 : 0;
      BlockRedstoneEvent var10 = new BlockRedstoneEvent(var8, var9, var9);
      if (this.isOn) {
         if (var5) {
            if (var9 != 0) {
               var10.setNewCurrent(0);
               var7.callEvent(var10);
               if (var10.getNewCurrent() != 0) {
                  return;
               }
            }

            var1.setBlockState(var2, Blocks.UNLIT_REDSTONE_TORCH.getDefaultState().withProperty(FACING, (EnumFacing)var3.getValue(FACING)), 3);
            if (this.isBurnedOut(var1, var2, true)) {
               var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (var1.rand.nextFloat() - var1.rand.nextFloat()) * 0.8F);

               for(int var11 = 0; var11 < 5; ++var11) {
                  double var12 = (double)var2.getX() + var4.nextDouble() * 0.6D + 0.2D;
                  double var14 = (double)var2.getY() + var4.nextDouble() * 0.6D + 0.2D;
                  double var16 = (double)var2.getZ() + var4.nextDouble() * 0.6D + 0.2D;
                  var1.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var12, var14, var16, 0.0D, 0.0D, 0.0D);
               }

               var1.scheduleUpdate(var2, var1.getBlockState(var2).getBlock(), 160);
            }
         }
      } else if (!var5 && !this.isBurnedOut(var1, var2, false)) {
         if (var9 != 15) {
            var10.setNewCurrent(15);
            var7.callEvent(var10);
            if (var10.getNewCurrent() != 15) {
               return;
            }
         }

         var1.setBlockState(var2, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(FACING, (EnumFacing)var3.getValue(FACING)), 3);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.onNeighborChangeInternal(var2, var3, var1) && this.isOn == this.shouldBeOff(var2, var3, var1)) {
         var2.scheduleUpdate(var3, this, this.tickRate(var2));
      }

   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return var4 == EnumFacing.DOWN ? var1.getWeakPower(var2, var3, var4) : 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.REDSTONE_TORCH);
   }

   public boolean isAssociatedBlock(Block var1) {
      return var1 == Blocks.UNLIT_REDSTONE_TORCH || var1 == Blocks.REDSTONE_TORCH;
   }

   static class Toggle {
      BlockPos pos;
      long time;

      public Toggle(BlockPos var1, long var2) {
         this.pos = var1;
         this.time = var2;
      }
   }
}
