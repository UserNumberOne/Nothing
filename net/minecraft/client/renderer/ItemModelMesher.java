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
      this.modelManager = var1;
   }

   public TextureAtlasSprite getParticleIcon(Item var1) {
      return this.getParticleIcon(var1, 0);
   }

   public TextureAtlasSprite getParticleIcon(Item var1, int var2) {
      ItemStack var3 = new ItemStack(var1, 1, var2);
      IBakedModel var4 = this.getItemModel(var3);
      return var4.getOverrides().handleItemState(var4, var3, (World)null, (EntityLivingBase)null).getParticleTexture();
   }

   public IBakedModel getItemModel(ItemStack var1) {
      Item var2 = var1.getItem();
      IBakedModel var3 = this.getItemModel(var2, this.getMetadata(var1));
      if (var3 == null) {
         ItemMeshDefinition var4 = (ItemMeshDefinition)this.shapers.get(var2);
         if (var4 != null) {
            var3 = this.modelManager.getModel(var4.getModelLocation(var1));
         }
      }

      if (var3 == null) {
         var3 = this.modelManager.getMissingModel();
      }

      return var3;
   }

   protected int getMetadata(ItemStack var1) {
      return var1.getMaxDamage() > 0 ? 0 : var1.getMetadata();
   }

   @Nullable
   protected IBakedModel getItemModel(Item var1, int var2) {
      return (IBakedModel)this.simpleShapesCache.get(Integer.valueOf(this.getIndex(var1, var2)));
   }

   private int getIndex(Item var1, int var2) {
      return Item.getIdFromItem(var1) << 16 | var2;
   }

   public void register(Item var1, int var2, ModelResourceLocation var3) {
      this.simpleShapes.put(Integer.valueOf(this.getIndex(var1, var2)), var3);
      this.simpleShapesCache.put(Integer.valueOf(this.getIndex(var1, var2)), this.modelManager.getModel(var3));
   }

   public void register(Item var1, ItemMeshDefinition var2) {
      this.shapers.put(var1, var2);
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public void rebuildCache() {
      this.simpleShapesCache.clear();

      for(Entry var2 : this.simpleShapes.entrySet()) {
         this.simpleShapesCache.put(var2.getKey(), this.modelManager.getModel((ModelResourceLocation)var2.getValue()));
      }

   }
}
