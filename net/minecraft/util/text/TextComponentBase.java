package net.minecraft.util.text;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public abstract class TextComponentBase implements ITextComponent {
   protected List siblings = Lists.newArrayList();
   private Style style;

   public ITextComponent appendSibling(ITextComponent var1) {
      var1.getStyle().setParentStyle(this.getStyle());
      this.siblings.add(var1);
      return this;
   }

   public List getSiblings() {
      return this.siblings;
   }

   public ITextComponent appendText(String var1) {
      return this.appendSibling(new TextComponentString(var1));
   }

   public ITextComponent setStyle(Style var1) {
      this.style = var1;

      for(ITextComponent var3 : this.siblings) {
         var3.getStyle().setParentStyle(this.getStyle());
      }

      return this;
   }

   public Style getStyle() {
      if (this.style == null) {
         this.style = new Style();

         for(ITextComponent var2 : this.siblings) {
            var2.getStyle().setParentStyle(this.style);
         }
      }

      return this.style;
   }

   public Iterator iterator() {
      return Iterators.concat(Iterators.forArray(new TextComponentBase[]{this}), createDeepCopyIterator(this.siblings));
   }

   public final String getUnformattedText() {
      StringBuilder var1 = new StringBuilder();

      for(ITextComponent var3 : this) {
         var1.append(var3.getUnformattedComponentText());
      }

      return var1.toString();
   }

   public final String getFormattedText() {
      StringBuilder var1 = new StringBuilder();

      for(ITextComponent var3 : this) {
         var1.append(var3.getStyle().getFormattingCode());
         var1.append(var3.getUnformattedComponentText());
         var1.append(TextFormatting.RESET);
      }

      return var1.toString();
   }

   public static Iterator createDeepCopyIterator(Iterable var0) {
      Iterator var1 = Iterators.concat(Iterators.transform(var0.iterator(), new Function() {
         public Iterator apply(@Nullable ITextComponent var1) {
            return var1.iterator();
         }
      }));
      var1 = Iterators.transform(var1, new Function() {
         public ITextComponent apply(@Nullable ITextComponent var1) {
            ITextComponent var2 = var1.createCopy();
            var2.setStyle(var2.getStyle().createDeepCopy());
            return var2;
         }
      });
      return var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof TextComponentBase)) {
         return false;
      } else {
         TextComponentBase var2 = (TextComponentBase)var1;
         return this.siblings.equals(var2.siblings) && this.getStyle().equals(var2.getStyle());
      }
   }

   public int hashCode() {
      return 31 * this.style.hashCode() + this.siblings.hashCode();
   }

   public String toString() {
      return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
   }
}
