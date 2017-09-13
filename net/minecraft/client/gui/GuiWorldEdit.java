package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiWorldEdit extends GuiScreen {
   private final GuiScreen lastScreen;
   private GuiTextField nameEdit;
   private final String worldId;

   public GuiWorldEdit(GuiScreen var1, String var2) {
      this.lastScreen = var1;
      this.worldId = var2;
   }

   public void updateScreen() {
      this.nameEdit.updateCursorCounter();
   }

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
      GuiButton var1 = this.addButton(new GuiButton(3, this.width / 2 - 100, this.height / 4 + 24 + 12, I18n.format("selectWorld.edit.resetIcon")));
      this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 48 + 12, I18n.format("selectWorld.edit.openFolder")));
      this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, I18n.format("selectWorld.edit.save")));
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel")));
      var1.enabled = this.mc.getSaveLoader().getFile(this.worldId, "icon.png").isFile();
      ISaveFormat var2 = this.mc.getSaveLoader();
      WorldInfo var3 = var2.getWorldInfo(this.worldId);
      String var4 = var3.getWorldName();
      this.nameEdit = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
      this.nameEdit.setFocused(true);
      this.nameEdit.setText(var4);
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 1) {
            this.mc.displayGuiScreen(this.lastScreen);
         } else if (var1.id == 0) {
            ISaveFormat var2 = this.mc.getSaveLoader();
            var2.renameWorld(this.worldId, this.nameEdit.getText().trim());
            this.mc.displayGuiScreen(this.lastScreen);
         } else if (var1.id == 3) {
            ISaveFormat var3 = this.mc.getSaveLoader();
            FileUtils.deleteQuietly(var3.getFile(this.worldId, "icon.png"));
            var1.enabled = false;
         } else if (var1.id == 4) {
            ISaveFormat var4 = this.mc.getSaveLoader();
            OpenGlHelper.openFile(var4.getFile(this.worldId, "icon.png").getParentFile());
         }
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      this.nameEdit.textboxKeyTyped(var1, var2);
      ((GuiButton)this.buttonList.get(2)).enabled = !this.nameEdit.getText().trim().isEmpty();
      if (var2 == 28 || var2 == 156) {
         this.actionPerformed((GuiButton)this.buttonList.get(2));
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      this.nameEdit.mouseClicked(var1, var2, var3);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, I18n.format("selectWorld.edit.title"), this.width / 2, 20, 16777215);
      this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterName"), this.width / 2 - 100, 47, 10526880);
      this.nameEdit.drawTextBox();
      super.drawScreen(var1, var2, var3);
   }
}
