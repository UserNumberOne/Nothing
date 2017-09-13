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
import net.minecraft.nbt.NBTBase;
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
      for(int i = 0; i < this.buttonList.size(); ++i) {
         ((GuiButton)this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY);
      }

      for(int j = 0; j < this.labelList.size(); ++j) {
         ((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (keyCode == 1) {
         this.mc.displayGuiScreen((GuiScreen)null);
         if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
         }
      }

   }

   protected GuiButton addButton(GuiButton var1) {
      this.buttonList.add(p_189646_1_);
      return p_189646_1_;
   }

   public static String getClipboardString() {
      try {
         Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object)null);
         if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return (String)transferable.getTransferData(DataFlavor.stringFlavor);
         }
      } catch (Exception var1) {
         ;
      }

      return "";
   }

   public static void setClipboardString(String var0) {
      if (!StringUtils.isEmpty(copyText)) {
         try {
            StringSelection stringselection = new StringSelection(copyText);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, (ClipboardOwner)null);
         } catch (Exception var2) {
            ;
         }
      }

   }

   protected void renderToolTip(ItemStack var1, int var2, int var3) {
      List list = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips);

      for(int i = 0; i < list.size(); ++i) {
         if (i == 0) {
            list.set(i, stack.getRarity().rarityColor + (String)list.get(i));
         } else {
            list.set(i, TextFormatting.GRAY + (String)list.get(i));
         }
      }

      FontRenderer font = stack.getItem().getFontRenderer(stack);
      GuiUtils.preItemToolTip(stack);
      this.drawHoveringText(list, x, y, font == null ? this.fontRendererObj : font);
      GuiUtils.postItemToolTip();
   }

   protected void drawCreativeTabHoveringText(String var1, int var2, int var3) {
      this.drawHoveringText(Arrays.asList(tabName), mouseX, mouseY);
   }

   protected void drawHoveringText(List var1, int var2, int var3) {
      this.drawHoveringText(textLines, x, y, this.fontRendererObj);
   }

   protected void drawHoveringText(List var1, int var2, int var3, FontRenderer var4) {
      GuiUtils.drawHoveringText(textLines, x, y, this.width, this.height, -1, font);
   }

   protected void handleComponentHover(ITextComponent var1, int var2, int var3) {
      if (component != null && component.getStyle().getHoverEvent() != null) {
         HoverEvent hoverevent = component.getStyle().getHoverEvent();
         if (hoverevent.getAction() == HoverEvent.Action.SHOW_ITEM) {
            ItemStack itemstack = null;

            try {
               NBTBase nbtbase = JsonToNBT.getTagFromJson(hoverevent.getValue().getUnformattedText());
               if (nbtbase instanceof NBTTagCompound) {
                  itemstack = ItemStack.loadItemStackFromNBT((NBTTagCompound)nbtbase);
               }
            } catch (NBTException var11) {
               ;
            }

            if (itemstack != null) {
               this.renderToolTip(itemstack, x, y);
            } else {
               this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Item!", x, y);
            }
         } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_ENTITY) {
            if (this.mc.gameSettings.advancedItemTooltips) {
               try {
                  NBTBase nbtbase1 = JsonToNBT.getTagFromJson(hoverevent.getValue().getUnformattedText());
                  if (nbtbase1 instanceof NBTTagCompound) {
                     List list1 = Lists.newArrayList();
                     NBTTagCompound nbttagcompound = (NBTTagCompound)nbtbase1;
                     list1.add(nbttagcompound.getString("name"));
                     if (nbttagcompound.hasKey("type", 8)) {
                        String s = nbttagcompound.getString("type");
                        list1.add("Type: " + s + " (" + EntityList.getIDFromString(s) + ")");
                     }

                     list1.add(nbttagcompound.getString("id"));
                     this.drawHoveringText(list1, x, y);
                  } else {
                     this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Entity!", x, y);
                  }
               } catch (NBTException var10) {
                  this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Entity!", x, y);
               }
            }
         } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_TEXT) {
            this.drawHoveringText(NEWLINE_SPLITTER.splitToList(hoverevent.getValue().getFormattedText()), x, y);
         } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
            StatBase statbase = StatList.getOneShotStat(hoverevent.getValue().getUnformattedText());
            if (statbase != null) {
               ITextComponent itextcomponent = statbase.getStatName();
               ITextComponent itextcomponent1 = new TextComponentTranslation("stats.tooltip.type." + (statbase.isAchievement() ? "achievement" : "statistic"), new Object[0]);
               itextcomponent1.getStyle().setItalic(Boolean.valueOf(true));
               String s1 = statbase instanceof Achievement ? ((Achievement)statbase).getDescription() : null;
               List list = Lists.newArrayList(new String[]{itextcomponent.getFormattedText(), itextcomponent1.getFormattedText()});
               if (s1 != null) {
                  list.addAll(this.fontRendererObj.listFormattedStringToWidth(s1, 150));
               }

               this.drawHoveringText(list, x, y);
            } else {
               this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid statistic/achievement!", x, y);
            }
         }

         GlStateManager.disableLighting();
      }

   }

   protected void setText(String var1, boolean var2) {
   }

   protected boolean handleComponentClick(ITextComponent var1) {
      if (component == null) {
         return false;
      } else {
         ClickEvent clickevent = component.getStyle().getClickEvent();
         if (isShiftKeyDown()) {
            if (component.getStyle().getInsertion() != null) {
               this.setText(component.getStyle().getInsertion(), false);
            }
         } else if (clickevent != null) {
            if (clickevent.getAction() == ClickEvent.Action.OPEN_URL) {
               if (!this.mc.gameSettings.chatLinks) {
                  return false;
               }

               try {
                  URI uri = new URI(clickevent.getValue());
                  String s = uri.getScheme();
                  if (s == null) {
                     throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                  }

                  if (!PROTOCOLS.contains(s.toLowerCase())) {
                     throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase());
                  }

                  if (this.mc.gameSettings.chatLinksPrompt) {
                     this.clickedLinkURI = uri;
                     this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickevent.getValue(), 31102009, false));
                  } else {
                     this.openWebLink(uri);
                  }
               } catch (URISyntaxException var5) {
                  LOGGER.error("Can't open url for {}", new Object[]{clickevent, var5});
               }
            } else if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE) {
               URI uri1 = (new File(clickevent.getValue())).toURI();
               this.openWebLink(uri1);
            } else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
               this.setText(clickevent.getValue(), true);
            } else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
               this.sendChatMessage(clickevent.getValue(), false);
            } else {
               LOGGER.error("Don't know how to handle {}", new Object[]{clickevent});
            }

            return true;
         }

         return false;
      }
   }

   public void sendChatMessage(String var1) {
      this.sendChatMessage(msg, true);
   }

   public void sendChatMessage(String var1, boolean var2) {
      if (addToChat) {
         this.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
      }

      if (ClientCommandHandler.instance.executeCommand(this.mc.player, msg) == 0) {
         this.mc.player.sendChatMessage(msg);
      }
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (mouseButton == 0) {
         for(int i = 0; i < this.buttonList.size(); ++i) {
            GuiButton guibutton = (GuiButton)this.buttonList.get(i);
            if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
               Pre event = new Pre(this, guibutton, this.buttonList);
               if (MinecraftForge.EVENT_BUS.post(event)) {
                  break;
               }

               guibutton = event.getButton();
               this.selectedButton = guibutton;
               guibutton.playPressSound(this.mc.getSoundHandler());
               this.actionPerformed(guibutton);
               if (this.equals(this.mc.currentScreen)) {
                  MinecraftForge.EVENT_BUS.post(new Post(this, event.getButton(), this.buttonList));
               }
            }
         }
      }

   }

   protected void mouseReleased(int var1, int var2, int var3) {
      if (this.selectedButton != null && state == 0) {
         this.selectedButton.mouseReleased(mouseX, mouseY);
         this.selectedButton = null;
      }

   }

   protected void mouseClickMove(int var1, int var2, int var3, long var4) {
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
   }

   public void setWorldAndResolution(Minecraft var1, int var2, int var3) {
      this.mc = mc;
      this.itemRender = mc.getRenderItem();
      this.fontRendererObj = mc.fontRendererObj;
      this.width = width;
      this.height = height;
      if (!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
         this.buttonList.clear();
         this.initGui();
      }

      MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
   }

   public void setGuiSize(int var1, int var2) {
      this.width = w;
      this.height = h;
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
      int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
      int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
      int k = Mouse.getEventButton();
      if (Mouse.getEventButtonState()) {
         if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
            return;
         }

         this.eventButton = k;
         this.lastMouseEvent = Minecraft.getSystemTime();
         this.mouseClicked(i, j, this.eventButton);
      } else if (k != -1) {
         if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) {
            return;
         }

         this.eventButton = -1;
         this.mouseReleased(i, j, k);
      } else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
         long l = Minecraft.getSystemTime() - this.lastMouseEvent;
         this.mouseClickMove(i, j, this.eventButton, l);
      }

   }

   public void handleKeyboardInput() throws IOException {
      char c0 = Keyboard.getEventCharacter();
      if (Keyboard.getEventKey() == 0 && c0 >= ' ' || Keyboard.getEventKeyState()) {
         this.keyTyped(c0, Keyboard.getEventKey());
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
         this.drawBackground(tint);
      }

   }

   public void drawBackground(int var1) {
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      Tessellator tessellator = Tessellator.getInstance();
      VertexBuffer vertexbuffer = tessellator.getBuffer();
      this.mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      float f = 32.0F;
      vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      vertexbuffer.pos(0.0D, (double)this.height, 0.0D).tex(0.0D, (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
      vertexbuffer.pos((double)this.width, (double)this.height, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)this.height / 32.0F + (float)tint)).color(64, 64, 64, 255).endVertex();
      vertexbuffer.pos((double)this.width, 0.0D, 0.0D).tex((double)((float)this.width / 32.0F), (double)tint).color(64, 64, 64, 255).endVertex();
      vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, (double)tint).color(64, 64, 64, 255).endVertex();
      tessellator.draw();
   }

   public boolean doesGuiPauseGame() {
      return true;
   }

   public void confirmClicked(boolean var1, int var2) {
      if (id == 31102009) {
         if (result) {
            this.openWebLink(this.clickedLinkURI);
         }

         this.clickedLinkURI = null;
         this.mc.displayGuiScreen(this);
      }

   }

   private void openWebLink(URI var1) {
      try {
         Class oclass = Class.forName("java.awt.Desktop");
         Object object = oclass.getMethod("getDesktop").invoke((Object)null);
         oclass.getMethod("browse", URI.class).invoke(object, url);
      } catch (Throwable var4) {
         Throwable throwable = var4.getCause();
         LOGGER.error("Couldn't open link: {}", new Object[]{throwable == null ? "<UNKNOWN>" : throwable.getMessage()});
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
      return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static boolean isKeyComboCtrlV(int var0) {
      return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static boolean isKeyComboCtrlC(int var0) {
      return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public static boolean isKeyComboCtrlA(int var0) {
      return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
   }

   public void onResize(Minecraft var1, int var2, int var3) {
      this.setWorldAndResolution(mcIn, w, h);
   }
}
