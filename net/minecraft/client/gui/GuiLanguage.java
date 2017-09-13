package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLanguage extends GuiScreen {
   protected GuiScreen parentScreen;
   private GuiLanguage.List list;
   private final GameSettings game_settings_3;
   private final LanguageManager languageManager;
   private GuiOptionButton forceUnicodeFontBtn;
   private GuiOptionButton confirmSettingsBtn;

   public GuiLanguage(GuiScreen var1, GameSettings var2, LanguageManager var3) {
      this.parentScreen = screen;
      this.game_settings_3 = gameSettingsObj;
      this.languageManager = manager;
   }

   public void initGui() {
      this.forceUnicodeFontBtn = (GuiOptionButton)this.addButton(new GuiOptionButton(100, this.width / 2 - 155, this.height - 38, GameSettings.Options.FORCE_UNICODE_FONT, this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
      this.confirmSettingsBtn = (GuiOptionButton)this.addButton(new GuiOptionButton(6, this.width / 2 - 155 + 160, this.height - 38, I18n.format("gui.done")));
      this.list = new GuiLanguage.List(this.mc);
      this.list.registerScrollButtons(7, 8);
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.list.handleMouseInput();
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (button.enabled) {
         switch(button.id) {
         case 5:
            break;
         case 6:
            this.mc.displayGuiScreen(this.parentScreen);
            break;
         case 100:
            if (button instanceof GuiOptionButton) {
               this.game_settings_3.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
               button.displayString = this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
               ScaledResolution scaledresolution = new ScaledResolution(this.mc);
               int i = scaledresolution.getScaledWidth();
               int j = scaledresolution.getScaledHeight();
               this.setWorldAndResolution(this.mc, i, j);
            }
            break;
         default:
            this.list.actionPerformed(button);
         }
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.list.drawScreen(mouseX, mouseY, partialTicks);
      this.drawCenteredString(this.fontRendererObj, I18n.format("options.language"), this.width / 2, 16, 16777215);
      this.drawCenteredString(this.fontRendererObj, "(" + I18n.format("options.languageWarning") + ")", this.width / 2, this.height - 56, 8421504);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   @SideOnly(Side.CLIENT)
   class List extends GuiSlot {
      private final java.util.List langCodeList = Lists.newArrayList();
      private final Map languageMap = Maps.newHashMap();

      public List(Minecraft var2) {
         super(mcIn, GuiLanguage.this.width, GuiLanguage.this.height, 32, GuiLanguage.this.height - 65 + 4, 18);

         for(Language language : GuiLanguage.this.languageManager.getLanguages()) {
            this.languageMap.put(language.getLanguageCode(), language);
            this.langCodeList.add(language.getLanguageCode());
         }

      }

      protected int getSize() {
         return this.langCodeList.size();
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
         Language language = (Language)this.languageMap.get(this.langCodeList.get(slotIndex));
         GuiLanguage.this.languageManager.setCurrentLanguage(language);
         GuiLanguage.this.game_settings_3.language = language.getLanguageCode();
         this.mc.refreshResources();
         GuiLanguage.this.fontRendererObj.setUnicodeFlag(GuiLanguage.this.languageManager.isCurrentLocaleUnicode() || GuiLanguage.this.game_settings_3.forceUnicodeFont);
         GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.isCurrentLanguageBidirectional());
         GuiLanguage.this.confirmSettingsBtn.displayString = I18n.format("gui.done");
         GuiLanguage.this.forceUnicodeFontBtn.displayString = GuiLanguage.this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
         GuiLanguage.this.game_settings_3.saveOptions();
      }

      protected boolean isSelected(int var1) {
         return ((String)this.langCodeList.get(slotIndex)).equals(GuiLanguage.this.languageManager.getCurrentLanguage().getLanguageCode());
      }

      protected int getContentHeight() {
         return this.getSize() * 18;
      }

      protected void drawBackground() {
         GuiLanguage.this.drawDefaultBackground();
      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         GuiLanguage.this.fontRendererObj.setBidiFlag(true);
         GuiLanguage.this.drawCenteredString(GuiLanguage.this.fontRendererObj, ((Language)this.languageMap.get(this.langCodeList.get(entryID))).toString(), this.width / 2, yPos + 1, 16777215);
         GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.getCurrentLanguage().isBidirectional());
      }
   }
}
