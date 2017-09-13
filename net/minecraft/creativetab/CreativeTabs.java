package net.minecraft.creativetab;

import net.minecraft.enchantment.EnumEnchantmentType;

public abstract class CreativeTabs {
   public static final CreativeTabs[] CREATIVE_TAB_ARRAY = new CreativeTabs[12];
   public static final CreativeTabs BUILDING_BLOCKS = new CreativeTabs(0, "buildingBlocks") {
   };
   public static final CreativeTabs DECORATIONS = new CreativeTabs(1, "decorations") {
   };
   public static final CreativeTabs REDSTONE = new CreativeTabs(2, "redstone") {
   };
   public static final CreativeTabs TRANSPORTATION = new CreativeTabs(3, "transportation") {
   };
   public static final CreativeTabs MISC = (new CreativeTabs(4, "misc") {
   }).setRelevantEnchantmentTypes(new EnumEnchantmentType[]{EnumEnchantmentType.ALL});
   public static final CreativeTabs SEARCH = (new CreativeTabs(5, "search") {
   }).setBackgroundImageName("item_search.png");
   public static final CreativeTabs FOOD = new CreativeTabs(6, "food") {
   };
   public static final CreativeTabs TOOLS = (new CreativeTabs(7, "tools") {
   }).setRelevantEnchantmentTypes(new EnumEnchantmentType[]{EnumEnchantmentType.DIGGER, EnumEnchantmentType.FISHING_ROD, EnumEnchantmentType.BREAKABLE});
   public static final CreativeTabs COMBAT = (new CreativeTabs(8, "combat") {
   }).setRelevantEnchantmentTypes(new EnumEnchantmentType[]{EnumEnchantmentType.ARMOR, EnumEnchantmentType.ARMOR_FEET, EnumEnchantmentType.ARMOR_HEAD, EnumEnchantmentType.ARMOR_LEGS, EnumEnchantmentType.ARMOR_CHEST, EnumEnchantmentType.BOW, EnumEnchantmentType.WEAPON});
   public static final CreativeTabs BREWING = new CreativeTabs(9, "brewing") {
   };
   public static final CreativeTabs MATERIALS = new CreativeTabs(10, "materials") {
   };
   public static final CreativeTabs INVENTORY = (new CreativeTabs(11, "inventory") {
   }).setBackgroundImageName("inventory.png").setNoScrollbar().setNoTitle();
   private final int tabIndex;
   private final String tabLabel;
   private String theTexture = "items.png";
   private boolean hasScrollbar = true;
   private boolean drawTitle = true;
   private EnumEnchantmentType[] enchantmentTypes;

   public CreativeTabs(int var1, String var2) {
      this.tabIndex = var1;
      this.tabLabel = var2;
      CREATIVE_TAB_ARRAY[var1] = this;
   }

   public CreativeTabs setBackgroundImageName(String var1) {
      this.theTexture = var1;
      return this;
   }

   public CreativeTabs setNoTitle() {
      this.drawTitle = false;
      return this;
   }

   public CreativeTabs setNoScrollbar() {
      this.hasScrollbar = false;
      return this;
   }

   public CreativeTabs setRelevantEnchantmentTypes(EnumEnchantmentType... var1) {
      this.enchantmentTypes = var1;
      return this;
   }
}
