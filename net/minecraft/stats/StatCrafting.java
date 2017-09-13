package net.minecraft.stats;

import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StatCrafting extends StatBase {
   private final Item item;

   public StatCrafting(String var1, String var2, ITextComponent var3, Item var4) {
      super(p_i45910_1_ + p_i45910_2_, statNameIn);
      this.item = p_i45910_4_;
   }

   @SideOnly(Side.CLIENT)
   public Item getItem() {
      return this.item;
   }
}
