package net.minecraft.client.gui;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IntHashMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPageButtonList extends GuiListExtended {
   private final List entries = Lists.newArrayList();
   private final IntHashMap componentMap = new IntHashMap();
   private final List editBoxes = Lists.newArrayList();
   private final GuiPageButtonList.GuiListEntry[][] pages;
   private int page;
   private final GuiPageButtonList.GuiResponder responder;
   private Gui focusedControl;

   public GuiPageButtonList(Minecraft var1, int var2, int var3, int var4, int var5, int var6, GuiPageButtonList.GuiResponder var7, GuiPageButtonList.GuiListEntry[]... var8) {
      super(var1, var2, var3, var4, var5, var6);
      this.responder = var7;
      this.pages = var8;
      this.centerListVertically = false;
      this.populateComponents();
      this.populateEntries();
   }

   private void populateComponents() {
      for(GuiPageButtonList.GuiListEntry[] var4 : this.pages) {
         for(int var5 = 0; var5 < var4.length; var5 += 2) {
            GuiPageButtonList.GuiListEntry var6 = var4[var5];
            GuiPageButtonList.GuiListEntry var7 = var5 < var4.length - 1 ? var4[var5 + 1] : null;
            Gui var8 = this.createEntry(var6, 0, var7 == null);
            Gui var9 = this.createEntry(var7, 160, var6 == null);
            GuiPageButtonList.GuiEntry var10 = new GuiPageButtonList.GuiEntry(var8, var9);
            this.entries.add(var10);
            if (var6 != null && var8 != null) {
               this.componentMap.addKey(var6.getId(), var8);
               if (var8 instanceof GuiTextField) {
                  this.editBoxes.add((GuiTextField)var8);
               }
            }

            if (var7 != null && var9 != null) {
               this.componentMap.addKey(var7.getId(), var9);
               if (var9 instanceof GuiTextField) {
                  this.editBoxes.add((GuiTextField)var9);
               }
            }
         }
      }

   }

   private void populateEntries() {
      this.entries.clear();

      for(int var1 = 0; var1 < this.pages[this.page].length; var1 += 2) {
         GuiPageButtonList.GuiListEntry var2 = this.pages[this.page][var1];
         GuiPageButtonList.GuiListEntry var3 = var1 < this.pages[this.page].length - 1 ? this.pages[this.page][var1 + 1] : null;
         Gui var4 = (Gui)this.componentMap.lookup(var2.getId());
         Gui var5 = var3 != null ? (Gui)this.componentMap.lookup(var3.getId()) : null;
         GuiPageButtonList.GuiEntry var6 = new GuiPageButtonList.GuiEntry(var4, var5);
         this.entries.add(var6);
      }

   }

   public void setPage(int var1) {
      if (var1 != this.page) {
         int var2 = this.page;
         this.page = var1;
         this.populateEntries();
         this.markVisibility(var2, var1);
         this.amountScrolled = 0.0F;
      }

   }

   public int getPage() {
      return this.page;
   }

   public int getPageCount() {
      return this.pages.length;
   }

   public Gui getFocusedControl() {
      return this.focusedControl;
   }

   public void previousPage() {
      if (this.page > 0) {
         this.setPage(this.page - 1);
      }

   }

   public void nextPage() {
      if (this.page < this.pages.length - 1) {
         this.setPage(this.page + 1);
      }

   }

   public Gui getComponent(int var1) {
      return (Gui)this.componentMap.lookup(var1);
   }

   private void markVisibility(int var1, int var2) {
      for(GuiPageButtonList.GuiListEntry var6 : this.pages[var1]) {
         if (var6 != null) {
            this.setComponentVisibility((Gui)this.componentMap.lookup(var6.getId()), false);
         }
      }

      for(GuiPageButtonList.GuiListEntry var10 : this.pages[var2]) {
         if (var10 != null) {
            this.setComponentVisibility((Gui)this.componentMap.lookup(var10.getId()), true);
         }
      }

   }

   private void setComponentVisibility(Gui var1, boolean var2) {
      if (var1 instanceof GuiButton) {
         ((GuiButton)var1).visible = var2;
      } else if (var1 instanceof GuiTextField) {
         ((GuiTextField)var1).setVisible(var2);
      } else if (var1 instanceof GuiLabel) {
         ((GuiLabel)var1).visible = var2;
      }

   }

   @Nullable
   private Gui createEntry(@Nullable GuiPageButtonList.GuiListEntry var1, int var2, boolean var3) {
      return (Gui)(var1 instanceof GuiPageButtonList.GuiSlideEntry ? this.createSlider(this.width / 2 - 155 + var2, 0, (GuiPageButtonList.GuiSlideEntry)var1) : (var1 instanceof GuiPageButtonList.GuiButtonEntry ? this.createButton(this.width / 2 - 155 + var2, 0, (GuiPageButtonList.GuiButtonEntry)var1) : (var1 instanceof GuiPageButtonList.EditBoxEntry ? this.createTextField(this.width / 2 - 155 + var2, 0, (GuiPageButtonList.EditBoxEntry)var1) : (var1 instanceof GuiPageButtonList.GuiLabelEntry ? this.createLabel(this.width / 2 - 155 + var2, 0, (GuiPageButtonList.GuiLabelEntry)var1, var3) : null))));
   }

   public void setActive(boolean var1) {
      for(GuiPageButtonList.GuiEntry var3 : this.entries) {
         if (var3.component1 instanceof GuiButton) {
            ((GuiButton)var3.component1).enabled = var1;
         }

         if (var3.component2 instanceof GuiButton) {
            ((GuiButton)var3.component2).enabled = var1;
         }
      }

   }

   public boolean mouseClicked(int var1, int var2, int var3) {
      boolean var4 = super.mouseClicked(var1, var2, var3);
      int var5 = this.getSlotIndexFromScreenCoords(var1, var2);
      if (var5 >= 0) {
         GuiPageButtonList.GuiEntry var6 = this.getListEntry(var5);
         if (this.focusedControl != var6.focusedControl && this.focusedControl != null && this.focusedControl instanceof GuiTextField) {
            ((GuiTextField)this.focusedControl).setFocused(false);
         }

         this.focusedControl = var6.focusedControl;
      }

      return var4;
   }

   private GuiSlider createSlider(int var1, int var2, GuiPageButtonList.GuiSlideEntry var3) {
      GuiSlider var4 = new GuiSlider(this.responder, var3.getId(), var1, var2, var3.getCaption(), var3.getMinValue(), var3.getMaxValue(), var3.getInitalValue(), var3.getFormatter());
      var4.visible = var3.shouldStartVisible();
      return var4;
   }

   private GuiListButton createButton(int var1, int var2, GuiPageButtonList.GuiButtonEntry var3) {
      GuiListButton var4 = new GuiListButton(this.responder, var3.getId(), var1, var2, var3.getCaption(), var3.getInitialValue());
      var4.visible = var3.shouldStartVisible();
      return var4;
   }

   private GuiTextField createTextField(int var1, int var2, GuiPageButtonList.EditBoxEntry var3) {
      GuiTextField var4 = new GuiTextField(var3.getId(), this.mc.fontRendererObj, var1, var2, 150, 20);
      var4.setText(var3.getCaption());
      var4.setGuiResponder(this.responder);
      var4.setVisible(var3.shouldStartVisible());
      var4.setValidator(var3.getFilter());
      return var4;
   }

   private GuiLabel createLabel(int var1, int var2, GuiPageButtonList.GuiLabelEntry var3, boolean var4) {
      GuiLabel var5;
      if (var4) {
         var5 = new GuiLabel(this.mc.fontRendererObj, var3.getId(), var1, var2, this.width - var1 * 2, 20, -1);
      } else {
         var5 = new GuiLabel(this.mc.fontRendererObj, var3.getId(), var1, var2, 150, 20, -1);
      }

      var5.visible = var3.shouldStartVisible();
      var5.addLine(var3.getCaption());
      var5.setCentered();
      return var5;
   }

   public void onKeyPressed(char var1, int var2) {
      if (this.focusedControl instanceof GuiTextField) {
         GuiTextField var3 = (GuiTextField)this.focusedControl;
         if (!GuiScreen.isKeyComboCtrlV(var2)) {
            if (var2 == 15) {
               var3.setFocused(false);
               int var4 = this.editBoxes.indexOf(this.focusedControl);
               if (GuiScreen.isShiftKeyDown()) {
                  if (var4 == 0) {
                     var4 = this.editBoxes.size() - 1;
                  } else {
                     --var4;
                  }
               } else if (var4 == this.editBoxes.size() - 1) {
                  var4 = 0;
               } else {
                  ++var4;
               }

               this.focusedControl = (Gui)this.editBoxes.get(var4);
               var3 = (GuiTextField)this.focusedControl;
               var3.setFocused(true);
               int var5 = var3.yPosition + this.slotHeight;
               int var6 = var3.yPosition;
               if (var5 > this.bottom) {
                  this.amountScrolled += (float)(var5 - this.bottom);
               } else if (var6 < this.top) {
                  this.amountScrolled = (float)var6;
               }
            } else {
               var3.textboxKeyTyped(var1, var2);
            }
         } else {
            String var14 = GuiScreen.getClipboardString();
            String[] var15 = var14.split(";");
            int var16 = this.editBoxes.indexOf(this.focusedControl);
            int var7 = var16;

            for(String var11 : var15) {
               ((GuiTextField)this.editBoxes.get(var7)).setText(var11);
               if (var7 == this.editBoxes.size() - 1) {
                  var7 = 0;
               } else {
                  ++var7;
               }

               if (var7 == var16) {
                  break;
               }
            }
         }
      }

   }

   public GuiPageButtonList.GuiEntry getListEntry(int var1) {
      return (GuiPageButtonList.GuiEntry)this.entries.get(var1);
   }

   public int getSize() {
      return this.entries.size();
   }

   public int getListWidth() {
      return 400;
   }

   protected int getScrollBarX() {
      return super.getScrollBarX() + 32;
   }

   @SideOnly(Side.CLIENT)
   public static class EditBoxEntry extends GuiPageButtonList.GuiListEntry {
      private final Predicate filter;

      public EditBoxEntry(int var1, String var2, boolean var3, Predicate var4) {
         super(var1, var2, var3);
         this.filter = (Predicate)Objects.firstNonNull(var4, Predicates.alwaysTrue());
      }

      public Predicate getFilter() {
         return this.filter;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GuiButtonEntry extends GuiPageButtonList.GuiListEntry {
      private final boolean initialValue;

      public GuiButtonEntry(int var1, String var2, boolean var3, boolean var4) {
         super(var1, var2, var3);
         this.initialValue = var4;
      }

      public boolean getInitialValue() {
         return this.initialValue;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GuiEntry implements GuiListExtended.IGuiListEntry {
      private final Minecraft client = Minecraft.getMinecraft();
      private final Gui component1;
      private final Gui component2;
      private Gui focusedControl;

      public GuiEntry(@Nullable Gui var1, @Nullable Gui var2) {
         this.component1 = var1;
         this.component2 = var2;
      }

      public Gui getComponent1() {
         return this.component1;
      }

      public Gui getComponent2() {
         return this.component2;
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         this.renderComponent(this.component1, var3, var6, var7, false);
         this.renderComponent(this.component2, var3, var6, var7, false);
      }

      private void renderComponent(Gui var1, int var2, int var3, int var4, boolean var5) {
         if (var1 != null) {
            if (var1 instanceof GuiButton) {
               this.renderButton((GuiButton)var1, var2, var3, var4, var5);
            } else if (var1 instanceof GuiTextField) {
               this.renderTextField((GuiTextField)var1, var2, var5);
            } else if (var1 instanceof GuiLabel) {
               this.renderLabel((GuiLabel)var1, var2, var3, var4, var5);
            }
         }

      }

      private void renderButton(GuiButton var1, int var2, int var3, int var4, boolean var5) {
         var1.yPosition = var2;
         if (!var5) {
            var1.drawButton(this.client, var3, var4);
         }

      }

      private void renderTextField(GuiTextField var1, int var2, boolean var3) {
         var1.yPosition = var2;
         if (!var3) {
            var1.drawTextBox();
         }

      }

      private void renderLabel(GuiLabel var1, int var2, int var3, int var4, boolean var5) {
         var1.y = var2;
         if (!var5) {
            var1.drawLabel(this.client, var3, var4);
         }

      }

      public void setSelected(int var1, int var2, int var3) {
         this.renderComponent(this.component1, var3, 0, 0, true);
         this.renderComponent(this.component2, var3, 0, 0, true);
      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         boolean var7 = this.clickComponent(this.component1, var2, var3, var4);
         boolean var8 = this.clickComponent(this.component2, var2, var3, var4);
         return var7 || var8;
      }

      private boolean clickComponent(Gui var1, int var2, int var3, int var4) {
         if (var1 == null) {
            return false;
         } else if (var1 instanceof GuiButton) {
            return this.clickButton((GuiButton)var1, var2, var3, var4);
         } else {
            if (var1 instanceof GuiTextField) {
               this.clickTextField((GuiTextField)var1, var2, var3, var4);
            }

            return false;
         }
      }

      private boolean clickButton(GuiButton var1, int var2, int var3, int var4) {
         boolean var5 = var1.mousePressed(this.client, var2, var3);
         if (var5) {
            this.focusedControl = var1;
         }

         return var5;
      }

      private void clickTextField(GuiTextField var1, int var2, int var3, int var4) {
         var1.mouseClicked(var2, var3, var4);
         if (var1.isFocused()) {
            this.focusedControl = var1;
         }

      }

      public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
         this.releaseComponent(this.component1, var2, var3, var4);
         this.releaseComponent(this.component2, var2, var3, var4);
      }

      private void releaseComponent(Gui var1, int var2, int var3, int var4) {
         if (var1 != null && var1 instanceof GuiButton) {
            this.releaseButton((GuiButton)var1, var2, var3, var4);
         }

      }

      private void releaseButton(GuiButton var1, int var2, int var3, int var4) {
         var1.mouseReleased(var2, var3);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GuiLabelEntry extends GuiPageButtonList.GuiListEntry {
      public GuiLabelEntry(int var1, String var2, boolean var3) {
         super(var1, var2, var3);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GuiListEntry {
      private final int id;
      private final String caption;
      private final boolean startVisible;

      public GuiListEntry(int var1, String var2, boolean var3) {
         this.id = var1;
         this.caption = var2;
         this.startVisible = var3;
      }

      public int getId() {
         return this.id;
      }

      public String getCaption() {
         return this.caption;
      }

      public boolean shouldStartVisible() {
         return this.startVisible;
      }
   }

   @SideOnly(Side.CLIENT)
   public interface GuiResponder {
      void setEntryValue(int var1, boolean var2);

      void setEntryValue(int var1, float var2);

      void setEntryValue(int var1, String var2);
   }

   @SideOnly(Side.CLIENT)
   public static class GuiSlideEntry extends GuiPageButtonList.GuiListEntry {
      private final GuiSlider.FormatHelper formatter;
      private final float minValue;
      private final float maxValue;
      private final float initialValue;

      public GuiSlideEntry(int var1, String var2, boolean var3, GuiSlider.FormatHelper var4, float var5, float var6, float var7) {
         super(var1, var2, var3);
         this.formatter = var4;
         this.minValue = var5;
         this.maxValue = var6;
         this.initialValue = var7;
      }

      public GuiSlider.FormatHelper getFormatter() {
         return this.formatter;
      }

      public float getMinValue() {
         return this.minValue;
      }

      public float getMaxValue() {
         return this.maxValue;
      }

      public float getInitalValue() {
         return this.initialValue;
      }
   }
}
