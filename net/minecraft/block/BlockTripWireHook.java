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

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch(BlockTripWireHook.SyntheticClass_1.a[((EnumFacing)var1.getValue(FACING)).ordinal()]) {
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
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return var3.getAxis().isHorizontal() && var1.getBlockState(var2.offset(var3.getOpposite())).isNormalCube();
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      for(EnumFacing var4 : EnumFacing.Plane.HORIZONTAL) {
         if (var1.getBlockState(var2.offset(var4)).isNormalCube()) {
            return true;
         }
      }

      return false;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = this.getDefaultState().withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false));
      if (var3.getAxis().isHorizontal()) {
         var9 = var9.withProperty(FACING, var3);
      }

      return var9;
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      this.calculateState(var1, var2, var3, false, false, -1, (IBlockState)null);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (var4 != this && this.checkForDrop(var2, var3, var1)) {
         EnumFacing var5 = (EnumFacing)var1.getValue(FACING);
         if (!var2.getBlockState(var3.offset(var5.getOpposite())).isNormalCube()) {
            this.dropBlockAsItem(var2, var3, var1, 0);
            var2.setBlockToAir(var3);
         }
      }

   }

   public void calculateState(World var1, BlockPos var2, IBlockState var3, boolean var4, boolean var5, int var6, @Nullable IBlockState var7) {
      EnumFacing var8 = (EnumFacing)var3.getValue(FACING);
      boolean var9 = ((Boolean)var3.getValue(ATTACHED)).booleanValue();
      boolean var10 = ((Boolean)var3.getValue(POWERED)).booleanValue();
      boolean var11 = !var4;
      boolean var12 = false;
      int var13 = 0;
      IBlockState[] var14 = new IBlockState[42];

      for(int var15 = 1; var15 < 42; ++var15) {
         BlockPos var16 = var2.offset(var8, var15);
         IBlockState var17 = var1.getBlockState(var16);
         if (var17.getBlock() == Blocks.TRIPWIRE_HOOK) {
            if (var17.getValue(FACING) == var8.getOpposite()) {
               var13 = var15;
            }
            break;
         }

         if (var17.getBlock() != Blocks.TRIPWIRE && var15 != var6) {
            var14[var15] = null;
            var11 = false;
         } else {
            if (var15 == var6) {
               var17 = (IBlockState)Objects.firstNonNull(var7, var17);
            }

            boolean var18 = !((Boolean)var17.getValue(BlockTripWire.DISARMED)).booleanValue();
            boolean var19 = ((Boolean)var17.getValue(BlockTripWire.POWERED)).booleanValue();
            var12 |= var18 && var19;
            var14[var15] = var17;
            if (var15 == var6) {
               var1.scheduleUpdate(var2, this, this.tickRate(var1));
               var11 &= var18;
            }
         }
      }

      var11 = var11 & var13 > 1;
      var12 = var12 & var11;
      IBlockState var24 = this.getDefaultState().withProperty(ATTACHED, Boolean.valueOf(var11)).withProperty(POWERED, Boolean.valueOf(var12));
      if (var13 > 0) {
         BlockPos var25 = var2.offset(var8, var13);
         EnumFacing var26 = var8.getOpposite();
         var1.setBlockState(var25, var24.withProperty(FACING, var26), 3);
         this.notifyNeighbors(var1, var25, var26);
         this.playSound(var1, var25, var11, var12, var9, var10);
      }

      org.bukkit.block.Block var27 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
      BlockRedstoneEvent var28 = new BlockRedstoneEvent(var27, 15, 0);
      var1.getServer().getPluginManager().callEvent(var28);
      if (var28.getNewCurrent() <= 0) {
         this.playSound(var1, var2, var11, var12, var9, var10);
         if (!var4) {
            var1.setBlockState(var2, var24.withProperty(FACING, var8), 3);
            if (var5) {
               this.notifyNeighbors(var1, var2, var8);
            }
         }

         if (var9 != var11) {
            for(int var29 = 1; var29 < var13; ++var29) {
               BlockPos var20 = var2.offset(var8, var29);
               IBlockState var21 = var14[var29];
               if (var21 != null && var1.getBlockState(var20).getMaterial() != Material.AIR) {
                  var1.setBlockState(var20, var21.withProperty(ATTACHED, Boolean.valueOf(var11)), 3);
               }
            }
         }

      }
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.calculateState(var1, var2, var3, false, true, -1, (IBlockState)null);
   }

   private void playSound(World var1, BlockPos var2, boolean var3, boolean var4, boolean var5, boolean var6) {
      if (var4 && !var6) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.4F, 0.6F);
      } else if (!var4 && var6) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 0.5F);
      } else if (var3 && !var5) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.4F, 0.7F);
      } else if (!var3 && var5) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.4F, 1.2F / (var1.rand.nextFloat() * 0.2F + 0.9F));
      }

   }

   private void notifyNeighbors(World var1, BlockPos var2, EnumFacing var3) {
      var1.notifyNeighborsOfStateChange(var2, this);
      var1.notifyNeighborsOfStateChange(var2.offset(var3.getOpposite()), this);
   }

   private boolean checkForDrop(World var1, BlockPos var2, IBlockState var3) {
      if (!this.canPlaceBlockAt(var1, var2)) {
         this.dropBlockAsItem(var1, var2, var3, 0);
         var1.setBlockToAir(var2);
         return false;
      } else {
         return true;
      }
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      boolean var4 = ((Boolean)var3.getValue(ATTACHED)).booleanValue();
      boolean var5 = ((Boolean)var3.getValue(POWERED)).booleanValue();
      if (var4 || var5) {
         this.calculateState(var1, var2, var3, true, false, -1, (IBlockState)null);
      }

      if (var5) {
         var1.notifyNeighborsOfStateChange(var2, this);
         var1.notifyNeighborsOfStateChange(var2.offset(((EnumFacing)var3.getValue(FACING)).getOpposite()), this);
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

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(var1 & 3)).withProperty(POWERED, Boolean.valueOf((var1 & 8) > 0)).withProperty(ATTACHED, Boolean.valueOf((var1 & 4) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      byte var2 = 0;
      int var3 = var2 | ((EnumFacing)var1.getValue(FACING)).getHorizontalIndex();
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var3 |= 8;
      }

      if (((Boolean)var1.getValue(ATTACHED)).booleanValue()) {
         var3 |= 4;
      }

      return var3;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
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
