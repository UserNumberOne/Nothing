package net.minecraft.client.gui.inventory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBrewingStand extends GuiContainer {
   private static final ResourceLocation BREWING_STAND_GUI_TEXTURES = new ResourceLocation("textures/gui/container/brewing_stand.png");
   private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};
   private final InventoryPlayer playerInventory;
   private final IInventory tileBrewingStand;

   public GuiBrewingStand(InventoryPlayer var1, IInventory var2) {
      super(new ContainerBrewingStand(var1, var2));
      this.playerInventory = var1;
      this.tileBrewingStand = var2;
   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
      String var3 = this.tileBrewingStand.getDisplayName().getUnformattedText();
      this.fontRendererObj.drawString(var3, this.xSize / 2 - this.fontRendererObj.getStringWidth(var3) / 2, 6, 4210752);
      this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(BREWING_STAND_GUI_TEXTURES);
      int var4 = (this.width - this.xSize) / 2;
      int var5 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var4, var5, 0, 0, this.xSize, this.ySize);
      int var6 = this.tileBrewingStand.getField(1);
      int var7 = MathHelper.clamp((18 * var6 + 20 - 1) / 20, 0, 18);
      if (var7 > 0) {
         this.drawTexturedModalRect(var4 + 60, var5 + 44, 176, 29, var7, 4);
      }

      int var8 = this.tileBrewingStand.getField(0);
      if (var8 > 0) {
         int var9 = (int)(28.0F * (1.0F - (float)var8 / 400.0F));
         if (var9 > 0) {
            this.drawTexturedModalRect(var4 + 97, var5 + 16, 176, 0, 9, var9);
         }

         var9 = BUBBLELENGTHS[var8 / 2 % 7];
         if (var9 > 0) {
            this.drawTexturedModalRect(var4 + 63, var5 + 14 + 29 - var9, 185, 29 - var9, 12, var9);
         }
      }

   }
}
