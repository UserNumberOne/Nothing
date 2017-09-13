package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiWorldSelection extends GuiScreen implements GuiYesNoCallback {
   private static final Logger LOGGER = LogManager.getLogger();
   protected GuiScreen prevScreen;
   protected String title = "Select world";
   private String worldVersTooltip;
   private GuiButton deleteButton;
   private GuiButton selectButton;
   private GuiButton renameButton;
   private GuiButton copyButton;
   private GuiListWorldSelection selectionList;

   public GuiWorldSelection(GuiScreen var1) {
      this.prevScreen = var1;
   }

   public void initGui() {
      this.title = I18n.format("selectWorld.title");
      this.selectionList = new GuiListWorldSelection(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
      this.postInit();
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.selectionList.handleMouseInput();
   }

   public void postInit() {
      this.selectButton = this.addButton(new GuiButton(1, this.width / 2 - 154, this.height - 52, 150, 20, I18n.format("selectWorld.select")));
      this.addButton(new GuiButton(3, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("selectWorld.create")));
      this.renameButton = this.addButton(new GuiButton(4, this.width / 2 - 154, this.height - 28, 72, 20, I18n.format("selectWorld.edit")));
      this.deleteButton = this.addButton(new GuiButton(2, this.width / 2 - 76, this.height - 28, 72, 20, I18n.format("selectWorld.delete")));
      this.copyButton = this.addButton(new GuiButton(5, this.width / 2 + 4, this.height - 28, 72, 20, I18n.format("selectWorld.recreate")));
      this.addButton(new GuiButton(0, this.width / 2 + 82, this.height - 28, 72, 20, I18n.format("gui.cancel")));
      this.selectButton.enabled = false;
      this.deleteButton.enabled = false;
      this.renameButton.enabled = false;
      this.copyButton.enabled = false;
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         GuiListWorldSelectionEntry var2 = this.selectionList.getSelectedWorld();
         if (var1.id == 2) {
            if (var2 != null) {
               var2.deleteWorld();
            }
         } else if (var1.id == 1) {
            if (var2 != null) {
               var2.joinWorld();
            }
         } else if (var1.id == 3) {
            this.mc.displayGuiScreen(new GuiCreateWorld(this));
         } else if (var1.id == 4) {
            if (var2 != null) {
               var2.editWorld();
            }
         } else if (var1.id == 0) {
            this.mc.displayGuiScreen(this.prevScreen);
         } else if (var1.id == 5 && var2 != null) {
            var2.recreateWorld();
         }
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.worldVersTooltip = null;
      this.selectionList.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 20, 16777215);
      super.drawScreen(var1, var2, var3);
      if (this.worldVersTooltip != null) {
         this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.worldVersTooltip)), var1, var2);
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      this.selectionList.mouseClicked(var1, var2, var3);
   }

   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
      this.selectionList.mouseReleased(var1, var2, var3);
   }

   public void setVersionTooltip(String var1) {
      this.worldVersTooltip = var1;
   }

   public void selectWorld(@Nullable GuiListWorldSelectionEntry var1) {
      boolean var2 = var1 != null;
      this.selectButton.enabled = var2;
      this.deleteButton.enabled = var2;
      this.renameButton.enabled = var2;
      this.copyButton.enabled = var2;
   }
}
