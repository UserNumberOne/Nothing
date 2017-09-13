package net.minecraft.client.shader;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.lwjgl.util.vector.Matrix4f;

@SideOnly(Side.CLIENT)
public class ShaderGroup {
   private final Framebuffer mainFramebuffer;
   private final IResourceManager resourceManager;
   private final String shaderGroupName;
   private final List listShaders = Lists.newArrayList();
   private final Map mapFramebuffers = Maps.newHashMap();
   private final List listFramebuffers = Lists.newArrayList();
   private Matrix4f projectionMatrix;
   private int mainFramebufferWidth;
   private int mainFramebufferHeight;
   private float time;
   private float lastStamp;

   public ShaderGroup(TextureManager var1, IResourceManager var2, Framebuffer var3, ResourceLocation var4) throws JsonException, IOException, JsonSyntaxException {
      this.resourceManager = var2;
      this.mainFramebuffer = var3;
      this.time = 0.0F;
      this.lastStamp = 0.0F;
      this.mainFramebufferWidth = var3.framebufferWidth;
      this.mainFramebufferHeight = var3.framebufferHeight;
      this.shaderGroupName = var4.toString();
      this.resetProjectionMatrix();
      this.parseGroup(var1, var4);
   }

   public void parseGroup(TextureManager var1, ResourceLocation var2) throws JsonException, IOException, JsonSyntaxException {
      JsonParser var3 = new JsonParser();
      IResource var4 = null;

      try {
         var4 = this.resourceManager.getResource(var2);
         JsonObject var5 = var3.parse(IOUtils.toString(var4.getInputStream(), Charsets.UTF_8)).getAsJsonObject();
         if (JsonUtils.isJsonArray(var5, "targets")) {
            JsonArray var21 = var5.getAsJsonArray("targets");
            int var7 = 0;

            for(JsonElement var9 : var21) {
               try {
                  this.initTarget(var9);
               } catch (Exception var18) {
                  JsonException var11 = JsonException.forException(var18);
                  var11.prependJsonKey("targets[" + var7 + "]");
                  throw var11;
               }

               ++var7;
            }
         }

         if (JsonUtils.isJsonArray(var5, "passes")) {
            JsonArray var22 = var5.getAsJsonArray("passes");
            int var23 = 0;

            for(JsonElement var25 : var22) {
               try {
                  this.parsePass(var1, var25);
               } catch (Exception var17) {
                  JsonException var26 = JsonException.forException(var17);
                  var26.prependJsonKey("passes[" + var23 + "]");
                  throw var26;
               }

               ++var23;
            }
         }
      } catch (Exception var19) {
         JsonException var6 = JsonException.forException(var19);
         var6.setFilenameAndFlush(var2.getResourcePath());
         throw var6;
      } finally {
         IOUtils.closeQuietly(var4);
      }

   }

   private void initTarget(JsonElement var1) throws JsonException {
      if (JsonUtils.isString(var1)) {
         this.addFramebuffer(var1.getAsString(), this.mainFramebufferWidth, this.mainFramebufferHeight);
      } else {
         JsonObject var2 = JsonUtils.getJsonObject(var1, "target");
         String var3 = JsonUtils.getString(var2, "name");
         int var4 = JsonUtils.getInt(var2, "width", this.mainFramebufferWidth);
         int var5 = JsonUtils.getInt(var2, "height", this.mainFramebufferHeight);
         if (this.mapFramebuffers.containsKey(var3)) {
            throw new JsonException(var3 + " is already defined");
         }

         this.addFramebuffer(var3, var4, var5);
      }

   }

   private void parsePass(TextureManager var1, JsonElement var2) throws JsonException, IOException {
      JsonObject var3 = JsonUtils.getJsonObject(var2, "pass");
      String var4 = JsonUtils.getString(var3, "name");
      String var5 = JsonUtils.getString(var3, "intarget");
      String var6 = JsonUtils.getString(var3, "outtarget");
      Framebuffer var7 = this.getFramebuffer(var5);
      Framebuffer var8 = this.getFramebuffer(var6);
      if (var7 == null) {
         throw new JsonException("Input target '" + var5 + "' does not exist");
      } else if (var8 == null) {
         throw new JsonException("Output target '" + var6 + "' does not exist");
      } else {
         Shader var9 = this.addShader(var4, var7, var8);
         JsonArray var10 = JsonUtils.getJsonArray(var3, "auxtargets", (JsonArray)null);
         if (var10 != null) {
            int var11 = 0;

            for(JsonElement var13 : var10) {
               try {
                  JsonObject var14 = JsonUtils.getJsonObject(var13, "auxtarget");
                  String var36 = JsonUtils.getString(var14, "name");
                  String var16 = JsonUtils.getString(var14, "id");
                  Framebuffer var17 = this.getFramebuffer(var16);
                  if (var17 == null) {
                     ResourceLocation var18 = new ResourceLocation("textures/effect/" + var16 + ".png");
                     IResource var19 = null;

                     try {
                        var19 = this.resourceManager.getResource(var18);
                     } catch (FileNotFoundException var29) {
                        throw new JsonException("Render target or texture '" + var16 + "' does not exist");
                     } finally {
                        IOUtils.closeQuietly(var19);
                     }

                     var1.bindTexture(var18);
                     ITextureObject var20 = var1.getTexture(var18);
                     int var21 = JsonUtils.getInt(var14, "width");
                     int var22 = JsonUtils.getInt(var14, "height");
                     boolean var23 = JsonUtils.getBoolean(var14, "bilinear");
                     if (var23) {
                        GlStateManager.glTexParameteri(3553, 10241, 9729);
                        GlStateManager.glTexParameteri(3553, 10240, 9729);
                     } else {
                        GlStateManager.glTexParameteri(3553, 10241, 9728);
                        GlStateManager.glTexParameteri(3553, 10240, 9728);
                     }

                     var9.addAuxFramebuffer(var36, Integer.valueOf(var20.getGlTextureId()), var21, var22);
                  } else {
                     var9.addAuxFramebuffer(var36, var17, var17.framebufferTextureWidth, var17.framebufferTextureHeight);
                  }
               } catch (Exception var31) {
                  JsonException var15 = JsonException.forException(var31);
                  var15.prependJsonKey("auxtargets[" + var11 + "]");
                  throw var15;
               }

               ++var11;
            }
         }

         JsonArray var32 = JsonUtils.getJsonArray(var3, "uniforms", (JsonArray)null);
         if (var32 != null) {
            int var33 = 0;

            for(JsonElement var35 : var32) {
               try {
                  this.initUniform(var35);
               } catch (Exception var28) {
                  JsonException var37 = JsonException.forException(var28);
                  var37.prependJsonKey("uniforms[" + var33 + "]");
                  throw var37;
               }

               ++var33;
            }
         }

      }
   }

