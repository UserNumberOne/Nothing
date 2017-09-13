package net.minecraft.item;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.TextFormatting;

public enum EnumDyeColor implements IStringSerializable {
   WHITE(0, 15, "white", "white", MapColor.SNOW, TextFormatting.WHITE),
   ORANGE(1, 14, "orange", "orange", MapColor.ADOBE, TextFormatting.GOLD),
   MAGENTA(2, 13, "magenta", "magenta", MapColor.MAGENTA, TextFormatting.AQUA),
   LIGHT_BLUE(3, 12, "light_blue", "lightBlue", MapColor.LIGHT_BLUE, TextFormatting.BLUE),
   YELLOW(4, 11, "yellow", "yellow", MapColor.YELLOW, TextFormatting.YELLOW),
   LIME(5, 10, "lime", "lime", MapColor.LIME, TextFormatting.GREEN),
   PINK(6, 9, "pink", "pink", MapColor.PINK, TextFormatting.LIGHT_PURPLE),
   GRAY(7, 8, "gray", "gray", MapColor.GRAY, TextFormatting.DARK_GRAY),
   SILVER(8, 7, "silver", "silver", MapColor.SILVER, TextFormatting.GRAY),
   CYAN(9, 6, "cyan", "cyan", MapColor.CYAN, TextFormatting.DARK_AQUA),
   PURPLE(10, 5, "purple", "purple", MapColor.PURPLE, TextFormatting.DARK_PURPLE),
   BLUE(11, 4, "blue", "blue", MapColor.BLUE, TextFormatting.DARK_BLUE),
   BROWN(12, 3, "brown", "brown", MapColor.BROWN, TextFormatting.GOLD),
   GREEN(13, 2, "green", "green", MapColor.GREEN, TextFormatting.DARK_GREEN),
   RED(14, 1, "red", "red", MapColor.RED, TextFormatting.DARK_RED),
   BLACK(15, 0, "black", "black", MapColor.BLACK, TextFormatting.BLACK);

   private static final EnumDyeColor[] META_LOOKUP = new EnumDyeColor[values().length];
   private static final EnumDyeColor[] DYE_DMG_LOOKUP = new EnumDyeColor[values().length];
   private final int meta;
   private final int dyeDamage;
   private final String name;
   private final String unlocalizedName;
   private final MapColor mapColor;
   private final TextFormatting chatColor;

   private EnumDyeColor(int var3, int var4, String var5, String var6, MapColor var7, TextFormatting var8) {
      this.meta = var3;
      this.dyeDamage = var4;
      this.name = var5;
      this.unlocalizedName = var6;
      this.mapColor = var7;
      this.chatColor = var8;
   }

   public int getMetadata() {
      return this.meta;
   }

   public int getDyeDamage() {
      return this.dyeDamage;
   }

   public String getUnlocalizedName() {
      return this.unlocalizedName;
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   public static EnumDyeColor byDyeDamage(int var0) {
      if (var0 < 0 || var0 >= DYE_DMG_LOOKUP.length) {
         var0 = 0;
      }

      return DYE_DMG_LOOKUP[var0];
   }

   public static EnumDyeColor byMetadata(int var0) {
      if (var0 < 0 || var0 >= META_LOOKUP.length) {
         var0 = 0;
      }

      return META_LOOKUP[var0];
   }

   public String toString() {
      return this.unlocalizedName;
   }

   public String getName() {
      return this.name;
   }

   static {
      for(EnumDyeColor var3 : values()) {
         META_LOOKUP[var3.getMetadata()] = var3;
         DYE_DMG_LOOKUP[var3.getDyeDamage()] = var3;
      }

   }
}
