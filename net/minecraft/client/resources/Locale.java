package net.minecraft.client.resources;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

@SideOnly(Side.CLIENT)
public class Locale {
   private static final Splitter SPLITTER = Splitter.on('=').limit(2);
   private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
   Map properties = Maps.newHashMap();
   private boolean unicode;

   public synchronized void loadLocaleDataFiles(IResourceManager var1, List var2) {
      this.properties.clear();

      for(String s : languageList) {
         String s1 = String.format("lang/%s.lang", s);

         for(String s2 : resourceManager.getResourceDomains()) {
            try {
               this.loadLocaleData(resourceManager.getAllResources(new ResourceLocation(s2, s1)));
            } catch (IOException var9) {
               ;
            }
         }
      }

      this.checkUnicode();
   }

   public boolean isUnicode() {
      return this.unicode;
   }

   private void checkUnicode() {
      this.unicode = false;
      int i = 0;
      int j = 0;

      for(String s : this.properties.values()) {
         int k = s.length();
         j += k;

         for(int l = 0; l < k; ++l) {
            if (s.charAt(l) >= 256) {
               ++i;
            }
         }
      }

      float f = (float)i / (float)j;
      this.unicode = (double)f > 0.1D;
   }

   private void loadLocaleData(List var1) throws IOException {
      for(IResource iresource : resourcesList) {
         InputStream inputstream = iresource.getInputStream();

         try {
            this.loadLocaleData(inputstream);
         } finally {
            IOUtils.closeQuietly(inputstream);
         }
      }

   }

   private void loadLocaleData(InputStream var1) throws IOException {
      inputStreamIn = FMLCommonHandler.instance().loadLanguage(this.properties, inputStreamIn);
      if (inputStreamIn != null) {
         for(String s : IOUtils.readLines(inputStreamIn, Charsets.UTF_8)) {
            if (!s.isEmpty() && s.charAt(0) != '#') {
               String[] astring = (String[])Iterables.toArray(SPLITTER.split(s), String.class);
               if (astring != null && astring.length == 2) {
                  String s1 = astring[0];
                  String s2 = PATTERN.matcher(astring[1]).replaceAll("%$1s");
                  this.properties.put(s1, s2);
               }
            }
         }

      }
   }

   private String translateKeyPrivate(String var1) {
      String s = (String)this.properties.get(translateKey);
      return s == null ? translateKey : s;
   }

   public String formatMessage(String var1, Object[] var2) {
      String s = this.translateKeyPrivate(translateKey);

      try {
         return String.format(s, parameters);
      } catch (IllegalFormatException var5) {
         return "Format error: " + s;
      }
   }

   public boolean hasKey(String var1) {
      return this.properties.containsKey(key);
   }
}
