package net.minecraft.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.AnimationMetadataSectionSerializer;
import net.minecraft.client.resources.data.FontMetadataSection;
import net.minecraft.client.resources.data.FontMetadataSectionSerializer;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.client.resources.data.LanguageMetadataSectionSerializer;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.client.resources.data.PackMetadataSectionSerializer;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSectionSerializer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.IStatStringFormat;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

@SideOnly(Side.CLIENT)
public class Minecraft implements IThreadListener, ISnooperInfo {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation LOCATION_MOJANG_PNG = new ResourceLocation("textures/gui/title/mojang.png");
   public static final boolean IS_RUNNING_ON_MAC = Util.getOSType() == Util.EnumOS.OSX;
   public static byte[] memoryReserve = new byte[10485760];
   private static final List MAC_DISPLAY_MODES = Lists.newArrayList(new DisplayMode[]{new DisplayMode(2560, 1600), new DisplayMode(2880, 1800)});
   private final File fileResourcepacks;
   private final PropertyMap twitchDetails;
   private final PropertyMap profileProperties;
   private ServerData currentServerData;
   public TextureManager renderEngine;
   private static Minecraft theMinecraft;
   private final DataFixer dataFixer;
   @Nullable
   public PlayerControllerMP playerController;
   private boolean fullscreen;
   private final boolean enableGLErrorChecking = true;
   private boolean hasCrashed;
   private CrashReport crashReporter;
   public int displayWidth;
   public int displayHeight;
   private boolean connectedToRealms;
   private final Timer timer = new Timer(20.0F);
   private final Snooper usageSnooper = new Snooper("client", this, MinecraftServer.getCurrentTimeMillis());
   public WorldClient world;
   public RenderGlobal renderGlobal;
   private RenderManager renderManager;
   private RenderItem renderItem;
   private ItemRenderer itemRenderer;
   public EntityPlayerSP player;
   @Nullable
   private Entity renderViewEntity;
   public Entity pointedEntity;
   public ParticleManager effectRenderer;
   private final Session session;
   private boolean isGamePaused;
   public FontRenderer fontRendererObj;
   public FontRenderer standardGalacticFontRenderer;
   @Nullable
   public GuiScreen currentScreen;
   public LoadingScreenRenderer loadingScreen;
   public EntityRenderer entityRenderer;
   public DebugRenderer debugRenderer;
   private int leftClickCounter;
   private final int tempDisplayWidth;
   private final int tempDisplayHeight;
   @Nullable
   private IntegratedServer theIntegratedServer;
   public GuiAchievement guiAchievement;
   public GuiIngame ingameGUI;
   public boolean skipRenderWorld;
   public RayTraceResult objectMouseOver;
   public GameSettings gameSettings;
   public MouseHelper mouseHelper;
   public final File mcDataDir;
   private final File fileAssets;
   private final String launchedVersion;
   private final String versionType;
   private final Proxy proxy;
   private ISaveFormat saveLoader;
   private static int debugFPS;
   private int rightClickDelayTimer;
   private String serverName;
   private int serverPort;
   public boolean inGameHasFocus;
   long systemTime = getSystemTime();
   private int joinPlayerCounter;
   public final FrameTimer frameTimer = new FrameTimer();
   long startNanoTime = System.nanoTime();
   private final boolean jvm64bit;
   private final boolean isDemo;
   @Nullable
   private NetworkManager myNetworkManager;
   private boolean integratedServerIsRunning;
   public final Profiler mcProfiler = new Profiler();
   private long debugCrashKeyPressTime = -1L;
   private IReloadableResourceManager mcResourceManager;
   private final MetadataSerializer metadataSerializer_ = new MetadataSerializer();
   private final List defaultResourcePacks = Lists.newArrayList();
   public final DefaultResourcePack mcDefaultResourcePack;
   private ResourcePackRepository mcResourcePackRepository;
   private LanguageManager mcLanguageManager;
   private BlockColors blockColors;
   private ItemColors itemColors;
   private Framebuffer framebufferMc;
   private TextureMap textureMapBlocks;
   private SoundHandler mcSoundHandler;
   private MusicTicker mcMusicTicker;
   private ResourceLocation mojangLogo;
   private final MinecraftSessionService sessionService;
   private SkinManager skinManager;
   private final Queue scheduledTasks = Queues.newArrayDeque();
   private final Thread mcThread = Thread.currentThread();
   private ModelManager modelManager;
   private BlockRendererDispatcher blockRenderDispatcher;
   volatile boolean running = true;
   public String debug = "";
   public boolean renderChunksMany = true;
   private long debugUpdateTime = getSystemTime();
   private int fpsCounter;
   private boolean actionKeyF3;
   long prevFrameTime = -1L;
   private String debugProfilerName = "root";

   public Minecraft(GameConfiguration var1) {
      theMinecraft = this;
      this.mcDataDir = var1.folderInfo.mcDataDir;
      this.fileAssets = var1.folderInfo.assetsDir;
      this.fileResourcepacks = var1.folderInfo.resourcePacksDir;
      this.launchedVersion = var1.gameInfo.version;
      this.versionType = var1.gameInfo.versionType;
      this.twitchDetails = var1.userInfo.userProperties;
      this.profileProperties = var1.userInfo.profileProperties;
      this.mcDefaultResourcePack = new DefaultResourcePack(var1.folderInfo.getAssetsIndex());
      this.proxy = var1.userInfo.proxy == null ? Proxy.NO_PROXY : var1.userInfo.proxy;
      this.sessionService = (new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
      this.session = var1.userInfo.session;
      LOGGER.info("Setting user: {}", new Object[]{this.session.getUsername()});
      this.isDemo = var1.gameInfo.isDemo;
      this.displayWidth = var1.displayInfo.width > 0 ? var1.displayInfo.width : 1;
      this.displayHeight = var1.displayInfo.height > 0 ? var1.displayInfo.height : 1;
      this.tempDisplayWidth = var1.displayInfo.width;
      this.tempDisplayHeight = var1.displayInfo.height;
      this.fullscreen = var1.displayInfo.fullscreen;
      this.jvm64bit = isJvm64bit();
      this.theIntegratedServer = null;
      if (var1.serverInfo.serverName != null) {
         this.serverName = var1.serverInfo.serverName;
         this.serverPort = var1.serverInfo.serverPort;
      }

      ImageIO.setUseCache(false);
      Bootstrap.register();
      this.dataFixer = DataFixesManager.createFixer();
   }

   public void run() {
      this.running = true;

      try {
         this.init();
      } catch (Throwable var11) {
         CrashReport var2 = CrashReport.makeCrashReport(var11, "Initializing game");
         var2.makeCategory("Initialization");
         this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(var2));
         return;
      }

      try {
         while(this.running) {
            if (this.hasCrashed && this.crashReporter != null) {
               this.displayCrashReport(this.crashReporter);
            } else {
               try {
                  this.runGameLoop();
               } catch (OutOfMemoryError var10) {
                  this.freeMemory();
                  this.displayGuiScreen(new GuiMemoryErrorScreen());
                  System.gc();
               }
            }
         }

         return;
      } catch (MinecraftError var12) {
         ;
      } catch (ReportedException var13) {
         this.addGraphicsAndWorldToCrashReport(var13.getCrashReport());
         this.freeMemory();
         LOGGER.fatal("Reported exception thrown!", var13);
         this.displayCrashReport(var13.getCrashReport());
      } catch (Throwable var14) {
         CrashReport var16 = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", var14));
         this.freeMemory();
         LOGGER.fatal("Unreported exception thrown!", var14);
         this.displayCrashReport(var16);
      } finally {
         this.shutdownMinecraftApplet();
      }

   }

   private void init() throws LWJGLException, IOException {
      this.gameSettings = new GameSettings(this, this.mcDataDir);
      this.defaultResourcePacks.add(this.mcDefaultResourcePack);
      this.startTimerHackThread();
      if (this.gameSettings.overrideHeight > 0 && this.gameSettings.overrideWidth > 0) {
         this.displayWidth = this.gameSettings.overrideWidth;
         this.displayHeight = this.gameSettings.overrideHeight;
      }

      LOGGER.info("LWJGL Version: {}", new Object[]{Sys.getVersion()});
      this.setWindowIcon();
      this.setInitialDisplayMode();
      this.createDisplay();
      OpenGlHelper.initializeTextures();
      this.framebufferMc = new Framebuffer(this.displayWidth, this.displayHeight, true);
      this.framebufferMc.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.registerMetadataSerializers();
      this.mcResourcePackRepository = new ResourcePackRepository(this.fileResourcepacks, new File(this.mcDataDir, "server-resource-packs"), this.mcDefaultResourcePack, this.metadataSerializer_, this.gameSettings);
      this.mcResourceManager = new SimpleReloadableResourceManager(this.metadataSerializer_);
      this.mcLanguageManager = new LanguageManager(this.metadataSerializer_, this.gameSettings.language);
      this.mcResourceManager.registerReloadListener(this.mcLanguageManager);
      FMLClientHandler.instance().beginMinecraftLoading(this, this.defaultResourcePacks, this.mcResourceManager);
      this.renderEngine = new TextureManager(this.mcResourceManager);
      this.mcResourceManager.registerReloadListener(this.renderEngine);
      SplashProgress.drawVanillaScreen(this.renderEngine);
      this.skinManager = new SkinManager(this.renderEngine, new File(this.fileAssets, "skins"), this.sessionService);
      this.saveLoader = new AnvilSaveConverter(new File(this.mcDataDir, "saves"), this.dataFixer);
      this.mcSoundHandler = new SoundHandler(this.mcResourceManager, this.gameSettings);
      this.mcResourceManager.registerReloadListener(this.mcSoundHandler);
      this.mcMusicTicker = new MusicTicker(this);
      this.fontRendererObj = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false);
      if (this.gameSettings.language != null) {
         this.fontRendererObj.setUnicodeFlag(this.isUnicode());
         this.fontRendererObj.setBidiFlag(this.mcLanguageManager.isCurrentLanguageBidirectional());
      }

