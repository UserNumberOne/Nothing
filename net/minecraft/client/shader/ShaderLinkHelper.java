package net.minecraft.client.shader;

import java.io.IOException;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.util.JsonException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ShaderLinkHelper {
   private static final Logger LOGGER = LogManager.getLogger();
   private static ShaderLinkHelper staticShaderLinkHelper;

   public static void setNewStaticShaderLinkHelper() {
      staticShaderLinkHelper = new ShaderLinkHelper();
   }

   public static ShaderLinkHelper getStaticShaderLinkHelper() {
      return staticShaderLinkHelper;
   }

   public void deleteShader(ShaderManager var1) {
      var1.getFragmentShaderLoader().deleteShader(var1);
      var1.getVertexShaderLoader().deleteShader(var1);
      OpenGlHelper.glDeleteProgram(var1.getProgram());
   }

   public int createProgram() throws JsonException {
      int var1 = OpenGlHelper.glCreateProgram();
      if (var1 <= 0) {
         throw new JsonException("Could not create shader program (returned program ID " + var1 + ")");
      } else {
         return var1;
      }
   }

   public void linkProgram(ShaderManager var1) throws IOException {
      var1.getFragmentShaderLoader().attachShader(var1);
      var1.getVertexShaderLoader().attachShader(var1);
      OpenGlHelper.glLinkProgram(var1.getProgram());
      int var2 = OpenGlHelper.glGetProgrami(var1.getProgram(), OpenGlHelper.GL_LINK_STATUS);
      if (var2 == 0) {
         LOGGER.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", new Object[]{var1.getVertexShaderLoader().getShaderFilename(), var1.getFragmentShaderLoader().getShaderFilename()});
         LOGGER.warn(OpenGlHelper.glGetProgramInfoLog(var1.getProgram(), 32768));
      }

   }
}
