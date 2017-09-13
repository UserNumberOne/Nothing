package net.minecraft.tileentity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

public class TileEntityBanner extends TileEntity {
   public int baseColor;
   public NBTTagList patterns;
   private boolean patternDataSet;
   private List patternList;
   private List colorList;
   private String patternResourceLocation;

   public void setItemValues(ItemStack itemstack) {
      this.patterns = null;
      if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
         NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");
         if (nbttagcompound.hasKey("Patterns")) {
            this.patterns = nbttagcompound.getTagList("Patterns", 10).copy();

            while(this.patterns.tagCount() > 20) {
               this.patterns.removeTag(20);
            }
         }

         if (nbttagcompound.hasKey("Base", 99)) {
            this.baseColor = nbttagcompound.getInteger("Base");
         } else {
            this.baseColor = itemstack.getMetadata() & 15;
         }
      } else {
         this.baseColor = itemstack.getMetadata() & 15;
      }

      this.patternList = null;
      this.colorList = null;
      this.patternResourceLocation = "";
      this.patternDataSet = true;
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      setBaseColorAndPatterns(nbttagcompound, this.baseColor, this.patterns);
      return nbttagcompound;
   }

   public static void setBaseColorAndPatterns(NBTTagCompound nbttagcompound, int i, @Nullable NBTTagList nbttaglist) {
      nbttagcompound.setInteger("Base", i);
      if (nbttaglist != null) {
         nbttagcompound.setTag("Patterns", nbttaglist);
      }

   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.baseColor = nbttagcompound.getInteger("Base");
      this.patterns = nbttagcompound.getTagList("Patterns", 10);

      while(this.patterns.tagCount() > 20) {
         this.patterns.removeTag(20);
      }

      this.patternList = null;
      this.colorList = null;
      this.patternResourceLocation = null;
      this.patternDataSet = true;
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 6, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   public int getBaseColor() {
      return this.baseColor;
   }

   public static int getBaseColor(ItemStack itemstack) {
      NBTTagCompound nbttagcompound = itemstack.getSubCompound("BlockEntityTag", false);
      return nbttagcompound != null && nbttagcompound.hasKey("Base") ? nbttagcompound.getInteger("Base") : itemstack.getMetadata();
   }

   public static int getPatterns(ItemStack itemstack) {
      NBTTagCompound nbttagcompound = itemstack.getSubCompound("BlockEntityTag", false);
      return nbttagcompound != null && nbttagcompound.hasKey("Patterns") ? nbttagcompound.getTagList("Patterns", 10).tagCount() : 0;
   }

   public NBTTagList getPatterns() {
      return this.patterns;
   }

   public static void addBaseColorTag(ItemStack itemstack, EnumDyeColor enumcolor) {
      NBTTagCompound nbttagcompound = itemstack.getSubCompound("BlockEntityTag", true);
      nbttagcompound.setInteger("Base", enumcolor.getDyeDamage());
   }

   public static void removeBannerData(ItemStack itemstack) {
      NBTTagCompound nbttagcompound = itemstack.getSubCompound("BlockEntityTag", false);
      if (nbttagcompound != null && nbttagcompound.hasKey("Patterns", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Patterns", 10);
         if (nbttaglist.tagCount() > 0) {
            nbttaglist.removeTag(nbttaglist.tagCount() - 1);
            if (nbttaglist.hasNoTags()) {
               itemstack.getTagCompound().removeTag("BlockEntityTag");
               if (itemstack.getTagCompound().hasNoTags()) {
                  itemstack.setTagCompound((NBTTagCompound)null);
               }
            }
         }
      }

   }

   public static enum EnumBannerPattern {
      BASE("base", "b"),
      SQUARE_BOTTOM_LEFT("square_bottom_left", "bl", "   ", "   ", "#  "),
      SQUARE_BOTTOM_RIGHT("square_bottom_right", "br", "   ", "   ", "  #"),
      SQUARE_TOP_LEFT("square_top_left", "tl", "#  ", "   ", "   "),
      SQUARE_TOP_RIGHT("square_top_right", "tr", "  #", "   ", "   "),
      STRIPE_BOTTOM("stripe_bottom", "bs", "   ", "   ", "###"),
      STRIPE_TOP("stripe_top", "ts", "###", "   ", "   "),
      STRIPE_LEFT("stripe_left", "ls", "#  ", "#  ", "#  "),
      STRIPE_RIGHT("stripe_right", "rs", "  #", "  #", "  #"),
      STRIPE_CENTER("stripe_center", "cs", " # ", " # ", " # "),
      STRIPE_MIDDLE("stripe_middle", "ms", "   ", "###", "   "),
      STRIPE_DOWNRIGHT("stripe_downright", "drs", "#  ", " # ", "  #"),
      STRIPE_DOWNLEFT("stripe_downleft", "dls", "  #", " # ", "#  "),
      STRIPE_SMALL("small_stripes", "ss", "# #", "# #", "   "),
      CROSS("cross", "cr", "# #", " # ", "# #"),
      STRAIGHT_CROSS("straight_cross", "sc", " # ", "###", " # "),
      TRIANGLE_BOTTOM("triangle_bottom", "bt", "   ", " # ", "# #"),
      TRIANGLE_TOP("triangle_top", "tt", "# #", " # ", "   "),
      TRIANGLES_BOTTOM("triangles_bottom", "bts", "   ", "# #", " # "),
      TRIANGLES_TOP("triangles_top", "tts", " # ", "# #", "   "),
      DIAGONAL_LEFT("diagonal_left", "ld", "## ", "#  ", "   "),
      DIAGONAL_RIGHT("diagonal_up_right", "rd", "   ", "  #", " ##"),
      DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud", "   ", "#  ", "## "),
      DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud", " ##", "  #", "   "),
      CIRCLE_MIDDLE("circle", "mc", "   ", " # ", "   "),
      RHOMBUS_MIDDLE("rhombus", "mr", " # ", "# #", " # "),
      HALF_VERTICAL("half_vertical", "vh", "## ", "## ", "## "),
      HALF_HORIZONTAL("half_horizontal", "hh", "###", "###", "   "),
      HALF_VERTICAL_MIRROR("half_vertical_right", "vhr", " ##", " ##", " ##"),
      HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb", "   ", "###", "###"),
      BORDER("border", "bo", "###", "# #", "###"),
      CURLY_BORDER("curly_border", "cbo", new ItemStack(Blocks.VINE)),
      CREEPER("creeper", "cre", new ItemStack(Items.SKULL, 1, 4)),
      GRADIENT("gradient", "gra", "# #", " # ", " # "),
      GRADIENT_UP("gradient_up", "gru", " # ", " # ", "# #"),
      BRICKS("bricks", "bri", new ItemStack(Blocks.BRICK_BLOCK)),
      SKULL("skull", "sku", new ItemStack(Items.SKULL, 1, 1)),
      FLOWER("flower", "flo", new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta())),
      MOJANG("mojang", "moj", new ItemStack(Items.GOLDEN_APPLE, 1, 1));

      private final String patternName;
      private final String patternID;
      private final String[] craftingLayers;
      private ItemStack patternCraftingStack;

      private EnumBannerPattern(String s, String s1) {
         this.craftingLayers = new String[3];
         this.patternName = s;
         this.patternID = s1;
      }

      private EnumBannerPattern(String s, String s1, ItemStack itemstack) {
         this(s, s1);
         this.patternCraftingStack = itemstack;
      }

      private EnumBannerPattern(String s, String s1, String s2, String s3, String s4) {
         this(s, s1);
         this.craftingLayers[0] = s2;
         this.craftingLayers[1] = s3;
         this.craftingLayers[2] = s4;
      }

      public String getPatternID() {
         return this.patternID;
      }

      public String[] getCraftingLayers() {
         return this.craftingLayers;
      }

      public boolean hasValidCrafting() {
         return this.patternCraftingStack != null || this.craftingLayers[0] != null;
      }

      public boolean hasCraftingStack() {
         return this.patternCraftingStack != null;
      }

      public ItemStack getCraftingStack() {
         return this.patternCraftingStack;
      }
   }
}
