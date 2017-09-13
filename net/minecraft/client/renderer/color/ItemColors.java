package net.minecraft.client.renderer.color;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFireworkCharge;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemColors {
   private final Map itemColorMap = Maps.newHashMap();

   public static ItemColors init(final BlockColors var0) {
      ItemColors itemcolors = new ItemColors();
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return tintIndex > 0 ? -1 : ((ItemArmor)stack.getItem()).getColor(stack);
         }
      }, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return tintIndex > 0 ? -1 : ItemBanner.getBaseColor(stack).getMapColor().colorValue;
         }
      }, Items.BANNER, Items.SHIELD);
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = BlockDoublePlant.EnumPlantType.byMetadata(stack.getMetadata());
            return blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.GRASS && blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.FERN ? -1 : ColorizerGrass.getGrassColor(0.5D, 1.0D);
         }
      }, Blocks.DOUBLE_PLANT);
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            if (tintIndex != 1) {
               return -1;
            } else {
               NBTBase nbtbase = ItemFireworkCharge.getExplosionTag(stack, "Colors");
               if (!(nbtbase instanceof NBTTagIntArray)) {
                  return 9079434;
               } else {
                  int[] aint = ((NBTTagIntArray)nbtbase).getIntArray();
                  if (aint.length == 1) {
                     return aint[0];
                  } else {
                     int i = 0;
                     int j = 0;
                     int k = 0;

                     for(int l : aint) {
                        i += (l & 16711680) >> 16;
                        j += (l & '\uff00') >> 8;
                        k += (l & 255) >> 0;
                     }

                     i = i / aint.length;
                     j = j / aint.length;
                     k = k / aint.length;
                     return i << 16 | j << 8 | k;
                  }
               }
            }
         }
      }, Items.FIREWORK_CHARGE);
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return tintIndex > 0 ? -1 : PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(stack));
         }
      }, Items.POTIONITEM, Items.SPLASH_POTION, Items.LINGERING_POTION);
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            EntityList.EntityEggInfo entitylist$entityegginfo = (EntityList.EntityEggInfo)EntityList.ENTITY_EGGS.get(ItemMonsterPlacer.getEntityIdFromItem(stack));
            return entitylist$entityegginfo == null ? -1 : (tintIndex == 0 ? entitylist$entityegginfo.primaryColor : entitylist$entityegginfo.secondaryColor);
         }
      }, Items.SPAWN_EGG);
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            IBlockState iblockstate = ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
            return p_186729_0_.colorMultiplier(iblockstate, (IBlockAccess)null, (BlockPos)null, tintIndex);
         }
      }, Blocks.GRASS, Blocks.TALLGRASS, Blocks.VINE, Blocks.LEAVES, Blocks.LEAVES2, Blocks.WATERLILY);
      itemcolors.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return tintIndex == 0 ? PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(stack)) : -1;
         }
      }, Items.TIPPED_ARROW);
      return itemcolors;
   }

   public int getColorFromItemstack(ItemStack var1, int var2) {
      IItemColor iitemcolor = (IItemColor)this.itemColorMap.get(stack.getItem().delegate);
      return iitemcolor == null ? -1 : iitemcolor.getColorFromItemstack(stack, tintIndex);
   }

   public void registerItemColorHandler(IItemColor var1, Block... var2) {
      for(Block block : blocksIn) {
         if (block == null) {
            throw new IllegalArgumentException("Block registered to item color handler cannot be null!");
         }

         if (block.getRegistryName() == null) {
            throw new IllegalArgumentException("Block must be registered before assigning color handler.");
         }

         this.itemColorMap.put(Item.getItemFromBlock(block).delegate, itemColor);
      }

   }

   public void registerItemColorHandler(IItemColor var1, Item... var2) {
      for(Item item : itemsIn) {
         if (item == null) {
            throw new IllegalArgumentException("Item registered to item color handler cannot be null!");
         }

         if (item.getRegistryName() == null) {
            throw new IllegalArgumentException("Item must be registered before assigning color handler.");
         }

         this.itemColorMap.put(item.delegate, itemColor);
      }

   }
}
