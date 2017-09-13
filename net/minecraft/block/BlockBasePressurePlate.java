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
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.PluginManager;

public abstract class BlockBasePressurePlate extends Block {
   protected static final AxisAlignedBB PRESSED_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.03125D, 0.9375D);
   protected static final AxisAlignedBB UNPRESSED_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.0625D, 0.9375D);
   protected static final AxisAlignedBB PRESSURE_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);

   protected BlockBasePressurePlate(Material var1) {
      this(var1, var1.getMaterialMapColor());
   }

   protected BlockBasePressurePlate(Material var1, MapColor var2) {
      super(var1, var2);
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      boolean var4 = this.getRedstoneStrength(var1) > 0;
      return var4 ? PRESSED_AABB : UNPRESSED_AABB;
   }

   public int tickRate(World var1) {
      return 20;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public boolean canSpawnInBlock() {
      return true;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return this.canBePlacedOn(var1, var2.down());
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canBePlacedOn(var2, var3.down())) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

   }

   private boolean canBePlacedOn(World var1, BlockPos var2) {
      return var1.getBlockState(var2).isFullyOpaque() || var1.getBlockState(var2).getBlock() instanceof BlockFence;
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote) {
         int var5 = this.getRedstoneStrength(var3);
         if (var5 > 0) {
            this.updateState(var1, var2, var3, var5);
         }
      }

   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!var1.isRemote) {
         int var5 = this.getRedstoneStrength(var3);
         if (var5 == 0) {
            this.updateState(var1, var2, var3, var5);
         }
      }

   }

   protected void updateState(World var1, BlockPos var2, IBlockState var3, int var4) {
      int var5 = this.computeRedstoneStrength(var1, var2);
      boolean var6 = var4 > 0;
      boolean var7 = var5 > 0;
      CraftWorld var8 = var1.getWorld();
      PluginManager var9 = var1.getServer().getPluginManager();
      if (var6 != var7) {
         BlockRedstoneEvent var10 = new BlockRedstoneEvent(var8.getBlockAt(var2.getX(), var2.getY(), var2.getZ()), var4, var5);
         var9.callEvent(var10);
         var7 = var10.getNewCurrent() > 0;
         var5 = var10.getNewCurrent();
      }

      if (var4 != var5) {
         var3 = this.setRedstoneStrength(var3, var5);
         var1.setBlockState(var2, var3, 2);
         this.updateNeighbors(var1, var2);
         var1.markBlockRangeForRenderUpdate(var2, var2);
      }

      if (!var7 && var6) {
         this.playClickOffSound(var1, var2);
      } else if (var7 && !var6) {
         this.playClickOnSound(var1, var2);
      }

      if (var7) {
         var1.scheduleUpdate(new BlockPos(var2), this, this.tickRate(var1));
      }

   }

   protected abstract void playClickOnSound(World var1, BlockPos var2);

   protected abstract void playClickOffSound(World var1, BlockPos var2);

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (this.getRedstoneStrength(var3) > 0) {
         this.updateNeighbors(var1, var2);
      }

      super.breakBlock(var1, var2, var3);
   }

   protected void updateNeighbors(World var1, BlockPos var2) {
      var1.notifyNeighborsOfStateChange(var2, this);
      var1.notifyNeighborsOfStateChange(var2.down(), this);
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return this.getRedstoneStrength(var1);
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return var4 == EnumFacing.UP ? this.getRedstoneStrength(var1) : 0;
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.DESTROY;
   }

   protected abstract int computeRedstoneStrength(World var1, BlockPos var2);

   protected abstract int getRedstoneStrength(IBlockState var1);

   protected abstract IBlockState setRedstoneStrength(IBlockState var1, int var2);
}
