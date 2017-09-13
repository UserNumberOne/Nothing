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
      this.texMap = textures;
      this.modelProvider = new BlockModelShapes(this);
   }

   public void onResourceManagerReload(IResourceManager var1) {
      ModelLoader modelbakery = new ModelLoader(resourceManager, this.texMap, this.modelProvider);
      this.modelRegistry = modelbakery.setupModelRegistry();
      this.defaultModel = (IBakedModel)this.modelRegistry.getObject(ModelBakery.MODEL_MISSING);
      ForgeHooksClient.onModelBake(this, this.modelRegistry, modelbakery);
      this.modelProvider.reloadModels();
   }

   public IBakedModel getModel(ModelResourceLocation var1) {
      if (modelLocation == null) {
         return this.defaultModel;
      } else {
         IBakedModel ibakedmodel = (IBakedModel)this.modelRegistry.getObject(modelLocation);
         return ibakedmodel == null ? this.defaultModel : ibakedmodel;
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
