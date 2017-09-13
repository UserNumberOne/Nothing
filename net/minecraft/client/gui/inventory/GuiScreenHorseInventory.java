package net.minecraft.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScreenHorseInventory extends GuiContainer {
   private static final ResourceLocation HORSE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/horse.png");
   private final IInventory playerInventory;
   private final IInventory horseInventory;
   private final EntityHorse horseEntity;
   private float mousePosx;
   private float mousePosY;

   public GuiScreenHorseInventory(IInventory var1, IInventory var2, EntityHorse var3) {
      super(new ContainerHorseInventory(var1, var2, var3, Minecraft.getMinecraft().player));
      this.playerInventory = var1;
      this.horseInventory = var2;
      this.horseEntity = var3;
      this.allowUserInput = false;
   }

   protected void drawGuiContainerForegroundLayer(int var1, int var2) {
      this.fontRendererObj.drawString(this.horseInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
      this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(HORSE_GUI_TEXTURES);
      int var4 = (this.width - this.xSize) / 2;
      int var5 = (this.height - this.ySize) / 2;
      this.drawTexturedModalRect(var4, var5, 0, 0, this.xSize, this.ySize);
      if (this.horseEntity.isChested()) {
         this.drawTexturedModalRect(var4 + 79, var5 + 17, 0, this.ySize, 90, 54);
      }

      if (this.horseEntity.getType().isHorse()) {
         this.drawTexturedModalRect(var4 + 7, var5 + 35, 0, this.ySize + 54, 18, 18);
      }

      GuiInventory.drawEntityOnScreen(var4 + 51, var5 + 60, 17, (float)(var4 + 51) - this.mousePosx, (float)(var5 + 75 - 50) - this.mousePosY, this.horseEntity);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.mousePosx = (float)var1;
      this.mousePosY = (float)var2;
      super.drawScreen(var1, var2, var3);
   }
}
