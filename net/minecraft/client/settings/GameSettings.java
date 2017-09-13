package net.minecraft.client.settings;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

@SideOnly(Side.CLIENT)
public class GameSettings {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final Type TYPE_LIST_STRING = new ParameterizedType() {
      public Type[] getActualTypeArguments() {
         return new Type[]{String.class};
      }

      public Type getRawType() {
         return List.class;
      }

      public Type getOwnerType() {
         return null;
      }
   };
   public static final Splitter COLON_SPLITTER = Splitter.on(':');
   private static final String[] GUISCALES = new String[]{"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
   private static final String[] PARTICLES = new String[]{"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
   private static final String[] AMBIENT_OCCLUSIONS = new String[]{"options.ao.off", "options.ao.min", "options.ao.max"};
   private static final String[] CLOUDS_TYPES = new String[]{"options.off", "options.graphics.fast", "options.graphics.fancy"};
   private static final String[] ATTACK_INDICATORS = new String[]{"options.off", "options.attack.crosshair", "options.attack.hotbar"};
   public float mouseSensitivity = 0.5F;
   public boolean invertMouse;
   public int renderDistanceChunks = -1;
   public boolean viewBobbing = true;
   public boolean anaglyph;
   public boolean fboEnable = true;
   public int limitFramerate = 120;
   public int clouds = 2;
   public boolean fancyGraphics = true;
   public int ambientOcclusion = 2;
   public List resourcePacks = Lists.newArrayList();
   public List incompatibleResourcePacks = Lists.newArrayList();
   public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
   public boolean chatColours = true;
   public boolean chatLinks = true;
   public boolean chatLinksPrompt = true;
   public float chatOpacity = 1.0F;
   public boolean snooperEnabled = true;
   public boolean fullScreen;
   public boolean enableVsync = true;
   public boolean useVbo = true;
   public boolean reducedDebugInfo;
   public boolean hideServerAddress;
   public boolean advancedItemTooltips;
   public boolean pauseOnLostFocus = true;
   private final Set setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
   public boolean touchscreen;
   public EnumHandSide mainHand = EnumHandSide.RIGHT;
   public int overrideWidth;
   public int overrideHeight;
   public boolean heldItemTooltips = true;
   public float chatScale = 1.0F;
   public float chatWidth = 1.0F;
   public float chatHeightUnfocused = 0.44366196F;
   public float chatHeightFocused = 1.0F;
   public boolean showInventoryAchievementHint = true;
   public int mipmapLevels = 4;
   private final Map soundLevels = Maps.newEnumMap(SoundCategory.class);
   public boolean useNativeTransport = true;
   public boolean entityShadows = true;
   public int attackIndicator = 1;
   public boolean enableWeakAttacks;
   public boolean showSubtitles;
   public boolean realmsNotifications = true;
   public boolean autoJump = true;
   public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
   public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
   public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
   public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
   public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
   public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
   public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
   public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
   public KeyBinding keyBindSwapHands = new KeyBinding("key.swapHands", 33, "key.categories.inventory");
   public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.inventory");
   public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
   public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
   public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
   public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
   public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
   public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
   public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
   public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
   public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
   public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
   public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
   public KeyBinding[] keyBindsHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
   public KeyBinding[] keyBindings;
   protected Minecraft mc;
   private File optionsFile;
   public EnumDifficulty difficulty;
   public boolean hideGUI;
   public int thirdPersonView;
   public boolean showDebugInfo;
   public boolean showDebugProfilerChart;
   public boolean showLagometer;
   public String lastServer;
   public boolean smoothCamera;
   public boolean debugCamEnable;
   public float fovSetting;
   public float gammaSetting;
   public float saturation;
   public int guiScale;
   public int particleSetting;
   public String language;
   public boolean forceUnicodeFont;
   private boolean needsResourceRefresh = false;

   public GameSettings(Minecraft var1, File var2) {
      this.setForgeKeybindProperties();
      this.keyBindings = (KeyBinding[])ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines, this.keyBindSwapHands}, this.keyBindsHotbar);
      this.difficulty = EnumDifficulty.NORMAL;
      this.lastServer = "";
      this.fovSetting = 70.0F;
      this.language = "en_US";
      this.mc = var1;
      this.optionsFile = new File(var2, "options.txt");
      if (var1.isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
         GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
      } else {
         GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
      }

      this.renderDistanceChunks = var1.isJava64bit() ? 12 : 8;
      this.loadOptions();
   }

