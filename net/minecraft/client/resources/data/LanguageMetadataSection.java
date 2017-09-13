package net.minecraft.client.resources.data;

import java.util.Collection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LanguageMetadataSection implements IMetadataSection {
   private final Collection languages;

   public LanguageMetadataSection(Collection var1) {
      this.languages = var1;
   }

   public Collection getLanguages() {
      return this.languages;
   }
}
