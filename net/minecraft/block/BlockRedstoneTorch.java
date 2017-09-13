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

   private boolean isBurnedOut(World world, BlockPos blockposition, boolean flag) {
      if (!toggles.containsKey(world)) {
         toggles.put(world, Lists.newArrayList());
      }

      List list = (List)toggles.get(world);
      if (flag) {
         list.add(new BlockRedstoneTorch.Toggle(blockposition, world.getTotalWorldTime()));
      }

      int i = 0;

      for(int j = 0; j < list.size(); ++j) {
         BlockRedstoneTorch.Toggle blockredstonetorch_redstoneupdateinfo = (BlockRedstoneTorch.Toggle)list.get(j);
         if (blockredstonetorch_redstoneupdateinfo.pos.equals(blockposition)) {
            ++i;
            if (i >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   protected BlockRedstoneTorch(boolean flag) {
      this.isOn = flag;
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
   }

   public int tickRate(World world) {
      return 2;
   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (this.isOn) {
         for(EnumFacing enumdirection : EnumFacing.values()) {
            world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection), this);
         }
      }

   }

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (this.isOn) {
         for(EnumFacing enumdirection : EnumFacing.values()) {
            world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection), this);
         }
      }

   }

   public int getWeakPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return this.isOn && iblockdata.getValue(FACING) != enumdirection ? 15 : 0;
   }

   private boolean shouldBeOff(World world, BlockPos blockposition, IBlockState iblockdata) {
      EnumFacing enumdirection = ((EnumFacing)iblockdata.getValue(FACING)).getOpposite();
      return world.isSidePowered(blockposition.offset(enumdirection), enumdirection);
   }

   public void randomTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      boolean flag = this.shouldBeOff(world, blockposition, iblockdata);
      List list = (List)toggles.get(world);

      while(list != null && !list.isEmpty() && world.getTotalWorldTime() - ((BlockRedstoneTorch.Toggle)list.get(0)).time > 60L) {
         list.remove(0);
      }

      PluginManager manager = world.getServer().getPluginManager();
      org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
      int oldCurrent = this.isOn ? 15 : 0;
      BlockRedstoneEvent event = new BlockRedstoneEvent(block, oldCurrent, oldCurrent);
      if (this.isOn) {
         if (flag) {
            if (oldCurrent != 0) {
               event.setNewCurrent(0);
               manager.callEvent(event);
               if (event.getNewCurrent() != 0) {
                  return;
               }
            }

            world.setBlockState(blockposition, Blocks.UNLIT_REDSTONE_TORCH.getDefaultState().withProperty(FACING, (EnumFacing)iblockdata.getValue(FACING)), 3);
            if (this.isBurnedOut(world, blockposition, true)) {
               world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

               for(int i = 0; i < 5; ++i) {
                  double d0 = (double)blockposition.getX() + random.nextDouble() * 0.6D + 0.2D;
                  double d1 = (double)blockposition.getY() + random.nextDouble() * 0.6D + 0.2D;
                  double d2 = (double)blockposition.getZ() + random.nextDouble() * 0.6D + 0.2D;
                  world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
               }

               world.scheduleUpdate(blockposition, world.getBlockState(blockposition).getBlock(), 160);
            }
         }
      } else if (!flag && !this.isBurnedOut(world, blockposition, false)) {
         if (oldCurrent != 15) {
            event.setNewCurrent(15);
            manager.callEvent(event);
            if (event.getNewCurrent() != 15) {
               return;
            }
         }

         world.setBlockState(blockposition, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(FACING, (EnumFacing)iblockdata.getValue(FACING)), 3);
      }

   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!this.onNeighborChangeInternal(world, blockposition, iblockdata) && this.isOn == this.shouldBeOff(world, blockposition, iblockdata)) {
         world.scheduleUpdate(blockposition, this, this.tickRate(world));
      }

   }

   public int getStrongPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return enumdirection == EnumFacing.DOWN ? iblockdata.getWeakPower(iblockaccess, blockposition, enumdirection) : 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
   }

   public boolean canProvidePower(IBlockState iblockdata) {
      return true;
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Blocks.REDSTONE_TORCH);
   }

   public boolean isAssociatedBlock(Block block) {
      return block == Blocks.UNLIT_REDSTONE_TORCH || block == Blocks.REDSTONE_TORCH;
   }

   static class Toggle {
      BlockPos pos;
      long time;

      public Toggle(BlockPos blockposition, long i) {
         this.pos = blockposition;
         this.time = i;
      }
   }
}