   public GameSettings() {
      this.setForgeKeybindProperties();
      this.keyBindings = (KeyBinding[])ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines, this.keyBindSwapHands}, this.keyBindsHotbar);
      this.difficulty = EnumDifficulty.NORMAL;
      this.lastServer = "";
      this.fovSetting = 70.0F;
      this.language = "en_US";
   }

   public static String getKeyDisplayString(int var0) {
      return var0 < 0 ? I18n.format("key.mouseButton", var0 + 101) : (var0 < 256 ? Keyboard.getKeyName(var0) : String.format("%c", (char)(var0 - 256)).toUpperCase());
   }

   public static boolean isKeyDown(KeyBinding var0) {
      int var1 = var0.getKeyCode();
      return var1 != 0 && var1 < 256 ? (var1 < 0 ? Mouse.isButtonDown(var1 + 100) : Keyboard.isKeyDown(var1)) : false;
   }

   public void setOptionKeyBinding(KeyBinding var1, int var2) {
      var1.setKeyCode(var2);
      this.saveOptions();
   }

   public void setOptionFloatValue(GameSettings.Options var1, float var2) {
      if (var1 == GameSettings.Options.SENSITIVITY) {
         this.mouseSensitivity = var2;
      }

      if (var1 == GameSettings.Options.FOV) {
         this.fovSetting = var2;
      }

      if (var1 == GameSettings.Options.GAMMA) {
         this.gammaSetting = var2;
      }

      if (var1 == GameSettings.Options.FRAMERATE_LIMIT) {
         this.limitFramerate = (int)var2;
      }

      if (var1 == GameSettings.Options.CHAT_OPACITY) {
         this.chatOpacity = var2;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (var1 == GameSettings.Options.CHAT_HEIGHT_FOCUSED) {
         this.chatHeightFocused = var2;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (var1 == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED) {
         this.chatHeightUnfocused = var2;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (var1 == GameSettings.Options.CHAT_WIDTH) {
         this.chatWidth = var2;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (var1 == GameSettings.Options.CHAT_SCALE) {
         this.chatScale = var2;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (var1 == GameSettings.Options.MIPMAP_LEVELS) {
         int var3 = this.mipmapLevels;
         this.mipmapLevels = (int)var2;
         if ((float)var3 != var2) {
            this.mc.getTextureMapBlocks().setMipmapLevels(this.mipmapLevels);
            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.mc.getTextureMapBlocks().setBlurMipmapDirect(false, this.mipmapLevels > 0);
            this.needsResourceRefresh = true;
         }
      }

      if (var1 == GameSettings.Options.RENDER_DISTANCE) {
         this.renderDistanceChunks = (int)var2;
         this.mc.renderGlobal.setDisplayListEntitiesDirty();
      }

   }

   public void setOptionValue(GameSettings.Options var1, int var2) {
      if (var1 == GameSettings.Options.RENDER_DISTANCE) {
         this.setOptionFloatValue(var1, MathHelper.clamp((float)(this.renderDistanceChunks + var2), var1.getValueMin(), var1.getValueMax()));
      }

      if (var1 == GameSettings.Options.MAIN_HAND) {
         this.mainHand = this.mainHand.opposite();
      }

      if (var1 == GameSettings.Options.INVERT_MOUSE) {
         this.invertMouse = !this.invertMouse;
      }

      if (var1 == GameSettings.Options.GUI_SCALE) {
         this.guiScale = this.guiScale + var2 & 3;
      }

      if (var1 == GameSettings.Options.PARTICLES) {
         this.particleSetting = (this.particleSetting + var2) % 3;
      }

      if (var1 == GameSettings.Options.VIEW_BOBBING) {
         this.viewBobbing = !this.viewBobbing;
      }

      if (var1 == GameSettings.Options.RENDER_CLOUDS) {
         this.clouds = (this.clouds + var2) % 3;
      }

      if (var1 == GameSettings.Options.FORCE_UNICODE_FONT) {
         this.forceUnicodeFont = !this.forceUnicodeFont;
         this.mc.fontRendererObj.setUnicodeFlag(this.mc.getLanguageManager().isCurrentLocaleUnicode() || this.forceUnicodeFont);
      }

      if (var1 == GameSettings.Options.FBO_ENABLE) {
         this.fboEnable = !this.fboEnable;
      }

      if (var1 == GameSettings.Options.ANAGLYPH) {
         this.anaglyph = !this.anaglyph;
         this.mc.refreshResources();
      }

      if (var1 == GameSettings.Options.GRAPHICS) {
         this.fancyGraphics = !this.fancyGraphics;
         this.mc.renderGlobal.loadRenderers();
      }

      if (var1 == GameSettings.Options.AMBIENT_OCCLUSION) {
         this.ambientOcclusion = (this.ambientOcclusion + var2) % 3;
         this.mc.renderGlobal.loadRenderers();
      }

      if (var1 == GameSettings.Options.CHAT_VISIBILITY) {
         this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((this.chatVisibility.getChatVisibility() + var2) % 3);
      }

      if (var1 == GameSettings.Options.CHAT_COLOR) {
         this.chatColours = !this.chatColours;
      }

      if (var1 == GameSettings.Options.CHAT_LINKS) {
         this.chatLinks = !this.chatLinks;
      }

      if (var1 == GameSettings.Options.CHAT_LINKS_PROMPT) {
         this.chatLinksPrompt = !this.chatLinksPrompt;
      }

      if (var1 == GameSettings.Options.SNOOPER_ENABLED) {
         this.snooperEnabled = !this.snooperEnabled;
      }

      if (var1 == GameSettings.Options.TOUCHSCREEN) {
         this.touchscreen = !this.touchscreen;
      }

      if (var1 == GameSettings.Options.USE_FULLSCREEN) {
         this.fullScreen = !this.fullScreen;
         if (this.mc.isFullScreen() != this.fullScreen) {
            this.mc.toggleFullscreen();
         }
      }

      if (var1 == GameSettings.Options.ENABLE_VSYNC) {
         this.enableVsync = !this.enableVsync;
         Display.setVSyncEnabled(this.enableVsync);
      }

      if (var1 == GameSettings.Options.USE_VBO) {
         this.useVbo = !this.useVbo;
         this.mc.renderGlobal.loadRenderers();
      }

      if (var1 == GameSettings.Options.REDUCED_DEBUG_INFO) {
         this.reducedDebugInfo = !this.reducedDebugInfo;
      }

      if (var1 == GameSettings.Options.ENTITY_SHADOWS) {
         this.entityShadows = !this.entityShadows;
      }

      if (var1 == GameSettings.Options.ATTACK_INDICATOR) {
         this.attackIndicator = (this.attackIndicator + var2) % 3;
      }

      if (var1 == GameSettings.Options.SHOW_SUBTITLES) {
         this.showSubtitles = !this.showSubtitles;
      }

      if (var1 == GameSettings.Options.REALMS_NOTIFICATIONS) {
         this.realmsNotifications = !this.realmsNotifications;
      }

      if (var1 == GameSettings.Options.AUTO_JUMP) {
         this.autoJump = !this.autoJump;
      }

      this.saveOptions();
   }

   public float getOptionFloatValue(GameSettings.Options var1) {
      return var1 == GameSettings.Options.FOV ? this.fovSetting : (var1 == GameSettings.Options.GAMMA ? this.gammaSetting : (var1 == GameSettings.Options.SATURATION ? this.saturation : (var1 == GameSettings.Options.SENSITIVITY ? this.mouseSensitivity : (var1 == GameSettings.Options.CHAT_OPACITY ? this.chatOpacity : (var1 == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? this.chatHeightFocused : (var1 == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? this.chatHeightUnfocused : (var1 == GameSettings.Options.CHAT_SCALE ? this.chatScale : (var1 == GameSettings.Options.CHAT_WIDTH ? this.chatWidth : (var1 == GameSettings.Options.FRAMERATE_LIMIT ? (float)this.limitFramerate : (var1 == GameSettings.Options.MIPMAP_LEVELS ? (float)this.mipmapLevels : (var1 == GameSettings.Options.RENDER_DISTANCE ? (float)this.renderDistanceChunks : 0.0F)))))))))));
   }

   public boolean getOptionOrdinalValue(GameSettings.Options var1) {
      switch(var1) {
      case INVERT_MOUSE:
         return this.invertMouse;
      case VIEW_BOBBING:
         return this.viewBobbing;
      case ANAGLYPH:
         return this.anaglyph;
      case FBO_ENABLE:
         return this.fboEnable;
      case CHAT_COLOR:
         return this.chatColours;
      case CHAT_LINKS:
         return this.chatLinks;
      case CHAT_LINKS_PROMPT:
         return this.chatLinksPrompt;
      case SNOOPER_ENABLED:
         return this.snooperEnabled;
      case USE_FULLSCREEN:
         return this.fullScreen;
      case ENABLE_VSYNC:
         return this.enableVsync;
      case USE_VBO:
         return this.useVbo;
      case TOUCHSCREEN:
         return this.touchscreen;
      case FORCE_UNICODE_FONT:
         return this.forceUnicodeFont;
      case REDUCED_DEBUG_INFO:
         return this.reducedDebugInfo;
      case ENTITY_SHADOWS:
         return this.entityShadows;
      case SHOW_SUBTITLES:
         return this.showSubtitles;
      case REALMS_NOTIFICATIONS:
         return this.realmsNotifications;
      case ENABLE_WEAK_ATTACKS:
         return this.enableWeakAttacks;
      case AUTO_JUMP:
         return this.autoJump;
      default:
         return false;
      }
   }

   private static String getTranslation(String[] var0, int var1) {
      if (var1 < 0 || var1 >= var0.length) {
         var1 = 0;
      }

      return I18n.format(var0[var1]);
   }

   public String getKeyBinding(GameSettings.Options var1) {
      String var2 = I18n.format(var1.getEnumString()) + ": ";
      if (var1.getEnumFloat()) {
         float var6 = this.getOptionFloatValue(var1);
         float var4 = var1.normalizeValue(var6);
         return var1 == GameSettings.Options.SENSITIVITY ? (var4 == 0.0F ? var2 + I18n.format("options.sensitivity.min") : (var4 == 1.0F ? var2 + I18n.format("options.sensitivity.max") : var2 + (int)(var4 * 200.0F) + "%")) : (var1 == GameSettings.Options.FOV ? (var6 == 70.0F ? var2 + I18n.format("options.fov.min") : (var6 == 110.0F ? var2 + I18n.format("options.fov.max") : var2 + (int)var6)) : (var1 == GameSettings.Options.FRAMERATE_LIMIT ? (var6 == var1.valueMax ? var2 + I18n.format("options.framerateLimit.max") : var2 + (int)var6 + " fps") : (var1 == GameSettings.Options.RENDER_CLOUDS ? (var6 == var1.valueMin ? var2 + I18n.format("options.cloudHeight.min") : var2 + ((int)var6 + 128)) : (var1 == GameSettings.Options.GAMMA ? (var4 == 0.0F ? var2 + I18n.format("options.gamma.min") : (var4 == 1.0F ? var2 + I18n.format("options.gamma.max") : var2 + "+" + (int)(var4 * 100.0F) + "%")) : (var1 == GameSettings.Options.SATURATION ? var2 + (int)(var4 * 400.0F) + "%" : (var1 == GameSettings.Options.CHAT_OPACITY ? var2 + (int)(var4 * 90.0F + 10.0F) + "%" : (var1 == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? var2 + GuiNewChat.calculateChatboxHeight(var4) + "px" : (var1 == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? var2 + GuiNewChat.calculateChatboxHeight(var4) + "px" : (var1 == GameSettings.Options.CHAT_WIDTH ? var2 + GuiNewChat.calculateChatboxWidth(var4) + "px" : (var1 == GameSettings.Options.RENDER_DISTANCE ? var2 + (int)var6 + " chunks" : (var1 == GameSettings.Options.MIPMAP_LEVELS ? (var6 == 0.0F ? var2 + I18n.format("options.off") : var2 + (int)var6) : (var4 == 0.0F ? var2 + I18n.format("options.off") : var2 + (int)(var4 * 100.0F) + "%"))))))))))));
      } else if (var1.getEnumBoolean()) {
         boolean var5 = this.getOptionOrdinalValue(var1);
         return var5 ? var2 + I18n.format("options.on") : var2 + I18n.format("options.off");
      } else if (var1 == GameSettings.Options.MAIN_HAND) {
         return var2 + this.mainHand;
      } else if (var1 == GameSettings.Options.GUI_SCALE) {
         return var2 + getTranslation(GUISCALES, this.guiScale);
      } else if (var1 == GameSettings.Options.CHAT_VISIBILITY) {
         return var2 + I18n.format(this.chatVisibility.getResourceKey());
      } else if (var1 == GameSettings.Options.PARTICLES) {
         return var2 + getTranslation(PARTICLES, this.particleSetting);
      } else if (var1 == GameSettings.Options.AMBIENT_OCCLUSION) {
         return var2 + getTranslation(AMBIENT_OCCLUSIONS, this.ambientOcclusion);
      } else if (var1 == GameSettings.Options.RENDER_CLOUDS) {
         return var2 + getTranslation(CLOUDS_TYPES, this.clouds);
      } else if (var1 == GameSettings.Options.GRAPHICS) {
         if (this.fancyGraphics) {
            return var2 + I18n.format("options.graphics.fancy");
         } else {
            String var3 = "options.graphics.fast";
            return var2 + I18n.format("options.graphics.fast");
         }
      } else {
         return var1 == GameSettings.Options.ATTACK_INDICATOR ? var2 + getTranslation(ATTACK_INDICATORS, this.attackIndicator) : var2;
      }
   }

   public void loadOptions() {
      try {
         if (!this.optionsFile.exists()) {
            return;
         }

         this.soundLevels.clear();
         List var1 = IOUtils.readLines(new FileInputStream(this.optionsFile));
         NBTTagCompound var2 = new NBTTagCompound();

         for(String var4 : var1) {
            try {
               Iterator var5 = COLON_SPLITTER.omitEmptyStrings().limit(2).split(var4).iterator();
               var2.setString((String)var5.next(), (String)var5.next());
            } catch (Exception var11) {
               LOGGER.warn("Skipping bad option: {}", new Object[]{var4});
            }
         }

         var2 = this.dataFix(var2);

         for(String var16 : var2.getKeySet()) {
            String var17 = var2.getString(var16);

            try {
               if ("mouseSensitivity".equals(var16)) {
                  this.mouseSensitivity = this.parseFloat(var17);
               }

               if ("fov".equals(var16)) {
                  this.fovSetting = this.parseFloat(var17) * 40.0F + 70.0F;
               }

               if ("gamma".equals(var16)) {
                  this.gammaSetting = this.parseFloat(var17);
               }

               if ("saturation".equals(var16)) {
                  this.saturation = this.parseFloat(var17);
               }

               if ("invertYMouse".equals(var16)) {
                  this.invertMouse = "true".equals(var17);
               }

               if ("renderDistance".equals(var16)) {
                  this.renderDistanceChunks = Integer.parseInt(var17);
               }

               if ("guiScale".equals(var16)) {
                  this.guiScale = Integer.parseInt(var17);
               }

               if ("particles".equals(var16)) {
                  this.particleSetting = Integer.parseInt(var17);
               }

               if ("bobView".equals(var16)) {
                  this.viewBobbing = "true".equals(var17);
               }

               if ("anaglyph3d".equals(var16)) {
                  this.anaglyph = "true".equals(var17);
               }

               if ("maxFps".equals(var16)) {
                  this.limitFramerate = Integer.parseInt(var17);
               }

               if ("fboEnable".equals(var16)) {
                  this.fboEnable = "true".equals(var17);
               }

               if ("difficulty".equals(var16)) {
                  this.difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(var17));
               }

               if ("fancyGraphics".equals(var16)) {
                  this.fancyGraphics = "true".equals(var17);
               }

               if ("ao".equals(var16)) {
                  if ("true".equals(var17)) {
                     this.ambientOcclusion = 2;
                  } else if ("false".equals(var17)) {
                     this.ambientOcclusion = 0;
                  } else {
                     this.ambientOcclusion = Integer.parseInt(var17);
                  }
               }

               if ("renderClouds".equals(var16)) {
                  if ("true".equals(var17)) {
                     this.clouds = 2;
                  } else if ("false".equals(var17)) {
                     this.clouds = 0;
                  } else if ("fast".equals(var17)) {
                     this.clouds = 1;
                  }
               }

               if ("attackIndicator".equals(var16)) {
                  if ("0".equals(var17)) {
                     this.attackIndicator = 0;
                  } else if ("1".equals(var17)) {
                     this.attackIndicator = 1;
                  } else if ("2".equals(var17)) {
                     this.attackIndicator = 2;
                  }
               }

               if ("resourcePacks".equals(var16)) {
                  this.resourcePacks = (List)GSON.fromJson(var17, TYPE_LIST_STRING);
                  if (this.resourcePacks == null) {
                     this.resourcePacks = Lists.newArrayList();
                  }
               }

               if ("incompatibleResourcePacks".equals(var16)) {
                  this.incompatibleResourcePacks = (List)GSON.fromJson(var17, TYPE_LIST_STRING);
                  if (this.incompatibleResourcePacks == null) {
                     this.incompatibleResourcePacks = Lists.newArrayList();
                  }
               }

               if ("lastServer".equals(var16)) {
                  this.lastServer = var17;
               }

               if ("lang".equals(var16)) {
                  this.language = var17;
               }

               if ("chatVisibility".equals(var16)) {
                  this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(var17));
               }

               if ("chatColors".equals(var16)) {
                  this.chatColours = "true".equals(var17);
               }

               if ("chatLinks".equals(var16)) {
                  this.chatLinks = "true".equals(var17);
               }

               if ("chatLinksPrompt".equals(var16)) {
                  this.chatLinksPrompt = "true".equals(var17);
               }

               if ("chatOpacity".equals(var16)) {
                  this.chatOpacity = this.parseFloat(var17);
               }

               if ("snooperEnabled".equals(var16)) {
                  this.snooperEnabled = "true".equals(var17);
               }

               if ("fullscreen".equals(var16)) {
                  this.fullScreen = "true".equals(var17);
               }

               if ("enableVsync".equals(var16)) {
                  this.enableVsync = "true".equals(var17);
               }

               if ("useVbo".equals(var16)) {
                  this.useVbo = "true".equals(var17);
               }

               if ("hideServerAddress".equals(var16)) {
                  this.hideServerAddress = "true".equals(var17);
               }

               if ("advancedItemTooltips".equals(var16)) {
                  this.advancedItemTooltips = "true".equals(var17);
               }

               if ("pauseOnLostFocus".equals(var16)) {
                  this.pauseOnLostFocus = "true".equals(var17);
               }

               if ("touchscreen".equals(var16)) {
                  this.touchscreen = "true".equals(var17);
               }

               if ("overrideHeight".equals(var16)) {
                  this.overrideHeight = Integer.parseInt(var17);
               }

               if ("overrideWidth".equals(var16)) {
                  this.overrideWidth = Integer.parseInt(var17);
               }

               if ("heldItemTooltips".equals(var16)) {
                  this.heldItemTooltips = "true".equals(var17);
               }

               if ("chatHeightFocused".equals(var16)) {
                  this.chatHeightFocused = this.parseFloat(var17);
               }

               if ("chatHeightUnfocused".equals(var16)) {
                  this.chatHeightUnfocused = this.parseFloat(var17);
               }

               if ("chatScale".equals(var16)) {
                  this.chatScale = this.parseFloat(var17);
               }

               if ("chatWidth".equals(var16)) {
                  this.chatWidth = this.parseFloat(var17);
               }

               if ("showInventoryAchievementHint".equals(var16)) {
                  this.showInventoryAchievementHint = "true".equals(var17);
               }

               if ("mipmapLevels".equals(var16)) {
                  this.mipmapLevels = Integer.parseInt(var17);
               }

               if ("forceUnicodeFont".equals(var16)) {
                  this.forceUnicodeFont = "true".equals(var17);
               }

               if ("reducedDebugInfo".equals(var16)) {
                  this.reducedDebugInfo = "true".equals(var17);
               }

               if ("useNativeTransport".equals(var16)) {
                  this.useNativeTransport = "true".equals(var17);
               }

               if ("entityShadows".equals(var16)) {
                  this.entityShadows = "true".equals(var17);
               }

               if ("mainHand".equals(var16)) {
                  this.mainHand = "left".equals(var17) ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
               }

               if ("showSubtitles".equals(var16)) {
                  this.showSubtitles = "true".equals(var17);
               }

               if ("realmsNotifications".equals(var16)) {
                  this.realmsNotifications = "true".equals(var17);
               }

               if ("enableWeakAttacks".equals(var16)) {
                  this.enableWeakAttacks = "true".equals(var17);
               }

               if ("autoJump".equals(var16)) {
                  this.autoJump = "true".equals(var17);
               }

               for(KeyBinding var9 : this.keyBindings) {
                  if (var16.equals("key_" + var9.getKeyDescription())) {
                     if (var17.indexOf(58) != -1) {
                        String[] var10 = var17.split(":");
                        var9.setKeyModifierAndCode(KeyModifier.valueFromString(var10[1]), Integer.parseInt(var10[0]));
                     } else {
                        var9.setKeyModifierAndCode(KeyModifier.NONE, Integer.parseInt(var17));
                     }
                  }
               }

               for(SoundCategory var24 : SoundCategory.values()) {
                  if (var16.equals("soundCategory_" + var24.getName())) {
                     this.soundLevels.put(var24, Float.valueOf(this.parseFloat(var17)));
                  }
               }

               for(EnumPlayerModelParts var25 : EnumPlayerModelParts.values()) {
                  if (var16.equals("modelPart_" + var25.getPartName())) {
                     this.setModelPartEnabled(var25, "true".equals(var17));
                  }
               }
            } catch (Exception var12) {
               LOGGER.warn("Skipping bad option: {}:{}", new Object[]{var16, var17});
            }
         }

         KeyBinding.resetKeyBindingArrayAndHash();
      } catch (Exception var13) {
         LOGGER.error("Failed to load options", var13);
      }

   }

   private NBTTagCompound dataFix(NBTTagCompound var1) {
      int var2 = 0;

      try {
         var2 = Integer.parseInt(var1.getString("version"));
      } catch (RuntimeException var4) {
         ;
      }

      return this.mc.getDataFixer().process(FixTypes.OPTIONS, var1, var2);
   }

   private float parseFloat(String var1) {
      return "true".equals(var1) ? 1.0F : ("false".equals(var1) ? 0.0F : Float.parseFloat(var1));
   }

   public void saveOptions() {
      if (!FMLClientHandler.instance().isLoading()) {
         PrintWriter var1 = null;

         try {
            var1 = new PrintWriter(new FileWriter(this.optionsFile));
            var1.println("version:512");
            var1.println("invertYMouse:" + this.invertMouse);
            var1.println("mouseSensitivity:" + this.mouseSensitivity);
            var1.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
            var1.println("gamma:" + this.gammaSetting);
            var1.println("saturation:" + this.saturation);
            var1.println("renderDistance:" + this.renderDistanceChunks);
            var1.println("guiScale:" + this.guiScale);
            var1.println("particles:" + this.particleSetting);
            var1.println("bobView:" + this.viewBobbing);
            var1.println("anaglyph3d:" + this.anaglyph);
            var1.println("maxFps:" + this.limitFramerate);
            var1.println("fboEnable:" + this.fboEnable);
            var1.println("difficulty:" + this.difficulty.getDifficultyId());
            var1.println("fancyGraphics:" + this.fancyGraphics);
            var1.println("ao:" + this.ambientOcclusion);
            switch(this.clouds) {
            case 0:
               var1.println("renderClouds:false");
               break;
            case 1:
               var1.println("renderClouds:fast");
               break;
            case 2:
               var1.println("renderClouds:true");
            }

            var1.println("resourcePacks:" + GSON.toJson(this.resourcePacks));
            var1.println("incompatibleResourcePacks:" + GSON.toJson(this.incompatibleResourcePacks));
            var1.println("lastServer:" + this.lastServer);
            var1.println("lang:" + this.language);
            var1.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
            var1.println("chatColors:" + this.chatColours);
            var1.println("chatLinks:" + this.chatLinks);
            var1.println("chatLinksPrompt:" + this.chatLinksPrompt);
            var1.println("chatOpacity:" + this.chatOpacity);
            var1.println("snooperEnabled:" + this.snooperEnabled);
            var1.println("fullscreen:" + this.fullScreen);
            var1.println("enableVsync:" + this.enableVsync);
            var1.println("useVbo:" + this.useVbo);
            var1.println("hideServerAddress:" + this.hideServerAddress);
            var1.println("advancedItemTooltips:" + this.advancedItemTooltips);
            var1.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            var1.println("touchscreen:" + this.touchscreen);
            var1.println("overrideWidth:" + this.overrideWidth);
            var1.println("overrideHeight:" + this.overrideHeight);
            var1.println("heldItemTooltips:" + this.heldItemTooltips);
            var1.println("chatHeightFocused:" + this.chatHeightFocused);
            var1.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            var1.println("chatScale:" + this.chatScale);
            var1.println("chatWidth:" + this.chatWidth);
            var1.println("showInventoryAchievementHint:" + this.showInventoryAchievementHint);
            var1.println("mipmapLevels:" + this.mipmapLevels);
            var1.println("forceUnicodeFont:" + this.forceUnicodeFont);
            var1.println("reducedDebugInfo:" + this.reducedDebugInfo);
            var1.println("useNativeTransport:" + this.useNativeTransport);
            var1.println("entityShadows:" + this.entityShadows);
            var1.println("mainHand:" + (this.mainHand == EnumHandSide.LEFT ? "left" : "right"));
            var1.println("attackIndicator:" + this.attackIndicator);
            var1.println("showSubtitles:" + this.showSubtitles);
            var1.println("realmsNotifications:" + this.realmsNotifications);
            var1.println("enableWeakAttacks:" + this.enableWeakAttacks);
            var1.println("autoJump:" + this.autoJump);

            for(KeyBinding var5 : this.keyBindings) {
               String var6 = "key_" + var5.getKeyDescription() + ":" + var5.getKeyCode();
               var1.println(var5.getKeyModifier() != KeyModifier.NONE ? var6 + ":" + var5.getKeyModifier() : var6);
            }

            for(SoundCategory var18 : SoundCategory.values()) {
               var1.println("soundCategory_" + var18.getName() + ":" + this.getSoundLevel(var18));
            }

            for(EnumPlayerModelParts var19 : EnumPlayerModelParts.values()) {
               var1.println("modelPart_" + var19.getPartName() + ":" + this.setModelParts.contains(var19));
            }
         } catch (Exception var10) {
            LOGGER.error("Failed to save options", var10);
         } finally {
            IOUtils.closeQuietly(var1);
         }

         this.sendSettingsToServer();
      }
   }

   public float getSoundLevel(SoundCategory var1) {
      return this.soundLevels.containsKey(var1) ? ((Float)this.soundLevels.get(var1)).floatValue() : 1.0F;
   }

   public void setSoundLevel(SoundCategory var1, float var2) {
      this.mc.getSoundHandler().setSoundLevel(var1, var2);
      this.soundLevels.put(var1, Float.valueOf(var2));
   }

   public void sendSettingsToServer() {
      if (this.mc.player != null) {
         int var1 = 0;

         for(EnumPlayerModelParts var3 : this.setModelParts) {
            var1 |= var3.getPartMask();
         }

         this.mc.player.connection.sendPacket(new CPacketClientSettings(this.language, this.renderDistanceChunks, this.chatVisibility, this.chatColours, var1, this.mainHand));
      }

   }

   public Set getModelParts() {
      return ImmutableSet.copyOf(this.setModelParts);
   }

   public void setModelPartEnabled(EnumPlayerModelParts var1, boolean var2) {
      if (var2) {
         this.setModelParts.add(var1);
      } else {
         this.setModelParts.remove(var1);
      }

      this.sendSettingsToServer();
   }

   public void switchModelPartEnabled(EnumPlayerModelParts var1) {
      if (this.getModelParts().contains(var1)) {
         this.setModelParts.remove(var1);
      } else {
         this.setModelParts.add(var1);
      }

      this.sendSettingsToServer();
   }

   public int shouldRenderClouds() {
      return this.renderDistanceChunks >= 4 ? this.clouds : 0;
   }

   public boolean isUsingNativeTransport() {
      return this.useNativeTransport;
   }

   private void setForgeKeybindProperties() {
      KeyConflictContext var1 = KeyConflictContext.IN_GAME;
      this.keyBindForward.setKeyConflictContext(var1);
      this.keyBindLeft.setKeyConflictContext(var1);
      this.keyBindBack.setKeyConflictContext(var1);
      this.keyBindRight.setKeyConflictContext(var1);
      this.keyBindJump.setKeyConflictContext(var1);
      this.keyBindSneak.setKeyConflictContext(var1);
      this.keyBindSprint.setKeyConflictContext(var1);
      this.keyBindAttack.setKeyConflictContext(var1);
      this.keyBindChat.setKeyConflictContext(var1);
      this.keyBindPlayerList.setKeyConflictContext(var1);
      this.keyBindCommand.setKeyConflictContext(var1);
      this.keyBindTogglePerspective.setKeyConflictContext(var1);
      this.keyBindSmoothCamera.setKeyConflictContext(var1);
      this.keyBindSwapHands.setKeyConflictContext(var1);
   }

   public void onGuiClosed() {
      if (this.needsResourceRefresh) {
         this.mc.scheduleResourcesRefresh();
         this.needsResourceRefresh = false;
      }

   }

   @SideOnly(Side.CLIENT)
   public static enum Options {
      INVERT_MOUSE("options.invertMouse", false, true),
      SENSITIVITY("options.sensitivity", true, false),
      FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
      GAMMA("options.gamma", true, false),
      SATURATION("options.saturation", true, false),
      RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
      VIEW_BOBBING("options.viewBobbing", false, true),
      ANAGLYPH("options.anaglyph", false, true),
      FRAMERATE_LIMIT("options.framerateLimit", true, false, 10.0F, 260.0F, 10.0F),
      FBO_ENABLE("options.fboEnable", false, true),
      RENDER_CLOUDS("options.renderClouds", false, false),
      GRAPHICS("options.graphics", false, false),
      AMBIENT_OCCLUSION("options.ao", false, false),
      GUI_SCALE("options.guiScale", false, false),
      PARTICLES("options.particles", false, false),
      CHAT_VISIBILITY("options.chat.visibility", false, false),
      CHAT_COLOR("options.chat.color", false, true),
      CHAT_LINKS("options.chat.links", false, true),
      CHAT_OPACITY("options.chat.opacity", true, false),
      CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
      SNOOPER_ENABLED("options.snooper", false, true),
      USE_FULLSCREEN("options.fullscreen", false, true),
      ENABLE_VSYNC("options.vsync", false, true),
      USE_VBO("options.vbo", false, true),
      TOUCHSCREEN("options.touchscreen", false, true),
      CHAT_SCALE("options.chat.scale", true, false),
      CHAT_WIDTH("options.chat.width", true, false),
      CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
      CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
      MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
      FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
      REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
      ENTITY_SHADOWS("options.entityShadows", false, true),
      MAIN_HAND("options.mainHand", false, false),
      ATTACK_INDICATOR("options.attackIndicator", false, false),
      ENABLE_WEAK_ATTACKS("options.enableWeakAttacks", false, true),
      SHOW_SUBTITLES("options.showSubtitles", false, true),
      REALMS_NOTIFICATIONS("options.realmsNotifications", false, true),
      AUTO_JUMP("options.autoJump", false, true);

      private final boolean enumFloat;
      private final boolean enumBoolean;
      private final String enumString;
      private final float valueStep;
      private float valueMin;
      private float valueMax;

      public static GameSettings.Options getEnumOptions(int var0) {
         for(GameSettings.Options var4 : values()) {
            if (var4.returnEnumOrdinal() == var0) {
               return var4;
            }
         }

         return null;
      }

      private Options(String var3, boolean var4, boolean var5) {
         this(var3, var4, var5, 0.0F, 1.0F, 0.0F);
      }

      private Options(String var3, boolean var4, boolean var5, float var6, float var7, float var8) {
         this.enumString = var3;
         this.enumFloat = var4;
         this.enumBoolean = var5;
         this.valueMin = var6;
         this.valueMax = var7;
         this.valueStep = var8;
      }

      public boolean getEnumFloat() {
         return this.enumFloat;
      }

      public boolean getEnumBoolean() {
         return this.enumBoolean;
      }

      public int returnEnumOrdinal() {
         return this.ordinal();
      }

      public String getEnumString() {
         return this.enumString;
      }

      public float getValueMin() {
         return this.valueMin;
      }

      public float getValueMax() {
         return this.valueMax;
      }

      public void setValueMax(float var1) {
         this.valueMax = var1;
      }

      public float normalizeValue(float var1) {
         return MathHelper.clamp((this.snapToStepClamp(var1) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
      }

      public float denormalizeValue(float var1) {
         return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp(var1, 0.0F, 1.0F));
      }

      public float snapToStepClamp(float var1) {
         var1 = this.snapToStep(var1);
         return MathHelper.clamp(var1, this.valueMin, this.valueMax);
      }

      private float snapToStep(float var1) {
         if (this.valueStep > 0.0F) {
            var1 = this.valueStep * (float)Math.round(var1 / this.valueStep);
         }

         return var1;
      }
   }
}
