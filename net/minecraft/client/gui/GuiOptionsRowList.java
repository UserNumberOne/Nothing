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
      super(var1, var2, var3, var4, var5, var6);
      this.centerListVertically = false;

      for(int var8 = 0; var8 < var7.length; var8 += 2) {
         GameSettings.Options var9 = var7[var8];
         GameSettings.Options var10 = var8 < var7.length - 1 ? var7[var8 + 1] : null;
         GuiButton var11 = this.createButton(var1, var2 / 2 - 155, 0, var9);
         GuiButton var12 = this.createButton(var1, var2 / 2 - 155 + 160, 0, var10);
         this.options.add(new GuiOptionsRowList.Row(var11, var12));
      }

   }

   private GuiButton createButton(Minecraft var1, int var2, int var3, GameSettings.Options var4) {
      if (var4 == null) {
         return null;
      } else {
         int var5 = var4.returnEnumOrdinal();
         return (GuiButton)(var4.getEnumFloat() ? new GuiOptionSlider(var5, var2, var3, var4) : new GuiOptionButton(var5, var2, var3, var4, var1.gameSettings.getKeyBinding(var4)));
      }
   }

   public GuiOptionsRowList.Row getListEntry(int var1) {
      return (GuiOptionsRowList.Row)this.options.get(var1);
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
         this.buttonA = var1;
         this.buttonB = var2;
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         if (this.buttonA != null) {
            this.buttonA.yPosition = var3;
            this.buttonA.drawButton(this.client, var6, var7);
         }

         if (this.buttonB != null) {
            this.buttonB.yPosition = var3;
            this.buttonB.drawButton(this.client, var6, var7);
         }

      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (this.buttonA.mousePressed(this.client, var2, var3)) {
            if (this.buttonA instanceof GuiOptionButton) {
               this.client.gameSettings.setOptionValue(((GuiOptionButton)this.buttonA).returnEnumOptions(), 1);
               this.buttonA.displayString = this.client.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(this.buttonA.id));
            }

            return true;
         } else if (this.buttonB != null && this.buttonB.mousePressed(this.client, var2, var3)) {
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
            this.buttonA.mouseReleased(var2, var3);
         }

         if (this.buttonB != null) {
            this.buttonB.mouseReleased(var2, var3);
         }

      }

      public void setSelected(int var1, int var2, int var3) {
      }
   }
}
