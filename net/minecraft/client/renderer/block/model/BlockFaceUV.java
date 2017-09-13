package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockFaceUV {
   public float[] uvs;
   public final int rotation;

   public BlockFaceUV(@Nullable float[] var1, int var2) {
      this.uvs = uvsIn;
      this.rotation = rotationIn;
   }

   public float getVertexU(int var1) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int i = this.getVertexRotated(p_178348_1_);
         return i != 0 && i != 1 ? this.uvs[2] : this.uvs[0];
      }
   }

   public float getVertexV(int var1) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int i = this.getVertexRotated(p_178346_1_);
         return i != 0 && i != 3 ? this.uvs[3] : this.uvs[1];
      }
   }

   private int getVertexRotated(int var1) {
      return (p_178347_1_ + this.rotation / 90) % 4;
   }

   public int getVertexRotatedRev(int var1) {
      return (p_178345_1_ + (4 - this.rotation / 90)) % 4;
   }

   public void setUvs(float[] var1) {
      if (this.uvs == null) {
         this.uvs = uvsIn;
      }

   }

   @SideOnly(Side.CLIENT)
   static class Deserializer implements JsonDeserializer {
      public BlockFaceUV deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         float[] afloat = this.parseUV(jsonobject);
         int i = this.parseRotation(jsonobject);
         return new BlockFaceUV(afloat, i);
      }

      protected int parseRotation(JsonObject var1) {
         int i = JsonUtils.getInt(object, "rotation", 0);
         if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
            return i;
         } else {
            throw new JsonParseException("Invalid rotation " + i + " found, only 0/90/180/270 allowed");
         }
      }

      @Nullable
      private float[] parseUV(JsonObject var1) {
         if (!object.has("uv")) {
            return null;
         } else {
            JsonArray jsonarray = JsonUtils.getJsonArray(object, "uv");
            if (jsonarray.size() != 4) {
               throw new JsonParseException("Expected 4 uv values, found: " + jsonarray.size());
            } else {
               float[] afloat = new float[4];

               for(int i = 0; i < afloat.length; ++i) {
                  afloat[i] = JsonUtils.getFloat(jsonarray.get(i), "uv[" + i + "]");
               }

               return afloat;
            }
         }
      }
   }
}
