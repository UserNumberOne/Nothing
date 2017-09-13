package net.minecraft.client.gui.achievement;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class GuiAchievements extends GuiScreen implements IProgressMeter {
   private static final int X_MIN = AchievementList.minDisplayColumn * 24 - 112;
   private static final int Y_MIN = AchievementList.minDisplayRow * 24 - 112;
   private static final int X_MAX = AchievementList.maxDisplayColumn * 24 - 77;
   private static final int Y_MAX = AchievementList.maxDisplayRow * 24 - 77;
   private static final ResourceLocation ACHIEVEMENT_BACKGROUND = new ResourceLocation("textures/gui/achievement/achievement_background.png");
   protected GuiScreen parentScreen;
   protected int imageWidth = 256;
   protected int imageHeight = 202;
   protected int xLastScroll;
   protected int yLastScroll;
   protected float zoom = 1.0F;
   protected double xScrollO;
   protected double yScrollO;
   protected double xScrollP;
   protected double yScrollP;
   protected double xScrollTarget;
   protected double yScrollTarget;
   private int scrolling;
   private final StatisticsManager statFileWriter;
   private boolean loadingAchievements = true;
   private int currentPage = -1;
   private GuiButton button;
   private LinkedList minecraftAchievements = new LinkedList();

   public GuiAchievements(GuiScreen var1, StatisticsManager var2) {
      this.parentScreen = var1;
      this.statFileWriter = var2;
      boolean var3 = true;
      boolean var4 = true;
      this.xScrollTarget = (double)(AchievementList.OPEN_INVENTORY.displayColumn * 24 - 70 - 12);
      this.xScrollO = this.xScrollTarget;
      this.xScrollP = this.xScrollTarget;
      this.yScrollTarget = (double)(AchievementList.OPEN_INVENTORY.displayRow * 24 - 70);
      this.yScrollO = this.yScrollTarget;
      this.yScrollP = this.yScrollTarget;
      this.minecraftAchievements.clear();

      for(Achievement var6 : AchievementList.ACHIEVEMENTS) {
         if (!AchievementPage.isAchievementInPages(var6)) {
            this.minecraftAchievements.add(var6);
         }
      }

   }

   public void initGui() {
      this.mc.getConnection().sendPacket(new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
      this.buttonList.clear();
      this.buttonList.add(new GuiOptionButton(1, this.width / 2 + 24, this.height / 2 + 74, 80, 20, I18n.format("gui.done")));
      this.buttonList.add(this.button = new GuiButton(2, (this.width - this.imageWidth) / 2 + 24, this.height / 2 + 74, 125, 20, AchievementPage.getTitle(this.currentPage)));
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (!this.loadingAchievements) {
         if (var1.id == 1) {
            this.mc.displayGuiScreen(this.parentScreen);
         }

         if (var1.id == 2) {
            ++this.currentPage;
            if (this.currentPage >= AchievementPage.getAchievementPages().size()) {
               this.currentPage = -1;
            }

            this.button.displayString = AchievementPage.getTitle(this.currentPage);
         }
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (this.mc.gameSettings.keyBindInventory.isActiveAndMatches(var2)) {
         this.mc.displayGuiScreen((GuiScreen)null);
         this.mc.setIngameFocus();
      } else {
         super.keyTyped(var1, var2);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      if (this.loadingAchievements) {
         this.drawDefaultBackground();
         this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingStats"), this.width / 2, this.height / 2, 16777215);
         this.drawCenteredString(this.fontRendererObj, LOADING_STRINGS[(int)(Minecraft.getSystemTime() / 150L % (long)LOADING_STRINGS.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2, 16777215);
      } else {
         if (Mouse.isButtonDown(0)) {
            int var4 = (this.width - this.imageWidth) / 2;
            int var5 = (this.height - this.imageHeight) / 2;
            int var6 = var4 + 8;
            int var7 = var5 + 17;
            if ((this.scrolling == 0 || this.scrolling == 1) && var1 >= var6 && var1 < var6 + 224 && var2 >= var7 && var2 < var7 + 155) {
               if (this.scrolling == 0) {
                  this.scrolling = 1;
               } else {
                  this.xScrollP -= (double)((float)(var1 - this.xLastScroll) * this.zoom);
                  this.yScrollP -= (double)((float)(var2 - this.yLastScroll) * this.zoom);
                  this.xScrollO = this.xScrollP;
                  this.yScrollO = this.yScrollP;
                  this.xScrollTarget = this.xScrollP;
                  this.yScrollTarget = this.yScrollP;
               }

               this.xLastScroll = var1;
               this.yLastScroll = var2;
            }
         } else {
            this.scrolling = 0;
         }

         int var10 = Mouse.getDWheel();
         float var11 = this.zoom;
         if (var10 < 0) {
            this.zoom += 0.25F;
         } else if (var10 > 0) {
            this.zoom -= 0.25F;
         }

         this.zoom = MathHelper.clamp(this.zoom, 1.0F, 2.0F);
         if (this.zoom != var11) {
            float var12 = var11 * (float)this.imageWidth;
            float var13 = var11 * (float)this.imageHeight;
            float var8 = this.zoom * (float)this.imageWidth;
            float var9 = this.zoom * (float)this.imageHeight;
            this.xScrollP -= (double)((var8 - var12) * 0.5F);
            this.yScrollP -= (double)((var9 - var13) * 0.5F);
            this.xScrollO = this.xScrollP;
            this.yScrollO = this.yScrollP;
            this.xScrollTarget = this.xScrollP;
            this.yScrollTarget = this.yScrollP;
         }

         if (this.xScrollTarget < (double)X_MIN) {
            this.xScrollTarget = (double)X_MIN;
         }

         if (this.yScrollTarget < (double)Y_MIN) {
            this.yScrollTarget = (double)Y_MIN;
         }

         if (this.xScrollTarget >= (double)X_MAX) {
            this.xScrollTarget = (double)(X_MAX - 1);
         }

         if (this.yScrollTarget >= (double)Y_MAX) {
            this.yScrollTarget = (double)(Y_MAX - 1);
         }

         this.drawDefaultBackground();
         this.drawAchievementScreen(var1, var2, var3);
         GlStateManager.disableLighting();
         GlStateManager.disableDepth();
         this.drawTitle();
         GlStateManager.enableLighting();
         GlStateManager.enableDepth();
      }

   }

   public void doneLoading() {
      if (this.loadingAchievements) {
         this.loadingAchievements = false;
      }

   }

   public void updateScreen() {
      if (!this.loadingAchievements) {
         this.xScrollO = this.xScrollP;
         this.yScrollO = this.yScrollP;
         double var1 = this.xScrollTarget - this.xScrollP;
         double var3 = this.yScrollTarget - this.yScrollP;
         if (var1 * var1 + var3 * var3 < 4.0D) {
            this.xScrollP += var1;
            this.yScrollP += var3;
         } else {
            this.xScrollP += var1 * 0.85D;
            this.yScrollP += var3 * 0.85D;
         }
      }

   }

   protected void drawTitle() {
      int var1 = (this.width - this.imageWidth) / 2;
      int var2 = (this.height - this.imageHeight) / 2;
      this.fontRendererObj.drawString(I18n.format("gui.achievements"), var1 + 15, var2 + 5, 4210752);
   }

   protected void drawAchievementScreen(int var1, int var2, float var3) {
      int var4 = MathHelper.floor(this.xScrollO + (this.xScrollP - this.xScrollO) * (double)var3);
      int var5 = MathHelper.floor(this.yScrollO + (this.yScrollP - this.yScrollO) * (double)var3);
      if (var4 < X_MIN) {
         var4 = X_MIN;
      }

      if (var5 < Y_MIN) {
         var5 = Y_MIN;
      }

      if (var4 >= X_MAX) {
         var4 = X_MAX - 1;
      }

      if (var5 >= Y_MAX) {
         var5 = Y_MAX - 1;
      }

      int var6 = (this.width - this.imageWidth) / 2;
      int var7 = (this.height - this.imageHeight) / 2;
      int var8 = var6 + 16;
      int var9 = var7 + 17;
      this.zLevel = 0.0F;
      GlStateManager.depthFunc(518);
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)var8, (float)var9, -200.0F);
      GlStateManager.scale(1.0F / this.zoom, 1.0F / this.zoom, 1.0F);
      GlStateManager.enableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableColorMaterial();
      int var10 = var4 + 288 >> 4;
      int var11 = var5 + 288 >> 4;
      int var12 = (var4 + 288) % 16;
      int var13 = (var5 + 288) % 16;
      boolean var14 = true;
      boolean var15 = true;
      boolean var16 = true;
      boolean var17 = true;
      boolean var18 = true;
      Random var19 = new Random();
      float var20 = 16.0F / this.zoom;
      float var21 = 16.0F / this.zoom;

      for(int var22 = 0; (float)var22 * var20 - (float)var13 < 155.0F; ++var22) {
         float var23 = 0.6F - (float)(var11 + var22) / 25.0F * 0.3F;
         GlStateManager.color(var23, var23, var23, 1.0F);

         for(int var24 = 0; (float)var24 * var21 - (float)var12 < 224.0F; ++var24) {
            var19.setSeed((long)(this.mc.getSession().getPlayerID().hashCode() + var10 + var24 + (var11 + var22) * 16));
            int var25 = var19.nextInt(1 + var11 + var22) + (var11 + var22) / 2;
            TextureAtlasSprite var26 = this.getTexture(Blocks.SAND);
            if (var25 <= 37 && var11 + var22 != 35) {
               if (var25 == 22) {
                  if (var19.nextInt(2) == 0) {
                     var26 = this.getTexture(Blocks.DIAMOND_ORE);
                  } else {
                     var26 = this.getTexture(Blocks.REDSTONE_ORE);
                  }
               } else if (var25 == 10) {
                  var26 = this.getTexture(Blocks.IRON_ORE);
               } else if (var25 == 8) {
                  var26 = this.getTexture(Blocks.COAL_ORE);
               } else if (var25 > 4) {
                  var26 = this.getTexture(Blocks.STONE);
               } else if (var25 > 0) {
                  var26 = this.getTexture(Blocks.DIRT);
               }
            } else {
               Block var27 = Blocks.BEDROCK;
               var26 = this.getTexture(var27);
            }

            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.drawTexturedModalRect(var24 * 16 - var12, var22 * 16 - var13, var26, 16, 16);
         }
      }

      GlStateManager.enableDepth();
      GlStateManager.depthFunc(515);
      this.mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
      Object var34 = this.currentPage == -1 ? this.minecraftAchievements : AchievementPage.getAchievementPage(this.currentPage).getAchievements();

      for(int var35 = 0; var35 < ((List)var34).size(); ++var35) {
         Achievement var37 = (Achievement)((List)var34).get(var35);
         if (var37.parentAchievement != null && ((List)var34).contains(var37.parentAchievement)) {
            int var39 = var37.displayColumn * 24 - var4 + 11;
            int var41 = var37.displayRow * 24 - var5 + 11;
            int var44 = var37.parentAchievement.displayColumn * 24 - var4 + 11;
            int var28 = var37.parentAchievement.displayRow * 24 - var5 + 11;
            boolean var29 = this.statFileWriter.hasAchievementUnlocked(var37);
            boolean var30 = this.statFileWriter.canUnlockAchievement(var37);
            int var31 = this.statFileWriter.countRequirementsUntilAvailable(var37);
            if (var31 <= 4) {
               int var32 = -16777216;
               if (var29) {
                  var32 = -6250336;
               } else if (var30) {
                  var32 = -16711936;
               }

               this.drawHorizontalLine(var39, var44, var41, var32);
               this.drawVerticalLine(var44, var41, var28, var32);
               if (var39 > var44) {
                  this.drawTexturedModalRect(var39 - 11 - 7, var41 - 5, 114, 234, 7, 11);
               } else if (var39 < var44) {
                  this.drawTexturedModalRect(var39 + 11, var41 - 5, 107, 234, 7, 11);
               } else if (var41 > var28) {
                  this.drawTexturedModalRect(var39 - 5, var41 - 11 - 7, 96, 234, 11, 7);
               } else if (var41 < var28) {
                  this.drawTexturedModalRect(var39 - 5, var41 + 11, 96, 241, 11, 7);
               }
            }
         }
      }

      Achievement var36 = null;
      float var38 = (float)(var1 - var8) * this.zoom;
      float var40 = (float)(var2 - var9) * this.zoom;
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableColorMaterial();

      for(int var42 = 0; var42 < ((List)var34).size(); ++var42) {
         Achievement var45 = (Achievement)((List)var34).get(var42);
         int var47 = var45.displayColumn * 24 - var4;
         int var49 = var45.displayRow * 24 - var5;
         if (var47 >= -24 && var49 >= -24 && (float)var47 <= 224.0F * this.zoom && (float)var49 <= 155.0F * this.zoom) {
            int var51 = this.statFileWriter.countRequirementsUntilAvailable(var45);
            if (this.statFileWriter.hasAchievementUnlocked(var45)) {
               float var53 = 0.75F;
               GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
            } else if (this.statFileWriter.canUnlockAchievement(var45)) {
               float var54 = 1.0F;
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            } else if (var51 < 3) {
               float var55 = 0.3F;
               GlStateManager.color(0.3F, 0.3F, 0.3F, 1.0F);
            } else if (var51 == 3) {
               float var56 = 0.2F;
               GlStateManager.color(0.2F, 0.2F, 0.2F, 1.0F);
            } else {
               if (var51 != 4) {
                  continue;
               }

               float var57 = 0.1F;
               GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F);
            }

            this.mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
            GlStateManager.enableBlend();
            if (var45.getSpecial()) {
               this.drawTexturedModalRect(var47 - 2, var49 - 2, 26, 202, 26, 26);
            } else {
               this.drawTexturedModalRect(var47 - 2, var49 - 2, 0, 202, 26, 26);
            }

            GlStateManager.disableBlend();
            if (!this.statFileWriter.canUnlockAchievement(var45)) {
               float var58 = 0.1F;
               GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F);
               this.itemRender.isNotRenderingEffectsInGUI(false);
            }

            GlStateManager.disableLighting();
            GlStateManager.enableCull();
            this.itemRender.renderItemAndEffectIntoGUI(var45.theItemStack, var47 + 3, var49 + 3);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();
            if (!this.statFileWriter.canUnlockAchievement(var45)) {
               this.itemRender.isNotRenderingEffectsInGUI(true);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (var38 >= (float)var47 && var38 <= (float)(var47 + 22) && var40 >= (float)var49 && var40 <= (float)(var49 + 22)) {
               var36 = var45;
            }
         }
      }

      GlStateManager.disableDepth();
      GlStateManager.enableBlend();
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
      this.drawTexturedModalRect(var6, var7, 0, 0, this.imageWidth, this.imageHeight);
      this.zLevel = 0.0F;
      GlStateManager.depthFunc(515);
      GlStateManager.disableDepth();
      GlStateManager.enableTexture2D();
      super.drawScreen(var1, var2, var3);
      if (var36 != null) {
         String var43 = var36.getStatName().getUnformattedText();
         String var46 = var36.getDescription();
         int var48 = var1 + 12;
         int var50 = var2 - 4;
         int var52 = this.statFileWriter.countRequirementsUntilAvailable(var36);
         if (this.statFileWriter.canUnlockAchievement(var36)) {
            int var59 = Math.max(this.fontRendererObj.getStringWidth(var43), 120);
            int var62 = this.fontRendererObj.splitStringWidth(var46, var59);
            if (this.statFileWriter.hasAchievementUnlocked(var36)) {
               var62 += 12;
            }

            this.drawGradientRect(var48 - 3, var50 - 3, var48 + var59 + 3, var50 + var62 + 3 + 12, -1073741824, -1073741824);
            this.fontRendererObj.drawSplitString(var46, var48, var50 + 12, var59, -6250336);
            if (this.statFileWriter.hasAchievementUnlocked(var36)) {
               this.fontRendererObj.drawStringWithShadow(I18n.format("achievement.taken"), (float)var48, (float)(var50 + var62 + 4), -7302913);
            }
         } else if (var52 == 3) {
            var43 = I18n.format("achievement.unknown");
            int var60 = Math.max(this.fontRendererObj.getStringWidth(var43), 120);
            String var63 = (new TextComponentTranslation("achievement.requires", new Object[]{var36.parentAchievement.getStatName()})).getUnformattedText();
            int var33 = this.fontRendererObj.splitStringWidth(var63, var60);
            this.drawGradientRect(var48 - 3, var50 - 3, var48 + var60 + 3, var50 + var33 + 12 + 3, -1073741824, -1073741824);
            this.fontRendererObj.drawSplitString(var63, var48, var50 + 12, var60, -9416624);
         } else if (var52 < 3) {
            int var61 = Math.max(this.fontRendererObj.getStringWidth(var43), 120);
            String var64 = (new TextComponentTranslation("achievement.requires", new Object[]{var36.parentAchievement.getStatName()})).getUnformattedText();
            int var65 = this.fontRendererObj.splitStringWidth(var64, var61);
            this.drawGradientRect(var48 - 3, var50 - 3, var48 + var61 + 3, var50 + var65 + 12 + 3, -1073741824, -1073741824);
            this.fontRendererObj.drawSplitString(var64, var48, var50 + 12, var61, -9416624);
         } else {
            var43 = null;
         }

         if (var43 != null) {
            this.fontRendererObj.drawStringWithShadow(var43, (float)var48, (float)var50, this.statFileWriter.canUnlockAchievement(var36) ? (var36.getSpecial() ? -128 : -1) : (var36.getSpecial() ? -8355776 : -8355712));
         }
      }

      GlStateManager.enableDepth();
      GlStateManager.enableLighting();
      RenderHelper.disableStandardItemLighting();
   }

   private TextureAtlasSprite getTexture(Block var1) {
      return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(var1.getDefaultState());
   }

   public boolean doesGuiPauseGame() {
      return !this.loadingAchievements;
   }
}
