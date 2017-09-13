package net.minecraft.server.dedicated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;

public class PropertyManager {
   private static final Logger LOGGER = LogManager.getLogger();
   public final Properties serverProperties;
   private final File serverPropertiesFile;
   private OptionSet options;

   public PropertyManager(File var1) {
      this.serverProperties = new Properties();
      this.options = null;
      this.serverPropertiesFile = var1;
      if (var1.exists()) {
         FileInputStream var2 = null;

         try {
            var2 = new FileInputStream(var1);
            this.serverProperties.load(var2);
         } catch (Exception var11) {
            LOGGER.warn("Failed to load {}", new Object[]{var1, var11});
            this.generateNewProperties();
         } finally {
            if (var2 != null) {
               try {
                  var2.close();
               } catch (IOException var10) {
                  ;
               }
            }

         }
      } else {
         LOGGER.warn("{} does not exist", new Object[]{var1});
         this.generateNewProperties();
      }

   }

   public PropertyManager(OptionSet var1) {
      this((File)var1.valueOf("config"));
      this.options = var1;
   }

   private Object getOverride(String var1, Object var2) {
      return this.options != null && this.options.has(var1) ? this.options.valueOf(var1) : var2;
   }

   public void generateNewProperties() {
      LOGGER.info("Generating new properties file");
      this.saveProperties();
   }

   public void saveProperties() {
      FileOutputStream var1 = null;

      try {
         if (!this.serverPropertiesFile.exists() || this.serverPropertiesFile.canWrite()) {
            var1 = new FileOutputStream(this.serverPropertiesFile);
            this.serverProperties.store(var1, "Minecraft server properties");
            return;
         }
      } catch (Exception var11) {
         LOGGER.warn("Failed to save {}", new Object[]{this.serverPropertiesFile, var11});
         this.generateNewProperties();
         return;
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

      return (String)this.getOverride(var1, this.serverProperties.getProperty(var1, var2));
   }

   public int getIntProperty(String var1, int var2) {
      try {
         return ((Integer)this.getOverride(var1, Integer.valueOf(Integer.parseInt(this.getStringProperty(var1, "" + var2))))).intValue();
      } catch (Exception var3) {
         this.serverProperties.setProperty(var1, "" + var2);
         this.saveProperties();
         return ((Integer)this.getOverride(var1, Integer.valueOf(var2))).intValue();
      }
   }

   public long getLongProperty(String var1, long var2) {
      try {
         return ((Long)this.getOverride(var1, Long.valueOf(Long.parseLong(this.getStringProperty(var1, "" + var2))))).longValue();
      } catch (Exception var4) {
         this.serverProperties.setProperty(var1, "" + var2);
         this.saveProperties();
         return ((Long)this.getOverride(var1, Long.valueOf(var2))).longValue();
      }
   }

   public boolean getBooleanProperty(String var1, boolean var2) {
      try {
         return ((Boolean)this.getOverride(var1, Boolean.valueOf(Boolean.parseBoolean(this.getStringProperty(var1, "" + var2))))).booleanValue();
      } catch (Exception var3) {
         this.serverProperties.setProperty(var1, "" + var2);
         this.saveProperties();
         return ((Boolean)this.getOverride(var1, Boolean.valueOf(var2))).booleanValue();
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
