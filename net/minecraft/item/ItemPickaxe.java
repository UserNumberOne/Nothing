package net.minecraft.item;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class ItemPickaxe extends ItemTool {
   private static final Set EFFECTIVE_ON = Sets.newHashSet(new Block[]{Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE});

   protected ItemPickaxe(Item.ToolMaterial var1) {
      super(1.0F, -2.8F, var1, EFFECTIVE_ON);
   }

   public boolean canHarvestBlock(IBlockState var1) {
      Block var2 = var1.getBlock();
      if (var2 == Blocks.OBSIDIAN) {
         return this.toolMaterial.getHarvestLevel() == 3;
      } else if (var2 != Blocks.DIAMOND_BLOCK && var2 != Blocks.DIAMOND_ORE) {
         if (var2 != Blocks.EMERALD_ORE && var2 != Blocks.EMERALD_BLOCK) {
            if (var2 != Blocks.GOLD_BLOCK && var2 != Blocks.GOLD_ORE) {
               if (var2 != Blocks.IRON_BLOCK && var2 != Blocks.IRON_ORE) {
                  if (var2 != Blocks.LAPIS_BLOCK && var2 != Blocks.LAPIS_ORE) {
                     if (var2 != Blocks.REDSTONE_ORE && var2 != Blocks.LIT_REDSTONE_ORE) {
                        Material var3 = var1.getMaterial();
                        if (var3 == Material.ROCK) {
                           return true;
                        } else if (var3 == Material.IRON) {
                           return true;
                        } else {
                           return var3 == Material.ANVIL;
                        }
                     } else {
                        return this.toolMaterial.getHarvestLevel() >= 2;
                     }
                  } else {
                     return this.toolMaterial.getHarvestLevel() >= 1;
                  }
               } else {
                  return this.toolMaterial.getHarvestLevel() >= 1;
               }
            } else {
               return this.toolMaterial.getHarvestLevel() >= 2;
            }
         } else {
            return this.toolMaterial.getHarvestLevel() >= 2;
         }
      } else {
         return this.toolMaterial.getHarvestLevel() >= 2;
      }
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      Material var3 = var2.getMaterial();
      return var3 != Material.IRON && var3 != Material.ANVIL && var3 != Material.ROCK ? super.getStrVsBlock(var1, var2) : this.efficiencyOnProperMaterial;
   }
}
