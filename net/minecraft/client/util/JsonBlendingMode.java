package net.minecraft.client.util;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class JsonBlendingMode {
   private static JsonBlendingMode lastApplied;
   private final int srcColorFactor;
   private final int srcAlphaFactor;
   private final int destColorFactor;
   private final int destAlphaFactor;
   private final int blendFunction;
   private final boolean separateBlend;
   private final boolean opaque;

   private JsonBlendingMode(boolean var1, boolean var2, int var3, int var4, int var5, int var6, int var7) {
      this.separateBlend = var1;
      this.srcColorFactor = var3;
      this.destColorFactor = var4;
      this.srcAlphaFactor = var5;
      this.destAlphaFactor = var6;
      this.opaque = var2;
      this.blendFunction = var7;
   }

   public JsonBlendingMode() {
      this(false, true, 1, 0, 1, 0, 32774);
   }

   public JsonBlendingMode(int var1, int var2, int var3) {
      this(false, false, var1, var2, var1, var2, var3);
   }

   public JsonBlendingMode(int var1, int var2, int var3, int var4, int var5) {
      this(true, false, var1, var2, var3, var4, var5);
   }

   public void apply() {
      if (!this.equals(lastApplied)) {
         if (lastApplied == null || this.opaque != lastApplied.isOpaque()) {
            lastApplied = this;
            if (this.opaque) {
               GlStateManager.disableBlend();
               return;
            }

            GlStateManager.enableBlend();
         }

         GlStateManager.glBlendEquation(this.blendFunction);
         if (this.separateBlend) {
            GlStateManager.tryBlendFuncSeparate(this.srcColorFactor, this.destColorFactor, this.srcAlphaFactor, this.destAlphaFactor);
         } else {
            GlStateManager.blendFunc(this.srcColorFactor, this.destColorFactor);
         }
      }

   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof JsonBlendingMode)) {
         return false;
      } else {
         JsonBlendingMode var2 = (JsonBlendingMode)var1;
         return this.blendFunction != var2.blendFunction ? false : (this.destAlphaFactor != var2.destAlphaFactor ? false : (this.destColorFactor != var2.destColorFactor ? false : (this.opaque != var2.opaque ? false : (this.separateBlend != var2.separateBlend ? false : (this.srcAlphaFactor != var2.srcAlphaFactor ? false : this.srcColorFactor == var2.srcColorFactor)))));
      }
   }

   public int hashCode() {
      int var1 = this.srcColorFactor;
      var1 = 31 * var1 + this.srcAlphaFactor;
      var1 = 31 * var1 + this.destColorFactor;
      var1 = 31 * var1 + this.destAlphaFactor;
      var1 = 31 * var1 + this.blendFunction;
      var1 = 31 * var1 + (this.separateBlend ? 1 : 0);
      var1 = 31 * var1 + (this.opaque ? 1 : 0);
      return var1;
   }

   public boolean isOpaque() {
      return this.opaque;
   }

   public static JsonBlendingMode parseBlendNode(JsonObject var0) {
      if (var0 == null) {
         return new JsonBlendingMode();
      } else {
         int var1 = 32774;
         int var2 = 1;
         int var3 = 0;
         int var4 = 1;
         int var5 = 0;
         boolean var6 = true;
         boolean var7 = false;
         if (JsonUtils.isString(var0, "func")) {
            var1 = stringToBlendFunction(var0.get("func").getAsString());
            if (var1 != 32774) {
               var6 = false;
            }
         }

         if (JsonUtils.isString(var0, "srcrgb")) {
            var2 = stringToBlendFactor(var0.get("srcrgb").getAsString());
            if (var2 != 1) {
               var6 = false;
            }
         }

         if (JsonUtils.isString(var0, "dstrgb")) {
            var3 = stringToBlendFactor(var0.get("dstrgb").getAsString());
            if (var3 != 0) {
               var6 = false;
            }
         }

         if (JsonUtils.isString(var0, "srcalpha")) {
            var4 = stringToBlendFactor(var0.get("srcalpha").getAsString());
            if (var4 != 1) {
               var6 = false;
            }

            var7 = true;
         }

         if (JsonUtils.isString(var0, "dstalpha")) {
            var5 = stringToBlendFactor(var0.get("dstalpha").getAsString());
            if (var5 != 0) {
               var6 = false;
            }

            var7 = true;
         }

         return var6 ? new JsonBlendingMode() : (var7 ? new JsonBlendingMode(var2, var3, var4, var5, var1) : new JsonBlendingMode(var2, var3, var1));
      }
   }

   private static int stringToBlendFunction(String var0) {
      String var1 = var0.trim().toLowerCase();
      return "add".equals(var1) ? '耆' : ("subtract".equals(var1) ? '耊' : ("reversesubtract".equals(var1) ? '耋' : ("reverse_subtract".equals(var1) ? '耋' : ("min".equals(var1) ? '耇' : ("max".equals(var1) ? '耈' : '耆')))));
   }

   private static int stringToBlendFactor(String var0) {
      String var1 = var0.trim().toLowerCase();
      var1 = var1.replaceAll("_", "");
      var1 = var1.replaceAll("one", "1");
      var1 = var1.replaceAll("zero", "0");
      var1 = var1.replaceAll("minus", "-");
      return "0".equals(var1) ? 0 : ("1".equals(var1) ? 1 : ("srccolor".equals(var1) ? 768 : ("1-srccolor".equals(var1) ? 769 : ("dstcolor".equals(var1) ? 774 : ("1-dstcolor".equals(var1) ? 775 : ("srcalpha".equals(var1) ? 770 : ("1-srcalpha".equals(var1) ? 771 : ("dstalpha".equals(var1) ? 772 : ("1-dstalpha".equals(var1) ? 773 : -1)))))))));
   }
}
