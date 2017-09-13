package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
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
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockMycelium extends Block {
   public static final PropertyBool SNOWY = PropertyBool.create("snowy");

   protected BlockMycelium() {
      super(Material.GRASS, MapColor.PURPLE);
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
               IBlockState var7 = var1.getBlockState(var6);
               IBlockState var8 = var1.getBlockState(var6.up());
               if (var7.getBlock() == Blocks.DIRT && var7.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && var1.getLightFromNeighbors(var6.up()) >= 4 && var8.getLightOpacity() <= 2) {
                  CraftWorld var9 = var1.getWorld();
                  BlockState var10 = var9.getBlockAt(var6.getX(), var6.getY(), var6.getZ()).getState();
                  var10.setType(CraftMagicNumbers.getMaterial(this));
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

   public int getMetaFromState(IBlockState var1) {
      return 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{SNOWY});
   }
}
