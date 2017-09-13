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

   public ITextComponent appendSibling(ITextComponent ichatbasecomponent) {
      ichatbasecomponent.getStyle().setParentStyle(this.getStyle());
      this.siblings.add(ichatbasecomponent);
      return this;
   }

   public List getSiblings() {
      return this.siblings;
   }

   public ITextComponent appendText(String s) {
      return this.appendSibling(new TextComponentString(s));
   }

   public ITextComponent setStyle(Style chatmodifier) {
      this.style = chatmodifier;

      for(ITextComponent ichatbasecomponent : this.siblings) {
         ichatbasecomponent.getStyle().setParentStyle(this.getStyle());
      }

      return this;
   }

   public Style getStyle() {
      if (this.style == null) {
         this.style = new Style();

         for(ITextComponent ichatbasecomponent : this.siblings) {
            ichatbasecomponent.getStyle().setParentStyle(this.style);
         }
      }

      return this.style;
   }

   public Iterator iterator() {
      return Iterators.concat(Iterators.forArray(new TextComponentBase[]{this}), createDeepCopyIterator(this.siblings));
   }

   public final String getUnformattedText() {
      StringBuilder stringbuilder = new StringBuilder();

      for(ITextComponent ichatbasecomponent : this) {
         stringbuilder.append(ichatbasecomponent.getUnformattedComponentText());
      }

      return stringbuilder.toString();
   }

   public static Iterator createDeepCopyIterator(Iterable iterable) {
      Iterator iterator = Iterators.concat(Iterators.transform(iterable.iterator(), new Function() {
         public Iterator apply(@Nullable ITextComponent ichatbasecomponent) {
            return ichatbasecomponent.iterator();
         }

         public Object apply(Object object) {
            return this.apply((ITextComponent)object);
         }
      }));
      iterator = Iterators.transform(iterator, new Function() {
         public ITextComponent apply(@Nullable ITextComponent ichatbasecomponent) {
            ITextComponent ichatbasecomponent1 = ichatbasecomponent.createCopy();
            ichatbasecomponent1.setStyle(ichatbasecomponent1.getStyle().createDeepCopy());
            return ichatbasecomponent1;
         }

         public Object apply(Object object) {
            return this.apply((ITextComponent)object);
         }
      });
      return iterator;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof TextComponentBase)) {
         return false;
      } else {
         TextComponentBase chatbasecomponent = (TextComponentBase)object;
         return this.siblings.equals(chatbasecomponent.siblings) && this.getStyle().equals(chatbasecomponent.getStyle());
      }
   }

   public int hashCode() {
      return 31 * this.getStyle().hashCode() + this.siblings.hashCode();
   }

   public String toString() {
      return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
   }
}
