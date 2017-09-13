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
      this.thirdperson_left = transforms.thirdperson_left;
      this.thirdperson_right = transforms.thirdperson_right;
      this.firstperson_left = transforms.firstperson_left;
      this.firstperson_right = transforms.firstperson_right;
      this.head = transforms.head;
      this.gui = transforms.gui;
      this.ground = transforms.ground;
      this.fixed = transforms.fixed;
   }

   /** @deprecated */
   @Deprecated
   public ItemCameraTransforms(ItemTransformVec3f var1, ItemTransformVec3f var2, ItemTransformVec3f var3, ItemTransformVec3f var4, ItemTransformVec3f var5, ItemTransformVec3f var6, ItemTransformVec3f var7, ItemTransformVec3f var8) {
      this.thirdperson_left = thirdperson_leftIn;
      this.thirdperson_right = thirdperson_rightIn;
      this.firstperson_left = firstperson_leftIn;
      this.firstperson_right = firstperson_rightIn;
      this.head = headIn;
      this.gui = guiIn;
      this.ground = groundIn;
      this.fixed = fixedIn;
   }

   public void applyTransform(ItemCameraTransforms.TransformType var1) {
      applyTransformSide(this.getTransform(type), false);
   }

   public static void applyTransformSide(ItemTransformVec3f var0, boolean var1) {
      if (vec != ItemTransformVec3f.DEFAULT) {
         int i = leftHand ? -1 : 1;
         GlStateManager.translate((float)i * (offsetTranslateX + vec.translation.x), offsetTranslateY + vec.translation.y, offsetTranslateZ + vec.translation.z);
         float f = offsetRotationX + vec.rotation.x;
         float f1 = offsetRotationY + vec.rotation.y;
         float f2 = offsetRotationZ + vec.rotation.z;
         if (leftHand) {
            f1 = -f1;
            f2 = -f2;
         }

         GlStateManager.rotate(makeQuaternion(f, f1, f2));
         GlStateManager.scale(offsetScaleX + vec.scale.x, offsetScaleY + vec.scale.y, offsetScaleZ + vec.scale.z);
      }

   }

   private static Quaternion makeQuaternion(float var0, float var1, float var2) {
      float f = p_188035_0_ * 0.017453292F;
      float f1 = p_188035_1_ * 0.017453292F;
      float f2 = p_188035_2_ * 0.017453292F;
      float f3 = MathHelper.sin(0.5F * f);
      float f4 = MathHelper.cos(0.5F * f);
      float f5 = MathHelper.sin(0.5F * f1);
      float f6 = MathHelper.cos(0.5F * f1);
      float f7 = MathHelper.sin(0.5F * f2);
      float f8 = MathHelper.cos(0.5F * f2);
      return new Quaternion(f3 * f6 * f8 + f4 * f5 * f7, f4 * f5 * f8 - f3 * f6 * f7, f3 * f5 * f8 + f4 * f6 * f7, f4 * f6 * f8 - f3 * f5 * f7);
   }

   /** @deprecated */
   @Deprecated
   public ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType var1) {
      switch(type) {
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
      return this.getTransform(type) != ItemTransformVec3f.DEFAULT;
   }

   @SideOnly(Side.CLIENT)
   static class Deserializer implements JsonDeserializer {
      public ItemCameraTransforms deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         ItemTransformVec3f itemtransformvec3f = this.getTransform(p_deserialize_3_, jsonobject, "thirdperson_righthand");
         ItemTransformVec3f itemtransformvec3f1 = this.getTransform(p_deserialize_3_, jsonobject, "thirdperson_lefthand");
         if (itemtransformvec3f1 == ItemTransformVec3f.DEFAULT) {
            itemtransformvec3f1 = itemtransformvec3f;
         }

         ItemTransformVec3f itemtransformvec3f2 = this.getTransform(p_deserialize_3_, jsonobject, "firstperson_righthand");
         ItemTransformVec3f itemtransformvec3f3 = this.getTransform(p_deserialize_3_, jsonobject, "firstperson_lefthand");
         if (itemtransformvec3f3 == ItemTransformVec3f.DEFAULT) {
            itemtransformvec3f3 = itemtransformvec3f2;
         }

         ItemTransformVec3f itemtransformvec3f4 = this.getTransform(p_deserialize_3_, jsonobject, "head");
         ItemTransformVec3f itemtransformvec3f5 = this.getTransform(p_deserialize_3_, jsonobject, "gui");
         ItemTransformVec3f itemtransformvec3f6 = this.getTransform(p_deserialize_3_, jsonobject, "ground");
         ItemTransformVec3f itemtransformvec3f7 = this.getTransform(p_deserialize_3_, jsonobject, "fixed");
         return new ItemCameraTransforms(itemtransformvec3f1, itemtransformvec3f, itemtransformvec3f3, itemtransformvec3f2, itemtransformvec3f4, itemtransformvec3f5, itemtransformvec3f6, itemtransformvec3f7);
      }

      private ItemTransformVec3f getTransform(JsonDeserializationContext var1, JsonObject var2, String var3) {
         return p_181683_2_.has(p_181683_3_) ? (ItemTransformVec3f)p_181683_1_.deserialize(p_181683_2_.get(p_181683_3_), ItemTransformVec3f.class) : ItemTransformVec3f.DEFAULT;
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
