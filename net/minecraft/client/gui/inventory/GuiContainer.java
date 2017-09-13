package net.minecraft.client.gui.inventory;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public abstract class GuiContainer extends GuiScreen {
   public static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation("textures/gui/container/inventory.png");
   protected int xSize = 176;
   protected int ySize = 166;
   public Container inventorySlots;
   protected int guiLeft;
   protected int guiTop;
   private Slot theSlot;
   private Slot clickedSlot;
   private boolean isRightMouseClick;
   private ItemStack draggedStack;
   private int touchUpX;
   private int touchUpY;
   private Slot returningStackDestSlot;
   private long returningStackTime;
   private ItemStack returningStack;
   private Slot currentDragTargetSlot;
   private long dragItemDropDelay;
   protected final Set dragSplittingSlots = Sets.newHashSet();
   protected boolean dragSplitting;
   private int dragSplittingLimit;
   private int dragSplittingButton;
   private boolean ignoreMouseUp;
   private int dragSplittingRemnant;
   private long lastClickTime;
   private Slot lastClickSlot;
   private int lastClickButton;
   private boolean doubleClick;
   private ItemStack shiftClickedSlot;

   public GuiContainer(Container var1) {
      this.inventorySlots = inventorySlotsIn;
      this.ignoreMouseUp = true;
   }

   public void initGui() {
      super.initGui();
      this.mc.player.openContainer = this.inventorySlots;
      this.guiLeft = (this.width - this.xSize) / 2;
      this.guiTop = (this.height - this.ySize) / 2;
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      int i = this.guiLeft;
      int j = this.guiTop;
      this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      super.drawScreen(mouseX, mouseY, partialTicks);
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)i, (float)j, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableRescaleNormal();
      this.theSlot = null;
      int k = 240;
      int l = 240;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

      for(int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
         Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i1);
         this.drawSlot(slot);
         if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered()) {
            this.theSlot = slot;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int j1 = slot.xPos;
            int k1 = slot.yPos;
            GlStateManager.colorMask(true, true, true, false);
            this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
         }
      }

      RenderHelper.disableStandardItemLighting();
      this.drawGuiContainerForegroundLayer(mouseX, mouseY);
      RenderHelper.enableGUIStandardItemLighting();
      InventoryPlayer inventoryplayer = this.mc.player.inventory;
      ItemStack itemstack = this.draggedStack == null ? inventoryplayer.getItemStack() : this.draggedStack;
      if (itemstack != null) {
         int j2 = 8;
         int k2 = this.draggedStack == null ? 8 : 16;
         String s = null;
         if (this.draggedStack != null && this.isRightMouseClick) {
            itemstack = itemstack.copy();
            itemstack.stackSize = MathHelper.ceil((float)itemstack.stackSize / 2.0F);
         } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
            itemstack = itemstack.copy();
            itemstack.stackSize = this.dragSplittingRemnant;
            if (itemstack.stackSize == 0) {
               s = "" + TextFormatting.YELLOW + "0";
            }
         }

         this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
      }

      if (this.returningStack != null) {
         float f = (float)(Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;
         if (f >= 1.0F) {
            f = 1.0F;
            this.returningStack = null;
         }

         int l2 = this.returningStackDestSlot.xPos - this.touchUpX;
         int i3 = this.returningStackDestSlot.yPos - this.touchUpY;
         int l1 = this.touchUpX + (int)((float)l2 * f);
         int i2 = this.touchUpY + (int)((float)i3 * f);
         this.drawItemStack(this.returningStack, l1, i2, (String)null);
      }

      GlStateManager.popMatrix();
      if (inventoryplayer.getItemStack() == null && this.theSlot != null && this.theSlot.getHasStack()) {
         ItemStack itemstack1 = this.theSlot.getStack();
         this.renderToolTip(itemstack1, mouseX, mouseY);
      }

      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      RenderHelper.enableStandardItemLighting();
   }

   private void drawItemStack(ItemStack var1, int var2, int var3, String var4) {
      GlStateManager.translate(0.0F, 0.0F, 32.0F);
      this.zLevel = 200.0F;
      this.itemRender.zLevel = 200.0F;
      FontRenderer font = null;
      if (stack != null) {
         font = stack.getItem().getFontRenderer(stack);
      }

      if (font == null) {
         font = this.fontRendererObj;
      }

      this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
      this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y - (this.draggedStack == null ? 0 : 8), altText);
      this.zLevel = 0.0F;
      this.itemRender.zLevel = 0.0F;
   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
   }

   protected abstract void drawGuiContainerBackgroundLayer(float var1, int var2, int var3);

   private void drawSlot(Slot var1) {
      int i = slotIn.xPos;
      int j = slotIn.yPos;
      ItemStack itemstack = slotIn.getStack();
      boolean flag = false;
      boolean flag1 = slotIn == this.clickedSlot && this.draggedStack != null && !this.isRightMouseClick;
      ItemStack itemstack1 = this.mc.player.inventory.getItemStack();
      String s = null;
      if (slotIn == this.clickedSlot && this.draggedStack != null && this.isRightMouseClick && itemstack != null) {
         itemstack = itemstack.copy();
         itemstack.stackSize /= 2;
      } else if (this.dragSplitting && this.dragSplittingSlots.contains(slotIn) && itemstack1 != null) {
         if (this.dragSplittingSlots.size() == 1) {
            return;
         }

         if (Container.canAddItemToSlot(slotIn, itemstack1, true) && this.inventorySlots.canDragIntoSlot(slotIn)) {
            itemstack = itemstack1.copy();
            flag = true;
            Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack, slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);
            if (itemstack.stackSize > itemstack.getMaxStackSize()) {
               s = TextFormatting.YELLOW + "" + itemstack.getMaxStackSize();
               itemstack.stackSize = itemstack.getMaxStackSize();
            }

            if (itemstack.stackSize > slotIn.getItemStackLimit(itemstack)) {
               s = TextFormatting.YELLOW + "" + slotIn.getItemStackLimit(itemstack);
               itemstack.stackSize = slotIn.getItemStackLimit(itemstack);
            }
         } else {
            this.dragSplittingSlots.remove(slotIn);
            this.updateDragSplitting();
         }
      }

      this.zLevel = 100.0F;
      this.itemRender.zLevel = 100.0F;
      if (itemstack == null && slotIn.canBeHovered()) {
         TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();
         if (textureatlassprite != null) {
            GlStateManager.disableLighting();
            this.mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
            this.drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
            GlStateManager.enableLighting();
            flag1 = true;
         }
      }

      if (!flag1) {
         if (flag) {
            drawRect(i, j, i + 16, j + 16, -2130706433);
         }

         GlStateManager.enableDepth();
         this.itemRender.renderItemAndEffectIntoGUI(this.mc.player, itemstack, i, j);
         this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, itemstack, i, j, s);
      }

      this.itemRender.zLevel = 0.0F;
      this.zLevel = 0.0F;
   }

   private void updateDragSplitting() {
      ItemStack itemstack = this.mc.player.inventory.getItemStack();
      if (itemstack != null && this.dragSplitting) {
         this.dragSplittingRemnant = itemstack.stackSize;

         for(Slot slot : this.dragSplittingSlots) {
            ItemStack itemstack1 = itemstack.copy();
            int i = slot.getStack() == null ? 0 : slot.getStack().stackSize;
            Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack1, i);
            if (itemstack1.stackSize > itemstack1.getMaxStackSize()) {
               itemstack1.stackSize = itemstack1.getMaxStackSize();
            }

            if (itemstack1.stackSize > slot.getItemStackLimit(itemstack1)) {
               itemstack1.stackSize = slot.getItemStackLimit(itemstack1);
            }

            this.dragSplittingRemnant -= itemstack1.stackSize - i;
         }
      }

   }

   private Slot getSlotAtPosition(int var1, int var2) {
      for(int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
         Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i);
         if (this.isMouseOverSlot(slot, x, y)) {
            return slot;
         }
      }

      return null;
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      boolean flag = this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100);
      Slot slot = this.getSlotAtPosition(mouseX, mouseY);
      long i = Minecraft.getSystemTime();
      this.doubleClick = this.lastClickSlot == slot && i - this.lastClickTime < 250L && this.lastClickButton == mouseButton;
      this.ignoreMouseUp = false;
      if (mouseButton == 0 || mouseButton == 1 || flag) {
         int j = this.guiLeft;
         int k = this.guiTop;
         boolean flag1 = mouseX < j || mouseY < k || mouseX >= j + this.xSize || mouseY >= k + this.ySize;
         if (slot != null) {
            flag1 = false;
         }

         int l = -1;
         if (slot != null) {
            l = slot.slotNumber;
         }

         if (flag1) {
            l = -999;
         }

         if (this.mc.gameSettings.touchscreen && flag1 && this.mc.player.inventory.getItemStack() == null) {
            this.mc.displayGuiScreen((GuiScreen)null);
            return;
         }

         if (l != -1) {
            if (this.mc.gameSettings.touchscreen) {
               if (slot != null && slot.getHasStack()) {
                  this.clickedSlot = slot;
                  this.draggedStack = null;
                  this.isRightMouseClick = mouseButton == 1;
               } else {
                  this.clickedSlot = null;
               }
            } else if (!this.dragSplitting) {
               if (this.mc.player.inventory.getItemStack() == null) {
                  if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100)) {
                     this.handleMouseClick(slot, l, mouseButton, ClickType.CLONE);
                  } else {
                     boolean flag2 = l != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
                     ClickType clicktype = ClickType.PICKUP;
                     if (flag2) {
                        this.shiftClickedSlot = slot != null && slot.getHasStack() ? slot.getStack() : null;
                        clicktype = ClickType.QUICK_MOVE;
                     } else if (l == -999) {
                        clicktype = ClickType.THROW;
                     }

                     this.handleMouseClick(slot, l, mouseButton, clicktype);
                  }

                  this.ignoreMouseUp = true;
               } else {
                  this.dragSplitting = true;
                  this.dragSplittingButton = mouseButton;
                  this.dragSplittingSlots.clear();
                  if (mouseButton == 0) {
                     this.dragSplittingLimit = 0;
                  } else if (mouseButton == 1) {
                     this.dragSplittingLimit = 1;
                  } else if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100)) {
                     this.dragSplittingLimit = 2;
                  }
               }
            }
         }
      }

      this.lastClickSlot = slot;
      this.lastClickTime = i;
      this.lastClickButton = mouseButton;
   }

   protected void mouseClickMove(int var1, int var2, int var3, long var4) {
      Slot slot = this.getSlotAtPosition(mouseX, mouseY);
      ItemStack itemstack = this.mc.player.inventory.getItemStack();
      if (this.clickedSlot != null && this.mc.gameSettings.touchscreen) {
         if (clickedMouseButton == 0 || clickedMouseButton == 1) {
            if (this.draggedStack == null) {
               if (slot != this.clickedSlot && this.clickedSlot.getStack() != null) {
                  this.draggedStack = this.clickedSlot.getStack().copy();
               }
            } else if (this.draggedStack.stackSize > 1 && slot != null && Container.canAddItemToSlot(slot, this.draggedStack, false)) {
               long i = Minecraft.getSystemTime();
               if (this.currentDragTargetSlot == slot) {
                  if (i - this.dragItemDropDelay > 500L) {
                     this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, ClickType.PICKUP);
                     this.handleMouseClick(slot, slot.slotNumber, 1, ClickType.PICKUP);
                     this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, ClickType.PICKUP);
                     this.dragItemDropDelay = i + 750L;
                     --this.draggedStack.stackSize;
                  }
               } else {
                  this.currentDragTargetSlot = slot;
                  this.dragItemDropDelay = i;
               }
            }
         }
      } else if (this.dragSplitting && slot != null && itemstack != null && itemstack.stackSize > this.dragSplittingSlots.size() && Container.canAddItemToSlot(slot, itemstack, true) && slot.isItemValid(itemstack) && this.inventorySlots.canDragIntoSlot(slot)) {
         this.dragSplittingSlots.add(slot);
         this.updateDragSplitting();
      }

   }

   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(mouseX, mouseY, state);
      Slot slot = this.getSlotAtPosition(mouseX, mouseY);
      int i = this.guiLeft;
      int j = this.guiTop;
      boolean flag = mouseX < i || mouseY < j || mouseX >= i + this.xSize || mouseY >= j + this.ySize;
      if (slot != null) {
         flag = false;
      }

      int k = -1;
      if (slot != null) {
         k = slot.slotNumber;
      }

      if (flag) {
         k = -999;
      }

      if (this.doubleClick && slot != null && state == 0 && this.inventorySlots.canMergeSlot((ItemStack)null, slot)) {
         if (isShiftKeyDown()) {
            if (slot != null && slot.inventory != null && this.shiftClickedSlot != null) {
               for(Slot slot2 : this.inventorySlots.inventorySlots) {
                  if (slot2 != null && slot2.canTakeStack(this.mc.player) && slot2.getHasStack() && slot2.isSameInventory(slot) && Container.canAddItemToSlot(slot2, this.shiftClickedSlot, true)) {
                     this.handleMouseClick(slot2, slot2.slotNumber, state, ClickType.QUICK_MOVE);
                  }
               }
            }
         } else {
            this.handleMouseClick(slot, k, state, ClickType.PICKUP_ALL);
         }

         this.doubleClick = false;
         this.lastClickTime = 0L;
      } else {
         if (this.dragSplitting && this.dragSplittingButton != state) {
            this.dragSplitting = false;
            this.dragSplittingSlots.clear();
            this.ignoreMouseUp = true;
            return;
         }

         if (this.ignoreMouseUp) {
            this.ignoreMouseUp = false;
            return;
         }

         if (this.clickedSlot != null && this.mc.gameSettings.touchscreen) {
            if (state == 0 || state == 1) {
               if (this.draggedStack == null && slot != this.clickedSlot) {
                  this.draggedStack = this.clickedSlot.getStack();
               }

               boolean flag2 = Container.canAddItemToSlot(slot, this.draggedStack, false);
               if (k != -1 && this.draggedStack != null && flag2) {
                  this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, state, ClickType.PICKUP);
                  this.handleMouseClick(slot, k, 0, ClickType.PICKUP);
                  if (this.mc.player.inventory.getItemStack() != null) {
                     this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, state, ClickType.PICKUP);
                     this.touchUpX = mouseX - i;
                     this.touchUpY = mouseY - j;
                     this.returningStackDestSlot = this.clickedSlot;
                     this.returningStack = this.draggedStack;
                     this.returningStackTime = Minecraft.getSystemTime();
                  } else {
                     this.returningStack = null;
                  }
               } else if (this.draggedStack != null) {
                  this.touchUpX = mouseX - i;
                  this.touchUpY = mouseY - j;
                  this.returningStackDestSlot = this.clickedSlot;
                  this.returningStack = this.draggedStack;
                  this.returningStackTime = Minecraft.getSystemTime();
               }

               this.draggedStack = null;
               this.clickedSlot = null;
            }
         } else if (this.dragSplitting && !this.dragSplittingSlots.isEmpty()) {
            this.handleMouseClick((Slot)null, -999, Container.getQuickcraftMask(0, this.dragSplittingLimit), ClickType.QUICK_CRAFT);

            for(Slot slot1 : this.dragSplittingSlots) {
               this.handleMouseClick(slot1, slot1.slotNumber, Container.getQuickcraftMask(1, this.dragSplittingLimit), ClickType.QUICK_CRAFT);
            }

            this.handleMouseClick((Slot)null, -999, Container.getQuickcraftMask(2, this.dragSplittingLimit), ClickType.QUICK_CRAFT);
         } else if (this.mc.player.inventory.getItemStack() != null) {
            if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(state - 100)) {
               this.handleMouseClick(slot, k, state, ClickType.CLONE);
            } else {
               boolean flag1 = k != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
               if (flag1) {
                  this.shiftClickedSlot = slot != null && slot.getHasStack() ? slot.getStack() : null;
               }

               this.handleMouseClick(slot, k, state, flag1 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
            }
         }
      }

      if (this.mc.player.inventory.getItemStack() == null) {
         this.lastClickTime = 0L;
      }

      this.dragSplitting = false;
   }

   private boolean isMouseOverSlot(Slot var1, int var2, int var3) {
      return this.isPointInRegion(slotIn.xPos, slotIn.yPos, 16, 16, mouseX, mouseY);
   }

   protected boolean isPointInRegion(int var1, int var2, int var3, int var4, int var5, int var6) {
      int i = this.guiLeft;
      int j = this.guiTop;
      pointX = pointX - i;
      pointY = pointY - j;
      return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
   }

   protected void handleMouseClick(Slot var1, int var2, int var3, ClickType var4) {
      if (slotIn != null) {
         slotId = slotIn.slotNumber;
      }

      this.mc.playerController.windowClick(this.inventorySlots.windowId, slotId, mouseButton, type, this.mc.player);
   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
         this.mc.player.closeScreen();
      }

      this.checkHotbarKeys(keyCode);
      if (this.theSlot != null && this.theSlot.getHasStack()) {
         if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(keyCode)) {
            this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, ClickType.CLONE);
         } else if (this.mc.gameSettings.keyBindDrop.isActiveAndMatches(keyCode)) {
            this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
         }
      }

   }

   protected boolean checkHotbarKeys(int var1) {
      if (this.mc.player.inventory.getItemStack() == null && this.theSlot != null) {
         for(int i = 0; i < 9; ++i) {
            if (this.mc.gameSettings.keyBindsHotbar[i].isActiveAndMatches(keyCode)) {
               this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, i, ClickType.SWAP);
               return true;
            }
         }
      }

      return false;
   }

   public void onGuiClosed() {
      if (this.mc.player != null) {
         this.inventorySlots.onContainerClosed(this.mc.player);
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public void updateScreen() {
      super.updateScreen();
      if (!this.mc.player.isEntityAlive() || this.mc.player.isDead) {
         this.mc.player.closeScreen();
      }

   }

   public Slot getSlotUnderMouse() {
      return this.theSlot;
   }
}
