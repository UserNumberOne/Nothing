package net.minecraft.util.text;

public class TextComponentSelector extends TextComponentBase {
   private final String selector;

   public TextComponentSelector(String var1) {
      this.selector = var1;
   }

   public String getSelector() {
      return this.selector;
   }

   public String getUnformattedComponentText() {
      return this.selector;
   }

   public TextComponentSelector createCopy() {
      TextComponentSelector var1 = new TextComponentSelector(this.selector);
      var1.setStyle(this.getStyle().createShallowCopy());

      for(ITextComponent var3 : this.getSiblings()) {
         var1.appendSibling(var3.createCopy());
      }

      return var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof TextComponentSelector)) {
         return false;
      } else {
         TextComponentSelector var2 = (TextComponentSelector)var1;
         return this.selector.equals(var2.selector) && super.equals(var1);
      }
   }

   public String toString() {
      return "SelectorComponent{pattern='" + this.selector + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }

   // $FF: synthetic method
   public ITextComponent createCopy() {
      return this.createCopy();
   }
}
