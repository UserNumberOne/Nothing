package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.glu.Project;

@SideOnly(Side.CLIENT)
public class GuiEnchantment extends GuiContainer {
   private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");
   private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
   private static final ModelBook MODEL_BOOK = new ModelBook();
   private final InventoryPlayer playerInventory;
   private final Random random = new Random();
   private final ContainerEnchantment container;
   public int ticks;
   public float flip;
   public float oFlip;
   public float flipT;
   public float flipA;
   public float open;
   public float oOpen;
   ItemStack last;
   private final IWorldNameable nameable;

   public GuiEnchantment(InventoryPlayer var1, World var2, IWorldNameable var3) {
      super(new ContainerEnchantment(var1, var2));
      this.playerInventory = var1;
      this.container = (ContainerEnchantment)this.inventorySlots;
      this.nameable = var3;
   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
      this.fontRendererObj.drawString(this.nameable.getDisplayName().getUnformattedText(), 12, 5, 4210752);
      this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
   }

   public void updateScreen() {
      super.updateScreen();
      this.tickBook();
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      int var4 = (this.width - this.xSize) / 2;
      int var5 = (this.height - this.ySize) / 2;

      for(int var6 = 0; var6 < 3; ++var6) {
         int var7 = var1 - (var4 + 60);
         int var8 = var2 - (var5 + 14 + 19 * var6);
         if (var7 >= 0 && var8 >= 0 && var7 < 108 && var8 < 19 && this.container.enchantItem(this.mc.player, var6)) {
            this.mc.playerController.sendEnchantPacket(this.container.windowId, var6);
         }
      }

   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
      int var4 = (this.width - this.xSize) / 2;
      int var5 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var4, var5, 0, 0, this.xSize, this.ySize);
      GlStateManager.pushMatrix();
      GlStateManager.matrixMode(5889);
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      ScaledResolution var6 = new ScaledResolution(this.mc);
      GlStateManager.viewport((var6.getScaledWidth() - 320) / 2 * var6.getScaleFactor(), (var6.getScaledHeight() - 240) / 2 * var6.getScaleFactor(), 320 * var6.getScaleFactor(), 240 * var6.getScaleFactor());
      GlStateManager.translate(-0.34F, 0.23F, 0.0F);
      Project.gluPerspective(90.0F, 1.3333334F, 9.0F, 80.0F);
      float var7 = 1.0F;
      GlStateManager.matrixMode(5888);
      GlStateManager.loadIdentity();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.translate(0.0F, 3.3F, -16.0F);
      GlStateManager.scale(1.0F, 1.0F, 1.0F);
      float var8 = 5.0F;
      GlStateManager.scale(5.0F, 5.0F, 5.0F);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_BOOK_TEXTURE);
      GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
      float var9 = this.oOpen + (this.open - this.oOpen) * var1;
      GlStateManager.translate((1.0F - var9) * 0.2F, (1.0F - var9) * 0.1F, (1.0F - var9) * 0.25F);
      GlStateManager.rotate(-(1.0F - var9) * 90.0F - 90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      float var10 = this.oFlip + (this.flip - this.oFlip) * var1 + 0.25F;
      float var11 = this.oFlip + (this.flip - this.oFlip) * var1 + 0.75F;
      var10 = (var10 - (float)MathHelper.fastFloor((double)var10)) * 1.6F - 0.3F;
      var11 = (var11 - (float)MathHelper.fastFloor((double)var11)) * 1.6F - 0.3F;
      if (var10 < 0.0F) {
         var10 = 0.0F;
      }

      if (var11 < 0.0F) {
         var11 = 0.0F;
      }

      if (var10 > 1.0F) {
         var10 = 1.0F;
      }

      if (var11 > 1.0F) {
         var11 = 1.0F;
      }

      GlStateManager.enableRescaleNormal();
      MODEL_BOOK.render((Entity)null, 0.0F, var10, var11, var9, 0.0F, 0.0625F);
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.matrixMode(5889);
      GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
      GlStateManager.popMatrix();
      GlStateManager.matrixMode(5888);
      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      EnchantmentNameParts.getInstance().reseedRandomGenerator((long)this.container.xpSeed);
      int var12 = this.container.getLapisAmount();

      for(int var13 = 0; var13 < 3; ++var13) {
         int var14 = var4 + 60;
         int var15 = var14 + 20;
         this.zLevel = 0.0F;
         this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
         int var16 = this.container.enchantLevels[var13];
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         if (var16 == 0) {
            this.drawTexturedModalRect(var14, var5 + 14 + 19 * var13, 0, 185, 108, 19);
         } else {
            String var17 = "" + var16;
            int var18 = 86 - this.fontRendererObj.getStringWidth(var17);
            String var19 = EnchantmentNameParts.getInstance().generateNewRandomName(this.fontRendererObj, var18);
            FontRenderer var20 = this.mc.standardGalacticFontRenderer;
            int var21 = 6839882;
            if ((var12 < var13 + 1 || this.mc.player.experienceLevel < var16) && !this.mc.player.capabilities.isCreativeMode) {
               this.drawTexturedModalRect(var14, var5 + 14 + 19 * var13, 0, 185, 108, 19);
               this.drawTexturedModalRect(var14 + 1, var5 + 15 + 19 * var13, 16 * var13, 239, 16, 16);
               var20.drawSplitString(var19, var15, var5 + 16 + 19 * var13, var18, (var21 & 16711422) >> 1);
               var21 = 4226832;
            } else {
               int var22 = var2 - (var4 + 60);
               int var23 = var3 - (var5 + 14 + 19 * var13);
               if (var22 >= 0 && var23 >= 0 && var22 < 108 && var23 < 19) {
                  this.drawTexturedModalRect(var14, var5 + 14 + 19 * var13, 0, 204, 108, 19);
                  var21 = 16777088;
               } else {
                  this.drawTexturedModalRect(var14, var5 + 14 + 19 * var13, 0, 166, 108, 19);
               }

               this.drawTexturedModalRect(var14 + 1, var5 + 15 + 19 * var13, 16 * var13, 223, 16, 16);
               var20.drawSplitString(var19, var15, var5 + 16 + 19 * var13, var18, var21);
               var21 = 8453920;
            }

            var20 = this.mc.fontRendererObj;
            var20.drawStringWithShadow(var17, (float)(var15 + 86 - var20.getStringWidth(var17)), (float)(var5 + 16 + 19 * var13 + 7), var21);
         }
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      super.drawScreen(var1, var2, var3);
      boolean var4 = this.mc.player.capabilities.isCreativeMode;
      int var5 = this.container.getLapisAmount();

      for(int var6 = 0; var6 < 3; ++var6) {
         int var7 = this.container.enchantLevels[var6];
         Enchantment var8 = Enchantment.getEnchantmentByID(this.container.enchantClue[var6]);
         int var9 = this.container.worldClue[var6];
         int var10 = var6 + 1;
         if (this.isPointInRegion(60, 14 + 19 * var6, 108, 17, var1, var2) && var7 > 0 && var9 >= 0 && var8 != null) {
            ArrayList var11 = Lists.newArrayList();
            var11.add("" + TextFormatting.WHITE + TextFormatting.ITALIC + I18n.format("container.enchant.clue", var8.getTranslatedName(var9)));
            if (!var4) {
               var11.add("");
               if (this.mc.player.experienceLevel < var7) {
                  var11.add(TextFormatting.RED + "Level Requirement: " + this.container.enchantLevels[var6]);
               } else {
                  String var12;
                  if (var10 == 1) {
                     var12 = I18n.format("container.enchant.lapis.one");
                  } else {
                     var12 = I18n.format("container.enchant.lapis.many", var10);
                  }

                  TextFormatting var13 = var5 >= var10 ? TextFormatting.GRAY : TextFormatting.RED;
                  var11.add(var13 + "" + var12);
                  if (var10 == 1) {
                     var12 = I18n.format("container.enchant.level.one");
                  } else {
                     var12 = I18n.format("container.enchant.level.many", var10);
                  }

                  var11.add(TextFormatting.GRAY + "" + var12);
               }
            }

            this.drawHoveringText(var11, var1, var2);
            break;
         }
      }

   }

   public void tickBook() {
      ItemStack var1 = this.inventorySlots.getSlot(0).getStack();
      if (!ItemStack.areItemStacksEqual(var1, this.last)) {
         this.last = var1;

         while(true) {
            this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));
            if (this.flip > this.flipT + 1.0F || this.flip < this.flipT - 1.0F) {
               break;
            }
         }
      }

      ++this.ticks;
      this.oFlip = this.flip;
      this.oOpen = this.open;
      boolean var2 = false;

      for(int var3 = 0; var3 < 3; ++var3) {
         if (this.container.enchantLevels[var3] != 0) {
            var2 = true;
         }
      }

      if (var2) {
         this.open += 0.2F;
      } else {
         this.open -= 0.2F;
      }

      this.open = MathHelper.clamp(this.open, 0.0F, 1.0F);
      float var5 = (this.flipT - this.flip) * 0.4F;
      float var4 = 0.2F;
      var5 = MathHelper.clamp(var5, -0.2F, 0.2F);
      this.flipA += (var5 - this.flipA) * 0.9F;
      this.flip += this.flipA;
   }
}
