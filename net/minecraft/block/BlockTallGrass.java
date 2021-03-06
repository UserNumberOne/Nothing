package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTallGrass extends BlockBush implements IGrowable {
   public static final PropertyEnum TYPE = PropertyEnum.create("type", BlockTallGrass.EnumType.class);
   protected static final AxisAlignedBB TALL_GRASS_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);

   protected BlockTallGrass() {
      super(Material.VINE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockTallGrass.EnumType.DEAD_BUSH));
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return TALL_GRASS_AABB;
   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      return this.canSustainBush(var1.getBlockState(var2.down()));
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return var2.nextInt(8) == 0 ? Items.WHEAT_SEEDS : null;
   }

   public int quantityDroppedWithBonus(int var1, Random var2) {
      return 1 + var2.nextInt(var1 * 2 + 1);
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      if (!var1.isRemote && var6 != null && var6.getItem() == Items.SHEARS) {
         var2.addStat(StatList.getBlockStats(this));
         spawnAsEntity(var1, var3, new ItemStack(Blocks.TALLGRASS, 1, ((BlockTallGrass.EnumType)var4.getValue(TYPE)).getMeta()));
      } else {
         super.harvestBlock(var1, var2, var3, var4, var5, var6);
      }

   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this, 1, var3.getBlock().getMetaFromState(var3));
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return var3.getValue(TYPE) != BlockTallGrass.EnumType.DEAD_BUSH;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return true;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      BlockDoublePlant.EnumPlantType var5 = BlockDoublePlant.EnumPlantType.GRASS;
      if (var4.getValue(TYPE) == BlockTallGrass.EnumType.FERN) {
         var5 = BlockDoublePlant.EnumPlantType.FERN;
      }

      if (Blocks.DOUBLE_PLANT.canPlaceBlockAt(var1, var3)) {
         Blocks.DOUBLE_PLANT.placeAt(var1, var3, var5, 2);
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(TYPE, BlockTallGrass.EnumType.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockTallGrass.EnumType)var1.getValue(TYPE)).getMeta();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TYPE});
   }

   public static enum EnumType implements IStringSerializable {
      DEAD_BUSH(0, "dead_bush"),
      GRASS(1, "tall_grass"),
      FERN(2, "fern");

      private static final BlockTallGrass.EnumType[] META_LOOKUP = new BlockTallGrass.EnumType[values().length];
      private final int meta;
      private final String name;

      private EnumType(int var3, String var4) {
         this.meta = var3;
         this.name = var4;
      }

      public int getMeta() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public static BlockTallGrass.EnumType byMetadata(int var0) {
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         return META_LOOKUP[var0];
      }

      public String getName() {
         return this.name;
      }

      static {
         for(BlockTallGrass.EnumType var3 : values()) {
            META_LOOKUP[var3.getMeta()] = var3;
         }

      }
   }
}
