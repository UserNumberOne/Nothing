package net.minecraft.item;

import com.google.common.base.Function;
import javax.annotation.Nullable;
import net.minecraft.block.Block;

public class ItemMultiTexture extends ItemBlock {
   protected final Block theBlock;
   protected final Function nameFunction;

   public ItemMultiTexture(Block var1, Block var2, Function var3) {
      super(var1);
      this.theBlock = var2;
      this.nameFunction = var3;
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public ItemMultiTexture(Block var1, Block var2, final String[] var3) {
      this(var1, var2, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            int var2 = var1.getMetadata();
            if (var2 < 0 || var2 >= var3.length) {
               var2 = 0;
            }

            return var3[var2];
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      });
   }

   public int getMetadata(int var1) {
      return var1;
   }

   public String getUnlocalizedName(ItemStack var1) {
      return super.getUnlocalizedName() + "." + (String)this.nameFunction.apply(var1);
   }
}
