package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemOverrideList {
   public static final ItemOverrideList NONE = new ItemOverrideList();
   private final List overrides = Lists.newArrayList();

   private ItemOverrideList() {
   }

   public ItemOverrideList(List var1) {
      for(int var2 = var1.size() - 1; var2 >= 0; --var2) {
         this.overrides.add(var1.get(var2));
      }

   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public ResourceLocation applyOverride(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
      if (!this.overrides.isEmpty()) {
         for(ItemOverride var5 : this.overrides) {
            if (var5.matchesItemStack(var1, var2, var3)) {
               return var5.getLocation();
            }
         }
      }

      return null;
   }

   public IBakedModel handleItemState(IBakedModel var1, ItemStack var2, World var3, EntityLivingBase var4) {
      Item var5 = var2.getItem();
      if (var5 != null && var5.hasCustomProperties()) {
         ResourceLocation var6 = this.applyOverride(var2, var3, var4);
         if (var6 != null) {
            return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(ModelLoader.getInventoryVariant(var6.toString()));
         }
      }

      return var1;
   }

   public ImmutableList getOverrides() {
      return ImmutableList.copyOf(this.overrides);
   }
}
