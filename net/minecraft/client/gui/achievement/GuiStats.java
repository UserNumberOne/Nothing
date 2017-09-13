package net.minecraft.client.gui.achievement;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class GuiStats extends GuiScreen implements IProgressMeter {
   protected GuiScreen parentScreen;
   protected String screenTitle = "Select world";
   private GuiStats.StatsGeneral generalStats;
   private GuiStats.StatsItem itemStats;
   private GuiStats.StatsBlock blockStats;
   private GuiStats.StatsMobsList mobStats;
   private final StatisticsManager stats;
   private GuiSlot displaySlot;
   private boolean doesGuiPauseGame = true;

   public GuiStats(GuiScreen var1, StatisticsManager var2) {
      this.parentScreen = var1;
      this.stats = var2;
   }

   public void initGui() {
      this.screenTitle = I18n.format("gui.stats");
      this.doesGuiPauseGame = true;
      this.mc.getConnection().sendPacket(new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      if (this.displaySlot != null) {
         this.displaySlot.handleMouseInput();
      }

   }

   public void initLists() {
      this.generalStats = new GuiStats.StatsGeneral(this.mc);
      this.generalStats.registerScrollButtons(1, 1);
      this.itemStats = new GuiStats.StatsItem(this.mc);
      this.itemStats.registerScrollButtons(1, 1);
      this.blockStats = new GuiStats.StatsBlock(this.mc);
      this.blockStats.registerScrollButtons(1, 1);
      this.mobStats = new GuiStats.StatsMobsList(this.mc);
      this.mobStats.registerScrollButtons(1, 1);
   }

   public void createButtons() {
      this.buttonList.add(new GuiButton(0, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.done")));
      this.buttonList.add(new GuiButton(1, this.width / 2 - 160, this.height - 52, 80, 20, I18n.format("stat.generalButton")));
      GuiButton var1 = this.addButton(new GuiButton(2, this.width / 2 - 80, this.height - 52, 80, 20, I18n.format("stat.blocksButton")));
      GuiButton var2 = this.addButton(new GuiButton(3, this.width / 2, this.height - 52, 80, 20, I18n.format("stat.itemsButton")));
      GuiButton var3 = this.addButton(new GuiButton(4, this.width / 2 + 80, this.height - 52, 80, 20, I18n.format("stat.mobsButton")));
      if (this.blockStats.getSize() == 0) {
         var1.enabled = false;
      }

      if (this.itemStats.getSize() == 0) {
         var2.enabled = false;
      }

      if (this.mobStats.getSize() == 0) {
         var3.enabled = false;
      }

   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 0) {
            this.mc.displayGuiScreen(this.parentScreen);
         } else if (var1.id == 1) {
            this.displaySlot = this.generalStats;
         } else if (var1.id == 3) {
            this.displaySlot = this.itemStats;
         } else if (var1.id == 2) {
            this.displaySlot = this.blockStats;
         } else if (var1.id == 4) {
            this.displaySlot = this.mobStats;
         } else {
            this.displaySlot.actionPerformed(var1);
         }
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      if (this.doesGuiPauseGame) {
         this.drawDefaultBackground();
         this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingStats"), this.width / 2, this.height / 2, 16777215);
         this.drawCenteredString(this.fontRendererObj, LOADING_STRINGS[(int)(Minecraft.getSystemTime() / 150L % (long)LOADING_STRINGS.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2, 16777215);
      } else {
         this.displaySlot.drawScreen(var1, var2, var3);
         this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 20, 16777215);
         super.drawScreen(var1, var2, var3);
      }

   }

   public void doneLoading() {
      if (this.doesGuiPauseGame) {
         this.initLists();
         this.createButtons();
         this.displaySlot = this.generalStats;
         this.doesGuiPauseGame = false;
      }

   }

   public boolean doesGuiPauseGame() {
      return !this.doesGuiPauseGame;
   }

   private void drawStatsScreen(int var1, int var2, Item var3) {
      this.drawButtonBackground(var1 + 1, var2 + 1);
      GlStateManager.enableRescaleNormal();
      RenderHelper.enableGUIStandardItemLighting();
      this.itemRender.renderItemIntoGUI(new ItemStack(var3), var1 + 2, var2 + 2);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
   }

   private void drawButtonBackground(int var1, int var2) {
      this.drawSprite(var1, var2, 0, 0);
   }

   private void drawSprite(int var1, int var2, int var3, int var4) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(STAT_ICONS);
      float var5 = 0.0078125F;
      float var6 = 0.0078125F;
      boolean var7 = true;
      boolean var8 = true;
      Tessellator var9 = Tessellator.getInstance();
      VertexBuffer var10 = var9.getBuffer();
      var10.begin(7, DefaultVertexFormats.POSITION_TEX);
      var10.pos((double)(var1 + 0), (double)(var2 + 18), (double)this.zLevel).tex((double)((float)(var3 + 0) * 0.0078125F), (double)((float)(var4 + 18) * 0.0078125F)).endVertex();
      var10.pos((double)(var1 + 18), (double)(var2 + 18), (double)this.zLevel).tex((double)((float)(var3 + 18) * 0.0078125F), (double)((float)(var4 + 18) * 0.0078125F)).endVertex();
      var10.pos((double)(var1 + 18), (double)(var2 + 0), (double)this.zLevel).tex((double)((float)(var3 + 18) * 0.0078125F), (double)((float)(var4 + 0) * 0.0078125F)).endVertex();
      var10.pos((double)(var1 + 0), (double)(var2 + 0), (double)this.zLevel).tex((double)((float)(var3 + 0) * 0.0078125F), (double)((float)(var4 + 0) * 0.0078125F)).endVertex();
      var9.draw();
   }

   @SideOnly(Side.CLIENT)
   abstract class Stats extends GuiSlot {
      protected int headerPressed = -1;
      protected List statsHolder;
      protected Comparator statSorter;
      protected int sortColumn = -1;
      protected int sortOrder;

      protected Stats(Minecraft var2) {
         super(var2, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 20);
         this.setShowSelectionBox(false);
         this.setHasListHeader(true, 20);
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      }

      protected boolean isSelected(int var1) {
         return false;
      }

      public int getListWidth() {
         return 375;
      }

      protected int getScrollBarX() {
         return this.width / 2 + 140;
      }

      protected void drawBackground() {
         GuiStats.this.drawDefaultBackground();
      }

      protected void drawListHeader(int var1, int var2, Tessellator var3) {
         if (!Mouse.isButtonDown(0)) {
            this.headerPressed = -1;
         }

         if (this.headerPressed == 0) {
            GuiStats.this.drawSprite(var1 + 115 - 18, var2 + 1, 0, 0);
         } else {
            GuiStats.this.drawSprite(var1 + 115 - 18, var2 + 1, 0, 18);
         }

         if (this.headerPressed == 1) {
            GuiStats.this.drawSprite(var1 + 165 - 18, var2 + 1, 0, 0);
         } else {
            GuiStats.this.drawSprite(var1 + 165 - 18, var2 + 1, 0, 18);
         }

         if (this.headerPressed == 2) {
            GuiStats.this.drawSprite(var1 + 215 - 18, var2 + 1, 0, 0);
         } else {
            GuiStats.this.drawSprite(var1 + 215 - 18, var2 + 1, 0, 18);
         }

         if (this.headerPressed == 3) {
            GuiStats.this.drawSprite(var1 + 265 - 18, var2 + 1, 0, 0);
         } else {
            GuiStats.this.drawSprite(var1 + 265 - 18, var2 + 1, 0, 18);
         }

         if (this.headerPressed == 4) {
            GuiStats.this.drawSprite(var1 + 315 - 18, var2 + 1, 0, 0);
         } else {
            GuiStats.this.drawSprite(var1 + 315 - 18, var2 + 1, 0, 18);
         }

         if (this.sortColumn != -1) {
            short var4 = 79;
            byte var5 = 18;
            if (this.sortColumn == 1) {
               var4 = 129;
            } else if (this.sortColumn == 2) {
               var4 = 179;
            } else if (this.sortColumn == 3) {
               var4 = 229;
            } else if (this.sortColumn == 4) {
               var4 = 279;
            }

            if (this.sortOrder == 1) {
               var5 = 36;
            }

            GuiStats.this.drawSprite(var1 + var4, var2 + 1, var5, 0);
         }

      }

      protected void clickedHeader(int var1, int var2) {
         this.headerPressed = -1;
         if (var1 >= 79 && var1 < 115) {
            this.headerPressed = 0;
         } else if (var1 >= 129 && var1 < 165) {
            this.headerPressed = 1;
         } else if (var1 >= 179 && var1 < 215) {
            this.headerPressed = 2;
         } else if (var1 >= 229 && var1 < 265) {
            this.headerPressed = 3;
         } else if (var1 >= 279 && var1 < 315) {
            this.headerPressed = 4;
         }

         if (this.headerPressed >= 0) {
            this.sortByColumn(this.headerPressed);
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

      }

      protected final int getSize() {
         return this.statsHolder.size();
      }

      protected final StatCrafting getSlotStat(int var1) {
         return (StatCrafting)this.statsHolder.get(var1);
      }

      protected abstract String getHeaderDescriptionId(int var1);

      protected void renderStat(StatBase var1, int var2, int var3, boolean var4) {
         if (var1 != null) {
            String var5 = var1.format(GuiStats.this.stats.readStat(var1));
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, var5, var2 - GuiStats.this.fontRendererObj.getStringWidth(var5), var3 + 5, var4 ? 16777215 : 9474192);
         } else {
            String var6 = "-";
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, "-", var2 - GuiStats.this.fontRendererObj.getStringWidth("-"), var3 + 5, var4 ? 16777215 : 9474192);
         }

      }

      protected void renderDecorations(int var1, int var2) {
         if (var2 >= this.top && var2 <= this.bottom) {
            int var3 = this.getSlotIndexFromScreenCoords(var1, var2);
            int var4 = (this.width - this.getListWidth()) / 2;
            if (var3 >= 0) {
               if (var1 < var4 + 40 || var1 > var4 + 40 + 20) {
                  return;
               }

               StatCrafting var10 = this.getSlotStat(var3);
               this.renderMouseHoverToolTip(var10, var1, var2);
            } else {
               String var5;
               if (var1 >= var4 + 115 - 18 && var1 <= var4 + 115) {
                  var5 = this.getHeaderDescriptionId(0);
               } else if (var1 >= var4 + 165 - 18 && var1 <= var4 + 165) {
                  var5 = this.getHeaderDescriptionId(1);
               } else if (var1 >= var4 + 215 - 18 && var1 <= var4 + 215) {
                  var5 = this.getHeaderDescriptionId(2);
               } else if (var1 >= var4 + 265 - 18 && var1 <= var4 + 265) {
                  var5 = this.getHeaderDescriptionId(3);
               } else {
                  if (var1 < var4 + 315 - 18 || var1 > var4 + 315) {
                     return;
                  }

                  var5 = this.getHeaderDescriptionId(4);
               }

               var5 = ("" + I18n.format(var5)).trim();
               if (!var5.isEmpty()) {
                  int var6 = var1 + 12;
                  int var7 = var2 - 12;
                  int var8 = GuiStats.this.fontRendererObj.getStringWidth(var5);
                  GuiStats.this.drawGradientRect(var6 - 3, var7 - 3, var6 + var8 + 3, var7 + 8 + 3, -1073741824, -1073741824);
                  GuiStats.this.fontRendererObj.drawStringWithShadow(var5, (float)var6, (float)var7, -1);
               }
            }
         }

      }

      protected void renderMouseHoverToolTip(StatCrafting var1, int var2, int var3) {
         if (var1 != null) {
            Item var4 = var1.getItem();
            ItemStack var5 = new ItemStack(var4);
            String var6 = var5.getUnlocalizedName();
            String var7 = ("" + I18n.format(var6 + ".name")).trim();
            if (!var7.isEmpty()) {
               int var8 = var2 + 12;
               int var9 = var3 - 12;
               int var10 = GuiStats.this.fontRendererObj.getStringWidth(var7);
               GuiStats.this.drawGradientRect(var8 - 3, var9 - 3, var8 + var10 + 3, var9 + 8 + 3, -1073741824, -1073741824);
               GuiStats.this.fontRendererObj.drawStringWithShadow(var7, (float)var8, (float)var9, -1);
            }
         }

      }

      protected void sortByColumn(int var1) {
         if (var1 != this.sortColumn) {
            this.sortColumn = var1;
            this.sortOrder = -1;
         } else if (this.sortOrder == -1) {
            this.sortOrder = 1;
         } else {
            this.sortColumn = -1;
            this.sortOrder = 0;
         }

         Collections.sort(this.statsHolder, this.statSorter);
      }
   }

   @SideOnly(Side.CLIENT)
   class StatsBlock extends GuiStats.Stats {
      public StatsBlock(Minecraft var2) {
         super(var2);
         this.statsHolder = Lists.newArrayList();

         for(StatCrafting var4 : StatList.MINE_BLOCK_STATS) {
            boolean var5 = false;
            Item var6 = var4.getItem();
            if (GuiStats.this.stats.readStat(var4) > 0) {
               var5 = true;
            } else if (StatList.getObjectUseStats(var6) != null && GuiStats.this.stats.readStat(StatList.getObjectUseStats(var6)) > 0) {
               var5 = true;
            } else if (StatList.getCraftStats(var6) != null && GuiStats.this.stats.readStat(StatList.getCraftStats(var6)) > 0) {
               var5 = true;
            } else if (StatList.getObjectsPickedUpStats(var6) != null && GuiStats.this.stats.readStat(StatList.getObjectsPickedUpStats(var6)) > 0) {
               var5 = true;
            } else if (StatList.getDroppedObjectStats(var6) != null && GuiStats.this.stats.readStat(StatList.getDroppedObjectStats(var6)) > 0) {
               var5 = true;
            }

            if (var5) {
               this.statsHolder.add(var4);
            }
         }

         this.statSorter = new Comparator() {
            public int compare(StatCrafting var1, StatCrafting var2) {
               Item var3 = var1.getItem();
               Item var4 = var2.getItem();
               StatBase var5 = null;
               StatBase var6 = null;
               if (StatsBlock.this.sortColumn == 2) {
                  var5 = StatList.getBlockStats(Block.getBlockFromItem(var3));
                  var6 = StatList.getBlockStats(Block.getBlockFromItem(var4));
               } else if (StatsBlock.this.sortColumn == 0) {
                  var5 = StatList.getCraftStats(var3);
                  var6 = StatList.getCraftStats(var4);
               } else if (StatsBlock.this.sortColumn == 1) {
                  var5 = StatList.getObjectUseStats(var3);
                  var6 = StatList.getObjectUseStats(var4);
               } else if (StatsBlock.this.sortColumn == 3) {
                  var5 = StatList.getObjectsPickedUpStats(var3);
                  var6 = StatList.getObjectsPickedUpStats(var4);
               } else if (StatsBlock.this.sortColumn == 4) {
                  var5 = StatList.getDroppedObjectStats(var3);
                  var6 = StatList.getDroppedObjectStats(var4);
               }

               if (var5 != null || var6 != null) {
                  if (var5 == null) {
                     return 1;
                  }

                  if (var6 == null) {
                     return -1;
                  }

                  int var7 = GuiStats.this.stats.readStat(var5);
                  int var8 = GuiStats.this.stats.readStat(var6);
                  if (var7 != var8) {
                     return (var7 - var8) * StatsBlock.this.sortOrder;
                  }
               }

               return Item.getIdFromItem(var3) - Item.getIdFromItem(var4);
            }
         };
      }

      protected void drawListHeader(int var1, int var2, Tessellator var3) {
         super.drawListHeader(var1, var2, var3);
         if (this.headerPressed == 0) {
            GuiStats.this.drawSprite(var1 + 115 - 18 + 1, var2 + 1 + 1, 18, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 115 - 18, var2 + 1, 18, 18);
         }

         if (this.headerPressed == 1) {
            GuiStats.this.drawSprite(var1 + 165 - 18 + 1, var2 + 1 + 1, 36, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 165 - 18, var2 + 1, 36, 18);
         }

         if (this.headerPressed == 2) {
            GuiStats.this.drawSprite(var1 + 215 - 18 + 1, var2 + 1 + 1, 54, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 215 - 18, var2 + 1, 54, 18);
         }

         if (this.headerPressed == 3) {
            GuiStats.this.drawSprite(var1 + 265 - 18 + 1, var2 + 1 + 1, 90, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 265 - 18, var2 + 1, 90, 18);
         }

         if (this.headerPressed == 4) {
            GuiStats.this.drawSprite(var1 + 315 - 18 + 1, var2 + 1 + 1, 108, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 315 - 18, var2 + 1, 108, 18);
         }

      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         StatCrafting var7 = this.getSlotStat(var1);
         Item var8 = var7.getItem();
         GuiStats.this.drawStatsScreen(var2 + 40, var3, var8);
         this.renderStat(StatList.getCraftStats(var8), var2 + 115, var3, var1 % 2 == 0);
         this.renderStat(StatList.getObjectUseStats(var8), var2 + 165, var3, var1 % 2 == 0);
         this.renderStat(var7, var2 + 215, var3, var1 % 2 == 0);
         this.renderStat(StatList.getObjectsPickedUpStats(var8), var2 + 265, var3, var1 % 2 == 0);
         this.renderStat(StatList.getDroppedObjectStats(var8), var2 + 315, var3, var1 % 2 == 0);
      }

      protected String getHeaderDescriptionId(int var1) {
         return var1 == 0 ? "stat.crafted" : (var1 == 1 ? "stat.used" : (var1 == 3 ? "stat.pickup" : (var1 == 4 ? "stat.dropped" : "stat.mined")));
      }
   }

   @SideOnly(Side.CLIENT)
   class StatsGeneral extends GuiSlot {
      public StatsGeneral(Minecraft var2) {
         super(var2, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 10);
         this.setShowSelectionBox(false);
      }

      protected int getSize() {
         return StatList.BASIC_STATS.size();
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      }

      protected boolean isSelected(int var1) {
         return false;
      }

      protected int getContentHeight() {
         return this.getSize() * 10;
      }

      protected void drawBackground() {
         GuiStats.this.drawDefaultBackground();
      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         StatBase var7 = (StatBase)StatList.BASIC_STATS.get(var1);
         GuiStats.this.drawString(GuiStats.this.fontRendererObj, var7.getStatName().getUnformattedText(), var2 + 2, var3 + 1, var1 % 2 == 0 ? 16777215 : 9474192);
         String var8 = var7.format(GuiStats.this.stats.readStat(var7));
         GuiStats.this.drawString(GuiStats.this.fontRendererObj, var8, var2 + 2 + 213 - GuiStats.this.fontRendererObj.getStringWidth(var8), var3 + 1, var1 % 2 == 0 ? 16777215 : 9474192);
      }
   }

   @SideOnly(Side.CLIENT)
   class StatsItem extends GuiStats.Stats {
      public StatsItem(Minecraft var2) {
         super(var2);
         this.statsHolder = Lists.newArrayList();

         for(StatCrafting var4 : StatList.USE_ITEM_STATS) {
            boolean var5 = false;
            Item var6 = var4.getItem();
            if (GuiStats.this.stats.readStat(var4) > 0) {
               var5 = true;
            } else if (StatList.getObjectBreakStats(var6) != null && GuiStats.this.stats.readStat(StatList.getObjectBreakStats(var6)) > 0) {
               var5 = true;
            } else if (StatList.getCraftStats(var6) != null && GuiStats.this.stats.readStat(StatList.getCraftStats(var6)) > 0) {
               var5 = true;
            } else if (StatList.getObjectsPickedUpStats(var6) != null && GuiStats.this.stats.readStat(StatList.getObjectsPickedUpStats(var6)) > 0) {
               var5 = true;
            } else if (StatList.getDroppedObjectStats(var6) != null && GuiStats.this.stats.readStat(StatList.getDroppedObjectStats(var6)) > 0) {
               var5 = true;
            }

            if (var5) {
               this.statsHolder.add(var4);
            }
         }

         this.statSorter = new Comparator() {
            public int compare(StatCrafting var1, StatCrafting var2) {
               Item var3 = var1.getItem();
               Item var4 = var2.getItem();
               int var5 = Item.getIdFromItem(var3);
               int var6 = Item.getIdFromItem(var4);
               StatBase var7 = null;
               StatBase var8 = null;
               if (StatsItem.this.sortColumn == 0) {
                  var7 = StatList.getObjectBreakStats(var3);
                  var8 = StatList.getObjectBreakStats(var4);
               } else if (StatsItem.this.sortColumn == 1) {
                  var7 = StatList.getCraftStats(var3);
                  var8 = StatList.getCraftStats(var4);
               } else if (StatsItem.this.sortColumn == 2) {
                  var7 = StatList.getObjectUseStats(var3);
                  var8 = StatList.getObjectUseStats(var4);
               } else if (StatsItem.this.sortColumn == 3) {
                  var7 = StatList.getObjectsPickedUpStats(var3);
                  var8 = StatList.getObjectsPickedUpStats(var4);
               } else if (StatsItem.this.sortColumn == 4) {
                  var7 = StatList.getDroppedObjectStats(var3);
                  var8 = StatList.getDroppedObjectStats(var4);
               }

               if (var7 != null || var8 != null) {
                  if (var7 == null) {
                     return 1;
                  }

                  if (var8 == null) {
                     return -1;
                  }

                  int var9 = GuiStats.this.stats.readStat(var7);
                  int var10 = GuiStats.this.stats.readStat(var8);
                  if (var9 != var10) {
                     return (var9 - var10) * StatsItem.this.sortOrder;
                  }
               }

               return var5 - var6;
            }
         };
      }

      protected void drawListHeader(int var1, int var2, Tessellator var3) {
         super.drawListHeader(var1, var2, var3);
         if (this.headerPressed == 0) {
            GuiStats.this.drawSprite(var1 + 115 - 18 + 1, var2 + 1 + 1, 72, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 115 - 18, var2 + 1, 72, 18);
         }

         if (this.headerPressed == 1) {
            GuiStats.this.drawSprite(var1 + 165 - 18 + 1, var2 + 1 + 1, 18, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 165 - 18, var2 + 1, 18, 18);
         }

         if (this.headerPressed == 2) {
            GuiStats.this.drawSprite(var1 + 215 - 18 + 1, var2 + 1 + 1, 36, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 215 - 18, var2 + 1, 36, 18);
         }

         if (this.headerPressed == 3) {
            GuiStats.this.drawSprite(var1 + 265 - 18 + 1, var2 + 1 + 1, 90, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 265 - 18, var2 + 1, 90, 18);
         }

         if (this.headerPressed == 4) {
            GuiStats.this.drawSprite(var1 + 315 - 18 + 1, var2 + 1 + 1, 108, 18);
         } else {
            GuiStats.this.drawSprite(var1 + 315 - 18, var2 + 1, 108, 18);
         }

      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         StatCrafting var7 = this.getSlotStat(var1);
         Item var8 = var7.getItem();
         GuiStats.this.drawStatsScreen(var2 + 40, var3, var8);
         this.renderStat(StatList.getObjectBreakStats(var8), var2 + 115, var3, var1 % 2 == 0);
         this.renderStat(StatList.getCraftStats(var8), var2 + 165, var3, var1 % 2 == 0);
         this.renderStat(var7, var2 + 215, var3, var1 % 2 == 0);
         this.renderStat(StatList.getObjectsPickedUpStats(var8), var2 + 265, var3, var1 % 2 == 0);
         this.renderStat(StatList.getDroppedObjectStats(var8), var2 + 315, var3, var1 % 2 == 0);
      }

      protected String getHeaderDescriptionId(int var1) {
         return var1 == 1 ? "stat.crafted" : (var1 == 2 ? "stat.used" : (var1 == 3 ? "stat.pickup" : (var1 == 4 ? "stat.dropped" : "stat.depleted")));
      }
   }

   @SideOnly(Side.CLIENT)
   class StatsMobsList extends GuiSlot {
      private final List mobs = Lists.newArrayList();

      public StatsMobsList(Minecraft var2) {
         super(var2, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, GuiStats.this.fontRendererObj.FONT_HEIGHT * 4);
         this.setShowSelectionBox(false);

         for(EntityList.EntityEggInfo var4 : EntityList.ENTITY_EGGS.values()) {
            if (GuiStats.this.stats.readStat(var4.killEntityStat) > 0 || GuiStats.this.stats.readStat(var4.entityKilledByStat) > 0) {
               this.mobs.add(var4);
            }
         }

      }

      protected int getSize() {
         return this.mobs.size();
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
      }

      protected boolean isSelected(int var1) {
         return false;
      }

      protected int getContentHeight() {
         return this.getSize() * GuiStats.this.fontRendererObj.FONT_HEIGHT * 4;
      }

      protected void drawBackground() {
         GuiStats.this.drawDefaultBackground();
      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         EntityList.EntityEggInfo var7 = (EntityList.EntityEggInfo)this.mobs.get(var1);
         String var8 = I18n.format("entity." + var7.spawnedID + ".name");
         int var9 = GuiStats.this.stats.readStat(var7.killEntityStat);
         int var10 = GuiStats.this.stats.readStat(var7.entityKilledByStat);
         String var11 = I18n.format("stat.entityKills", var9, var8);
         String var12 = I18n.format("stat.entityKilledBy", var8, var10);
         if (var9 == 0) {
            var11 = I18n.format("stat.entityKills.none", var8);
         }

         if (var10 == 0) {
            var12 = I18n.format("stat.entityKilledBy.none", var8);
         }

         GuiStats.this.drawString(GuiStats.this.fontRendererObj, var8, var2 + 2 - 10, var3 + 1, 16777215);
         GuiStats.this.drawString(GuiStats.this.fontRendererObj, var11, var2 + 2, var3 + 1 + GuiStats.this.fontRendererObj.FONT_HEIGHT, var9 == 0 ? 6316128 : 9474192);
         GuiStats.this.drawString(GuiStats.this.fontRendererObj, var12, var2 + 2, var3 + 1 + GuiStats.this.fontRendererObj.FONT_HEIGHT * 2, var10 == 0 ? 6316128 : 9474192);
      }
   }
}
