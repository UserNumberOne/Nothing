package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockVine extends Block {
   public static final PropertyBool UP = PropertyBool.create("up");
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   public static final PropertyBool[] ALL_FACES = new PropertyBool[]{UP, NORTH, SOUTH, WEST, EAST};
   protected static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0D, 0.9375D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0625D, 1.0D, 1.0D);
   protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.9375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.0625D);
   protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.9375D, 1.0D, 1.0D, 1.0D);

   public BlockVine() {
      super(Material.VINE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(UP, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      iblockdata = iblockdata.getActualState(iblockaccess, blockposition);
      int i = 0;
      AxisAlignedBB axisalignedbb = FULL_BLOCK_AABB;
      if (((Boolean)iblockdata.getValue(UP)).booleanValue()) {
         axisalignedbb = UP_AABB;
         ++i;
      }

      if (((Boolean)iblockdata.getValue(NORTH)).booleanValue()) {
         axisalignedbb = NORTH_AABB;
         ++i;
      }

      if (((Boolean)iblockdata.getValue(EAST)).booleanValue()) {
         axisalignedbb = EAST_AABB;
         ++i;
      }

      if (((Boolean)iblockdata.getValue(SOUTH)).booleanValue()) {
         axisalignedbb = SOUTH_AABB;
         ++i;
      }

      if (((Boolean)iblockdata.getValue(WEST)).booleanValue()) {
         axisalignedbb = WEST_AABB;
         ++i;
      }

      return i == 1 ? axisalignedbb : FULL_BLOCK_AABB;
   }

   public IBlockState getActualState(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return iblockdata.withProperty(UP, Boolean.valueOf(iblockaccess.getBlockState(blockposition.up()).isBlockNormalCube()));
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isReplaceable(IBlockAccess iblockaccess, BlockPos blockposition) {
      return true;
   }

   public boolean canPlaceBlockOnSide(World world, BlockPos blockposition, EnumFacing enumdirection) {
      switch(BlockVine.SyntheticClass_1.a[enumdirection.ordinal()]) {
      case 1:
         return this.canAttachVineOn(world.getBlockState(blockposition.up()));
      case 2:
      case 3:
      case 4:
      case 5:
         return this.canAttachVineOn(world.getBlockState(blockposition.offset(enumdirection.getOpposite())));
      default:
         return false;
      }
   }

   private boolean canAttachVineOn(IBlockState iblockdata) {
      return iblockdata.isFullCube() && iblockdata.getMaterial().blocksMovement();
   }

   private boolean recheckGrownSides(World world, BlockPos blockposition, IBlockState iblockdata) {
      IBlockState iblockdata1 = iblockdata;

      for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
         PropertyBool blockstateboolean = getPropertyFor(enumdirection);
         if (((Boolean)iblockdata.getValue(blockstateboolean)).booleanValue() && !this.canAttachVineOn(world.getBlockState(blockposition.offset(enumdirection)))) {
            IBlockState iblockdata2 = world.getBlockState(blockposition.up());
            if (iblockdata2.getBlock() != this || !((Boolean)iblockdata2.getValue(blockstateboolean)).booleanValue()) {
               iblockdata = iblockdata.withProperty(blockstateboolean, Boolean.valueOf(false));
            }
         }
      }

      if (getNumGrownFaces(iblockdata) == 0) {
         return false;
      } else {
         if (iblockdata1 != iblockdata) {
            world.setBlockState(blockposition, iblockdata, 2);
         }

         return true;
      }
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!world.isRemote && !this.recheckGrownSides(world, blockposition, iblockdata)) {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
      }

   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote && world.rand.nextInt(4) == 0) {
         int i = 5;
         boolean flag1 = false;

         label177:
         for(int j = -4; j <= 4; ++j) {
            for(int k = -4; k <= 4; ++k) {
               for(int l = -1; l <= 1; ++l) {
                  if (world.getBlockState(blockposition.add(j, l, k)).getBlock() == this) {
                     --i;
                     if (i <= 0) {
                        flag1 = true;
                        break label177;
                     }
                  }
               }
            }
         }

         EnumFacing enumdirection = EnumFacing.random(random);
         BlockPos blockposition1 = blockposition.up();
         if (enumdirection == EnumFacing.UP && blockposition.getY() < 255 && world.isAirBlock(blockposition1)) {
            if (!flag1) {
               IBlockState iblockdata1 = iblockdata;

               for(EnumFacing enumdirection1 : EnumFacing.Plane.HORIZONTAL) {
                  if (random.nextBoolean() || !this.canAttachVineOn(world.getBlockState(blockposition1.offset(enumdirection1)))) {
                     iblockdata1 = iblockdata1.withProperty(getPropertyFor(enumdirection1), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)iblockdata1.getValue(NORTH)).booleanValue() || ((Boolean)iblockdata1.getValue(EAST)).booleanValue() || ((Boolean)iblockdata1.getValue(SOUTH)).booleanValue() || ((Boolean)iblockdata1.getValue(WEST)).booleanValue()) {
                  org.bukkit.block.Block source = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                  org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ());
                  CraftEventFactory.handleBlockSpreadEvent(block, source, this, this.getMetaFromState(iblockdata1));
               }
            }
         } else if (enumdirection.getAxis().isHorizontal() && !((Boolean)iblockdata.getValue(getPropertyFor(enumdirection))).booleanValue()) {
            if (!flag1) {
               BlockPos blockposition2 = blockposition.offset(enumdirection);
               IBlockState iblockdata2 = world.getBlockState(blockposition2);
               Block block = iblockdata2.getBlock();
               if (block.blockMaterial == Material.AIR) {
                  EnumFacing enumdirection2 = enumdirection.rotateY();
                  EnumFacing enumdirection3 = enumdirection.rotateYCCW();
                  boolean flag2 = ((Boolean)iblockdata.getValue(getPropertyFor(enumdirection2))).booleanValue();
                  boolean flag3 = ((Boolean)iblockdata.getValue(getPropertyFor(enumdirection3))).booleanValue();
                  BlockPos blockposition3 = blockposition2.offset(enumdirection2);
                  BlockPos blockposition4 = blockposition2.offset(enumdirection3);
                  org.bukkit.block.Block source = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                  org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ());
                  if (flag2 && this.canAttachVineOn(world.getBlockState(blockposition3))) {
                     CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(enumdirection2), Boolean.valueOf(true))));
                  } else if (flag3 && this.canAttachVineOn(world.getBlockState(blockposition4))) {
                     CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(enumdirection3), Boolean.valueOf(true))));
                  } else if (flag2 && world.isAirBlock(blockposition3) && this.canAttachVineOn(world.getBlockState(blockposition.offset(enumdirection2)))) {
                     bukkitBlock = world.getWorld().getBlockAt(blockposition3.getX(), blockposition3.getY(), blockposition3.getZ());
                     CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(enumdirection.getOpposite()), Boolean.valueOf(true))));
                  } else if (flag3 && world.isAirBlock(blockposition4) && this.canAttachVineOn(world.getBlockState(blockposition.offset(enumdirection3)))) {
                     bukkitBlock = world.getWorld().getBlockAt(blockposition4.getX(), blockposition4.getY(), blockposition4.getZ());
                     CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(enumdirection.getOpposite()), Boolean.valueOf(true))));
                  } else if (this.canAttachVineOn(world.getBlockState(blockposition2.up()))) {
                     CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, this.getMetaFromState(this.getDefaultState()));
                  }
               } else if (block.blockMaterial.isOpaque() && iblockdata2.isFullCube()) {
                  world.setBlockState(blockposition, iblockdata.withProperty(getPropertyFor(enumdirection), Boolean.valueOf(true)), 2);
               }
            }
         } else if (blockposition.getY() > 1) {
            BlockPos blockposition2 = blockposition.down();
            IBlockState iblockdata2 = world.getBlockState(blockposition2);
            Block block = iblockdata2.getBlock();
            if (block.blockMaterial == Material.AIR) {
               IBlockState iblockdata3 = iblockdata;

               for(EnumFacing enumdirection4 : EnumFacing.Plane.HORIZONTAL) {
                  if (random.nextBoolean()) {
                     iblockdata3 = iblockdata3.withProperty(getPropertyFor(enumdirection4), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)iblockdata3.getValue(NORTH)).booleanValue() || ((Boolean)iblockdata3.getValue(EAST)).booleanValue() || ((Boolean)iblockdata3.getValue(SOUTH)).booleanValue() || ((Boolean)iblockdata3.getValue(WEST)).booleanValue()) {
                  org.bukkit.block.Block source = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                  org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ());
                  CraftEventFactory.handleBlockSpreadEvent(bukkitBlock, source, this, this.getMetaFromState(iblockdata3));
               }
            } else if (block == this) {
               IBlockState iblockdata3 = iblockdata2;

               for(EnumFacing enumdirection4 : EnumFacing.Plane.HORIZONTAL) {
                  PropertyBool blockstateboolean = getPropertyFor(enumdirection4);
                  if (random.nextBoolean() && ((Boolean)iblockdata.getValue(blockstateboolean)).booleanValue()) {
                     iblockdata3 = iblockdata3.withProperty(blockstateboolean, Boolean.valueOf(true));
                  }
               }

               if (((Boolean)iblockdata3.getValue(NORTH)).booleanValue() || ((Boolean)iblockdata3.getValue(EAST)).booleanValue() || ((Boolean)iblockdata3.getValue(SOUTH)).booleanValue() || ((Boolean)iblockdata3.getValue(WEST)).booleanValue()) {
                  world.setBlockState(blockposition2, iblockdata3, 2);
               }
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      IBlockState iblockdata = this.getDefaultState().withProperty(UP, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false));
      return enumdirection.getAxis().isHorizontal() ? iblockdata.withProperty(getPropertyFor(enumdirection.getOpposite()), Boolean.valueOf(true)) : iblockdata;
   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return null;
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public void harvestBlock(World world, EntityPlayer entityhuman, BlockPos blockposition, IBlockState iblockdata, @Nullable TileEntity tileentity, @Nullable ItemStack itemstack) {
      if (!world.isRemote && itemstack != null && itemstack.getItem() == Items.SHEARS) {
         entityhuman.addStat(StatList.getBlockStats(this));
         spawnAsEntity(world, blockposition, new ItemStack(Blocks.VINE, 1, 0));
      } else {
         super.harvestBlock(world, entityhuman, blockposition, iblockdata, tileentity, itemstack);
      }

   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(SOUTH, Boolean.valueOf((i & 1) > 0)).withProperty(WEST, Boolean.valueOf((i & 2) > 0)).withProperty(NORTH, Boolean.valueOf((i & 4) > 0)).withProperty(EAST, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      int i = 0;
      if (((Boolean)iblockdata.getValue(SOUTH)).booleanValue()) {
         i |= 1;
      }

      if (((Boolean)iblockdata.getValue(WEST)).booleanValue()) {
         i |= 2;
      }

      if (((Boolean)iblockdata.getValue(NORTH)).booleanValue()) {
         i |= 4;
      }

      if (((Boolean)iblockdata.getValue(EAST)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{UP, NORTH, EAST, SOUTH, WEST});
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      switch(BlockVine.SyntheticClass_1.b[enumblockrotation.ordinal()]) {
      case 1:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(SOUTH)).withProperty(EAST, (Boolean)iblockdata.getValue(WEST)).withProperty(SOUTH, (Boolean)iblockdata.getValue(NORTH)).withProperty(WEST, (Boolean)iblockdata.getValue(EAST));
      case 2:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(EAST)).withProperty(EAST, (Boolean)iblockdata.getValue(SOUTH)).withProperty(SOUTH, (Boolean)iblockdata.getValue(WEST)).withProperty(WEST, (Boolean)iblockdata.getValue(NORTH));
      case 3:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(WEST)).withProperty(EAST, (Boolean)iblockdata.getValue(NORTH)).withProperty(SOUTH, (Boolean)iblockdata.getValue(EAST)).withProperty(WEST, (Boolean)iblockdata.getValue(SOUTH));
      default:
         return iblockdata;
      }
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      switch(BlockVine.SyntheticClass_1.c[enumblockmirror.ordinal()]) {
      case 1:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(SOUTH)).withProperty(SOUTH, (Boolean)iblockdata.getValue(NORTH));
      case 2:
         return iblockdata.withProperty(EAST, (Boolean)iblockdata.getValue(WEST)).withProperty(WEST, (Boolean)iblockdata.getValue(EAST));
      default:
         return super.withMirror(iblockdata, enumblockmirror);
      }
   }

   public static PropertyBool getPropertyFor(EnumFacing enumdirection) {
      switch(BlockVine.SyntheticClass_1.a[enumdirection.ordinal()]) {
      case 1:
         return UP;
      case 2:
         return NORTH;
      case 3:
         return SOUTH;
      case 4:
         return EAST;
      case 5:
         return WEST;
      default:
         throw new IllegalArgumentException(enumdirection + " is an invalid choice");
      }
   }

   public static int getNumGrownFaces(IBlockState iblockdata) {
      int i = 0;

      for(PropertyBool blockstateboolean : ALL_FACES) {
         if (((Boolean)iblockdata.getValue(blockstateboolean)).booleanValue()) {
            ++i;
         }
      }

      return i;
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b;
      static final int[] c = new int[Mirror.values().length];

      static {
         try {
            c[Mirror.LEFT_RIGHT.ordinal()] = 1;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            c[Mirror.FRONT_BACK.ordinal()] = 2;
         } catch (NoSuchFieldError var8) {
            ;
         }

         b = new int[Rotation.values().length];

         try {
            b[Rotation.CLOCKWISE_180.ordinal()] = 1;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            b[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            b[Rotation.CLOCKWISE_90.ordinal()] = 3;
         } catch (NoSuchFieldError var5) {
            ;
         }

         a = new int[EnumFacing.values().length];

         try {
            a[EnumFacing.UP.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 5;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
