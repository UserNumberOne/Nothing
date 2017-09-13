package net.minecraft.stats;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class Achievement extends StatBase {
   public final int displayColumn;
   public final int displayRow;
   public final Achievement parentAchievement;
   private final String achievementDescription;
   public final ItemStack theItemStack;
   private boolean isSpecial;

   public Achievement(String var1, String var2, int var3, int var4, Item var5, Achievement var6) {
      this(var1, var2, var3, var4, new ItemStack(var5), var6);
   }

   public Achievement(String var1, String var2, int var3, int var4, Block var5, Achievement var6) {
      this(var1, var2, var3, var4, new ItemStack(var5), var6);
   }

   public Achievement(String var1, String var2, int var3, int var4, ItemStack var5, Achievement var6) {
      super(var1, new TextComponentTranslation("achievement." + var2, new Object[0]));
      this.theItemStack = var5;
      this.achievementDescription = "achievement." + var2 + ".desc";
      this.displayColumn = var3;
      this.displayRow = var4;
      if (var3 < AchievementList.minDisplayColumn) {
         AchievementList.minDisplayColumn = var3;
      }

      if (var4 < AchievementList.minDisplayRow) {
         AchievementList.minDisplayRow = var4;
      }

      if (var3 > AchievementList.maxDisplayColumn) {
         AchievementList.maxDisplayColumn = var3;
      }

      if (var4 > AchievementList.maxDisplayRow) {
         AchievementList.maxDisplayRow = var4;
      }

      this.parentAchievement = var6;
   }

   public Achievement initIndependentStat() {
      this.isIndependent = true;
      return this;
   }

   public Achievement setSpecial() {
      this.isSpecial = true;
      return this;
   }

   public Achievement registerStat() {
      super.registerStat();
      AchievementList.ACHIEVEMENTS.add(this);
      return this;
   }

   public boolean isAchievement() {
      return true;
   }

   public ITextComponent getStatName() {
      ITextComponent var1 = super.getStatName();
      var1.getStyle().setColor(this.getSpecial() ? TextFormatting.DARK_PURPLE : TextFormatting.GREEN);
      return var1;
   }

   public Achievement setSerializableClazz(Class var1) {
      return (Achievement)super.setSerializableClazz(var1);
   }

   public boolean getSpecial() {
      return this.isSpecial;
   }

   // $FF: synthetic method
   public StatBase setSerializableClazz(Class var1) {
      return this.setSerializableClazz(var1);
   }

   // $FF: synthetic method
   public StatBase registerStat() {
      return this.registerStat();
   }

   // $FF: synthetic method
   public StatBase initIndependentStat() {
      return this.initIndependentStat();
   }
}
