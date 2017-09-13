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
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      var1 = var1.getActualState(var2, var3);
      int var4 = 0;
      AxisAlignedBB var5 = FULL_BLOCK_AABB;
      if (((Boolean)var1.getValue(UP)).booleanValue()) {
         var5 = UP_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(NORTH)).booleanValue()) {
         var5 = NORTH_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(EAST)).booleanValue()) {
         var5 = EAST_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(SOUTH)).booleanValue()) {
         var5 = SOUTH_AABB;
         ++var4;
      }

      if (((Boolean)var1.getValue(WEST)).booleanValue()) {
         var5 = WEST_AABB;
         ++var4;
      }

      return var4 == 1 ? var5 : FULL_BLOCK_AABB;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return var1.withProperty(UP, Boolean.valueOf(var2.getBlockState(var3.up()).isBlockNormalCube()));
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      switch(BlockVine.SyntheticClass_1.a[var3.ordinal()]) {
      case 1:
         return this.canAttachVineOn(var1.getBlockState(var2.up()));
      case 2:
      case 3:
      case 4:
      case 5:
         return this.canAttachVineOn(var1.getBlockState(var2.offset(var3.getOpposite())));
      default:
         return false;
      }
   }

   private boolean canAttachVineOn(IBlockState var1) {
      return var1.isFullCube() && var1.getMaterial().blocksMovement();
   }

   private boolean recheckGrownSides(World var1, BlockPos var2, IBlockState var3) {
      IBlockState var4 = var3;

      for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
         PropertyBool var7 = getPropertyFor(var6);
         if (((Boolean)var3.getValue(var7)).booleanValue() && !this.canAttachVineOn(var1.getBlockState(var2.offset(var6)))) {
            IBlockState var8 = var1.getBlockState(var2.up());
            if (var8.getBlock() != this || !((Boolean)var8.getValue(var7)).booleanValue()) {
               var3 = var3.withProperty(var7, Boolean.valueOf(false));
            }
         }
      }

      if (getNumGrownFaces(var3) == 0) {
         return false;
      } else {
         if (var4 != var3) {
            var1.setBlockState(var2, var3, 2);
         }

         return true;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote && !this.recheckGrownSides(var2, var3, var1)) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && var1.rand.nextInt(4) == 0) {
         int var5 = 5;
         boolean var6 = false;

         label177:
         for(int var7 = -4; var7 <= 4; ++var7) {
            for(int var8 = -4; var8 <= 4; ++var8) {
               for(int var9 = -1; var9 <= 1; ++var9) {
                  if (var1.getBlockState(var2.add(var7, var9, var8)).getBlock() == this) {
                     --var5;
                     if (var5 <= 0) {
                        var6 = true;
                        break label177;
                     }
                  }
               }
            }
         }

         EnumFacing var20 = EnumFacing.random(var4);
         BlockPos var21 = var2.up();
         if (var20 == EnumFacing.UP && var2.getY() < 255 && var1.isAirBlock(var21)) {
            if (!var6) {
               IBlockState var24 = var3;

               for(EnumFacing var28 : EnumFacing.Plane.HORIZONTAL) {
                  if (var4.nextBoolean() || !this.canAttachVineOn(var1.getBlockState(var21.offset(var28)))) {
                     var24 = var24.withProperty(getPropertyFor(var28), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)var24.getValue(NORTH)).booleanValue() || ((Boolean)var24.getValue(EAST)).booleanValue() || ((Boolean)var24.getValue(SOUTH)).booleanValue() || ((Boolean)var24.getValue(WEST)).booleanValue()) {
                  org.bukkit.block.Block var31 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
                  org.bukkit.block.Block var34 = var1.getWorld().getBlockAt(var21.getX(), var21.getY(), var21.getZ());
                  CraftEventFactory.handleBlockSpreadEvent(var34, var31, this, this.getMetaFromState(var24));
               }
            }
         } else if (var20.getAxis().isHorizontal() && !((Boolean)var3.getValue(getPropertyFor(var20))).booleanValue()) {
            if (!var6) {
               BlockPos var27 = var2.offset(var20);
               IBlockState var23 = var1.getBlockState(var27);
               Block var25 = var23.getBlock();
               if (var25.blockMaterial == Material.AIR) {
                  EnumFacing var30 = var20.rotateY();
                  EnumFacing var33 = var20.rotateYCCW();
                  boolean var36 = ((Boolean)var3.getValue(getPropertyFor(var30))).booleanValue();
                  boolean var38 = ((Boolean)var3.getValue(getPropertyFor(var33))).booleanValue();
                  BlockPos var39 = var27.offset(var30);
                  BlockPos var17 = var27.offset(var33);
                  org.bukkit.block.Block var18 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
                  org.bukkit.block.Block var19 = var1.getWorld().getBlockAt(var27.getX(), var27.getY(), var27.getZ());
                  if (var36 && this.canAttachVineOn(var1.getBlockState(var39))) {
                     CraftEventFactory.handleBlockSpreadEvent(var19, var18, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(var30), Boolean.valueOf(true))));
                  } else if (var38 && this.canAttachVineOn(var1.getBlockState(var17))) {
                     CraftEventFactory.handleBlockSpreadEvent(var19, var18, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(var33), Boolean.valueOf(true))));
                  } else if (var36 && var1.isAirBlock(var39) && this.canAttachVineOn(var1.getBlockState(var2.offset(var30)))) {
                     var19 = var1.getWorld().getBlockAt(var39.getX(), var39.getY(), var39.getZ());
                     CraftEventFactory.handleBlockSpreadEvent(var19, var18, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(var20.getOpposite()), Boolean.valueOf(true))));
                  } else if (var38 && var1.isAirBlock(var17) && this.canAttachVineOn(var1.getBlockState(var2.offset(var33)))) {
                     var19 = var1.getWorld().getBlockAt(var17.getX(), var17.getY(), var17.getZ());
                     CraftEventFactory.handleBlockSpreadEvent(var19, var18, this, this.getMetaFromState(this.getDefaultState().withProperty(getPropertyFor(var20.getOpposite()), Boolean.valueOf(true))));
                  } else if (this.canAttachVineOn(var1.getBlockState(var27.up()))) {
                     CraftEventFactory.handleBlockSpreadEvent(var19, var18, this, this.getMetaFromState(this.getDefaultState()));
                  }
               } else if (var25.blockMaterial.isOpaque() && var23.isFullCube()) {
                  var1.setBlockState(var2, var3.withProperty(getPropertyFor(var20), Boolean.valueOf(true)), 2);
               }
            }
         } else if (var2.getY() > 1) {
            BlockPos var11 = var2.down();
            IBlockState var22 = var1.getBlockState(var11);
            Block var10 = var22.getBlock();
            if (var10.blockMaterial == Material.AIR) {
               IBlockState var12 = var3;

               for(EnumFacing var14 : EnumFacing.Plane.HORIZONTAL) {
                  if (var4.nextBoolean()) {
                     var12 = var12.withProperty(getPropertyFor(var14), Boolean.valueOf(false));
                  }
               }

               if (((Boolean)var12.getValue(NORTH)).booleanValue() || ((Boolean)var12.getValue(EAST)).booleanValue() || ((Boolean)var12.getValue(SOUTH)).booleanValue() || ((Boolean)var12.getValue(WEST)).booleanValue()) {
                  org.bukkit.block.Block var15 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
                  org.bukkit.block.Block var16 = var1.getWorld().getBlockAt(var11.getX(), var11.getY(), var11.getZ());
                  CraftEventFactory.handleBlockSpreadEvent(var16, var15, this, this.getMetaFromState(var12));
               }
            } else if (var10 == this) {
               IBlockState var29 = var22;

               for(EnumFacing var35 : EnumFacing.Plane.HORIZONTAL) {
                  PropertyBool var37 = getPropertyFor(var35);
                  if (var4.nextBoolean() && ((Boolean)var3.getValue(var37)).booleanValue()) {
                     var29 = var29.withProperty(var37, Boolean.valueOf(true));
                  }
               }

               if (((Boolean)var29.getValue(NORTH)).booleanValue() || ((Boolean)var29.getValue(EAST)).booleanValue() || ((Boolean)var29.getValue(SOUTH)).booleanValue() || ((Boolean)var29.getValue(WEST)).booleanValue()) {
                  var1.setBlockState(var11, var29, 2);
               }
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = this.getDefaultState().withProperty(UP, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false));
      return var3.getAxis().isHorizontal() ? var9.withProperty(getPropertyFor(var3.getOpposite()), Boolean.valueOf(true)) : var9;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      if (!var1.isRemote && var6 != null && var6.getItem() == Items.SHEARS) {
         var2.addStat(StatList.getBlockStats(this));
         spawnAsEntity(var1, var3, new ItemStack(Blocks.VINE, 1, 0));
      } else {
         super.harvestBlock(var1, var2, var3, var4, var5, var6);
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SOUTH, Boolean.valueOf((var1 & 1) > 0)).withProperty(WEST, Boolean.valueOf((var1 & 2) > 0)).withProperty(NORTH, Boolean.valueOf((var1 & 4) > 0)).withProperty(EAST, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      if (((Boolean)var1.getValue(SOUTH)).booleanValue()) {
         var2 |= 1;
      }

      if (((Boolean)var1.getValue(WEST)).booleanValue()) {
         var2 |= 2;
      }

      if (((Boolean)var1.getValue(NORTH)).booleanValue()) {
         var2 |= 4;
      }

      if (((Boolean)var1.getValue(EAST)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{UP, NORTH, EAST, SOUTH, WEST});
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(BlockVine.SyntheticClass_1.b[var2.ordinal()]) {
      case 1:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(SOUTH)).withProperty(EAST, (Boolean)var1.getValue(WEST)).withProperty(SOUTH, (Boolean)var1.getValue(NORTH)).withProperty(WEST, (Boolean)var1.getValue(EAST));
      case 2:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(EAST)).withProperty(EAST, (Boolean)var1.getValue(SOUTH)).withProperty(SOUTH, (Boolean)var1.getValue(WEST)).withProperty(WEST, (Boolean)var1.getValue(NORTH));
      case 3:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(WEST)).withProperty(EAST, (Boolean)var1.getValue(NORTH)).withProperty(SOUTH, (Boolean)var1.getValue(EAST)).withProperty(WEST, (Boolean)var1.getValue(SOUTH));
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      switch(BlockVine.SyntheticClass_1.c[var2.ordinal()]) {
      case 1:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(SOUTH)).withProperty(SOUTH, (Boolean)var1.getValue(NORTH));
      case 2:
         return var1.withProperty(EAST, (Boolean)var1.getValue(WEST)).withProperty(WEST, (Boolean)var1.getValue(EAST));
      default:
         return super.withMirror(var1, var2);
      }
   }

   public static PropertyBool getPropertyFor(EnumFacing var0) {
      switch(BlockVine.SyntheticClass_1.a[var0.ordinal()]) {
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
         throw new IllegalArgumentException(var0 + " is an invalid choice");
      }
   }

   public static int getNumGrownFaces(IBlockState var0) {
      int var1 = 0;

      for(PropertyBool var5 : ALL_FACES) {
         if (((Boolean)var0.getValue(var5)).booleanValue()) {
            ++var1;
         }
      }

      return var1;
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
