package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFire extends Block {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   public static final PropertyBool UPPER = PropertyBool.create("up");
   private final Map encouragements = Maps.newIdentityHashMap();
   private final Map flammabilities = Maps.newIdentityHashMap();

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return !var2.getBlockState(var3.down()).isSideSolid(var2, var3.down(), EnumFacing.UP) && !Blocks.FIRE.canCatchFire(var2, var3.down(), EnumFacing.UP) ? var1.withProperty(NORTH, Boolean.valueOf(this.canCatchFire(var2, var3.north(), EnumFacing.SOUTH))).withProperty(EAST, Boolean.valueOf(this.canCatchFire(var2, var3.east(), EnumFacing.WEST))).withProperty(SOUTH, Boolean.valueOf(this.canCatchFire(var2, var3.south(), EnumFacing.NORTH))).withProperty(WEST, Boolean.valueOf(this.canCatchFire(var2, var3.west(), EnumFacing.EAST))).withProperty(UPPER, Boolean.valueOf(this.canCatchFire(var2, var3.up(), EnumFacing.DOWN))) : this.getDefaultState();
   }

   protected BlockFire() {
      super(Material.FIRE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)).withProperty(UPPER, Boolean.valueOf(false)));
      this.setTickRandomly(true);
   }

   public static void init() {
      Blocks.FIRE.setFireInfo(Blocks.PLANKS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DOUBLE_WOODEN_SLAB, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.WOODEN_SLAB, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.OAK_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.SPRUCE_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.BIRCH_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.JUNGLE_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.ACACIA_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.OAK_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.SPRUCE_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.BIRCH_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.JUNGLE_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DARK_OAK_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.ACACIA_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.OAK_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.BIRCH_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.SPRUCE_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.JUNGLE_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.ACACIA_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DARK_OAK_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.LOG, 5, 5);
      Blocks.FIRE.setFireInfo(Blocks.LOG2, 5, 5);
      Blocks.FIRE.setFireInfo(Blocks.LEAVES, 30, 60);
      Blocks.FIRE.setFireInfo(Blocks.LEAVES2, 30, 60);
      Blocks.FIRE.setFireInfo(Blocks.BOOKSHELF, 30, 20);
      Blocks.FIRE.setFireInfo(Blocks.TNT, 15, 100);
      Blocks.FIRE.setFireInfo(Blocks.TALLGRASS, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.DOUBLE_PLANT, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.YELLOW_FLOWER, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.RED_FLOWER, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.DEADBUSH, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.WOOL, 30, 60);
      Blocks.FIRE.setFireInfo(Blocks.VINE, 15, 100);
      Blocks.FIRE.setFireInfo(Blocks.COAL_BLOCK, 5, 5);
      Blocks.FIRE.setFireInfo(Blocks.HAY_BLOCK, 60, 20);
      Blocks.FIRE.setFireInfo(Blocks.CARPET, 60, 20);
   }

   public void setFireInfo(Block var1, int var2, int var3) {
      if (var1 == Blocks.AIR) {
         throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
      } else {
         this.encouragements.put(var1, Integer.valueOf(var2));
         this.flammabilities.put(var1, Integer.valueOf(var3));
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

   public int quantityDropped(Random var1) {
      return 0;
   }

   public int tickRate(World var1) {
      return 30;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (var1.getGameRules().getBoolean("doFireTick")) {
         if (!this.canPlaceBlockAt(var1, var2)) {
            var1.setBlockToAir(var2);
         }

         Block var5 = var1.getBlockState(var2.down()).getBlock();
         boolean var6 = var5.isFireSource(var1, var2.down(), EnumFacing.UP);
         int var7 = ((Integer)var3.getValue(AGE)).intValue();
         if (!var6 && var1.isRaining() && this.canDie(var1, var2) && var4.nextFloat() < 0.2F + (float)var7 * 0.03F) {
            var1.setBlockToAir(var2);
         } else {
            if (var7 < 15) {
               var3 = var3.withProperty(AGE, Integer.valueOf(var7 + var4.nextInt(3) / 2));
               var1.setBlockState(var2, var3, 4);
            }

            var1.scheduleUpdate(var2, this, this.tickRate(var1) + var4.nextInt(10));
            if (!var6) {
               if (!this.canNeighborCatchFire(var1, var2)) {
                  if (!var1.getBlockState(var2.down()).isSideSolid(var1, var2.down(), EnumFacing.UP) || var7 > 3) {
                     var1.setBlockToAir(var2);
                  }

                  return;
               }

               if (!this.canCatchFire(var1, var2.down(), EnumFacing.UP) && var7 == 15 && var4.nextInt(4) == 0) {
                  var1.setBlockToAir(var2);
                  return;
               }
            }

            boolean var8 = var1.isBlockinHighHumidity(var2);
            byte var9 = 0;
            if (var8) {
               var9 = -50;
            }

            this.tryCatchFire(var1, var2.east(), 300 + var9, var4, var7, EnumFacing.WEST);
            this.tryCatchFire(var1, var2.west(), 300 + var9, var4, var7, EnumFacing.EAST);
            this.tryCatchFire(var1, var2.down(), 250 + var9, var4, var7, EnumFacing.UP);
            this.tryCatchFire(var1, var2.up(), 250 + var9, var4, var7, EnumFacing.DOWN);
            this.tryCatchFire(var1, var2.north(), 300 + var9, var4, var7, EnumFacing.SOUTH);
            this.tryCatchFire(var1, var2.south(), 300 + var9, var4, var7, EnumFacing.NORTH);

            for(int var10 = -1; var10 <= 1; ++var10) {
               for(int var11 = -1; var11 <= 1; ++var11) {
                  for(int var12 = -1; var12 <= 4; ++var12) {
                     if (var10 != 0 || var12 != 0 || var11 != 0) {
                        int var13 = 100;
                        if (var12 > 1) {
                           var13 += (var12 - 1) * 100;
                        }

                        BlockPos var14 = var2.add(var10, var12, var11);
                        int var15 = this.getNeighborEncouragement(var1, var14);
                        if (var15 > 0) {
                           int var16 = (var15 + 40 + var1.getDifficulty().getDifficultyId() * 7) / (var7 + 30);
                           if (var8) {
                              var16 /= 2;
                           }

                           if (var16 > 0 && var4.nextInt(var13) <= var16 && (!var1.isRaining() || !this.canDie(var1, var14))) {
                              int var17 = var7 + var4.nextInt(5) / 4;
                              if (var17 > 15) {
                                 var17 = 15;
                              }

                              var1.setBlockState(var14, var3.withProperty(AGE, Integer.valueOf(var17)), 3);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   protected boolean canDie(World var1, BlockPos var2) {
      return var1.isRainingAt(var2) || var1.isRainingAt(var2.west()) || var1.isRainingAt(var2.east()) || var1.isRainingAt(var2.north()) || var1.isRainingAt(var2.south());
   }

   public boolean requiresUpdates() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public int getFlammability(Block var1) {
      Integer var2 = (Integer)this.flammabilities.get(var1);
      return var2 == null ? 0 : var2.intValue();
   }

   /** @deprecated */
   @Deprecated
   public int getEncouragement(Block var1) {
      Integer var2 = (Integer)this.encouragements.get(var1);
      return var2 == null ? 0 : var2.intValue();
   }

   /** @deprecated */
   @Deprecated
   private void catchOnFire(World var1, BlockPos var2, int var3, Random var4, int var5) {
      this.tryCatchFire(var1, var2, var3, var4, var5, EnumFacing.UP);
   }

   private void tryCatchFire(World var1, BlockPos var2, int var3, Random var4, int var5, EnumFacing var6) {
      int var7 = var1.getBlockState(var2).getBlock().getFlammability(var1, var2, var6);
      if (var4.nextInt(var3) < var7) {
         IBlockState var8 = var1.getBlockState(var2);
         if (var4.nextInt(var5 + 10) < 5 && !var1.isRainingAt(var2)) {
            int var9 = var5 + var4.nextInt(5) / 4;
            if (var9 > 15) {
               var9 = 15;
            }

            var1.setBlockState(var2, this.getDefaultState().withProperty(AGE, Integer.valueOf(var9)), 3);
         } else {
            var1.setBlockToAir(var2);
         }

         if (var8.getBlock() == Blocks.TNT) {
            Blocks.TNT.onBlockDestroyedByPlayer(var1, var2, var8.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
         }
      }

   }

   private boolean canNeighborCatchFire(World var1, BlockPos var2) {
      for(EnumFacing var6 : EnumFacing.values()) {
         if (this.canCatchFire(var1, var2.offset(var6), var6.getOpposite())) {
            return true;
         }
      }

      return false;
   }

   private int getNeighborEncouragement(World var1, BlockPos var2) {
      if (!var1.isAirBlock(var2)) {
         return 0;
      } else {
         int var3 = 0;

         for(EnumFacing var7 : EnumFacing.values()) {
            var3 = Math.max(var1.getBlockState(var2.offset(var7)).getBlock().getFireSpreadSpeed(var1, var2.offset(var7), var7.getOpposite()), var3);
         }

         return var3;
      }
   }

   public boolean isCollidable() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean canCatchFire(IBlockAccess var1, BlockPos var2) {
      return this.canCatchFire(var1, var2, EnumFacing.UP);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).isFullyOpaque() || this.canNeighborCatchFire(var1, var2);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.getBlockState(var3.down()).isFullyOpaque() && !this.canNeighborCatchFire(var2, var3)) {
         var2.setBlockToAir(var3);
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (var1.provider.getDimensionType().getId() > 0 || !Blocks.PORTAL.trySpawnPortal(var1, var2)) {
         if (!var1.getBlockState(var2.down()).isFullyOpaque() && !this.canNeighborCatchFire(var1, var2)) {
            var1.setBlockToAir(var2);
         } else {
            var1.scheduleUpdate(var2, this, this.tickRate(var1) + var1.rand.nextInt(10));
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (var4.nextInt(24) == 0) {
         var2.playSound((double)((float)var3.getX() + 0.5F), (double)((float)var3.getY() + 0.5F), (double)((float)var3.getZ() + 0.5F), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + var4.nextFloat(), var4.nextFloat() * 0.7F + 0.3F, false);
      }

      if (!var2.getBlockState(var3.down()).isSideSolid(var2, var3.down(), EnumFacing.UP) && !Blocks.FIRE.canCatchFire(var2, var3.down(), EnumFacing.UP)) {
         if (Blocks.FIRE.canCatchFire(var2, var3.west(), EnumFacing.EAST)) {
            for(int var12 = 0; var12 < 2; ++var12) {
               double var17 = (double)var3.getX() + var4.nextDouble() * 0.10000000149011612D;
               double var22 = (double)var3.getY() + var4.nextDouble();
               double var27 = (double)var3.getZ() + var4.nextDouble();
               var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, var17, var22, var27, 0.0D, 0.0D, 0.0D);
            }
         }

         if (Blocks.FIRE.canCatchFire(var2, var3.east(), EnumFacing.WEST)) {
            for(int var13 = 0; var13 < 2; ++var13) {
               double var18 = (double)(var3.getX() + 1) - var4.nextDouble() * 0.10000000149011612D;
               double var23 = (double)var3.getY() + var4.nextDouble();
               double var28 = (double)var3.getZ() + var4.nextDouble();
               var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, var18, var23, var28, 0.0D, 0.0D, 0.0D);
            }
         }

         if (Blocks.FIRE.canCatchFire(var2, var3.north(), EnumFacing.SOUTH)) {
            for(int var14 = 0; var14 < 2; ++var14) {
               double var19 = (double)var3.getX() + var4.nextDouble();
               double var24 = (double)var3.getY() + var4.nextDouble();
               double var29 = (double)var3.getZ() + var4.nextDouble() * 0.10000000149011612D;
               var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, var19, var24, var29, 0.0D, 0.0D, 0.0D);
            }
         }

         if (Blocks.FIRE.canCatchFire(var2, var3.south(), EnumFacing.NORTH)) {
            for(int var15 = 0; var15 < 2; ++var15) {
               double var20 = (double)var3.getX() + var4.nextDouble();
               double var25 = (double)var3.getY() + var4.nextDouble();
               double var30 = (double)(var3.getZ() + 1) - var4.nextDouble() * 0.10000000149011612D;
               var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, var20, var25, var30, 0.0D, 0.0D, 0.0D);
            }
         }

         if (Blocks.FIRE.canCatchFire(var2, var3.up(), EnumFacing.DOWN)) {
            for(int var16 = 0; var16 < 2; ++var16) {
               double var21 = (double)var3.getX() + var4.nextDouble();
               double var26 = (double)(var3.getY() + 1) - var4.nextDouble() * 0.10000000149011612D;
               double var31 = (double)var3.getZ() + var4.nextDouble();
               var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, var21, var26, var31, 0.0D, 0.0D, 0.0D);
            }
         }
      } else {
         for(int var5 = 0; var5 < 3; ++var5) {
            double var6 = (double)var3.getX() + var4.nextDouble();
            double var8 = (double)var3.getY() + var4.nextDouble() * 0.5D + 0.5D;
            double var10 = (double)var3.getZ() + var4.nextDouble();
            var2.spawnParticle(EnumParticleTypes.SMOKE_LARGE, var6, var8, var10, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.TNT;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE, NORTH, EAST, SOUTH, WEST, UPPER});
   }

   public boolean canCatchFire(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      return var1.getBlockState(var2).getBlock().isFlammable(var1, var2, var3);
   }
}
