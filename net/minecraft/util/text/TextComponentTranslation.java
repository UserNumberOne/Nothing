package net.minecraft.util.text;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.text.translation.I18n;

public class TextComponentTranslation extends TextComponentBase {
   private final String key;
   private final Object[] formatArgs;
   private final Object syncLock = new Object();
   private long lastTranslationUpdateTimeInMilliseconds = -1L;
   @VisibleForTesting
   List children = Lists.newArrayList();
   public static final Pattern STRING_VARIABLE_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   public TextComponentTranslation(String var1, Object... var2) {
      this.key = var1;
      this.formatArgs = var2;

      for(Object var6 : var2) {
         if (var6 instanceof ITextComponent) {
            ((ITextComponent)var6).getStyle().setParentStyle(this.getStyle());
         }
      }

   }

   @VisibleForTesting
   synchronized void ensureInitialized() {
      synchronized(this.syncLock) {
         long var2 = I18n.getLastTranslationUpdateTimeInMilliseconds();
         if (var2 == this.lastTranslationUpdateTimeInMilliseconds) {
            return;
         }

         this.lastTranslationUpdateTimeInMilliseconds = var2;
         this.children.clear();
      }

      try {
         this.initializeFromFormat(I18n.translateToLocal(this.key));
      } catch (TextComponentTranslationFormatException var6) {
         this.children.clear();

         try {
            this.initializeFromFormat(I18n.translateToFallback(this.key));
         } catch (TextComponentTranslationFormatException var5) {
            throw var6;
         }
      }

   }

   protected void initializeFromFormat(String var1) {
      boolean var2 = false;
      Matcher var3 = STRING_VARIABLE_PATTERN.matcher(var1);
      int var4 = 0;
      int var5 = 0;

      try {
         int var6;
         for(; var3.find(var5); var5 = var6) {
            int var7 = var3.start();
            var6 = var3.end();
            if (var7 > var5) {
               TextComponentString var8 = new TextComponentString(String.format(var1.substring(var5, var7)));
               var8.getStyle().setParentStyle(this.getStyle());
               this.children.add(var8);
            }

            String var14 = var3.group(2);
            String var9 = var1.substring(var7, var6);
            if ("%".equals(var14) && "%%".equals(var9)) {
               TextComponentString var15 = new TextComponentString("%");
               var15.getStyle().setParentStyle(this.getStyle());
               this.children.add(var15);
            } else {
               if (!"s".equals(var14)) {
                  throw new TextComponentTranslationFormatException(this, "Unsupported format: '" + var9 + "'");
               }

               String var10 = var3.group(1);
               int var11 = var10 != null ? Integer.parseInt(var10) - 1 : var4++;
               if (var11 < this.formatArgs.length) {
                  this.children.add(this.getFormatArgumentAsComponent(var11));
               }
            }
         }

         if (var5 < var1.length()) {
            TextComponentString var13 = new TextComponentString(String.format(var1.substring(var5)));
            var13.getStyle().setParentStyle(this.getStyle());
            this.children.add(var13);
         }

      } catch (IllegalFormatException var12) {
         throw new TextComponentTranslationFormatException(this, var12);
      }
   }

   private ITextComponent getFormatArgumentAsComponent(int var1) {
      if (var1 >= this.formatArgs.length) {
         throw new TextComponentTranslationFormatException(this, var1);
      } else {
         Object var2 = this.formatArgs[var1];
         Object var3;
         if (var2 instanceof ITextComponent) {
            var3 = (ITextComponent)var2;
         } else {
            var3 = new TextComponentString(var2 == null ? "null" : var2.toString());
            ((ITextComponent)var3).getStyle().setParentStyle(this.getStyle());
         }

         return (ITextComponent)var3;
      }
   }

   public ITextComponent setStyle(Style var1) {
      super.setStyle(var1);

      for(Object var5 : this.formatArgs) {
         if (var5 instanceof ITextComponent) {
            ((ITextComponent)var5).getStyle().setParentStyle(this.getStyle());
         }
      }

      if (this.lastTranslationUpdateTimeInMilliseconds > -1L) {
         for(ITextComponent var7 : this.children) {
            var7.getStyle().setParentStyle(var1);
         }
      }

      return this;
   }

   public Iterator iterator() {
      this.ensureInitialized();
      return Iterators.concat(createDeepCopyIterator(this.children), createDeepCopyIterator(this.siblings));
   }

   public String getUnformattedComponentText() {
      this.ensureInitialized();
      StringBuilder var1 = new StringBuilder();

      for(ITextComponent var3 : this.children) {
         var1.append(var3.getUnformattedComponentText());
      }

      return var1.toString();
   }

   public TextComponentTranslation createCopy() {
      Object[] var1 = new Object[this.formatArgs.length];

      for(int var2 = 0; var2 < this.formatArgs.length; ++var2) {
         if (this.formatArgs[var2] instanceof ITextComponent) {
            var1[var2] = ((ITextComponent)this.formatArgs[var2]).createCopy();
         } else {
            var1[var2] = this.formatArgs[var2];
         }
      }

      TextComponentTranslation var5 = new TextComponentTranslation(this.key, var1);
      var5.setStyle(this.getStyle().createShallowCopy());

      for(ITextComponent var4 : this.getSiblings()) {
         var5.appendSibling(var4.createCopy());
      }

      return var5;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof TextComponentTranslation)) {
         return false;
      } else {
         TextComponentTranslation var2 = (TextComponentTranslation)var1;
         return Arrays.equals(this.formatArgs, var2.formatArgs) && this.key.equals(var2.key) && super.equals(var1);
      }
   }

   public int hashCode() {
      int var1 = super.hashCode();
      var1 = 31 * var1 + this.key.hashCode();
      var1 = 31 * var1 + Arrays.hashCode(this.formatArgs);
      return var1;
   }

   public String toString() {
      return "TranslatableComponent{key='" + this.key + '\'' + ", args=" + Arrays.toString(this.formatArgs) + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }

   public String getKey() {
      return this.key;
   }

   public Object[] getFormatArgs() {
      return this.formatArgs;
   }
}
