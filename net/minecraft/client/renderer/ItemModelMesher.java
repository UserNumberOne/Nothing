package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemModelMesher {
   private final Map simpleShapes = Maps.newHashMap();
   private final Map simpleShapesCache = Maps.newHashMap();
   private final Map shapers = Maps.newHashMap();
   private final ModelManager modelManager;

   public ItemModelMesher(ModelManager var1) {
      this.modelManager = modelManager;
   }

   public TextureAtlasSprite getParticleIcon(Item var1) {
      return this.getParticleIcon(item, 0);
   }

   public TextureAtlasSprite getParticleIcon(Item var1, int var2) {
      ItemStack stack = new ItemStack(item, 1, meta);
      IBakedModel model = this.getItemModel(stack);
      return model.getOverrides().handleItemState(model, stack, (World)null, (EntityLivingBase)null).getParticleTexture();
   }

   public IBakedModel getItemModel(ItemStack var1) {
      Item item = stack.getItem();
      IBakedModel ibakedmodel = this.getItemModel(item, this.getMetadata(stack));
      if (ibakedmodel == null) {
         ItemMeshDefinition itemmeshdefinition = (ItemMeshDefinition)this.shapers.get(item);
         if (itemmeshdefinition != null) {
            ibakedmodel = this.modelManager.getModel(itemmeshdefinition.getModelLocation(stack));
         }
      }

      if (ibakedmodel == null) {
         ibakedmodel = this.modelManager.getMissingModel();
      }

      return ibakedmodel;
   }

   protected int getMetadata(ItemStack var1) {
      return stack.getMaxDamage() > 0 ? 0 : stack.getMetadata();
   }

   @Nullable
   protected IBakedModel getItemModel(Item var1, int var2) {
      return (IBakedModel)this.simpleShapesCache.get(Integer.valueOf(this.getIndex(item, meta)));
   }

   private int getIndex(Item var1, int var2) {
      return Item.getIdFromItem(item) << 16 | meta;
   }

   public void register(Item var1, int var2, ModelResourceLocation var3) {
      this.simpleShapes.put(Integer.valueOf(this.getIndex(item, meta)), location);
      this.simpleShapesCache.put(Integer.valueOf(this.getIndex(item, meta)), this.modelManager.getModel(location));
   }

   public void register(Item var1, ItemMeshDefinition var2) {
      this.shapers.put(item, definition);
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public void rebuildCache() {
      this.simpleShapesCache.clear();

      for(Entry entry : this.simpleShapes.entrySet()) {
         this.simpleShapesCache.put(entry.getKey(), this.modelManager.getModel((ModelResourceLocation)entry.getValue()));
      }

   }
}
