package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class LanguageManager implements IResourceManagerReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MetadataSerializer theMetadataSerializer;
   private String currentLanguage;
   protected static final Locale CURRENT_LOCALE = new Locale();
   private final Map languageMap = Maps.newHashMap();

   public LanguageManager(MetadataSerializer var1, String var2) {
      this.theMetadataSerializer = var1;
      this.currentLanguage = var2;
      I18n.setLocale(CURRENT_LOCALE);
   }

   public void parseLanguageMetadata(List var1) {
      this.languageMap.clear();

      for(IResourcePack var3 : var1) {
         try {
            LanguageMetadataSection var4 = (LanguageMetadataSection)var3.getPackMetadata(this.theMetadataSerializer, "language");
            if (var4 != null) {
               for(Language var6 : var4.getLanguages()) {
                  if (!this.languageMap.containsKey(var6.getLanguageCode())) {
                     this.languageMap.put(var6.getLanguageCode(), var6);
                  }
               }
            }
         } catch (RuntimeException var7) {
            LOGGER.warn("Unable to parse metadata section of resourcepack: {}", new Object[]{var3.getPackName(), var7});
         } catch (IOException var8) {
            LOGGER.warn("Unable to parse metadata section of resourcepack: {}", new Object[]{var3.getPackName(), var8});
         }
      }

   }

   public void onResourceManagerReload(IResourceManager var1) {
      ArrayList var2 = Lists.newArrayList(new String[]{"en_US"});
      if (!"en_US".equals(this.currentLanguage)) {
         var2.add(this.currentLanguage);
      }

      CURRENT_LOCALE.loadLocaleDataFiles(var1, var2);
      LanguageMap.replaceWith(CURRENT_LOCALE.properties);
   }

   public boolean isCurrentLocaleUnicode() {
      return CURRENT_LOCALE.isUnicode();
   }

   public boolean isCurrentLanguageBidirectional() {
      return this.getCurrentLanguage() != null && this.getCurrentLanguage().isBidirectional();
   }

   public void setCurrentLanguage(Language var1) {
      this.currentLanguage = var1.getLanguageCode();
   }

   public Language getCurrentLanguage() {
      String var1 = this.languageMap.containsKey(this.currentLanguage) ? this.currentLanguage : "en_US";
      return (Language)this.languageMap.get(var1);
   }

   public SortedSet getLanguages() {
      return Sets.newTreeSet(this.languageMap.values());
   }
}
