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

   protected BlockButton(boolean flag) {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.valueOf(false)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.wooden = flag;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public int tickRate(World world) {
      return this.wooden ? 30 : 20;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World world, BlockPos blockposition, EnumFacing enumdirection) {
      return canPlaceBlock(world, blockposition, enumdirection.getOpposite());
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      for(EnumFacing enumdirection : EnumFacing.values()) {
         if (canPlaceBlock(world, blockposition, enumdirection)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean canPlaceBlock(World world, BlockPos blockposition, EnumFacing enumdirection) {
      BlockPos blockposition1 = blockposition.offset(enumdirection);
      return enumdirection == EnumFacing.DOWN ? world.getBlockState(blockposition1).isFullyOpaque() : world.getBlockState(blockposition1).isNormalCube();
   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      return canPlaceBlock(world, blockposition, enumdirection.getOpposite()) ? this.getDefaultState().withProperty(FACING, enumdirection).withProperty(POWERED, Boolean.valueOf(false)) : this.getDefaultState().withProperty(FACING, EnumFacing.DOWN).withProperty(POWERED, Boolean.valueOf(false));
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (this.checkForDrop(world, blockposition, iblockdata) && !canPlaceBlock(world, blockposition, ((EnumFacing)iblockdata.getValue(FACING)).getOpposite())) {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
      }

   }

   private boolean checkForDrop(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (this.canPlaceBlockAt(world, blockposition)) {
         return true;
      } else {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
         return false;
      }
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
      boolean flag = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
      switch(BlockButton.SyntheticClass_1.a[enumdirection.ordinal()]) {
      case 1:
         return flag ? AABB_EAST_ON : AABB_EAST_OFF;
      case 2:
         return flag ? AABB_WEST_ON : AABB_WEST_OFF;
      case 3:
         return flag ? AABB_SOUTH_ON : AABB_SOUTH_OFF;
      case 4:
      default:
         return flag ? AABB_NORTH_ON : AABB_NORTH_OFF;
      case 5:
         return flag ? AABB_UP_ON : AABB_UP_OFF;
      case 6:
         return flag ? AABB_DOWN_ON : AABB_DOWN_OFF;
      }
   }

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         return true;
      } else {
         boolean powered = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
         org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         int old = powered ? 15 : 0;
         int current = !powered ? 15 : 0;
         BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, old, current);
         world.getServer().getPluginManager().callEvent(eventRedstone);
         if (eventRedstone.getNewCurrent() > 0 != !powered) {
            return true;
         } else {
            world.setBlockState(blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(true)), 3);
            world.markBlockRangeForRenderUpdate(blockposition, blockposition);
            this.playClickSound(entityhuman, world, blockposition);
            this.notifyNeighbors(world, blockposition, (EnumFacing)iblockdata.getValue(FACING));
            world.scheduleUpdate(blockposition, this, this.tickRate(world));
            return true;
         }
      }
   }

   protected abstract void playClickSound(@Nullable EntityPlayer var1, World var2, BlockPos var3);

   protected abstract void playReleaseSound(World var1, BlockPos var2);

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         this.notifyNeighbors(world, blockposition, (EnumFacing)iblockdata.getValue(FACING));
      }

      super.breakBlock(world, blockposition, iblockdata);
   }

   public int getWeakPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return ((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return !((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 0 : (iblockdata.getValue(FACING) == enumdirection ? 15 : 0);
   }

   public boolean canProvidePower(IBlockState iblockdata) {
      return true;
   }

   public void randomTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote && ((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         if (this.wooden) {
            this.checkPressed(iblockdata, world, blockposition);
         } else {
            org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 15, 0);
            world.getServer().getPluginManager().callEvent(eventRedstone);
            if (eventRedstone.getNewCurrent() > 0) {
               return;
            }

            world.setBlockState(blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(false)));
            this.notifyNeighbors(world, blockposition, (EnumFacing)iblockdata.getValue(FACING));
            this.playReleaseSound(world, blockposition);
            world.markBlockRangeForRenderUpdate(blockposition, blockposition);
         }
      }

   }

   public void onEntityCollidedWithBlock(World world, BlockPos blockposition, IBlockState iblockdata, Entity entity) {
      if (!world.isRemote && this.wooden && !((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         this.checkPressed(iblockdata, world, blockposition);
      }

   }

   private void checkPressed(IBlockState iblockdata, World world, BlockPos blockposition) {
      List list = world.getEntitiesWithinAABB(EntityArrow.class, iblockdata.getBoundingBox(world, blockposition).offset(blockposition));
      boolean flag = !list.isEmpty();
      boolean flag1 = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
      if (flag1 != flag && flag) {
         org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         boolean allowed = false;

         for(Object object : list) {
            if (object != null) {
               EntityInteractEvent event = new EntityInteractEvent(((Entity)object).getBukkitEntity(), block);
               world.getServer().getPluginManager().callEvent(event);
               if (!event.isCancelled()) {
                  allowed = true;
                  break;
               }
            }
         }

         if (!allowed) {
            return;
         }
      }

      if (flag && !flag1) {
         org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 0, 15);
         world.getServer().getPluginManager().callEvent(eventRedstone);
         if (eventRedstone.getNewCurrent() <= 0) {
            return;
         }

         world.setBlockState(blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(true)));
         this.notifyNeighbors(world, blockposition, (EnumFacing)iblockdata.getValue(FACING));
         world.markBlockRangeForRenderUpdate(blockposition, blockposition);
         this.playClickSound((EntityPlayer)null, world, blockposition);
      }

      if (!flag && flag1) {
         org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 15, 0);
         world.getServer().getPluginManager().callEvent(eventRedstone);
         if (eventRedstone.getNewCurrent() > 0) {
            return;
         }

         world.setBlockState(blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(false)));
         this.notifyNeighbors(world, blockposition, (EnumFacing)iblockdata.getValue(FACING));
         world.markBlockRangeForRenderUpdate(blockposition, blockposition);
         this.playReleaseSound(world, blockposition);
      }

      if (flag) {
         world.scheduleUpdate(new BlockPos(blockposition), this, this.tickRate(world));
      }

   }

   private void notifyNeighbors(World world, BlockPos blockposition, EnumFacing enumdirection) {
      world.notifyNeighborsOfStateChange(blockposition, this);
      world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection.getOpposite()), this);
   }

   public IBlockState getStateFromMeta(int i) {
      EnumFacing enumdirection;
      switch(i & 7) {
      case 0:
         enumdirection = EnumFacing.DOWN;
         break;
      case 1:
         enumdirection = EnumFacing.EAST;
         break;
      case 2:
         enumdirection = EnumFacing.WEST;
         break;
      case 3:
         enumdirection = EnumFacing.SOUTH;
         break;
      case 4:
         enumdirection = EnumFacing.NORTH;
         break;
      case 5:
      default:
         enumdirection = EnumFacing.UP;
      }

      return this.getDefaultState().withProperty(FACING, enumdirection).withProperty(POWERED, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      int i;
      switch(BlockButton.SyntheticClass_1.a[((EnumFacing)iblockdata.getValue(FACING)).ordinal()]) {
      case 1:
         i = 1;
         break;
      case 2:
         i = 2;
         break;
      case 3:
         i = 3;
         break;
      case 4:
         i = 4;
         break;
      case 5:
      default:
         i = 5;
         break;
      case 6:
         i = 0;
      }

      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      return iblockdata.withProperty(FACING, enumblockrotation.rotate((EnumFacing)iblockdata.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      return iblockdata.withRotation(enumblockmirror.toRotation((EnumFacing)iblockdata.getValue(FACING)));
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
