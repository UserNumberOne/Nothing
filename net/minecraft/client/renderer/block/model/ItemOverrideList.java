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
      for(int i = overridesIn.size() - 1; i >= 0; --i) {
         this.overrides.add(overridesIn.get(i));
      }

   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public ResourceLocation applyOverride(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
      if (!this.overrides.isEmpty()) {
         for(ItemOverride itemoverride : this.overrides) {
            if (itemoverride.matchesItemStack(stack, worldIn, entityIn)) {
               return itemoverride.getLocation();
            }
         }
      }

      return null;
   }

   public IBakedModel handleItemState(IBakedModel var1, ItemStack var2, World var3, EntityLivingBase var4) {
      Item item = stack.getItem();
      if (item != null && item.hasCustomProperties()) {
         ResourceLocation location = this.applyOverride(stack, world, entity);
         if (location != null) {
            return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(ModelLoader.getInventoryVariant(location.toString()));
         }
      }

      return originalModel;
   }

   public ImmutableList getOverrides() {
      return ImmutableList.copyOf(this.overrides);
   }
}
