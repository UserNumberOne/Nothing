package net.minecraft.client.gui.inventory;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiBeacon extends GuiContainer {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation BEACON_GUI_TEXTURES = new ResourceLocation("textures/gui/container/beacon.png");
   private final IInventory tileBeacon;
   private GuiBeacon.ConfirmButton beaconConfirmButton;
   private boolean buttonsNotDrawn;

   public GuiBeacon(InventoryPlayer var1, IInventory var2) {
      super(new ContainerBeacon(var1, var2));
      this.tileBeacon = var2;
      this.xSize = 230;
      this.ySize = 219;
   }

   public void initGui() {
      super.initGui();
      this.beaconConfirmButton = new GuiBeacon.ConfirmButton(-1, this.guiLeft + 164, this.guiTop + 107);
      this.buttonList.add(this.beaconConfirmButton);
      this.buttonList.add(new GuiBeacon.CancelButton(-2, this.guiLeft + 190, this.guiTop + 107));
      this.buttonsNotDrawn = true;
      this.beaconConfirmButton.enabled = false;
   }

   public void updateScreen() {
      super.updateScreen();
      int var1 = this.tileBeacon.getField(0);
      Potion var2 = Potion.getPotionById(this.tileBeacon.getField(1));
      Potion var3 = Potion.getPotionById(this.tileBeacon.getField(2));
      if (this.buttonsNotDrawn && var1 >= 0) {
         this.buttonsNotDrawn = false;
         int var4 = 100;

         for(int var5 = 0; var5 <= 2; ++var5) {
            int var6 = TileEntityBeacon.EFFECTS_LIST[var5].length;
            int var7 = var6 * 22 + (var6 - 1) * 2;

            for(int var8 = 0; var8 < var6; ++var8) {
               Potion var9 = TileEntityBeacon.EFFECTS_LIST[var5][var8];
               GuiBeacon.PowerButton var10 = new GuiBeacon.PowerButton(var4++, this.guiLeft + 76 + var8 * 24 - var7 / 2, this.guiTop + 22 + var5 * 25, var9, var5);
               this.buttonList.add(var10);
               if (var5 >= var1) {
                  var10.enabled = false;
               } else if (var9 == var2) {
                  var10.setSelected(true);
               }
            }
         }

         boolean var12 = true;
         int var13 = TileEntityBeacon.EFFECTS_LIST[3].length + 1;
         int var14 = var13 * 22 + (var13 - 1) * 2;

         for(int var15 = 0; var15 < var13 - 1; ++var15) {
            Potion var17 = TileEntityBeacon.EFFECTS_LIST[3][var15];
            GuiBeacon.PowerButton var18 = new GuiBeacon.PowerButton(var4++, this.guiLeft + 167 + var15 * 24 - var14 / 2, this.guiTop + 47, var17, 3);
            this.buttonList.add(var18);
            if (3 >= var1) {
               var18.enabled = false;
            } else if (var17 == var3) {
               var18.setSelected(true);
            }
         }

         if (var2 != null) {
            GuiBeacon.PowerButton var16 = new GuiBeacon.PowerButton(var4++, this.guiLeft + 167 + (var13 - 1) * 24 - var14 / 2, this.guiTop + 47, var2, 3);
            this.buttonList.add(var16);
            if (3 >= var1) {
               var16.enabled = false;
            } else if (var2 == var3) {
               var16.setSelected(true);
            }
         }
      }

      this.beaconConfirmButton.enabled = this.tileBeacon.getStackInSlot(0) != null && var2 != null;
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == -2) {
         this.mc.player.connection.sendPacket(new CPacketCloseWindow(this.mc.player.openContainer.windowId));
         this.mc.displayGuiScreen((GuiScreen)null);
      } else if (var1.id == -1) {
         String var2 = "MC|Beacon";
         PacketBuffer var3 = new PacketBuffer(Unpooled.buffer());
         var3.writeInt(this.tileBeacon.getField(1));
         var3.writeInt(this.tileBeacon.getField(2));
         this.mc.getConnection().sendPacket(new CPacketCustomPayload("MC|Beacon", var3));
         this.mc.player.connection.sendPacket(new CPacketCloseWindow(this.mc.player.openContainer.windowId));
         this.mc.displayGuiScreen((GuiScreen)null);
      } else if (var1 instanceof GuiBeacon.PowerButton) {
         GuiBeacon.PowerButton var4 = (GuiBeacon.PowerButton)var1;
         if (var4.isSelected()) {
            return;
         }

         int var5 = Potion.getIdFromPotion(var4.effect);
         if (var4.tier < 3) {
            this.tileBeacon.setField(1, var5);
         } else {
            this.tileBeacon.setField(2, var5);
         }

         this.buttonList.clear();
         this.initGui();
         this.updateScreen();
      }

   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
      RenderHelper.disableStandardItemLighting();
      this.drawCenteredString(this.fontRendererObj, I18n.format("tile.beacon.primary"), 62, 10, 14737632);
      this.drawCenteredString(this.fontRendererObj, I18n.format("tile.beacon.secondary"), 169, 10, 14737632);

      for(GuiButton var4 : this.buttonList) {
         if (var4.isMouseOver()) {
            var4.drawButtonForegroundLayer(var1 - this.guiLeft, var2 - this.guiTop);
            break;
         }
      }

      RenderHelper.enableGUIStandardItemLighting();
   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(BEACON_GUI_TEXTURES);
      int var4 = (this.width - this.xSize) / 2;
      int var5 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var4, var5, 0, 0, this.xSize, this.ySize);
      this.itemRender.zLevel = 100.0F;
      this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.EMERALD), var4 + 42, var5 + 109);
      this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.DIAMOND), var4 + 42 + 22, var5 + 109);
      this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.GOLD_INGOT), var4 + 42 + 44, var5 + 109);
      this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.IRON_INGOT), var4 + 42 + 66, var5 + 109);
      this.itemRender.zLevel = 0.0F;
   }

   @SideOnly(Side.CLIENT)
   static class Button extends GuiButton {
      private final ResourceLocation iconTexture;
      private final int iconX;
      private final int iconY;
      private boolean selected;

      protected Button(int var1, int var2, int var3, ResourceLocation var4, int var5, int var6) {
         super(var1, var2, var3, 22, 22, "");
         this.iconTexture = var4;
         this.iconX = var5;
         this.iconY = var6;
      }

      public void drawButton(Minecraft var1, int var2, int var3) {
         if (this.visible) {
            var1.getTextureManager().bindTexture(GuiBeacon.BEACON_GUI_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = var2 >= this.xPosition && var3 >= this.yPosition && var2 < this.xPosition + this.width && var3 < this.yPosition + this.height;
            boolean var4 = true;
            int var5 = 0;
            if (!this.enabled) {
               var5 += this.width * 2;
            } else if (this.selected) {
               var5 += this.width * 1;
            } else if (this.hovered) {
               var5 += this.width * 3;
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, var5, 219, this.width, this.height);
            if (!GuiBeacon.BEACON_GUI_TEXTURES.equals(this.iconTexture)) {
               var1.getTextureManager().bindTexture(this.iconTexture);
            }

            this.drawTexturedModalRect(this.xPosition + 2, this.yPosition + 2, this.iconX, this.iconY, 18, 18);
         }

      }

      public boolean isSelected() {
         return this.selected;
      }

      public void setSelected(boolean var1) {
         this.selected = var1;
      }
   }

   @SideOnly(Side.CLIENT)
   class CancelButton extends GuiBeacon.Button {
      public CancelButton(int var2, int var3, int var4) {
         super(var2, var3, var4, GuiBeacon.BEACON_GUI_TEXTURES, 112, 220);
      }

      public void drawButtonForegroundLayer(int var1, int var2) {
         GuiBeacon.this.drawCreativeTabHoveringText(I18n.format("gui.cancel"), var1, var2);
      }
   }

   @SideOnly(Side.CLIENT)
   class ConfirmButton extends GuiBeacon.Button {
      public ConfirmButton(int var2, int var3, int var4) {
         super(var2, var3, var4, GuiBeacon.BEACON_GUI_TEXTURES, 90, 220);
      }

      public void drawButtonForegroundLayer(int var1, int var2) {
         GuiBeacon.this.drawCreativeTabHoveringText(I18n.format("gui.done"), var1, var2);
      }
   }

   @SideOnly(Side.CLIENT)
   class PowerButton extends GuiBeacon.Button {
      private final Potion effect;
      private final int tier;

      public PowerButton(int var2, int var3, int var4, Potion var5, int var6) {
         super(var2, var3, var4, GuiContainer.INVENTORY_BACKGROUND, var5.getStatusIconIndex() % 8 * 18, 198 + var5.getStatusIconIndex() / 8 * 18);
         this.effect = var5;
         this.tier = var6;
      }

      public void drawButtonForegroundLayer(int var1, int var2) {
         String var3 = I18n.format(this.effect.getName());
         if (this.tier >= 3 && this.effect != MobEffects.REGENERATION) {
            var3 = var3 + " II";
         }

         GuiBeacon.this.drawCreativeTabHoveringText(var3, var1, var2);
      }
   }
}
