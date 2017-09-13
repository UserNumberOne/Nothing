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

   public ItemOverrideList(List overridesIn) {
      for(int i = overridesIn.size() - 1; i >= 0; --i) {
         this.overrides.add(overridesIn.get(i));
      }

   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public ResourceLocation applyOverride(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
      if (!this.overrides.isEmpty()) {
         for(ItemOverride itemoverride : this.overrides) {
            if (itemoverride.matchesItemStack(stack, worldIn, entityIn)) {
               return itemoverride.getLocation();
            }
         }
      }

      return null;
   }

   public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
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
