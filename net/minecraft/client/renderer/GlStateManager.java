package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Quaternion;

@SideOnly(Side.CLIENT)
public class GlStateManager {
   private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
   private static final FloatBuffer BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4);
   private static final GlStateManager.AlphaState alphaState = new GlStateManager.AlphaState();
   private static final GlStateManager.BooleanState lightingState = new GlStateManager.BooleanState(2896);
   private static final GlStateManager.BooleanState[] lightState = new GlStateManager.BooleanState[8];
   private static final GlStateManager.ColorMaterialState colorMaterialState;
   private static final GlStateManager.BlendState blendState;
   private static final GlStateManager.DepthState depthState;
   private static final GlStateManager.FogState fogState;
   private static final GlStateManager.CullState cullState;
   private static final GlStateManager.PolygonOffsetState polygonOffsetState;
   private static final GlStateManager.ColorLogicState colorLogicState;
   private static final GlStateManager.TexGenState texGenState;
   private static final GlStateManager.ClearState clearState;
   private static final GlStateManager.StencilState stencilState;
   private static final GlStateManager.BooleanState normalizeState;
   private static int activeTextureUnit;
   private static final GlStateManager.TextureState[] textureState;
   private static int activeShadeModel;
   private static final GlStateManager.BooleanState rescaleNormalState;
   private static final GlStateManager.ColorMask colorMaskState;
   private static final GlStateManager.Color colorState;

   public static void pushAttrib() {
      GL11.glPushAttrib(8256);
   }

   public static void popAttrib() {
      GL11.glPopAttrib();
   }

   public static void disableAlpha() {
      alphaState.alphaTest.setDisabled();
   }

   public static void enableAlpha() {
      alphaState.alphaTest.setEnabled();
   }

   public static void alphaFunc(int var0, float var1) {
      if (var0 != alphaState.func || var1 != alphaState.ref) {
         alphaState.func = var0;
         alphaState.ref = var1;
         GL11.glAlphaFunc(var0, var1);
      }

   }

   public static void enableLighting() {
      lightingState.setEnabled();
   }

   public static void disableLighting() {
      lightingState.setDisabled();
   }

   public static void enableLight(int var0) {
      lightState[var0].setEnabled();
   }

   public static void disableLight(int var0) {
      lightState[var0].setDisabled();
   }

   public static void enableColorMaterial() {
      colorMaterialState.colorMaterial.setEnabled();
   }

   public static void disableColorMaterial() {
      colorMaterialState.colorMaterial.setDisabled();
   }

   public static void colorMaterial(int var0, int var1) {
      if (var0 != colorMaterialState.face || var1 != colorMaterialState.mode) {
         colorMaterialState.face = var0;
         colorMaterialState.mode = var1;
         GL11.glColorMaterial(var0, var1);
      }

   }

   public static void glLight(int var0, int var1, FloatBuffer var2) {
      GL11.glLight(var0, var1, var2);
   }

   public static void glLightModel(int var0, FloatBuffer var1) {
      GL11.glLightModel(var0, var1);
   }

   public static void glNormal3f(float var0, float var1, float var2) {
      GL11.glNormal3f(var0, var1, var2);
   }

   public static void disableDepth() {
      depthState.depthTest.setDisabled();
   }

   public static void enableDepth() {
      depthState.depthTest.setEnabled();
   }

   public static void depthFunc(int var0) {
      if (var0 != depthState.depthFunc) {
         depthState.depthFunc = var0;
         GL11.glDepthFunc(var0);
      }

   }

   public static void depthMask(boolean var0) {
      if (var0 != depthState.maskEnabled) {
         depthState.maskEnabled = var0;
         GL11.glDepthMask(var0);
      }

   }

   public static void disableBlend() {
      blendState.blend.setDisabled();
   }

   public static void enableBlend() {
      blendState.blend.setEnabled();
   }

   public static void blendFunc(GlStateManager.SourceFactor var0, GlStateManager.DestFactor var1) {
      blendFunc(var0.factor, var1.factor);
   }

   public static void blendFunc(int var0, int var1) {
      if (var0 != blendState.srcFactor || var1 != blendState.dstFactor) {
         blendState.srcFactor = var0;
         blendState.dstFactor = var1;
         GL11.glBlendFunc(var0, var1);
      }

   }

   public static void tryBlendFuncSeparate(GlStateManager.SourceFactor var0, GlStateManager.DestFactor var1, GlStateManager.SourceFactor var2, GlStateManager.DestFactor var3) {
      tryBlendFuncSeparate(var0.factor, var1.factor, var2.factor, var3.factor);
   }

   public static void tryBlendFuncSeparate(int var0, int var1, int var2, int var3) {
      if (var0 != blendState.srcFactor || var1 != blendState.dstFactor || var2 != blendState.srcFactorAlpha || var3 != blendState.dstFactorAlpha) {
         blendState.srcFactor = var0;
         blendState.dstFactor = var1;
         blendState.srcFactorAlpha = var2;
         blendState.dstFactorAlpha = var3;
         OpenGlHelper.glBlendFunc(var0, var1, var2, var3);
      }

   }

   public static void glBlendEquation(int var0) {
      GL14.glBlendEquation(var0);
   }

   public static void enableOutlineMode(int var0) {
      BUF_FLOAT_4.put(0, (float)(var0 >> 16 & 255) / 255.0F);
      BUF_FLOAT_4.put(1, (float)(var0 >> 8 & 255) / 255.0F);
      BUF_FLOAT_4.put(2, (float)(var0 >> 0 & 255) / 255.0F);
      BUF_FLOAT_4.put(3, (float)(var0 >> 24 & 255) / 255.0F);
      glTexEnv(8960, 8705, BUF_FLOAT_4);
      glTexEnvi(8960, 8704, 34160);
      glTexEnvi(8960, 34161, 7681);
      glTexEnvi(8960, 34176, 34166);
      glTexEnvi(8960, 34192, 768);
      glTexEnvi(8960, 34162, 7681);
      glTexEnvi(8960, 34184, 5890);
      glTexEnvi(8960, 34200, 770);
   }

   public static void disableOutlineMode() {
      glTexEnvi(8960, 8704, 8448);
      glTexEnvi(8960, 34161, 8448);
      glTexEnvi(8960, 34162, 8448);
      glTexEnvi(8960, 34176, 5890);
      glTexEnvi(8960, 34184, 5890);
      glTexEnvi(8960, 34192, 768);
      glTexEnvi(8960, 34200, 770);
   }

   public static void enableFog() {
      fogState.fog.setEnabled();
   }

   public static void disableFog() {
      fogState.fog.setDisabled();
   }

   public static void setFog(GlStateManager.FogMode var0) {
      setFog(var0.capabilityId);
   }

   private static void setFog(int var0) {
      if (var0 != fogState.mode) {
         fogState.mode = var0;
         GL11.glFogi(2917, var0);
      }

   }

   public static void setFogDensity(float var0) {
      if (var0 != fogState.density) {
         fogState.density = var0;
         GL11.glFogf(2914, var0);
      }

   }

   public static void setFogStart(float var0) {
      if (var0 != fogState.start) {
         fogState.start = var0;
         GL11.glFogf(2915, var0);
      }

   }

   public static void setFogEnd(float var0) {
      if (var0 != fogState.end) {
         fogState.end = var0;
         GL11.glFogf(2916, var0);
      }

   }

   public static void glFog(int var0, FloatBuffer var1) {
      GL11.glFog(var0, var1);
   }

   public static void glFogi(int var0, int var1) {
      GL11.glFogi(var0, var1);
   }

   public static void enableCull() {
      cullState.cullFace.setEnabled();
   }

   public static void disableCull() {
      cullState.cullFace.setDisabled();
   }

   public static void cullFace(GlStateManager.CullFace var0) {
      cullFace(var0.mode);
   }

   private static void cullFace(int var0) {
      if (var0 != cullState.mode) {
         cullState.mode = var0;
         GL11.glCullFace(var0);
      }

   }

   public static void glPolygonMode(int var0, int var1) {
      GL11.glPolygonMode(var0, var1);
   }

   public static void enablePolygonOffset() {
      polygonOffsetState.polygonOffsetFill.setEnabled();
   }

   public static void disablePolygonOffset() {
      polygonOffsetState.polygonOffsetFill.setDisabled();
   }

   public static void doPolygonOffset(float var0, float var1) {
      if (var0 != polygonOffsetState.factor || var1 != polygonOffsetState.units) {
         polygonOffsetState.factor = var0;
         polygonOffsetState.units = var1;
         GL11.glPolygonOffset(var0, var1);
      }

   }

   public static void enableColorLogic() {
      colorLogicState.colorLogicOp.setEnabled();
   }

   public static void disableColorLogic() {
      colorLogicState.colorLogicOp.setDisabled();
   }

   public static void colorLogicOp(GlStateManager.LogicOp var0) {
      colorLogicOp(var0.opcode);
   }

   public static void colorLogicOp(int var0) {
      if (var0 != colorLogicState.opcode) {
         colorLogicState.opcode = var0;
         GL11.glLogicOp(var0);
      }

   }

   public static void enableTexGenCoord(GlStateManager.TexGen var0) {
      texGenCoord(var0).textureGen.setEnabled();
   }

   public static void disableTexGenCoord(GlStateManager.TexGen var0) {
      texGenCoord(var0).textureGen.setDisabled();
   }

   public static void texGen(GlStateManager.TexGen var0, int var1) {
      GlStateManager.TexGenCoord var2 = texGenCoord(var0);
      if (var1 != var2.param) {
         var2.param = var1;
         GL11.glTexGeni(var2.coord, 9472, var1);
      }

   }

   public static void texGen(GlStateManager.TexGen var0, int var1, FloatBuffer var2) {
      GL11.glTexGen(texGenCoord(var0).coord, var1, var2);
   }

   private static GlStateManager.TexGenCoord texGenCoord(GlStateManager.TexGen var0) {
      switch(var0) {
      case S:
         return texGenState.s;
      case T:
         return texGenState.t;
      case R:
         return texGenState.r;
      case Q:
         return texGenState.q;
      default:
         return texGenState.s;
      }
   }

   public static void setActiveTexture(int var0) {
      if (activeTextureUnit != var0 - OpenGlHelper.defaultTexUnit) {
         activeTextureUnit = var0 - OpenGlHelper.defaultTexUnit;
         OpenGlHelper.setActiveTexture(var0);
      }

   }

   public static void enableTexture2D() {
      textureState[activeTextureUnit].texture2DState.setEnabled();
   }

   public static void disableTexture2D() {
      textureState[activeTextureUnit].texture2DState.setDisabled();
   }

   public static void glTexEnv(int var0, int var1, FloatBuffer var2) {
      GL11.glTexEnv(var0, var1, var2);
   }

   public static void glTexEnvi(int var0, int var1, int var2) {
      GL11.glTexEnvi(var0, var1, var2);
   }

   public static void glTexEnvf(int var0, int var1, float var2) {
      GL11.glTexEnvf(var0, var1, var2);
   }

   public static void glTexParameterf(int var0, int var1, float var2) {
      GL11.glTexParameterf(var0, var1, var2);
   }

   public static void glTexParameteri(int var0, int var1, int var2) {
      GL11.glTexParameteri(var0, var1, var2);
   }

   public static int glGetTexLevelParameteri(int var0, int var1, int var2) {
      return GL11.glGetTexLevelParameteri(var0, var1, var2);
   }

   public static int generateTexture() {
      return GL11.glGenTextures();
   }

   public static void deleteTexture(int var0) {
      GL11.glDeleteTextures(var0);

      for(GlStateManager.TextureState var4 : textureState) {
         if (var4.textureName == var0) {
            var4.textureName = -1;
         }
      }

   }

   public static void bindTexture(int var0) {
      if (var0 != textureState[activeTextureUnit].textureName) {
         textureState[activeTextureUnit].textureName = var0;
         GL11.glBindTexture(3553, var0);
      }

   }

   public static void glTexImage2D(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, @Nullable IntBuffer var8) {
      GL11.glTexImage2D(var0, var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public static void glTexSubImage2D(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, IntBuffer var8) {
      GL11.glTexSubImage2D(var0, var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public static void glCopyTexSubImage2D(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      GL11.glCopyTexSubImage2D(var0, var1, var2, var3, var4, var5, var6, var7);
   }

   public static void glGetTexImage(int var0, int var1, int var2, int var3, IntBuffer var4) {
      GL11.glGetTexImage(var0, var1, var2, var3, var4);
   }

   public static void enableNormalize() {
      normalizeState.setEnabled();
   }

   public static void disableNormalize() {
      normalizeState.setDisabled();
   }

   public static void shadeModel(int var0) {
      if (var0 != activeShadeModel) {
         activeShadeModel = var0;
         GL11.glShadeModel(var0);
      }

   }

   public static void enableRescaleNormal() {
      rescaleNormalState.setEnabled();
   }

   public static void disableRescaleNormal() {
      rescaleNormalState.setDisabled();
   }

   public static void viewport(int var0, int var1, int var2, int var3) {
      GL11.glViewport(var0, var1, var2, var3);
   }

   public static void colorMask(boolean var0, boolean var1, boolean var2, boolean var3) {
      if (var0 != colorMaskState.red || var1 != colorMaskState.green || var2 != colorMaskState.blue || var3 != colorMaskState.alpha) {
         colorMaskState.red = var0;
         colorMaskState.green = var1;
         colorMaskState.blue = var2;
         colorMaskState.alpha = var3;
         GL11.glColorMask(var0, var1, var2, var3);
      }

   }

   public static void clearDepth(double var0) {
      if (var0 != clearState.depth) {
         clearState.depth = var0;
         GL11.glClearDepth(var0);
      }

   }

   public static void clearColor(float var0, float var1, float var2, float var3) {
      if (var0 != clearState.color.red || var1 != clearState.color.green || var2 != clearState.color.blue || var3 != clearState.color.alpha) {
         clearState.color.red = var0;
         clearState.color.green = var1;
         clearState.color.blue = var2;
         clearState.color.alpha = var3;
         GL11.glClearColor(var0, var1, var2, var3);
      }

   }

   public static void clear(int var0) {
      GL11.glClear(var0);
   }

   public static void matrixMode(int var0) {
      GL11.glMatrixMode(var0);
   }

   public static void loadIdentity() {
      GL11.glLoadIdentity();
   }

   public static void pushMatrix() {
      GL11.glPushMatrix();
   }

   public static void popMatrix() {
      GL11.glPopMatrix();
   }

   public static void getFloat(int var0, FloatBuffer var1) {
      GL11.glGetFloat(var0, var1);
   }

   public static void ortho(double var0, double var2, double var4, double var6, double var8, double var10) {
      GL11.glOrtho(var0, var2, var4, var6, var8, var10);
   }

   public static void rotate(float var0, float var1, float var2, float var3) {
      GL11.glRotatef(var0, var1, var2, var3);
   }

   public static void scale(float var0, float var1, float var2) {
      GL11.glScalef(var0, var1, var2);
   }

   public static void scale(double var0, double var2, double var4) {
      GL11.glScaled(var0, var2, var4);
   }

   public static void translate(float var0, float var1, float var2) {
      GL11.glTranslatef(var0, var1, var2);
   }

   public static void translate(double var0, double var2, double var4) {
      GL11.glTranslated(var0, var2, var4);
   }

   public static void multMatrix(FloatBuffer var0) {
      GL11.glMultMatrix(var0);
   }

   public static void rotate(Quaternion var0) {
      multMatrix(quatToGlMatrix(BUF_FLOAT_16, var0));
   }

   public static FloatBuffer quatToGlMatrix(FloatBuffer var0, Quaternion var1) {
      var0.clear();
      float var2 = var1.x * var1.x;
      float var3 = var1.x * var1.y;
      float var4 = var1.x * var1.z;
      float var5 = var1.x * var1.w;
      float var6 = var1.y * var1.y;
      float var7 = var1.y * var1.z;
      float var8 = var1.y * var1.w;
      float var9 = var1.z * var1.z;
      float var10 = var1.z * var1.w;
      var0.put(1.0F - 2.0F * (var6 + var9));
      var0.put(2.0F * (var3 + var10));
      var0.put(2.0F * (var4 - var8));
      var0.put(0.0F);
      var0.put(2.0F * (var3 - var10));
      var0.put(1.0F - 2.0F * (var2 + var9));
      var0.put(2.0F * (var7 + var5));
      var0.put(0.0F);
      var0.put(2.0F * (var4 + var8));
      var0.put(2.0F * (var7 - var5));
      var0.put(1.0F - 2.0F * (var2 + var6));
      var0.put(0.0F);
      var0.put(0.0F);
      var0.put(0.0F);
      var0.put(0.0F);
      var0.put(1.0F);
      var0.rewind();
      return var0;
   }

   public static void color(float var0, float var1, float var2, float var3) {
      if (var0 != colorState.red || var1 != colorState.green || var2 != colorState.blue || var3 != colorState.alpha) {
         colorState.red = var0;
         colorState.green = var1;
         colorState.blue = var2;
         colorState.alpha = var3;
         GL11.glColor4f(var0, var1, var2, var3);
      }

   }

   public static void color(float var0, float var1, float var2) {
      color(var0, var1, var2, 1.0F);
   }

   public static void glTexCoord2f(float var0, float var1) {
      GL11.glTexCoord2f(var0, var1);
   }

   public static void glVertex3f(float var0, float var1, float var2) {
      GL11.glVertex3f(var0, var1, var2);
   }

   public static void resetColor() {
      colorState.red = -1.0F;
      colorState.green = -1.0F;
      colorState.blue = -1.0F;
      colorState.alpha = -1.0F;
   }

   public static void glNormalPointer(int var0, int var1, ByteBuffer var2) {
      GL11.glNormalPointer(var0, var1, var2);
   }

   public static void glTexCoordPointer(int var0, int var1, int var2, int var3) {
      GL11.glTexCoordPointer(var0, var1, var2, (long)var3);
   }

   public static void glTexCoordPointer(int var0, int var1, int var2, ByteBuffer var3) {
      GL11.glTexCoordPointer(var0, var1, var2, var3);
   }

   public static void glVertexPointer(int var0, int var1, int var2, int var3) {
      GL11.glVertexPointer(var0, var1, var2, (long)var3);
   }

   public static void glVertexPointer(int var0, int var1, int var2, ByteBuffer var3) {
      GL11.glVertexPointer(var0, var1, var2, var3);
   }

   public static void glColorPointer(int var0, int var1, int var2, int var3) {
      GL11.glColorPointer(var0, var1, var2, (long)var3);
   }

   public static void glColorPointer(int var0, int var1, int var2, ByteBuffer var3) {
      GL11.glColorPointer(var0, var1, var2, var3);
   }

   public static void glDisableClientState(int var0) {
      GL11.glDisableClientState(var0);
   }

   public static void glEnableClientState(int var0) {
      GL11.glEnableClientState(var0);
   }

   public static void glBegin(int var0) {
      GL11.glBegin(var0);
   }

   public static void glEnd() {
      GL11.glEnd();
   }

   public static void glDrawArrays(int var0, int var1, int var2) {
      GL11.glDrawArrays(var0, var1, var2);
   }

   public static void glLineWidth(float var0) {
      GL11.glLineWidth(var0);
   }

   public static void callList(int var0) {
      GL11.glCallList(var0);
   }

   public static void glDeleteLists(int var0, int var1) {
      GL11.glDeleteLists(var0, var1);
   }

   public static void glNewList(int var0, int var1) {
      GL11.glNewList(var0, var1);
   }

   public static void glEndList() {
      GL11.glEndList();
   }

   public static int glGenLists(int var0) {
      return GL11.glGenLists(var0);
   }

   public static void glPixelStorei(int var0, int var1) {
      GL11.glPixelStorei(var0, var1);
   }

   public static void glReadPixels(int var0, int var1, int var2, int var3, int var4, int var5, IntBuffer var6) {
      GL11.glReadPixels(var0, var1, var2, var3, var4, var5, var6);
   }

   public static int glGetError() {
      return GL11.glGetError();
   }

   public static String glGetString(int var0) {
      return GL11.glGetString(var0);
   }

   public static void glGetInteger(int var0, IntBuffer var1) {
      GL11.glGetInteger(var0, var1);
   }

   public static int glGetInteger(int var0) {
      return GL11.glGetInteger(var0);
   }

   public static void enableBlendProfile(GlStateManager.Profile var0) {
      var0.apply();
   }

   public static void disableBlendProfile(GlStateManager.Profile var0) {
      var0.clean();
   }

   static {
      for(int var0 = 0; var0 < 8; ++var0) {
         lightState[var0] = new GlStateManager.BooleanState(16384 + var0);
      }

      colorMaterialState = new GlStateManager.ColorMaterialState();
      blendState = new GlStateManager.BlendState();
      depthState = new GlStateManager.DepthState();
      fogState = new GlStateManager.FogState();
      cullState = new GlStateManager.CullState();
      polygonOffsetState = new GlStateManager.PolygonOffsetState();
      colorLogicState = new GlStateManager.ColorLogicState();
      texGenState = new GlStateManager.TexGenState();
      clearState = new GlStateManager.ClearState();
      stencilState = new GlStateManager.StencilState();
      normalizeState = new GlStateManager.BooleanState(2977);
      textureState = new GlStateManager.TextureState[8];

      for(int var1 = 0; var1 < 8; ++var1) {
         textureState[var1] = new GlStateManager.TextureState();
      }

      activeShadeModel = 7425;
      rescaleNormalState = new GlStateManager.BooleanState(32826);
      colorMaskState = new GlStateManager.ColorMask();
      colorState = new GlStateManager.Color();
   }

   @SideOnly(Side.CLIENT)
   static class AlphaState {
      public GlStateManager.BooleanState alphaTest;
      public int func;
      public float ref;

      private AlphaState() {
         this.alphaTest = new GlStateManager.BooleanState(3008);
         this.func = 519;
         this.ref = -1.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   static class BlendState {
      public GlStateManager.BooleanState blend;
      public int srcFactor;
      public int dstFactor;
      public int srcFactorAlpha;
      public int dstFactorAlpha;

      private BlendState() {
         this.blend = new GlStateManager.BooleanState(3042);
         this.srcFactor = 1;
         this.dstFactor = 0;
         this.srcFactorAlpha = 1;
         this.dstFactorAlpha = 0;
      }
   }

   @SideOnly(Side.CLIENT)
   static class BooleanState {
      private final int capability;
      private boolean currentState;

      public BooleanState(int var1) {
         this.capability = var1;
      }

      public void setDisabled() {
         this.setState(false);
      }

      public void setEnabled() {
         this.setState(true);
      }

      public void setState(boolean var1) {
         if (var1 != this.currentState) {
            this.currentState = var1;
            if (var1) {
               GL11.glEnable(this.capability);
            } else {
               GL11.glDisable(this.capability);
            }
         }

      }
   }

   @SideOnly(Side.CLIENT)
   static class ClearState {
      public double depth;
      public GlStateManager.Color color;

      private ClearState() {
         this.depth = 1.0D;
         this.color = new GlStateManager.Color(0.0F, 0.0F, 0.0F, 0.0F);
      }
   }

   @SideOnly(Side.CLIENT)
   static class Color {
      public float red;
      public float green;
      public float blue;
      public float alpha;

      public Color() {
         this(1.0F, 1.0F, 1.0F, 1.0F);
      }

      public Color(float var1, float var2, float var3, float var4) {
         this.red = 1.0F;
         this.green = 1.0F;
         this.blue = 1.0F;
         this.alpha = 1.0F;
         this.red = var1;
         this.green = var2;
         this.blue = var3;
         this.alpha = var4;
      }
   }

   @SideOnly(Side.CLIENT)
   static class ColorLogicState {
      public GlStateManager.BooleanState colorLogicOp;
      public int opcode;

      private ColorLogicState() {
         this.colorLogicOp = new GlStateManager.BooleanState(3058);
         this.opcode = 5379;
      }
   }

   @SideOnly(Side.CLIENT)
   static class ColorMask {
      public boolean red;
      public boolean green;
      public boolean blue;
      public boolean alpha;

      private ColorMask() {
         this.red = true;
         this.green = true;
         this.blue = true;
         this.alpha = true;
      }
   }

   @SideOnly(Side.CLIENT)
   static class ColorMaterialState {
      public GlStateManager.BooleanState colorMaterial;
      public int face;
      public int mode;

      private ColorMaterialState() {
         this.colorMaterial = new GlStateManager.BooleanState(2903);
         this.face = 1032;
         this.mode = 5634;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum CullFace {
      FRONT(1028),
      BACK(1029),
      FRONT_AND_BACK(1032);

      public final int mode;

      private CullFace(int var3) {
         this.mode = var3;
      }
   }

   @SideOnly(Side.CLIENT)
   static class CullState {
      public GlStateManager.BooleanState cullFace;
      public int mode;

      private CullState() {
         this.cullFace = new GlStateManager.BooleanState(2884);
         this.mode = 1029;
      }
   }

   @SideOnly(Side.CLIENT)
   static class DepthState {
      public GlStateManager.BooleanState depthTest;
      public boolean maskEnabled;
      public int depthFunc;

      private DepthState() {
         this.depthTest = new GlStateManager.BooleanState(2929);
         this.maskEnabled = true;
         this.depthFunc = 513;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum DestFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_COLOR(768),
      ZERO(0);

      public final int factor;

      private DestFactor(int var3) {
         this.factor = var3;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum FogMode {
      LINEAR(9729),
      EXP(2048),
      EXP2(2049);

      public final int capabilityId;

      private FogMode(int var3) {
         this.capabilityId = var3;
      }
   }

   @SideOnly(Side.CLIENT)
   static class FogState {
      public GlStateManager.BooleanState fog;
      public int mode;
      public float density;
      public float start;
      public float end;

      private FogState() {
         this.fog = new GlStateManager.BooleanState(2912);
         this.mode = 2048;
         this.density = 1.0F;
         this.end = 1.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum LogicOp {
      AND(5377),
      AND_INVERTED(5380),
      AND_REVERSE(5378),
      CLEAR(5376),
      COPY(5379),
      COPY_INVERTED(5388),
      EQUIV(5385),
      INVERT(5386),
      NAND(5390),
      NOOP(5381),
      NOR(5384),
      OR(5383),
      OR_INVERTED(5389),
      OR_REVERSE(5387),
      SET(5391),
      XOR(5382);

      public final int opcode;

      private LogicOp(int var3) {
         this.opcode = var3;
      }
   }

   @SideOnly(Side.CLIENT)
   static class PolygonOffsetState {
      public GlStateManager.BooleanState polygonOffsetFill;
      public GlStateManager.BooleanState polygonOffsetLine;
      public float factor;
      public float units;

      private PolygonOffsetState() {
         this.polygonOffsetFill = new GlStateManager.BooleanState(32823);
         this.polygonOffsetLine = new GlStateManager.BooleanState(10754);
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum Profile {
      DEFAULT {
         public void apply() {
            GlStateManager.disableAlpha();
            GlStateManager.alphaFunc(519, 0.0F);
            GlStateManager.disableLighting();
            GL11.glLightModel(2899, RenderHelper.setColorBuffer(0.2F, 0.2F, 0.2F, 1.0F));

            for(int var1 = 0; var1 < 8; ++var1) {
               GlStateManager.disableLight(var1);
               GL11.glLight(16384 + var1, 4608, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
               GL11.glLight(16384 + var1, 4611, RenderHelper.setColorBuffer(0.0F, 0.0F, 1.0F, 0.0F));
               if (var1 == 0) {
                  GL11.glLight(16384 + var1, 4609, RenderHelper.setColorBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                  GL11.glLight(16384 + var1, 4610, RenderHelper.setColorBuffer(1.0F, 1.0F, 1.0F, 1.0F));
               } else {
                  GL11.glLight(16384 + var1, 4609, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                  GL11.glLight(16384 + var1, 4610, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
               }
            }

            GlStateManager.disableColorMaterial();
            GlStateManager.colorMaterial(1032, 5634);
            GlStateManager.disableDepth();
            GlStateManager.depthFunc(513);
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GL14.glBlendEquation(32774);
            GlStateManager.disableFog();
            GL11.glFogi(2917, 2048);
            GlStateManager.setFogDensity(1.0F);
            GlStateManager.setFogStart(0.0F);
            GlStateManager.setFogEnd(1.0F);
            GL11.glFog(2918, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
            if (GLContext.getCapabilities().GL_NV_fog_distance) {
               GL11.glFogi(2917, 34140);
            }

            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disableColorLogic();
            GlStateManager.colorLogicOp(5379);
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
            GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.S, 9474, RenderHelper.setColorBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.S, 9217, RenderHelper.setColorBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
            GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.T, 9474, RenderHelper.setColorBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.T, 9217, RenderHelper.setColorBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
            GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.R, 9474, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.R, 9217, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.Q);
            GlStateManager.texGen(GlStateManager.TexGen.Q, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.Q, 9474, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.Q, 9217, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.setActiveTexture(0);
            GL11.glTexParameteri(3553, 10240, 9729);
            GL11.glTexParameteri(3553, 10241, 9986);
            GL11.glTexParameteri(3553, 10242, 10497);
            GL11.glTexParameteri(3553, 10243, 10497);
            GL11.glTexParameteri(3553, 33085, 1000);
            GL11.glTexParameteri(3553, 33083, 1000);
            GL11.glTexParameteri(3553, 33082, -1000);
            GL11.glTexParameterf(3553, 34049, 0.0F);
            GL11.glTexEnvi(8960, 8704, 8448);
            GL11.glTexEnv(8960, 8705, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexEnvi(8960, 34161, 8448);
            GL11.glTexEnvi(8960, 34162, 8448);
            GL11.glTexEnvi(8960, 34176, 5890);
            GL11.glTexEnvi(8960, 34177, 34168);
            GL11.glTexEnvi(8960, 34178, 34166);
            GL11.glTexEnvi(8960, 34184, 5890);
            GL11.glTexEnvi(8960, 34185, 34168);
            GL11.glTexEnvi(8960, 34186, 34166);
            GL11.glTexEnvi(8960, 34192, 768);
            GL11.glTexEnvi(8960, 34193, 768);
            GL11.glTexEnvi(8960, 34194, 770);
            GL11.glTexEnvi(8960, 34200, 770);
            GL11.glTexEnvi(8960, 34201, 770);
            GL11.glTexEnvi(8960, 34202, 770);
            GL11.glTexEnvf(8960, 34163, 1.0F);
            GL11.glTexEnvf(8960, 3356, 1.0F);
            GlStateManager.disableNormalize();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableRescaleNormal();
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.clearDepth(1.0D);
            GL11.glLineWidth(1.0F);
            GL11.glNormal3f(0.0F, 0.0F, 1.0F);
            GL11.glPolygonMode(1028, 6914);
            GL11.glPolygonMode(1029, 6914);
         }

         public void clean() {
         }
      },
      PLAYER_SKIN {
         public void apply() {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         }

         public void clean() {
            GlStateManager.disableBlend();
         }
      },
      TRANSPARENT_MODEL {
         public void apply() {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(516, 0.003921569F);
         }

         public void clean() {
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.depthMask(true);
         }
      };

      private Profile() {
      }

      public abstract void apply();

      public abstract void clean();
   }

   @SideOnly(Side.CLIENT)
   public static enum SourceFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_ALPHA_SATURATE(776),
      SRC_COLOR(768),
      ZERO(0);

      public final int factor;

      private SourceFactor(int var3) {
         this.factor = var3;
      }
   }

   @SideOnly(Side.CLIENT)
   static class StencilFunc {
      public int func;
      public int mask;

      private StencilFunc() {
         this.func = 519;
         this.mask = -1;
      }
   }

   @SideOnly(Side.CLIENT)
   static class StencilState {
      public GlStateManager.StencilFunc func;
      public int mask;
      public int fail;
      public int zfail;
      public int zpass;

      private StencilState() {
         this.func = new GlStateManager.StencilFunc();
         this.mask = -1;
         this.fail = 7680;
         this.zfail = 7680;
         this.zpass = 7680;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum TexGen {
      S,
      T,
      R,
      Q;
   }

   @SideOnly(Side.CLIENT)
   static class TexGenCoord {
      public GlStateManager.BooleanState textureGen;
      public int coord;
      public int param = -1;

      public TexGenCoord(int var1, int var2) {
         this.coord = var1;
         this.textureGen = new GlStateManager.BooleanState(var2);
      }
   }

   @SideOnly(Side.CLIENT)
   static class TexGenState {
      public GlStateManager.TexGenCoord s;
      public GlStateManager.TexGenCoord t;
      public GlStateManager.TexGenCoord r;
      public GlStateManager.TexGenCoord q;

      private TexGenState() {
         this.s = new GlStateManager.TexGenCoord(8192, 3168);
         this.t = new GlStateManager.TexGenCoord(8193, 3169);
         this.r = new GlStateManager.TexGenCoord(8194, 3170);
         this.q = new GlStateManager.TexGenCoord(8195, 3171);
      }
   }

   @SideOnly(Side.CLIENT)
   static class TextureState {
      public GlStateManager.BooleanState texture2DState;
      public int textureName;

      private TextureState() {
         this.texture2DState = new GlStateManager.BooleanState(3553);
      }
   }
}
