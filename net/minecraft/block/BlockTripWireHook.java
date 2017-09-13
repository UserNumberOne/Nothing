package net.minecraft.block;

import com.google.common.base.Objects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockTripWireHook extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   public static final PropertyBool ATTACHED = PropertyBool.create("attached");
   protected static final AxisAlignedBB HOOK_NORTH_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.625D, 0.6875D, 0.625D, 1.0D);
   protected static final AxisAlignedBB HOOK_SOUTH_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.0D, 0.6875D, 0.625D, 0.375D);
   protected static final AxisAlignedBB HOOK_WEST_AABB = new AxisAlignedBB(0.625D, 0.0D, 0.3125D, 1.0D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB HOOK_EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.3125D, 0.375D, 0.625D, 0.6875D);

   public BlockTripWireHook() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      switch(BlockTripWireHook.SyntheticClass_1.a[((EnumFacing)iblockdata.getValue(FACING)).ordinal()]) {
      case 1:
      default:
         return HOOK_EAST_AABB;
      case 2:
         return HOOK_WEST_AABB;
      case 3:
         return HOOK_SOUTH_AABB;
      case 4:
         return HOOK_NORTH_AABB;
      }
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

   public boolean canPlaceBlockOnSide(World world, BlockPos blockposition, EnumFacing enumdirection) {
      return enumdirection.getAxis().isHorizontal() && world.getBlockState(blockposition.offset(enumdirection.getOpposite())).isNormalCube();
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
         if (world.getBlockState(blockposition.offset(enumdirection)).isNormalCube()) {
            return true;
         }
      }

      return false;
   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      IBlockState iblockdata = this.getDefaultState().withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false));
      if (enumdirection.getAxis().isHorizontal()) {
         iblockdata = iblockdata.withProperty(FACING, enumdirection);
      }

      return iblockdata;
   }

   public void onBlockPlacedBy(World world, BlockPos blockposition, IBlockState iblockdata, EntityLivingBase entityliving, ItemStack itemstack) {
      this.calculateState(world, blockposition, iblockdata, false, false, -1, (IBlockState)null);
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (block != this && this.checkForDrop(world, blockposition, iblockdata)) {
         EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
         if (!world.getBlockState(blockposition.offset(enumdirection.getOpposite())).isNormalCube()) {
            this.dropBlockAsItem(world, blockposition, iblockdata, 0);
            world.setBlockToAir(blockposition);
         }
      }

   }

   public void calculateState(World world, BlockPos blockposition, IBlockState iblockdata, boolean flag, boolean flag1, int i, @Nullable IBlockState iblockdata1) {
      EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
      boolean flag2 = ((Boolean)iblockdata.getValue(ATTACHED)).booleanValue();
      boolean flag3 = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
      boolean flag4 = !flag;
      boolean flag5 = false;
      int j = 0;
      IBlockState[] aiblockdata = new IBlockState[42];

      for(int k = 1; k < 42; ++k) {
         BlockPos blockposition1 = blockposition.offset(enumdirection, k);
         IBlockState iblockdata2 = world.getBlockState(blockposition1);
         if (iblockdata2.getBlock() == Blocks.TRIPWIRE_HOOK) {
            if (iblockdata2.getValue(FACING) == enumdirection.getOpposite()) {
               j = k;
            }
            break;
         }

         if (iblockdata2.getBlock() != Blocks.TRIPWIRE && k != i) {
            aiblockdata[k] = null;
            flag4 = false;
         } else {
            if (k == i) {
               iblockdata2 = (IBlockState)Objects.firstNonNull(iblockdata1, iblockdata2);
            }

            boolean flag6 = !((Boolean)iblockdata2.getValue(BlockTripWire.DISARMED)).booleanValue();
            boolean flag7 = ((Boolean)iblockdata2.getValue(BlockTripWire.POWERED)).booleanValue();
            flag5 |= flag6 && flag7;
            aiblockdata[k] = iblockdata2;
            if (k == i) {
               world.scheduleUpdate(blockposition, this, this.tickRate(world));
               flag4 &= flag6;
            }
         }
      }

      flag4 = flag4 & j > 1;
      flag5 = flag5 & flag4;
      IBlockState iblockdata3 = this.getDefaultState().withProperty(ATTACHED, Boolean.valueOf(flag4)).withProperty(POWERED, Boolean.valueOf(flag5));
      if (j > 0) {
         BlockPos blockposition1 = blockposition.offset(enumdirection, j);
         EnumFacing enumdirection1 = enumdirection.getOpposite();
         world.setBlockState(blockposition1, iblockdata3.withProperty(FACING, enumdirection1), 3);
         this.notifyNeighbors(world, blockposition1, enumdirection1);
         this.playSound(world, blockposition1, flag4, flag5, flag2, flag3);
      }

      org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
      BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 15, 0);
      world.getServer().getPluginManager().callEvent(eventRedstone);
      if (eventRedstone.getNewCurrent() <= 0) {
         this.playSound(world, blockposition, flag4, flag5, flag2, flag3);
         if (!flag) {
            world.setBlockState(blockposition, iblockdata3.withProperty(FACING, enumdirection), 3);
            if (flag1) {
               this.notifyNeighbors(world, blockposition, enumdirection);
            }
         }

         if (flag2 != flag4) {
            for(int l = 1; l < j; ++l) {
               BlockPos blockposition2 = blockposition.offset(enumdirection, l);
               IBlockState iblockdata4 = aiblockdata[l];
               if (iblockdata4 != null && world.getBlockState(blockposition2).getMaterial() != Material.AIR) {
                  world.setBlockState(blockposition2, iblockdata4.withProperty(ATTACHED, Boolean.valueOf(flag4)), 3);
               }
            }
         }

      }
   }

   public void randomTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      this.calculateState(world, blockposition, iblockdata, false, true, -1, (IBlockState)null);
   }

   private void playSound(World world, BlockPos blockposition, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      if (flag1 && !flag3) {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.4F, 0.6F);
      } else if (!flag1 && flag3) {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 0.5F);
      } else if (flag && !flag2) {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.4F, 0.7F);
      } else if (!flag && flag2) {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.4F, 1.2F / (world.rand.nextFloat() * 0.2F + 0.9F));
      }

   }

   private void notifyNeighbors(World world, BlockPos blockposition, EnumFacing enumdirection) {
      world.notifyNeighborsOfStateChange(blockposition, this);
      world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection.getOpposite()), this);
   }

   private boolean checkForDrop(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (!this.canPlaceBlockAt(world, blockposition)) {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
         return false;
      } else {
         return true;
      }
   }

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      boolean flag = ((Boolean)iblockdata.getValue(ATTACHED)).booleanValue();
      boolean flag1 = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
      if (flag || flag1) {
         this.calculateState(world, blockposition, iblockdata, true, false, -1, (IBlockState)null);
      }

      if (flag1) {
         world.notifyNeighborsOfStateChange(blockposition, this);
         world.notifyNeighborsOfStateChange(blockposition.offset(((EnumFacing)iblockdata.getValue(FACING)).getOpposite()), this);
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

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(i & 3)).withProperty(POWERED, Boolean.valueOf((i & 8) > 0)).withProperty(ATTACHED, Boolean.valueOf((i & 4) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i = b0 | ((EnumFacing)iblockdata.getValue(FACING)).getHorizontalIndex();
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      if (((Boolean)iblockdata.getValue(ATTACHED)).booleanValue()) {
         i |= 4;
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
      return new BlockStateContainer(this, new IProperty[]{FACING, POWERED, ATTACHED});
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.EAST.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 4;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
