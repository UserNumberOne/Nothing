package net.minecraft.client.gui.inventory;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiEditStructure extends GuiScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final int[] LEGAL_KEY_CODES = new int[]{203, 205, 14, 211, 199, 207};
   private final TileEntityStructure tileStructure;
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private TileEntityStructure.Mode mode = TileEntityStructure.Mode.DATA;
   private boolean ignoreEntities;
   private boolean showAir;
   private boolean showBoundingBox;
   private GuiTextField nameEdit;
   private GuiTextField posXEdit;
   private GuiTextField posYEdit;
   private GuiTextField posZEdit;
   private GuiTextField sizeXEdit;
   private GuiTextField sizeYEdit;
   private GuiTextField sizeZEdit;
   private GuiTextField integrityEdit;
   private GuiTextField seedEdit;
   private GuiTextField dataEdit;
   private GuiButton doneButton;
   private GuiButton cancelButton;
   private GuiButton saveButton;
   private GuiButton loadButton;
   private GuiButton rotateZeroDegreesButton;
   private GuiButton rotateNinetyDegreesButton;
   private GuiButton rotate180DegreesButton;
   private GuiButton rotate270DegressButton;
   private GuiButton modeButton;
   private GuiButton detectSizeButton;
   private GuiButton showEntitiesButton;
   private GuiButton mirrorButton;
   private GuiButton showAirButton;
   private GuiButton showBoundingBoxButton;
   private final List tabOrder = Lists.newArrayList();
   private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

   public GuiEditStructure(TileEntityStructure var1) {
      this.tileStructure = var1;
      this.decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
   }

   public void updateScreen() {
      this.nameEdit.updateCursorCounter();
      this.posXEdit.updateCursorCounter();
      this.posYEdit.updateCursorCounter();
      this.posZEdit.updateCursorCounter();
      this.sizeXEdit.updateCursorCounter();
      this.sizeYEdit.updateCursorCounter();
      this.sizeZEdit.updateCursorCounter();
      this.integrityEdit.updateCursorCounter();
      this.seedEdit.updateCursorCounter();
      this.dataEdit.updateCursorCounter();
   }

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
      this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, 210, 150, 20, I18n.format("gui.done")));
      this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 + 4, 210, 150, 20, I18n.format("gui.cancel")));
      this.saveButton = this.addButton(new GuiButton(9, this.width / 2 + 4 + 100, 185, 50, 20, I18n.format("structure_block.button.save")));
      this.loadButton = this.addButton(new GuiButton(10, this.width / 2 + 4 + 100, 185, 50, 20, I18n.format("structure_block.button.load")));
      this.modeButton = this.addButton(new GuiButton(18, this.width / 2 - 4 - 150, 185, 50, 20, "MODE"));
      this.detectSizeButton = this.addButton(new GuiButton(19, this.width / 2 + 4 + 100, 120, 50, 20, I18n.format("structure_block.button.detect_size")));
      this.showEntitiesButton = this.addButton(new GuiButton(20, this.width / 2 + 4 + 100, 160, 50, 20, "ENTITIES"));
      this.mirrorButton = this.addButton(new GuiButton(21, this.width / 2 - 20, 185, 40, 20, "MIRROR"));
      this.showAirButton = this.addButton(new GuiButton(22, this.width / 2 + 4 + 100, 80, 50, 20, "SHOWAIR"));
      this.showBoundingBoxButton = this.addButton(new GuiButton(23, this.width / 2 + 4 + 100, 80, 50, 20, "SHOWBB"));
      this.rotateZeroDegreesButton = this.addButton(new GuiButton(11, this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20, "0"));
      this.rotateNinetyDegreesButton = this.addButton(new GuiButton(12, this.width / 2 - 1 - 40 - 20, 185, 40, 20, "90"));
      this.rotate180DegreesButton = this.addButton(new GuiButton(13, this.width / 2 + 1 + 20, 185, 40, 20, "180"));
      this.rotate270DegressButton = this.addButton(new GuiButton(14, this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20, "270"));
      this.nameEdit = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 152, 40, 300, 20);
      this.nameEdit.setMaxStringLength(64);
      this.nameEdit.setText(this.tileStructure.getName());
      this.tabOrder.add(this.nameEdit);
      BlockPos var1 = this.tileStructure.getPosition();
      this.posXEdit = new GuiTextField(3, this.fontRendererObj, this.width / 2 - 152, 80, 80, 20);
      this.posXEdit.setMaxStringLength(15);
      this.posXEdit.setText(Integer.toString(var1.getX()));
      this.tabOrder.add(this.posXEdit);
      this.posYEdit = new GuiTextField(4, this.fontRendererObj, this.width / 2 - 72, 80, 80, 20);
      this.posYEdit.setMaxStringLength(15);
      this.posYEdit.setText(Integer.toString(var1.getY()));
      this.tabOrder.add(this.posYEdit);
      this.posZEdit = new GuiTextField(5, this.fontRendererObj, this.width / 2 + 8, 80, 80, 20);
      this.posZEdit.setMaxStringLength(15);
      this.posZEdit.setText(Integer.toString(var1.getZ()));
      this.tabOrder.add(this.posZEdit);
      BlockPos var2 = this.tileStructure.getStructureSize();
      this.sizeXEdit = new GuiTextField(6, this.fontRendererObj, this.width / 2 - 152, 120, 80, 20);
      this.sizeXEdit.setMaxStringLength(15);
      this.sizeXEdit.setText(Integer.toString(var2.getX()));
      this.tabOrder.add(this.sizeXEdit);
      this.sizeYEdit = new GuiTextField(7, this.fontRendererObj, this.width / 2 - 72, 120, 80, 20);
      this.sizeYEdit.setMaxStringLength(15);
      this.sizeYEdit.setText(Integer.toString(var2.getY()));
      this.tabOrder.add(this.sizeYEdit);
      this.sizeZEdit = new GuiTextField(8, this.fontRendererObj, this.width / 2 + 8, 120, 80, 20);
      this.sizeZEdit.setMaxStringLength(15);
      this.sizeZEdit.setText(Integer.toString(var2.getZ()));
      this.tabOrder.add(this.sizeZEdit);
      this.integrityEdit = new GuiTextField(15, this.fontRendererObj, this.width / 2 - 152, 120, 80, 20);
      this.integrityEdit.setMaxStringLength(15);
      this.integrityEdit.setText(this.decimalFormat.format((double)this.tileStructure.getIntegrity()));
      this.tabOrder.add(this.integrityEdit);
      this.seedEdit = new GuiTextField(16, this.fontRendererObj, this.width / 2 - 72, 120, 80, 20);
      this.seedEdit.setMaxStringLength(31);
      this.seedEdit.setText(Long.toString(this.tileStructure.getSeed()));
      this.tabOrder.add(this.seedEdit);
      this.dataEdit = new GuiTextField(17, this.fontRendererObj, this.width / 2 - 152, 120, 240, 20);
      this.dataEdit.setMaxStringLength(128);
      this.dataEdit.setText(this.tileStructure.getMetadata());
      this.tabOrder.add(this.dataEdit);
      this.mirror = this.tileStructure.getMirror();
      this.updateMirrorButton();
      this.rotation = this.tileStructure.getRotation();
      this.updateDirectionButtons();
      this.mode = this.tileStructure.getMode();
      this.updateMode();
      this.ignoreEntities = this.tileStructure.ignoresEntities();
      this.updateEntitiesButton();
      this.showAir = this.tileStructure.showsAir();
      this.updateToggleAirButton();
      this.showBoundingBox = this.tileStructure.showsBoundingBox();
      this.updateToggleBoundingBox();
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 1) {
            this.tileStructure.setMirror(this.mirror);
            this.tileStructure.setRotation(this.rotation);
            this.tileStructure.setMode(this.mode);
            this.tileStructure.setIgnoresEntities(this.ignoreEntities);
            this.tileStructure.setShowAir(this.showAir);
            this.tileStructure.setShowBoundingBox(this.showBoundingBox);
            this.mc.displayGuiScreen((GuiScreen)null);
         } else if (var1.id == 0) {
            if (this.sendToServer(1)) {
               this.mc.displayGuiScreen((GuiScreen)null);
            }
         } else if (var1.id == 9) {
            if (this.tileStructure.getMode() == TileEntityStructure.Mode.SAVE) {
               this.sendToServer(2);
               this.mc.displayGuiScreen((GuiScreen)null);
            }
         } else if (var1.id == 10) {
            if (this.tileStructure.getMode() == TileEntityStructure.Mode.LOAD) {
               this.sendToServer(3);
               this.mc.displayGuiScreen((GuiScreen)null);
            }
         } else if (var1.id == 11) {
            this.tileStructure.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
         } else if (var1.id == 12) {
            this.tileStructure.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
         } else if (var1.id == 13) {
            this.tileStructure.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
         } else if (var1.id == 14) {
            this.tileStructure.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
         } else if (var1.id == 18) {
            this.tileStructure.nextMode();
            this.updateMode();
         } else if (var1.id == 19) {
            if (this.tileStructure.getMode() == TileEntityStructure.Mode.SAVE) {
               this.sendToServer(4);
               this.mc.displayGuiScreen((GuiScreen)null);
            }
         } else if (var1.id == 20) {
            this.tileStructure.setIgnoresEntities(!this.tileStructure.ignoresEntities());
            this.updateEntitiesButton();
         } else if (var1.id == 22) {
            this.tileStructure.setShowAir(!this.tileStructure.showsAir());
            this.updateToggleAirButton();
         } else if (var1.id == 23) {
            this.tileStructure.setShowBoundingBox(!this.tileStructure.showsBoundingBox());
            this.updateToggleBoundingBox();
         } else if (var1.id == 21) {
            switch(this.tileStructure.getMirror()) {
            case NONE:
               this.tileStructure.setMirror(Mirror.LEFT_RIGHT);
               break;
            case LEFT_RIGHT:
               this.tileStructure.setMirror(Mirror.FRONT_BACK);
               break;
            case FRONT_BACK:
               this.tileStructure.setMirror(Mirror.NONE);
            }

            this.updateMirrorButton();
         }
      }

   }

   private void updateEntitiesButton() {
      boolean var1 = !this.tileStructure.ignoresEntities();
      if (var1) {
         this.showEntitiesButton.displayString = I18n.format("options.on");
      } else {
         this.showEntitiesButton.displayString = I18n.format("options.off");
      }

   }

   private void updateToggleAirButton() {
      boolean var1 = this.tileStructure.showsAir();
      if (var1) {
         this.showAirButton.displayString = I18n.format("options.on");
      } else {
         this.showAirButton.displayString = I18n.format("options.off");
      }

   }

   private void updateToggleBoundingBox() {
      boolean var1 = this.tileStructure.showsBoundingBox();
      if (var1) {
         this.showBoundingBoxButton.displayString = I18n.format("options.on");
      } else {
         this.showBoundingBoxButton.displayString = I18n.format("options.off");
      }

   }

   private void updateMirrorButton() {
      Mirror var1 = this.tileStructure.getMirror();
      switch(var1) {
      case NONE:
         this.mirrorButton.displayString = "|";
         break;
      case LEFT_RIGHT:
         this.mirrorButton.displayString = "< >";
         break;
      case FRONT_BACK:
         this.mirrorButton.displayString = "^ v";
      }

   }

   private void updateDirectionButtons() {
      this.rotateZeroDegreesButton.enabled = true;
      this.rotateNinetyDegreesButton.enabled = true;
      this.rotate180DegreesButton.enabled = true;
      this.rotate270DegressButton.enabled = true;
      switch(this.tileStructure.getRotation()) {
      case NONE:
         this.rotateZeroDegreesButton.enabled = false;
         break;
      case CLOCKWISE_180:
         this.rotate180DegreesButton.enabled = false;
         break;
      case COUNTERCLOCKWISE_90:
         this.rotate270DegressButton.enabled = false;
         break;
      case CLOCKWISE_90:
         this.rotateNinetyDegreesButton.enabled = false;
      }

   }

   private void updateMode() {
      this.nameEdit.setFocused(false);
      this.posXEdit.setFocused(false);
      this.posYEdit.setFocused(false);
      this.posZEdit.setFocused(false);
      this.sizeXEdit.setFocused(false);
      this.sizeYEdit.setFocused(false);
      this.sizeZEdit.setFocused(false);
      this.integrityEdit.setFocused(false);
      this.seedEdit.setFocused(false);
      this.dataEdit.setFocused(false);
      this.nameEdit.setVisible(false);
      this.nameEdit.setFocused(false);
      this.posXEdit.setVisible(false);
      this.posYEdit.setVisible(false);
      this.posZEdit.setVisible(false);
      this.sizeXEdit.setVisible(false);
      this.sizeYEdit.setVisible(false);
      this.sizeZEdit.setVisible(false);
      this.integrityEdit.setVisible(false);
      this.seedEdit.setVisible(false);
      this.dataEdit.setVisible(false);
      this.saveButton.visible = false;
      this.loadButton.visible = false;
      this.detectSizeButton.visible = false;
      this.showEntitiesButton.visible = false;
      this.mirrorButton.visible = false;
      this.rotateZeroDegreesButton.visible = false;
      this.rotateNinetyDegreesButton.visible = false;
      this.rotate180DegreesButton.visible = false;
      this.rotate270DegressButton.visible = false;
      this.showAirButton.visible = false;
      this.showBoundingBoxButton.visible = false;
      switch(this.tileStructure.getMode()) {
      case SAVE:
         this.nameEdit.setVisible(true);
         this.nameEdit.setFocused(true);
         this.posXEdit.setVisible(true);
         this.posYEdit.setVisible(true);
         this.posZEdit.setVisible(true);
         this.sizeXEdit.setVisible(true);
         this.sizeYEdit.setVisible(true);
         this.sizeZEdit.setVisible(true);
         this.saveButton.visible = true;
         this.detectSizeButton.visible = true;
         this.showEntitiesButton.visible = true;
         this.showAirButton.visible = true;
         break;
      case LOAD:
         this.nameEdit.setVisible(true);
         this.nameEdit.setFocused(true);
         this.posXEdit.setVisible(true);
         this.posYEdit.setVisible(true);
         this.posZEdit.setVisible(true);
         this.integrityEdit.setVisible(true);
         this.seedEdit.setVisible(true);
         this.loadButton.visible = true;
         this.showEntitiesButton.visible = true;
         this.mirrorButton.visible = true;
         this.rotateZeroDegreesButton.visible = true;
         this.rotateNinetyDegreesButton.visible = true;
         this.rotate180DegreesButton.visible = true;
         this.rotate270DegressButton.visible = true;
         this.showBoundingBoxButton.visible = true;
         this.updateDirectionButtons();
         break;
      case CORNER:
         this.nameEdit.setVisible(true);
         this.nameEdit.setFocused(true);
         break;
      case DATA:
         this.dataEdit.setVisible(true);
         this.dataEdit.setFocused(true);
      }

      this.modeButton.displayString = I18n.format("structure_block.mode." + this.tileStructure.getMode().getName());
   }

   private boolean sendToServer(int var1) {
      try {
         PacketBuffer var2 = new PacketBuffer(Unpooled.buffer());
         this.tileStructure.writeCoordinates(var2);
         var2.writeByte(var1);
         var2.writeString(this.tileStructure.getMode().toString());
         var2.writeString(this.nameEdit.getText());
         var2.writeInt(this.parseCoordinate(this.posXEdit.getText()));
         var2.writeInt(this.parseCoordinate(this.posYEdit.getText()));
         var2.writeInt(this.parseCoordinate(this.posZEdit.getText()));
         var2.writeInt(this.parseCoordinate(this.sizeXEdit.getText()));
         var2.writeInt(this.parseCoordinate(this.sizeYEdit.getText()));
         var2.writeInt(this.parseCoordinate(this.sizeZEdit.getText()));
         var2.writeString(this.tileStructure.getMirror().toString());
         var2.writeString(this.tileStructure.getRotation().toString());
         var2.writeString(this.dataEdit.getText());
         var2.writeBoolean(this.tileStructure.ignoresEntities());
         var2.writeBoolean(this.tileStructure.showsAir());
         var2.writeBoolean(this.tileStructure.showsBoundingBox());
         var2.writeFloat(this.parseIntegrity(this.integrityEdit.getText()));
         var2.writeVarLong(this.parseSeed(this.seedEdit.getText()));
         this.mc.getConnection().sendPacket(new CPacketCustomPayload("MC|Struct", var2));
         return true;
      } catch (Exception var3) {
         LOGGER.warn("Could not send structure block info", var3);
         return false;
      }
   }

   private long parseSeed(String var1) {
      try {
         return Long.valueOf(var1).longValue();
      } catch (NumberFormatException var3) {
         return 0L;
      }
   }

   private float parseIntegrity(String var1) {
      try {
         return Float.valueOf(var1).floatValue();
      } catch (NumberFormatException var3) {
         return 1.0F;
      }
   }

   private int parseCoordinate(String var1) {
      try {
         return Integer.parseInt(var1);
      } catch (NumberFormatException var3) {
         return 0;
      }
   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (this.nameEdit.getVisible() && isValidCharacterForName(var1, var2)) {
         this.nameEdit.textboxKeyTyped(var1, var2);
      }

      if (this.posXEdit.getVisible()) {
         this.posXEdit.textboxKeyTyped(var1, var2);
      }

      if (this.posYEdit.getVisible()) {
         this.posYEdit.textboxKeyTyped(var1, var2);
      }

      if (this.posZEdit.getVisible()) {
         this.posZEdit.textboxKeyTyped(var1, var2);
      }

      if (this.sizeXEdit.getVisible()) {
         this.sizeXEdit.textboxKeyTyped(var1, var2);
      }

      if (this.sizeYEdit.getVisible()) {
         this.sizeYEdit.textboxKeyTyped(var1, var2);
      }

      if (this.sizeZEdit.getVisible()) {
         this.sizeZEdit.textboxKeyTyped(var1, var2);
      }

      if (this.integrityEdit.getVisible()) {
         this.integrityEdit.textboxKeyTyped(var1, var2);
      }

      if (this.seedEdit.getVisible()) {
         this.seedEdit.textboxKeyTyped(var1, var2);
      }

      if (this.dataEdit.getVisible()) {
         this.dataEdit.textboxKeyTyped(var1, var2);
      }

      if (var2 == 15) {
         GuiTextField var3 = null;
         GuiTextField var4 = null;

         for(GuiTextField var6 : this.tabOrder) {
            if (var3 != null && var6.getVisible()) {
               var4 = var6;
               break;
            }

            if (var6.isFocused() && var6.getVisible()) {
               var3 = var6;
            }
         }

         if (var3 != null && var4 == null) {
            for(GuiTextField var8 : this.tabOrder) {
               if (var8.getVisible() && var8 != var3) {
                  var4 = var8;
                  break;
               }
            }
         }

         if (var4 != null && var4 != var3) {
            var3.setFocused(false);
            var4.setFocused(true);
         }
      }

      if (var2 != 28 && var2 != 156) {
         if (var2 == 1) {
            this.actionPerformed(this.cancelButton);
         }
      } else {
         this.actionPerformed(this.doneButton);
      }

   }

   private static boolean isValidCharacterForName(char var0, int var1) {
      boolean var2 = true;

      for(int var6 : LEGAL_KEY_CODES) {
         if (var6 == var1) {
            return true;
         }
      }

      for(char var10 : ChatAllowedCharacters.ILLEGAL_STRUCTURE_CHARACTERS) {
         if (var10 == var0) {
            var2 = false;
            break;
         }
      }

      return var2;
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      if (this.nameEdit.getVisible()) {
         this.nameEdit.mouseClicked(var1, var2, var3);
      }

      if (this.posXEdit.getVisible()) {
         this.posXEdit.mouseClicked(var1, var2, var3);
      }

      if (this.posYEdit.getVisible()) {
         this.posYEdit.mouseClicked(var1, var2, var3);
      }

      if (this.posZEdit.getVisible()) {
         this.posZEdit.mouseClicked(var1, var2, var3);
      }

      if (this.sizeXEdit.getVisible()) {
         this.sizeXEdit.mouseClicked(var1, var2, var3);
      }

      if (this.sizeYEdit.getVisible()) {
         this.sizeYEdit.mouseClicked(var1, var2, var3);
      }

      if (this.sizeZEdit.getVisible()) {
         this.sizeZEdit.mouseClicked(var1, var2, var3);
      }

      if (this.integrityEdit.getVisible()) {
         this.integrityEdit.mouseClicked(var1, var2, var3);
      }

      if (this.seedEdit.getVisible()) {
         this.seedEdit.mouseClicked(var1, var2, var3);
      }

      if (this.dataEdit.getVisible()) {
         this.dataEdit.mouseClicked(var1, var2, var3);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      TileEntityStructure.Mode var4 = this.tileStructure.getMode();
      this.drawCenteredString(this.fontRendererObj, I18n.format("tile.structureBlock.name"), this.width / 2, 10, 16777215);
      if (var4 != TileEntityStructure.Mode.DATA) {
         this.drawString(this.fontRendererObj, I18n.format("structure_block.structure_name"), this.width / 2 - 153, 30, 10526880);
         this.nameEdit.drawTextBox();
      }

      if (var4 == TileEntityStructure.Mode.LOAD || var4 == TileEntityStructure.Mode.SAVE) {
         this.drawString(this.fontRendererObj, I18n.format("structure_block.position"), this.width / 2 - 153, 70, 10526880);
         this.posXEdit.drawTextBox();
         this.posYEdit.drawTextBox();
         this.posZEdit.drawTextBox();
         String var5 = I18n.format("structure_block.include_entities");
         int var6 = this.fontRendererObj.getStringWidth(var5);
         this.drawString(this.fontRendererObj, var5, this.width / 2 + 154 - var6, 150, 10526880);
      }

      if (var4 == TileEntityStructure.Mode.SAVE) {
         this.drawString(this.fontRendererObj, I18n.format("structure_block.size"), this.width / 2 - 153, 110, 10526880);
         this.sizeXEdit.drawTextBox();
         this.sizeYEdit.drawTextBox();
         this.sizeZEdit.drawTextBox();
         String var9 = I18n.format("structure_block.detect_size");
         int var12 = this.fontRendererObj.getStringWidth(var9);
         this.drawString(this.fontRendererObj, var9, this.width / 2 + 154 - var12, 110, 10526880);
         String var7 = I18n.format("structure_block.show_air");
         int var8 = this.fontRendererObj.getStringWidth(var7);
         this.drawString(this.fontRendererObj, var7, this.width / 2 + 154 - var8, 70, 10526880);
      }

      if (var4 == TileEntityStructure.Mode.LOAD) {
         this.drawString(this.fontRendererObj, I18n.format("structure_block.integrity"), this.width / 2 - 153, 110, 10526880);
         this.integrityEdit.drawTextBox();
         this.seedEdit.drawTextBox();
         String var10 = I18n.format("structure_block.show_boundingbox");
         int var13 = this.fontRendererObj.getStringWidth(var10);
         this.drawString(this.fontRendererObj, var10, this.width / 2 + 154 - var13, 70, 10526880);
      }

      if (var4 == TileEntityStructure.Mode.DATA) {
         this.drawString(this.fontRendererObj, I18n.format("structure_block.custom_data"), this.width / 2 - 153, 110, 10526880);
         this.dataEdit.drawTextBox();
      }

      String var11 = "structure_block.mode_info." + var4.getName();
      this.drawString(this.fontRendererObj, I18n.format(var11), this.width / 2 - 153, 174, 10526880);
      super.drawScreen(var1, var2, var3);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }
}
