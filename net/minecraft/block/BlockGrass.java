package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockGrass extends Block implements IGrowable {
   public static final PropertyBool SNOWY = PropertyBool.create("snowy");

   protected BlockGrass() {
      super(Material.GRASS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(SNOWY, Boolean.valueOf(false)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      Block var4 = var2.getBlockState(var3.up()).getBlock();
      return var1.withProperty(SNOWY, Boolean.valueOf(var4 == Blocks.SNOW || var4 == Blocks.SNOW_LAYER));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote) {
         if (var1.getLightFromNeighbors(var2.up()) < 4 && var1.getBlockState(var2.up()).getLightOpacity() > 2) {
            CraftWorld var12 = var1.getWorld();
            BlockState var13 = var12.getBlockAt(var2.getX(), var2.getY(), var2.getZ()).getState();
            var13.setType(CraftMagicNumbers.getMaterial(Blocks.DIRT));
            BlockFadeEvent var14 = new BlockFadeEvent(var13.getBlock(), var13);
            var1.getServer().getPluginManager().callEvent(var14);
            if (!var14.isCancelled()) {
               var13.update(true);
            }
         } else if (var1.getLightFromNeighbors(var2.up()) >= 9) {
            for(int var5 = 0; var5 < 4; ++var5) {
               BlockPos var6 = var2.add(var4.nextInt(3) - 1, var4.nextInt(5) - 3, var4.nextInt(3) - 1);
               if (var6.getY() >= 0 && var6.getY() < 256 && !var1.isBlockLoaded(var6)) {
                  return;
               }

               IBlockState var7 = var1.getBlockState(var6.up());
               IBlockState var8 = var1.getBlockState(var6);
               if (var8.getBlock() == Blocks.DIRT && var8.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && var1.getLightFromNeighbors(var6.up()) >= 4 && var7.getLightOpacity() <= 2) {
                  CraftWorld var9 = var1.getWorld();
                  BlockState var10 = var9.getBlockAt(var6.getX(), var6.getY(), var6.getZ()).getState();
                  var10.setType(CraftMagicNumbers.getMaterial(Blocks.GRASS));
                  BlockSpreadEvent var11 = new BlockSpreadEvent(var10.getBlock(), var9.getBlockAt(var2.getX(), var2.getY(), var2.getZ()), var10);
                  var1.getServer().getPluginManager().callEvent(var11);
                  if (!var11.isCancelled()) {
                     var10.update(true);
                  }
               }
            }
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), var2, var3);
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return true;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return true;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      BlockPos var5 = var3.up();

      for(int var6 = 0; var6 < 128; ++var6) {
         BlockPos var7 = var5;
         int var8 = 0;

         while(true) {
            if (var8 >= var6 / 16) {
               if (var1.getBlockState(var7).getBlock().blockMaterial == Material.AIR) {
                  if (var2.nextInt(8) == 0) {
                     BlockFlower.EnumFlowerType var9 = var1.getBiome(var7).pickRandomFlower(var2, var7);
                     BlockFlower var10 = var9.getBlockType().getBlock();
                     IBlockState var11 = var10.getDefaultState().withProperty(var10.getTypeProperty(), var9);
                     if (var10.canBlockStay(var1, var7, var11)) {
                        CraftEventFactory.handleBlockGrowEvent(var1, var7.getX(), var7.getY(), var7.getZ(), var11.getBlock(), var11.getBlock().getMetaFromState(var11));
                     }
                  } else {
                     IBlockState var12 = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
                     if (Blocks.TALLGRASS.canBlockStay(var1, var7, var12)) {
                        CraftEventFactory.handleBlockGrowEvent(var1, var7.getX(), var7.getY(), var7.getZ(), var12.getBlock(), var12.getBlock().getMetaFromState(var12));
                     }
                  }
               }
               break;
            }

            var7 = var7.add(var2.nextInt(3) - 1, (var2.nextInt(3) - 1) * var2.nextInt(3) / 2, var2.nextInt(3) - 1);
            if (var1.getBlockState(var7.down()).getBlock() != Blocks.GRASS || var1.getBlockState(var7).isNormalCube()) {
               break;
            }

            ++var8;
         }
      }

   }

   public int getMetaFromState(IBlockState var1) {
      return 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{SNOWY});
   }
}
