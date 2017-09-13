package net.minecraft.stats;

import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StatCrafting extends StatBase {
   private final Item item;

   public StatCrafting(String var1, String var2, ITextComponent var3, Item var4) {
      super(var1 + var2, var3);
      this.item = var4;
   }

   @SideOnly(Side.CLIENT)
   public Item getItem() {
      return this.item;
   }
}
