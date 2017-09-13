package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiListWorldSelection extends GuiListExtended {
   private static final Logger LOGGER = LogManager.getLogger();
   private final GuiWorldSelection worldSelectionObj;
   private final List entries = Lists.newArrayList();
   private int selectedIdx = -1;

   public GuiListWorldSelection(GuiWorldSelection var1, Minecraft var2, int var3, int var4, int var5, int var6, int var7) {
      super(var2, var3, var4, var5, var6, var7);
      this.worldSelectionObj = var1;
      this.refreshList();
   }

   public void refreshList() {
      ISaveFormat var1 = this.mc.getSaveLoader();

      List var2;
      try {
         var2 = var1.getSaveList();
      } catch (AnvilConverterException var5) {
         LOGGER.error("Couldn't load level list", var5);
         this.mc.displayGuiScreen(new GuiErrorScreen("Unable to load worlds", var5.getMessage()));
         return;
      }

      Collections.sort(var2);

      for(WorldSummary var4 : var2) {
         this.entries.add(new GuiListWorldSelectionEntry(this, var4, this.mc.getSaveLoader()));
      }

   }

   public GuiListWorldSelectionEntry getListEntry(int var1) {
      return (GuiListWorldSelectionEntry)this.entries.get(var1);
   }

   protected int getSize() {
      return this.entries.size();
   }

   protected int getScrollBarX() {
      return super.getScrollBarX() + 20;
   }

   public int getListWidth() {
      return super.getListWidth() + 50;
   }

   public void selectWorld(int var1) {
      this.selectedIdx = var1;
      this.worldSelectionObj.selectWorld(this.getSelectedWorld());
   }

   protected boolean isSelected(int var1) {
      return var1 == this.selectedIdx;
   }

   @Nullable
   public GuiListWorldSelectionEntry getSelectedWorld() {
      return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
   }

   public GuiWorldSelection getGuiWorldSelection() {
      return this.worldSelectionObj;
   }
}
