package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public abstract class GuiScreen extends Gui implements GuiYesNoCallback {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Set PROTOCOLS = Sets.newHashSet(new String[]{"http", "https"});
   private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
   public Minecraft mc;
   protected RenderItem itemRender;
   public int width;
   public int height;
   protected List buttonList = Lists.newArrayList();
   protected List labelList = Lists.newArrayList();
   public boolean allowUserInput;
   protected FontRenderer fontRendererObj;
   private GuiButton selectedButton;
   private int eventButton;
   private long lastMouseEvent;
   private int touchValue;
   private URI clickedLinkURI;

   public void drawScreen(int var1, int var2, float var3) {
      for(int var4 = 0; var4 < this.buttonList.size(); ++var4) {
         ((GuiButton)this.buttonList.get(var4)).drawButton(this.mc, var1, var2);
      }

      for(int var5 = 0; var5 < this.labelList.size(); ++var5) {
         ((GuiLabel)this.labelList.get(var5)).drawLabel(this.mc, var1, var2);
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (var2 == 1) {
         this.mc.displayGuiScreen((GuiScreen)null);
         if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
         }
      }

   }

   protected GuiButton addButton(GuiButton var1) {
      this.buttonList.add(var1);
      return var1;
   }

   public static String getClipboardString() {
      try {
         Transferable var0 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object)null);
         if (var0 != null && var0.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return (String)var0.getTransferData(DataFlavor.stringFlavor);
         }
      } catch (Exception var1) {
         ;
      }

      return "";
   }

   public static void setClipboardString(String var0) {
      if (!StringUtils.isEmpty(var0)) {
         try {
            StringSelection var1 = new StringSelection(var0);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(var1, (ClipboardOwner)null);
         } catch (Exception var2) {
            ;
         }
      }

   }

   protected void renderToolTip(ItemStack var1, int var2, int var3) {
      List var4 = var1.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips);

      for(int var5 = 0; var5 < var4.size(); ++var5) {
         if (var5 == 0) {
            var4.set(var5, var1.getRarity().rarityColor + (String)var4.get(var5));
         } else {
            var4.set(var5, TextFormatting.GRAY + (String)var4.get(var5));
         }
      }

      FontRenderer var6 = var1.getItem().getFontRenderer(var1);
      GuiUtils.preItemToolTip(var1);
      this.drawHoveringText(var4, var2, var3, var6 == null ? this.fontRendererObj : var6);
      GuiUtils.postItemToolTip();
   }

   protected void drawCreativeTabHoveringText(String var1, int var2, int var3) {
      this.drawHoveringText(Arrays.asList(var1), var2, var3);
   }

   protected void drawHoveringText(List var1, int var2, int var3) {
      this.drawHoveringText(var1, var2, var3, this.fontRendererObj);
   }

   protected void drawHoveringText(List var1, int var2, int var3, FontRenderer var4) {
      GuiUtils.drawHoveringText(var1, var2, var3, this.width, this.height, -1, var4);
   }

   protected void handleComponentHover(ITextComponent var1, int var2, int var3) {
      if (var1 != null && var1.getStyle().getHoverEvent() != null) {
         HoverEvent var4 = var1.getStyle().getHoverEvent();
         if (var4.getAction() == HoverEvent.Action.SHOW_ITEM) {
            ItemStack var5 = null;

            try {
               NBTTagCompound var6 = JsonToNBT.getTagFromJson(var4.getValue().getUnformattedText());
               if (var6 instanceof NBTTagCompound) {
                  var5 = ItemStack.loadItemStackFromNBT(var6);
               }
            } catch (NBTException var11) {
               ;
            }

            if (var5 != null) {
               this.renderToolTip(var5, var2, var3);
            } else {
               this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Item!", var2, var3);
            }
         } else if (var4.getAction() == HoverEvent.Action.SHOW_ENTITY) {
            if (this.mc.gameSettings.advancedItemTooltips) {
               try {
                  NBTTagCompound var12 = JsonToNBT.getTagFromJson(var4.getValue().getUnformattedText());
                  if (var12 instanceof NBTTagCompound) {
                     ArrayList var14 = Lists.newArrayList();
                     NBTTagCompound var7 = var12;
                     var14.add(var7.getString("name"));
                     if (var7.hasKey("type", 8)) {
                        String var8 = var7.getString("type");
                        var14.add("Type: " + var8 + " (" + EntityList.getIDFromString(var8) + ")");
                     }

                     var14.add(var7.getString("id"));
                     this.drawHoveringText(var14, var2, var3);
                  } else {
                     this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Entity!", var2, var3);
                  }
               } catch (NBTException var10) {
                  this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Entity!", var2, var3);
               }
            }
         } else if (var4.getAction() == HoverEvent.Action.SHOW_TEXT) {
            this.drawHoveringText(NEWLINE_SPLITTER.splitToList(var4.getValue().getFormattedText()), var2, var3);
         } else if (var4.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
            StatBase var13 = StatList.getOneShotStat(var4.getValue().getUnformattedText());
            if (var13 != null) {
               ITextComponent var15 = var13.getStatName();
               TextComponentTranslation var16 = new TextComponentTranslation("stats.tooltip.type." + (var13.isAchievement() ? "achievement" : "statistic"), new Object[0]);
               var16.getStyle().setItalic(Boolean.valueOf(true));
               String var17 = var13 instanceof Achievement ? ((Achievement)var13).getDescription() : null;
               ArrayList var9 = Lists.newArrayList(new String[]{var15.getFormattedText(), var16.getFormattedText()});
               if (var17 != null) {
                  var9.addAll(this.fontRendererObj.listFormattedStringToWidth(var17, 150));
               }

               this.drawHoveringText(var9, var2, var3);
            } else {
               this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid statistic/achievement!", var2, var3);
            }
         }

         GlStateManager.disableLighting();
      }

   }

   protected void setText(String var1, boolean var2) {
   }

   protected boolean handleComponentClick(ITextComponent var1) {
      if (var1 == null) {
         return false;
      } else {
         ClickEvent var2 = var1.getStyle().getClickEvent();
         if (isShiftKeyDown()) {
            if (var1.getStyle().getInsertion() != null) {
               this.setText(var1.getStyle().getInsertion(), false);
            }
         } else if (var2 != null) {
            if (var2.getAction() == ClickEvent.Action.OPEN_URL) {
               if (!this.mc.gameSettings.chatLinks) {
                  return false;
               }

               try {
                  URI var3 = new URI(var2.getValue());
                  String var4 = var3.getScheme();
                  if (var4 == null) {
                     throw new URISyntaxException(var2.getValue(), "Missing protocol");
                  }

                  if (!PROTOCOLS.contains(var4.toLowerCase())) {
                     throw new URISyntaxException(var2.getValue(), "Unsupported protocol: " + var4.toLowerCase());
                  }

                  if (this.mc.gameSettings.chatLinksPrompt) {
                     this.clickedLinkURI = var3;
                     this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, var2.getValue(), 31102009, false));
                  } else {
                     this.openWebLink(var3);
                  }
               } catch (URISyntaxException var5) {
                  LOGGER.error("Can't open url for {}", new Object[]{var2, var5});
               }
            } else if (var2.getAction() == ClickEvent.Action.OPEN_FILE) {
               URI var6 = (new File(var2.getValue())).toURI();
               this.openWebLink(var6);
            } else if (var2.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
               this.setText(var2.getValue(), true);
            } else if (var2.getAction() == ClickEvent.Action.RUN_COMMAND) {
               this.sendChatMessage(var2.getValue(), false);
            } else {
               LOGGER.error("Don't know how to handle {}", new Object[]{var2});
            }

            return true;
         }

         return false;
      }
   }

   public void sendChatMessage(String var1) {
      this.sendChatMessage(var1, true);
   }

   public void sendChatMessage(String var1, boolean var2) {
      if (var2) {
         this.mc.ingameGUI.getChatGUI().addToSentMessages(var1);
      }

      if (ClientCommandHandler.instance.executeCommand(this.mc.player, var1) == 0) {
         this.mc.player.sendChatMessage(var1);
      }
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (var3 == 0) {
         for(int var4 = 0; var4 < this.buttonList.size(); ++var4) {
            GuiButton var5 = (GuiButton)this.buttonList.get(var4);
            if (var5.mousePressed(this.mc, var1, var2)) {
               Pre var6 = new Pre(this, var5, this.buttonList);
               if (MinecraftForge.EVENT_BUS.post(var6)) {
                  break;
               }

               var5 = var6.getButton();
               this.selectedButton = var5;
               var5.playPressSound(this.mc.getSoundHandler());
               this.actionPerformed(var5);
               if (this.equals(this.mc.currentScreen)) {
                  MinecraftForge.EVENT_BUS.post(new Post(this, var6.getButton(), this.buttonList));
               }
            }
         }
      }

   }

   protected void mouseReleased(int var1, int var2, int var3) {
      if (this.selectedButton != null && var3 == 0) {
         this.selectedButton.mouseReleased(var1, var2);
         this.selectedButton = null;
      }

   }

   protected void mouseClickMove(int var1, int var2, int var3, long var4) {
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
   }

   public void setWorldAndResolution(Minecraft var1, int var2, int var3) {
      this.mc = var1;
      this.itemRender = var1.getRenderItem();
      this.fontRendererObj = var1.fontRendererObj;
      this.width = var2;
      this.height = var3;
      if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
         this.buttonList.clear();
         this.initGui();
      }

      MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
   }

   public void setGuiSize(int var1, int var2) {
      this.width = var1;
      this.height = var2;
   }

   public void initGui() {
   }

   public void handleInput() throws IOException {
      if (Mouse.isCreated()) {
         while(Mouse.next()) {
            if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Pre(this))) {
               this.handleMouseInput();
               if (this.equals(this.mc.currentScreen)) {
                  MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Post(this));
               }
            }
         }
      }

      if (Keyboard.isCreated()) {
         while(Keyboard.next()) {
            if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Pre(this))) {
               this.handleKeyboardInput();
               if (this.equals(this.mc.currentScreen)) {
                  MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Post(this));
               }
            }
         }
      }

   }

   public void handleMouseInput() throws IOException {
      int var1 = Mouse.getEventX() * this.width / this.mc.displayWidth;
      int var2 = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
      int var3 = Mouse.getEventButton();
      if (Mouse.getEventButtonState()) {
         if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
            return;
         }

         this.eventButton = var3;
         this.lastMouseEvent = Minecraft.getSystemTime();
         this.mouseClicked(var1, var2, this.eventButton);
      } else if (var3 != -1) {
         if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) {
            return;
         }

         this.eventButton = -1;
         this.mouseReleased(var1, var2, var3);
      } else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
         long var4 = Minecraft.getSystemTime() - this.lastMouseEvent;
         this.mouseClickMove(var1, var2, this.eventButton, var4);
      }

   }

   public void handleKeyboardInput() throws IOException {
      char var1 = Keyboard.getEventCharacter();
      if (Keyboard.getEventKey() == 0 && var1 >= ' ' || Keyboard.getEventKeyState()) {
         this.keyTyped(var1, Keyboard.getEventKey());
      }

      this.mc.dispatchKeypresses();
   }

   public void updateScreen() {
   }

   public void onGuiClosed() {
   }

   public void drawDefaultBackground() {
      this.drawWorldBackground(0);
      MinecraftForge.EVENT_BUS.post(new BackgroundDrawnEvent(this));
   }

   public void drawWorldBackground(int var1) {
      if (this.mc.world != null) {
         this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
      } else {
         this.drawBackground(var1);
      }

   }

   public void drawBackground(int var1) {
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      Tessellator var2 = Tessellator.getInstance();
      VertexBuffer var3 = var2.getBuffer();
      this.mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      float var4 = 32.0F;
      var3.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var3.pos(0.0D, (double)this.height, 0.0D).tex(0.0D, (double)((float)this.height / 32.0F + (float)var1)).color(64, 64, 64, 255).endVertex();
      var3.pos((double)this.width, (double)this.height, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)this.height / 32.0F + (float)var1)).color(64, 64, 64, 255).endVertex();
      var3.pos((double)this.width, 0.0D, 0.0D).tex((double)((float)this.width / 32.0F), (double)var1).color(64, 64, 64, 255).endVertex();
      var3.pos(0.0D, 0.0D, 0.0D).tex(0.0D, (double)var1).color(64, 64, 64, 255).endVertex();
      var2.draw();
   }

   public boolean doesGuiPauseGame() {
      return true;
   }

   public void confirmClicked(boolean var1, int var2) {
      if (var2 == 31102009) {
         if (var1) {
            this.openWebLink(this.clickedLinkURI);
         }

         this.clickedLinkURI = null;
         this.mc.displayGuiScreen(this);
      }

   }

   private void openWebLink(URI var1) {
      try {
         Class var2 = Class.forName("java.awt.Desktop");
         Object var5 = var2.getMethod("getDesktop").invoke((Object)null);
         var2.getMethod("browse", URI.class).invoke(var5, var1);
      } catch (Throwable var4) {
         Throwable var3 = var4.getCause();
         LOGGER.error("Couldn't open link: {}", new Object[]{var3 == null ? "<UNKNOWN>" : var3.getMessage()});
      }

   }

   public static boolean isCtrlKeyDown() {
      return Minecraft.IS_RUNNING_ON_MAC ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
   }

   public static boolean isShiftKeyDown() {
      return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
   }

   public static boolean isAltKeyDown() {
      return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
   }

   public static boolean isKeyComboCtrlX(int var0) {
      return var0 == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static boolean isKeyComboCtrlV(int var0) {
      return var0 == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static boolean isKeyComboCtrlC(int var0) {
      return var0 == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static boolean isKeyComboCtrlA(int var0) {
      return var0 == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public void onResize(Minecraft var1, int var2, int var3) {
      this.setWorldAndResolution(var1, var2, var3);
   }
}
