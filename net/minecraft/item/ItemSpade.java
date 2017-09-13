package net.minecraft.item;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSpade extends ItemTool {
   private static final Set EFFECTIVE_ON = Sets.newHashSet(new Block[]{Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH});

   public ItemSpade(Item.ToolMaterial var1) {
      super(1.5F, -3.0F, var1, EFFECTIVE_ON);
   }

   public boolean canHarvestBlock(IBlockState var1) {
      Block var2 = var1.getBlock();
      return var2 == Blocks.SNOW_LAYER ? true : var2 == Blocks.SNOW;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (!var2.canPlayerEdit(var4.offset(var6), var6, var1)) {
         return EnumActionResult.FAIL;
      } else {
         IBlockState var10 = var3.getBlockState(var4);
         Block var11 = var10.getBlock();
         if (var6 != EnumFacing.DOWN && var3.getBlockState(var4.up()).getMaterial() == Material.AIR && var11 == Blocks.GRASS) {
            IBlockState var12 = Blocks.GRASS_PATH.getDefaultState();
            var3.playSound(var2, var4, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!var3.isRemote) {
               var3.setBlockState(var4, var12, 11);
               var1.damageItem(1, var2);
            }

            return EnumActionResult.SUCCESS;
         } else {
            return EnumActionResult.PASS;
         }
      }
   }
}
