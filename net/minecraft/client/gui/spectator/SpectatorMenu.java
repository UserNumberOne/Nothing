package net.minecraft.client.gui.spectator;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.categories.SpectatorDetails;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpectatorMenu {
   private static final ISpectatorMenuObject CLOSE_ITEM = new SpectatorMenu.EndSpectatorObject();
   private static final ISpectatorMenuObject SCROLL_LEFT = new SpectatorMenu.MoveMenuObject(-1, true);
   private static final ISpectatorMenuObject SCROLL_RIGHT_ENABLED = new SpectatorMenu.MoveMenuObject(1, true);
   private static final ISpectatorMenuObject SCROLL_RIGHT_DISABLED = new SpectatorMenu.MoveMenuObject(1, false);
   public static final ISpectatorMenuObject EMPTY_SLOT = new ISpectatorMenuObject() {
      public void selectItem(SpectatorMenu var1) {
      }

      public ITextComponent getSpectatorName() {
         return new TextComponentString("");
      }

      public void renderIcon(float var1, int var2) {
      }

      public boolean isEnabled() {
         return false;
      }
   };
   private final ISpectatorMenuRecipient listener;
   private final List previousCategories = Lists.newArrayList();
   private ISpectatorMenuView category = new BaseSpectatorGroup();
   private int selectedSlot = -1;
   private int page;

   public SpectatorMenu(ISpectatorMenuRecipient var1) {
      this.listener = var1;
   }

   public ISpectatorMenuObject getItem(int var1) {
      int var2 = var1 + this.page * 6;
      return this.page > 0 && var1 == 0 ? SCROLL_LEFT : (var1 == 7 ? (var2 < this.category.getItems().size() ? SCROLL_RIGHT_ENABLED : SCROLL_RIGHT_DISABLED) : (var1 == 8 ? CLOSE_ITEM : (var2 >= 0 && var2 < this.category.getItems().size() ? (ISpectatorMenuObject)Objects.firstNonNull(this.category.getItems().get(var2), EMPTY_SLOT) : EMPTY_SLOT)));
   }

   public List getItems() {
      ArrayList var1 = Lists.newArrayList();

      for(int var2 = 0; var2 <= 8; ++var2) {
         var1.add(this.getItem(var2));
      }

      return var1;
   }

   public ISpectatorMenuObject getSelectedItem() {
      return this.getItem(this.selectedSlot);
   }

   public ISpectatorMenuView getSelectedCategory() {
      return this.category;
   }

   public void selectSlot(int var1) {
      ISpectatorMenuObject var2 = this.getItem(var1);
      if (var2 != EMPTY_SLOT) {
         if (this.selectedSlot == var1 && var2.isEnabled()) {
            var2.selectItem(this);
         } else {
            this.selectedSlot = var1;
         }
      }

   }

   public void exit() {
      this.listener.onSpectatorMenuClosed(this);
   }

   public int getSelectedSlot() {
      return this.selectedSlot;
   }

   public void selectCategory(ISpectatorMenuView var1) {
      this.previousCategories.add(this.getCurrentPage());
      this.category = var1;
      this.selectedSlot = -1;
      this.page = 0;
   }

   public SpectatorDetails getCurrentPage() {
      return new SpectatorDetails(this.category, this.getItems(), this.selectedSlot);
   }

   @SideOnly(Side.CLIENT)
   static class EndSpectatorObject implements ISpectatorMenuObject {
      private EndSpectatorObject() {
      }

      public void selectItem(SpectatorMenu var1) {
         var1.exit();
      }

      public ITextComponent getSpectatorName() {
         return new TextComponentString("Close menu");
      }

      public void renderIcon(float var1, int var2) {
         Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.SPECTATOR_WIDGETS);
         Gui.drawModalRectWithCustomSizedTexture(0, 0, 128.0F, 0.0F, 16, 16, 256.0F, 256.0F);
      }

      public boolean isEnabled() {
         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   static class MoveMenuObject implements ISpectatorMenuObject {
      private final int direction;
      private final boolean enabled;

      public MoveMenuObject(int var1, boolean var2) {
         this.direction = var1;
         this.enabled = var2;
      }

      public void selectItem(SpectatorMenu var1) {
         var1.page = this.direction;
      }

      public ITextComponent getSpectatorName() {
         return this.direction < 0 ? new TextComponentString("Previous Page") : new TextComponentString("Next Page");
      }

      public void renderIcon(float var1, int var2) {
         Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.SPECTATOR_WIDGETS);
         if (this.direction < 0) {
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 144.0F, 0.0F, 16, 16, 256.0F, 256.0F);
         } else {
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 160.0F, 0.0F, 16, 16, 256.0F, 256.0F);
         }

      }

      public boolean isEnabled() {
         return this.enabled;
      }
   }
}
