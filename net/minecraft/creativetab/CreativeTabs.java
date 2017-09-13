package net.minecraft.creativetab;

import java.util.List;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class CreativeTabs {
   public static CreativeTabs[] CREATIVE_TAB_ARRAY = new CreativeTabs[12];
   public static final CreativeTabs BUILDING_BLOCKS = new CreativeTabs(0, "buildingBlocks") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Item.getItemFromBlock(Blocks.BRICK_BLOCK);
      }
   };
   public static final CreativeTabs DECORATIONS = new CreativeTabs(1, "decorations") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Item.getItemFromBlock(Blocks.DOUBLE_PLANT);
      }

      @SideOnly(Side.CLIENT)
      public int getIconItemDamage() {
         return BlockDoublePlant.EnumPlantType.PAEONIA.getMeta();
      }
   };
   public static final CreativeTabs REDSTONE = new CreativeTabs(2, "redstone") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.REDSTONE;
      }
   };
   public static final CreativeTabs TRANSPORTATION = new CreativeTabs(3, "transportation") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Item.getItemFromBlock(Blocks.GOLDEN_RAIL);
      }
   };
   public static final CreativeTabs MISC = (new CreativeTabs(4, "misc") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.LAVA_BUCKET;
      }
   }).setRelevantEnchantmentTypes(new EnumEnchantmentType[]{EnumEnchantmentType.ALL});
   public static final CreativeTabs SEARCH = (new CreativeTabs(5, "search") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.COMPASS;
      }
   }).setBackgroundImageName("item_search.png");
   public static final CreativeTabs FOOD = new CreativeTabs(6, "food") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.APPLE;
      }
   };
   public static final CreativeTabs TOOLS = (new CreativeTabs(7, "tools") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.IRON_AXE;
      }
   }).setRelevantEnchantmentTypes(new EnumEnchantmentType[]{EnumEnchantmentType.DIGGER, EnumEnchantmentType.FISHING_ROD, EnumEnchantmentType.BREAKABLE});
   public static final CreativeTabs COMBAT = (new CreativeTabs(8, "combat") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.GOLDEN_SWORD;
      }
   }).setRelevantEnchantmentTypes(new EnumEnchantmentType[]{EnumEnchantmentType.ARMOR, EnumEnchantmentType.ARMOR_FEET, EnumEnchantmentType.ARMOR_HEAD, EnumEnchantmentType.ARMOR_LEGS, EnumEnchantmentType.ARMOR_CHEST, EnumEnchantmentType.BOW, EnumEnchantmentType.WEAPON});
   public static final CreativeTabs BREWING = new CreativeTabs(9, "brewing") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.POTIONITEM;
      }
   };
   public static final CreativeTabs MATERIALS = new CreativeTabs(10, "materials") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Items.STICK;
      }
   };
   public static final CreativeTabs INVENTORY = (new CreativeTabs(11, "inventory") {
      @SideOnly(Side.CLIENT)
      public Item getTabIconItem() {
         return Item.getItemFromBlock(Blocks.CHEST);
      }
   }).setBackgroundImageName("inventory.png").setNoScrollbar().setNoTitle();
   private final int tabIndex;
   private final String tabLabel;
   private String theTexture;
   private boolean hasScrollbar;
   private boolean drawTitle;
   private EnumEnchantmentType[] enchantmentTypes;
   @SideOnly(Side.CLIENT)
   private ItemStack iconItemStack;

   public CreativeTabs(String var1) {
      this(getNextID(), var1);
   }

   public CreativeTabs(int var1, String var2) {
      this.theTexture = "items.png";
      this.hasScrollbar = true;
      this.drawTitle = true;
      if (var1 >= CREATIVE_TAB_ARRAY.length) {
         CreativeTabs[] var3 = new CreativeTabs[var1 + 1];

         for(int var4 = 0; var4 < CREATIVE_TAB_ARRAY.length; ++var4) {
            var3[var4] = CREATIVE_TAB_ARRAY[var4];
         }

         CREATIVE_TAB_ARRAY = var3;
      }

      this.tabIndex = var1;
      this.tabLabel = var2;
      CREATIVE_TAB_ARRAY[var1] = this;
   }

   @SideOnly(Side.CLIENT)
   public int getTabIndex() {
      return this.tabIndex;
   }

   public CreativeTabs setBackgroundImageName(String var1) {
      this.theTexture = var1;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public String getTabLabel() {
      return this.tabLabel;
   }

   @SideOnly(Side.CLIENT)
   public String getTranslatedTabLabel() {
      return "itemGroup." + this.getTabLabel();
   }

   @SideOnly(Side.CLIENT)
   public ItemStack getIconItemStack() {
      if (this.iconItemStack == null) {
         this.iconItemStack = new ItemStack(this.getTabIconItem(), 1, this.getIconItemDamage());
      }

      return this.iconItemStack;
   }

   @SideOnly(Side.CLIENT)
   public abstract Item getTabIconItem();

   @SideOnly(Side.CLIENT)
   public int getIconItemDamage() {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public String getBackgroundImageName() {
      return this.theTexture;
   }

   @SideOnly(Side.CLIENT)
   public boolean drawInForegroundOfTab() {
      return this.drawTitle;
   }

   public CreativeTabs setNoTitle() {
      this.drawTitle = false;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldHidePlayerInventory() {
      return this.hasScrollbar;
   }

   public CreativeTabs setNoScrollbar() {
      this.hasScrollbar = false;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public int getTabColumn() {
      return this.tabIndex > 11 ? (this.tabIndex - 12) % 10 % 5 : this.tabIndex % 6;
   }

   @SideOnly(Side.CLIENT)
   public boolean isTabInFirstRow() {
      if (this.tabIndex > 11) {
         return (this.tabIndex - 12) % 10 < 5;
      } else {
         return this.tabIndex < 6;
      }
   }

   @SideOnly(Side.CLIENT)
   public EnumEnchantmentType[] getRelevantEnchantmentTypes() {
      return this.enchantmentTypes;
   }

   public CreativeTabs setRelevantEnchantmentTypes(EnumEnchantmentType... var1) {
      this.enchantmentTypes = var1;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean hasRelevantEnchantmentType(EnumEnchantmentType var1) {
      if (this.enchantmentTypes == null) {
         return false;
      } else {
         for(EnumEnchantmentType var5 : this.enchantmentTypes) {
            if (var5 == var1) {
               return true;
            }
         }

         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public void displayAllRelevantItems(List var1) {
      for(Item var3 : Item.REGISTRY) {
         if (var3 != null) {
            for(CreativeTabs var7 : var3.getCreativeTabs()) {
               if (var7 == this) {
                  var3.getSubItems(var3, this, var1);
               }
            }
         }
      }

      if (this.getRelevantEnchantmentTypes() != null) {
         this.addEnchantmentBooksToList(var1, this.getRelevantEnchantmentTypes());
      }

   }

   @SideOnly(Side.CLIENT)
   public void addEnchantmentBooksToList(List var1, EnumEnchantmentType... var2) {
      for(Enchantment var4 : Enchantment.REGISTRY) {
         if (var4 != null && var4.type != null) {
            boolean var5 = false;

            for(int var6 = 0; var6 < var2.length && !var5; ++var6) {
               if (var4.type == var2[var6]) {
                  var5 = true;
               }
            }

            if (var5) {
               var1.add(Items.ENCHANTED_BOOK.getEnchantedItemStack(new EnchantmentData(var4, var4.getMaxLevel())));
            }
         }
      }

   }

   public int getTabPage() {
      return this.tabIndex > 11 ? (this.tabIndex - 12) / 10 + 1 : 0;
   }

   public static int getNextID() {
      return CREATIVE_TAB_ARRAY.length;
   }

   public boolean hasSearchBar() {
      return this.tabIndex == SEARCH.tabIndex;
   }

   public int getSearchbarWidth() {
      return 89;
   }
}