   private void initUniform(JsonElement var1) throws JsonException {
      JsonObject var2 = JsonUtils.getJsonObject(var1, "uniform");
      String var3 = JsonUtils.getString(var2, "name");
      ShaderUniform var4 = ((Shader)this.listShaders.get(this.listShaders.size() - 1)).getShaderManager().getShaderUniform(var3);
      if (var4 == null) {
         throw new JsonException("Uniform '" + var3 + "' does not exist");
      } else {
         float[] var5 = new float[4];
         int var6 = 0;

         for(JsonElement var8 : JsonUtils.getJsonArray(var2, "values")) {
            try {
               var5[var6] = JsonUtils.getFloat(var8, "value");
            } catch (Exception var11) {
               JsonException var10 = JsonException.forException(var11);
               var10.prependJsonKey("values[" + var6 + "]");
               throw var10;
            }

            ++var6;
         }

         switch(var6) {
         case 0:
         default:
            break;
         case 1:
            var4.set(var5[0]);
            break;
         case 2:
            var4.set(var5[0], var5[1]);
            break;
         case 3:
            var4.set(var5[0], var5[1], var5[2]);
            break;
         case 4:
            var4.set(var5[0], var5[1], var5[2], var5[3]);
         }

      }
   }

   public Framebuffer getFramebufferRaw(String var1) {
      return (Framebuffer)this.mapFramebuffers.get(var1);
   }

   public void addFramebuffer(String var1, int var2, int var3) {
      Framebuffer var4 = new Framebuffer(var2, var3, true);
      var4.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.mapFramebuffers.put(var1, var4);
      if (var2 == this.mainFramebufferWidth && var3 == this.mainFramebufferHeight) {
         this.listFramebuffers.add(var4);
      }

   }

   public void deleteShaderGroup() {
      for(Framebuffer var2 : this.mapFramebuffers.values()) {
         var2.deleteFramebuffer();
      }

      for(Shader var4 : this.listShaders) {
         var4.deleteShader();
      }

      this.listShaders.clear();
   }

   public Shader addShader(String var1, Framebuffer var2, Framebuffer var3) throws JsonException, IOException {
      Shader var4 = new Shader(this.resourceManager, var1, var2, var3);
      this.listShaders.add(this.listShaders.size(), var4);
      return var4;
   }

   private void resetProjectionMatrix() {
      this.projectionMatrix = new Matrix4f();
      this.projectionMatrix.setIdentity();
      this.projectionMatrix.m00 = 2.0F / (float)this.mainFramebuffer.framebufferTextureWidth;
      this.projectionMatrix.m11 = 2.0F / (float)(-this.mainFramebuffer.framebufferTextureHeight);
      this.projectionMatrix.m22 = -0.0020001999F;
      this.projectionMatrix.m33 = 1.0F;
      this.projectionMatrix.m03 = -1.0F;
      this.projectionMatrix.m13 = 1.0F;
      this.projectionMatrix.m23 = -1.0001999F;
   }

   public void createBindFramebuffers(int var1, int var2) {
      this.mainFramebufferWidth = this.mainFramebuffer.framebufferTextureWidth;
      this.mainFramebufferHeight = this.mainFramebuffer.framebufferTextureHeight;
      this.resetProjectionMatrix();

      for(Shader var4 : this.listShaders) {
         var4.setProjectionMatrix(this.projectionMatrix);
      }

      for(Framebuffer var6 : this.listFramebuffers) {
         var6.createBindFramebuffer(var1, var2);
      }

   }

   public void loadShaderGroup(float var1) {
      if (var1 < this.lastStamp) {
         this.time += 1.0F - this.lastStamp;
         this.time += var1;
      } else {
         this.time += var1 - this.lastStamp;
      }

      for(this.lastStamp = var1; this.time > 20.0F; this.time -= 20.0F) {
         ;
      }

      for(Shader var3 : this.listShaders) {
         var3.loadShader(this.time / 20.0F);
      }

   }

   public final String getShaderGroupName() {
      return this.shaderGroupName;
   }

   private Framebuffer getFramebuffer(String var1) {
      return var1 == null ? null : (var1.equals("minecraft:main") ? this.mainFramebuffer : (Framebuffer)this.mapFramebuffers.get(var1));
   }
}
