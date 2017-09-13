package net.minecraft.client.renderer.color;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockStem;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockColors {
   private final Map blockColorMap = Maps.newHashMap();

   public static BlockColors init() {
      final BlockColors blockcolors = new BlockColors();
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (BlockDoublePlant.EnumPlantType)state.getValue(BlockDoublePlant.VARIANT);
            return worldIn != null && pos != null && (blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.GRASS || blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.FERN) ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos) : -1;
         }
      }, Blocks.DOUBLE_PLANT);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            if (worldIn != null && pos != null) {
               TileEntity tileentity = worldIn.getTileEntity(pos);
               if (tileentity instanceof TileEntityFlowerPot) {
                  Item item = ((TileEntityFlowerPot)tileentity).getFlowerPotItem();
                  if (item instanceof ItemBlock) {
                     IBlockState iblockstate = Block.getBlockFromItem(item).getDefaultState();
                     return blockcolors.colorMultiplier(iblockstate, worldIn, pos, tintIndex);
                  }
               }

               return -1;
            } else {
               return -1;
            }
         }
      }, Blocks.FLOWER_POT);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos) : ColorizerGrass.getGrassColor(0.5D, 1.0D);
         }
      }, Blocks.GRASS);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            BlockPlanks.EnumType blockplanks$enumtype = (BlockPlanks.EnumType)state.getValue(BlockOldLeaf.VARIANT);
            return blockplanks$enumtype == BlockPlanks.EnumType.SPRUCE ? ColorizerFoliage.getFoliageColorPine() : (blockplanks$enumtype == BlockPlanks.EnumType.BIRCH ? ColorizerFoliage.getFoliageColorBirch() : (worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos) : ColorizerFoliage.getFoliageColorBasic()));
         }
      }, Blocks.LEAVES);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos) : ColorizerFoliage.getFoliageColorBasic();
         }
      }, Blocks.LEAVES2);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? BiomeColorHelper.getWaterColorAtPos(worldIn, pos) : -1;
         }
      }, Blocks.WATER, Blocks.FLOWING_WATER);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return BlockRedstoneWire.colorMultiplier(((Integer)state.getValue(BlockRedstoneWire.POWER)).intValue());
         }
      }, Blocks.REDSTONE_WIRE);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos) : -1;
         }
      }, Blocks.REEDS);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            int i = ((Integer)state.getValue(BlockStem.AGE)).intValue();
            int j = i * 32;
            int k = 255 - i * 8;
            int l = i * 4;
            return j << 16 | k << 8 | l;
         }
      }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos) : (state.getValue(BlockTallGrass.TYPE) == BlockTallGrass.EnumType.DEAD_BUSH ? 16777215 : ColorizerGrass.getGrassColor(0.5D, 1.0D));
         }
      }, Blocks.TALLGRASS);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos) : ColorizerFoliage.getFoliageColorBasic();
         }
      }, Blocks.VINE);
      blockcolors.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            return worldIn != null && pos != null ? 2129968 : 7455580;
         }
      }, Blocks.WATERLILY);
      return blockcolors;
   }

   public int getColor(IBlockState p_189991_1_) {
      IBlockColor iblockcolor = (IBlockColor)this.blockColorMap.get(p_189991_1_.getBlock().delegate);
      if (iblockcolor != null) {
         return iblockcolor.colorMultiplier(p_189991_1_, (IBlockAccess)null, (BlockPos)null, 0);
      } else {
         MapColor mapcolor = p_189991_1_.getMapColor();
         return mapcolor != null ? mapcolor.colorValue : -1;
      }
   }

   public int colorMultiplier(IBlockState state, @Nullable IBlockAccess blockAccess, @Nullable BlockPos pos, int renderPass) {
      IBlockColor iblockcolor = (IBlockColor)this.blockColorMap.get(state.getBlock().delegate);
      return iblockcolor == null ? -1 : iblockcolor.colorMultiplier(state, blockAccess, pos, renderPass);
   }

   public void registerBlockColorHandler(IBlockColor blockColor, Block... blocksIn) {
      for(Block block : blocksIn) {
         if (block == null) {
            throw new IllegalArgumentException("Block registered to block color handler cannot be null!");
         }

         if (block.getRegistryName() == null) {
            throw new IllegalArgumentException("Block must be registered before assigning color handler.");
         }

         this.blockColorMap.put(block.delegate, blockColor);
      }

   }
}
