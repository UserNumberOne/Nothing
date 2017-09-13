package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

@SideOnly(Side.CLIENT)
public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
   private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Random RANDOM = new Random();
   private final float updateCounter;
   private String splashText;
   private GuiButton buttonResetDemo;
   private int panoramaTimer;
   private DynamicTexture viewportTexture;
   private final boolean mcoEnabled = true;
   private final Object threadLock = new Object();
   private String openGLWarning1;
   private String openGLWarning2;
   private String openGLWarningLink;
   private static final ResourceLocation SPLASH_TEXTS = new ResourceLocation("texts/splashes.txt");
   private static final ResourceLocation MINECRAFT_TITLE_TEXTURES = new ResourceLocation("textures/gui/title/minecraft.png");
   private static final ResourceLocation[] TITLE_PANORAMA_PATHS = new ResourceLocation[]{new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
   public static final String MORE_INFO_TEXT = "Please click " + TextFormatting.UNDERLINE + "here" + TextFormatting.RESET + " for more information.";
   private int openGLWarning2Width;
   private int openGLWarning1Width;
   private int openGLWarningX1;
   private int openGLWarningY1;
   private int openGLWarningX2;
   private int openGLWarningY2;
   private ResourceLocation backgroundTexture;
   private GuiButton realmsButton;
   private boolean hasCheckedForRealmsNotification;
   private GuiScreen realmsNotification;
   private GuiButton modButton;
   private NotificationModUpdateScreen modUpdateNotification;

   public GuiMainMenu() {
      this.openGLWarning2 = MORE_INFO_TEXT;
      this.splashText = "missingno";
      IResource var1 = null;

      try {
         ArrayList var2 = Lists.newArrayList();
         var1 = Minecraft.getMinecraft().getResourceManager().getResource(SPLASH_TEXTS);
         BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream(), Charsets.UTF_8));

         String var4;
         while((var4 = var3.readLine()) != null) {
            var4 = var4.trim();
            if (!var4.isEmpty()) {
               var2.add(var4);
            }
         }

         if (!var2.isEmpty()) {
            while(true) {
               this.splashText = (String)var2.get(RANDOM.nextInt(var2.size()));
               if (this.splashText.hashCode() != 125780783) {
                  break;
               }
            }
         }
      } catch (IOException var8) {
         ;
      } finally {
         IOUtils.closeQuietly(var1);
      }

      this.updateCounter = RANDOM.nextFloat();
      this.openGLWarning1 = "";
      if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
         this.openGLWarning1 = I18n.format("title.oldgl1");
         this.openGLWarning2 = I18n.format("title.oldgl2");
         this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
      }

   }

   private boolean areRealmsNotificationsEnabled() {
      return Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS) && this.realmsNotification != null;
   }

   public void updateScreen() {
      ++this.panoramaTimer;
      if (this.areRealmsNotificationsEnabled()) {
         this.realmsNotification.updateScreen();
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   protected void keyTyped(char var1, int var2) throws IOException {
   }

   public void initGui() {
      this.viewportTexture = new DynamicTexture(256, 256);
      this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
      Calendar var1 = Calendar.getInstance();
      var1.setTime(new Date());
      if (var1.get(2) + 1 == 12 && var1.get(5) == 24) {
         this.splashText = "Merry X-mas!";
      } else if (var1.get(2) + 1 == 1 && var1.get(5) == 1) {
         this.splashText = "Happy new year!";
      } else if (var1.get(2) + 1 == 10 && var1.get(5) == 31) {
         this.splashText = "OOoooOOOoooo! Spooky!";
      }

      boolean var2 = true;
      int var3 = this.height / 4 + 48;
      if (this.mc.isDemo()) {
         this.addDemoButtons(var3, 24);
      } else {
         this.addSingleplayerMultiplayerButtons(var3, 24);
      }

      this.buttonList.add(new GuiButton(0, this.width / 2 - 100, var3 + 72 + 12, 98, 20, I18n.format("menu.options")));
      this.buttonList.add(new GuiButton(4, this.width / 2 + 2, var3 + 72 + 12, 98, 20, I18n.format("menu.quit")));
      this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, var3 + 72 + 12));
      synchronized(this.threadLock) {
         this.openGLWarning1Width = this.fontRendererObj.getStringWidth(this.openGLWarning1);
         this.openGLWarning2Width = this.fontRendererObj.getStringWidth(this.openGLWarning2);
         int var5 = Math.max(this.openGLWarning1Width, this.openGLWarning2Width);
         this.openGLWarningX1 = (this.width - var5) / 2;
         this.openGLWarningY1 = ((GuiButton)this.buttonList.get(0)).yPosition - 24;
         this.openGLWarningX2 = this.openGLWarningX1 + var5;
         this.openGLWarningY2 = this.openGLWarningY1 + 24;
      }

      this.mc.setConnectedToRealms(false);
      if (Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS) && !this.hasCheckedForRealmsNotification) {
         RealmsBridge var8 = new RealmsBridge();
         this.realmsNotification = var8.getNotificationScreen(this);
         this.hasCheckedForRealmsNotification = true;
      }

      if (this.areRealmsNotificationsEnabled()) {
         this.realmsNotification.setGuiSize(this.width, this.height);
         this.realmsNotification.initGui();
      }

      this.modUpdateNotification = NotificationModUpdateScreen.init(this, this.modButton);
   }

   private void addSingleplayerMultiplayerButtons(int var1, int var2) {
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, var1, I18n.format("menu.singleplayer")));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 100, var1 + var2 * 1, I18n.format("menu.multiplayer")));
      this.realmsButton = this.addButton(new GuiButton(14, this.width / 2 + 2, var1 + var2 * 2, 98, 20, I18n.format("menu.online").replace("Minecraft", "").trim()));
      this.buttonList.add(this.modButton = new GuiButton(6, this.width / 2 - 100, var1 + var2 * 2, 98, 20, I18n.format("fml.menu.mods")));
   }

   private void addDemoButtons(int var1, int var2) {
      this.buttonList.add(new GuiButton(11, this.width / 2 - 100, var1, I18n.format("menu.playdemo")));
      this.buttonResetDemo = this.addButton(new GuiButton(12, this.width / 2 - 100, var1 + var2 * 1, I18n.format("menu.resetdemo")));
      ISaveFormat var3 = this.mc.getSaveLoader();
      WorldInfo var4 = var3.getWorldInfo("Demo_World");
      if (var4 == null) {
         this.buttonResetDemo.enabled = false;
      }

   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == 0) {
         this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
      }

      if (var1.id == 5) {
         this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
      }

      if (var1.id == 1) {
         this.mc.displayGuiScreen(new GuiWorldSelection(this));
      }

      if (var1.id == 2) {
         this.mc.displayGuiScreen(new GuiMultiplayer(this));
      }

      if (var1.id == 14 && this.realmsButton.visible) {
         this.switchToRealms();
      }

      if (var1.id == 4) {
         this.mc.shutdown();
      }

      if (var1.id == 6) {
         this.mc.displayGuiScreen(new GuiModList(this));
      }

      if (var1.id == 11) {
         this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.DEMO_WORLD_SETTINGS);
      }

      if (var1.id == 12) {
         ISaveFormat var2 = this.mc.getSaveLoader();
         WorldInfo var3 = var2.getWorldInfo("Demo_World");
         if (var3 != null) {
            this.mc.displayGuiScreen(new GuiYesNo(this, I18n.format("selectWorld.deleteQuestion"), "'" + var3.getWorldName() + "' " + I18n.format("selectWorld.deleteWarning"), I18n.format("selectWorld.deleteButton"), I18n.format("gui.cancel"), 12));
         }
      }

   }

   private void switchToRealms() {
      RealmsBridge var1 = new RealmsBridge();
      var1.switchToRealms(this);
   }

   public void confirmClicked(boolean var1, int var2) {
      if (var1 && var2 == 12) {
         ISaveFormat var6 = this.mc.getSaveLoader();
         var6.flushCache();
         var6.deleteWorldDirectory("Demo_World");
         this.mc.displayGuiScreen(this);
      } else if (var2 == 13) {
         if (var1) {
            try {
               Class var3 = Class.forName("java.awt.Desktop");
               Object var4 = var3.getMethod("getDesktop").invoke((Object)null);
               var3.getMethod("browse", URI.class).invoke(var4, new URI(this.openGLWarningLink));
            } catch (Throwable var5) {
               LOGGER.error("Couldn't open link", var5);
            }
         }

         this.mc.displayGuiScreen(this);
      }

   }

   private void drawPanorama(int var1, int var2, float var3) {
      Tessellator var4 = Tessellator.getInstance();
      VertexBuffer var5 = var4.getBuffer();
      GlStateManager.matrixMode(5889);
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
      GlStateManager.matrixMode(5888);
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.disableCull();
      GlStateManager.depthMask(false);
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      boolean var6 = true;

      for(int var7 = 0; var7 < 64; ++var7) {
         GlStateManager.pushMatrix();
         float var8 = ((float)(var7 % 8) / 8.0F - 0.5F) / 64.0F;
         float var9 = ((float)(var7 / 8) / 8.0F - 0.5F) / 64.0F;
         float var10 = 0.0F;
         GlStateManager.translate(var8, var9, 0.0F);
         GlStateManager.rotate(MathHelper.sin(((float)this.panoramaTimer + var3) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(-((float)this.panoramaTimer + var3) * 0.1F, 0.0F, 1.0F, 0.0F);

         for(int var11 = 0; var11 < 6; ++var11) {
            GlStateManager.pushMatrix();
            if (var11 == 1) {
               GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var11 == 2) {
               GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var11 == 3) {
               GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var11 == 4) {
               GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (var11 == 5) {
               GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            this.mc.getTextureManager().bindTexture(TITLE_PANORAMA_PATHS[var11]);
            var5.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            int var12 = 255 / (var7 + 1);
            float var13 = 0.0F;
            var5.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, var12).endVertex();
            var5.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, var12).endVertex();
            var5.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, var12).endVertex();
            var5.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, var12).endVertex();
            var4.draw();
            GlStateManager.popMatrix();
         }

         GlStateManager.popMatrix();
         GlStateManager.colorMask(true, true, true, false);
      }

      var5.setTranslation(0.0D, 0.0D, 0.0D);
      GlStateManager.colorMask(true, true, true, true);
      GlStateManager.matrixMode(5889);
      GlStateManager.popMatrix();
      GlStateManager.matrixMode(5888);
      GlStateManager.popMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.enableCull();
      GlStateManager.enableDepth();
   }

   private void rotateAndBlurSkybox(float var1) {
      this.mc.getTextureManager().bindTexture(this.backgroundTexture);
      GlStateManager.glTexParameteri(3553, 10241, 9729);
      GlStateManager.glTexParameteri(3553, 10240, 9729);
      GlStateManager.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.colorMask(true, true, true, false);
      Tessellator var2 = Tessellator.getInstance();
      VertexBuffer var3 = var2.getBuffer();
      var3.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      GlStateManager.disableAlpha();
      boolean var4 = true;

      for(int var5 = 0; var5 < 3; ++var5) {
         float var6 = 1.0F / (float)(var5 + 1);
         int var7 = this.width;
         int var8 = this.height;
         float var9 = (float)(var5 - 1) / 256.0F;
         var3.pos((double)var7, (double)var8, (double)this.zLevel).tex((double)(0.0F + var9), 1.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
         var3.pos((double)var7, 0.0D, (double)this.zLevel).tex((double)(1.0F + var9), 1.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
         var3.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(1.0F + var9), 0.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
         var3.pos(0.0D, (double)var8, (double)this.zLevel).tex((double)(0.0F + var9), 0.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
      }

      var2.draw();
      GlStateManager.enableAlpha();
      GlStateManager.colorMask(true, true, true, true);
   }

   private void renderSkybox(int var1, int var2, float var3) {
      this.mc.getFramebuffer().unbindFramebuffer();
      GlStateManager.viewport(0, 0, 256, 256);
      this.drawPanorama(var1, var2, var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.mc.getFramebuffer().bindFramebuffer(true);
      GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
      float var4 = 120.0F / (float)(this.width > this.height ? this.width : this.height);
      float var5 = (float)this.height * var4 / 256.0F;
      float var6 = (float)this.width * var4 / 256.0F;
      int var7 = this.width;
      int var8 = this.height;
      Tessellator var9 = Tessellator.getInstance();
      VertexBuffer var10 = var9.getBuffer();
      var10.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var10.pos(0.0D, (double)var8, (double)this.zLevel).tex((double)(0.5F - var5), (double)(0.5F + var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var10.pos((double)var7, (double)var8, (double)this.zLevel).tex((double)(0.5F - var5), (double)(0.5F - var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var10.pos((double)var7, 0.0D, (double)this.zLevel).tex((double)(0.5F + var5), (double)(0.5F - var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var10.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(0.5F + var5), (double)(0.5F + var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var9.draw();
   }

   public void drawScreen(int var1, int var2, float var3) {
      GlStateManager.disableAlpha();
      this.renderSkybox(var1, var2, var3);
      GlStateManager.enableAlpha();
      boolean var4 = true;
      int var5 = this.width / 2 - 137;
      boolean var6 = true;
      this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
      this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
      this.mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if ((double)this.updateCounter < 1.0E-4D) {
         this.drawTexturedModalRect(var5 + 0, 30, 0, 0, 99, 44);
         this.drawTexturedModalRect(var5 + 99, 30, 129, 0, 27, 44);
         this.drawTexturedModalRect(var5 + 99 + 26, 30, 126, 0, 3, 44);
         this.drawTexturedModalRect(var5 + 99 + 26 + 3, 30, 99, 0, 26, 44);
         this.drawTexturedModalRect(var5 + 155, 30, 0, 45, 155, 44);
      } else {
         this.drawTexturedModalRect(var5 + 0, 30, 0, 0, 155, 44);
         this.drawTexturedModalRect(var5 + 155, 30, 0, 45, 155, 44);
      }

      this.splashText = ForgeHooksClient.renderMainMenu(this, this.fontRendererObj, this.width, this.height, this.splashText);
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
      GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
      float var7 = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
      var7 = var7 * 100.0F / (float)(this.fontRendererObj.getStringWidth(this.splashText) + 32);
      GlStateManager.scale(var7, var7, var7);
      this.drawCenteredString(this.fontRendererObj, this.splashText, 0, -8, -256);
      GlStateManager.popMatrix();
      String var8 = "Minecraft 1.10.2";
      if (this.mc.isDemo()) {
         var8 = var8 + " Demo";
      } else {
         var8 = var8 + ("release".equalsIgnoreCase(this.mc.getVersionType()) ? "" : "/" + this.mc.getVersionType());
      }

      List var9 = Lists.reverse(FMLCommonHandler.instance().getBrandings(true));

      for(int var10 = 0; var10 < var9.size(); ++var10) {
         String var11 = (String)var9.get(var10);
         if (!Strings.isNullOrEmpty(var11)) {
            this.drawString(this.fontRendererObj, var11, 2, this.height - (10 + var10 * (this.fontRendererObj.FONT_HEIGHT + 1)), 16777215);
         }
      }

      String var15 = "Copyright Mojang AB. Do not distribute!";
      this.drawString(this.fontRendererObj, "Copyright Mojang AB. Do not distribute!", this.width - this.fontRendererObj.getStringWidth("Copyright Mojang AB. Do not distribute!") - 2, this.height - 10, -1);
      if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty()) {
         drawRect(this.openGLWarningX1 - 2, this.openGLWarningY1 - 2, this.openGLWarningX2 + 2, this.openGLWarningY2 - 1, 1428160512);
         this.drawString(this.fontRendererObj, this.openGLWarning1, this.openGLWarningX1, this.openGLWarningY1, -1);
         this.drawString(this.fontRendererObj, this.openGLWarning2, (this.width - this.openGLWarning2Width) / 2, ((GuiButton)this.buttonList.get(0)).yPosition - 12, -1);
      }

      super.drawScreen(var1, var2, var3);
      if (this.areRealmsNotificationsEnabled()) {
         this.realmsNotification.drawScreen(var1, var2, var3);
      }

      this.modUpdateNotification.drawScreen(var1, var2, var3);
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      synchronized(this.threadLock) {
         if (!this.openGLWarning1.isEmpty() && var1 >= this.openGLWarningX1 && var1 <= this.openGLWarningX2 && var2 >= this.openGLWarningY1 && var2 <= this.openGLWarningY2) {
            GuiConfirmOpenLink var5 = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
            var5.disableSecurityWarning();
            this.mc.displayGuiScreen(var5);
         }
      }

      if (this.areRealmsNotificationsEnabled()) {
         this.realmsNotification.mouseClicked(var1, var2, var3);
      }

      ForgeHooksClient.mainMenuMouseClick(var1, var2, var3, this.fontRendererObj, this.width);
   }

   public void onGuiClosed() {
      if (this.realmsNotification != null) {
         this.realmsNotification.onGuiClosed();
      }

   }
}
