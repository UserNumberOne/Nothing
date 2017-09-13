package net.minecraft.server.dedicated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.SERVER)
public class PropertyManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Properties serverProperties = new Properties();
   private final File serverPropertiesFile;

   public PropertyManager(File var1) {
      this.serverPropertiesFile = var1;
      if (var1.exists()) {
         FileInputStream var2 = null;

         try {
            var2 = new FileInputStream(var1);
            this.serverProperties.load(var2);
         } catch (Exception var12) {
            LOGGER.warn("Failed to load {}", new Object[]{var1, var12});
            this.generateNewProperties();
         } finally {
            if (var2 != null) {
               try {
                  var2.close();
               } catch (IOException var11) {
                  ;
               }
            }

         }
      } else {
         LOGGER.warn("{} does not exist", new Object[]{var1});
         this.generateNewProperties();
      }

   }

   public void generateNewProperties() {
      LOGGER.info("Generating new properties file");
      this.saveProperties();
   }

   public void saveProperties() {
      FileOutputStream var1 = null;

      try {
         var1 = new FileOutputStream(this.serverPropertiesFile);
         this.serverProperties.store(var1, "Minecraft server properties");
      } catch (Exception var11) {
         LOGGER.warn("Failed to save {}", new Object[]{this.serverPropertiesFile, var11});
         this.generateNewProperties();
      } finally {
         if (var1 != null) {
            try {
               var1.close();
            } catch (IOException var10) {
               ;
            }
         }

      }

   }

   public File getPropertiesFile() {
      return this.serverPropertiesFile;
   }

   public String getStringProperty(String var1, String var2) {
      if (!this.serverProperties.containsKey(var1)) {
         this.serverProperties.setProperty(var1, var2);
         this.saveProperties();
         this.saveProperties();
      }

      return this.serverProperties.getProperty(var1, var2);
   }

   public int getIntProperty(String var1, int var2) {
      try {
         return Integer.parseInt(this.getStringProperty(var1, "" + var2));
      } catch (Exception var4) {
         this.serverProperties.setProperty(var1, "" + var2);
         this.saveProperties();
         return var2;
      }
   }

   public long getLongProperty(String var1, long var2) {
      try {
         return Long.parseLong(this.getStringProperty(var1, "" + var2));
      } catch (Exception var5) {
         this.serverProperties.setProperty(var1, "" + var2);
         this.saveProperties();
         return var2;
      }
   }

   public boolean getBooleanProperty(String var1, boolean var2) {
      try {
         return Boolean.parseBoolean(this.getStringProperty(var1, "" + var2));
      } catch (Exception var4) {
         this.serverProperties.setProperty(var1, "" + var2);
         this.saveProperties();
         return var2;
      }
   }

   public void setProperty(String var1, Object var2) {
      this.serverProperties.setProperty(var1, "" + var2);
   }

   public boolean hasProperty(String var1) {
      return this.serverProperties.containsKey(var1);
   }

   public void removeProperty(String var1) {
      this.serverProperties.remove(var1);
   }
}
