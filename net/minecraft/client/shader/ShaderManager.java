package net.minecraft.client.shader;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonBlendingMode;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ShaderManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ShaderDefault DEFAULT_SHADER_UNIFORM = new ShaderDefault();
   private static ShaderManager staticShaderManager;
   private static int currentProgram = -1;
   private static boolean lastCull = true;
   private final Map shaderSamplers = Maps.newHashMap();
   private final List samplerNames = Lists.newArrayList();
   private final List shaderSamplerLocations = Lists.newArrayList();
   private final List shaderUniforms = Lists.newArrayList();
   private final List shaderUniformLocations = Lists.newArrayList();
   private final Map mappedShaderUniforms = Maps.newHashMap();
   private final int program;
   private final String programFilename;
   private final boolean useFaceCulling;
   private boolean isDirty;
   private final JsonBlendingMode blendingMode;
   private final List attribLocations;
   private final List attributes;
   private final ShaderLoader vertexShaderLoader;
   private final ShaderLoader fragmentShaderLoader;

   public ShaderManager(IResourceManager var1, String var2) throws JsonException, IOException {
      JsonParser var3 = new JsonParser();
      ResourceLocation var4 = new ResourceLocation("shaders/program/" + var2 + ".json");
      this.programFilename = var2;
      IResource var5 = null;

      try {
         var5 = var1.getResource(var4);
         JsonObject var6 = var3.parse(IOUtils.toString(var5.getInputStream(), Charsets.UTF_8)).getAsJsonObject();
         String var28 = JsonUtils.getString(var6, "vertex");
         String var8 = JsonUtils.getString(var6, "fragment");
         JsonArray var9 = JsonUtils.getJsonArray(var6, "samplers", (JsonArray)null);
         if (var9 != null) {
            int var10 = 0;

            for(JsonElement var12 : var9) {
               try {
                  this.parseSampler(var12);
               } catch (Exception var25) {
                  JsonException var14 = JsonException.forException(var25);
                  var14.prependJsonKey("samplers[" + var10 + "]");
                  throw var14;
               }

               ++var10;
            }
         }

         JsonArray var29 = JsonUtils.getJsonArray(var6, "attributes", (JsonArray)null);
         if (var29 != null) {
            int var30 = 0;
            this.attribLocations = Lists.newArrayListWithCapacity(var29.size());
            this.attributes = Lists.newArrayListWithCapacity(var29.size());

            for(JsonElement var13 : var29) {
               try {
                  this.attributes.add(JsonUtils.getString(var13, "attribute"));
               } catch (Exception var24) {
                  JsonException var15 = JsonException.forException(var24);
                  var15.prependJsonKey("attributes[" + var30 + "]");
                  throw var15;
               }

               ++var30;
            }
         } else {
            this.attribLocations = null;
            this.attributes = null;
         }

         JsonArray var31 = JsonUtils.getJsonArray(var6, "uniforms", (JsonArray)null);
         if (var31 != null) {
            int var33 = 0;

            for(JsonElement var37 : var31) {
               try {
                  this.parseUniform(var37);
               } catch (Exception var23) {
                  JsonException var16 = JsonException.forException(var23);
                  var16.prependJsonKey("uniforms[" + var33 + "]");
                  throw var16;
               }

               ++var33;
            }
         }

         this.blendingMode = JsonBlendingMode.parseBlendNode(JsonUtils.getJsonObject(var6, "blend", (JsonObject)null));
         this.useFaceCulling = JsonUtils.getBoolean(var6, "cull", true);
         this.vertexShaderLoader = ShaderLoader.loadShader(var1, ShaderLoader.ShaderType.VERTEX, var28);
         this.fragmentShaderLoader = ShaderLoader.loadShader(var1, ShaderLoader.ShaderType.FRAGMENT, var8);
         this.program = ShaderLinkHelper.getStaticShaderLinkHelper().createProgram();
         ShaderLinkHelper.getStaticShaderLinkHelper().linkProgram(this);
         this.setupUniforms();
         if (this.attributes != null) {
            for(String var36 : this.attributes) {
               int var38 = OpenGlHelper.glGetAttribLocation(this.program, var36);
               this.attribLocations.add(Integer.valueOf(var38));
            }
         }
      } catch (Exception var26) {
         JsonException var7 = JsonException.forException(var26);
         var7.setFilenameAndFlush(var4.getResourcePath());
         throw var7;
      } finally {
         IOUtils.closeQuietly(var5);
      }

      this.markDirty();
   }

   public void deleteShader() {
      ShaderLinkHelper.getStaticShaderLinkHelper().deleteShader(this);
   }

   public void endShader() {
      OpenGlHelper.glUseProgram(0);
      currentProgram = -1;
      staticShaderManager = null;
      lastCull = true;

      for(int var1 = 0; var1 < this.shaderSamplerLocations.size(); ++var1) {
         if (this.shaderSamplers.get(this.samplerNames.get(var1)) != null) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + var1);
            GlStateManager.bindTexture(0);
         }
      }

   }

   public void useShader() {
      this.isDirty = false;
      staticShaderManager = this;
      this.blendingMode.apply();
      if (this.program != currentProgram) {
         OpenGlHelper.glUseProgram(this.program);
         currentProgram = this.program;
      }

      if (this.useFaceCulling) {
         GlStateManager.enableCull();
      } else {
         GlStateManager.disableCull();
      }

      for(int var1 = 0; var1 < this.shaderSamplerLocations.size(); ++var1) {
         if (this.shaderSamplers.get(this.samplerNames.get(var1)) != null) {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + var1);
            GlStateManager.enableTexture2D();
            Object var2 = this.shaderSamplers.get(this.samplerNames.get(var1));
            int var3 = -1;
            if (var2 instanceof Framebuffer) {
               var3 = ((Framebuffer)var2).framebufferTexture;
            } else if (var2 instanceof ITextureObject) {
               var3 = ((ITextureObject)var2).getGlTextureId();
            } else if (var2 instanceof Integer) {
               var3 = ((Integer)var2).intValue();
            }

            if (var3 != -1) {
               GlStateManager.bindTexture(var3);
               OpenGlHelper.glUniform1i(OpenGlHelper.glGetUniformLocation(this.program, (CharSequence)this.samplerNames.get(var1)), var1);
            }
         }
      }

      for(ShaderUniform var5 : this.shaderUniforms) {
         var5.upload();
      }

   }

   public void markDirty() {
      this.isDirty = true;
   }

   public ShaderUniform getShaderUniform(String var1) {
      return this.mappedShaderUniforms.containsKey(var1) ? (ShaderUniform)this.mappedShaderUniforms.get(var1) : null;
   }

   public ShaderUniform getShaderUniformOrDefault(String var1) {
      return (ShaderUniform)(this.mappedShaderUniforms.containsKey(var1) ? (ShaderUniform)this.mappedShaderUniforms.get(var1) : DEFAULT_SHADER_UNIFORM);
   }

   private void setupUniforms() {
      int var1 = 0;

      for(int var2 = 0; var1 < this.samplerNames.size(); ++var2) {
         String var3 = (String)this.samplerNames.get(var1);
         int var4 = OpenGlHelper.glGetUniformLocation(this.program, var3);
         if (var4 == -1) {
            LOGGER.warn("Shader {}could not find sampler named {} in the specified shader program.", new Object[]{this.programFilename, var3});
            this.shaderSamplers.remove(var3);
            this.samplerNames.remove(var2);
            --var2;
         } else {
            this.shaderSamplerLocations.add(Integer.valueOf(var4));
         }

         ++var1;
      }

      for(ShaderUniform var7 : this.shaderUniforms) {
         String var8 = var7.getShaderName();
         int var5 = OpenGlHelper.glGetUniformLocation(this.program, var8);
         if (var5 == -1) {
            LOGGER.warn("Could not find uniform named {} in the specified shader program.", new Object[]{var8});
         } else {
            this.shaderUniformLocations.add(Integer.valueOf(var5));
            var7.setUniformLocation(var5);
            this.mappedShaderUniforms.put(var8, var7);
         }
      }

   }

   private void parseSampler(JsonElement var1) throws JsonException {
      JsonObject var2 = JsonUtils.getJsonObject(var1, "sampler");
      String var3 = JsonUtils.getString(var2, "name");
      if (!JsonUtils.isString(var2, "file")) {
         this.shaderSamplers.put(var3, (Object)null);
         this.samplerNames.add(var3);
      } else {
         this.samplerNames.add(var3);
      }

   }

   public void addSamplerTexture(String var1, Object var2) {
      if (this.shaderSamplers.containsKey(var1)) {
         this.shaderSamplers.remove(var1);
      }

      this.shaderSamplers.put(var1, var2);
      this.markDirty();
   }

   private void parseUniform(JsonElement var1) throws JsonException {
      JsonObject var2 = JsonUtils.getJsonObject(var1, "uniform");
      String var3 = JsonUtils.getString(var2, "name");
      int var4 = ShaderUniform.parseType(JsonUtils.getString(var2, "type"));
      int var5 = JsonUtils.getInt(var2, "count");
      float[] var6 = new float[Math.max(var5, 16)];
      JsonArray var7 = JsonUtils.getJsonArray(var2, "values");
      if (var7.size() != var5 && var7.size() > 1) {
         throw new JsonException("Invalid amount of values specified (expected " + var5 + ", found " + var7.size() + ")");
      } else {
         int var8 = 0;

         for(JsonElement var10 : var7) {
            try {
               var6[var8] = JsonUtils.getFloat(var10, "value");
            } catch (Exception var13) {
               JsonException var12 = JsonException.forException(var13);
               var12.prependJsonKey("values[" + var8 + "]");
               throw var12;
            }

            ++var8;
         }

         if (var5 > 1 && var7.size() == 1) {
            while(var8 < var5) {
               var6[var8] = var6[0];
               ++var8;
            }
         }

         int var14 = var5 > 1 && var5 <= 4 && var4 < 8 ? var5 - 1 : 0;
         ShaderUniform var15 = new ShaderUniform(var3, var4 + var14, var5, this);
         if (var4 <= 3) {
            var15.set((int)var6[0], (int)var6[1], (int)var6[2], (int)var6[3]);
         } else if (var4 <= 7) {
            var15.setSafe(var6[0], var6[1], var6[2], var6[3]);
         } else {
            var15.set(var6);
         }

         this.shaderUniforms.add(var15);
      }
   }

   public ShaderLoader getVertexShaderLoader() {
      return this.vertexShaderLoader;
   }

   public ShaderLoader getFragmentShaderLoader() {
      return this.fragmentShaderLoader;
   }

   public int getProgram() {
      return this.program;
   }
}
