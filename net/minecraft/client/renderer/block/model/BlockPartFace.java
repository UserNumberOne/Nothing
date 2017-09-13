package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockPartFace {
   public static final EnumFacing FACING_DEFAULT = null;
   public final EnumFacing cullFace;
   public final int tintIndex;
   public final String texture;
   public final BlockFaceUV blockFaceUV;

   public BlockPartFace(@Nullable EnumFacing var1, int var2, String var3, BlockFaceUV var4) {
      this.cullFace = cullFaceIn;
      this.tintIndex = tintIndexIn;
      this.texture = textureIn;
      this.blockFaceUV = blockFaceUVIn;
   }

   @SideOnly(Side.CLIENT)
   static class Deserializer implements JsonDeserializer {
      public BlockPartFace deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         EnumFacing enumfacing = this.parseCullFace(jsonobject);
         int i = this.parseTintIndex(jsonobject);
         String s = this.parseTexture(jsonobject);
         BlockFaceUV blockfaceuv = (BlockFaceUV)p_deserialize_3_.deserialize(jsonobject, BlockFaceUV.class);
         return new BlockPartFace(enumfacing, i, s, blockfaceuv);
      }

      protected int parseTintIndex(JsonObject var1) {
         return JsonUtils.getInt(object, "tintindex", -1);
      }

      private String parseTexture(JsonObject var1) {
         return JsonUtils.getString(object, "texture");
      }

      @Nullable
      private EnumFacing parseCullFace(JsonObject var1) {
         String s = JsonUtils.getString(object, "cullface", "");
         return EnumFacing.byName(s);
      }
   }
}
