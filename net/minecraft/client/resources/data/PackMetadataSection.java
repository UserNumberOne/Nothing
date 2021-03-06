package net.minecraft.client.resources.data;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PackMetadataSection implements IMetadataSection {
   private final ITextComponent packDescription;
   private final int packFormat;

   public PackMetadataSection(ITextComponent var1, int var2) {
      this.packDescription = var1;
      this.packFormat = var2;
   }

   public ITextComponent getPackDescription() {
      return this.packDescription;
   }

   public int getPackFormat() {
      return this.packFormat;
   }
}
