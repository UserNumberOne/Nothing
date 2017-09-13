package net.minecraft.client.gui.inventory;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class GuiContainerCreative extends InventoryEffectRenderer {
   private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
   private static final InventoryBasic basicInventory = new InventoryBasic("tmp", true, 45);
   private static int selectedTabIndex = CreativeTabs.BUILDING_BLOCKS.getTabIndex();
   private float currentScroll;
   private boolean isScrolling;
   private boolean wasClicking;
   private GuiTextField searchField;
   private List originalSlots;
   private Slot destroyItemSlot;
   private boolean clearSearch;
   private CreativeCrafting listener;
   private static int tabPage = 0;
   private int maxPages = 0;

   public GuiContainerCreative(EntityPlayer var1) {
      super(new GuiContainerCreative.ContainerCreative(var1));
      var1.openContainer = this.inventorySlots;
      this.allowUserInput = true;
      this.ySize = 136;
      this.xSize = 195;
   }

   public void updateScreen() {
      if (!this.mc.playerController.isInCreativeMode()) {
         this.mc.displayGuiScreen(new GuiInventory(this.mc.player));
      }

   }

   protected void handleMouseClick(Slot var1, int var2, int var3, ClickType var4) {
      this.clearSearch = true;
      boolean var5 = var4 == ClickType.QUICK_MOVE;
      var4 = var2 == -999 && var4 == ClickType.PICKUP ? ClickType.THROW : var4;
      if (var1 == null && selectedTabIndex != CreativeTabs.INVENTORY.getTabIndex() && var4 != ClickType.QUICK_CRAFT) {
         InventoryPlayer var15 = this.mc.player.inventory;
         if (var15.getItemStack() != null) {
            if (var3 == 0) {
               this.mc.player.dropItem(var15.getItemStack(), true);
               this.mc.playerController.sendPacketDropItem(var15.getItemStack());
               var15.setItemStack((ItemStack)null);
            }

            if (var3 == 1) {
               ItemStack var17 = var15.getItemStack().splitStack(1);
               this.mc.player.dropItem(var17, true);
               this.mc.playerController.sendPacketDropItem(var17);
               if (var15.getItemStack().stackSize == 0) {
                  var15.setItemStack((ItemStack)null);
               }
            }
         }
      } else if (var1 == this.destroyItemSlot && var5) {
         for(int var14 = 0; var14 < this.mc.player.inventoryContainer.getInventory().size(); ++var14) {
            this.mc.playerController.sendSlotPacket((ItemStack)null, var14);
         }
      } else if (selectedTabIndex == CreativeTabs.INVENTORY.getTabIndex()) {
         if (var1 == this.destroyItemSlot) {
            this.mc.player.inventory.setItemStack((ItemStack)null);
         } else if (var4 == ClickType.THROW && var1 != null && var1.getHasStack()) {
            ItemStack var6 = var1.decrStackSize(var3 == 0 ? 1 : var1.getStack().getMaxStackSize());
            this.mc.player.dropItem(var6, true);
            this.mc.playerController.sendPacketDropItem(var6);
         } else if (var4 == ClickType.THROW && this.mc.player.inventory.getItemStack() != null) {
            this.mc.player.dropItem(this.mc.player.inventory.getItemStack(), true);
            this.mc.playerController.sendPacketDropItem(this.mc.player.inventory.getItemStack());
            this.mc.player.inventory.setItemStack((ItemStack)null);
         } else {
            this.mc.player.inventoryContainer.slotClick(var1 == null ? var2 : ((GuiContainerCreative.CreativeSlot)var1).slot.slotNumber, var3, var4, this.mc.player);
            this.mc.player.inventoryContainer.detectAndSendChanges();
         }
      } else if (var4 != ClickType.QUICK_CRAFT && var1.inventory == basicInventory) {
         InventoryPlayer var13 = this.mc.player.inventory;
         ItemStack var7 = var13.getItemStack();
         ItemStack var8 = var1.getStack();
         if (var4 == ClickType.SWAP) {
            if (var8 != null && var3 >= 0 && var3 < 9) {
               ItemStack var19 = var8.copy();
               var19.stackSize = var19.getMaxStackSize();
               this.mc.player.inventory.setInventorySlotContents(var3, var19);
               this.mc.player.inventoryContainer.detectAndSendChanges();
            }

            return;
         }

         if (var4 == ClickType.CLONE) {
            if (var13.getItemStack() == null && var1.getHasStack()) {
               ItemStack var18 = var1.getStack().copy();
               var18.stackSize = var18.getMaxStackSize();
               var13.setItemStack(var18);
            }

            return;
         }

         if (var4 == ClickType.THROW) {
            if (var8 != null) {
               ItemStack var9 = var8.copy();
               var9.stackSize = var3 == 0 ? 1 : var9.getMaxStackSize();
               this.mc.player.dropItem(var9, true);
               this.mc.playerController.sendPacketDropItem(var9);
            }

            return;
         }

         if (var7 != null && var8 != null && var7.isItemEqual(var8) && ItemStack.areItemStackTagsEqual(var7, var8)) {
            if (var3 == 0) {
               if (var5) {
                  var7.stackSize = var7.getMaxStackSize();
               } else if (var7.stackSize < var7.getMaxStackSize()) {
                  ++var7.stackSize;
               }
            } else if (var7.stackSize <= 1) {
               var13.setItemStack((ItemStack)null);
            } else {
               --var7.stackSize;
            }
         } else if (var8 != null && var7 == null) {
            var13.setItemStack(ItemStack.copyItemStack(var8));
            var7 = var13.getItemStack();
            if (var5) {
               var7.stackSize = var7.getMaxStackSize();
            }
         } else {
            var13.setItemStack((ItemStack)null);
         }
      } else {
         this.inventorySlots.slotClick(var1 == null ? var2 : var1.slotNumber, var3, var4, this.mc.player);
         if (Container.getDragEvent(var3) == 2) {
            for(int var11 = 0; var11 < 9; ++var11) {
               this.mc.playerController.sendSlotPacket(this.inventorySlots.getSlot(45 + var11).getStack(), 36 + var11);
            }
         } else if (var1 != null) {
            ItemStack var12 = this.inventorySlots.getSlot(var1.slotNumber).getStack();
            this.mc.playerController.sendSlotPacket(var12, var1.slotNumber - this.inventorySlots.inventorySlots.size() + 9 + 36);
         }
      }

   }

   protected void updateActivePotionEffects() {
      int var1 = this.guiLeft;
      super.updateActivePotionEffects();
      if (this.searchField != null && this.guiLeft != var1) {
         this.searchField.xPosition = this.guiLeft + 82;
      }

   }

   public void initGui() {
      if (this.mc.playerController.isInCreativeMode()) {
         super.initGui();
         this.buttonList.clear();
         Keyboard.enableRepeatEvents(true);
         this.searchField = new GuiTextField(0, this.fontRendererObj, this.guiLeft + 82, this.guiTop + 6, 89, this.fontRendererObj.FONT_HEIGHT);
         this.searchField.setMaxStringLength(15);
         this.searchField.setEnableBackgroundDrawing(false);
         this.searchField.setVisible(false);
         this.searchField.setTextColor(16777215);
         int var1 = selectedTabIndex;
         selectedTabIndex = -1;
         this.setCurrentCreativeTab(CreativeTabs.CREATIVE_TAB_ARRAY[var1]);
         this.listener = new CreativeCrafting(this.mc);
         this.mc.player.inventoryContainer.addListener(this.listener);
         int var2 = CreativeTabs.CREATIVE_TAB_ARRAY.length;
         if (var2 > 12) {
            this.buttonList.add(new GuiButton(101, this.guiLeft, this.guiTop - 50, 20, 20, "<"));
            this.buttonList.add(new GuiButton(102, this.guiLeft + this.xSize - 20, this.guiTop - 50, 20, 20, ">"));
            this.maxPages = (var2 - 12) / 10 + 1;
         }
      } else {
         this.mc.displayGuiScreen(new GuiInventory(this.mc.player));
      }

   }

   public void onGuiClosed() {
      super.onGuiClosed();
      if (this.mc.player != null && this.mc.player.inventory != null) {
         this.mc.player.inventoryContainer.removeListener(this.listener);
      }

      Keyboard.enableRepeatEvents(false);
   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (!CreativeTabs.CREATIVE_TAB_ARRAY[selectedTabIndex].hasSearchBar()) {
         if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindChat)) {
            this.setCurrentCreativeTab(CreativeTabs.SEARCH);
         } else {
            super.keyTyped(var1, var2);
         }
      } else {
         if (this.clearSearch) {
            this.clearSearch = false;
            this.searchField.setText("");
         }

         if (!this.checkHotbarKeys(var2)) {
            if (this.searchField.textboxKeyTyped(var1, var2)) {
               this.updateCreativeSearch();
            } else {
               super.keyTyped(var1, var2);
            }
         }
      }

   }

   private void updateCreativeSearch() {
      GuiContainerCreative.ContainerCreative var1 = (GuiContainerCreative.ContainerCreative)this.inventorySlots;
      var1.itemList.clear();
      CreativeTabs var2 = CreativeTabs.CREATIVE_TAB_ARRAY[selectedTabIndex];
      if (var2.hasSearchBar() && var2 != CreativeTabs.SEARCH) {
         var2.displayAllRelevantItems(var1.itemList);
         this.updateFilteredItems(var1);
      } else {
         for(Item var4 : Item.REGISTRY) {
            if (var4 != null && var4.getCreativeTab() != null) {
               var4.getSubItems(var4, (CreativeTabs)null, var1.itemList);
            }
         }

         this.updateFilteredItems(var1);
      }
   }

   private void updateFilteredItems(GuiContainerCreative.ContainerCreative var1) {
      if (CreativeTabs.CREATIVE_TAB_ARRAY[selectedTabIndex] == CreativeTabs.SEARCH) {
         for(Enchantment var3 : Enchantment.REGISTRY) {
            if (var3 != null && var3.type != null) {
               Items.ENCHANTED_BOOK.getAll(var3, var1.itemList);
            }
         }
      }

      Iterator var8 = var1.itemList.iterator();
      String var9 = this.searchField.getText().toLowerCase();

      while(var8.hasNext()) {
         ItemStack var4 = (ItemStack)var8.next();
         boolean var5 = false;

         for(String var7 : var4.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips)) {
            if (TextFormatting.getTextWithoutFormattingCodes(var7).toLowerCase().contains(var9)) {
               var5 = true;
               break;
            }
         }

         if (!var5) {
            var8.remove();
         }
      }

      this.currentScroll = 0.0F;
      var1.scrollTo(0.0F);
   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
      CreativeTabs var3 = CreativeTabs.CREATIVE_TAB_ARRAY[selectedTabIndex];
      if (var3 != null && var3.drawInForegroundOfTab()) {
         GlStateManager.disableBlend();
         this.fontRendererObj.drawString(I18n.format(var3.getTranslatedTabLabel()), 8, 6, 4210752);
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (var3 == 0) {
         int var4 = var1 - this.guiLeft;
         int var5 = var2 - this.guiTop;

         for(CreativeTabs var9 : CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (this.isMouseOverTab(var9, var4, var5)) {
               return;
            }
         }
      }

      super.mouseClicked(var1, var2, var3);
   }

   protected void mouseReleased(int var1, int var2, int var3) {
      if (var3 == 0) {
         int var4 = var1 - this.guiLeft;
         int var5 = var2 - this.guiTop;

         for(CreativeTabs var9 : CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (var9 != null && this.isMouseOverTab(var9, var4, var5)) {
               this.setCurrentCreativeTab(var9);
               return;
            }
         }
      }

      super.mouseReleased(var1, var2, var3);
   }

   private boolean needsScrollBars() {
      if (CreativeTabs.CREATIVE_TAB_ARRAY[selectedTabIndex] == null) {
         return false;
      } else {
         return selectedTabIndex != CreativeTabs.INVENTORY.getTabIndex() && CreativeTabs.CREATIVE_TAB_ARRAY[selectedTabIndex].shouldHidePlayerInventory() && ((GuiContainerCreative.ContainerCreative)this.inventorySlots).canScroll();
      }
   }

   private void setCurrentCreativeTab(CreativeTabs var1) {
      if (var1 != null) {
         int var2 = selectedTabIndex;
         selectedTabIndex = var1.getTabIndex();
         GuiContainerCreative.ContainerCreative var3 = (GuiContainerCreative.ContainerCreative)this.inventorySlots;
         this.dragSplittingSlots.clear();
         var3.itemList.clear();
         var1.displayAllRelevantItems(var3.itemList);
         if (var1 == CreativeTabs.INVENTORY) {
            Container var4 = this.mc.player.inventoryContainer;
            if (this.originalSlots == null) {
               this.originalSlots = var3.inventorySlots;
            }

            var3.inventorySlots = Lists.newArrayList();

            for(int var5 = 0; var5 < var4.inventorySlots.size(); ++var5) {
               GuiContainerCreative.CreativeSlot var6 = new GuiContainerCreative.CreativeSlot((Slot)var4.inventorySlots.get(var5), var5);
               var3.inventorySlots.add(var6);
               if (var5 >= 5 && var5 < 9) {
                  int var10 = var5 - 5;
                  int var11 = var10 / 2;
                  int var12 = var10 % 2;
                  var6.xPos = 54 + var11 * 54;
                  var6.yPos = 6 + var12 * 27;
               } else if (var5 >= 0 && var5 < 5) {
                  var6.xPos = -2000;
                  var6.yPos = -2000;
               } else if (var5 == 45) {
                  var6.xPos = 35;
                  var6.yPos = 20;
               } else if (var5 < var4.inventorySlots.size()) {
                  int var7 = var5 - 9;
                  int var8 = var7 % 9;
                  int var9 = var7 / 9;
                  var6.xPos = 9 + var8 * 18;
                  if (var5 >= 36) {
                     var6.yPos = 112;
                  } else {
                     var6.yPos = 54 + var9 * 18;
                  }
               }
            }

            this.destroyItemSlot = new Slot(basicInventory, 0, 173, 112);
            var3.inventorySlots.add(this.destroyItemSlot);
         } else if (var2 == CreativeTabs.INVENTORY.getTabIndex()) {
            var3.inventorySlots = this.originalSlots;
            this.originalSlots = null;
         }

         if (this.searchField != null) {
            if (var1.hasSearchBar()) {
               this.searchField.setVisible(true);
               this.searchField.setCanLoseFocus(false);
               this.searchField.setFocused(true);
               this.searchField.setText("");
               this.searchField.width = var1.getSearchbarWidth();
               this.searchField.xPosition = this.guiLeft + 171 - this.searchField.width;
               this.updateCreativeSearch();
            } else {
               this.searchField.setVisible(false);
               this.searchField.setCanLoseFocus(true);
               this.searchField.setFocused(false);
            }
         }

         this.currentScroll = 0.0F;
         var3.scrollTo(0.0F);
      }
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int var1 = Mouse.getEventDWheel();
      if (var1 != 0 && this.needsScrollBars()) {
         int var2 = (((GuiContainerCreative.ContainerCreative)this.inventorySlots).itemList.size() + 9 - 1) / 9 - 5;
         if (var1 > 0) {
            var1 = 1;
         }

         if (var1 < 0) {
            var1 = -1;
         }

         this.currentScroll = (float)((double)this.currentScroll - (double)var1 / (double)var2);
         this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
         ((GuiContainerCreative.ContainerCreative)this.inventorySlots).scrollTo(this.currentScroll);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      boolean var4 = Mouse.isButtonDown(0);
      int var5 = this.guiLeft;
      int var6 = this.guiTop;
      int var7 = var5 + 175;
      int var8 = var6 + 18;
      int var9 = var7 + 14;
      int var10 = var8 + 112;
      if (!this.wasClicking && var4 && var1 >= var7 && var2 >= var8 && var1 < var9 && var2 < var10) {
         this.isScrolling = this.needsScrollBars();
      }

      if (!var4) {
         this.isScrolling = false;
      }

      this.wasClicking = var4;
      if (this.isScrolling) {
         this.currentScroll = ((float)(var2 - var8) - 7.5F) / ((float)(var10 - var8) - 15.0F);
         this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
         ((GuiContainerCreative.ContainerCreative)this.inventorySlots).scrollTo(this.currentScroll);
      }

      super.drawScreen(var1, var2, var3);
      int var11 = tabPage * 10;
      int var12 = Math.min(CreativeTabs.CREATIVE_TAB_ARRAY.length, (tabPage + 1) * 10 + 2);
      if (tabPage != 0) {
         var11 += 2;
      }

      boolean var13 = false;

      for(CreativeTabs var17 : (CreativeTabs[])Arrays.copyOfRange(CreativeTabs.CREATIVE_TAB_ARRAY, var11, var12)) {
         if (var17 != null && this.renderCreativeInventoryHoveringText(var17, var1, var2)) {
            var13 = true;
            break;
         }
      }

      if (!var13 && !this.renderCreativeInventoryHoveringText(CreativeTabs.SEARCH, var1, var2)) {
         this.renderCreativeInventoryHoveringText(CreativeTabs.INVENTORY, var1, var2);
      }

      if (this.destroyItemSlot != null && selectedTabIndex == CreativeTabs.INVENTORY.getTabIndex() && this.isPointInRegion(this.destroyItemSlot.xPos, this.destroyItemSlot.yPos, 16, 16, var1, var2)) {
         this.drawCreativeTabHoveringText(I18n.format("inventory.binSlot"), var1, var2);
      }

      if (this.maxPages != 0) {
         String var18 = String.format("%d / %d", tabPage + 1, this.maxPages + 1);
         int var19 = this.fontRendererObj.getStringWidth(var18);
         GlStateManager.disableLighting();
         this.zLevel = 300.0F;
         this.itemRender.zLevel = 300.0F;
         this.fontRendererObj.drawString(var18, this.guiLeft + this.xSize / 2 - var19 / 2, this.guiTop - 44, -1);
         this.zLevel = 0.0F;
         this.itemRender.zLevel = 0.0F;
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableLighting();
   }

   protected void renderToolTip(ItemStack var1, int var2, int var3) {
      if (selectedTabIndex == CreativeTabs.SEARCH.getTabIndex()) {
         List var4 = var1.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips);
         CreativeTabs var5 = var1.getItem().getCreativeTab();
         if (var5 == null && var1.getItem() == Items.ENCHANTED_BOOK) {
            Map var6 = EnchantmentHelper.getEnchantments(var1);
            if (var6.size() == 1) {
               Enchantment var7 = (Enchantment)var6.keySet().iterator().next();

               for(CreativeTabs var11 : CreativeTabs.CREATIVE_TAB_ARRAY) {
                  if (var11.hasRelevantEnchantmentType(var7.type)) {
                     var5 = var11;
                     break;
                  }
               }
            }
         }

         if (var5 != null) {
            var4.add(1, "" + TextFormatting.BOLD + TextFormatting.BLUE + I18n.format(var5.getTranslatedTabLabel()));
         }

         for(int var12 = 0; var12 < var4.size(); ++var12) {
            if (var12 == 0) {
               var4.set(var12, var1.getRarity().rarityColor + (String)var4.get(var12));
            } else {
               var4.set(var12, TextFormatting.GRAY + (String)var4.get(var12));
            }
         }

         GuiUtils.preItemToolTip(var1);
         this.drawHoveringText(var4, var2, var3);
         GuiUtils.postItemToolTip();
      } else {
         super.renderToolTip(var1, var2, var3);
      }

   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      RenderHelper.enableGUIStandardItemLighting();
      CreativeTabs var4 = CreativeTabs.CREATIVE_TAB_ARRAY[selectedTabIndex];
      int var5 = tabPage * 10;
      int var6 = Math.min(CreativeTabs.CREATIVE_TAB_ARRAY.length, (tabPage + 1) * 10 + 2);
      if (tabPage != 0) {
         var5 += 2;
      }

      for(CreativeTabs var10 : (CreativeTabs[])Arrays.copyOfRange(CreativeTabs.CREATIVE_TAB_ARRAY, var5, var6)) {
         this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
         if (var10 != null && var10.getTabIndex() != selectedTabIndex) {
            this.drawTab(var10);
         }
      }

      if (tabPage != 0) {
         if (var4 != CreativeTabs.SEARCH) {
            this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
            this.drawTab(CreativeTabs.SEARCH);
         }

         if (var4 != CreativeTabs.INVENTORY) {
            this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
            this.drawTab(CreativeTabs.INVENTORY);
         }
      }

      this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tab_" + var4.getBackgroundImageName()));
      this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
      this.searchField.drawTextBox();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      int var11 = this.guiLeft + 175;
      int var12 = this.guiTop + 18;
      int var13 = var12 + 112;
      this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
      if (var4.shouldHidePlayerInventory()) {
         this.drawTexturedModalRect(var11, var12 + (int)((float)(var13 - var12 - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
      }

      if (var4 != null && var4.getTabPage() == tabPage || var4 == CreativeTabs.SEARCH || var4 == CreativeTabs.INVENTORY) {
         this.drawTab(var4);
         if (var4 == CreativeTabs.INVENTORY) {
            GuiInventory.drawEntityOnScreen(this.guiLeft + 88, this.guiTop + 45, 20, (float)(this.guiLeft + 88 - var2), (float)(this.guiTop + 45 - 30 - var3), this.mc.player);
         }

      }
   }

   protected boolean isMouseOverTab(CreativeTabs var1, int var2, int var3) {
      if (var1.getTabPage() != tabPage && var1 != CreativeTabs.SEARCH && var1 != CreativeTabs.INVENTORY) {
         return false;
      } else {
         int var4 = var1.getTabColumn();
         int var5 = 28 * var4;
         int var6 = 0;
         if (var4 == 5) {
            var5 = this.xSize - 28 + 2;
         } else if (var4 > 0) {
            var5 += var4;
         }

         if (var1.isTabInFirstRow()) {
            var6 = var6 - 32;
         } else {
            var6 = var6 + this.ySize;
         }

         return var2 >= var5 && var2 <= var5 + 28 && var3 >= var6 && var3 <= var6 + 32;
      }
   }

   protected boolean renderCreativeInventoryHoveringText(CreativeTabs var1, int var2, int var3) {
      int var4 = var1.getTabColumn();
      int var5 = 28 * var4;
      int var6 = 0;
      if (var4 == 5) {
         var5 = this.xSize - 28 + 2;
      } else if (var4 > 0) {
         var5 += var4;
      }

      if (var1.isTabInFirstRow()) {
         var6 = var6 - 32;
      } else {
         var6 = var6 + this.ySize;
      }

      if (this.isPointInRegion(var5 + 3, var6 + 3, 23, 27, var2, var3)) {
         this.drawCreativeTabHoveringText(I18n.format(var1.getTranslatedTabLabel()), var2, var3);
         return true;
      } else {
         return false;
      }
   }

   protected void drawTab(CreativeTabs var1) {
      boolean var2 = var1.getTabIndex() == selectedTabIndex;
      boolean var3 = var1.isTabInFirstRow();
      int var4 = var1.getTabColumn();
      int var5 = var4 * 28;
      int var6 = 0;
      int var7 = this.guiLeft + 28 * var4;
      int var8 = this.guiTop;
      boolean var9 = true;
      if (var2) {
         var6 += 32;
      }

      if (var4 == 5) {
         var7 = this.guiLeft + this.xSize - 28;
      } else if (var4 > 0) {
         var7 += var4;
      }

      if (var3) {
         var8 = var8 - 28;
      } else {
         var6 += 64;
         var8 = var8 + (this.ySize - 4);
      }

      GlStateManager.disableLighting();
      GlStateManager.color(1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      this.drawTexturedModalRect(var7, var8, var5, var6, 28, 32);
      this.zLevel = 100.0F;
      this.itemRender.zLevel = 100.0F;
      var7 = var7 + 6;
      var8 = var8 + 8 + (var3 ? 1 : -1);
      GlStateManager.enableLighting();
      GlStateManager.enableRescaleNormal();
      ItemStack var10 = var1.getIconItemStack();
      this.itemRender.renderItemAndEffectIntoGUI(var10, var7, var8);
      this.itemRender.renderItemOverlays(this.fontRendererObj, var10, var7, var8);
      GlStateManager.disableLighting();
      this.itemRender.zLevel = 0.0F;
      this.zLevel = 0.0F;
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == 0) {
         this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.player.getStatFileWriter()));
      }

      if (var1.id == 1) {
         this.mc.displayGuiScreen(new GuiStats(this, this.mc.player.getStatFileWriter()));
      }

      if (var1.id == 101) {
         tabPage = Math.max(tabPage - 1, 0);
      } else if (var1.id == 102) {
         tabPage = Math.min(tabPage + 1, this.maxPages);
      }

   }

   public int getSelectedTabIndex() {
      return selectedTabIndex;
   }

   @SideOnly(Side.CLIENT)
   static class ContainerCreative extends Container {
      public List itemList = Lists.newArrayList();

      public ContainerCreative(EntityPlayer var1) {
         InventoryPlayer var2 = var1.inventory;

         for(int var3 = 0; var3 < 5; ++var3) {
            for(int var4 = 0; var4 < 9; ++var4) {
               this.addSlotToContainer(new Slot(GuiContainerCreative.basicInventory, var3 * 9 + var4, 9 + var4 * 18, 18 + var3 * 18));
            }
         }

         for(int var5 = 0; var5 < 9; ++var5) {
            this.addSlotToContainer(new Slot(var2, var5, 9 + var5 * 18, 112));
         }

         this.scrollTo(0.0F);
      }

      public boolean canInteractWith(EntityPlayer var1) {
         return true;
      }

      public void scrollTo(float var1) {
         int var2 = (this.itemList.size() + 9 - 1) / 9 - 5;
         int var3 = (int)((double)(var1 * (float)var2) + 0.5D);
         if (var3 < 0) {
            var3 = 0;
         }

         for(int var4 = 0; var4 < 5; ++var4) {
            for(int var5 = 0; var5 < 9; ++var5) {
               int var6 = var5 + (var4 + var3) * 9;
               if (var6 >= 0 && var6 < this.itemList.size()) {
                  GuiContainerCreative.basicInventory.setInventorySlotContents(var5 + var4 * 9, (ItemStack)this.itemList.get(var6));
               } else {
                  GuiContainerCreative.basicInventory.setInventorySlotContents(var5 + var4 * 9, (ItemStack)null);
               }
            }
         }

      }

      public boolean canScroll() {
         return this.itemList.size() > 45;
      }

      protected void retrySlotClick(int var1, int var2, boolean var3, EntityPlayer var4) {
      }

      @Nullable
      public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
         if (var2 >= this.inventorySlots.size() - 9 && var2 < this.inventorySlots.size()) {
            Slot var3 = (Slot)this.inventorySlots.get(var2);
            if (var3 != null && var3.getHasStack()) {
               var3.putStack((ItemStack)null);
            }
         }

         return null;
      }

      public boolean canMergeSlot(ItemStack var1, Slot var2) {
         return var2.yPos > 90;
      }

      public boolean canDragIntoSlot(Slot var1) {
         return var1.inventory instanceof InventoryPlayer || var1.yPos > 90 && var1.xPos <= 162;
      }
   }

   @SideOnly(Side.CLIENT)
   class CreativeSlot extends Slot {
      private final Slot slot;

      public CreativeSlot(Slot var2, int var3) {
         super(var2.inventory, var3, 0, 0);
         this.slot = var2;
      }

      public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
         this.slot.onPickupFromSlot(var1, var2);
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return this.slot.isItemValid(var1);
      }

      public ItemStack getStack() {
         return this.slot.getStack();
      }

      public boolean getHasStack() {
         return this.slot.getHasStack();
      }

      public void putStack(@Nullable ItemStack var1) {
         this.slot.putStack(var1);
      }

      public void onSlotChanged() {
         this.slot.onSlotChanged();
      }

      public int getSlotStackLimit() {
         return this.slot.getSlotStackLimit();
      }

      public int getItemStackLimit(ItemStack var1) {
         return this.slot.getItemStackLimit(var1);
      }

      @Nullable
      public String getSlotTexture() {
         return this.slot.getSlotTexture();
      }

      public ItemStack decrStackSize(int var1) {
         return this.slot.decrStackSize(var1);
      }

      public boolean isHere(IInventory var1, int var2) {
         return this.slot.isHere(var1, var2);
      }

      public boolean canBeHovered() {
         return this.slot.canBeHovered();
      }

      public boolean canTakeStack(EntityPlayer var1) {
         return this.slot.canTakeStack(var1);
      }

      public void onSlotChange(ItemStack var1, ItemStack var2) {
         super.onSlotChange(var1, var2);
      }
   }
}
