package net.minecraft.entity.passive;

import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum HorseArmorType {
   NONE(0),
   IRON(5, "iron", "meo"),
   GOLD(7, "gold", "goo"),
   DIAMOND(11, "diamond", "dio");

   private final String textureName;
   private final String hash;
   private final int protection;

   private HorseArmorType(int var3) {
      this.protection = var3;
      this.textureName = null;
      this.hash = "";
   }

   private HorseArmorType(int var3, String var4, String var5) {
      this.protection = var3;
      this.textureName = "textures/entity/horse/armor/horse_armor_" + var4 + ".png";
      this.hash = var5;
   }

   public int getOrdinal() {
      return this.ordinal();
   }

   @SideOnly(Side.CLIENT)
   public String getHash() {
      return this.hash;
   }

   public int getProtection() {
      return this.protection;
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public String getTextureName() {
      return this.textureName;
   }

   public static HorseArmorType getByOrdinal(int var0) {
      return values()[var0];
   }

   public static HorseArmorType getByItemStack(@Nullable ItemStack var0) {
      return var0 == null ? NONE : getByItem(var0.getItem());
   }

   public static HorseArmorType getByItem(@Nullable Item var0) {
      return var0 == Items.IRON_HORSE_ARMOR ? IRON : (var0 == Items.GOLDEN_HORSE_ARMOR ? GOLD : (var0 == Items.DIAMOND_HORSE_ARMOR ? DIAMOND : NONE));
   }

   public static boolean isHorseArmor(@Nullable Item var0) {
      return getByItem(var0) != NONE;
   }
}
