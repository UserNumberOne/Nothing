package net.minecraft.client.resources;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Language implements Comparable {
   private final String languageCode;
   private final String region;
   private final String name;
   private final boolean bidirectional;

   public Language(String var1, String var2, String var3, boolean var4) {
      this.languageCode = languageCodeIn;
      this.region = regionIn;
      this.name = nameIn;
      this.bidirectional = bidirectionalIn;
   }

   public String getLanguageCode() {
      return this.languageCode;
   }

   public boolean isBidirectional() {
      return this.bidirectional;
   }

   public String toString() {
      return String.format("%s (%s)", this.name, this.region);
   }

   public boolean equals(Object var1) {
      return this == p_equals_1_ ? true : (!(p_equals_1_ instanceof Language) ? false : this.languageCode.equals(((Language)p_equals_1_).languageCode));
   }

   public int hashCode() {
      return this.languageCode.hashCode();
   }

   public int compareTo(Language var1) {
      return this.languageCode.compareTo(p_compareTo_1_.languageCode);
   }
}
