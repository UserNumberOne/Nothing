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
      this.cooked = cooked;
   }

   public int getHealAmount(ItemStack var1) {
      ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);
      return this.cooked && itemfishfood$fishtype.canCook() ? itemfishfood$fishtype.getCookedHealAmount() : itemfishfood$fishtype.getUncookedHealAmount();
   }

   public float getSaturationModifier(ItemStack var1) {
      ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);
      return this.cooked && itemfishfood$fishtype.canCook() ? itemfishfood$fishtype.getCookedSaturationModifier() : itemfishfood$fishtype.getUncookedSaturationModifier();
   }

   protected void onFoodEaten(ItemStack var1, World var2, EntityPlayer var3) {
      ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);
      if (itemfishfood$fishtype == ItemFishFood.FishType.PUFFERFISH) {
         player.addPotionEffect(new PotionEffect(MobEffects.POISON, 1200, 3));
         player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 300, 2));
         player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 1));
      }

      super.onFoodEaten(stack, worldIn, player);
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(ItemFishFood.FishType itemfishfood$fishtype : ItemFishFood.FishType.values()) {
         if (!this.cooked || itemfishfood$fishtype.canCook()) {
            subItems.add(new ItemStack(this, 1, itemfishfood$fishtype.getMetadata()));
         }
      }

   }

   public String getUnlocalizedName(ItemStack var1) {
      ItemFishFood.FishType itemfishfood$fishtype = ItemFishFood.FishType.byItemStack(stack);
      return this.getUnlocalizedName() + "." + itemfishfood$fishtype.getUnlocalizedName() + "." + (this.cooked && itemfishfood$fishtype.canCook() ? "cooked" : "raw");
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
         this.meta = meta;
         this.unlocalizedName = unlocalizedName;
         this.uncookedHealAmount = uncookedHeal;
         this.uncookedSaturationModifier = uncookedSaturation;
         this.cookedHealAmount = cookedHeal;
         this.cookedSaturationModifier = cookedSaturation;
         this.cookable = true;
      }

      private FishType(int var3, String var4, int var5, float var6) {
         this.meta = meta;
         this.unlocalizedName = unlocalizedName;
         this.uncookedHealAmount = uncookedHeal;
         this.uncookedSaturationModifier = uncookedSaturation;
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
         ItemFishFood.FishType itemfishfood$fishtype = (ItemFishFood.FishType)META_LOOKUP.get(Integer.valueOf(meta));
         return itemfishfood$fishtype == null ? COD : itemfishfood$fishtype;
      }

      public static ItemFishFood.FishType byItemStack(ItemStack var0) {
         return stack.getItem() instanceof ItemFishFood ? byMetadata(stack.getMetadata()) : COD;
      }

      static {
         for(ItemFishFood.FishType itemfishfood$fishtype : values()) {
            META_LOOKUP.put(Integer.valueOf(itemfishfood$fishtype.getMetadata()), itemfishfood$fishtype);
         }

      }
   }
}
