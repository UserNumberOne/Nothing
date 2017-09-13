package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFishFood extends ItemFood {
   private final boolean cooked;

   public ItemFishFood(boolean var1) {
      super(0, 0.0F, false);
      this.cooked = var1;
   }

   public int getHealAmount(ItemStack var1) {
      ItemFishFood.FishType var2 = ItemFishFood.FishType.byItemStack(var1);
      return this.cooked && var2.canCook() ? var2.getCookedHealAmount() : var2.getUncookedHealAmount();
   }

   public float getSaturationModifier(ItemStack var1) {
      ItemFishFood.FishType var2 = ItemFishFood.FishType.byItemStack(var1);
      return this.cooked && var2.canCook() ? var2.getCookedSaturationModifier() : var2.getUncookedSaturationModifier();
   }

   protected void onFoodEaten(ItemStack var1, World var2, EntityPlayer var3) {
      ItemFishFood.FishType var4 = ItemFishFood.FishType.byItemStack(var1);
      if (var4 == ItemFishFood.FishType.PUFFERFISH) {
         var3.addPotionEffect(new PotionEffect(MobEffects.POISON, 1200, 3));
         var3.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 300, 2));
         var3.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 1));
      }

      super.onFoodEaten(var1, var2, var3);
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(ItemFishFood.FishType var7 : ItemFishFood.FishType.values()) {
         if (!this.cooked || var7.canCook()) {
            var3.add(new ItemStack(this, 1, var7.getMetadata()));
         }
      }

   }

   public String getUnlocalizedName(ItemStack var1) {
      ItemFishFood.FishType var2 = ItemFishFood.FishType.byItemStack(var1);
      return this.getUnlocalizedName() + "." + var2.getUnlocalizedName() + "." + (this.cooked && var2.canCook() ? "cooked" : "raw");
   }

   public static enum FishType {
      COD(0, "cod", 2, 0.1F, 5, 0.6F),
      SALMON(1, "salmon", 2, 0.1F, 6, 0.8F),
      CLOWNFISH(2, "clownfish", 1, 0.1F),
      PUFFERFISH(3, "pufferfish", 1, 0.1F);

      private static final Map META_LOOKUP = Maps.newHashMap();
      private final int meta;
      private final String unlocalizedName;
      private final int uncookedHealAmount;
      private final float uncookedSaturationModifier;
      private final int cookedHealAmount;
      private final float cookedSaturationModifier;
      private boolean cookable;

      private FishType(int var3, String var4, int var5, float var6, int var7, float var8) {
         this.meta = var3;
         this.unlocalizedName = var4;
         this.uncookedHealAmount = var5;
         this.uncookedSaturationModifier = var6;
         this.cookedHealAmount = var7;
         this.cookedSaturationModifier = var8;
         this.cookable = true;
      }

      private FishType(int var3, String var4, int var5, float var6) {
         this.meta = var3;
         this.unlocalizedName = var4;
         this.uncookedHealAmount = var5;
         this.uncookedSaturationModifier = var6;
         this.cookedHealAmount = 0;
         this.cookedSaturationModifier = 0.0F;
         this.cookable = false;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      public int getUncookedHealAmount() {
         return this.uncookedHealAmount;
      }

      public float getUncookedSaturationModifier() {
         return this.uncookedSaturationModifier;
      }

      public int getCookedHealAmount() {
         return this.cookedHealAmount;
      }

      public float getCookedSaturationModifier() {
         return this.cookedSaturationModifier;
      }

      public boolean canCook() {
         return this.cookable;
      }

      public static ItemFishFood.FishType byMetadata(int var0) {
         ItemFishFood.FishType var1 = (ItemFishFood.FishType)META_LOOKUP.get(Integer.valueOf(var0));
         return var1 == null ? COD : var1;
      }

      public static ItemFishFood.FishType byItemStack(ItemStack var0) {
         return var0.getItem() instanceof ItemFishFood ? byMetadata(var0.getMetadata()) : COD;
      }

      static {
         for(ItemFishFood.FishType var3 : values()) {
            META_LOOKUP.put(Integer.valueOf(var3.getMetadata()), var3);
         }

      }
   }
}
