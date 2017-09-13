package net.minecraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockRailBase extends Block {
   protected static final AxisAlignedBB FLAT_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
   protected final boolean isPowered;

   public static boolean isRailBlock(World var0, BlockPos var1) {
      return isRailBlock(worldIn.getBlockState(pos));
   }

   public static boolean isRailBlock(IBlockState var0) {
      Block block = state.getBlock();
      return block instanceof BlockRailBase;
   }

   protected BlockRailBase(boolean var1) {
      super(Material.CIRCUITS);
      this.isPowered = isPowered;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getBlock() == this ? (BlockRailBase.EnumRailDirection)state.getValue(this.getShapeProperty()) : null;
      return blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending() ? FULL_BLOCK_AABB : FLAT_AABB;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!worldIn.isRemote) {
         state = this.updateDir(worldIn, pos, state, true);
         if (this.isPowered) {
            state.neighborChanged(worldIn, pos, this);
         }
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!worldIn.isRemote) {
         BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)state.getValue(this.getShapeProperty());
         boolean flag = false;
         if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
            flag = true;
         }

         if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_EAST && !worldIn.getBlockState(pos.east()).isSideSolid(worldIn, pos.east(), EnumFacing.UP)) {
            flag = true;
         } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_WEST && !worldIn.getBlockState(pos.west()).isSideSolid(worldIn, pos.west(), EnumFacing.UP)) {
            flag = true;
         } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_NORTH && !worldIn.getBlockState(pos.north()).isSideSolid(worldIn, pos.north(), EnumFacing.UP)) {
            flag = true;
         } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_SOUTH && !worldIn.getBlockState(pos.south()).isSideSolid(worldIn, pos.south(), EnumFacing.UP)) {
            flag = true;
         }

         if (flag && !worldIn.isAirBlock(pos)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
         } else {
            this.updateState(state, worldIn, pos, blockIn);
         }
      }

   }

   protected void updateState(IBlockState var1, World var2, BlockPos var3, Block var4) {
   }

   protected IBlockState updateDir(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return worldIn.isRemote ? state : (new BlockRailBase.Rail(worldIn, pos, state)).place(worldIn.isBlockPowered(pos), p_176564_4_).getBlockState();
   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.NORMAL;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(worldIn, pos, state);
      if (((BlockRailBase.EnumRailDirection)state.getValue(this.getShapeProperty())).isAscending()) {
         worldIn.notifyNeighborsOfStateChange(pos.up(), this);
      }

      if (this.isPowered) {
         worldIn.notifyNeighborsOfStateChange(pos, this);
         worldIn.notifyNeighborsOfStateChange(pos.down(), this);
      }

   }

   public abstract IProperty getShapeProperty();

   public boolean isFlexibleRail(IBlockAccess var1, BlockPos var2) {
      return !this.isPowered;
   }

   public boolean canMakeSlopes(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public BlockRailBase.EnumRailDirection getRailDirection(IBlockAccess var1, BlockPos var2, IBlockState var3, @Nullable EntityMinecart var4) {
      return (BlockRailBase.EnumRailDirection)state.getValue(this.getShapeProperty());
   }

   public float getRailMaxSpeed(World var1, EntityMinecart var2, BlockPos var3) {
      return 0.4F;
   }

   public void onMinecartPass(World var1, EntityMinecart var2, BlockPos var3) {
   }

   public boolean rotateBlock(World var1, BlockPos var2, EnumFacing var3) {
      IBlockState state = world.getBlockState(pos);
      UnmodifiableIterator var5 = state.getProperties().keySet().iterator();

      while(var5.hasNext()) {
         IProperty prop = (IProperty)var5.next();
         if (prop.getName().equals("shape")) {
            world.setBlockState(pos, state.cycleProperty(prop));
            return true;
         }
      }

      return false;
   }

   public static enum EnumRailDirection implements IStringSerializable {
      NORTH_SOUTH(0, "north_south"),
      EAST_WEST(1, "east_west"),
      ASCENDING_EAST(2, "ascending_east"),
      ASCENDING_WEST(3, "ascending_west"),
      ASCENDING_NORTH(4, "ascending_north"),
      ASCENDING_SOUTH(5, "ascending_south"),
      SOUTH_EAST(6, "south_east"),
      SOUTH_WEST(7, "south_west"),
      NORTH_WEST(8, "north_west"),
      NORTH_EAST(9, "north_east");

      private static final BlockRailBase.EnumRailDirection[] META_LOOKUP = new BlockRailBase.EnumRailDirection[values().length];
      private final int meta;
      private final String name;

      private EnumRailDirection(int var3, String var4) {
         this.meta = meta;
         this.name = name;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public boolean isAscending() {
         return this == ASCENDING_NORTH || this == ASCENDING_EAST || this == ASCENDING_SOUTH || this == ASCENDING_WEST;
      }

      public static BlockRailBase.EnumRailDirection byMetadata(int var0) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      public String getName() {
         return this.name;
      }

      static {
         for(BlockRailBase.EnumRailDirection blockrailbase$enumraildirection : values()) {
            META_LOOKUP[blockrailbase$enumraildirection.getMetadata()] = blockrailbase$enumraildirection;
         }

      }
   }

   public class Rail {
      private final World world;
      private final BlockPos pos;
      private final BlockRailBase block;
      private IBlockState state;
      private final boolean isPowered;
      private final List connectedRails = Lists.newArrayList();
      private final boolean canMakeSlopes;

      public Rail(World var2, BlockPos var3, IBlockState var4) {
         this.world = worldIn;
         this.pos = pos;
         this.state = state;
         this.block = (BlockRailBase)state.getBlock();
         BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = this.block.getRailDirection(worldIn, pos, state, (EntityMinecart)null);
         this.isPowered = !this.block.isFlexibleRail(worldIn, pos);
         this.canMakeSlopes = this.block.canMakeSlopes(worldIn, pos);
         this.updateConnectedRails(blockrailbase$enumraildirection);
      }

      public List getConnectedRails() {
         return this.connectedRails;
      }

      private void updateConnectedRails(BlockRailBase.EnumRailDirection var1) {
         this.connectedRails.clear();
         switch(railDirection) {
         case NORTH_SOUTH:
            this.connectedRails.add(this.pos.north());
            this.connectedRails.add(this.pos.south());
            break;
         case EAST_WEST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.east());
            break;
         case ASCENDING_EAST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.east().up());
            break;
         case ASCENDING_WEST:
            this.connectedRails.add(this.pos.west().up());
            this.connectedRails.add(this.pos.east());
            break;
         case ASCENDING_NORTH:
            this.connectedRails.add(this.pos.north().up());
            this.connectedRails.add(this.pos.south());
            break;
         case ASCENDING_SOUTH:
            this.connectedRails.add(this.pos.north());
            this.connectedRails.add(this.pos.south().up());
            break;
         case SOUTH_EAST:
            this.connectedRails.add(this.pos.east());
            this.connectedRails.add(this.pos.south());
            break;
         case SOUTH_WEST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.south());
            break;
         case NORTH_WEST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.north());
            break;
         case NORTH_EAST:
            this.connectedRails.add(this.pos.east());
            this.connectedRails.add(this.pos.north());
         }

      }

      private void removeSoftConnections() {
         for(int i = 0; i < this.connectedRails.size(); ++i) {
            BlockRailBase.Rail blockrailbase$rail = this.findRailAt((BlockPos)this.connectedRails.get(i));
            if (blockrailbase$rail != null && blockrailbase$rail.isConnectedToRail(this)) {
               this.connectedRails.set(i, blockrailbase$rail.pos);
            } else {
               this.connectedRails.remove(i--);
            }
         }

      }

      private boolean hasRailAt(BlockPos var1) {
         return BlockRailBase.isRailBlock(this.world, pos) || BlockRailBase.isRailBlock(this.world, pos.up()) || BlockRailBase.isRailBlock(this.world, pos.down());
      }

      @Nullable
      private BlockRailBase.Rail findRailAt(BlockPos var1) {
         IBlockState iblockstate = this.world.getBlockState(pos);
         if (BlockRailBase.isRailBlock(iblockstate)) {
            BlockRailBase var8 = BlockRailBase.this;
            BlockRailBase.this.getClass();
            return var8.new Rail(this.world, pos, iblockstate);
         } else {
            BlockPos lvt_2_1_ = pos.up();
            iblockstate = this.world.getBlockState(lvt_2_1_);
            if (BlockRailBase.isRailBlock(iblockstate)) {
               BlockRailBase var7 = BlockRailBase.this;
               BlockRailBase.this.getClass();
               return var7.new Rail(this.world, lvt_2_1_, iblockstate);
            } else {
               lvt_2_1_ = pos.down();
               iblockstate = this.world.getBlockState(lvt_2_1_);
               BlockRailBase.Rail var10000;
               if (BlockRailBase.isRailBlock(iblockstate)) {
                  BlockRailBase var10002 = BlockRailBase.this;
                  BlockRailBase.this.getClass();
                  var10000 = var10002.new Rail(this.world, lvt_2_1_, iblockstate);
               } else {
                  var10000 = null;
               }

               return var10000;
            }
         }
      }

      private boolean isConnectedToRail(BlockRailBase.Rail var1) {
         return this.isConnectedTo(rail.pos);
      }

      private boolean isConnectedTo(BlockPos var1) {
         for(int i = 0; i < this.connectedRails.size(); ++i) {
            BlockPos blockpos = (BlockPos)this.connectedRails.get(i);
            if (blockpos.getX() == posIn.getX() && blockpos.getZ() == posIn.getZ()) {
               return true;
            }
         }

         return false;
      }

      protected int countAdjacentRails() {
         int i = 0;

         for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (this.hasRailAt(this.pos.offset(enumfacing))) {
               ++i;
            }
         }

         return i;
      }

      private boolean canConnectTo(BlockRailBase.Rail var1) {
         return this.isConnectedToRail(rail) || this.connectedRails.size() != 2;
      }

      private void connectTo(BlockRailBase.Rail var1) {
         this.connectedRails.add(p_150645_1_.pos);
         BlockPos blockpos = this.pos.north();
         BlockPos blockpos1 = this.pos.south();
         BlockPos blockpos2 = this.pos.west();
         BlockPos blockpos3 = this.pos.east();
         boolean flag = this.isConnectedTo(blockpos);
         boolean flag1 = this.isConnectedTo(blockpos1);
         boolean flag2 = this.isConnectedTo(blockpos2);
         boolean flag3 = this.isConnectedTo(blockpos3);
         BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;
         if (flag || flag1) {
            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         if (flag2 || flag3) {
            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
         }

         if (!this.isPowered) {
            if (flag1 && flag3 && !flag && !flag2) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
            }

            if (flag1 && flag2 && !flag && !flag3) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
            }

            if (flag && flag2 && !flag1 && !flag3) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
            }

            if (flag && flag3 && !flag1 && !flag2) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
            }
         }

         if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH && this.canMakeSlopes) {
            if (BlockRailBase.isRailBlock(this.world, blockpos.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
            }

            if (BlockRailBase.isRailBlock(this.world, blockpos1.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
            }
         }

         if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST && this.canMakeSlopes) {
            if (BlockRailBase.isRailBlock(this.world, blockpos3.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
            }

            if (BlockRailBase.isRailBlock(this.world, blockpos2.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
            }
         }

         if (blockrailbase$enumraildirection == null) {
            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);
         this.world.setBlockState(this.pos, this.state, 3);
      }

      private boolean hasNeighborRail(BlockPos var1) {
         BlockRailBase.Rail blockrailbase$rail = this.findRailAt(p_180361_1_);
         if (blockrailbase$rail == null) {
            return false;
         } else {
            blockrailbase$rail.removeSoftConnections();
            return blockrailbase$rail.canConnectTo(this);
         }
      }

      public BlockRailBase.Rail place(boolean var1, boolean var2) {
         BlockPos blockpos = this.pos.north();
         BlockPos blockpos1 = this.pos.south();
         BlockPos blockpos2 = this.pos.west();
         BlockPos blockpos3 = this.pos.east();
         boolean flag = this.hasNeighborRail(blockpos);
         boolean flag1 = this.hasNeighborRail(blockpos1);
         boolean flag2 = this.hasNeighborRail(blockpos2);
         boolean flag3 = this.hasNeighborRail(blockpos3);
         BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;
         if ((flag || flag1) && !flag2 && !flag3) {
            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         if ((flag2 || flag3) && !flag && !flag1) {
            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
         }

         if (!this.isPowered) {
            if (flag1 && flag3 && !flag && !flag2) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
            }

            if (flag1 && flag2 && !flag && !flag3) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
            }

            if (flag && flag2 && !flag1 && !flag3) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
            }

            if (flag && flag3 && !flag1 && !flag2) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
            }
         }

         if (blockrailbase$enumraildirection == null) {
            if (flag || flag1) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if (flag2 || flag3) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!this.isPowered) {
               if (p_180364_1_) {
                  if (flag1 && flag3) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                  }

                  if (flag2 && flag1) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                  }

                  if (flag3 && flag) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                  }

                  if (flag && flag2) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                  }
               } else {
                  if (flag && flag2) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                  }

                  if (flag3 && flag) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                  }

                  if (flag2 && flag1) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                  }

                  if (flag1 && flag3) {
                     blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                  }
               }
            }
         }

         if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH && this.canMakeSlopes) {
            if (BlockRailBase.isRailBlock(this.world, blockpos.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
            }

            if (BlockRailBase.isRailBlock(this.world, blockpos1.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
            }
         }

         if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST && this.canMakeSlopes) {
            if (BlockRailBase.isRailBlock(this.world, blockpos3.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
            }

            if (BlockRailBase.isRailBlock(this.world, blockpos2.up())) {
               blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
            }
         }

         if (blockrailbase$enumraildirection == null) {
            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         this.updateConnectedRails(blockrailbase$enumraildirection);
         this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);
         if (p_180364_2_ || this.world.getBlockState(this.pos) != this.state) {
            this.world.setBlockState(this.pos, this.state, 3);

            for(int i = 0; i < this.connectedRails.size(); ++i) {
               BlockRailBase.Rail blockrailbase$rail = this.findRailAt((BlockPos)this.connectedRails.get(i));
               if (blockrailbase$rail != null) {
                  blockrailbase$rail.removeSoftConnections();
                  if (blockrailbase$rail.canConnectTo(this)) {
                     blockrailbase$rail.connectTo(this);
                  }
               }
            }
         }

         return this;
      }

      public IBlockState getBlockState() {
         return this.state;
      }
   }
}
