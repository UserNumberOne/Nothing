package net.minecraft.util.text;

public class TextComponentString extends TextComponentBase {
   private final String text;

   public TextComponentString(String var1) {
      this.text = var1;
   }

   public String getText() {
      return this.text;
   }

   public String getUnformattedComponentText() {
      return this.text;
   }

   public TextComponentString createCopy() {
      TextComponentString var1 = new TextComponentString(this.text);
      var1.setStyle(this.getStyle().createShallowCopy());

      for(ITextComponent var3 : this.getSiblings()) {
         var1.appendSibling(var3.createCopy());
      }

      return var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof TextComponentString)) {
         return false;
      } else {
         TextComponentString var2 = (TextComponentString)var1;
         return this.text.equals(var2.getText()) && super.equals(var1);
      }
   }

   public String toString() {
      return "TextComponent{text='" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }
}
