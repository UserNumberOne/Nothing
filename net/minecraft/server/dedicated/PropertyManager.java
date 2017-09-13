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
      this.serverPropertiesFile = propertiesFile;
      if (propertiesFile.exists()) {
         FileInputStream fileinputstream = null;

         try {
            fileinputstream = new FileInputStream(propertiesFile);
            this.serverProperties.load(fileinputstream);
         } catch (Exception var12) {
            LOGGER.warn("Failed to load {}", new Object[]{propertiesFile, var12});
            this.generateNewProperties();
         } finally {
            if (fileinputstream != null) {
               try {
                  fileinputstream.close();
               } catch (IOException var11) {
                  ;
               }
            }

         }
      } else {
         LOGGER.warn("{} does not exist", new Object[]{propertiesFile});
         this.generateNewProperties();
      }

   }

   public void generateNewProperties() {
      LOGGER.info("Generating new properties file");
      this.saveProperties();
   }

   public void saveProperties() {
      FileOutputStream fileoutputstream = null;

      try {
         fileoutputstream = new FileOutputStream(this.serverPropertiesFile);
         this.serverProperties.store(fileoutputstream, "Minecraft server properties");
      } catch (Exception var11) {
         LOGGER.warn("Failed to save {}", new Object[]{this.serverPropertiesFile, var11});
         this.generateNewProperties();
      } finally {
         if (fileoutputstream != null) {
            try {
               fileoutputstream.close();
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
      if (!this.serverProperties.containsKey(key)) {
         this.serverProperties.setProperty(key, defaultValue);
         this.saveProperties();
         this.saveProperties();
      }

      return this.serverProperties.getProperty(key, defaultValue);
   }

   public int getIntProperty(String var1, int var2) {
      try {
         return Integer.parseInt(this.getStringProperty(key, "" + defaultValue));
      } catch (Exception var4) {
         this.serverProperties.setProperty(key, "" + defaultValue);
         this.saveProperties();
         return defaultValue;
      }
   }

   public long getLongProperty(String var1, long var2) {
      try {
         return Long.parseLong(this.getStringProperty(key, "" + defaultValue));
      } catch (Exception var5) {
         this.serverProperties.setProperty(key, "" + defaultValue);
         this.saveProperties();
         return defaultValue;
      }
   }

   public boolean getBooleanProperty(String var1, boolean var2) {
      try {
         return Boolean.parseBoolean(this.getStringProperty(key, "" + defaultValue));
      } catch (Exception var4) {
         this.serverProperties.setProperty(key, "" + defaultValue);
         this.saveProperties();
         return defaultValue;
      }
   }

   public void setProperty(String var1, Object var2) {
      this.serverProperties.setProperty(key, "" + value);
   }

   public boolean hasProperty(String var1) {
      return this.serverProperties.containsKey(key);
   }

   public void removeProperty(String var1) {
      this.serverProperties.remove(key);
   }
}
