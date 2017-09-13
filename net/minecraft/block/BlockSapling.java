package net.minecraft.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;

public class BlockSapling extends BlockBush implements IGrowable {
   public static final PropertyEnum TYPE = PropertyEnum.create("type", BlockPlanks.EnumType.class);
   public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);
   protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);
   public static TreeType treeType;

   protected BlockSapling() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockPlanks.EnumType.OAK).withProperty(STAGE, Integer.valueOf(0)));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return SAPLING_AABB;
   }

   public String getLocalizedName() {
      return I18n.translateToLocal(this.getUnlocalizedName() + "." + BlockPlanks.EnumType.OAK.getUnlocalizedName() + ".name");
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote) {
         super.updateTick(var1, var2, var3, var4);
         if (var1.getLightFromNeighbors(var2.up()) >= 9 && var4.nextInt(7) == 0) {
            var1.captureTreeGeneration = true;
            this.grow(var1, var2, var3, var4);
            var1.captureTreeGeneration = false;
            if (var1.capturedBlockStates.size() > 0) {
               TreeType var5 = treeType;
               treeType = null;
               Location var6 = new Location(var1.getWorld(), (double)var2.getX(), (double)var2.getY(), (double)var2.getZ());
               List var7 = (List)var1.capturedBlockStates.clone();
               var1.capturedBlockStates.clear();
               StructureGrowEvent var8 = null;
               if (var5 != null) {
                  var8 = new StructureGrowEvent(var6, var5, false, (Player)null, var7);
                  Bukkit.getPluginManager().callEvent(var8);
               }

               if (var8 == null || !var8.isCancelled()) {
                  for(BlockState var10 : var7) {
                     var10.update(true);
                  }
               }
            }
         }
      }

   }

   public void grow(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (((Integer)var3.getValue(STAGE)).intValue() == 0) {
         var1.setBlockState(var2, var3.cycleProperty(STAGE), 4);
      } else {
         this.generateTree(var1, var2, var3, var4);
      }

   }

   public void generateTree(World var1, BlockPos var2, IBlockState var3, Random var4) {
      Object var5;
      if (var4.nextInt(10) == 0) {
         treeType = TreeType.BIG_TREE;
         var5 = new WorldGenBigTree(true);
      } else {
         treeType = TreeType.TREE;
         var5 = new WorldGenTrees(true);
      }

      int var6 = 0;
      int var7 = 0;
      boolean var8 = false;
      switch(BlockSapling.SyntheticClass_1.a[((BlockPlanks.EnumType)var3.getValue(TYPE)).ordinal()]) {
      case 1:
         label68:
         for(var6 = 0; var6 >= -1; --var6) {
            for(var7 = 0; var7 >= -1; --var7) {
               if (this.isTwoByTwoOfType(var1, var2, var6, var7, BlockPlanks.EnumType.SPRUCE)) {
                  treeType = TreeType.MEGA_REDWOOD;
                  var5 = new WorldGenMegaPineTree(false, var4.nextBoolean());
                  var8 = true;
                  break label68;
               }
            }
         }

         if (!var8) {
            var6 = 0;
            var7 = 0;
            treeType = TreeType.REDWOOD;
            var5 = new WorldGenTaiga2(true);
         }
         break;
      case 2:
         treeType = TreeType.BIRCH;
         var5 = new WorldGenBirchTree(true, false);
         break;
      case 3:
         IBlockState var9 = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
         IBlockState var10 = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

         label82:
         for(var6 = 0; var6 >= -1; --var6) {
            for(var7 = 0; var7 >= -1; --var7) {
               if (this.isTwoByTwoOfType(var1, var2, var6, var7, BlockPlanks.EnumType.JUNGLE)) {
                  treeType = TreeType.JUNGLE;
                  var5 = new WorldGenMegaJungle(true, 10, 20, var9, var10);
                  var8 = true;
                  break label82;
               }
            }
         }

         if (!var8) {
            var6 = 0;
            var7 = 0;
            treeType = TreeType.SMALL_JUNGLE;
            var5 = new WorldGenTrees(true, 4 + var4.nextInt(7), var9, var10, false);
         }
         break;
      case 4:
         treeType = TreeType.ACACIA;
         var5 = new WorldGenSavannaTree(true);
         break;
      case 5:
         label96:
         for(var6 = 0; var6 >= -1; --var6) {
            for(var7 = 0; var7 >= -1; --var7) {
               if (this.isTwoByTwoOfType(var1, var2, var6, var7, BlockPlanks.EnumType.DARK_OAK)) {
                  treeType = TreeType.DARK_OAK;
                  var5 = new WorldGenCanopyTree(true);
                  var8 = true;
                  break label96;
               }
            }
         }

         if (!var8) {
            return;
         }
      case 6:
      }

      IBlockState var11 = Blocks.AIR.getDefaultState();
      if (var8) {
         var1.setBlockState(var2.add(var6, 0, var7), var11, 4);
         var1.setBlockState(var2.add(var6 + 1, 0, var7), var11, 4);
         var1.setBlockState(var2.add(var6, 0, var7 + 1), var11, 4);
         var1.setBlockState(var2.add(var6 + 1, 0, var7 + 1), var11, 4);
      } else {
         var1.setBlockState(var2, var11, 4);
      }

      if (!((WorldGenerator)var5).generate(var1, var4, var2.add(var6, 0, var7))) {
         if (var8) {
            var1.setBlockState(var2.add(var6, 0, var7), var3, 4);
            var1.setBlockState(var2.add(var6 + 1, 0, var7), var3, 4);
            var1.setBlockState(var2.add(var6, 0, var7 + 1), var3, 4);
            var1.setBlockState(var2.add(var6 + 1, 0, var7 + 1), var3, 4);
         } else {
            var1.setBlockState(var2, var3, 4);
         }
      }

   }

   private boolean isTwoByTwoOfType(World var1, BlockPos var2, int var3, int var4, BlockPlanks.EnumType var5) {
      return this.isTypeAt(var1, var2.add(var3, 0, var4), var5) && this.isTypeAt(var1, var2.add(var3 + 1, 0, var4), var5) && this.isTypeAt(var1, var2.add(var3, 0, var4 + 1), var5) && this.isTypeAt(var1, var2.add(var3 + 1, 0, var4 + 1), var5);
   }

   public boolean isTypeAt(World var1, BlockPos var2, BlockPlanks.EnumType var3) {
      IBlockState var4 = var1.getBlockState(var2);
      return var4.getBlock() == this && var4.getValue(TYPE) == var3;
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockPlanks.EnumType)var1.getValue(TYPE)).getMetadata();
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return true;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return (double)var1.rand.nextFloat() < 0.45D;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.grow(var1, var3, var4, var2);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(TYPE, BlockPlanks.EnumType.byMetadata(var1 & 7)).withProperty(STAGE, Integer.valueOf((var1 & 8) >> 3));
   }

   public int getMetaFromState(IBlockState var1) {
      byte var2 = 0;
      int var3 = var2 | ((BlockPlanks.EnumType)var1.getValue(TYPE)).getMetadata();
      var3 = var3 | ((Integer)var1.getValue(STAGE)).intValue() << 3;
      return var3;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TYPE, STAGE});
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[BlockPlanks.EnumType.values().length];

      static {
         try {
            a[BlockPlanks.EnumType.SPRUCE.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[BlockPlanks.EnumType.BIRCH.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[BlockPlanks.EnumType.JUNGLE.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[BlockPlanks.EnumType.ACACIA.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[BlockPlanks.EnumType.DARK_OAK.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[BlockPlanks.EnumType.OAK.ordinal()] = 6;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
