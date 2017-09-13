package net.minecraft.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public abstract class ModelBase {
   public float swingProgress;
   public boolean isRiding;
   public boolean isChild = true;
   public List boxList = Lists.newArrayList();
   private final Map modelTextureMap = Maps.newHashMap();
   public int textureWidth = 64;
   public int textureHeight = 32;

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
   }

   public void setLivingAnimations(EntityLivingBase var1, float var2, float var3, float var4) {
   }

   public ModelRenderer getRandomModelBox(Random var1) {
      return (ModelRenderer)this.boxList.get(rand.nextInt(this.boxList.size()));
   }

   protected void setTextureOffset(String var1, int var2, int var3) {
      this.modelTextureMap.put(partName, new TextureOffset(x, y));
   }

   public TextureOffset getTextureOffset(String var1) {
      return (TextureOffset)this.modelTextureMap.get(partName);
   }

   public static void copyModelAngles(ModelRenderer var0, ModelRenderer var1) {
      dest.rotateAngleX = source.rotateAngleX;
      dest.rotateAngleY = source.rotateAngleY;
      dest.rotateAngleZ = source.rotateAngleZ;
      dest.rotationPointX = source.rotationPointX;
      dest.rotationPointY = source.rotationPointY;
      dest.rotationPointZ = source.rotationPointZ;
   }

   public void setModelAttributes(ModelBase var1) {
      this.swingProgress = model.swingProgress;
      this.isRiding = model.isRiding;
      this.isChild = model.isChild;
   }
}
