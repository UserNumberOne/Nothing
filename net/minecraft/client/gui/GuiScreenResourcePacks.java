package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.client.resources.ResourcePackListEntryDefault;
import net.minecraft.client.resources.ResourcePackListEntryFound;
import net.minecraft.client.resources.ResourcePackListEntryServer;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScreenResourcePacks extends GuiScreen {
   private final GuiScreen parentScreen;
   private List availableResourcePacks;
   private List selectedResourcePacks;
   private GuiResourcePackAvailable availableResourcePacksList;
   private GuiResourcePackSelected selectedResourcePacksList;
   private boolean changed;

   public GuiScreenResourcePacks(GuiScreen var1) {
      this.parentScreen = var1;
   }

   public void initGui() {
      this.buttonList.add(new GuiOptionButton(2, this.width / 2 - 154, this.height - 48, I18n.format("resourcePack.openFolder")));
      this.buttonList.add(new GuiOptionButton(1, this.width / 2 + 4, this.height - 48, I18n.format("gui.done")));
      if (!this.changed) {
         this.availableResourcePacks = Lists.newArrayList();
         this.selectedResourcePacks = Lists.newArrayList();
         ResourcePackRepository var1 = this.mc.getResourcePackRepository();
         var1.updateRepositoryEntriesAll();
         ArrayList var2 = Lists.newArrayList(var1.getRepositoryEntriesAll());
         var2.removeAll(var1.getRepositoryEntries());

         for(ResourcePackRepository.Entry var4 : var2) {
            this.availableResourcePacks.add(new ResourcePackListEntryFound(this, var4));
         }

         ResourcePackRepository.Entry var6 = var1.getResourcePackEntry();
         if (var6 != null) {
            this.selectedResourcePacks.add(new ResourcePackListEntryServer(this, var1.getResourcePackInstance()));
         }

         for(ResourcePackRepository.Entry var5 : Lists.reverse(var1.getRepositoryEntries())) {
            this.selectedResourcePacks.add(new ResourcePackListEntryFound(this, var5));
         }

         this.selectedResourcePacks.add(new ResourcePackListEntryDefault(this));
      }

      this.availableResourcePacksList = new GuiResourcePackAvailable(this.mc, 200, this.height, this.availableResourcePacks);
      this.availableResourcePacksList.setSlotXBoundsFromLeft(this.width / 2 - 4 - 200);
      this.availableResourcePacksList.registerScrollButtons(7, 8);
      this.selectedResourcePacksList = new GuiResourcePackSelected(this.mc, 200, this.height, this.selectedResourcePacks);
      this.selectedResourcePacksList.setSlotXBoundsFromLeft(this.width / 2 + 4);
      this.selectedResourcePacksList.registerScrollButtons(7, 8);
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.selectedResourcePacksList.handleMouseInput();
      this.availableResourcePacksList.handleMouseInput();
   }

   public boolean hasResourcePackEntry(ResourcePackListEntry var1) {
      return this.selectedResourcePacks.contains(var1);
   }

   public List getListContaining(ResourcePackListEntry var1) {
      return this.hasResourcePackEntry(var1) ? this.selectedResourcePacks : this.availableResourcePacks;
   }

   public List getAvailableResourcePacks() {
      return this.availableResourcePacks;
   }

   public List getSelectedResourcePacks() {
      return this.selectedResourcePacks;
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 2) {
            File var2 = this.mc.getResourcePackRepository().getDirResourcepacks();
            OpenGlHelper.openFile(var2);
         } else if (var1.id == 1) {
            if (this.changed) {
               ArrayList var5 = Lists.newArrayList();

               for(ResourcePackListEntry var4 : this.selectedResourcePacks) {
                  if (var4 instanceof ResourcePackListEntryFound) {
                     var5.add(((ResourcePackListEntryFound)var4).getResourcePackEntry());
                  }
               }

               Collections.reverse(var5);
               this.mc.getResourcePackRepository().setRepositories(var5);
               this.mc.gameSettings.resourcePacks.clear();
               this.mc.gameSettings.incompatibleResourcePacks.clear();

               for(ResourcePackRepository.Entry var7 : var5) {
                  this.mc.gameSettings.resourcePacks.add(var7.getResourcePackName());
                  if (var7.getPackFormat() != 2) {
                     this.mc.gameSettings.incompatibleResourcePacks.add(var7.getResourcePackName());
                  }
               }

               this.mc.gameSettings.saveOptions();
               this.mc.refreshResources();
            }

            this.mc.displayGuiScreen(this.parentScreen);
         }
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      this.availableResourcePacksList.mouseClicked(var1, var2, var3);
      this.selectedResourcePacksList.mouseClicked(var1, var2, var3);
   }

   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawBackground(0);
      this.availableResourcePacksList.drawScreen(var1, var2, var3);
      this.selectedResourcePacksList.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, I18n.format("resourcePack.title"), this.width / 2, 16, 16777215);
      this.drawCenteredString(this.fontRendererObj, I18n.format("resourcePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);
      super.drawScreen(var1, var2, var3);
   }

   public void markChanged() {
      this.changed = true;
   }
}