      this.standardGalacticFontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), this.renderEngine, false);
      this.mcResourceManager.registerReloadListener(this.fontRendererObj);
      this.mcResourceManager.registerReloadListener(this.standardGalacticFontRenderer);
      this.mcResourceManager.registerReloadListener(new GrassColorReloadListener());
      this.mcResourceManager.registerReloadListener(new FoliageColorReloadListener());
      AchievementList.OPEN_INVENTORY.setStatStringFormatter(new IStatStringFormat() {
         public String formatString(String var1) {
            try {
               return String.format(var1, Minecraft.this.gameSettings.keyBindInventory.getDisplayName());
            } catch (Exception var3) {
               return "Error: " + var3.getLocalizedMessage();
            }
         }
      });
      this.mouseHelper = new MouseHelper();
      ProgressBar var1 = ProgressManager.push("Rendering Setup", 5, true);
      var1.step("GL Setup");
      this.checkGLError("Pre startup");
      GlStateManager.enableTexture2D();
      GlStateManager.shadeModel(7425);
      GlStateManager.clearDepth(1.0D);
      GlStateManager.enableDepth();
      GlStateManager.depthFunc(515);
      GlStateManager.enableAlpha();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.cullFace(GlStateManager.CullFace.BACK);
      GlStateManager.matrixMode(5889);
      GlStateManager.loadIdentity();
      GlStateManager.matrixMode(5888);
      this.checkGLError("Startup");
      var1.step("Loading Texture Map");
      this.textureMapBlocks = new TextureMap("textures", true);
      this.textureMapBlocks.setMipmapLevels(this.gameSettings.mipmapLevels);
      this.renderEngine.loadTickableTexture(TextureMap.LOCATION_BLOCKS_TEXTURE, this.textureMapBlocks);
      this.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      this.textureMapBlocks.setBlurMipmapDirect(false, this.gameSettings.mipmapLevels > 0);
      var1.step("Loading Model Manager");
      this.modelManager = new ModelManager(this.textureMapBlocks);
      this.mcResourceManager.registerReloadListener(this.modelManager);
      this.blockColors = BlockColors.init();
      this.itemColors = ItemColors.init(this.blockColors);
      var1.step("Loading Item Renderer");
      this.renderItem = new RenderItem(this.renderEngine, this.modelManager, this.itemColors);
      this.renderManager = new RenderManager(this.renderEngine, this.renderItem);
      this.itemRenderer = new ItemRenderer(this);
      this.mcResourceManager.registerReloadListener(this.renderItem);
      var1.step("Loading Entity Renderer");
      SplashProgress.pause();
      this.entityRenderer = new EntityRenderer(this, this.mcResourceManager);
      this.mcResourceManager.registerReloadListener(this.entityRenderer);
      this.blockRenderDispatcher = new BlockRendererDispatcher(this.modelManager.getBlockModelShapes(), this.blockColors);
      this.mcResourceManager.registerReloadListener(this.blockRenderDispatcher);
      this.renderGlobal = new RenderGlobal(this);
      this.mcResourceManager.registerReloadListener(this.renderGlobal);
      this.guiAchievement = new GuiAchievement(this);
      GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);
      this.effectRenderer = new ParticleManager(this.world, this.renderEngine);
      SplashProgress.resume();
      ProgressManager.pop(var1);
      FMLClientHandler.instance().finishMinecraftLoading();
      this.checkGLError("Post startup");
      this.ingameGUI = new GuiIngameForge(this);
      if (this.serverName != null) {
         FMLClientHandler.instance().connectToServerAtStartup(this.serverName, this.serverPort);
      } else {
         this.displayGuiScreen(new GuiMainMenu());
      }

      SplashProgress.clearVanillaResources(this.renderEngine, this.mojangLogo);
      this.mojangLogo = null;
      this.loadingScreen = new LoadingScreenRenderer(this);
      this.debugRenderer = new DebugRenderer(this);
      FMLClientHandler.instance().onInitializationComplete();
      if (this.gameSettings.fullScreen && !this.fullscreen) {
         this.toggleFullscreen();
      }

      try {
         Display.setVSyncEnabled(this.gameSettings.enableVsync);
      } catch (OpenGLException var3) {
         this.gameSettings.enableVsync = false;
         this.gameSettings.saveOptions();
      }

      this.renderGlobal.makeEntityOutlineShader();
   }

   private void registerMetadataSerializers() {
      this.metadataSerializer_.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
      this.metadataSerializer_.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
      this.metadataSerializer_.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
      this.metadataSerializer_.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
      this.metadataSerializer_.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
   }

   private void createDisplay() throws LWJGLException {
      Display.setResizable(true);
      Display.setTitle("Minecraft 1.10.2");

      try {
         Display.create((new PixelFormat()).withDepthBits(24));
      } catch (LWJGLException var4) {
         LOGGER.error("Couldn't set pixel format", var4);

         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var3) {
            ;
         }

         if (this.fullscreen) {
            this.updateDisplayMode();
         }

         Display.create();
      }

   }

   private void setInitialDisplayMode() throws LWJGLException {
      if (this.fullscreen) {
         Display.setFullscreen(true);
         DisplayMode var1 = Display.getDisplayMode();
         this.displayWidth = Math.max(1, var1.getWidth());
         this.displayHeight = Math.max(1, var1.getHeight());
      } else {
         Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
      }

   }

   private void setWindowIcon() {
      Util.EnumOS var1 = Util.getOSType();
      if (var1 != Util.EnumOS.OSX) {
         InputStream var2 = null;
         InputStream var3 = null;

         try {
            var2 = this.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
            var3 = this.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));
            if (var2 != null && var3 != null) {
               Display.setIcon(new ByteBuffer[]{this.readImageToBuffer(var2), this.readImageToBuffer(var3)});
            }
         } catch (IOException var8) {
            LOGGER.error("Couldn't set icon", var8);
         } finally {
            IOUtils.closeQuietly(var2);
            IOUtils.closeQuietly(var3);
         }
      }

   }

   private static boolean isJvm64bit() {
      String[] var0 = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

      for(String var4 : var0) {
         String var5 = System.getProperty(var4);
         if (var5 != null && var5.contains("64")) {
            return true;
         }
      }

      return false;
   }

   public Framebuffer getFramebuffer() {
      return this.framebufferMc;
   }

   public String getVersion() {
      return this.launchedVersion;
   }

   public String getVersionType() {
      return this.versionType;
   }

   private void startTimerHackThread() {
      Thread var1 = new Thread("Timer hack thread") {
         public void run() {
            while(Minecraft.this.running) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException var2) {
                  ;
               }
            }

         }
      };
      var1.setDaemon(true);
      var1.start();
   }

   public void crashed(CrashReport var1) {
      this.hasCrashed = true;
      this.crashReporter = var1;
   }

   public void displayCrashReport(CrashReport var1) {
      File var2 = new File(getMinecraft().mcDataDir, "crash-reports");
      File var3 = new File(var2, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
      Bootstrap.printToSYSOUT(var1.getCompleteReport());
      byte var4;
      if (var1.getFile() != null) {
         Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + var1.getFile());
         var4 = -1;
      } else if (var1.saveToFile(var3)) {
         Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + var3.getAbsolutePath());
         var4 = -1;
      } else {
         Bootstrap.printToSYSOUT("#@?@# Game crashed! Crash report could not be saved. #@?@#");
         var4 = -2;
      }

      FMLCommonHandler.instance().handleExit(var4);
   }

   public boolean isUnicode() {
      return this.mcLanguageManager.isCurrentLocaleUnicode() || this.gameSettings.forceUnicodeFont;
   }

   public void refreshResources() {
      ArrayList var1 = Lists.newArrayList(this.defaultResourcePacks);
      if (this.theIntegratedServer != null) {
         this.theIntegratedServer.reloadLootTables();
      }

      for(ResourcePackRepository.Entry var3 : this.mcResourcePackRepository.getRepositoryEntries()) {
         var1.add(var3.getResourcePack());
      }

      if (this.mcResourcePackRepository.getResourcePackInstance() != null) {
         var1.add(this.mcResourcePackRepository.getResourcePackInstance());
      }

      try {
         this.mcResourceManager.reloadResources(var1);
      } catch (RuntimeException var4) {
         LOGGER.info("Caught error stitching, removing all assigned resourcepacks", var4);
         var1.clear();
         var1.addAll(this.defaultResourcePacks);
         this.mcResourcePackRepository.setRepositories(Collections.emptyList());
         this.mcResourceManager.reloadResources(var1);
         this.gameSettings.resourcePacks.clear();
         this.gameSettings.incompatibleResourcePacks.clear();
         this.gameSettings.saveOptions();
      }

      this.mcLanguageManager.parseLanguageMetadata(var1);
      if (this.renderGlobal != null) {
         this.renderGlobal.loadRenderers();
      }

   }

   private ByteBuffer readImageToBuffer(InputStream var1) throws IOException {
      BufferedImage var2 = ImageIO.read(var1);
      int[] var3 = var2.getRGB(0, 0, var2.getWidth(), var2.getHeight(), (int[])null, 0, var2.getWidth());
      ByteBuffer var4 = ByteBuffer.allocate(4 * var3.length);

      for(int var8 : var3) {
         var4.putInt(var8 << 8 | var8 >> 24 & 255);
      }

      var4.flip();
      return var4;
   }

   private void updateDisplayMode() throws LWJGLException {
      HashSet var1 = Sets.newHashSet();
      Collections.addAll(var1, Display.getAvailableDisplayModes());
      DisplayMode var2 = Display.getDesktopDisplayMode();
      if (!var1.contains(var2) && Util.getOSType() == Util.EnumOS.OSX) {
         label52:
         for(DisplayMode var4 : MAC_DISPLAY_MODES) {
            boolean var5 = true;

            for(DisplayMode var7 : var1) {
               if (var7.getBitsPerPixel() == 32 && var7.getWidth() == var4.getWidth() && var7.getHeight() == var4.getHeight()) {
                  var5 = false;
                  break;
               }
            }

            if (!var5) {
               Iterator var8 = var1.iterator();

               DisplayMode var9;
               while(true) {
                  if (!var8.hasNext()) {
                     continue label52;
                  }

                  var9 = (DisplayMode)var8.next();
                  if (var9.getBitsPerPixel() == 32 && var9.getWidth() == var4.getWidth() / 2 && var9.getHeight() == var4.getHeight() / 2) {
                     break;
                  }
               }

               var2 = var9;
            }
         }
      }

      Display.setDisplayMode(var2);
      this.displayWidth = var2.getWidth();
      this.displayHeight = var2.getHeight();
   }

   public void drawSplashScreen(TextureManager var1) throws LWJGLException {
      ScaledResolution var2 = new ScaledResolution(this);
      int var3 = var2.getScaleFactor();
      Framebuffer var4 = new Framebuffer(var2.getScaledWidth() * var3, var2.getScaledHeight() * var3, true);
      var4.bindFramebuffer(false);
      GlStateManager.matrixMode(5889);
      GlStateManager.loadIdentity();
      GlStateManager.ortho(0.0D, (double)var2.getScaledWidth(), (double)var2.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
      GlStateManager.matrixMode(5888);
      GlStateManager.loadIdentity();
      GlStateManager.translate(0.0F, 0.0F, -2000.0F);
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      GlStateManager.disableDepth();
      GlStateManager.enableTexture2D();
      InputStream var5 = null;

      try {
         var5 = this.mcDefaultResourcePack.getInputStream(LOCATION_MOJANG_PNG);
         this.mojangLogo = var1.getDynamicTextureLocation("logo", new DynamicTexture(ImageIO.read(var5)));
         var1.bindTexture(this.mojangLogo);
      } catch (IOException var12) {
         LOGGER.error("Unable to load logo: {}", new Object[]{LOCATION_MOJANG_PNG, var12});
      } finally {
         IOUtils.closeQuietly(var5);
      }

      Tessellator var6 = Tessellator.getInstance();
      VertexBuffer var7 = var6.getBuffer();
      var7.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var7.pos(0.0D, (double)this.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
      var7.pos((double)this.displayWidth, (double)this.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
      var7.pos((double)this.displayWidth, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
      var7.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
      var6.draw();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      boolean var8 = true;
      boolean var9 = true;
      this.draw((var2.getScaledWidth() - 256) / 2, (var2.getScaledHeight() - 256) / 2, 0, 0, 256, 256, 255, 255, 255, 255);
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      var4.unbindFramebuffer();
      var4.framebufferRender(var2.getScaledWidth() * var3, var2.getScaledHeight() * var3);
      GlStateManager.enableAlpha();
      GlStateManager.alphaFunc(516, 0.1F);
      this.updateDisplay();
   }

   public void draw(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      VertexBuffer var11 = Tessellator.getInstance().getBuffer();
      var11.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      float var12 = 0.00390625F;
      float var13 = 0.00390625F;
      var11.pos((double)var1, (double)(var2 + var6), 0.0D).tex((double)((float)var3 * 0.00390625F), (double)((float)(var4 + var6) * 0.00390625F)).color(var7, var8, var9, var10).endVertex();
      var11.pos((double)(var1 + var5), (double)(var2 + var6), 0.0D).tex((double)((float)(var3 + var5) * 0.00390625F), (double)((float)(var4 + var6) * 0.00390625F)).color(var7, var8, var9, var10).endVertex();
      var11.pos((double)(var1 + var5), (double)var2, 0.0D).tex((double)((float)(var3 + var5) * 0.00390625F), (double)((float)var4 * 0.00390625F)).color(var7, var8, var9, var10).endVertex();
      var11.pos((double)var1, (double)var2, 0.0D).tex((double)((float)var3 * 0.00390625F), (double)((float)var4 * 0.00390625F)).color(var7, var8, var9, var10).endVertex();
      Tessellator.getInstance().draw();
   }

   public ISaveFormat getSaveLoader() {
      return this.saveLoader;
   }

   public void displayGuiScreen(@Nullable GuiScreen var1) {
      if (var1 == null && this.world == null) {
         var1 = new GuiMainMenu();
      } else if (var1 == null && this.player.getHealth() <= 0.0F) {
         var1 = new GuiGameOver((ITextComponent)null);
      }

      GuiScreen var2 = this.currentScreen;
      GuiOpenEvent var3 = new GuiOpenEvent(var1);
      if (!MinecraftForge.EVENT_BUS.post(var3)) {
         var1 = var3.getGui();
         if (var2 != null && var1 != var2) {
            var2.onGuiClosed();
         }

         if (var1 instanceof GuiMainMenu || var1 instanceof GuiMultiplayer) {
            this.gameSettings.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages();
         }

         this.currentScreen = var1;
         if (var1 != null) {
            this.setIngameNotInFocus();
            KeyBinding.unPressAllKeys();

            while(Mouse.next()) {
               ;
            }

            while(Keyboard.next()) {
               ;
            }

            ScaledResolution var4 = new ScaledResolution(this);
            int var5 = var4.getScaledWidth();
            int var6 = var4.getScaledHeight();
            var1.setWorldAndResolution(this, var5, var6);
            this.skipRenderWorld = false;
         } else {
            this.mcSoundHandler.resumeSounds();
            this.setIngameFocus();
         }

      }
   }

   private void checkGLError(String var1) {
      int var2 = GlStateManager.glGetError();
      if (var2 != 0) {
         String var3 = GLU.gluErrorString(var2);
         LOGGER.error("########## GL ERROR ##########");
         LOGGER.error("@ {}", new Object[]{var1});
         LOGGER.error("{}: {}", new Object[]{var2, var3});
      }

   }

   public void shutdownMinecraftApplet() {
      try {
         LOGGER.info("Stopping!");

         try {
            this.loadWorld((WorldClient)null);
         } catch (Throwable var5) {
            ;
         }

         this.mcSoundHandler.unloadSounds();
      } finally {
         Display.destroy();
         if (!this.hasCrashed) {
            System.exit(0);
         }

      }

      System.gc();
   }

   private void runGameLoop() throws IOException {
      long var1 = System.nanoTime();
      this.mcProfiler.startSection("root");
      if (Display.isCreated() && Display.isCloseRequested()) {
         this.shutdown();
      }

      if (this.isGamePaused && this.world != null) {
         float var3 = this.timer.renderPartialTicks;
         this.timer.updateTimer();
         this.timer.renderPartialTicks = var3;
      } else {
         this.timer.updateTimer();
      }

      this.mcProfiler.startSection("scheduledExecutables");
      synchronized(this.scheduledTasks) {
         while(!this.scheduledTasks.isEmpty()) {
            Util.runTask((FutureTask)this.scheduledTasks.poll(), LOGGER);
         }
      }

      this.mcProfiler.endSection();
      long var11 = System.nanoTime();
      this.mcProfiler.startSection("tick");

      for(int var5 = 0; var5 < this.timer.elapsedTicks; ++var5) {
         this.runTick();
      }

      this.mcProfiler.endStartSection("preRenderErrors");
      long var12 = System.nanoTime() - var11;
      this.checkGLError("Pre render");
      this.mcProfiler.endStartSection("sound");
      this.mcSoundHandler.setListener(this.player, this.timer.renderPartialTicks);
      this.mcProfiler.endSection();
      this.mcProfiler.startSection("render");
      GlStateManager.pushMatrix();
      GlStateManager.clear(16640);
      this.framebufferMc.bindFramebuffer(true);
      this.mcProfiler.startSection("display");
      GlStateManager.enableTexture2D();
      this.mcProfiler.endSection();
      if (!this.skipRenderWorld) {
         FMLCommonHandler.instance().onRenderTickStart(this.timer.renderPartialTicks);
         this.mcProfiler.endStartSection("gameRenderer");
         this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks, var1);
         this.mcProfiler.endSection();
         FMLCommonHandler.instance().onRenderTickEnd(this.timer.renderPartialTicks);
      }

      this.mcProfiler.endSection();
      if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart && !this.gameSettings.hideGUI) {
         if (!this.mcProfiler.profilingEnabled) {
            this.mcProfiler.clearProfiling();
         }

         this.mcProfiler.profilingEnabled = true;
         this.displayDebugInfo(var12);
      } else {
         this.mcProfiler.profilingEnabled = false;
         this.prevFrameTime = System.nanoTime();
      }

      this.guiAchievement.updateAchievementWindow();
      this.framebufferMc.unbindFramebuffer();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      this.entityRenderer.renderStreamIndicator(this.timer.renderPartialTicks);
      GlStateManager.popMatrix();
      this.mcProfiler.startSection("root");
      this.updateDisplay();
      Thread.yield();
      this.checkGLError("Post render");
      ++this.fpsCounter;
      this.isGamePaused = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.theIntegratedServer.getPublic();
      long var7 = System.nanoTime();
      this.frameTimer.addFrame(var7 - this.startNanoTime);
      this.startNanoTime = var7;

      while(getSystemTime() >= this.debugUpdateTime + 1000L) {
         debugFPS = this.fpsCounter;
         this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated == 1 ? "" : "s", (float)this.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : this.gameSettings.limitFramerate, this.gameSettings.enableVsync ? " vsync" : "", this.gameSettings.fancyGraphics ? "" : " fast", this.gameSettings.clouds == 0 ? "" : (this.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
         RenderChunk.renderChunksUpdated = 0;
         this.debugUpdateTime += 1000L;
         this.fpsCounter = 0;
         this.usageSnooper.addMemoryStatsToSnooper();
         if (!this.usageSnooper.isSnooperRunning()) {
            this.usageSnooper.startSnooper();
         }
      }

      if (this.isFramerateLimitBelowMax()) {
         this.mcProfiler.startSection("fpslimit_wait");
         Display.sync(this.getLimitFramerate());
         this.mcProfiler.endSection();
      }

      this.mcProfiler.endSection();
   }

   public void updateDisplay() {
      this.mcProfiler.startSection("display_update");
      Display.update();
      this.mcProfiler.endSection();
      this.checkWindowResize();
   }

   protected void checkWindowResize() {
      if (!this.fullscreen && Display.wasResized()) {
         int var1 = this.displayWidth;
         int var2 = this.displayHeight;
         this.displayWidth = Display.getWidth();
         this.displayHeight = Display.getHeight();
         if (this.displayWidth != var1 || this.displayHeight != var2) {
            if (this.displayWidth <= 0) {
               this.displayWidth = 1;
            }

            if (this.displayHeight <= 0) {
               this.displayHeight = 1;
            }

            this.resize(this.displayWidth, this.displayHeight);
         }
      }

   }

   public int getLimitFramerate() {
      return this.world == null && this.currentScreen != null ? 30 : this.gameSettings.limitFramerate;
   }

   public boolean isFramerateLimitBelowMax() {
      return (float)this.getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
   }

   public void freeMemory() {
      try {
         memoryReserve = new byte[0];
         this.renderGlobal.deleteAllDisplayLists();
      } catch (Throwable var3) {
         ;
      }

      try {
         System.gc();
         this.loadWorld((WorldClient)null);
      } catch (Throwable var2) {
         ;
      }

      System.gc();
   }

   private void updateDebugProfilerName(int var1) {
      List var2 = this.mcProfiler.getProfilingData(this.debugProfilerName);
      if (var2 != null && !var2.isEmpty()) {
         Profiler.Result var3 = (Profiler.Result)var2.remove(0);
         if (var1 == 0) {
            if (!var3.profilerName.isEmpty()) {
               int var4 = this.debugProfilerName.lastIndexOf(46);
               if (var4 >= 0) {
                  this.debugProfilerName = this.debugProfilerName.substring(0, var4);
               }
            }
         } else {
            --var1;
            if (var1 < var2.size() && !"unspecified".equals(((Profiler.Result)var2.get(var1)).profilerName)) {
               if (!this.debugProfilerName.isEmpty()) {
                  this.debugProfilerName = this.debugProfilerName + ".";
               }

               this.debugProfilerName = this.debugProfilerName + ((Profiler.Result)var2.get(var1)).profilerName;
            }
         }
      }

   }

   private void displayDebugInfo(long var1) {
      if (this.mcProfiler.profilingEnabled) {
         List var3 = this.mcProfiler.getProfilingData(this.debugProfilerName);
         Profiler.Result var4 = (Profiler.Result)var3.remove(0);
         GlStateManager.clear(256);
         GlStateManager.matrixMode(5889);
         GlStateManager.enableColorMaterial();
         GlStateManager.loadIdentity();
         GlStateManager.ortho(0.0D, (double)this.displayWidth, (double)this.displayHeight, 0.0D, 1000.0D, 3000.0D);
         GlStateManager.matrixMode(5888);
         GlStateManager.loadIdentity();
         GlStateManager.translate(0.0F, 0.0F, -2000.0F);
         GlStateManager.glLineWidth(1.0F);
         GlStateManager.disableTexture2D();
         Tessellator var5 = Tessellator.getInstance();
         VertexBuffer var6 = var5.getBuffer();
         boolean var7 = true;
         int var8 = this.displayWidth - 160 - 10;
         int var9 = this.displayHeight - 320;
         GlStateManager.enableBlend();
         var6.begin(7, DefaultVertexFormats.POSITION_COLOR);
         var6.pos((double)((float)var8 - 176.0F), (double)((float)var9 - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
         var6.pos((double)((float)var8 - 176.0F), (double)(var9 + 320), 0.0D).color(200, 0, 0, 0).endVertex();
         var6.pos((double)((float)var8 + 176.0F), (double)(var9 + 320), 0.0D).color(200, 0, 0, 0).endVertex();
         var6.pos((double)((float)var8 + 176.0F), (double)((float)var9 - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
         var5.draw();
         GlStateManager.disableBlend();
         double var10 = 0.0D;

         for(int var12 = 0; var12 < var3.size(); ++var12) {
            Profiler.Result var13 = (Profiler.Result)var3.get(var12);
            int var14 = MathHelper.floor(var13.usePercentage / 4.0D) + 1;
            var6.begin(6, DefaultVertexFormats.POSITION_COLOR);
            int var15 = var13.getColor();
            int var16 = var15 >> 16 & 255;
            int var17 = var15 >> 8 & 255;
            int var18 = var15 & 255;
            var6.pos((double)var8, (double)var9, 0.0D).color(var16, var17, var18, 255).endVertex();

            for(int var19 = var14; var19 >= 0; --var19) {
               float var20 = (float)((var10 + var13.usePercentage * (double)var19 / (double)var14) * 6.283185307179586D / 100.0D);
               float var21 = MathHelper.sin(var20) * 160.0F;
               float var22 = MathHelper.cos(var20) * 160.0F * 0.5F;
               var6.pos((double)((float)var8 + var21), (double)((float)var9 - var22), 0.0D).color(var16, var17, var18, 255).endVertex();
            }

            var5.draw();
            var6.begin(5, DefaultVertexFormats.POSITION_COLOR);

            for(int var34 = var14; var34 >= 0; --var34) {
               float var35 = (float)((var10 + var13.usePercentage * (double)var34 / (double)var14) * 6.283185307179586D / 100.0D);
               float var36 = MathHelper.sin(var35) * 160.0F;
               float var37 = MathHelper.cos(var35) * 160.0F * 0.5F;
               var6.pos((double)((float)var8 + var36), (double)((float)var9 - var37), 0.0D).color(var16 >> 1, var17 >> 1, var18 >> 1, 255).endVertex();
               var6.pos((double)((float)var8 + var36), (double)((float)var9 - var37 + 10.0F), 0.0D).color(var16 >> 1, var17 >> 1, var18 >> 1, 255).endVertex();
            }

            var5.draw();
            var10 += var13.usePercentage;
         }

         DecimalFormat var23 = new DecimalFormat("##0.00");
         GlStateManager.enableTexture2D();
         String var24 = "";
         if (!"unspecified".equals(var4.profilerName)) {
            var24 = var24 + "[0] ";
         }

         if (var4.profilerName.isEmpty()) {
            var24 = var24 + "ROOT ";
         } else {
            var24 = var24 + var4.profilerName + ' ';
         }

         int var27 = 16777215;
         this.fontRendererObj.drawStringWithShadow(var24, (float)(var8 - 160), (float)(var9 - 80 - 16), 16777215);
         var24 = var23.format(var4.totalUsePercentage) + "%";
         this.fontRendererObj.drawStringWithShadow(var24, (float)(var8 + 160 - this.fontRendererObj.getStringWidth(var24)), (float)(var9 - 80 - 16), 16777215);

         for(int var28 = 0; var28 < var3.size(); ++var28) {
            Profiler.Result var29 = (Profiler.Result)var3.get(var28);
            StringBuilder var30 = new StringBuilder();
            if ("unspecified".equals(var29.profilerName)) {
               var30.append("[?] ");
            } else {
               var30.append("[").append(var28 + 1).append("] ");
            }

            String var31 = var30.append(var29.profilerName).toString();
            this.fontRendererObj.drawStringWithShadow(var31, (float)(var8 - 160), (float)(var9 + 80 + var28 * 8 + 20), var29.getColor());
            var31 = var23.format(var29.usePercentage) + "%";
            this.fontRendererObj.drawStringWithShadow(var31, (float)(var8 + 160 - 50 - this.fontRendererObj.getStringWidth(var31)), (float)(var9 + 80 + var28 * 8 + 20), var29.getColor());
            var31 = var23.format(var29.totalUsePercentage) + "%";
            this.fontRendererObj.drawStringWithShadow(var31, (float)(var8 + 160 - this.fontRendererObj.getStringWidth(var31)), (float)(var9 + 80 + var28 * 8 + 20), var29.getColor());
         }
      }

   }

   public void shutdown() {
      this.running = false;
   }

   public void setIngameFocus() {
      if (Display.isActive() && !this.inGameHasFocus) {
         if (!IS_RUNNING_ON_MAC) {
            KeyBinding.updateKeyBindState();
         }

         this.inGameHasFocus = true;
         this.mouseHelper.grabMouseCursor();
         this.displayGuiScreen((GuiScreen)null);
         this.leftClickCounter = 10000;
      }

   }

   public void setIngameNotInFocus() {
      if (this.inGameHasFocus) {
         this.inGameHasFocus = false;
         this.mouseHelper.ungrabMouseCursor();
      }

   }

   public void displayInGameMenu() {
      if (this.currentScreen == null) {
         this.displayGuiScreen(new GuiIngameMenu());
         if (this.isSingleplayer() && !this.theIntegratedServer.getPublic()) {
            this.mcSoundHandler.pauseSounds();
         }
      }

   }

   private void sendClickBlockToController(boolean var1) {
      if (!var1) {
         this.leftClickCounter = 0;
      }

      if (this.leftClickCounter <= 0 && !this.player.isHandActive()) {
         if (var1 && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos var2 = this.objectMouseOver.getBlockPos();
            if (!this.world.isAirBlock(var2) && this.playerController.onPlayerDamageBlock(var2, this.objectMouseOver.sideHit)) {
               this.effectRenderer.addBlockHitEffects(var2, this.objectMouseOver);
               this.player.swingArm(EnumHand.MAIN_HAND);
            }
         } else {
            this.playerController.resetBlockRemoving();
         }
      }

   }

   private void clickMouse() {
      if (this.leftClickCounter <= 0) {
         if (this.objectMouseOver == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.playerController.isNotCreative()) {
               this.leftClickCounter = 10;
            }
         } else if (!this.player.isRowingBoat()) {
            switch(this.objectMouseOver.typeOfHit) {
            case ENTITY:
               this.playerController.attackEntity(this.player, this.objectMouseOver.entityHit);
               break;
            case BLOCK:
               BlockPos var1 = this.objectMouseOver.getBlockPos();
               if (!this.world.isAirBlock(var1)) {
                  this.playerController.clickBlock(var1, this.objectMouseOver.sideHit);
                  break;
               }
            case MISS:
               if (this.playerController.isNotCreative()) {
                  this.leftClickCounter = 10;
               }

               this.player.resetCooldown();
               ForgeHooks.onEmptyLeftClick(this.player, this.player.getHeldItemMainhand());
            }

            this.player.swingArm(EnumHand.MAIN_HAND);
         }
      }

   }

   private void rightClickMouse() {
      if (!this.playerController.getIsHittingBlock()) {
         this.rightClickDelayTimer = 4;
         if (!this.player.isRowingBoat()) {
            for(EnumHand var4 : EnumHand.values()) {
               ItemStack var5 = this.player.getHeldItem(var4);
               if (this.objectMouseOver == null) {
                  LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
               } else {
                  switch(this.objectMouseOver.typeOfHit) {
                  case ENTITY:
                     if (this.playerController.interactWithEntity(this.player, this.objectMouseOver.entityHit, this.objectMouseOver, this.player.getHeldItem(var4), var4) == EnumActionResult.SUCCESS) {
                        return;
                     }

                     if (this.playerController.interactWithEntity(this.player, this.objectMouseOver.entityHit, this.player.getHeldItem(var4), var4) == EnumActionResult.SUCCESS) {
                        return;
                     }
                     break;
                  case BLOCK:
                     BlockPos var6 = this.objectMouseOver.getBlockPos();
                     if (this.world.getBlockState(var6).getMaterial() != Material.AIR) {
                        int var7 = var5 != null ? var5.stackSize : 0;
                        EnumActionResult var8 = this.playerController.processRightClickBlock(this.player, this.world, var5, var6, this.objectMouseOver.sideHit, this.objectMouseOver.hitVec, var4);
                        if (var8 == EnumActionResult.SUCCESS) {
                           this.player.swingArm(var4);
                           if (var5 != null) {
                              if (var5.stackSize == 0) {
                                 this.player.setHeldItem(var4, (ItemStack)null);
                              } else if (var5.stackSize != var7 || this.playerController.isInCreativeMode()) {
                                 this.entityRenderer.itemRenderer.resetEquippedProgress(var4);
                              }
                           }

                           return;
                        }
                     }
                  }
               }

               ItemStack var9 = this.player.getHeldItem(var4);
               if (var9 == null && (this.objectMouseOver == null || this.objectMouseOver.typeOfHit == RayTraceResult.Type.MISS)) {
                  ForgeHooks.onEmptyClick(this.player, var4);
               }

               if (var9 != null && this.playerController.processRightClick(this.player, this.world, var9, var4) == EnumActionResult.SUCCESS) {
                  this.entityRenderer.itemRenderer.resetEquippedProgress(var4);
                  return;
               }
            }
         }
      }

   }

   public void toggleFullscreen() {
      try {
         this.fullscreen = !this.fullscreen;
         this.gameSettings.fullScreen = this.fullscreen;
         if (this.fullscreen) {
            this.updateDisplayMode();
            this.displayWidth = Display.getDisplayMode().getWidth();
            this.displayHeight = Display.getDisplayMode().getHeight();
            if (this.displayWidth <= 0) {
               this.displayWidth = 1;
            }

            if (this.displayHeight <= 0) {
               this.displayHeight = 1;
            }
         } else {
            Display.setDisplayMode(new DisplayMode(this.tempDisplayWidth, this.tempDisplayHeight));
            this.displayWidth = this.tempDisplayWidth;
            this.displayHeight = this.tempDisplayHeight;
            if (this.displayWidth <= 0) {
               this.displayWidth = 1;
            }

            if (this.displayHeight <= 0) {
               this.displayHeight = 1;
            }
         }

         if (this.currentScreen != null) {
            this.resize(this.displayWidth, this.displayHeight);
         } else {
            this.updateFramebufferSize();
         }

         Display.setFullscreen(this.fullscreen);
         Display.setVSyncEnabled(this.gameSettings.enableVsync);
         this.updateDisplay();
      } catch (Exception var2) {
         LOGGER.error("Couldn't toggle fullscreen", var2);
      }

   }

   public void resize(int var1, int var2) {
      this.displayWidth = Math.max(1, var1);
      this.displayHeight = Math.max(1, var2);
      if (this.currentScreen != null) {
         ScaledResolution var3 = new ScaledResolution(this);
         this.currentScreen.onResize(this, var3.getScaledWidth(), var3.getScaledHeight());
      }

      this.loadingScreen = new LoadingScreenRenderer(this);
      this.updateFramebufferSize();
   }

   private void updateFramebufferSize() {
      this.framebufferMc.createBindFramebuffer(this.displayWidth, this.displayHeight);
      if (this.entityRenderer != null) {
         this.entityRenderer.updateShaderGroupSize(this.displayWidth, this.displayHeight);
      }

   }

   public MusicTicker getMusicTicker() {
      return this.mcMusicTicker;
   }

   public void runTick() throws IOException {
      if (this.rightClickDelayTimer > 0) {
         --this.rightClickDelayTimer;
      }

      FMLCommonHandler.instance().onPreClientTick();
      this.mcProfiler.startSection("gui");
      if (!this.isGamePaused) {
         this.ingameGUI.updateTick();
      }

      this.mcProfiler.endSection();
      this.entityRenderer.getMouseOver(1.0F);
      this.mcProfiler.startSection("gameMode");
      if (!this.isGamePaused && this.world != null) {
         this.playerController.updateController();
      }

      this.mcProfiler.endStartSection("textures");
      if (!this.isGamePaused) {
         this.renderEngine.tick();
      }

      if (this.currentScreen == null && this.player != null) {
         if (this.player.getHealth() <= 0.0F && !(this.currentScreen instanceof GuiGameOver)) {
            this.displayGuiScreen((GuiScreen)null);
         } else if (this.player.isPlayerSleeping() && this.world != null) {
            this.displayGuiScreen(new GuiSleepMP());
         }
      } else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.player.isPlayerSleeping()) {
         this.displayGuiScreen((GuiScreen)null);
      }

      if (this.currentScreen != null) {
         this.leftClickCounter = 10000;
      }

      if (this.currentScreen != null) {
         try {
            this.currentScreen.handleInput();
         } catch (Throwable var5) {
            CrashReport var2 = CrashReport.makeCrashReport(var5, "Updating screen events");
            CrashReportCategory var3 = var2.makeCategory("Affected screen");
            var3.setDetail("Screen name", new ICrashReportDetail() {
               public String call() throws Exception {
                  return Minecraft.this.currentScreen.getClass().getCanonicalName();
               }
            });
            throw new ReportedException(var2);
         }

         if (this.currentScreen != null) {
            try {
               this.currentScreen.updateScreen();
            } catch (Throwable var4) {
               CrashReport var7 = CrashReport.makeCrashReport(var4, "Ticking screen");
               CrashReportCategory var9 = var7.makeCategory("Affected screen");
               var9.setDetail("Screen name", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return Minecraft.this.currentScreen.getClass().getCanonicalName();
                  }
               });
               throw new ReportedException(var7);
            }
         }
      }

      if (this.currentScreen == null || this.currentScreen.allowUserInput) {
         this.mcProfiler.endStartSection("mouse");
         this.runTickMouse();
         if (this.leftClickCounter > 0) {
            --this.leftClickCounter;
         }

         this.mcProfiler.endStartSection("keyboard");
         this.runTickKeyboard();
      }

      if (this.world != null) {
         if (this.player != null) {
            ++this.joinPlayerCounter;
            if (this.joinPlayerCounter == 30) {
               this.joinPlayerCounter = 0;
               this.world.joinEntityInSurroundings(this.player);
            }

            FMLCommonHandler.instance().fireMouseInput();
         }

         this.mcProfiler.endStartSection("gameRenderer");
         if (!this.isGamePaused) {
            this.entityRenderer.updateRenderer();
         }

         this.mcProfiler.endStartSection("levelRenderer");
         if (!this.isGamePaused) {
            this.renderGlobal.updateClouds();
         }

         this.mcProfiler.endStartSection("level");
         if (!this.isGamePaused) {
            if (this.world.getLastLightningBolt() > 0) {
               this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
            }

            this.world.updateEntities();
         }
      } else if (this.entityRenderer.isShaderActive()) {
         this.entityRenderer.stopUseShader();
      }

      if (!this.isGamePaused) {
         this.mcMusicTicker.update();
         this.mcSoundHandler.update();
      }

      if (this.world != null) {
         if (!this.isGamePaused) {
            this.world.setAllowedSpawnTypes(this.world.getDifficulty() != EnumDifficulty.PEACEFUL, true);

            try {
               this.world.tick();
            } catch (Throwable var6) {
               CrashReport var8 = CrashReport.makeCrashReport(var6, "Exception in world tick");
               if (this.world == null) {
                  CrashReportCategory var10 = var8.makeCategory("Affected level");
                  var10.addCrashSection("Problem", "Level is null!");
               } else {
                  this.world.addWorldInfoToCrashReport(var8);
               }

               throw new ReportedException(var8);
            }
         }

         this.mcProfiler.endStartSection("animateTick");
         if (!this.isGamePaused && this.world != null) {
            this.world.doVoidFogParticles(MathHelper.floor(this.player.posX), MathHelper.floor(this.player.posY), MathHelper.floor(this.player.posZ));
         }

         this.mcProfiler.endStartSection("particles");
         if (!this.isGamePaused) {
            this.effectRenderer.updateEffects();
         }
      } else if (this.myNetworkManager != null) {
         this.mcProfiler.endStartSection("pendingConnection");
         this.myNetworkManager.processReceivedPackets();
      }

      FMLCommonHandler.instance().onPostClientTick();
      this.mcProfiler.endSection();
      this.systemTime = getSystemTime();
   }

   private void runTickKeyboard() throws IOException {
      for(; Keyboard.next(); FMLCommonHandler.instance().fireKeyInput()) {
         int var1 = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
         if (this.debugCrashKeyPressTime > 0L) {
            if (getSystemTime() - this.debugCrashKeyPressTime >= 6000L) {
               throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
            }

            if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
               this.debugCrashKeyPressTime = -1L;
            }
         } else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
            this.actionKeyF3 = true;
            this.debugCrashKeyPressTime = getSystemTime();
         }

         this.dispatchKeypresses();
         if (this.currentScreen != null) {
            this.currentScreen.handleKeyboardInput();
         }

         boolean var2 = Keyboard.getEventKeyState();
         if (var2) {
            if (var1 == 62 && this.entityRenderer != null) {
               this.entityRenderer.switchUseShader();
            }

            boolean var3 = false;
            if (this.currentScreen == null) {
               if (var1 == 1) {
                  this.displayInGameMenu();
               }

               var3 = Keyboard.isKeyDown(61) && this.processKeyF3(var1);
               this.actionKeyF3 |= var3;
               if (var1 == 59) {
                  this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
               }
            }

            if (var3) {
               KeyBinding.setKeyBindState(var1, false);
            } else {
               KeyBinding.setKeyBindState(var1, true);
               KeyBinding.onTick(var1);
            }

            if (this.gameSettings.showDebugProfilerChart) {
               if (var1 == 11) {
                  this.updateDebugProfilerName(0);
               }

               for(int var4 = 0; var4 < 9; ++var4) {
                  if (var1 == 2 + var4) {
                     this.updateDebugProfilerName(var4 + 1);
                  }
               }
            }
         } else {
            KeyBinding.setKeyBindState(var1, false);
            if (var1 == 61) {
               if (this.actionKeyF3) {
                  this.actionKeyF3 = false;
               } else {
                  this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
                  this.gameSettings.showDebugProfilerChart = this.gameSettings.showDebugInfo && GuiScreen.isShiftKeyDown();
                  this.gameSettings.showLagometer = this.gameSettings.showDebugInfo && GuiScreen.isAltKeyDown();
               }
            }
         }
      }

      this.processKeyBinds();
   }

   private boolean processKeyF3(int var1) {
      if (var1 == 30) {
         this.renderGlobal.loadRenderers();
         this.debugChatMessage("Reloading all chunks");
         return true;
      } else if (var1 == 48) {
         boolean var4 = !this.renderManager.isDebugBoundingBox();
         this.renderManager.setDebugBoundingBox(var4);
         this.debugChatMessage("Hitboxes: {0}", var4 ? "shown" : "hidden");
         return true;
      } else if (var1 == 32) {
         if (this.ingameGUI != null) {
            this.ingameGUI.getChatGUI().clearChatMessages();
         }

         return true;
      } else if (var1 == 33) {
         this.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
         this.debugChatMessage("RenderDistance: {0}", this.gameSettings.renderDistanceChunks);
         return true;
      } else if (var1 == 34) {
         boolean var3 = this.debugRenderer.toggleDebugScreen();
         this.debugChatMessage("Chunk borders: {0}", var3 ? "shown" : "hidden");
         return true;
      } else if (var1 == 35) {
         this.gameSettings.advancedItemTooltips = !this.gameSettings.advancedItemTooltips;
         this.debugChatMessage("Advanced tooltips: {0}", this.gameSettings.advancedItemTooltips ? "shown" : "hidden");
         this.gameSettings.saveOptions();
         return true;
      } else if (var1 == 49) {
         if (!this.player.canUseCommand(2, "")) {
            this.debugChatMessage("Unable to switch gamemode, no permission");
         } else if (this.player.isCreative()) {
            this.player.sendChatMessage("/gamemode spectator");
         } else if (this.player.isSpectator()) {
            this.player.sendChatMessage("/gamemode creative");
         }

         return true;
      } else if (var1 == 25) {
         this.gameSettings.pauseOnLostFocus = !this.gameSettings.pauseOnLostFocus;
         this.gameSettings.saveOptions();
         this.debugChatMessage("PauseOnLostFocus: {0}", this.gameSettings.pauseOnLostFocus ? "enabled" : "disabled");
         return true;
      } else if (var1 == 16) {
         this.debugChatMessage("Keybindings:");
         GuiNewChat var2 = this.ingameGUI.getChatGUI();
         var2.printChatMessage(new TextComponentString("F3 + A = Reload chunks"));
         var2.printChatMessage(new TextComponentString("F3 + B = Show hitboxes"));
         var2.printChatMessage(new TextComponentString("F3 + D = Clear chat"));
         var2.printChatMessage(new TextComponentString("F3 + F = Cycle renderdistance (Shift to inverse)"));
         var2.printChatMessage(new TextComponentString("F3 + G = Show chunk boundaries"));
         var2.printChatMessage(new TextComponentString("F3 + H = Advanced tooltips"));
         var2.printChatMessage(new TextComponentString("F3 + N = Cycle creative <-> spectator"));
         var2.printChatMessage(new TextComponentString("F3 + P = Pause on lost focus"));
         var2.printChatMessage(new TextComponentString("F3 + Q = Show this list"));
         var2.printChatMessage(new TextComponentString("F3 + T = Reload resourcepacks"));
         return true;
      } else if (var1 == 20) {
         this.refreshResources();
         this.debugChatMessage("Reloaded resourcepacks");
         return true;
      } else {
         return false;
      }
   }

   private void processKeyBinds() {
      for(; this.gameSettings.keyBindTogglePerspective.isPressed(); this.renderGlobal.setDisplayListEntitiesDirty()) {
         ++this.gameSettings.thirdPersonView;
         if (this.gameSettings.thirdPersonView > 2) {
            this.gameSettings.thirdPersonView = 0;
         }

         if (this.gameSettings.thirdPersonView == 0) {
            this.entityRenderer.loadEntityShader(this.getRenderViewEntity());
         } else if (this.gameSettings.thirdPersonView == 1) {
            this.entityRenderer.loadEntityShader((Entity)null);
         }
      }

      while(this.gameSettings.keyBindSmoothCamera.isPressed()) {
         this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
      }

      for(int var1 = 0; var1 < 9; ++var1) {
         if (this.gameSettings.keyBindsHotbar[var1].isPressed()) {
            if (this.player.isSpectator()) {
               this.ingameGUI.getSpectatorGui().onHotbarSelected(var1);
            } else {
               this.player.inventory.currentItem = var1;
            }
         }
      }

      while(this.gameSettings.keyBindInventory.isPressed()) {
         this.getConnection().sendPacket(new CPacketClientStatus(CPacketClientStatus.State.OPEN_INVENTORY_ACHIEVEMENT));
         if (this.playerController.isRidingHorse()) {
            this.player.sendHorseInventory();
         } else {
            this.displayGuiScreen(new GuiInventory(this.player));
         }
      }

      while(this.gameSettings.keyBindSwapHands.isPressed()) {
         if (!this.player.isSpectator()) {
            this.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
         }
      }

      while(this.gameSettings.keyBindDrop.isPressed()) {
         if (!this.player.isSpectator()) {
            this.player.dropItem(GuiScreen.isCtrlKeyDown());
         }
      }

      boolean var2 = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;
      if (var2) {
         while(this.gameSettings.keyBindChat.isPressed()) {
            this.displayGuiScreen(new GuiChat());
         }

         if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed()) {
            this.displayGuiScreen(new GuiChat("/"));
         }
      }

      if (this.player.isHandActive()) {
         if (!this.gameSettings.keyBindUseItem.isKeyDown()) {
            this.playerController.onStoppedUsingItem(this.player);
         }

         while(this.gameSettings.keyBindAttack.isPressed()) {
            ;
         }

         label100:
         while(true) {
            if (!this.gameSettings.keyBindUseItem.isPressed()) {
               while(true) {
                  if (this.gameSettings.keyBindPickBlock.isPressed()) {
                     continue;
                  }
                  break label100;
               }
            }
         }
      } else {
         while(this.gameSettings.keyBindAttack.isPressed()) {
            this.clickMouse();
         }

         while(this.gameSettings.keyBindUseItem.isPressed()) {
            this.rightClickMouse();
         }

         while(this.gameSettings.keyBindPickBlock.isPressed()) {
            this.middleClickMouse();
         }
      }

      if (this.gameSettings.keyBindUseItem.isKeyDown() && this.rightClickDelayTimer == 0 && !this.player.isHandActive()) {
         this.rightClickMouse();
      }

      this.sendClickBlockToController(this.currentScreen == null && this.gameSettings.keyBindAttack.isKeyDown() && this.inGameHasFocus);
   }

   private void runTickMouse() throws IOException {
      while(Mouse.next()) {
         if (!ForgeHooksClient.postMouseEvent()) {
            int var1 = Mouse.getEventButton();
            KeyBinding.setKeyBindState(var1 - 100, Mouse.getEventButtonState());
            if (Mouse.getEventButtonState()) {
               if (this.player.isSpectator() && var1 == 2) {
                  this.ingameGUI.getSpectatorGui().onMiddleClick();
               } else {
                  KeyBinding.onTick(var1 - 100);
               }
            }

            long var2 = getSystemTime() - this.systemTime;
            if (var2 <= 200L) {
               int var4 = Mouse.getEventDWheel();
               if (var4 != 0) {
                  if (this.player.isSpectator()) {
                     var4 = var4 < 0 ? -1 : 1;
                     if (this.ingameGUI.getSpectatorGui().isMenuActive()) {
                        this.ingameGUI.getSpectatorGui().onMouseScroll(-var4);
                     } else {
                        float var5 = MathHelper.clamp(this.player.capabilities.getFlySpeed() + (float)var4 * 0.005F, 0.0F, 0.2F);
                        this.player.capabilities.setFlySpeed(var5);
                     }
                  } else {
                     this.player.inventory.changeCurrentItem(var4);
                  }
               }

               if (this.currentScreen == null) {
                  if (!this.inGameHasFocus && Mouse.getEventButtonState()) {
                     this.setIngameFocus();
                  }
               } else if (this.currentScreen != null) {
                  this.currentScreen.handleMouseInput();
               }
            }
         }
      }

   }

   private void debugChatMessage(String var1, Object... var2) {
      this.ingameGUI.getChatGUI().printChatMessage((new TextComponentString("")).appendSibling((new TextComponentString("[Debug]: ")).setStyle((new Style()).setColor(TextFormatting.YELLOW).setBold(Boolean.valueOf(true)))).appendText(MessageFormat.format(var1, var2)));
   }

   public void launchIntegratedServer(String var1, String var2, @Nullable WorldSettings var3) {
      FMLClientHandler.instance().startIntegratedServer(var1, var2, var3);
      this.loadWorld((WorldClient)null);
      System.gc();
      ISaveHandler var4 = this.saveLoader.getSaveLoader(var1, false);
      WorldInfo var5 = var4.loadWorldInfo();
      if (var5 == null && var3 != null) {
         var5 = new WorldInfo(var3, var1);
         var4.saveWorldInfo(var5);
      }

      if (var3 == null) {
         var3 = new WorldSettings(var5);
      }

      try {
         YggdrasilAuthenticationService var6 = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString());
         MinecraftSessionService var14 = var6.createMinecraftSessionService();
         GameProfileRepository var16 = var6.createProfileRepository();
         PlayerProfileCache var9 = new PlayerProfileCache(var16, new File(this.mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
         TileEntitySkull.setProfileCache(var9);
         TileEntitySkull.setSessionService(var14);
         PlayerProfileCache.setOnlineMode(false);
         this.theIntegratedServer = new IntegratedServer(this, var1, var2, var3, var6, var14, var16, var9);
         this.theIntegratedServer.startServerThread();
         this.integratedServerIsRunning = true;
      } catch (Throwable var11) {
         CrashReport var7 = CrashReport.makeCrashReport(var11, "Starting integrated server");
         CrashReportCategory var8 = var7.makeCategory("Starting integrated server");
         var8.addCrashSection("Level ID", var1);
         var8.addCrashSection("Level Name", var2);
         throw new ReportedException(var7);
      }

      this.loadingScreen.displaySavingString(I18n.format("menu.loadingLevel"));

      while(!this.theIntegratedServer.serverIsInRunLoop()) {
         if (!StartupQuery.check()) {
            this.loadWorld((WorldClient)null);
            this.displayGuiScreen((GuiScreen)null);
            return;
         }

         String var12 = this.theIntegratedServer.getUserMessage();
         if (var12 != null) {
            this.loadingScreen.displayLoadingString(I18n.format(var12));
         } else {
            this.loadingScreen.displayLoadingString("");
         }

         try {
            Thread.sleep(200L);
         } catch (InterruptedException var10) {
            ;
         }
      }

      this.displayGuiScreen(new GuiScreenWorking());
      SocketAddress var13 = this.theIntegratedServer.getNetworkSystem().addLocalEndpoint();
      NetworkManager var15 = NetworkManager.provideLocalClient(var13);
      var15.setNetHandler(new NetHandlerLoginClient(var15, this, (GuiScreen)null));
      var15.sendPacket(new C00Handshake(210, var13.toString(), 0, EnumConnectionState.LOGIN, true));
      GameProfile var17 = this.getSession().getProfile();
      if (!this.getSession().hasCachedProperties()) {
         var17 = this.sessionService.fillProfileProperties(var17, true);
         this.getSession().setProperties(var17.getProperties());
      }

      var15.sendPacket(new CPacketLoginStart(var17));
      this.myNetworkManager = var15;
   }

   public void loadWorld(WorldClient var1) {
      this.loadWorld(var1, "");
   }

   public void loadWorld(@Nullable WorldClient var1, String var2) {
      if (this.world != null) {
         MinecraftForge.EVENT_BUS.post(new Unload(this.world));
      }

      if (var1 == null) {
         NetHandlerPlayClient var3 = this.getConnection();
         if (var3 != null) {
            var3.cleanup();
         }

         if (this.theIntegratedServer != null && this.theIntegratedServer.isAnvilFileSet()) {
            this.theIntegratedServer.initiateShutdown();
            if (this.loadingScreen != null) {
               this.loadingScreen.displayLoadingString(I18n.format("forge.client.shutdown.internal"));
            }

            while(!this.theIntegratedServer.isServerStopped()) {
               try {
                  Thread.sleep(10L);
               } catch (InterruptedException var7) {
                  ;
               }
            }
         }

         this.theIntegratedServer = null;
         this.guiAchievement.clearAchievements();
         this.entityRenderer.getMapItemRenderer().clearLoadedMaps();
         this.playerController = null;
      }

      this.renderViewEntity = null;
      this.myNetworkManager = null;
      if (this.loadingScreen != null) {
         this.loadingScreen.resetProgressAndMessage(var2);
         this.loadingScreen.displayLoadingString("");
      }

      if (var1 == null && this.world != null) {
         this.mcResourcePackRepository.clearResourcePack();
         this.ingameGUI.resetPlayersOverlayFooterHeader();
         this.setServerData((ServerData)null);
         this.integratedServerIsRunning = false;
         FMLClientHandler.instance().handleClientWorldClosing(this.world);
      }

      this.mcSoundHandler.stopSounds();
      this.world = var1;
      if (this.renderGlobal != null) {
         this.renderGlobal.setWorldAndLoadRenderers(var1);
      }

      if (this.effectRenderer != null) {
         this.effectRenderer.clearEffects(var1);
      }

      TileEntityRendererDispatcher.instance.setWorld(var1);
      if (var1 != null) {
         if (!this.integratedServerIsRunning) {
            YggdrasilAuthenticationService var8 = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString());
            MinecraftSessionService var4 = var8.createMinecraftSessionService();
            GameProfileRepository var5 = var8.createProfileRepository();
            PlayerProfileCache var6 = new PlayerProfileCache(var5, new File(this.mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
            TileEntitySkull.setProfileCache(var6);
            TileEntitySkull.setSessionService(var4);
            PlayerProfileCache.setOnlineMode(false);
         }

         if (this.player == null) {
            this.player = this.playerController.createClientPlayer(var1, new StatisticsManager());
            this.playerController.flipPlayer(this.player);
         }

         this.player.preparePlayerToSpawn();
         var1.spawnEntity(this.player);
         this.player.movementInput = new MovementInputFromOptions(this.gameSettings);
         this.playerController.setPlayerCapabilities(this.player);
         this.renderViewEntity = this.player;
      } else {
         this.saveLoader.flushCache();
         this.player = null;
      }

      System.gc();
      this.systemTime = 0L;
   }

   public void setDimensionAndSpawnPlayer(int var1) {
      this.world.setInitialSpawnLocation();
      this.world.removeAllEntities();
      int var2 = 0;
      String var3 = null;
      if (this.player != null) {
         var2 = this.player.getEntityId();
         this.world.removeEntity(this.player);
         var3 = this.player.getServerBrand();
      }

      this.renderViewEntity = null;
      EntityPlayerSP var4 = this.player;
      this.player = this.playerController.createClientPlayer(this.world, this.player == null ? new StatisticsManager() : this.player.getStatFileWriter());
      this.player.getDataManager().setEntryValues(var4.getDataManager().getAll());
      this.player.dimension = var1;
      this.renderViewEntity = this.player;
      this.player.preparePlayerToSpawn();
      this.player.setServerBrand(var3);
      this.world.spawnEntity(this.player);
      this.playerController.flipPlayer(this.player);
      this.player.movementInput = new MovementInputFromOptions(this.gameSettings);
      this.player.setEntityId(var2);
      this.playerController.setPlayerCapabilities(this.player);
      this.player.setReducedDebug(var4.hasReducedDebug());
      if (this.currentScreen instanceof GuiGameOver) {
         this.displayGuiScreen((GuiScreen)null);
      }

   }

   public final boolean isDemo() {
      return this.isDemo;
   }

   @Nullable
   public NetHandlerPlayClient getConnection() {
      return this.player == null ? null : this.player.connection;
   }

   public static boolean isGuiEnabled() {
      return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
   }

   public static boolean isFancyGraphicsEnabled() {
      return theMinecraft != null && theMinecraft.gameSettings.fancyGraphics;
   }

   public static boolean isAmbientOcclusionEnabled() {
      return theMinecraft != null && theMinecraft.gameSettings.ambientOcclusion != 0;
   }

   private void middleClickMouse() {
      if (this.objectMouseOver != null && this.objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) {
         ForgeHooks.onPickBlock(this.objectMouseOver, this.player, this.world);
      }

   }

   public ItemStack storeTEInStack(ItemStack var1, TileEntity var2) {
      NBTTagCompound var3 = var2.writeToNBT(new NBTTagCompound());
      if (var1.getItem() == Items.SKULL && var3.hasKey("Owner")) {
         NBTTagCompound var6 = var3.getCompoundTag("Owner");
         NBTTagCompound var7 = new NBTTagCompound();
         var7.setTag("SkullOwner", var6);
         var1.setTagCompound(var7);
         return var1;
      } else {
         var1.setTagInfo("BlockEntityTag", var3);
         NBTTagCompound var4 = new NBTTagCompound();
         NBTTagList var5 = new NBTTagList();
         var5.appendTag(new NBTTagString("(+NBT)"));
         var4.setTag("Lore", var5);
         var1.setTagInfo("display", var4);
         return var1;
      }
   }

   public CrashReport addGraphicsAndWorldToCrashReport(CrashReport var1) {
      var1.getCategory().setDetail("Launched Version", new ICrashReportDetail() {
         public String call() throws Exception {
            return Minecraft.this.launchedVersion;
         }
      });
      var1.getCategory().setDetail("LWJGL", new ICrashReportDetail() {
         public String call() {
            return Sys.getVersion();
         }
      });
      var1.getCategory().setDetail("OpenGL", new ICrashReportDetail() {
         public String call() {
            return GlStateManager.glGetString(7937) + " GL version " + GlStateManager.glGetString(7938) + ", " + GlStateManager.glGetString(7936);
         }
      });
      var1.getCategory().setDetail("GL Caps", new ICrashReportDetail() {
         public String call() {
            return OpenGlHelper.getLogText();
         }
      });
      var1.getCategory().setDetail("Using VBOs", new ICrashReportDetail() {
         public String call() {
            return Minecraft.this.gameSettings.useVbo ? "Yes" : "No";
         }
      });
      var1.getCategory().setDetail("Is Modded", new ICrashReportDetail() {
         public String call() throws Exception {
            String var1 = ClientBrandRetriever.getClientModName();
            return !"vanilla".equals(var1) ? "Definitely; Client brand changed to '" + var1 + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and client brand is untouched.");
         }
      });
      var1.getCategory().setDetail("Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return "Client (map_client.txt)";
         }
      });
      var1.getCategory().setDetail("Resource Packs", new ICrashReportDetail() {
         public String call() throws Exception {
            StringBuilder var1 = new StringBuilder();

            for(String var3 : Minecraft.this.gameSettings.resourcePacks) {
               if (var1.length() > 0) {
                  var1.append(", ");
               }

               var1.append(var3);
               if (Minecraft.this.gameSettings.incompatibleResourcePacks.contains(var3)) {
                  var1.append(" (incompatible)");
               }
            }

            return var1.toString();
         }
      });
      var1.getCategory().setDetail("Current Language", new ICrashReportDetail() {
         public String call() throws Exception {
            return Minecraft.this.mcLanguageManager.getCurrentLanguage().toString();
         }
      });
      var1.getCategory().setDetail("Profiler Position", new ICrashReportDetail() {
         public String call() throws Exception {
            return Minecraft.this.mcProfiler.profilingEnabled ? Minecraft.this.mcProfiler.getNameOfLastSection() : "N/A (disabled)";
         }
      });
      var1.getCategory().setDetail("CPU", new ICrashReportDetail() {
         public String call() {
            return OpenGlHelper.getCpu();
         }
      });
      if (this.world != null) {
         this.world.addWorldInfoToCrashReport(var1);
      }

      return var1;
   }

   public static Minecraft getMinecraft() {
      return theMinecraft;
   }

   public ListenableFuture scheduleResourcesRefresh() {
      return this.addScheduledTask(new Runnable() {
         public void run() {
            Minecraft.this.refreshResources();
         }
      });
   }

   public void addServerStatsToSnooper(Snooper var1) {
      var1.addClientStat("fps", Integer.valueOf(debugFPS));
      var1.addClientStat("vsync_enabled", Boolean.valueOf(this.gameSettings.enableVsync));
      var1.addClientStat("display_frequency", Integer.valueOf(Display.getDisplayMode().getFrequency()));
      var1.addClientStat("display_type", this.fullscreen ? "fullscreen" : "windowed");
      var1.addClientStat("run_time", Long.valueOf((MinecraftServer.getCurrentTimeMillis() - var1.getMinecraftStartTimeMillis()) / 60L * 1000L));
      var1.addClientStat("current_action", this.getCurrentAction());
      var1.addClientStat("language", this.gameSettings.language == null ? "en_US" : this.gameSettings.language);
      String var2 = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "little" : "big";
      var1.addClientStat("endianness", var2);
      var1.addClientStat("subtitles", Boolean.valueOf(this.gameSettings.showSubtitles));
      var1.addClientStat("resource_packs", Integer.valueOf(this.mcResourcePackRepository.getRepositoryEntries().size()));
      int var3 = 0;

      for(ResourcePackRepository.Entry var5 : this.mcResourcePackRepository.getRepositoryEntries()) {
         var1.addClientStat("resource_pack[" + var3++ + "]", var5.getResourcePackName());
      }

      if (this.theIntegratedServer != null && this.theIntegratedServer.getPlayerUsageSnooper() != null) {
         var1.addClientStat("snooper_partner", this.theIntegratedServer.getPlayerUsageSnooper().getUniqueID());
      }

   }

   private String getCurrentAction() {
      return this.theIntegratedServer != null ? (this.theIntegratedServer.getPublic() ? "hosting_lan" : "singleplayer") : (this.currentServerData != null ? (this.currentServerData.isOnLAN() ? "playing_lan" : "multiplayer") : "out_of_game");
   }

   public void addServerTypeToSnooper(Snooper var1) {
      var1.addStatToSnooper("opengl_version", GlStateManager.glGetString(7938));
      var1.addStatToSnooper("opengl_vendor", GlStateManager.glGetString(7936));
      var1.addStatToSnooper("client_brand", ClientBrandRetriever.getClientModName());
      var1.addStatToSnooper("launched_version", this.launchedVersion);
      ContextCapabilities var2 = GLContext.getCapabilities();
      var1.addStatToSnooper("gl_caps[ARB_arrays_of_arrays]", Boolean.valueOf(var2.GL_ARB_arrays_of_arrays));
      var1.addStatToSnooper("gl_caps[ARB_base_instance]", Boolean.valueOf(var2.GL_ARB_base_instance));
      var1.addStatToSnooper("gl_caps[ARB_blend_func_extended]", Boolean.valueOf(var2.GL_ARB_blend_func_extended));
      var1.addStatToSnooper("gl_caps[ARB_clear_buffer_object]", Boolean.valueOf(var2.GL_ARB_clear_buffer_object));
      var1.addStatToSnooper("gl_caps[ARB_color_buffer_float]", Boolean.valueOf(var2.GL_ARB_color_buffer_float));
      var1.addStatToSnooper("gl_caps[ARB_compatibility]", Boolean.valueOf(var2.GL_ARB_compatibility));
      var1.addStatToSnooper("gl_caps[ARB_compressed_texture_pixel_storage]", Boolean.valueOf(var2.GL_ARB_compressed_texture_pixel_storage));
      var1.addStatToSnooper("gl_caps[ARB_compute_shader]", Boolean.valueOf(var2.GL_ARB_compute_shader));
      var1.addStatToSnooper("gl_caps[ARB_copy_buffer]", Boolean.valueOf(var2.GL_ARB_copy_buffer));
      var1.addStatToSnooper("gl_caps[ARB_copy_image]", Boolean.valueOf(var2.GL_ARB_copy_image));
      var1.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", Boolean.valueOf(var2.GL_ARB_depth_buffer_float));
      var1.addStatToSnooper("gl_caps[ARB_compute_shader]", Boolean.valueOf(var2.GL_ARB_compute_shader));
      var1.addStatToSnooper("gl_caps[ARB_copy_buffer]", Boolean.valueOf(var2.GL_ARB_copy_buffer));
      var1.addStatToSnooper("gl_caps[ARB_copy_image]", Boolean.valueOf(var2.GL_ARB_copy_image));
      var1.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", Boolean.valueOf(var2.GL_ARB_depth_buffer_float));
      var1.addStatToSnooper("gl_caps[ARB_depth_clamp]", Boolean.valueOf(var2.GL_ARB_depth_clamp));
      var1.addStatToSnooper("gl_caps[ARB_depth_texture]", Boolean.valueOf(var2.GL_ARB_depth_texture));
      var1.addStatToSnooper("gl_caps[ARB_draw_buffers]", Boolean.valueOf(var2.GL_ARB_draw_buffers));
      var1.addStatToSnooper("gl_caps[ARB_draw_buffers_blend]", Boolean.valueOf(var2.GL_ARB_draw_buffers_blend));
      var1.addStatToSnooper("gl_caps[ARB_draw_elements_base_vertex]", Boolean.valueOf(var2.GL_ARB_draw_elements_base_vertex));
      var1.addStatToSnooper("gl_caps[ARB_draw_indirect]", Boolean.valueOf(var2.GL_ARB_draw_indirect));
      var1.addStatToSnooper("gl_caps[ARB_draw_instanced]", Boolean.valueOf(var2.GL_ARB_draw_instanced));
      var1.addStatToSnooper("gl_caps[ARB_explicit_attrib_location]", Boolean.valueOf(var2.GL_ARB_explicit_attrib_location));
      var1.addStatToSnooper("gl_caps[ARB_explicit_uniform_location]", Boolean.valueOf(var2.GL_ARB_explicit_uniform_location));
      var1.addStatToSnooper("gl_caps[ARB_fragment_layer_viewport]", Boolean.valueOf(var2.GL_ARB_fragment_layer_viewport));
      var1.addStatToSnooper("gl_caps[ARB_fragment_program]", Boolean.valueOf(var2.GL_ARB_fragment_program));
      var1.addStatToSnooper("gl_caps[ARB_fragment_shader]", Boolean.valueOf(var2.GL_ARB_fragment_shader));
      var1.addStatToSnooper("gl_caps[ARB_fragment_program_shadow]", Boolean.valueOf(var2.GL_ARB_fragment_program_shadow));
      var1.addStatToSnooper("gl_caps[ARB_framebuffer_object]", Boolean.valueOf(var2.GL_ARB_framebuffer_object));
      var1.addStatToSnooper("gl_caps[ARB_framebuffer_sRGB]", Boolean.valueOf(var2.GL_ARB_framebuffer_sRGB));
      var1.addStatToSnooper("gl_caps[ARB_geometry_shader4]", Boolean.valueOf(var2.GL_ARB_geometry_shader4));
      var1.addStatToSnooper("gl_caps[ARB_gpu_shader5]", Boolean.valueOf(var2.GL_ARB_gpu_shader5));
      var1.addStatToSnooper("gl_caps[ARB_half_float_pixel]", Boolean.valueOf(var2.GL_ARB_half_float_pixel));
      var1.addStatToSnooper("gl_caps[ARB_half_float_vertex]", Boolean.valueOf(var2.GL_ARB_half_float_vertex));
      var1.addStatToSnooper("gl_caps[ARB_instanced_arrays]", Boolean.valueOf(var2.GL_ARB_instanced_arrays));
      var1.addStatToSnooper("gl_caps[ARB_map_buffer_alignment]", Boolean.valueOf(var2.GL_ARB_map_buffer_alignment));
      var1.addStatToSnooper("gl_caps[ARB_map_buffer_range]", Boolean.valueOf(var2.GL_ARB_map_buffer_range));
      var1.addStatToSnooper("gl_caps[ARB_multisample]", Boolean.valueOf(var2.GL_ARB_multisample));
      var1.addStatToSnooper("gl_caps[ARB_multitexture]", Boolean.valueOf(var2.GL_ARB_multitexture));
      var1.addStatToSnooper("gl_caps[ARB_occlusion_query2]", Boolean.valueOf(var2.GL_ARB_occlusion_query2));
      var1.addStatToSnooper("gl_caps[ARB_pixel_buffer_object]", Boolean.valueOf(var2.GL_ARB_pixel_buffer_object));
      var1.addStatToSnooper("gl_caps[ARB_seamless_cube_map]", Boolean.valueOf(var2.GL_ARB_seamless_cube_map));
      var1.addStatToSnooper("gl_caps[ARB_shader_objects]", Boolean.valueOf(var2.GL_ARB_shader_objects));
      var1.addStatToSnooper("gl_caps[ARB_shader_stencil_export]", Boolean.valueOf(var2.GL_ARB_shader_stencil_export));
      var1.addStatToSnooper("gl_caps[ARB_shader_texture_lod]", Boolean.valueOf(var2.GL_ARB_shader_texture_lod));
      var1.addStatToSnooper("gl_caps[ARB_shadow]", Boolean.valueOf(var2.GL_ARB_shadow));
      var1.addStatToSnooper("gl_caps[ARB_shadow_ambient]", Boolean.valueOf(var2.GL_ARB_shadow_ambient));
      var1.addStatToSnooper("gl_caps[ARB_stencil_texturing]", Boolean.valueOf(var2.GL_ARB_stencil_texturing));
      var1.addStatToSnooper("gl_caps[ARB_sync]", Boolean.valueOf(var2.GL_ARB_sync));
      var1.addStatToSnooper("gl_caps[ARB_tessellation_shader]", Boolean.valueOf(var2.GL_ARB_tessellation_shader));
      var1.addStatToSnooper("gl_caps[ARB_texture_border_clamp]", Boolean.valueOf(var2.GL_ARB_texture_border_clamp));
      var1.addStatToSnooper("gl_caps[ARB_texture_buffer_object]", Boolean.valueOf(var2.GL_ARB_texture_buffer_object));
      var1.addStatToSnooper("gl_caps[ARB_texture_cube_map]", Boolean.valueOf(var2.GL_ARB_texture_cube_map));
      var1.addStatToSnooper("gl_caps[ARB_texture_cube_map_array]", Boolean.valueOf(var2.GL_ARB_texture_cube_map_array));
      var1.addStatToSnooper("gl_caps[ARB_texture_non_power_of_two]", Boolean.valueOf(var2.GL_ARB_texture_non_power_of_two));
      var1.addStatToSnooper("gl_caps[ARB_uniform_buffer_object]", Boolean.valueOf(var2.GL_ARB_uniform_buffer_object));
      var1.addStatToSnooper("gl_caps[ARB_vertex_blend]", Boolean.valueOf(var2.GL_ARB_vertex_blend));
      var1.addStatToSnooper("gl_caps[ARB_vertex_buffer_object]", Boolean.valueOf(var2.GL_ARB_vertex_buffer_object));
      var1.addStatToSnooper("gl_caps[ARB_vertex_program]", Boolean.valueOf(var2.GL_ARB_vertex_program));
      var1.addStatToSnooper("gl_caps[ARB_vertex_shader]", Boolean.valueOf(var2.GL_ARB_vertex_shader));
      var1.addStatToSnooper("gl_caps[EXT_bindable_uniform]", Boolean.valueOf(var2.GL_EXT_bindable_uniform));
      var1.addStatToSnooper("gl_caps[EXT_blend_equation_separate]", Boolean.valueOf(var2.GL_EXT_blend_equation_separate));
      var1.addStatToSnooper("gl_caps[EXT_blend_func_separate]", Boolean.valueOf(var2.GL_EXT_blend_func_separate));
      var1.addStatToSnooper("gl_caps[EXT_blend_minmax]", Boolean.valueOf(var2.GL_EXT_blend_minmax));
      var1.addStatToSnooper("gl_caps[EXT_blend_subtract]", Boolean.valueOf(var2.GL_EXT_blend_subtract));
      var1.addStatToSnooper("gl_caps[EXT_draw_instanced]", Boolean.valueOf(var2.GL_EXT_draw_instanced));
      var1.addStatToSnooper("gl_caps[EXT_framebuffer_multisample]", Boolean.valueOf(var2.GL_EXT_framebuffer_multisample));
      var1.addStatToSnooper("gl_caps[EXT_framebuffer_object]", Boolean.valueOf(var2.GL_EXT_framebuffer_object));
      var1.addStatToSnooper("gl_caps[EXT_framebuffer_sRGB]", Boolean.valueOf(var2.GL_EXT_framebuffer_sRGB));
      var1.addStatToSnooper("gl_caps[EXT_geometry_shader4]", Boolean.valueOf(var2.GL_EXT_geometry_shader4));
      var1.addStatToSnooper("gl_caps[EXT_gpu_program_parameters]", Boolean.valueOf(var2.GL_EXT_gpu_program_parameters));
      var1.addStatToSnooper("gl_caps[EXT_gpu_shader4]", Boolean.valueOf(var2.GL_EXT_gpu_shader4));
      var1.addStatToSnooper("gl_caps[EXT_multi_draw_arrays]", Boolean.valueOf(var2.GL_EXT_multi_draw_arrays));
      var1.addStatToSnooper("gl_caps[EXT_packed_depth_stencil]", Boolean.valueOf(var2.GL_EXT_packed_depth_stencil));
      var1.addStatToSnooper("gl_caps[EXT_paletted_texture]", Boolean.valueOf(var2.GL_EXT_paletted_texture));
      var1.addStatToSnooper("gl_caps[EXT_rescale_normal]", Boolean.valueOf(var2.GL_EXT_rescale_normal));
      var1.addStatToSnooper("gl_caps[EXT_separate_shader_objects]", Boolean.valueOf(var2.GL_EXT_separate_shader_objects));
      var1.addStatToSnooper("gl_caps[EXT_shader_image_load_store]", Boolean.valueOf(var2.GL_EXT_shader_image_load_store));
      var1.addStatToSnooper("gl_caps[EXT_shadow_funcs]", Boolean.valueOf(var2.GL_EXT_shadow_funcs));
      var1.addStatToSnooper("gl_caps[EXT_shared_texture_palette]", Boolean.valueOf(var2.GL_EXT_shared_texture_palette));
      var1.addStatToSnooper("gl_caps[EXT_stencil_clear_tag]", Boolean.valueOf(var2.GL_EXT_stencil_clear_tag));
      var1.addStatToSnooper("gl_caps[EXT_stencil_two_side]", Boolean.valueOf(var2.GL_EXT_stencil_two_side));
      var1.addStatToSnooper("gl_caps[EXT_stencil_wrap]", Boolean.valueOf(var2.GL_EXT_stencil_wrap));
      var1.addStatToSnooper("gl_caps[EXT_texture_3d]", Boolean.valueOf(var2.GL_EXT_texture_3d));
      var1.addStatToSnooper("gl_caps[EXT_texture_array]", Boolean.valueOf(var2.GL_EXT_texture_array));
      var1.addStatToSnooper("gl_caps[EXT_texture_buffer_object]", Boolean.valueOf(var2.GL_EXT_texture_buffer_object));
      var1.addStatToSnooper("gl_caps[EXT_texture_integer]", Boolean.valueOf(var2.GL_EXT_texture_integer));
      var1.addStatToSnooper("gl_caps[EXT_texture_lod_bias]", Boolean.valueOf(var2.GL_EXT_texture_lod_bias));
      var1.addStatToSnooper("gl_caps[EXT_texture_sRGB]", Boolean.valueOf(var2.GL_EXT_texture_sRGB));
      var1.addStatToSnooper("gl_caps[EXT_vertex_shader]", Boolean.valueOf(var2.GL_EXT_vertex_shader));
      var1.addStatToSnooper("gl_caps[EXT_vertex_weighting]", Boolean.valueOf(var2.GL_EXT_vertex_weighting));
      var1.addStatToSnooper("gl_caps[gl_max_vertex_uniforms]", Integer.valueOf(GlStateManager.glGetInteger(35658)));
      GlStateManager.glGetError();
      var1.addStatToSnooper("gl_caps[gl_max_fragment_uniforms]", Integer.valueOf(GlStateManager.glGetInteger(35657)));
      GlStateManager.glGetError();
      var1.addStatToSnooper("gl_caps[gl_max_vertex_attribs]", Integer.valueOf(GlStateManager.glGetInteger(34921)));
      GlStateManager.glGetError();
      var1.addStatToSnooper("gl_caps[gl_max_vertex_texture_image_units]", Integer.valueOf(GlStateManager.glGetInteger(35660)));
      GlStateManager.glGetError();
      var1.addStatToSnooper("gl_caps[gl_max_texture_image_units]", Integer.valueOf(GlStateManager.glGetInteger(34930)));
      GlStateManager.glGetError();
      var1.addStatToSnooper("gl_caps[gl_max_array_texture_layers]", Integer.valueOf(GlStateManager.glGetInteger(35071)));
      GlStateManager.glGetError();
      var1.addStatToSnooper("gl_max_texture_size", Integer.valueOf(getGLMaximumTextureSize()));
      GameProfile var3 = this.session.getProfile();
      if (var3 != null && var3.getId() != null) {
         var1.addStatToSnooper("uuid", Hashing.sha1().hashBytes(var3.getId().toString().getBytes(Charsets.ISO_8859_1)).toString());
      }

   }

   public static int getGLMaximumTextureSize() {
      return SplashProgress.getMaxTextureSize();
   }

   public boolean isSnooperEnabled() {
      return this.gameSettings.snooperEnabled;
   }

   public void setServerData(ServerData var1) {
      this.currentServerData = var1;
   }

   @Nullable
   public ServerData getCurrentServerData() {
      return this.currentServerData;
   }

   public boolean isIntegratedServerRunning() {
      return this.integratedServerIsRunning;
   }

   public boolean isSingleplayer() {
      return this.integratedServerIsRunning && this.theIntegratedServer != null;
   }

   @Nullable
   public IntegratedServer getIntegratedServer() {
      return this.theIntegratedServer;
   }

   public static void stopIntegratedServer() {
      if (theMinecraft != null) {
         IntegratedServer var0 = theMinecraft.getIntegratedServer();
         if (var0 != null) {
            var0.stopServer();
         }
      }

   }

   public Snooper getPlayerUsageSnooper() {
      return this.usageSnooper;
   }

   public static long getSystemTime() {
      return Sys.getTime() * 1000L / Sys.getTimerResolution();
   }

   public boolean isFullScreen() {
      return this.fullscreen;
   }

   public Session getSession() {
      return this.session;
   }

   public PropertyMap getProfileProperties() {
      if (this.profileProperties.isEmpty()) {
         GameProfile var1 = this.getSessionService().fillProfileProperties(this.session.getProfile(), false);
         this.profileProperties.putAll(var1.getProperties());
      }

      return this.profileProperties;
   }

   public Proxy getProxy() {
      return this.proxy;
   }

   public TextureManager getTextureManager() {
      return this.renderEngine;
   }

   public IResourceManager getResourceManager() {
      return this.mcResourceManager;
   }

   public ResourcePackRepository getResourcePackRepository() {
      return this.mcResourcePackRepository;
   }

   public LanguageManager getLanguageManager() {
      return this.mcLanguageManager;
   }

   public TextureMap getTextureMapBlocks() {
      return this.textureMapBlocks;
   }

   public boolean isJava64bit() {
      return this.jvm64bit;
   }

   public boolean isGamePaused() {
      return this.isGamePaused;
   }

   public SoundHandler getSoundHandler() {
      return this.mcSoundHandler;
   }

   public MusicTicker.MusicType getAmbientMusicType() {
      return this.player != null ? (this.player.world.provider instanceof WorldProviderHell ? MusicTicker.MusicType.NETHER : (this.player.world.provider instanceof WorldProviderEnd ? (this.ingameGUI.getBossOverlay().shouldPlayEndBossMusic() ? MusicTicker.MusicType.END_BOSS : MusicTicker.MusicType.END) : (this.player.capabilities.isCreativeMode && this.player.capabilities.allowFlying ? MusicTicker.MusicType.CREATIVE : MusicTicker.MusicType.GAME))) : MusicTicker.MusicType.MENU;
   }

   public void dispatchKeypresses() {
      int var1 = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
      if (var1 != 0 && !Keyboard.isRepeatEvent() && (!(this.currentScreen instanceof GuiControls) || ((GuiControls)this.currentScreen).time <= getSystemTime() - 20L)) {
         if (Keyboard.getEventKeyState()) {
            if (this.gameSettings.keyBindFullscreen.isActiveAndMatches(var1)) {
               this.toggleFullscreen();
            } else if (this.gameSettings.keyBindScreenshot.isActiveAndMatches(var1)) {
               this.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(this.mcDataDir, this.displayWidth, this.displayHeight, this.framebufferMc));
            }
         } else if (this.currentScreen instanceof GuiControls) {
            ((GuiControls)this.currentScreen).buttonId = null;
         }
      }

   }

   public MinecraftSessionService getSessionService() {
      return this.sessionService;
   }

   public SkinManager getSkinManager() {
      return this.skinManager;
   }

   @Nullable
   public Entity getRenderViewEntity() {
      return this.renderViewEntity;
   }

   public void setRenderViewEntity(Entity var1) {
      this.renderViewEntity = var1;
      this.entityRenderer.loadEntityShader(var1);
   }

   public ListenableFuture addScheduledTask(Callable var1) {
      Validate.notNull(var1);
      if (this.isCallingFromMinecraftThread()) {
         try {
            return Futures.immediateFuture(var1.call());
         } catch (Exception var5) {
            return Futures.immediateFailedCheckedFuture(var5);
         }
      } else {
         ListenableFutureTask var2 = ListenableFutureTask.create(var1);
         synchronized(this.scheduledTasks) {
            this.scheduledTasks.add(var2);
            return var2;
         }
      }
   }

   public ListenableFuture addScheduledTask(Runnable var1) {
      Validate.notNull(var1);
      return this.addScheduledTask(Executors.callable(var1));
   }

   public boolean isCallingFromMinecraftThread() {
      return Thread.currentThread() == this.mcThread;
   }

   public BlockRendererDispatcher getBlockRendererDispatcher() {
      return this.blockRenderDispatcher;
   }

   public RenderManager getRenderManager() {
      return this.renderManager;
   }

   public RenderItem getRenderItem() {
      return this.renderItem;
   }

   public ItemRenderer getItemRenderer() {
      return this.itemRenderer;
   }

   public static int getDebugFPS() {
      return debugFPS;
   }

   public FrameTimer getFrameTimer() {
      return this.frameTimer;
   }

   public boolean isConnectedToRealms() {
      return this.connectedToRealms;
   }

   public void setConnectedToRealms(boolean var1) {
      this.connectedToRealms = var1;
   }

   public DataFixer getDataFixer() {
      return this.dataFixer;
   }

   public float getRenderPartialTicks() {
      return this.timer.renderPartialTicks;
   }

   public BlockColors getBlockColors() {
      return this.blockColors;
   }

   public ItemColors getItemColors() {
      return this.itemColors;
   }

   public boolean isReducedDebug() {
      return this.player != null && this.player.hasReducedDebug() || this.gameSettings.reducedDebugInfo;
   }
}
