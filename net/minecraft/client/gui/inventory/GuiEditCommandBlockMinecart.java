package net.minecraft.client.gui.inventory;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiEditCommandBlockMinecart extends GuiScreen implements ITabCompleter {
   private GuiTextField commandField;
   private GuiTextField previousEdit;
   private final CommandBlockBaseLogic commandBlockLogic;
   private GuiButton doneButton;
   private GuiButton cancelButton;
   private GuiButton outputButton;
   private boolean trackOutput;
   private TabCompleter tabCompleter;

   public GuiEditCommandBlockMinecart(CommandBlockBaseLogic var1) {
      this.commandBlockLogic = var1;
   }

   public void updateScreen() {
      this.commandField.updateCursorCounter();
   }

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
      this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.done")));
      this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel")));
      this.outputButton = this.addButton(new GuiButton(4, this.width / 2 + 150 - 20, 150, 20, 20, "O"));
      this.commandField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 150, 50, 300, 20);
      this.commandField.setMaxStringLength(32500);
      this.commandField.setFocused(true);
      this.commandField.setText(this.commandBlockLogic.getCommand());
      this.previousEdit = new GuiTextField(3, this.fontRendererObj, this.width / 2 - 150, 150, 276, 20);
      this.previousEdit.setMaxStringLength(32500);
      this.previousEdit.setEnabled(false);
      this.previousEdit.setText("-");
      this.trackOutput = this.commandBlockLogic.shouldTrackOutput();
      this.updateCommandOutput();
      this.doneButton.enabled = !this.commandField.getText().trim().isEmpty();
      this.tabCompleter = new TabCompleter(this.commandField, true) {
         @Nullable
         public BlockPos getTargetBlockPos() {
            return GuiEditCommandBlockMinecart.this.commandBlockLogic.getPosition();
         }
      };
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 1) {
            this.commandBlockLogic.setTrackOutput(this.trackOutput);
            this.mc.displayGuiScreen((GuiScreen)null);
         } else if (var1.id == 0) {
            PacketBuffer var2 = new PacketBuffer(Unpooled.buffer());
            var2.writeByte(this.commandBlockLogic.getCommandBlockType());
            this.commandBlockLogic.fillInInfo(var2);
            var2.writeString(this.commandField.getText());
            var2.writeBoolean(this.commandBlockLogic.shouldTrackOutput());
            this.mc.getConnection().sendPacket(new CPacketCustomPayload("MC|AdvCmd", var2));
            if (!this.commandBlockLogic.shouldTrackOutput()) {
               this.commandBlockLogic.setLastOutput((ITextComponent)null);
            }

            this.mc.displayGuiScreen((GuiScreen)null);
         } else if (var1.id == 4) {
            this.commandBlockLogic.setTrackOutput(!this.commandBlockLogic.shouldTrackOutput());
            this.updateCommandOutput();
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

      this.commandField.textboxKeyTyped(var1, var2);
      this.previousEdit.textboxKeyTyped(var1, var2);
      this.doneButton.enabled = !this.commandField.getText().trim().isEmpty();
      if (var2 != 28 && var2 != 156) {
         if (var2 == 1) {
            this.actionPerformed(this.cancelButton);
         }
      } else {
         this.actionPerformed(this.doneButton);
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
      this.commandField.mouseClicked(var1, var2, var3);
      this.previousEdit.mouseClicked(var1, var2, var3);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, I18n.format("advMode.setCommand"), this.width / 2, 20, 16777215);
      this.drawString(this.fontRendererObj, I18n.format("advMode.command"), this.width / 2 - 150, 37, 10526880);
      this.commandField.drawTextBox();
      int var4 = 75;
      int var5 = 0;
      this.drawString(this.fontRendererObj, I18n.format("advMode.nearestPlayer"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, I18n.format("advMode.randomPlayer"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, I18n.format("advMode.allPlayers"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, I18n.format("advMode.allEntities"), this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      this.drawString(this.fontRendererObj, "", this.width / 2 - 150, var4 + var5++ * this.fontRendererObj.FONT_HEIGHT, 10526880);
      if (!this.previousEdit.getText().isEmpty()) {
         var4 = var4 + var5 * this.fontRendererObj.FONT_HEIGHT + 16;
         this.drawString(this.fontRendererObj, I18n.format("advMode.previousOutput"), this.width / 2 - 150, var4, 10526880);
         this.previousEdit.drawTextBox();
      }

      super.drawScreen(var1, var2, var3);
   }

   private void updateCommandOutput() {
      if (this.commandBlockLogic.shouldTrackOutput()) {
         this.outputButton.displayString = "O";
         if (this.commandBlockLogic.getLastOutput() != null) {
            this.previousEdit.setText(this.commandBlockLogic.getLastOutput().getUnformattedText());
         }
      } else {
         this.outputButton.displayString = "X";
         this.previousEdit.setText("-");
      }

   }

   public void setCompletions(String... var1) {
      this.tabCompleter.setCompletions(var1);
   }
}
