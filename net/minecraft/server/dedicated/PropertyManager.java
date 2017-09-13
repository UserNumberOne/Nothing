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

   public PropertyManager(File file) {
      this.serverProperties = new Properties();
      this.options = null;
      this.serverPropertiesFile = file;
      if (file.exists()) {
         FileInputStream fileinputstream = null;

         try {
            fileinputstream = new FileInputStream(file);
            this.serverProperties.load(fileinputstream);
         } catch (Exception var11) {
            LOGGER.warn("Failed to load {}", new Object[]{file, var11});
            this.generateNewProperties();
         } finally {
            if (fileinputstream != null) {
               try {
                  fileinputstream.close();
               } catch (IOException var10) {
                  ;
               }
            }

         }
      } else {
         LOGGER.warn("{} does not exist", new Object[]{file});
         this.generateNewProperties();
      }

   }

   public PropertyManager(OptionSet options) {
      this((File)options.valueOf("config"));
      this.options = options;
   }

   private Object getOverride(String name, Object value) {
      return this.options != null && this.options.has(name) ? this.options.valueOf(name) : value;
   }

   public void generateNewProperties() {
      LOGGER.info("Generating new properties file");
      this.saveProperties();
   }

   public void saveProperties() {
      FileOutputStream fileoutputstream = null;

      try {
         if (!this.serverPropertiesFile.exists() || this.serverPropertiesFile.canWrite()) {
            fileoutputstream = new FileOutputStream(this.serverPropertiesFile);
            this.serverProperties.store(fileoutputstream, "Minecraft server properties");
            return;
         }
      } catch (Exception var11) {
         LOGGER.warn("Failed to save {}", new Object[]{this.serverPropertiesFile, var11});
         this.generateNewProperties();
         return;
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

   public String getStringProperty(String s, String s1) {
      if (!this.serverProperties.containsKey(s)) {
         this.serverProperties.setProperty(s, s1);
         this.saveProperties();
         this.saveProperties();
      }

      return (String)this.getOverride(s, this.serverProperties.getProperty(s, s1));
   }

   public int getIntProperty(String s, int i) {
      try {
         return ((Integer)this.getOverride(s, Integer.valueOf(Integer.parseInt(this.getStringProperty(s, "" + i))))).intValue();
      } catch (Exception var3) {
         this.serverProperties.setProperty(s, "" + i);
         this.saveProperties();
         return ((Integer)this.getOverride(s, Integer.valueOf(i))).intValue();
      }
   }

   public long getLongProperty(String s, long i) {
      try {
         return ((Long)this.getOverride(s, Long.valueOf(Long.parseLong(this.getStringProperty(s, "" + i))))).longValue();
      } catch (Exception var4) {
         this.serverProperties.setProperty(s, "" + i);
         this.saveProperties();
         return ((Long)this.getOverride(s, Long.valueOf(i))).longValue();
      }
   }

   public boolean getBooleanProperty(String s, boolean flag) {
      try {
         return ((Boolean)this.getOverride(s, Boolean.valueOf(Boolean.parseBoolean(this.getStringProperty(s, "" + flag))))).booleanValue();
      } catch (Exception var3) {
         this.serverProperties.setProperty(s, "" + flag);
         this.saveProperties();
         return ((Boolean)this.getOverride(s, Boolean.valueOf(flag))).booleanValue();
      }
   }

   public void setProperty(String s, Object object) {
      this.serverProperties.setProperty(s, "" + object);
   }

   public boolean hasProperty(String s) {
      return this.serverProperties.containsKey(s);
   }

   public void removeProperty(String s) {
      this.serverProperties.remove(s);
   }
}
