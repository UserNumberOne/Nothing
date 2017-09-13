package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Quaternion;

@SideOnly(Side.CLIENT)
public class ItemCameraTransforms {
   public static final ItemCameraTransforms DEFAULT = new ItemCameraTransforms();
   public static float offsetTranslateX;
   public static float offsetTranslateY;
   public static float offsetTranslateZ;
   public static float offsetRotationX;
   public static float offsetRotationY;
   public static float offsetRotationZ;
   public static float offsetScaleX;
   public static float offsetScaleY;
   public static float offsetScaleZ;
   public final ItemTransformVec3f thirdperson_left;
   public final ItemTransformVec3f thirdperson_right;
   public final ItemTransformVec3f firstperson_left;
   public final ItemTransformVec3f firstperson_right;
   public final ItemTransformVec3f head;
   public final ItemTransformVec3f gui;
   public final ItemTransformVec3f ground;
   public final ItemTransformVec3f fixed;

   private ItemCameraTransforms() {
      this(ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT);
   }

   /** @deprecated */
   @Deprecated
   public ItemCameraTransforms(ItemCameraTransforms var1) {
      this.thirdperson_left = var1.thirdperson_left;
      this.thirdperson_right = var1.thirdperson_right;
      this.firstperson_left = var1.firstperson_left;
      this.firstperson_right = var1.firstperson_right;
      this.head = var1.head;
      this.gui = var1.gui;
      this.ground = var1.ground;
      this.fixed = var1.fixed;
   }

   /** @deprecated */
   @Deprecated
   public ItemCameraTransforms(ItemTransformVec3f var1, ItemTransformVec3f var2, ItemTransformVec3f var3, ItemTransformVec3f var4, ItemTransformVec3f var5, ItemTransformVec3f var6, ItemTransformVec3f var7, ItemTransformVec3f var8) {
      this.thirdperson_left = var1;
      this.thirdperson_right = var2;
      this.firstperson_left = var3;
      this.firstperson_right = var4;
      this.head = var5;
      this.gui = var6;
      this.ground = var7;
      this.fixed = var8;
   }

   public void applyTransform(ItemCameraTransforms.TransformType var1) {
      applyTransformSide(this.getTransform(var1), false);
   }

   public static void applyTransformSide(ItemTransformVec3f var0, boolean var1) {
      if (var0 != ItemTransformVec3f.DEFAULT) {
         int var2 = var1 ? -1 : 1;
         GlStateManager.translate((float)var2 * (offsetTranslateX + var0.translation.x), offsetTranslateY + var0.translation.y, offsetTranslateZ + var0.translation.z);
         float var3 = offsetRotationX + var0.rotation.x;
         float var4 = offsetRotationY + var0.rotation.y;
         float var5 = offsetRotationZ + var0.rotation.z;
         if (var1) {
            var4 = -var4;
            var5 = -var5;
         }

         GlStateManager.rotate(makeQuaternion(var3, var4, var5));
         GlStateManager.scale(offsetScaleX + var0.scale.x, offsetScaleY + var0.scale.y, offsetScaleZ + var0.scale.z);
      }

   }

   private static Quaternion makeQuaternion(float var0, float var1, float var2) {
      float var3 = var0 * 0.017453292F;
      float var4 = var1 * 0.017453292F;
      float var5 = var2 * 0.017453292F;
      float var6 = MathHelper.sin(0.5F * var3);
      float var7 = MathHelper.cos(0.5F * var3);
      float var8 = MathHelper.sin(0.5F * var4);
      float var9 = MathHelper.cos(0.5F * var4);
      float var10 = MathHelper.sin(0.5F * var5);
      float var11 = MathHelper.cos(0.5F * var5);
      return new Quaternion(var6 * var9 * var11 + var7 * var8 * var10, var7 * var8 * var11 - var6 * var9 * var10, var6 * var8 * var11 + var7 * var9 * var10, var7 * var9 * var11 - var6 * var8 * var10);
   }

   /** @deprecated */
   @Deprecated
   public ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType var1) {
      switch(var1) {
      case THIRD_PERSON_LEFT_HAND:
         return this.thirdperson_left;
      case THIRD_PERSON_RIGHT_HAND:
         return this.thirdperson_right;
      case FIRST_PERSON_LEFT_HAND:
         return this.firstperson_left;
      case FIRST_PERSON_RIGHT_HAND:
         return this.firstperson_right;
      case HEAD:
         return this.head;
      case GUI:
         return this.gui;
      case GROUND:
         return this.ground;
      case FIXED:
         return this.fixed;
      default:
         return ItemTransformVec3f.DEFAULT;
      }
   }

   public boolean hasCustomTransform(ItemCameraTransforms.TransformType var1) {
      return this.getTransform(var1) != ItemTransformVec3f.DEFAULT;
   }

   @SideOnly(Side.CLIENT)
   static class Deserializer implements JsonDeserializer {
      public ItemCameraTransforms deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = var1.getAsJsonObject();
         ItemTransformVec3f var5 = this.getTransform(var3, var4, "thirdperson_righthand");
         ItemTransformVec3f var6 = this.getTransform(var3, var4, "thirdperson_lefthand");
         if (var6 == ItemTransformVec3f.DEFAULT) {
            var6 = var5;
         }

         ItemTransformVec3f var7 = this.getTransform(var3, var4, "firstperson_righthand");
         ItemTransformVec3f var8 = this.getTransform(var3, var4, "firstperson_lefthand");
         if (var8 == ItemTransformVec3f.DEFAULT) {
            var8 = var7;
         }

         ItemTransformVec3f var9 = this.getTransform(var3, var4, "head");
         ItemTransformVec3f var10 = this.getTransform(var3, var4, "gui");
         ItemTransformVec3f var11 = this.getTransform(var3, var4, "ground");
         ItemTransformVec3f var12 = this.getTransform(var3, var4, "fixed");
         return new ItemCameraTransforms(var6, var5, var8, var7, var9, var10, var11, var12);
      }

      private ItemTransformVec3f getTransform(JsonDeserializationContext var1, JsonObject var2, String var3) {
         return var2.has(var3) ? (ItemTransformVec3f)var1.deserialize(var2.get(var3), ItemTransformVec3f.class) : ItemTransformVec3f.DEFAULT;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum TransformType implements IModelPart {
      NONE,
      THIRD_PERSON_LEFT_HAND,
      THIRD_PERSON_RIGHT_HAND,
      FIRST_PERSON_LEFT_HAND,
      FIRST_PERSON_RIGHT_HAND,
      HEAD,
      GUI,
      GROUND,
      FIXED;
   }
}
