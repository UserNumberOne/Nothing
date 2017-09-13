package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiScreenCustomizePresets extends GuiScreen {
   private static final List PRESETS = Lists.newArrayList();
   private GuiScreenCustomizePresets.ListPreset list;
   private GuiButton select;
   private GuiTextField export;
   private final GuiCustomizeWorldScreen parent;
   protected String title = "Customize World Presets";
   private String shareText;
   private String listText;

   public GuiScreenCustomizePresets(GuiCustomizeWorldScreen var1) {
      this.parent = var1;
   }

   public void initGui() {
      this.buttonList.clear();
      Keyboard.enableRepeatEvents(true);
      this.title = I18n.format("createWorld.customize.custom.presets.title");
      this.shareText = I18n.format("createWorld.customize.presets.share");
      this.listText = I18n.format("createWorld.customize.presets.list");
      this.export = new GuiTextField(2, this.fontRendererObj, 50, 40, this.width - 100, 20);
      this.list = new GuiScreenCustomizePresets.ListPreset();
      this.export.setMaxStringLength(2000);
      this.export.setText(this.parent.saveValues());
      this.select = this.addButton(new GuiButton(0, this.width / 2 - 102, this.height - 27, 100, 20, I18n.format("createWorld.customize.presets.select")));
      this.buttonList.add(new GuiButton(1, this.width / 2 + 3, this.height - 27, 100, 20, I18n.format("gui.cancel")));
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
      switch(var1.id) {
      case 0:
         this.parent.loadValues(this.export.getText());
         this.mc.displayGuiScreen(this.parent);
         break;
      case 1:
         this.mc.displayGuiScreen(this.parent);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.list.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 16777215);
      this.drawString(this.fontRendererObj, this.shareText, 50, 30, 10526880);
      this.drawString(this.fontRendererObj, this.listText, 50, 70, 10526880);
      this.export.drawTextBox();
      super.drawScreen(var1, var2, var3);
   }

   public void updateScreen() {
      this.export.updateCursorCounter();
      super.updateScreen();
   }

   public void updateButtonValidity() {
      this.select.enabled = this.hasValidSelection();
   }

   private boolean hasValidSelection() {
      return this.list.selected > -1 && this.list.selected < PRESETS.size() || this.export.getText().length() > 1;
   }

   static {
      ChunkProviderSettings.Factory var0 = ChunkProviderSettings.Factory.jsonToFactory("{ \"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":8.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":0.5, \"biomeScaleWeight\":2.0, \"biomeScaleOffset\":0.375, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":255 }");
      ResourceLocation var1 = new ResourceLocation("textures/gui/presets/water.png");
      PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.waterWorld"), var1, var0));
      var0 = ChunkProviderSettings.Factory.jsonToFactory("{\"coordinateScale\":3000.0, \"heightScale\":6000.0, \"upperLimitScale\":250.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
      var1 = new ResourceLocation("textures/gui/presets/isles.png");
      PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.isleLand"), var1, var0));
      var0 = ChunkProviderSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":5000.0, \"mainNoiseScaleY\":1000.0, \"mainNoiseScaleZ\":5000.0, \"baseSize\":8.5, \"stretchY\":5.0, \"biomeDepthWeight\":2.0, \"biomeDepthOffset\":1.0, \"biomeScaleWeight\":4.0, \"biomeScaleOffset\":1.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
      var1 = new ResourceLocation("textures/gui/presets/delight.png");
      PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.caveDelight"), var1, var0));
      var0 = ChunkProviderSettings.Factory.jsonToFactory("{\"coordinateScale\":738.41864, \"heightScale\":157.69133, \"upperLimitScale\":801.4267, \"lowerLimitScale\":1254.1643, \"depthNoiseScaleX\":374.93652, \"depthNoiseScaleZ\":288.65228, \"depthNoiseScaleExponent\":1.2092624, \"mainNoiseScaleX\":1355.9908, \"mainNoiseScaleY\":745.5343, \"mainNoiseScaleZ\":1183.464, \"baseSize\":1.8758626, \"stretchY\":1.7137525, \"biomeDepthWeight\":1.7553768, \"biomeDepthOffset\":3.4701107, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":2.535211, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":63 }");
      var1 = new ResourceLocation("textures/gui/presets/madness.png");
      PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.mountains"), var1, var0));
      var0 = ChunkProviderSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":1000.0, \"mainNoiseScaleY\":3000.0, \"mainNoiseScaleZ\":1000.0, \"baseSize\":8.5, \"stretchY\":10.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":20 }");
      var1 = new ResourceLocation("textures/gui/presets/drought.png");
      PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.drought"), var1, var0));
      var0 = ChunkProviderSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":2.0, \"lowerLimitScale\":64.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":false, \"seaLevel\":6 }");
      var1 = new ResourceLocation("textures/gui/presets/chaos.png");
      PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.caveChaos"), var1, var0));
      var0 = ChunkProviderSettings.Factory.jsonToFactory("{\"coordinateScale\":684.412, \"heightScale\":684.412, \"upperLimitScale\":512.0, \"lowerLimitScale\":512.0, \"depthNoiseScaleX\":200.0, \"depthNoiseScaleZ\":200.0, \"depthNoiseScaleExponent\":0.5, \"mainNoiseScaleX\":80.0, \"mainNoiseScaleY\":160.0, \"mainNoiseScaleZ\":80.0, \"baseSize\":8.5, \"stretchY\":12.0, \"biomeDepthWeight\":1.0, \"biomeDepthOffset\":0.0, \"biomeScaleWeight\":1.0, \"biomeScaleOffset\":0.0, \"useCaves\":true, \"useDungeons\":true, \"dungeonChance\":8, \"useStrongholds\":true, \"useVillages\":true, \"useMineShafts\":true, \"useTemples\":true, \"useRavines\":true, \"useWaterLakes\":true, \"waterLakeChance\":4, \"useLavaLakes\":true, \"lavaLakeChance\":80, \"useLavaOceans\":true, \"seaLevel\":40 }");
      var1 = new ResourceLocation("textures/gui/presets/luck.png");
      PRESETS.add(new GuiScreenCustomizePresets.Info(I18n.format("createWorld.customize.custom.preset.goodLuck"), var1, var0));
   }

   @SideOnly(Side.CLIENT)
   static class Info {
      public String name;
      public ResourceLocation texture;
      public ChunkProviderSettings.Factory settings;

      public Info(String var1, ResourceLocation var2, ChunkProviderSettings.Factory var3) {
         this.name = var1;
         this.texture = var2;
         this.settings = var3;
      }
   }

   @SideOnly(Side.CLIENT)
   class ListPreset extends GuiSlot {
      public int selected = -1;

      public ListPreset() {
         super(GuiScreenCustomizePresets.this.mc, GuiScreenCustomizePresets.this.width, GuiScreenCustomizePresets.this.height, 80, GuiScreenCustomizePresets.this.height - 32, 38);
      }

      protected int getSize() {
         return GuiScreenCustomizePresets.PRESETS.size();
      }

      protected void elementClicked(int var1, boolean var2, int var3, int var4) {
         this.selected = var1;
         GuiScreenCustomizePresets.this.updateButtonValidity();
         GuiScreenCustomizePresets.this.export.setText(((GuiScreenCustomizePresets.Info)GuiScreenCustomizePresets.PRESETS.get(GuiScreenCustomizePresets.this.list.selected)).settings.toString());
      }

      protected boolean isSelected(int var1) {
         return var1 == this.selected;
      }

      protected void drawBackground() {
      }

      private void blitIcon(int var1, int var2, ResourceLocation var3) {
         int var4 = var1 + 5;
         GuiScreenCustomizePresets.this.drawHorizontalLine(var4 - 1, var4 + 32, var2 - 1, -2039584);
         GuiScreenCustomizePresets.this.drawHorizontalLine(var4 - 1, var4 + 32, var2 + 32, -6250336);
         GuiScreenCustomizePresets.this.drawVerticalLine(var4 - 1, var2 - 1, var2 + 32, -2039584);
         GuiScreenCustomizePresets.this.drawVerticalLine(var4 + 32, var2 - 1, var2 + 32, -6250336);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.getTextureManager().bindTexture(var3);
         boolean var5 = true;
         boolean var6 = true;
         Tessellator var7 = Tessellator.getInstance();
         VertexBuffer var8 = var7.getBuffer();
         var8.begin(7, DefaultVertexFormats.POSITION_TEX);
         var8.pos((double)(var4 + 0), (double)(var2 + 32), 0.0D).tex(0.0D, 1.0D).endVertex();
         var8.pos((double)(var4 + 32), (double)(var2 + 32), 0.0D).tex(1.0D, 1.0D).endVertex();
         var8.pos((double)(var4 + 32), (double)(var2 + 0), 0.0D).tex(1.0D, 0.0D).endVertex();
         var8.pos((double)(var4 + 0), (double)(var2 + 0), 0.0D).tex(0.0D, 0.0D).endVertex();
         var7.draw();
      }

      protected void drawSlot(int var1, int var2, int var3, int var4, int var5, int var6) {
         GuiScreenCustomizePresets.Info var7 = (GuiScreenCustomizePresets.Info)GuiScreenCustomizePresets.PRESETS.get(var1);
         this.blitIcon(var2, var3, var7.texture);
         GuiScreenCustomizePresets.this.fontRendererObj.drawString(var7.name, var2 + 32 + 10, var3 + 14, 16777215);
      }
   }
}
