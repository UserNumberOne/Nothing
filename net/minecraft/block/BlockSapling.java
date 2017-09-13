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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSapling extends BlockBush implements IGrowable {
   public static final PropertyEnum TYPE = PropertyEnum.create("type", BlockPlanks.EnumType.class);
   public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);
   protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);

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
            this.grow(var1, var2, var3, var4);
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
      if (TerrainGen.saplingGrowTree(var1, var4, var2)) {
         Object var5 = var4.nextInt(10) == 0 ? new WorldGenBigTree(true) : new WorldGenTrees(true);
         int var6 = 0;
         int var7 = 0;
         boolean var8 = false;
         switch((BlockPlanks.EnumType)var3.getValue(TYPE)) {
         case SPRUCE:
            label70:
            for(var6 = 0; var6 >= -1; --var6) {
               for(var7 = 0; var7 >= -1; --var7) {
                  if (this.isTwoByTwoOfType(var1, var2, var6, var7, BlockPlanks.EnumType.SPRUCE)) {
                     var5 = new WorldGenMegaPineTree(false, var4.nextBoolean());
                     var8 = true;
                     break label70;
                  }
               }
            }

            if (!var8) {
               var6 = 0;
               var7 = 0;
               var5 = new WorldGenTaiga2(true);
            }
            break;
         case BIRCH:
            var5 = new WorldGenBirchTree(true, false);
            break;
         case JUNGLE:
            IBlockState var9 = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
            IBlockState var10 = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

            label84:
            for(var6 = 0; var6 >= -1; --var6) {
               for(var7 = 0; var7 >= -1; --var7) {
                  if (this.isTwoByTwoOfType(var1, var2, var6, var7, BlockPlanks.EnumType.JUNGLE)) {
                     var5 = new WorldGenMegaJungle(true, 10, 20, var9, var10);
                     var8 = true;
                     break label84;
                  }
               }
            }

            if (!var8) {
               var6 = 0;
               var7 = 0;
               var5 = new WorldGenTrees(true, 4 + var4.nextInt(7), var9, var10, false);
            }
            break;
         case ACACIA:
            var5 = new WorldGenSavannaTree(true);
            break;
         case DARK_OAK:
            label98:
            for(var6 = 0; var6 >= -1; --var6) {
               for(var7 = 0; var7 >= -1; --var7) {
                  if (this.isTwoByTwoOfType(var1, var2, var6, var7, BlockPlanks.EnumType.DARK_OAK)) {
                     var5 = new WorldGenCanopyTree(true);
                     var8 = true;
                     break label98;
                  }
               }
            }

            if (!var8) {
               return;
            }
         case OAK:
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

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(BlockPlanks.EnumType var7 : BlockPlanks.EnumType.values()) {
         var3.add(new ItemStack(var1, 1, var7.getMetadata()));
      }

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
      int var2 = 0;
      var2 = var2 | ((BlockPlanks.EnumType)var1.getValue(TYPE)).getMetadata();
      var2 = var2 | ((Integer)var1.getValue(STAGE)).intValue() << 3;
      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TYPE, STAGE});
   }
}
