package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOptionsRowList extends GuiListExtended {
   private final List options = Lists.newArrayList();

   public GuiOptionsRowList(Minecraft var1, int var2, int var3, int var4, int var5, int var6, GameSettings.Options... var7) {
      super(mcIn, p_i45015_2_, p_i45015_3_, p_i45015_4_, p_i45015_5_, p_i45015_6_);
      this.centerListVertically = false;

      for(int i = 0; i < p_i45015_7_.length; i += 2) {
         GameSettings.Options gamesettings$options = p_i45015_7_[i];
         GameSettings.Options gamesettings$options1 = i < p_i45015_7_.length - 1 ? p_i45015_7_[i + 1] : null;
         GuiButton guibutton = this.createButton(mcIn, p_i45015_2_ / 2 - 155, 0, gamesettings$options);
         GuiButton guibutton1 = this.createButton(mcIn, p_i45015_2_ / 2 - 155 + 160, 0, gamesettings$options1);
         this.options.add(new GuiOptionsRowList.Row(guibutton, guibutton1));
      }

   }

   private GuiButton createButton(Minecraft var1, int var2, int var3, GameSettings.Options var4) {
      if (options == null) {
         return null;
      } else {
         int i = options.returnEnumOrdinal();
         return (GuiButton)(options.getEnumFloat() ? new GuiOptionSlider(i, p_148182_2_, p_148182_3_, options) : new GuiOptionButton(i, p_148182_2_, p_148182_3_, options, mcIn.gameSettings.getKeyBinding(options)));
      }
   }

   public GuiOptionsRowList.Row getListEntry(int var1) {
      return (GuiOptionsRowList.Row)this.options.get(index);
   }

   protected int getSize() {
      return this.options.size();
   }

   public int getListWidth() {
      return 400;
   }

   protected int getScrollBarX() {
      return super.getScrollBarX() + 32;
   }

   @SideOnly(Side.CLIENT)
   public static class Row implements GuiListExtended.IGuiListEntry {
      private final Minecraft client = Minecraft.getMinecraft();
      private final GuiButton buttonA;
      private final GuiButton buttonB;

      public Row(GuiButton var1, GuiButton var2) {
         this.buttonA = buttonAIn;
         this.buttonB = buttonBIn;
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         if (this.buttonA != null) {
            this.buttonA.yPosition = y;
            this.buttonA.drawButton(this.client, mouseX, mouseY);
         }

         if (this.buttonB != null) {
            this.buttonB.yPosition = y;
            this.buttonB.drawButton(this.client, mouseX, mouseY);
         }

      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (this.buttonA.mousePressed(this.client, mouseX, mouseY)) {
            if (this.buttonA instanceof GuiOptionButton) {
               this.client.gameSettings.setOptionValue(((GuiOptionButton)this.buttonA).returnEnumOptions(), 1);
               this.buttonA.displayString = this.client.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(this.buttonA.id));
            }

            return true;
         } else if (this.buttonB != null && this.buttonB.mousePressed(this.client, mouseX, mouseY)) {
            if (this.buttonB instanceof GuiOptionButton) {
               this.client.gameSettings.setOptionValue(((GuiOptionButton)this.buttonB).returnEnumOptions(), 1);
               this.buttonB.displayString = this.client.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(this.buttonB.id));
            }

            return true;
         } else {
            return false;
         }
      }

      public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (this.buttonA != null) {
            this.buttonA.mouseReleased(x, y);
         }

         if (this.buttonB != null) {
            this.buttonB.mouseReleased(x, y);
         }

      }

      public void setSelected(int var1, int var2, int var3) {
      }
   }
}
