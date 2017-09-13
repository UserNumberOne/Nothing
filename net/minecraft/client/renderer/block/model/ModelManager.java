package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelManager implements IResourceManagerReloadListener {
   private IRegistry modelRegistry;
   private final TextureMap texMap;
   private final BlockModelShapes modelProvider;
   private IBakedModel defaultModel;

   public ModelManager(TextureMap var1) {
      this.texMap = var1;
      this.modelProvider = new BlockModelShapes(this);
   }

   public void onResourceManagerReload(IResourceManager var1) {
      ModelLoader var2 = new ModelLoader(var1, this.texMap, this.modelProvider);
      this.modelRegistry = var2.setupModelRegistry();
      this.defaultModel = (IBakedModel)this.modelRegistry.getObject(ModelBakery.MODEL_MISSING);
      ForgeHooksClient.onModelBake(this, this.modelRegistry, var2);
      this.modelProvider.reloadModels();
   }

   public IBakedModel getModel(ModelResourceLocation var1) {
      if (var1 == null) {
         return this.defaultModel;
      } else {
         IBakedModel var2 = (IBakedModel)this.modelRegistry.getObject(var1);
         return var2 == null ? this.defaultModel : var2;
      }
   }

   public IBakedModel getMissingModel() {
      return this.defaultModel;
   }

   public TextureMap getTextureMap() {
      return this.texMap;
   }

   public BlockModelShapes getBlockModelShapes() {
      return this.modelProvider;
   }
}
