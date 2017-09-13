package net.minecraft.client.gui;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiCommandBlock extends GuiScreen implements ITabCompleter {
   private GuiTextField commandTextField;
   private GuiTextField previousOutputTextField;
   private final TileEntityCommandBlock commandBlock;
   private GuiButton doneBtn;
   private GuiButton cancelBtn;
   private GuiButton outputBtn;
   private GuiButton modeBtn;
   private GuiButton conditionalBtn;
   private GuiButton autoExecBtn;
   private boolean trackOutput;
   private TileEntityCommandBlock.Mode commandBlockMode = TileEntityCommandBlock.Mode.REDSTONE;
   private TabCompleter tabCompleter;
   private boolean conditional;
   private boolean automatic;

   public GuiCommandBlock(TileEntityCommandBlock var1) {
      this.commandBlock = var1;
   }

   public void updateScreen() {
      this.commandTextField.updateCursorCounter();
   }

   public void initGui() {
      final CommandBlockBaseLogic var1 = this.commandBlock.getCommandBlockLogic();
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
      this.doneBtn = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.done")));
      this.cancelBtn = this.addButton(new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel")));
      this.outputBtn = this.addButton(new GuiButton(4, this.width / 2 + 150 - 20, 135, 20, 20, "O"));
      this.modeBtn = this.addButton(new GuiButton(5, this.width / 2 - 50 - 100 - 4, 165, 100, 20, I18n.format("advMode.mode.sequence")));
      this.conditionalBtn = this.addButton(new GuiButton(6, this.width / 2 - 50, 165, 100, 20, I18n.format("advMode.mode.unconditional")));
      this.autoExecBtn = this.addButton(new GuiButton(7, this.width / 2 + 50 + 4, 165, 100, 20, I18n.format("advMode.mode.redstoneTriggered")));
      this.commandTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 150, 50, 300, 20);
      this.commandTextField.setMaxStringLength(32500);
      this.commandTextField.setFocused(true);
      this.previousOutputTextField = new GuiTextField(3, this.fontRendererObj, this.width / 2 - 150, 135, 276, 20);
      this.previousOutputTextField.setMaxStringLength(32500);
      this.previousOutputTextField.setEnabled(false);
      this.previousOutputTextField.setText("-");
      this.doneBtn.enabled = false;
      this.outputBtn.enabled = false;
      this.modeBtn.enabled = false;
      this.conditionalBtn.enabled = false;
      this.autoExecBtn.enabled = false;
      this.tabCompleter = new TabCompleter(this.commandTextField, true) {
         @Nullable
         public BlockPos getTargetBlockPos() {
            return var1.getPosition();
         }
      };
   }

   public void updateGui() {
      CommandBlockBaseLogic var1 = this.commandBlock.getCommandBlockLogic();
      this.commandTextField.setText(var1.getCommand());
      this.trackOutput = var1.shouldTrackOutput();
      this.commandBlockMode = this.commandBlock.getMode();
      this.conditional = this.commandBlock.isConditional();
      this.automatic = this.commandBlock.isAuto();
      this.updateCmdOutput();
      this.updateMode();
      this.updateConditional();
      this.updateAutoExec();
      this.doneBtn.enabled = true;
      this.outputBtn.enabled = true;
      this.modeBtn.enabled = true;
      this.conditionalBtn.enabled = true;
      this.autoExecBtn.enabled = true;
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         CommandBlockBaseLogic var2 = this.commandBlock.getCommandBlockLogic();
         if (var1.id == 1) {
            var2.setTrackOutput(this.trackOutput);
            this.mc.displayGuiScreen((GuiScreen)null);
         } else if (var1.id == 0) {
            PacketBuffer var3 = new PacketBuffer(Unpooled.buffer());
            var2.fillInInfo(var3);
            var3.writeString(this.commandTextField.getText());
            var3.writeBoolean(var2.shouldTrackOutput());
            var3.writeString(this.commandBlockMode.name());
            var3.writeBoolean(this.conditional);
            var3.writeBoolean(this.automatic);
            this.mc.getConnection().sendPacket(new CPacketCustomPayload("MC|AutoCmd", var3));
            if (!var2.shouldTrackOutput()) {
               var2.setLastOutput((ITextComponent)null);
            }

            this.mc.displayGuiScreen((GuiScreen)null);
         } else if (var1.id == 4) {
            var2.setTrackOutput(!var2.shouldTrackOutput());
            this.updateCmdOutput();
         } else if (var1.id == 5) {
            this.nextMode();
            this.updateMode();
         } else if (var1.id == 6) {
            this.conditional = !this.conditional;
            this.updateConditional();
         } else if (var1.id == 7) {
            this.automatic = !this.automatic;
            this.updateAutoExec();
         }
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      this.tabCompleter.resetRequested();
      if (var2 == 15) {
         this.tabCompleter.complete();
      } else {
         this.tabCompleter.resetDidComplete();
      }

      this.commandTextField.textboxKeyTyped(var1, var2);
      this.previousOutputTextField.textboxKeyTyped(var1, var2);
      if (var2 != 28 && var2 != 156) {
         if (var2 == 1) {
            this.actionPerformed(this.cancelBtn);
         }
      } else {
         this.actionPerformed(this.doneBtn);
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      this.commandTextField.mouseClicked(var1, var2, var3);
      this.previousOutputTextField.mouseClicked(var1, var2, var3);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, I18n.format("advMode.setCommand"), this.width / 2, 20, 16777215);
      this.drawString(this.fontRendererObj, I18n.format("advMode.command"), this.width / 2 - 150, 37, 10526880);
      this.commandTextField.drawTextBox();
      int var4 = 75;
      int var5 = 0;
      this.drawString(this.fontRendererObj, I18n.format("advMode.nearestPlayer"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, I18n.format("advMode.randomPlayer"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, I18n.format("advMode.allPlayers"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, I18n.format("advMode.allEntities"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, "", this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      if (!this.previousOutputTextField.getText().isEmpty()) {
         var4 = var4 + var5 * this.fontRendererObj.FONT_HEIGHT + 1;
         this.drawString(this.fontRendererObj, I18n.format("advMode.previousOutput"), this.width / 2 - 150, var4, 10526880);
         this.previousOutputTextField.drawTextBox();
      }

      super.drawScreen(var1, var2, var3);
   }

   private void updateCmdOutput() {
      CommandBlockBaseLogic var1 = this.commandBlock.getCommandBlockLogic();
      if (var1.shouldTrackOutput()) {
         this.outputBtn.displayString = "O";
         if (var1.getLastOutput() != null) {
            this.previousOutputTextField.setText(var1.getLastOutput().getUnformattedText());
         }
      } else {
         this.outputBtn.displayString = "X";
         this.previousOutputTextField.setText("-");
      }

   }

   private void updateMode() {
      switch(this.commandBlockMode) {
      case SEQUENCE:
         this.modeBtn.displayString = I18n.format("advMode.mode.sequence");
         break;
      case AUTO:
         this.modeBtn.displayString = I18n.format("advMode.mode.auto");
         break;
      case REDSTONE:
         this.modeBtn.displayString = I18n.format("advMode.mode.redstone");
      }

   }

   private void nextMode() {
      switch(this.commandBlockMode) {
      case SEQUENCE:
         this.commandBlockMode = TileEntityCommandBlock.Mode.AUTO;
         break;
      case AUTO:
         this.commandBlockMode = TileEntityCommandBlock.Mode.REDSTONE;
         break;
      case REDSTONE:
         this.commandBlockMode = TileEntityCommandBlock.Mode.SEQUENCE;
      }

   }

   private void updateConditional() {
      if (this.conditional) {
         this.conditionalBtn.displayString = I18n.format("advMode.mode.conditional");
      } else {
         this.conditionalBtn.displayString = I18n.format("advMode.mode.unconditional");
      }

   }

   private void updateAutoExec() {
      if (this.automatic) {
         this.autoExecBtn.displayString = I18n.format("advMode.mode.autoexec.bat");
      } else {
         this.autoExecBtn.displayString = I18n.format("advMode.mode.redstoneTriggered");
      }

   }

   public void setCompletions(String... var1) {
      this.tabCompleter.setCompletions(var1);
   }
}
