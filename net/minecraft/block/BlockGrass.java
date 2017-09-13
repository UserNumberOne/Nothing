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

   public IBlockState getActualState(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      Block block = iblockaccess.getBlockState(blockposition.up()).getBlock();
      return iblockdata.withProperty(SNOWY, Boolean.valueOf(block == Blocks.SNOW || block == Blocks.SNOW_LAYER));
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote) {
         if (world.getLightFromNeighbors(blockposition.up()) < 4 && world.getBlockState(blockposition.up()).getLightOpacity() > 2) {
            org.bukkit.World bworld = world.getWorld();
            BlockState blockState = bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()).getState();
            blockState.setType(CraftMagicNumbers.getMaterial(Blocks.DIRT));
            BlockFadeEvent event = new BlockFadeEvent(blockState.getBlock(), blockState);
            world.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               blockState.update(true);
            }
         } else if (world.getLightFromNeighbors(blockposition.up()) >= 9) {
            for(int i = 0; i < 4; ++i) {
               BlockPos blockposition1 = blockposition.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
               if (blockposition1.getY() >= 0 && blockposition1.getY() < 256 && !world.isBlockLoaded(blockposition1)) {
                  return;
               }

               IBlockState iblockdata1 = world.getBlockState(blockposition1.up());
               IBlockState iblockdata2 = world.getBlockState(blockposition1);
               if (iblockdata2.getBlock() == Blocks.DIRT && iblockdata2.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && world.getLightFromNeighbors(blockposition1.up()) >= 4 && iblockdata1.getLightOpacity() <= 2) {
                  org.bukkit.World bworld = world.getWorld();
                  BlockState blockState = bworld.getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();
                  blockState.setType(CraftMagicNumbers.getMaterial(Blocks.GRASS));
                  BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), blockState);
                  world.getServer().getPluginManager().callEvent(event);
                  if (!event.isCancelled()) {
                     blockState.update(true);
                  }
               }
            }
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), random, i);
   }

   public boolean canGrow(World world, BlockPos blockposition, IBlockState iblockdata, boolean flag) {
      return true;
   }

   public boolean canUseBonemeal(World world, Random random, BlockPos blockposition, IBlockState iblockdata) {
      return true;
   }

   public void grow(World world, Random random, BlockPos blockposition, IBlockState iblockdata) {
      BlockPos blockposition1 = blockposition.up();

      for(int i = 0; i < 128; ++i) {
         BlockPos blockposition2 = blockposition1;
         int j = 0;

         while(true) {
            if (j >= i / 16) {
               if (world.getBlockState(blockposition2).getBlock().blockMaterial == Material.AIR) {
                  if (random.nextInt(8) == 0) {
                     BlockFlower.EnumFlowerType blockflowers_enumflowervarient = world.getBiome(blockposition2).pickRandomFlower(random, blockposition2);
                     BlockFlower blockflowers = blockflowers_enumflowervarient.getBlockType().getBlock();
                     IBlockState iblockdata1 = blockflowers.getDefaultState().withProperty(blockflowers.getTypeProperty(), blockflowers_enumflowervarient);
                     if (blockflowers.canBlockStay(world, blockposition2, iblockdata1)) {
                        CraftEventFactory.handleBlockGrowEvent(world, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), iblockdata1.getBlock(), iblockdata1.getBlock().getMetaFromState(iblockdata1));
                     }
                  } else {
                     IBlockState iblockdata2 = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
                     if (Blocks.TALLGRASS.canBlockStay(world, blockposition2, iblockdata2)) {
                        CraftEventFactory.handleBlockGrowEvent(world, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), iblockdata2.getBlock(), iblockdata2.getBlock().getMetaFromState(iblockdata2));
                     }
                  }
               }
               break;
            }

            blockposition2 = blockposition2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
            if (world.getBlockState(blockposition2.down()).getBlock() != Blocks.GRASS || world.getBlockState(blockposition2).isNormalCube()) {
               break;
            }

            ++j;
         }
      }

   }

   public int getMetaFromState(IBlockState iblockdata) {
      return 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{SNOWY});
   }
}
