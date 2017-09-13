package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiFlatPresets extends GuiScreen {
   private static final List FLAT_WORLD_PRESETS = Lists.newArrayList();
   private final GuiCreateFlatWorld parentScreen;
   private String presetsTitle;
   private String presetsShare;
   private String listText;
   private GuiFlatPresets.ListSlot list;
   private GuiButton btnSelect;
   private GuiTextField export;

   public GuiFlatPresets(GuiCreateFlatWorld var1) {
      this.parentScreen = var1;
   }

   public void initGui() {
      this.buttonList.clear();
      Keyboard.enableRepeatEvents(true);
      this.presetsTitle = I18n.format("createWorld.customize.presets.title");
      this.presetsShare = I18n.format("createWorld.customize.presets.share");
      this.listText = I18n.format("createWorld.customize.presets.list");
      this.export = new GuiTextField(2, this.fontRendererObj, 50, 40, this.width - 100, 20);
      this.list = new GuiFlatPresets.ListSlot();
      this.export.setMaxStringLength(1230);
      this.export.setText(this.parentScreen.getPreset());
      this.btnSelect = this.addButton(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("createWorld.customize.presets.select")));
      this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
      this.updateButtonValidity();
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.list.handleMouseInput();
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      this.export.mouseClicked(var1, var2, var3);
      super.mouseClicked(var1, var2, var3);
   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (!this.export.textboxKeyTyped(var1, var2)) {
         super.keyTyped(var1, var2);
      }

   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == 0 && this.hasValidSelection()) {
         this.parentScreen.setPreset(this.export.getText());
         this.mc.displayGuiScreen(this.parentScreen);
      } else if (var1.id == 1) {
         this.mc.displayGuiScreen(this.parentScreen);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.list.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, this.presetsTitle, this.width / 2, 8, 16777215);
      this.drawString(this.fontRendererObj, this.presetsShare, 50, 30, 10526880);
      this.drawString(this.fontRendererObj, this.listText, 50, 70, 10526880);
      this.export.drawTextBox();
      super.drawScreen(var1, var2, var3);
   }

   public void updateScreen() {
      this.export.updateCursorCounter();
      super.updateScreen();
   }

   public void updateButtonValidity() {
      this.btnSelect.enabled = this.hasValidSelection();
   }

   private boolean hasValidSelection() {
      return this.list.selected > -1 && this.list.selected < FLAT_WORLD_PRESETS.size() || this.export.getText().length() > 1;
   }

   private static void registerPreset(String var0, Item var1, Biome var2, FlatLayerInfo... var3) {
      registerPreset(var0, var1, 0, var2, (List)null, var3);
   }

   private static void registerPreset(String var0, Item var1, Biome var2, @Nullable List var3, FlatLayerInfo... var4) {
      registerPreset(var0, var1, 0, var2, var3, var4);
   }

   private static void registerPreset(String var0, Item var1, int var2, Biome var3, @Nullable List var4, FlatLayerInfo... var5) {
      FlatGeneratorInfo var6 = new FlatGeneratorInfo();

      for(int var7 = var5.length - 1; var7 >= 0; --var7) {
         var6.getFlatLayers().add(var5[var7]);
      }

      var6.setBiome(Biome.getIdForBiome(var3));
      var6.updateLayers();
      if (var4 != null) {
         for(String var8 : var4) {
            var6.getWorldFeatures().put(var8, Maps.newHashMap());
         }
      }

      FLAT_WORLD_PRESETS.add(new GuiFlatPresets.LayerItem(var1, var2, var0, var6.toString()));
   }

   static {
      registerPreset("Classic Flat", Item.getItemFromBlock(Blocks.GRASS), Biomes.PLAINS, Arrays.asList("village"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(2, Blocks.DIRT), new FlatLayerInfo(1, Blocks.BEDROCK));
      registerPreset("Tunnelers' Dream", Item.getItemFromBlock(Blocks.STONE), Biomes.EXTREME_HILLS, Arrays.asList("biome_1", "dungeon", "decoration", "stronghold", "mineshaft"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(230, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      registerPreset("Water World", Items.WATER_BUCKET, Biomes.DEEP_OCEAN, Arrays.asList("biome_1", "oceanmonument"), new FlatLayerInfo(90, Blocks.WATER), new FlatLayerInfo(5, Blocks.SAND), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(5, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      registerPreset("Overworld", Item.getItemFromBlock(Blocks.TALLGRASS), BlockTallGrass.EnumType.GRASS.getMeta(), Biomes.PLAINS, Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon", "lake", "lava_lake"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      registerPreset("Snowy Kingdom", Item.getItemFromBlock(Blocks.SNOW_LAYER), Biomes.ICE_PLAINS, Arrays.asList("village", "biome_1"), new FlatLayerInfo(1, Blocks.SNOW_LAYER), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      registerPreset("Bottomless Pit", Items.FEATHER, Biomes.PLAINS, Arrays.asList("village", "biome_1"), new FlatLayerInfo(1, Blocks.GRASS), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(2, Blocks.COBBLESTONE));
      registerPreset("Desert", Item.getItemFromBlock(Blocks.SAND), Biomes.DESERT, Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon"), new FlatLayerInfo(8, Blocks.SAND), new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      registerPreset("Redstone Ready", Items.REDSTONE, Biomes.DESERT, new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      registerPreset("The Void", Item.getItemFromBlock(Blocks.BARRIER), Biomes.VOID, Arrays.asList("decoration"), new FlatLayerInfo(1, Blocks.AIR));
   }

   @SideOnly(Side.CLIENT)
   static class LayerItem {
      public Item icon;
      public int iconMetadata;
      public String name;
      public String generatorInfo;

      public LayerItem(Item var1, int var2, String var3, String var4) {
         this.icon = var1;
         this.iconMetadata = var2;
         this.name = var3;
         this.generatorInfo = var4;
      }
   }

   @SideOnly(Side.CLIENT)
   class ListSlot extends GuiSlot {
      public int selected = -1;

      public ListSlot() {
         super(GuiFlatPresets.this.mc, GuiFlatPresets.this.width, GuiFlatPresets.this.height, 80, GuiFlatPresets.this.height - 37, 24);
      }

      private void renderIcon(int var1, int var2, Item var3, int var4) {
         this.blitSlotBg(var1 + 1, var2 + 1);
         GlStateManager.enableRescaleNormal();
         RenderHelper.enableGUIStandardItemLighting();
         GuiFlatPresets.this.itemRender.renderItemIntoGUI(new ItemStack(var3, 1, var4), var1 + 2, var2 + 2);
         RenderHelper.disableStandardItemLighting();
         GlStateManager.disableRescaleNormal();
      }

      private void blitSlotBg(int var1, int var2) {
         this.blitSlotIcon(var1, var2, 0, 0);
      }

      private void blitSlotIcon(int var1, int var2, int var3, int var4) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.getTextureManager().bindTexture(Gui.STAT_ICONS);
         float var5 = 0.0078125F;
         float var6 = 0.0078125F;
         boolean var7 = true;
         boolean var8 = true;
         Tessellator var9 = Tessellator.getInstance();
         VertexBuffer var10 = var9.getBuffer();
         var10.begin(7, DefaultVertexFormats.POSITION_TEX);
         var10.pos((double)(var1 + 0), (double)(var2 + 18), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(var3 + 0) * 0.0078125F), (double)((float)(var4 + 18) * 0.0078125F)).endVertex();
         var10.pos((double)(var1 + 18), (double)(var2 + 18), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(var3 + 18) * 0.0078125F), (double)((float)(var4 + 18) * 0.0078125F)).endVertex();
         var10.pos((double)(var1 + 18), (double)(var2 + 0), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(var3 + 18) * 0.0078125F), (double)((float)(var4 + 0) * 0.0078125F)).endVertex();
         var10.pos((double)(var1 + 0), (double)(var2 + 0), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(var3 + 0) * 0.0078125F), (double)((float)(var4 + 0) * 0.0078125F)).endVertex();
         var9.draw();
      }

      protected int getSize() {
         return GuiFlatPresets.FLAT_WORLD_PRESETS.size();
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
         this.selected = var1;
         GuiFlatPresets.this.updateButtonValidity();
         GuiFlatPresets.this.export.setText(((GuiFlatPresets.LayerItem)GuiFlatPresets.FLAT_WORLD_PRESETS.get(GuiFlatPresets.this.list.selected)).generatorInfo);
      }

      protected boolean isSelected(int var1) {
         return var1 == this.selected;
      }

      protected void drawBackground() {
      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         GuiFlatPresets.LayerItem var7 = (GuiFlatPresets.LayerItem)GuiFlatPresets.FLAT_WORLD_PRESETS.get(var1);
         this.renderIcon(var2, var3, var7.icon, var7.iconMetadata);
         GuiFlatPresets.this.fontRendererObj.drawString(var7.name, var2 + 18 + 5, var3 + 6, 16777215);
      }
   }
}
