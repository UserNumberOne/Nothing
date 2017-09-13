package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRedstoneComparator extends BlockRedstoneDiode implements ITileEntityProvider {
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   public static final PropertyEnum MODE = PropertyEnum.create("mode", BlockRedstoneComparator.Mode.class);

   public BlockRedstoneComparator(boolean var1) {
      super(powered);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.valueOf(false)).withProperty(MODE, BlockRedstoneComparator.Mode.COMPARE));
      this.isBlockContainer = true;
   }

   public String getLocalizedName() {
      return I18n.translateToLocal("item.comparator.name");
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.COMPARATOR;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.COMPARATOR);
   }

   protected int getDelay(IBlockState var1) {
      return 2;
   }

   protected IBlockState getPoweredState(IBlockState var1) {
      Boolean obool = (Boolean)unpoweredState.getValue(POWERED);
      BlockRedstoneComparator.Mode blockredstonecomparator$mode = (BlockRedstoneComparator.Mode)unpoweredState.getValue(MODE);
      EnumFacing enumfacing = (EnumFacing)unpoweredState.getValue(FACING);
      return Blocks.POWERED_COMPARATOR.getDefaultState().withProperty(FACING, enumfacing).withProperty(POWERED, obool).withProperty(MODE, blockredstonecomparator$mode);
   }

   protected IBlockState getUnpoweredState(IBlockState var1) {
      Boolean obool = (Boolean)poweredState.getValue(POWERED);
      BlockRedstoneComparator.Mode blockredstonecomparator$mode = (BlockRedstoneComparator.Mode)poweredState.getValue(MODE);
      EnumFacing enumfacing = (EnumFacing)poweredState.getValue(FACING);
      return Blocks.UNPOWERED_COMPARATOR.getDefaultState().withProperty(FACING, enumfacing).withProperty(POWERED, obool).withProperty(MODE, blockredstonecomparator$mode);
   }

   protected boolean isPowered(IBlockState var1) {
      return this.isRepeaterPowered || ((Boolean)state.getValue(POWERED)).booleanValue();
   }

   protected int getActiveSignal(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      return tileentity instanceof TileEntityComparator ? ((TileEntityComparator)tileentity).getOutputSignal() : 0;
   }

   private int calculateOutput(World var1, BlockPos var2, IBlockState var3) {
      return state.getValue(MODE) == BlockRedstoneComparator.Mode.SUBTRACT ? Math.max(this.calculateInputStrength(worldIn, pos, state) - this.getPowerOnSides(worldIn, pos, state), 0) : this.calculateInputStrength(worldIn, pos, state);
   }

   protected boolean shouldBePowered(World var1, BlockPos var2, IBlockState var3) {
      int i = this.calculateInputStrength(worldIn, pos, state);
      if (i >= 15) {
         return true;
      } else if (i == 0) {
         return false;
      } else {
         int j = this.getPowerOnSides(worldIn, pos, state);
         return j == 0 ? true : i >= j;
      }
   }

   protected int calculateInputStrength(World var1, BlockPos var2, IBlockState var3) {
      int i = super.calculateInputStrength(worldIn, pos, state);
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      BlockPos blockpos = pos.offset(enumfacing);
      IBlockState iblockstate = worldIn.getBlockState(blockpos);
      if (iblockstate.hasComparatorInputOverride()) {
         i = iblockstate.getComparatorInputOverride(worldIn, blockpos);
      } else if (i < 15 && iblockstate.isNormalCube()) {
         blockpos = blockpos.offset(enumfacing);
         iblockstate = worldIn.getBlockState(blockpos);
         if (iblockstate.hasComparatorInputOverride()) {
            i = iblockstate.getComparatorInputOverride(worldIn, blockpos);
         } else if (iblockstate.getMaterial() == Material.AIR) {
            EntityItemFrame entityitemframe = this.findItemFrame(worldIn, enumfacing, blockpos);
            if (entityitemframe != null) {
               i = entityitemframe.getAnalogOutput();
            }
         }
      }

      return i;
   }

   @Nullable
   private EntityItemFrame findItemFrame(World var1, final EnumFacing var2, BlockPos var3) {
      List list = worldIn.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1)), new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            return p_apply_1_ != null && p_apply_1_.getHorizontalFacing() == facing;
         }
      });
      return list.size() == 1 ? (EntityItemFrame)list.get(0) : null;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (!playerIn.capabilities.allowEdit) {
         return false;
      } else {
         state = state.cycleProperty(MODE);
         float f = state.getValue(MODE) == BlockRedstoneComparator.Mode.SUBTRACT ? 0.55F : 0.5F;
         worldIn.playSound(playerIn, pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3F, f);
         worldIn.setBlockState(pos, state, 2);
         this.onStateChange(worldIn, pos, state);
         return true;
      }
   }

   protected void updateState(World var1, BlockPos var2, IBlockState var3) {
      if (!worldIn.isBlockTickPending(pos, this)) {
         int i = this.calculateOutput(worldIn, pos, state);
         TileEntity tileentity = worldIn.getTileEntity(pos);
         int j = tileentity instanceof TileEntityComparator ? ((TileEntityComparator)tileentity).getOutputSignal() : 0;
         if (i != j || this.isPowered(state) != this.shouldBePowered(worldIn, pos, state)) {
            if (this.isFacingTowardsRepeater(worldIn, pos, state)) {
               worldIn.updateBlockTick(pos, this, 2, -1);
            } else {
               worldIn.updateBlockTick(pos, this, 2, 0);
            }
         }
      }

   }

   private void onStateChange(World var1, BlockPos var2, IBlockState var3) {
      int i = this.calculateOutput(worldIn, pos, state);
      TileEntity tileentity = worldIn.getTileEntity(pos);
      int j = 0;
      if (tileentity instanceof TileEntityComparator) {
         TileEntityComparator tileentitycomparator = (TileEntityComparator)tileentity;
         j = tileentitycomparator.getOutputSignal();
         tileentitycomparator.setOutputSignal(i);
      }

      if (j != i || state.getValue(MODE) == BlockRedstoneComparator.Mode.COMPARE) {
         boolean flag1 = this.shouldBePowered(worldIn, pos, state);
         boolean flag = this.isPowered(state);
         if (flag && !flag1) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(false)), 2);
         } else if (!flag && flag1) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(true)), 2);
         }

         this.notifyNeighbors(worldIn, pos, state);
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (this.isRepeaterPowered) {
         worldIn.setBlockState(pos, this.getUnpoweredState(state).withProperty(POWERED, Boolean.valueOf(true)), 4);
      }

      this.onStateChange(worldIn, pos, state);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      super.onBlockAdded(worldIn, pos, state);
      worldIn.setTileEntity(pos, this.createNewTileEntity(worldIn, 0));
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(worldIn, pos, state);
      worldIn.removeTileEntity(pos);
      this.notifyNeighbors(worldIn, pos, state);
   }

   public boolean eventReceived(IBlockState var1, World var2, BlockPos var3, int var4, int var5) {
      super.eventReceived(state, worldIn, pos, id, param);
      TileEntity tileentity = worldIn.getTileEntity(pos);
      return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityComparator();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta)).withProperty(POWERED, Boolean.valueOf((meta & 8) > 0)).withProperty(MODE, (meta & 4) > 0 ? BlockRedstoneComparator.Mode.SUBTRACT : BlockRedstoneComparator.Mode.COMPARE);
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
      if (((Boolean)state.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      if (state.getValue(MODE) == BlockRedstoneComparator.Mode.SUBTRACT) {
         i |= 4;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, MODE, POWERED});
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(POWERED, Boolean.valueOf(false)).withProperty(MODE, BlockRedstoneComparator.Mode.COMPARE);
   }

   public void onNeighborChange(IBlockAccess var1, BlockPos var2, BlockPos var3) {
      if (pos.getY() == neighbor.getY() && world instanceof World) {
         this.neighborChanged(world.getBlockState(pos), (World)world, pos, world.getBlockState(neighbor).getBlock());
      }

   }

   public boolean getWeakChanges(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public static enum Mode implements IStringSerializable {
      COMPARE("compare"),
      SUBTRACT("subtract");

      private final String name;

      private Mode(String var3) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
