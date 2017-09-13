package net.minecraft.item;

import com.google.common.base.Function;
import javax.annotation.Nullable;
import net.minecraft.block.Block;

public class ItemMultiTexture extends ItemBlock {
   protected final Block theBlock;
   protected final Function nameFunction;

   public ItemMultiTexture(Block var1, Block var2, Function var3) {
      super(block);
      this.theBlock = block2;
      this.nameFunction = nameFunction;
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public ItemMultiTexture(Block var1, Block var2, final String[] var3) {
      this(block, block2, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            int i = p_apply_1_.getMetadata();
            if (i < 0 || i >= namesByMeta.length) {
               i = 0;
            }

            return namesByMeta[i];
         }
      });
   }

   public int getMetadata(int var1) {
      return damage;
   }

   public String getUnlocalizedName(ItemStack var1) {
      return super.getUnlocalizedName() + "." + (String)this.nameFunction.apply(stack);
   }
}
