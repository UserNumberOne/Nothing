package net.minecraft.client.resources;

import com.google.common.collect.Sets;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

@SideOnly(Side.CLIENT)
public class FolderResourcePack extends AbstractResourcePack {
   public FolderResourcePack(File var1) {
      super(resourcePackFileIn);
   }

   protected InputStream getInputStreamByName(String var1) throws IOException {
      return new BufferedInputStream(new FileInputStream(new File(this.resourcePackFile, name)));
   }

   protected boolean hasResourceName(String var1) {
      return (new File(this.resourcePackFile, name)).isFile();
   }

   public Set getResourceDomains() {
      Set set = Sets.newHashSet();
      File file1 = new File(this.resourcePackFile, "assets/");
      if (file1.isDirectory()) {
         for(File file2 : file1.listFiles(DirectoryFileFilter.DIRECTORY)) {
            String s = getRelativeName(file1, file2);
            if (s.equals(s.toLowerCase())) {
               set.add(s.substring(0, s.length() - 1));
            } else {
               this.logNameNotLowercase(s);
            }
         }
      }

      return set;
   }
}
