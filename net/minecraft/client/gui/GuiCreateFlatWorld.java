package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCreateFlatWorld extends GuiScreen {
   private final GuiCreateWorld createWorldGui;
   private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();
   private String flatWorldTitle;
   private String materialText;
   private String heightText;
   private GuiCreateFlatWorld.Details createFlatWorldListSlotGui;
   private GuiButton addLayerButton;
   private GuiButton editLayerButton;
   private GuiButton removeLayerButton;

   public GuiCreateFlatWorld(GuiCreateWorld var1, String var2) {
      this.createWorldGui = var1;
      this.setPreset(var2);
   }

   public String getPreset() {
      return this.theFlatGeneratorInfo.toString();
   }

   public void setPreset(String var1) {
      this.theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(var1);
   }

   public void initGui() {
      this.buttonList.clear();
      this.flatWorldTitle = I18n.format("createWorld.customize.flat.title");
      this.materialText = I18n.format("createWorld.customize.flat.tile");
      this.heightText = I18n.format("createWorld.customize.flat.height");
      this.createFlatWorldListSlotGui = new GuiCreateFlatWorld.Details();
      this.addLayerButton = this.addButton(new GuiButton(2, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer") + " (NYI)"));
      this.editLayerButton = this.addButton(new GuiButton(3, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer") + " (NYI)"));
      this.removeLayerButton = this.addButton(new GuiButton(4, this.width / 2 - 155, this.height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer")));
      this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
      this.buttonList.add(new GuiButton(5, this.width / 2 + 5, this.height - 52, 150, 20, I18n.format("createWorld.customize.presets")));
      this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
      this.addLayerButton.visible = false;
      this.editLayerButton.visible = false;
      this.theFlatGeneratorInfo.updateLayers();
      this.onLayersChanged();
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.createFlatWorldListSlotGui.handleMouseInput();
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      int var2 = this.theFlatGeneratorInfo.getFlatLayers().size() - this.createFlatWorldListSlotGui.selectedLayer - 1;
      if (var1.id == 1) {
         this.mc.displayGuiScreen(this.createWorldGui);
      } else if (var1.id == 0) {
         this.createWorldGui.chunkProviderSettingsJson = this.getPreset();
         this.mc.displayGuiScreen(this.createWorldGui);
      } else if (var1.id == 5) {
         this.mc.displayGuiScreen(new GuiFlatPresets(this));
      } else if (var1.id == 4 && this.hasSelectedLayer()) {
         this.theFlatGeneratorInfo.getFlatLayers().remove(var2);
         this.createFlatWorldListSlotGui.selectedLayer = Math.min(this.createFlatWorldListSlotGui.selectedLayer, this.theFlatGeneratorInfo.getFlatLayers().size() - 1);
      }

      this.theFlatGeneratorInfo.updateLayers();
      this.onLayersChanged();
   }

   public void onLayersChanged() {
      boolean var1 = this.hasSelectedLayer();
      this.removeLayerButton.enabled = var1;
      this.editLayerButton.enabled = var1;
      this.editLayerButton.enabled = false;
      this.addLayerButton.enabled = false;
   }

   private boolean hasSelectedLayer() {
      return this.createFlatWorldListSlotGui.selectedLayer > -1 && this.createFlatWorldListSlotGui.selectedLayer < this.theFlatGeneratorInfo.getFlatLayers().size();
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.createFlatWorldListSlotGui.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, this.flatWorldTitle, this.width / 2, 8, 16777215);
      int var4 = this.width / 2 - 92 - 16;
      this.drawString(this.fontRendererObj, this.materialText, var4, 32, 16777215);
      this.drawString(this.fontRendererObj, this.heightText, var4 + 2 + 213 - this.fontRendererObj.getStringWidth(this.heightText), 32, 16777215);
      super.drawScreen(var1, var2, var3);
   }

   @SideOnly(Side.CLIENT)
   class Details extends GuiSlot {
      public int selectedLayer = -1;

      public Details() {
         super(GuiCreateFlatWorld.this.mc, GuiCreateFlatWorld.this.width, GuiCreateFlatWorld.this.height, 43, GuiCreateFlatWorld.this.height - 60, 24);
      }

      private void drawItem(int var1, int var2, ItemStack var3) {
         this.drawItemBackground(var1 + 1, var2 + 1);
         GlStateManager.enableRescaleNormal();
         if (var3 != null && var3.getItem() != null) {
            RenderHelper.enableGUIStandardItemLighting();
            GuiCreateFlatWorld.this.itemRender.renderItemIntoGUI(var3, var1 + 2, var2 + 2);
            RenderHelper.disableStandardItemLighting();
         }

         GlStateManager.disableRescaleNormal();
      }

      private void drawItemBackground(int var1, int var2) {
         this.drawItemBackground(var1, var2, 0, 0);
      }

      private void drawItemBackground(int var1, int var2, int var3, int var4) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.getTextureManager().bindTexture(Gui.STAT_ICONS);
         float var5 = 0.0078125F;
         float var6 = 0.0078125F;
         boolean var7 = true;
         boolean var8 = true;
         Tessellator var9 = Tessellator.getInstance();
         VertexBuffer var10 = var9.getBuffer();
         var10.begin(7, DefaultVertexFormats.POSITION_TEX);
         var10.pos((double)(var1 + 0), (double)(var2 + 18), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(var3 + 0) * 0.0078125F), (double)((float)(var4 + 18) * 0.0078125F)).endVertex();
         var10.pos((double)(var1 + 18), (double)(var2 + 18), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(var3 + 18) * 0.0078125F), (double)((float)(var4 + 18) * 0.0078125F)).endVertex();
         var10.pos((double)(var1 + 18), (double)(var2 + 0), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(var3 + 18) * 0.0078125F), (double)((float)(var4 + 0) * 0.0078125F)).endVertex();
         var10.pos((double)(var1 + 0), (double)(var2 + 0), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(var3 + 0) * 0.0078125F), (double)((float)(var4 + 0) * 0.0078125F)).endVertex();
         var9.draw();
      }

      protected int getSize() {
         return GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size();
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
         this.selectedLayer = var1;
         GuiCreateFlatWorld.this.onLayersChanged();
      }

      protected boolean isSelected(int var1) {
         return var1 == this.selectedLayer;
      }

      protected void drawBackground() {
      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         FlatLayerInfo var7 = (FlatLayerInfo)GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().get(GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - var1 - 1);
         IBlockState var8 = var7.getLayerMaterial();
         Block var9 = var8.getBlock();
         Item var10 = Item.getItemFromBlock(var9);
         ItemStack var11 = var9 != Blocks.AIR && var10 != null ? new ItemStack(var10, 1, var9.getMetaFromState(var8)) : null;
         String var12 = var11 == null ? I18n.format("createWorld.customize.flat.air") : var10.getItemStackDisplayName(var11);
         if (var10 == null) {
            if (var9 != Blocks.WATER && var9 != Blocks.FLOWING_WATER) {
               if (var9 == Blocks.LAVA || var9 == Blocks.FLOWING_LAVA) {
                  var10 = Items.LAVA_BUCKET;
               }
            } else {
               var10 = Items.WATER_BUCKET;
            }

            if (var10 != null) {
               var11 = new ItemStack(var10, 1, var9.getMetaFromState(var8));
               var12 = var9.getLocalizedName();
            }
         }

         this.drawItem(var2, var3, var11);
         GuiCreateFlatWorld.this.fontRendererObj.drawString(var12, var2 + 18 + 5, var3 + 3, 16777215);
         String var13;
         if (var1 == 0) {
            var13 = I18n.format("createWorld.customize.flat.layer.top", var7.getLayerCount());
         } else if (var1 == GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - 1) {
            var13 = I18n.format("createWorld.customize.flat.layer.bottom", var7.getLayerCount());
         } else {
            var13 = I18n.format("createWorld.customize.flat.layer", var7.getLayerCount());
         }

         GuiCreateFlatWorld.this.fontRendererObj.drawString(var13, var2 + 2 + 213 - GuiCreateFlatWorld.this.fontRendererObj.getStringWidth(var13), var3 + 3, 16777215);
      }

      protected int getScrollBarX() {
         return this.width - 70;
      }
   }
}
