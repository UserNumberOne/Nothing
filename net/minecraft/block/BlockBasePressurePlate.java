package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.PluginManager;

public abstract class BlockBasePressurePlate extends Block {
   protected static final AxisAlignedBB PRESSED_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.03125D, 0.9375D);
   protected static final AxisAlignedBB UNPRESSED_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.0625D, 0.9375D);
   protected static final AxisAlignedBB PRESSURE_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);

   protected BlockBasePressurePlate(Material material) {
      this(material, material.getMaterialMapColor());
   }

   protected BlockBasePressurePlate(Material material, MapColor materialmapcolor) {
      super(material, materialmapcolor);
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      boolean flag = this.getRedstoneStrength(iblockdata) > 0;
      return flag ? PRESSED_AABB : UNPRESSED_AABB;
   }

   public int tickRate(World world) {
      return 20;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isPassable(IBlockAccess iblockaccess, BlockPos blockposition) {
      return true;
   }

   public boolean canSpawnInBlock() {
      return true;
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return this.canBePlacedOn(world, blockposition.down());
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!this.canBePlacedOn(world, blockposition.down())) {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
      }

   }

   private boolean canBePlacedOn(World world, BlockPos blockposition) {
      return world.getBlockState(blockposition).isFullyOpaque() || world.getBlockState(blockposition).getBlock() instanceof BlockFence;
   }

   public void randomTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote) {
         int i = this.getRedstoneStrength(iblockdata);
         if (i > 0) {
            this.updateState(world, blockposition, iblockdata, i);
         }
      }

   }

   public void onEntityCollidedWithBlock(World world, BlockPos blockposition, IBlockState iblockdata, Entity entity) {
      if (!world.isRemote) {
         int i = this.getRedstoneStrength(iblockdata);
         if (i == 0) {
            this.updateState(world, blockposition, iblockdata, i);
         }
      }

   }

   protected void updateState(World world, BlockPos blockposition, IBlockState iblockdata, int i) {
      int j = this.computeRedstoneStrength(world, blockposition);
      boolean flag = i > 0;
      boolean flag1 = j > 0;
      org.bukkit.World bworld = world.getWorld();
      PluginManager manager = world.getServer().getPluginManager();
      if (flag != flag1) {
         BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), i, j);
         manager.callEvent(eventRedstone);
         flag1 = eventRedstone.getNewCurrent() > 0;
         j = eventRedstone.getNewCurrent();
      }

      if (i != j) {
         iblockdata = this.setRedstoneStrength(iblockdata, j);
         world.setBlockState(blockposition, iblockdata, 2);
         this.updateNeighbors(world, blockposition);
         world.markBlockRangeForRenderUpdate(blockposition, blockposition);
      }

      if (!flag1 && flag) {
         this.playClickOffSound(world, blockposition);
      } else if (flag1 && !flag) {
         this.playClickOnSound(world, blockposition);
      }

      if (flag1) {
         world.scheduleUpdate(new BlockPos(blockposition), this, this.tickRate(world));
      }

   }

   protected abstract void playClickOnSound(World var1, BlockPos var2);

   protected abstract void playClickOffSound(World var1, BlockPos var2);

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (this.getRedstoneStrength(iblockdata) > 0) {
         this.updateNeighbors(world, blockposition);
      }

      super.breakBlock(world, blockposition, iblockdata);
   }

   protected void updateNeighbors(World world, BlockPos blockposition) {
      world.notifyNeighborsOfStateChange(blockposition, this);
      world.notifyNeighborsOfStateChange(blockposition.down(), this);
   }

   public int getWeakPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return this.getRedstoneStrength(iblockdata);
   }

   public int getStrongPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return enumdirection == EnumFacing.UP ? this.getRedstoneStrength(iblockdata) : 0;
   }

   public boolean canProvidePower(IBlockState iblockdata) {
      return true;
   }

   public EnumPushReaction getMobilityFlag(IBlockState iblockdata) {
      return EnumPushReaction.DESTROY;
   }

   protected abstract int computeRedstoneStrength(World var1, BlockPos var2);

   protected abstract int getRedstoneStrength(IBlockState var1);

   protected abstract IBlockState setRedstoneStrength(IBlockState var1, int var2);
}
