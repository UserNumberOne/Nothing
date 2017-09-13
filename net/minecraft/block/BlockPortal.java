package net.minecraft.block;

import com.google.common.cache.LoadingCache;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;

public class BlockPortal extends BlockBreakable {
   public static final PropertyEnum AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class, (Enum[])(EnumFacing.Axis.X, EnumFacing.Axis.Z));
   protected static final AxisAlignedBB X_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
   protected static final AxisAlignedBB Z_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
   protected static final AxisAlignedBB Y_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);

   public BlockPortal() {
      super(Material.PORTAL, false);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch(BlockPortal.SyntheticClass_1.a[((EnumFacing.Axis)var1.getValue(AXIS)).ordinal()]) {
      case 1:
         return X_AABB;
      case 2:
      default:
         return Y_AABB;
      case 3:
         return Z_AABB;
      }
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      super.updateTick(var1, var2, var3, var4);
      if (var1.provider.isSurfaceWorld() && var1.getGameRules().getBoolean("doMobSpawning") && var4.nextInt(2000) < var1.getDifficulty().getDifficultyId()) {
         int var5 = var2.getY();

         BlockPos var6;
         for(var6 = var2; !var1.getBlockState(var6).isFullyOpaque() && var6.getY() > 0; var6 = var6.down()) {
            ;
         }

         if (var5 > 0 && !var1.getBlockState(var6.up()).isNormalCube()) {
            Entity var7 = ItemMonsterPlacer.spawnCreature(var1, EntityList.getEntityStringFromClass(EntityPigZombie.class), (double)var6.getX() + 0.5D, (double)var6.getY() + 1.1D, (double)var6.getZ() + 0.5D, SpawnReason.NETHER_PORTAL);
            if (var7 != null) {
               var7.timeUntilPortal = var7.getPortalCooldown();
            }
         }
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public static int getMetaForAxis(EnumFacing.Axis var0) {
      return var0 == EnumFacing.Axis.X ? 1 : (var0 == EnumFacing.Axis.Z ? 2 : 0);
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean trySpawnPortal(World var1, BlockPos var2) {
      BlockPortal.Size var3 = new BlockPortal.Size(var1, var2, EnumFacing.Axis.X);
      if (var3.isValid() && var3.portalBlockCount == 0) {
         return var3.createPortal();
      } else {
         BlockPortal.Size var4 = new BlockPortal.Size(var1, var2, EnumFacing.Axis.Z);
         return var4.isValid() && var4.portalBlockCount == 0 ? var4.createPortal() : false;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      EnumFacing.Axis var5 = (EnumFacing.Axis)var1.getValue(AXIS);
      if (var5 == EnumFacing.Axis.X) {
         BlockPortal.Size var6 = new BlockPortal.Size(var2, var3, EnumFacing.Axis.X);
         if (!var6.isValid() || var6.portalBlockCount < var6.width * var6.height) {
            var2.setBlockState(var3, Blocks.AIR.getDefaultState());
         }
      } else if (var5 == EnumFacing.Axis.Z) {
         BlockPortal.Size var7 = new BlockPortal.Size(var2, var3, EnumFacing.Axis.Z);
         if (!var7.isValid() || var7.portalBlockCount < var7.width * var7.height) {
            var2.setBlockState(var3, Blocks.AIR.getDefaultState());
         }
      }

   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!var4.isRiding() && !var4.isBeingRidden() && var4.isNonBoss()) {
         EntityPortalEnterEvent var5 = new EntityPortalEnterEvent(var4.getBukkitEntity(), new Location(var1.getWorld(), (double)var2.getX(), (double)var2.getY(), (double)var2.getZ()));
         var1.getServer().getPluginManager().callEvent(var5);
         var4.setPortal(var2);
      }

   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return null;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AXIS, (var1 & 3) == 2 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
   }

   public int getMetaFromState(IBlockState var1) {
      return getMetaForAxis((EnumFacing.Axis)var1.getValue(AXIS));
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(BlockPortal.SyntheticClass_1.b[var2.ordinal()]) {
      case 1:
      case 2:
         switch(BlockPortal.SyntheticClass_1.a[((EnumFacing.Axis)var1.getValue(AXIS)).ordinal()]) {
         case 1:
            return var1.withProperty(AXIS, EnumFacing.Axis.Z);
         case 2:
         default:
            return var1;
         case 3:
            return var1.withProperty(AXIS, EnumFacing.Axis.X);
         }
      default:
         return var1;
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AXIS});
   }

   public BlockPattern.PatternHelper createPatternHelper(World var1, BlockPos var2) {
      EnumFacing.Axis var3 = EnumFacing.Axis.Z;
      BlockPortal.Size var4 = new BlockPortal.Size(var1, var2, EnumFacing.Axis.X);
      LoadingCache var5 = BlockPattern.createLoadingCache(var1, true);
      if (!var4.isValid()) {
         var3 = EnumFacing.Axis.X;
         var4 = new BlockPortal.Size(var1, var2, EnumFacing.Axis.Z);
      }

      if (!var4.isValid()) {
         return new BlockPattern.PatternHelper(var2, EnumFacing.NORTH, EnumFacing.UP, var5, 1, 1, 1);
      } else {
         int[] var6 = new int[EnumFacing.AxisDirection.values().length];
         EnumFacing var7 = var4.rightDir.rotateYCCW();
         BlockPos var8 = var4.bottomLeft.up(var4.getHeight() - 1);

         for(EnumFacing.AxisDirection var12 : EnumFacing.AxisDirection.values()) {
            BlockPattern.PatternHelper var13 = new BlockPattern.PatternHelper(var7.getAxisDirection() == var12 ? var8 : var8.offset(var4.rightDir, var4.getWidth() - 1), EnumFacing.getFacingFromAxis(var12, var3), EnumFacing.UP, var5, var4.getWidth(), var4.getHeight(), 1);

            for(int var14 = 0; var14 < var4.getWidth(); ++var14) {
               for(int var15 = 0; var15 < var4.getHeight(); ++var15) {
                  BlockWorldState var16 = var13.translateOffset(var14, var15, 1);
                  if (var16.getBlockState() != null && var16.getBlockState().getMaterial() != Material.AIR) {
                     ++var6[var12.ordinal()];
                  }
               }
            }
         }

         EnumFacing.AxisDirection var18 = EnumFacing.AxisDirection.POSITIVE;

         for(EnumFacing.AxisDirection var21 : EnumFacing.AxisDirection.values()) {
            if (var6[var21.ordinal()] < var6[var18.ordinal()]) {
               var18 = var21;
            }
         }

         return new BlockPattern.PatternHelper(var7.getAxisDirection() == var18 ? var8 : var8.offset(var4.rightDir, var4.getWidth() - 1), EnumFacing.getFacingFromAxis(var18, var3), EnumFacing.UP, var5, var4.getWidth(), var4.getHeight(), 1);
      }
   }

   public static class Size {
      private final World world;
      private final EnumFacing.Axis axis;
      private final EnumFacing rightDir;
      private final EnumFacing leftDir;
      private int portalBlockCount;
      private BlockPos bottomLeft;
      private int height;
      private int width;
      Collection blocks = new HashSet();

      public Size(World var1, BlockPos var2, EnumFacing.Axis var3) {
         this.world = var1;
         this.axis = var3;
         if (var3 == EnumFacing.Axis.X) {
            this.leftDir = EnumFacing.EAST;
            this.rightDir = EnumFacing.WEST;
         } else {
            this.leftDir = EnumFacing.NORTH;
            this.rightDir = EnumFacing.SOUTH;
         }

         for(BlockPos var4 = var2; var2.getY() > var4.getY() - 21 && var2.getY() > 0 && this.isEmptyBlock(var1.getBlockState(var2.down()).getBlock()); var2 = var2.down()) {
            ;
         }

         int var5 = this.getDistanceUntilEdge(var2, this.leftDir) - 1;
         if (var5 >= 0) {
            this.bottomLeft = var2.offset(this.leftDir, var5);
            this.width = this.getDistanceUntilEdge(this.bottomLeft, this.rightDir);
            if (this.width < 2 || this.width > 21) {
               this.bottomLeft = null;
               this.width = 0;
            }
         }

         if (this.bottomLeft != null) {
            this.height = this.calculatePortalHeight();
         }

      }

      protected int getDistanceUntilEdge(BlockPos var1, EnumFacing var2) {
         int var3;
         for(var3 = 0; var3 < 22; ++var3) {
            BlockPos var4 = var1.offset(var2, var3);
            if (!this.isEmptyBlock(this.world.getBlockState(var4).getBlock()) || this.world.getBlockState(var4.down()).getBlock() != Blocks.OBSIDIAN) {
               break;
            }
         }

         Block var5 = this.world.getBlockState(var1.offset(var2, var3)).getBlock();
         return var5 == Blocks.OBSIDIAN ? var3 : 0;
      }

      public int getHeight() {
         return this.height;
      }

      public int getWidth() {
         return this.width;
      }

      protected int calculatePortalHeight() {
         this.blocks.clear();
         CraftWorld var1 = this.world.getWorld();

         label60:
         for(this.height = 0; this.height < 21; ++this.height) {
            for(int var2 = 0; var2 < this.width; ++var2) {
               BlockPos var3 = this.bottomLeft.offset(this.rightDir, var2).up(this.height);
               Block var4 = this.world.getBlockState(var3).getBlock();
               if (!this.isEmptyBlock(var4)) {
                  break label60;
               }

               if (var4 == Blocks.PORTAL) {
                  ++this.portalBlockCount;
               }

               if (var2 == 0) {
                  var4 = this.world.getBlockState(var3.offset(this.leftDir)).getBlock();
                  if (var4 != Blocks.OBSIDIAN) {
                     break label60;
                  }

                  BlockPos var5 = var3.offset(this.leftDir);
                  this.blocks.add(var1.getBlockAt(var5.getX(), var5.getY(), var5.getZ()));
               } else if (var2 == this.width - 1) {
                  var4 = this.world.getBlockState(var3.offset(this.rightDir)).getBlock();
                  if (var4 != Blocks.OBSIDIAN) {
                     break label60;
                  }

                  BlockPos var10 = var3.offset(this.rightDir);
                  this.blocks.add(var1.getBlockAt(var10.getX(), var10.getY(), var10.getZ()));
               }
            }
         }

         for(int var6 = 0; var6 < this.width; ++var6) {
            if (this.world.getBlockState(this.bottomLeft.offset(this.rightDir, var6).up(this.height)).getBlock() != Blocks.OBSIDIAN) {
               this.height = 0;
               break;
            }

            BlockPos var7 = this.bottomLeft.offset(this.rightDir, var6).up(this.height);
            this.blocks.add(var1.getBlockAt(var7.getX(), var7.getY(), var7.getZ()));
         }

         if (this.height <= 21 && this.height >= 3) {
            return this.height;
         } else {
            this.bottomLeft = null;
            this.width = 0;
            this.height = 0;
            return 0;
         }
      }

      protected boolean isEmptyBlock(Block var1) {
         return var1.blockMaterial == Material.AIR || var1 == Blocks.FIRE || var1 == Blocks.PORTAL;
      }

      public boolean isValid() {
         return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
      }

      public boolean createPortal() {
         CraftWorld var1 = this.world.getWorld();

         for(int var2 = 0; var2 < this.width; ++var2) {
            BlockPos var3 = this.bottomLeft.offset(this.rightDir, var2);

            for(int var4 = 0; var4 < this.height; ++var4) {
               BlockPos var5 = var3.up(var4);
               this.blocks.add(var1.getBlockAt(var5.getX(), var5.getY(), var5.getZ()));
            }
         }

         PortalCreateEvent var6 = new PortalCreateEvent(this.blocks, var1, CreateReason.FIRE);
         this.world.getServer().getPluginManager().callEvent(var6);
         if (var6.isCancelled()) {
            return false;
         } else {
            for(int var7 = 0; var7 < this.width; ++var7) {
               BlockPos var8 = this.bottomLeft.offset(this.rightDir, var7);

               for(int var9 = 0; var9 < this.height; ++var9) {
                  this.world.setBlockState(var8.up(var9), Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, this.axis), 2);
               }
            }

            return true;
         }
      }
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b = new int[Rotation.values().length];

      static {
         try {
            b[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            b[Rotation.CLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         a = new int[EnumFacing.Axis.values().length];

         try {
            a[EnumFacing.Axis.X.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.Axis.Y.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.Axis.Z.ordinal()] = 3;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
