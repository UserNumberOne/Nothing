package net.minecraft.client.renderer;

import com.google.common.collect.Ordering;
import java.util.Collection;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.GuiScreenEvent.PotionShiftEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class InventoryEffectRenderer extends GuiContainer {
   private boolean hasActivePotionEffects;

   public InventoryEffectRenderer(Container var1) {
      super(var1);
   }

   public void initGui() {
      super.initGui();
      this.updateActivePotionEffects();
   }

   protected void updateActivePotionEffects() {
      boolean var1 = false;

      for(PotionEffect var3 : this.mc.player.getActivePotionEffects()) {
         Potion var4 = var3.getPotion();
         if (var4.shouldRender(var3)) {
            var1 = true;
            break;
         }
      }

      if (!this.mc.player.getActivePotionEffects().isEmpty() && var1) {
         if (MinecraftForge.EVENT_BUS.post(new PotionShiftEvent(this))) {
            this.guiLeft = (this.width - this.xSize) / 2;
         } else {
            this.guiLeft = 160 + (this.width - this.xSize - 200) / 2;
         }

         this.hasActivePotionEffects = true;
      } else {
         this.guiLeft = (this.width - this.xSize) / 2;
         this.hasActivePotionEffects = false;
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      if (this.hasActivePotionEffects) {
         this.drawActivePotionEffects();
      }

   }

   private void drawActivePotionEffects() {
      int var1 = this.guiLeft - 124;
      int var2 = this.guiTop;
      boolean var3 = true;
      Collection var4 = this.mc.player.getActivePotionEffects();
      if (!var4.isEmpty()) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableLighting();
         int var5 = 33;
         if (var4.size() > 5) {
            var5 = 132 / (var4.size() - 1);
         }

         for(PotionEffect var7 : Ordering.natural().sortedCopy(var4)) {
            Potion var8 = var7.getPotion();
            if (var8.shouldRender(var7)) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
               this.drawTexturedModalRect(var1, var2, 0, 166, 140, 32);
               if (var8.hasStatusIcon()) {
                  int var9 = var8.getStatusIconIndex();
                  this.drawTexturedModalRect(var1 + 6, var2 + 7, 0 + var9 % 8 * 18, 198 + var9 / 8 * 18, 18, 18);
               }

               var8.renderInventoryEffect(var1, var2, var7, this.mc);
               if (!var8.shouldRenderInvText(var7)) {
                  var2 += var5;
               } else {
                  String var11 = I18n.format(var8.getName());
                  if (var7.getAmplifier() == 1) {
                     var11 = var11 + " " + I18n.format("enchantment.level.2");
                  } else if (var7.getAmplifier() == 2) {
                     var11 = var11 + " " + I18n.format("enchantment.level.3");
                  } else if (var7.getAmplifier() == 3) {
                     var11 = var11 + " " + I18n.format("enchantment.level.4");
                  }

                  this.fontRendererObj.drawStringWithShadow(var11, (float)(var1 + 10 + 18), (float)(var2 + 6), 16777215);
                  String var10 = Potion.getPotionDurationString(var7, 1.0F);
                  this.fontRendererObj.drawStringWithShadow(var10, (float)(var1 + 10 + 18), (float)(var2 + 6 + 10), 8355711);
                  var2 += var5;
               }
            }
         }
      }

   }
}
