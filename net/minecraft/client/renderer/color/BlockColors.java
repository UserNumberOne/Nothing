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
      final BlockColors var0 = new BlockColors();
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            BlockDoublePlant.EnumPlantType var5 = (BlockDoublePlant.EnumPlantType)var1.getValue(BlockDoublePlant.VARIANT);
            return var2 != null && var3 != null && (var5 == BlockDoublePlant.EnumPlantType.GRASS || var5 == BlockDoublePlant.EnumPlantType.FERN) ? BiomeColorHelper.getGrassColorAtPos(var2, var3) : -1;
         }
      }, Blocks.DOUBLE_PLANT);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            if (var2 != null && var3 != null) {
               TileEntity var5 = var2.getTileEntity(var3);
               if (var5 instanceof TileEntityFlowerPot) {
                  Item var6 = ((TileEntityFlowerPot)var5).getFlowerPotItem();
                  if (var6 instanceof ItemBlock) {
                     IBlockState var7 = Block.getBlockFromItem(var6).getDefaultState();
                     return var0.colorMultiplier(var7, var2, var3, var4);
                  }
               }

               return -1;
            } else {
               return -1;
            }
         }
      }, Blocks.FLOWER_POT);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return var2 != null && var3 != null ? BiomeColorHelper.getGrassColorAtPos(var2, var3) : ColorizerGrass.getGrassColor(0.5D, 1.0D);
         }
      }, Blocks.GRASS);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            BlockPlanks.EnumType var5 = (BlockPlanks.EnumType)var1.getValue(BlockOldLeaf.VARIANT);
            return var5 == BlockPlanks.EnumType.SPRUCE ? ColorizerFoliage.getFoliageColorPine() : (var5 == BlockPlanks.EnumType.BIRCH ? ColorizerFoliage.getFoliageColorBirch() : (var2 != null && var3 != null ? BiomeColorHelper.getFoliageColorAtPos(var2, var3) : ColorizerFoliage.getFoliageColorBasic()));
         }
      }, Blocks.LEAVES);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return var2 != null && var3 != null ? BiomeColorHelper.getFoliageColorAtPos(var2, var3) : ColorizerFoliage.getFoliageColorBasic();
         }
      }, Blocks.LEAVES2);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return var2 != null && var3 != null ? BiomeColorHelper.getWaterColorAtPos(var2, var3) : -1;
         }
      }, Blocks.WATER, Blocks.FLOWING_WATER);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return BlockRedstoneWire.colorMultiplier(((Integer)var1.getValue(BlockRedstoneWire.POWER)).intValue());
         }
      }, Blocks.REDSTONE_WIRE);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return var2 != null && var3 != null ? BiomeColorHelper.getGrassColorAtPos(var2, var3) : -1;
         }
      }, Blocks.REEDS);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            int var5 = ((Integer)var1.getValue(BlockStem.AGE)).intValue();
            int var6 = var5 * 32;
            int var7 = 255 - var5 * 8;
            int var8 = var5 * 4;
            return var6 << 16 | var7 << 8 | var8;
         }
      }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return var2 != null && var3 != null ? BiomeColorHelper.getGrassColorAtPos(var2, var3) : (var1.getValue(BlockTallGrass.TYPE) == BlockTallGrass.EnumType.DEAD_BUSH ? 16777215 : ColorizerGrass.getGrassColor(0.5D, 1.0D));
         }
      }, Blocks.TALLGRASS);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return var2 != null && var3 != null ? BiomeColorHelper.getFoliageColorAtPos(var2, var3) : ColorizerFoliage.getFoliageColorBasic();
         }
      }, Blocks.VINE);
      var0.registerBlockColorHandler(new IBlockColor() {
         public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
            return var2 != null && var3 != null ? 2129968 : 7455580;
         }
      }, Blocks.WATERLILY);
      return var0;
   }

   public int getColor(IBlockState var1) {
      IBlockColor var2 = (IBlockColor)this.blockColorMap.get(var1.getBlock().delegate);
      if (var2 != null) {
         return var2.colorMultiplier(var1, (IBlockAccess)null, (BlockPos)null, 0);
      } else {
         MapColor var3 = var1.getMapColor();
         return var3 != null ? var3.colorValue : -1;
      }
   }

   public int colorMultiplier(IBlockState var1, @Nullable IBlockAccess var2, @Nullable BlockPos var3, int var4) {
      IBlockColor var5 = (IBlockColor)this.blockColorMap.get(var1.getBlock().delegate);
      return var5 == null ? -1 : var5.colorMultiplier(var1, var2, var3, var4);
   }

   public void registerBlockColorHandler(IBlockColor var1, Block... var2) {
      for(Block var6 : var2) {
         if (var6 == null) {
            throw new IllegalArgumentException("Block registered to block color handler cannot be null!");
         }

         if (var6.getRegistryName() == null) {
            throw new IllegalArgumentException("Block must be registered before assigning color handler.");
         }

         this.blockColorMap.put(var6.delegate, var1);
      }

   }
}
