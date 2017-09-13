package net.minecraft.client.resources;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ResourcePackListEntry implements GuiListExtended.IGuiListEntry {
   private static final ResourceLocation RESOURCE_PACKS_TEXTURE = new ResourceLocation("textures/gui/resource_packs.png");
   private static final ITextComponent INCOMPATIBLE = new TextComponentTranslation("resourcePack.incompatible", new Object[0]);
   private static final ITextComponent INCOMPATIBLE_OLD = new TextComponentTranslation("resourcePack.incompatible.old", new Object[0]);
   private static final ITextComponent INCOMPATIBLE_NEW = new TextComponentTranslation("resourcePack.incompatible.new", new Object[0]);
   protected final Minecraft mc;
   protected final GuiScreenResourcePacks resourcePacksGUI;

   public ResourcePackListEntry(GuiScreenResourcePacks var1) {
      this.resourcePacksGUI = var1;
      this.mc = Minecraft.getMinecraft();
   }

   public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
      int var9 = this.getResourcePackFormat();
      if (var9 != 2) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         Gui.drawRect(var2 - 1, var3 - 1, var2 + var4 - 9, var3 + var5 + 1, -8978432);
      }

      this.bindResourcePackIcon();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      Gui.drawModalRectWithCustomSizedTexture(var2, var3, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
      String var10 = this.getResourcePackName();
      String var11 = this.getResourcePackDescription();
      if (this.showHoverOverlay() && (this.mc.gameSettings.touchscreen || var8)) {
         this.mc.getTextureManager().bindTexture(RESOURCE_PACKS_TEXTURE);
         Gui.drawRect(var2, var3, var2 + 32, var3 + 32, -1601138544);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         int var12 = var6 - var2;
         int var13 = var7 - var3;
         if (var9 < 2) {
            var10 = INCOMPATIBLE.getFormattedText();
            var11 = INCOMPATIBLE_OLD.getFormattedText();
         } else if (var9 > 2) {
            var10 = INCOMPATIBLE.getFormattedText();
            var11 = INCOMPATIBLE_NEW.getFormattedText();
         }

         if (this.canMoveRight()) {
            if (var12 < 32) {
               Gui.drawModalRectWithCustomSizedTexture(var2, var3, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
            } else {
               Gui.drawModalRectWithCustomSizedTexture(var2, var3, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
            }
         } else {
            if (this.canMoveLeft()) {
               if (var12 < 16) {
                  Gui.drawModalRectWithCustomSizedTexture(var2, var3, 32.0F, 32.0F, 32, 32, 256.0F, 256.0F);
               } else {
                  Gui.drawModalRectWithCustomSizedTexture(var2, var3, 32.0F, 0.0F, 32, 32, 256.0F, 256.0F);
               }
            }

            if (this.canMoveUp()) {
               if (var12 < 32 && var12 > 16 && var13 < 16) {
                  Gui.drawModalRectWithCustomSizedTexture(var2, var3, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
               } else {
                  Gui.drawModalRectWithCustomSizedTexture(var2, var3, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
               }
            }

            if (this.canMoveDown()) {
               if (var12 < 32 && var12 > 16 && var13 > 16) {
                  Gui.drawModalRectWithCustomSizedTexture(var2, var3, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
               } else {
                  Gui.drawModalRectWithCustomSizedTexture(var2, var3, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
               }
            }
         }
      }

      int var15 = this.mc.fontRendererObj.getStringWidth(var10);
      if (var15 > 157) {
         var10 = this.mc.fontRendererObj.trimStringToWidth(var10, 157 - this.mc.fontRendererObj.getStringWidth("...")) + "...";
      }

      this.mc.fontRendererObj.drawStringWithShadow(var10, (float)(var2 + 32 + 2), (float)(var3 + 1), 16777215);
      List var16 = this.mc.fontRendererObj.listFormattedStringToWidth(var11, 157);

      for(int var14 = 0; var14 < 2 && var14 < var16.size(); ++var14) {
         this.mc.fontRendererObj.drawStringWithShadow((String)var16.get(var14), (float)(var2 + 32 + 2), (float)(var3 + 12 + 10 * var14), 8421504);
      }

   }

   protected abstract int getResourcePackFormat();

   protected abstract String getResourcePackDescription();

   protected abstract String getResourcePackName();

   protected abstract void bindResourcePackIcon();

   protected boolean showHoverOverlay() {
      return true;
   }

   protected boolean canMoveRight() {
      return !this.resourcePacksGUI.hasResourcePackEntry(this);
   }

   protected boolean canMoveLeft() {
      return this.resourcePacksGUI.hasResourcePackEntry(this);
   }

   protected boolean canMoveUp() {
      List var1 = this.resourcePacksGUI.getListContaining(this);
      int var2 = var1.indexOf(this);
      return var2 > 0 && ((ResourcePackListEntry)var1.get(var2 - 1)).showHoverOverlay();
   }

   protected boolean canMoveDown() {
      List var1 = this.resourcePacksGUI.getListContaining(this);
      int var2 = var1.indexOf(this);
      return var2 >= 0 && var2 < var1.size() - 1 && ((ResourcePackListEntry)var1.get(var2 + 1)).showHoverOverlay();
   }

   public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (this.showHoverOverlay() && var5 <= 32) {
         if (this.canMoveRight()) {
            this.resourcePacksGUI.markChanged();
            final int var12 = ((ResourcePackListEntry)this.resourcePacksGUI.getSelectedResourcePacks().get(0)).isServerPack() ? 1 : 0;
            int var14 = this.getResourcePackFormat();
            if (var14 == 2) {
               this.resourcePacksGUI.getListContaining(this).remove(this);
               this.resourcePacksGUI.getSelectedResourcePacks().add(var12, this);
            } else {
               String var9 = I18n.format("resourcePack.incompatible.confirm.title");
               String var10 = I18n.format("resourcePack.incompatible.confirm." + (var14 > 2 ? "new" : "old"));
               this.mc.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback() {
                  public void confirmClicked(boolean var1, int var2) {
                     List var3 = ResourcePackListEntry.this.resourcePacksGUI.getListContaining(ResourcePackListEntry.this);
                     ResourcePackListEntry.this.mc.displayGuiScreen(ResourcePackListEntry.this.resourcePacksGUI);
                     if (var1) {
                        var3.remove(ResourcePackListEntry.this);
                        ResourcePackListEntry.this.resourcePacksGUI.getSelectedResourcePacks().add(var12, ResourcePackListEntry.this);
                     }

                  }
               }, var9, var10, 0));
            }

            return true;
         }

         if (var5 < 16 && this.canMoveLeft()) {
            this.resourcePacksGUI.getListContaining(this).remove(this);
            this.resourcePacksGUI.getAvailableResourcePacks().add(0, this);
            this.resourcePacksGUI.markChanged();
            return true;
         }

         if (var5 > 16 && var6 < 16 && this.canMoveUp()) {
            List var11 = this.resourcePacksGUI.getListContaining(this);
            int var13 = var11.indexOf(this);
            var11.remove(this);
            var11.add(var13 - 1, this);
            this.resourcePacksGUI.markChanged();
            return true;
         }

         if (var5 > 16 && var6 > 16 && this.canMoveDown()) {
            List var7 = this.resourcePacksGUI.getListContaining(this);
            int var8 = var7.indexOf(this);
            var7.remove(this);
            var7.add(var8 + 1, this);
            this.resourcePacksGUI.markChanged();
            return true;
         }
      }

      return false;
   }

   public void setSelected(int var1, int var2, int var3) {
   }

   public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   public boolean isServerPack() {
      return false;
   }
}
