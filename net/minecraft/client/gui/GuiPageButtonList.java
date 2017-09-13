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
      super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
      this.responder = p_i45536_7_;
      this.pages = p_i45536_8_;
      this.centerListVertically = false;
      this.populateComponents();
      this.populateEntries();
   }

   private void populateComponents() {
      for(GuiPageButtonList.GuiListEntry[] aguipagebuttonlist$guilistentry : this.pages) {
         for(int i = 0; i < aguipagebuttonlist$guilistentry.length; i += 2) {
            GuiPageButtonList.GuiListEntry guipagebuttonlist$guilistentry = aguipagebuttonlist$guilistentry[i];
            GuiPageButtonList.GuiListEntry guipagebuttonlist$guilistentry1 = i < aguipagebuttonlist$guilistentry.length - 1 ? aguipagebuttonlist$guilistentry[i + 1] : null;
            Gui gui = this.createEntry(guipagebuttonlist$guilistentry, 0, guipagebuttonlist$guilistentry1 == null);
            Gui gui1 = this.createEntry(guipagebuttonlist$guilistentry1, 160, guipagebuttonlist$guilistentry == null);
            GuiPageButtonList.GuiEntry guipagebuttonlist$guientry = new GuiPageButtonList.GuiEntry(gui, gui1);
            this.entries.add(guipagebuttonlist$guientry);
            if (guipagebuttonlist$guilistentry != null && gui != null) {
               this.componentMap.addKey(guipagebuttonlist$guilistentry.getId(), gui);
               if (gui instanceof GuiTextField) {
                  this.editBoxes.add((GuiTextField)gui);
               }
            }

            if (guipagebuttonlist$guilistentry1 != null && gui1 != null) {
               this.componentMap.addKey(guipagebuttonlist$guilistentry1.getId(), gui1);
               if (gui1 instanceof GuiTextField) {
                  this.editBoxes.add((GuiTextField)gui1);
               }
            }
         }
      }

   }

   private void populateEntries() {
      this.entries.clear();

      for(int i = 0; i < this.pages[this.page].length; i += 2) {
         GuiPageButtonList.GuiListEntry guipagebuttonlist$guilistentry = this.pages[this.page][i];
         GuiPageButtonList.GuiListEntry guipagebuttonlist$guilistentry1 = i < this.pages[this.page].length - 1 ? this.pages[this.page][i + 1] : null;
         Gui gui = (Gui)this.componentMap.lookup(guipagebuttonlist$guilistentry.getId());
         Gui gui1 = guipagebuttonlist$guilistentry1 != null ? (Gui)this.componentMap.lookup(guipagebuttonlist$guilistentry1.getId()) : null;
         GuiPageButtonList.GuiEntry guipagebuttonlist$guientry = new GuiPageButtonList.GuiEntry(gui, gui1);
         this.entries.add(guipagebuttonlist$guientry);
      }

   }

   public void setPage(int var1) {
      if (p_181156_1_ != this.page) {
         int i = this.page;
         this.page = p_181156_1_;
         this.populateEntries();
         this.markVisibility(i, p_181156_1_);
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
      return (Gui)this.componentMap.lookup(p_178061_1_);
   }

   private void markVisibility(int var1, int var2) {
      for(GuiPageButtonList.GuiListEntry guipagebuttonlist$guilistentry : this.pages[p_178060_1_]) {
         if (guipagebuttonlist$guilistentry != null) {
            this.setComponentVisibility((Gui)this.componentMap.lookup(guipagebuttonlist$guilistentry.getId()), false);
         }
      }

      for(GuiPageButtonList.GuiListEntry guipagebuttonlist$guilistentry1 : this.pages[p_178060_2_]) {
         if (guipagebuttonlist$guilistentry1 != null) {
            this.setComponentVisibility((Gui)this.componentMap.lookup(guipagebuttonlist$guilistentry1.getId()), true);
         }
      }

   }

   private void setComponentVisibility(Gui var1, boolean var2) {
      if (p_178066_1_ instanceof GuiButton) {
         ((GuiButton)p_178066_1_).visible = p_178066_2_;
      } else if (p_178066_1_ instanceof GuiTextField) {
         ((GuiTextField)p_178066_1_).setVisible(p_178066_2_);
      } else if (p_178066_1_ instanceof GuiLabel) {
         ((GuiLabel)p_178066_1_).visible = p_178066_2_;
      }

   }

   @Nullable
   private Gui createEntry(@Nullable GuiPageButtonList.GuiListEntry var1, int var2, boolean var3) {
      return (Gui)(p_178058_1_ instanceof GuiPageButtonList.GuiSlideEntry ? this.createSlider(this.width / 2 - 155 + p_178058_2_, 0, (GuiPageButtonList.GuiSlideEntry)p_178058_1_) : (p_178058_1_ instanceof GuiPageButtonList.GuiButtonEntry ? this.createButton(this.width / 2 - 155 + p_178058_2_, 0, (GuiPageButtonList.GuiButtonEntry)p_178058_1_) : (p_178058_1_ instanceof GuiPageButtonList.EditBoxEntry ? this.createTextField(this.width / 2 - 155 + p_178058_2_, 0, (GuiPageButtonList.EditBoxEntry)p_178058_1_) : (p_178058_1_ instanceof GuiPageButtonList.GuiLabelEntry ? this.createLabel(this.width / 2 - 155 + p_178058_2_, 0, (GuiPageButtonList.GuiLabelEntry)p_178058_1_, p_178058_3_) : null))));
   }

   public void setActive(boolean var1) {
      for(GuiPageButtonList.GuiEntry guipagebuttonlist$guientry : this.entries) {
         if (guipagebuttonlist$guientry.component1 instanceof GuiButton) {
            ((GuiButton)guipagebuttonlist$guientry.component1).enabled = p_181155_1_;
         }

         if (guipagebuttonlist$guientry.component2 instanceof GuiButton) {
            ((GuiButton)guipagebuttonlist$guientry.component2).enabled = p_181155_1_;
         }
      }

   }

   public boolean mouseClicked(int var1, int var2, int var3) {
      boolean flag = super.mouseClicked(mouseX, mouseY, mouseEvent);
      int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
      if (i >= 0) {
         GuiPageButtonList.GuiEntry guipagebuttonlist$guientry = this.getListEntry(i);
         if (this.focusedControl != guipagebuttonlist$guientry.focusedControl && this.focusedControl != null && this.focusedControl instanceof GuiTextField) {
            ((GuiTextField)this.focusedControl).setFocused(false);
         }

         this.focusedControl = guipagebuttonlist$guientry.focusedControl;
      }

      return flag;
   }

   private GuiSlider createSlider(int var1, int var2, GuiPageButtonList.GuiSlideEntry var3) {
      GuiSlider guislider = new GuiSlider(this.responder, p_178067_3_.getId(), p_178067_1_, p_178067_2_, p_178067_3_.getCaption(), p_178067_3_.getMinValue(), p_178067_3_.getMaxValue(), p_178067_3_.getInitalValue(), p_178067_3_.getFormatter());
      guislider.visible = p_178067_3_.shouldStartVisible();
      return guislider;
   }

   private GuiListButton createButton(int var1, int var2, GuiPageButtonList.GuiButtonEntry var3) {
      GuiListButton guilistbutton = new GuiListButton(this.responder, p_178065_3_.getId(), p_178065_1_, p_178065_2_, p_178065_3_.getCaption(), p_178065_3_.getInitialValue());
      guilistbutton.visible = p_178065_3_.shouldStartVisible();
      return guilistbutton;
   }

   private GuiTextField createTextField(int var1, int var2, GuiPageButtonList.EditBoxEntry var3) {
      GuiTextField guitextfield = new GuiTextField(p_178068_3_.getId(), this.mc.fontRendererObj, p_178068_1_, p_178068_2_, 150, 20);
      guitextfield.setText(p_178068_3_.getCaption());
      guitextfield.setGuiResponder(this.responder);
      guitextfield.setVisible(p_178068_3_.shouldStartVisible());
      guitextfield.setValidator(p_178068_3_.getFilter());
      return guitextfield;
   }

   private GuiLabel createLabel(int var1, int var2, GuiPageButtonList.GuiLabelEntry var3, boolean var4) {
      GuiLabel guilabel;
      if (p_178063_4_) {
         guilabel = new GuiLabel(this.mc.fontRendererObj, p_178063_3_.getId(), p_178063_1_, p_178063_2_, this.width - p_178063_1_ * 2, 20, -1);
      } else {
         guilabel = new GuiLabel(this.mc.fontRendererObj, p_178063_3_.getId(), p_178063_1_, p_178063_2_, 150, 20, -1);
      }

      guilabel.visible = p_178063_3_.shouldStartVisible();
      guilabel.addLine(p_178063_3_.getCaption());
      guilabel.setCentered();
      return guilabel;
   }

   public void onKeyPressed(char var1, int var2) {
      if (this.focusedControl instanceof GuiTextField) {
         GuiTextField guitextfield = (GuiTextField)this.focusedControl;
         if (!GuiScreen.isKeyComboCtrlV(p_178062_2_)) {
            if (p_178062_2_ == 15) {
               guitextfield.setFocused(false);
               int k = this.editBoxes.indexOf(this.focusedControl);
               if (GuiScreen.isShiftKeyDown()) {
                  if (k == 0) {
                     k = this.editBoxes.size() - 1;
                  } else {
                     --k;
                  }
               } else if (k == this.editBoxes.size() - 1) {
                  k = 0;
               } else {
                  ++k;
               }

               this.focusedControl = (Gui)this.editBoxes.get(k);
               guitextfield = (GuiTextField)this.focusedControl;
               guitextfield.setFocused(true);
               int l = guitextfield.yPosition + this.slotHeight;
               int i1 = guitextfield.yPosition;
               if (l > this.bottom) {
                  this.amountScrolled += (float)(l - this.bottom);
               } else if (i1 < this.top) {
                  this.amountScrolled = (float)i1;
               }
            } else {
               guitextfield.textboxKeyTyped(p_178062_1_, p_178062_2_);
            }
         } else {
            String s = GuiScreen.getClipboardString();
            String[] astring = s.split(";");
            int i = this.editBoxes.indexOf(this.focusedControl);
            int j = i;

            for(String s1 : astring) {
               ((GuiTextField)this.editBoxes.get(j)).setText(s1);
               if (j == this.editBoxes.size() - 1) {
                  j = 0;
               } else {
                  ++j;
               }

               if (j == i) {
                  break;
               }
            }
         }
      }

   }

   public GuiPageButtonList.GuiEntry getListEntry(int var1) {
      return (GuiPageButtonList.GuiEntry)this.entries.get(index);
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
         super(p_i45534_1_, p_i45534_2_, p_i45534_3_);
         this.filter = (Predicate)Objects.firstNonNull(p_i45534_4_, Predicates.alwaysTrue());
      }

      public Predicate getFilter() {
         return this.filter;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GuiButtonEntry extends GuiPageButtonList.GuiListEntry {
      private final boolean initialValue;

      public GuiButtonEntry(int var1, String var2, boolean var3, boolean var4) {
         super(p_i45535_1_, p_i45535_2_, p_i45535_3_);
         this.initialValue = p_i45535_4_;
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
         this.component1 = p_i45533_1_;
         this.component2 = p_i45533_2_;
      }

      public Gui getComponent1() {
         return this.component1;
      }

      public Gui getComponent2() {
         return this.component2;
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         this.renderComponent(this.component1, y, mouseX, mouseY, false);
         this.renderComponent(this.component2, y, mouseX, mouseY, false);
      }

      private void renderComponent(Gui var1, int var2, int var3, int var4, boolean var5) {
         if (p_178017_1_ != null) {
            if (p_178017_1_ instanceof GuiButton) {
               this.renderButton((GuiButton)p_178017_1_, p_178017_2_, p_178017_3_, p_178017_4_, p_178017_5_);
            } else if (p_178017_1_ instanceof GuiTextField) {
               this.renderTextField((GuiTextField)p_178017_1_, p_178017_2_, p_178017_5_);
            } else if (p_178017_1_ instanceof GuiLabel) {
               this.renderLabel((GuiLabel)p_178017_1_, p_178017_2_, p_178017_3_, p_178017_4_, p_178017_5_);
            }
         }

      }

      private void renderButton(GuiButton var1, int var2, int var3, int var4, boolean var5) {
         p_178024_1_.yPosition = p_178024_2_;
         if (!p_178024_5_) {
            p_178024_1_.drawButton(this.client, p_178024_3_, p_178024_4_);
         }

      }

      private void renderTextField(GuiTextField var1, int var2, boolean var3) {
         p_178027_1_.yPosition = p_178027_2_;
         if (!p_178027_3_) {
            p_178027_1_.drawTextBox();
         }

      }

      private void renderLabel(GuiLabel var1, int var2, int var3, int var4, boolean var5) {
         p_178025_1_.y = p_178025_2_;
         if (!p_178025_5_) {
            p_178025_1_.drawLabel(this.client, p_178025_3_, p_178025_4_);
         }

      }

      public void setSelected(int var1, int var2, int var3) {
         this.renderComponent(this.component1, p_178011_3_, 0, 0, true);
         this.renderComponent(this.component2, p_178011_3_, 0, 0, true);
      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         boolean flag = this.clickComponent(this.component1, mouseX, mouseY, mouseEvent);
         boolean flag1 = this.clickComponent(this.component2, mouseX, mouseY, mouseEvent);
         return flag || flag1;
      }

      private boolean clickComponent(Gui var1, int var2, int var3, int var4) {
         if (p_178026_1_ == null) {
            return false;
         } else if (p_178026_1_ instanceof GuiButton) {
            return this.clickButton((GuiButton)p_178026_1_, p_178026_2_, p_178026_3_, p_178026_4_);
         } else {
            if (p_178026_1_ instanceof GuiTextField) {
               this.clickTextField((GuiTextField)p_178026_1_, p_178026_2_, p_178026_3_, p_178026_4_);
            }

            return false;
         }
      }

      private boolean clickButton(GuiButton var1, int var2, int var3, int var4) {
         boolean flag = p_178023_1_.mousePressed(this.client, p_178023_2_, p_178023_3_);
         if (flag) {
            this.focusedControl = p_178023_1_;
         }

         return flag;
      }

      private void clickTextField(GuiTextField var1, int var2, int var3, int var4) {
         p_178018_1_.mouseClicked(p_178018_2_, p_178018_3_, p_178018_4_);
         if (p_178018_1_.isFocused()) {
            this.focusedControl = p_178018_1_;
         }

      }

      public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
         this.releaseComponent(this.component1, x, y, mouseEvent);
         this.releaseComponent(this.component2, x, y, mouseEvent);
      }

      private void releaseComponent(Gui var1, int var2, int var3, int var4) {
         if (p_178016_1_ != null && p_178016_1_ instanceof GuiButton) {
            this.releaseButton((GuiButton)p_178016_1_, p_178016_2_, p_178016_3_, p_178016_4_);
         }

      }

      private void releaseButton(GuiButton var1, int var2, int var3, int var4) {
         p_178019_1_.mouseReleased(p_178019_2_, p_178019_3_);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GuiLabelEntry extends GuiPageButtonList.GuiListEntry {
      public GuiLabelEntry(int var1, String var2, boolean var3) {
         super(p_i45532_1_, p_i45532_2_, p_i45532_3_);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GuiListEntry {
      private final int id;
      private final String caption;
      private final boolean startVisible;

      public GuiListEntry(int var1, String var2, boolean var3) {
         this.id = p_i45531_1_;
         this.caption = p_i45531_2_;
         this.startVisible = p_i45531_3_;
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
         super(p_i45530_1_, p_i45530_2_, p_i45530_3_);
         this.formatter = p_i45530_4_;
         this.minValue = p_i45530_5_;
         this.maxValue = p_i45530_6_;
         this.initialValue = p_i45530_7_;
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
