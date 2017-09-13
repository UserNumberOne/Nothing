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
      ItemColors var1 = new ItemColors();
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return var2 > 0 ? -1 : ((ItemArmor)var1.getItem()).getColor(var1);
         }
      }, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return var2 > 0 ? -1 : ItemBanner.getBaseColor(var1).getMapColor().colorValue;
         }
      }, Items.BANNER, Items.SHIELD);
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            BlockDoublePlant.EnumPlantType var3 = BlockDoublePlant.EnumPlantType.byMetadata(var1.getMetadata());
            return var3 != BlockDoublePlant.EnumPlantType.GRASS && var3 != BlockDoublePlant.EnumPlantType.FERN ? -1 : ColorizerGrass.getGrassColor(0.5D, 1.0D);
         }
      }, Blocks.DOUBLE_PLANT);
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            if (var2 != 1) {
               return -1;
            } else {
               NBTBase var3 = ItemFireworkCharge.getExplosionTag(var1, "Colors");
               if (!(var3 instanceof NBTTagIntArray)) {
                  return 9079434;
               } else {
                  int[] var4 = ((NBTTagIntArray)var3).getIntArray();
                  if (var4.length == 1) {
                     return var4[0];
                  } else {
                     int var5 = 0;
                     int var6 = 0;
                     int var7 = 0;

                     for(int var11 : var4) {
                        var5 += (var11 & 16711680) >> 16;
                        var6 += (var11 & '\uff00') >> 8;
                        var7 += (var11 & 255) >> 0;
                     }

                     var5 = var5 / var4.length;
                     var6 = var6 / var4.length;
                     var7 = var7 / var4.length;
                     return var5 << 16 | var6 << 8 | var7;
                  }
               }
            }
         }
      }, Items.FIREWORK_CHARGE);
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return var2 > 0 ? -1 : PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(var1));
         }
      }, Items.POTIONITEM, Items.SPLASH_POTION, Items.LINGERING_POTION);
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            EntityList.EntityEggInfo var3 = (EntityList.EntityEggInfo)EntityList.ENTITY_EGGS.get(ItemMonsterPlacer.getEntityIdFromItem(var1));
            return var3 == null ? -1 : (var2 == 0 ? var3.primaryColor : var3.secondaryColor);
         }
      }, Items.SPAWN_EGG);
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            IBlockState var3 = ((ItemBlock)var1.getItem()).getBlock().getStateFromMeta(var1.getMetadata());
            return var0.colorMultiplier(var3, (IBlockAccess)null, (BlockPos)null, var2);
         }
      }, Blocks.GRASS, Blocks.TALLGRASS, Blocks.VINE, Blocks.LEAVES, Blocks.LEAVES2, Blocks.WATERLILY);
      var1.registerItemColorHandler(new IItemColor() {
         public int getColorFromItemstack(ItemStack var1, int var2) {
            return var2 == 0 ? PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(var1)) : -1;
         }
      }, Items.TIPPED_ARROW);
      return var1;
   }

   public int getColorFromItemstack(ItemStack var1, int var2) {
      IItemColor var3 = (IItemColor)this.itemColorMap.get(var1.getItem().delegate);
      return var3 == null ? -1 : var3.getColorFromItemstack(var1, var2);
   }

   public void registerItemColorHandler(IItemColor var1, Block... var2) {
      for(Block var6 : var2) {
         if (var6 == null) {
            throw new IllegalArgumentException("Block registered to item color handler cannot be null!");
         }

         if (var6.getRegistryName() == null) {
            throw new IllegalArgumentException("Block must be registered before assigning color handler.");
         }

         this.itemColorMap.put(Item.getItemFromBlock(var6).delegate, var1);
      }

   }

   public void registerItemColorHandler(IItemColor var1, Item... var2) {
      for(Item var6 : var2) {
         if (var6 == null) {
            throw new IllegalArgumentException("Item registered to item color handler cannot be null!");
         }

         if (var6.getRegistryName() == null) {
            throw new IllegalArgumentException("Item must be registered before assigning color handler.");
         }

         this.itemColorMap.put(var6.delegate, var1);
      }

   }
}
