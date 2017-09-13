package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityInteractEvent;

public abstract class BlockButton extends BlockDirectional {
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   protected static final AxisAlignedBB AABB_DOWN_OFF = new AxisAlignedBB(0.3125D, 0.875D, 0.375D, 0.6875D, 1.0D, 0.625D);
   protected static final AxisAlignedBB AABB_UP_OFF = new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.6875D, 0.125D, 0.625D);
   protected static final AxisAlignedBB AABB_NORTH_OFF = new AxisAlignedBB(0.3125D, 0.375D, 0.875D, 0.6875D, 0.625D, 1.0D);
   protected static final AxisAlignedBB AABB_SOUTH_OFF = new AxisAlignedBB(0.3125D, 0.375D, 0.0D, 0.6875D, 0.625D, 0.125D);
   protected static final AxisAlignedBB AABB_WEST_OFF = new AxisAlignedBB(0.875D, 0.375D, 0.3125D, 1.0D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB AABB_EAST_OFF = new AxisAlignedBB(0.0D, 0.375D, 0.3125D, 0.125D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB AABB_DOWN_ON = new AxisAlignedBB(0.3125D, 0.9375D, 0.375D, 0.6875D, 1.0D, 0.625D);
   protected static final AxisAlignedBB AABB_UP_ON = new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.6875D, 0.0625D, 0.625D);
   protected static final AxisAlignedBB AABB_NORTH_ON = new AxisAlignedBB(0.3125D, 0.375D, 0.9375D, 0.6875D, 0.625D, 1.0D);
   protected static final AxisAlignedBB AABB_SOUTH_ON = new AxisAlignedBB(0.3125D, 0.375D, 0.0D, 0.6875D, 0.625D, 0.0625D);
   protected static final AxisAlignedBB AABB_WEST_ON = new AxisAlignedBB(0.9375D, 0.375D, 0.3125D, 1.0D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB AABB_EAST_ON = new AxisAlignedBB(0.0D, 0.375D, 0.3125D, 0.0625D, 0.625D, 0.6875D);
   private final boolean wooden;

   protected BlockButton(boolean var1) {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.valueOf(false)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.wooden = var1;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public int tickRate(World var1) {
      return this.wooden ? 30 : 20;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return canPlaceBlock(var1, var2, var3.getOpposite());
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      for(EnumFacing var6 : EnumFacing.values()) {
         if (canPlaceBlock(var1, var2, var6)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean canPlaceBlock(World var0, BlockPos var1, EnumFacing var2) {
      BlockPos var3 = var1.offset(var2);
      return var2 == EnumFacing.DOWN ? var0.getBlockState(var3).isFullyOpaque() : var0.getBlockState(var3).isNormalCube();
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return canPlaceBlock(var1, var2, var3.getOpposite()) ? this.getDefaultState().withProperty(FACING, var3).withProperty(POWERED, Boolean.valueOf(false)) : this.getDefaultState().withProperty(FACING, EnumFacing.DOWN).withProperty(POWERED, Boolean.valueOf(false));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (this.checkForDrop(var2, var3, var1) && !canPlaceBlock(var2, var3, ((EnumFacing)var1.getValue(FACING)).getOpposite())) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

   }

   private boolean checkForDrop(World var1, BlockPos var2, IBlockState var3) {
      if (this.canPlaceBlockAt(var1, var2)) {
         return true;
      } else {
         this.dropBlockAsItem(var1, var2, var3, 0);
         var1.setBlockToAir(var2);
         return false;
      }
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      EnumFacing var4 = (EnumFacing)var1.getValue(FACING);
      boolean var5 = ((Boolean)var1.getValue(POWERED)).booleanValue();
      switch(BlockButton.SyntheticClass_1.a[var4.ordinal()]) {
      case 1:
         return var5 ? AABB_EAST_ON : AABB_EAST_OFF;
      case 2:
         return var5 ? AABB_WEST_ON : AABB_WEST_OFF;
      case 3:
         return var5 ? AABB_SOUTH_ON : AABB_SOUTH_OFF;
      case 4:
      default:
         return var5 ? AABB_NORTH_ON : AABB_NORTH_OFF;
      case 5:
         return var5 ? AABB_UP_ON : AABB_UP_OFF;
      case 6:
         return var5 ? AABB_DOWN_ON : AABB_DOWN_OFF;
      }
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (((Boolean)var3.getValue(POWERED)).booleanValue()) {
         return true;
      } else {
         boolean var11 = ((Boolean)var3.getValue(POWERED)).booleanValue();
         org.bukkit.block.Block var12 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
         int var13 = var11 ? 15 : 0;
         int var14 = !var11 ? 15 : 0;
         BlockRedstoneEvent var15 = new BlockRedstoneEvent(var12, var13, var14);
         var1.getServer().getPluginManager().callEvent(var15);
         if (var15.getNewCurrent() > 0 != !var11) {
            return true;
         } else {
            var1.setBlockState(var2, var3.withProperty(POWERED, Boolean.valueOf(true)), 3);
            var1.markBlockRangeForRenderUpdate(var2, var2);
            this.playClickSound(var4, var1, var2);
            this.notifyNeighbors(var1, var2, (EnumFacing)var3.getValue(FACING));
            var1.scheduleUpdate(var2, this, this.tickRate(var1));
            return true;
         }
      }
   }

   protected abstract void playClickSound(@Nullable EntityPlayer var1, World var2, BlockPos var3);

   protected abstract void playReleaseSound(World var1, BlockPos var2);

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (((Boolean)var3.getValue(POWERED)).booleanValue()) {
         this.notifyNeighbors(var1, var2, (EnumFacing)var3.getValue(FACING));
      }

      super.breakBlock(var1, var2, var3);
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return ((Boolean)var1.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !((Boolean)var1.getValue(POWERED)).booleanValue() ? 0 : (var1.getValue(FACING) == var4 ? 15 : 0);
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && ((Boolean)var3.getValue(POWERED)).booleanValue()) {
         if (this.wooden) {
            this.checkPressed(var3, var1, var2);
         } else {
            org.bukkit.block.Block var5 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
            BlockRedstoneEvent var6 = new BlockRedstoneEvent(var5, 15, 0);
            var1.getServer().getPluginManager().callEvent(var6);
            if (var6.getNewCurrent() > 0) {
               return;
            }

            var1.setBlockState(var2, var3.withProperty(POWERED, Boolean.valueOf(false)));
            this.notifyNeighbors(var1, var2, (EnumFacing)var3.getValue(FACING));
            this.playReleaseSound(var1, var2);
            var1.markBlockRangeForRenderUpdate(var2, var2);
         }
      }

   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!var1.isRemote && this.wooden && !((Boolean)var3.getValue(POWERED)).booleanValue()) {
         this.checkPressed(var3, var1, var2);
      }

   }

   private void checkPressed(IBlockState var1, World var2, BlockPos var3) {
      List var4 = var2.getEntitiesWithinAABB(EntityArrow.class, var1.getBoundingBox(var2, var3).offset(var3));
      boolean var5 = !var4.isEmpty();
      boolean var6 = ((Boolean)var1.getValue(POWERED)).booleanValue();
      if (var6 != var5 && var5) {
         org.bukkit.block.Block var7 = var2.getWorld().getBlockAt(var3.getX(), var3.getY(), var3.getZ());
         boolean var8 = false;

         for(Object var10 : var4) {
            if (var10 != null) {
               EntityInteractEvent var11 = new EntityInteractEvent(((Entity)var10).getBukkitEntity(), var7);
               var2.getServer().getPluginManager().callEvent(var11);
               if (!var11.isCancelled()) {
                  var8 = true;
                  break;
               }
            }
         }

         if (!var8) {
            return;
         }
      }

      if (var5 && !var6) {
         org.bukkit.block.Block var12 = var2.getWorld().getBlockAt(var3.getX(), var3.getY(), var3.getZ());
         BlockRedstoneEvent var14 = new BlockRedstoneEvent(var12, 0, 15);
         var2.getServer().getPluginManager().callEvent(var14);
         if (var14.getNewCurrent() <= 0) {
            return;
         }

         var2.setBlockState(var3, var1.withProperty(POWERED, Boolean.valueOf(true)));
         this.notifyNeighbors(var2, var3, (EnumFacing)var1.getValue(FACING));
         var2.markBlockRangeForRenderUpdate(var3, var3);
         this.playClickSound((EntityPlayer)null, var2, var3);
      }

      if (!var5 && var6) {
         org.bukkit.block.Block var13 = var2.getWorld().getBlockAt(var3.getX(), var3.getY(), var3.getZ());
         BlockRedstoneEvent var15 = new BlockRedstoneEvent(var13, 15, 0);
         var2.getServer().getPluginManager().callEvent(var15);
         if (var15.getNewCurrent() > 0) {
            return;
         }

         var2.setBlockState(var3, var1.withProperty(POWERED, Boolean.valueOf(false)));
         this.notifyNeighbors(var2, var3, (EnumFacing)var1.getValue(FACING));
         var2.markBlockRangeForRenderUpdate(var3, var3);
         this.playReleaseSound(var2, var3);
      }

      if (var5) {
         var2.scheduleUpdate(new BlockPos(var3), this, this.tickRate(var2));
      }

   }

   private void notifyNeighbors(World var1, BlockPos var2, EnumFacing var3) {
      var1.notifyNeighborsOfStateChange(var2, this);
      var1.notifyNeighborsOfStateChange(var2.offset(var3.getOpposite()), this);
   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing var2;
      switch(var1 & 7) {
      case 0:
         var2 = EnumFacing.DOWN;
         break;
      case 1:
         var2 = EnumFacing.EAST;
         break;
      case 2:
         var2 = EnumFacing.WEST;
         break;
      case 3:
         var2 = EnumFacing.SOUTH;
         break;
      case 4:
         var2 = EnumFacing.NORTH;
         break;
      case 5:
      default:
         var2 = EnumFacing.UP;
      }

      return this.getDefaultState().withProperty(FACING, var2).withProperty(POWERED, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2;
      switch(BlockButton.SyntheticClass_1.a[((EnumFacing)var1.getValue(FACING)).ordinal()]) {
      case 1:
         var2 = 1;
         break;
      case 2:
         var2 = 2;
         break;
      case 3:
         var2 = 3;
         break;
      case 4:
         var2 = 4;
         break;
      case 5:
      default:
         var2 = 5;
         break;
      case 6:
         var2 = 0;
      }

      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, POWERED});
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.EAST.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.UP.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.DOWN.ordinal()] = 6;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
