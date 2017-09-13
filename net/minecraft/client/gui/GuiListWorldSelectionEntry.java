package net.minecraft.client.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiListWorldSelectionEntry implements GuiListExtended.IGuiListEntry {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
   private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
   private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
   private final Minecraft client;
   private final GuiWorldSelection worldSelScreen;
   private final WorldSummary worldSummary;
   private final ResourceLocation iconLocation;
   private final GuiListWorldSelection containingListSel;
   private File iconFile;
   private DynamicTexture icon;
   private long lastClickTime;

   public GuiListWorldSelectionEntry(GuiListWorldSelection var1, WorldSummary var2, ISaveFormat var3) {
      this.containingListSel = var1;
      this.worldSelScreen = var1.getGuiWorldSelection();
      this.worldSummary = var2;
      this.client = Minecraft.getMinecraft();
      this.iconLocation = new ResourceLocation("worlds/" + var2.getFileName() + "/icon");
      this.iconFile = var3.getFile(var2.getFileName(), "icon.png");
      if (!this.iconFile.isFile()) {
         this.iconFile = null;
      }

      this.loadServerIcon();
   }

   public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      String var9 = this.worldSummary.getDisplayName();
      String var10 = this.worldSummary.getFileName() + " (" + DATE_FORMAT.format(new Date(this.worldSummary.getLastTimePlayed())) + ")";
      String var11 = "";
      if (StringUtils.isEmpty(var9)) {
         var9 = I18n.format("selectWorld.world") + " " + (var1 + 1);
      }

      if (this.worldSummary.requiresConversion()) {
         var11 = I18n.format("selectWorld.conversion") + " " + var11;
      } else {
         var11 = I18n.format("gameMode." + this.worldSummary.getEnumGameType().getName());
         if (this.worldSummary.isHardcoreModeEnabled()) {
            var11 = TextFormatting.DARK_RED + I18n.format("gameMode.hardcore") + TextFormatting.RESET;
         }

         if (this.worldSummary.getCheatsEnabled()) {
            var11 = var11 + ", " + I18n.format("selectWorld.cheats");
         }

         String var12 = this.worldSummary.getVersionName();
         if (this.worldSummary.markVersionInList()) {
            if (this.worldSummary.askToOpenWorld()) {
               var11 = var11 + ", " + I18n.format("selectWorld.version") + " " + TextFormatting.RED + var12 + TextFormatting.RESET;
            } else {
               var11 = var11 + ", " + I18n.format("selectWorld.version") + " " + TextFormatting.ITALIC + var12 + TextFormatting.RESET;
            }
         } else {
            var11 = var11 + ", " + I18n.format("selectWorld.version") + " " + var12;
         }
      }

      this.client.fontRendererObj.drawString(var9, var2 + 32 + 3, var3 + 1, 16777215);
      this.client.fontRendererObj.drawString(var10, var2 + 32 + 3, var3 + this.client.fontRendererObj.FONT_HEIGHT + 3, 8421504);
      this.client.fontRendererObj.drawString(var11, var2 + 32 + 3, var3 + this.client.fontRendererObj.FONT_HEIGHT + this.client.fontRendererObj.FONT_HEIGHT + 3, 8421504);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(this.icon != null ? this.iconLocation : ICON_MISSING);
      GlStateManager.enableBlend();
      Gui.drawModalRectWithCustomSizedTexture(var2, var3, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
      GlStateManager.disableBlend();
      if (this.client.gameSettings.touchscreen || var8) {
         this.client.getTextureManager().bindTexture(ICON_OVERLAY_LOCATION);
         Gui.drawRect(var2, var3, var2 + 32, var3 + 32, -1601138544);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         int var16 = var6 - var2;
         int var13 = var16 < 32 ? 32 : 0;
         if (this.worldSummary.markVersionInList()) {
            Gui.drawModalRectWithCustomSizedTexture(var2, var3, 32.0F, (float)var13, 32, 32, 256.0F, 256.0F);
            if (this.worldSummary.askToOpenWorld()) {
               Gui.drawModalRectWithCustomSizedTexture(var2, var3, 96.0F, (float)var13, 32, 32, 256.0F, 256.0F);
               if (var16 < 32) {
                  this.worldSelScreen.setVersionTooltip(TextFormatting.RED + I18n.format("selectWorld.tooltip.fromNewerVersion1") + "\n" + TextFormatting.RED + I18n.format("selectWorld.tooltip.fromNewerVersion2"));
               }
            } else {
               Gui.drawModalRectWithCustomSizedTexture(var2, var3, 64.0F, (float)var13, 32, 32, 256.0F, 256.0F);
               if (var16 < 32) {
                  this.worldSelScreen.setVersionTooltip(TextFormatting.GOLD + I18n.format("selectWorld.tooltip.snapshot1") + "\n" + TextFormatting.GOLD + I18n.format("selectWorld.tooltip.snapshot2"));
               }
            }
         } else {
            Gui.drawModalRectWithCustomSizedTexture(var2, var3, 0.0F, (float)var13, 32, 32, 256.0F, 256.0F);
         }
      }

   }

   public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.containingListSel.selectWorld(var1);
      if (var5 <= 32 && var5 < 32) {
         this.joinWorld();
         return true;
      } else if (Minecraft.getSystemTime() - this.lastClickTime < 250L) {
         this.joinWorld();
         return true;
      } else {
         this.lastClickTime = Minecraft.getSystemTime();
         return false;
      }
   }

   public void joinWorld() {
      if (this.worldSummary.askToOpenWorld()) {
         this.client.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback() {
            public void confirmClicked(boolean var1, int var2) {
               if (var1) {
                  GuiListWorldSelectionEntry.this.loadWorld();
               } else {
                  GuiListWorldSelectionEntry.this.client.displayGuiScreen(GuiListWorldSelectionEntry.this.worldSelScreen);
               }

            }
         }, I18n.format("selectWorld.versionQuestion"), I18n.format("selectWorld.versionWarning", this.worldSummary.getVersionName()), I18n.format("selectWorld.versionJoinButton"), I18n.format("gui.cancel"), 0));
      } else {
         this.loadWorld();
      }

   }

   public void deleteWorld() {
      this.client.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback() {
         public void confirmClicked(boolean var1, int var2) {
            if (var1) {
               GuiListWorldSelectionEntry.this.client.displayGuiScreen(new GuiScreenWorking());
               ISaveFormat var3 = GuiListWorldSelectionEntry.this.client.getSaveLoader();
               var3.flushCache();
               var3.deleteWorldDirectory(GuiListWorldSelectionEntry.this.worldSummary.getFileName());
               GuiListWorldSelectionEntry.this.containingListSel.refreshList();
            }

            GuiListWorldSelectionEntry.this.client.displayGuiScreen(GuiListWorldSelectionEntry.this.worldSelScreen);
         }
      }, I18n.format("selectWorld.deleteQuestion"), "'" + this.worldSummary.getDisplayName() + "' " + I18n.format("selectWorld.deleteWarning"), I18n.format("selectWorld.deleteButton"), I18n.format("gui.cancel"), 0));
   }

   public void editWorld() {
      this.client.displayGuiScreen(new GuiWorldEdit(this.worldSelScreen, this.worldSummary.getFileName()));
   }

   public void recreateWorld() {
      this.client.displayGuiScreen(new GuiScreenWorking());
      GuiCreateWorld var1 = new GuiCreateWorld(this.worldSelScreen);
      ISaveHandler var2 = this.client.getSaveLoader().getSaveLoader(this.worldSummary.getFileName(), false);
      WorldInfo var3 = var2.loadWorldInfo();
      var2.flush();
      var1.recreateFromExistingWorld(var3);
      this.client.displayGuiScreen(var1);
   }

   private void loadWorld() {
      this.client.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      if (this.client.getSaveLoader().canLoadWorld(this.worldSummary.getFileName())) {
         FMLClientHandler.instance().tryLoadExistingWorld(this.worldSelScreen, this.worldSummary);
      }

   }

   private void loadServerIcon() {
      boolean var1 = this.iconFile != null && this.iconFile.isFile();
      if (var1) {
         BufferedImage var2;
         try {
            var2 = ImageIO.read(this.iconFile);
            Validate.validState(var2.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
            Validate.validState(var2.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
         } catch (Throwable var4) {
            LOGGER.error("Invalid icon for world {}", new Object[]{this.worldSummary.getFileName(), var4});
            this.iconFile = null;
            return;
         }

         if (this.icon == null) {
            this.icon = new DynamicTexture(var2.getWidth(), var2.getHeight());
            this.client.getTextureManager().loadTexture(this.iconLocation, this.icon);
         }

         var2.getRGB(0, 0, var2.getWidth(), var2.getHeight(), this.icon.getTextureData(), 0, var2.getWidth());
         this.icon.updateDynamicTexture();
      } else if (!var1) {
         this.client.getTextureManager().deleteTexture(this.iconLocation);
         this.icon = null;
      }

   }

   public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   public void setSelected(int var1, int var2, int var3) {
   }
}
