package net.minecraft.client.resources;

import com.google.gson.JsonParseException;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ResourcePackListEntryServer extends ResourcePackListEntry {
   private static final Logger LOGGER = LogManager.getLogger();
   private final IResourcePack resourcePack;
   private final ResourceLocation resourcePackIcon;

   public ResourcePackListEntryServer(GuiScreenResourcePacks var1, IResourcePack var2) {
      super(p_i46594_1_);
      this.resourcePack = p_i46594_2_;

      DynamicTexture dynamictexture;
      try {
         dynamictexture = new DynamicTexture(p_i46594_2_.getPackImage());
      } catch (IOException var5) {
         dynamictexture = TextureUtil.MISSING_TEXTURE;
      }

      this.resourcePackIcon = this.mc.getTextureManager().getDynamicTextureLocation("texturepackicon", dynamictexture);
   }

   protected int getResourcePackFormat() {
      return 2;
   }

   protected String getResourcePackDescription() {
      try {
         PackMetadataSection packmetadatasection = (PackMetadataSection)this.resourcePack.getPackMetadata(this.mc.getResourcePackRepository().rprMetadataSerializer, "pack");
         if (packmetadatasection != null) {
            return packmetadatasection.getPackDescription().getFormattedText();
         }
      } catch (JsonParseException var2) {
         LOGGER.error("Couldn't load metadata info", var2);
      } catch (IOException var3) {
         LOGGER.error("Couldn't load metadata info", var3);
      }

      return TextFormatting.RED + "Missing pack.mcmeta :(";
   }

   protected boolean canMoveRight() {
      return false;
   }

   protected boolean canMoveLeft() {
      return false;
   }

   protected boolean canMoveUp() {
      return false;
   }

   protected boolean canMoveDown() {
      return false;
   }

   protected String getResourcePackName() {
      return "Server";
   }

   protected void bindResourcePackIcon() {
      this.mc.getTextureManager().bindTexture(this.resourcePackIcon);
   }

   protected boolean showHoverOverlay() {
      return false;
   }

   public boolean isServerPack() {
      return true;
   }
}
