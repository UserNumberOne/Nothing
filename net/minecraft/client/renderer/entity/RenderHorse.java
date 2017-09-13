package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHorse extends RenderLiving {
   private static final Map LAYERED_LOCATION_CACHE = Maps.newHashMap();

   public RenderHorse(RenderManager var1, ModelHorse var2, float var3) {
      super(var1, var2, var3);
   }

   protected void preRenderCallback(EntityHorse var1, float var2) {
      float var3 = 1.0F;
      HorseType var4 = var1.getType();
      if (var4 == HorseType.DONKEY) {
         var3 *= 0.87F;
      } else if (var4 == HorseType.MULE) {
         var3 *= 0.92F;
      }

      GlStateManager.scale(var3, var3, var3);
      super.preRenderCallback(var1, var2);
   }

   protected ResourceLocation getEntityTexture(EntityHorse var1) {
      return !var1.hasLayeredTextures() ? var1.getType().getTexture() : this.getOrCreateLayeredResourceLoc(var1);
   }

   @Nullable
   private ResourceLocation getOrCreateLayeredResourceLoc(EntityHorse var1) {
      String var2 = var1.getHorseTexture();
      if (!var1.hasTexture()) {
         return null;
      } else {
         ResourceLocation var3 = (ResourceLocation)LAYERED_LOCATION_CACHE.get(var2);
         if (var3 == null) {
            var3 = new ResourceLocation(var2);
            Minecraft.getMinecraft().getTextureManager().loadTexture(var3, new LayeredTexture(var1.getVariantTexturePaths()));
            LAYERED_LOCATION_CACHE.put(var2, var3);
         }

         return var3;
      }
   }
}
