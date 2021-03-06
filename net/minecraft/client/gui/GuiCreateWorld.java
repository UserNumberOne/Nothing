package net.minecraft.client.gui;

import java.io.IOException;
import java.util.Random;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiCreateWorld extends GuiScreen {
   private final GuiScreen parentScreen;
   private GuiTextField worldNameField;
   private GuiTextField worldSeedField;
   private String saveDirName;
   private String gameMode = "survival";
   private String savedGameMode;
   private boolean generateStructuresEnabled = true;
   private boolean allowCheats;
   private boolean allowCheatsWasSetByUser;
   private boolean bonusChestEnabled;
   private boolean hardCoreMode;
   private boolean alreadyGenerated;
   private boolean inMoreWorldOptionsDisplay;
   private GuiButton btnGameMode;
   private GuiButton btnMoreOptions;
   private GuiButton btnMapFeatures;
   private GuiButton btnBonusItems;
   private GuiButton btnMapType;
   private GuiButton btnAllowCommands;
   private GuiButton btnCustomizeType;
   private String gameModeDesc1;
   private String gameModeDesc2;
   private String worldSeed;
   private String worldName;
   private int selectedIndex;
   public String chunkProviderSettingsJson = "";
   private static final String[] DISALLOWED_FILENAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

   public GuiCreateWorld(GuiScreen var1) {
      this.parentScreen = var1;
      this.worldSeed = "";
      this.worldName = I18n.format("selectWorld.newWorld");
   }

   public void updateScreen() {
      this.worldNameField.updateCursorCounter();
      this.worldSeedField.updateCursorCounter();
   }

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
      this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("selectWorld.create")));
      this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
      this.btnGameMode = this.addButton(new GuiButton(2, this.width / 2 - 75, 115, 150, 20, I18n.format("selectWorld.gameMode")));
      this.btnMoreOptions = this.addButton(new GuiButton(3, this.width / 2 - 75, 187, 150, 20, I18n.format("selectWorld.moreWorldOptions")));
      this.btnMapFeatures = this.addButton(new GuiButton(4, this.width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.mapFeatures")));
      this.btnMapFeatures.visible = false;
      this.btnBonusItems = this.addButton(new GuiButton(7, this.width / 2 + 5, 151, 150, 20, I18n.format("selectWorld.bonusItems")));
      this.btnBonusItems.visible = false;
      this.btnMapType = this.addButton(new GuiButton(5, this.width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.mapType")));
      this.btnMapType.visible = false;
      this.btnAllowCommands = this.addButton(new GuiButton(6, this.width / 2 - 155, 151, 150, 20, I18n.format("selectWorld.allowCommands")));
      this.btnAllowCommands.visible = false;
      this.btnCustomizeType = this.addButton(new GuiButton(8, this.width / 2 + 5, 120, 150, 20, I18n.format("selectWorld.customizeType")));
      this.btnCustomizeType.visible = false;
      this.worldNameField = new GuiTextField(9, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
      this.worldNameField.setFocused(true);
      this.worldNameField.setText(this.worldName);
      this.worldSeedField = new GuiTextField(10, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
      this.worldSeedField.setText(this.worldSeed);
      this.showMoreWorldOptions(this.inMoreWorldOptionsDisplay);
      this.calcSaveDirName();
      this.updateDisplayState();
   }

   private void calcSaveDirName() {
      this.saveDirName = this.worldNameField.getText().trim();

      for(char var4 : ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS) {
         this.saveDirName = this.saveDirName.replace(var4, '_');
      }

      if (StringUtils.isEmpty(this.saveDirName)) {
         this.saveDirName = "World";
      }

      this.saveDirName = getUncollidingSaveDirName(this.mc.getSaveLoader(), this.saveDirName);
   }

   private void updateDisplayState() {
      this.btnGameMode.displayString = I18n.format("selectWorld.gameMode") + ": " + I18n.format("selectWorld.gameMode." + this.gameMode);
      this.gameModeDesc1 = I18n.format("selectWorld.gameMode." + this.gameMode + ".line1");
      this.gameModeDesc2 = I18n.format("selectWorld.gameMode." + this.gameMode + ".line2");
      this.btnMapFeatures.displayString = I18n.format("selectWorld.mapFeatures") + " ";
      if (this.generateStructuresEnabled) {
         this.btnMapFeatures.displayString = this.btnMapFeatures.displayString + I18n.format("options.on");
      } else {
         this.btnMapFeatures.displayString = this.btnMapFeatures.displayString + I18n.format("options.off");
      }

      this.btnBonusItems.displayString = I18n.format("selectWorld.bonusItems") + " ";
      if (this.bonusChestEnabled && !this.hardCoreMode) {
         this.btnBonusItems.displayString = this.btnBonusItems.displayString + I18n.format("options.on");
      } else {
         this.btnBonusItems.displayString = this.btnBonusItems.displayString + I18n.format("options.off");
      }

      this.btnMapType.displayString = I18n.format("selectWorld.mapType") + " " + I18n.format(WorldType.WORLD_TYPES[this.selectedIndex].getTranslateName());
      this.btnAllowCommands.displayString = I18n.format("selectWorld.allowCommands") + " ";
      if (this.allowCheats && !this.hardCoreMode) {
         this.btnAllowCommands.displayString = this.btnAllowCommands.displayString + I18n.format("options.on");
      } else {
         this.btnAllowCommands.displayString = this.btnAllowCommands.displayString + I18n.format("options.off");
      }

   }

   public static String getUncollidingSaveDirName(ISaveFormat var0, String var1) {
      var1 = var1.replaceAll("[\\./\"]", "_");

      for(String var5 : DISALLOWED_FILENAMES) {
         if (var1.equalsIgnoreCase(var5)) {
            var1 = "_" + var1 + "_";
         }
      }

      while(var0.getWorldInfo(var1) != null) {
         var1 = var1 + "-";
      }

      return var1;
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 1) {
            this.mc.displayGuiScreen(this.parentScreen);
         } else if (var1.id == 0) {
            this.mc.displayGuiScreen((GuiScreen)null);
            if (this.alreadyGenerated) {
               return;
            }

            this.alreadyGenerated = true;
            long var2 = (new Random()).nextLong();
            String var4 = this.worldSeedField.getText();
            if (!StringUtils.isEmpty(var4)) {
               try {
                  long var5 = Long.parseLong(var4);
                  if (var5 != 0L) {
                     var2 = var5;
                  }
               } catch (NumberFormatException var7) {
                  var2 = (long)var4.hashCode();
               }
            }

            WorldType.WORLD_TYPES[this.selectedIndex].onGUICreateWorldPress();
            WorldSettings var8 = new WorldSettings(var2, GameType.getByName(this.gameMode), this.generateStructuresEnabled, this.hardCoreMode, WorldType.WORLD_TYPES[this.selectedIndex]);
            var8.setGeneratorOptions(this.chunkProviderSettingsJson);
            if (this.bonusChestEnabled && !this.hardCoreMode) {
               var8.enableBonusChest();
            }

            if (this.allowCheats && !this.hardCoreMode) {
               var8.enableCommands();
            }

            this.mc.launchIntegratedServer(this.saveDirName, this.worldNameField.getText().trim(), var8);
         } else if (var1.id == 3) {
            this.toggleMoreWorldOptions();
         } else if (var1.id == 2) {
            if ("survival".equals(this.gameMode)) {
               if (!this.allowCheatsWasSetByUser) {
                  this.allowCheats = false;
               }

               this.hardCoreMode = false;
               this.gameMode = "hardcore";
               this.hardCoreMode = true;
               this.btnAllowCommands.enabled = false;
               this.btnBonusItems.enabled = false;
               this.updateDisplayState();
            } else if ("hardcore".equals(this.gameMode)) {
               if (!this.allowCheatsWasSetByUser) {
                  this.allowCheats = true;
               }

               this.hardCoreMode = false;
               this.gameMode = "creative";
               this.updateDisplayState();
               this.hardCoreMode = false;
               this.btnAllowCommands.enabled = true;
               this.btnBonusItems.enabled = true;
            } else {
               if (!this.allowCheatsWasSetByUser) {
                  this.allowCheats = false;
               }

               this.gameMode = "survival";
               this.updateDisplayState();
               this.btnAllowCommands.enabled = true;
               this.btnBonusItems.enabled = true;
               this.hardCoreMode = false;
            }

            this.updateDisplayState();
         } else if (var1.id == 4) {
            this.generateStructuresEnabled = !this.generateStructuresEnabled;
            this.updateDisplayState();
         } else if (var1.id == 7) {
            this.bonusChestEnabled = !this.bonusChestEnabled;
            this.updateDisplayState();
         } else if (var1.id == 5) {
            ++this.selectedIndex;
            if (this.selectedIndex >= WorldType.WORLD_TYPES.length) {
               this.selectedIndex = 0;
            }

            while(!this.canSelectCurWorldType()) {
               ++this.selectedIndex;
               if (this.selectedIndex >= WorldType.WORLD_TYPES.length) {
                  this.selectedIndex = 0;
               }
            }

            this.chunkProviderSettingsJson = "";
            this.updateDisplayState();
            this.showMoreWorldOptions(this.inMoreWorldOptionsDisplay);
         } else if (var1.id == 6) {
            this.allowCheatsWasSetByUser = true;
            this.allowCheats = !this.allowCheats;
            this.updateDisplayState();
         } else if (var1.id == 8) {
            WorldType.WORLD_TYPES[this.selectedIndex].onCustomizeButton(this.mc, this);
         }
      }

   }

   private boolean canSelectCurWorldType() {
      WorldType var1 = WorldType.WORLD_TYPES[this.selectedIndex];
      return var1 != null && var1.canBeCreated() ? (var1 == WorldType.DEBUG_WORLD ? isShiftKeyDown() : true) : false;
   }

   private void toggleMoreWorldOptions() {
      this.showMoreWorldOptions(!this.inMoreWorldOptionsDisplay);
   }

   private void showMoreWorldOptions(boolean var1) {
      this.inMoreWorldOptionsDisplay = var1;
      if (WorldType.WORLD_TYPES[this.selectedIndex] == WorldType.DEBUG_WORLD) {
         this.btnGameMode.visible = !this.inMoreWorldOptionsDisplay;
         this.btnGameMode.enabled = false;
         if (this.savedGameMode == null) {
            this.savedGameMode = this.gameMode;
         }

         this.gameMode = "spectator";
         this.btnMapFeatures.visible = false;
         this.btnBonusItems.visible = false;
         this.btnMapType.visible = this.inMoreWorldOptionsDisplay;
         this.btnAllowCommands.visible = false;
         this.btnCustomizeType.visible = false;
      } else {
         this.btnGameMode.visible = !this.inMoreWorldOptionsDisplay;
         this.btnGameMode.enabled = true;
         if (this.savedGameMode != null) {
            this.gameMode = this.savedGameMode;
            this.savedGameMode = null;
         }

         this.btnMapFeatures.visible = this.inMoreWorldOptionsDisplay && WorldType.WORLD_TYPES[this.selectedIndex] != WorldType.CUSTOMIZED;
         this.btnBonusItems.visible = this.inMoreWorldOptionsDisplay;
         this.btnMapType.visible = this.inMoreWorldOptionsDisplay;
         this.btnAllowCommands.visible = this.inMoreWorldOptionsDisplay;
         this.btnCustomizeType.visible = this.inMoreWorldOptionsDisplay && WorldType.WORLD_TYPES[this.selectedIndex].isCustomizable();
      }

      this.updateDisplayState();
      if (this.inMoreWorldOptionsDisplay) {
         this.btnMoreOptions.displayString = I18n.format("gui.done");
      } else {
         this.btnMoreOptions.displayString = I18n.format("selectWorld.moreWorldOptions");
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (this.worldNameField.isFocused() && !this.inMoreWorldOptionsDisplay) {
         this.worldNameField.textboxKeyTyped(var1, var2);
         this.worldName = this.worldNameField.getText();
      } else if (this.worldSeedField.isFocused() && this.inMoreWorldOptionsDisplay) {
         this.worldSeedField.textboxKeyTyped(var1, var2);
         this.worldSeed = this.worldSeedField.getText();
      }

      if (var2 == 28 || var2 == 156) {
         this.actionPerformed((GuiButton)this.buttonList.get(0));
      }

      ((GuiButton)this.buttonList.get(0)).enabled = !this.worldNameField.getText().isEmpty();
      this.calcSaveDirName();
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      if (this.inMoreWorldOptionsDisplay) {
         this.worldSeedField.mouseClicked(var1, var2, var3);
      } else {
         this.worldNameField.mouseClicked(var1, var2, var3);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, I18n.format("selectWorld.create"), this.width / 2, 20, -1);
      if (this.inMoreWorldOptionsDisplay) {
         this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterSeed"), this.width / 2 - 100, 47, -6250336);
         this.drawString(this.fontRendererObj, I18n.format("selectWorld.seedInfo"), this.width / 2 - 100, 85, -6250336);
         if (this.btnMapFeatures.visible) {
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.mapFeatures.info"), this.width / 2 - 150, 122, -6250336);
         }

         if (this.btnAllowCommands.visible) {
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.allowCommands.info"), this.width / 2 - 150, 172, -6250336);
         }

         this.worldSeedField.drawTextBox();
         if (WorldType.WORLD_TYPES[this.selectedIndex].showWorldInfoNotice()) {
            this.fontRendererObj.drawSplitString(I18n.format(WorldType.WORLD_TYPES[this.selectedIndex].getTranslatedInfo()), this.btnMapType.xPosition + 2, this.btnMapType.yPosition + 22, this.btnMapType.getButtonWidth(), 10526880);
         }
      } else {
         this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterName"), this.width / 2 - 100, 47, -6250336);
         this.drawString(this.fontRendererObj, I18n.format("selectWorld.resultFolder") + " " + this.saveDirName, this.width / 2 - 100, 85, -6250336);
         this.worldNameField.drawTextBox();
         this.drawString(this.fontRendererObj, this.gameModeDesc1, this.width / 2 - 100, 137, -6250336);
         this.drawString(this.fontRendererObj, this.gameModeDesc2, this.width / 2 - 100, 149, -6250336);
      }

      super.drawScreen(var1, var2, var3);
   }

   public void recreateFromExistingWorld(WorldInfo var1) {
      this.worldName = I18n.format("selectWorld.newWorld.copyOf", var1.getWorldName());
      this.worldSeed = var1.getSeed() + "";
      this.selectedIndex = var1.getTerrainType().getWorldTypeID();
      this.chunkProviderSettingsJson = var1.getGeneratorOptions();
      this.generateStructuresEnabled = var1.isMapFeaturesEnabled();
      this.allowCheats = var1.areCommandsAllowed();
      if (var1.isHardcoreModeEnabled()) {
         this.gameMode = "hardcore";
      } else if (var1.getGameType().isSurvivalOrAdventure()) {
         this.gameMode = "survival";
      } else if (var1.getGameType().isCreative()) {
         this.gameMode = "creative";
      }

   }
}
