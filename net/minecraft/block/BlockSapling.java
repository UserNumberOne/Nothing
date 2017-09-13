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
      if (!worldIn.isRemote) {
         super.updateTick(worldIn, pos, state, rand);
         if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
            this.grow(worldIn, pos, state, rand);
         }
      }

   }

   public void grow(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (((Integer)state.getValue(STAGE)).intValue() == 0) {
         worldIn.setBlockState(pos, state.cycleProperty(STAGE), 4);
      } else {
         this.generateTree(worldIn, pos, state, rand);
      }

   }

   public void generateTree(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (TerrainGen.saplingGrowTree(worldIn, rand, pos)) {
         WorldGenerator worldgenerator = (WorldGenerator)(rand.nextInt(10) == 0 ? new WorldGenBigTree(true) : new WorldGenTrees(true));
         int i = 0;
         int j = 0;
         boolean flag = false;
         switch((BlockPlanks.EnumType)state.getValue(TYPE)) {
         case SPRUCE:
            label70:
            for(i = 0; i >= -1; --i) {
               for(j = 0; j >= -1; --j) {
                  if (this.isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.SPRUCE)) {
                     worldgenerator = new WorldGenMegaPineTree(false, rand.nextBoolean());
                     flag = true;
                     break label70;
                  }
               }
            }

            if (!flag) {
               i = 0;
               j = 0;
               worldgenerator = new WorldGenTaiga2(true);
            }
            break;
         case BIRCH:
            worldgenerator = new WorldGenBirchTree(true, false);
            break;
         case JUNGLE:
            IBlockState iblockstate = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
            IBlockState iblockstate1 = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

            label84:
            for(i = 0; i >= -1; --i) {
               for(j = 0; j >= -1; --j) {
                  if (this.isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.JUNGLE)) {
                     worldgenerator = new WorldGenMegaJungle(true, 10, 20, iblockstate, iblockstate1);
                     flag = true;
                     break label84;
                  }
               }
            }

            if (!flag) {
               i = 0;
               j = 0;
               worldgenerator = new WorldGenTrees(true, 4 + rand.nextInt(7), iblockstate, iblockstate1, false);
            }
            break;
         case ACACIA:
            worldgenerator = new WorldGenSavannaTree(true);
            break;
         case DARK_OAK:
            label98:
            for(i = 0; i >= -1; --i) {
               for(j = 0; j >= -1; --j) {
                  if (this.isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.DARK_OAK)) {
                     worldgenerator = new WorldGenCanopyTree(true);
                     flag = true;
                     break label98;
                  }
               }
            }

            if (!flag) {
               return;
            }
         case OAK:
         }

         IBlockState iblockstate2 = Blocks.AIR.getDefaultState();
         if (flag) {
            worldIn.setBlockState(pos.add(i, 0, j), iblockstate2, 4);
            worldIn.setBlockState(pos.add(i + 1, 0, j), iblockstate2, 4);
            worldIn.setBlockState(pos.add(i, 0, j + 1), iblockstate2, 4);
            worldIn.setBlockState(pos.add(i + 1, 0, j + 1), iblockstate2, 4);
         } else {
            worldIn.setBlockState(pos, iblockstate2, 4);
         }

         if (!worldgenerator.generate(worldIn, rand, pos.add(i, 0, j))) {
            if (flag) {
               worldIn.setBlockState(pos.add(i, 0, j), state, 4);
               worldIn.setBlockState(pos.add(i + 1, 0, j), state, 4);
               worldIn.setBlockState(pos.add(i, 0, j + 1), state, 4);
               worldIn.setBlockState(pos.add(i + 1, 0, j + 1), state, 4);
            } else {
               worldIn.setBlockState(pos, state, 4);
            }
         }

      }
   }

   private boolean isTwoByTwoOfType(World var1, BlockPos var2, int var3, int var4, BlockPlanks.EnumType var5) {
      return this.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_), type) && this.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_), type) && this.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_ + 1), type) && this.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_ + 1), type);
   }

   public boolean isTypeAt(World var1, BlockPos var2, BlockPlanks.EnumType var3) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      return iblockstate.getBlock() == this && iblockstate.getValue(TYPE) == type;
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockPlanks.EnumType)state.getValue(TYPE)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(BlockPlanks.EnumType blockplanks$enumtype : BlockPlanks.EnumType.values()) {
         list.add(new ItemStack(itemIn, 1, blockplanks$enumtype.getMetadata()));
      }

   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return true;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return (double)worldIn.rand.nextFloat() < 0.45D;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.grow(worldIn, pos, state, rand);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(TYPE, BlockPlanks.EnumType.byMetadata(meta & 7)).withProperty(STAGE, Integer.valueOf((meta & 8) >> 3));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((BlockPlanks.EnumType)state.getValue(TYPE)).getMetadata();
      i = i | ((Integer)state.getValue(STAGE)).intValue() << 3;
      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TYPE, STAGE});
   }
}
