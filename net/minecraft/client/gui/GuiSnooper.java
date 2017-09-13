package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSnooper extends GuiScreen {
   private final GuiScreen lastScreen;
   private final GameSettings game_settings_2;
   private final java.util.List keys = Lists.newArrayList();
   private final java.util.List values = Lists.newArrayList();
   private String title;
   private String[] desc;
   private GuiSnooper.List list;
   private GuiButton toggleButton;

   public GuiSnooper(GuiScreen var1, GameSettings var2) {
      this.lastScreen = var1;
      this.game_settings_2 = var2;
   }

   public void initGui() {
      this.title = I18n.format("options.snooper.title");
      String var1 = I18n.format("options.snooper.desc");
      ArrayList var2 = Lists.newArrayList();

      for(String var4 : this.fontRendererObj.listFormattedStringToWidth(var1, this.width - 30)) {
         var2.add(var4);
      }

      this.desc = (String[])var2.toArray(new String[var2.size()]);
      this.keys.clear();
      this.values.clear();
      this.toggleButton = this.addButton(new GuiButton(1, this.width / 2 - 152, this.height - 30, 150, 20, this.game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED)));
      this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height - 30, 150, 20, I18n.format("gui.done")));
      boolean var6 = this.mc.getIntegratedServer() != null && this.mc.getIntegratedServer().getPlayerUsageSnooper() != null;

      for(Entry var5 : (new TreeMap(this.mc.getPlayerUsageSnooper().getCurrentStats())).entrySet()) {
         this.keys.add((var6 ? "C " : "") + (String)var5.getKey());
         this.values.add(this.fontRendererObj.trimStringToWidth((String)var5.getValue(), this.width - 220));
      }

      if (var6) {
         for(Entry var9 : (new TreeMap(this.mc.getIntegratedServer().getPlayerUsageSnooper().getCurrentStats())).entrySet()) {
            this.keys.add("S " + (String)var9.getKey());
            this.values.add(this.fontRendererObj.trimStringToWidth((String)var9.getValue(), this.width - 220));
         }
      }

      this.list = new GuiSnooper.List();
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.list.handleMouseInput();
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 2) {
            this.game_settings_2.saveOptions();
            this.game_settings_2.saveOptions();
            this.mc.displayGuiScreen(this.lastScreen);
         }

         if (var1.id == 1) {
            this.game_settings_2.setOptionValue(GameSettings.Options.SNOOPER_ENABLED, 1);
            this.toggleButton.displayString = this.game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED);
         }
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.list.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 16777215);
      int var4 = 22;

      for(String var8 : this.desc) {
         this.drawCenteredString(this.fontRendererObj, var8, this.width / 2, var4, 8421504);
         var4 += this.fontRendererObj.FONT_HEIGHT;
      }

      super.drawScreen(var1, var2, var3);
   }

   @SideOnly(Side.CLIENT)
   class List extends GuiSlot {
      public List() {
         super(GuiSnooper.this.mc, GuiSnooper.this.width, GuiSnooper.this.height, 80, GuiSnooper.this.height - 40, GuiSnooper.this.fontRendererObj.FONT_HEIGHT + 1);
      }

      protected int getSize() {
         return GuiSnooper.this.keys.size();
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      }

      protected boolean isSelected(int var1) {
         return false;
      }

      protected void drawBackground() {
      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         GuiSnooper.this.fontRendererObj.drawString((String)GuiSnooper.this.keys.get(var1), 10, var3, 16777215);
         GuiSnooper.this.fontRendererObj.drawString((String)GuiSnooper.this.values.get(var1), 230, var3, 16777215);
      }

      protected int getScrollBarX() {
         return this.width - 10;
      }
   }
}
