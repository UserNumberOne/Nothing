package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiIngame extends Gui {
   protected static final ResourceLocation VIGNETTE_TEX_PATH = new ResourceLocation("textures/misc/vignette.png");
   protected static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");
   protected static final ResourceLocation PUMPKIN_BLUR_TEX_PATH = new ResourceLocation("textures/misc/pumpkinblur.png");
   protected final Random rand = new Random();
   protected final Minecraft mc;
   protected final RenderItem itemRenderer;
   protected final GuiNewChat persistantChatGUI;
   protected int updateCounter;
   protected String overlayMessage = "";
   protected int overlayMessageTime;
   protected boolean animateOverlayMessageColor;
   public float prevVignetteBrightness = 1.0F;
   protected int remainingHighlightTicks;
   protected ItemStack highlightingItemStack;
   protected final GuiOverlayDebug overlayDebug;
   protected final GuiSubtitleOverlay overlaySubtitle;
   protected final GuiSpectator spectatorGui;
   protected final GuiPlayerTabOverlay overlayPlayerList;
   protected final GuiBossOverlay overlayBoss;
   protected int titlesTimer;
   protected String displayedTitle = "";
   protected String displayedSubTitle = "";
   protected int titleFadeIn;
   protected int titleDisplayTime;
   protected int titleFadeOut;
   protected int playerHealth;
   protected int lastPlayerHealth;
   protected long lastSystemTime;
   protected long healthUpdateCounter;

   public GuiIngame(Minecraft var1) {
      this.mc = var1;
      this.itemRenderer = var1.getRenderItem();
      this.overlayDebug = new GuiOverlayDebug(var1);
      this.spectatorGui = new GuiSpectator(var1);
      this.persistantChatGUI = new GuiNewChat(var1);
      this.overlayPlayerList = new GuiPlayerTabOverlay(var1, this);
      this.overlayBoss = new GuiBossOverlay(var1);
      this.overlaySubtitle = new GuiSubtitleOverlay(var1);
      this.setDefaultTitlesTimes();
   }

   public void setDefaultTitlesTimes() {
      this.titleFadeIn = 10;
      this.titleDisplayTime = 70;
      this.titleFadeOut = 20;
   }

   public void renderGameOverlay(float var1) {
      ScaledResolution var2 = new ScaledResolution(this.mc);
      int var3 = var2.getScaledWidth();
      int var4 = var2.getScaledHeight();
      FontRenderer var5 = this.getFontRenderer();
      this.mc.entityRenderer.setupOverlayRendering();
      GlStateManager.enableBlend();
      if (Minecraft.isFancyGraphicsEnabled()) {
         this.renderVignette(this.mc.player.getBrightness(var1), var2);
      } else {
         GlStateManager.enableDepth();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      }

      ItemStack var6 = this.mc.player.inventory.armorItemInSlot(3);
      if (this.mc.gameSettings.thirdPersonView == 0 && var6 != null && var6.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
         this.renderPumpkinOverlay(var2);
      }

      if (!this.mc.player.isPotionActive(MobEffects.NAUSEA)) {
         float var7 = this.mc.player.prevTimeInPortal + (this.mc.player.timeInPortal - this.mc.player.prevTimeInPortal) * var1;
         if (var7 > 0.0F) {
            this.renderPortal(var7, var2);
         }
      }

      if (this.mc.playerController.isSpectator()) {
         this.spectatorGui.renderTooltip(var2, var1);
      } else {
         this.renderHotbar(var2, var1);
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(ICONS);
      GlStateManager.enableBlend();
      this.renderAttackIndicator(var1, var2);
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      this.mc.mcProfiler.startSection("bossHealth");
      this.overlayBoss.renderBossHealth();
      this.mc.mcProfiler.endSection();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(ICONS);
      if (this.mc.playerController.shouldDrawHUD()) {
         this.renderPlayerStats(var2);
      }

      this.renderMountHealth(var2);
      GlStateManager.disableBlend();
      if (this.mc.player.getSleepTimer() > 0) {
         this.mc.mcProfiler.startSection("sleep");
         GlStateManager.disableDepth();
         GlStateManager.disableAlpha();
         int var12 = this.mc.player.getSleepTimer();
         float var8 = (float)var12 / 100.0F;
         if (var8 > 1.0F) {
            var8 = 1.0F - (float)(var12 - 100) / 10.0F;
         }

         int var9 = (int)(220.0F * var8) << 24 | 1052704;
         drawRect(0, 0, var3, var4, var9);
         GlStateManager.enableAlpha();
         GlStateManager.enableDepth();
         this.mc.mcProfiler.endSection();
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      int var13 = var3 / 2 - 91;
      if (this.mc.player.isRidingHorse()) {
         this.renderHorseJumpBar(var2, var13);
      } else if (this.mc.playerController.gameIsSurvivalOrAdventure()) {
         this.renderExpBar(var2, var13);
      }

      if (this.mc.gameSettings.heldItemTooltips && !this.mc.playerController.isSpectator()) {
         this.renderSelectedItem(var2);
      } else if (this.mc.player.isSpectator()) {
         this.spectatorGui.renderSelectedItem(var2);
      }

      if (this.mc.isDemo()) {
         this.renderDemo(var2);
      }

      this.renderPotionEffects(var2);
      if (this.mc.gameSettings.showDebugInfo) {
         this.overlayDebug.renderDebugInfo(var2);
      }

      if (this.overlayMessageTime > 0) {
         this.mc.mcProfiler.startSection("overlayMessage");
         float var14 = (float)this.overlayMessageTime - var1;
         int var17 = (int)(var14 * 255.0F / 20.0F);
         if (var17 > 255) {
            var17 = 255;
         }

         if (var17 > 8) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)(var3 / 2), (float)(var4 - 68), 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int var10 = 16777215;
            if (this.animateOverlayMessageColor) {
               var10 = MathHelper.hsvToRGB(var14 / 50.0F, 0.7F, 0.6F) & 16777215;
            }

            var5.drawString(this.overlayMessage, -var5.getStringWidth(this.overlayMessage) / 2, -4, var10 + (var17 << 24 & -16777216));
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         }

         this.mc.mcProfiler.endSection();
      }

      this.overlaySubtitle.renderSubtitles(var2);
      if (this.titlesTimer > 0) {
         this.mc.mcProfiler.startSection("titleAndSubtitle");
         float var15 = (float)this.titlesTimer - var1;
         int var18 = 255;
         if (this.titlesTimer > this.titleFadeOut + this.titleDisplayTime) {
            float var21 = (float)(this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut) - var15;
            var18 = (int)(var21 * 255.0F / (float)this.titleFadeIn);
         }

         if (this.titlesTimer <= this.titleFadeOut) {
            var18 = (int)(var15 * 255.0F / (float)this.titleFadeOut);
         }

         var18 = MathHelper.clamp(var18, 0, 255);
         if (var18 > 8) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)(var3 / 2), (float)(var4 / 2), 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 4.0F);
            int var22 = var18 << 24 & -16777216;
            var5.drawString(this.displayedTitle, (float)(-var5.getStringWidth(this.displayedTitle) / 2), -10.0F, 16777215 | var22, true);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            var5.drawString(this.displayedSubTitle, (float)(-var5.getStringWidth(this.displayedSubTitle) / 2), 5.0F, 16777215 | var22, true);
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         }

         this.mc.mcProfiler.endSection();
      }

      Scoreboard var16 = this.mc.world.getScoreboard();
      ScoreObjective var20 = null;
      ScorePlayerTeam var23 = var16.getPlayersTeam(this.mc.player.getName());
      if (var23 != null) {
         int var11 = var23.getChatFormat().getColorIndex();
         if (var11 >= 0) {
            var20 = var16.getObjectiveInDisplaySlot(3 + var11);
         }
      }

      ScoreObjective var24 = var20 != null ? var20 : var16.getObjectiveInDisplaySlot(1);
      if (var24 != null) {
         this.renderScoreboard(var24, var2);
      }

      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.disableAlpha();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, (float)(var4 - 48), 0.0F);
      this.mc.mcProfiler.startSection("chat");
      this.persistantChatGUI.drawChat(this.updateCounter);
      this.mc.mcProfiler.endSection();
      GlStateManager.popMatrix();
      var24 = var16.getObjectiveInDisplaySlot(0);
      if (this.mc.gameSettings.keyBindPlayerList.isKeyDown() && (!this.mc.isIntegratedServerRunning() || this.mc.player.connection.getPlayerInfoMap().size() > 1 || var24 != null)) {
         this.overlayPlayerList.updatePlayerList(true);
         this.overlayPlayerList.renderPlayerlist(var3, var16, var24);
      } else {
         this.overlayPlayerList.updatePlayerList(false);
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableLighting();
      GlStateManager.enableAlpha();
   }

   protected void renderAttackIndicator(float var1, ScaledResolution var2) {
      GameSettings var3 = this.mc.gameSettings;
      if (var3.thirdPersonView == 0) {
         if (this.mc.playerController.isSpectator() && this.mc.pointedEntity == null) {
            RayTraceResult var4 = this.mc.objectMouseOver;
            if (var4 == null || var4.typeOfHit != RayTraceResult.Type.BLOCK) {
               return;
            }

            BlockPos var5 = var4.getBlockPos();
            IBlockState var6 = this.mc.world.getBlockState(var5);
            if (!var6.getBlock().hasTileEntity(var6) || !(this.mc.world.getTileEntity(var5) instanceof IInventory)) {
               return;
            }
         }

         int var10 = var2.getScaledWidth();
         int var11 = var2.getScaledHeight();
         if (var3.showDebugInfo && !var3.hideGUI && !this.mc.player.hasReducedDebug() && !var3.reducedDebugInfo) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)(var10 / 2), (float)(var11 / 2), this.zLevel);
            Entity var13 = this.mc.getRenderViewEntity();
            GlStateManager.rotate(var13.prevRotationPitch + (var13.rotationPitch - var13.prevRotationPitch) * var1, -1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(var13.prevRotationYaw + (var13.rotationYaw - var13.prevRotationYaw) * var1, 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(-1.0F, -1.0F, -1.0F);
            OpenGlHelper.renderDirections(10);
            GlStateManager.popMatrix();
         } else {
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.enableAlpha();
            this.drawTexturedModalRect(var10 / 2 - 7, var11 / 2 - 7, 0, 0, 16, 16);
            if (this.mc.gameSettings.attackIndicator == 1) {
               float var12 = this.mc.player.getCooledAttackStrength(0.0F);
               if (var12 < 1.0F) {
                  int var7 = var11 / 2 - 7 + 16;
                  int var8 = var10 / 2 - 7;
                  int var9 = (int)(var12 * 17.0F);
                  this.drawTexturedModalRect(var8, var7, 36, 94, 16, 4);
                  this.drawTexturedModalRect(var8, var7, 52, 94, var9, 4);
               }
            }
         }
      }

   }

   protected void renderPotionEffects(ScaledResolution var1) {
      Collection var2 = this.mc.player.getActivePotionEffects();
      if (!var2.isEmpty()) {
         this.mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
         GlStateManager.enableBlend();
         int var3 = 0;
         int var4 = 0;

         for(PotionEffect var6 : Ordering.natural().reverse().sortedCopy(var2)) {
            Potion var7 = var6.getPotion();
            if (var7.shouldRenderHUD(var6)) {
               this.mc.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
               if (var6.doesShowParticles()) {
                  int var8 = var1.getScaledWidth();
                  int var9 = 1;
                  int var10 = var7.getStatusIconIndex();
                  if (var7.isBeneficial()) {
                     ++var3;
                     var8 = var8 - 25 * var3;
                  } else {
                     ++var4;
                     var8 = var8 - 25 * var4;
                     var9 += 26;
                  }

                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  float var11 = 1.0F;
                  if (var6.getIsAmbient()) {
                     this.drawTexturedModalRect(var8, var9, 165, 166, 24, 24);
                  } else {
                     this.drawTexturedModalRect(var8, var9, 141, 166, 24, 24);
                     if (var6.getDuration() <= 200) {
                        int var12 = 10 - var6.getDuration() / 20;
                        var11 = MathHelper.clamp((float)var6.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)var6.getDuration() * 3.1415927F / 5.0F) * MathHelper.clamp((float)var12 / 10.0F * 0.25F, 0.0F, 0.25F);
                     }
                  }

                  GlStateManager.color(1.0F, 1.0F, 1.0F, var11);
                  if (var7.hasStatusIcon()) {
                     this.drawTexturedModalRect(var8 + 3, var9 + 3, var10 % 8 * 18, 198 + var10 / 8 * 18, 18, 18);
                  }

                  var7.renderHUDEffect(var8, var9, var6, this.mc, var11);
               }
            }
         }
      }

   }

   protected void renderHotbar(ScaledResolution var1, float var2) {
      if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);
         EntityPlayer var3 = (EntityPlayer)this.mc.getRenderViewEntity();
         ItemStack var4 = var3.getHeldItemOffhand();
         EnumHandSide var5 = var3.getPrimaryHand().opposite();
         int var6 = var1.getScaledWidth() / 2;
         float var7 = this.zLevel;
         boolean var8 = true;
         boolean var9 = true;
         this.zLevel = -90.0F;
         this.drawTexturedModalRect(var6 - 91, var1.getScaledHeight() - 22, 0, 0, 182, 22);
         this.drawTexturedModalRect(var6 - 91 - 1 + var3.inventory.currentItem * 20, var1.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
         if (var4 != null) {
            if (var5 == EnumHandSide.LEFT) {
               this.drawTexturedModalRect(var6 - 91 - 29, var1.getScaledHeight() - 23, 24, 22, 29, 24);
            } else {
               this.drawTexturedModalRect(var6 + 91, var1.getScaledHeight() - 23, 53, 22, 29, 24);
            }
         }

         this.zLevel = var7;
         GlStateManager.enableRescaleNormal();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         RenderHelper.enableGUIStandardItemLighting();

         for(int var10 = 0; var10 < 9; ++var10) {
            int var11 = var6 - 90 + var10 * 20 + 2;
            int var12 = var1.getScaledHeight() - 16 - 3;
            this.renderHotbarItem(var11, var12, var2, var3, var3.inventory.mainInventory[var10]);
         }

         if (var4 != null) {
            int var14 = var1.getScaledHeight() - 16 - 3;
            if (var5 == EnumHandSide.LEFT) {
               this.renderHotbarItem(var6 - 91 - 26, var14, var2, var3, var4);
            } else {
               this.renderHotbarItem(var6 + 91 + 10, var14, var2, var3, var4);
            }
         }

         if (this.mc.gameSettings.attackIndicator == 2) {
            float var15 = this.mc.player.getCooledAttackStrength(0.0F);
            if (var15 < 1.0F) {
               int var16 = var1.getScaledHeight() - 20;
               int var17 = var6 + 91 + 6;
               if (var5 == EnumHandSide.RIGHT) {
                  var17 = var6 - 91 - 22;
               }

               this.mc.getTextureManager().bindTexture(Gui.ICONS);
               int var13 = (int)(var15 * 19.0F);
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               this.drawTexturedModalRect(var17, var16, 0, 94, 18, 18);
               this.drawTexturedModalRect(var17, var16 + 18 - var13, 18, 112 - var13, 18, var13);
            }
         }

         RenderHelper.disableStandardItemLighting();
         GlStateManager.disableRescaleNormal();
         GlStateManager.disableBlend();
      }

   }

   public void renderHorseJumpBar(ScaledResolution var1, int var2) {
      this.mc.mcProfiler.startSection("jumpBar");
      this.mc.getTextureManager().bindTexture(Gui.ICONS);
      float var3 = this.mc.player.getHorseJumpPower();
      boolean var4 = true;
      int var5 = (int)(var3 * 183.0F);
      int var6 = var1.getScaledHeight() - 32 + 3;
      this.drawTexturedModalRect(var2, var6, 0, 84, 182, 5);
      if (var5 > 0) {
         this.drawTexturedModalRect(var2, var6, 0, 89, var5, 5);
      }

      this.mc.mcProfiler.endSection();
   }

   public void renderExpBar(ScaledResolution var1, int var2) {
      this.mc.mcProfiler.startSection("expBar");
      this.mc.getTextureManager().bindTexture(Gui.ICONS);
      int var3 = this.mc.player.xpBarCap();
      if (var3 > 0) {
         boolean var4 = true;
         int var5 = (int)(this.mc.player.experience * 183.0F);
         int var6 = var1.getScaledHeight() - 32 + 3;
         this.drawTexturedModalRect(var2, var6, 0, 64, 182, 5);
         if (var5 > 0) {
            this.drawTexturedModalRect(var2, var6, 0, 69, var5, 5);
         }
      }

      this.mc.mcProfiler.endSection();
      if (this.mc.player.experienceLevel > 0) {
         this.mc.mcProfiler.startSection("expLevel");
         String var7 = "" + this.mc.player.experienceLevel;
         int var8 = (var1.getScaledWidth() - this.getFontRenderer().getStringWidth(var7)) / 2;
         int var9 = var1.getScaledHeight() - 31 - 4;
         this.getFontRenderer().drawString(var7, var8 + 1, var9, 0);
         this.getFontRenderer().drawString(var7, var8 - 1, var9, 0);
         this.getFontRenderer().drawString(var7, var8, var9 + 1, 0);
         this.getFontRenderer().drawString(var7, var8, var9 - 1, 0);
         this.getFontRenderer().drawString(var7, var8, var9, 8453920);
         this.mc.mcProfiler.endSection();
      }

   }

   public void renderSelectedItem(ScaledResolution var1) {
      this.mc.mcProfiler.startSection("selectedItemName");
      if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null) {
         String var2 = this.highlightingItemStack.getDisplayName();
         if (this.highlightingItemStack.hasDisplayName()) {
            var2 = TextFormatting.ITALIC + var2;
         }

         int var3 = (var1.getScaledWidth() - this.getFontRenderer().getStringWidth(var2)) / 2;
         int var4 = var1.getScaledHeight() - 59;
         if (!this.mc.playerController.shouldDrawHUD()) {
            var4 += 14;
         }

         int var5 = (int)((float)this.remainingHighlightTicks * 256.0F / 10.0F);
         if (var5 > 255) {
            var5 = 255;
         }

         if (var5 > 0) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            this.getFontRenderer().drawStringWithShadow(var2, (float)var3, (float)var4, 16777215 + (var5 << 24));
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         }
      }

      this.mc.mcProfiler.endSection();
   }

   public void renderDemo(ScaledResolution var1) {
      this.mc.mcProfiler.startSection("demo");
      String var2;
      if (this.mc.world.getTotalWorldTime() >= 120500L) {
         var2 = I18n.format("demo.demoExpired");
      } else {
         var2 = I18n.format("demo.remainingTime", StringUtils.ticksToElapsedTime((int)(120500L - this.mc.world.getTotalWorldTime())));
      }

      int var3 = this.getFontRenderer().getStringWidth(var2);
      this.getFontRenderer().drawStringWithShadow(var2, (float)(var1.getScaledWidth() - var3 - 10), 5.0F, 16777215);
      this.mc.mcProfiler.endSection();
   }

   protected void renderScoreboard(ScoreObjective var1, ScaledResolution var2) {
      Scoreboard var3 = var1.getScoreboard();
      Collection var4 = var3.getSortedScores(var1);
      ArrayList var5 = Lists.newArrayList(Iterables.filter(var4, new Predicate() {
         public boolean apply(@Nullable Score var1) {
            return var1.getPlayerName() != null && !var1.getPlayerName().startsWith("#");
         }
      }));
      ArrayList var20;
      if (var5.size() > 15) {
         var20 = Lists.newArrayList(Iterables.skip(var5, var4.size() - 15));
      } else {
         var20 = var5;
      }

      int var6 = this.getFontRenderer().getStringWidth(var1.getDisplayName());

      for(Score var8 : var20) {
         ScorePlayerTeam var9 = var3.getPlayersTeam(var8.getPlayerName());
         String var10 = ScorePlayerTeam.formatPlayerName(var9, var8.getPlayerName()) + ": " + TextFormatting.RED + var8.getScorePoints();
         var6 = Math.max(var6, this.getFontRenderer().getStringWidth(var10));
      }

      int var21 = var20.size() * this.getFontRenderer().FONT_HEIGHT;
      int var22 = var2.getScaledHeight() / 2 + var21 / 3;
      boolean var23 = true;
      int var24 = var2.getScaledWidth() - var6 - 3;
      int var11 = 0;

      for(Score var13 : var20) {
         ++var11;
         ScorePlayerTeam var14 = var3.getPlayersTeam(var13.getPlayerName());
         String var15 = ScorePlayerTeam.formatPlayerName(var14, var13.getPlayerName());
         String var16 = TextFormatting.RED + "" + var13.getScorePoints();
         int var17 = var22 - var11 * this.getFontRenderer().FONT_HEIGHT;
         int var18 = var2.getScaledWidth() - 3 + 2;
         drawRect(var24 - 2, var17, var18, var17 + this.getFontRenderer().FONT_HEIGHT, 1342177280);
         this.getFontRenderer().drawString(var15, var24, var17, 553648127);
         this.getFontRenderer().drawString(var16, var18 - this.getFontRenderer().getStringWidth(var16), var17, 553648127);
         if (var11 == var20.size()) {
            String var19 = var1.getDisplayName();
            drawRect(var24 - 2, var17 - this.getFontRenderer().FONT_HEIGHT - 1, var18, var17 - 1, 1610612736);
            drawRect(var24 - 2, var17 - 1, var18, var17, 1342177280);
            this.getFontRenderer().drawString(var19, var24 + var6 / 2 - this.getFontRenderer().getStringWidth(var19) / 2, var17 - this.getFontRenderer().FONT_HEIGHT, 553648127);
         }
      }

   }

   protected void renderPlayerStats(ScaledResolution var1) {
      if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
         EntityPlayer var2 = (EntityPlayer)this.mc.getRenderViewEntity();
         int var3 = MathHelper.ceil(var2.getHealth());
         boolean var4 = this.healthUpdateCounter > (long)this.updateCounter && (this.healthUpdateCounter - (long)this.updateCounter) / 3L % 2L == 1L;
         if (var3 < this.playerHealth && var2.hurtResistantTime > 0) {
            this.lastSystemTime = Minecraft.getSystemTime();
            this.healthUpdateCounter = (long)(this.updateCounter + 20);
         } else if (var3 > this.playerHealth && var2.hurtResistantTime > 0) {
            this.lastSystemTime = Minecraft.getSystemTime();
            this.healthUpdateCounter = (long)(this.updateCounter + 10);
         }

         if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L) {
            this.playerHealth = var3;
            this.lastPlayerHealth = var3;
            this.lastSystemTime = Minecraft.getSystemTime();
         }

         this.playerHealth = var3;
         int var5 = this.lastPlayerHealth;
         this.rand.setSeed((long)(this.updateCounter * 312871));
         FoodStats var6 = var2.getFoodStats();
         int var7 = var6.getFoodLevel();
         IAttributeInstance var8 = var2.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
         int var9 = var1.getScaledWidth() / 2 - 91;
         int var10 = var1.getScaledWidth() / 2 + 91;
         int var11 = var1.getScaledHeight() - 39;
         float var12 = (float)var8.getAttributeValue();
         int var13 = MathHelper.ceil(var2.getAbsorptionAmount());
         int var14 = MathHelper.ceil((var12 + (float)var13) / 2.0F / 10.0F);
         int var15 = Math.max(10 - (var14 - 2), 3);
         int var16 = var11 - (var14 - 1) * var15 - 10;
         int var17 = var11 - 10;
         int var18 = var13;
         int var19 = var2.getTotalArmorValue();
         int var20 = -1;
         if (var2.isPotionActive(MobEffects.REGENERATION)) {
            var20 = this.updateCounter % MathHelper.ceil(var12 + 5.0F);
         }

         this.mc.mcProfiler.startSection("armor");

         for(int var21 = 0; var21 < 10; ++var21) {
            if (var19 > 0) {
               int var22 = var9 + var21 * 8;
               if (var21 * 2 + 1 < var19) {
                  this.drawTexturedModalRect(var22, var16, 34, 9, 9, 9);
               }

               if (var21 * 2 + 1 == var19) {
                  this.drawTexturedModalRect(var22, var16, 25, 9, 9, 9);
               }

               if (var21 * 2 + 1 > var19) {
                  this.drawTexturedModalRect(var22, var16, 16, 9, 9, 9);
               }
            }
         }

         this.mc.mcProfiler.endStartSection("health");

         for(int var28 = MathHelper.ceil((var12 + (float)var13) / 2.0F) - 1; var28 >= 0; --var28) {
            int var30 = 16;
            if (var2.isPotionActive(MobEffects.POISON)) {
               var30 += 36;
            } else if (var2.isPotionActive(MobEffects.WITHER)) {
               var30 += 72;
            }

            byte var23 = 0;
            if (var4) {
               var23 = 1;
            }

            int var24 = MathHelper.ceil((float)(var28 + 1) / 10.0F) - 1;
            int var25 = var9 + var28 % 10 * 8;
            int var26 = var11 - var24 * var15;
            if (var3 <= 4) {
               var26 += this.rand.nextInt(2);
            }

            if (var18 <= 0 && var28 == var20) {
               var26 -= 2;
            }

            byte var27 = 0;
            if (var2.world.getWorldInfo().isHardcoreModeEnabled()) {
               var27 = 5;
            }

            this.drawTexturedModalRect(var25, var26, 16 + var23 * 9, 9 * var27, 9, 9);
            if (var4) {
               if (var28 * 2 + 1 < var5) {
                  this.drawTexturedModalRect(var25, var26, var30 + 54, 9 * var27, 9, 9);
               }

               if (var28 * 2 + 1 == var5) {
                  this.drawTexturedModalRect(var25, var26, var30 + 63, 9 * var27, 9, 9);
               }
            }

            if (var18 > 0) {
               if (var18 == var13 && var13 % 2 == 1) {
                  this.drawTexturedModalRect(var25, var26, var30 + 153, 9 * var27, 9, 9);
                  --var18;
               } else {
                  this.drawTexturedModalRect(var25, var26, var30 + 144, 9 * var27, 9, 9);
                  var18 -= 2;
               }
            } else {
               if (var28 * 2 + 1 < var3) {
                  this.drawTexturedModalRect(var25, var26, var30 + 36, 9 * var27, 9, 9);
               }

               if (var28 * 2 + 1 == var3) {
                  this.drawTexturedModalRect(var25, var26, var30 + 45, 9 * var27, 9, 9);
               }
            }
         }

         Entity var29 = var2.getRidingEntity();
         if (var29 == null) {
            this.mc.mcProfiler.endStartSection("food");

            for(int var31 = 0; var31 < 10; ++var31) {
               int var33 = var11;
               int var35 = 16;
               byte var37 = 0;
               if (var2.isPotionActive(MobEffects.HUNGER)) {
                  var35 += 36;
                  var37 = 13;
               }

               if (var2.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (var7 * 3 + 1) == 0) {
                  var33 = var11 + (this.rand.nextInt(3) - 1);
               }

               int var39 = var10 - var31 * 8 - 9;
               this.drawTexturedModalRect(var39, var33, 16 + var37 * 9, 27, 9, 9);
               if (var31 * 2 + 1 < var7) {
                  this.drawTexturedModalRect(var39, var33, var35 + 36, 27, 9, 9);
               }

               if (var31 * 2 + 1 == var7) {
                  this.drawTexturedModalRect(var39, var33, var35 + 45, 27, 9, 9);
               }
            }
         }

         this.mc.mcProfiler.endStartSection("air");
         if (var2.isInsideOfMaterial(Material.WATER)) {
            int var32 = this.mc.player.getAir();
            int var34 = MathHelper.ceil((double)(var32 - 2) * 10.0D / 300.0D);
            int var36 = MathHelper.ceil((double)var32 * 10.0D / 300.0D) - var34;

            for(int var38 = 0; var38 < var34 + var36; ++var38) {
               if (var38 < var34) {
                  this.drawTexturedModalRect(var10 - var38 * 8 - 9, var17, 16, 18, 9, 9);
               } else {
                  this.drawTexturedModalRect(var10 - var38 * 8 - 9, var17, 25, 18, 9, 9);
               }
            }
         }

         this.mc.mcProfiler.endSection();
      }

   }

   protected void renderMountHealth(ScaledResolution var1) {
      if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
         EntityPlayer var2 = (EntityPlayer)this.mc.getRenderViewEntity();
         Entity var3 = var2.getRidingEntity();
         if (var3 instanceof EntityLivingBase) {
            this.mc.mcProfiler.endStartSection("mountHealth");
            EntityLivingBase var4 = (EntityLivingBase)var3;
            int var5 = (int)Math.ceil((double)var4.getHealth());
            float var6 = var4.getMaxHealth();
            int var7 = (int)(var6 + 0.5F) / 2;
            if (var7 > 30) {
               var7 = 30;
            }

            int var8 = var1.getScaledHeight() - 39;
            int var9 = var1.getScaledWidth() / 2 + 91;
            int var10 = var8;
            int var11 = 0;

            for(boolean var12 = false; var7 > 0; var11 += 20) {
               int var13 = Math.min(var7, 10);
               var7 -= var13;

               for(int var14 = 0; var14 < var13; ++var14) {
                  boolean var15 = true;
                  byte var16 = 0;
                  int var17 = var9 - var14 * 8 - 9;
                  this.drawTexturedModalRect(var17, var10, 52 + var16 * 9, 9, 9, 9);
                  if (var14 * 2 + 1 + var11 < var5) {
                     this.drawTexturedModalRect(var17, var10, 88, 9, 9, 9);
                  }

                  if (var14 * 2 + 1 + var11 == var5) {
                     this.drawTexturedModalRect(var17, var10, 97, 9, 9, 9);
                  }
               }

               var10 -= 10;
            }
         }
      }

   }

   protected void renderPumpkinOverlay(ScaledResolution var1) {
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableAlpha();
      this.mc.getTextureManager().bindTexture(PUMPKIN_BLUR_TEX_PATH);
      Tessellator var2 = Tessellator.getInstance();
      VertexBuffer var3 = var2.getBuffer();
      var3.begin(7, DefaultVertexFormats.POSITION_TEX);
      var3.pos(0.0D, (double)var1.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
      var3.pos((double)var1.getScaledWidth(), (double)var1.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
      var3.pos((double)var1.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
      var3.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
      var2.draw();
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   protected void renderVignette(float var1, ScaledResolution var2) {
      var1 = 1.0F - var1;
      var1 = MathHelper.clamp(var1, 0.0F, 1.0F);
      WorldBorder var3 = this.mc.world.getWorldBorder();
      float var4 = (float)var3.getClosestDistance(this.mc.player);
      double var5 = Math.min(var3.getResizeSpeed() * (double)var3.getWarningTime() * 1000.0D, Math.abs(var3.getTargetSize() - var3.getDiameter()));
      double var7 = Math.max((double)var3.getWarningDistance(), var5);
      if ((double)var4 < var7) {
         var4 = 1.0F - (float)((double)var4 / var7);
      } else {
         var4 = 0.0F;
      }

      this.prevVignetteBrightness = (float)((double)this.prevVignetteBrightness + (double)(var1 - this.prevVignetteBrightness) * 0.01D);
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      if (var4 > 0.0F) {
         GlStateManager.color(0.0F, var4, var4, 1.0F);
      } else {
         GlStateManager.color(this.prevVignetteBrightness, this.prevVignetteBrightness, this.prevVignetteBrightness, 1.0F);
      }

      this.mc.getTextureManager().bindTexture(VIGNETTE_TEX_PATH);
      Tessellator var9 = Tessellator.getInstance();
      VertexBuffer var10 = var9.getBuffer();
      var10.begin(7, DefaultVertexFormats.POSITION_TEX);
      var10.pos(0.0D, (double)var2.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
      var10.pos((double)var2.getScaledWidth(), (double)var2.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
      var10.pos((double)var2.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
      var10.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
      var9.draw();
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
   }

   protected void renderPortal(float var1, ScaledResolution var2) {
      if (var1 < 1.0F) {
         var1 = var1 * var1;
         var1 = var1 * var1;
         var1 = var1 * 0.8F + 0.2F;
      }

      GlStateManager.disableAlpha();
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.color(1.0F, 1.0F, 1.0F, var1);
      this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      TextureAtlasSprite var3 = this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.PORTAL.getDefaultState());
      float var4 = var3.getMinU();
      float var5 = var3.getMinV();
      float var6 = var3.getMaxU();
      float var7 = var3.getMaxV();
      Tessellator var8 = Tessellator.getInstance();
      VertexBuffer var9 = var8.getBuffer();
      var9.begin(7, DefaultVertexFormats.POSITION_TEX);
      var9.pos(0.0D, (double)var2.getScaledHeight(), -90.0D).tex((double)var4, (double)var7).endVertex();
      var9.pos((double)var2.getScaledWidth(), (double)var2.getScaledHeight(), -90.0D).tex((double)var6, (double)var7).endVertex();
      var9.pos((double)var2.getScaledWidth(), 0.0D, -90.0D).tex((double)var6, (double)var5).endVertex();
      var9.pos(0.0D, 0.0D, -90.0D).tex((double)var4, (double)var5).endVertex();
      var8.draw();
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   protected void renderHotbarItem(int var1, int var2, float var3, EntityPlayer var4, @Nullable ItemStack var5) {
      if (var5 != null) {
         float var6 = (float)var5.animationsToGo - var3;
         if (var6 > 0.0F) {
            GlStateManager.pushMatrix();
            float var7 = 1.0F + var6 / 5.0F;
            GlStateManager.translate((float)(var1 + 8), (float)(var2 + 12), 0.0F);
            GlStateManager.scale(1.0F / var7, (var7 + 1.0F) / 2.0F, 1.0F);
            GlStateManager.translate((float)(-(var1 + 8)), (float)(-(var2 + 12)), 0.0F);
         }

         this.itemRenderer.renderItemAndEffectIntoGUI(var4, var5, var1, var2);
         if (var6 > 0.0F) {
            GlStateManager.popMatrix();
         }

         this.itemRenderer.renderItemOverlays(this.mc.fontRendererObj, var5, var1, var2);
      }

   }

   public void updateTick() {
      if (this.overlayMessageTime > 0) {
         --this.overlayMessageTime;
      }

      if (this.titlesTimer > 0) {
         --this.titlesTimer;
         if (this.titlesTimer <= 0) {
            this.displayedTitle = "";
            this.displayedSubTitle = "";
         }
      }

      ++this.updateCounter;
      if (this.mc.player != null) {
         ItemStack var1 = this.mc.player.inventory.getCurrentItem();
         if (var1 == null) {
            this.remainingHighlightTicks = 0;
         } else if (this.highlightingItemStack != null && var1.getItem() == this.highlightingItemStack.getItem() && ItemStack.areItemStackTagsEqual(var1, this.highlightingItemStack) && (var1.isItemStackDamageable() || var1.getMetadata() == this.highlightingItemStack.getMetadata())) {
            if (this.remainingHighlightTicks > 0) {
               --this.remainingHighlightTicks;
            }
         } else {
            this.remainingHighlightTicks = 40;
         }

         this.highlightingItemStack = var1;
      }

   }

   public void setRecordPlayingMessage(String var1) {
      this.setOverlayMessage(I18n.format("record.nowPlaying", var1), true);
   }

   public void setOverlayMessage(String var1, boolean var2) {
      this.overlayMessage = var1;
      this.overlayMessageTime = 60;
      this.animateOverlayMessageColor = var2;
   }

   public void displayTitle(String var1, String var2, int var3, int var4, int var5) {
      if (var1 == null && var2 == null && var3 < 0 && var4 < 0 && var5 < 0) {
         this.displayedTitle = "";
         this.displayedSubTitle = "";
         this.titlesTimer = 0;
      } else if (var1 != null) {
         this.displayedTitle = var1;
         this.titlesTimer = this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut;
      } else if (var2 != null) {
         this.displayedSubTitle = var2;
      } else {
         if (var3 >= 0) {
            this.titleFadeIn = var3;
         }

         if (var4 >= 0) {
            this.titleDisplayTime = var4;
         }

         if (var5 >= 0) {
            this.titleFadeOut = var5;
         }

         if (this.titlesTimer > 0) {
            this.titlesTimer = this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut;
         }
      }

   }

   public void setOverlayMessage(ITextComponent var1, boolean var2) {
      this.setOverlayMessage(var1.getUnformattedText(), var2);
   }

   public GuiNewChat getChatGUI() {
      return this.persistantChatGUI;
   }

   public int getUpdateCounter() {
      return this.updateCounter;
   }

   public FontRenderer getFontRenderer() {
      return this.mc.fontRendererObj;
   }

   public GuiSpectator getSpectatorGui() {
      return this.spectatorGui;
   }

   public GuiPlayerTabOverlay getTabList() {
      return this.overlayPlayerList;
   }

   public void resetPlayersOverlayFooterHeader() {
      this.overlayPlayerList.resetFooterHeader();
      this.overlayBoss.clearBossInfos();
   }

   public GuiBossOverlay getBossOverlay() {
      return this.overlayBoss;
   }
}
